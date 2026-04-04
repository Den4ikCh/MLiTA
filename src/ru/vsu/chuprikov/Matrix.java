package ru.vsu.chuprikov;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

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

    public static double[] methodCramer(double[][] matrix) throws IllegalArgumentException {
        if (matrix.length != matrix[0].length - 1) {
            throw new IllegalArgumentException("Невозможно применить метод Крамера к данной матрице");
        }
        double[] result = new double[matrix.length];

        double[][] temp = new double[matrix.length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < temp.length; j++) {
                temp[i][j] = matrix[i][j];
            }
        }
        double determinant = determinantCalculation(temp);

        boolean isAllDeterminantsZero = true;

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
            if (determinant1 != 0) {
                isAllDeterminantsZero = false;
            }
            result[i] = determinant1 / determinant;
        }

        if (Math.abs(determinant) < 1e-10) {
            if (isAllDeterminantsZero) {
                return new double[0]; //бесконечно много решений
            } else {
                return null; //нет решений
            }
        }

        return result;
    }

    public static List<Object> methodGaussian(double[][] matrix) throws IllegalArgumentException {
        if (matrix.length != matrix[0].length - 1) {
            throw new IllegalArgumentException("Невозможно применить метод Гаусса к данной матрице");
        }

        int n = matrix.length;
        double[] result = new double[n];

        double[][] temp = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= n; j++) {
                temp[i][j] = matrix[i][j];
            }
        }

        for (int i = 0; i < n; i++) {
            int index = -1;

            for (int j = i; j < n; j++) {
                if (Math.abs(temp[j][i]) > 1e-10) {
                    index = j;
                    break;
                }
            }

            if (index == -1) {
                boolean hasSolution = true;
                for (int j = i; j < n; j++) {
                    if (Math.abs(temp[j][n]) > 1e-10) {
                        hasSolution = false;
                        break;
                    }
                }
                if (!hasSolution) {
                    return null;
                }
                continue;
            }

            if (index != i) {
                double[] temp1 = temp[i];
                temp[i] = temp[index];
                temp[index] = temp1;
            }

            double pivot = temp[i][i];
            for (int j = i; j <= n; j++) {
                temp[i][j] /= pivot;
            }

            for (int j = 0; j < n; j++) {
                if (j == i || Math.abs(temp[j][i]) < 1e-10) {
                    continue;
                }
                double factor = temp[j][i];
                for (int k = i; k <= n; k++) {
                    temp[j][k] -= factor * temp[i][k];
                }
            }
        }

        for (int i = 0; i < n; i++) {
            boolean allZeros = true;
            for (int j = 0; j < n; j++) {
                if (Math.abs(temp[i][j]) > 1e-10) {
                    allZeros = false;
                    break;
                }
            }
            if (allZeros && Math.abs(temp[i][n]) < 1e-10) {
                return new ArrayList<>();
            }
        }

        for (int i = 0; i < n; i++) {
            result[i] = temp[i][n];
        }

        ArrayList<Object> list = new ArrayList<>();
        list.add(result);
        list.add(temp);
        return list;
    }

    public static double[][] getInverseMatrix(double[][] matrix) throws IllegalArgumentException {
        if (matrix.length != matrix[0].length) {
            throw new IllegalArgumentException("Данная матрица не является квадратной");
        }

        int n = matrix.length;
        double[][] result = new double[n][n];

        double determinant = determinantCalculation(matrix);

        if (Math.abs(determinant) < 1e-10) {
            throw new IllegalArgumentException("Определитель матрицы равен 0, обратная матрица не существует");
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double[][] temp = new double[n - 1][n - 1];
                int x = 0;
                int y = 0;
                for (int k = 0; k < n; k++) {
                    for (int l = 0; l < n; l++) {
                        if (k != i && l != j) {
                            temp[x][y] = matrix[k][l];
                            y++;
                            if (y == n - 1) {
                                y = 0;
                                x++;
                            }
                        }
                    }
                }
                result[j][i] = Math.pow(-1, i + j) * determinantCalculation(temp) / determinant;
            }
        }

        return result;
    }

    public static double[][] getMatrixMultiply(double[][] matrix1, double matrix2[][]) throws IllegalArgumentException {
        if (matrix1[0].length != matrix2.length) {
            throw new IllegalArgumentException("Нельзя найти произведение данных матриц. Количество столбцов первой матрицы должно равняться количеству строк второй");
        }

        double[][] result = new double[matrix1.length][matrix2[0].length];
        int n = matrix1[0].length;

        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                for (int k = 0; k < n; k++) {
                    result[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
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
