package com.maruhxn.todomon.core.domain.member.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.domain.member.dto.request.UpsertTitleNameReq;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.member.implement.TitleNameReader;
import com.maruhxn.todomon.core.domain.member.implement.TitleNameWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TitleNameService {

    private final MemberReader memberReader;
    private final TitleNameReader titleNameReader;
    private final TitleNameWriter titleNameWriter;

    public void upsertTitleName(Member member, UpsertTitleNameReq req) {
        log.info("유저 칭호 수정 === 유저 아이디: {}, 요청 정보: {}", member.getId(), req);
        titleNameReader.findByMember_Id(member.getId())
                .ifPresentOrElse(
                        tn -> tn.update(req.getName(), req.getColor()),
                        () -> titleNameWriter.create(member, req)
                );
    }

    public void deleteTitleName(Long memberId) {
        log.info("유저 칭호 삭제 === 유저 아이디: {}", memberId);
        Member member = memberReader.findById(memberId);
        TitleName titleName = titleNameReader.findByMemberId(member.getId());
        member.setTitleName(null);
        titleNameWriter.delete(titleName);
    }
}
