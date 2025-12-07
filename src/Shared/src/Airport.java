package Shared.src;
public interface Airport {
        // Request a runway for arrival (blocks until available or interrupted)
        void requestRunwayForArrival(Airplane a) throws InterruptedException;

        // Request a runway for departure (blocks until available or interrupted)
        void requestRunwayForDeparture(Airplane a) throws InterruptedException;

        // Request a gate for parking (arrival -> gate)
        void requestGate(Airplane a) throws InterruptedException;

        // Release resources
        void releaseRunway(Airplane a);
        void releaseGate(Airplane a);

        // For status/debug
        String getStatus();
    }