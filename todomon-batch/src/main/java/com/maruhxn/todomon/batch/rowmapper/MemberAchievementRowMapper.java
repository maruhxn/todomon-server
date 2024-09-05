package com.maruhxn.todomon.batch.rowmapper;

import com.maruhxn.todomon.batch.vo.MemberAchievementDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberAchievementRowMapper implements RowMapper<MemberAchievementDTO> {

    private final String date;

    public MemberAchievementRowMapper(String date) {
        this.date = date;
    }

    @Override
    public MemberAchievementDTO mapRow(ResultSet rs, int i) throws SQLException {
        return new MemberAchievementDTO(
                rs.getLong("id"),
                rs.getLong("daily_achievement_cnt"),
                date
        );
    }
}
