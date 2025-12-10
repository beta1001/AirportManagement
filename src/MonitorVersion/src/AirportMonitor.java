
package MonitorVersion.src;

import Shared.src.Airplane;
import Shared.src.Airport;
import Shared.src.AirportTelemetry;
import Shared.src.SupportsTelemetry;

import java.util.concurrent.ConcurrentHashMap;

public class AirportMonitor implements Airport, SupportsTelemetry {

    private final Object lock = new Object();
    private int availableRunways;
    private int availableGates;
    // Logical queues (counts) used for priority decisions and visibility in status.
    private int waitingArrivals = 0;
    private int waitingDepartures = 0;

    private AirportTelemetry telemetry;

    // Track runway hold durations per thread
    //Records when a thread acquired a runway to later compute occupied duration on release.
    private final ConcurrentHashMap<Long, Long> runwayHoldStart = new ConcurrentHashMap<>();

    public AirportMonitor(int runways, int gates) {
        this.availableRunways = runways;
        this.availableGates = gates;
    }

    @Override
    public void setTelemetry(AirportTelemetry telemetry) {
        this.telemetry = telemetry;
    }

    //Block until a runway is free for landing.
    @Override
    public void requestRunwayForArrival(Airplane a) throws InterruptedException {
        final long waitStart = System.currentTimeMillis();
        synchronized (lock) {
            waitingArrivals++;
            boolean decremented = false;
            try {
            	//no available runways -> wait
                while (availableRunways == 0) {
                    lock.wait();
                }
                //else resource(Runway) is acquired,
                availableRunways--;
                //Update telemetry: record hold start time, accumulate runway wait time.
                if (telemetry != null) {
                    telemetry.incRunwayOccupied();
                    runwayHoldStart.put(Thread.currentThread().getId(), System.currentTimeMillis());
                    telemetry.addRunwayWait(System.currentTimeMillis() - waitStart);
                }
            } catch (InterruptedException ie) {
                waitingArrivals--;
                decremented = true;
                if (telemetry != null) telemetry.incNotifyAll();
                lock.notifyAll();
                throw ie;
            } finally {
                if (!decremented) {
                    waitingArrivals--;
                    //give departures a chance to proceed.
                    if (waitingArrivals == 0) {
                        if (telemetry != null) telemetry.incNotifyAll();
                        lock.notifyAll();
                    }
                }
            }
        }
    }

    // Block until a runway is free for takeoff, but only when no arrivals are waiting.
    @Override
    public void requestRunwayForDeparture(Airplane a) throws InterruptedException {
        final long waitStart = System.currentTimeMillis();
        synchronized (lock) {
            waitingDepartures++;
            try {
            	//Departures wait When: “ no runway is available” or when “there are arrivals ”.
                while (availableRunways == 0 || waitingArrivals > 0) {
                    lock.wait();
                }
                // else resource(Runway) is acquired,
                availableRunways--;
                //Telemetry: incRunwayOccupied(), record hold start, accumulate runway wait time.
                if (telemetry != null) {
                    telemetry.incRunwayOccupied();
                    runwayHoldStart.put(Thread.currentThread().getId(), System.currentTimeMillis());
                    telemetry.addRunwayWait(System.currentTimeMillis() - waitStart);
                }
            } finally {
                waitingDepartures--;
            }
        }
    }

    //Block until a gate is free for parking (after landing).
    @Override
    public void requestGate(Airplane a) throws InterruptedException {
        final String planeId = Thread.currentThread().getName(); // e.g., GUIAirplane-P1
        final long waitStart = System.currentTimeMillis();
        synchronized (lock) {
            // Landed—now possibly waiting on taxiway(file d'attente)
            while (availableGates == 0) {
            	//Adds the plane to taxiway queue for GUI while waiting.
                if (telemetry != null) telemetry.addTaxi(planeId);
                lock.wait();
            }
            //else : Remove the plane from taxi queue when a gate becomes available.
            if (telemetry != null) telemetry.removeTaxi(planeId);
            // resource(Gate) is acquired,
            availableGates--;
            //Telemetry: incGateOccupied(), accumulate gate wait time.
            if (telemetry != null) {
                telemetry.incGateOccupied();
                telemetry.addGateWait(System.currentTimeMillis() - waitStart);
            }
        }
    }

    //Mark runway as free after landing/takeoff.
    @Override
    public void releaseRunway(Airplane a) {
        synchronized (lock) {
        	// resource(Runway) is released
            availableRunways++;
            //Telemetry: decRunwayOccupied() (released.), compute occupied duration from runwayHoldStart, increment notifyAll counter.
            if (telemetry != null) {
                telemetry.decRunwayOccupied();
                Long start = runwayHoldStart.remove(Thread.currentThread().getId());
                if (start != null) telemetry.addRunwayOccupiedDuration(System.currentTimeMillis() - start);
                telemetry.incNotifyAll();
            }
            // wake any waiters (arrivals or departures).
            lock.notifyAll();
        }
    }

    //Mark gate as free after parking phase.
    @Override
    public void releaseGate(Airplane a) {
        synchronized (lock) {
        	// resource(gate) is released
            availableGates++;
            //Telemetry: decGateOccupied() (released.), increment notifyAll counter.
            if (telemetry != null) {
                telemetry.decGateOccupied();
                telemetry.incNotifyAll();
            }
            //wake any arrivals waiting for gates.
            lock.notifyAll();
        }
    }

    //Returns a readable snapshot
    @Override
    public String getStatus() {
        synchronized (lock) {
            return String.format("[Monitor] runways=%d, gates=%d, waitingArrivals=%d, waitingDepartures=%d",
                    availableRunways, availableGates, waitingArrivals, waitingDepartures);
        }
    }
}