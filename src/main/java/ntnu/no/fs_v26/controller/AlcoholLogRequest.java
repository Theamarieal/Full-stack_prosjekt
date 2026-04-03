package ntnu.no.fs_v26.controller;

import ntnu.no.fs_v26.model.AlcoholLogType;

/**
 * Request body used when creating a new alcohol log entry.
 */
public class AlcoholLogRequest {

    /**
     * Type of alcohol log entry.
     */
    private AlcoholLogType type;

    /**
     * Optional notes for the log entry.
     *
     * <p>
     * Notes are only used for incident entries.
     */
    private String notes;

    /**
     * Time of day when the event happened.
     */
    private String recordedTime;

    /**
     * Guest age used for age-check validation.
     */
    private Integer guestAge;

    /**
     * Alcohol percentage used for age-check validation.
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