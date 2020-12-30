package net.dankito.fritzbox.services;


public interface INetworkService {

  String getCurrentSsid();

  boolean isInHomeNetwork(String homeNetworkSsid);

}
