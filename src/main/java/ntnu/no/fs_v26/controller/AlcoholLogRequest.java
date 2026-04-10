package ntnu.no.fs_v26.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ntnu.no.fs_v26.model.AlcoholLogType;

/**
 * Request body used when creating a new alcohol log entry.
 *
 * <p>Different fields are relevant depending on the log type. For instance,
 * {@code guestAge}, {@code alcoholPercentage}, {@code idChecked}, and
 * {@code serviceDenied} are primarily used for age-check entries,
 * while {@code notes} is used for incident descriptions.
 */
public class AlcoholLogRequest {

    /** The type of alcohol log entry. Required. */
    @NotNull(message = "Log type is required")
    private AlcoholLogType type;

    /**
     * Optional free-text notes for the log entry.
     * Typically used for incident or denied-service entries.
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    /** The time of day when the event occurred, e.g. {@code "14:30"}. */
    @Size(max = 10, message = "Recorded time must not exceed 10 characters")
    private String recordedTime;

    /**
     * The estimated or verified age of the guest.
     * Used for age-check validation. Must be between 0 and 90 if provided.
     */
    @Min(value = 0, message = "Guest age must be 0 or greater")
    @Max(value = 90, message = "Guest age must be 90 or less")
    private Integer guestAge;

    /** The alcohol percentage of the beverage being served. */
    private Double alcoholPercentage;

    /** Whether identification was checked for the guest. */
    private Boolean idChecked;

    /** Whether service was denied to the guest. */
    private Boolean serviceDenied;

    public AlcoholLogType getType() { return type; }
    public void setType(AlcoholLogType type) { this.type = type; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRecordedTime() { return recordedTime; }
    public void setRecordedTime(String recordedTime) { this.recordedTime = recordedTime; }

    public Integer getGuestAge() { return guestAge; }
    public void setGuestAge(Integer guestAge) { this.guestAge = guestAge; }

    public Double getAlcoholPercentage() { return alcoholPercentage; }
    public void setAlcoholPercentage(Double alcoholPercentage) { this.alcoholPercentage = alcoholPercentage; }

    public Boolean getIdChecked() { return idChecked; }
    public void setIdChecked(Boolean idChecked) { this.idChecked = idChecked; }

    public Boolean getServiceDenied() { return serviceDenied; }
    public void setServiceDenied(Boolean serviceDenied) { this.serviceDenied = serviceDenied; }
}
