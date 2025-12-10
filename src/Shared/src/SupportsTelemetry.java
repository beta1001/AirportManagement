
package Shared.src;

public interface SupportsTelemetry {
	/* Provide the controller with a telemetry endpoint to report:
	 * Resource occupancy changes (runways/gates),
		Wait times (runway/gate),
		Runway utilization,
		Wake-up counts (notifyAll, ...),
		Taxiway queue entries.
	*/
    void setTelemetry(AirportTelemetry telemetry);
}