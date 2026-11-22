# TaskFlow — Java Desktop Application

TaskFlow is a Java Swing scheduling and task management desktop application.
It uses SQLite for persistent local storage, FlatLaf for light/dark themes,
and manually implemented sorting and discrete mathematics services.

## Requirements
- JDK 17 or newer
- Maven 3.8+
- Internet access on the first Maven build to download declared dependencies

## Run in IntelliJ IDEA
1. Extract the ZIP.
2. Open the `TaskFlowDesktop` folder (the folder containing `pom.xml`) in IntelliJ IDEA.
3. Allow IntelliJ to import the Maven project and download dependencies.
4. Set Project SDK to JDK 17 or newer.
5. Open `src/main/java/com/taskflow/Main.java`.
6. Run `Main`.

## Run from terminal
From the folder containing `pom.xml`:

    mvn clean package
    java -jar target/taskflow-desktop-1.0.0.jar

The Maven Shade plugin creates the runnable JAR with dependencies included.

## Database
On first launch, TaskFlow creates `data/taskflow.db` in the working directory.
Tasks and categories are stored in SQLite. Keep this folder to retain your records.
Do not delete or move the database while the application is running.

## Current modules
- Dashboard: task counts, overdue count, and upcoming tasks.
- My Tasks: create, edit, delete, search, and table sorting.
- Scheduler: deadline-ordered schedule with overdue/today/upcoming labels.
- Sorting Lab: Bubble, Selection, Insertion, Merge, and Quick Sort with trace output.
- Discrete Math Lab: set union/intersection/difference, ordered pairs, Cartesian product,
  relation properties, and a propositional-logic truth table.
- Reports: status and category summary.

## Discrete Mathematics implementation notes
- Sets: task ID sets grouped by category and status; union, intersection, and difference.
- Ordered pairs: task ID/deadline relation.
- Cartesian product: status set × category set.
- Relations: same-category relation on task IDs, with reflexive, symmetric, and transitive checks.
- Propositional logic: P = incomplete, Q = due today; eligibility is P AND Q.
  Truth table includes AND, OR, and implication.

## Sorting implementation notes
All five algorithms are implemented in `SortingService.java` without calling
`Collections.sort()` or `List.sort()` for the algorithm itself. The sorting lab
prints pass/merge/partition trace messages. The task list shown in the Tasks tab
is not mutated by a sorting demonstration.

## External libraries
- Xerial SQLite JDBC (`org.xerial:sqlite-jdbc`) — SQLite database driver.
- FlatLaf (`com.formdev:flatlaf`) — Swing look and feel.
Both are declared in `pom.xml` and are downloaded by Maven. No external chart
library is used in this starter implementation.

## Important project note
This is a substantial first implementation, but test it on the exact lab computer
and verify the required presentation scenarios before submission. The calendar
module is currently a deadline schedule list rather than a month-grid calendar.
