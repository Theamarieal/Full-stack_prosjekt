package ntnu.no.fs_v26.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) containing summarized report statistics.
 *
 * <p>This class provides aggregated data used in reports, giving an overview
 * of deviations and logged events within a specific time period.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryDto {

    /**
     * Total number of deviations recorded.
     */
    private long totalDeviations;

    /**
     * Number of currently open deviations.
     */
    private long openDeviations;

    /**
     * Number of resolved deviations.
     */
    private long resolvedDeviations;

    /**
     * Total number of temperature logs recorded.
     */
    private long totalTemperatureLogs;

    /**
     * Number of temperature logs classified as deviations.
     */
    private long temperatureDeviations;

    /**
     * Total number of alcohol logs recorded.
     */
    private long totalAlcoholLogs;

    /**
     * Number of alcohol-related deviation events.
     */
    private long alcoholDeviationEvents;
}