package net.dankito.fritzbox.services;

import net.dankito.fritzbox.model.Call;
import net.dankito.fritzbox.model.HashAlgorithm;
import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.utils.StringUtils;
import net.dankito.fritzbox.utils.web.IWebClient;
import net.dankito.fritzbox.utils.web.RequestParameters;
import net.dankito.fritzbox.utils.web.callbacks.GetCallListCallback;
import net.dankito.fritzbox.utils.web.callbacks.GetStringInfoCallback;
import net.dankito.fritzbox.utils.web.callbacks.LoginCallback;
import net.dankito.fritzbox.utils.web.callbacks.RequestCallback;
import net.dankito.fritzbox.utils.web.responses.GetCallListResponse;
import net.dankito.fritzbox.utils.web.responses.GetStringInfoResponse;
import net.dankito.fritzbox.utils.web.responses.LoginResponse;
import net.dankito.fritzbox.utils.web.responses.WebClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * In large parts a copy of https://github.com/ISchwarz23/FritzBox-API.
 */
public class FritzBoxClient {

  protected static final int COUNT_CONNECTION_RETRIES = 2;

  protected static final int RETRY_TO_GET_CALL_LIST_AFTER_SESSION_TIMEOUT_MILLIS = 30 * 1000;


  private static final Logger log = LoggerFactory.getLogger(FritzBoxClient.class);


  protected UserSettings userSettings;

  protected String sessionId;

  protected IWebClient webClient;

  protected IDigestService digestService;

  protected ICsvParser csvParser;


  public FritzBoxClient(UserSettings userSettings, IWebClient webClient, IDigestService digestService, ICsvParser csvParser) {
    this.userSettings = userSettings;
    this.webClient = webClient;
    this.digestService = digestService;
    this.csvParser = csvParser;
  }


  public void loginAsync(final LoginCallback callback) {
    loginAsync(userSettings.getFritzBoxType(), userSettings.getFritzBoxAddress(), userSettings.getFritzBoxPort(), userSettings.getFritzBoxUsername(), userSettings.getFritzBoxPassword(), callback);
  }

  public void loginAsync(final String fritzBoxType, final String fritzBoxAddress, final String fritzBoxPort, final String fritzBoxUsername, final String fritzBoxPassword, final LoginCallback callback) {
    if(StringUtils.isNullOrEmpty(fritzBoxType) || StringUtils.isNullOrEmpty(fritzBoxAddress) || StringUtils.isNullOrEmpty(fritzBoxUsername) || StringUtils.isNullOrEmpty(fritzBoxPassword)) {
      callback.completed(new LoginResponse("Entweder FritzBox Adresse oder Password is nicht gesetzt")); // TODO: translate
      return;
    }

    this.getChallenge(fritzBoxAddress, new GetStringInfoCallback() {
      @Override
      public void completed(GetStringInfoResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new LoginResponse(response.getError()));
        }
        else {
          String challenge = response.getStringInfo();
          sendLogin(fritzBoxType, fritzBoxAddress, fritzBoxPort, fritzBoxUsername, fritzBoxPassword, challenge, new GetStringInfoCallback() {
            @Override
            public void completed(GetStringInfoResponse response) {
              loginCompleted(response, callback);
            }
          });
        }
      }
    });
  }

  protected void loginCompleted(GetStringInfoResponse loginResponse, LoginCallback callback) {
    if(loginResponse.isSuccessful() == false) {
      callback.completed(new LoginResponse(loginResponse.getError()));
    }
    else {
      String sid = loginResponse.getStringInfo();

      if(sid.equals("0000000000000000")) {
        callback.completed(new LoginResponse("Not able to login to FritzBox. Wrong password!"));
      }
      else {
        this.sessionId = sid;
        callback.completed(new LoginResponse(true));
      }
    }
  }

  protected void getChallenge(String fritzBoxAddress, final GetStringInfoCallback callback) {
    String url = "http://" + fritzBoxAddress + "/login_sid.lua";
    webClient.getAsync(createDefaultRequestParameters(url), new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetStringInfoResponse(response.getError()));
        }
        else {
          // get challenge challenge
          String responseBody = response.getBody();
          final String challenge = responseBody.substring(responseBody.indexOf("<Challenge>") + 11,
              responseBody.indexOf("<Challenge>") + 19);

          callback.completed(new GetStringInfoResponse(true, challenge));
        }
      }
    });
  }

  protected void sendLogin(String fritzBoxType, String fritzBoxAddress, String fritzBoxPort, String fritzBoxUsername, String fritzBoxPassword, String challenge, final GetStringInfoCallback callback) {
    try {
      final String loginChallengeResponse = calculateLoginChallengeResponse(challenge, fritzBoxPassword);
//      String url = "http://" + fritzBoxAddress + "/login_sid.lua?user&response=" + loginChallengeResponse;
      String url = fritzBoxType + "://" + fritzBoxAddress + ":"+fritzBoxPort+"/login_sid.lua?username="+fritzBoxUsername+"&response=" + loginChallengeResponse;

      webClient.getAsync(createDefaultRequestParameters(url), new RequestCallback() {
        @Override
        public void completed(WebClientResponse response) {
          if(response.isSuccessful() == false) {
            callback.completed(new GetStringInfoResponse(response.getError()));
          }
          else {
            parseSessionIdFromLoginResponse(response, callback);
            return;
          }
        }
      });
    } catch (final UnsupportedEncodingException e) {
      // will never appear
      log.error("UTF-16LE is not supported", e);
      callback.completed(new GetStringInfoResponse("UTF-16LE is not supported"));
    } catch (final NoSuchAlgorithmException e) {
      // will never appear
      log.error("MD5 is not supported", e);
    }
  }

  protected void parseSessionIdFromLoginResponse(WebClientResponse response, GetStringInfoCallback callback) {
    try {
      String responseBody = response.getBody();

      String sessionId = responseBody.substring(responseBody.indexOf("<SID>") + 5, responseBody.indexOf("<SID>") + 21);

      callback.completed(new GetStringInfoResponse(true, sessionId));
    } catch(Exception e) {
      log.error("Could not parse SessionId from Login Response", e);
      callback.completed(new GetStringInfoResponse("Could not parse SessionId from Login Response: " + e.getLocalizedMessage()));
    }
  }

  protected String calculateLoginChallengeResponse(String challenge, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException {
    final String stringToHash = challenge + "-" + password;
    String stringToHashUTF16 = new String(stringToHash.getBytes("UTF-16LE"), "UTF-8");

    final String md5 = digestService.calculateDigest(stringToHashUTF16, HashAlgorithm.MD5);
    return challenge + "-" + md5;
  }


  /**
   * Requests the call list from the FritzBox and returns it.
   *
   * @return A list of call objects.
   * @throws IOException
   *             when an exception during communication occurred.
   */
  public void getCallListAsync(final GetCallListCallback callback) {
    if(isLoggedIn() == false) {
      loginAsync(new LoginCallback() {
        @Override
        public void completed(LoginResponse response) {
          if(response.isSuccessful() == false) {
            callback.completed(new GetCallListResponse(response.getError()));
          }
          else {
            doGetCallListAsync(callback);
          }
        }
      });
    }
    else {
      doGetCallListAsync(callback);
    }
  }

  protected void doGetCallListAsync(final GetCallListCallback callback) {
    String url = "http://" + userSettings.getFritzBoxAddress() + "/fon_num/foncalls_list.lua?sid="+ this.sessionId + "&csv=";

    webClient.getAsync(createDefaultRequestParameters(url), new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetCallListResponse(response.getError()));
        }
        else if(csvParser.isCallListCsv(response.getBody())) {
          parseCallListCsv(response, callback);
        }
        else if(isSessionTimedOutResponse(response)) {
          handleSessionTimedOutForGetCallList(response, callback);
        }
      }
    });
  }

  protected void parseCallListCsv(WebClientResponse response, GetCallListCallback callback) {
    String csvFileContent = response.getBody();
    List<Call> callList = csvParser.parseCallList(csvFileContent);

    callback.completed(new GetCallListResponse(callList));
  }

  protected boolean isSessionTimedOutResponse(WebClientResponse response) {
    String responseString = response.getBody();

    return StringUtils.isNotNullOrEmpty(responseString) && responseString.contains("action=\"/login.lua\"");
  }

  protected void handleSessionTimedOutForGetCallList(WebClientResponse response, final GetCallListCallback callback) {
    invalidateSession();

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        getCallListAsync(callback);
      }
    }, RETRY_TO_GET_CALL_LIST_AFTER_SESSION_TIMEOUT_MILLIS); // try again in 30 seconds
  }

  protected void invalidateSession() {
    sessionId = null;
  }


  protected RequestParameters createDefaultRequestParameters(String url) {
    RequestParameters parameters = new RequestParameters(url);

    parameters.setCountConnectionRetries(COUNT_CONNECTION_RETRIES);

    return parameters;
  }


  public boolean isLoggedIn() {
    return StringUtils.isNotNullOrEmpty(sessionId);
  }

}
