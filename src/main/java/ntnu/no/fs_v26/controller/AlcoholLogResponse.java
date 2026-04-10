package ntnu.no.fs_v26.controller;

import ntnu.no.fs_v26.model.AlcoholLogType;

import java.time.LocalDateTime;

/**
 * Response body representing a single alcohol log entry.
 *
 * <p>Returned by the alcohol log endpoints to expose log data
 * without leaking internal entity structure.
 */
public class AlcoholLogResponse {

    /** The unique identifier of the log entry. */
    private Long id;

    /** The type of alcohol log entry (e.g. OPENING, CLOSING, AGE_CHECK). */
    private AlcoholLogType type;

    /** Optional notes attached to the log entry. */
    private String notes;

    /** The timestamp when the log entry was recorded. */
    private LocalDateTime recordedAt;

    /** The email of the user who recorded the log entry. */
    private String recordedBy;

    /** The age of the guest, if applicable. */
    private Integer guestAge;

    /** The alcohol percentage of the beverage, if applicable. */
    private Double alcoholPercentage;

    /** Whether identification was checked, if applicable. */
    private Boolean idChecked;

    /** Whether service was denied, if applicable. */
    private Boolean serviceDenied;

    /**
     * Constructs an {@code AlcoholLogResponse} with all fields.
     *
     * @param id                the log entry ID
     * @param type              the log type
     * @param notes             optional notes
     * @param recordedAt        the timestamp of the entry
     * @param recordedBy        the email of the user who recorded it
     * @param guestAge          the guest's age, if applicable
     * @param alcoholPercentage the alcohol percentage, if applicable
     * @param idChecked         whether ID was checked, if applicable
     * @param serviceDenied     whether service was denied, if applicable
     */
    public AlcoholLogResponse(
        Long id, AlcoholLogType type, String notes, LocalDateTime recordedAt,
        String recordedBy, Integer guestAge, Double alcoholPercentage,
        Boolean idChecked, Boolean serviceDenied) {
        this.id = id;
        this.type = type;
        this.notes = notes;
        this.recordedAt = recordedAt;
        this.recordedBy = recordedBy;
        this.guestAge = guestAge;
        this.alcoholPercentage = alcoholPercentage;
        this.idChecked = idChecked;
        this.serviceDenied = serviceDenied;
    }

    public Long getId() { return id; }
    public AlcoholLogType getType() { return type; }
    public String getNotes() { return notes; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public String getRecordedBy() { return recordedBy; }
    public Integer getGuestAge() { return guestAge; }
    public Double getAlcoholPercentage() { return alcoholPercentage; }
    public Boolean getIdChecked() { return idChecked; }
    public Boolean getServiceDenied() { return serviceDenied; }
}