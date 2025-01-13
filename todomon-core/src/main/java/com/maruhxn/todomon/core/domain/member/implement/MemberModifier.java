package com.maruhxn.todomon.core.domain.member.implement;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.dto.request.UpdateMemberProfileReq;
import com.maruhxn.todomon.core.infra.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class MemberModifier {

    private final MemberRepository memberRepository;
    private final FileService fileService;

    public void modify(Member member, UpdateMemberProfileReq req) {
        if (StringUtils.hasText(req.getUsername())) member.updateUsername(req.getUsername());
        if (req.getProfileImage() != null) this.updateProfileImage(req.getProfileImage(), member);
    }

    private void updateProfileImage(MultipartFile profileImage, Member member) {
        fileService.deleteProfileImage(member.getProfileImageUrl());
        String newProfileImageUrl = fileService.storeOneFile(profileImage);
        member.updateProfileImageUrl(newProfileImageUrl);
    }
}
