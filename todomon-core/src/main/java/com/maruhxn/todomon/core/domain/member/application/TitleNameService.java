package com.maruhxn.todomon.core.domain.member.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.domain.member.dto.request.UpsertTitleNameRequest;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TitleNameService {

    private final MemberRepository memberRepository;
    private final TitleNameRepository titleNameRepository;

    public void upsertTitleName(Member member, UpsertTitleNameRequest req) {
        titleNameRepository.findByMember_Id(member.getId())
                .ifPresentOrElse(
                        tn -> {
                            tn.update(req.getName(), req.getColor());
                        },
                        () -> {
                            TitleName titleName = req.toEntity();
                            member.setTitleName(titleName);

                            titleNameRepository.save(titleName);
                        });
    }

    public void deleteTitleName(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
        TitleName findTitleName = titleNameRepository.findByMember_Id(findMember.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TITLE_NAME));

        findMember.setTitleName(null);
        titleNameRepository.delete(findTitleName);
    }
}
