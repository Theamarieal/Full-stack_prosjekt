package ntnu.no.fs_v26.model;

/**
 * Enumeration representing the lifecycle status of a deviation.
 *
 * <p>This enum is used to track the progress of a {@link Deviation}
 * from creation to resolution. It helps indicate whether a deviation
 * is newly reported, being handled, or fully resolved.</p>
 */
public enum DeviationStatus {

    /**
     * The deviation has been reported but not yet addressed.
     */
    OPEN,

    /**
     * The deviation is currently being handled or investigated.
     */
    IN_PROGRESS,

    /**
     * The deviation has been resolved and no further action is required.
     */
    RESOLVED
}