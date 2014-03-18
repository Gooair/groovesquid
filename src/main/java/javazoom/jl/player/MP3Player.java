package javazoom.jl.player;

import java.io.InputStream;
import javazoom.jl.decoder.*;

/**
 * A fork of the Javazoom JLayer MP3 Player.
 *
 * @see <a href="http://www.javazoom.net/index.shtml">Javazoom JLayer (Java MP3 Player)</a>
 */
public class MP3Player {
    /**
     * The MPEG audio bitstream.
     */
    protected Bitstream bitstream;
    /**
     * The MPEG audio decoder.
     */
    protected Decoder decoder;
    /**
     * The AudioDevice the audio samples are written to.
     */
    protected AudioDevice audio;
    /**
     * Has the player been closed forcefully?
     */
    protected boolean closeForced = false;
    /**
     * Has the player played back all frames from the stream?
     */
    private boolean complete = false;
    /**
     * The current frame.
     */
    protected int currentFrame = 0;
    /**
     * The current position in milliseconds.
     */
    protected float currentPosition = 0;
    /**
     * Listener for the playback process.
     */
    protected PlaybackListener listener;

    /**
     * Creates a new <code>Player</code> instance.
     *
     * @param stream input
     * @throws JavaLayerException on decode errors
     */
    public MP3Player(InputStream stream) throws JavaLayerException {
        this(stream, null);
    }

    /**
     * Creates a new <code>Player</code> instance.
     *
     * @param stream input
     * @param device audio device
     * @throws JavaLayerException on decode errors
     */
    public MP3Player(InputStream stream, AudioDevice device) throws JavaLayerException {
        bitstream = new Bitstream(stream);
        if (device != null)
            audio = device;
        else
            audio = new JavaSoundAudioDevice();
        audio.open(decoder = new Decoder());
    }

    /**
     * set a <code>PlaybackListener</code>. Set to {@code null} to remove it.
     *
     * @param listener PlaybackListener
     */
    void setPlayBackListener(PlaybackListener listener) {
        this.listener = listener;
    }

    /**
     * Retrieves the position in milliseconds of the current audio
     * sample being played. This method delegates to the <code>
     * AudioDevice</code> that is used by this player to sound
     * the decoded audio samples. Returns 0 if no sample is being
     * played.
     *
     * @return position, in milliseconds
     */
    public int getCurrentAudioDevicePosition() {
        AudioDevice out = audio;
        if (out != null) {
            return out.getPosition();
        } else {
            return 0;
        }
    }

    /**
     * Returns the position in milliseconds of the current audio
     * sample being played. This method computes the elapsed time
     * from the number of frames that already have been played.
     *
     * @return position, in milliseconds
     */
    public float getCurrentPosition() {
        return currentPosition;
    }
    
    public void setCurrentPosition(float currentPosition) {
        this.currentPosition = currentPosition;
    }

    /**
     * @return the number of the last played MPEG audio frame.
     */
    public int getCurrentFrame() {
        return currentFrame;
    }

    /**
     * Returns the completed status of this player.
     *
     * @return true if all available MPEG audio frames have been decoded, or false otherwise.
     */
    public synchronized boolean isComplete() {
        return complete;
    }

    public void play() throws JavaLayerException {
        currentFrame = 0;
        play(Integer.MAX_VALUE);
    }

    /**
     * Plays a range of MPEG audio frames
     *
     * @param start The first frame to play
     * @param end   The last frame to play
     * @return true if there are more frames to decode, false otherwise.
     * @throws JavaLayerException on decode errors
     */
    public boolean play(final int start, final int end) throws JavaLayerException {
        boolean ret = true;
        int offset = start;
        while (offset-- > 0 && ret)
            ret = skipFrame();
        currentFrame = start;
        return play(end - start);
    }

    /**
     * Stops this player. Any audio currently playing is stopped immediately.
     * Input and (audio) output streams get closed.
     */
    public void stop() {
        AudioDevice out = audio;
        if (out != null) {
            closeForced = true;
            audio = null;
            out.close();
            try {
                bitstream.close();
            }
            catch (BitstreamException ex) {
                // ignore
            }
        }
    }

    /**
     * Plays a number of MPEG audio frames.
     *
     * @param frames The number of frames to play.
     * @return true if there are more frames to decode, false otherwise.
     * @throws JavaLayerException on decode errors
     */
    protected boolean play(int frames) throws JavaLayerException {
        try {
            boolean ret = true;

            // report to listeners
            firePlaybackStartedEvent(audio);

            while (frames-- > 0 && ret) {
                ret = decodeFrame();
                firePositionChangedEvent(audio);
            }

            // last frame, ensure all data flushed to the audio device.
            AudioDevice out = audio;
            if (out != null) {
                out.flush();
                synchronized (this) {
                    complete = !closeForced && currentFrame > 0;
                    stop();
                }

                // report to listeners
                firePlaybackFinishedEvent(out);
            }

            return ret;
        }
        catch (Exception ex) {
            fireExceptionEvent(ex);
            return false;
        }
    }

    /**
     * Decodes a single frame.
     *
     * @return true if there are more frames to decode, false otherwise.
     * @throws JavaLayerException on decode errors
     */
    protected boolean decodeFrame() throws JavaLayerException {
        try {
            AudioDevice out = audio;
            if (out == null)
                return false;

            if (Thread.currentThread().isInterrupted())
                return false;

            Header h = bitstream.readFrame();
            if (h == null)
                return false;

            // sample buffer set when decoder constructed
            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);

            synchronized (this) {
                out = audio;
                if (out != null) {
                    out.write(output.getBuffer(), 0, output.getBufferLength());
                }
            }

            currentFrame++;
            currentPosition = currentFrame * h.ms_per_frame();
            bitstream.closeFrame();
        }
        catch (RuntimeException ex) {
            throw new JavaLayerException("Exception decoding audio frame", ex);
        }
        return true;
    }

    /**
     * skips over a single frame
     *
     * @return false if there are no more frames to decode, true otherwise.
     * @throws JavaLayerException on decode errors
     */
    protected boolean skipFrame() throws JavaLayerException {
        Header h = bitstream.readFrame();
        if (h == null) return false;
        bitstream.closeFrame();
        return true;
    }

    protected void firePlaybackStartedEvent(AudioDevice dev) {
        if (listener != null)
            listener.playbackStarted(this, dev != null ? dev.getPosition() : -1);
    }

    protected void firePlaybackFinishedEvent(AudioDevice dev) {
        if (listener != null)
            listener.playbackFinished(this, dev != null ? dev.getPosition() : -1);
    }

    protected void firePositionChangedEvent(AudioDevice dev) {
        if (listener != null && dev != null)
            listener.positionChanged(this, dev.getPosition());
    }

    protected void fireExceptionEvent(Exception ex) {
        if (listener != null)
            listener.exception(this, ex);
    }
    
    public void setGain(float gain) {
        if (audio instanceof JavaSoundAudioDevice) {
            JavaSoundAudioDevice jsAudio = (JavaSoundAudioDevice) audio;
            jsAudio.setLineGain(gain);
        }
    }
}