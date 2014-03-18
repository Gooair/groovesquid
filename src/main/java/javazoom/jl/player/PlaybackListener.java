package javazoom.jl.player;

/**
 * Listener for MP3Player playback.
 */
public interface PlaybackListener {
    void playbackStarted(MP3Player player, int audioPosition);

    void playbackFinished(MP3Player player, int audioPosition);

    void positionChanged(MP3Player player, int audioPosition);

    void exception(MP3Player player, Exception ex);
}
