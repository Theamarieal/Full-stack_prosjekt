package ntnu.no.fs_v26.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.dto.EquipmentResponse;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.service.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for equipment management.
 *
 * <p>Provides endpoints for retrieving equipment registered for an organization.
 * Equipment is used in the IK-Mat module to associate temperature logs
 * with specific units such as refrigerators and freezers.
 *
 * <p>Base path: {@code /api/v1/equipment}
 */
@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
@Tag(name = "Equipment", description = "Endpoints for equipment management")
public class EquipmentController {

    private final EquipmentService equipmentService;

    /**
     * Returns all equipment registered for the authenticated user's organization.
     *
     * @param user the authenticated user
     * @return a list of {@link EquipmentResponse} objects including acceptable temperature ranges
     */
    @GetMapping
    @Operation(summary = "Get all equipment for the logged-in user's organization")
    public ResponseEntity<List<EquipmentResponse>> getAllEquipment(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(equipmentService.getEquipmentForOrganization(user));
    }
}