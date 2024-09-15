package com.maruhxn.todomon.core.global.auth.checker;

import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateAndDeleteTodoQueryParams;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.maruhxn.todomon.core.global.auth.model.Role.ROLE_ADMIN;

@Aspect
@Component
@RequiredArgsConstructor
public class MyTodoOrAdminAspect extends AuthAspect {

    private final TodoRepository todoRepository;
    private final TodoInstanceRepository todoInstanceRepository;

    @Pointcut("@annotation(com.maruhxn.todomon.core.global.auth.checker.IsMyTodoOrAdmin)")
    public void isMyTodoOrAdminPointcut() {
    }

    @Around("isMyTodoOrAdminPointcut() && args(objectId, arg,..)")
    public void checkIsMyTodoOrAdmin(ProceedingJoinPoint joinPoint, Long objectId, Object arg) throws Throwable {
        TodomonOAuth2User todomonOAuth2User = getPrincipal();

        Todo todo = null;
        Boolean isInstance = null;

        if (arg instanceof UpdateAndDeleteTodoQueryParams) {
            isInstance = ((UpdateAndDeleteTodoQueryParams) arg).getIsInstance();
        } else {
            isInstance = (Boolean) arg;
        }

        if (isInstance) {
            TodoInstance todoInstance = todoInstanceRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));

            todo = todoInstance.getTodo();
        } else {
            todo = todoRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
        }

        if (!hasAdminAuthority()
                && isNotMyTodo(todomonOAuth2User, todo)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        joinPoint.proceed();
    }

    private static boolean isNotMyTodo(TodomonOAuth2User todomonOAuth2User, Todo todo) {
        return !Objects.equals(todomonOAuth2User.getId(), todo.getWriter().getId());
    }
    public boolean hasAdminAuthority() {
        return getPrincipal().getAuthorities().contains(new SimpleGrantedAuthority(ROLE_ADMIN.name()));
    }

    public TodomonOAuth2User getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (TodomonOAuth2User) authentication.getPrincipal();
    }
}
