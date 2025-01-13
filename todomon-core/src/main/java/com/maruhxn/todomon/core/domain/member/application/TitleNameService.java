package com.maruhxn.todomon.core.domain.member.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.dto.request.UpsertTitleNameRequest;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.member.implement.TitleNameCreator;
import com.maruhxn.todomon.core.domain.member.implement.TitleNameReader;
import com.maruhxn.todomon.core.domain.member.implement.TitleNameRemover;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TitleNameService {

    private final MemberReader memberReader;
    private final TitleNameCreator titleNameCreator;
    private final TitleNameReader titleNameReader;
    private final TitleNameRemover titleNameRemover;

    public void upsertTitleName(Member member, UpsertTitleNameRequest req) {
        titleNameReader.findByMember_Id(member.getId())
                .ifPresentOrElse(
                        tn -> tn.update(req.getName(), req.getColor()),
                        () -> titleNameCreator.create(member, req)
                );
    }

    public void deleteTitleName(Long memberId) {
        Member member = memberReader.findById(memberId);
        titleNameRemover.delete(member);
    }
}
