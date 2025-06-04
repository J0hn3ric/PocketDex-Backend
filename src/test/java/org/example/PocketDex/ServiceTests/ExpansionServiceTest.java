package org.example.PocketDex.ServiceTests;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.PocketDex.Model.Expansion;
import org.example.PocketDex.Repository.ExpansionRepository;
import org.example.PocketDex.Service.ExpansionService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ExpansionServiceTest {

    @Autowired
    private ExpansionRepository expansionRepository;

    @Autowired
    private ExpansionService expansionService;

    @BeforeAll
    static void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }

    @Test
    void expansionService_GetAll_ExpansionListLengthCorrect() {
        int numberOfDocuments = 7;

        List<Expansion> allExpansions = expansionService.getAllExpansions();

        assertEquals(numberOfDocuments, allExpansions.size());
    }

    @Test
    void expansionService_GetExpansionByIdWithValidId_ExpansionInstantiatedCorrectly() {
        String id = "A3";

        Expansion expectedExpansion = new Expansion(
                id,
                "Celestial Guardians",
                "https://storage.googleapis.com/pocketdeximages.firebasestorage.app/Expansion_imgs/celestialguardians.png"
        );

        try {
            Optional<Expansion> expansionQueried = expansionService.getExpansionById(id);

            assertAll(
                    () -> assertInstanceOf(Expansion.class, expansionQueried.get()),
                    () -> assertEquals(expectedExpansion.getId(), expansionQueried.get().getId()),
                    () -> assertEquals(expectedExpansion.getName(), expansionQueried.get().getName()),
                    () -> assertEquals(expectedExpansion.getRes(), expansionQueried.get().getRes())
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void expansionService_GetExpansionByIdWithInvalidId_ServiceReturnsEmpytOptional() {
        String id = "invalidId";

        assertThrows(
                Exception.class,
                () -> expansionService.getExpansionById(id)
        );
    }
}
