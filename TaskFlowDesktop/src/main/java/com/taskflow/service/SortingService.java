package com.taskflow.service;

import com.taskflow.model.Task;
import java.util.*;
import java.util.function.Consumer;

/**
 * Sorting algorithms are implemented manually. A trace line is emitted after
 * each meaningful pass/partition/merge so learners can inspect the process.
 */
public class SortingService {
    public enum Algorithm { BUBBLE, SELECTION, INSERTION, MERGE, QUICK }
    public enum Field { DEADLINE, PRIORITY, TITLE, CREATED }

    public List<Task> sort(List<Task> source, Algorithm algorithm, Field field,
                           Consumer<String> trace) {
        List<Task> data = new ArrayList<>(source);
        Comparator<Task> comparator = comparator(field);
        switch (algorithm) {
            case BUBBLE -> bubble(data, comparator, trace);
            case SELECTION -> selection(data, comparator, trace);
            case INSERTION -> insertion(data, comparator, trace);
            case MERGE -> mergeSort(data, 0, data.size() - 1, comparator, trace);
            case QUICK -> quickSort(data, 0, data.size() - 1, comparator, trace);
        }
        return data;
    }

    private Comparator<Task> comparator(Field field) {
        return switch (field) {
            case DEADLINE -> Comparator.comparing(Task::getDeadline);
            case PRIORITY -> Comparator.comparingInt(t -> t.getPriority().rank());
            case TITLE -> Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER);
            case CREATED -> Comparator.comparing(Task::getCreatedAt);
        };
    }

    private void bubble(List<Task> a, Comparator<Task> c, Consumer<String> log) {
        for (int i = 0; i < a.size() - 1; i++) {
            boolean changed = false;
            for (int j = 0; j < a.size() - 1 - i; j++) {
                if (c.compare(a.get(j), a.get(j + 1)) > 0) {
                    Collections.swap(a, j, j + 1); changed = true;
                }
            }
            log.accept("Pass " + (i + 1) + ": " + labels(a));
            if (!changed) break;
        }
    }

    private void selection(List<Task> a, Comparator<Task> c, Consumer<String> log) {
        for (int i = 0; i < a.size() - 1; i++) {
            int min = i;
            for (int j = i + 1; j < a.size(); j++)
                if (c.compare(a.get(j), a.get(min)) < 0) min = j;
            Collections.swap(a, i, min);
            log.accept("Pass " + (i + 1) + " (selected minimum): " + labels(a));
        }
    }

    private void insertion(List<Task> a, Comparator<Task> c, Consumer<String> log) {
        for (int i = 1; i < a.size(); i++) {
            Task key = a.get(i);
            int j = i - 1;
            while (j >= 0 && c.compare(a.get(j), key) > 0) {
                a.set(j + 1, a.get(j)); j--;
            }
            a.set(j + 1, key);
            log.accept("Pass " + i + " (inserted " + key.getTitle() + "): " + labels(a));
        }
    }

    private void mergeSort(List<Task> a, int left, int right, Comparator<Task> c, Consumer<String> log) {
        if (left >= right) return;
        int mid = (left + right) / 2;
        mergeSort(a, left, mid, c, log);
        mergeSort(a, mid + 1, right, c, log);
        List<Task> merged = new ArrayList<>();
        int i = left, j = mid + 1;
        while (i <= mid && j <= right)
            merged.add(c.compare(a.get(i), a.get(j)) <= 0 ? a.get(i++) : a.get(j++));
        while (i <= mid) merged.add(a.get(i++));
        while (j <= right) merged.add(a.get(j++));
        for (int k = 0; k < merged.size(); k++) a.set(left + k, merged.get(k));
        log.accept("Merged range " + left + "-" + right + ": " + labels(a));
    }

    private void quickSort(List<Task> a, int low, int high, Comparator<Task> c, Consumer<String> log) {
        if (low >= high) return;
        int p = partition(a, low, high, c);
        log.accept("Partitioned around pivot " + a.get(p).getTitle() + ": " + labels(a));
        quickSort(a, low, p - 1, c, log);
        quickSort(a, p + 1, high, c, log);
    }

    private int partition(List<Task> a, int low, int high, Comparator<Task> c) {
        Task pivot = a.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (c.compare(a.get(j), pivot) <= 0) {
                i++; Collections.swap(a, i, j);
            }
        }
        Collections.swap(a, i + 1, high);
        return i + 1;
    }

    private String labels(List<Task> tasks) {
        List<String> names = new ArrayList<>();
        for (Task t : tasks) names.add(t.getTitle());
        return names.toString();
    }
}