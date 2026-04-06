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

@Service
@RequiredArgsConstructor
public class ReportService {

    private final DeviationRepository deviationRepository;
    private final TemperatureLogRepository temperatureLogRepository;
    private final AlcoholLogRepository alcoholLogRepository;

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
                .findByOrganizationIdAndRecordedAtBetweenOrderByRecordedAtDesc(organizationId, fromDateTime,
                        toDateTime);

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

    private boolean isAlcoholDeviationEvent(AlcoholLog log) {
        if (log.getType() == null) {
            return false;
        }

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