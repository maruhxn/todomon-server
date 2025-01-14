package com.maruhxn.todomon.core.domain.member.implement;

import com.maruhxn.todomon.core.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.domain.member.dto.request.UpsertTitleNameReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TitleNameWriter {

    private final TitleNameRepository titleNameRepository;
    private final TitleNameReader reader;

    public void create(Member member, UpsertTitleNameReq req) {
        TitleName titleName = req.toEntity();
        member.setTitleName(titleName);
        titleNameRepository.save(titleName);
    }

    public void delete(TitleName titleName) {
        titleNameRepository.delete(titleName);
    }
}
