package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.controller.DeviationRequest;
import ntnu.no.fs_v26.model.Deviation;
import ntnu.no.fs_v26.model.DeviationStatus;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.DeviationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviationService {

    private final DeviationRepository repository;

    /**
     * Returns all deviations belonging to the user's organization.
     *
     * @param user the authenticated user
     * @return list of deviations for the user's organization
     */
    public List<Deviation> getDeviations(User user) {
        return repository.findAllByOrganizationId(user.getOrganization().getId());
    }

    /**
     * Creates and saves a new deviation report.
     *
     * <p>Accepts a {@link DeviationRequest} DTO instead of the entity directly
     * to prevent mass-assignment — callers cannot set status, organization,
     * reportedBy, or timestamps through the request body.
     *
     * @param request the validated request body
     * @param user    the authenticated user reporting the deviation
     * @return the saved deviation with status OPEN
     */
    public Deviation reportDeviation(DeviationRequest request, User user) {
        Deviation deviation = Deviation.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .module(request.getModule())
                .status(DeviationStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .reportedBy(user)
                .organization(user.getOrganization())
                .build();

        return repository.save(deviation);
    }

    /**
     * Updates the status of an existing deviation.
     *
     * <p>Verifies that the deviation belongs to the user's organization
     * before allowing the update.
     *
     * @param id        the id of the deviation to update
     * @param newStatus the new status to set
     * @param user      the authenticated manager or admin
     * @return the updated deviation
     * @throws IllegalArgumentException if the deviation is not found or belongs to a different org
     */
    public Deviation updateStatus(Long id, DeviationStatus newStatus, User user) {
        Deviation deviation = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deviation not found"));

        // Security: verify deviation belongs to the user's organization
        if (!deviation.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: deviation belongs to a different organization");
        }

        deviation.setStatus(newStatus);

        if (newStatus == DeviationStatus.RESOLVED) {
            deviation.setResolvedAt(LocalDateTime.now());
        } else {
            deviation.setResolvedAt(null);
        }

        return repository.save(deviation);
    }
}

