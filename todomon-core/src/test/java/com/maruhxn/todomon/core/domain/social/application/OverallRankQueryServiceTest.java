package com.maruhxn.todomon.core.domain.social.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.domain.PetType;
import com.maruhxn.todomon.core.domain.pet.domain.Rarity;
import com.maruhxn.todomon.core.domain.social.dto.response.CollectedPetRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.TodoAchievementRankItem;
import com.maruhxn.todomon.core.domain.todo.dao.TodoAchievementHistoryRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoAchievementHistory;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.util.IntegrationTestSupport;
import com.maruhxn.todomon.core.util.TestTodoFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("[Service] - OverallRankQueryService")
class OverallRankQueryServiceTest extends IntegrationTestSupport {

    @Autowired
    TestTodoFactory testTodoFactory;

    @Autowired
    OverallRankQueryService overallRankQueryService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TodoRepository todoRepository;

    @Autowired
    TodoAchievementHistoryRepository todoAchievementHistoryRepository;

    @Test
    @DisplayName("일관성 레벨이 가장 높은 탑 10 유저를 조회한다. (게이지 -> 레벨 -> 가입일자 순)")
    void getTop10MembersByDiligenceLevelAndGauge() {
        // given
        List<Member> members = createAndSaveMembers(20);
        for (int i = 1; i <= 5; i++) { // tester2, tester4, tester6, tester8, tester10
            Member member = members.get(2 * i - 1);
            createTitleName(member);
            member.getDiligence().levelUp(2 * i);
        }
        memberRepository.saveAll(members);
        // when
        List<DiligenceRankItem> result = overallRankQueryService.getTop10MembersByDiligenceLevelAndGauge();
        Member tester10 = members.get(9);
        Member tester8 = members.get(7);
        Member tester6 = members.get(5);
        Member tester4 = members.get(3);
        Member tester2 = members.get(1);
        Member tester1 = members.get(0);
        Member tester3 = members.get(2);
        Member tester5 = members.get(4);
        Member tester7 = members.get(6);
        Member tester9 = members.get(8);

        // then
        assertThat(result)
                .hasSize(10)
                .extracting("memberId", "username", "profileImageUrl", "level", "title.name", "title.color")
                .containsExactly(
                        tuple(tester10.getId(), tester10.getUsername(), tester10.getProfileImageUrl(), tester10.getDiligence().getLevel(), "TEST", "#000000"),
                        tuple(tester8.getId(), tester8.getUsername(), tester8.getProfileImageUrl(), tester8.getDiligence().getLevel(), "TEST", "#000000"),
                        tuple(tester6.getId(), tester6.getUsername(), tester6.getProfileImageUrl(), tester6.getDiligence().getLevel(), "TEST", "#000000"),
                        tuple(tester4.getId(), tester4.getUsername(), tester4.getProfileImageUrl(), tester4.getDiligence().getLevel(), "TEST", "#000000"),
                        tuple(tester2.getId(), tester2.getUsername(), tester2.getProfileImageUrl(), tester2.getDiligence().getLevel(), "TEST", "#000000"),
                        tuple(tester1.getId(), tester1.getUsername(), tester1.getProfileImageUrl(), tester1.getDiligence().getLevel(), null, null),
                        tuple(tester3.getId(), tester3.getUsername(), tester3.getProfileImageUrl(), tester3.getDiligence().getLevel(), null, null),
                        tuple(tester5.getId(), tester5.getUsername(), tester5.getProfileImageUrl(), tester5.getDiligence().getLevel(), null, null),
                        tuple(tester7.getId(), tester7.getUsername(), tester7.getProfileImageUrl(), tester7.getDiligence().getLevel(), null, null),
                        tuple(tester9.getId(), tester9.getUsername(), tester9.getProfileImageUrl(), tester9.getDiligence().getLevel(), null, null)
                );
    }

    @Test
    @DisplayName("가장 많은 펫을 획득한 탑 10 유저를 조회한다. (획득한 펫 수 -> 마지막 획득 일자 -> 가입일자 순)")
    void getTop10MembersByCollectedPetCnt() {
        // given
        List<Member> members = createAndSaveMembers(20);
        for (int i = 1; i <= 5; i++) { // tester2, tester4, tester6, tester8, tester10
            Member member = members.get(2 * i - 1);
            createTitleName(member);
            int petCnt = i % (PetType.values().length + 1);
            createPets(member, petCnt == 0 ? 1 : petCnt); // 1 2 3 1 1
        }

        memberRepository.saveAll(members);

        // when
        List<CollectedPetRankItem> result = overallRankQueryService.getTop10MembersByCollectedPetCnt();
        Member tester10 = members.get(9);
        Member tester8 = members.get(7);
        Member tester6 = members.get(5);
        Member tester4 = members.get(3);
        Member tester2 = members.get(1);
        Member tester1 = members.get(0);
        Member tester3 = members.get(2);
        Member tester5 = members.get(4);
        Member tester7 = members.get(6);
        Member tester9 = members.get(8);

        // then
        assertThat(result)
                .hasSize(10)
                .extracting("memberId", "username", "profileImageUrl", "petCnt", "title.name", "title.color")
                .containsExactly(
                        tuple(tester6.getId(), tester6.getUsername(), tester6.getProfileImageUrl(), 3, "TEST", "#000000"),
                        tuple(tester4.getId(), tester4.getUsername(), tester4.getProfileImageUrl(), 2, "TEST", "#000000"),
                        tuple(tester2.getId(), tester2.getUsername(), tester2.getProfileImageUrl(), 1, "TEST", "#000000"),
                        tuple(tester8.getId(), tester8.getUsername(), tester8.getProfileImageUrl(), 1, "TEST", "#000000"),
                        tuple(tester10.getId(), tester10.getUsername(), tester10.getProfileImageUrl(), 1, "TEST", "#000000"),
                        tuple(tester1.getId(), tester1.getUsername(), tester1.getProfileImageUrl(), 0, null, null),
                        tuple(tester3.getId(), tester3.getUsername(), tester3.getProfileImageUrl(), 0, null, null),
                        tuple(tester5.getId(), tester5.getUsername(), tester5.getProfileImageUrl(), 0, null, null),
                        tuple(tester7.getId(), tester7.getUsername(), tester7.getProfileImageUrl(), 0, null, null),
                        tuple(tester9.getId(), tester9.getUsername(), tester9.getProfileImageUrl(), 0, null, null)
                );
    }

    @Test
    @DisplayName("전날 가장 많은 투두를 수행한 탑 10 유저를 조회한다.")
    void getRankingOfDailyAchievement() {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Member> members = createAndSaveMembers(12);
        members.forEach(this::createTitleName);
        memberRepository.saveAll(members);
        members.forEach(member -> {
            List<Todo> todoList = createManySingleTodo(yesterday, member, 10);
        });
        // 투두 수행
        for (int i = 1; i <= 5; i++) { // tester2, tester4, tester6, tester8, tester10
            Member member = members.get(2 * i - 1);
            LocalDateTime startAt = yesterday.atStartOfDay();
            LocalDateTime endAt = LocalDateTime.of(yesterday, LocalTime.of(23, 59, 59, 999999999));
            List<Todo> todos = todoRepository.findSingleTodosByWriterIdAndDate(member.getId(), startAt, endAt);
            // 각각 1, 2, 3, 4, 5개 수행
            for (int j = 0; j < i; j++) {
                Todo todo = todos.get(j);
                todo.updateIsDone(true);
            }
            // 투두 수행 내역 저장
            TodoAchievementHistory history = TodoAchievementHistory.builder()
                    .memberId(member.getId())
                    .date(yesterday)
                    .cnt((long) i)
                    .build();

            todoAchievementHistoryRepository.save(history);
        }

        // when
        List<TodoAchievementRankItem> result = overallRankQueryService.getRankingOfDailyAchievement();
        Member tester2 = members.get(1);
        Member tester4 = members.get(3);
        Member tester6 = members.get(5);
        Member tester8 = members.get(7);
        Member tester10 = members.get(9);

        // then
        assertThat(result)
                .hasSize(5)
                .extracting("memberId", "username", "profileImageUrl", "cnt", "title.name", "title.color")
                .containsExactly(
                        tuple(tester10.getId(), tester10.getUsername(), tester10.getProfileImageUrl(), 5L, "TEST", "#000000"),
                        tuple(tester8.getId(), tester8.getUsername(), tester8.getProfileImageUrl(), 4L, "TEST", "#000000"),
                        tuple(tester6.getId(), tester6.getUsername(), tester6.getProfileImageUrl(), 3L, "TEST", "#000000"),
                        tuple(tester4.getId(), tester4.getUsername(), tester4.getProfileImageUrl(), 2L, "TEST", "#000000"),
                        tuple(tester2.getId(), tester2.getUsername(), tester2.getProfileImageUrl(), 1L, "TEST", "#000000")
                );
    }

    @Test
    @DisplayName("지난 주 가장 많은 투두를 수행한 탑 10 유저를 조회한다.")
    void getRankingOfWeeklyAchievement() {
        LocalDate startOfCurrentWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startOfLastWeek = startOfCurrentWeek.minusWeeks(1);
        LocalDate endOfLastWeek = startOfCurrentWeek.minusDays(1);
        List<Member> members = createAndSaveMembers(12);
        members.forEach(this::createTitleName);
        memberRepository.saveAll(members);
        members.forEach(member -> {
            List<Todo> todos = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                Todo todo = getSingleAlldayTodo(startOfLastWeek.plusDays(i), member);
                todos.add(todo);
            }
            todoRepository.saveAll(todos);
        });
        // 투두 수행
        for (int i = 1; i <= 5; i++) { // tester2, tester4, tester6, tester8, tester10
            Member member = members.get(2 * i - 1);
            LocalDateTime startAt = startOfLastWeek.atStartOfDay();
            LocalDateTime endAt = LocalDateTime.of(
                    endOfLastWeek,
                    LocalTime.of(23, 59, 59, 999999999)
            );
            List<Todo> todos = todoRepository.findSingleTodosByWriterIdAndDate(member.getId(), startAt, endAt);
            // 각각 1, 2, 3, 4, 5개 수행
            for (int j = 0; j < i; j++) {
                Todo todo = todos.get(j);
                todo.updateIsDone(true);
                TodoAchievementHistory history = TodoAchievementHistory.builder()
                        .memberId(member.getId())
                        .date(todo.getStartAt().toLocalDate())
                        .cnt((long) i)
                        .build();
                todoAchievementHistoryRepository.save(history);
            }

        }

        // when
        List<TodoAchievementRankItem> result = overallRankQueryService.getRankingOfWeeklyAchievement();
        Member tester2 = members.get(1);
        Member tester4 = members.get(3);
        Member tester6 = members.get(5);
        Member tester8 = members.get(7);
        Member tester10 = members.get(9);

        // then
        assertThat(result)
                .hasSize(5)
                .extracting("memberId", "username", "profileImageUrl", "cnt", "title.name", "title.color")
                .containsExactly(
                        tuple(tester10.getId(), tester10.getUsername(), tester10.getProfileImageUrl(), 5L, "TEST", "#000000"),
                        tuple(tester8.getId(), tester8.getUsername(), tester8.getProfileImageUrl(), 4L, "TEST", "#000000"),
                        tuple(tester6.getId(), tester6.getUsername(), tester6.getProfileImageUrl(), 3L, "TEST", "#000000"),
                        tuple(tester4.getId(), tester4.getUsername(), tester4.getProfileImageUrl(), 2L, "TEST", "#000000"),
                        tuple(tester2.getId(), tester2.getUsername(), tester2.getProfileImageUrl(), 1L, "TEST", "#000000")
                );
    }

    private List<Todo> createManySingleTodo(LocalDate date, Member member, int cnt) {
        return IntStream.rangeClosed(0, cnt)
                .mapToObj(i -> getSingleAlldayTodo(date, member)
                ).toList();
    }

    private Todo getSingleAlldayTodo(LocalDate date, Member member) {
        return testTodoFactory.createSingleTodo(
                date.atStartOfDay(),
                date.atStartOfDay(),
                true,
                member);
    }

    private List<Member> createAndSaveMembers(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(this::createMember)
                .toList();
    }

    private void createPets(Member member, int cnt) {
        IntStream.rangeClosed(0, cnt - 1)
                .forEach(i -> {
                    Pet pet = Pet.builder()
                            .petType(PetType.values()[i])
                            .rarity(Rarity.COMMON)
                            .build();
                    member.addPet(pet);

                    CollectedPet collectedPet = CollectedPet.of(pet);
                    member.addCollection(collectedPet);
                });
    }

    private Member createMember(int index) {
        String username = "tester" + index;
        Member member = Member.builder()
                .username(username)
                .email(username + "@test.com")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google_" + username)
                .role(Role.ROLE_USER)
                .profileImageUrl("profileImageUrl")
                .build();
        member.initDiligence();
        return member;
    }

    private void createTitleName(Member member) {
        TitleName titleName = TitleName.builder()
                .name("TEST")
                .color("#000000")
                .build();
        member.setTitleName(titleName);
    }

}