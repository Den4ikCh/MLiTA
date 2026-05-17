package ru.vsu.chuprikov;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class WindowApp extends JFrame {
    private String path = System.getProperty("user.dir") + "\\src\\ru\\vsu\\chuprikov\\";
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField rowsField;
    private JTextField colsField;

    public WindowApp() {
        setTitle("Работа с матрицами");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        initializeComponents();
    }

    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.setBorder(BorderFactory.createTitledBorder("Размер матрицы"));

        rowsField = new JTextField("3", 3);
        colsField = new JTextField("3", 3);

        JButton createTableBtn = new JButton("Задать размер");

        row1.add(new JLabel("Строки:"));
        row1.add(rowsField);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(new JLabel("Столбцы:"));
        row1.add(colsField);
        row1.add(Box.createHorizontalStrut(20));
        row1.add(createTableBtn);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.setBorder(BorderFactory.createTitledBorder("Операции"));

        JButton loadBtn = new JButton("Загрузить из файла");
        JButton calculateBtn = new JButton("Вычислить определитель");
        JButton methodCramerBtn = new JButton("Метод Крамера");
        JButton methodGaussianBtn = new JButton("Метод Гаусса");
        JButton inverseMatrixBtn = new JButton("Обратная матрица");
        JButton inverseMethodBtn = new JButton("Метод обратной матрицы");
        JButton eigenBtn = new JButton("Собственные значения/векторы");

        row2.add(loadBtn);
        row2.add(Box.createHorizontalStrut(5));
        row2.add(calculateBtn);
        row2.add(Box.createHorizontalStrut(5));
        row2.add(methodCramerBtn);
        row2.add(Box.createHorizontalStrut(5));
        row2.add(methodGaussianBtn);
        row2.add(Box.createHorizontalStrut(5));
        row2.add(inverseMatrixBtn);
        row2.add(inverseMethodBtn);
        row2.add(Box.createHorizontalStrut(5));
        row2.add(eigenBtn);

        controlPanel.add(row1);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(row2);

        tableModel = new DefaultTableModel(3, 3) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Double.class;
            }
        };

        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        for (int i = 0; i < 3; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(80);
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tableModel.setValueAt(0.0, i, j);
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Введите матрицу"));

        createTableBtn.addActionListener(e -> createTable());
        loadBtn.addActionListener(e -> loadFromFile());
        calculateBtn.addActionListener(e -> calculateMatrix());
        methodCramerBtn.addActionListener(e -> methodCramer());
        methodGaussianBtn.addActionListener(e -> methodGaussian());
        inverseMatrixBtn.addActionListener(e -> inverseMatrix());
        inverseMethodBtn.addActionListener(e -> methodInverseMatrix());
        eigenBtn.addActionListener(e -> calculateEigen());

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    private void inverseMatrix() {
        try {
            double[][] array = readArrayFromTable();

            if (array.length != array[0].length) {
                JOptionPane.showMessageDialog(this, "Матрица должна быть квадратной для нахождения обратной");
                return;
            }

            long startTime = System.nanoTime();
            double[][] inverse = Matrix.getInverseMatrix(array);
            long endTime = System.nanoTime();

            double durationSeconds = (endTime - startTime) / 1e9;

            displayArrayInTable(inverse);

            double[][] product = Matrix.getMatrixMultiply(array, inverse);

            boolean isIdentity = true;
            for (int i = 0; i < product.length; i++) {
                for (int j = 0; j < product.length; j++) {
                    if (i == j) {
                        if (Math.abs(product[i][j] - 1.0) > 1e-9) {
                            isIdentity = false;
                            break;
                        }
                    } else {
                        if (Math.abs(product[i][j]) > 1e-9) {
                            isIdentity = false;
                            break;
                        }
                    }
                }
            }

            StringBuilder message = new StringBuilder();
            message.append("Обратная матрица найдена и отображена в таблице.\n\n");
            message.append("Проверка: A * A⁻¹ = \n");
            for (int i = 0; i < Math.min(5, product.length); i++) {
                for (int j = 0; j < Math.min(5, product.length); j++) {
                    message.append(String.format("%8.4f ", product[i][j]));
                }
                message.append("\n");
            }

            if (product.length > 5) {
                message.append("...\n");
            }

            if (isIdentity) {
                message.append("\nРезультат: Единичная матрица ✓");
            } else {
                message.append("\nРезультат: НЕ единичная матрица ✗");
            }

            message.append(String.format("\n\nВремя: %.9f сек.", durationSeconds));

            JOptionPane.showMessageDialog(this, message.toString(), "Обратная матрица", JOptionPane.INFORMATION_MESSAGE);
            updateStatus("Обратная матрица найдена");

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
        }
    }

    private void methodInverseMatrix() {
        try {
            double[][] array = readArrayFromTable();

            if (array[0].length != array.length + 1) {
                JOptionPane.showMessageDialog(this,
                        "Для метода обратной матрицы нужно " + array.length +
                                " строк и " + (array.length + 1) + " столбцов");
                return;
            }

            long startTime = System.nanoTime();
            double[] result = Matrix.methodInverseMatrix(array);
            long endTime = System.nanoTime();

            double durationSeconds = (endTime - startTime) / 1e9;

            if (result == null) {
                JOptionPane.showMessageDialog(this, "Матрица вырождена, обратной не существует");
            } else {
                StringBuilder sb = new StringBuilder("Решение методом обратной матрицы:\n\n");
                for (int i = 0; i < result.length; i++) {
                    sb.append(String.format("x%d = %.4f\n", i + 1, result[i]));
                }
                sb.append(String.format("\nВремя: %.9f сек.", durationSeconds));
                JOptionPane.showMessageDialog(this, sb.toString());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
        }
    }

    private void calculateEigen() {
        try {
            double[][] array = readArrayFromTable();

            if (array.length != array[0].length) {
                JOptionPane.showMessageDialog(this, "Матрица должна быть квадратной");
                return;
            }

            long startTime = System.nanoTime();
            //Object[] result = Matrix.getEigenValuesAndVectors(array);
            Object[] result = new Object[] {null, null, "Данная функция пока не поддерживается"};
            long endTime = System.nanoTime();

            double durationSeconds = (endTime - startTime) / 1e9;

            String error = (String) result[2];

            if (error != null) {
                JOptionPane.showMessageDialog(this, error);
                return;
            }

            double[] eigenValues = (double[]) result[0];
            double[][] eigenVectors = (double[][]) result[1];

            StringBuilder sb = new StringBuilder("Собственные значения и векторы:\n\n");

            for (int i = 0; i < eigenValues.length; i++) {
                sb.append(String.format("λ%d = %.4f\n", i + 1, eigenValues[i]));
                sb.append("Вектор: (");
                for (int j = 0; j < eigenVectors[i].length; j++) {
                    sb.append(String.format("%.4f", eigenVectors[i][j]));
                    if (j < eigenVectors[i].length - 1) sb.append(", ");
                }
                sb.append(")\n\n");
            }

            sb.append(String.format("Время: %.9f сек.", durationSeconds));
            JOptionPane.showMessageDialog(this, sb.toString());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
        }
    }

    private void updateStatus(String text) {
        Component component = ((BorderLayout)getContentPane().getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        if (component instanceof JLabel) {
            ((JLabel) component).setText(" " + text);
        }
    }

    private void createTable() {
        try {
            int rows = Integer.parseInt(rowsField.getText());
            int cols = Integer.parseInt(colsField.getText());
            if (rows <= 0 || cols <= 0) {
                throw new Exception();
            }

            tableModel.setRowCount(rows);
            tableModel.setColumnCount(cols);

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    tableModel.setValueAt(0, i, j);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Введите натуральное число");
        }
    }

    private void loadFromFile() {
        JFileChooser fileChooser = new JFileChooser(path);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(".txt");
            }
            @Override
            public String getDescription() {
                return "Текстовые файлы (*.txt)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                double[][] array = Matrix.readMatrixFromFile(fileChooser.getSelectedFile().getName());
                displayArrayInTable(array);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки файла: " + ex.getMessage());
            }
        }
    }

    private void calculateMatrix() {
        try {
            double[][] array = readArrayFromTable();

            long startTime = System.nanoTime();
            double result = Matrix.determinantCalculation(array);
            long endTime = System.nanoTime();

            double durationSeconds = (endTime - startTime) / 1e9;

            String message = String.format("Результат: %.6f. Время работы программы %.9f секунд.",
                    result, durationSeconds);
            JOptionPane.showMessageDialog(this, message);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Все элементы должны быть числами (формат: 123 или 123.456)");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка при вычислении: " + e.getMessage());
        }
    }

    private void methodGaussian() {
        try {
            double[][] array = readArrayFromTable();

            if (array[0].length != array.length + 1) {
                JOptionPane.showMessageDialog(this,
                        "Для метода Гаусса матрица должна иметь " + array.length +
                                " строк и " + (array.length + 1) + " столбцов");
                return;
            }

            long startTime = System.nanoTime();
            List<Object> results = Matrix.methodGaussian(array);
            long endTime = System.nanoTime();

            double durationSeconds = (endTime - startTime) / 1e9;

            if (results == null) {
                JOptionPane.showMessageDialog(this, "Система не имеет решений.");
                updateStatus("Метод Гаусса: нет решений");
            } else {
                double[][] transformed = (double[][]) results.get(0);
                int[] pivotColumns = (int[]) results.get(1);
                List<Integer> freeVars = (List<Integer>) results.get(2);

                displayArrayInTable(transformed);

                if (freeVars.isEmpty()) {
                    double[] solution = new double[array.length];
                    for (int i = 0; i < array.length; i++) {
                        solution[i] = transformed[i][array.length];
                    }

                    StringBuilder sb = new StringBuilder("Единственное решение:\n\n");
                    for (int i = 0; i < solution.length; i++) {
                        sb.append(String.format("x%d = %.4f\n", i + 1, solution[i]));
                    }
                    sb.append(String.format("\nВремя: %.9f сек.", durationSeconds));
                    JOptionPane.showMessageDialog(this, sb.toString());
                } else {
                    StringBuilder sb = new StringBuilder("Система имеет бесконечно много решений.\n\n");
                    sb.append("Выражение через свободные переменные:\n\n");

                    for (int i = 0; i < array.length; i++) {
                        if (pivotColumns[i] != -1) {
                            sb.append(String.format("x%d = ", pivotColumns[i] + 1));
                            double constTerm = transformed[i][array.length];
                            if (Math.abs(constTerm) > 1e-10) {
                                sb.append(String.format("%.2f", constTerm));
                            }

                            for (int freeVar : freeVars) {
                                double coeff = -transformed[i][freeVar];
                                if (Math.abs(coeff) > 1e-10) {
                                    if (coeff > 0 && sb.toString().contains("=")) {
                                        sb.append(" + ");
                                    } else if (coeff < 0) {
                                        sb.append(" - ");
                                    }
                                    sb.append(String.format("%.2f·x%d", Math.abs(coeff), freeVar + 1));
                                }
                            }

                            if (!sb.toString().contains("x")) {
                                sb.append("0");
                            }
                            sb.append("\n");
                        }
                    }

                    sb.append("\nСвободные переменные: ");
                    for (int i = 0; i < freeVars.size(); i++) {
                        sb.append(String.format("x%d", freeVars.get(i) + 1));
                        if (i < freeVars.size() - 1) sb.append(", ");
                    }

                    sb.append(String.format("\n\nВремя: %.9f сек.", durationSeconds));
                    JOptionPane.showMessageDialog(this, sb.toString());
                }
                updateStatus("Метод Гаусса выполнен");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
        }
    }

    private void methodCramer() {
        try {
            double[][] array = readArrayFromTable();

            if (array[0].length != array.length + 1) {
                JOptionPane.showMessageDialog(this,
                        "Для метода Крамера матрица должна иметь " + array.length +
                                " строк и " + (array.length + 1) + " столбцов");
                return;
            }

            long startTime = System.nanoTime();
            Object[] results = Matrix.methodCramer(array);
            long endTime = System.nanoTime();

            double durationSeconds = (endTime - startTime) / 1e9;

            double[] solution = (double[]) results[0];
            double[][] transformed = (double[][]) results[1];
            int[] pivotColumns = (int[]) results[2];
            List<Integer> freeVars = (List<Integer>) results[3];

            if (solution == null && transformed == null) {
                JOptionPane.showMessageDialog(this, "Система не имеет решений.");
            } else if (solution != null) {
                StringBuilder sb = new StringBuilder("Единственное решение:\n\n");
                for (int i = 0; i < solution.length; i++) {
                    sb.append(String.format("x%d = %.4f\n", i + 1, solution[i]));
                }
                sb.append(String.format("\nВремя: %.9f сек.", durationSeconds));
                JOptionPane.showMessageDialog(this, sb.toString());
            } else {
                displayArrayInTable(transformed);

                StringBuilder sb = new StringBuilder("Система имеет бесконечно много решений.\n\n");
                sb.append("Выражение через свободные переменные:\n\n");

                for (int i = 0; i < array.length; i++) {
                    if (pivotColumns[i] != -1) {
                        sb.append(String.format("x%d = ", pivotColumns[i] + 1));
                        double constTerm = transformed[i][array.length];
                        if (Math.abs(constTerm) > 1e-10) {
                            sb.append(String.format("%.2f", constTerm));
                        }

                        for (int freeVar : freeVars) {
                            double coeff = -transformed[i][freeVar];
                            if (Math.abs(coeff) > 1e-10) {
                                if (coeff > 0 && sb.toString().contains("=")) {
                                    sb.append(" + ");
                                } else if (coeff < 0) {
                                    sb.append(" - ");
                                }
                                sb.append(String.format("%.2f·x%d", Math.abs(coeff), freeVar + 1));
                            }
                        }

                        if (!sb.toString().contains("x")) {
                            sb.append("0");
                        }
                        sb.append("\n");
                    }
                }

                sb.append("\nСвободные переменные: ");
                for (int i = 0; i < freeVars.size(); i++) {
                    sb.append(String.format("x%d", freeVars.get(i) + 1));
                    if (i < freeVars.size() - 1) sb.append(", ");
                }

                sb.append(String.format("\n\nВремя: %.9f сек.", durationSeconds));
                JOptionPane.showMessageDialog(this, sb.toString());
            }
            updateStatus("Метод Крамера выполнен");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
        }
    }

    private double[][] readArrayFromTable() {
        int rows = tableModel.getRowCount();
        int cols = tableModel.getColumnCount();
        double[][] array = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Object value = tableModel.getValueAt(i, j);
                if (value == null || value.toString().trim().isEmpty()) {
                    array[i][j] = 0.0;
                } else {
                    String strValue = value.toString().trim().replace(',', '.');
                    array[i][j] = Double.parseDouble(strValue);
                }
            }
        }
        return array;
    }

    private void displayArrayInTable(double[][] array) {
        tableModel.setRowCount(array.length);
        tableModel.setColumnCount(array.length > 0 ? array[0].length : 0);

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                tableModel.setValueAt(array[i][j], i, j);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new WindowApp().setVisible(true);
            }
        });
    }
}