package ntnu.no.fs_v26.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ntnu.no.fs_v26.model.Frequency;
import ntnu.no.fs_v26.model.ModuleType;
import java.util.List;

/**
 * Request body used when creating a new checklist.
 *
 * <p>Using a dedicated DTO instead of the {@link ntnu.no.fs_v26.model.Checklist} entity
 * directly prevents mass-assignment vulnerabilities and makes validation explicit.
 * Fields such as organization and creation timestamp are set server-side.
 */
public class ChecklistRequest {

    /** The title of the checklist. Required, max 255 characters. */
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    /** An optional description of the checklist's purpose. Max 1000 characters. */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    /** How often the checklist should be completed (e.g. DAILY, WEEKLY, MONTHLY). */
    private Frequency frequency;

    /** The compliance module this checklist belongs to (e.g. KITCHEN, BAR). */
    private ModuleType module;

    /** Optional list of checklist item descriptions to create along with the checklist. */
    private List<String> items;

    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }

    public ModuleType getModule() { return module; }
    public void setModule(ModuleType module) { this.module = module; }
}
