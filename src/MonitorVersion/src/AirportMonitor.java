package MonitorVersion.src;

import Shared.src.Airplane;
import Shared.src.Airport;

public class AirportMonitor implements Airport {

	private final Object lock = new Object();
	private int availableRunways;
	private int availableGates;
	private int waitingArrivals = 0;
	private int waitingDepartures = 0;
	
	public AirportMonitor(int runways, int gates) {
		this.availableRunways = runways;
		this.availableGates = gates;
	}
	
	
	@Override
	public void requestRunwayForArrival(Airplane a) throws InterruptedException {
		synchronized (lock) {
			waitingArrivals++;
			try {
				while (availableRunways == 0) {
					lock.wait();
				}
				availableRunways--;
			} finally {
				waitingArrivals--;
			}
		}
	}
	
	
	@Override
	public void requestRunwayForDeparture(Airplane a) throws InterruptedException {
		synchronized (lock) {
			waitingDepartures++;
			try {
				while (availableRunways == 0 || waitingArrivals > 0) {
					lock.wait();
				}
				availableRunways--;
			} finally {
				waitingDepartures--;
			}
		}
	}
	
	
	@Override
	public void requestGate(Airplane a) throws InterruptedException {
		synchronized (lock) {
			while (availableGates == 0) {
				lock.wait();
			}
			availableGates--;
		}
	}
	
	
	@Override
	public void releaseRunway(Airplane a) {
		synchronized (lock) {
			availableRunways++;
			lock.notifyAll();
		}
	}
	
	
	@Override
	public void releaseGate(Airplane a) {
		synchronized (lock) {
			availableGates++;
			lock.notifyAll();
		}
	}
	
	
	@Override
	public String getStatus() {
		synchronized (lock) {
			return String.format("[Monitor] runways=%d, gates=%d, waitingArrivals=%d, waitingDepartures=%d",
			availableRunways, availableGates, waitingArrivals, waitingDepartures);
		}
	}
}