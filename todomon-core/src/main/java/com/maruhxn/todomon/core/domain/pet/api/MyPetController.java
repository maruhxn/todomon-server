package com.maruhxn.todomon.core.domain.pet.api;

import com.maruhxn.todomon.core.domain.pet.application.PetService;
import com.maruhxn.todomon.core.domain.pet.application.RepresentPetService;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.common.dto.response.BaseResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 로그인한 유저 자신만 사용하는 API
@RestController
@RequestMapping("/api/pets/my")
@RequiredArgsConstructor
public class MyPetController {

    private final PetService petService;
    private final RepresentPetService representPetService;

    @PostMapping
    public BaseResponse createPet(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        petService.create(todomonOAuth2User.getId());
        return new BaseResponse("펫 생성 성공");
    }

    @PatchMapping("/represent-pet")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authChecker.isMyPetOrAdmin(#petId)")
    public void updateRepresentPet(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestParam(required = false) Long petId
    ) {
        representPetService.setRepresentPet(todomonOAuth2User.getId(), petId);
    }

    @PatchMapping("/{petId}/feed")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authChecker.isMyPetOrAdmin(#petId)")
    public void feedToPet(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("petId") Long petId,
            @RequestParam(required = true) @Valid @Min(value = 1, message = "1개 이상의 먹이 수를 입력해주세요") Long foodCnt
    ) {
        petService.feed(petId, todomonOAuth2User.getMember(), foodCnt);
    }

    @DeleteMapping("/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authChecker.isMyPetOrAdmin(#petId)")
    public void deletePet(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("petId") Long petId
    ) {
        petService.deletePet(todomonOAuth2User.getId(), petId);
    }

}
