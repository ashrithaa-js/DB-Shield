package com.fabriciojf.postgresql.backup.gui;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;

import com.fabriciojf.standalone.helper.HashHelper;

public class MainController {

    @FXML private TextField dbNameField;
    @FXML private TextField userField;
    @FXML private TextField passwordField;
    @FXML private TextField pathField;
    @FXML private TextField hostField;
    @FXML private TextField labelField;
    @FXML private TextArea outputArea;
    @FXML private ComboBox<String> dbTypeCombo;

    @FXML
    private void onBrowse(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Backup Directory");
        File selectedDir = directoryChooser.showDialog(new Stage());
        if (selectedDir != null) {
            pathField.setText(selectedDir.getAbsolutePath());
        }
    }

    @FXML
    private void onBackup(ActionEvent event) {
        String host = hostField.getText().trim();
        String dbName = dbNameField.getText().trim();
        String user = userField.getText().trim();
        String password = passwordField.getText().trim();
        String backupPath = pathField.getText().trim();
        String label = labelField.getText().trim();
        String dbType = dbTypeCombo.getValue();

        if (host.isEmpty() || dbName.isEmpty() || user.isEmpty() ||
                password.isEmpty() || backupPath.isEmpty() || label.isEmpty() || dbType == null) {
            outputArea.appendText("Please fill in all required fields.\n");
            return;
        }

        Task<Void> backupTask = new Task<>() {
            @Override
            protected Void call() {
                updateMessage("Starting backup...\n");

                try {
                    String backupFile = backupPath + File.separator + dbName + ".backup";

                    if ("postgresql".equalsIgnoreCase(dbType)) {
                        String pgDumpPath = "C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe"; // make sure path is correct
                        runDump(pgDumpPath, host, user, password, dbName, backupFile);
                    } else if ("mysql".equalsIgnoreCase(dbType)) {
                        String mySqlDumpPath = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe"; // path to mysqldump
                        runDump(mySqlDumpPath, host, user, password, dbName, backupFile);
                    } else {
                        updateMessage("❌ Unsupported database type.\n");
                        return null;
                    }

                    updateMessage("✅ Backup completed successfully!\n");
                } catch (Exception e) {
                    e.printStackTrace();
                    updateMessage("❌ Backup failed: " + e.getMessage() + "\n");
                }
                return null;
            }
        };

        backupTask.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            outputArea.appendText(newMsg);
        });

        new Thread(backupTask).start();
    }

    private void runDump(String dumpPath, String host, String user, String password, String dbName, String backupFile) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder();

        if (dumpPath.toLowerCase().contains("pg_dump")) {
            // PostgreSQL command
            pb.command(dumpPath, "-U", user, "-h", host, "-F", "c", "-b", "-v", "-f", backupFile, dbName);
            pb.environment().put("PGPASSWORD", password); // set password for pg_dump
        } else {
            // MySQL command
            pb.command(dumpPath, "-u" + user, "-p" + password, "-h", host, dbName, "--result-file=" + backupFile);
        }

        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Read process output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String msg = line + "\n";
                javafx.application.Platform.runLater(() -> outputArea.appendText(msg));
            }
        }

        process.waitFor();
    }

    @FXML
    private void onViewLog(ActionEvent event) {
        outputArea.appendText("Viewing backup logs...\n");
        // TODO: implement log viewing if needed
    }
}
