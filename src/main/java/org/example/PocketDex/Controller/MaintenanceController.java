package org.example.PocketDex.Controller;

import org.example.PocketDex.Service.JWTService;
import org.example.PocketDex.Service.UserMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class MaintenanceController {

    @Autowired
    private UserMonitorService userMonitorService;

    @Autowired
    private JWTService jwtService;

    @PostMapping("/check-inactive-users")
    public ResponseEntity<String> checkInactiveUsers(
        @RequestHeader("Authorization") String authHeader
    ) {
        String key = jwtService.extractToken(authHeader);

        if (!key.equals(System.getenv("INTERNAL_SECRET_KEY"))) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: key given doesn't correspond to internal key");
        }

        int deletedUsers = userMonitorService.checkAndDeleteInactiveUsers();

        return ResponseEntity
                .ok()
                .body("Success: Inactive user check completed. Deleted " + deletedUsers + " users.");
    }


}
