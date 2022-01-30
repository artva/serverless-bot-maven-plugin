package ru.artva.tg.serverless.maven;

import org.apache.http.client.HttpResponseException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MockHttpServletResponse implements HttpServletResponse {

    private int status = 200;
    private final Map<String, String> headers = new HashMap<>();
    private final ByteArrayOutputStream out;
    private final PrintWriter writer;
    private Locale locale;
    private String contentType;
    private String encoding = StandardCharsets.UTF_8.name();

    public MockHttpServletResponse() {
        this.out = new ByteArrayOutputStream();
        this.writer = new PrintWriter(out);
    }

    @Override
    public void addCookie(Cookie cookie) {
        // ignored
    }

    @Override
    public boolean containsHeader(String s) {
        return headers.containsKey(s);
    }

    @Override
    public String encodeURL(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    @Override
    public String encodeRedirectURL(String s) {
        return encodeURL(s);
    }

    @Override
    public String encodeUrl(String s) {
        return encodeURL(s);
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return null;
    }

    @Override
    public void sendError(int i, String s) throws IOException {
        throw new HttpResponseException(i, s);
    }

    @Override
    public void sendError(int i) throws IOException {
        sendError(i, "");
    }

    @Override
    public void sendRedirect(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDateHeader(String s, long l) {
        headers.put(s, String.valueOf(l));
    }

    @Override
    public void addDateHeader(String s, long l) {
        headers.put(s, String.valueOf(l));
    }

    @Override
    public void setHeader(String s, String s1) {
        headers.put(s, s1);
    }

    @Override
    public void addHeader(String s, String s1) {
        headers.put(s, s1);
    }

    @Override
    public void setIntHeader(String s, int i) {
        headers.put(s, String.valueOf(i));
    }

    @Override
    public void addIntHeader(String s, int i) {
        headers.put(s, String.valueOf(i));
    }

    @Override
    public void setStatus(int i) {
        status = i;
    }

    @Override
    public void setStatus(int i, String s) {
        status = i;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getHeader(String s) {
        return headers.get(s);
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return Collections.singleton(headers.get(s));
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public String getCharacterEncoding() {
        return encoding;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {
                out.write(b);
            }
        };
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    @Override
    public void setCharacterEncoding(String s) {
        this.encoding = s;
    }

    @Override
    public void setContentLength(int i) {
        // ignored
    }

    @Override
    public void setContentLengthLong(long l) {
        // ignored
    }

    @Override
    public void setContentType(String s) {
        this.contentType = s;
    }

    @Override
    public void setBufferSize(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBufferSize() {
        return out.size();
    }

    @Override
    public void flushBuffer() throws IOException {
        out.flush();
    }

    @Override
    public void resetBuffer() {
        out.reset();
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {
        status = 200;

    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    public String getBodyString() {
        return out.toString();
    }

}
