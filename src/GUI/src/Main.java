package GUI.src;
import java.util.ArrayList;
import java.util.List;

import SemaphoreVersion.src.AirportSemaphore;
import Shared.src.Airplane;
import Shared.src.Airport;
public class Main {
	public static void main(String[] args) throws InterruptedException {
        // Choose implementation here: swap to AirportMonitor or AirportReentrantLock
        final Airport airport = new AirportSemaphore(2, 4);
        System.out.println("Starting simulation with: " + airport.getStatus());

        List<Airplane> fleet = new ArrayList<>();
        // create a mix of arrivals and departures
        for (int i = 0; i < 6; i++) {
            boolean arrivalFirst = (i % 2 == 0);
            Airplane p = new Airplane("P" + (i + 1), airport, arrivalFirst);
            fleet.add(p);
            p.start();
            Thread.sleep(120); // stagger arrivals
        }

        // Wait for all to finish
        for (Airplane p : fleet) {
            p.join();
        }

        System.out.println("Simulation finished. Final status: " + airport.getStatus());
    }
}
