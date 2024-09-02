package com.maruhxn.todomon.core.domain.pet.api;

import com.maruhxn.todomon.core.domain.pet.application.PetQueryService;
import com.maruhxn.todomon.core.domain.pet.dto.response.PetDexItem;
import com.maruhxn.todomon.core.global.common.dto.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetQueryService petQueryService;

    @GetMapping
    public DataResponse<List<PetDexItem>> getAllPetTypes() {
        List<PetDexItem> allPetTypes = petQueryService.findAllPetTypes();
        return DataResponse.of("모든 종류 펫 조회 성공", allPetTypes);
    }
}
