package com.maruhxn.todomon.core.domain.member.implement;

import com.maruhxn.todomon.core.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TitleNameReader {

    private final TitleNameRepository titleNameRepository;

    public Optional<TitleName> findByMember_Id(Long memberId) {
        return titleNameRepository.findByMember_Id(memberId);
    }

    public TitleName findByMemberId(Long memberId) {
        return titleNameRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TITLE_NAME));
    }
}
