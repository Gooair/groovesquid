package groovesquid.service;

import groovesquid.model.Track;

public interface DownloadListener {
    void statusChanged(Track track);

    void downloadedBytesChanged(Track track);
}
