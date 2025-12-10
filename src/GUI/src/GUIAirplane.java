
package GUI.src;

import Shared.src.Airport;
import java.awt.*;
import javax.swing.SwingUtilities;

/**
 * GUIAirplane: thread that coordinates GUI sprite animation with airport synchronization calls.
 * It calls Airport.requestRunwayForArrival/requestGate/requestRunwayForDeparture and updates UI.
 */
public class GUIAirplane extends Thread {
    private final String id;
    private final Airport airport;
    private final boolean arrivalFirst;
    private final MapPanel map;
    private final RunwaysPanel runwaysPanel;
    private final GatesPanel gatesPanel;
    private final LogPanel logPanel;
    private final Color color;
    private final AirplaneSprite sprite;
    private volatile boolean stopped = false;
    private volatile boolean paused = false;

    public GUIAirplane(String id, Airport airport, boolean arrivalFirst, MapPanel map,
                       RunwaysPanel r, GatesPanel g, LogPanel log, Color color) {
        this.id = id;
        this.airport = airport;
        this.arrivalFirst = arrivalFirst;
        this.map = map;
        this.runwaysPanel = r;
        this.gatesPanel = g;
        this.logPanel = log;
        this.color = color;
        this.sprite = new AirplaneSprite(id, 300, -20, color); // start above map
        SwingUtilities.invokeLater(() -> map.addSprite(sprite));
        setName("GUIAirplane-" + id);
    }

    public void requestStop() { stopped = true; interrupt(); }
    public void togglePause() { paused = !paused; }

    private void waitWhilePaused() throws InterruptedException {
        while (paused) Thread.sleep(100);
    }

    @Override
    public void run() {
        boolean runwayHeld = false;
        boolean gateHeld = false;

        try {
            if (arrivalFirst) {
                logPanel.log(id, "Requesting runway to land", color);
                sprite.moveTo(150, 70); // go to runway 1
                waitWhilePaused();

                airport.requestRunwayForArrival(null);
                runwayHeld = true;
                logPanel.log(id, "Landed on runway", color);

                Thread.sleep(200 + (int)(Math.random() * 300)); // landing time
                airport.releaseRunway(null);
                runwayHeld = false;

                // taxi to gate
                sprite.moveTo(120, 320);
                waitWhilePaused();

                airport.requestGate(null);
                gateHeld = true;
                logPanel.log(id, "Parked at gate", color);

                // stay parked
                Thread.sleep(700 + (int)(Math.random() * 800));
                waitWhilePaused();

                airport.releaseGate(null);
                gateHeld = false;
                logPanel.log(id, "Left gate (preparing departure)", color);

                // request departure runway
                sprite.moveTo(180, 70); // taxi to runway
                waitWhilePaused();

                airport.requestRunwayForDeparture(null);
                runwayHeld = true;
                logPanel.log(id, "Took off", color);

                sprite.moveTo(300, -40); // fly away
                waitWhilePaused();

                airport.releaseRunway(null);
                runwayHeld = false;

                SwingUtilities.invokeLater(() -> map.removeSprite(sprite));
            } else {
                // direct departure
                logPanel.log(id, "Requesting runway to depart", color);
                sprite.moveTo(180, 70);
                waitWhilePaused();

                airport.requestRunwayForDeparture(null);
                runwayHeld = true;
                logPanel.log(id, "Departed", color);

                Thread.sleep(200 + (int)(Math.random() * 300));
                sprite.moveTo(300, -40);
                waitWhilePaused();

                airport.releaseRunway(null);
                runwayHeld = false;

                SwingUtilities.invokeLater(() -> map.removeSprite(sprite));
            }
        } catch (InterruptedException e) {
            logPanel.log(id, "Interrupted", color);
            try { if (gateHeld) airport.releaseGate(null); } catch (Throwable ignored) {}
            try { if (runwayHeld) airport.releaseRunway(null); } catch (Throwable ignored) {}
            SwingUtilities.invokeLater(() -> map.removeSprite(sprite));
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            logPanel.log(id, "Fatal error: " + t.getMessage(), color);
            SwingUtilities.invokeLater(() -> map.removeSprite(sprite));
        }
    }
}