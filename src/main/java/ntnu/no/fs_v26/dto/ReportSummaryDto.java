package ntnu.no.fs_v26.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryDto {
    private long totalDeviations;
    private long openDeviations;
    private long resolvedDeviations;
    private long totalTemperatureLogs;
    private long temperatureDeviations;
    private long totalAlcoholLogs;
    private long alcoholDeviationEvents;
}