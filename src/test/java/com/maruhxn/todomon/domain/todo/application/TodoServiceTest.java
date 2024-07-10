package com.maruhxn.todomon.domain.todo.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.domain.todo.domain.Frequency;
import com.maruhxn.todomon.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.domain.todo.domain.Todo;
import com.maruhxn.todomon.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.domain.todo.dto.request.CreateTodoReq;
import com.maruhxn.todomon.domain.todo.dto.request.RepeatInfoItem;
import com.maruhxn.todomon.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[Service] - TodoService")
class TodoServiceTest extends IntegrationTestSupport {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TodoRepository todoRepository;

    @Autowired
    TodoInstanceRepository todoInstanceRepository;
    @Autowired
    TodoService todoService;

    Member member;

    @BeforeEach
    void setUp() {
        member = createMember();
    }

    @Test
    @DisplayName("일간 반복 일정을 생성한다. - 1")
    void createDailyTodo1() {
        // given
        CreateTodoReq req = CreateTodoReq.builder()
                .content("test")
                .startAt(LocalDateTime.of(2024, 7, 10, 7, 0))
                .endAt(LocalDateTime.of(2024, 7, 10, 8, 0))
                .repeatInfoItem(
                        RepeatInfoItem.builder()
                                .frequency(Frequency.DAILY)
                                .interval(2)
                                .count(3)
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member, req);

        // then
        List<TodoInstance> all = todoInstanceRepository.findAll();
        assertThat(all).hasSize(3);
        assertThat(all.get(2).getStartAt()).isEqualTo(LocalDateTime.of(2024, 7, 14, 7, 0));
    }

    @Test
    @DisplayName("일간 반복 일정을 생성한다. - 2")
    void createDailyTodo2() {
        // given
        CreateTodoReq req = CreateTodoReq.builder()
                .content("test")
                .startAt(LocalDateTime.of(2024, 7, 10, 7, 0))
                .endAt(LocalDateTime.of(2024, 7, 10, 8, 0))
                .repeatInfoItem(
                        RepeatInfoItem.builder()
                                .frequency(Frequency.DAILY)
                                .interval(2)
                                .until(LocalDate.of(2024, 7, 14))
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member, req);

        // then
        List<TodoInstance> all = todoInstanceRepository.findAll();
        assertThat(all).hasSize(3);
        assertThat(all.get(2).getStartAt()).isEqualTo(LocalDateTime.of(2024, 7, 14, 7, 0));
    }

    @Test
    @DisplayName("주간 반복 일정을 생성한다. - 1")
    void createWeeklyTodo1() {
        // given
        CreateTodoReq req = CreateTodoReq.builder()
                .content("test")
                .startAt(LocalDateTime.of(2024, 7, 10, 7, 0))
                .endAt(LocalDateTime.of(2024, 7, 10, 8, 0))
                .repeatInfoItem(
                        RepeatInfoItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .interval(2)
                                .byDay("MON,WED,FRI")
                                .count(3)
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member, req);

        // then
        List<TodoInstance> all = todoInstanceRepository.findAll();
        assertThat(all).hasSize(3);
        assertThat(all.get(2).getStartAt()).isEqualTo(LocalDateTime.of(2024, 7, 22, 7, 0));
    }

    @Test
    @DisplayName("주간 반복 일정을 생성한다. - 2")
    void createWeeklyTodo2() {
        // given
        CreateTodoReq req = CreateTodoReq.builder()
                .content("test")
                .startAt(LocalDateTime.of(2024, 7, 10, 7, 0))
                .endAt(LocalDateTime.of(2024, 7, 10, 8, 0))
                .repeatInfoItem(
                        RepeatInfoItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .interval(1)
                                .byDay("MON,FRI")
                                .count(4)
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member, req);

        // then
        List<TodoInstance> all = todoInstanceRepository.findAll();
        assertThat(all).hasSize(4);
        assertThat(all.get(0).getStartAt()).isEqualTo(LocalDateTime.of(2024, 7, 12, 7, 0));
        assertThat(all.get(3).getStartAt()).isEqualTo(LocalDateTime.of(2024, 7, 22, 7, 0));
        assertThat(all.get(3).getStartAt().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("주간 반복 일정을 생성한다. - 3")
    void createWeeklyTodo3() {
        // given
        CreateTodoReq req = CreateTodoReq.builder()
                .content("test")
                .startAt(LocalDateTime.of(2024, 7, 10, 7, 0))
                .endAt(LocalDateTime.of(2024, 7, 10, 8, 0))
                .repeatInfoItem(
                        RepeatInfoItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .interval(1)
                                .byDay("MON,FRI")
                                .until(LocalDate.of(2024, 7, 19))
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member, req);

        // then
        List<TodoInstance> all = todoInstanceRepository.findAll();
        assertThat(all).hasSize(3);
        assertThat(all.get(0).getStartAt()).isEqualTo(LocalDateTime.of(2024, 7, 12, 7, 0));
        assertThat(all.get(2).getStartAt()).isEqualTo(LocalDateTime.of(2024, 7, 19, 7, 0));
    }

    @Test
    @DisplayName("월간 반복 일정을 생성한다. - 1")
    void createMonthlyTodo1() {
        // given
        CreateTodoReq req = CreateTodoReq.builder()
                .content("test")
                .startAt(LocalDateTime.of(2024, 7, 10, 7, 0))
                .endAt(LocalDateTime.of(2024, 7, 10, 8, 0))
                .repeatInfoItem(
                        RepeatInfoItem.builder()
                                .frequency(Frequency.MONTHLY)
                                .interval(1)
                                .byMonthDay(15)
                                .count(3)
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member, req);

        // then
        List<TodoInstance> all = todoInstanceRepository.findAll();
        assertThat(all).hasSize(3);
        assertThat(all.get(0).getStartAt()).isEqualTo(LocalDateTime.of(2024, 7, 15, 7, 0));
        assertThat(all.get(1).getStartAt()).isEqualTo(LocalDateTime.of(2024, 8, 15, 7, 0));
        assertThat(all.get(2).getStartAt()).isEqualTo(LocalDateTime.of(2024, 9, 15, 7, 0));
    }

    @Test
    @DisplayName("월간 반복 일정을 생성한다. - 2")
    void createMonthlyTodo2() {
        // given
        CreateTodoReq req = CreateTodoReq.builder()
                .content("test")
                .startAt(LocalDateTime.of(2024, 7, 10, 7, 0))
                .endAt(LocalDateTime.of(2024, 7, 10, 8, 0))
                .repeatInfoItem(
                        RepeatInfoItem.builder()
                                .frequency(Frequency.MONTHLY)
                                .interval(2)
                                .byMonthDay(15)
                                .until(LocalDate.of(2024, 10, 1))
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member, req);

        // then
        List<TodoInstance> all = todoInstanceRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getStartAt()).isEqualTo(LocalDateTime.of(2024, 7, 15, 7, 0));
        assertThat(all.get(1).getStartAt()).isEqualTo(LocalDateTime.of(2024, 9, 15, 7, 0));
    }

    @Test
    @DisplayName("월간 반복 일정을 생성한다. - 3")
    void createMonthlyTodo3() {
        // given
        CreateTodoReq req = CreateTodoReq.builder()
                .content("test")
                .startAt(LocalDateTime.of(2024, 9, 10, 7, 0))
                .endAt(LocalDateTime.of(2024, 9, 10, 8, 0))
                .repeatInfoItem(
                        RepeatInfoItem.builder()
                                .frequency(Frequency.MONTHLY)
                                .interval(1)
                                .byMonthDay(31)
                                .count(2)
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member, req);

        // then
        List<TodoInstance> all = todoInstanceRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getStartAt()).isEqualTo(LocalDateTime.of(2024, 10, 31, 7, 0));
        assertThat(all.get(1).getStartAt()).isEqualTo(LocalDateTime.of(2024, 12, 31, 7, 0));
    }

    @Test
    @DisplayName("todo를 수정한다.")
    void update() {
        // given
        LocalDateTime startAt = LocalDateTime.of(2024, 7, 7, 7, 0);
        Todo todo = Todo.builder()
                .content("테스트")
                .startAt(startAt)
                .endAt(LocalDateTime.of(2024, 7, 7, 8, 0))
                .isAllDay(false)
                .writer(member)
                .build();
        RepeatInfo repeatInfo = RepeatInfo.builder()
                .frequency(Frequency.DAILY)
                .interval(1)
                .until(LocalDate.from(startAt.plusDays(3)))
                .build();
        todo.setRepeatInfo(repeatInfo);
        todoRepository.save(todo);

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
        // when
        todoService.update(todo.getId(), req);

        // then
        Todo findTodo = todoRepository.findById(todo.getId()).get();
        assertThat(findTodo.getContent()).isEqualTo("수정됨");
        assertThat(findTodo.getRepeatInfo().getFrequency()).isEqualTo(Frequency.WEEKLY);
        assertThat(findTodo.getRepeatInfo().getByDay()).isEqualTo("MON,WED");
        assertThat(todoInstanceRepository.findAll()).hasSize(2);
    }

    private Member createMember() {
        member = Member.builder()
                .username("tester")
                .email("test@test.com")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google_foobarfoobar")
                .role(Role.ROLE_USER)
                .profileImageUrl("profileImageUrl")
                .build();
        return memberRepository.save(member);
    }
}