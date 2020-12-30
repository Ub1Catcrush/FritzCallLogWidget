package net.dankito.fritzbox.utils.web.responses;


/**
 * Created by ganymed on 03/11/16.
 */

public class WebClientResponse extends ResponseBase {

  protected String body;


  public WebClientResponse(String error) {
    super(error);
  }

  public WebClientResponse(boolean isSuccessful) {
    super(isSuccessful);
  }

  public WebClientResponse(boolean successful, String body) {
    super(successful);

    this.body = body;
  }


  public String getBody() {
    return body;
  }


  @Override
  public String toString() {
    return super.toString() + "; body = \n" + body;
  }
}
