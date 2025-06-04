package org.example.PocketDex.Service;

import org.example.PocketDex.Model.Pack;
import org.example.PocketDex.Repository.PackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PackService {
    private final PackRepository packRepository;

    @Autowired
    public PackService(PackRepository packRepository) {
        this.packRepository = packRepository;
    }
    public List<Pack> getAllPacks() {
        return this.packRepository.findAll();
    }

    public List<Pack> getPackWithExpansionId(String expansionId) throws Exception {
        List<Pack> packList = packRepository.findByExpansionId(expansionId);

        if (packList.isEmpty()) {
            throw new Exception(
                    "Packs with " +
                    expansionId +
                    " as expansionId not found, Invalid Expansion Id given"
            );
        }

        return packList;
    }

    public Optional<Pack> getPackWithId(String id) throws Exception {
        Optional<Pack> pack = this.packRepository.findById(id);

        if (pack.equals(Optional.empty())) {
            throw new Exception("Pack not Found, possible error: Invalid Pack Id given");
        }

        return pack;
    }
}
