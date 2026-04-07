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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private String organizationName;
    private LocalDate from;
    private LocalDate to;
    private ReportSummaryDto summary;
    private List<Deviation> deviations;
    private List<TemperatureLog> temperatureLogs;
    private List<TemperatureLog> temperatureDeviationLogs;
    private List<AlcoholLog> alcoholLogs;
    private List<AlcoholLog> alcoholDeviationEvents;
}