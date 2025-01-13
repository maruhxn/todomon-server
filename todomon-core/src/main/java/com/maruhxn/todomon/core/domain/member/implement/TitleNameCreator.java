package com.maruhxn.todomon.core.domain.member.implement;

import com.maruhxn.todomon.core.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.domain.member.dto.request.UpsertTitleNameRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TitleNameCreator {

    private final TitleNameRepository titleNameRepository;

    public void create(Member member, UpsertTitleNameRequest req) {
        TitleName titleName = req.toEntity();
        member.setTitleName(titleName);
        titleNameRepository.save(titleName);
    }
}
