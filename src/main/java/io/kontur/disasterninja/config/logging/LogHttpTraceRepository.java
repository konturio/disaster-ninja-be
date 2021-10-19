package io.kontur.disasterninja.config.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;

import java.util.List;

import static java.lang.String.format;

public class LogHttpTraceRepository implements HttpTraceRepository {

    public static final Logger LOG = LoggerFactory.getLogger("httptrace");

    @Override
    public List<HttpTrace> findAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(HttpTrace trace) {
        HttpTrace.Request request = trace.getRequest();
        HttpTrace.Response response = trace.getResponse();

        String message = format("[%s] [%sms] [%s] [%s] [HEADERS: %s] [ADDRESS: %s]",
                response.getStatus(), trace.getTimeTaken(), request.getMethod(), request.getUri(),
                request.getHeaders(), request.getRemoteAddress());

        if (response.getStatus() >= 200 && response.getStatus() < 300) {
            LOG.info(message);
        } else {
            LOG.warn(message);
        }

        if (trace.getTimeTaken() > 30_000){
            LOG.warn("[slow_request] " + message);
        }
    }
}