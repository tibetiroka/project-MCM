/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.engine;

import dartproductions.mcleodmassacre.GameState;
import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.entity.Entity;
import dartproductions.mcleodmassacre.entity.PlayerEntity;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.graphics.RenderingLayer;
import dartproductions.mcleodmassacre.input.InputManager;
import dartproductions.mcleodmassacre.input.InputManager.ActionType;
import dartproductions.mcleodmassacre.input.InputManager.InputAction;
import dartproductions.mcleodmassacre.resources.ResourceManager;
import dartproductions.mcleodmassacre.sound.SoundManager;
import dartproductions.mcleodmassacre.util.Pair.ImmutablePair.ImmutableNullsafePair;
import net.java.games.input.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class handling most of the stuff related to game mechanics and physics. Also responsible for scheduling user input and graphics updates.
 *
 * @since 0.1.0
 */
public class GameEngine {
	/**
	 * Lock object for the engine's scheduled tasks.
	 *
	 * @since 0.1.0
	 */
	public static final @NotNull Object ENGINE_TASK_LOCK = new Object();
	/**
	 * Lock object for suspending the engine. Used in timing frames and pausing.
	 *
	 * @since 0.1.0
	 */
	public static final @NotNull Object ENGINE_WAIT_LOCK = new Object();
	/**
	 * Lock object for entity-related operations. Used for avoiding {@link java.util.ConcurrentModificationException} in entity-related lists.
	 *
	 * @since 0.1.0
	 */
	public static final @NotNull Object ENTITY_LOCK = new Object();
	/**
	 * The delay between two frames (in milliseconds).
	 *
	 * @since 0.1.0
	 */
	public static final int FRAME_LENGTH = 30;
	/**
	 * The delay between two frames (in nanoseconds)
	 *
	 * @since 0.1.0
	 */
	public static final int FRAME_LENGTH_NANO = FRAME_LENGTH * 1000 * 1000;
	/**
	 * List of all entities that can collide.
	 *
	 * @since 0.1.0
	 */
	protected static final @NotNull ArrayList<Entity> COLLIDABLE_ENTITIES = new ArrayList<>();
	/**
	 * List of delayed engine tasks
	 *
	 * @since 0.1.0
	 */
	protected static final @NotNull ArrayList<ImmutableNullsafePair<Long, Runnable>> DELAYED_TASKS = new ArrayList<>();
	/**
	 * List of all existing entities
	 *
	 * @since 0.1.0
	 */
	protected static final @NotNull ArrayList<Entity> ENTITIES = new ArrayList<>();
	/**
	 * List of entities to be added in the next frame
	 *
	 * @since 0.1.0
	 */
	protected static final @NotNull ArrayList<Entity> ENTITIES_TO_ADD = new ArrayList<>();
	/**
	 * List of entities to be removed in the next frame
	 *
	 * @since 0.1.0
	 */
	protected static final @NotNull ArrayList<Entity> ENTITIES_TO_REMOVE = new ArrayList<>();
	/**
	 * Logger for the engine
	 *
	 * @since 0.1.0
	 */
	protected static final Logger LOGGER = LogManager.getLogger(GameEngine.class);
	/**
	 * List of all entities that can move. Being part of the list does not mean the entity can be moved by collisions, or that it will move at any time.
	 *
	 * @since 0.1.0
	 */
	protected static final @NotNull ArrayList<Entity> MOVABLE_ENTITIES = new ArrayList<>();
	/**
	 * List of player entities
	 *
	 * @since 0.1.0
	 */
	protected static final @NotNull ArrayList<PlayerEntity> PLAYERS = new ArrayList<>();
	/**
	 * The main engine thread
	 *
	 * @since 0.1.0
	 */
	public static @Nullable Thread ENGINE_THREAD;
	/**
	 * The state of the game engine
	 *
	 * @since 0.1.0
	 */
	private static volatile boolean RUNNING = false;
	/**
	 * Time since the previous frame
	 *
	 * @since 0.1.0
	 */
	private static volatile long delta = 0;
	/**
	 * The current 'frame' of the engine. The {@link #processFrame()} method is called exactly once in every frame.
	 *
	 * @since 0.1.0
	 */
	private static long frame = 0;
	/**
	 * The time when the previous frame started
	 *
	 * @since 0.1.0
	 */
	private static volatile long previous = 0;
	
	/**
	 * Checks if the engine is still running. Might return 'true' if the thread didn't shut down correctly.
	 *
	 * @return True if running
	 * @since 0.1.0
	 */
	public static boolean isRunning() {
		return RUNNING;
	}
	
	/**
	 * Registers an entity in the engine. The entity does NOT get registered in the rendering engine.
	 *
	 * @param e The entity to register
	 * @since 0.1.0
	 */
	public static void registerEntity(@NotNull Entity e) {
		synchronized(ENTITY_LOCK) {
			ENTITIES_TO_ADD.add(e);
		}
	}
	
	/**
	 * Schedules a task for execution on the main engine thread.
	 *
	 * @param delay The amount of frames to wait before execution
	 * @param task  The task to execute
	 * @since 0.1.0
	 */
	public static void scheduleTask(int delay, @NotNull Runnable task) {
		synchronized(ENGINE_TASK_LOCK) {
			DELAYED_TASKS.add(new ImmutableNullsafePair<>(delay + frame, task));
		}
	}
	
	/**
	 * Starts the game engine. Fails silently if the engine is already running.
	 *
	 * @since 0.1.0
	 */
	public static void start() {
		if(ENGINE_THREAD != null && ENGINE_THREAD.isAlive()) {//check if engine is running
			LOGGER.error("Attempted to start engine thread while the thread was still running");
			return;
		}
		ENGINE_THREAD = new Thread(() -> {//create engine thread
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 3);
			RUNNING = true;
			LOGGER.info("Started game engine thread");
			previous = System.nanoTime();
			while(isRunning() && Main.isRunning()) {
				if(delta > FRAME_LENGTH_NANO * 30) {
					LOGGER.warn("Massive lag spike: " + delta + " ns (" + Math.round(delta / (double) FRAME_LENGTH_NANO * 100) / 100.0 + " frames)");
				}
				while(delta >= FRAME_LENGTH_NANO) {
					delta -= FRAME_LENGTH_NANO;
					frame++;
					processFrame();
					if(frame % 100 == 0) {
						ResourceManager.unloadAll(0.9);
					}
				}
				synchronized(GraphicsManager.GRAPHICS_LOCK) {
					GraphicsManager.GRAPHICS_LOCK.notifyAll();
				}
				synchronized(SoundManager.AUDIO_LOCK) {
					SoundManager.AUDIO_LOCK.notifyAll();
				}
				while(Main.isRunning()) {
					Thread.onSpinWait();
					if(GraphicsManager.WINDOW != null && !GraphicsManager.WINDOW.isActive() && Main.isRunning()) {
						SoundManager.pause();
						ResourceManager.unloadAll(0);
						while(GraphicsManager.WINDOW != null && !GraphicsManager.WINDOW.isActive() && Main.isRunning()) {
							try {
								Thread.sleep(100);
							} catch(InterruptedException e) {
								LOGGER.warn("Interrupted wait for window focus in engine", e);
							}
							previous = System.nanoTime();
							delta = 0;
						}
						SoundManager.resume();
					}
					long l = System.nanoTime();
					if(l - previous + delta > FRAME_LENGTH_NANO) {
						delta += l - previous;
						previous = l;
						break;
					}
					try {//trying to decrease busy-wait, but can't do much - same issue with any other timer
						Thread.sleep(5);
					} catch(InterruptedException e) {
						LOGGER.warn("Interrupted sleep in engine", e);
					}
				}
			}
			synchronized(GraphicsManager.GRAPHICS_LOCK) {
				GraphicsManager.GRAPHICS_LOCK.notifyAll();
			}
			SoundManager.stopAll();
			SoundManager.clear();
			LOGGER.info("Engine thread shut down normally");
			
		}, "Engine");
		ENGINE_THREAD.setUncaughtExceptionHandler((t, e) -> {
			LOGGER.error("Uncaught exception in the main engine thread (" + t.getName() + ")", e);
			Main.panic();
		});
		ENGINE_THREAD.start();
	}
	
	public static void unregisterAllEntities() {
		synchronized(ENTITY_LOCK) {
			ENTITIES_TO_REMOVE.addAll(ENTITIES);
		}
	}
	
	/**
	 * Removes an entity from the engine. The entity does NOT get removed from the rendering engine.
	 *
	 * @param e The entity to remove
	 * @since 0.1.0
	 */
	public static void unregisterEntity(@NotNull Entity e) {
		synchronized(ENTITY_LOCK) {
			ENTITIES_TO_REMOVE.add(e);
		}
	}
	
	/**
	 * Checks if two entities are intersecting with each other.
	 *
	 * @param first  The first entity
	 * @param second The second entity
	 * @return True if they are intersecting
	 * @since 0.1.0
	 */
	private static boolean areIntersecting(@NotNull Entity first, @NotNull Entity second) {
		Area a = second.getCurrentAnimation().getCurrentHitbox().createTransformedArea(AffineTransform.getTranslateInstance(first.getLocation().x + first.getCurrentAnimation().getOffset().width, first.getLocation().y + first.getCurrentAnimation().getOffset().height));
		a.intersect((first.getCurrentAnimation().getCurrentHitbox().createTransformedArea(AffineTransform.getTranslateInstance(second.getLocation().x + second.getCurrentAnimation().getOffset().width, second.getLocation().y + second.getCurrentAnimation().getOffset().height))));
		return !a.isEmpty();
	}
	
	/**
	 * Gets the intersection area of the entities.
	 *
	 * @param first  The first entity
	 * @param second The second entity
	 * @return Their intersection
	 * @since 0.1.0
	 */
	private static Area getIntersection(@NotNull Entity first, @NotNull Entity second) {
		Area a = second.getCurrentAnimation().getCurrentHitbox().createTransformedArea(AffineTransform.getTranslateInstance(first.getLocation().x + first.getCurrentAnimation().getOffset().width, first.getLocation().y + first.getCurrentAnimation().getOffset().height));
		a.intersect((first.getCurrentAnimation().getCurrentHitbox().createTransformedArea(AffineTransform.getTranslateInstance(second.getLocation().x + second.getCurrentAnimation().getOffset().width, second.getLocation().y + second.getCurrentAnimation().getOffset().height))));
		return a;
	}
	
	/**
	 * Gets the specified player from the players.
	 *
	 * @param id The id of the player
	 * @return The player or null
	 * @since 0.1.0
	 */
	private static @Nullable PlayerEntity getPlayer(int id) {
		synchronized(ENTITY_LOCK) {
			if(id < 0 || PLAYERS.size() <= id) {
				return null;
			}
			return PLAYERS.get(id);
		}
	}
	
	/**
	 * Handles all entity removals and additions to the engine.
	 *
	 * @since 0.1.0
	 */
	private static void handleEntities() {
		if(Main.getGameState().isPausingState()) {
			return;
		}
		synchronized(ENTITY_LOCK) {
			for(Entity e : ENTITIES_TO_REMOVE) {//removing entities
				ENTITIES.remove(e);
				if(e.hasCollision()) {
					COLLIDABLE_ENTITIES.remove(e);
				}
				if(e.isMovable()) {
					MOVABLE_ENTITIES.remove(e);
				}
				if(e instanceof PlayerEntity en) {
					PLAYERS.add(en);
				}
			}
			ENTITIES_TO_REMOVE.clear();
			for(Entity e : ENTITIES_TO_ADD) {//adding entities
				ENTITIES.add(e);
				if(e.hasCollision()) {
					COLLIDABLE_ENTITIES.add(e);
				}
				if(e.isMovable()) {
					MOVABLE_ENTITIES.add(e);
				}
				if(e instanceof PlayerEntity && PLAYERS.contains(e)) {
					PLAYERS.set(PLAYERS.indexOf(e), null);
					//auto-clear the player list when no players remain
					boolean has = false;
					for(PlayerEntity p : PLAYERS) {
						if(p != null) {
							has = true;
							break;
						}
					}
					if(!has) {
						PLAYERS.clear();
					}
				}
			}
			ENTITIES_TO_ADD.clear();
		}
	}
	
	/**
	 * Handles input actions queried by {@link InputManager}.
	 *
	 * @since 0.1.0
	 */
	private static void handleInput() {
		ArrayList<InputAction<Event>> actions = InputManager.getActions();//get events
		actionLoop:
		for(InputAction<Event> action : actions) {
			if(action.getActionType() == ActionType.PAUSE) {//pause
				if(Main.getGameState() == GameState.IN_GAME) {
					Main.setGameState(GameState.IN_GAME_PAUSED, GameState.IN_GAME);
				}
				continue actionLoop;
			}
			switch(action.getActionType()) {//mouse events
				case LEFT_BUTTON_PRESS -> {
					Point location = action.getLocation();
					for(int i = GraphicsManager.LAYER_TOP; i >= 0; i--) {
						RenderingLayer layer = GraphicsManager.getLayer(i);
						Entity entity = layer.getEntity(location);
						if(entity != null) {
							entity.onSelect();
							entity.onMousePress();
							break;
						}
					}
					continue actionLoop;
				}
				case LEFT_BUTTON_RELEASE -> {
					Point location = action.getLocation();
					for(int i = GraphicsManager.LAYER_TOP; i >= 0; i--) {
						Entity entity = GraphicsManager.getLayer(i).getEntity(location);
						if(entity != null) {
							entity.onSelect();
							entity.onMouseRelease();
							break;
						}
					}
					continue actionLoop;
				}
			}
			if(Main.getGameState() == GameState.IN_GAME) {
				PlayerEntity player = getPlayer(action.getPlayerId());//player-related events
				if(player == null) {
					continue actionLoop;
				}
				switch(action.getActionType()) {
					case GRAB -> player.grab();
					case WALK -> player.walk();
					case TAUNT -> player.taunt();
					case ATTACK -> player.attack();
					case SHIELD -> player.shield();
					case MOVE_UP -> player.moveUp();
					case MOVE_DOWN -> player.moveDown();
					case MOVE_LEFT -> player.moveLeft();
					case MOVE_RIGHT -> player.moveRight();
					case SPECIAL -> player.special();
					case JUMP_LEFT -> {
						player.moveUp();
						player.moveLeft();
					}
					case JUMP_RIGHT -> {
						player.moveUp();
						player.moveRight();
					}
				}
			}
		}
		InputManager.pollAsync();//start the next input query (async, runs on InputManager's own thread)
	}
	
	/**
	 * Runs all tasks scheduled for this frame. Makes sure all tasks have their delays adjusted if necessary.
	 *
	 * @since 0.1.0
	 */
	private static void handleTasks() {
		synchronized(ENGINE_TASK_LOCK) {
			for(Iterator<ImmutableNullsafePair<Long, Runnable>> iterator = DELAYED_TASKS.iterator(); iterator.hasNext(); ) {
				ImmutableNullsafePair<Long, Runnable> delayedTask = iterator.next();
				if(delayedTask.first() <= frame) {
					delayedTask.second().run();
				}
				iterator.remove();
			}
		}
	}
	
	/**
	 * Method for doing all calculations in a frame.
	 *
	 * @since 0.1.0
	 */
	private static void processFrame() {
		handleTasks();
		handleEntities();
		handleInput();
		if(Main.getGameState().isPausingState()) {
			return;
		}
		synchronized(ENGINE_WAIT_LOCK) {
			for(Entity entity : ENTITIES) {//self-processing (movement etc)
				entity.process();
			}
			handleEntities();//allows removing both BEFORE and AFTER processing, without staying in the engine for a frame
			for(Entity first : COLLIDABLE_ENTITIES) {//collision
				for(Entity second : COLLIDABLE_ENTITIES) {
					if(second != first && (second.isCollisionMovable() ^ first.isCollisionMovable()) && areIntersecting(first, second)) {
						Entity moving, staying;
						if(first.isCollisionMovable()) {
							moving = first;
							staying = second;
						} else {
							moving = second;
							staying = first;
						}
						{
							Dimension vel = moving.getVelocity();
							moving.move(-vel.width, -vel.height);
							if(areIntersecting(moving, staying)) {//ignore collisions from previous frames
								moving.move(vel.width, vel.height);
								continue;
							}
							moving.move(vel.width, vel.height);
						}
						if(moving.onCollision(staying) && staying.onCollision(moving)) {
							Dimension d = moving.getVelocity();
							double delta = Math.sqrt(d.height * d.height + d.width * d.width);
							double dx = d.width / delta;
							double dy = d.height / delta;
							int x = moving.getLocation().x;
							int y = moving.getLocation().y;
							for(int i = 1; areIntersecting(moving, staying); i++) {
								moving.getLocation().x = x + (int) (dx * i);
								moving.getLocation().y = y + (int) (dy * i);
							}
						}
					}
				}
			}
		}
	}
}
