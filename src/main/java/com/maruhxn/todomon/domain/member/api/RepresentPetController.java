package com.maruhxn.todomon.domain.member.api;

import com.maruhxn.todomon.domain.member.application.RepresentPetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/{memberId}/represent-pet")
@RequiredArgsConstructor
public class RepresentPetController {

    private final RepresentPetService representPetService;

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authChecker.isMeOrAdmin(#memberId)")
    public void updateRepresentPet(
            @PathVariable Long memberId,
            @RequestParam("petId") Long petId
    ) {
        representPetService.setRepresentPet(memberId, petId);
    }
}
