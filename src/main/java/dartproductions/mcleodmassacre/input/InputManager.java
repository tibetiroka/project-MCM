package dartproductions.mcleodmassacre.input;

import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.input.InputManager.ActionType.ControlConfig.ValueControlConfig;
import net.java.games.input.*;
import net.java.games.input.Controller.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputManager {
	private static final Logger LOGGER = LogManager.getLogger(InputManager.class);
	private static final ArrayList<Controller> PLAYER_DEVICES = new ArrayList<>();
	private static final Object KEY_LOCK = new Object(), MOUSE_LOCK = new Object(), CONTROLLER_LOCK = new Object(), DEVICE_LOCK = new Object();
	//
	//private static final LinkedList<KeyEvent> KEY_EVENTS = new LinkedList<>();
	//private static final LinkedList<MouseEvent> MOUSE_EVENTS = new LinkedList<>();
	//
	//private static final AtomicInteger KEY_EVENT_REQUESTS = new AtomicInteger(0);
	//private static final AtomicInteger MOUSE_EVENT_REQUESTS = new AtomicInteger(0);
	//
	//private static final ConcurrentHashMap<Integer, Boolean> KEY_STATES = new ConcurrentHashMap<>();
	//private static final boolean[] MOUSE_STATES = new boolean[MouseInfo.getNumberOfButtons()];
	private static final ArrayList<InputAction<Event>> ACTIONS = new ArrayList<>();
	//
	private static volatile KeyEvent LATEST_KEY_EVENT;
	private static volatile MouseEvent LATEST_MOUSE_EVENT;
	
	public static void initialize() {
		/*KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
			synchronized(KEY_LOCK) {
				switch(e.getID()) {
					case KeyEvent.KEY_PRESSED -> KEY_STATES.put(e.getKeyCode(), true);
					case KeyEvent.KEY_RELEASED -> KEY_STATES.put(e.getKeyCode(), false);
				}
				if(KEY_EVENT_REQUESTS.get() > 0) {
					LATEST_KEY_EVENT = e;
					KEY_EVENT_REQUESTS.decrementAndGet();
					KEY_EVENT_REQUESTS.notify();
				} else {
					//todo create input event
				}
			}
			return false;
		});*/
		/*long eventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK;
		Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
			synchronized(MOUSE_LOCK) {
				switch(e.getID()) {
					case MouseEvent.MOUSE_PRESSED -> MOUSE_STATES[((MouseEvent) e).getButton()] = true;
					case MouseEvent.MOUSE_RELEASED -> MOUSE_STATES[((MouseEvent) e).getButton()] = false;
				}
				if(MOUSE_EVENT_REQUESTS.get() > 0) {
					LATEST_MOUSE_EVENT = (MouseEvent) e;
					MOUSE_EVENT_REQUESTS.decrementAndGet();
					MOUSE_EVENT_REQUESTS.notify();
				} else {
					//todo create input event
				}
			}
		}, eventMask);*/
		//init input devices
		PLAYER_DEVICES.addAll(Arrays.asList(ControllerEnvironment.getDefaultEnvironment().getControllers()));
		/*{
			Controller keyboard = null;
			for(Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
				if(controller.getType() == Type.KEYBOARD) {
					keyboard = controller;
					break;
				}
			}
			if(keyboard != null) {
				PLAYER_DEVICES.add(keyboard);//okay, let me explain some stuff here. For key events, we are still relying on awt - it turned out to be a lot more reliable. It is however queried and added to the list. Why, you might ask. It is for the sake making keyboard input optional - you should have the option to turn it off at any time, by mapping a controller to the first player. This would be harder to do otherwise. The only drawback of awt is that you can't use multiple keyboards for players - well, that's a price we have to pay. If that becomes necessary, we will switch to jinput, but
			}
			for(Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
				if(controller.getType() == Type.GAMEPAD) {
					PLAYER_DEVICES.add(controller);
				}
			}
		}*/
		ControllerEnvironment.getDefaultEnvironment().addControllerListener(new ControllerListener() {
			@Override
			public void controllerRemoved(ControllerEvent ev) {
				synchronized(DEVICE_LOCK) {
					PLAYER_DEVICES.remove(ev.getController());
				}
			}
			
			@Override
			public void controllerAdded(ControllerEvent ev) {
				synchronized(DEVICE_LOCK) {
					ev.getController().poll();
					if(ev.getController().getType() == Type.GAMEPAD) {
						PLAYER_DEVICES.add(ev.getController());
					} else if(ev.getController().getType() == Type.KEYBOARD) {
						if(PLAYER_DEVICES.isEmpty() || PLAYER_DEVICES.get(0).getType() != Type.KEYBOARD) {
							PLAYER_DEVICES.add(0, ev.getController());
						}
					}
				}
			}
		});
		
		LOGGER.info("Initialized input manager");
	}
	
	public static ArrayList<InputAction<Event>> getActions() {
		synchronized(DEVICE_LOCK) {
			var a = new ArrayList<>(ACTIONS);
			ACTIONS.clear();
			return a;
		}
	}
	
	public static void pollAsync() {
		if(Main.isRunning()) {
			Main.getExecutors().execute(() -> {
				synchronized(DEVICE_LOCK) {
					for(int i = 0; i < PLAYER_DEVICES.size(); i++) {
						Controller controller = PLAYER_DEVICES.get(i);
						if(!controller.poll()) {
							PLAYER_DEVICES.remove(i);
							i--;
							continue;
						}
						Event event = new Event();
						while(controller.getEventQueue().getNextEvent(event)) {
							Instant time = Instant.ofEpochMilli(event.getNanos() / 1000);
							float value = event.getComponent().getPollData();
							event.getComponent().getDeadZone();
							ActionType type = ActionType.getAction(controller, event);
							if(type != null) {
								InputAction<Event> inputAction = new InputAction<>(type, time, value, i, event);
								ACTIONS.add(inputAction);
								System.out.println("Parsed action " + type);
								//System.out.println(event + " " + "| dead zone: " + event.getComponent().getDeadZone());
							}
						}
					}
				}
			});
		}
	}
	
/*	public static KeyEvent getNextKeyEvent() {
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
	}*/
	
	
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
/*	public static boolean isKeyPressed(int key) {
		return KEY_STATES.getOrDefault(key, false);
	}*/
	
	/**
	 * Checks if the specified mouse button is held down.
	 *
	 * @param button The mouse button
	 * @return True if being pressed
	 */
/*	public static boolean isMouseButtonPressed(int button) {
		if(MOUSE_STATES.length > button && button >= 0) {
			return MOUSE_STATES[button];
		}
		return false;
	}*/
	
	public static enum ActionType {
		FOCUS_UP("Move up the focus", new ValueControlConfig(0.25f, "Hat Switch", Type.GAMEPAD)),
		FOCUS_DOWN("Move down the focus", new ValueControlConfig(0.5f, "Hat Switch", Type.GAMEPAD), new ValueControlConfig(1, "Button 7", Type.GAMEPAD)),
		FOCUS_LEFT("Move left the focus", new ValueControlConfig(1.0f, "Hat Switch", Type.GAMEPAD), new ValueControlConfig(1, "Button 6", Type.GAMEPAD)),
		FOCUS_RIGHT("Move right the focus", new ValueControlConfig(0.75f, "Hat Switch", Type.GAMEPAD)),
		JUMP_LEFT("Jump left", new ValueControlConfig(1, "Q", Type.KEYBOARD)),//todo controller input
		JUMP_RIGHT("Jump right", new ValueControlConfig(1, "E", Type.KEYBOARD)),
		MOVE_UP("Up", new ValueControlConfig(1, "W", Type.KEYBOARD), new ValueControlConfig(1, "UP", Type.KEYBOARD), new ValueControlConfig(-1, "Y Axis", Type.GAMEPAD)),
		MOVE_DOWN("Down", new ValueControlConfig(1, "S", Type.KEYBOARD), new ValueControlConfig(1, "DOWN", Type.KEYBOARD), new ValueControlConfig(1, "Y Axis", Type.GAMEPAD)),
		MOVE_LEFT("Left", new ValueControlConfig(1, "A", Type.KEYBOARD), new ValueControlConfig(1, "LEFT", Type.KEYBOARD), new ValueControlConfig(-1, "X Axis", Type.GAMEPAD)),
		MOVE_RIGHT("Right", new ValueControlConfig(1, "D", Type.KEYBOARD), new ValueControlConfig(1, "RIGHT", Type.KEYBOARD), new ValueControlConfig(1, "X Axis", Type.GAMEPAD)),
		ATTACK("Attack", new ValueControlConfig(1, "P", Type.KEYBOARD), new ValueControlConfig(1, "Button 1", Type.GAMEPAD)),
		SPECIAL("Special", new ValueControlConfig(1, "0", Type.KEYBOARD), new ValueControlConfig(1, "Button 2", Type.GAMEPAD)),
		GRAB("Grab", new ValueControlConfig(1, "U", Type.KEYBOARD), new ValueControlConfig(1, "Button 0", Type.GAMEPAD)),
		TAUNT("Taunt", new ValueControlConfig(1, "K", Type.KEYBOARD), new ValueControlConfig(1, "Button 3", Type.GAMEPAD)),
		PAUSE("Pause", new ValueControlConfig(1, "BACK", Type.KEYBOARD)),
		WALK("Walk", new ValueControlConfig(1, "L", Type.KEYBOARD), new ValueControlConfig(1, "Button 8", Type.GAMEPAD)),
		SHIELD("Shield", new ValueControlConfig(1, "I", Type.KEYBOARD), new ValueControlConfig(0.5f, 1, "Button 9", Type.GAMEPAD)),
		LEFT_CLICK("Left click", new ValueControlConfig(1, "Left", Type.MOUSE), new ValueControlConfig(1, "Button 8", Type.GAMEPAD), new ValueControlConfig(1, "Button 9", Type.GAMEPAD)),
		RIGHT_CLICK("Right click", new ValueControlConfig(1, "Left", Type.MOUSE));
		public static final ActionType[] values = values();
		public final String name;
		public final ControlConfig[] controls;
		
		private ActionType(String name, ControlConfig... controls) {
			this.name = name;
			this.controls = controls;
		}
		
		public static ActionType getAction(Controller controller, Event event) {
			for(ActionType type : values) {
				if(type.matches(controller, event)) {
					return type;
				}
			}
			return null;
		}
		
		public boolean matches(Controller controller, Event event) {
			for(ControlConfig control : controls) {
				if(control.isValid(controller, event)) {
					return true;
				}
			}
			return false;
		}
		
		public static interface ControlConfig {
			public boolean isValid(Controller controller, Event event);
			
			public static class ValueControlConfig implements ControlConfig {
				protected float min, max;
				protected String component;
				protected List<Controller.Type> types;
				
				public ValueControlConfig(float min, float max, String component, Type... types) {
					this.types = List.of(types);
					this.min = min;
					this.max = max;
					this.component = component;
				}
				
				public ValueControlConfig(float value, String component, Type... types) {
					this(value, value, component, types);
				}
				
				@Override
				public boolean isValid(Controller controller, Event event) {
					boolean contains = types.contains(controller.getType());
					if(Math.abs(event.getValue()) - event.getComponent().getDeadZone() < 0) {
						return false;
					}
					if(contains) {
						return (event.getValue() >= min && event.getValue() <= max) && (component == null || component.equalsIgnoreCase(event.getComponent().getName()));
					}
					return false;
				}
				
				public float getMax() {
					return max;
				}
				
				public void setMax(float max) {
					this.max = max;
				}
				
				public String getComponent() {
					return component;
				}
				
				public void setComponent(String component) {
					this.component = component;
				}
				
				public float getMin() {
					return min;
				}
				
				public void setMin(float min) {
					this.min = min;
				}
				
				public List<Type> getTypes() {
					return types;
				}
			}
		}
	}
	
	public static class InputAction<T> {
		protected final ActionType action;
		protected final Instant time;
		protected final Point location;
		protected final float value;
		protected final int playerId;
		protected final T event;
		
		public InputAction(ActionType type, int playerId, T event) {
			this(type, Instant.now(), playerId, event);
		}
		
		public InputAction(ActionType type, Instant time, int playerId, T event) {
			this(type, time, null, 0, playerId, event);
		}
		
		public InputAction(ActionType type, Point location, int playerId, T event) {
			this(type, Instant.now(), location, 0, playerId, event);
		}
		
		public InputAction(ActionType type, Instant time, Point location, int playerId, T event) {
			this(type, time, location, 0, playerId, event);
		}
		
		public InputAction(ActionType type, float value, int playerId, T event) {
			this(type, Instant.now(), null, value, playerId, event);
		}
		
		public InputAction(ActionType type, Instant time, float value, int playerId, T event) {
			this(type, time, null, value, playerId, event);
		}
		
		public InputAction(ActionType type, Instant time, Point location, float value, int playerId, T event) {
			this.action = type;
			this.time = time;
			this.location = location;
			this.value = value;
			this.playerId = playerId;
			this.event = event;
		}
		
		public ActionType getActionType() {
			return action;
		}
		
		public double getValue() {
			return value;
		}
		
		public Instant getEventTime() {
			return time;
		}
		
		public Point getLocation() {
			return location;
		}
		
		public int getPlayerId() {
			return playerId;
		}
		
		public T getData() {
			return event;
		}
		
		@Override
		public String toString() {
			return "InputAction{" +
			       "action=" + action +
			       ", time=" + time +
			       ", location=" + location +
			       ", value=" + value +
			       ", playerId=" + playerId +
			       ", event=" + event +
			       '}';
		}
	}
}
