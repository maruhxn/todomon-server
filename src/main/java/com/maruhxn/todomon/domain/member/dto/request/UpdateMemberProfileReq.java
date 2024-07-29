package com.maruhxn.todomon.domain.member.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public class UpdateMemberProfileReq {

    @Size(min = 2, max = 20, message = "유저명은 2 ~ 20글자입니다.")
    private String username;

    private MultipartFile profileImage;

    @Builder
    public UpdateMemberProfileReq(String username, MultipartFile profileImage) {
        this.username = username;
        this.profileImage = profileImage;
    }
}
