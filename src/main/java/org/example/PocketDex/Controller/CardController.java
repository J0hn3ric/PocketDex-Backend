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
            @RequestParam(defaultValue = "A1-001") String lastSeenId
    ) {
        return cardService.getPaginatedCards(lastSeenId)
                .map(cards -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(cards, null)
                ))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        new ApiResponseDTO<>(null, "Error fetching cards: " + e.getMessage())
                )));
    }

    @GetMapping("/by-rarity")
    public Mono<ResponseEntity<ApiResponseDTO<List<Card>>>> getCardsByRarityWithPagination(
            @RequestParam String rarityString,
            @RequestParam(defaultValue = "A1-001") String lastSeenId
    ) {
        return cardService.getPaginatedCardByRarity(rarityString, lastSeenId)
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

    @GetMapping("/by-expansion")
    public Mono<ResponseEntity<ApiResponseDTO<List<Card>>>> getCardsByExpansionWithPagination(
            @RequestParam(required = true) String expansion,
            @RequestParam(required = true) String lastSeenId
    ) {
        return cardService.getPaginatedCardByExpansion(expansion, lastSeenId)
                .map(cards -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(cards, null)
                ))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        new ApiResponseDTO<>(null, "Error fetching cards: " + e.getMessage())
                )));
    }

    @GetMapping("/by-pack")
    public Mono<ResponseEntity<ApiResponseDTO<List<Card>>>> getCardsByPackWithPagination(
            @RequestParam(required = true) String packId,
            @RequestParam(required = true) String lastSeenId
    ) {
        return cardService.getPaginatedCardByPack(packId, lastSeenId)
                .map(cards -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(cards, null)
                ))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        new ApiResponseDTO<>(null, "Error fetching cards: " + e.getMessage())
                )));
    }

    @GetMapping("/by-expansion-all")
    public Mono<ResponseEntity<ApiResponseDTO<List<Card>>>> getCardsByExpansion(
            @RequestParam(required = true) String expansion
    ) {
        return cardService.getCardByExpansion(expansion)
                .map(cards -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(cards, null)
                ))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        new ApiResponseDTO<>(null, "Error fetching cards: " + e.getMessage())
                )));
    }

    @GetMapping("/by-pack-all")
    public Mono<ResponseEntity<ApiResponseDTO<List<Card>>>> getCardsByPack(
            @RequestParam String pack
    ) {
        return cardService.getCardByPack(pack)
                .map(cards -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(cards, null)
                ))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        new ApiResponseDTO<>(null, "Error fetching cards: " + e.getMessage())
                )));
    }

    @GetMapping("/by-rarity-all")
    public Mono<ResponseEntity<ApiResponseDTO<List<Card>>>> getCardsByRarity(
            @RequestParam(defaultValue = "A1-001") String lastSeenId,
            @RequestParam String rarity
    ) {
        return cardService.getPaginatedCardByRarity(rarity, lastSeenId)
                .map(cards -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(cards, null)
                ))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        new ApiResponseDTO<>(null, "Error fetching cards: " + e.getMessage())
                )));
    }

    @GetMapping("/by-name")
    public Mono<ResponseEntity<ApiResponseDTO<List<Card>>>> getCardsByName(
            @RequestParam String name
    ) {
        return cardService.getCardByName(name)
                .map(cards -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(cards, null)
                ))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        new ApiResponseDTO<>(null, "Error fetching cards: " + e.getMessage())
                )));
    }


}
