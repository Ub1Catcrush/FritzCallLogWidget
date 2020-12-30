package net.dankito.fritzbox.services;

/**
 * Created by ganymed on 26/11/16.
 */

public interface IFileStorageService {

  void writeObjectToFile(Object object, String filename) throws Exception;

  void writeToFile(String fileContent, String filename) throws Exception;


  <T> T readObjectFromFile(String filename, Class<T> objectClass) throws Exception;

  String readFromFile(String filename) throws Exception;

}
