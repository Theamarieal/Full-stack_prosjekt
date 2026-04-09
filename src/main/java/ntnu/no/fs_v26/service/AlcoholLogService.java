package ntnu.no.fs_v26.service;

import ntnu.no.fs_v26.controller.AlcoholLogRequest;
import ntnu.no.fs_v26.controller.AlcoholLogResponse;
import ntnu.no.fs_v26.model.AlcoholLog;
import ntnu.no.fs_v26.model.AlcoholLogType;
import ntnu.no.fs_v26.model.Deviation;
import ntnu.no.fs_v26.model.DeviationModule;
import ntnu.no.fs_v26.model.DeviationStatus;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.AlcoholLogRepository;
import ntnu.no.fs_v26.repository.DeviationRepository;
import ntnu.no.fs_v26.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service class for alcohol compliance log operations.
 *
 * <p>Handles creation and retrieval of alcohol log entries as part of the IK-Alkohol module.
 * Enforces Norwegian alcohol serving regulations, including:
 * <ul>
 *   <li>Age requirements (18+ for beverages under 22%, 20+ for beverages at 22% or above)</li>
 *   <li>Serving time windows (low ABV from 08:00–01:00, high ABV from 13:00–23:59)</li>
 *   <li>Serving state validation (age checks and incidents require an active serving period)</li>
 * </ul>
 *
 * <p>Deviations are automatically created when compliance violations are detected.
 */
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

    /**
     * Creates and saves a new alcohol log entry for the current shift.
     *
     * <p>Validates the serving state and request fields before saving.
     * Automatically creates a deviation if a compliance violation is detected.
     *
     * @param request   the log request body
     * @param userEmail the email of the authenticated user creating the log
     * @return the saved log as an {@link AlcoholLogResponse}
     * @throws IllegalArgumentException if validation fails (e.g. missing fields, serving not started)
     */
    public AlcoholLogResponse createLog(AlcoholLogRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        LocalDate shiftDate = LocalDate.now();

        if (request.getRecordedTime() == null || request.getRecordedTime().isBlank()) {
            throw new IllegalArgumentException("Recorded time is required.");
        }

        LocalTime recordedTime = LocalTime.parse(request.getRecordedTime());
        LocalDateTime recordedAt = LocalDateTime.of(shiftDate, recordedTime);

        Organization organization = user.getOrganization();

        validateServingStateForShift(request.getType(), shiftDate, organization.getId());
        validateRequest(request);

        AlcoholLog log = new AlcoholLog();
        log.setType(request.getType());
        log.setRecordedAt(recordedAt);
        log.setShiftDate(shiftDate);
        log.setRecordedBy(user);
        log.setOrganization(organization);
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

    /**
     * Returns all alcohol log entries recorded for the current day in the user's organization.
     *
     * @param userEmail the email of the authenticated user
     * @return a list of today's log entries as {@link AlcoholLogResponse} objects
     */
    public List<AlcoholLogResponse> getCurrentDayLogs(String userEmail) {
        User user = getUserByEmail(userEmail);
        LocalDate today = LocalDate.now();

        return alcoholLogRepository
            .findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(user.getOrganization().getId(), today)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    /**
     * Returns alcohol log entries for a specific date in the user's organization.
     * Only accessible to users with the MANAGER or ADMIN role.
     *
     * @param userEmail the email of the authenticated user
     * @param date      the date to retrieve logs for
     * @return a list of log entries for the given date
     * @throws IllegalArgumentException if the user does not have the required role
     */
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

    /**
     * Checks whether any alcohol logs have been recorded today in the user's organization.
     * Used by the dashboard to indicate compliance status.
     *
     * @param userEmail the email of the authenticated user
     * @return {@code true} if at least one log exists for today
     */
    public boolean hasLogsForToday(String userEmail) {
        User user = getUserByEmail(userEmail);
        LocalDate today = LocalDate.now();

        return !alcoholLogRepository
            .findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(user.getOrganization().getId(), today)
            .isEmpty();
    }

    /**
     * Validates the full request body based on log type and field requirements.
     *
     * @param request the log request to validate
     * @throws IllegalArgumentException if any required field is missing or notes are not allowed
     */
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

    /**
     * Validates fields specific to age check log entries.
     *
     * @param request the log request to validate
     * @throws IllegalArgumentException if any required age check field is missing or out of range
     */
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

    /**
     * Automatically creates a deviation if a compliance violation is detected in the log entry.
     * Violations include: ID not checked, serving to underage guests, and serving outside legal hours.
     *
     * @param log the saved alcohol log to evaluate
     */
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

        if (title == null) return;

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

    /**
     * Checks whether a recorded serving time violates the legal serving time window.
     *
     * @param recordedTime the time to evaluate
     * @return {@code true} if the time is outside both allowed windows
     */
    private boolean isServingTimeViolation(LocalTime recordedTime) {
        boolean allowedLowAbv = isWithinRange(recordedTime, LOW_ABV_START, LOW_ABV_END);
        boolean allowedHighAbv = !recordedTime.isBefore(HIGH_ABV_START)
            && !recordedTime.isAfter(HIGH_ABV_END);
        return !allowedLowAbv && !allowedHighAbv;
    }

    /**
     * Checks whether a time value falls within a time range, supporting ranges that cross midnight.
     *
     * @param value the time to check
     * @param start the start of the range
     * @param end   the end of the range
     * @return {@code true} if the value is within the range
     */
    private boolean isWithinRange(LocalTime value, LocalTime start, LocalTime end) {
        if (end.isAfter(start) || end.equals(start)) {
            return !value.isBefore(start) && !value.isAfter(end);
        }
        return !value.isBefore(start) || !value.isAfter(end);
    }

    /**
     * Looks up a user by email address.
     *
     * @param email the email to look up
     * @return the matching user
     * @throws IllegalArgumentException if no user with the given email exists
     */
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found."));
    }

    /**
     * Checks whether the user has the MANAGER or ADMIN role.
     *
     * @param user the user to check
     * @return {@code true} if the user is a manager or admin
     */
    private boolean isManagerOrAdmin(User user) {
        return user.getRole() != null
            && ("MANAGER".equals(user.getRole().name()) || "ADMIN".equals(user.getRole().name()));
    }

    /**
     * Maps an {@link AlcoholLog} entity to an {@link AlcoholLogResponse} DTO.
     *
     * @param log the log entity to map
     * @return the corresponding response DTO
     */
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

    /**
     * Determines whether a guest meets the legal age requirement for the given alcohol percentage.
     * Beverages at 22% ABV or above require the guest to be at least 20 years old.
     *
     * @param guestAge          the guest's age
     * @param alcoholPercentage the alcohol percentage of the beverage
     * @return {@code true} if the guest is old enough to be served
     */
    private boolean isGuestOldEnough(Integer guestAge, Double alcoholPercentage) {
        if (guestAge == null || alcoholPercentage == null) return false;
        if (alcoholPercentage >= 22.0) return guestAge >= 20;
        return guestAge >= 18;
    }

    /**
     * Validates that the log type is permitted given the current serving state of the shift.
     * For example, age checks and incidents require serving to have started.
     *
     * @param type           the log type being registered
     * @param shiftDate      the current shift date
     * @param organizationId the ID of the organization
     * @throws IllegalArgumentException if the log type is not permitted in the current serving state
     */
    private void validateServingStateForShift(
        AlcoholLogType type,
        LocalDate shiftDate,
        Long organizationId) {
        boolean hasServingStart = hasLogTypeOnShift(shiftDate, organizationId, AlcoholLogType.SERVING_START);
        boolean hasServingEnd = hasLogTypeOnShift(shiftDate, organizationId, AlcoholLogType.SERVING_END);
        boolean servingActive = hasServingStart && !hasServingEnd;

        if (type == AlcoholLogType.SERVING_START && hasServingStart) {
            throw new IllegalArgumentException("Serving start has already been registered for this shift.");
        }

        if (type == AlcoholLogType.SERVING_END) {
            if (!hasServingStart) {
                throw new IllegalArgumentException("Serving end cannot be registered before serving start.");
            }
            if (hasServingEnd) {
                throw new IllegalArgumentException("Serving end has already been registered for this shift.");
            }
        }

        if ((type == AlcoholLogType.AGE_CHECK || type == AlcoholLogType.BREAK || type == AlcoholLogType.INCIDENT)
            && !servingActive) {
            throw new IllegalArgumentException(getServingStateErrorMessage(type));
        }
    }

    /**
     * Checks whether a specific log type has been registered during a shift.
     *
     * @param shiftDate      the date of the shift
     * @param organizationId the ID of the organization
     * @param type           the log type to check for
     * @return {@code true} if a log of the given type exists for the shift
     */
    private boolean hasLogTypeOnShift(LocalDate shiftDate, Long organizationId, AlcoholLogType type) {
        return alcoholLogRepository
            .findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(organizationId, shiftDate)
            .stream()
            .anyMatch(log -> log.getType() == type);
    }

    /**
     * Returns an appropriate error message when a log type is not permitted
     * given the current serving state.
     *
     * @param type the log type that was attempted
     * @return a human-readable error message
     */
    private String getServingStateErrorMessage(AlcoholLogType type) {
        return switch (type) {
            case AGE_CHECK ->
                "Age check can only be registered after serving has started and before serving has ended.";
            case BREAK -> "Break can only be registered after serving has started and before serving has ended.";
            case INCIDENT -> "Incident can only be registered after serving has started and before serving has ended.";
            default -> "This log type cannot be registered right now.";
        };
    }
}