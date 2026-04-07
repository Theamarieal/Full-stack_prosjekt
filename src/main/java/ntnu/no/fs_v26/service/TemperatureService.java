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

@Service
@RequiredArgsConstructor
public class TemperatureService {

    private final TemperatureLogRepository logRepository;
    private final EquipmentRepository equipmentRepository;
    private final DeviationRepository deviationRepository;

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

    public List<TemperatureLog> getLogsForOrganization(User user) {
        return logRepository.findAllByEquipmentOrganizationId(user.getOrganization().getId());
    }

    public List<TemperatureLog> getLatestLogs(User user, int limit) {
        List<TemperatureLog> logs = logRepository.findAllByEquipmentOrganizationId(user.getOrganization().getId())
                .stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());

        return logs.stream().limit(limit).collect(Collectors.toList());
    }

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
                    if (fromDate == null || fromDate.isBlank()) {
                        return true;
                    }
                    LocalDate from = LocalDate.parse(fromDate);
                    return !log.getTimestamp().toLocalDate().isBefore(from);
                })
                .filter(log -> {
                    if (toDate == null || toDate.isBlank()) {
                        return true;
                    }
                    LocalDate to = LocalDate.parse(toDate);
                    return !log.getTimestamp().toLocalDate().isAfter(to);
                })
                .filter(log -> {
                    if (status == null || status.isBlank()) {
                        return true;
                    }
                    if (status.equalsIgnoreCase("OK")) {
                        return !log.isDeviation();
                    }
                    if (status.equalsIgnoreCase("DEVIATION")) {
                        return log.isDeviation();
                    }
                    return true;
                })
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getSummary(User user) {
        Long organizationId = user.getOrganization().getId();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        long measurementsToday = logRepository.countByEquipmentOrganizationIdAndTimestampBetween(
                organizationId,
                startOfDay,
                endOfDay);

        long deviationsToday = logRepository.countByEquipmentOrganizationIdAndIsDeviationTrueAndTimestampBetween(
                organizationId,
                startOfDay,
                endOfDay);

        Map<String, Object> summary = new HashMap<>();
        summary.put("measurementsToday", measurementsToday);
        summary.put("deviationsToday", deviationsToday);

        return summary;
    }

    public List<TemperatureLog> getLatestDeviations(User user, int limit) {
        List<TemperatureLog> logs = logRepository.findAllByEquipmentOrganizationId(user.getOrganization().getId())
                .stream()
                .filter(TemperatureLog::isDeviation)
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());

        return logs.stream().limit(limit).collect(Collectors.toList());
    }

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