package com.user.service.configuration.Interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;


@Configuration
@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Autowired
private OAuth2AuthorizedClientManager manager;

    @Override
    public void apply(RequestTemplate requestTemplate) {
try {
  OAuth2AuthorizedClient auth2AuthorizedClient = manager
            .authorize(OAuth2AuthorizeRequest
                    .withClientRegistrationId("my-internal-client")
                    .principal("token")
                    .build());
  String token = auth2AuthorizedClient.getAccessToken().getTokenValue();
    requestTemplate.header("Authorization","Bearer"+ token);
}
catch(Exception ex){
    System.out.println(ex.getMessage());
}

    }
}
