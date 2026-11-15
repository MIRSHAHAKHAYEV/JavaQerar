package com.courseerpbackend.javaqerar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlternativeScore {

    private String name;
    private double distanceToBest;
    private double distanceToWorst;
    private double closenessCoefficient;
    private int rank;
}
