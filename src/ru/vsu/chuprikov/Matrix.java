package ru.vsu.chuprikov;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Matrix {
    static String path = System.getProperty("user.dir") + "\\src\\ru\\vsu\\chuprikov\\";

    public static int determinantCalculation(int[][] matrix) throws IllegalArgumentException {
        if (matrix.length != matrix[0].length) {
            throw new IllegalArgumentException("Данная матрица не является квадратной");
        }
        if (matrix.length == 1) {
            return matrix[0][0];
        }
        if (matrix.length == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        }
        int sum = 0;
        for (int i = 0; i < matrix.length; i++) {
            int[][] temp = new int[matrix.length - 1][matrix.length - 1];
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
            sum += matrix[i][0] * (int) Math.pow(-1, i) * determinantCalculation(temp);
        }
        return sum;
    }

    public static int[][] readMatrixFromFile(String filename) throws FileNotFoundException {
        int[][] matrix = {};
        File file = new File(path + filename);

        if (!file.exists()) {
            throw new FileNotFoundException(filename + " not found");
        }

        List<int[]> rows = new ArrayList<>();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                int[] row = new int[parts.length];

                for (int i = 0; i < parts.length; i++) {
                    row[i] = Integer.parseInt(parts[i]);
                }

                rows.add(row);
            }
        }

        int[][] result = new int[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            result[i] = rows.get(i);
        }

        return result;
    }
}
