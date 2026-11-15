package com.courseerpbackend.javaqerar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComputerDto {

    @NotBlank
    private String name;

    @Positive
    private double cpuScore;

    @Positive
    private double ramGB;

    @Positive
    private double storageGB;

    @Positive
    private double gpuScore;

    @Positive
    private double price;
}
