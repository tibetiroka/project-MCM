/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.input;

import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.graphics.ResolutionManager;
import dartproductions.mcleodmassacre.input.InputManager.ActionType.ControlConfig.ValueControlConfig;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;
import net.java.games.input.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.MouseInfo;
import java.awt.Point;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for managing user input.
 *
 * @since 0.1.0
 */
public class InputManager {
	private static final @NotNull Logger LOGGER = LogManager.getLogger(InputManager.class);
	//
	private static final @NotNull ArrayList<InputAction<Event>> ACTIONS = new ArrayList<>();
	/**
	 * Lock object used for synchronizations related to the player devices.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Object DEVICE_LOCK = new Object();
	/**
	 * The list of devices used by players. The nth device corresponds to the nth player.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull ArrayList<Controller> PLAYER_DEVICES = new ArrayList<>();
	
	/**
	 * Gets the list of actions that have been detected since this method was last called.
	 *
	 * @return The new actions
	 * @since 0.1.0
	 */
	public static @NotNull ArrayList<InputAction<Event>> getActions() {
		synchronized(DEVICE_LOCK) {
			var a = new ArrayList<>(ACTIONS);
			ACTIONS.clear();
			return a;
		}
	}
	
	/**
	 * Gets the cursor location as it would appear on the (nonexistent) development machine. Useful if you don't want to fuck around with conversions all day.
	 *
	 * @return The cursor location in the original coordinate system
	 * @since 0.1.0
	 */
	public static @NotNull Point getCursorLocation() {
		Point screenLoc = MouseInfo.getPointerInfo().getLocation();
		screenLoc.setLocation(screenLoc.getX() / ResolutionManager.getScreenRatio() - ResolutionManager.getScreenOffsetX(), screenLoc.getY() / ResolutionManager.getScreenRatio() - ResolutionManager.getScreenOffsetY());
		return screenLoc;
	}
	
	/**
	 * Gets the location of the cursor on the graphics buffer.
	 *
	 * @return The cursor's location on the buffer
	 * @since 0.1.0
	 */
	public static @NotNull Point getCursorLocationBuffer() {
		Point loc = getCursorLocationLocal();
		loc.translate(ResolutionManager.getOriginOnBuffer().x, ResolutionManager.getOriginOnBuffer().y);
		return loc;
	}
	
	/**
	 * Gets the cursor location on the local machine.
	 *
	 * @return The cursor's location
	 * @since 0.1.0
	 */
	public static @NotNull Point getCursorLocationLocal() {
		return MouseInfo.getPointerInfo().getLocation();
	}
	
	/**
	 * Initializes the input manager
	 *
	 * @since 0.1.0
	 */
	public static void initialize() {
		//init input devices
		PLAYER_DEVICES.addAll(Arrays.asList(ControllerEnvironment.getDefaultEnvironment().getControllers()));
		
		//listen to controller changes
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
	
	/**
	 * Gathers information from the input devices and parses valid {@link InputAction input actions} from them. Runs asynchronously on {@link Main#getExecutors()}.
	 *
	 * @since 0.1.0
	 */
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
							}
						}
					}
				}
			});
		}
	}
	
	/**
	 * Enum describing all user actions.
	 *
	 * @since 0.1.0
	 */
	public enum ActionType {
		/**
		 * Moves the focus upwards in a menu.
		 *
		 * @since 0.1.0
		 */
		FOCUS_UP("Move up the focus", new ValueControlConfig(0.25f, "Hat Switch", Type.GAMEPAD)),
		/**
		 * Moves the focus downwards in a menu.
		 *
		 * @since 0.1.0
		 */
		FOCUS_DOWN("Move down the focus", new ValueControlConfig(0.5f, "Hat Switch", Type.GAMEPAD), new ValueControlConfig(1, "Button 7", Type.GAMEPAD)),
		/**
		 * Moves the focus to the left in a menu.
		 *
		 * @since 0.1.0
		 */
		FOCUS_LEFT("Move left the focus", new ValueControlConfig(1.0f, "Hat Switch", Type.GAMEPAD), new ValueControlConfig(1, "Button 6", Type.GAMEPAD)),
		/**
		 * Moves the focus to the right in a menu.
		 *
		 * @since 0.1.0
		 */
		FOCUS_RIGHT("Move right the focus", new ValueControlConfig(0.75f, "Hat Switch", Type.GAMEPAD)),
		/**
		 * Makes a player jump and go to the left
		 *
		 * @since 0.1.0
		 */
		JUMP_LEFT("Jump left", new ValueControlConfig(1, "Q", Type.KEYBOARD)),//todo controller input
		/**
		 * Makes a player jump and go to the right
		 *
		 * @since 0.1.0
		 */
		JUMP_RIGHT("Jump right", new ValueControlConfig(1, "E", Type.KEYBOARD)),
		/**
		 * Makes a player jump
		 *
		 * @since 0.1.0
		 */
		MOVE_UP("Up", new ValueControlConfig(1, "W", Type.KEYBOARD), new ValueControlConfig(1, "UP", Type.KEYBOARD), new ValueControlConfig(-1, "Y Axis", Type.GAMEPAD)),
		
		/**
		 * Makes a player move downwards
		 *
		 * @since 0.1.0
		 */
		MOVE_DOWN("Down", new ValueControlConfig(1, "S", Type.KEYBOARD), new ValueControlConfig(1, "DOWN", Type.KEYBOARD), new ValueControlConfig(1, "Y Axis", Type.GAMEPAD)),
		/**
		 * Makes a player move to the left
		 *
		 * @since 0.1.0
		 */
		MOVE_LEFT("Left", new ValueControlConfig(1, "A", Type.KEYBOARD), new ValueControlConfig(1, "LEFT", Type.KEYBOARD), new ValueControlConfig(-1, "X Axis", Type.GAMEPAD)),
		/**
		 * Makes a player move to the right
		 *
		 * @since 0.1.0
		 */
		MOVE_RIGHT("Right", new ValueControlConfig(1, "D", Type.KEYBOARD), new ValueControlConfig(1, "RIGHT", Type.KEYBOARD), new ValueControlConfig(1, "X Axis", Type.GAMEPAD)),
		/**
		 * Makes a player attack
		 *
		 * @since 0.1.0
		 */
		ATTACK("Attack", new ValueControlConfig(1, "P", Type.KEYBOARD), new ValueControlConfig(1, "Button 1", Type.GAMEPAD)),
		/**
		 * Makes a player use its special ability
		 *
		 * @since 0.1.0
		 */
		SPECIAL("Special", new ValueControlConfig(1, "0", Type.KEYBOARD), new ValueControlConfig(1, "Button 2", Type.GAMEPAD)),
		/**
		 * Makes a player use its 'grab' ability (wtf is that btw?)
		 *
		 * @since 0.1.0
		 */
		GRAB("Grab", new ValueControlConfig(1, "U", Type.KEYBOARD), new ValueControlConfig(1, "Button 0", Type.GAMEPAD)),
		/**
		 * Makes a player use its 'taunt' ability
		 *
		 * @since 0.1.0
		 */
		TAUNT("Taunt", new ValueControlConfig(1, "K", Type.KEYBOARD), new ValueControlConfig(1, "Button 3", Type.GAMEPAD)),
		/**
		 * Pauses the game
		 *
		 * @since 0.1.0
		 */
		PAUSE("Pause", new ValueControlConfig(1, "BACK", Type.KEYBOARD)),
		/**
		 * Makes a player walk forward (left or right, depending on the facing)
		 *
		 * @since 0.1.0
		 */
		WALK("Walk", new ValueControlConfig(1, "L", Type.KEYBOARD), new ValueControlConfig(1, "Button 8", Type.GAMEPAD)),
		/**
		 * Makes a player activate its shield
		 *
		 * @since 0.1.0
		 */
		SHIELD("Shield", new ValueControlConfig(1, "I", Type.KEYBOARD), new ValueControlConfig(0.5f, 1, "Button 9", Type.GAMEPAD)),
		/**
		 * Indicates a left mouse button press
		 *
		 * @since 0.1.0
		 */
		LEFT_BUTTON_PRESS("Left click", new ValueControlConfig(1, "Left", Type.MOUSE), new ValueControlConfig(1, "Button 8", Type.GAMEPAD), new ValueControlConfig(1, "Button 9", Type.GAMEPAD)),
		/**
		 * Indicates a left mouse button release
		 *
		 * @since 0.1.0
		 */
		LEFT_BUTTON_RELEASE("Left click", new ValueControlConfig(0, "Left", Type.MOUSE), new ValueControlConfig(0, "Button 8", Type.GAMEPAD), new ValueControlConfig(0, "Button 9", Type.GAMEPAD)),
		/**
		 * Indicates a right mouse button press
		 *
		 * @since 0.1.0
		 */
		RIGHT_BUTTON_PRESS("Right click", new ValueControlConfig(1, "Right", Type.MOUSE)),
		/**
		 * Indicates a right mouse button release
		 *
		 * @since 0.1.0
		 */
		RIGHT_BUTTON_RELEASE("Right click", new ValueControlConfig(0, "Right", Type.MOUSE));
		
		/**
		 * The action types; only exists because {@link #values()} is very poorly optimized
		 *
		 * @since 0.1.0
		 */
		public static final @NotNull ActionType[] values = values();
		/**
		 * The controls for this action
		 *
		 * @since 0.1.0
		 */
		public final @NotNull ControlConfig[] controls;
		/**
		 * The user-friendly name of the action
		 *
		 * @since 0.1.0
		 */
		public final @NotNull String name;
		
		/**
		 * Creates a new action type
		 *
		 * @param name     The name of the action
		 * @param controls The controls to activate the action
		 * @since 0.1.0
		 */
		ActionType(@NotNull String name, @NotNull ControlConfig... controls) {
			this.name = name;
			this.controls = controls;
		}
		
		/**
		 * Gets an action that matches the specified event
		 *
		 * @param controller The controller creating the event
		 * @param event      The event
		 * @return The action or null if not found
		 * @since 0.1.0
		 */
		public static @Nullable ActionType getAction(@NotNull Controller controller, @NotNull Event event) {
			for(ActionType type : values) {
				if(type.matches(controller, event)) {
					return type;
				}
			}
			return null;
		}
		
		/**
		 * Checks if this action matches the specified event.
		 *
		 * @param controller The controller creating the event
		 * @param event      The event
		 * @return True if matches
		 * @since 0.1.0
		 */
		public boolean matches(@NotNull Controller controller, @NotNull Event event) {
			for(ControlConfig control : controls) {
				if(control.isValid(controller, event)) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Configuration for checking if an input event is valid as an action.
		 *
		 * @since 0.1.0
		 */
		public interface ControlConfig {
			/**
			 * Checks if this config matches with the specified event
			 *
			 * @param controller The controller creating the event
			 * @param event      The event
			 * @return True if matches
			 * @since 0.1.0
			 */
			boolean isValid(@NotNull Controller controller, @NotNull Event event);
			
			/**
			 * {@link ControlConfig} implementation where a certain value is expected from a component.
			 *
			 * @since 0.1.0
			 */
			class ValueControlConfig implements ControlConfig {
				/**
				 * The accepted controller types
				 *
				 * @since 0.1.0
				 */
				protected final @NotNull List<Controller.Type> types;
				/**
				 * The name of the input component
				 *
				 * @since 0.1.0
				 */
				protected @NotNull String component;
				/**
				 * The maximum acepted value
				 *
				 * @since 0.1.0
				 */
				protected float max;
				/**
				 * The minimum accepted value
				 *
				 * @since 0.1.0
				 */
				protected float min;
				
				/**
				 * Creates a new config
				 *
				 * @param min       The minimum accepted value
				 * @param max       The maximum accepted value
				 * @param component The input device's component
				 * @param types     The accepted device types
				 * @since 0.1.0
				 */
				public ValueControlConfig(float min, float max, @NotNull String component, @NotNull Type... types) {
					this.types = List.of(types);
					this.min = min;
					this.max = max;
					this.component = component;
				}
				
				/**
				 * Creates a new config accepting a single value
				 *
				 * @param value     The value to accept
				 * @param component The input device's component
				 * @param types     The accepted device types
				 * @since 0.1.0
				 */
				public ValueControlConfig(float value, @NotNull String component, @NotNull Type... types) {
					this(value, value, component, types);
				}
				
				public @NotNull String getComponent() {
					return component;
				}
				
				public void setComponent(@NotNull String component) {
					this.component = component;
				}
				
				public float getMax() {
					return max;
				}
				
				public void setMax(float max) {
					this.max = max;
				}
				
				public float getMin() {
					return min;
				}
				
				public void setMin(float min) {
					this.min = min;
				}
				
				public @NotNull List<Type> getTypes() {
					return types;
				}
				
				@Override
				public boolean isValid(@NotNull Controller controller, @NotNull Event event) {
					boolean contains = types.contains(controller.getType());
					if(Math.abs(event.getValue()) - event.getComponent().getDeadZone() < 0) {
						return false;
					}
					if(contains) {
						return (event.getValue() >= min && event.getValue() <= max) && (component == null || component.equalsIgnoreCase(event.getComponent().getName()));
					}
					return false;
				}
			}
		}
	}
	
	/**
	 * Wrapper for input actions. Contains the type of the action, the time it occurred, the cursor location and the event's data.
	 *
	 * @param <T> The type of event to store
	 * @since 0.1.0
	 */
	public static class InputAction<T> {
		/**
		 * The type of the action
		 *
		 * @since 0.1.0
		 */
		protected final @NotNull ActionType action;
		/**
		 * The event data
		 *
		 * @since 0.1.0
		 */
		protected final T event;
		/**
		 * The cursor location at the time of the event
		 *
		 * @since 0.1.0
		 */
		protected final @NotNull Point location;
		/**
		 * The player's id associated with the input device
		 *
		 * @since 0.1.0
		 */
		protected final int playerId;
		/**
		 * The time the action occurred
		 *
		 * @since 0.1.0
		 */
		protected final @NotNull Instant time;
		/**
		 * The event's input value
		 *
		 * @since 0.1.0
		 */
		protected final float value;
		
		/**
		 * Creates a new input action. Uses the current time for the event's 'occurred at' time, {@link #getCursorLocation()} for the cursor location and 0 for the event's value.
		 *
		 * @param type     The type of the action
		 * @param playerId The id of the player inputting
		 * @param event    The event data
		 * @since 0.1.0
		 */
		public InputAction(@NotNull ActionType type, int playerId, @NotNull T event) {
			this(type, Instant.now(), playerId, event);
		}
		
		/**
		 * Creates a new input action. Uses {@link #getCursorLocation()} for the cursor's location and 0 for the event's value.
		 *
		 * @param type     The type of the action
		 * @param time     The time when it occurred
		 * @param playerId The id of the player inputting
		 * @param event    The event data
		 * @since 0.1.0
		 */
		public InputAction(@NotNull ActionType type, @NotNull Instant time, int playerId, @NotNull T event) {
			this(type, time, getCursorLocation(), 0, playerId, event);
		}
		
		/**
		 * Creates a new input action. Uses the current time as the event's 'occurred at' time, and 0 as the event's value.
		 *
		 * @param type     The type of the action
		 * @param location The cursor location
		 * @param playerId The id of the player inputting
		 * @param event    The event data
		 * @since 0.1.0
		 */
		public InputAction(@NotNull ActionType type, @NotNull Point location, int playerId, @NotNull T event) {
			this(type, Instant.now(), location, 0, playerId, event);
		}
		
		/**
		 * Creates a new input action. Uses 0 as the event's value.
		 *
		 * @param type     The type of the action
		 * @param time     The time when it occurred
		 * @param location The cursor location
		 * @param playerId The id of the player inputting
		 * @param event    The event data
		 * @since 0.1.0
		 */
		public InputAction(@NotNull ActionType type, @NotNull Instant time, @NotNull Point location, int playerId, @NotNull T event) {
			this(type, time, location, 0, playerId, event);
		}
		
		/**
		 * Creates a new input action. Uses the current time as the event's 'occurred at' time, and {@link #getCursorLocation()} for the cursor location.
		 *
		 * @param type     The type of the action
		 * @param value    The event's value
		 * @param playerId The id of the player inputting
		 * @param event    The event data
		 * @since 0.1.0
		 */
		public InputAction(@NotNull ActionType type, float value, int playerId, @NotNull T event) {
			this(type, Instant.now(), getCursorLocation(), value, playerId, event);
		}
		
		/**
		 * Creates a new input action. Uses {@link #getCursorLocation()} as the cursor location.
		 *
		 * @param type     The type of the action
		 * @param time     The time when it occurred
		 * @param value    The event's value
		 * @param playerId The id of the player inputting
		 * @param event    The event data
		 * @since 0.1.0
		 */
		public InputAction(@NotNull ActionType type, @NotNull Instant time, float value, int playerId, @NotNull T event) {
			this(type, time, getCursorLocation(), value, playerId, event);
		}
		
		/**
		 * Creates a new input action
		 *
		 * @param type     The type of the action
		 * @param time     The time when it occurred
		 * @param location The cursor location
		 * @param value    The event's value
		 * @param playerId The id of the player inputting
		 * @param event    The event data
		 * @since 0.1.0
		 */
		public InputAction(@NotNull ActionType type, @NotNull Instant time, @NotNull Point location, float value, int playerId, @NotNull T event) {
			this.action = type;
			this.time = time;
			this.location = location;
			this.value = value;
			this.playerId = playerId;
			this.event = event;
		}
		
		/**
		 * Gets the type of the action this event represents
		 *
		 * @return The action's type
		 * @since 0.1.0
		 */
		public @NotNull ActionType getActionType() {
			return action;
		}
		
		/**
		 * Gets the event data
		 *
		 * @return The data
		 * @since 0.1.0
		 */
		public @NotNull T getData() {
			return event;
		}
		
		/**
		 * Gets the time the event occurred at.
		 *
		 * @return The event's creation time
		 * @since 0.1.0
		 */
		public @NotNull Instant getEventTime() {
			return time;
		}
		
		/**
		 * Gets the cursor's location at the time the event occurred.
		 *
		 * @return The cursor's location
		 * @since 0.1.0
		 */
		public @NotNull Point getLocation() {
			return location;
		}
		
		/**
		 * Gets the ID of the player who inputted the action.
		 *
		 * @return The player's id
		 * @since 0.1.0
		 */
		public int getPlayerId() {
			return playerId;
		}
		
		/**
		 * Gets the value of the event (semi-magic number in e.g. {@link Event#getValue()})
		 *
		 * @return The event's value
		 * @since 0.1.0
		 */
		public double getValue() {
			return value;
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
