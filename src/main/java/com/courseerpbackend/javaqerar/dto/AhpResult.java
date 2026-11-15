package com.courseerpbackend.javaqerar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AhpResult {

    private double[] weights;
    private double lambdaMax;
    private double consistencyIndex;
    private double consistencyRatio;
    private boolean consistent;
}
