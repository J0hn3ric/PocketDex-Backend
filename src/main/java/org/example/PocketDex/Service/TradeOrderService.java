package org.example.PocketDex.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

// TODO: implement Trade Features (TradeOrder and TradeOffer)
@Service
public class TradeOrderService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TradeOrderService(@Qualifier("supabaseWebClient") WebClient webClient) { this.webClient = webClient; }

    // input is, userToken and
   /* public Mono<JsonNode> createTradeOrder() {

    }*/

}
