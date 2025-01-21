package com.maruhxn.todomon.core.domain.todo.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.Frequency;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.domain.todo.dto.request.*;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.util.IntegrationTestSupport;
import com.maruhxn.todomon.core.util.TestTodoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.maruhxn.todomon.core.global.common.Constants.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("[Service] - TodoService")
class TodoServiceTest extends IntegrationTestSupport {

    @Autowired
    TestTodoFactory testTodoFactory;

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
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.DAILY)
                                .interval(2)
                                .count(3)
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member.getId(), req);

        // then
        Todo todo = todoRepository.findAll().get(0);
        assertThat(todo.getStartAt()).isEqualTo(LocalDateTime.of(2024, 7, 10, 7, 0));
        assertThat(todo.getEndAt()).isEqualTo(LocalDateTime.of(2024, 7, 14, 8, 0));
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
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.DAILY)
                                .interval(2)
                                .until(LocalDate.of(2024, 7, 14))
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member.getId(), req);

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
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .interval(2)
                                .byDay("MON,WED,FRI")
                                .count(3)
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member.getId(), req);

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
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .interval(1)
                                .byDay("MON,FRI")
                                .count(4)
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member.getId(), req);

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
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.WEEKLY)
                                .interval(1)
                                .byDay("MON,FRI")
                                .until(LocalDate.of(2024, 7, 19))
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member.getId(), req);

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
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.MONTHLY)
                                .interval(1)
                                .byMonthDay(15)
                                .count(3)
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member.getId(), req);

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
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.MONTHLY)
                                .interval(2)
                                .byMonthDay(15)
                                .until(LocalDate.of(2024, 10, 1))
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member.getId(), req);

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
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.MONTHLY)
                                .interval(1)
                                .byMonthDay(31)
                                .count(2)
                                .build()
                )
                .isAllDay(false)
                .build();

        // when
        todoService.create(member.getId(), req);

        // then
        Todo todo = todoRepository.findAll().get(0);
        List<TodoInstance> all = todoInstanceRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getStartAt()).isEqualTo(LocalDateTime.of(2024, 10, 31, 7, 0));
        assertThat(all.get(1).getStartAt()).isEqualTo(LocalDateTime.of(2024, 12, 31, 7, 0));
        assertThat(todo.getStartAt()).isEqualTo(all.get(0).getStartAt());
        assertThat(todo.getEndAt()).isEqualTo(all.get(1).getEndAt());
    }

    @Test
    @DisplayName("todo를 수정한다.")
    void update_single_todo() {
        // given
        saveMemberToContext(member);
        LocalDateTime now = LocalDate.now().atStartOfDay();

        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );

        UpdateAndDeleteTodoQueryParams params = UpdateAndDeleteTodoQueryParams.builder()
                .isInstance(false)
                .build();

        LocalDateTime updatedStartAt = now.plusMinutes(10);
        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .isAllDay(true)
                .startAt(updatedStartAt)
                .endAt(updatedStartAt.plusHours(1))
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.DAILY)
                                .interval(1)
                                .count(2)
                                .build()
                )
                .build();

        // when
        todoService.update(todo.getId(), params, req);

        // then
        assertThat(todo)
                .extracting("content", "isAllDay", "startAt", "endAt")
                .containsExactly("수정됨", true, updatedStartAt.toLocalDate().atStartOfDay(), LocalDateTime.of(updatedStartAt.toLocalDate().plusDays(1), LocalDateTime.MAX.toLocalTime()).truncatedTo(ChronoUnit.MICROS));
        assertThat(todoInstanceRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("todoinstance 하나를 수정한다.")
    void update_todoInstance_THIS_TASK() {
        // given
        saveMemberToContext(member);
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

        UpdateAndDeleteTodoQueryParams params = UpdateAndDeleteTodoQueryParams.builder()
                .targetType(TargetType.THIS_TASK)
                .isInstance(true)
                .build();

        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .startAt(now.plusHours(2))
                .endAt(now.plusHours(3))
                .build();

        List<TodoInstance> todoInstances = todo.getTodoInstances();
        // when
        TodoInstance firstInstance = todoInstances.get(0);
        todoService.update(firstInstance.getId(), params, req);

        // then
        assertThat(firstInstance)
                .extracting("content", "startAt", "endAt")
                .containsExactly("수정됨", now.plusHours(2), now.plusHours(3));
        assertThat(todoInstanceRepository.findAll()).hasSize(4);
    }

    @Test
    @DisplayName("todoinstance 전체를 수정한다.")
    void update_todoInstance_ALL_TASKS() {
        // given
        saveMemberToContext(member);
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

        UpdateAndDeleteTodoQueryParams params = UpdateAndDeleteTodoQueryParams.builder()
                .targetType(TargetType.ALL_TASKS)
                .isInstance(true)
                .build();

        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .isAllDay(true)
                .build();

        List<TodoInstance> todoInstances = todo.getTodoInstances();
        // when
        TodoInstance firstInstance = todoInstances.get(0);
        todoService.update(firstInstance.getId(), params, req);

        // then
        assertThat(todoInstances)
                .hasSize(4)
                .extracting("content", "startAt", "endAt")
                .containsExactly(
                        tuple("수정됨", now.toLocalDate().atStartOfDay(), LocalDateTime.of(now.toLocalDate(), LocalDateTime.MAX.toLocalTime())),
                        tuple("수정됨", now.toLocalDate().plusDays(1).atStartOfDay(), LocalDateTime.of(now.toLocalDate().plusDays(1), LocalDateTime.MAX.toLocalTime())),
                        tuple("수정됨", now.toLocalDate().plusDays(2).atStartOfDay(), LocalDateTime.of(now.toLocalDate().plusDays(2), LocalDateTime.MAX.toLocalTime())),
                        tuple("수정됨", now.toLocalDate().plusDays(3).atStartOfDay(), LocalDateTime.of(now.toLocalDate().plusDays(3), LocalDateTime.MAX.toLocalTime()))
                );
    }

    @Test
    @DisplayName("todoinstance 전체의 반복 정보를 수정한다.")
    void update_todoInstance_ALL_TASKS_with_new_repeat_info() {
        // given
        saveMemberToContext(member);
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

        UpdateAndDeleteTodoQueryParams params = UpdateAndDeleteTodoQueryParams.builder()
                .targetType(TargetType.ALL_TASKS)
                .isInstance(true)
                .build();

        UpdateTodoReq req = UpdateTodoReq.builder()
                .content("수정됨")
                .isAllDay(true)
                .repeatInfoReqItem(
                        RepeatInfoReqItem.builder()
                                .frequency(Frequency.DAILY)
                                .interval(1)
                                .count(2)
                                .build()
                )
                .build();

        // when
        TodoInstance firstInstance = todo.getTodoInstances().get(0);
        todoService.update(firstInstance.getId(), params, req);

        // then
        assertThat(todo.getTodoInstances())
                .hasSize(2)
                .extracting("content", "startAt", "endAt")
                .containsExactly(
                        tuple("수정됨", now.toLocalDate().atStartOfDay(), LocalDateTime.of(now.toLocalDate(), LocalDateTime.MAX.toLocalTime()).truncatedTo(ChronoUnit.MICROS)),
                        tuple("수정됨", now.toLocalDate().plusDays(1).atStartOfDay(), LocalDateTime.of(now.toLocalDate().plusDays(1), LocalDateTime.MAX.toLocalTime()).truncatedTo(ChronoUnit.MICROS))
                );
    }

    @Test
    @DisplayName("단일 일정 완료 처리")
    void completeSingleTodoAndReward() {
        // given
        saveMemberToContext(member);
        LocalDateTime now = LocalDate.now().atStartOfDay();

        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(true)
                .build();

        // when
        todoService.updateStatusAndReward(todo.getId(), false, member.getId(), req);

        // then
        assertThat(todo.isDone()).isTrue();
        assertThat(member.getDiligence().getGauge()).isEqualTo(GAUGE_INCREASE_RATE);
        assertThat(member.getScheduledReward()).isEqualTo(10L);
    }

    @Test
    @DisplayName("단일 일정 완료 처리 시, 일관성 게이지는 '(비율) / 유저 레벨'만큼 찬다")
    void completeSingleTodoAndRewardDependsOnlLevel() {
        // given
        member.getDiligence().levelUp(9);
        memberRepository.save(member);

        saveMemberToContext(member);
        LocalDateTime now = LocalDate.now().atStartOfDay();

        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(true)
                .build();

        // when
        todoService.updateStatusAndReward(todo.getId(), false, member.getId(), req);

        // then
        assertThat(todo.isDone()).isTrue();
        assertThat(member.getDiligence().getGauge()).isEqualTo(GAUGE_INCREASE_RATE / member.getDiligence().getLevel());
        assertThat(member.getScheduledReward()).isEqualTo(10L);
    }

    @Test
    @DisplayName("단일 일정 완료 처리 시, 레벨업을 하는 경우 보상을 지급한다")
    void completeSingleTodoAndRewardDependsOnlLevel2() {
        // given
        member.getDiligence().levelUp(9); // level 10
        member.getDiligence().increaseGaugeByTodoCnt(99); // level 10의 경우, 100개 성공 시 레벨업
        memberRepository.save(member);

        saveMemberToContext(member);
        LocalDateTime now = LocalDate.now().atStartOfDay();

        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(true)
                .build();

        // when
        todoService.updateStatusAndReward(todo.getId(), false, member.getId(), req);

        // then
        assertThat(todo.isDone()).isTrue();
        assertThat(member.getDiligence().getLevel()).isEqualTo(11);
        assertThat(member.getDiligence().getGauge()).isEqualTo(0.0);
        int levelUpReward = REWARD_UNIT * 11;
        assertThat(member.getScheduledReward()).isEqualTo(10 + levelUpReward);
    }

    @Test
    @DisplayName("단일 일정 취소 처리")
    void cancelSingleTodoAndWithdraw() {
        // given
        saveMemberToContext(member);
        LocalDateTime now = LocalDate.now().atStartOfDay();

        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );
        todo.updateIsDone(true);
        member.getDiligence().increaseGaugeByTodoCnt(4);
        member.addScheduledReward(100);

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(false)
                .build();

        // when
        todoService.updateStatusAndReward(todo.getId(), false, member.getId(), req);

        // then
        assertThat(todo.isDone()).isFalse();
        assertThat(member.getDiligence().getGauge()).isEqualTo(3 * GAUGE_INCREASE_RATE);
        assertThat(member.getScheduledReward()).isEqualTo(90);
    }

    @Test
    @DisplayName("단일 일정 취소 처리, 일관성 게이지는 '(비율) / 유저 레벨'만큼 감소한다")
    void cancelSingleTodoAndWithdraw2() {
        // given
        member.getDiligence().levelUp(9);
        memberRepository.save(member);
        saveMemberToContext(member);
        LocalDateTime now = LocalDate.now().atStartOfDay();

        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );
        todo.updateIsDone(true);
        member.getDiligence().increaseGaugeByTodoCnt(4);
        member.addScheduledReward(100);

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(false)
                .build();

        // when
        todoService.updateStatusAndReward(todo.getId(), false, member.getId(), req);

        // then
        assertThat(todo.isDone()).isFalse();
        assertThat(member.getDiligence().getGauge()).isEqualTo(3 * (GAUGE_INCREASE_RATE / member.getDiligence().getLevel()));
        assertThat(member.getScheduledReward()).isEqualTo(90);
    }

    @Test
    @DisplayName("단일 일정 취소 처리 시, 이전에 레벨업을 했었다면 이를 취소하고 보상을 회수한다")
    void cancelSingleTodoAndWithdraw3() {
        // given
        member.getDiligence().levelUp(9); // level 10, gauge 0.0
        memberRepository.save(member);
        member.addScheduledReward((int) (1000 + (REWARD_UNIT * REWARD_LEVERAGE_RATE * 1))); // 이전에 하나를 수행하여 10레벨 달성 보상
        saveMemberToContext(member);
        LocalDateTime now = LocalDate.now().atStartOfDay();

        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );
        todo.updateIsDone(true);

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(false)
                .build();

        // when
        todoService.updateStatusAndReward(todo.getId(), false, member.getId(), req);

        // then
        assertThat(todo.isDone()).isFalse();
        assertThat(member.getDiligence().getLevel()).isEqualTo(9); // 레벨 10 -> 9로 감소
        assertThat(member.getDiligence().getGauge()).isEqualTo(99); // 게이지 0.0 -> 99,0으로 감소
        assertThat(member.getScheduledReward()).isEqualTo(0); // 10렙 보상 회수
    }

    @Test
    @DisplayName("반복 일정 완료 처리")
    void completeRepeatedTodoAndReward() {
        // given
        saveMemberToContext(member);
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
        List<TodoInstance> todoInstances = todoInstanceRepository.findAllByTodo_Id(todo.getId());

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(true)
                .build();
        // when
        todoService.updateStatusAndReward(todoInstances.get(0).getId(), true, member.getId(), req);

        // then
        assertThat(todoInstanceRepository.findAll()).hasSize(4);
        assertThat(todoInstances.get(0).isDone()).isTrue();
        assertThat(member.getDiligence().getGauge()).isEqualTo(GAUGE_INCREASE_RATE);
        assertThat(member.getScheduledReward()).isEqualTo(10L);
    }

    @Test
    @DisplayName("일간 반복 일정 전체 완료 처리")
    void completeAllRepeatedTodoAndReward() {
        // given
        saveMemberToContext(member);
        LocalDateTime now = LocalDate.now().minusDays(3).atStartOfDay();

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
        List<TodoInstance> todoInstances = todoInstanceRepository.findAllByTodo_Id(todo.getId());

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(true)
                .build();

        int size = todoInstances.size();
        for (int i = 0; i < size; i++) {
            if (i != size - 1) {
                todoInstances.get(i).updateIsDone(true);
            }
        }

        // when
        todoService.updateStatusAndReward(todoInstances.get(size - 1).getId(), true, member.getId(), req);

        // then
        assertThat(todo.isDone()).isTrue();
        assertThat(todoInstanceRepository.findAll()).hasSize(4);
        assertThat(todoInstances.get(size - 1).isDone()).isTrue();
        assertThat(member.getDiligence().getGauge()).isEqualTo(GAUGE_INCREASE_RATE * (size + 1));
        assertThat(member.getScheduledReward()).isEqualTo((long) (REWARD_UNIT * REWARD_LEVERAGE_RATE * (size + 1)));
    }

    @Test
    @DisplayName("반복 일정 취소 처리")
    void cancelRepeatedTodoAndWithdraw() {
        // given
        saveMemberToContext(member);
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
        List<TodoInstance> todoInstances = todoInstanceRepository.findAllByTodo_Id(todo.getId());

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(false)
                .build();

        TodoInstance firstInstance = todoInstances.get(0);
        firstInstance.updateIsDone(true);
        todoInstanceRepository.save(firstInstance);
        member.getDiligence().increaseGaugeByTodoCnt(4);
        member.addScheduledReward(100);
        // when
        todoService.updateStatusAndReward(firstInstance.getId(), true, member.getId(), req);

        // then
        assertThat(firstInstance.isDone()).isFalse();
        assertThat(member.getDiligence().getGauge()).isEqualTo(3 * GAUGE_INCREASE_RATE);
        assertThat(member.getScheduledReward()).isEqualTo((int) (100 - REWARD_UNIT * REWARD_LEVERAGE_RATE));
    }

    @Test
    @DisplayName("전체 완료되었던 일간 반복 일정 부분 취소 처리")
    void cancelAllRepeatedTodoAndWithdraw() {
        // given
        saveMemberToContext(member);
        LocalDateTime now = LocalDate.now().minusDays(3).atStartOfDay();

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
        List<TodoInstance> todoInstances = todoInstanceRepository.findAllByTodo_Id(todo.getId());

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(false)
                .build();

        int size = todoInstances.size();
        todoInstances.forEach(todoInstance -> todoInstance.updateIsDone(true));
        todo.updateIsDone(true);
        todoInstanceRepository.saveAll(todoInstances);
        member.getDiligence().increaseGaugeByTodoCnt(4);
        member.getDiligence().increaseGaugeByTodoCnt(4);
        member.addScheduledReward(100);
        // when
        todoService.updateStatusAndReward(todoInstances.get(size - 1).getId(), true, member.getId(), req);

        // then
        assertThat(todo.isDone()).isFalse();
        assertThat(todoInstances.get(size - 1).isDone()).isFalse();
        assertThat(member.getDiligence().getGauge()).isEqualTo(80 - GAUGE_INCREASE_RATE * (size + 1));
        assertThat(member.getScheduledReward()).isEqualTo((int) (100L - REWARD_UNIT * (size + 1) * REWARD_LEVERAGE_RATE));
    }

    @Test
    @DisplayName("수정 요청하는 todoInstance의 startAt의 날짜가 오늘이 아닐 경우, 에러를 반환한다.")
    void updateStatusFailWhenIsNotToday() {
        // given
        saveMemberToContext(member);
        LocalDateTime now = LocalDate.now().minusDays(5).atStartOfDay();

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
        List<TodoInstance> todoInstances = todoInstanceRepository.findAllByTodo_Id(todo.getId());

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(true)
                .build();
        // when / then
        assertThatThrownBy(() -> todoService.updateStatusAndReward(todoInstances.get(0).getId(), true, member.getId(), req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("오늘에 해당하는 할 일만 수정이 가능합니다.");
    }

    @Test
    @DisplayName("수정 요청하는 todo의 startAt의 날짜가 오늘이 아닐 경우, 에러를 반환한다.")
    void updateFailWhenIsNotToday() {
        // given
        saveMemberToContext(member);
        LocalDateTime now = LocalDate.now().minusDays(1).atStartOfDay();

        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );

        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(true)
                .build();
        // when / then
        assertThatThrownBy(() -> todoService.updateStatusAndReward(todo.getId(), false, member.getId(), req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("오늘에 해당하는 할 일만 수정이 가능합니다.");
    }

    @Test
    @DisplayName("todo를 삭제한다.")
    void deleteSingleTodo() {
        // given
        saveMemberToContext(member);
        saveMemberToContext(member);
        LocalDateTime now = LocalDate.now().atStartOfDay();

        Todo todo = testTodoFactory.createSingleTodo(
                now,
                now.plusHours(1),
                false,
                member
        );

        UpdateAndDeleteTodoQueryParams params = UpdateAndDeleteTodoQueryParams.builder()
                .isInstance(false)
                .build();

        // when
        todoService.deleteTodo(todo.getId(), params);

        // then
        assertThat(todoRepository.findById(todo.getId())).isEmpty();
    }

    @Test
    @DisplayName("한 개의 반복 일정을 삭제할 수 있다.")
    void deleteRepeatedTodo_THIS_TASK() {
        // given
        saveMemberToContext(member);
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

        UpdateAndDeleteTodoQueryParams params = UpdateAndDeleteTodoQueryParams.builder()
                .isInstance(true)
                .targetType(TargetType.THIS_TASK)
                .build();

        // when
        todoService.deleteTodo(todo.getTodoInstances().get(0).getId(), params);

        // then
        assertThat(todo.getTodoInstances()).hasSize(3);
    }

    @Test
    @DisplayName("전체 반복 일정을 삭제할 수 있다.")
    void deleteRepeatedTodo_ALL_TASKS() {
        // given
        saveMemberToContext(member);
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

        UpdateAndDeleteTodoQueryParams params = UpdateAndDeleteTodoQueryParams.builder()
                .isInstance(true)
                .targetType(TargetType.ALL_TASKS)
                .build();

        // when
        todoService.deleteTodo(todo.getTodoInstances().get(0).getId(), params);

        // then
        assertThat(todo.getTodoInstances()).hasSize(0);
        assertThat(todoRepository.findById(todo.getId())).isEmpty();
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
        member.initDiligence();
        return memberRepository.save(member);
    }
}