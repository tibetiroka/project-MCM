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

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
	private static final @NotNull ExecutorService EXECUTORS = Executors.newFixedThreadPool(4, new ThreadFactory() {
		protected int count = 0;
		
		@Override
		public Thread newThread(@NotNull Runnable r) {
			Thread t = new Thread(r, "Executor " + (count++));
			t.setUncaughtExceptionHandler((t1, e) -> {
				LOGGER.error("Uncaught exception in async executor thread (" + t1.getName() + ")", e);
				Main.panic();
			});
			return t;
		}
	});
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
		System.setOut(createLoggingProxy(System.out, Level.DEBUG));
		System.setErr(createLoggingProxy(System.err, Level.WARN));
		if(isDebug()) {
			printSystemDebugInfo();
		}
	}
	
	/**
	 * Creates a new {@link PrintStream} from the specified stream that writes to the log output.
	 *
	 * @param stream The original stream
	 * @param level  The level of the logged messages
	 * @return The new logging stream
	 * @since 0.1.0
	 */
	private static PrintStream createLoggingProxy(final PrintStream stream, final Level level) {
		return new PrintStream(stream) {
			public void print(final String string) {
				LOGGER.log(level, string.strip());
			}
		};
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
		LOGGER.debug("Changed state to " + GAME_STATE + (newNextState == null ? "" : " (with next state " + newNextState + ")"));
		GameEngine.onStateChange(newGameState, newNextState);
		GraphicsManager.onStateChange(newGameState, newNextState);
		SoundManager.onStateChange(newGameState, newNextState);
	}
	
	/**
	 * Prints debug information about the underlying OS, the java instance, and the runtime in general.
	 *
	 * @since 0.1.0
	 */
	public static void printSystemDebugInfo() {
		LOGGER.info("\n=======DEBUG INFO=======\n");
		LOGGER.info("This data is only queried in debug mode or after a crash, and doesn't contain any personal information. It is used for debugging purposes only.");
		LOGGER.info("\n---ENVIRONMENT INFO---");
		String[] properties = new String[]{"file.encoding", "java.class.path", "java.class.version", "java.library.path", "java.runtime.name", "java.runtime.version", "java.specification.name", "java.specification.vendor", "java.specification.version", "java.vendor", "java.vendor.url", "java.version", "java.version.date", "java.vm.compressedOopsMode", "java.vm.info", "java.vm.name", "java.vm.specification.name", "java.vm.specification.vendor", "java.vm.specification.version", "java.vm.vendor", "java.vm.version", "jdk.debug", "os.arch", "os.name", "os.version", "sun.arch.data.model", "sun.cpu.endian", "sun.cpu.isalist", "sun.io.unicode.encoding", "sun.java.command", "sun.java.launcher", "sun.jnu.encoding", "sun.management.compiler", "sun.stderr.encoding", "sun.stdout.encoding"};
		for(String property : properties) {
			LOGGER.info(property + ": " + System.getProperty(property));
		}
		LOGGER.info("\n---RUNTIME INFO---");
		LOGGER.info("Available processors: " + Runtime.getRuntime().availableProcessors());
		LOGGER.info("Free memory: " + Runtime.getRuntime().freeMemory());
		LOGGER.info("Total memory: " + Runtime.getRuntime().totalMemory());
		LOGGER.info("Maximum memory: " + Runtime.getRuntime().maxMemory());
		try {
			com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
			LOGGER.info("\n---OS INFO---");
			LOGGER.info("Committed virtual memory size: " + os.getCommittedVirtualMemorySize());
			LOGGER.info("Free memory: " + os.getFreeMemorySize());
			LOGGER.info("Free swap space: " + os.getFreeSwapSpaceSize());
			LOGGER.info("Total memory size: " + os.getTotalMemorySize());
			LOGGER.info("Total swap space: " + os.getTotalSwapSpaceSize());
		} catch(Exception e) {
			LOGGER.info("Could not query further OS information (build version, available memory etc). Please make sure to include the exact version of your OS (with the specific build id) in the description of any bug report.");
		}
		LOGGER.info("\n=======END OF DEBUG INFO=======\n");
	}
	
	/**
	 * Prints debug information about the cpu's and the system's utilization in general.
	 *
	 * @since 0.1.0
	 */
	public static void printSystemUsageDebugInfo() {
		try {
			OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
			LOGGER.info("\n---SYSTEM USAGE INFO---");
			if(os instanceof com.sun.management.OperatingSystemMXBean osBean) {
				LOGGER.info("CPU load: " + osBean.getCpuLoad());
				LOGGER.info("Process CPU load: " + osBean.getProcessCpuLoad());
				LOGGER.info("Process CPU time: " + osBean.getProcessCpuTime());
			}
			LOGGER.info("Average system load: " + os.getSystemLoadAverage());
			LOGGER.info("\n---END OF SYSTEM USAGE INFO---");
		} catch(Exception e) {
			LOGGER.info("Could not query exact system usage information", e);
		}
	}
	
	/**
	 * Dumps the thread stacks to the output and the logger.
	 *
	 * @since 0.1.0
	 */
	public static void printThreadDump() {
		LOGGER.info("\n---THREAD DUMP---");
		Thread.getAllStackTraces().forEach((thread, stackTrace) -> {
			if(thread != null && stackTrace.length > 0) {
				LOGGER.info("Dumping thread " + thread.getName());
				LOGGER.info(stackTrace[0]);
				for(int i = 1; i < stackTrace.length; i++) {
					LOGGER.info("\tat " + stackTrace[i]);
				}
			}
		});
		LOGGER.info("\n---END OF THREAD DUMP---");
	}
	
	public static void panic() {
		printSystemUsageDebugInfo();
		printSystemDebugInfo();
		printThreadDump();
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
		VERSUS_MENU,
		/**
		 * The credits screen
		 *
		 * @since 0.1.0
		 */
		CREDITS
	}
}
