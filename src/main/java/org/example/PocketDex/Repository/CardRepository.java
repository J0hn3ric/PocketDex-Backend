package org.example.PocketDex.Repository;

import org.example.PocketDex.Rarity;
import org.example.PocketDex.Model.Card;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface CardRepository extends ReactiveMongoRepository<Card, String> {
    Flux<Card> findByExpansion(String expansion);
    Flux<Card> findByPackId(String packId);
    Flux<Card> findByName(String name);
    Flux<Card> findByRarity(Rarity rarity);
}
