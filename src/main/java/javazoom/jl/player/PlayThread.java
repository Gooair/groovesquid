package javazoom.jl.player;

import java.io.IOException;
import java.io.InputStream;
import javazoom.jl.decoder.JavaLayerException;

/**
 * Usage:
 * <pre>
 * File mp3File = new File(...);
 * InputStream mp3Stream = new FileInputStream(mp3File);
 * PlayThread playThread = new PlayThread(mp3Stream);
 * playThread.start();
 * ...
 * int lastFrame = playThread.forceStop(); // stops player, closes file stream
 * ...
 * mp3Stream = new FileInputStream(mp3File);
 * playThread = new PlayThread(mp3Stream, lastFrame);
 * playThread.start(); // reopenes stream and resumes play at last position
 * ...
 * playThread.forceStop();
 * </pre>
 */
public class PlayThread extends Thread {

    private final InputStream inputStream;
    private final int firstFrame;
    private final AudioDevice audioDevice;
    private volatile MP3Player player;
    private volatile boolean stopForced;
    private volatile PlaybackListener playbackListener;

    public PlayThread() {
        this(null);
    }

    public PlayThread(InputStream inputStream) {
        this(inputStream, 0);
    }

    public PlayThread(InputStream inputStream, int firstFrame) {
        this(inputStream, firstFrame, new JavaSoundAudioDevice());
    }

    public PlayThread(InputStream inputStream, int firstFrame, AudioDevice audioDevice) {
        super("player");
        setDaemon(true);
        setPriority(NORM_PRIORITY + 2);
        this.inputStream = inputStream;
        this.firstFrame = firstFrame;
        this.audioDevice = audioDevice;
    }

    public PlaybackListener getPlaybackListener() {
        return playbackListener;
    }

    public void setPlaybackListener(PlaybackListener playbackListener) {
        this.playbackListener = playbackListener;
    }

    /**
     * Retrieves the position in milliseconds of the current audio sample being played.
     *
     * @return position, in milliseconds
     */
    public int getCurrentPosition() {
        return player != null ? (int) player.getCurrentPosition() : 0;
    }
    
    public void setCurrentPosition(float currentPosition) {
        player.setCurrentPosition(currentPosition);
    }

    public boolean isStopForced() {
        return stopForced;
    }

    @Override
    public void run() {
        if (inputStream == null)
            return;
        try {
            try {
                player = new MP3Player(inputStream, audioDevice);
                LocalPlaybackListener localPlaybackListener = new LocalPlaybackListener();
                if (playbackListener != null)
                    localPlaybackListener.otherListener = playbackListener;
                player.setPlayBackListener(localPlaybackListener);
                player.play(firstFrame, Integer.MAX_VALUE);
            } finally {
                inputStream.close();
            }
        } catch (JavaLayerException ex) {
            if (playbackListener != null)
                playbackListener.exception(player, new RuntimeException(ex.getMessage(), ex.getException()));
            else
                throw new RuntimeException(ex);
        } catch (Exception ex) {
            if (playbackListener != null)
                playbackListener.exception(player, ex);
            else
                throw new RuntimeException(ex);
        } finally {
            System.out.println("thread ends");
        }
    }

    public int forceStop() {
        stopForced = true;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ignore) {
                throw new RuntimeException(ignore);
            }
        }
        int currentFrame = 0;
        if (player != null) {
            synchronized (this) {
                if (player != null) {
                    currentFrame = player.getCurrentFrame();
                    player.stop();
                    player = null;
                }
            }
        }
        return currentFrame;
    }

    public void setGain(float gain) {
        if(player != null) {
            player.setGain(gain);
        }
    }

    private class LocalPlaybackListener implements PlaybackListener {
        private PlaybackListener otherListener;

        @Override public void playbackStarted(MP3Player player, int audioPosition) {
            if (otherListener != null)
                otherListener.playbackStarted(player, audioPosition);
        }

        @Override public void playbackFinished(MP3Player player, int audioPosition) {
            synchronized (PlayThread.this) {
                PlayThread.this.player = null;
            }
            if (otherListener != null)
                otherListener.playbackFinished(player, audioPosition);
        }

        @Override public void positionChanged(MP3Player player, int audioPosition) {
            if (otherListener != null)
                otherListener.positionChanged(player, audioPosition);
        }

        @Override public void exception(MP3Player player, Exception ex) {
            if (otherListener != null)
                otherListener.exception(player, ex);
        }
    }
}
