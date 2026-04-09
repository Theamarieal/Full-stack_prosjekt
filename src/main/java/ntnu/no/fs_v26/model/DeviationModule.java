package ntnu.no.fs_v26.model;

/**
 * Enumeration representing the module or domain area where a deviation occurred.
 *
 * <p>This enum is used to classify deviations according to the part of the
 * system or compliance domain they belong to. It helps separate deviations
 * related to food safety from those related to alcohol compliance.</p>
 */
public enum DeviationModule {

    /**
     * Deviation related to internal control for food handling.
     */
    IK_MAT,

    /**
     * Deviation related to internal control for alcohol handling.
     */
    IK_ALKOHOL
}