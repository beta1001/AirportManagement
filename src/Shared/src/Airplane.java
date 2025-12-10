
package Shared.src;

import java.util.Random;

public class Airplane extends Thread {
    public enum Stage { INBOUND, PARKED, OUTBOUND }

    private final String id;
    private final Airport airport;
    private final boolean isArrivalFirst; // if true start as arrival, else as departure
    private final Random rng = new Random();

    public Airplane(String id, Airport airport, boolean isArrivalFirst) {
        this.id = id;
        this.airport = airport;
        this.isArrivalFirst = isArrivalFirst;
        setName("Airplane-" + id);
    }

    private void log(String msg) {
        System.out.printf("[%s] %s%n", id, msg);
    }

    @Override
    public void run() {
        try {
            if (isArrivalFirst) {
                boolean runwayHeld = false;
                boolean gateHeld = false;
                try {
                    log("wants to ARRIVE");
                    airport.requestRunwayForArrival(this);
                    runwayHeld = true;

                    log("using runway to land");
                    Thread.sleep(200 + rng.nextInt(300));

                    airport.releaseRunway(this);
                    runwayHeld = false;

                    log("wants a GATE to park");
                    airport.requestGate(this);
                    gateHeld = true;

                    log("parked at gate");
                    Thread.sleep(400 + rng.nextInt(800));

                    airport.releaseGate(this);
                    gateHeld = false;

                    log("preparing for DEPARTURE");
                    Thread.sleep(200 + rng.nextInt(300));

                    log("wants to DEPART");
                    airport.requestRunwayForDeparture(this);
                    runwayHeld = true;

                    log("using runway to takeoff");
                    Thread.sleep(200 + rng.nextInt(300));

                    airport.releaseRunway(this);
                    runwayHeld = false;

                    log("completed cycle");
                } catch (InterruptedException e) {
                    log("interrupted");
                    if (gateHeld) try { airport.releaseGate(this); } catch (Throwable ignored) {}
                    if (runwayHeld) try { airport.releaseRunway(this); } catch (Throwable ignored) {}
                    Thread.currentThread().interrupt();
                }
            } else {
                boolean runwayHeld = false;
                try {
                    log("wants to DEPART");
                    airport.requestRunwayForDeparture(this);
                    runwayHeld = true;

                    log("using runway to takeoff");
                    Thread.sleep(200 + rng.nextInt(300));

                    airport.releaseRunway(this);
                    runwayHeld = false;
                    log("departed");
                } catch (InterruptedException e) {
                    log("interrupted");
                    if (runwayHeld) 
                    {
                    	try { 
                    		airport.releaseRunway(this); 
                    		} catch (Throwable ignored) {
                    		}
                    }
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Throwable t) {
            log("fatal error: " + t.getMessage());
        }
    }
}
