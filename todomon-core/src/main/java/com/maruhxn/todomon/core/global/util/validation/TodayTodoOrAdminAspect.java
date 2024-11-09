package com.maruhxn.todomon.core.global.util.validation;

import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateAndDeleteTodoQueryParams;
import com.maruhxn.todomon.core.global.auth.checker.AuthAspect;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Aspect
@Component
@RequiredArgsConstructor
public class TodayTodoOrAdminAspect extends AuthAspect {

    private final TodoRepository todoRepository;
    private final TodoInstanceRepository todoInstanceRepository;

    @Pointcut("@annotation(com.maruhxn.todomon.core.global.util.validation.IsTodayTodoOrAdmin)")
    public void IsTodayTodoOrAdminPointcut() {
    }

    @Around("IsTodayTodoOrAdminPointcut() && args(objectId, arg,..)")
    public void checkIsTodayTodoOrAdmin(ProceedingJoinPoint joinPoint, Long objectId, Object arg) throws Throwable {

        LocalDate date;
        Boolean isInstance = null;

        if (arg instanceof UpdateAndDeleteTodoQueryParams) {
            isInstance = ((UpdateAndDeleteTodoQueryParams) arg).getIsInstance();
        } else {
            isInstance = (Boolean) arg;
        }

        if (isInstance) {
            TodoInstance todoInstance = todoInstanceRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
            date = todoInstance.getStartAt().toLocalDate();
        } else {
            Todo todo = todoRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
            date = todo.getStartAt().toLocalDate();
        }

        if (!hasAdminAuthority()
                && isNotTodayTodo(date)) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "오늘에 해당하는 할 일만 수정이 가능합니다.");
        }

        joinPoint.proceed();
    }

    private boolean isNotTodayTodo(LocalDate startAtDate) {
        LocalDate today = LocalDate.now();

        return !startAtDate.isEqual(today);
    }

}
