package org.example.PocketDex.Controller;

import org.example.PocketDex.DTO.response.ApiResponseDTO;
import org.example.PocketDex.Model.Card;
import org.example.PocketDex.Service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    @Autowired
    private CardService cardService;

    @GetMapping
    public Mono<ResponseEntity<ApiResponseDTO<List<Card>>>> getAllCardsWithPagination(
            @RequestParam(defaultValue = "A1-001") String lastSeenId,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String expansion,
            @RequestParam(required = false) String packId
    ) {
        return cardService.getPaginatedCards(
                    lastSeenId,
                    rarity,
                    name,
                    expansion,
                    packId
                )
                .map(cards -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(cards, null)
                ))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        new ApiResponseDTO<>(null, "Error fetching cards: " + e.getMessage())
                )));
    }

    @GetMapping("/{cardId}")
    public Mono<ResponseEntity<ApiResponseDTO<Card>>> getCardById(@PathVariable String cardId) {
        return cardService.getCardById(cardId)
                .map(body -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(body, null)
                ))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        new ApiResponseDTO<>(null, "Error fetching cards: " + e.getMessage())
                )));
    }

}
