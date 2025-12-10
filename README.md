# Airport Management Simulator

## Overview

This project simulates concurrent airport operations using Java Swing for visualization and compares two synchronization strategies:

Monitor (synchronized, wait, notifyAll)
Semaphore (java.util.concurrent.Semaphore)

The simulation models:

Limited runways and gates
Priority for arrivals over departures
Real-time telemetry tracking and CSV export for performance analysis

## Features

Interactive GUI:

Start simulation with chosen version (Monitor or Semaphore)
Add arrivals or departures dynamically
Pause/Resume and Reset controls

Telemetry:

Tracks occupied runways/gates, taxi queue size, average wait times, runway utilization, and wake-up counts
Exports data to telemetry_data.csv for analysis

Comparative Analysis:

Two scenarios tested:

Phase A: Bursty traffic (alternating arrivals/departures)
Phase B: Steady mixed load (~80 planes)

Metrics visualized via Python charts (Matplotlib)

## Project Structure

```
AirportManagement/
├── GUI/src/ # Swing GUI components (MainWindow, panels)
├── MonitorVersion/src # Monitor-based implementation
├── SemaphoreVersion/src # Semaphore-based implementation
├── Shared/src/ # Common classes (AirportTelemetry, Airport interface)
├── Docs/ # Documentation and comparative report
├── Tests/ # contains phase A and B tests
├── generate_charts.py # Python script for CSV visualization
└── README.md

```

## How to Run

Compile and run GUI:

```
javac GUI/src/MainWindow.java
java GUI.src.MainWindowShow

```

Start simulation:

Select version: Monitor or Semaphore
Set runways and gates
Click Start

Add planes:

Use Add Arrival or Add Departure

Export telemetry:

Data saved automatically to telemetry_data.csv in the working directory

## Analysis Workflow

Run simulations for both versions under chosen scenarios.
Collect CSV files from Tests folder

Generate charts:
generate_charts.py
Charts include:

Average wait times
Runway utilization
Taxi queue size
Wake-up counts (notifyAll)

## Results Summary

Phase A (Bursty):

Monitor: avgRunwayWait ≈ 0.5 ms, avgGateWait ≈ 24.7 ms, utilization ≈ 0.37, notifyAll = 93
Semaphore: avgRunwayWait ≈ 85 ms, avgGateWait ≈ 80 ms, utilization ≈ 0.13, notifyAll = 0

Phase B (Steady):

Monitor: avgRunwayWait ≈ 747 ms, avgGateWait ≈ 942 ms, utilization ≈ 0.49, notifyAll ≈ 197
Semaphore: avgRunwayWait ≈ 370 ms, avgGateWait ≈ 239 ms, utilization ≈ 0.44, notifyAll = 0

Conclusion:

Monitor excels in bursty traffic (lower waits, higher responsiveness).
Semaphore excels in steady load (lower waits, zero wake-up overhead).

## Requirements

Java 17+
Python 3.8+ (for charts)
Libraries: pandas, matplotlib
