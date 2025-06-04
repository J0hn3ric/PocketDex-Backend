package org.example.PocketDex.Service;

import org.example.PocketDex.Model.Expansion;
import org.example.PocketDex.Repository.ExpansionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpansionService {
    private final ExpansionRepository expansionRepository;

    @Autowired
    public ExpansionService(ExpansionRepository expansionRepository) {
        this.expansionRepository = expansionRepository;
    }

    public List<Expansion> getAllExpansions() {
        return this.expansionRepository.findAll();
    }

    public Optional<Expansion> getExpansionById(String id) throws Exception {
        Optional<Expansion> expansion = this.expansionRepository.findById(id);

        if (expansion.equals(Optional.empty())) {
            throw new Exception(
                "Expansion with " +
                id +
                " as Expansion Id, Invalid Expansion Id given"
            );
        }
        return this.expansionRepository.findById(id);
    }
}
