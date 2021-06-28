/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.sound;

import dartproductions.mcleodmassacre.GameState;
import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.entity.Entity;
import dartproductions.mcleodmassacre.graphics.ResolutionManager;
import dartproductions.mcleodmassacre.resources.ResourceManager;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import dartproductions.mcleodmassacre.util.Pair;
import dartproductions.mcleodmassacre.util.Pair.ImmutablePair;
import dartproductions.mcleodmassacre.util.Pair.ImmutablePair.ImmutableNullsafePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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
	private static final Logger LOGGER = LogManager.getLogger(SoundManager.class);
	/**
	 * Lock object for the audio thread
	 *
	 * @since 0.1.0
	 */
	public static final Object AUDIO_LOCK = new Object();
	/**
	 * The active audio clips. These clips are playing audio.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull ConcurrentHashMap<Identifier, ArrayList<Pair<Entity, ResumableClip>>> ACTIVE_SFX = new ConcurrentHashMap<>();
	/**
	 * Clip used to avoid the 'pop' when playing music/sfx
	 *
	 * @since 0.1.0
	 */
	private static final Clip FUCK_THIS;
	/**
	 * The created but inactive audio clips. These clips are not playing audio.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull ConcurrentHashMap<Identifier, ArrayList<ResumableClip>> INACTIVE_SFX = new ConcurrentHashMap<>();
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
	private static volatile @Nullable Identifier BACKGROUND_MUSIC_NAME;
	/**
	 * The volume of sound effects on a linear scale between 0 and 1.
	 *
	 * @since 0.1.0
	 */
	private static float MUSIC_VOLUME = 0.8f;
	/**
	 * The volume of sound effects on a linear scale between 0 and 1.
	 *
	 * @since 0.1.0
	 */
	private static float SFX_VOLUME = 0.8f;
	
	static {
		Clip clip = null;
		try {
			clip = AudioSystem.getClip(null);
		} catch(Exception e) {
			LOGGER.error("Could not create clip for sound engine", e);
			Main.panic("Could not initialize sound engine");
		}
		FUCK_THIS = clip;
	}
	
	static {//starting sound engine
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
				synchronized(ACTIVE_SFX) {//automatic panning
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
		}, "Sound engine");
		thread.setDaemon(true);
		thread.setUncaughtExceptionHandler((t, e) -> {
			LOGGER.error("Uncaught exception in sound engine", e);
			Main.panic("Uncaught exception in sound engine");
		});
	}
	
	/**
	 * Changes the volume of all background music. The new value will also become the default volume for background music played in the future.
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
	
	/**
	 * Changes the volume of all sound effects. The new value will also become the default volume for sound effects played in the future.
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
	 * Releases all audio resources cached in this class. This includes both the curently active and inactive clips. All audio output is stopped.
	 *
	 * @since 0.1.0.
	 */
	public static void clear() {
		synchronized(INACTIVE_SFX) {
			INACTIVE_SFX.clear();
		}
		synchronized(ACTIVE_SFX) {
			stopAllSfx();
			ACTIVE_SFX.clear();
		}
		stopAllMusic();
		BACKGROUND_MUSIC = null;
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
	 * Plays a sound effect. Doesn't block the executing thread. Uses the inactive clips from the buffer if possible.
	 *
	 * @param id     The identifier of the effect to play
	 * @param entity The entity to use for audio panning, or null for balanced panning (equal volume on left and right audio channels)
	 * @since 0.1.0
	 */
	public static void playEffect(final @NotNull Identifier id, final @Nullable Entity entity) {
		Main.getExecutors().execute(() -> {
			try {
				ResumableClip clip = null;
				synchronized(INACTIVE_SFX) {
					ArrayList<ResumableClip> clips = INACTIVE_SFX.get(id);//get clip from buffer
					if(clips != null && !clips.isEmpty()) {
						clip = clips.remove(clips.size() - 1);
						clip.setFramePosition(0);
						clip.loop(0);
						clip.start();
					}
				}
				if(clip == null) {//not found in buffer -> create new clip
					clip = ResumableClip.createFromClip(AudioSystem.getClip(null));
					clip.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(ResourceManager.getAudio(id))));
					clip.start();
					final ResumableClip clip_ = clip;
					clip.addLineListener(event -> {//deactivating clip when done
						if(event.getFramePosition() >= clip_.getFrameLength()) {
							synchronized(ACTIVE_SFX) {
								ACTIVE_SFX.get(id).removeIf(p -> p.second() == clip_);
							}
							synchronized(INACTIVE_SFX) {
								var a = INACTIVE_SFX.getOrDefault(id, new ArrayList<>());
								a.add(clip_);
								INACTIVE_SFX.put(id, a);
							}
						}
					});
				}
				{//setting volume of the clip
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
				synchronized(ACTIVE_SFX) {//registering clip
					ArrayList<Pair<Entity, ResumableClip>> clips = ACTIVE_SFX.getOrDefault(id, new ArrayList<>());
					clips.add(new ImmutablePair<>(entity, clip));
					ACTIVE_SFX.put(id, clips);
				}
			} catch(UnsupportedAudioFileException | IOException | LineUnavailableException | NullPointerException e) {
				LOGGER.warn("Could not play sound effect", e);
			}
		});
	}
	
	/**
	 * Plays a random audio resource that has the specified tag as background music.
	 *
	 * @param category The identifier of the tag
	 * @since 0.1.0
	 */
	public static void playMusic(@Nullable Identifier category) {
		try {
			stopAllMusic();
			ImmutableNullsafePair<Identifier, byte[]> pair = ResourceManager.getRandomAudio(category);
			if(pair == null) {
				return;
			}
			BACKGROUND_MUSIC = ResumableClip.createFromClip(AudioSystem.getClip(null));
			BACKGROUND_MUSIC.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(pair.second())));
			setVolume(BACKGROUND_MUSIC, false);
			BACKGROUND_MUSIC.start();
			BACKGROUND_MUSIC.addLineListener(event -> {
				if(event.getFramePosition() >= BACKGROUND_MUSIC.getFrameLength()) {
					Main.getExecutors().execute(SoundManager::updateBackgroundMusic);
					BACKGROUND_MUSIC.close();
				}
			});
			BACKGROUND_MUSIC_NAME = pair.first();
		} catch(LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			LOGGER.warn("Could not play background music", e);
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
	 * Stops all audio output.
	 *
	 * @see #stopAllMusic()
	 * @see #stopAllSfx()
	 * @since 0.1.0
	 */
	public static void stopAll() {
		stopAllSfx();
		stopAllMusic();
	}
	
	/**
	 * Stops the background music.
	 *
	 * @since 0.1.0
	 */
	public static void stopAllMusic() {
		if(BACKGROUND_MUSIC != null) {
			BACKGROUND_MUSIC.stop();
			BACKGROUND_MUSIC = null;
			BACKGROUND_MUSIC_NAME = null;
		}
	}
	
	/**
	 * Stops all active sound effects.
	 *
	 * @since 0.1.0
	 */
	public static void stopAllSfx() {
		synchronized(ACTIVE_SFX) {
			ACTIVE_SFX.values().forEach(list -> list.forEach(p -> p.second().stop()));
		}
	}
	
	/**
	 * Updates the background music. If the audio is playing and it is valid for the current game state, no changes are made. Otherwise a new background music is chosen and played.
	 *
	 * @since 0.1.0
	 */
	public static synchronized void updateBackgroundMusic() {
		if(Main.getGameState() != GameState.IN_GAME_PAUSED) {
			if(BACKGROUND_MUSIC == null || !BACKGROUND_MUSIC.isOpen() || !ResourceManager.hasTag(BACKGROUND_MUSIC_NAME, Main.getGameState().getBackgroundMusicTag(Main.getNextState()))) {
				if(!(FUCK_THIS.isRunning() || FUCK_THIS.isOpen())) {
					try {
						FUCK_THIS.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(ResourceManager.getAudio(Identifier.fromString("silence")))));
					} catch(Exception e) {
						e.printStackTrace();
						Main.panic("Could not start sound engine");
					}
					FUCK_THIS.loop(Clip.LOOP_CONTINUOUSLY);
					FUCK_THIS.start();
				}
				playMusic(Main.getGameState().getBackgroundMusicTag(Main.getNextState()));
			}
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
		 * True if the clip is paused
		 *
		 * @since 0.1.0
		 */
		protected boolean paused;
		/**
		 * The microsecond position inside the clip
		 *
		 * @since 0.1.0
		 */
		protected long position;
		
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
		public long getLongFramePosition() {
			return clip.getLongFramePosition();
		}
		
		@Override
		public long getMicrosecondPosition() {
			return clip.getMicrosecondPosition();
		}
		
		@Override
		public float getLevel() {
			return clip.getLevel();
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
		public void setFramePosition(int frames) {
			clip.setFramePosition(frames);
		}
		
		@Override
		public void setMicrosecondPosition(long microseconds) {
			clip.setMicrosecondPosition(microseconds);
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
		public Line.Info getLineInfo() {
			return clip.getLineInfo();
		}
		
		@Override
		public void open() throws LineUnavailableException {
			clip.open();
		}
		
		@Override
		public void close() {
			clip.setFramePosition(clip.getFrameLength());
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
		 * Resets the position of the clip in the audio sample data to frame 0.
		 *
		 * @since 0.1.0
		 */
		public void reset() {
			clip.setFramePosition(0);
			position = 0;
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
	}
}
