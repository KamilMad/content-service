package pl.kamil.content_service.util;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;

public class TokenRelayInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            System.out.println("Relaying token for user: " + jwt.getSubject());
            request.getHeaders().setBearerAuth(jwt.getTokenValue());
        } else {
            System.out.println("DEBUG: No JWT found in SecurityContext! Outgoing request will be anonymous.");
        }
        return execution.execute(request, body);
    }
}
