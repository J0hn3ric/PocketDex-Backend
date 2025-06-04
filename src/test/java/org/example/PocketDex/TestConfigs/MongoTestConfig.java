package org.example.PocketDex.TestConfigs;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.PocketDex.Config.RarityReadConverter;
import org.example.PocketDex.Config.RarityWriteConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.ArrayList;
import java.util.List;

@TestConfiguration
@EnableMongoRepositories(basePackages = "org.example.PocketDex.mongo.Repository")
public class MongoTestConfig extends AbstractMongoClientConfiguration{
    Dotenv dotenv = Dotenv.configure().load();

    @Override
    protected String getDatabaseName() {
        return dotenv.get("MONGO_DB"); // Set to your actual database
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<Converter<?, ?>>();
        converterList.add(new RarityReadConverter());
        converterList.add(new RarityWriteConverter());
        return new MongoCustomConversions(converterList);
    }

    @Bean
    public MongoClient mongoClient() {

        return MongoClients.create(dotenv.get("MONGO_URL"));
    }

    @Bean
    public MongoTemplate mongoTemplate() {

        return new MongoTemplate(mongoClient(), dotenv.get("MONGO_DB"));
    }
}