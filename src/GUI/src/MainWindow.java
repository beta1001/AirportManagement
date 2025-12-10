
package GUI.src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import Shared.src.Airport;
import Shared.src.AirportTelemetry;
import Shared.src.SupportsTelemetry;
import MonitorVersion.src.AirportMonitor;
import SemaphoreVersion.src.AirportSemaphore;

public class MainWindow extends JFrame {
    private final ControlPanel controlPanel;
    private final MapPanel mapPanel;
    private final LogPanel logPanel;
    private final JLabel statusLabel; // bottom status bar

    private FileWriter csvWriter;
    private AirportTelemetry telemetry;
    private javax.swing.Timer telemetryTimer;

    private Airport airport;
    private final java.util.List<GUIAirplane> activePlanes = new ArrayList<>();
    private final Map<String, Color> colorMap = new HashMap<>();

    public MainWindow() {
        super("Airport Simulator — Swing GUI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLayout(new BorderLayout());

        // Panels
        controlPanel = new ControlPanel(
                this::onStart,
                (arrivalFirst, e) -> onAddPlane(arrivalFirst),
                this::onPauseAll,
                this::onReset
        );

        mapPanel = new MapPanel();
        logPanel = new LogPanel();
        statusLabel = new JLabel("Ready");

        JPanel left = new JPanel(new BorderLayout());
        left.add(mapPanel, BorderLayout.CENTER);

        add(controlPanel, BorderLayout.NORTH);
        add(left, BorderLayout.CENTER);
        add(logPanel, BorderLayout.EAST);
        add(statusLabel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void onStart(String version, int runways, int gates) {
        switch (version) {
            case "Monitor" -> airport = new AirportMonitor(runways, gates);
            default -> airport = new AirportSemaphore(runways, gates);
        }
        logPanel.logSystem("Airport started using: " + version + " (R=" + runways + ", G=" + gates + ")");

        telemetry = new AirportTelemetry(runways, gates);

        // Prepare CSV file
        try {
            csvWriter = new FileWriter("telemetry_data.csv");
            csvWriter.write("time,occupiedRunways,occupiedGates,taxiQueueSize,avgRunwayWait,avgGateWait,utilization,notifyAllCount\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (airport instanceof SupportsTelemetry st) {
            st.setTelemetry(telemetry);
        }

        mapPanel.setAirport(airport);
        mapPanel.setTelemetry(telemetry);
        mapPanel.setRunwayCount(runways);
        mapPanel.setGateCount(gates);

        if (telemetryTimer != null) telemetryTimer.stop();

        long startTime = System.currentTimeMillis();

        telemetryTimer = new javax.swing.Timer(1000, e -> {
            if (telemetry != null) {
                String summary = telemetry.summary();
                if (!summary.equals(statusLabel.getText())) {
                    statusLabel.setText(summary);
                }
                mapPanel.repaint();

                // Write telemetry data to CSV
                long elapsedSec = (System.currentTimeMillis() - startTime) / 1000;
                String row = String.format(Locale.US,
                        "%d,%d,%d,%d,%.1f,%.1f,%.2f,%d\n",
                        elapsedSec,
                        telemetry.getOccupiedRunways(),
                        telemetry.getOccupiedGates(),
                        telemetry.taxiQueueSnapshot().size(),
                        telemetry.avgRunwayWaitMs(),
                        telemetry.avgGateWaitMs(),
                        telemetry.runwayUtilization(),
                        telemetry.getNotifyAllCount()
                        
                );
                try {
                    csvWriter.write(row);
                    csvWriter.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        telemetryTimer.start();
    }

    private void onAddPlane(boolean arrivalFirst) {
        if (airport == null) {
            JOptionPane.showMessageDialog(this, "Start the airport first!");
            return;
        }
        String id = "P" + (activePlanes.size() + 1);
        Color c = randomColorFor(id);

        GUIAirplane g = new GUIAirplane(id, airport, arrivalFirst, mapPanel, null, null, logPanel, c);
        activePlanes.add(g);
        g.start();
        logPanel.log(id, "Created", c);
    }

    private void onPauseAll(ActionEvent e) {
        for (GUIAirplane g : activePlanes) g.togglePause();
        logPanel.logSystem("Toggled pause for all airplanes");
    }

    private void onReset(ActionEvent e) {
        for (GUIAirplane g : activePlanes) g.requestStop();
        activePlanes.clear();

        if (telemetryTimer != null) telemetryTimer.stop();
        try {
            if (csvWriter != null) csvWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        mapPanel.clearSprites();
        statusLabel.setText("Simulation reset");
        logPanel.logSystem("Simulation reset");
    }

    private Color randomColorFor(String id) {
        return colorMap.computeIfAbsent(id, k -> new Color((int) (Math.random() * 0xFFFFFF)));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}