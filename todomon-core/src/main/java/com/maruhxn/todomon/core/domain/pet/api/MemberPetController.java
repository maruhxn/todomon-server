package com.maruhxn.todomon.core.domain.pet.api;

import com.maruhxn.todomon.core.domain.pet.application.PetQueryService;
import com.maruhxn.todomon.core.domain.pet.dto.response.PetDexItem;
import com.maruhxn.todomon.core.domain.pet.dto.response.PetInfoDto;
import com.maruhxn.todomon.core.global.common.dto.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 다른 유저와 관련된 데이터를 조회하거나 액세스하는 API
@RestController
@RequestMapping("/api/members/{memberId}/pets")
@RequiredArgsConstructor
public class MemberPetController {

    private final PetQueryService petQueryService;

    @GetMapping
    public DataResponse<PetInfoDto> getPetInfo(
            @PathVariable Long memberId
    ) {
        PetInfoDto petInfoDto = petQueryService.findAllOwnPets(memberId);
        return DataResponse.of("소유 펫 정보 조회 성공", petInfoDto);
    }

    @GetMapping("/collections")
    public DataResponse<List<PetDexItem>> getPetCollections(
            @PathVariable Long memberId
    ) {
        List<PetDexItem> collections = petQueryService.findAllOwnCollectedPets(memberId);
        return DataResponse.of("펫 획득 목록 조회 성공", collections);
    }

}
