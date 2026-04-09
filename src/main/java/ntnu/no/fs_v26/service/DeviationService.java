package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.controller.DeviationRequest;
import ntnu.no.fs_v26.model.Deviation;
import ntnu.no.fs_v26.model.DeviationStatus;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.DeviationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for deviation report management.
 *
 * <p>Handles creation, retrieval, and status updates of deviation reports.
 * All operations are scoped to the authenticated user's organization.
 * Status updates are restricted to users with the MANAGER or ADMIN role.
 */
@Service
@RequiredArgsConstructor
public class DeviationService {

    private final DeviationRepository repository;

    /**
     * Returns a paginated list of deviations for the user's organization,
     * sorted by creation date descending.
     *
     * @param user the authenticated user
     * @param page the page number (zero-indexed)
     * @param size the number of items per page
     * @return a page of deviations for the organization
     */
    public Page<Deviation> getDeviations(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repository.findAllByOrganizationId(user.getOrganization().getId(), pageable);
    }

    /**
     * Returns all deviations for the user's organization.
     * Used by report generation.
     *
     * @param user the authenticated user
     * @return a list of all deviations for the organization
     */
    public List<Deviation> getAllDeviations(User user) {
        return repository.findAllByOrganizationId(user.getOrganization().getId());
    }

    /**
     * Creates and saves a new deviation report for the user's organization.
     *
     * <p>Uses a {@link DeviationRequest} DTO to prevent mass-assignment —
     * fields such as status, organization, reportedBy, and timestamps
     * are set server-side and cannot be influenced by the caller.
     *
     * @param request the validated request body
     * @param user    the authenticated user reporting the deviation
     * @return the saved deviation with status {@link DeviationStatus#OPEN}
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
     * <p>Verifies that the deviation belongs to the user's organization before updating.
     * If the new status is {@link DeviationStatus#RESOLVED}, the resolved timestamp is set automatically.
     *
     * @param id        the ID of the deviation to update
     * @param newStatus the new status to set
     * @param user      the authenticated manager or admin
     * @return the updated deviation
     * @throws IllegalArgumentException if the deviation is not found
     * @throws org.springframework.security.access.AccessDeniedException if the deviation belongs to a different organization
     */
    public Deviation updateStatus(Long id, DeviationStatus newStatus, User user) {
        Deviation deviation = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Deviation not found"));

        if (!deviation.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Access denied: deviation belongs to a different organization");
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

