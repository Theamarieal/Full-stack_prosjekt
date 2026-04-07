package ntnu.no.fs_v26.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ntnu.no.fs_v26.model.AlcoholLogType;

/**
 * Request body used when creating a new alcohol log entry.
 */
public class AlcoholLogRequest {

    /**
     * Type of alcohol log entry. Required.
     */
    @NotNull(message = "Log type is required")
    private AlcoholLogType type;

    /**
     * Optional notes for the log entry.
     * Notes are only used for incident entries.
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    /**
     * Time of day when the event happened (e.g. "14:30").
     */
    @Size(max = 10, message = "Recorded time must not exceed 10 characters")
    private String recordedTime;

    /**
     * Guest age used for age-check validation.
     * Must be between 0 and 90 if provided.
     */
    @Min(value = 0, message = "Guest age must be 0 or greater")
    @Max(value = 90, message = "Guest age must be 90 or less")
    private Integer guestAge;

    /**
     * Alcohol percentage used for age-check validation.
     * Must be between 0.0 and 100.0 if provided.
     */
    private Double alcoholPercentage;

    /**
     * Indicates whether identification was checked.
     */
    private Boolean idChecked;

    /**
     * Indicates whether service was denied.
     */
    private Boolean serviceDenied;

    public AlcoholLogType getType() {
        return type;
    }

    public void setType(AlcoholLogType type) {
        this.type = type;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRecordedTime() {
        return recordedTime;
    }

    public void setRecordedTime(String recordedTime) {
        this.recordedTime = recordedTime;
    }

    public Integer getGuestAge() {
        return guestAge;
    }

    public void setGuestAge(Integer guestAge) {
        this.guestAge = guestAge;
    }

    public Double getAlcoholPercentage() {
        return alcoholPercentage;
    }

    public void setAlcoholPercentage(Double alcoholPercentage) {
        this.alcoholPercentage = alcoholPercentage;
    }

    public Boolean getIdChecked() {
        return idChecked;
    }

    public void setIdChecked(Boolean idChecked) {
        this.idChecked = idChecked;
    }

    public Boolean getServiceDenied() {
        return serviceDenied;
    }

    public void setServiceDenied(Boolean serviceDenied) {
        this.serviceDenied = serviceDenied;
    }
}
