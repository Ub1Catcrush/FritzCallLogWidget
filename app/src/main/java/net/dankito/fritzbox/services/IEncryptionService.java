package net.dankito.fritzbox.services;

import net.dankito.fritzbox.services.exceptions.EncryptionServiceException;

/**
 * Created by ganymed on 27/11/16.
 */
public interface IEncryptionService {

  String encrypt(String value) throws EncryptionServiceException;

  String decrypt(String securedEncodedValue) throws EncryptionServiceException;

}
