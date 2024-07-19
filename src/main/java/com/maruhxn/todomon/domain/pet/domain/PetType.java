package com.maruhxn.todomon.domain.pet.domain;

import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import lombok.Getter;

import java.util.Random;

@Getter
public enum PetType {

    DOG(
            new EvolutionStage("강아지", "\uD83D\uDC36"),
            new EvolutionStage("개", "\uD83D\uDC15"),
            new EvolutionStage("푸들", "\uD83D\uDC29")
    ),
    HORSE(
            new EvolutionStage("조랑말", "\uD83D\uDC34"),
            new EvolutionStage("말", "\uD83D\uDC0E"),
            new EvolutionStage("유니콘", "\uD83E\uDD84")
    ),
    CAT(
            new EvolutionStage("새끼 고양이", "\uD83D\uDC31"),
            new EvolutionStage("고양이", "🐈"),
            new EvolutionStage("검은 고양이", "\uD83D\uDC08\u200D⬛")
    );

    private final EvolutionStage[] evolutionStages;

    PetType(EvolutionStage... evolutionStages) {
        this.evolutionStages = evolutionStages;
    }

    public int getEvolutionaryCnt() {
        return evolutionStages.length - 1;
    }

    public static PetType getRandomPetType() {
        Random random = new Random();
        return PetType.values()[random.nextInt(0, PetType.values().length)];
    }

    public EvolutionStage getEvolutionStage(int index) {
        if (index < 0 || index >= evolutionStages.length) {
            throw new BadRequestException(
                    ErrorCode.BAD_REQUEST,
                    String.format("해당 펫은 %d차 진화 형태가 존재하지 않습니다.", index)
            );
        }
        return evolutionStages[index];
    }

    @Getter
    public static class EvolutionStage {
        private final String name;
        private final String form;

        public EvolutionStage(String name, String form) {
            this.name = name;
            this.form = form;
        }
    }

}
