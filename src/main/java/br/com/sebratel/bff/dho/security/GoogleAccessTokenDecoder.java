package br.com.sebratel.bff.dho.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

@Slf4j
@RequiredArgsConstructor
public class GoogleAccessTokenDecoder implements JwtDecoder {

    private final String issuerUri;
    private final RestTemplate restTemplate;
    private JwtDecoder delegate;

    @Override
    public Jwt decode(String token) throws JwtException {
        // Tenta decodificar como JWT primeiro
        try {
            if (delegate == null) {
                log.info("Inicializando NimbusJwtDecoder com issuer: {} e clock skew de 60s", issuerUri);
                NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
                
                OAuth2TokenValidator<Jwt> withClockSkew = new DelegatingOAuth2TokenValidator<>(
                    new JwtTimestampValidator(Duration.ofSeconds(60)),
                    new JwtIssuerValidator(issuerUri)
                );
                
                jwtDecoder.setJwtValidator(withClockSkew);
                delegate = jwtDecoder;
            }
            return delegate.decode(token);
        } catch (Exception e) {
            log.warn("Falha ao decodificar como JWT: {}. Mensagem: {}", 
                token.substring(0, Math.min(token.length(), 15)) + "...", e.getMessage());
            
            // Se falhar e parecer um token do Google (ya29.), tenta validar como Access Token
            if (token.startsWith("ya29.")) {
                log.info("Token identificado como Google Access Token (ya29.), tentando validação via UserInfo API");
                return decodeAccessToken(token);
            }
            throw new JwtException("Token inválido: " + e.getMessage(), e);
        }
    }

    private Jwt decodeAccessToken(String token) {
        try {
            String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
            
            // Adiciona o token no Header de Autorização para a chamada ao Google
            Map<String, Object> response = restTemplate.getForObject(
                    userInfoUrl + "?access_token=" + token, 
                    Map.class
            );

            if (response == null || !response.containsKey("email")) {
                throw new JwtException("Não foi possível recuperar informações do usuário do Google");
            }

            Map<String, Object> claims = new HashMap<>(response);
            Map<String, Object> headers = new HashMap<>();
            headers.put("alg", "none");

            // Adiciona claims essenciais para o Spring Security manter a consistência
            String email = (String) response.get("email");
            claims.put("sub", email);
            claims.put("iss", "https://accounts.google.com");
            
            Instant now = Instant.now();
            return new Jwt(
                    token,
                    now,
                    now.plusSeconds(3600),
                    headers,
                    claims
            );
        } catch (Exception e) {
            log.error("Erro ao validar Access Token do Google", e);
            throw new JwtException("Erro ao validar Access Token: " + e.getMessage());
        }
    }
}
