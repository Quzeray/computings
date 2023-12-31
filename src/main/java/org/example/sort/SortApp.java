package org.example.sort;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SortApp {
    private int[] originalArray;
    private int[] arrayBubbleSort;
    private int[] arrayInsertionSort;
    private int[] arraySelectionSort;

    private JList<String> listOriginal;
    private JList<String> listBubbleSort;
    private JList<String> listInsertionSort;
    private JList<String> listSelectionSort;

    private JTextArea logArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SortApp().createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Сортировки");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel arraysPanel = new JPanel(new GridLayout(1, 4));

        listOriginal = new JList<>();
        listBubbleSort = new JList<>();
        listInsertionSort = new JList<>();
        listSelectionSort = new JList<>();

        JScrollPane scrollOriginal = new JScrollPane(listOriginal);
        JScrollPane scrollBubbleSort = new JScrollPane(listBubbleSort);
        JScrollPane scrollInsertionSort = new JScrollPane(listInsertionSort);
        JScrollPane scrollSelectionSort = new JScrollPane(listSelectionSort);

        arraysPanel.add(scrollOriginal);
        arraysPanel.add(scrollBubbleSort);
        arraysPanel.add(scrollInsertionSort);
        arraysPanel.add(scrollSelectionSort);

        logArea = new JTextArea();
        logArea.setEditable(false);

        JButton createButton = new JButton("Создать массив");
        createButton.addActionListener(e -> {
            int arraySize = Integer.parseInt(JOptionPane.showInputDialog("Размер массива:"));
            int minValue = Integer.parseInt(JOptionPane.showInputDialog("Минимальное значение случайных чисел:"));
            int maxValue = Integer.parseInt(JOptionPane.showInputDialog("Максимальное значение случайных числе:"));

            originalArray = generateRandomArray(arraySize, minValue, maxValue);
            arrayBubbleSort = Arrays.copyOf(originalArray, originalArray.length);
            arrayInsertionSort = Arrays.copyOf(originalArray, originalArray.length);
            arraySelectionSort = Arrays.copyOf(originalArray, originalArray.length);

            displayArray(originalArray, listOriginal, "Оригинальный массив");
        });

        JButton sortButton = new JButton("Начать сортировку");
        sortButton.addActionListener(e -> {
            ExecutorService executorService = Executors.newFixedThreadPool(3);

            executorService.submit(createSortingTask(arrayBubbleSort, "Сортировка пузырьком", this::bubbleSort, listBubbleSort));
            executorService.submit(createSortingTask(arrayInsertionSort, "Сортировка вставками", this::insertionSort, listInsertionSort));
            executorService.submit(createSortingTask(arraySelectionSort, "Сортировка выбором", this::selectionSort, listSelectionSort));

            executorService.shutdown();
        });

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(createButton);
        buttonPanel.add(sortButton);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(arraysPanel, BorderLayout.CENTER);
        frame.getContentPane().add(logArea, BorderLayout.SOUTH);
        frame.getContentPane().add(buttonPanel, BorderLayout.NORTH);

        frame.setVisible(true);
    }

    private int[] generateRandomArray(int size, int minValue, int maxValue) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = (int) (Math.random() * (maxValue - minValue + 1) + minValue);
        }
        return array;
    }

    private void displayArray(int[] array, JList<String> list, String label) {
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement(label);
        for (int value : array) {
            model.addElement(String.valueOf(value));
        }
        list.setModel(model);
    }

    private void logStats(String algorithmName, int comparisons, int swaps) {
        logArea.append(algorithmName + ". Количество сравнений: " + comparisons +
                ", Количество перестановок: " + swaps + "\n");
    }

    private void bubbleSort(int[] array) {
        int n = array.length;
        boolean swapped;
        int comparisons = 0;
        int swaps = 0;

        do {
            swapped = false;
            for (int i = 1; i < n; i++) {
                comparisons++;
                if (array[i - 1] > array[i]) {
                    swaps++;
                    int temp = array[i - 1];
                    array[i - 1] = array[i];
                    array[i] = temp;
                    swapped = true;
                }
            }
            n--;
        } while (swapped);

        logStats("Сортировка пузырьком", comparisons, swaps);
    }

    private void insertionSort(int[] array) {
        int n = array.length;
        int comparisons = 0;
        int swaps = 0;

        for (int i = 1; i < n; i++) {
            int key = array[i];
            int j = i - 1;

            while (j >= 0 && array[j] > key) {
                comparisons++;
                swaps++;
                array[j + 1] = array[j];
                j--;
            }

            array[j + 1] = key;
        }

        logStats("Сортировка вставками", comparisons, swaps);
    }

    private void selectionSort(int[] array) {
        int n = array.length;
        int comparisons = 0;
        int swaps = 0;

        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                comparisons++;
                if (array[j] < array[minIndex]) {
                    minIndex = j;
                }
            }

            swaps++;
            int temp = array[i];
            array[i] = array[minIndex];
            array[minIndex] = temp;
        }

        logStats("Сортировка выбором", comparisons, swaps);
    }

    private Runnable createSortingTask(int[] array, String sortMethodName, Consumer<int[]> sortMethod, JList<String> listSort) {
        return () -> {
            long startTime = System.currentTimeMillis();
            sortMethod.accept(array);
            long endTime = System.currentTimeMillis();
            long sortTime = endTime - startTime;

            displayArray(array, listSort, sortMethodName);
            logArea.append(sortMethodName + ". Затраты времени: " + sortTime + " мс\n");
        };
    }
}
