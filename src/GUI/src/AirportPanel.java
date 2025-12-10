package GUI.src;

import javax.swing.*;
import java.awt.*;

public class AirportPanel extends JPanel {

    public AirportPanel() {
        setPreferredSize(new Dimension(400, 500));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw runways
        g.setColor(Color.GRAY);
        g.fillRect(50, 50, 300, 40);
        g.fillRect(50, 120, 300, 40);

        // Draw gates
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 3; i++) {
            g.fillRect(50 + (i * 100), 250, 80, 50);
        }

        // Labels
        g.setColor(Color.BLACK);
        g.drawString("Runway 1", 50, 45);
        g.drawString("Runway 2", 50, 115);
        g.drawString("Gates", 50, 315);
    }
}
