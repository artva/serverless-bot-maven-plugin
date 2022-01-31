package ru.artva.tg.serverless.maven;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.HttpRequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.plugin.logging.Log;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WrappingPollingBot extends TelegramLongPollingBot {

    private static final String TELEGRAM_BOT_API_URL = "https://api.telegram.org/bot";

    private final String botName;
    private final String botToken;
    private final HttpRequestHandler httpRequestHandler;
    private final Context context;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    @SuppressWarnings("unchecked")
    public static BotSession start(String handlerClassName, String botName, String botToken, Log log) throws Exception {
        Class<?> handlerClass = Class.forName(handlerClassName);
        HttpRequestHandler handler = (HttpRequestHandler) handlerClass.getConstructor().newInstance();
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        return api.registerBot(new WrappingPollingBot(botName, botToken, handler,
                new MockContext(log, handlerClassName + "::handleRequest")));
    }

    private WrappingPollingBot(String botName, String botToken, HttpRequestHandler handler, Context context) {
        this.botName = botName;
        this.botToken = botToken;
        httpRequestHandler = handler;
        this.context = context;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onUpdateReceived(Update update) {
        try {
            HttpServletRequest request = new MockHttServletRequest(objectMapper.writeValueAsString(update));
            MockHttpServletResponse response = new MockHttpServletResponse();
            httpRequestHandler.handleRequest(request, response, context);
            sendResponse(response);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private HttpEntity sendResponse(MockHttpServletResponse response) {
        try {
            String responseBody = response.getBodyString();
            String responseUrl = String.format("%s%s/%s", TELEGRAM_BOT_API_URL, botToken, getMethod(responseBody));
            HttpPost httpPost = new HttpPost(responseUrl);
            httpPost.setEntity(new StringEntity(responseBody, ContentType.APPLICATION_JSON));
            httpPost.addHeader("charset", StandardCharsets.UTF_8.name());
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                return httpResponse.getEntity();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getMethod(String responseBody) throws JsonProcessingException {
        return objectMapper.readTree(responseBody).at("/method").textValue();
    }
}
