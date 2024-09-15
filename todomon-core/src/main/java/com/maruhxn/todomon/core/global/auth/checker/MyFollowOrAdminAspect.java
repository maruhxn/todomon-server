package com.maruhxn.todomon.core.global.auth.checker;

import com.maruhxn.todomon.core.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.core.domain.social.domain.Follow;
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
public class MyFollowOrAdminAspect extends AuthAspect {

    private final FollowRepository followRepository;

    @Pointcut("@annotation(com.maruhxn.todomon.core.global.auth.checker.IsMyFollowOrAdmin)")
    public void isMyFollowOrAdminPointcut() {
    }

    @Around("isMyFollowOrAdminPointcut() && args(followId,..)")
    public void checkIsMyFollowOrAdmin(ProceedingJoinPoint joinPoint, Long followId) throws Throwable {
        TodomonOAuth2User todomonOAuth2User = getPrincipal();

        if (followId == null) return;

        Follow follow = followRepository.findOneByIdWithMember(followId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));

        if (!hasAdminAuthority() && isNotMyFollow(follow, todomonOAuth2User)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        joinPoint.proceed();
    }

    private static boolean isNotMyFollow(Follow follow, TodomonOAuth2User todomonOAuth2User) {
        return !follow.getFollowee().getId().equals(todomonOAuth2User.getId());
    }
}
