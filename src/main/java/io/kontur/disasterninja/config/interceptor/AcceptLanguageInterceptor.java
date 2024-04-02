package io.kontur.disasterninja.config.interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AcceptLanguageInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpServletRequest currentRequest = getCurrentHttpRequest();
        if (currentRequest != null) {
            String acceptLanguage = currentRequest.getHeader("Accept-Language");
            if (acceptLanguage != null) {
                request.getHeaders().add("Accept-Language", acceptLanguage);
            }
        }
        return execution.execute(request, body);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return sra != null ? sra.getRequest() : null;
    }
}