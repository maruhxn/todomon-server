package com.maruhxn.todomon.batch.chunk.writer;

import com.maruhxn.todomon.batch.vo.MemberAchievementDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class MemberUpdateWriter implements ItemWriter<MemberAchievementDTO> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void write(Chunk<? extends MemberAchievementDTO> chunk) throws Exception {
//        List<? extends MemberAchievementDTO> members = chunk.getItems();
//
//        String sql = ;
//
//        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
//            @Override
//            public void setValues(PreparedStatement ps, int i) throws SQLException {
//                MemberAchievementDTO dto = members.get(i);
//
//                ps.setLong(1, dto.getMemberId());
//            }
//
//            @Override
//            public int getBatchSize() {
//                return 100;
//            }
//        });
    }
}
