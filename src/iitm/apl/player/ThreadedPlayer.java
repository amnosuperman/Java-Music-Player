package iitm.apl.player;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.event.EventListenerList;

/**
 * Threaded player implements a music player that plays songs in its own thread.
 * 
 * Once the player has been started on a thread, set a song to be played using
 * the "setSong" function. You can also pause/resume using the setState
 * function. To stop playing the track, and perform clean-up actions, set the
 * state to Stop.
 */
public class ThreadedPlayer implements Runnable {

	public enum State {
		PLAY, // Playing a song; the thread is active
		PAUSE, // Paused; waiting till the state is changed from pause
		STOP
		// Stopped, with nothing in the buffer
	};

	private State state = null;
	private boolean running;
	public boolean playing = false;
	public boolean song_changed = false;

	private final Lock songLock = new ReentrantLock();
	private final Condition hasSong = songLock.newCondition();
	private final Lock stateLock = new ReentrantLock();
	private final Condition notPaused = stateLock.newCondition();
	private Thread playthread;

	// Listeners for when the player stops playing a song.
	private EventListenerList listeners;

	public Song song;

	boolean debug = false;

	public ThreadedPlayer() {
		this.state = State.STOP;
		listeners = new EventListenerList();
	}

	@SuppressWarnings("unused")
	private void dispose() {
		running = false;
		setState(State.STOP);
	}

	public State getState() {
		return this.state;
	}

	/**
	 * Set the state of the player. Takes control of the state lock
	 */
	public void setState(State st) {
		stateLock.lock();
		try {
			state = st;
			if (st != State.PAUSE)
				notPaused.signal();
		} finally {
			stateLock.unlock();
		}
	}

	/**
	 * Play a song; extract the appropriate audio input stream, and then send it
	 * to rawPlay to actually write the bytes to the audio device.
	 * 
	 * @param song
	 */
	public void stopSong() {

		if (playing) {
			playing = false;
			playthread.stop();
			setState(State.STOP);
		}
		
	}
	public void playSong() {
		song_changed = false;
		
		if (playing) {
			playing = false;
			playthread.stop();
		}

		playthread = new Thread(new Runnable() {
			public void run() {
				playing = true;
				System.out.println("playing");
				if (song == null)
					return;
				String filePath = song.getFile().getAbsolutePath();
				System.err.println("Playing : " + filePath);
				try {
					File file = new File(filePath);
					System.err.println("playing : " + file.getName());

					AudioInputStream in = AudioSystem.getAudioInputStream(file);
					AudioInputStream din = null;
					AudioFormat baseFormat = in.getFormat();
					AudioFormat decodedFormat = new AudioFormat(
							AudioFormat.Encoding.PCM_SIGNED, baseFormat
									.getSampleRate(), 16, baseFormat
									.getChannels(),
							baseFormat.getChannels() * 2, baseFormat
									.getSampleRate(), false);
					din = AudioSystem.getAudioInputStream(decodedFormat, in);

					// Play now.
					rawPlay(decodedFormat, din);
					in.close();

				} catch (Exception e) {
					System.out.println(e.toString());
					System.exit(1);
				}

			}
		}, "playthread");

		playthread.start();

	}

	/**
	 * Get a line to the audio mixer. Writing to the SourceDataLine writes audio
	 * to the mixer, which in turn plays it!
	 */
	private SourceDataLine getLine(AudioFormat audioFormat)
			throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}

	/**
	 * Actually write the audio bytes to the sound device (through
	 * SourceDataLine).
	 */
	private void rawPlay(AudioFormat targetFormat, AudioInputStream din)
			throws IOException, LineUnavailableException, InterruptedException {

		SourceDataLine line = getLine(targetFormat);
		if (line != null) {
			// Start
			line.start();
			int nBytesRead = 0;

			byte[] data = new byte[4096];
			boolean trackComplete = false;

			// Keep playing till you reach the end of the file or the player has
			// been stopped.
			System.out.println(state);
			while (!trackComplete && state != State.STOP && playing) {
				if (state == State.PAUSE) {
					if (line.isRunning())
						line.stop();
				} 
				else {
					// If you haven't started writing to the device before, do
					// so
					// now.
					if (!line.isRunning()) {
						line.start();
					}
					trackComplete = ((nBytesRead = din.read(data, 0,
							data.length)) == -1);

					// Actually write bytes to the audio device
					line.write(data, 0, nBytesRead);
				}
			}
			if (trackComplete)
				fireAction(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
						"track-complete"));

			// drain the pipeline to play the leftover frames, and free up
			// memory, etc.
			line.drain();
			line.stop();
			line.close();
			din.close();

			playing = false;

		}
	}

	// Set the song
	public void setSong(Song song) {
		songLock.lock();
		stateLock.lock();
		try {
			this.song = song;
			if (song != null)
				hasSong.signal();
		} finally {
			songLock.unlock();
		}
	}

	@Override
	public void run() {
		// Keep running in a loop; When a song is set, play it.
		System.out.println("at run");

		running = true;
		while (running) {
			songLock.lock();
			try {
				while (song == null) {
					hasSong.await();
				}
			} catch (InterruptedException e) {
				return;
			} finally {
				songLock.unlock();
			}
			// Play the song!
			setState(State.PLAY);
			System.out.println("check");
			playSong();
			setSong(null);
			setState(State.STOP);
		}
	}

	// Action handling stuff

	public void addActionListener(ActionListener listener) {
		listeners.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener) {
		listeners.remove(ActionListener.class, listener);
	}

	protected void fireAction(ActionEvent e) {
		for (ActionListener listener : listeners
				.getListeners(ActionListener.class))
			listener.actionPerformed(e);
	}
}
