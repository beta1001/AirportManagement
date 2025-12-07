package Shared.src;

import java.util.Random;

public class Airplane extends Thread {
    public enum Stage {INBOUND, PARKED, OUTBOUND}

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
        System.out.printf("[%s] %s\n", id, msg);
    }

    @Override
    public void run() {
        try {
            if (isArrivalFirst) {
                // Arrival -> Gate -> Later departure
                log("wants to ARRIVE");
                airport.requestRunwayForArrival(this);
                log("using runway to land");
                Thread.sleep(200 + rng.nextInt(300));
                airport.releaseRunway(this);

                log("wants a GATE to park");
                airport.requestGate(this);
                log("parked at gate");
                Thread.sleep(400 + rng.nextInt(800));
                airport.releaseGate(this);

                // prepare departure
                log("preparing for DEPARTURE");
                Thread.sleep(200 + rng.nextInt(300));

                log("wants to DEPART");
                airport.requestRunwayForDeparture(this);
                log("using runway to takeoff");
                Thread.sleep(200 + rng.nextInt(300));
                airport.releaseRunway(this);

                log("completed cycle");
            } else {
                // Direct departure (for testing), could also be parked->depart
                log("wants to DEPART");
                airport.requestRunwayForDeparture(this);
                log("using runway to takeoff");
                Thread.sleep(200 + rng.nextInt(300));
                airport.releaseRunway(this);
                log("departed");
            }
        } catch (InterruptedException e) {
            log("interrupted");
            Thread.currentThread().interrupt();
        }
    }
}