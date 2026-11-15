package com.courseerpbackend.javaqerar.service;

import com.courseerpbackend.javaqerar.dto.AhpResult;
import org.springframework.stereotype.Service;

/**
 * Classic Saaty AHP: derives criteria weights and the consistency ratio (CR)
 * from a user-supplied pairwise comparison matrix.
 */
@Service
public class AhpService {

    // Saaty's random index table for n = 1..10
    private static final double[] RANDOM_INDEX = {0, 0, 0.58, 0.90, 1.12, 1.24, 1.32, 1.41, 1.45, 1.49};

    public AhpResult computeWeights(double[][] pairwiseMatrix) {
        int n = pairwiseMatrix.length;
        validateMatrix(pairwiseMatrix, n);

        double[] columnSums = new double[n];
        for (double[] row : pairwiseMatrix) {
            for (int j = 0; j < n; j++) {
                columnSums[j] += row[j];
            }
        }

        double[][] normalized = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                normalized[i][j] = pairwiseMatrix[i][j] / columnSums[j];
            }
        }

        double[] weights = new double[n];
        for (int i = 0; i < n; i++) {
            double rowSum = 0;
            for (int j = 0; j < n; j++) {
                rowSum += normalized[i][j];
            }
            weights[i] = rowSum / n;
        }

        double[] weightedSum = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (int j = 0; j < n; j++) {
                sum += pairwiseMatrix[i][j] * weights[j];
            }
            weightedSum[i] = sum;
        }

        double lambdaMax = 0;
        for (int i = 0; i < n; i++) {
            lambdaMax += weightedSum[i] / weights[i];
        }
        lambdaMax /= n;

        double consistencyIndex = (n <= 2) ? 0 : (lambdaMax - n) / (n - 1);
        double randomIndex = (n - 1 < RANDOM_INDEX.length) ? RANDOM_INDEX[n - 1] : RANDOM_INDEX[RANDOM_INDEX.length - 1];
        double consistencyRatio = (n <= 2 || randomIndex == 0) ? 0 : consistencyIndex / randomIndex;

        boolean consistent = consistencyRatio <= 0.1;

        return new AhpResult(weights, lambdaMax, consistencyIndex, consistencyRatio, consistent);
    }

    private void validateMatrix(double[][] matrix, int n) {
        if (n == 0) {
            throw new IllegalArgumentException("Pairwise matrix must not be empty");
        }
        for (double[] row : matrix) {
            if (row.length != n) {
                throw new IllegalArgumentException("Pairwise matrix must be square (NxN)");
            }
        }
        for (int i = 0; i < n; i++) {
            if (Math.abs(matrix[i][i] - 1.0) > 1e-6) {
                throw new IllegalArgumentException("Pairwise matrix diagonal must be 1");
            }
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] <= 0) {
                    throw new IllegalArgumentException("Pairwise matrix values must be positive");
                }
            }
        }
    }
}
