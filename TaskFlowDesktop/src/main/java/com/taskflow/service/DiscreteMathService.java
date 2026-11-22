package com.taskflow.service;

import com.taskflow.model.Task;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Demonstrates set theory, Cartesian products, relations, and propositional logic.
 * Sets use task IDs so that each task has a unique identity.
 */
public class DiscreteMathService {
    public record OrderedPair<A, B>(A first, B second) {
        @Override public String toString() { return "(" + first + ", " + second + ")"; }
    }

    public record TruthRow(boolean p, boolean q, boolean and, boolean or, boolean implication) {}

    public Set<Integer> taskIdsByStatus(Collection<Task> tasks, Task.Status status) {
        return tasks.stream().filter(t -> t.getStatus() == status)
            .map(Task::getId).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Integer> taskIdsByCategory(Collection<Task> tasks, String category) {
        return tasks.stream().filter(t -> t.getCategory().equalsIgnoreCase(category))
            .map(Task::getId).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public <T> Set<T> union(Set<T> a, Set<T> b) {
        Set<T> result = new LinkedHashSet<>(a);
        result.addAll(b);
        return result;
    }

    public <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Set<T> result = new LinkedHashSet<>(a);
        result.retainAll(b);
        return result;
    }

    public <T> Set<T> difference(Set<T> a, Set<T> b) {
        Set<T> result = new LinkedHashSet<>(a);
        result.removeAll(b);
        return result;
    }

    public <A, B> Set<OrderedPair<A, B>> cartesianProduct(Set<A> a, Set<B> b) {
        Set<OrderedPair<A, B>> result = new LinkedHashSet<>();
        for (A x : a) for (B y : b) result.add(new OrderedPair<>(x, y));
        return result;
    }

    public List<OrderedPair<Integer, LocalDate>> taskDeadlineRelation(Collection<Task> tasks) {
        return tasks.stream()
            .map(t -> new OrderedPair<>(t.getId(), t.getDeadline()))
            .collect(Collectors.toList());
    }

    // R contains (a,b) when a and b have the same category.
    public Set<OrderedPair<Integer, Integer>> sameCategoryRelation(Collection<Task> tasks) {
        Set<OrderedPair<Integer, Integer>> relation = new LinkedHashSet<>();
        for (Task a : tasks) {
            for (Task b : tasks) {
                if (a.getCategory().equalsIgnoreCase(b.getCategory())) {
                    relation.add(new OrderedPair<>(a.getId(), b.getId()));
                }
            }
        }
        return relation;
    }

    public boolean isReflexive(Collection<Task> tasks,
                               Set<OrderedPair<Integer, Integer>> relation) {
        for (Task task : tasks) {
            if (!relation.contains(new OrderedPair<>(task.getId(), task.getId()))) return false;
        }
        return true;
    }

    public boolean isSymmetric(Set<OrderedPair<Integer, Integer>> relation) {
        for (OrderedPair<Integer, Integer> pair : relation) {
            if (!relation.contains(new OrderedPair<>(pair.second(), pair.first()))) return false;
        }
        return true;
    }

    public boolean isTransitive(Set<OrderedPair<Integer, Integer>> relation) {
        for (OrderedPair<Integer, Integer> first : relation) {
            for (OrderedPair<Integer, Integer> second : relation) {
                if (first.second().equals(second.first())
                    && !relation.contains(new OrderedPair<>(first.first(), second.second()))) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isEligibleForToday(Task task) {
        boolean incomplete = task.getStatus() != Task.Status.COMPLETED; // P
        boolean dueToday = task.getDeadline().equals(LocalDate.now());  // Q
        return incomplete && dueToday; // P AND Q
    }

    public List<TruthRow> andOrImplicationTruthTable() {
        List<TruthRow> rows = new ArrayList<>();
        boolean[] values = {true, false};
        for (boolean p : values) {
            for (boolean q : values) {
                rows.add(new TruthRow(p, q, p && q, p || q, !p || q));
            }
        }
        return rows;
    }
}