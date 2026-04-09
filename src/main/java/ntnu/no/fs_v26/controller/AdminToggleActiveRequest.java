package ntnu.no.fs_v26.controller;

import lombok.Data;

/**
 * Request body for toggling the active status of a user account.
 * Used by admins to activate or deactivate users in their organization.
 */
@Data
public class AdminToggleActiveRequest {

  /** Whether the user account should be active ({@code true}) or inactive ({@code false}). */
  private boolean active;
}
