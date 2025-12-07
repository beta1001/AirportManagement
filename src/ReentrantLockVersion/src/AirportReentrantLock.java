package ReentrantLockVersion.src;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Shared.src.Airplane;
import Shared.src.Airport;

public class AirportReentrantLock implements Airport {
        private final ReentrantLock lock = new ReentrantLock(true);
        private final Condition resourceAvailable = lock.newCondition();

        private int availableRunways;
        private int availableGates;

        private int waitingArrivals = 0;
        private int waitingDepartures = 0;

        public AirportReentrantLock(int runways, int gates) {
            this.availableRunways = runways;
            this.availableGates = gates;
        }

        @Override
        public void requestRunwayForArrival(Airplane a) throws InterruptedException {
            lock.lockInterruptibly();
            try {
                waitingArrivals++;
                while (availableRunways == 0) {
                    resourceAvailable.await();
                }
                availableRunways--;
                waitingArrivals--;
            } finally {
                // if no arrivals remain, signal potential departures
                try {
                    if (waitingArrivals == 0) {
                        resourceAvailable.signalAll();
                    }
                } finally {
                    lock.unlock();
                }
            }
        }

        @Override
        public void requestRunwayForDeparture(Airplane a) throws InterruptedException {
            lock.lockInterruptibly();
            try {
                waitingDepartures++;
                while (availableRunways == 0 || waitingArrivals > 0) {
                    resourceAvailable.await();
                }
                availableRunways--;
                waitingDepartures--;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void requestGate(Airplane a) throws InterruptedException {
            lock.lockInterruptibly();
            try {
                while (availableGates == 0) {
                    resourceAvailable.await();
                }
                availableGates--;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void releaseRunway(Airplane a) {
            lock.lock();
            try {
                availableRunways++;
                resourceAvailable.signalAll();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void releaseGate(Airplane a) {
            lock.lock();
            try {
                availableGates++;
                resourceAvailable.signalAll();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public String getStatus() {
            lock.lock();
            try {
                return String.format("[ReentrantLock] runways=%d, gates=%d, waitingArrivals=%d, waitingDepartures=%d",
                        availableRunways, availableGates, waitingArrivals, waitingDepartures);
            } finally {
                lock.unlock();
            }
        }
    }
