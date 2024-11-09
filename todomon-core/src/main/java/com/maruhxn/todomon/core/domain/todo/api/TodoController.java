package com.maruhxn.todomon.core.domain.todo.api;

import com.maruhxn.todomon.core.domain.todo.application.TodoQueryService;
import com.maruhxn.todomon.core.domain.todo.application.TodoService;
import com.maruhxn.todomon.core.domain.todo.dto.request.CreateTodoReq;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateAndDeleteTodoQueryParams;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoStatusReq;
import com.maruhxn.todomon.core.domain.todo.dto.response.TodoItem;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.common.dto.response.BaseResponse;
import com.maruhxn.todomon.core.global.common.dto.response.DataResponse;
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
            @RequestBody @Valid CreateTodoReq req,
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        todoService.create(todomonOAuth2User.getId(), req);
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
                        : date, todomonOAuth2User.getId()
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
                        : startOfWeek, todomonOAuth2User.getId()
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
                        : YearMonth.parse(yearMonth), todomonOAuth2User.getId()
        );
        return DataResponse.of("월별 조회 성공", todos);
    }

    /**
     * 어떤 투두(단일 투두 혹은 인스턴스)에 대한 수정 요청
     * 단일 투두일 경우 -> 그냥 수정하면 됨. 만약 반복 정보가 들어오면 인스턴스 생성까지
     * 인스턴스일 경우 -> '이 할 일', '이번 및 향후 할 일', '모든 할 일' 중 한 가지 선택 필요
     */
    @PatchMapping("/{objectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @PreAuthorize("@authChecker.isMyTodoOrAdmin(#objectId, #params.isInstance)")
    public void updateTodo(
            @PathVariable Long objectId,
            @ModelAttribute @Valid UpdateAndDeleteTodoQueryParams params,
            @RequestBody @Valid UpdateTodoReq req
    ) {
        todoService.update(objectId, params, req);
    }

    /**
     * Todo의 완료 여부를 업데이트한다.
     *
     * @param objectId
     * @param req
     */
    @PatchMapping("/{objectId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @PreAuthorize("@authChecker.isMyTodoOrAdmin(#objectId, #isInstance)")
    public void updateTodoStatus(
            @PathVariable Long objectId,
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestParam(required = true) boolean isInstance,
            @RequestBody @Valid UpdateTodoStatusReq req
    ) {
        todoService.updateStatusAndReward(objectId, isInstance, todomonOAuth2User.getId(), req);
    }

    @DeleteMapping("/{objectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @PreAuthorize("@authChecker.isMyTodoOrAdmin(#objectId, #params.isInstance)")
    public void deleteTodo(
            @PathVariable Long objectId,
            @Valid UpdateAndDeleteTodoQueryParams params
    ) {
        todoService.deleteTodo(objectId, params);
    }
}
