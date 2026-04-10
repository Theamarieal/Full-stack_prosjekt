package ntnu.no.fs_v26.model;

/**
 * Enumeration representing how a training document must be completed.
 *
 * <p>This enum defines the different completion mechanisms supported
 * by the system for training documents.</p>
 */
public enum TrainingCompletionType {

    /**
     * The user must read and acknowledge the document.
     */
    READ_ACKNOWLEDGE,

    /**
     * The user must complete and pass a quiz.
     */
    QUIZ,

    /**
     * The user must complete practical training approved by another user.
     */
    PRACTICAL_SIGN_OFF
}