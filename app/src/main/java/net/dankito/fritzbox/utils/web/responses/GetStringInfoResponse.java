package net.dankito.fritzbox.utils.web.responses;

/**
 * Created by ganymed on 26/11/16.
 */

public class GetStringInfoResponse extends ResponseBase {

  protected String stringInfo;


  public GetStringInfoResponse(String error) {
    super(error);
  }

  public GetStringInfoResponse(boolean isSuccessful, String stringInfo) {
    super(isSuccessful);
    this.stringInfo = stringInfo;
  }


  public String getStringInfo() {
    return stringInfo;
  }


  @Override
  public String toString() {
    return super.toString() + "; stringInfo = " + stringInfo;
  }

}
