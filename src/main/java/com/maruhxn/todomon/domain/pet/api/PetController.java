package com.maruhxn.todomon.domain.pet.api;

import com.maruhxn.todomon.domain.pet.application.PetQueryService;
import com.maruhxn.todomon.domain.pet.application.PetService;
import com.maruhxn.todomon.domain.pet.dto.request.FeedReq;
import com.maruhxn.todomon.domain.pet.dto.response.PetDexItem;
import com.maruhxn.todomon.domain.pet.dto.response.PetItem;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.common.dto.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final PetQueryService petQueryService;

    @GetMapping
    public DataResponse<List<PetDexItem>> getAllPetTypes() {
        List<PetDexItem> allPetTypes = petQueryService.findAllPetTypes();
        return DataResponse.of("모든 종류 펫 조회 성공", allPetTypes);
    }

    @GetMapping("/my")
    public DataResponse<List<PetItem>> getMyPets(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        List<PetItem> pets = petQueryService.findAllMyPets(todomonOAuth2User.getMember());
        return DataResponse.of("펫 조회 성공", pets);
    }

    @GetMapping("/collections")
    public DataResponse<List<PetDexItem>> getMyCollections(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        List<PetDexItem> collections = petQueryService.findAllMyCollectedPets(todomonOAuth2User.getMember());
        return DataResponse.of("펫 획득 목록 조회 성공", collections);
    }

    @PatchMapping("/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authChecker.isMyPetOrAdmin(#petId)")
    public void feedToPet(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("petId") Long petId,
            @RequestBody FeedReq req
    ) {
        petService.feed(petId, todomonOAuth2User.getMember(), req);
    }

    @DeleteMapping("/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authChecker.isMyPetOrAdmin(#petId)")
    public void deletePet(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("petId") Long petId
    ) {
        petService.deletePet(petId);
    }
}
