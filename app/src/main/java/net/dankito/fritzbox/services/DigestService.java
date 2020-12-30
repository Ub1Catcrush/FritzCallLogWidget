package net.dankito.fritzbox.services;

import net.dankito.fritzbox.model.HashAlgorithm;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ganymed on 26/11/16.
 */

public class DigestService implements IDigestService {

  public String calculateDigest(String stringToHash, HashAlgorithm algorithm) throws NoSuchAlgorithmException {
    MessageDigest digest;
    digest = MessageDigest.getInstance(algorithm.getAlgorithmName());
    digest.update(stringToHash.getBytes());

    final byte byteData[] = digest.digest();
    final BigInteger bigInt = new BigInteger(1, byteData);

    return bigInt.toString(16);
  }

}
