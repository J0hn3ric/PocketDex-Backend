package org.example.PocketDex.Repository;

import org.example.PocketDex.Model.Icon;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IconRepository extends ReactiveMongoRepository<Icon, String> { }
