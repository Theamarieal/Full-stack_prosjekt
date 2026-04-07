package ntnu.no.fs_v26.service;

import ntnu.no.fs_v26.controller.AlcoholLogRequest;
import ntnu.no.fs_v26.controller.AlcoholLogResponse;
import ntnu.no.fs_v26.model.AlcoholLog;
import ntnu.no.fs_v26.model.AlcoholLogType;
import ntnu.no.fs_v26.model.Deviation;
import ntnu.no.fs_v26.model.DeviationModule;
import ntnu.no.fs_v26.model.DeviationStatus;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.AlcoholLogRepository;
import ntnu.no.fs_v26.repository.DeviationRepository;
import ntnu.no.fs_v26.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AlcoholLogService {

    private static final LocalTime LOW_ABV_START = LocalTime.of(8, 0);
    private static final LocalTime LOW_ABV_END = LocalTime.of(1, 0);

    private static final LocalTime HIGH_ABV_START = LocalTime.of(13, 0);
    private static final LocalTime HIGH_ABV_END = LocalTime.of(23, 59);

    private final AlcoholLogRepository alcoholLogRepository;
    private final UserRepository userRepository;
    private final DeviationRepository deviationRepository;

    public AlcoholLogService(
            AlcoholLogRepository alcoholLogRepository,
            UserRepository userRepository,
            DeviationRepository deviationRepository) {
        this.alcoholLogRepository = alcoholLogRepository;
        this.userRepository = userRepository;
        this.deviationRepository = deviationRepository;
    }

    public AlcoholLogResponse createLog(AlcoholLogRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        LocalDate shiftDate = LocalDate.now();

        if (request.getRecordedTime() == null || request.getRecordedTime().isBlank()) {
            throw new IllegalArgumentException("Recorded time is required.");
        }

        LocalTime recordedTime = LocalTime.parse(request.getRecordedTime());
        LocalDateTime recordedAt = LocalDateTime.of(shiftDate, recordedTime);

        validateRequest(request);

        AlcoholLog log = new AlcoholLog();
        log.setType(request.getType());
        log.setRecordedAt(recordedAt);
        log.setShiftDate(shiftDate);
        log.setRecordedBy(user);
        log.setOrganization(user.getOrganization());
        log.setGuestAge(request.getGuestAge());
        log.setAlcoholPercentage(request.getAlcoholPercentage());
        log.setIdChecked(request.getIdChecked());
        log.setServiceDenied(request.getServiceDenied());

        if (request.getType() == AlcoholLogType.INCIDENT) {
            log.setNotes(request.getNotes());
        } else if (request.getType() == AlcoholLogType.AGE_CHECK
                && Boolean.TRUE.equals(request.getServiceDenied())
                && isGuestOldEnough(request.getGuestAge(), request.getAlcoholPercentage())) {
            log.setNotes(request.getNotes());
        } else {
            log.setNotes(null);
        }

        AlcoholLog saved = alcoholLogRepository.save(log);

        createDeviationIfNeeded(saved);

        return mapToResponse(saved);
    }

    public List<AlcoholLogResponse> getCurrentDayLogs(String userEmail) {
        User user = getUserByEmail(userEmail);
        LocalDate today = LocalDate.now();

        return alcoholLogRepository
                .findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(user.getOrganization().getId(), today)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AlcoholLogResponse> getLogsByDate(String userEmail, LocalDate date) {
        User user = getUserByEmail(userEmail);

        if (!isManagerOrAdmin(user)) {
            throw new IllegalArgumentException("You are not allowed to search historical alcohol logs.");
        }

        return alcoholLogRepository
                .findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(user.getOrganization().getId(), date)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public boolean hasLogsForToday(String userEmail) {
        User user = getUserByEmail(userEmail);
        LocalDate today = LocalDate.now();

        return !alcoholLogRepository
                .findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(user.getOrganization().getId(), today)
                .isEmpty();
    }

    private void validateRequest(AlcoholLogRequest request) {
        if (request.getType() == null) {
            throw new IllegalArgumentException("Log type is required.");
        }

        if (request.getRecordedTime() == null || request.getRecordedTime().isBlank()) {
            throw new IllegalArgumentException("Recorded time is required.");
        }

        boolean denialNotesAllowed = request.getType() == AlcoholLogType.AGE_CHECK
                && Boolean.TRUE.equals(request.getServiceDenied())
                && isGuestOldEnough(request.getGuestAge(), request.getAlcoholPercentage());

        if (request.getType() == AlcoholLogType.INCIDENT) {
            if (request.getNotes() == null || request.getNotes().isBlank()) {
                throw new IllegalArgumentException("Notes are required for incident logs.");
            }
        } else if (denialNotesAllowed) {
            if (request.getNotes() == null || request.getNotes().isBlank()) {
                throw new IllegalArgumentException(
                        "A reason is required when service is denied despite the guest being old enough.");
            }
        } else if (request.getNotes() != null && !request.getNotes().isBlank()) {
            throw new IllegalArgumentException(
                    "Notes are only allowed for incidents or denied age checks that require an explanation.");
        }

        if (request.getType() == AlcoholLogType.AGE_CHECK) {
            validateAgeCheck(request);
        }
    }

    private void validateAgeCheck(AlcoholLogRequest request) {
        if (request.getGuestAge() == null) {
            throw new IllegalArgumentException("Guest age is required for age checks.");
        }

        if (request.getAlcoholPercentage() == null) {
            throw new IllegalArgumentException("Alcohol percentage is required for age checks.");
        }

        if (request.getIdChecked() == null) {
            throw new IllegalArgumentException("ID check status is required for age checks.");
        }

        if (request.getServiceDenied() == null) {
            throw new IllegalArgumentException("Service denied status is required for age checks.");
        }

        if (request.getGuestAge() < 0 || request.getGuestAge() > 90) {
            throw new IllegalArgumentException("Guest age must be between 0 and 90.");
        }

        if (request.getAlcoholPercentage() < 0 || request.getAlcoholPercentage() > 60) {
            throw new IllegalArgumentException("Alcohol percentage must be between 0 and 60.");
        }
    }

    private void createDeviationIfNeeded(AlcoholLog log) {
        String title = null;
        String description = null;

        if (log.getType() == AlcoholLogType.AGE_CHECK) {
            if (Boolean.FALSE.equals(log.getIdChecked())) {
                title = "Alcohol compliance deviation: ID not checked";
                description = "An age check was registered without ID verification.";
            } else if (Boolean.FALSE.equals(log.getServiceDenied())
                    && log.getGuestAge() != null
                    && log.getAlcoholPercentage() != null
                    && log.getGuestAge() < 18) {
                title = "Alcohol compliance deviation: Service to guest under 18";
                description = "Alcohol service was not denied for a guest under 18.";
            } else if (Boolean.FALSE.equals(log.getServiceDenied())
                    && log.getGuestAge() != null
                    && log.getAlcoholPercentage() != null
                    && log.getAlcoholPercentage() >= 22.0
                    && log.getGuestAge() < 20) {
                title = "Alcohol compliance deviation: Strong alcohol served to guest under 20";
                description = "Alcohol at 22% or above was not denied for a guest under 20.";
            } else if (Boolean.TRUE.equals(log.getServiceDenied())
                    && isGuestOldEnough(log.getGuestAge(), log.getAlcoholPercentage())) {
                title = "Alcohol compliance deviation: Service denied despite legal age";
                description = "Service was denied even though the guest met the legal age requirement.";
            }
        }

        if ((log.getType() == AlcoholLogType.SERVING_START || log.getType() == AlcoholLogType.SERVING_END)
                && isServingTimeViolation(log.getRecordedAt().toLocalTime())) {
            title = "Alcohol compliance deviation: Serving outside allowed time window";
            description = "Serving activity was registered outside the allowed legal normal-time window.";
        }

        if (title == null) {
            return;
        }

        Deviation deviation = Deviation.builder()
                .title(title)
                .description(description)
                .module(DeviationModule.IK_ALKOHOL)
                .status(DeviationStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .reportedBy(log.getRecordedBy())
                .organization(log.getOrganization())
                .build();

        deviationRepository.save(deviation);
    }

    private boolean isServingTimeViolation(LocalTime recordedTime) {
        boolean allowedLowAbv = isWithinRange(recordedTime, LOW_ABV_START, LOW_ABV_END);
        boolean allowedHighAbv = !recordedTime.isBefore(HIGH_ABV_START)
                && !recordedTime.isAfter(HIGH_ABV_END);

        return !allowedLowAbv && !allowedHighAbv;
    }

    private boolean isWithinRange(LocalTime value, LocalTime start, LocalTime end) {
        if (end.isAfter(start) || end.equals(start)) {
            return !value.isBefore(start) && !value.isAfter(end);
        }
        return !value.isBefore(start) || !value.isAfter(end);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found."));
    }

    private boolean isManagerOrAdmin(User user) {
        return user.getRole() != null
                && ("MANAGER".equals(user.getRole().name()) || "ADMIN".equals(user.getRole().name()));
    }

    private AlcoholLogResponse mapToResponse(AlcoholLog log) {
        return new AlcoholLogResponse(
                log.getId(),
                log.getType(),
                log.getNotes(),
                log.getRecordedAt(),
                log.getRecordedBy().getEmail(),
                log.getGuestAge(),
                log.getAlcoholPercentage(),
                log.getIdChecked(),
                log.getServiceDenied());
    }

    private boolean isGuestOldEnough(Integer guestAge, Double alcoholPercentage) {
        if (guestAge == null || alcoholPercentage == null) {
            return false;
        }

        if (alcoholPercentage >= 22.0) {
            return guestAge >= 20;
        }

        return guestAge >= 18;
    }
}