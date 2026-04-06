package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.dto.EquipmentResponse;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

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