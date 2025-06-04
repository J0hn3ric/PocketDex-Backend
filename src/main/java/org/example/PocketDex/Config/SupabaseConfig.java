package org.example.PocketDex.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String url;

    @Value("${supabase.api.key}")
    private String apiKey;

    @Bean
    public WebClient supabaseWebClient() {
        return WebClient
                .builder()
                .baseUrl(this.url + "/rest/v1")
                .defaultHeader("Authorization", "Bearer " + this.apiKey)
                .defaultHeader("apikey", this.apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }


}
