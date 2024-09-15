package com.maruhxn.todomon.core.global.auth.checker;

import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor
public class MeOrAdminAspect extends AuthAspect {

    @Pointcut("@annotation(com.maruhxn.todomon.core.global.auth.checker.IsMeOrAdmin)")
    public void isMeOrAdminPointcut() {
    }

    @Around("isMeOrAdminPointcut() && args(memberId,..)")
    public void checkIsMeOrAdmin(ProceedingJoinPoint joinPoint, Long memberId) throws Throwable {
        TodomonOAuth2User todomonOAuth2User = getPrincipal();

        if (!hasAdminAuthority() && isNotMe(memberId, todomonOAuth2User)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        joinPoint.proceed();
    }

    private static boolean isNotMe(Long memberId, TodomonOAuth2User todomonOAuth2User) {
        return !Objects.equals(todomonOAuth2User.getId(), memberId);
    }
}
