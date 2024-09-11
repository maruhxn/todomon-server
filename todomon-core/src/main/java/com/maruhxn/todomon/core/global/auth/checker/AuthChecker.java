package com.maruhxn.todomon.core.global.auth.checker;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.core.domain.social.domain.Follow;
import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.maruhxn.todomon.core.global.auth.model.Role.ROLE_ADMIN;

@Component("authChecker")
@RequiredArgsConstructor
public class AuthChecker {

    private final PetRepository petRepository;
    private final TodoRepository todoRepository;
    private final TodoInstanceRepository todoInstanceRepository;
    private final FollowRepository followRepository;

    public boolean isMeOrAdmin(Long memberId) {
        TodomonOAuth2User todomonOAuth2User = getPrincipal();

        if (!hasAdminAuthority()
                && !Objects.equals(todomonOAuth2User.getId(), memberId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        return true;
    }

    public boolean isMyPetOrAdmin(Long petId) {
        if (petId == null) return true;

        TodomonOAuth2User todomonOAuth2User = getPrincipal();

        Pet findPet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));

        if (!hasAdminAuthority()
                && !Objects.equals(todomonOAuth2User.getId(), findPet.getMember().getId())) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        return true;
    }

    public boolean isMyTodoOrAdmin(Long objectId, boolean isInstance) {
        TodomonOAuth2User todomonOAuth2User = getPrincipal();

        Todo todo = null;

        if (isInstance) {
            TodoInstance todoInstance = todoInstanceRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));

            todo = todoInstance.getTodo();
        } else {
            todo = todoRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
        }

        if (!hasAdminAuthority()
                && !Objects.equals(todomonOAuth2User.getId(), todo.getWriter().getId())) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        return true;
    }

    public boolean isMyFollowOrAdmin(Long followId) {
        TodomonOAuth2User todomonOAuth2User = getPrincipal();

        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));

        if (!hasAdminAuthority() &&
                !Objects.equals(follow.getFollowee().getId(), todomonOAuth2User.getId())) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        return true;
    }

    /**
     * 프리미엄 아이템을 사용 혹은 구매 시 멤버는 유료 플랜을 구독 중이어야 한다.
     *
     * @param member
     * @param item
     */
    public void isPremiumButNotSubscribing(Member member, Item item) {
        if (item.getIsPremium() && !member.isSubscribed()) {
            throw new ForbiddenException(ErrorCode.NOT_SUBSCRIPTION);
        }
    }

    private boolean hasAdminAuthority() {
        return getPrincipal().getAuthorities().contains(new SimpleGrantedAuthority(ROLE_ADMIN.name()));
    }

    private static TodomonOAuth2User getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (TodomonOAuth2User) authentication.getPrincipal();
    }

}
