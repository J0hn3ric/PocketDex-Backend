package org.example.PocketDex.Controller;

import org.example.PocketDex.Model.Pack;
import org.example.PocketDex.Service.PackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/api/packs")
public class PackController {

    @Autowired
    private PackService packService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllPacks() {
        try {
            List<Pack> packsReturned = packService.getAllPacks();

            return ResponseEntity.ok(packsReturned);
        } catch (Exception e) {
            String errorMsg = "Error fetching all packs: " + e.getMessage();

            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    @GetMapping("/by-expansion")
    public ResponseEntity<?> getPackByExpansionId(
            @RequestParam String expansionId
    ) {
        try {
            List<Pack> packsReturned = packService.getPackWithExpansionId(expansionId);

            return ResponseEntity.ok(packsReturned);
        } catch (Exception e) {
            String errorMsg = "Error fetching Packs with expansionId=" +
                    expansionId +
                    ": " + e.getMessage();

            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    @GetMapping("/{packId}")
    public ResponseEntity<?> getPackbyId(@PathVariable String packId) {
        try {
            Pack packReturned = packService.getPackWithId(packId).get();

            return ResponseEntity.ok(packReturned);

        } catch (Exception e) {
            String errorMsg = "Error fetching Pack with id=" +
                    packId +
                    ": " + e.getMessage();

            return ResponseEntity.badRequest().body(errorMsg);
        }
    }
}
