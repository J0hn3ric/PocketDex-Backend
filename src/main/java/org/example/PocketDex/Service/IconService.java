package org.example.PocketDex.Service;

import org.example.PocketDex.Model.Icon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class IconService {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public IconService(
            ReactiveMongoTemplate reactiveMongoTemplate
    ) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<List<Icon>> getAllIcons() {
        Query query = new Query()
                .with(Sort.by(Sort.Direction.ASC, "_id"));

        return reactiveMongoTemplate.find(query, Icon.class)
                .collectList();
    }
}
