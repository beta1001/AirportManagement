
package Shared.src;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AirportTelemetry {
    // Configuration
    private final int totalRunways;
    private final int totalGates;
    private final long simStartMs = System.currentTimeMillis();

    // Live occupancy (increment/decrement by controllers)
    private final AtomicInteger occupiedRunways = new AtomicInteger(0);
    private final AtomicInteger occupiedGates   = new AtomicInteger(0);

    // Taxiway queue: planes that finished landing but are waiting for a gate
    private final CopyOnWriteArrayList<String> taxiQueue = new CopyOnWriteArrayList<>();

    // Wait time metrics
    private final AtomicLong runwayWaitTotalMs = new AtomicLong(0);
    private final AtomicLong runwayWaitCount   = new AtomicLong(0);
    private final AtomicLong gateWaitTotalMs   = new AtomicLong(0);
    private final AtomicLong gateWaitCount     = new AtomicLong(0);

    // Runway utilization: sum of all "runway occupied durations"
    private final AtomicLong runwayOccupiedTotalMs = new AtomicLong(0);

    // Wake-ups/Signals (for comparative report)
    private final AtomicLong notifyAllCount = new AtomicLong(0);
    

    public AirportTelemetry(int totalRunways, int totalGates) {
        this.totalRunways = Math.max(1, totalRunways);
        this.totalGates   = Math.max(1, totalGates);
    }

    // --- Occupancy updates ---
    public void incRunwayOccupied() { occupiedRunways.incrementAndGet(); }
    public void decRunwayOccupied() { occupiedRunways.decrementAndGet(); }
    public void incGateOccupied()   { occupiedGates.incrementAndGet(); }
    public void decGateOccupied()   { occupiedGates.decrementAndGet(); }

    // --- Taxiway queue ---
    public void addTaxi(String planeId) {
        if (planeId != null && !taxiQueue.contains(planeId)) taxiQueue.add(planeId);
    }
    public void removeTaxi(String planeId) { taxiQueue.remove(planeId); }
    public List<String> taxiQueueSnapshot() { return List.copyOf(taxiQueue); }

    // --- Wait time accumulation ---
    public void addRunwayWait(long ms) { runwayWaitTotalMs.addAndGet(ms); runwayWaitCount.incrementAndGet(); }
    public void addGateWait(long ms)   { gateWaitTotalMs.addAndGet(ms);   gateWaitCount.incrementAndGet(); }

    public double avgRunwayWaitMs() {
        long c = runwayWaitCount.get();
        return c == 0 ? 0.0 : ((double) runwayWaitTotalMs.get() / c);
    }
    public double avgGateWaitMs() {
        long c = gateWaitCount.get();
        return c == 0 ? 0.0 : ((double) gateWaitTotalMs.get() / c);
    }

    // --- Runway utilization --- : Measures how busy runways are relative to total simulation time and capacity.
    public void addRunwayOccupiedDuration(long ms) { runwayOccupiedTotalMs.addAndGet(ms); }
    public double runwayUtilization() {
        long elapsed = Math.max(1, System.currentTimeMillis() - simStartMs);
        // Sum of per-runway occupied time divided by total capacity*time
        return Math.min(1.0, (double) runwayOccupiedTotalMs.get() / (totalRunways * (double) elapsed));
    }

    // --- Wake-ups Counters---
    public void incNotifyAll() { notifyAllCount.incrementAndGet(); }
    

    public long getNotifyAllCount() { return notifyAllCount.get(); }
    


    // --- Snapshots for GUI ---
    public int getTotalRunways() { return totalRunways; }
    public int getTotalGates()   { return totalGates; }
    public int getOccupiedRunways() { return Math.max(0, Math.min(totalRunways, occupiedRunways.get())); }
    public int getOccupiedGates()   { return Math.max(0, Math.min(totalGates, occupiedGates.get())); }

    public String summary() {
    
     // Build the values first to avoid evaluation order confusion
     int occRunways = getOccupiedRunways();
     int totRunways = getTotalRunways();
     int occGates = getOccupiedGates();
     int totGates = getTotalGates();
     List<String> taxi = taxiQueueSnapshot();
     double avgRunway = avgRunwayWaitMs();
     double avgGate = avgGateWaitMs();
     double util = runwayUtilization();
     long nAll = notifyAllCount.get();
     

     // Use Locale.US to ensure '.' decimal and prevent locale issues
     return String.format(
             Locale.US,
             "Telemetry: runways=%d/%d, gates=%d/%d, taxi=%s, avgWait(runway)=%.1f ms, avgWait(gate)=%.1f ms, util(runway)=%.2f, notifyAll=%d",
             occRunways, totRunways,
             occGates,   totGates,
             taxi,                 // <-- %s for List is fine
             avgRunway,            // <-- %.1f needs a double
             avgGate,              // <-- %.1f needs a double
             util,                 // <-- %.2f needs a double 
             nAll      // <-- %d is longs (OK)
     );
 }
}
