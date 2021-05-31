package dartproductions.mcleodmassacre.sound;

import dartproductions.mcleodmassacre.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for playing sound effects and managing sound resources.
 */
public class SoundManager {
	/**
	 * The created audio clips
	 */
	private static final @NotNull ConcurrentHashMap<String, ArrayList<Clip>> AUDIO_CLIPS = new ConcurrentHashMap<>();
	private static final Logger LOGGER = LogManager.getLogger(SoundManager.class);
	private static float VOLUME = 0.8f;
	
	public static void play(@NotNull String name, boolean looping) {
		ArrayList<Clip> clips = AUDIO_CLIPS.getOrDefault(name, new ArrayList<>());
		for(Clip clip : clips) {
			if(!clip.isRunning()) {
				clip.setFramePosition(0);
				if(looping) {
					clip.loop(Clip.LOOP_CONTINUOUSLY);
				} else {
					clip.loop(0);
				}
				clip.start();
				return;
			}
		}
		try {
			Clip clip = AudioSystem.getClip(null);
			clip.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(ResourceManager.getSound(name))));
			clip.start();
			clips.add(clip);
			setVolume(clip);
			if(looping) {
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			} else {
				clip.loop(0);
			}
			AUDIO_CLIPS.put(name, clips);
		} catch(UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			LOGGER.warn("Could not create audio clip", e);
		}
	}
	
	private static void setVolume(Clip clip) {
		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float range = gainControl.getMaximum() - gainControl.getMinimum();
		float gain = (range * VOLUME) + gainControl.getMinimum();
		gainControl.setValue(gain);
	}
	
	public static void changeVolume(float volume) {
		VOLUME = volume;
		for(ArrayList<Clip> list : AUDIO_CLIPS.values()) {
			for(Clip clip : list) {
				setVolume(clip);
			}
		}
	}
	
	public static void stopAll() {
		for(ArrayList<Clip> value : AUDIO_CLIPS.values()) {
			for(Clip clip : value) {
				clip.stop();
			}
		}
	}
	
	public static void clear() {
		AUDIO_CLIPS.clear();
	}
}
