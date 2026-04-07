package ntnu.no.fs_v26.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ntnu.no.fs_v26.model.Frequency;
import ntnu.no.fs_v26.model.ModuleType;
import java.util.List;

/**
 * Request body used when creating a new checklist.
 *
 * <p>Using a dedicated DTO instead of the Checklist entity directly prevents
 * mass-assignment vulnerabilities and makes validation explicit.
 */
public class ChecklistRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Frequency frequency;

    private ModuleType module;

    private List<String> items;

    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }

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

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public ModuleType getModule() {
        return module;
    }

    public void setModule(ModuleType module) {
        this.module = module;
    }
}
