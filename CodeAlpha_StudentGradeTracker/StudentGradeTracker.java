import java.io.*;
import java.util.*;

/*
 * @author [Snehal Kale]
 * @version 2.0
 */
public class StudentGradeTracker {
    private static final String DATA_FILE = "students.csv"; // File for saving/loading data
    private static ArrayList<Student> students = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    /**
     * Main entry point. Runs the menu-driven application.
     */
    public static void main(String[] args) {
        loadData(); // Load existing data if available
        while (true) {
            displayMenu();
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (!processChoice(choice)) break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        saveData(); // Save data before exiting
        System.out.println("Exiting... Data saved.");
    }

    /**
     * Displays the main menu options.
     */
    private static void displayMenu() {
        System.out.println("\n=== Student Grade Tracker ===");
        System.out.println("1. Add Student");
        System.out.println("2. Add Grade to Student");
        System.out.println("3. Remove Student");
        System.out.println("4. Remove Grade from Student");
        System.out.println("5. Search Student");
        System.out.println("6. View Summary Report");
        System.out.println("7. Exit");
        System.out.print("Choose an option: ");
    }

    /**
     * Processes the user's menu choice.
     * @param choice The selected menu option.
     * @return true to continue, false to exit.
     */
    private static boolean processChoice(int choice) {
        switch (choice) {
            case 1: addStudent(); break;
            case 2: addGradeToStudent(); break;
            case 3: removeStudent(); break;
            case 4: removeGradeFromStudent(); break;
            case 5: searchStudent(); break;
            case 6: displaySummary(); break;
            case 7: return false;
            default: System.out.println("Invalid option. Try again.");
        }
        return true;
    }

    /**
     * Adds a new student, ensuring no duplicates.
     */
    private static void addStudent() {
        System.out.print("Enter student name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        for (Student s : students) {
            if (s.getName().equalsIgnoreCase(name)) {
                System.out.println("Student already exists.");
                return;
            }
        }
        students.add(new Student(name));
        System.out.println("Student added successfully.");
    }

    /**
     * Adds a grade to an existing student with validation.
     */
    private static void addGradeToStudent() {
        if (students.isEmpty()) {
            System.out.println("No students available. Add a student first.");
            return;
        }
        Student student = selectStudent();
        if (student == null) return;
        System.out.print("Enter grade (0-100): ");
        try {
            double grade = Double.parseDouble(scanner.nextLine().trim());
            if (grade < 0 || grade > 100) {
                System.out.println("Grade must be between 0 and 100.");
                return;
            }
            student.addGrade(grade);
            System.out.println("Grade added successfully.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid grade. Enter a number.");
        }
    }

    /**
     * Removes a student.
     */
    private static void removeStudent() {
        if (students.isEmpty()) {
            System.out.println("No students to remove.");
            return;
        }
        Student student = selectStudent();
        if (student != null) {
            students.remove(student);
            System.out.println("Student removed.");
        }
    }

    /**
     * Removes a grade from a student.
     */
    private static void removeGradeFromStudent() {
        Student student = selectStudent();
        if (student == null || student.getGrades().isEmpty()) {
            System.out.println("No grades to remove.");
            return;
        }
        System.out.println("Grades: " + student.getGrades());
        System.out.print("Enter grade index to remove (1-based): ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (index < 0 || index >= student.getGrades().size()) {
                System.out.println("Invalid index.");
                return;
            }
            student.getGrades().remove(index);
            System.out.println("Grade removed.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid index.");
        }
    }

    /**
     * Searches for a student by name.
     */
    private static void searchStudent() {
        System.out.print("Enter student name to search: ");
        String name = scanner.nextLine().trim();
        for (Student s : students) {
            if (s.getName().equalsIgnoreCase(name)) {
                System.out.println(s);
                return;
            }
        }
        System.out.println("Student not found.");
    }

    /**
     * Displays a sorted summary report of all students.
     */
    private static void displaySummary() {
        if (students.isEmpty()) {
            System.out.println("No students to display.");
            return;
        }
        // Sort by average descending
        students.sort((s1, s2) -> Double.compare(s2.getAverage(), s1.getAverage()));
        System.out.println("\n=== Summary Report (Sorted by Average Descending) ===");
        for (Student student : students) {
            System.out.println(student);
        }
    }

    /**
     * Helper to select a student from the list.
     * @return The selected Student or null if invalid.
     */
    private static Student selectStudent() {
        System.out.println("Select a student:");
        for (int i = 0; i < students.size(); i++) {
            System.out.println((i + 1) + ". " + students.get(i).getName());
        }
        System.out.print("Enter student number: ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (index < 0 || index >= students.size()) {
                System.out.println("Invalid selection.");
                return null;
            }
            return students.get(index);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return null;
        }
    }

    /**
     * Saves student data to a CSV file.
     */
    private static void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (Student s : students) {
                writer.println(s.getName() + "," + String.join(",", s.getGrades().stream().map(String::valueOf).toArray(String[]::new)));
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Loads student data from a CSV file.
     */
    private static void loadData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    Student student = new Student(parts[0]);
                    for (int i = 1; i < parts.length; i++) {
                        try {
                            student.addGrade(Double.parseDouble(parts[i]));
                        } catch (NumberFormatException ignored) {}
                    }
                    students.add(student);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, no issue
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    /**
     * Represents a student with grades.
     */
    static class Student {
        private String name;
        private ArrayList<Double> grades;

        public Student(String name) {
            this.name = name;
            this.grades = new ArrayList<>();
        }

        public void addGrade(double grade) {
            grades.add(grade);
        }

        public String getName() {
            return name;
        }

        public ArrayList<Double> getGrades() {
            return grades;
        }

        public double getAverage() {
            return grades.isEmpty() ? 0.0 : grades.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }

        public double getHighest() {
            return grades.isEmpty() ? 0.0 : grades.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        }

        public double getLowest() {
            return grades.isEmpty() ? 0.0 : grades.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        }

        @Override
        public String toString() {
            return String.format("Student: %s\nGrades: %s\nAverage: %.2f\nHighest: %.2f\nLowest: %.2f\n",
                    name, grades, getAverage(), getHighest(), getLowest());
        }
    }
}