package dartproductions.mcleodmassacre.engine;

import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.Main.GameState;
import dartproductions.mcleodmassacre.entity.Background;
import dartproductions.mcleodmassacre.entity.Button;
import dartproductions.mcleodmassacre.entity.Entity;
import dartproductions.mcleodmassacre.entity.PlayerEntity;
import dartproductions.mcleodmassacre.graphics.Animation.LoopingAnimation;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.graphics.RenderingLayer;
import dartproductions.mcleodmassacre.hitbox.ImageHitbox;
import dartproductions.mcleodmassacre.input.InputManager;
import dartproductions.mcleodmassacre.input.InputManager.ActionType;
import dartproductions.mcleodmassacre.input.InputManager.InputAction;
import dartproductions.mcleodmassacre.util.Pair;
import net.java.games.input.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class handling most of the stuff related to game mechanics and physics. Also responsible for scheduling user input and graphics updates.
 */
public class GameEngine {
	/**
	 * Lock object for suspending the engine. Used in timing frames and pausing.
	 */
	public static final Object ENGINE_WAIT_LOCK = new Object();
	/**
	 * Lock object for entity-related operations. Used for avoiding {@link java.util.ConcurrentModificationException} in entity-related lists.
	 */
	public static final Object ENTITY_LOCK = new Object();
	/**
	 * Lock object for the engine's scheduled tasks.
	 */
	public static final Object ENGINE_TASK_LOCK = new Object();
	/**
	 * The delay between two frames (in milliseconds).
	 */
	public static final int FRAME_LENGTH = 20;
	/**
	 * Logger for the engine
	 */
	protected static final Logger LOGGER = LogManager.getLogger(GameEngine.class);
	/**
	 * List of all existing entities
	 */
	protected static final ArrayList<Entity> ENTITIES = new ArrayList<>();
	/**
	 * List of all entities that can move. Being part of the list does not mean the entity can be moved by collisions, or that it will move at any time.
	 */
	protected static final ArrayList<Entity> MOVABLE_ENTITIES = new ArrayList<>();
	/**
	 * List of all entities that can collide.
	 */
	protected static final ArrayList<Entity> COLLIDABLE_ENTITIES = new ArrayList<>();
	/**
	 * List of entities to be removed in the next frame
	 */
	protected static final ArrayList<Entity> ENTITIES_TO_REMOVE = new ArrayList<>();
	/**
	 * List of entities to be added in the next frame
	 */
	protected static final ArrayList<Entity> ENTITIES_TO_ADD = new ArrayList<>();
	/**
	 * List of player entities
	 */
	protected static final ArrayList<PlayerEntity> PLAYERS = new ArrayList<>();
	/**
	 * List of delayed engine tasks
	 */
	protected static final ArrayList<Pair<Integer, Runnable>> DELAYED_TASKS = new ArrayList<>();
	/**
	 * The main engine thread
	 */
	public static @Nullable Thread ENGINE_THREAD;
	/**
	 * The state of the game engine
	 */
	private static volatile boolean RUNNING = false;
	/**
	 * The time when the previous frame started
	 */
	private static volatile long previous = 0;
	/**
	 * Time since the previous frame
	 */
	private static volatile long delta = 0;
	
	/**
	 * Starts the game engine. Fails silently if the engine is already running.
	 */
	public static void start() {
		if(ENGINE_THREAD != null && ENGINE_THREAD.isAlive()) {//check if engine is running
			LOGGER.error("Attempted to start engine thread while the thread was still running");
			return;
		}
		ENGINE_THREAD = new Thread(() -> {//create engine thread
			RUNNING = true;
			ImageHitbox.waitForProcessing();
			LOGGER.info("Started game engine thread");
			previous = Instant.now().toEpochMilli();
			while(isRunning() && Main.isRunning()) {
				while(delta >= FRAME_LENGTH) {
					delta -= FRAME_LENGTH;
					processFrame();
				}
				synchronized(GraphicsManager.GRAPHICS_LOCK) {
					GraphicsManager.GRAPHICS_LOCK.notifyAll();
				}
				try {
					synchronized(ENGINE_WAIT_LOCK) {
						ENGINE_WAIT_LOCK.wait(100);
					}
					long l = Instant.now().toEpochMilli();
					delta += (l - previous);
					previous = l;
				} catch(InterruptedException e) {
					LOGGER.error("Interrupted wait in engine thread", e);
				}
			}
			
			LOGGER.info("Engine thread shut down normally");
			
		}, "Main Engine Thread");
		
		//engine timer for scheduling frames
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if(Main.isRunning() && GameEngine.isRunning()) {
					if(GraphicsManager.WINDOW.isActive()) {
						synchronized(ENGINE_WAIT_LOCK) {//run frame
							ENGINE_WAIT_LOCK.notifyAll();
						}
					} else {//skip frame without running it later
						long l = Instant.now().toEpochMilli();
						delta += (l - previous);
						previous = l;
						delta = delta % FRAME_LENGTH;
					}
				} else {
					cancel();
				}
			}
		}, 20, 20);
		ENGINE_THREAD.setUncaughtExceptionHandler((t, e) -> LOGGER.error("Uncaught exception in the main engine thread", e));
		ENGINE_THREAD.start();
	}
	
	/**
	 * Method for doing all calculations in a frame.
	 */
	private static void processFrame() {
		handleTasks();
		handleEntities();
		handleInput();
		synchronized(ENGINE_WAIT_LOCK) {
			for(Entity entity : ENTITIES) {//self-processing (movement etc)
				entity.process();
			}
			
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
	
	/**
	 * Runs all tasks scheduled for this frame. Makes sure all tasks have their delays adjusted if necessary.
	 */
	private static void handleTasks() {
		synchronized(ENGINE_TASK_LOCK) {
			for(Iterator<Pair<Integer, Runnable>> iterator = DELAYED_TASKS.iterator(); iterator.hasNext(); ) {
				Pair<Integer, Runnable> delayedTask = iterator.next();
				delayedTask.first--;
				if(delayedTask.first <= 0) {
					delayedTask.second().run();
				}
				iterator.remove();
			}
		}
	}
	
	/**
	 * Schedules a task for execution on the main engine thread.
	 *
	 * @param delay The amount of frames to wait before execution
	 * @param task  The task to execute
	 */
	public static void scheduleTask(int delay, @NotNull Runnable task) {
		synchronized(ENGINE_TASK_LOCK) {
			DELAYED_TASKS.add(new Pair<>(delay, task));
		}
	}
	
	/**
	 * Handles all entity removals and additions to the engine.
	 */
	private static void handleEntities() {
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
	 * Gets the specified player from the players.
	 *
	 * @param id The id of the player
	 * @return The player or null
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
	 * Handles input actions queried by {@link InputManager}.
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
		InputManager.pollAsync();//start the next input query (async, runs on InputManager's own thread)
	}
	
	/**
	 * Checks if the engine is still running. Might return 'true' if the thread didn't shut down correctly.
	 *
	 * @return True if running
	 */
	public static boolean isRunning() {
		return RUNNING;
	}
	
	/**
	 * Handles any changes when the application's state changes.
	 *
	 * @param newGameState The new state of the application
	 * @param newNextState The expected state after the new state
	 */
	public static synchronized void onStateChange(@NotNull GameState newGameState, @Nullable GameState newNextState) {
		synchronized(ENTITY_LOCK) {//todo
			if(newGameState == GameState.IN_GAME_PAUSED) {
			} else {
				ENTITIES.forEach(Entity::unregister);
				switch(newGameState) {
					case MAIN_MENU -> {
						new Background(new LoopingAnimation("even_better_main_menu_2"), new Point(0, 0)).register();
						Button button = new Button(new LoopingAnimation("solo_button"), null, null, null, new Point(63, 147), () -> Main.setGameState(GameState.ROSTER, null));
						button.register();
					}
					case LOADING -> {
						scheduleTask(1, () -> Main.setGameState(newNextState, null));
					}
				}
			}
		}
	}
	
	/**
	 * Removes an entity from the engine. The entity does NOT get removed from the rendering engine.
	 *
	 * @param e The entity to remove
	 */
	public static void unregisterEntity(@NotNull Entity e) {
		synchronized(ENTITY_LOCK) {
			ENTITIES_TO_REMOVE.add(e);
		}
	}
	
	/**
	 * Registers an entity in the engine. The entity does NOT get registered in the rendering engine.
	 *
	 * @param e The entity to register
	 */
	public static void registerEntity(@NotNull Entity e) {
		synchronized(ENTITY_LOCK) {
			ENTITIES_TO_ADD.add(e);
		}
	}
	
	/**
	 * Checks if two entities are intersecting with each other.
	 *
	 * @param first  The first entity
	 * @param second The second entity
	 * @return True if they are intersecting
	 */
	private static boolean areIntersecting(@NotNull Entity first, @NotNull Entity second) {
		Area a = second.getCurrentAnimation().getCurrentHitboxArea().createTransformedArea(AffineTransform.getTranslateInstance(first.getLocation().x + first.getCurrentAnimation().getOffset().width, first.getLocation().y + first.getCurrentAnimation().getOffset().height));
		a.intersect((first.getCurrentAnimation().getCurrentHitboxArea().createTransformedArea(AffineTransform.getTranslateInstance(second.getLocation().x + second.getCurrentAnimation().getOffset().width, second.getLocation().y + second.getCurrentAnimation().getOffset().height))));
		return !a.isEmpty();
	}
	
	/**
	 * Gets the intersection area of the entities.
	 *
	 * @param first  The first entity
	 * @param second The second entity
	 * @return Their intersection
	 */
	private static Area getIntersection(@NotNull Entity first, @NotNull Entity second) {
		Area a = second.getCurrentAnimation().getCurrentHitboxArea().createTransformedArea(AffineTransform.getTranslateInstance(first.getLocation().x + first.getCurrentAnimation().getOffset().width, first.getLocation().y + first.getCurrentAnimation().getOffset().height));
		a.intersect((first.getCurrentAnimation().getCurrentHitboxArea().createTransformedArea(AffineTransform.getTranslateInstance(second.getLocation().x + second.getCurrentAnimation().getOffset().width, second.getLocation().y + second.getCurrentAnimation().getOffset().height))));
		return a;
	}
}
