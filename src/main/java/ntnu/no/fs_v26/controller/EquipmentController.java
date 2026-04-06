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

@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
@Tag(name = "Equipment", description = "Endpoints for equipment management")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    @Operation(summary = "Get all equipment for the logged-in user's organization")
    public ResponseEntity<List<EquipmentResponse>> getAllEquipment(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(equipmentService.getEquipmentForOrganization(user));
    }
}