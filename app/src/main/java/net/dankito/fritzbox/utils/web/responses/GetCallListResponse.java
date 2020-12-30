package net.dankito.fritzbox.utils.web.responses;

import net.dankito.fritzbox.model.Call;

import java.util.List;

/**
 * Created by ganymed on 26/11/16.
 */

public class GetCallListResponse extends ResponseBase {

  protected List<Call> callList = null;


  public GetCallListResponse(String error) {
    super(error);
  }

  public GetCallListResponse(List<Call> callList) {
    super(true);
    this.callList = callList;
  }


  public List<Call> getCallList() {
    return callList;
  }


  @Override
  public String toString() {
    return super.toString() + "; CallList = " + (callList == null ? "null" : callList.size());
  }

}
