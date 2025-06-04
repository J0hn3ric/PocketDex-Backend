package org.example.PocketDex.Controller;

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

@Controller
@RequestMapping("/api/cards")
public class CardController {

    @Autowired
    private CardService cardService;

    @GetMapping
    public Mono<ResponseEntity<Object>> getCardsByPagination(
            @RequestParam(defaultValue = "A1-1") String lastSeenId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return cardService.getPaginatedCards(lastSeenId, limit)
                .map(cards -> ResponseEntity.ok((Object) cards))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity
                                .badRequest()
                                .body("Error fetching all cards: " + e.getMessage()))
                );
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<?> getCardById(@PathVariable String cardId) {
        try {
            Card cardReturned = cardService.getCardById(cardId).get();

            return ResponseEntity.ok(cardReturned);
        } catch (Exception e) {
            String errorMsg = "Error fetching Card with id=" + cardId + ": " + e.getMessage();

            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    @GetMapping("/by-expansion")
    public ResponseEntity<?> getCardsByExpansion(
            @RequestParam(required = true) String expansion
    ) {
        try {
            List<Card> cardsReturned = cardService.getCardByExpansion(expansion);

            return ResponseEntity.ok(cardsReturned);
        } catch (Exception e) {
            String errorMsg = "Error fetching all cards: " + e.getMessage();

            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    @GetMapping("/by-pack")
    public ResponseEntity<?> getCardsByPack(
            @RequestParam String pack
    ) {
        try {
            List<Card> cardsReturned = cardService.getCardByPack(pack);

            return ResponseEntity.ok(cardsReturned);
        } catch (Exception e) {
            String errorMsg = "Error fetching all cards: " + e.getMessage();

            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    @GetMapping("/by-rarity")
    public Mono<ResponseEntity<Object>> getCardsByRarity(
            @RequestParam(defaultValue = "A1-1") String lastSeenId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam String rarity
    ) {
        return cardService.getPaginatedCardByRarity(rarity, lastSeenId, limit)
                .map(cards -> ResponseEntity.ok((Object) cards))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity
                                .badRequest().body(e.getMessage()))
                );
    }

    @GetMapping("/by-name")
    public ResponseEntity<?> getCardsByName(
            @RequestParam String name
    ) {
        try {
            List<Card> cardsReturned = cardService.getCardByName(name);

            return ResponseEntity.ok(cardsReturned);
        } catch (Exception e) {
            String errorMsg = "Error fetching all cards: " + e.getMessage();

            return ResponseEntity.badRequest().body(errorMsg);
        }
    }


}
