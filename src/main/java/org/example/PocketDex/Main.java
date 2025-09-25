package org.example.PocketDex;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@SpringBootApplication
@EnableMongoRepositories(basePackages = "org.example.PocketDex.Repository")
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to
        //
        // see how IntelliJ IDEA suggests fixing it.

        // this is for Development ONLY
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        SpringApplication.run(Main.class, args);
    }
}