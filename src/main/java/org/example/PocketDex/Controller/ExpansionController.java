package org.example.PocketDex.Controller;

import org.example.PocketDex.Model.Expansion;
import org.example.PocketDex.Service.ExpansionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/expansions")
public class ExpansionController {

    @Autowired
    private ExpansionService expansionService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllExpansions() {
        try {
            List<Expansion> expansionsReturned = expansionService.getAllExpansions();

            return ResponseEntity.ok(expansionsReturned);
        } catch (Exception e) {
            String errorMsg = "Error fetching all expansions: " + e.getMessage();

            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    @GetMapping("/{expansionId}")
    public ResponseEntity<?> getExpansionById(
            @PathVariable String expansionId
    ) {
        try {
            Expansion expansionReturned = expansionService.getExpansionById(expansionId).get();

            return ResponseEntity.ok(expansionReturned);
        } catch (Exception e) {
            String errorMsg = "Error fetching expansion with id=" +
                    expansionId +
                    ": " + e.getMessage();

            return ResponseEntity.badRequest().body(errorMsg);
        }
    }
}
