package com.maruhxn.todomon.core.domain.todo.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.domain.Frequency;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.dto.response.TodoItem;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.util.IntegrationTestSupport;
import com.maruhxn.todomon.core.util.TestTodoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[Service] - TodoQueryService")
class TodoQueryServiceTest extends IntegrationTestSupport {

    @Autowired
    TestTodoFactory testTodoFactory;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TodoQueryService todoQueryService;

    Member member;
    Member member2;

    @BeforeEach
    void setUp() {
        member = createMember("tester", "test@test.com", "google_tester");
        member2 = createMember("tester2", "test2@test.com", "google_tester2");
    }

    @Test
    @DisplayName("일별 조회")
    void getTodosByDay() {
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
        Todo todo3 = testTodoFactory.createSingleTodo(
                LocalDateTime.of(2024, 7, 7, 7, 0),
                LocalDateTime.of(2024, 7, 7, 15, 0),
                true,
                member2
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
        List<TodoItem> todosByDay = todoQueryService
                .getTodosByDay(LocalDate.of(2024, 7, 7), member.getId());
        List<TodoItem> todosByDay2 = todoQueryService.getTodosByDay(LocalDate.of(2024, 7, 9), member.getId());

        // then
        assertThat(todosByDay).hasSize(3);
        assertThat(todosByDay2).hasSize(1);
    }

    @Test
    @DisplayName("주별 조회")
    void getTodosByWeek() {
        // given
        Todo todo1 = testTodoFactory.createSingleTodo(
                LocalDateTime.of(2024, 7, 7, 7, 0),
                LocalDateTime.of(2024, 7, 7, 8, 0),
                false,
                member
        );
        Todo todo2 = testTodoFactory.createSingleTodo(
                LocalDateTime.of(2024, 7, 5, 7, 0),
                LocalDateTime.of(2024, 7, 5, 15, 0),
                true,
                member
        );
        Todo todo3 = testTodoFactory.createSingleTodo(
                LocalDateTime.of(2024, 7, 7, 7, 0),
                LocalDateTime.of(2024, 7, 7, 15, 0),
                true,
                member2
        );
        testTodoFactory.createRepeatedTodo(
                LocalDateTime.of(2024, 7, 5, 7, 0),
                LocalDateTime.of(2024, 7, 5, 15, 0),
                true,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.DAILY)
                        .interval(2)
                        .count(4)
                        .build()
        );

        testTodoFactory.createRepeatedTodo(
                LocalDateTime.of(2024, 7, 5, 7, 0),
                LocalDateTime.of(2024, 7, 5, 15, 0),
                true,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.WEEKLY)
                        .interval(1)
                        .byDay("TUE,THU")
                        .count(3)
                        .build()
        );
        // when
        List<TodoItem> todosByDay = todoQueryService
                .getTodosByWeek(LocalDate.of(2024, 7, 5), member.getId());
        // then
        assertThat(todosByDay).hasSize(8);
    }

    @Test
    @DisplayName("월별 조회")
    void getTodosByMonth() {
        // given
        // 1개
        Todo todo1 = testTodoFactory.createSingleTodo(
                LocalDateTime.of(2024, 7, 7, 7, 0),
                LocalDateTime.of(2024, 7, 7, 8, 0),
                false,
                member
        );
        // 0개
        Todo todo2 = testTodoFactory.createSingleTodo(
                LocalDateTime.of(2024, 8, 5, 7, 0),
                LocalDateTime.of(2024, 8, 5, 15, 0),
                true,
                member
        );
        // 0개
        Todo todo3 = testTodoFactory.createSingleTodo(
                LocalDateTime.of(2024, 7, 7, 7, 0),
                LocalDateTime.of(2024, 7, 7, 15, 0),
                true,
                member2
        );
        // 4개 (7/5, 7/7, 7/9, 7/11)
        testTodoFactory.createRepeatedTodo(
                LocalDateTime.of(2024, 7, 5, 7, 0),
                LocalDateTime.of(2024, 7, 5, 15, 0),
                true,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.DAILY)
                        .interval(2)
                        .count(4)
                        .build()
        );

        // 2개 (7/8, 7/20)
        testTodoFactory.createRepeatedTodo(
                LocalDateTime.of(2024, 7, 5, 7, 0),
                LocalDateTime.of(2024, 7, 5, 15, 0),
                true,
                member,
                RepeatInfo.builder()
                        .frequency(Frequency.WEEKLY)
                        .interval(2)
                        .byDay("TUE")
                        .count(3)
                        .build()
        );
        // when
        List<TodoItem> todosByDay = todoQueryService
                .getTodosByMonth(YearMonth.of(2024, 7), member.getId());
        // then
        assertThat(todosByDay).hasSize(7);
    }

    private Member createMember(String username, String email, String providerId) {
        Member member = Member.builder()
                .username(username)
                .email(email)
                .provider(OAuth2Provider.GOOGLE)
                .providerId(providerId)
                .role(Role.ROLE_USER)
                .profileImageUrl("profileImageUrl")
                .build();
        member.initDiligence();
        return memberRepository.save(member);
    }
}