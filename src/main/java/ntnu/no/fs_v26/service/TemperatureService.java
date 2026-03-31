package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.*;
import ntnu.no.fs_v26.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemperatureService {
    private final TemperatureLogRepository logRepository;
    private final EquipmentRepository equipmentRepository;

    public TemperatureLog logTemperature(Long equipmentId, double value, User user) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        // automatic deviationdetection
        boolean deviation = value < equipment.getMinTemp() || value > equipment.getMaxTemp();

        TemperatureLog log = TemperatureLog.builder()
                .value(value)
                .timestamp(LocalDateTime.now())
                .equipment(equipment)
                .loggedBy(user)
                .isDeviation(deviation)
                .build();

        return logRepository.save(log);
    }

    public List<TemperatureLog> getLogsForOrganization(User user) {
        return logRepository.findAllByEquipmentOrganizationId(user.getOrganization().getId());
    }
}