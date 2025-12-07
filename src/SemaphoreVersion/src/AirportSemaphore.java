package SemaphoreVersion.src;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Shared.src.Airplane;
import Shared.src.Airport;

public class AirportSemaphore implements Airport {
    private final Semaphore runwaySemaphore;
    private final Semaphore gateSemaphore;

    // For priority: count waiting arrivals; departures wait on this lock if >0
    private final ReentrantLock priorityLock = new ReentrantLock();
    private final Condition noArrivalsWaiting = priorityLock.newCondition();
    private final AtomicInteger waitingArrivals = new AtomicInteger(0);

    public AirportSemaphore(int runways, int gates) {
        this.runwaySemaphore = new Semaphore(runways, true);
        this.gateSemaphore = new Semaphore(gates, true);
    }

    @Override
    public void requestRunwayForArrival(Airplane a) throws InterruptedException {
        // Register interest in arrival
        priorityLock.lock();
        try {
            waitingArrivals.incrementAndGet();
        } finally {
            priorityLock.unlock();
        }

        // Acquire runway (blocks on semaphore)
        runwaySemaphore.acquire();

        // Once acquired, decrement waiting arrivals and signal departures if none remain
        priorityLock.lock();
        try {
            if (waitingArrivals.decrementAndGet() == 0) {
                noArrivalsWaiting.signalAll();
            }
        } finally {
            priorityLock.unlock();
        }
    }

    @Override
    public void requestRunwayForDeparture(Airplane a) throws InterruptedException {
        // Wait until there are no arrivals waiting; departures should be blocked when arrivals exist
        priorityLock.lockInterruptibly();
        try {
            while (waitingArrivals.get() > 0) {
                noArrivalsWaiting.await();
            }
        } finally {
            priorityLock.unlock();
        }

        // Now try to get a runway
        runwaySemaphore.acquire();
    }

    @Override
    public void requestGate(Airplane a) throws InterruptedException {
        gateSemaphore.acquire();
    }

    @Override
    public void releaseRunway(Airplane a) {
        runwaySemaphore.release();
    }

    @Override
    public void releaseGate(Airplane a) {
        gateSemaphore.release();
    }

    @Override
    public String getStatus() {
        return String.format("[Semaphore] runways available=%d, gates available=%d, waitingArrivals=%d",
                runwaySemaphore.availablePermits(), gateSemaphore.availablePermits(), waitingArrivals.get());
    }
}
