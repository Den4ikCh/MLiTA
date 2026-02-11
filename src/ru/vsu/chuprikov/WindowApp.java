package ru.vsu.chuprikov;

import org.w3c.dom.ls.LSOutput;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.Arrays;


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
        JPanel controlPanel = new JPanel(new FlowLayout());

        rowsField = new JTextField("3", 3);
        colsField = new JTextField("3", 3);

        JButton createTableBtn = new JButton("Задать размер матрицы");
        JButton loadBtn = new JButton("Загрузить из файла");
        JButton calculateBtn = new JButton("Вычислить определитель");
        JButton methodCramerBtn = new JButton("Вычислить методом Крамера");

        controlPanel.add(new JLabel("Строки:"));
        controlPanel.add(rowsField);
        controlPanel.add(new JLabel("Столбцы:"));
        controlPanel.add(colsField);
        controlPanel.add(createTableBtn);
        controlPanel.add(loadBtn);
        controlPanel.add(calculateBtn);
        controlPanel.add(methodCramerBtn);

        tableModel = new DefaultTableModel(3, 3) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Double.class;
            }
        };
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tableModel.setValueAt(0, i, j);
            }
        }
        JScrollPane scrollPane = new JScrollPane(table);

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        createTableBtn.addActionListener(e -> createTable());
        loadBtn.addActionListener(e -> loadFromFile());
        calculateBtn.addActionListener(e -> calculcateMatrix());
        methodCramerBtn.addActionListener(e -> methodCramer());
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

    private void calculcateMatrix() {
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

    private void methodCramer() {
        try {
            double[][] array = readArrayFromTable();

            long startTime = System.nanoTime();
            double[] result = Matrix.methodCramer(array);
            long endTime = System.nanoTime();

            double durationSeconds = (endTime - startTime) / 1e9;

            String message;
            if (result == null) {
                message = "Система не имеет решений.";
            } else if (result.length == 0) {
                message = "Система имеет бесконечно много решений.";
            }
            else {
                message = String.format("Результат: %s. Время работы программы %f секунд.",
                                Arrays.toString(result), durationSeconds);
            }
            JOptionPane.showMessageDialog(this, message);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка при вычислении: " + e.getMessage());
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