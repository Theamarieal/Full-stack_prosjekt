package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.Deviation;
import ntnu.no.fs_v26.model.DeviationModule;
import ntnu.no.fs_v26.model.DeviationStatus;
import ntnu.no.fs_v26.model.Equipment;
import ntnu.no.fs_v26.model.TemperatureLog;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.DeviationRepository;
import ntnu.no.fs_v26.repository.EquipmentRepository;
import ntnu.no.fs_v26.repository.TemperatureLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for temperature logging and monitoring.
 *
 * <p>Handles recording and retrieval of temperature readings as part of the
 * IK-Mat food safety compliance module. Temperatures are validated against
 * the acceptable range configured per equipment, and a deviation is automatically
 * created when a reading falls outside that range.
 */
@Service
@RequiredArgsConstructor
public class TemperatureService {

    private final TemperatureLogRepository logRepository;
    private final EquipmentRepository equipmentRepository;
    private final DeviationRepository deviationRepository;

    /**
     * Records a new temperature reading for a specific piece of equipment.
     *
     * <p>Validates that the equipment belongs to the user's organization and that the
     * temperature value is within the physically plausible range of -50 to 100 °C.
     * If the reading is outside the equipment's configured acceptable range,
     * a deviation is automatically created.
     *
     * @param equipmentId the ID of the equipment being logged
     * @param value       the recorded temperature in degrees Celsius
     * @param user        the authenticated user recording the reading
     * @return the saved {@link TemperatureLog}
     * @throws RuntimeException if the equipment is not found, belongs to a different organization,
     *                          or the temperature value is out of the valid input range
     */
    public TemperatureLog logTemperature(Long equipmentId, double value, User user) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if (!equipment.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new RuntimeException("You do not have access to this equipment");
        }

        if (value < -50 || value > 100) {
            throw new RuntimeException("Temperature must be between -50 and 100 °C");
        }

        boolean deviation = value < equipment.getMinTemp() || value > equipment.getMaxTemp();

        TemperatureLog log = TemperatureLog.builder()
            .value(value)
            .timestamp(LocalDateTime.now())
            .equipment(equipment)
            .loggedBy(user)
            .isDeviation(deviation)
            .build();

        TemperatureLog savedLog = logRepository.save(log);

        if (deviation) {
            createTemperatureDeviation(savedLog);
        }

        return savedLog;
    }

    /**
     * Returns all temperature logs for the authenticated user's organization.
     *
     * @param user the authenticated user
     * @return a list of all {@link TemperatureLog} entries for the organization
     */
    public List<TemperatureLog> getLogsForOrganization(User user) {
        return logRepository.findAllByEquipmentOrganizationId(user.getOrganization().getId());
    }

    /**
     * Returns the most recent temperature logs for the organization, up to the given limit.
     *
     * @param user  the authenticated user
     * @param limit the maximum number of entries to return
     * @return a list of the latest {@link TemperatureLog} entries sorted by timestamp descending
     */
    public List<TemperatureLog> getLatestLogs(User user, int limit) {
        return logRepository.findAllByEquipmentOrganizationId(user.getOrganization().getId())
            .stream()
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Returns a filtered list of temperature logs for the organization.
     *
     * <p>Supports optional filtering by equipment ID, date range, and deviation status.
     * Results are sorted by timestamp descending.
     *
     * @param user        the authenticated user
     * @param equipmentId optional filter by equipment ID; {@code null} returns all equipment
     * @param fromDate    optional start date in ISO format (YYYY-MM-DD); {@code null} or blank to skip
     * @param toDate      optional end date in ISO format (YYYY-MM-DD); {@code null} or blank to skip
     * @param status      optional filter: {@code "OK"} for normal readings, {@code "DEVIATION"} for out-of-range; {@code null} returns all
     * @return a filtered and sorted list of {@link TemperatureLog} entries
     */
    public List<TemperatureLog> getHistory(
        User user,
        Long equipmentId,
        String fromDate,
        String toDate,
        String status) {

        return logRepository.findAllByEquipmentOrganizationId(user.getOrganization().getId())
            .stream()
            .filter(log -> equipmentId == null || log.getEquipment().getId().equals(equipmentId))
            .filter(log -> {
                if (fromDate == null || fromDate.isBlank()) return true;
                LocalDate from = LocalDate.parse(fromDate);
                return !log.getTimestamp().toLocalDate().isBefore(from);
            })
            .filter(log -> {
                if (toDate == null || toDate.isBlank()) return true;
                LocalDate to = LocalDate.parse(toDate);
                return !log.getTimestamp().toLocalDate().isAfter(to);
            })
            .filter(log -> {
                if (status == null || status.isBlank()) return true;
                if (status.equalsIgnoreCase("OK")) return !log.isDeviation();
                if (status.equalsIgnoreCase("DEVIATION")) return log.isDeviation();
                return true;
            })
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
    }

    /**
     * Returns a dashboard summary of today's temperature activity for the organization.
     *
     * <p>Includes the total number of measurements recorded today and
     * how many of those were flagged as deviations.
     *
     * @param user the authenticated user
     * @return a map containing {@code measurementsToday} and {@code deviationsToday}
     */
    public Map<String, Object> getSummary(User user) {
        Long organizationId = user.getOrganization().getId();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        long measurementsToday = logRepository.countByEquipmentOrganizationIdAndTimestampBetween(
            organizationId, startOfDay, endOfDay);

        long deviationsToday = logRepository.countByEquipmentOrganizationIdAndIsDeviationTrueAndTimestampBetween(
            organizationId, startOfDay, endOfDay);

        Map<String, Object> summary = new HashMap<>();
        summary.put("measurementsToday", measurementsToday);
        summary.put("deviationsToday", deviationsToday);

        return summary;
    }

    /**
     * Returns the most recent temperature deviation entries for the organization.
     *
     * @param user  the authenticated user
     * @param limit the maximum number of entries to return
     * @return a list of the latest {@link TemperatureLog} entries flagged as deviations
     */
    public List<TemperatureLog> getLatestDeviations(User user, int limit) {
        return logRepository.findAllByEquipmentOrganizationId(user.getOrganization().getId())
            .stream()
            .filter(TemperatureLog::isDeviation)
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Creates a deviation report when a temperature reading falls outside the acceptable range.
     *
     * @param log the temperature log entry that triggered the deviation
     */
    private void createTemperatureDeviation(TemperatureLog log) {
        String equipmentName = log.getEquipment() != null ? log.getEquipment().getName() : "Unknown equipment";

        Deviation deviation = Deviation.builder()
            .title("Temperature deviation detected")
            .description("Temperature reading " + log.getValue() + " °C for " + equipmentName
                + " is outside the allowed range.")
            .module(DeviationModule.IK_MAT)
            .status(DeviationStatus.OPEN)
            .createdAt(LocalDateTime.now())
            .reportedBy(log.getLoggedBy())
            .organization(log.getEquipment().getOrganization())
            .build();

        deviationRepository.save(deviation);
    }
}