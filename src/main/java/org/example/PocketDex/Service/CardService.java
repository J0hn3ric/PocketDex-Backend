package org.example.PocketDex.Service;

import org.example.PocketDex.Rarity;
import org.example.PocketDex.Model.Card;
import org.example.PocketDex.Repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

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

    public Mono<List<Card>> getPaginatedCards(String lastSeenId, int limit) {
        Query query = new Query()
                .addCriteria(Criteria.where("_id").gt(lastSeenId))
                .limit(limit)
                .with(Sort.by(Sort.Direction.ASC, "_id"));

        return reactiveMongoTemplate
                .find(query, Card.class)
                .collectList();
    }

    public Optional<Card> getCardById(String id) throws Exception {
        Optional<Card> card = this.cardRepository.findById(id);

        if (card.equals(Optional.empty())) {
            throw new Exception(
                    "Card with "+
                    id +
                    " as Id not found, Invalid CardId given"
            );
        }

        return card;
    }

    public List<Card> getCardByExpansion(String expansion) throws Exception {
        List<Card> cardList = this.cardRepository.findByExpansion(expansion);

        if (cardList.isEmpty()) {
            throw new Exception(
                    "Cards with "+
                    expansion +
                    " as ExpansionId not found, Invalid ExpansionId given"
            );
        }
        return cardList;
    }

    public List<Card> getCardByPack(String pack) throws Exception {
        List<Card> cardList = this.cardRepository.findByPackId(pack);

        if (cardList.isEmpty()) {
            throw new Exception(
                    "Cards with "+
                    pack +
                    " as PackId not found, Invalid PackId given"
            );
        }
        return cardList;
    }

    public List<Card> getCardByName(String name) throws Exception {
        List<Card> cardList = this.cardRepository.findByName(name);

        if (cardList.isEmpty()) {
            throw new Exception(
                    "Cards with "+
                    name +
                    " as Card name not found, Invalid Card name given"
            );
        }
        return cardList;
    }

    public Mono<List<Card>> getPaginatedCardByRarity(String rarityString, String lastSeenId, int limit) {
        try {
            Rarity rarity = Rarity.fromValue(rarityString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Cards with "+
                            rarityString +
                            " as Rarity not found, Invalid Rarity value given"
            );
        }

        Query query = new Query()
                .addCriteria(Criteria.where("_id").gt(lastSeenId))
                .addCriteria(Criteria.where("card_rarity").is(rarityString))
                .limit(limit)
                .with(Sort.by(Sort.Direction.ASC, "_id"));

        return reactiveMongoTemplate
                .find(query, Card.class)
                .collectList();
    }
}
