package com.courseerpbackend.javaqerar.service;

import com.courseerpbackend.javaqerar.dto.AhpResult;
import com.courseerpbackend.javaqerar.dto.AlternativeScore;
import com.courseerpbackend.javaqerar.dto.ComparisonRequest;
import com.courseerpbackend.javaqerar.dto.ComparisonResponse;
import com.courseerpbackend.javaqerar.dto.ComputerDto;
import com.courseerpbackend.javaqerar.model.Criterion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComparisonService {

    private final AhpService ahpService;
    private final TopsisService topsisService;

    public ComparisonResponse compare(ComparisonRequest request) {
        List<ComputerDto> computers = request.getComputers();

        AhpResult ahpResult = ahpService.computeWeights(request.getPairwiseMatrix());
        if (ahpResult.getWeights().length != Criterion.ORDER.length) {
            throw new IllegalArgumentException(
                    "Pairwise matrix must be " + Criterion.ORDER.length + "x" + Criterion.ORDER.length + " "
                            + "(criteria order: " + Arrays.toString(Criterion.ORDER) + ")");
        }

        String[] names = computers.stream().map(ComputerDto::getName).toArray(String[]::new);
        double[][] decisionMatrix = buildDecisionMatrix(computers);

        List<AlternativeScore> scores = topsisService.rank(names, decisionMatrix, ahpResult.getWeights());
        String winner = scores.stream()
                .filter(s -> s.getRank() == 1)
                .findFirst()
                .map(AlternativeScore::getName)
                .orElse(null);

        List<String> criteriaNames = Arrays.stream(Criterion.ORDER).map(Enum::name).toList();
        return new ComparisonResponse(criteriaNames, ahpResult, scores, winner);
    }

    private double[][] buildDecisionMatrix(List<ComputerDto> computers) {
        double[][] matrix = new double[computers.size()][Criterion.ORDER.length];
        for (int i = 0; i < computers.size(); i++) {
            ComputerDto c = computers.get(i);
            matrix[i][0] = c.getCpuScore();
            matrix[i][1] = c.getRamGB();
            matrix[i][2] = c.getStorageGB();
            matrix[i][3] = c.getGpuScore();
            matrix[i][4] = c.getPrice();
        }
        return matrix;
    }
}
