package net.dankito.fritzbox.utils.web.callbacks;

import net.dankito.fritzbox.utils.web.responses.GetCallListResponse;

/**
 * Created by ganymed on 26/11/16.
 */

public interface GetCallListCallback {

  void completed(GetCallListResponse response);

}
