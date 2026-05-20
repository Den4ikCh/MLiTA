package ru.vsu.chuprikov;

import org.apache.commons.math4.legacy.linear.EigenDecomposition;
import org.apache.commons.math4.legacy.linear.MatrixUtils;
import org.apache.commons.math4.legacy.linear.RealMatrix;
import org.apache.commons.math4.legacy.linear.RealVector;

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

    public static Object[] methodCramer(double[][] matrix) throws IllegalArgumentException {
        if (matrix.length != matrix[0].length - 1) {
            throw new IllegalArgumentException("Невозможно применить метод Крамера к данной матрице");
        }

        int n = matrix.length;

        double[][] temp = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                temp[i][j] = matrix[i][j];
            }
        }

        double determinant = determinantCalculation(temp);

        if (Math.abs(determinant) < 1e-10) {
            boolean hasSolution = true;

            for (int i = 0; i < n; i++) {
                double[][] temp1 = new double[n][n];
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        temp1[j][k] = temp[j][k];
                        if (k == i) {
                            temp1[j][k] = matrix[j][n];
                        }
                    }
                }
                double determinant1 = determinantCalculation(temp1);
                if (Math.abs(determinant1) > 1e-10) {
                    hasSolution = false;
                    break;
                }
            }

            if (!hasSolution) {
                return new Object[] {null, null, null, null};
            }

            List<Object> gaussResult = methodGaussian(matrix);
            double[][] transformed = (double[][]) gaussResult.get(0);
            int[] pivotColumns = (int[]) gaussResult.get(1);
            List<Integer> freeVars = (List<Integer>) gaussResult.get(2);

            for (int i = 0; i < n; i++) {
                boolean allZeros = true;
                for (int j = 0; j < n; j++) {
                    if (Math.abs(transformed[i][j]) > 1e-10) {
                        allZeros = false;
                        break;
                    }
                }
                if (allZeros && Math.abs(transformed[i][n]) > 1e-10) {
                    return new Object[]{null, null, null, null};
                }
            }

            if (freeVars.isEmpty()) {
                double[] solution = new double[n];
                for (int i = 0; i < n; i++) {
                    solution[i] = transformed[i][n];
                }
                return new Object[]{solution, null, null, null};
            } else {
                return new Object[]{null, transformed, pivotColumns, freeVars};
            }
        }

        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            double[][] temp1 = new double[n][n];
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    temp1[j][k] = temp[j][k];
                    if (k == i) {
                        temp1[j][k] = matrix[j][n];
                    }
                }
            }
            result[i] = determinantCalculation(temp1) / determinant;
        }

        return new Object[]{result, null, null, null};
    }

    public static List<Object> methodGaussian(double[][] matrix) throws IllegalArgumentException {
        if (matrix.length != matrix[0].length - 1) {
            throw new IllegalArgumentException("Невозможно применить метод Гаусса к данной матрице");
        }

        int n = matrix.length;
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
            if (allZeros && Math.abs(temp[i][n]) > 1e-10) {
                return null;
            }
        }

        int[] pivotColumns = new int[n];
        for (int i = 0; i < n; i++) {
            pivotColumns[i] = -1;
            for (int j = 0; j < n; j++) {
                if (Math.abs(temp[i][j]) > 1e-10) {
                    pivotColumns[i] = j;
                    break;
                }
            }
        }

        List<Integer> freeVars = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            boolean isPivot = false;
            for (int i = 0; i < n; i++) {
                if (pivotColumns[i] == j) {
                    isPivot = true;
                    break;
                }
            }
            if (!isPivot) {
                freeVars.add(j);
            }
        }

        List<Object> result = new ArrayList<>();
        result.add(temp);
        result.add(pivotColumns);
        result.add(freeVars);

        return result;
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

    public static double[] methodInverseMatrix(double[][] matrix) throws IllegalArgumentException {
        if (matrix[0].length != matrix.length + 1) {
            throw new IllegalArgumentException("Для метода обратной матрицы матрица должна иметь " +
                    matrix.length + " строк и " + (matrix.length + 1) + " столбцов");
        }

        int n = matrix.length;

        double[][] A = new double[n][n]; //матрица левой части
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = matrix[i][j];
            }
        }

        double[] B = new double[n]; //матрица правой части
        for (int i = 0; i < n; i++) {
            B[i] = matrix[i][n];
        }

        double[][] inverseA = getInverseMatrix(A);

        if (inverseA == null) {
            return null;
        }

        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i] += inverseA[i][j] * B[j]; //умножаем обратную матрицу A на B
            }
        }

        return result;
    }

    public static Object[] getEigenValuesAndVectors(double[][] matrix) {
        int n = matrix.length;

        double[] eigenValues;

        if (n == 2) {
            eigenValues = getEigenValues2x2(matrix);
            if (eigenValues == null) {
                return new Object[]{null, null, "Комплексные собственные значения"};
            }
        } else {
            try {
                RealMatrix realMatrix = MatrixUtils.createRealMatrix(matrix);
                EigenDecomposition eigenDecomp = new EigenDecomposition(realMatrix);
                eigenValues = eigenDecomp.getRealEigenvalues();
            } catch (Exception e) {
                return new Object[]{null, null, "Ошибка: " + e.getMessage()};
            }
        }

        double[][] eigenVectors = new double[n][n];
        boolean[] used = new boolean[n];

        for (int i = 0; i < n; i++) {
            if (used[i]) continue;

            List<Integer> sameIndices = new ArrayList<>();
            for (int j = i; j < n; j++) {
                if (Math.abs(eigenValues[j] - eigenValues[i]) < 1e-10) {
                    sameIndices.add(j);
                    used[j] = true;
                }
            }

            List<double[]> vectors = getEigenVectorsForLambda(matrix, eigenValues[i], sameIndices.size());
            for (int k = 0; k < vectors.size(); k++) {
                eigenVectors[sameIndices.get(k)] = vectors.get(k);
            }
        }

        return new Object[]{eigenValues, eigenVectors, null};
    }

    private static double[] getEigenValues2x2(double[][] A) {
        double a = A[0][0], b = A[0][1];
        double c = A[1][0], d = A[1][1];

        double trace = a + d;
        double det = a * d - b * c;
        double D = trace * trace - 4 * det;

        if (D < -1e-10) {
            return null;
        }

        D = Math.max(0, D);
        double sqrtD = Math.sqrt(D);

        return new double[] {(trace + sqrtD) / 2, (trace - sqrtD) / 2};
    }

    private static List<double[]> getEigenVectorsForLambda(double[][] A, double lambda, int count) {
        int n = A.length;

        double[][] M = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                M[i][j] = A[i][j] - (i == j ? lambda : 0); //A - лямбда * E
            }
            M[i][n] = 0;
        }

        List<Object> gaussResult = methodGaussian(M); //находим решение методом Гаусса
        if (gaussResult == null) return new ArrayList<>();

        double[][] transformed = (double[][]) gaussResult.get(0);
        List<Integer> freeVars = (List<Integer>) gaussResult.get(2);

        List<double[]> vectors = new ArrayList<>();

        for (int freeVar : freeVars) {
            double[] v = new double[n];
            v[freeVar] = 1.0; //задаём свободной переменной 1

            for (int i = n - 1; i >= 0; i--) {
                int pivotCol = -1;
                for (int j = 0; j < n; j++) {
                    if (Math.abs(transformed[i][j]) > 1e-10) {
                        pivotCol = j;
                        break;
                    }
                }
                if (pivotCol != -1 && pivotCol != freeVar) {
                    double sum = 0;
                    for (int j = pivotCol + 1; j < n; j++) {
                        sum += transformed[i][j] * v[j];
                    }
                    v[pivotCol] = -sum / transformed[i][pivotCol]; //выражаем через свободные переменные
                }
            }

            double norm = 0;
            for (double val : v) norm += val * val;
            norm = Math.sqrt(norm);
            if (norm > 1e-10) {
                for (int i = 0; i < n; i++) v[i] /= norm; //нормируем все векторы до длины 1
            }

            vectors.add(v);
            if (vectors.size() >= count) break;
        }

        return vectors;
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
