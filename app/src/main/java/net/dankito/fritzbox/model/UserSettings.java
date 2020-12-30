package net.dankito.fritzbox.model;

import net.dankito.fritzbox.utils.StringUtils;

/**
 * Created by ganymed on 26/11/16.
 */

public class UserSettings {

  protected String fritzBoxType;

  protected String fritzBoxAddress;

  protected String fritzBoxPort;

  protected String fritzBoxUsername;

  protected String fritzBoxPassword;

  protected boolean checkOnlyInHomeNetwork;

  protected String homeNetworkSsid;

  protected boolean isPeriodicalMissedCallsCheckEnabled;

  protected long periodicalMissedCallsCheckInterval;


  public UserSettings() { // for Jackson

  }

  public UserSettings(String fritzBoxType, String fritzBoxAddress, String fritzBoxPort, String fritzBoxUsername, String fritzBoxPassword) {
    this.fritzBoxType = fritzBoxType;
    this.fritzBoxAddress = fritzBoxAddress;
    this.fritzBoxPort = fritzBoxPort;
    this.fritzBoxUsername = fritzBoxUsername;
    this.fritzBoxPassword = fritzBoxPassword;

    this.checkOnlyInHomeNetwork = true;
  }


  public boolean isFritzBoxAddressSet() {
    return StringUtils.isNotNullOrEmpty(getFritzBoxAddress());
  }

  public String getFritzBoxType() {
    return fritzBoxType;
  }

  public void setFritzBoxType(String fritzBoxType) {
    this.fritzBoxType = fritzBoxType;
  }

  public String getFritzBoxAddress() {
    return fritzBoxAddress;
  }

  public void setFritzBoxAddress(String fritzBoxAddress) {
    this.fritzBoxAddress = fritzBoxAddress;
  }

  public String getFritzBoxPort() {
    return fritzBoxPort;
  }

  public void setFritzBoxPort(String fritzBoxPort) {
    this.fritzBoxPort = fritzBoxPort;
  }

  public boolean isFritzBoxUsernameSet() {
    return StringUtils.isNotNullOrEmpty(getFritzBoxUsername());
  }

  public boolean isFritzBoxPasswordSet() {
    return StringUtils.isNotNullOrEmpty(getFritzBoxPassword());
  }

  public String getFritzBoxUsername() {
    return fritzBoxUsername;
  }

  public void setFritzBoxUsername(String fritzBoxUsername) {
    this.fritzBoxUsername = fritzBoxUsername;
  }

  public String getFritzBoxPassword() {
    return fritzBoxPassword;
  }

  public void setFritzBoxPassword(String fritzBoxPassword) {
    this.fritzBoxPassword = fritzBoxPassword;
  }

  public boolean isCheckOnlyInHomeNetwork() {
    return checkOnlyInHomeNetwork;
  }

  public void setCheckOnlyInHomeNetwork(boolean checkOnlyInHomeNetwork) {
    this.checkOnlyInHomeNetwork = checkOnlyInHomeNetwork;
  }

  public String getHomeNetworkSsid() {
    return homeNetworkSsid;
  }

  public void setHomeNetworkSsid(String homeNetworkSsid) {
    this.homeNetworkSsid = homeNetworkSsid;
  }

  public boolean isPeriodicalMissedCallsCheckEnabled() {
    return isPeriodicalMissedCallsCheckEnabled;
  }

  public void setPeriodicalMissedCallsCheckEnabled(boolean periodicalMissedCallsCheckEnabled) {
    isPeriodicalMissedCallsCheckEnabled = periodicalMissedCallsCheckEnabled;
  }

  public long getPeriodicalMissedCallsCheckInterval() {
    return periodicalMissedCallsCheckInterval;
  }

  public void setPeriodicalMissedCallsCheckInterval(long periodicalMissedCallsCheckInterval) {
    this.periodicalMissedCallsCheckInterval = periodicalMissedCallsCheckInterval;
  }


  @Override
  public String toString() {
    return getFritzBoxAddress();
  }

}
