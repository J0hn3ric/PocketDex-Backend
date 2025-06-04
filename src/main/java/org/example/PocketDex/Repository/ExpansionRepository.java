package org.example.PocketDex.Repository;

import org.example.PocketDex.Model.Expansion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpansionRepository extends MongoRepository<Expansion, String> {
}
