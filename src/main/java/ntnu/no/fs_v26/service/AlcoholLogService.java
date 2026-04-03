package ntnu.no.fs_v26.service;

import ntnu.no.fs_v26.controller.AlcoholLogRequest;
import ntnu.no.fs_v26.controller.AlcoholLogResponse;
import ntnu.no.fs_v26.model.AlcoholLog;
import ntnu.no.fs_v26.model.AlcoholLogType;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.AlcoholLogRepository;
import ntnu.no.fs_v26.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service layer for alcohol log operations.
 *
 * <p>
 * In this simplified model, one day is treated as one shift.
 * The service validates age rules and serving times before saving logs.
 */
@Service
public class AlcoholLogService {

    /**
     * Normal serving start time for alcohol below 22 percent.
     */
    private static final LocalTime LOW_ABV_START = LocalTime.of(8, 0);

    /**
     * Normal serving end time for alcohol below 22 percent.
     */
    private static final LocalTime LOW_ABV_END = LocalTime.of(1, 0);

    /**
     * Normal serving start time for alcohol at or above 22 percent.
     */
    private static final LocalTime HIGH_ABV_START = LocalTime.of(13, 0);

    /**
     * Normal serving end time for alcohol at or above 22 percent.
     *
     * <p>
     * Midnight is represented as 23:59 to keep validation simple in LocalTime.
     */
    private static final LocalTime HIGH_ABV_END = LocalTime.of(23, 59);

    private final AlcoholLogRepository alcoholLogRepository;
    private final UserRepository userRepository;

    public AlcoholLogService(
            AlcoholLogRepository alcoholLogRepository,
            UserRepository userRepository) {
        this.alcoholLogRepository = alcoholLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new alcohol log for the authenticated user's organization.
     *
     * @param request   the alcohol log request
     * @param userEmail the authenticated user's email
     * @return the saved alcohol log as a response DTO
     */
    public AlcoholLogResponse createLog(AlcoholLogRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        LocalDate shiftDate = LocalDate.now();
        LocalTime recordedTime = LocalTime.parse(request.getRecordedTime());
        LocalDateTime recordedAt = LocalDateTime.of(shiftDate, recordedTime);

        validateRequest(request, recordedTime);

        AlcoholLog log = new AlcoholLog();
        log.setType(request.getType());
        boolean denialNotesAllowed = request.getType() == AlcoholLogType.AGE_CHECK
                && Boolean.TRUE.equals(request.getServiceDenied())
                && isGuestOldEnough(request.getGuestAge(), request.getAlcoholPercentage());

        if (request.getType() == AlcoholLogType.INCIDENT || denialNotesAllowed) {
            log.setNotes(request.getNotes());
        } else {
            log.setNotes(null);
        }

        log.setRecordedAt(recordedAt);
        log.setShiftDate(shiftDate);
        log.setRecordedBy(user);
        log.setOrganization(user.getOrganization());
        log.setGuestAge(request.getGuestAge());
        log.setAlcoholPercentage(request.getAlcoholPercentage());
        log.setIdChecked(request.getIdChecked());
        log.setServiceDenied(request.getServiceDenied());

        AlcoholLog saved = alcoholLogRepository.save(log);

        /**
         * Create an additional INCIDENT log if service is denied
         * even though the guest is legally old enough.
         */
        if (request.getType() == AlcoholLogType.AGE_CHECK
                && Boolean.TRUE.equals(request.getServiceDenied())
                && isGuestOldEnough(request.getGuestAge(), request.getAlcoholPercentage())
                && request.getNotes() != null
                && !request.getNotes().isBlank()) {

            AlcoholLog incidentLog = new AlcoholLog();
            incidentLog.setType(AlcoholLogType.INCIDENT);
            incidentLog.setNotes(request.getNotes());
            incidentLog.setRecordedAt(recordedAt);
            incidentLog.setShiftDate(shiftDate);
            incidentLog.setRecordedBy(user);
            incidentLog.setOrganization(user.getOrganization());
            incidentLog.setGuestAge(request.getGuestAge());
            incidentLog.setAlcoholPercentage(request.getAlcoholPercentage());
            incidentLog.setIdChecked(request.getIdChecked());
            incidentLog.setServiceDenied(true);

            alcoholLogRepository.save(incidentLog);
        }

        return mapToResponse(saved);
    }

    /**
     * Returns alcohol logs for the current day for the authenticated user's
     * organization.
     *
     * @param userEmail the authenticated user's email
     * @return a list of alcohol log response DTOs
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
     * Returns alcohol logs for a specific date for the authenticated user's
     * organization.
     *
     * <p>
     * This method is restricted to managers and administrators.
     *
     * @param userEmail the authenticated user's email
     * @param date      the date to search for
     * @return a list of alcohol log response DTOs
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
     * Validates the request before persistence.
     *
     * @param request      the alcohol log request
     * @param recordedTime the parsed recorded time
     */
    private void validateRequest(AlcoholLogRequest request, LocalTime recordedTime) {
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
        } else {
            if (request.getNotes() != null && !request.getNotes().isBlank()) {
                throw new IllegalArgumentException(
                        "Notes are only allowed for incidents or denied age checks that require an explanation.");
            }
        }

        if (request.getType() == AlcoholLogType.AGE_CHECK) {
            validateAgeCheck(request);
        }

        if (request.getType() == AlcoholLogType.SERVING_START || request.getType() == AlcoholLogType.SERVING_END) {
            validateServingTime(recordedTime);
        }
    }

    /**
     * Validates an age-check request against alcohol age rules.
     *
     * @param request the alcohol log request
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

        if (!Boolean.TRUE.equals(request.getIdChecked())) {
            throw new IllegalArgumentException("ID must be checked for an age check registration.");
        }

        int age = request.getGuestAge();
        double percentage = request.getAlcoholPercentage();

        if (age < 18 && !Boolean.TRUE.equals(request.getServiceDenied())) {
            throw new IllegalArgumentException("Service must be denied for guests under 18.");
        }

        if (percentage >= 22.0 && age < 20 && !Boolean.TRUE.equals(request.getServiceDenied())) {
            throw new IllegalArgumentException(
                    "Service must be denied for alcohol at 22% or more when the guest is under 20.");
        }
    }

    /**
     * Validates serving times against the simplified legal normal-time model.
     *
     * @param recordedTime the serving time to validate
     */
    private void validateServingTime(LocalTime recordedTime) {
        boolean allowedLowAbv = isWithinRange(recordedTime, LOW_ABV_START, LOW_ABV_END);
        boolean allowedHighAbv = !recordedTime.isBefore(HIGH_ABV_START) && !recordedTime.isAfter(HIGH_ABV_END);

        if (!allowedLowAbv && !allowedHighAbv) {
            throw new IllegalArgumentException("Serving time is outside the allowed legal normal-time window.");
        }
    }

    /**
     * Returns whether a time is inside a range that passes midnight.
     *
     * @param value the time to test
     * @param start the range start
     * @param end   the range end
     * @return true if the value is inside the range
     */
    private boolean isWithinRange(LocalTime value, LocalTime start, LocalTime end) {
        if (end.isAfter(start) || end.equals(start)) {
            return !value.isBefore(start) && !value.isAfter(end);
        }
        return !value.isBefore(start) || !value.isAfter(end);
    }

    /**
     * Returns the authenticated user by email.
     *
     * @param email the authenticated user's email
     * @return the user entity
     */
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found."));
    }

    /**
     * Returns whether the given user has manager or administrator access.
     *
     * @param user the user entity
     * @return true if the user is a manager or administrator
     */
    private boolean isManagerOrAdmin(User user) {
        String role = String.valueOf(user.getRole());
        return "MANAGER".equals(role) || "ADMIN".equals(role);
    }

    /**
     * Maps an alcohol log entity to a response DTO.
     *
     * @param log the alcohol log entity
     * @return the response DTO
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
     * Returns whether the current day has any alcohol logs.
     *
     * @param userEmail the authenticated user's email
     * @return true if at least one log exists for today
     */
    public boolean hasLogsForToday(String userEmail) {
        User user = getUserByEmail(userEmail);
        LocalDate today = LocalDate.now();

        return !alcoholLogRepository
                .findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                        user.getOrganization().getId(),
                        today)
                .isEmpty();
    }

    /**
     * Returns whether the guest is legally old enough for the given alcohol
     * percentage.
     *
     * @param guestAge          the guest age
     * @param alcoholPercentage the alcohol percentage
     * @return true if the guest is legally old enough
     */
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