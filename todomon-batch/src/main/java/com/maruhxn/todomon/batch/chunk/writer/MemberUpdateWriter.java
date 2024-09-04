package com.maruhxn.todomon.batch.chunk.writer;

import com.maruhxn.todomon.batch.service.MemberStateUpdateService;
import com.maruhxn.todomon.batch.vo.MemberAchievementDTO;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MemberUpdateWriter implements ItemWriter<Member> {

    private final List<MemberAchievementDTO> processedMembers;
    private final MemberStateUpdateService memberStateUpdateService;

    @Override
    public void write(Chunk<? extends Member> chunk) throws Exception {
        List<? extends Member> members = chunk.getItems();
        members.forEach(member -> {
            processedMembers.add(new MemberAchievementDTO(member.getId(), member.getDailyAchievementCnt()));
            memberStateUpdateService.addStarAndResetAchieveCnt(member);
        });

        log.info("============ Member Status Update Completed ============");
    }
}
