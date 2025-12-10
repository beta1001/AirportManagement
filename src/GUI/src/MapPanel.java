
package GUI.src;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import Shared.src.Airport;
import Shared.src.AirportTelemetry;

public class MapPanel extends JPanel {
    private final List<AirplaneSprite> sprites = new CopyOnWriteArrayList<>();
    private Airport airport;
    private AirportTelemetry telemetry;

    private int runwayCount = 2;
    private int gateCount   = 3;

    public MapPanel() {
        setPreferredSize(new Dimension(600, 480));
        setBackground(new Color(0xDFF3FF));
        new Timer(40, e -> {
            for (AirplaneSprite s : sprites) s.updatePosition();
            repaint();
        }).start();
    }

    public void setAirport(Airport a) { this.airport = a; }
    public void setTelemetry(AirportTelemetry t) { this.telemetry = t; }
    public void setRunwayCount(int n) { this.runwayCount = Math.max(1, n); repaint(); }
    public void setGateCount(int n)    { this.gateCount   = Math.max(1, n); repaint(); }

    public void addSprite(AirplaneSprite s) { sprites.add(s); }
    public void removeSprite(AirplaneSprite s) { sprites.remove(s); }
    public void clearSprites() { sprites.clear(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int left = 50;
        int runwayTop = 50;
        int runwayWidth = getWidth() - 100;
        int runwayHeight = 40;
        int runwayGap = 70;

        int occRunways = telemetry != null ? telemetry.getOccupiedRunways() : 0;
        int occGates   = telemetry != null ? telemetry.getOccupiedGates()   : 0;

        // --- Runways (color occupied vs free) ---
        for (int i = 0; i < runwayCount; i++) {
            int y = runwayTop + i * runwayGap;
            boolean occupied = i < occRunways;
            g2.setColor(occupied ? new Color(0x444444) : new Color(0xAAAAAA));
            g2.fillRect(left, y, runwayWidth, runwayHeight);
            g2.setColor(Color.BLACK);
            g2.drawRect(left, y, runwayWidth, runwayHeight);
            g2.drawString("Runway " + (i + 1) + (occupied ? " (occupied)" : " (free)"), left + 5, y - 5);
        }

        // --- Gates area ---
        int gatesTop = runwayTop + runwayCount * runwayGap + 60;
        int gateWidth = 60;
        int gateHeight = 40;
        int gateGap = 80;
        int gatesPerRow = Math.max(1, Math.min(gateCount, Math.max(1, (getWidth() - 120) / gateGap)));
        int totalRows = (gateCount + gatesPerRow - 1) / gatesPerRow;

        int idx = 0;
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < gatesPerRow && idx < gateCount; col++, idx++) {
                int x = left + col * gateGap + 10;
                int y = gatesTop + row * (gateHeight + 30);
                boolean occupied = idx < occGates;
                g2.setColor(occupied ? new Color(0x8080FF) : Color.LIGHT_GRAY);
                g2.fillRect(x, y, gateWidth, gateHeight);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, gateWidth, gateHeight);
                g2.drawString("G" + (idx + 1) + (occupied ? " (occupied)" : " (free)"), x + 5, y + (gateHeight / 2) + 4);
            }
        }

        // --- Taxiway queue ---
        int taxiY = gatesTop + totalRows * (gateHeight + 30) + 20;
        g2.setColor(Color.BLACK);
        g2.drawString("Taxiway queue (waiting for gate):", left, taxiY);
        if (telemetry != null) {
            List<String> q = telemetry.taxiQueueSnapshot();
            int x = left;
            int y = taxiY + 15;
            for (String id : q) {
                g2.setColor(new Color(0xFFD27F));
                g2.fillRoundRect(x, y, 60, 22, 8, 8);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(x, y, 60, 22, 8, 8);
                g2.drawString(id, x + 8, y + 16);
                x += 70;
            }
        }

        // --- Sprites on top ---
        for (AirplaneSprite s : sprites) s.paint(g2);
    }
}