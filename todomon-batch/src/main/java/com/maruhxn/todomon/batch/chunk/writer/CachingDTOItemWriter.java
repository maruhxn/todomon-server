package com.maruhxn.todomon.batch.chunk.writer;

import com.maruhxn.todomon.batch.vo.MemberAchievementDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@RequiredArgsConstructor
public class CachingDTOItemWriter implements ItemWriter<MemberAchievementDTO> {

    private final List<MemberAchievementDTO> processedMembers;

    @Override
    public void write(Chunk<? extends MemberAchievementDTO> chunk) throws Exception {
        processedMembers.addAll(chunk.getItems());
    }
}