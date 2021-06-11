package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.input.InputManager;
import dartproductions.mcleodmassacre.options.Option.IntOption;
import dartproductions.mcleodmassacre.options.Options;
import dartproductions.mcleodmassacre.options.Options.StandardOptions;
import dartproductions.mcleodmassacre.options.QualityOption;
import dartproductions.mcleodmassacre.resources.ResourceManager;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static dartproductions.mcleodmassacre.graphics.ResolutionManager.*;

/**
 * Class for managing graphics-related locks and paint operations.
 *
 * @since 0.1.0
 */
public class GraphicsManager extends JPanel {
	/**
	 * General-purpose lock for functions related to graphics
	 *
	 * @since 0.1.0
	 */
	public static final @NotNull Object GRAPHICS_LOCK = new Object();
	/**
	 * Index of the standard rendering layer used for backgrounds
	 *
	 * @since 0.1.0
	 */
	public static final int LAYER_BACKGROUND = 4;
	/**
	 * The index of the lowest priority rendering layer
	 *
	 * @since 0.1.0
	 */
	public static final int LAYER_BOTTOM = 0;
	/**
	 * Index of the standard rendering layer used for game characters
	 *
	 * @since 0.1.0
	 */
	public static final int LAYER_CHARACTERS = 11;
	/**
	 * Index of the standard rendering layer used for GUIs
	 *
	 * @since 0.1.0
	 */
	public static final int LAYER_GUI = 14;
	/**
	 * Index of the standard rending layer used for game level/map components (ground, walls, etc.)
	 *
	 * @since 0.1.0
	 */
	public static final int LAYER_MAP = 8;
	/**
	 * Index of the highest priority rendering layer
	 *
	 * @since 0.1.0
	 */
	public static final int LAYER_TOP = 15;
	/**
	 * The rendering layers
	 *
	 * @since 0.1.0
	 */
	protected static final @NotNull RenderingLayer[] LAYERS = new RenderingLayer[16];
	/**
	 * Graphics-related logger
	 *
	 * @since 0.1.0
	 */
	protected static final Logger LOGGER = LogManager.getLogger(GraphicsManager.class);
	/**
	 * The main graphics thread
	 *
	 * @since 0.1.0
	 */
	public static @Nullable Thread GRAPHICS_THREAD;
	/**
	 * The game window's content pane used for rendering
	 *
	 * @since 0.1.0
	 */
	public static @NotNull GraphicsManager PANEL;
	/**
	 * The game window
	 *
	 * @since 0.1.0
	 */
	public static @NotNull JFrame WINDOW;
	/**
	 * The state of the main graphics thread
	 *
	 * @since 0.1.0
	 */
	private static volatile boolean RUNNING = false;
	
	
	static {
		//creating layers
		Arrays.setAll(LAYERS, i -> new RenderingLayer());
	}
	
	/**
	 * Removes all entities from a rendering layer. This does NOT remove them from the game engine.
	 *
	 * @param i The index of the layer
	 * @since 0.1.0
	 */
	public static void clearLayer(int i) {
		getLayer(i).entities.clear();
	}
	
	/**
	 * Removes all entities from all rendering layers. This does NOT remove them from the game engine.
	 *
	 * @since 0.1.0
	 */
	public static void clearLayers() {
		for(RenderingLayer layer : LAYERS) {
			layer.entities.clear();
		}
	}
	
	/**
	 * Configures the {@link ResolutionManager#BUFFER_GRAPHICS}'s quality settings.
	 *
	 * @since 0.1.0
	 */
	public static void configureQuality() {
		synchronized(GRAPHICS_LOCK) {//quality settings
			QualityOption quality = (QualityOption) ResourceManager.getOptions().getSetting("Quality").getValue();
			BUFFER_GRAPHICS.setRenderingHint(RenderingHints.KEY_RENDERING, switch(quality) {
				case LOW -> RenderingHints.VALUE_RENDER_SPEED;
				case NORMAL -> RenderingHints.VALUE_RENDER_DEFAULT;
				case HIGH -> RenderingHints.VALUE_RENDER_QUALITY;
			});
			BUFFER_GRAPHICS.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, switch(quality) {
				case LOW -> RenderingHints.VALUE_COLOR_RENDER_SPEED;
				case NORMAL -> RenderingHints.VALUE_COLOR_RENDER_DEFAULT;
				case HIGH -> RenderingHints.VALUE_COLOR_RENDER_QUALITY;
			});
			BUFFER_GRAPHICS.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, switch(quality) {
				case LOW -> RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED;
				case NORMAL -> RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT;
				case HIGH -> RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
			});
		}
	}
	
	/**
	 * Gets the specified rendering layer
	 *
	 * @param index The index of the layer
	 * @return The layer
	 * @since 0.1.0
	 */
	public static @NotNull RenderingLayer getLayer(int index) {
		return index < 0 ? LAYERS[0] : index >= LAYERS.length ? LAYERS[LAYERS.length - 1] : LAYERS[index];
	}
	
	/**
	 * Checks if the graphics thread is still running. Might keep returning 'true' if the thread didn't shut down correctly.
	 *
	 * @return True if running
	 * @since 0.1.0
	 */
	public static boolean isRunning() {
		return RUNNING;
	}
	
	/**
	 * Starts the graphics game loop. (Starts the graphics thread.) Fails silently if the thread is already running.
	 *
	 * @since 0.1.0
	 */
	public static void startGameLoop() {
		if(GRAPHICS_THREAD != null && GRAPHICS_THREAD.isAlive()) {//if running, fail
			LOGGER.error("Attempted to start graphics game loop while previous loop was still running");
			return;
		}
		GRAPHICS_THREAD = new Thread(() -> {//graphics thread
			RUNNING = true;
			LOGGER.info("Started graphics thread");
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 1);
			initGraphics();
			gameLoop();
		}, "Graphics");
		GRAPHICS_THREAD.setUncaughtExceptionHandler((t, e) -> {
			LOGGER.error("Uncaught exception in the main graphics thread (" + t.getName() + ")", e);
			Main.panic();
			Main.setRunning(false);
			System.exit(-10002);
		});
		GRAPHICS_THREAD.start();
	}
	
	/**
	 * Closes the application window
	 *
	 * @since 0.1.0
	 */
	private static void closeWindow() {
		WINDOW.setVisible(false);
		WINDOW.dispose();
	}
	
	/**
	 * Rendering loop
	 *
	 * @since 0.1.0
	 */
	private static void gameLoop() {
		while(Main.isRunning()) {
			try {//draw on buffer
				paintGraphics();
			} catch(Exception e) {
				LOGGER.error("Error during graphics painting", e);
			}
			WINDOW.repaint();//draw buffer to screen
			synchronized(GRAPHICS_LOCK) {//wait for engine
				try {
					if(Main.isRunning()) {
						GRAPHICS_LOCK.wait();
					}
				} catch(InterruptedException e) {
					LOGGER.warn("Interrupted wait in graphics thread", e);
				}
			}
		}
		closeWindow();
		RUNNING = false;
		
		LOGGER.info("Graphics thread shut down normally");
	}
	
	/**
	 * Loads all necessary graphical resources and creates the game window.
	 *
	 * @since 0.1.0
	 */
	private static void initGraphics() {
		Options options = ResourceManager.getOptions();
		WINDOW = new JFrame("McLeod Massacre");
		//
		WINDOW.setIconImage(ResourceManager.getImage(Identifier.fromString("icon")));
		WINDOW.setSize(((IntOption) options.getSetting(StandardOptions.WIDTH)).getValue(), ((IntOption) options.getSetting(StandardOptions.HEIGHT)).getValue());
		WINDOW.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		WINDOW.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Main.setRunning(false);
				LOGGER.info("Shutting off application because the window is being closed");
			}
		});
		//
		PANEL = new GraphicsManager();
		WINDOW.setContentPane(PANEL);
		//
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			LOGGER.warn("Couldn't set platform look-and-feel", e);
		}
		//
		PANEL.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		WINDOW.setExtendedState(JFrame.MAXIMIZED_BOTH);
		WINDOW.setResizable(false);
		//
		configureQuality();
		//
		//setting cursor
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		BufferedImage image = ResourceManager.getBufferedImage(Identifier.fromString("cursor"));
		Cursor c = toolkit.createCustomCursor(image, new Point(0, toolkit.getBestCursorSize(image.getWidth(), image.getHeight()).height - 1), "MCM standard");
		WINDOW.setCursor(c);
		//
		WINDOW.setUndecorated(true);
		WINDOW.setVisible(true);
		WINDOW.setLocation(0, 0);
		WINDOW.toFront();
		WINDOW.requestFocus();
		LOGGER.info("Window created");
	}
	
	/**
	 * Paints the graphics of the rendering layers to the buffer.
	 *
	 * @since 0.1.0
	 */
	private static void paintGraphics() {
		synchronized(GRAPHICS_LOCK) {
			//test for image fitting
			/*
			BUFFER_GRAPHICS.setColor(Color.RED);
			fillRectOnScreen(-getOriginOnBuffer().x, -getOriginOnBuffer().y, getBufferSize().width, getBufferSize().height);
			BUFFER_GRAPHICS.setColor(Color.BLUE);
			fillRectOnScreen(0, 0, getDefaultScreenSize().width, getDefaultScreenSize().height);
			BUFFER_GRAPHICS.setColor(Color.GREEN);
			drawRectOnScreen(0, 0, getDefaultScreenSize().width - 1, getDefaultScreenSize().height - 1);*/
			//
			for(RenderingLayer layer : LAYERS) {
				layer.paint();
			}
			if(Main.isDebug()) {
				ResolutionManager.BUFFER_GRAPHICS.setColor(Color.RED);
				ResolutionManager.fillRectOnScreen(InputManager.getCursorLocation().x - 2, InputManager.getCursorLocation().y - 2, 5, 5);
			}
			ResolutionManager.fillVisibleAreas();
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		synchronized(GRAPHICS_LOCK) {
			super.paintComponent(g);
			g.drawImage(ResolutionManager.bufferToScreenImage(), 0, 0, this);
		}
	}
	
}
