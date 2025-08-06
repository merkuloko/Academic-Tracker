package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import com.opencsv.CSVReader;

public class AcademicTrackerGUI extends JFrame {
    private static final String CSV_PATH = "src/MMDC_Academic_Track_Template.csv";
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTable courseTable;
    private DefaultTableModel model;
    private JLabel twaLabel, gwaLabel, progressLabel, honorLabel;

    public AcademicTrackerGUI() {
        setTitle("Academic Tracker");
        setSize(450, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel homePanel = createHomePanel();
        JPanel addCoursePanel = createAddCoursePanel();
        JPanel viewGradesPanel = createViewGradesPanel();

        mainPanel.add(homePanel, "home");
        mainPanel.add(addCoursePanel, "addCourse");
        mainPanel.add(viewGradesPanel, "viewGrades");

        setContentPane(mainPanel);
        cardLayout.show(mainPanel, "home");
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton addCourseBtn = new JButton("Add Course Grade");
        addCourseBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addCourseBtn.setMaximumSize(new Dimension(200, 40));
        addCourseBtn.addActionListener(e -> {
            setSize(1200, 600);
            cardLayout.show(mainPanel, "addCourse");
        });

        JButton viewGradesBtn = new JButton("View Grades");
        viewGradesBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewGradesBtn.setMaximumSize(new Dimension(200, 40));
        viewGradesBtn.addActionListener(e -> {
            setSize(900, 700);
            cardLayout.show(mainPanel, "viewGrades");
        });

        panel.add(Box.createRigidArea(new Dimension(0, 60)));
        panel.add(addCourseBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(viewGradesBtn);

        return panel;
    }

    private JPanel createAddCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton backBtn = new JButton("← Back Home");
        backBtn.addActionListener(e -> {
            setSize(450, 300);
            cardLayout.show(mainPanel, "home");
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backBtn);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JComboBox<String> yearBox = new JComboBox<>();
        JComboBox<String> termBox = new JComboBox<>();
        JComboBox<String> courseBox = new JComboBox<>();
        JComboBox<String> titleBox = new JComboBox<>();

        JComboBox<String> gradeBox = new JComboBox<>(new String[]{
                "1.00", "1.25", "1.50", "1.75", "2.00", "2.25", "2.50", "2.75", "3.00", "5.00", "INC", "P"
        });

        List<String[]> allRows = CSVUtils.readCSV(CSV_PATH);
        populateYearBox(yearBox, allRows);

        yearBox.addActionListener(e -> populateTermBox(termBox, allRows, (String) yearBox.getSelectedItem()));
        termBox.addActionListener(e -> populateCourseBox(courseBox, allRows, (String) yearBox.getSelectedItem(), (String) termBox.getSelectedItem()));
        courseBox.addActionListener(e -> populateTitleBox(titleBox, allRows, (String) yearBox.getSelectedItem(), (String) termBox.getSelectedItem(), (String) courseBox.getSelectedItem()));

        inputPanel.add(new JLabel("Academic Year:"));
        inputPanel.add(yearBox);
        inputPanel.add(new JLabel("Term:"));
        inputPanel.add(termBox);
        inputPanel.add(new JLabel("Course:"));
        inputPanel.add(courseBox);
        inputPanel.add(new JLabel("Course Title:"));
        inputPanel.add(titleBox);
        inputPanel.add(new JLabel("Grade:"));
        inputPanel.add(gradeBox);

        DefaultTableModel addModel = new DefaultTableModel(
                new String[]{"Year", "Term", "Course", "Course Code", "Title", "Units", "Grade"}, 0);
        JTable table = new JTable(addModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton addBtn = new JButton("Add Course");
        inputPanel.add(addBtn);
        addBtn.addActionListener(e -> {
            String year = (String) yearBox.getSelectedItem();
            String term = (String) termBox.getSelectedItem();
            String course = (String) courseBox.getSelectedItem();
            String title = (String) titleBox.getSelectedItem();
            String grade = (String) gradeBox.getSelectedItem();

            if (year == null || term == null || course == null || title == null || grade == null) {
                JOptionPane.showMessageDialog(this, "Fill all fields.");
                return;
            }

            for (String[] row : allRows) {
                if (row.length >= 6 &&
                        row[0].equals(year) &&
                        row[1].equals(term) &&
                        row[2].equals(course) &&
                        row[4].equals(title)) {

                    String[] newRow = Arrays.copyOf(row, 7);
                    newRow[6] = grade;
                    addModel.addRow(newRow);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Matching course not found.");
        });

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                addModel.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            }
        });

        JButton saveBtn = new JButton("Save to CSV");
        saveBtn.addActionListener(e -> {
            if (addModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No data to save.");
                return;
            }

            List<String[]> original = CSVUtils.readCSV(CSV_PATH);

            for (int i = 0; i < addModel.getRowCount(); i++) {
                String code = addModel.getValueAt(i, 3).toString();
                String grade = addModel.getValueAt(i, 6).toString();
                for (int j = 0; j < original.size(); j++) {
                    String[] row = original.get(j);
                    if (row.length >= 4 && row[3].equals(code)) {
                        if (row.length < 7) {
                            String[] updatedRow = Arrays.copyOf(row, 7);
                            updatedRow[6] = grade;
                            original.set(j, updatedRow);
                        } else {
                            row[6] = grade;
                        }
                        break;
                    }
                }
            }

            CSVUtils.writeCSV(CSV_PATH, original);
            JOptionPane.showMessageDialog(this, "Saved!");
            addModel.setRowCount(0);
        });

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(deleteBtn, BorderLayout.SOUTH);

        // Bottom panel containing the Save button to commit new grades to the CSV file
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(saveBtn);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(topPanel);
        top.add(inputPanel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void populateYearBox(JComboBox<String> box, List<String[]> rows) {
        Set<String> years = new TreeSet<>();
        for (String[] r : rows) if (r.length >= 1) years.add(r[0]);
        box.removeAllItems();
        years.forEach(box::addItem);
    }

    private void populateTermBox(JComboBox<String> box, List<String[]> rows, String year) {
        Set<String> terms = new TreeSet<>();
        for (String[] r : rows) if (r.length >= 2 && r[0].equals(year)) terms.add(r[1]);
        box.removeAllItems();
        terms.forEach(box::addItem);
    }

    private void populateCourseBox(JComboBox<String> box, List<String[]> rows, String year, String term) {
        Set<String> courses = new TreeSet<>();
        for (String[] r : rows) if (r.length >= 3 && r[0].equals(year) && r[1].equals(term)) courses.add(r[2]);
        box.removeAllItems();
        courses.forEach(box::addItem);
    }

    private void populateTitleBox(JComboBox<String> box, List<String[]> rows, String year, String term, String course) {
        Set<String> titles = new TreeSet<>();
        for (String[] r : rows) if (r.length >= 5 && r[0].equals(year) && r[1].equals(term) && r[2].equals(course)) titles.add(r[4]);
        box.removeAllItems();
        titles.forEach(box::addItem);
    }

    private JPanel createViewGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> {
            setSize(450, 300);
            cardLayout.show(mainPanel, "home");
        });

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.add(backBtn);

        JComboBox<String> yearBox = new JComboBox<>();
        JComboBox<String> termBox = new JComboBox<>();
        JComboBox<String> courseBox = new JComboBox<>();

        yearBox.addItem("All");
        termBox.addItem("All");
        courseBox.addItem("All");

        populateDropdowns(yearBox, termBox, courseBox);

        JButton viewBtn = new JButton("View");

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.setBorder(BorderFactory.createTitledBorder("Filter Grades"));
        filters.add(new JLabel("Academic Year:")); filters.add(yearBox);
        filters.add(new JLabel("Term:")); filters.add(termBox);
        filters.add(new JLabel("Course:")); filters.add(courseBox);
        filters.add(viewBtn);

        model = new DefaultTableModel(
                new String[]{"Academic Year", "Term", "Course", "Code", "Title", "Units", "Grade"}, 0);
        courseTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(courseTable);

        // Academic Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(2, 2, 10, 10));
        twaLabel = new JLabel("TWA: N/A");
        gwaLabel = new JLabel("GWA: N/A");
        progressLabel = new JLabel("Progress: N/A");
        honorLabel = new JLabel("Latin Honor: N/A");
        infoPanel.add(twaLabel);
        infoPanel.add(gwaLabel);
        infoPanel.add(progressLabel);
        infoPanel.add(honorLabel);

        viewBtn.addActionListener(e -> {
            model.setRowCount(0);
            List<String[]> rows = CSVUtils.readCSV(CSV_PATH);
            double gwaSum = 0;
            double twaSum = 0;
            int gwaCount = 0;
            int twaCount = 0;

            for (String[] row : rows) {
                if (row.length >= 7) {
                    boolean match =
                            (yearBox.getSelectedItem().equals("All") || row[0].equals(yearBox.getSelectedItem())) &&
                                    (termBox.getSelectedItem().equals("All") || row[1].equals(termBox.getSelectedItem())) &&
                                    (courseBox.getSelectedItem().equals("All") || row[2].equals(courseBox.getSelectedItem()));

                    if (match) {
                        model.addRow(row);
                        try {
                            double grade = Double.parseDouble(row[6]);
                            double units = Double.parseDouble(row[5]);
                            gwaSum += grade * units;
                            gwaCount += units;
                            if (termBox.getSelectedItem().equals("All") || row[1].equals(termBox.getSelectedItem())) {
                                twaSum += grade * units;
                                twaCount += units;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            double gwa = gwaCount > 0 ? gwaSum / gwaCount : 0;
            double twa = twaCount > 0 ? twaSum / twaCount : 0;

            gwaLabel.setText(String.format("GWA: %.2f", gwa));
            twaLabel.setText(String.format("TWA: %.2f", twa));

            int passed = 0;
            int total = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                String gradeStr = model.getValueAt(i, 6).toString();
                try {
                    double grade = Double.parseDouble(gradeStr);
                    if (grade <= 3.0) passed++;
                    total++;
                } catch (NumberFormatException ignored) {}
            }
            progressLabel.setText("Progress: " + passed + "/" + total + " Passed");

            if (gwa <= 1.20) honorLabel.setText("Latin Honor: Summa Cum Laude");
            else if (gwa <= 1.45) honorLabel.setText("Latin Honor: Magna Cum Laude");
            else if (gwa <= 1.75) honorLabel.setText("Latin Honor: Cum Laude");
            else honorLabel.setText("Latin Honor: None");
        });

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(backPanel);
        top.add(filters);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void populateDropdowns(JComboBox<String> y, JComboBox<String> t, JComboBox<String> c) {
        List<String[]> rows = CSVUtils.readCSV(CSV_PATH);
        Set<String> years = new TreeSet<>();
        Set<String> terms = new TreeSet<>();
        Set<String> courses = new TreeSet<>();

        for (String[] r : rows) {
            if (r.length >= 6) {
                years.add(r[0]);
                terms.add(r[1]);
                courses.add(r[2]);
            }
        }
        years.forEach(y::addItem);
        terms.forEach(t::addItem);
        courses.forEach(c::addItem);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AcademicTrackerGUI().setVisible(true));
    }

    private static class CSVUtils {
        public static List<String[]> readCSV(String path) {
            List<String[]> rows = new ArrayList<>();
            try (CSVReader reader = new CSVReader(new FileReader(path))) {
                reader.readNext();
                String[] line;
                while ((line = reader.readNext()) != null) rows.add(line);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "CSV Read Error: " + e.getMessage());
            }
            return rows;
        }

        public static void writeCSV(String path, List<String[]> data) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
                writer.println("Academic Year,Term,Course,Course Code,Title,Units,Grades");
                for (String[] row : data) {
                    writer.println(String.join(",", Arrays.copyOf(row, 7)));
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "CSV Write Error: " + e.getMessage());
            }
        }
    }
}