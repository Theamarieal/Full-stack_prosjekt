package ntnu.no.fs_v26.model;

/**
 * Enumeration representing different types of alcohol compliance log entries.
 *
 * <p>This enum defines the various categories of events that can be recorded
 * in an {@link AlcoholLog}. Each value corresponds to a specific type of
 * alcohol-related activity or compliance check performed during a shift.</p>
 */
public enum AlcoholLogType {

    /**
     * Indicates that an age verification check was performed.
     */
    AGE_CHECK,

    /**
     * Indicates the start of alcohol serving.
     */
    SERVING_START,

    /**
     * Indicates the end of alcohol serving.
     */
    SERVING_END,

    /**
     * Indicates a break period during serving.
     */
    BREAK,

    /**
     * Indicates an incident related to alcohol handling or compliance.
     */
    INCIDENT
}