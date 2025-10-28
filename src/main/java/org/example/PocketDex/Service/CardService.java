package org.example.PocketDex.Service;

import com.mongodb.lang.Nullable;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public CardService(
            CardRepository cardRepository,
            ReactiveMongoTemplate reactiveMongoTemplate
    ) {
        this.cardRepository = cardRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<List<Card>> getPaginatedCards(
            String lastSeenId,
            @Nullable String rarity,
            @Nullable String name,
            @Nullable String expansion,
            @Nullable String packId
    ) {
        Criteria criteria = Criteria.where("_id").gte(lastSeenId);
        criteria = addNameCriteriaIfNameIsNotEmptyOrNull(criteria, name);
        criteria = addParameterCriteriaIfParameterNotEmptyOrNull(
                criteria,
                MongoConstants.CARD_RARITY,
                rarity
        );
        criteria = addParameterCriteriaIfParameterNotEmptyOrNull(
                criteria,
                MongoConstants.CARD_PACK,
                packId
        );
        criteria = addParameterCriteriaIfParameterNotEmptyOrNull(
                criteria,
                MongoConstants.EXPANSION,
                expansion
        );

        int cardLimit = 50;
        Query query = new Query(criteria)
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
                        .filter(userCard -> cardMap.get(userCard.getCardId()) != null)
                        .map(userCard -> new UserCardWithCardInfoResponseDTO(
                                    userCard.getQuantity(),
                                    userCard.isTradable(),
                                    cardMap.get(userCard.getCardId())
                        ))
                );
    }

    private Criteria addNameCriteriaIfNameIsNotEmptyOrNull(
            Criteria criteria,
            @Nullable String name
    ) {
        if (name != null && !name.isEmpty()) {
            criteria = criteria.and("name").regex(
                    "^" + Pattern.quote(name) + ".*",
                    "i"
            );
        }
        return criteria;
    }

    private Criteria addParameterCriteriaIfParameterNotEmptyOrNull(
            Criteria criteria,
            String parameterKey,
            @Nullable String parameterValue
    ) {
        if (parameterValue != null && !parameterValue.isEmpty()) {
            criteria = criteria.and(parameterKey).is(parameterValue);
        }

        return criteria;
    }
}
