package com.example.randomshit;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelloController {
    @FXML
    private Label tempLabel,titleLabel,totalDiskSpaceLabel,usedDiskSpaceLabel,freeDiskSpaceLabel,diskModelLabel,diskTypeLabel,fileSystemLabel;
    private Button refreshButton,scanButton;
    private TextArea textArea;
    private TextArea resultsArea;
    private ScrollPane scrollPane;
    private VBox layout;
    private Stage newWindow;


    public void onPCpartsClick() {
        openNewWindow();
    }

    private void openNewWindow() {
        newWindow = new Stage();

        refreshButton = new Button("Check CPU Temperature");
        refreshButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
        refreshButton.setOnAction(e -> checkTemperature());

        titleLabel = new Label("System Peripheral Devices Scanner");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setWrapText(true);
        resultsArea.setText(getPCDetails());

        scanButton = new Button("Scan All Devices");
        scanButton.setPrefSize(200, 50);
        scanButton.setStyle("-fx-font-size: 14px;");
        scanButton.setOnAction(e -> {
            if (resultsArea != null){
                resultsArea.clear();
            scanAllDevices();
        }
        });

        totalDiskSpaceLabel = new Label();
        usedDiskSpaceLabel = new Label();
        freeDiskSpaceLabel = new Label();
        diskModelLabel = new Label();
        diskTypeLabel = new Label();
        fileSystemLabel = new Label();
        tempLabel = new Label();

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(getPCDetails());

        scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        layout = new VBox(scrollPane);
        layout.getChildren().addAll(totalDiskSpaceLabel, usedDiskSpaceLabel, freeDiskSpaceLabel, diskModelLabel, diskTypeLabel, fileSystemLabel);
        layout.getChildren().addAll(titleLabel,scanButton);
        getDiskInformation(totalDiskSpaceLabel, usedDiskSpaceLabel, freeDiskSpaceLabel, diskModelLabel, diskTypeLabel, fileSystemLabel);
        Scene secondScene = new Scene(layout, 600, 500);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(tempLabel, refreshButton);

        newWindow.setTitle("PC Hardware Details");
        newWindow.setScene(secondScene);
        newWindow.show();
    }

    private void scanAllDevices() {
        scanUsbDevices();
        scanBluetoothDevices();
        scanWebcamDevices();
    }

    private void scanWebcamDevices() {
        resultsArea.appendText("==================== WEBCAM/CAMERA DEVICES ====================\n\n");

        List<String> output;

        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                // Windows command
                output = executeCommand("powershell", "Get-PnpDevice | Where-Object {$_.Class -eq 'Camera'} | Format-Table -Property FriendlyName, Status -AutoSize");
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                // macOS command
                output = executeCommand("system_profiler", "SPCameraDataType");
            } else {
                // Linux command
                output = executeCommand("ls", "-l /dev/video*");
                try {
                    List<String> v4lOutput = executeCommand("v4l2-ctl", "--list-devices");
                    output.addAll(v4lOutput);
                } catch (Exception e) {
                    output.add("v4l2-ctl command failed, may not be installed");
                }
            }

            for (String line : output) {
                resultsArea.appendText(line + "\n");
            }
        } catch (IOException | InterruptedException e) {
            resultsArea.appendText("Error scanning webcam/camera devices: " + e.getMessage() + "\n");
        }
    }

    private void scanBluetoothDevices() {
        resultsArea.appendText("==================== BLUETOOTH DEVICES ====================\n\n");

        List<String> output;

        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                // Windows command
                output = executeCommand("powershell", "Get-PnpDevice | Where-Object {$_.Class -eq 'Bluetooth'} | Format-Table -Property FriendlyName, Status -AutoSize");
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                // macOS command
                output = executeCommand("system_profiler", "SPBluetoothDataType");
            } else {
                // Linux command
                output = executeCommand("bluetoothctl", "devices");
            }

            for (String line : output) {
                resultsArea.appendText(line + "\n");
            }
        } catch (IOException | InterruptedException e) {
            resultsArea.appendText("Error scanning Bluetooth devices: " + e.getMessage() + "\n");
        }

        resultsArea.appendText("\n");
    }

    private void scanUsbDevices() {
        resultsArea.appendText("==================== USB DEVICES ====================\n\n");

        List<String> output;

        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                // Windows command
                output = executeCommand("powershell", "Get-PnpDevice | Where-Object {$_.InstanceId -match 'USB'} | Format-Table -Property FriendlyName, Status, Class -AutoSize");
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                // macOS command
                output = executeCommand("system_profiler", "SPUSBDataType");
            } else {
                // Linux command
                output = Collections.singletonList(executeCommand("lsusb"));
            }

            for (String line : output) {
                resultsArea.appendText(line + "\n");
            }
        } catch (IOException | InterruptedException e) {
            resultsArea.appendText("Error scanning USB devices: " + e.getMessage() + "\n");
        }

        resultsArea.appendText("\n");

    }

    private String getPCDetails() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== System Information ===\n");
        sb.append("OS: ").append(System.getProperty("os.name")).append("\n");
        sb.append("OS Version: ").append(System.getProperty("os.version")).append("\n");
        sb.append("Architecture: ").append(System.getProperty("os.arch")).append("\n");
        sb.append("User: ").append(System.getProperty("user.name")).append("\n\n");

        sb.append("=== CPU Details ===\n");
        sb.append(runCommand("wmic cpu get name,NumberOfCores,NumberOfLogicalProcessors"));

        sb.append("\n=== Memory (RAM) ===\n");
        sb.append(runCommand("wmic memorychip get Capacity"));

        sb.append("\n=== Motherboard Details ===\n");
        sb.append(runCommand("wmic baseboard get Manufacturer,Product"));

        sb.append("\n=== GPU Details ===\n");
        sb.append(runCommand("wmic path win32_videocontroller get name"));

        sb.append("\n=== Storage Drives ===\n");
        sb.append(runCommand("wmic diskdrive get Model,Size"));

        sb.append("\n=== Network Interfaces ===\n");
        sb.append(runCommand("wmic nic get Name, MACAddress"));

        return sb.toString();
    }

    private String runCommand(String command) {
        StringBuilder result = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line.trim()).append("\n");
            }
        } catch (Exception e) {
            result.append("Error fetching details.\n");
        }
        return result.toString();
    }

    private void checkTemperature() {
        Task<Double> task = new Task<>() {
            @Override
            protected Double call() throws Exception {
                try {
                    Process process = Runtime.getRuntime().exec(
                                    "wmic /namespace:\\\\root\\wmi PATH MSAcpi_ThermalZoneTemperature get CurrentTemperature"
                    );

                    try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(process.getInputStream()))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.trim().isEmpty() || line.contains("CurrentTemperature")) continue;
                            return (Double.parseDouble(line.trim()) / 10.0) - 273.15;
                        }
                    }

                    if (process.waitFor() != 0) {
                        throw new Exception("Command failed with code: " + process.exitValue());
                    }
                    throw new Exception("No temperature data found");
                } catch (Exception ex) {
                    throw new Exception("Error: " + ex.getMessage());
                }
            }
        };

        task.setOnSucceeded(e -> {
            tempLabel.setText(String.format("Temperature: %.2f °C", task.getValue()));
            tempLabel.setStyle("-fx-text-fill: green;");
            refreshButton.setDisable(false);
        });

        task.setOnFailed(e -> {
            tempLabel.setText("Error: " + task.getException().getMessage());
            tempLabel.setStyle("-fx-text-fill: red;");
            refreshButton.setDisable(false);
        });

        refreshButton.setDisable(true);
        new Thread(task).start();
    }

    private void getDiskInformation(Label totalDiskSpaceLabel, Label usedDiskSpaceLabel,
                    Label freeDiskSpaceLabel, Label diskModelLabel,
                    Label diskTypeLabel, Label fileSystemLabel) {
        try {
            FileSystem fs = FileSystems.getDefault();
            Path root = Paths.get("/");
            File file = new File(root.toString());
            FileStore store = Files.getFileStore(root);
            totalDiskSpaceLabel.setText("Total Disk Space: " + file.getTotalSpace() / (1024 * 1024 * 1024) + " GB");
            freeDiskSpaceLabel.setText("Free Disk Space: " + file.getFreeSpace() / (1024 * 1024 * 1024) + " GB");
            usedDiskSpaceLabel.setText("Used Disk Space: " + (file.getTotalSpace() - file.getFreeSpace()) / (1024 * 1024 * 1024) + " GB");
            fileSystemLabel.setText("File System Type: " + store.type());
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                diskModelLabel.setText("Disk Model: " + getDiskModelWindows());
                diskTypeLabel.setText("Disk Type: " + getDiskTypeWindows());
            } else if (os.contains("mac")) {
                diskModelLabel.setText("Disk Model: " + getDiskModelMac());
                diskTypeLabel.setText("Disk Type: " + getDiskTypeMac());
            } else if (os.contains("nix") || os.contains("nux")) {
                diskModelLabel.setText("Disk Model: " + getDiskModelLinux());
                diskTypeLabel.setText("Disk Type: " + getDiskTypeLinux());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDiskModelWindows() {
        return executeCommand("wmic diskdrive get model");
    }

    private String getDiskTypeWindows() {
        return executeCommand("wmic diskdrive get mediaType");
    }

    private String getDiskModelMac() {
        return executeCommand("diskutil info / | grep 'Device / Media Name'");
    }

    private String getDiskTypeMac() {
        return executeCommand("diskutil info / | grep 'Solid State'");
    }

    private String getDiskModelLinux() {
        return executeCommand("lsblk -d -o model");
    }

    private String getDiskTypeLinux() {
        return executeCommand("lsblk -d -o name,rota");
    }

    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString().trim();
    }

    @FXML
    private void onNetworkButtonClick() {
        Stage networkStage = new Stage();
        VBox networkLayout = new VBox(10);
        networkLayout.setPadding(new Insets(20));
        networkLayout.setAlignment(Pos.TOP_LEFT);

        Label publicIpLabel = new Label("Public IP: Loading...");
        Label localIpLabel = new Label("Local IP: " + getLocalIP());
        Label gatewayLabel = new Label("Default Gateway: Loading...");
        Label dnsLabel = new Label("DNS Servers: Loading...");
        TextArea connectionsArea = new TextArea();
        TextArea speedArea = new TextArea();
        connectionsArea.setEditable(false);
        speedArea.setEditable(false);

        networkLayout.getChildren().addAll(
                        publicIpLabel, localIpLabel, gatewayLabel, dnsLabel,
                        new Label("\nActive Connections:"), connectionsArea,
                        new Label("\nNetwork Speed:"), speedArea
        );

        Button refreshButton = new Button("Refresh Data");
        refreshButton.setOnAction(e -> refreshNetworkData(
                        publicIpLabel, gatewayLabel, dnsLabel, connectionsArea, speedArea
        ));
        networkLayout.getChildren().add(refreshButton);

        ScrollPane scrollPane = new ScrollPane(networkLayout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 600, 500);
        networkStage.setTitle("Network Information");
        networkStage.setScene(scene);

        refreshNetworkData(publicIpLabel, gatewayLabel, dnsLabel, connectionsArea, speedArea);
        networkStage.show();
    }


    private void refreshNetworkData(Label publicIpLabel, Label gatewayLabel,
                    Label dnsLabel, TextArea connectionsArea,
                    TextArea speedArea) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                // Public IP (async)
                Platform.runLater(() -> publicIpLabel.setText("Public IP: Loading..."));
                String publicIp = getPublicIP();
                Platform.runLater(() -> publicIpLabel.setText("Public IP: " + publicIp));

                // Gateway and DNS
                String os = System.getProperty("os.name").toLowerCase();
                String gateway = runCommand(os.contains("win") ?
                                "ipconfig | findstr /i \"Default Gateway\"" :
                                "netstat -nr | grep default");
                String dns = runCommand(os.contains("win") ?
                                "ipconfig /all | findstr /i \"DNS Servers\"" :
                                "grep nameserver /etc/resolv.conf");

                // Connections and speed
                String connections = getNetworkConnections();
                String speed = getNetworkSpeed();

                // Update UI
                Platform.runLater(() -> {
                    gatewayLabel.setText("Default Gateway:\n" + gateway.trim());
                    dnsLabel.setText("DNS Servers:\n" + dns.trim());
                    connectionsArea.setText(connections);
                    speedArea.setText(speed);
                });
                return null;
            }
        };

        new Thread(task).start();
    }

    private String getNetworkConnections() {
        String command = System.getProperty("os.name").toLowerCase().contains("win") ?
                        "netstat -ano" :
                        "netstat -tuln";
        return runCommand(command);
    }

    private String getNetworkSpeed() {
        try {
            return runCommand("ping -n 4 8.8.8.8"); // Simple latency test
        } catch (Exception e) {
            return "Speed test failed: " + e.getMessage();
        }
    }

    private String getPublicIP() {
        try {
            URL url = new URL("https://api.ipify.org");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            return "N/A (Check internet connection)";
        }
    }

    private String getLocalIP() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "N/A";
        }
    }

    private List<String> executeCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        List<String> output = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            output.add("Command execution failed with exit code: " + exitCode);
        }

        return output;
    }
}