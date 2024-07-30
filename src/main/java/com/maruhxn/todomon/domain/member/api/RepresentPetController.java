package com.maruhxn.todomon.domain.member.api;

import com.maruhxn.todomon.domain.member.application.RepresentPetService;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/{memberId}/represent-pet")
@RequiredArgsConstructor
public class RepresentPetController {

    private final RepresentPetService representPetService;

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRepresentPet(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestParam("petId") Long petId
    ) {
        representPetService.setRepresentPet(todomonOAuth2User.getId(), petId);
    }
}
