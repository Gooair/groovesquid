package groovesquid.service;

import groovesquid.model.Song;
import groovesquid.model.Track;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javazoom.jl.player.MP3Player;
import javazoom.jl.player.PlayThread;
import javazoom.jl.player.PlaybackListener;

public class PlayService {

    public static enum AddMode {
        NOW, NEXT, LAST, REPLACE
    }

    private static final Log log = LogFactory.getLog(PlayService.class);

    /**
     * how many bytes to pre-download before actually begin playing
     */
    private static final long PLAY_BUFFER_SIZE = 100000L;

    private final DownloadService downloadService;
    private final List<Song> playlist = new ArrayList<Song>();
    private int currentSongIndex = -1;
    private Track currentTrack;
    private int pausedFrame = -1;
    private int pausedAudioPosition = 0;
    private PlayServiceListener listener;
    private PlayThread playThread;
    private boolean radio;
    private float gain = 0.0f;

    public PlayService(DownloadService downloadService) {
        this.downloadService = downloadService;
        this.playThread = new PlayThread();
    }

    public void setListener(PlayServiceListener listener) {
        this.listener = listener;
    }

    public List<Song> getPlaylist() {
        return playlist;
    }

    public synchronized void add(List<Song> songs, AddMode addMode) {
        if (songs.isEmpty()) {
            return;
        }
        if (addMode == AddMode.REPLACE) {
            clearPlaylist();
        }
        int insertIdx = (addMode == AddMode.LAST ? playlist.size() : currentSongIndex + 1);
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            log.info("adding: " + song);
            if (insertIdx <= playlist.size()) {
                playlist.add(insertIdx, song);
            } else {
                insertIdx = playlist.indexOf(song);
            }
            if (i == 0 && (addMode == AddMode.NOW || addMode == AddMode.REPLACE)) {
                currentSongIndex = insertIdx;
            }
            insertIdx++;
        }
        if (addMode == AddMode.NOW || addMode == AddMode.REPLACE) {
            Song song = songs.get(0);
            stopPlaying();
            startPlaying(song, 0, 0);
        }
    }

    public synchronized void play() {
        if (currentSongIndex < 0 && !playlist.isEmpty())
            currentSongIndex = 0;
        Song currentSong = getCurrentSong();
        if (currentSong != null) {
            log.info("starting: " + currentSong);
            startPlaying(currentSong, 0, 0);
        } else {
            log.info("no current song");
        }
    }

    public synchronized void stop() {
        Song currentSong = getCurrentSong();
        if (currentSong != null) log.info("stopping: " + currentSong);
        stopPlaying();
    }

    public synchronized void skipForward() {
        Song currentSong = getCurrentSong();
        if (currentSong != null) log.info("stopping because of skip: " + currentSong);
        stopPlaying();
        skipToNext();
    }

    public synchronized void skipBackward() {
        Song currentSong = getCurrentSong();
        if (currentSong != null) log.info("stopping because of skip: " + currentSong);
        stopPlaying();
        skipToPrevious();
    }

    public synchronized void pause() {
        Song currentSong = getCurrentSong();
        if (currentSong != null && !playThread.isStopForced()) {
            log.info("pausing: " + currentSong);
            pausedAudioPosition = playThread.getCurrentPosition();
            pausedFrame = playThread.forceStop();
            try {
                playThread.join();
            } catch (InterruptedException e) {
                // ignored
            }
            log.debug("paused at frame: " + pausedFrame + ", audioPosition: " + pausedAudioPosition);
            if (listener != null)
                listener.playbackPaused(currentTrack, pausedAudioPosition);
        } else {
            log.info(currentSong);
        }
    }

    public synchronized void resume() {
        Song currentSong = getCurrentSong();
        if (currentSong != null && pausedFrame != -1) {
            log.info("resuming from frame: " + pausedFrame + ", audioPosition: " + pausedAudioPosition + ": " + currentSong);
            startPlaying(currentSong, pausedFrame, pausedAudioPosition);
            pausedFrame = -1;
        }
    }

    public synchronized boolean isPaused() {
        return pausedFrame != -1;
    }

    public synchronized boolean isPlaying() {
        return playThread.isAlive();
    }

    public synchronized void clearPlaylist() {
        stopPlaying();
        currentSongIndex = -1;
        playlist.clear();
        radio = false;
    }

    /**
     * Retrieves the position in milliseconds of the current audio sample being played.
     *
     * @return current audio position, in milliseconds
     */
    public int getCurrentPosition() {
        return playThread.getCurrentPosition();
    }
    
    public void setCurrentPosition(int currentPosition) {
        playThread.setCurrentPosition(currentPosition);
    }

    /**
     * @return the index of the song being played currently
     */
    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    public synchronized void playSong(int songIndex) {
        if (songIndex == currentSongIndex)
            return;
        if (songIndex < 0 || songIndex >= playlist.size()) {
            log.error("playSong: index out of bounds: " + songIndex + "; must be in range [0," + playlist.size() + ")");
            return;
        }
        Song currentSong = getCurrentSong();
        if (currentSong != null)
            log.info("stopping because of song index change to " + songIndex + ": " + currentSong);
        stopPlaying();
        currentSongIndex = songIndex;
        currentSong = getCurrentSong();
        log.info("skipping to song index " + songIndex + ": " + currentSong);
        startPlaying(currentSong, 0, 0);
    }

    /**
     * @return the track being played currently
     */
    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void setRadio(boolean radio) {
        if (!radio) { // switch off radio
            this.radio = radio;
            return;
        }
        // to enable radio playlist must not be empty
        if (playlist.isEmpty())
            return;
        this.radio = radio;
        //addNextRadioSong();
    }

    private Song getCurrentSong() {
        return currentSongIndex >= 0 ? playlist.get(currentSongIndex) : null;
    }

    private void startPlaying(final Song song, final int framePosition, final int audioPosition) {
        if (currentTrack != null && currentTrack.getSong() != song)
            stopPlaying();
        log.info("starting from " + framePosition + ": " + song);
        if (currentTrack == null || currentTrack.getSong() != song) {
            currentTrack = downloadService.downloadToMemory(song, new ChainedPlayServiceListener(listener) {
                @Override public void downloadedBytesChanged(Track track) {
                    if (!isPlaying() && !isPaused() && track == currentTrack && track.getDownloadedBytes() > PLAY_BUFFER_SIZE) {
                        startPlayingCurrentTrack(framePosition, audioPosition);
                    }
                    super.downloadedBytesChanged(track);
                }
            });
        } else {
            startPlayingCurrentTrack(framePosition, audioPosition);
        }
    }

    private void startPlayingCurrentTrack(int framePosition, int audioPosition) {
        try {
            InputStream inputStream = currentTrack.getStore().getInputStream();
            playThread = new PlayThread(inputStream, framePosition);
            playThread.setPlaybackListener(new PlayThreadListener(currentTrack, audioPosition));
            playThread.start();
        } catch (IOException ex) {
            handlePlayException(currentTrack, ex);
        }
    }

    private void stopPlaying() {
        int stopFrame = playThread.forceStop();
        playThread.interrupt();
        try {
            playThread.join();
        } catch (InterruptedException ignore) {
            // intentionally ignored
        }
        if (stopFrame == 0 && currentTrack != null) // player didn't start yet
            if (listener != null)
                listener.playbackFinished(currentTrack, playThread.getCurrentPosition());
        if (currentTrack != null)
            downloadService.cancelDownload(currentTrack, true);
        currentTrack = null;
        pausedFrame = -1;
        pausedAudioPosition = 0;
    }

    private void skipToNext() {
        if (currentSongIndex < playlist.size() - 1) {
            currentSongIndex++;
            Song currentSong = getCurrentSong();
            log.info("skipping forward to: " + currentSong);
            startPlaying(currentSong, 0, 0);
        } else {
            log.info("skipped beyond end of playlist");
            if (radio) {
                //addNextRadioSong();
                skipToNext();
            }
        }
    }

    private void skipToPrevious() {
        if (currentSongIndex > 0) {
            currentSongIndex--;
            Song currentSong = getCurrentSong();
            log.info("skipping back to: " + currentSong);
            startPlaying(currentSong, 0, 0);
        } else {
            log.info("skipped beyond start of playlist");
        }
    }

    /*private void addNextRadioSong() {
        try {
            log.info("fetching next radio song...");
            Song nextRadioSong = Services.getSearchService().autoplayGetSong(playlist);
            add(new ArrayList<Song>(nextRadioSong), AddMode.LAST);
        } catch (Exception ex) {
            log.error("error fetching next song for radio", ex);
        }
    }*/
    
    // sliderValue is from -2000 to 0
    public void setVolume(int sliderValue) {
        if(playThread != null) {
            int minSliderValue = -2000;
            if(sliderValue <= minSliderValue) {
                // mute
                sliderValue = -10000;
            }
            gain = (float) (Math.sqrt(sliderValue * (-1)) * (-1));
            setGain();
        }
    }
    
    private void setGain() {
        playThread.setGain(gain);
    }

    private void handlePlayException(Track track, Exception ex) {
        log.error("error playing track " + track, ex);
        if (listener != null)
            listener.exception(track, ex);
        stop();
    }


    private class PlayThreadListener implements PlaybackListener {
        private final Track track;
        private final int audioPositionOffset;

        private PlayThreadListener(Track track, int audioPositionOffset) {
            this.track = track;
            this.audioPositionOffset = audioPositionOffset;
        }

        @Override public void playbackStarted(MP3Player player, int audioPosition) {
            log.info("playback started: " + track);
            if (listener != null)
                listener.playbackStarted(track);
        }

        @Override public void playbackFinished(MP3Player player, int audioPosition) {
            log.info("playback finished: " + track);
            if (listener != null)
                listener.playbackFinished(track, audioPositionOffset + audioPosition);
            if (player.isComplete()) {
                skipToNext();
            }
        }

        @Override public void positionChanged(MP3Player player, int audioPosition) {
            if (listener != null)
                listener.positionChanged(track, audioPositionOffset + audioPosition);
            setGain();
        }

        @Override public void exception(MP3Player player, Exception ex) {
            handlePlayException(track, ex);
        }
    }

    private abstract class ChainedPlayServiceListener implements PlayServiceListener {
        private final PlayServiceListener origListener;

        private ChainedPlayServiceListener(PlayServiceListener origListener) {
            this.origListener = origListener;
        }

        @Override public void playbackStarted(Track track) {
            if (origListener != null) origListener.playbackStarted(track);
        }

        @Override public void playbackPaused(Track track, int audioPosition) {
            if (origListener != null) origListener.playbackPaused(track, audioPosition);
        }

        @Override public void playbackFinished(Track track, int audioPosition) {
            if (origListener != null) origListener.playbackFinished(track, audioPosition);
        }

        @Override public void positionChanged(Track track, int audioPosition) {
            if (origListener != null) origListener.positionChanged(track, audioPosition);
        }

        @Override public void exception(Track track, Exception ex) {
            if (origListener != null) origListener.exception(track, ex);
        }

        @Override public void statusChanged(Track track) {
            if (origListener != null) origListener.statusChanged(track);
        }

        @Override public void downloadedBytesChanged(Track track) {
            if (origListener != null) origListener.downloadedBytesChanged(track);
        }
    }
}
