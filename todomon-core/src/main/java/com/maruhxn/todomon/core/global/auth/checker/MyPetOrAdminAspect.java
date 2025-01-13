package com.maruhxn.todomon.core.global.auth.checker;

import com.maruhxn.todomon.core.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class MyPetOrAdminAspect extends AuthAspect {

    private final PetRepository petRepository;

    @Pointcut("@annotation(com.maruhxn.todomon.core.global.auth.checker.IsMyPetOrAdmin)")
    public void isMyPetOrAdminPointcut() {
    }

    @Around("isMyPetOrAdminPointcut() && args(memberId, petId,..)")
    public void checkIsMyPetOrAdmin(ProceedingJoinPoint joinPoint, Long memberId, Long petId) throws Throwable {
        TodomonOAuth2User todomonOAuth2User = getPrincipal();

        if (memberId == null || petId == null) return;

        Pet findPet = petRepository.findOneByIdWithMember(petId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));

        if (!hasAdminAuthority() && isNotMyPet(findPet, todomonOAuth2User)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        joinPoint.proceed();
    }

    private static boolean isNotMyPet(Pet findPet, TodomonOAuth2User todomonOAuth2User) {
        return !findPet.getMember().getId().equals(todomonOAuth2User.getId());
    }
}
