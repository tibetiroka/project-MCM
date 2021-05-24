package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.ResourceManager;
import dartproductions.mcleodmassacre.engine.GameEngine;
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
import java.util.Timer;
import java.util.TimerTask;

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
		
		new Timer().scheduleAtFixedRate(new TimerTask() {//Guarantees frequent updating even when the engine isn't running. Nothing else. The timer is not accurate (5-10ms differences) and there isn't any need to correct this.
			@Override
			public void run() {
				if(isRunning()) {
					if(!GameEngine.isRunning()) {
						synchronized(GRAPHICS_LOCK) {
							GRAPHICS_LOCK.notifyAll();
						}
					}
				} else {
					cancel();
				}
			}
		}, 20, 20);
	}
	
	private static void initGraphics() {
		Options options = ResourceManager.getOptions();
		WINDOW = new JFrame("McLeod Massacre");
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
		ResourceManager.createAnimations();
		//
		WINDOW.setUndecorated(true);
		WINDOW.setVisible(true);
		WINDOW.setLocation(0, 0);
		LOGGER.info("Window created");
	}
	
	private static void gameLoop() {
		while(Main.isRunning()) {
			try {
				paintGraphics();
			} catch(Exception e) {
				LOGGER.error("Error during graphics painting", e);
			}
			WINDOW.repaint();
			synchronized(GRAPHICS_LOCK) {
				try {
					GRAPHICS_LOCK.wait();
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
			BUFFER_GRAPHICS.setColor(Color.RED);
			fillRectOnScreen(-getOriginOnBuffer().x, -getOriginOnBuffer().y, getBufferSize().width, getBufferSize().height);
			BUFFER_GRAPHICS.setColor(Color.BLUE);
			fillRectOnScreen(0, 0, getDefaultScreenDimension().width, getDefaultScreenDimension().height);
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
		synchronized(GRAPHICS_LOCK) {
			super.paintComponent(g);
			g.drawImage(ResolutionManager.bufferToScreenImage(), 0, 0, this);
		}
	}
	
}
