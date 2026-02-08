package ui.util;

import javafx.stage.FileChooser;
import ui.service.ReportService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class AdminExport {
    public static File saveCSVDialog(String defaultName) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        return fc.showSaveDialog(null);
    }

    public static void writeTopUsersCSV(File file, List<ReportService.TopUserRow> rows) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("User,Reservations,Amount");
            for (var r : rows) {
                pw.printf("%s,%d,%.2f%n", r.email, r.count, r.amount);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}