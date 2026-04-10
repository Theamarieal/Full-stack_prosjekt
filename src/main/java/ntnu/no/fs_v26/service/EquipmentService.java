package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.dto.EquipmentResponse;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for equipment retrieval.
 *
 * <p>Provides access to equipment registered for an organization,
 * mapped to {@link EquipmentResponse} DTOs for use in the temperature logging module.
 */
@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    /**
     * Returns all equipment registered for the authenticated user's organization,
     * sorted alphabetically by name.
     *
     * @param user the authenticated user
     * @return a list of {@link EquipmentResponse} objects including id, name, and temperature range
     */
    public List<EquipmentResponse> getEquipmentForOrganization(User user) {
        return equipmentRepository.findAllByOrganizationIdOrderByNameAsc(user.getOrganization().getId())
            .stream()
            .map(equipment -> new EquipmentResponse(
                equipment.getId(),
                equipment.getName(),
                equipment.getMinTemp(),
                equipment.getMaxTemp()))
            .toList();
    }
}