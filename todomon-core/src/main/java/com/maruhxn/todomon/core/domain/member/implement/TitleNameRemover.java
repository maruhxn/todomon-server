package com.maruhxn.todomon.core.domain.member.implement;

import com.maruhxn.todomon.core.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TitleNameRemover {

    private final TitleNameRepository titleNameRepository;
    private final TitleNameReader reader;

    public void delete(Member member) {
        TitleName titleName = reader.findByMemberId(member.getId());
        member.setTitleName(null);
        titleNameRepository.delete(titleName);
    }
}
