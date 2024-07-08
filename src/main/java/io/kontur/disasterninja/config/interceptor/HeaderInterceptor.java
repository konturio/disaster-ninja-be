package io.kontur.disasterninja.config.interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public class HeaderInterceptor implements ClientHttpRequestInterceptor {

    private List<String> headers;

    public HeaderInterceptor(List<String> headers) {
        // what request headers should be passed from DN-BE further?
        this.headers = headers;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpServletRequest currentRequest = getCurrentHttpRequest();
        if (currentRequest != null) {
            for (String header : headers) {
                String headerValue = currentRequest.getHeader(header);
                if (headerValue != null) {
                    request.getHeaders().add(header, headerValue);
                }
            }
        }
        return execution.execute(request, body);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return sra != null ? sra.getRequest() : null;
    }
}
