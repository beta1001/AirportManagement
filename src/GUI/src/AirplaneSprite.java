package GUI.src;

import java.awt.*;

public class AirplaneSprite {
    public double x, y;
    public double tx, ty; // target
    public final String id;
    public final Color color;
    public boolean finished = false;

    public AirplaneSprite(String id, double x, double y, Color color) {
        this.id = id;
        this.x = x; this.y = y; this.tx = x; this.ty = y; this.color = color;
    }

    public void moveTo(double tx, double ty) {
        this.tx = tx; this.ty = ty;
    }

    public void updatePosition() {
        double dx = tx - x; double dy = ty - y;
        double dist = Math.sqrt(dx*dx + dy*dy);
        if (dist < 1) { x = tx; y = ty; return; }
        double step = Math.min(6.0, dist);
        x += dx/dist * step;
        y += dy/dist * step;
    }

    public void paint(Graphics2D g2) {
        g2.setColor(color);
        g2.fillOval((int)x-8, (int)y-8, 16, 16);
        g2.setColor(Color.BLACK);
        g2.drawString(id, (int)x-8, (int)y-12);
    }
}