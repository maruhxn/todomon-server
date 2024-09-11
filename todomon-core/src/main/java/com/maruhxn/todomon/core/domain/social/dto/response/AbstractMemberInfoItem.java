package com.maruhxn.todomon.core.domain.social.dto.response;

import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class AbstractMemberInfoItem {

    private String username;
    private String profileImageUrl;
    private TitleNameItem title;

    public AbstractMemberInfoItem(String username, String profileImageUrl, TitleNameItem title) {
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.title = title;
    }

    public void setTitleToNullIfIsEmpty() {
        if (title.name == null && title.color == null) {
            this.title = null;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class TitleNameItem {

        private String name;
        private String color;

        public TitleNameItem(String name, String color) {
            this.name = name;
            this.color = color;
        }

        public TitleNameItem from(TitleName titleName) {
            return new TitleNameItem(titleName.getName(), titleName.getColor());
        }
    }
}
