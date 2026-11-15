package com.courseerpbackend.javaqerar.service;

import com.courseerpbackend.javaqerar.dto.AlternativeScore;
import com.courseerpbackend.javaqerar.model.Criterion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Classic TOPSIS ranking: vector-normalizes the decision matrix, applies the
 * AHP-derived weights, then ranks alternatives by closeness coefficient to
 * the ideal solution.
 */
@Service
public class TopsisService {

    public List<AlternativeScore> rank(String[] alternativeNames, double[][] decisionMatrix, double[] weights) {
        int alternatives = decisionMatrix.length;
        int criteriaCount = weights.length;

        double[] columnNorms = new double[criteriaCount];
        for (int j = 0; j < criteriaCount; j++) {
            double sumSquares = 0;
            for (int i = 0; i < alternatives; i++) {
                sumSquares += decisionMatrix[i][j] * decisionMatrix[i][j];
            }
            columnNorms[j] = Math.sqrt(sumSquares);
        }

        double[][] weighted = new double[alternatives][criteriaCount];
        for (int i = 0; i < alternatives; i++) {
            for (int j = 0; j < criteriaCount; j++) {
                double normalized = columnNorms[j] == 0 ? 0 : decisionMatrix[i][j] / columnNorms[j];
                weighted[i][j] = normalized * weights[j];
            }
        }

        double[] idealBest = new double[criteriaCount];
        double[] idealWorst = new double[criteriaCount];
        for (int j = 0; j < criteriaCount; j++) {
            double max = weighted[0][j];
            double min = weighted[0][j];
            for (int i = 1; i < alternatives; i++) {
                max = Math.max(max, weighted[i][j]);
                min = Math.min(min, weighted[i][j]);
            }
            boolean benefit = Criterion.ORDER[j].isBenefit();
            idealBest[j] = benefit ? max : min;
            idealWorst[j] = benefit ? min : max;
        }

        List<AlternativeScore> result = new ArrayList<>();
        for (int i = 0; i < alternatives; i++) {
            double distBest = 0;
            double distWorst = 0;
            for (int j = 0; j < criteriaCount; j++) {
                distBest += Math.pow(weighted[i][j] - idealBest[j], 2);
                distWorst += Math.pow(weighted[i][j] - idealWorst[j], 2);
            }
            distBest = Math.sqrt(distBest);
            distWorst = Math.sqrt(distWorst);
            double closeness = (distBest + distWorst) == 0 ? 0 : distWorst / (distBest + distWorst);
            result.add(new AlternativeScore(alternativeNames[i], distBest, distWorst, closeness, 0));
        }

        result.sort(Comparator.comparingDouble(AlternativeScore::getClosenessCoefficient).reversed());
        for (int rank = 0; rank < result.size(); rank++) {
            result.get(rank).setRank(rank + 1);
        }
        return result;
    }
}
