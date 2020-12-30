package net.dankito.fritzbox.utils.web.callbacks;

import net.dankito.fritzbox.utils.web.responses.WebClientResponse;

/**
 * Created by ganymed on 03/11/16.
 */

public interface RequestCallback {

  void completed(WebClientResponse response);

}
