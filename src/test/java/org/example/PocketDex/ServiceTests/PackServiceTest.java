package org.example.PocketDex.ServiceTests;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.PocketDex.Model.Pack;
import org.example.PocketDex.Repository.PackRepository;
import org.example.PocketDex.Service.PackService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PackServiceTest {

    @Autowired
    private PackRepository packRepository;

    @Autowired
    private PackService packService;

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
    void packService_GetAllPacks_LengthOfListIsCorrect() {
        int numberOfDocuments = 7;

        List<Pack> packList = packService.getAllPacks();

        assertEquals(numberOfDocuments, packList.size());
    }

    @Test
    void packService_GetPackByCorrectId_PackInstantiatedCorrectly() throws Exception {
        String id = "A1-Charizard_Pack";

        Pack expectedPack = new Pack(
                id,
                "Charizard_Pack",
                "https://storage.googleapis.com/pocketdeximages.firebasestorage.app/Pack_imgs/A1-Charizard_Pack.png",
                "Genetic Apex",
                "A1"
        );

        try {
            Optional<Pack> packQueried = packService.getPackWithId(id);

            assertAll(
                    () -> assertInstanceOf(Pack.class, packQueried.get()),
                    () -> assertEquals(expectedPack.getId(), packQueried.get().getId()),
                    () -> assertEquals(expectedPack.getPackName(), packQueried.get().getPackName()),
                    () -> assertEquals(expectedPack.getPackRes(), packQueried.get().getPackRes()),
                    () -> assertEquals(expectedPack.getExpansion(), packQueried.get().getExpansion()),
                    () -> assertEquals(expectedPack.getExpansionId(), packQueried.get().getExpansionId())
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void packService_GetPackByInvalidId_ReturnsEmpty() {
        String id = "InavlidId";

        assertThrows(
            Exception.class,
            () -> packService.getPackWithId(id)
        );
    }

    @Test
    void packService_GetPackWithValidExpansionId_LengthOfListIsCorrect() {
        int numberOfPacksExpected = 3;
        String expansionId = "A1";

        try {
            List<Pack> packList =packService.getPackWithExpansionId(expansionId);

            assertAll(
                    () -> assertEquals(numberOfPacksExpected, packList.size()),
                    () ->
                        packList.forEach(p ->
                            assertEquals(expansionId, p.getExpansionId())
                        )
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void packService_GetPackWithInvaliExpansionId_ThrowsAnException() {
        String invalidExpansionId = "InvalidId";

        assertThrows(
                Exception.class,
                () -> packService.getPackWithExpansionId(invalidExpansionId)
        );
    }
}
