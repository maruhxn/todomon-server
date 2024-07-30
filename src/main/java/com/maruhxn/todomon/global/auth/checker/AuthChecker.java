package com.maruhxn.todomon.global.auth.checker;

import com.maruhxn.todomon.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.domain.todo.domain.Todo;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.maruhxn.todomon.global.auth.model.Role.ROLE_ADMIN;

@Component("authChecker")
@RequiredArgsConstructor
public class AuthChecker {

    private final PetRepository petRepository;
    private final TodoRepository todoRepository;

    public boolean isMeOrAdmin(Long memberId) {
        TodomonOAuth2User todomonOAuth2User = getPrincipal();

        if (!hasAdminAuthority()
                && !Objects.equals(todomonOAuth2User.getId(), memberId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        return true;
    }

    public boolean isMyPetOrAdmin(Long petId) {
        TodomonOAuth2User todomonOAuth2User = getPrincipal();

        Pet findPet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));

        if (!hasAdminAuthority()
                && !Objects.equals(todomonOAuth2User.getId(), findPet.getMember().getId())) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        return true;
    }

    public boolean isMyTodoOrAdmin(Long todoId) {
        TodomonOAuth2User todomonOAuth2User = getPrincipal();

        Todo findTodo = todoRepository.findById(todoId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));

        if (!hasAdminAuthority()
                && !Objects.equals(todomonOAuth2User.getId(), findTodo.getWriter().getId())) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        return true;
    }

    private boolean hasAdminAuthority() {
        return getPrincipal().getAuthorities().contains(new SimpleGrantedAuthority(ROLE_ADMIN.name()));
    }

    private static TodomonOAuth2User getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (TodomonOAuth2User) authentication.getPrincipal();
    }

}
