package ntnu.no.fs_v26.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing an alcohol compliance log entry.
 *
 * <p>An {@code AlcoholLog} stores information about an alcohol-related event
 * recorded by a user within an organization. The entity supports different
 * types of compliance-related registrations, such as age checks, identification
 * checks, denied service incidents, and general alcohol-related observations.</p>
 *
 * <p>Each log entry belongs to exactly one organization and is recorded by one user.
 * In this application, one calendar date is treated as one work shift, and the
 * {@code shiftDate} field is used to group log entries by shift.</p>
 */
@Entity
@Table(name = "alcohol_logs")
public class AlcoholLog {

    /**
     * Unique identifier for the alcohol log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The type of alcohol log entry.
     *
     * <p>This value determines what kind of alcohol-related event the log represents.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlcoholLogType type;

    /**
     * Optional notes associated with the log entry.
     *
     * <p>This field may be used to store additional context or remarks
     * about the recorded event.</p>
     */
    @Column(length = 1000)
    private String notes;

    /**
     * The exact date and time when the log entry was recorded.
     */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    /**
     * The calendar date the log belongs to.
     *
     * <p>In this application, one date is treated as one shift.</p>
     */
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    /**
     * The guest's age, when relevant for the log entry.
     *
     * <p>This field is primarily used for entries related to age verification.</p>
     */
    @Column(name = "guest_age")
    private Integer guestAge;

    /**
     * The alcohol percentage involved in the recorded event, when applicable.
     *
     * <p>This field may be relevant for age checks or compliance assessments
     * involving alcoholic beverages.</p>
     */
    @Column(name = "alcohol_percentage")
    private Double alcoholPercentage;

    /**
     * Indicates whether identification was checked.
     *
     * <p>This field is typically used in entries involving age verification.</p>
     */
    @Column(name = "id_checked")
    private Boolean idChecked;

    /**
     * Indicates whether service was denied.
     *
     * <p>This field can be used to document compliance actions where alcohol
     * service was refused.</p>
     */
    @Column(name = "service_denied")
    private Boolean serviceDenied;

    /**
     * The user who recorded the alcohol log entry.
     */
    @ManyToOne
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy;

    /**
     * The organization that the alcohol log entry belongs to.
     */
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /**
     * Creates a new empty {@code AlcoholLog}.
     */
    public AlcoholLog() {
    }

    /**
     * Returns the unique identifier of the alcohol log entry.
     *
     * @return the log ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the type of alcohol log entry.
     *
     * @return the log type
     */
    public AlcoholLogType getType() {
        return type;
    }

    /**
     * Sets the type of alcohol log entry.
     *
     * @param type the log type to set
     */
    public void setType(AlcoholLogType type) {
        this.type = type;
    }

    /**
     * Returns the optional notes attached to the log entry.
     *
     * @return the notes, or {@code null} if no notes are provided
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the optional notes attached to the log entry.
     *
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Returns the exact date and time when the log was recorded.
     *
     * @return the recorded timestamp
     */
    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    /**
     * Sets the exact date and time when the log was recorded.
     *
     * @param recordedAt the timestamp to set
     */
    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    /**
     * Returns the shift date associated with the log entry.
     *
     * @return the shift date
     */
    public LocalDate getShiftDate() {
        return shiftDate;
    }

    /**
     * Sets the shift date associated with the log entry.
     *
     * @param shiftDate the shift date to set
     */
    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    /**
     * Returns the user who recorded the log entry.
     *
     * @return the recording user
     */
    public User getRecordedBy() {
        return recordedBy;
    }

    /**
     * Sets the user who recorded the log entry.
     *
     * @param recordedBy the user to set
     */
    public void setRecordedBy(User recordedBy) {
        this.recordedBy = recordedBy;
    }

    /**
     * Returns the organization that the log entry belongs to.
     *
     * @return the organization
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * Sets the organization that the log entry belongs to.
     *
     * @param organization the organization to set
     */
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * Returns the guest age associated with the log entry.
     *
     * @return the guest age, or {@code null} if not applicable
     */
    public Integer getGuestAge() {
        return guestAge;
    }

    /**
     * Sets the guest age associated with the log entry.
     *
     * @param guestAge the guest age to set
     */
    public void setGuestAge(Integer guestAge) {
        this.guestAge = guestAge;
    }

    /**
     * Returns the alcohol percentage associated with the log entry.
     *
     * @return the alcohol percentage, or {@code null} if not applicable
     */
    public Double getAlcoholPercentage() {
        return alcoholPercentage;
    }

    /**
     * Sets the alcohol percentage associated with the log entry.
     *
     * @param alcoholPercentage the alcohol percentage to set
     */
    public void setAlcoholPercentage(Double alcoholPercentage) {
        this.alcoholPercentage = alcoholPercentage;
    }

    /**
     * Returns whether identification was checked.
     *
     * @return {@code true} if identification was checked, {@code false} if not,
     *         or {@code null} if not applicable
     */
    public Boolean getIdChecked() {
        return idChecked;
    }

    /**
     * Sets whether identification was checked.
     *
     * @param idChecked {@code true} if identification was checked, otherwise {@code false}
     */
    public void setIdChecked(Boolean idChecked) {
        this.idChecked = idChecked;
    }

    /**
     * Returns whether service was denied.
     *
     * @return {@code true} if service was denied, {@code false} if not,
     *         or {@code null} if not applicable
     */
    public Boolean getServiceDenied() {
        return serviceDenied;
    }

    /**
     * Sets whether service was denied.
     *
     * @param serviceDenied {@code true} if service was denied, otherwise {@code false}
     */
    public void setServiceDenied(Boolean serviceDenied) {
        this.serviceDenied = serviceDenied;
    }
}