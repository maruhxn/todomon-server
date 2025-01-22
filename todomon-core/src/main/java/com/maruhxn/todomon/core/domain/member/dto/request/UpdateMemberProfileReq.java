package com.maruhxn.todomon.core.domain.member.dto.request;

import com.maruhxn.todomon.core.global.util.validation.AtLeastOneFieldNotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@Getter
@NoArgsConstructor
@AtLeastOneFieldNotNull(message = "수정할 데이터를 전달해야 합니다.")
public class UpdateMemberProfileReq {

    @Size(min = 2, max = 20, message = "유저명은 2 ~ 20글자입니다.")
    private String username;

    private MultipartFile profileImage;

    @Builder
    public UpdateMemberProfileReq(String username, MultipartFile profileImage) {
        this.username = username;
        this.profileImage = profileImage;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProfileImage(MultipartFile profileImage) {
        this.profileImage = profileImage;
    }
}
