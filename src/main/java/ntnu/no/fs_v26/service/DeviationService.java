package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
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

    public List<Deviation> getDeviations(User user) {
        return repository.findAllByOrganizationId(user.getOrganization().getId());
    }

    public Deviation reportDeviation(Deviation deviation, User user) {
        deviation.setReportedBy(user);
        deviation.setOrganization(user.getOrganization());
        deviation.setStatus(DeviationStatus.OPEN);
        deviation.setCreatedAt(LocalDateTime.now());
        return repository.save(deviation);
    }

    public Deviation updateStatus(Long id, DeviationStatus newStatus, User user) {
        Deviation deviation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deviation not found"));

        if (!deviation.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized");
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