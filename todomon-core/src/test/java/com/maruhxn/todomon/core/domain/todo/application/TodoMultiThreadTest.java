package com.maruhxn.todomon.core.domain.todo.application;

import com.maruhxn.todomon.config.TestConfig;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.Frequency;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.domain.todo.dto.request.CreateTodoReq;
import com.maruhxn.todomon.core.domain.todo.dto.request.RepeatInfoReqItem;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoStatusReq;
import com.maruhxn.todomon.core.global.auth.dto.MemberDTO;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.infra.file.FileService;
import com.maruhxn.todomon.util.TestTodoFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class TodoMultiThreadTest {

    @MockBean
    protected FileService fileService;

    @Autowired
    TodoRepository todoRepository;

    @Autowired
    TodoInstanceRepository todoInstanceRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TestTodoFactory testTodoFactory;

    @Autowired
    TodoService todoService;

    @AfterEach
    public void tearDown() throws Exception {
        todoInstanceRepository.deleteAllInBatch();
        todoRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("100명의 유저가 todo를 생성")
    void multiThreadCreate_Todo() throws InterruptedException {
        // given
        int threadCount = 100;
        int TODO_CNT_PER_MEMBER = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount * TODO_CNT_PER_MEMBER);

        List<Member> testers = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Member tester = createMember(i);
            testers.add(tester);
        }
        memberRepository.saveAll(testers);

        // when
        for (int k = 0; k < TODO_CNT_PER_MEMBER; k++) {
            for (int i = 0; i < threadCount; i++) {
                int finalK = k; // 로컬 변수로 복사
                int finalI = i;
                executorService.submit(() -> {
                    try {
                        int index = threadCount * finalK + finalI; // 각 스레드에서 독립적으로 사용할 수 있도록 수정
                        Member tester = testers.get(finalI);
                        saveMemberToContext(tester);

                        Random random = new Random(index);
                        LocalDateTime startAt = LocalDate.now().atStartOfDay().plusHours(random.nextInt(22));
                        LocalDateTime endAt = startAt.plusHours(random.nextInt(1));
                        CreateTodoReq req = CreateTodoReq.builder()
                                .content("test")
                                .startAt(startAt)
                                .endAt(endAt)
                                .isAllDay(random.nextBoolean())
                                .build();

                        todoService.create(tester.getId(), req);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await();
        // then
        assertThat(todoRepository.findAll())
                .hasSize(threadCount * TODO_CNT_PER_MEMBER);
    }

    @Test
    @DisplayName("100명의 유저가 todo instance를 생성")
    void multiThreadCreate_TodoInstance() throws InterruptedException {
        // given
        int threadCount = 300;
        int TODO_CNT_PER_MEMBER = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount * TODO_CNT_PER_MEMBER);

        List<Member> testers = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Member tester = createMember(i);
            testers.add(tester);
        }
        memberRepository.saveAll(testers);

        // 성능 측정을 위한 시간 시작
        Instant start = Instant.now();
        // when
        for (int k = 0; k < TODO_CNT_PER_MEMBER; k++) {
            for (int i = 0; i < threadCount; i++) {
                int finalK = k; // 로컬 변수로 복사
                int finalI = i;
                executorService.submit(() -> {
                    try {
                        int index = threadCount * finalK + finalI; // 각 스레드에서 독립적으로 사용할 수 있도록 수정
                        Member tester = testers.get(finalI);
                        saveMemberToContext(tester);

                        Random random = new Random(index);
                        LocalDateTime startAt = LocalDate.now().atStartOfDay().plusHours(random.nextInt(22));
                        LocalDateTime endAt = startAt.plusHours(random.nextInt(1));
                        CreateTodoReq req = CreateTodoReq.builder()
                                .content("test")
                                .startAt(startAt)
                                .endAt(endAt)
                                .isAllDay(random.nextBoolean())
                                .repeatInfoReqItem(
                                        RepeatInfoReqItem.builder()
                                                .frequency(Frequency.DAILY)
                                                .interval(2)
                                                .count(3)
                                                .build()
                                )
                                .build();

                        todoService.create(tester.getId(), req);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await();
        Instant finish = Instant.now();
        Duration timeElapsed = Duration.between(start, finish);
        System.out.println("Elapsed time in milliseconds: " + timeElapsed.toMillis());

        Thread.sleep(15000);

        // then
        assertThat(todoRepository.findAll())
                .hasSize(threadCount * TODO_CNT_PER_MEMBER);
        assertThat(todoInstanceRepository.findAll())
                .hasSize(threadCount * 3 * TODO_CNT_PER_MEMBER);
    }

    @Test
    @DisplayName("100명의 유저가 3개의 todo에 대한 완료 요청")
    void mulithreadUpdateStatus_Todo() throws InterruptedException {
        // given
        int threadCount = 100;
        int TODO_CNT_PER_MEMBER = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch latch = new CountDownLatch(threadCount * TODO_CNT_PER_MEMBER);

        List<Member> testers = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Member tester = createMember(i);
            testers.add(tester);
        }
        memberRepository.saveAll(testers);

        List<Todo> todos = new ArrayList<>();
        for (int i = 0; i < TODO_CNT_PER_MEMBER; i++) {
            for (int j = 0; j < threadCount; j++) {
                Todo todo = testTodoFactory.createSingleTodo(
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(1),
                        false,
                        testers.get(j)
                );
                todos.add(todo);
            }
        }
        todoRepository.saveAll(todos);


        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(true)
                .build();

        // when

        for (int k = 0; k < TODO_CNT_PER_MEMBER; k++) {
            for (int i = 0; i < threadCount; i++) {
                int finalK = k; // 로컬 변수로 복사
                int finalI = i;
                executorService.submit(() -> {
                    try {
                        int index = threadCount * finalK + finalI; // 각 스레드에서 독립적으로 사용할 수 있도록 수정
                        Member tester = testers.get(finalI);
                        saveMemberToContext(tester);
                        todoService.updateStatusAndReward(todos.get(index).getId(), false, tester.getId(), req);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await();

        // then
        assertThat(todoRepository.findAll())
                .hasSize(threadCount * TODO_CNT_PER_MEMBER)
                .extracting("isDone")
                .containsOnly(true);
        assertThat(memberRepository.findAll())
                .hasSize(threadCount)
                .allMatch(member -> member.getDailyAchievementCnt() == TODO_CNT_PER_MEMBER)
                .allMatch(member -> member.getScheduledReward() == TODO_CNT_PER_MEMBER * 10);
    }

    @Test
    @DisplayName("100명의 유저가 3개의 todoInstance에 대한 완료 요청 (2개는 이미 완료, 1개는 미완료)")
    void mulithreadUpdateStatus_TodoInstance() throws InterruptedException {
        // given
        int threadCount = 100;
        int INSTANCE_COUNT = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch latch = new CountDownLatch(threadCount * INSTANCE_COUNT);

        List<Member> testers = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Member tester = createMember(i);
            testers.add(tester);
        }
        memberRepository.saveAll(testers);

        List<Todo> todos = new ArrayList<>();
        for (int j = 0; j < threadCount; j++) {
            Todo repeatedTodo = testTodoFactory.createRepeatedTodo(
                    LocalDate.now().minusDays(INSTANCE_COUNT - 1).atStartOfDay(),
                    LocalDate.now().minusDays(INSTANCE_COUNT - 1).atStartOfDay(),
                    false,
                    testers.get(j),
                    RepeatInfo.builder()
                            .frequency(Frequency.DAILY)
                            .interval(1)
                            .count(INSTANCE_COUNT)
                            .build()
            );
            todos.add(repeatedTodo);
            List<TodoInstance> todoInstances = repeatedTodo.getTodoInstances();
            for (int i = 0; i < todoInstances.size(); i++) {
                if (i != todoInstances.size() - 1) {
                    todoInstances.get(i).updateIsDone(true);
                }
            }
            todoInstanceRepository.saveAll(todoInstances);
        }
        todoRepository.saveAll(todos);


        UpdateTodoStatusReq req = UpdateTodoStatusReq.builder()
                .isDone(true)
                .build();

        // when

        for (int k = 0; k < INSTANCE_COUNT; k++) {
            for (int i = 0; i < threadCount; i++) {
                int finalK = k; // 로컬 변수로 복사
                int finalI = i;
                Todo repeatedTodo = todos.get(finalI);
                List<TodoInstance> todoInstances = repeatedTodo.getTodoInstances();
                executorService.submit(() -> {
                    try {
                        Member tester = testers.get(finalI);
                        saveMemberToContext(tester);
                        todoService.updateStatusAndReward(todoInstances.get(finalK).getId(), true, tester.getId(), req);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await();

        // then
        assertThat(todoRepository.findAll())
                .hasSize(threadCount)
                .extracting("isDone")
                .containsOnly(true);
        assertThat(todoInstanceRepository.findAll())
                .hasSize(threadCount * INSTANCE_COUNT)
                .extracting("isDone")
                .containsOnly(true);
        assertThat(memberRepository.findAll())
                .hasSize(threadCount)
                .allMatch(member -> member.getDailyAchievementCnt() == 1)
                .allMatch(member -> member.getScheduledReward() == 10 * (INSTANCE_COUNT + 1));
    }

    private Member createMember(long i) {
        Member tester = Member.builder()
                .username("tester" + i)
                .email("tester" + i + "@test.com")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google_foobarfoobar" + i)
                .role(Role.ROLE_USER)
                .profileImageUrl("img")
                .build();
        tester.initDiligence();

        return tester;
    }

    private static void saveMemberToContext(Member member) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        MemberDTO dto = MemberDTO.from(member);
        TodomonOAuth2User todomonOAuth2User = TodomonOAuth2User.from(dto); // 사용자의 커스텀 정보 설정
        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                todomonOAuth2User,
                todomonOAuth2User.getAuthorities(),
                todomonOAuth2User.getProvider()
        );
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }
}
