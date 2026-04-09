package ntnu.no.fs_v26.model;

/**
 * Enumeration representing different operational modules within the system.
 *
 * <p>This enum is used to categorize entities such as checklists and deviations
 * based on the area of operation they belong to, such as kitchen, bar, or safety.</p>
 */
public enum ModuleType {

    /**
     * Module related to kitchen operations and food handling.
     */
    KITCHEN,

    /**
     * Module related to cleaning and hygiene tasks.
     */
    CLEANING,

    /**
     * Module related to bar operations and alcohol handling.
     */
    BAR,

    /**
     * Module related to reception or front desk operations.
     */
    RECEPTION,

    /**
     * Module related to safety procedures and compliance.
     */
    SAFETY
}