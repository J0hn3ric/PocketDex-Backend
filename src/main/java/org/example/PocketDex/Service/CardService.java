package org.example.PocketDex.Service;

import org.example.PocketDex.DTO.response.UserCardWithCardInfoResponseDTO;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Rarity;
import org.example.PocketDex.Model.Card;
import org.example.PocketDex.Repository.CardRepository;
import org.example.PocketDex.Utils.MongoConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final int cardLimit = 50;

    @Autowired
    public CardService(
            CardRepository cardRepository,
            ReactiveMongoTemplate reactiveMongoTemplate
    ) {
        this.cardRepository = cardRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<List<Card>> getPaginatedCards(
            String lastSeenId
    ) {
        Query query = new Query()
                .addCriteria(Criteria.where("_id").gte(lastSeenId))
                .limit(cardLimit)
                .with(Sort.by(Sort.Direction.ASC, "_id"));

        return reactiveMongoTemplate
                .find(query, Card.class)
                .collectList();
    }

    public Mono<Card> getCardById(
            String id
    ) {
        return cardRepository.findById(id);
    }

    public Flux<UserCardWithCardInfoResponseDTO> getUserCardsWithInfo(
            List<UserCard> userCards
    ) {
        List<String> cardIdList = userCards.stream()
                .map(UserCard::getCardId)
                .toList();

        return cardRepository.findAllById(cardIdList)
                .collectMap(Card::getId)
                .flatMapMany(cardMap -> Flux.fromIterable(userCards)
                        .map(userCard -> new UserCardWithCardInfoResponseDTO(
                                userCard.getQuantity(),
                                userCard.isTradable(),
                                cardMap.get(userCard.getCardId())
                        ))
                );
    }

    public Mono<List<Card>> getCardByExpansion(
            String expansion
    ) {
        return cardRepository
                .findByExpansion(expansion)
                .collectList();
    }

    public Mono<List<Card>> getCardByPack(
            String pack
    ) {
        return cardRepository
                .findByPackId(pack)
                .collectList();
    }

    public Mono<List<Card>> getCardByName(
            String name
    ) {
        return cardRepository
                .findByName(name)
                .collectList();
    }

    public Mono<List<Card>> getPaginatedCardByRarity(
            String rarityString,
            String lastSeenId
    ) {
        return getPaginatedCardByCriteria(
                MongoConstants.CARD_RARITY,
                rarityString,
                lastSeenId
        );
    }

    public Mono<List<Card>> getPaginatedCardByPack(
            String pack,
            String lastSeenId
    ) {
        return getPaginatedCardByCriteria(
                MongoConstants.CARD_PACK,
                pack,
                lastSeenId
        );
    }

    public Mono<List<Card>> getPaginatedCardByExpansion(
            String expansion,
            String lastSeenId
    ) {
        return getPaginatedCardByCriteria(
                MongoConstants.EXPANSION,
                expansion,
                lastSeenId
        );
    }

    private Mono<List<Card>> getPaginatedCardByCriteria(
            String criteriaKey,
            String criteriaValue,
            String lastSeenId
    ) {
        Query query = new Query()
                .addCriteria(Criteria.where("_id").gt(lastSeenId))
                .addCriteria(Criteria.where(criteriaKey).is(criteriaValue))
                .limit(cardLimit)
                .with(Sort.by(Sort.Direction.ASC, "_id"));

        return reactiveMongoTemplate
                .find(query, Card.class)
                .collectList();
    }
}
