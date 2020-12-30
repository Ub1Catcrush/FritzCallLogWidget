package net.dankito.fritzbox.services.listener;

import net.dankito.fritzbox.model.UserSettings;

/**
 * Created by ganymed on 27/11/16.
 */

public interface UserSettingsManagerListener {

  void userSettingsUpdated(UserSettings updatedSettings);

}
