package com.maruhxn.todomon.domain.todo.api;

import com.maruhxn.todomon.domain.todo.domain.Frequency;
import com.maruhxn.todomon.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.domain.todo.domain.Todo;
import com.maruhxn.todomon.domain.todo.dto.request.CreateTodoReq;
import com.maruhxn.todomon.domain.todo.dto.request.RepeatInfoItem;
import com.maruhxn.todomon.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.domain.todo.dto.request.UpdateTodoStatusReq;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.util.ControllerIntegrationTestSupport;
import com.maruhxn.todomon.util.TestTodoFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.maruhxn.todomon.global.auth.application.JwtProvider.BEARER_PREFIX;
import static com.maruhxn.todomon.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.global.common.Constants.REFRESH_TOKEN_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] - Todo")
public class TodoIntegrationTest extends ControllerIntegrationTestSupport {
    static final String TODO_BASE_URL = "/api/todo";

    @Autowired
    TestTodoFactory testTodoFactory;

    @Test
    @DisplayName("GET /api/todo/day - 일별 조회")
    void getTodoByDay() throws Exception {
        // given
        Todo todo1 = testTodoFactory.createSingleTodo(
                LocalDateTime.of(2024, 7, 7, 7, 0),
                LocalDateTime.of(2024, 7, 7, 8, 0),
                false,
                member
        );
        Todo todo2 = testTodoFactory.createSingleTodo(
                LocalDateTime.of(2024, 7, 7, 7, 0),
                LocalDateTime.of(2024, 7, 7, 15, 0),
                true,
                member
        );
        testTodoFactory.createRepeatedTodo(
                LocalDateTime.of(2024, 7, 7, 7, 0),
                LocalDateTime.of(2024, 7, 7, 15, 0),
                true,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.DAILY)
                        .interval(2)
                        .count(3)
                        .build()
        );
        // when
        mockMvc.perform(
                        get(TODO_BASE_URL + "/day")
                                .queryParam("date", "2024-07-07")
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("일별 조회 성공"))
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data.size()").value(3));
    }

    @Test
    @DisplayName("POST /api/todo - 단일 todo 생성")
    void createSingleTodo() throws Exception {
        // given
        CreateTodoReq req = CreateTodoReq.builder()
                .content("테스트")
                .startAt(LocalDateTime.of(2024, 7, 7, 7, 0))
                .endAt(LocalDateTime.of(2024, 7, 7, 8, 0))
                .isAllDay(true)
                .build();

        // when / then
        mockMvc.perform(
                        post(TODO_BASE_URL)
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("투두 생성 성공"));
    }

    @Test
    @DisplayName("종료 시간이 시작 시간 이전이면, 유효성 검증에 실패한다.")
    void failToCreateSingleTodoWithInvalidDateRange() throws Exception {
        // given
        CreateTodoReq req = CreateTodoReq.builder()
                .content("테스트")
                .startAt(LocalDateTime.of(2024, 7, 7, 7, 0))
                .endAt(LocalDateTime.of(2024, 7, 6, 8, 0))
                .isAllDay(true)
                .build();

        // when / then
        mockMvc.perform(
                        post(TODO_BASE_URL)
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("message").value(ErrorCode.VALIDATION_ERROR.getMessage()));
    }

    @Test
    @DisplayName("POST /api/todo - 4일간 매일 반복하는 todo 생성")
    void createDailyTodoWith4Times() throws Exception {
        // given
        LocalDateTime startAt = LocalDateTime.of(2024, 7, 7, 7, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 7, 7, 10, 0);
        CreateTodoReq req = CreateTodoReq.builder()
                .content("테스트")
                .startAt(startAt)
                .endAt(endAt)
                .isAllDay(true)
                .repeatInfoItem(
                        RepeatInfoItem.builder()
                                .frequency(Frequency.DAILY)
                                .interval(1)
                                .until(LocalDate.from(startAt.plusDays(3)))
                                .build()
                )
                .build();

        // when / then
        mockMvc.perform(
                        post(TODO_BASE_URL)
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("투두 생성 성공"));

    }

    @Test
    @DisplayName("PATCH /api/todo/{todoId}")
    void updateTodo() throws Exception {
        // given
        RepeatInfo repeatInfo = RepeatInfo.builder()
                .frequency(Frequency.DAILY)
                .interval(1)
                .until(LocalDate.of(2024, 7, 8))
                .build();
        Todo todo = testTodoFactory.createRepeatedTodo(
                LocalDateTime.of(2024, 7, 7, 7, 0),
                LocalDateTime.of(2024, 7, 7, 8, 0),
                false,
                member,
                repeatInfo
        );

        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .isAllDay(true)
                .repeatInfoItem(
                        RepeatInfoItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .byDay("MON,WED")
                                .interval(1)
                                .count(2)
                                .build()
                )
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{todoId}", todo.getId())
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/todo/{todoId}/status - 일정 상태 변경")
    void updateStatus() throws Exception {
        // given
        RepeatInfo repeatInfo = RepeatInfo.builder()
                .frequency(Frequency.DAILY)
                .interval(1)
                .until(LocalDate.of(2024, 7, 8))
                .build();
        Todo todo = testTodoFactory.createRepeatedTodo(
                LocalDateTime.of(2024, 7, 7, 7, 0),
                LocalDateTime.of(2024, 7, 7, 8, 0),
                false,
                member,
                repeatInfo
        );

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isInstance(true)
                .isDone(true)
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{todoId}/status", todo.getId())
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }


}
