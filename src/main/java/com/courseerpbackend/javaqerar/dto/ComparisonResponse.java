package com.courseerpbackend.javaqerar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ComparisonResponse {

    private List<String> criteria;
    private AhpResult ahp;
    private List<AlternativeScore> scores;
    private String winner;
}
