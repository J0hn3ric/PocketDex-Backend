package org.example.PocketDex.ServiceTests;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.PocketDex.Service.UserMonitorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserMonitorServiceTest {

    @Autowired
    private UserMonitorService userMonitorService;

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
    void userMonitorService_CheckAndDeleteInactiveUsers_WorksAsIntended() {
        try {
            int deleted = userMonitorService.checkAndDeleteInactiveUsers();

            assertEquals(0, deleted);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertFalse(true);
        }
    }

}
