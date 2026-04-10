package ntnu.no.fs_v26.model;

/**
 * Enumeration representing how often a task or checklist should be performed.
 *
 * <p>This enum is typically used in {@link Checklist} to define how frequently
 * a checklist must be completed.</p>
 */
public enum Frequency {

    /**
     * Task must be performed every day.
     */
    DAILY,

    /**
     * Task must be performed once per week.
     */
    WEEKLY,

    /**
     * Task must be performed once per month.
     */
    MONTHLY,

    /**
     * Task must be performed once per year.
     */
    YEARLY
}