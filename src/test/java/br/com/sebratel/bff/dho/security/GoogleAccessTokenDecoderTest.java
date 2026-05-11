package br.com.sebratel.bff.dho.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleAccessTokenDecoderTest {

    private GoogleAccessTokenDecoder decoder;

    @Mock
    private RestTemplate restTemplate;

    private final String issuerUri = "https://accounts.google.com";
    private final String testToken = "ya29.fake-token-for-testing";

    @BeforeEach
    void setUp() {
        decoder = new GoogleAccessTokenDecoder(issuerUri, restTemplate);
    }

    @Test
    void shouldDecodeGoogleAccessToken() {
        // GIVEN
        Map<String, Object> googleResponse = new HashMap<>();
        googleResponse.put("sub", "123456789");
        googleResponse.put("email", "test@sebratel.com.br");
        googleResponse.put("name", "Test User");
        googleResponse.put("picture", "https://example.com/photo.jpg");
        googleResponse.put("email_verified", true);

        String expectedUrl = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + testToken;
        when(restTemplate.getForObject(eq(expectedUrl), eq(Map.class))).thenReturn(googleResponse);

        // WHEN
        Jwt jwt = decoder.decode(testToken);

        // THEN
        assertNotNull(jwt);
        assertEquals(testToken, jwt.getTokenValue());
        assertEquals("test@sebratel.com.br", jwt.getClaimAsString("email"));
        assertEquals("Test User", jwt.getClaimAsString("name"));
        assertEquals("test@sebratel.com.br", jwt.getClaimAsString("sub"));
    }

    @Test
    void shouldFailWhenGoogleReturnsNull() {
        // GIVEN
        String expectedUrl = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + testToken;
        when(restTemplate.getForObject(eq(expectedUrl), eq(Map.class))).thenReturn(null);

        // WHEN & THEN
        assertThrows(org.springframework.security.oauth2.jwt.JwtException.class, () -> {
            decoder.decode(testToken);
        });
    }
}
