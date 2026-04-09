package ntnu.no.fs_v26.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ntnu.no.fs_v26.model.AlcoholLog;
import ntnu.no.fs_v26.model.Deviation;
import ntnu.no.fs_v26.model.TemperatureLog;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object (DTO) representing a generated report.
 *
 * <p>This class contains aggregated and detailed data for a given organization
 * within a specified date range. It includes summary information as well as
 * various logs and deviation events.</p>
 *
 * <p>The DTO is typically used as a response when generating reports for
 * monitoring, auditing, or analysis purposes.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    /**
     * The name of the organization the report belongs to.
     */
    private String organizationName;

    /**
     * The start date of the report period.
     */
    private LocalDate from;

    /**
     * The end date of the report period.
     */
    private LocalDate to;

    /**
     * A summary of the report, containing aggregated statistics.
     */
    private ReportSummaryDto summary;

    /**
     * A list of registered deviations within the report period.
     */
    private List<Deviation> deviations;

    /**
     * A list of all recorded temperature logs.
     */
    private List<TemperatureLog> temperatureLogs;

    /**
     * A list of temperature logs that represent deviations.
     */
    private List<TemperatureLog> temperatureDeviationLogs;

    /**
     * A list of alcohol measurement logs.
     */
    private List<AlcoholLog> alcoholLogs;

    /**
     * A list of alcohol-related deviation events.
     */
    private List<AlcoholLog> alcoholDeviationEvents;
}