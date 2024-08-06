package com.maruhxn.todomon.domain.member.application;

import com.maruhxn.todomon.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.member.domain.TitleName;
import com.maruhxn.todomon.domain.member.dto.request.CreateTitleNameReq;
import com.maruhxn.todomon.domain.member.dto.request.UpdateTitleNameReq;
import com.maruhxn.todomon.domain.member.dto.response.TitleNameItem;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TitleNameService {

    private final TitleNameRepository titleNameRepository;

    @Transactional(readOnly = true)
    public TitleNameItem getTitleName(Member member) {
        TitleName findTitleName = titleNameRepository.findByMember_Id(member.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TITLE_NAME));
        return TitleNameItem.of(findTitleName);
    }

    public void createTitleName(Member member, CreateTitleNameReq req) {
        TitleName titleName = TitleName.builder()
                .name(req.getName())
                .color(req.getColor())
                .member(member)
                .build();
        titleNameRepository.save(titleName);
    }

    public void updateTitleName(Member member, UpdateTitleNameReq req) {
        if (req.getName() == null && req.getColor() == null)
            throw new BadRequestException(ErrorCode.VALIDATION_ERROR, "수정할 내용을 입력해주세요.");
        TitleName findTitleName = titleNameRepository.findByMember_Id(member.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TITLE_NAME));

        findTitleName.update(req.getName(), req.getColor());
    }

    public void deleteTitleName(Member member) {
        TitleName findTitleName = titleNameRepository.findByMember_Id(member.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TITLE_NAME));

        titleNameRepository.delete(findTitleName);
    }
}
