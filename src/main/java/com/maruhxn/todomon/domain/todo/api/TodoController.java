package com.maruhxn.todomon.domain.todo.api;

import com.maruhxn.todomon.domain.todo.application.TodoQueryService;
import com.maruhxn.todomon.domain.todo.application.TodoService;
import com.maruhxn.todomon.domain.todo.dto.request.CreateTodoReq;
import com.maruhxn.todomon.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.domain.todo.dto.request.UpdateTodoStatusReq;
import com.maruhxn.todomon.domain.todo.dto.response.TodoItem;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.common.dto.response.BaseResponse;
import com.maruhxn.todomon.global.common.dto.response.DataResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;
    private final TodoQueryService todoQueryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse createTodo(
            @Valid @RequestBody CreateTodoReq req,
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        todoService.create(todomonOAuth2User.getMember(), req);
        return new BaseResponse("투두 생성 성공");
    }

    @GetMapping("/day")
    public DataResponse<List<TodoItem>> getTodoByDay(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestParam(required = false, name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<TodoItem> todos = todoQueryService.getTodosByDay(
                date == null
                        ? LocalDate.now()
                        : date, todomonOAuth2User.getMember()
        );
        return DataResponse.of("일별 조회 성공", todos);
    }

    @GetMapping("/week")
    public DataResponse<List<TodoItem>> getTodoByWeek(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestParam(required = false, name = "startOfWeek") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startOfWeek
    ) {
        List<TodoItem> todos = todoQueryService.getTodosByWeek(
                startOfWeek == null
                        ? LocalDate.now()
                        : startOfWeek, todomonOAuth2User.getMember()
        );
        return DataResponse.of("주별 조회 성공", todos);
    }

    @GetMapping("/month")
    public DataResponse<List<TodoItem>> getTodoByMonth(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestParam(required = false, name = "yearMonth") @DateTimeFormat(pattern = "yyyy-MM") String yearMonth
    ) {
        List<TodoItem> todos = todoQueryService.getTodosByMonth(
                yearMonth == null
                        ? YearMonth.now()
                        : YearMonth.parse(yearMonth), todomonOAuth2User.getMember()
        );
        return DataResponse.of("월별 조회 성공", todos);
    }

    @PatchMapping("/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTodo(
            @PathVariable("todoId") Long todoId,
            @RequestBody UpdateTodoReq req
    ) {
        todoService.update(todoId, req);
    }

    /**
     * Todo의 완료 여부를 업데이트한다.
     *
     * @param todoId
     * @param req
     */
    @PatchMapping("/{todoId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTodoStatus(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("todoId") Long todoId,
            @RequestBody UpdateTodoStatusReq req
    ) {
        todoService.updateStatusAndReward(todoId, todomonOAuth2User.getMember(), req);
    }

    @DeleteMapping("/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTodo(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("todoId") Long todoId
    ) {
        todoService.deleteTodo(todoId);
    }
}
