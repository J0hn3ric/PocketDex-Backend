package org.example.PocketDex.Controller;

import org.example.PocketDex.Controller.utils.ControllerUtils;
import org.example.PocketDex.DTO.response.ApiResponseDTO;
import org.example.PocketDex.Model.Icon;
import org.example.PocketDex.Service.IconService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/icons")
public class IconController {

    @Autowired
    private IconService iconService;

    @GetMapping
    public Mono<ResponseEntity<ApiResponseDTO<List<Icon>>>> getAllIcons() {
        return ControllerUtils.toApiResponse(
                iconService.getAllIcons()
        );
    }
}
