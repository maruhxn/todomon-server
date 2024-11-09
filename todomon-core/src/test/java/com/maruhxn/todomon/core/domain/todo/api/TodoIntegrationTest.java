package com.maruhxn.todomon.core.domain.todo.api;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.domain.Frequency;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.dto.request.*;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.util.ControllerIntegrationTestSupport;
import com.maruhxn.todomon.util.TestTodoFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.maruhxn.todomon.core.global.auth.application.JwtProvider.BEARER_PREFIX;
import static com.maruhxn.todomon.core.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.core.global.common.Constants.REFRESH_TOKEN_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] - Todo")
public class TodoIntegrationTest extends ControllerIntegrationTestSupport {
    static final String TODO_BASE_URL = "/api/todo";

    @Autowired
    TestTodoFactory testTodoFactory;

    @Test
    @DisplayName("반복 투두 생성 시 count 값은 0보다 작을 수 없다.")
    void createTodo() throws Exception {
        // given
        CreateTodoReq req = CreateTodoReq.builder()
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(1))
                .content("test")
                .color("#000000")
                .isAllDay(false)
                .repeatInfoReqItem(RepeatInfoReqItem.builder()
                        .count(0)
                        .frequency(Frequency.DAILY)
                        .interval(1)
                        .build())
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
                .andExpect(jsonPath("message").value(ErrorCode.VALIDATION_ERROR.getMessage()))
                .andExpect(jsonPath("errors[0].reason").value("최소 반복 횟수는 2번입니다."));
    }

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
        LocalDateTime now = LocalDateTime.now();
        CreateTodoReq req = CreateTodoReq.builder()
                .content("테스트")
                .startAt(now)
                .endAt(now.plusHours(1))
                .isAllDay(true)
                .color("#000000")
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
        LocalDateTime now = LocalDateTime.now();
        CreateTodoReq req = CreateTodoReq.builder()
                .content("테스트")
                .startAt(now)
                .endAt(now.plusHours(1))
                .isAllDay(true)
                .color("#000000")
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.DAILY)
                                .interval(1)
                                .until(LocalDate.from(now.plusDays(3)))
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
    @DisplayName("PATCH /api/todo/{objectId}?instance=false")
    void updateSingleTodoSimple() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );

        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .isAllDay(true)
                .startAt(now.plusHours(1))
                .endAt(now.plusHours(2))
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}", todo.getId())
                                .queryParam("isInstance", String.valueOf(false))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/todo/{objectId}?instance=true&taskType=THIS_TASK")
    void updateTodo_THIS_TASK() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createRepeatedTodo(
                now,
                now.plusHours(1),
                false,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.DAILY)
                        .interval(1)
                        .until(now.plusDays(3).toLocalDate())
                        .build()
        );

        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .isAllDay(true)
                .startAt(now.plusHours(1))
                .endAt(now.plusHours(2))
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}", todo.getTodoInstances().get(0).getId())
                                .queryParam("isInstance", String.valueOf(true))
                                .queryParam("targetType", String.valueOf(TargetType.THIS_TASK))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/todo/{objectId}?instance=true&taskType=THIS_TASK - 단일 인스턴스 수정 시 반복 정보 수정은 불가능하다.")
    void updateTodo_THIS_TASK_return400() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createRepeatedTodo(
                now,
                now.plusHours(1),
                false,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.DAILY)
                        .interval(1)
                        .until(now.plusDays(3).toLocalDate())
                        .build()
        );

        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .isAllDay(true)
                .startAt(now.plusHours(1))
                .endAt(now.plusHours(2))
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .byDay("MON,WED")
                                .interval(1)
                                .count(2)
                                .build()
                )
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}", todo.getTodoInstances().get(0).getId())
                                .queryParam("isInstance", String.valueOf(true))
                                .queryParam("targetType", String.valueOf(TargetType.THIS_TASK))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.BAD_REQUEST.name()))
                .andExpect(jsonPath("message").value("전체 인스턴스에 대해서만 반복 정보 수정이 가능합니다."));
    }

    @Test
    @DisplayName("PATCH /api/todo/{objectId}?instance=true&taskType=THIS_TASK - 단일 인스턴스 수정 시 시간 정보가 변경될 경우 유효해야 한다. (1)")
    void updateTodo_THIS_TASK_return400WithInvalidDateRange_1() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createRepeatedTodo(
                now,
                now.plusHours(1),
                false,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.DAILY)
                        .interval(1)
                        .until(now.plusDays(3).toLocalDate())
                        .build()
        );

        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .isAllDay(true)
                .startAt(now.plusDays(3))
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}", todo.getTodoInstances().get(0).getId())
                                .queryParam("isInstance", String.valueOf(true))
                                .queryParam("targetType", String.valueOf(TargetType.THIS_TASK))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.BAD_REQUEST.name()))
                .andExpect(jsonPath("message").value("시작 시각은 종료 시각보다 이전이어야 합니다."));
    }

    @Test
    @DisplayName("PATCH /api/todo/{objectId}?instance=true&taskType=ALL_TASKS")
    void updateTodo_ALL_TASKS() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createRepeatedTodo(
                now,
                now.plusHours(1),
                false,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.DAILY)
                        .interval(1)
                        .until(now.plusDays(3).toLocalDate())
                        .build()
        );

        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .isAllDay(true)
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .byDay("MON,WED")
                                .interval(1)
                                .count(2)
                                .build()
                )
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}", todo.getTodoInstances().get(0).getId())
                                .queryParam("isInstance", String.valueOf(true))
                                .queryParam("targetType", String.valueOf(TargetType.ALL_TASKS))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/todo/{objectId}?instance=true&taskType=ALL_TASKS - 전체 인스턴스를 대상으로 수정 시 시간정보는 변경할 수 없다.")
    void updateTodo_ALL_TASKS_return400() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createRepeatedTodo(
                now,
                now.plusHours(1),
                false,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.DAILY)
                        .interval(1)
                        .until(now.plusDays(3).toLocalDate())
                        .build()
        );

        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .isAllDay(true)
                .startAt(now.plusHours(2))
                .endAt(now.plusHours(3))
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .byDay("MON,WED")
                                .interval(1)
                                .count(2)
                                .build()
                )
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}", todo.getTodoInstances().get(0).getId())
                                .queryParam("isInstance", String.valueOf(true))
                                .queryParam("targetType", String.valueOf(TargetType.ALL_TASKS))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.BAD_REQUEST.name()))
                .andExpect(jsonPath("message").value("시간 정보 수정은 단일 인스턴스에 대해서만 수정 가능합니다."));
    }

    @Test
    @DisplayName("PATCH /api/todo/{objectId}?isInstance=false - 단일 투두를 수정한다.")
    void updateSingleTodo() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );

        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .isAllDay(true)
                .startAt(now.plusHours(2))
                .endAt(now.plusHours(3))
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .byDay("MON,WED")
                                .interval(1)
                                .count(2)
                                .build()
                )
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}", todo.getId())
                                .queryParam("isInstance", String.valueOf(false))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("PATCH /api/todo/{objectId} - 아무 데이터를 넘기지 않을 경우 400 에러를 반환한다.")
    void updateTodoReturn400() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );

        UpdateTodoReq req = UpdateTodoReq.builder()
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}", todo.getId())
                                .queryParam("isInstance", String.valueOf(false))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("message").value("수정할 데이터를 넘겨주세요"));
    }

    @Test
    @DisplayName("PATCH /api/todo/{objectId} - 올바르지 않은 수정 범위를 넘길 경우 400 에러를 반환한다..")
    void updateTodoReturn400WithInvalidUpdateTarget() throws Exception {
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
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .byDay("MON,WED")
                                .interval(1)
                                .count(2)
                                .build()
                )
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}", todo.getTodoInstances().get(0).getId())
                                .queryParam("isInstance", String.valueOf(true))
                                .queryParam("targetType", "ALL")
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
    @DisplayName("PATCH /api/todo/{objectId} - 잘못된 날짜 범위를 넘길 경우 400 에러를 반환한다.")
    void updateTodoReturn400WithInvalidDateRange() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );

        UpdateTodoReq req = UpdateTodoReq.builder()
                .startAt(now.plusHours(2))
                .endAt(now.plusHours(1))
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}", todo.getId())
                                .queryParam("isInstance", String.valueOf(false))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("message").value(ErrorCode.VALIDATION_ERROR.getMessage()))
                .andExpect(jsonPath("errors[0].reason").value("시작 시각은 종료 시각보다 이전이어야 합니다."));
    }


    @Test
    @DisplayName("PATCH /api/todo/{objectId} - 투두 작성자가 아니면 403 에러를 반환한다.")
    void updateTodoReturn403() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Todo todo = testTodoFactory.createSingleTodo(
                LocalDateTime.of(2024, 7, 7, 7, 0),
                LocalDateTime.of(2024, 7, 7, 8, 0),
                false,
                tester1
        );

        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .isAllDay(true)
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .byDay("MON,WED")
                                .interval(1)
                                .count(2)
                                .build()
                )
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}", todo.getId())
                                .queryParam("isInstance", String.valueOf(false))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value(ErrorCode.FORBIDDEN.name()))
                .andExpect(jsonPath("message").value(ErrorCode.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("PATCH /api/todo/{objectId}/status - 일정 상태 변경")
    void updateStatus() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createRepeatedTodo(
                now,
                now.plusHours(1),
                false,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.DAILY)
                        .interval(1)
                        .until(now.plusDays(3).toLocalDate())
                        .build()
        );

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(true)
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}/status", todo.getTodoInstances().get(0).getId())
                                .queryParam("isInstance", String.valueOf(true))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/todo/{objectId}/status - 데이터를 넘기지 않으면 400 에러를 반환한다.")
    void updateStatusReturn400() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createRepeatedTodo(
                now,
                now.plusHours(1),
                false,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.DAILY)
                        .interval(1)
                        .until(now.plusDays(3).toLocalDate())
                        .build()
        );

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}/status", todo.getTodoInstances().get(0).getId())
                                .queryParam("isInstance", String.valueOf(true))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("message").value(ErrorCode.VALIDATION_ERROR.getMessage()))
                .andExpect(jsonPath("errors.size()").value(1));
    }

    @Test
    @DisplayName("PATCH /api/todo/{objectId}/status - 투두 작성자가 아니면 403 에러를 반환한다.")
    void updateStatusReturn403() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                tester1
        );

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(true)
                .build();

        // when / then
        mockMvc.perform(
                        patch(TODO_BASE_URL + "/{objectId}/status", todo.getId())
                                .queryParam("isInstance", String.valueOf(false))
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value(ErrorCode.FORBIDDEN.name()))
                .andExpect(jsonPath("message").value(ErrorCode.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("DELETE /api/todo/{objectId}?isInstance=false - 단일 투두 삭제")
    void deleteTodo() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );

        // when / then
        mockMvc.perform(
                        delete(TODO_BASE_URL + "/{objectId}", todo.getId())
                                .queryParam("isInstance", String.valueOf(false))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/todo/{objectId}?isInstance=true&targetType=THIS_TASK - 투두 인스턴스 1개 삭제")
    void deleteTodoInstance_THIS_TASK() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createRepeatedTodo(
                now,
                now.plusHours(1),
                false,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.DAILY)
                        .interval(1)
                        .until(now.plusDays(3).toLocalDate())
                        .build()
        );

        // when / then
        mockMvc.perform(
                        delete(TODO_BASE_URL + "/{objectId}?isInstance=true&targetType=THIS_TASK", todo.getTodoInstances().get(0).getId())
//                                .queryParam("isInstance", String.valueOf(true))
//                                .queryParam("targetType", String.valueOf(TargetType.THIS_TASK))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/todo/{objectId}?isInstance=true&targetType=ALL_TASKS - 투두 인스턴스 전체 삭제")
    void deleteTodoInstance_ALL_TASKS() throws Exception {
        // given
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Todo todo = testTodoFactory.createRepeatedTodo(
                now,
                now.plusHours(1),
                false,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.DAILY)
                        .interval(1)
                        .until(now.plusDays(3).toLocalDate())
                        .build()
        );

        // when / then
        mockMvc.perform(
                        delete(TODO_BASE_URL + "/{objectId}", todo.getTodoInstances().get(0).getId())
                                .queryParam("isInstance", String.valueOf(true))
                                .queryParam("targetType", String.valueOf(TargetType.ALL_TASKS))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    private Member createMember(String username) {
        Member member = Member.builder()
                .username(username)
                .email(username + "@test.com")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google_" + username)
                .role(Role.ROLE_USER)
                .profileImageUrl("profileImageUrl")
                .build();
        member.initDiligence();
        return memberRepository.save(member);
    }

}
