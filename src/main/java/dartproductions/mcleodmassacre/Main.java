package dartproductions.mcleodmassacre;

import dartproductions.mcleodmassacre.engine.GameEngine;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.hitbox.ImageHitbox;
import dartproductions.mcleodmassacre.input.InputManager;
import dartproductions.mcleodmassacre.sound.SoundManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main application class, launches the game, handles global game states
 *
 * @since 0.1.0
 */
public class Main {
	/**
	 * The logger of the main class
	 *
	 * @since 0.1.0
	 */
	private static final Logger LOGGER = LogManager.getLogger(Main.class);
	/**
	 * Globally shared executor service for async tasks
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull ExecutorService EXECUTORS = Executors.newFixedThreadPool(4, Executors.privilegedThreadFactory());
	/**
	 * True if additional debug information should be logged. Defaults to false.
	 *
	 * @since 0.1.0
	 */
	private static boolean DEBUG;
	/**
	 * The state of the application: true if it is running, false if it is shutting down.
	 *
	 * @since 0.1.0
	 */
	private static volatile boolean RUNNING = true;
	/**
	 * The current state of the game
	 *
	 * @since 0.1.0
	 */
	private static volatile @NotNull GameState GAME_STATE = GameState.LOADING;
	/**
	 * The next state of the game; not specified for every state
	 *
	 * @since 0.1.0
	 */
	private static volatile @Nullable GameState NEXT_STATE = GameState.MAIN_MENU;
	
	public static void main(String[] args) {
		checkDebugMode(args);
		configureLogger();
		parseArgs(args);
		loadAppData();
		startGameLoops();
		setGameState(GameState.LOADING, GameState.MAIN_MENU);
	}
	
	/**
	 * Starts the game loops and handling threads.
	 *
	 * @since 0.1.0
	 */
	private static void startGameLoops() {
		InputManager.initialize();
		ImageHitbox.waitForProcessing();
		GraphicsManager.startGameLoop();
		GameEngine.start();
	}
	
	/**
	 * Loads the app's default data.
	 *
	 * @since 0.1.0
	 */
	private static void loadAppData() {
		ResourceManager.extractResources();
		ResourceManager.getOptions();
		ResourceManager.loadStandardGraphics();
		ResourceManager.loadStandardMusic();
	}
	
	/**
	 * Configures the global logger
	 *
	 * @since 0.1.0
	 */
	private static void configureLogger() {
		System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		Configurator.setAllLevels("", isDebug() ? Level.DEBUG : Level.INFO);
	}
	
	/**
	 * Checks if the application is launched in debug mode
	 *
	 * @param args The command line arguments
	 * @since 0.1.0
	 */
	private static void checkDebugMode(@NotNull String[] args) {
		DEBUG = Arrays.asList(args).contains("--debug");
		LOGGER.info("Debug mode is turned " + (DEBUG ? "on" : "off"));
	}
	
	/**
	 * Parses the command line arguments
	 *
	 * @param args The arguments
	 * @since 0.1.0
	 */
	private static void parseArgs(String[] args) {
		for(String arg : args) {
			switch(arg) {
				case "--debug" -> {
				}
				default -> LOGGER.warn("Unrecognised command line argument \"{}\"", arg);
			}
		}
	}
	
	/**
	 * Checks if the app is in debug mode.
	 *
	 * @return True if debug mode is on
	 * @since 0.1.0
	 */
	public static boolean isDebug() {
		return DEBUG;
	}
	
	/**
	 * Checks if the game is running
	 *
	 * @return True if running
	 * @since 0.1.0
	 */
	public static boolean isRunning() {
		return RUNNING;
	}
	
	/**
	 * Sets the state of the application. If false, the shutdown process will begin.
	 *
	 * @param running The new state
	 * @since 0.1.0
	 */
	public static synchronized void setRunning(boolean running) {
		Main.RUNNING = running;
		LOGGER.info("Changed running state to " + running);
		if(!isRunning()) {
			EXECUTORS.shutdown();
			LOGGER.info("Shutting down the global executors");
		}
	}
	
	/**
	 * Gets the global executor service, used for various async tasks.
	 *
	 * @return The executor
	 * @since 0.1.0
	 */
	public static @NotNull ExecutorService getExecutors() {
		return EXECUTORS;
	}
	
	/**
	 * Gets the current state of the game
	 *
	 * @return {@link #GAME_STATE}
	 * @since 0.1.0
	 */
	public static @NotNull GameState getGameState() {
		return GAME_STATE;
	}
	
	/**
	 * Gets the next state of the game. Not specified for every state.
	 *
	 * @return {@link #NEXT_STATE}
	 * @since 0.1.0
	 */
	public static @Nullable GameState getNextState() {
		return NEXT_STATE;
	}
	
	/**
	 * Sets the state of the game.
	 *
	 * @param newGameState The new state
	 * @param newNextState The new next state
	 * @since 0.1.0
	 */
	public static synchronized void setGameState(@NotNull GameState newGameState, @Nullable GameState newNextState) {
		GAME_STATE = newGameState;
		NEXT_STATE = newNextState;
		LOGGER.info("Changed state to " + GAME_STATE + (newNextState == null ? "" : " (with next state " + newNextState + ")"));
		GameEngine.onStateChange(newGameState, newNextState);
		GraphicsManager.onStateChange(newGameState, newNextState);
		SoundManager.onStateChange(newGameState, newNextState);
	}
	
	/**
	 * All of the supported states of the application
	 *
	 * @since 0.1.0
	 */
	public static enum GameState {
		/**
		 * Loading state between two 'normal' states. The next game state should be specified when changing state to loading.
		 *
		 * @since 0.1.0
		 */
		LOADING,
		/**
		 * State for the main menu. This is the first screen the player sees after the initial loading.
		 *
		 * @since 0.1.0
		 */
		MAIN_MENU,
		/**
		 * This state is used when the game is paused while on a map, fighting with other characters.
		 *
		 * @since 0.1.0
		 */
		IN_GAME_PAUSED,
		/**
		 * The general settings menu
		 *
		 * @since 0.1.0
		 */
		SETTINGS_MENU,
		/**
		 * The sound settings menu
		 *
		 * @since 0.1.0
		 */
		SOUND_SETTINGS,
		/**
		 * The control settings menu
		 *
		 * @since 0.1.0
		 */
		CONTROL_SETTINGS,
		/**
		 * The quality settings menu
		 *
		 * @since 0.1.0
		 */
		QUALITY_SETTINGS,
		/**
		 * The roster screen
		 *
		 * @since 0.1.0
		 */
		ROSTER,
		/**
		 * Indicates that the player is playing on a map against other characters.
		 *
		 * @since 0.1.0
		 */
		IN_GAME,
		/**
		 * The gallery menu
		 *
		 * @since 0.1.0
		 */
		GALLERY,
		/**
		 * The data menu
		 *
		 * @since 0.1.0
		 */
		DATA_MENU,
		/**
		 * The menu for playing agains other players
		 *
		 * @since 0.1.0
		 */
		VERSUS_MENU
	}
}
