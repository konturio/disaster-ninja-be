package io.kontur.disasterninja.config.interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class UserLanguageInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpServletRequest currentRequest = getCurrentHttpRequest();
        if (currentRequest != null) {
            String userLanguage = currentRequest.getHeader("User-Language");
            if (userLanguage != null) {
                request.getHeaders().add("User-Language", userLanguage);
            }
        }
        return execution.execute(request, body);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return sra != null ? sra.getRequest() : null;
    }
}