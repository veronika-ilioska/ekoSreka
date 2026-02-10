package com.zinemasterapp.ekosrekja.quiz;

import java.util.List;

public class dto {

    public record QuizListItem(Long id, String title, String level, String description, Integer timeMinutes) {}

    public record QuizQuestionDto(Long id, String text, List<OptionDto> options) {
        public record OptionDto(Long id, String text, Integer ord) {}
    }


    public record QuizResultDto(int score, int total, List<QuestionResult> details) {
        public record QuestionResult(Long questionId, Long chosenOptionId, Long correctOptionId, boolean correct) {}
    }
}

