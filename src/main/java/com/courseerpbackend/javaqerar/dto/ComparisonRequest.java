package com.courseerpbackend.javaqerar.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ComparisonRequest {

    /**
     * Exactly 2 computers, in the order they should be compared.
     */
    @NotNull
    @Size(min = 2, max = 2, message = "Exactly 2 computers are required for comparison")
    @Valid
    private List<ComputerDto> computers;

    /**
     * 5x5 AHP pairwise comparison matrix for the fixed criteria order:
     * CPU, RAM, STORAGE, GPU, PRICE.
     */
    @NotNull
    private double[][] pairwiseMatrix;
}
