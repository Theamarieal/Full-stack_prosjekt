package ntnu.no.fs_v26.model;

/**
 * Enumeration representing user roles within the system.
 *
 * <p>This enum defines different levels of access and responsibility,
 * used for authorization and role-based access control.</p>
 */
public enum Role {

    /**
     * Administrator with full system access.
     */
    ADMIN,

    /**
     * Manager with elevated permissions within an organization.
     */
    MANAGER,

    /**
     * Standard employee with limited access.
     */
    EMPLOYEE
}