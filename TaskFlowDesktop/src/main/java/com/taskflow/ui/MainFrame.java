package com.taskflow.ui;

import com.taskflow.model.Task;
import com.taskflow.service.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
    private final TaskService tasks;
    private final DiscreteMathService math = new DiscreteMathService();
    private final SortingService sorting = new SortingService();
    private final JTabbedPane tabs = new JTabbedPane();
    private final DefaultTableModel taskModel = new DefaultTableModel(
        new Object[]{"ID", "Task", "Deadline", "Priority", "Status", "Category"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable taskTable = new JTable(taskModel);
    private final JTextArea mathOutput = outputArea();
    private final JTextArea sortOutput = outputArea();
    private final JLabel dashboardStats = new JLabel();
    private final JPanel dashboardCards = new JPanel(new GridLayout(1, 4, 12, 12));
    private boolean dark;

    public MainFrame(TaskService tasks) {
        this.tasks = tasks;
        setTitle("TaskFlow | Scheduling & Task Management");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setSize(1240, 790);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
        refreshAll();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 22, 16, 22));
        JLabel brand = new JLabel("TASKFLOW");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 23f));
        JLabel subtitle = new JLabel("  Scheduling & Task Management");
        subtitle.setForeground(new Color(125, 135, 150));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.add(brand); left.add(subtitle);
        JButton theme = new JButton("Toggle light / dark");
        theme.addActionListener(e -> {
            dark = !dark;
            ThemeManager.install(dark);
            SwingUtilities.updateComponentTreeUI(this);
        });
        header.add(left, BorderLayout.WEST);
        header.add(theme, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane buildTabs() {
        tabs.addTab("Dashboard", dashboardPanel());
        tabs.addTab("My Tasks", tasksPanel());
        tabs.addTab("Scheduler", schedulerPanel());
        tabs.addTab("Sorting Lab", sortingPanel());
        tabs.addTab("Discrete Math Lab", mathPanel());
        tabs.addTab("Reports", reportsPanel());
        return tabs;
    }

    private JPanel dashboardPanel() {
        JPanel panel = page();
        JLabel heading = heading("Good day. Here's your task overview.");
        panel.add(heading, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 18));
        dashboardCards.setOpaque(false);
        center.add(dashboardCards, BorderLayout.NORTH);
        JPanel summary = cardPanel();
        summary.setLayout(new BorderLayout(8, 8));
        summary.add(sectionTitle("Upcoming tasks"), BorderLayout.NORTH);
        dashboardStats.setVerticalAlignment(SwingConstants.TOP);
        summary.add(dashboardStats, BorderLayout.CENTER);
        center.add(summary, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel tasksPanel() {
        JPanel panel = page();
        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);
        top.add(heading("My Tasks"), BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JTextField search = new JTextField(16);
        JButton find = new JButton("Search");
        JButton add = new JButton("+ New Task");
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");
        JButton refresh = new JButton("Refresh");
        actions.add(search); actions.add(find); actions.add(add);
        actions.add(edit); actions.add(delete); actions.add(refresh);
        top.add(actions, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        taskTable.setRowHeight(30);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(taskTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        add.addActionListener(e -> {
            try {
                Task t = TaskDialog.show(this, tasks.categories(), null);
                if (t != null) { tasks.create(t.getTitle(), t.getDescription(), t.getDeadline(),
                    t.getPriority(), t.getCategory()); refreshAll(); }
            } catch (Exception ex) { error(ex); }
        });
        edit.addActionListener(e -> {
            Task selected = selectedTask();
            if (selected == null) return;
            try {
                Task updated = TaskDialog.show(this, tasks.categories(), selected);
                if (updated != null) { tasks.update(updated); refreshAll(); }
            } catch (Exception ex) { error(ex); }
        });
        delete.addActionListener(e -> {
            Task selected = selectedTask();
            if (selected == null) return;
            if (JOptionPane.showConfirmDialog(this, "Delete \"" + selected.getTitle() + "\"?",
                    "Confirm deletion", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try { tasks.delete(selected.getId()); refreshAll(); } catch (Exception ex) { error(ex); }
            }
        });
        refresh.addActionListener(e -> refreshAll());
        find.addActionListener(e -> {
            try { loadTable(tasks.search(search.getText())); }
            catch (Exception ex) { error(ex); }
        });
        search.addActionListener(e -> find.doClick());
        return panel;
    }

    private JPanel schedulerPanel() {
        JPanel panel = page();
        panel.add(heading("Schedule & deadlines"), BorderLayout.NORTH);
        JTextArea area = outputArea();
        JButton refresh = new JButton("Refresh schedule");
        refresh.addActionListener(e -> {
            try {
                List<Task> list = new ArrayList<>(tasks.all());
                list.sort(Comparator.comparing(Task::getDeadline));
                StringBuilder b = new StringBuilder("UPCOMING SCHEDULE\n\n");
                LocalDate today = LocalDate.now();
                for (Task t : list) {
                    String marker = t.getStatus() == Task.Status.COMPLETED ? "[DONE]"
                        : t.getDeadline().isBefore(today) ? "[OVERDUE]"
                        : t.getDeadline().equals(today) ? "[DUE TODAY]" : "[UPCOMING]";
                    b.append(String.format("%-13s %s  |  %s  |  %s%n",
                        marker, t.getDeadline(), t.getTitle(), t.getStatus()));
                }
                if (list.isEmpty()) b.append("No scheduled tasks yet.");
                area.setText(b.toString());
            } catch (Exception ex) { error(ex); }
        });
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false); north.add(heading("Schedule & deadlines"), BorderLayout.WEST);
        north.add(refresh, BorderLayout.EAST);
        panel.add(north, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        refresh.doClick();
        return panel;
    }

    private JPanel sortingPanel() {
        JPanel panel = page();
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.setOpaque(false);
        north.add(heading("Sorting Laboratory"));
        JComboBox<SortingService.Algorithm> algorithm = new JComboBox<>(SortingService.Algorithm.values());
        JComboBox<SortingService.Field> field = new JComboBox<>(SortingService.Field.values());
        JButton run = new JButton("Run sorting");
        north.add(new JLabel("Algorithm:")); north.add(algorithm);
        north.add(new JLabel("Sort by:")); north.add(field); north.add(run);
        panel.add(north, BorderLayout.NORTH);
        panel.add(new JScrollPane(sortOutput), BorderLayout.CENTER);
        run.addActionListener(e -> {
            try {
                List<Task> source = tasks.all();
                StringBuilder trace = new StringBuilder();
                List<Task> sorted = sorting.sort(source,
                    (SortingService.Algorithm) algorithm.getSelectedItem(),
                    (SortingService.Field) field.getSelectedItem(),
                    line -> trace.append(line).append("\n"));
                trace.append("\nFINAL ORDER\n");
                for (int i = 0; i < sorted.size(); i++)
                    trace.append(i + 1).append(". ").append(sorted.get(i).getTitle())
                        .append(" | ").append(sorted.get(i).getDeadline())
                        .append(" | ").append(sorted.get(i).getPriority()).append("\n");
                if (source.isEmpty()) trace.append("Add tasks first to run a sort.");
                sortOutput.setText(trace.toString());
                sortOutput.setCaretPosition(0);
            } catch (Exception ex) { error(ex); }
        });
        return panel;
    }

    private JPanel mathPanel() {
        JPanel panel = page();
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setOpaque(false);
        JButton sets = new JButton("Set operations");
        JButton pairs = new JButton("Ordered pairs & Cartesian product");
        JButton relations = new JButton("Relation properties");
        JButton logic = new JButton("Truth table");
        controls.add(sets); controls.add(pairs); controls.add(relations); controls.add(logic);
        panel.add(heading("Discrete Mathematics Laboratory"), BorderLayout.NORTH);
        panel.add(controls, BorderLayout.CENTER);
        panel.add(new JScrollPane(mathOutput), BorderLayout.SOUTH);
        sets.addActionListener(e -> showSetOperations());
        pairs.addActionListener(e -> showPairs());
        relations.addActionListener(e -> showRelations());
        logic.addActionListener(e -> showTruthTable());
        return panel;
    }

    private JPanel reportsPanel() {
        JPanel panel = page();
        panel.add(heading("Reports & analytics"), BorderLayout.NORTH);
        JTextArea report = outputArea();
        JButton generate = new JButton("Generate report");
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false); north.add(heading("Reports & analytics"), BorderLayout.WEST);
        north.add(generate, BorderLayout.EAST);
        panel.add(north, BorderLayout.NORTH);
        panel.add(new JScrollPane(report), BorderLayout.CENTER);
        generate.addActionListener(e -> {
            try {
                List<Task> all = tasks.all();
                long completed = all.stream().filter(t -> t.getStatus() == Task.Status.COMPLETED).count();
                long pending = all.stream().filter(t -> t.getStatus() == Task.Status.PENDING).count();
                long progress = all.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count();
                long overdue = all.stream().filter(Task::isOverdue).count();
                Map<String, Long> byCategory = new TreeMap<>();
                for (Task t : all) byCategory.merge(t.getCategory(), 1L, Long::sum);
                report.setText("TASKFLOW SUMMARY REPORT\n\n"
                    + "Total tasks: " + all.size() + "\nCompleted: " + completed
                    + "\nIn progress: " + progress + "\nPending: " + pending
                    + "\nOverdue: " + overdue + "\n\nTASKS BY CATEGORY\n"
                    + byCategory);
            } catch (Exception ex) { error(ex); }
        });
        generate.doClick();
        return panel;
    }

    private void showSetOperations() {
        try {
            List<Task> all = tasks.all();
            Set<Integer> school = math.taskIdsByCategory(all, "School");
            Set<Integer> pending = math.taskIdsByStatus(all, Task.Status.PENDING);
            Set<Integer> union = math.union(school, pending);
            Set<Integer> intersection = math.intersection(school, pending);
            Set<Integer> difference = math.difference(school, pending);
            mathOutput.setText("SET THEORY – TASK ID SETS\n\n"
                + "A = School-category tasks: " + school + "\n"
                + "B = Pending tasks: " + pending + "\n\n"
                + "A ∪ B (union): " + union + "\n"
                + "A ∩ B (intersection): " + intersection + "\n"
                + "A − B (difference): " + difference
                + "\n\nThe values are task IDs. An empty set is shown as [].");
        } catch (Exception ex) { error(ex); }
    }

    private void showPairs() {
        try {
            List<Task> all = tasks.all();
            StringBuilder b = new StringBuilder("ORDERED PAIRS AND CARTESIAN PRODUCT\n\n");
            b.append("Task-deadline relation R = (task ID, deadline)\n");
            math.taskDeadlineRelation(all).forEach(p -> b.append(p).append("\n"));
            Set<String> statuses = new LinkedHashSet<>(List.of("Pending", "In Progress", "Completed"));
            Set<String> categories = new LinkedHashSet<>(tasks.categories());
            b.append("\nStatus set × Category set:\n");
            math.cartesianProduct(statuses, categories).forEach(p -> b.append(p).append("\n"));
            mathOutput.setText(b.toString());
        } catch (Exception ex) { error(ex); }
    }

    private void showRelations() {
        try {
            List<Task> all = tasks.all();
            var relation = math.sameCategoryRelation(all);
            mathOutput.setText("RELATION: two task IDs are related when their tasks share a category.\n\n"
                + "R = " + relation + "\n\n"
                + "Reflexive: " + math.isReflexive(all, relation) + "\n"
                + "Symmetric: " + math.isSymmetric(relation) + "\n"
                + "Transitive: " + math.isTransitive(relation)
                + "\n\nThis same-category relation should be an equivalence relation "
                + "on the set of existing task IDs.");
        } catch (Exception ex) { error(ex); }
    }

    private void showTruthTable() {
        StringBuilder b = new StringBuilder("PROPOSITIONAL LOGIC – TRUTH TABLE\n\n");
        b.append("P = task is incomplete\nQ = task deadline is today\n");
        b.append("P AND Q = task is eligible for the due-today alert\n\n");
        b.append(String.format("%-8s %-8s %-8s %-8s %-14s%n", "P", "Q", "P∧Q", "P∨Q", "P→Q"));
        for (var row : math.andOrImplicationTruthTable()) {
            b.append(String.format("%-8s %-8s %-8s %-8s %-14s%n",
                row.p(), row.q(), row.and(), row.or(), row.implication()));
        }
        b.append("\nIn Java, implication is evaluated as !P || Q.");
        mathOutput.setText(b.toString());
    }

    private JPanel page() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setBorder(new EmptyBorder(20, 22, 22, 22));
        return p;
    }

    private JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setBackground(UIManager.getColor("Panel.background"));
        return p;
    }

    private JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 21f));
        return label;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        return label;
    }

    private JTextArea outputArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(new EmptyBorder(12, 12, 12, 12));
        return area;
    }

    private Task selectedTask() {
        int row = taskTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a task first.");
            return null;
        }
        int modelRow = taskTable.convertRowIndexToModel(row);
        int id = (Integer) taskModel.getValueAt(modelRow, 0);
        try {
            return tasks.all().stream().filter(t -> t.getId() == id).findFirst().orElse(null);
        } catch (Exception ex) { error(ex); return null; }
    }

    private void refreshAll() {
        try {
            List<Task> all = tasks.all();
            loadTable(all);
            long completed = all.stream().filter(t -> t.getStatus() == Task.Status.COMPLETED).count();
            long pending = all.stream().filter(t -> t.getStatus() == Task.Status.PENDING).count();
            long progress = all.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count();
            long overdue = all.stream().filter(Task::isOverdue).count();
            dashboardCards.removeAll();
            dashboardCards.add(statCard("TOTAL TASKS", String.valueOf(all.size())));
            dashboardCards.add(statCard("COMPLETED", String.valueOf(completed)));
            dashboardCards.add(statCard("IN PROGRESS", String.valueOf(progress)));
            dashboardCards.add(statCard("OVERDUE", String.valueOf(overdue)));
            StringBuilder upcoming = new StringBuilder("<html><div style='line-height:1.7'>");
            all.stream().filter(t -> t.getStatus() != Task.Status.COMPLETED)
                .sorted(Comparator.comparing(Task::getDeadline)).limit(8)
                .forEach(t -> upcoming.append("• ").append(t.getDeadline()).append(" — ")
                    .append(escape(t.getTitle())).append(" (").append(t.getPriority()).append(")<br>"));
            if (all.isEmpty()) upcoming.append("No tasks yet. Select My Tasks to create your first task.");
            upcoming.append("</div></html>");
            dashboardStats.setText(upcoming.toString());
            dashboardCards.revalidate(); dashboardCards.repaint();
        } catch (Exception ex) { error(ex); }
    }

    private JPanel statCard(String title, String value) {
        JPanel p = new JPanel(new BorderLayout(4, 8));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
            new EmptyBorder(14, 16, 14, 16)));
        JLabel t = new JLabel(title);
        t.setForeground(new Color(120, 130, 145));
        JLabel v = new JLabel(value);
        v.setFont(v.getFont().deriveFont(Font.BOLD, 27f));
        p.add(t, BorderLayout.NORTH); p.add(v, BorderLayout.CENTER);
        return p;
    }

    private void loadTable(List<Task> list) {
        taskModel.setRowCount(0);
        for (Task t : list) taskModel.addRow(new Object[]{
            t.getId(), t.getTitle(), t.getDeadline(), t.getPriority(),
            t.getStatus(), t.getCategory()
        });
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void error(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(),
            "TaskFlow error", JOptionPane.ERROR_MESSAGE);
    }
}