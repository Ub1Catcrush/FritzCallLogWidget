package net.dankito.fritzbox.services;

import net.dankito.fritzbox.model.HashAlgorithm;

import java.security.NoSuchAlgorithmException;

/**
 * Created by ganymed on 26/11/16.
 */

public interface IDigestService {

  String calculateDigest(String stringToHash, HashAlgorithm algorithm) throws NoSuchAlgorithmException;

}
