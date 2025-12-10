
package GUI.src;

import javax.swing.*;
import java.awt.*;

public class RunwaysPanel extends JPanel {
    private int runways = 2;

    public RunwaysPanel() {
        setPreferredSize(new Dimension(600, 60));
    }

    public void setRunwayCount(int r) { this.runways = Math.max(1, r); repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.GRAY);
        for (int i = 0; i < runways; i++) {
            g.fillRect(50 + i * 260, 10, 200, 40);
            g.setColor(Color.BLACK);
            g.drawRect(50 + i * 260, 10, 200, 40);
            g.drawString("Runway " + (i + 1), 60 + i * 260, 35);
            g.setColor(Color.GRAY);
        }
    }
}