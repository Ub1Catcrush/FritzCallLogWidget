package net.dankito.fritzbox.utils.web.responses;

/**
 * Created by ganymed on 26/11/16.
 */

public class LoginResponse extends ResponseBase {

  public LoginResponse(String error) {
    super(error);
  }

  public LoginResponse(boolean isSuccessful) {
    super(isSuccessful);
  }

}
