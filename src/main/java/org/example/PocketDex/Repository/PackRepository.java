package org.example.PocketDex.Repository;

import org.example.PocketDex.Model.Pack;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackRepository extends MongoRepository<Pack, String> {

    List<Pack> findByExpansionId(String expansionId);
}
