package ntnu.no.fs_v26.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing an alcohol compliance log entry.
 *
 * <p>
 * Each alcohol log belongs to one organization and one calendar date.
 * In this simplified model, one day is treated as one shift.
 */
@Entity
@Table(name = "alcohol_logs")
public class AlcoholLog {

    /**
     * Unique identifier for the alcohol log.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type of alcohol log entry.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlcoholLogType type;

    /**
     * Optional notes attached to the log entry.
     */
    @Column(length = 1000)
    private String notes;

    /**
     * Full date-time when the log was recorded.
     */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    /**
     * Calendar date the log belongs to.
     *
     * <p>
     * In this application, one date is treated as one shift.
     */
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    /**
     * Guest age used for age-check entries.
     */
    @Column(name = "guest_age")
    private Integer guestAge;

    /**
     * Alcohol percentage used for age-check entries.
     */
    @Column(name = "alcohol_percentage")
    private Double alcoholPercentage;

    /**
     * Indicates whether identification was checked.
     */
    @Column(name = "id_checked")
    private Boolean idChecked;

    /**
     * Indicates whether service was denied.
     */
    @Column(name = "service_denied")
    private Boolean serviceDenied;

    /**
     * User who recorded the log entry.
     */
    @ManyToOne
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy;

    /**
     * Organization the log belongs to.
     */
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public AlcoholLog() {
    }

    public Long getId() {
        return id;
    }

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

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public User getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(User recordedBy) {
        this.recordedBy = recordedBy;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
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