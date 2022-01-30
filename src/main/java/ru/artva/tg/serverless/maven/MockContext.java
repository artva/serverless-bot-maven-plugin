package ru.artva.tg.serverless.maven;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.Credentials;
import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.fc.runtime.FunctionParam;
import org.apache.maven.plugin.logging.Log;

import java.util.UUID;

public class MockContext implements Context {

    private final Log log;
    private final String handlerName;

    public MockContext(Log log, String handlerName) {
        this.log = log;
        this.handlerName = handlerName;
    }

    @Override
    public String getRequestId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Credentials getExecutionCredentials() {
        return new Credentials() {
            @Override
            public String getAccessKeyId() {
                return "STUB_ACCESS_KEY_ID";
            }

            @Override
            public String getAccessKeySecret() {
                return "STUB_ACCESS_KEY_SECRET";
            }

            @Override
            public String getSecurityToken() {
                return "STUB_SECURITY_TOKEN";
            }
        };
    }

    @Override
    public FunctionParam getFunctionParam() {

        return new FunctionParam() {
            @Override
            public String getFunctionName() {
                return "MAVEN_STUB_FUNCTION";
            }

            @Override
            public String getFunctionHandler() {
                return handlerName;
            }

            @Override
            public String getFunctionInitializer() {
                return "STUB_INITIALIZER";
            }

            @Override
            public int getExecTimeLimitInMillis() {
                return 0;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 0;
            }
        };
    }

    @Override
    public FunctionComputeLogger getLogger() {
        return new FunctionComputeLogger() {
            @Override
            public void trace(String string) {
                log.debug(string);
            }

            @Override
            public void debug(String string) {
                log.debug(string);
            }

            @Override
            public void info(String string) {
                log.info(string);
            }

            @Override
            public void warn(String string) {
                log.warn(string);
            }

            @Override
            public void error(String string) {
                log.error(string);
            }

            @Override
            public void fatal(String string) {
                log.error(string);
            }

            @Override
            public void setLogLevel(Level level) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
