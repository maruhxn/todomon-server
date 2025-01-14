package com.maruhxn.todomon.core.domain.member.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.domain.member.dto.request.UpsertTitleNameReq;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.member.implement.TitleNameReader;
import com.maruhxn.todomon.core.domain.member.implement.TitleNameWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TitleNameService {

    private final MemberReader memberReader;
    private final TitleNameReader titleNameReader;
    private final TitleNameWriter titleNameWriter;

    public void upsertTitleName(Member member, UpsertTitleNameReq req) {
        titleNameReader.findByMember_Id(member.getId())
                .ifPresentOrElse(
                        tn -> tn.update(req.getName(), req.getColor()),
                        () -> titleNameWriter.create(member, req)
                );
    }

    public void deleteTitleName(Long memberId) {
        Member member = memberReader.findById(memberId);
        TitleName titleName = titleNameReader.findByMemberId(member.getId());
        member.setTitleName(null);
        titleNameWriter.delete(titleName);
    }
}
