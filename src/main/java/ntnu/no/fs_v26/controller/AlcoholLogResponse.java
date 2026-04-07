package ntnu.no.fs_v26.controller;

import ntnu.no.fs_v26.model.AlcoholLogType;

import java.time.LocalDateTime;

public class AlcoholLogResponse {

    private Long id;
    private AlcoholLogType type;
    private String notes;
    private LocalDateTime recordedAt;
    private String recordedBy;
    private Integer guestAge;
    private Double alcoholPercentage;
    private Boolean idChecked;
    private Boolean serviceDenied;

    public AlcoholLogResponse(
            Long id,
            AlcoholLogType type,
            String notes,
            LocalDateTime recordedAt,
            String recordedBy,
            Integer guestAge,
            Double alcoholPercentage,
            Boolean idChecked,
            Boolean serviceDenied
    ) {
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

    public Long getId() {
        return id;
    }

    public AlcoholLogType getType() {
        return type;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public Integer getGuestAge() {
        return guestAge;
    }

    public Double getAlcoholPercentage() {
        return alcoholPercentage;
    }

    public Boolean getIdChecked() {
        return idChecked;
    }

    public Boolean getServiceDenied() {
        return serviceDenied;
    }
}