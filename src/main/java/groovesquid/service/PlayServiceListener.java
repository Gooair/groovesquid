package groovesquid.service;

import groovesquid.model.Track;

public interface PlayServiceListener extends DownloadListener {
    void playbackStarted(Track track);

    void playbackPaused(Track track, int audioPosition);

    void playbackFinished(Track track, int audioPosition);

    void positionChanged(Track track, int audioPosition);

    void exception(Track track, Exception ex);
}
