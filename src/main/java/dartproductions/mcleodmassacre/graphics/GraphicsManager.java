package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.ResourceManager;
import dartproductions.mcleodmassacre.engine.GameEngine;
import dartproductions.mcleodmassacre.engine.GameEngine.EngineState;
import dartproductions.mcleodmassacre.options.Option.IntOption;
import dartproductions.mcleodmassacre.options.Options;
import dartproductions.mcleodmassacre.options.Options.StandardOptions;
import dartproductions.mcleodmassacre.options.QualityOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import static dartproductions.mcleodmassacre.graphics.ResolutionManager.*;

public class GraphicsManager extends JPanel {
	public static final Object GRAPHICS_LOCK = new Object();
	protected static final Logger LOGGER = LogManager.getLogger(GraphicsManager.class);
	protected static final BufferedImage BUFFER = ResolutionManager.createBufferImage();
	protected static final Graphics2D BUFFER_GRAPHICS = BUFFER.createGraphics();
	public static Thread GRAPHICS_THREAD;
	public static JFrame WINDOW;
	public static GraphicsManager PANEL;
	private static volatile boolean RUNNING = false;
	
	public static void startGameLoop() {
		if(GRAPHICS_THREAD != null && GRAPHICS_THREAD.isAlive()) {
			LOGGER.error("Attempted to start graphics game loop while previous loop was still running");
			return;
		}
		
		GRAPHICS_THREAD = new Thread(() -> {
			RUNNING = true;
			LOGGER.info("Started graphics thread");
			Thread.currentThread().setPriority(6);
			initGraphics();
			gameLoop();
		}, "Main Graphics Thread");
		GRAPHICS_THREAD.setUncaughtExceptionHandler((t, e) -> LOGGER.error("Uncaught exception in the main graphics thread", e));
		GRAPHICS_THREAD.start();
	}
	
	private static void initGraphics() {
		Options options = ResourceManager.getOptions();
		WINDOW = new JFrame("McLeod Massacre");
		//
		ResourceManager.loadStandardGraphics();
		//
		WINDOW.setIconImage(ResourceManager.getImage("icon"));
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
		WINDOW.setUndecorated(true);
		WINDOW.setVisible(true);
		WINDOW.setLocation(0, 0);
		LOGGER.info("Window created");
	}
	
	private static void gameLoop() {
		while(Main.isRunning()) {
			//BUFFER = new BufferedImage(visible.width == 0 ? WINDOW.getWidth() - 16 : visible.width, visible.height == 0 ? WINDOW.getHeight() - 16 - 23 : visible.height, BufferedImage.TYPE_INT_ARGB);
			try {
				paintGraphics();
			} catch(Exception e) {
				LOGGER.error("Error during graphics painting", e);
			}
			WINDOW.repaint();
			if(GameEngine.isRunning() && GameEngine.getState() != EngineState.LOADING) {
				synchronized(GRAPHICS_LOCK) {
					try {
						GRAPHICS_LOCK.wait();
					} catch(InterruptedException e) {
						LOGGER.warn("Interrupted wait in graphics thread", e);
					}
				}
			} else {
				try {
					synchronized(GRAPHICS_LOCK) {
						GRAPHICS_LOCK.wait(20);
					}
				} catch(InterruptedException e) {
					LOGGER.warn("Interrupted wait in graphics thread", e);
				}
			}
		}
		closeWindow();
		RUNNING = false;
	}
	
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
	
	private static void paintGraphics() {
		synchronized(GRAPHICS_LOCK) {
			
			/*switch(Main.getGameState()) {
				case LOADING -> {
					switch(Main.getNextState()) {
						default:
							return;
					}
				}
			}*/
			
			//test for image fitting
			BUFFER_GRAPHICS.setColor(Color.GREEN);
			drawRectOnScreen(0, 0, getDefaultScreenDimension().width - 1, getDefaultScreenDimension().height - 1);
		}
	}
	
	private static void closeWindow() {
		WINDOW.setVisible(false);
		WINDOW.dispose();
	}
	
	public static boolean isRunning() {
		return RUNNING;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(ResolutionManager.bufferToScreenImage(), 0, 0, this);
	}
	
}
