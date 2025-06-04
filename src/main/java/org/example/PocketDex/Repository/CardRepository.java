package org.example.PocketDex.Repository;

import org.example.PocketDex.Rarity;
import org.example.PocketDex.Model.Card;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends MongoRepository<Card, String> {

    List<Card> findByExpansion(String expansion);
    List<Card> findByPackId(String packId);
    List<Card> findByName(String name);
    List<Card> findByRarity(Rarity rarity);
}
