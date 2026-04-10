package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.dto.ReportResponse;
import ntnu.no.fs_v26.dto.ReportSummaryDto;
import ntnu.no.fs_v26.model.AlcoholLog;
import ntnu.no.fs_v26.model.Deviation;
import ntnu.no.fs_v26.model.DeviationStatus;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.AlcoholLogRepository;
import ntnu.no.fs_v26.repository.DeviationRepository;
import ntnu.no.fs_v26.repository.TemperatureLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service class for compliance report generation.
 *
 * <p>Aggregates data from deviations, temperature logs, and alcohol logs
 * across a specified date range to produce a full compliance report
 * for use by managers and administrators.
 *
 * <p>Alcohol deviation events are identified by evaluating individual log entries
 * against Norwegian alcohol serving regulations (age requirements, ID checks).
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final DeviationRepository deviationRepository;
    private final TemperatureLogRepository temperatureLogRepository;
    private final AlcoholLogRepository alcoholLogRepository;

    /**
     * Generates a compliance report for the authenticated user's organization
     * covering the specified date range.
     *
     * <p>The report includes deviations, temperature logs, alcohol logs,
     * and derived compliance event counts summarised in a {@link ReportSummaryDto}.
     *
     * @param user the authenticated manager or admin
     * @param from the start date of the report period (inclusive)
     * @param to   the end date of the report period (inclusive)
     * @return a {@link ReportResponse} containing all aggregated compliance data
     * @throws IllegalArgumentException if either date is null or if {@code from} is after {@code to}
     */
    public ReportResponse generateReport(User user, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both from and to dates are required.");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date cannot be after 'to' date.");
        }

        Long organizationId = user.getOrganization().getId();
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<Deviation> deviations = deviationRepository
            .findAllByOrganizationIdAndCreatedAtBetween(organizationId, fromDateTime, toDateTime);

        List<ntnu.no.fs_v26.model.TemperatureLog> temperatureLogs = temperatureLogRepository
            .findAllByEquipmentOrganizationIdAndTimestampBetween(organizationId, fromDateTime, toDateTime);

        List<AlcoholLog> alcoholLogs = alcoholLogRepository
            .findByOrganizationIdAndRecordedAtBetweenOrderByRecordedAtDesc(organizationId, fromDateTime, toDateTime);

        List<ntnu.no.fs_v26.model.TemperatureLog> temperatureDeviationLogs = temperatureLogs.stream()
            .filter(ntnu.no.fs_v26.model.TemperatureLog::isDeviation)
            .toList();

        List<AlcoholLog> alcoholDeviationEvents = alcoholLogs.stream()
            .filter(this::isAlcoholDeviationEvent)
            .toList();

        ReportSummaryDto summary = ReportSummaryDto.builder()
            .totalDeviations(deviations.size())
            .openDeviations(deviations.stream().filter(d -> d.getStatus() == DeviationStatus.OPEN).count())
            .resolvedDeviations(deviations.stream().filter(d -> d.getStatus() == DeviationStatus.RESOLVED).count())
            .totalTemperatureLogs(temperatureLogs.size())
            .temperatureDeviations(temperatureDeviationLogs.size())
            .totalAlcoholLogs(alcoholLogs.size())
            .alcoholDeviationEvents(alcoholDeviationEvents.size())
            .build();

        return ReportResponse.builder()
            .organizationName(user.getOrganization().getName())
            .from(from)
            .to(to)
            .summary(summary)
            .deviations(deviations)
            .temperatureLogs(temperatureLogs)
            .temperatureDeviationLogs(temperatureDeviationLogs)
            .alcoholLogs(alcoholLogs)
            .alcoholDeviationEvents(alcoholDeviationEvents)
            .build();
    }

    /**
     * Determines whether an alcohol log entry constitutes a compliance deviation.
     *
     * <p>A log is considered a deviation if any of the following are true:
     * <ul>
     *   <li>ID was not checked during an age check</li>
     *   <li>Alcohol was served to a guest under 18, or strong alcohol (22%+) to a guest under 20</li>
     *   <li>Service was denied despite the guest meeting the legal age requirement</li>
     * </ul>
     *
     * @param log the alcohol log entry to evaluate
     * @return {@code true} if the entry represents a compliance violation
     */
    private boolean isAlcoholDeviationEvent(AlcoholLog log) {
        if (log.getType() == null) return false;

        boolean noIdCheck = Boolean.FALSE.equals(log.getIdChecked());
        boolean servedMinor = Boolean.FALSE.equals(log.getServiceDenied())
            && log.getGuestAge() != null
            && log.getAlcoholPercentage() != null
            && (log.getGuestAge() < 18
            || (log.getAlcoholPercentage() >= 22.0 && log.getGuestAge() < 20));

        boolean deniedOldEnough = Boolean.TRUE.equals(log.getServiceDenied())
            && log.getGuestAge() != null
            && log.getAlcoholPercentage() != null
            && ((log.getAlcoholPercentage() < 22.0 && log.getGuestAge() >= 18)
            || (log.getAlcoholPercentage() >= 22.0 && log.getGuestAge() >= 20));

        return noIdCheck || servedMinor || deniedOldEnough;
    }
}