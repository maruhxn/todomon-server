package com.maruhxn.todomon.core.domain.member.application;

import com.maruhxn.todomon.core.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.domain.member.dto.request.UpsertTitleNameRequest;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TitleNameService {

    private final TitleNameRepository titleNameRepository;

    public void upsertTitleName(Member member, UpsertTitleNameRequest req) {
        titleNameRepository.findByMember_Id(member.getId())
                .ifPresentOrElse(
                        tn -> {
                            if (req.getName() == null && req.getColor() == null)
                                throw new BadRequestException(ErrorCode.VALIDATION_ERROR, "수정할 내용을 입력해주세요.");
                            tn.update(req.getName(), req.getColor());
                        },
                        () -> {
                            TitleName titleName = TitleName.builder()
                                    .name(req.getName())
                                    .color(req.getColor())
                                    .member(member)
                                    .build();
                            member.setTitleName(titleName);

                            titleNameRepository.save(titleName);
                        });
    }

    public void deleteTitleName(Member member) {
        TitleName findTitleName = titleNameRepository.findByMember_Id(member.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TITLE_NAME));

        titleNameRepository.delete(findTitleName);
    }
}
