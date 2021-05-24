package dartproductions.mcleodmassacre.engine;

import dartproductions.mcleodmassacre.ResourceManager;
import dartproductions.mcleodmassacre.hitbox.ImageHitbox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;

public class GameEngine {
	public static final Object ENGINE_LOCK = new Object();
	protected static final Logger LOGGER = LogManager.getLogger(GameEngine.class);
	public static Thread ENGINE_THREAD;
	private static boolean RUNNING = false;
	private static volatile EngineState STATE = EngineState.STOPPED;
	private static Instant previousFrame;
	private static Instant nextFrame;
	private static boolean firstStart = false;
	
	public static void start() {
		if(ENGINE_THREAD != null && ENGINE_THREAD.isAlive()) {
			LOGGER.error("Attempted to start engine thread while the thread was still running");
			return;
		}
		ENGINE_THREAD = new Thread(() -> {
			RUNNING = true;
			LOGGER.info("Started game engine thread");
			if(STATE == EngineState.LOADING) {
				ImageHitbox.waitForProcessing();
				STATE = EngineState.PAUSED;
			}
			//todo engine stuff, timings
		}, "Main Engine Thread");
		ENGINE_THREAD.setUncaughtExceptionHandler((t, e) -> LOGGER.error("Uncaught exception in the main engine thread", e));
		if(firstStart) {
			firstStart = false;
			ResourceManager.createAnimations();
		}
		ENGINE_THREAD.start();
	}
	
	public static boolean isRunning() {
		return RUNNING;
	}
	
	public static EngineState getState() {
		return STATE;
	}
	
	public static synchronized void setState(EngineState state) {
		STATE = state;
	}
	
	public static void loadEngine() {
		setState(EngineState.LOADING);
	}
	
	public static void pause() {
		setState(EngineState.PAUSED);
	}
	
	public static void unpause() {
		if(getState() == EngineState.PAUSED) {
			setState(EngineState.RUNNING);
		}
	}
	
	public static void stop() {
		setState(EngineState.STOPPING);
	}
	
	public static enum EngineState {
		LOADING, PAUSED, RUNNING, STOPPING, STOPPED;
	}
}
