package dartproductions.mcleodmassacre.engine;

import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.Main.GameState;
import dartproductions.mcleodmassacre.ResourceManager;
import dartproductions.mcleodmassacre.entity.Background;
import dartproductions.mcleodmassacre.entity.Entity;
import dartproductions.mcleodmassacre.entity.PlayerEntity;
import dartproductions.mcleodmassacre.graphics.Animation.LoopingAnimation;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.input.InputManager;
import dartproductions.mcleodmassacre.input.InputManager.ActionType;
import dartproductions.mcleodmassacre.input.InputManager.InputAction;
import net.java.games.input.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class GameEngine {
	public static final Object ENGINE_LOCK = new Object(), ENGINE_LOCK_2 = new Object();
	protected static final Logger LOGGER = LogManager.getLogger(GameEngine.class);
	protected static final ArrayList<Entity> ENTITIES = new ArrayList<>();
	protected static final ArrayList<Entity> MOVABLE_ENTITIES = new ArrayList<>();
	protected static final ArrayList<Entity> COLLIDABLE_ENTITIES = new ArrayList<>();
	protected static final ArrayList<Entity> ENTITIES_TO_REMOVE = new ArrayList<>();
	protected static final ArrayList<Entity> ENTITIES_TO_ADD = new ArrayList<>();
	protected static final ArrayList<PlayerEntity> PLAYERS = new ArrayList<>();
	private static final int frameLength = 20;
	public static Thread ENGINE_THREAD;
	private static boolean RUNNING = false;
	private static long previous = 0, delta = 0;
	private static boolean firstStart = false;
	
	public static void start() {
		if(ENGINE_THREAD != null && ENGINE_THREAD.isAlive()) {
			LOGGER.error("Attempted to start engine thread while the thread was still running");
			return;
		}
		ENGINE_THREAD = new Thread(() -> {
			RUNNING = true;
			LOGGER.info("Started game engine thread");
			previous = Instant.now().toEpochMilli();
			while(isRunning() && Main.isRunning()) {
				while(delta >= frameLength) {
					delta -= frameLength;
					processFrame();
				}
				synchronized(GraphicsManager.GRAPHICS_LOCK) {
					GraphicsManager.GRAPHICS_LOCK.notifyAll();
				}
				try {
					synchronized(ENGINE_LOCK) {
						ENGINE_LOCK.wait(100);
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
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if(Main.isRunning() && GameEngine.isRunning()) {
					synchronized(ENGINE_LOCK) {
						ENGINE_LOCK.notifyAll();
					}
				} else {
					cancel();
				}
			}
		}, 20, 1);
		//javax.management.timer.Timer
		ENGINE_THREAD.setUncaughtExceptionHandler((t, e) -> LOGGER.error("Uncaught exception in the main engine thread", e));
		if(firstStart) {
			firstStart = false;
			ResourceManager.createAnimations();
		}
		ENGINE_THREAD.start();
	}
	
	private static void processFrame() {
		handleEntities();
		handleInput();
		synchronized(ENGINE_LOCK) {
			for(Entity entity : ENTITIES) {
				entity.process();
			}
			for(Entity first : COLLIDABLE_ENTITIES) {
				for(Entity second : COLLIDABLE_ENTITIES) {
					if(second != first && (second.isCollisionMovable() ^ first.isCollisionMovable()) && areIntersecting(first, second)) {
						//collision with entities that care about it - there is no need for two movable entities
						Entity moving, staying;
						if(first.isCollisionMovable() && !second.isCollisionMovable()) {
							moving = first;
							staying = second;
						} else if(!first.isCollisionMovable() && second.isCollisionMovable()) {
							moving = second;
							staying = second;
						} else {
							return;
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
			
			//todo engine stuff
		}
	}
	
	private static void handleEntities() {
		synchronized(ENGINE_LOCK_2) {
			for(Entity e : ENTITIES_TO_REMOVE) {
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
			for(Entity e : ENTITIES_TO_ADD) {
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
		}
	}
	
	private static PlayerEntity getPlayer(int id) {
		if(id < 0 || PLAYERS.size() <= id) {
			return null;
		}
		return PLAYERS.get(id);
	}
	
	private static void handleInput() {
		ArrayList<InputAction<Event>> actions = InputManager.getActions();//get events
		for(InputAction<Event> action : actions) {
			if(action.getActionType() == ActionType.PAUSE) {
				Main.setGameState(GameState.IN_GAME_PAUSED, GameState.IN_GAME);
				continue;
			}
			PlayerEntity player = getPlayer(action.getPlayerId());
			if(player == null) {
				continue;
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
		InputManager.pollAsync();//start the next input query
	}
	
	public static boolean isRunning() {
		return RUNNING;
	}
	
	public static synchronized void onStateChange(GameState newGameState, GameState newNextState) {
		synchronized(ENGINE_LOCK_2) {//todo
			if(newGameState == GameState.IN_GAME_PAUSED) {
			} else {
				ENTITIES.forEach(Entity::unregister);
				switch(newGameState) {
					case MAIN_MENU -> {
						new Background(new LoopingAnimation("menu"), new Point(0, 0)).register();
					}
				}
			}
		}
	}
	
	public static void unregisterEntity(Entity e) {
		synchronized(ENGINE_LOCK_2) {
			ENTITIES_TO_REMOVE.add(e);
		}
	}
	
	public static void registerEntity(Entity e) {
		synchronized(ENGINE_LOCK_2) {
			ENTITIES_TO_ADD.add(e);
		}
	}
	
	private static boolean areIntersecting(Entity first, Entity second) {
		Area a = second.getCurrentAnimation().getCurrentHitboxArea().createTransformedArea(AffineTransform.getTranslateInstance(first.getLocation().x + first.getCurrentAnimation().getOffset().width, first.getLocation().y + first.getCurrentAnimation().getOffset().height));
		a.intersect((first.getCurrentAnimation().getCurrentHitboxArea().createTransformedArea(AffineTransform.getTranslateInstance(second.getLocation().x + second.getCurrentAnimation().getOffset().width, second.getLocation().y + second.getCurrentAnimation().getOffset().height))));
		return !a.isEmpty();
	}
	
	private static Area getIntersection(Entity first, Entity second) {
		Area a = second.getCurrentAnimation().getCurrentHitboxArea().createTransformedArea(AffineTransform.getTranslateInstance(first.getLocation().x + first.getCurrentAnimation().getOffset().width, first.getLocation().y + first.getCurrentAnimation().getOffset().height));
		a.intersect((first.getCurrentAnimation().getCurrentHitboxArea().createTransformedArea(AffineTransform.getTranslateInstance(second.getLocation().x + second.getCurrentAnimation().getOffset().width, second.getLocation().y + second.getCurrentAnimation().getOffset().height))));
		return a;
	}
}
