
package SemaphoreVersion.src;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

import Shared.src.Airplane;
import Shared.src.Airport;
import Shared.src.AirportTelemetry;
import Shared.src.SupportsTelemetry;

public class AirportSemaphore implements Airport, SupportsTelemetry {

	//Split runway capacity for strong priority.:
    private final Semaphore arrivalRunways;    // starts with all runway permits
    private final Semaphore departureRunways;  // starts with 0
    //Gate capacity
    private final Semaphore gates;
    //Tracks arrivals waiting : used to decide where releases go and for status/UI
    private final AtomicInteger waitingArrivals = new AtomicInteger(0);
    //runwayHoldStart: Per-thread timestamp to compute runway occupied durations for utilization.
    private final ConcurrentHashMap<Long, Long> runwayHoldStart = new ConcurrentHashMap<>();
    //Fixed capacities (used for status/metrics).
    private final int totalRunways;
    private final int totalGates;
    //Optional unified metrics sink.
    private AirportTelemetry telemetry;

    public AirportSemaphore(int runways, int gatesCount) {
        this.totalRunways = runways;
        this.totalGates   = gatesCount;
        this.arrivalRunways   = new Semaphore(runways, true);
        this.departureRunways = new Semaphore(0, true);
        this.gates = new Semaphore(gatesCount, true);
    }

    @Override
    public void setTelemetry(AirportTelemetry telemetry) { this.telemetry = telemetry; }

    //Block until a runway is available for landing (arrival)
    @Override
    public void requestRunwayForArrival(Airplane a) throws InterruptedException {
        long waitStart = System.currentTimeMillis();
        //Increment waitingArrivals
        if (waitingArrivals.incrementAndGet() == 1) {
        	//drain any free departure permits and return them to arrival permits:
            int freeDep = departureRunways.drainPermits();
            if (freeDep > 0) arrivalRunways.release(freeDep);
        }
        try {
        	//blocks until an arrival permit is available.
            arrivalRunways.acquire();
            if (telemetry != null) {
                telemetry.incRunwayOccupied();
                runwayHoldStart.put(Thread.currentThread().getId(), System.currentTimeMillis());
                telemetry.addRunwayWait(System.currentTimeMillis() - waitStart);
            }
        } finally {
        	//decrement waitingArrivals.
            waitingArrivals.decrementAndGet();
        }
    }

    // Block until a runway is available for takeoff (departure).
    @Override
    public void requestRunwayForDeparture(Airplane a) throws InterruptedException {
        long waitStart = System.currentTimeMillis();
        //blocks until a departure permit is available
        //When arrivals are waiting, releases go to arrivalRunways, so departures wait
        //When arrivals clear, releases feed the departureRunways pool.
        departureRunways.acquire();
        if (telemetry != null) {
            telemetry.incRunwayOccupied();
            runwayHoldStart.put(Thread.currentThread().getId(), System.currentTimeMillis());
            telemetry.addRunwayWait(System.currentTimeMillis() - waitStart);
        }
    }

    //Block until a gate is available (post-landing).
    //While waiting for a gate, the plane is on taxiway. (file d'attente)
    @Override
    public void requestGate(Airplane a) throws InterruptedException {
        String planeId = Thread.currentThread().getName();
        long waitStart = System.currentTimeMillis();
        // No direct way to observe "waiting" before acquire, so we add to taxiQueue first if needed
        //If not acquired, add planeId to the telemetry taxi queue (addTaxi()),
        while (!gates.tryAcquire()) {
            if (telemetry != null) telemetry.addTaxi(planeId);
            Thread.sleep(20); // small backoff; avoids busy spin at 100% CPU
        }
        //else :Once acquired, remove planeId from taxi queue, update telemetry
        if (telemetry != null) {
            telemetry.removeTaxi(planeId);
            telemetry.incGateOccupied();
            telemetry.addGateWait(System.currentTimeMillis() - waitStart);
        }
        /*
         * Note:
         * Semaphore.acquire() blocks but doesn’t 
         * provide a hook to mark “waiting” beforehand. 
         * The tryAcquire() loop with tiny sleeps makes 
         * the GUI taxiway queue visible without pegging CPU.
         */
    }

    //Free a runway after landing/takeoff and route capacity based on arrival priority
    @Override
    public void releaseRunway(Airplane a) {
        // If arrivals waiting, give capacity to arrivals; else to departures
        if (waitingArrivals.get() > 0) {
            arrivalRunways.release();
        } else {
            departureRunways.release();
        }
        if (telemetry != null) {
            telemetry.decRunwayOccupied();
            Long start = runwayHoldStart.remove(Thread.currentThread().getId());
            if (start != null) telemetry.addRunwayOccupiedDuration(System.currentTimeMillis() - start);
        }
    }
    //Free a gate after the parking phase.
    @Override
    public void releaseGate(Airplane a) {
        gates.release();
        if (telemetry != null) telemetry.decGateOccupied();
    }

    /*
     * Returns a snapshot including:
		Runways occupied vs total,
		Gates free vs total,
		Arrivals waiting.
     */
    @Override
    public String getStatus() {
        int freeArrival = arrivalRunways.availablePermits();
        int freeDeparture = departureRunways.availablePermits();
        int freeGates = gates.availablePermits();
        int occRunways = totalRunways - (freeArrival + freeDeparture);
       return String.format("[Semaphore] runwaysOcc=%d/%d, gatesFree=%d/%d, arrivalsWaiting=%d",
                occRunways, totalRunways, freeGates, totalGates, waitingArrivals.get());
    }
}