package org.example.PocketDex.Service;

import org.example.PocketDex.Model.Icon;
import org.example.PocketDex.Repository.IconRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class IconService {

    private final IconRepository iconRepository;

    @Autowired
    public IconService(IconRepository iconRepository) {
        this.iconRepository = iconRepository;
    }

    public Flux<Icon> getAllIcons() {
        return iconRepository.findAll();
    }
}
