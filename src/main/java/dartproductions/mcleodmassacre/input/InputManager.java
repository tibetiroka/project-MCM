package dartproductions.mcleodmassacre.input;

import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InputManager {
	private static final Logger LOGGER = LogManager.getLogger(InputManager.class);
	//
	private static final LinkedList<KeyEvent> KEY_EVENTS = new LinkedList<>();
	private static final LinkedList<MouseEvent> MOUSE_EVENTS = new LinkedList<>();
	//
	private static final AtomicInteger KEY_EVENT_REQUESTS = new AtomicInteger(0);
	private static final AtomicInteger MOUSE_EVENT_REQUESTS = new AtomicInteger(0);
	//
	private static final ConcurrentHashMap<Integer, Boolean> KEY_STATES = new ConcurrentHashMap<>();
	private static final boolean[] MOUSE_STATES = new boolean[MouseInfo.getNumberOfButtons()];
	//
	private static volatile KeyEvent LATEST_KEY_EVENT;
	private static volatile MouseEvent LATEST_MOUSE_EVENT;
	
	public static void initialize() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
			synchronized(KEY_EVENTS) {
				switch(e.getID()) {
					case KeyEvent.KEY_PRESSED -> KEY_STATES.put(e.getKeyCode(), true);
					case KeyEvent.KEY_RELEASED -> KEY_STATES.put(e.getKeyCode(), false);
				}
				if(KEY_EVENT_REQUESTS.get() > 0) {
					LATEST_KEY_EVENT = e;
					KEY_EVENT_REQUESTS.decrementAndGet();
					KEY_EVENT_REQUESTS.notify();
				} else {
					KEY_EVENTS.add(e);
				}
			}
			return false;
		});
		long eventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK;
		Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
			synchronized(MOUSE_EVENTS) {
				switch(e.getID()) {
					case MouseEvent.MOUSE_PRESSED -> MOUSE_STATES[((MouseEvent) e).getButton()] = true;
					case MouseEvent.MOUSE_RELEASED -> MOUSE_STATES[((MouseEvent) e).getButton()] = false;
				}
				if(MOUSE_EVENT_REQUESTS.get() > 0) {
					LATEST_MOUSE_EVENT = (MouseEvent) e;
					MOUSE_EVENT_REQUESTS.decrementAndGet();
					MOUSE_EVENT_REQUESTS.notify();
				} else {
					MOUSE_EVENTS.add((MouseEvent) e);
				}
			}
		}, eventMask);
		LOGGER.info("Initialized input manager");
	}
	
	public static KeyEvent getNextKeyEvent() {
		synchronized(KEY_EVENT_REQUESTS) {
			KEY_EVENT_REQUESTS.incrementAndGet();
			try {
				KEY_EVENT_REQUESTS.wait();
			} catch(InterruptedException e) {
				LOGGER.warn("Wait for key event was interrupted", e);
			}
			return LATEST_KEY_EVENT;
		}
	}
	
	public static MouseEvent getNextMouseEvent() {
		synchronized(MOUSE_EVENT_REQUESTS) {
			MOUSE_EVENT_REQUESTS.incrementAndGet();
			try {
				MOUSE_EVENT_REQUESTS.wait();
			} catch(InterruptedException e) {
				LOGGER.warn("Wait for mouse event was interrupted", e);
			}
			return LATEST_MOUSE_EVENT;
		}
	}
	
	public static KeyEvent getKeyEvent() {
		if(KEY_EVENTS.isEmpty()) {
			return null;
		}
		return KEY_EVENTS.removeFirst();
	}
	
	public static MouseEvent getMouseEvent() {
		if(MOUSE_EVENTS.isEmpty()) {
			return null;
		}
		return MOUSE_EVENTS.removeFirst();
	}
	
	/**
	 * Gets the cursor location on the local machine.
	 *
	 * @return The cursor's location
	 */
	public static Point getCursorLocationLocal() {
		Point screenLoc = MouseInfo.getPointerInfo().getLocation();
		screenLoc.x -= GraphicsManager.PANEL.getLocationOnScreen().x;
		screenLoc.y -= GraphicsManager.PANEL.getLocationOnScreen().y;
		return screenLoc;
	}
	
	/**
	 * Gets the cursor location as it would appear on tib's machine. Useful if you don't want to fuck around with conversions all day.
	 *
	 * @return The cursor location in the original coordinate system
	 */
	public static Point getCursorLocation() {
		Point screenLoc = MouseInfo.getPointerInfo().getLocation();
		double currentWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		double currentHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		double myWidth = 1280.;
		double myHeight = 1024.;
		screenLoc.setLocation(screenLoc.getX() * myWidth / currentWidth, screenLoc.getY() * myHeight / currentHeight);
		return screenLoc;
	}
	
	/**
	 * Checks if the key of the given key code is held down.
	 *
	 * @param key The key code
	 * @return True if being pressed
	 */
	public static boolean isKeyPressed(int key) {
		return KEY_STATES.getOrDefault(key, false);
	}
	
	/**
	 * Checks if the specified mouse button is held down.
	 *
	 * @param button The mouse button
	 * @return True if being pressed
	 */
	public static boolean isMouseButtonPressed(int button) {
		if(MOUSE_STATES.length > button && button >= 0) {
			return MOUSE_STATES[button];
		}
		return false;
	}
	
}
