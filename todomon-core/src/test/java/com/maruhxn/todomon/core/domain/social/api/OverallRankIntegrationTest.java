package com.maruhxn.todomon.core.domain.social.api;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.domain.PetType;
import com.maruhxn.todomon.core.domain.pet.domain.Rarity;
import com.maruhxn.todomon.core.domain.todo.dao.TodoAchievementHistoryRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoAchievementHistory;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.util.ControllerIntegrationTestSupport;
import com.maruhxn.todomon.util.TestTodoFactory;
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

import static com.maruhxn.todomon.core.global.auth.application.JwtProvider.BEARER_PREFIX;
import static com.maruhxn.todomon.core.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.core.global.common.Constants.REFRESH_TOKEN_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] - OverallRank")
class OverallRankIntegrationTest extends ControllerIntegrationTestSupport {

    static final String OVERALL_RANK_BASE_URL = "/api/overall/rank";

    @Autowired
    TestTodoFactory testTodoFactory;

    @Autowired
    TodoRepository todoRepository;

    @Autowired
    TodoAchievementHistoryRepository todoAchievementHistoryRepository;

    @Test
    @DisplayName("GET /api/overall/rank/achievement/daily - 전체 일간 투두 달성 랭킹 조회")
    void getOverallRankingOfDailyAchievement() throws Exception {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Member> members = createAndSaveMembers(12);
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

        Member tester10 = members.get(9);

        // when / then
        mockMvc.perform(
                        get(OVERALL_RANK_BASE_URL + "/achievement/daily")
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("전체 일간 투두 달성 랭킹 조회 성공"))
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data[0].memberId").value(tester10.getId()))
                .andExpect(jsonPath("data[0].username").value(tester10.getUsername()))
                .andExpect(jsonPath("data[0].profileImageUrl").value(tester10.getProfileImageUrl()))
                .andExpect(jsonPath("data[0].cnt").value(5L));
    }

    @Test
    @DisplayName("GET /api/overall/rank/achievement/weekly - 전체 주간 투두 달성 랭킹 조회")
    void getOverallRankingOfWeeklyAchievement() throws Exception {
        // given
        LocalDate startOfCurrentWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startOfLastWeek = startOfCurrentWeek.minusWeeks(1);
        LocalDate endOfLastWeek = startOfCurrentWeek.minusDays(1);
        List<Member> members = createAndSaveMembers(12);
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

        Member tester10 = members.get(9);

        // when / then
        mockMvc.perform(
                        get(OVERALL_RANK_BASE_URL + "/achievement/weekly")
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("전체 주간 투두 달성 랭킹 조회 성공"))
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data[0].memberId").value(tester10.getId()))
                .andExpect(jsonPath("data[0].username").value(tester10.getUsername()))
                .andExpect(jsonPath("data[0].profileImageUrl").value(tester10.getProfileImageUrl()))
                .andExpect(jsonPath("data[0].cnt").value(5L));
    }

    @Test
    @DisplayName("GET /api/overall/rank/diligence - 전체 일관성 레벨 랭킹 조회")
    void getOverallRankingOfDiligence() throws Exception {
        // given
        List<Member> members = createAndSaveMembers(20);
        for (int i = 1; i <= 5; i++) { // tester2, tester4, tester6, tester8, tester10
            Member member = members.get(2 * i - 1);
            member.getDiligence().levelUp(2 * i);
        }
        memberRepository.saveAll(members);
        Member tester10 = members.get(9);

        // when / then
        mockMvc.perform(
                        get(OVERALL_RANK_BASE_URL + "/diligence")
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("전체 일관성 레벨 랭킹 조회 성공"))
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data[0].memberId").value(tester10.getId()))
                .andExpect(jsonPath("data[0].username").value(tester10.getUsername()))
                .andExpect(jsonPath("data[0].profileImageUrl").value(tester10.getProfileImageUrl()))
                .andExpect(jsonPath("data[0].level").value(tester10.getDiligence().getLevel()));
    }

    @Test
    @DisplayName("GET /api/overall/rank/collection - 전체 도감 랭킹 조회")
    void getOverallRankingOfCollection() throws Exception {
        // given
        List<Member> members = createAndSaveMembers(20);
        for (int i = 1; i <= 5; i++) { // tester2, tester4, tester6, tester8, tester10
            Member member = members.get(2 * i - 1);
            int petCnt = i % (PetType.values().length + 1);
            createPets(member, petCnt == 0 ? 1 : petCnt); // 1 2 3 1 1
        }

        memberRepository.saveAll(members);
        Member tester6 = members.get(5);

        // when / then
        mockMvc.perform(
                        get(OVERALL_RANK_BASE_URL + "/collection")
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("전체 도감 랭킹 조회 성공"))
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data[0].memberId").value(tester6.getId()))
                .andExpect(jsonPath("data[0].username").value(tester6.getUsername()))
                .andExpect(jsonPath("data[0].profileImageUrl").value(tester6.getProfileImageUrl()))
                .andExpect(jsonPath("data[0].petCnt").value(3));
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
}