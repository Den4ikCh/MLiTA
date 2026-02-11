package ru.vsu.chuprikov;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Matrix {
    static String path = System.getProperty("user.dir") + "\\src\\ru\\vsu\\chuprikov\\";

    public static double determinantCalculation(double[][] matrix) throws IllegalArgumentException {
        if (matrix.length != matrix[0].length) {
            throw new IllegalArgumentException("Данная матрица не является квадратной");
        }
        if (matrix.length == 1) {
            return matrix[0][0];
        }
        if (matrix.length == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        }
        double sum = 0;
        for (int i = 0; i < matrix.length; i++) {
            double[][] temp = new double[matrix.length - 1][matrix.length - 1];
            int x = 0;
            int y = 0;
            for (int j = 0; j < matrix.length; j++) {
                for (int k = 1; k < matrix.length; k++) {
                    if (j != i) {
                        temp[x][y] = matrix[j][k];
                        y++;
                        if (y == temp.length) {
                            y = 0;
                            x++;
                        }
                    }
                }
            }
            sum += matrix[i][0] * Math.pow(-1, i) * determinantCalculation(temp);
        }
        return sum;
    }

    public static double[] methodCramer(double[][] matrix) {
        double[] result = new double[matrix.length];

        double[][] temp = new double[matrix.length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < temp.length; j++) {
                temp[i][j] = matrix[i][j];
            }
        }
        double determinant = determinantCalculation(temp);

        for (int i = 0; i < result.length; i++) {
            double[][] temp1 = new double[matrix.length][matrix.length];
            for (int j = 0; j < temp.length; j++) {
                for (int k = 0; k < temp.length; k++) {
                    temp1[j][k] = temp[j][k];
                    if (k == i) {
                        temp1[j][k] = matrix[j][temp[0].length];
                    }
                }
            }
            double determinant1 = determinantCalculation(temp1);
            result[i] = determinant1 / determinant;
        }

        return result;
    }

    public static double[][] readMatrixFromFile(String filename) throws FileNotFoundException {
        File file = new File(path + filename);

        if (!file.exists()) {
            throw new FileNotFoundException(filename + " not found");
        }

        List<double[]> rows = new ArrayList<>();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                double[] row = new double[parts.length];

                for (int i = 0; i < parts.length; i++) {
                    row[i] = Double.parseDouble(parts[i].replace(',', '.'));
                }

                rows.add(row);
            }
        }

        double[][] result = new double[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            result[i] = rows.get(i);
        }

        return result;
    }
}
