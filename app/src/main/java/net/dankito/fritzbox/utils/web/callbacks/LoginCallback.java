package net.dankito.fritzbox.utils.web.callbacks;

import net.dankito.fritzbox.utils.web.responses.LoginResponse;

/**
 * Created by ganymed on 26/11/16.
 */

public interface LoginCallback {

  void completed(LoginResponse response);

}
