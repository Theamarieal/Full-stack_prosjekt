package ntnu.no.fs_v26.dto;

import lombok.Data;

@Data
public class PracticalSignOffRequest {
    private Long employeeId;
    private String note;
}