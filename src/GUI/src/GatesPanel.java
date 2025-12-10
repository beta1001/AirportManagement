
package GUI.src;

import javax.swing.*;
import java.awt.*;

public class GatesPanel extends JPanel {
    private int gates = 3;

    public GatesPanel() {
        setPreferredSize(new Dimension(600, 100));
    }

    public void setGateCount(int g) { this.gates = Math.max(1, g); repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < gates; i++) {
            g.fillRect(20 + i * 80, 10, 70, 60);
            g.setColor(Color.BLACK);
            g.drawRect(20 + i * 80, 10, 70, 60);
            g.drawString("Gate " + (i + 1), 25 + i * 80, 40);
            g.setColor(Color.LIGHT_GRAY);
        }
    }
}