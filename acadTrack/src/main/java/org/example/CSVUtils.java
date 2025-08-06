package org.example;

import com.opencsv.CSVReader;
import javax.swing.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {

    public static List<String[]> readCSV(String path) {
        List<String[]> data = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            reader.readNext(); // skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "CSV read error: " + e.getMessage());
        }
        return data;
    }
}