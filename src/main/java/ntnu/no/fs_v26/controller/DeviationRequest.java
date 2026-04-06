package ntnu.no.fs_v26.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ntnu.no.fs_v26.model.DeviationModule;

/**
 * Request body used when reporting a new deviation.
 *
 * <p>Using a dedicated DTO instead of the Deviation entity directly prevents
 * mass-assignment vulnerabilities — callers cannot set status, organization,
 * or reportedBy through the request body.
 */
public class DeviationRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private DeviationModule module;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DeviationModule getModule() {
        return module;
    }

    public void setModule(DeviationModule module) {
        this.module = module;
    }
}
