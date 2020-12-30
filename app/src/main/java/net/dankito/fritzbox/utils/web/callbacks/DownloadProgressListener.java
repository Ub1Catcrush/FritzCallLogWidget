package net.dankito.fritzbox.utils.web.callbacks;

/**
 * Created by ganymed on 03/11/16.
 */

public interface DownloadProgressListener {

  void progress(float progress, byte[] downloadedChunk);

}
