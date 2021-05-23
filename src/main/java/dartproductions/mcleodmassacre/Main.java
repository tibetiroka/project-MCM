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
	 * True if additional debug information should be logged. Defaults to false.
	 */
	private static boolean DEBUG;
	/**
	 * The state of the application: true if it is running, false if it is shutting down.
	 */
	private static volatile boolean RUNNING = true;
	private static ExecutorService EXECUTORS = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() - 4));
	private static volatile GameState GAME_STATE = GameState.LOADING;
	private static volatile GameState NEXT_STATE = GameState.MAIN_MENU;
	
	public static void main(String[] args) {
		checkDebugMode(args);
		configureLogger();
		parseArgs(args);
		loadAppData();
		startGameLoops();
	}
	
	private static void startGameLoops() {
		GraphicsManager.startGameLoop();
		InputManager.initialize();
	}
	
	private static void loadAppData() {
		ResourceManager.getOptions();
	}
	
	private static void configureLogger() {
		Configurator.setAllLevels(Main.class.getPackageName(), isDebug() ? Level.DEBUG : Level.INFO);
	}
	
	private static void checkDebugMode(String[] args) {
		DEBUG = Arrays.asList(args).contains("--debug");
		LOGGER.info("Debug mode is turned " + (DEBUG ? "on" : "off"));
	}
	
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
	
	public static boolean isRunning() {
		return RUNNING;
	}
	
	public static void setRunning(boolean running) {
		Main.RUNNING = running;
		LOGGER.info("Changed running state to " + running);
	}
	
	public static ExecutorService getExecutors() {
		return EXECUTORS;
	}
	
	public static GameState getGameState() {
		return GAME_STATE;
	}
	
	public static GameState getNextState() {
		return NEXT_STATE;
	}
	
	public static synchronized void setGameState(GameState newGameState, GameState newNextState) {
		GAME_STATE = newGameState;
		NEXT_STATE = newNextState;
	}
	
	public static enum GameState {
		LOADING, MAIN_MENU, SETTINGS_MENU, SOUND_SETTINGS, CONTROL_SETTINGS, QUALITY_SETTINGS, ROSTER, IN_GAME
	}
}
