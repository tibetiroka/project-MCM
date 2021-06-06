package dartproductions.mcleodmassacre.sound;

import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.Main.GameState;
import dartproductions.mcleodmassacre.ResourceManager;
import dartproductions.mcleodmassacre.entity.Entity;
import dartproductions.mcleodmassacre.graphics.ResolutionManager;
import dartproductions.mcleodmassacre.util.Pair;
import dartproductions.mcleodmassacre.util.Pair.ImmutablePair;
import dartproductions.mcleodmassacre.util.Pair.ImmutablePair.ImmutableNullsafePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.*;
import javax.sound.sampled.Control.Type;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for playing sound effects and managing sound resources.
 *
 * @since 0.1.0
 */
public class SoundManager {
	/**
	 * Sound category for menu background music
	 *
	 * @since 0.1.0
	 */
	public static final String CATEGORY_MENU = "MENU";
	/**
	 * Sound category for unknown game states.
	 *
	 * @since 0.1.0
	 */
	public static final String CATEGORY_UNKNOWN = "UNKNOWN";
	/**
	 * Lock object for the audio thread
	 *
	 * @since 0.1.0
	 */
	public static final Object AUDIO_LOCK = new Object();
	/**
	 * The created but inactive audio clips. These clips are not playing audio.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull ConcurrentHashMap<String, ArrayList<ResumableClip>> INACTIVE_SFX = new ConcurrentHashMap<>();
	/**
	 * The active audio clips. These clips are playing audio.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull ConcurrentHashMap<String, ArrayList<Pair<Entity, ResumableClip>>> ACTIVE_SFX = new ConcurrentHashMap<>();
	private static final Logger LOGGER = LogManager.getLogger(SoundManager.class);
	/**
	 * The volume of sound effects on a linear scale between 0 and 1.
	 *
	 * @since 0.1.0
	 */
	private static float SFX_VOLUME = 0.8f;
	/**
	 * The volume of sound effects on a linear scale between 0 and 1.
	 *
	 * @since 0.1.0
	 */
	private static float MUSIC_VOLUME = 0.8f;
	/**
	 * The clip used for playing background music
	 *
	 * @since 0.1.0
	 */
	private static volatile @Nullable ResumableClip BACKGROUND_MUSIC;
	/**
	 * The name of the active background music.
	 *
	 * @since 0.1.0
	 */
	private static volatile @Nullable String BACKGROUND_MUSIC_NAME;
	
	//TODO: add sound categories, play sounds automatically based on game state, separate volume for music and sfx, panning via FloatContol, etc.
	
	static {
		Thread thread = new Thread(() -> {
			final float centerX = ResolutionManager.getOriginOnBuffer().x + ResolutionManager.getDefaultScreenSize().width / 2.0f;
			final float width = (float) ResolutionManager.getDefaultScreenSize().getWidth();
			while(Main.isRunning()) {
				synchronized(AUDIO_LOCK) {
					try {
						AUDIO_LOCK.wait();
					} catch(InterruptedException e) {
						LOGGER.warn("Interrupted wait in audio thread", e);
					}
				}
				synchronized(ACTIVE_SFX) {
					ACTIVE_SFX.values().forEach(list -> list.forEach(pair -> {
						if(pair.first() != null) {
							float entityX = (float) pair.first().getCurrentAnimation().getCurrentHitbox().getBounds().getCenterX();
							float distance = entityX - centerX;
							distance /= width;
							FloatControl control = (FloatControl) pair.second().getControl(FloatControl.Type.PAN);
							control.setValue(distance < -1 ? -1 : (distance > 1 ? 1 : distance));
						}
					}));
				}
			}
		}, "Audio thread");
		thread.setDaemon(true);
		thread.setUncaughtExceptionHandler((t, e) -> LOGGER.error("Uncaught exception in audio thread", e));
	}
	
	/**
	 * Plays a sound effect.
	 *
	 * @param name   The name of the effect to play
	 * @param entity The entity to use for audio panning, or null for balanced panning
	 * @since 0.1.0
	 */
	public static void playEffect(final @NotNull String name, final @Nullable Entity entity) {
		Main.getExecutors().execute(() -> {
			try {
				ResumableClip clip = null;
				synchronized(INACTIVE_SFX) {
					ArrayList<ResumableClip> clips = INACTIVE_SFX.get(name);
					if(clips != null && clips.size() > 0) {
						clip = clips.remove(clips.size() - 1);
						clip.setFramePosition(0);
						clip.loop(0);
						clip.start();
					}
				}
				if(clip == null) {
					clip = ResumableClip.createFromClip(AudioSystem.getClip(null));
					clip.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(ResourceManager.getSound(name))));
					clip.start();
					final ResumableClip clip_ = clip;
					clip.addLineListener(event -> {//deactivating clip
						if(event.getType() == LineEvent.Type.CLOSE || event.getFramePosition() >= clip_.getFrameLength()) {
							synchronized(ACTIVE_SFX) {
								ACTIVE_SFX.get(name).removeIf(p -> p.second() == clip_);
							}
							synchronized(INACTIVE_SFX) {
								var a = INACTIVE_SFX.getOrDefault(name, new ArrayList<>());
								a.add(clip_);
								INACTIVE_SFX.put(name, a);
							}
						}
					});
				}
				{
					setVolume(clip, true);
					FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.PAN);
					if(entity == null) {
						control.setValue(0);
					} else if(!entity.isMovable()) {
						final float centerX = ResolutionManager.getOriginOnBuffer().x + ResolutionManager.getDefaultScreenSize().width / 2.0f;
						final float width = (float) ResolutionManager.getDefaultScreenSize().getWidth();
						final float entityX = (float) entity.getCurrentAnimation().getCurrentHitbox().getBounds().getCenterX();
						float distance = entityX - centerX;
						distance /= width;
						control.setValue(distance < -1 ? -1 : (distance > 1 ? 1 : distance));
					}
				}
				synchronized(ACTIVE_SFX) {
					ArrayList<Pair<Entity, ResumableClip>> clips = ACTIVE_SFX.getOrDefault(name, new ArrayList<>());
					clips.add(new ImmutablePair<>(entity, clip));
					ACTIVE_SFX.put(name, clips);
				}
			} catch(UnsupportedAudioFileException | IOException | LineUnavailableException e) {
				LOGGER.warn("Could not play sound effect", e);
			}
		});
	}
	
	/**
	 * Pauses all sound effects and background music.
	 *
	 * @since 0.1.0
	 */
	public static void pause() {
		synchronized(ACTIVE_SFX) {
			ACTIVE_SFX.values().forEach(list -> list.forEach(p -> p.second().pause()));
		}
		if(BACKGROUND_MUSIC != null) {
			BACKGROUND_MUSIC.pause();
		}
	}
	
	/**
	 * Continues playing all paused sound effects and background music.
	 *
	 * @since 0.1.0
	 */
	public static void resume() {
		synchronized(ACTIVE_SFX) {
			ACTIVE_SFX.values().forEach(list -> list.forEach(p -> p.second().resume()));
		}
		if(BACKGROUND_MUSIC != null) {
			BACKGROUND_MUSIC.resume();
		}
	}
	
	/**
	 * Sets the volume of the specified clip to match the value of {@link #SFX_VOLUME}. Please note that while {@link #SFX_VOLUME} is between 0 and 1 in a linear scale, the volume of the clip is measured by its offset in decibels.
	 *
	 * @param clip The clip
	 * @param sfx  True if the clip is a sound effect, false for music
	 * @since 0.1.0
	 */
	private static void setVolume(@NotNull Clip clip, boolean sfx) {
		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float range = gainControl.getMaximum() - gainControl.getMinimum();
		float gain = (range * (sfx ? SFX_VOLUME : MUSIC_VOLUME)) + gainControl.getMinimum();
		gainControl.setValue(gain);
	}
	
	/**
	 * Changes the volume of all sound effects.
	 *
	 * @param volume The new volume between 0 and 1
	 * @since 0.1.0
	 */
	public static void changeSfxVolume(float volume) {
		SFX_VOLUME = volume;
		synchronized(ACTIVE_SFX) {
			for(ArrayList<Pair<Entity, ResumableClip>> value : ACTIVE_SFX.values()) {
				for(Pair<Entity, ResumableClip> sfx : value) {
					setVolume(sfx.getSecond(), true);
				}
			}
		}
	}
	
	/**
	 * Changes the volume of all background music.
	 *
	 * @param volume The new volume between 0 and 1
	 * @since 0.1.0
	 */
	public static void changeMusicVolume(float volume) {
		MUSIC_VOLUME = volume;
		if(BACKGROUND_MUSIC != null) {
			setVolume(BACKGROUND_MUSIC, false);
		}
	}
	
	public static void stopAll() {
		stopAllSfx();
		stopAllMusic();
	}
	
	public static void stopAllSfx() {
		synchronized(ACTIVE_SFX) {
			ACTIVE_SFX.values().forEach(list -> list.forEach(p -> p.second().stop()));
		}
	}
	
	public static void stopAllMusic() {
		if(BACKGROUND_MUSIC != null) {
			BACKGROUND_MUSIC.stop();
			BACKGROUND_MUSIC = null;
			BACKGROUND_MUSIC_NAME = null;
		}
	}
	
	public static void playMusic(@NotNull String category) {
		try {
			stopAllMusic();
			ImmutableNullsafePair<String, byte[]> pair = ResourceManager.getRandomSound(category);
			if(pair == null) {
				return;
			}
			BACKGROUND_MUSIC = ResumableClip.createFromClip(AudioSystem.getClip(null));
			BACKGROUND_MUSIC.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(pair.second())));
			setVolume(BACKGROUND_MUSIC, false);
			BACKGROUND_MUSIC.start();
			BACKGROUND_MUSIC_NAME = pair.first();
		} catch(LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			LOGGER.warn("Could not play background music", e);
		}
	}
	
	public static void clear() {
		synchronized(INACTIVE_SFX) {
			INACTIVE_SFX.clear();
		}
		synchronized(ACTIVE_SFX) {
			stopAllSfx();
			ACTIVE_SFX.clear();
		}
		BACKGROUND_MUSIC = null;
	}
	
	/**
	 * Handles any changes when the application's state changes.
	 *
	 * @param newGameState The new state of the application
	 * @param newNextState The expected state after the new state
	 * @since 0.1.0
	 */
	public static synchronized void onStateChange(@NotNull Main.GameState newGameState, @Nullable Main.GameState newNextState) {
		stopAllSfx();
		if(newGameState != GameState.IN_GAME_PAUSED) {
			if(!(ResourceManager.isValidSoundForCategory(BACKGROUND_MUSIC_NAME, getSoundCategory(newGameState)) || (newGameState == GameState.LOADING && ResourceManager.isValidSoundForCategory(BACKGROUND_MUSIC_NAME, getSoundCategory(newNextState))))) {
				//if the sound is not valid for this state, or the state is LOADING and the sound is not valid for the next one
				if(newGameState != GameState.LOADING) {
					playMusic(getSoundCategory(newGameState));
				}
			}
		}
	}
	
	/**
	 * Gets the sound category for the specified game state.
	 *
	 * @param state The game state
	 * @return The sound category
	 * @since 0.1.0
	 */
	private static @NotNull String getSoundCategory(@NotNull GameState state) {
		return switch(state) {
			case MAIN_MENU, DATA_MENU, CONTROL_SETTINGS, GALLERY, SOUND_SETTINGS, SETTINGS_MENU, QUALITY_SETTINGS, ROSTER, VERSUS_MENU -> CATEGORY_MENU;
			default -> CATEGORY_UNKNOWN;
		};
	}
	
	/**
	 * Clip implementation that allows pausing and resuming the clip at any time.
	 *
	 * @since 0.1.0
	 */
	protected static class ResumableClip implements Clip {
		
		/**
		 * The wrapped clip
		 *
		 * @since 0.1.0
		 */
		protected final @NotNull Clip clip;
		/**
		 * The microsecond position inside the clip
		 *
		 * @since 0.1.0
		 */
		protected long position;
		/**
		 * True if the clip is paused
		 *
		 * @since 0.1.0
		 */
		protected boolean paused;
		
		/**
		 * Creates a new resumable clip.
		 *
		 * @param clip The wrapped clip
		 * @since 0.1.0
		 */
		protected ResumableClip(@NotNull Clip clip) {
			this.clip = clip;
		}
		
		/**
		 * Creates a clip that can be paused. If the specified {@link Clip} is already a {@link ResumableClip}, the original clip is returned. Otherwise a new clip is created.
		 *
		 * @param clip The clip to use
		 * @return The resumable clip
		 * @since 0.1.0
		 */
		public static @NotNull ResumableClip createFromClip(@NotNull Clip clip) {
			if(clip instanceof ResumableClip) {
				return (ResumableClip) clip;
			}
			return new ResumableClip(clip);
		}
		
		@Override
		public void open(AudioFormat format, byte[] data, int offset, int bufferSize) throws LineUnavailableException {
			clip.open(format, data, offset, bufferSize);
		}
		
		@Override
		public void open(AudioInputStream stream) throws LineUnavailableException, IOException {
			clip.open(stream);
		}
		
		@Override
		public int getFrameLength() {
			return clip.getFrameLength();
		}
		
		@Override
		public long getMicrosecondLength() {
			return clip.getMicrosecondPosition();
		}
		
		@Override
		public void setLoopPoints(int start, int end) {
			clip.setLoopPoints(start, end);
		}
		
		@Override
		public void loop(int count) {
			clip.loop(count);
		}
		
		@Override
		public void drain() {
			clip.drain();
		}
		
		@Override
		public void flush() {
			clip.flush();
		}
		
		@Override
		public void start() {
			paused = false;
			clip.start();
			setMicrosecondPosition(position);
		}
		
		@Override
		public void stop() {
			clip.stop();
			if(!paused) {
				position = 0;
			}
		}
		
		@Override
		public boolean isRunning() {
			return clip.isRunning();
		}
		
		@Override
		public boolean isActive() {
			return clip.isActive();
		}
		
		@Override
		public AudioFormat getFormat() {
			return clip.getFormat();
		}
		
		@Override
		public int getBufferSize() {
			return clip.getBufferSize();
		}
		
		@Override
		public int available() {
			return clip.available();
		}
		
		@Override
		public int getFramePosition() {
			return clip.getFramePosition();
		}
		
		@Override
		public void setFramePosition(int frames) {
			clip.setFramePosition(frames);
		}
		
		@Override
		public long getLongFramePosition() {
			return clip.getLongFramePosition();
		}
		
		@Override
		public long getMicrosecondPosition() {
			return clip.getMicrosecondPosition();
		}
		
		@Override
		public void setMicrosecondPosition(long microseconds) {
			clip.setMicrosecondPosition(microseconds);
		}
		
		@Override
		public float getLevel() {
			return clip.getLevel();
		}
		
		@Override
		public Line.Info getLineInfo() {
			return clip.getLineInfo();
		}
		
		@Override
		public void open() throws LineUnavailableException {
			clip.open();
		}
		
		@Override
		public void close() {
			clip.close();
		}
		
		@Override
		public boolean isOpen() {
			return clip.isOpen();
		}
		
		@Override
		public Control[] getControls() {
			return clip.getControls();
		}
		
		@Override
		public boolean isControlSupported(Type control) {
			return clip.isControlSupported(control);
		}
		
		@Override
		public Control getControl(Type control) {
			return clip.getControl(control);
		}
		
		@Override
		public void addLineListener(LineListener listener) {
			clip.addLineListener(listener);
		}
		
		@Override
		public void removeLineListener(LineListener listener) {
			clip.removeLineListener(listener);
		}
		
		/**
		 * Pauses the clip. When the {@link #start()} method is called on a paused clip, it will continue playing from its current location.
		 *
		 * @since 0.1.0
		 */
		public void pause() {
			if(!isPaused()) {
				paused = true;
				position = clip.getMicrosecondPosition();
				clip.stop();
			}
		}
		
		/**
		 * Starts this clip if paused.
		 *
		 * @since 0.1.0
		 */
		public void resume() {
			if(isPaused()) {
				start();
			}
		}
		
		/**
		 * Checks if the clip is paused.
		 *
		 * @return True if paused
		 * @since 0.1.0
		 */
		public boolean isPaused() {
			return paused;
		}
		
		/**
		 * Resets the position of the clip in the audio sample data to frame 0.
		 *
		 * @since 0.1.0
		 */
		public void reset() {
			clip.setFramePosition(0);
			position = 0;
		}
	}
}
