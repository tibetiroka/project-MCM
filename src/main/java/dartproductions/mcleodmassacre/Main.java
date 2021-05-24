package dartproductions.mcleodmassacre;

import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.input.InputManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main application class, launches the game
 */
public class Main {
	/**
	 * The logger of the main class
	 */
	private static final Logger LOGGER = LogManager.getLogger(Main.class);
	/**
	 * Globally shared executor service for async tasks
	 */
	private static final ExecutorService EXECUTORS = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() - 4));
	/**
	 * True if additional debug information should be logged. Defaults to false.
	 */
	private static boolean DEBUG;
	/**
	 * The state of the application: true if it is running, false if it is shutting down.
	 */
	private static volatile boolean RUNNING = true;
	/**
	 * The current state of the game
	 */
	private static volatile GameState GAME_STATE = GameState.LOADING;
	/**
	 * The next state of the game; not specified for every state
	 */
	private static volatile GameState NEXT_STATE = GameState.MAIN_MENU;
	
	public static void main(String[] args) {
		checkDebugMode(args);
		configureLogger();
		parseArgs(args);
		loadAppData();
		startGameLoops();
	}
	
	/**
	 * Starts the game loops and async threads
	 */
	private static void startGameLoops() {
		GraphicsManager.startGameLoop();
		InputManager.initialize();
	}
	
	/**
	 * Loads the app's default data
	 */
	private static void loadAppData() {
		ResourceManager.getOptions();
		ResourceManager.loadStandardGraphics();
		getExecutors().execute(ResourceManager::loadAllResources);
	}
	
	/**
	 * Configures the global logger
	 */
	private static void configureLogger() {
		Configurator.setAllLevels(Main.class.getPackageName(), isDebug() ? Level.DEBUG : Level.INFO);
	}
	
	/**
	 * Checks if the application is launched in debug mode
	 *
	 * @param args The command line arguments
	 */
	private static void checkDebugMode(String[] args) {
		DEBUG = Arrays.asList(args).contains("--debug");
		LOGGER.info("Debug mode is turned " + (DEBUG ? "on" : "off"));
	}
	
	/**
	 * Parses the command line arguments
	 *
	 * @param args The arguments
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
	 */
	public static boolean isDebug() {
		return DEBUG;
	}
	
	/**
	 * Checks if the game is running
	 *
	 * @return True if running
	 */
	public static boolean isRunning() {
		return RUNNING;
	}
	
	/**
	 * Sets the state of the application
	 *
	 * @param running The new state
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
	 */
	public static ExecutorService getExecutors() {
		return EXECUTORS;
	}
	
	/**
	 * Gets the current state of the game
	 *
	 * @return {@link #GAME_STATE}
	 */
	public static GameState getGameState() {
		return GAME_STATE;
	}
	
	/**
	 * Gets the next state of the game. Not specified for every state.
	 *
	 * @return {@link #NEXT_STATE}
	 */
	public static GameState getNextState() {
		return NEXT_STATE;
	}
	
	/**
	 * Sets the state of the game.
	 *
	 * @param newGameState The new state
	 * @param newNextState The new next state
	 */
	public static synchronized void setGameState(GameState newGameState, GameState newNextState) {
		GAME_STATE = newGameState;
		NEXT_STATE = newNextState;
	}
	
	public static enum GameState {
		LOADING, MAIN_MENU, SETTINGS_MENU, SOUND_SETTINGS, CONTROL_SETTINGS, QUALITY_SETTINGS, ROSTER, IN_GAME
	}
}
