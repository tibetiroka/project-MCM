package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.engine.GameEngine;
import dartproductions.mcleodmassacre.graphics.Animation;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.graphics.RenderingLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * An entity representing a player-like character (a player or a bot). Isn't interactive, but can move, collide, and can be moved by collisions.
 *
 * @since 0.1.0
 */
public interface PlayerEntity extends Entity {//todo
	
	/**
	 * Makes this player attack
	 *
	 * @since 0.1.0
	 */
	void attack();
	
	/**
	 * Deals damage to the player.
	 *
	 * @param i The amount of health to lose
	 * @since 0.1.0
	 */
	default void damage(int i) {
		setHealth(getHealth() - i);
	}
	
	/**
	 * Gets the delay before the player can attack again. The player can attack if this value is not positive.
	 *
	 * @return The attack delay
	 * @since 0.1.0
	 */
	int getAttackDelay();
	
	/**
	 * Sets the delay before the player can attack again.
	 *
	 * @param delay The new attack delay
	 * @since 0.1.0
	 */
	void setAttackDelay(int delay);
	
	@Override
	@NotNull Animation.MirrorableAnimation getCurrentAnimation();
	
	@Override
	@NotNull
	default RenderingLayer getDefaultLayer() {
		return GraphicsManager.getLayer(GraphicsManager.LAYER_CHARACTERS);
	}
	
	@Override
	default boolean onCollision(@NotNull Entity e) {
		return getVelocity().height <= 0;
	}
	
	/**
	 * Gets the delay before the player can grab again. The player can grab if this value is not positive.
	 *
	 * @return The grab delay
	 * @since 0.1.0
	 */
	int getGrabDelay();
	
	/**
	 * Sets the delay before the player can grab again.
	 *
	 * @param delay The new grab delay
	 * @since 0.1.0
	 */
	void setGrabDelay(int delay);
	
	/**
	 * Gets the current health of this player.
	 *
	 * @return The current health
	 * @since 0.1.0
	 */
	int getHealth();
	
	/**
	 * Sets the current health of the player.
	 *
	 * @param i The player's new health
	 * @since 0.1.0
	 */
	void setHealth(int i);
	
	/**
	 * Gets the delay before the player can jump again. The player can jump if this value is not positive.
	 *
	 * @return The jump delay
	 * @since 0.1.0
	 */
	int getJumpDelay();
	
	/**
	 * Sets the delay before the player can jump again.
	 *
	 * @param delay The new jump delay
	 * @since 0.1.0
	 */
	void setJumpDelay(int delay);
	
	/**
	 * Gets the vertical movement speed of the player.
	 *
	 * @return The jump speed
	 * @since 0.1.0
	 */
	int getJumpSpeed();
	
	/**
	 * Gets the maximum (default) health of this player.
	 *
	 * @return The max health
	 * @since 0.1.0
	 */
	int getMaxHealth();
	
	/**
	 * Gets the delay before the player can change its movement. The player can move if this value is not positive.
	 *
	 * @return The movement delay
	 * @since 0.1.0
	 */
	int getMovementDelay();
	
	/**
	 * Sets the delay before the player can move again.
	 *
	 * @param delay The new movement delay
	 * @since 0.1.0
	 */
	void setMovementDelay(int delay);
	
	/**
	 * Gets the horizontal movement speed of the player.
	 *
	 * @return The movement speed
	 * @since 0.1.0
	 */
	int getMovementSpeed();
	
	/**
	 * Gets the delay before the player can use its special ability again. The player can uts it if this value is not positive.
	 *
	 * @return The special delay
	 * @since 0.1.0
	 */
	int getSpecialDelay();
	
	/**
	 * Sets the delay before the player can use its special ability again.
	 *
	 * @param delay The new special ability delay
	 * @since 0.1.0
	 */
	void setSpecialDelay(int delay);
	
	/**
	 * Gets the current states of the player.
	 *
	 * @return The states
	 * @since 0.1.0
	 */
	@NotNull ArrayList<PlayerState> getStates();
	
	/**
	 * Gets the delay before the player can taunt again. The player can taunt if this value is not positive.
	 *
	 * @return The taunt delay
	 * @since 0.1.0
	 */
	int getTauntDelay();
	
	/**
	 * Sets the delay before the player can taunt again.
	 *
	 * @param delay The new taunt delay
	 * @since 0.1.0
	 */
	void setTauntDelay(int delay);
	
	/**
	 * Makes the player use its grab ability
	 *
	 * @since 0.1.0
	 */
	void grab();
	
	/**
	 * Checks if the player has the specified state.
	 *
	 * @param state The state to check
	 * @return True if the player has it
	 * @since 0.1.0
	 */
	default boolean hasState(@Nullable PlayerState state) {
		return getStates().contains(state);
	}
	
	/**
	 * Checks if the player is facing towards the left side of the map.
	 *
	 * @return True if left, false if right
	 * @since 0.1.0
	 */
	boolean isFacingLeft();
	
	/**
	 * Sets whether the player is facing towards the left or the right of the map.
	 *
	 * @param isLeft True if left, false if right
	 * @since 0.1.0
	 */
	void setFacingLeft(boolean isLeft);
	
	/**
	 * Moves this player downwards
	 *
	 * @since 0.1.0
	 */
	default void moveDown() {
		if(hasState(PlayerState.STANDING)) {
			accelerate(0, getJumpSpeed());
			setCollisionMovable(false);
			PlayerEntity e = this;
			GameEngine.scheduleTask(5, () -> e.setCollisionMovable(true));
			setState(PlayerState.STANDING, false);
			setState(PlayerState.FALLING, true);
		}
	}
	
	/**
	 * Moves this player to the left.
	 *
	 * @since 0.1.0
	 */
	default void moveLeft() {
		if(hasState(PlayerState.STANDING) || hasState(PlayerState.FALLING)) {
			if(getMovementDelay() <= 0) {
				setFacingLeft(true);
				accelerate(-getMovementSpeed(), 0);
				setMovementDelay(getMovementDelay());
			}
		}
	}
	
	/**
	 * Moves this player to the right
	 *
	 * @since 0.1.0
	 */
	default void moveRight() {
		if(hasState(PlayerState.STANDING) || hasState(PlayerState.FALLING)) {
			if(getMovementDelay() <= 0) {
				setFacingLeft(false);
				accelerate(getMovementSpeed(), 0);
				setMovementDelay(getMovementDelay());
			}
		}
	}
	
	/**
	 * Moves this player upwards
	 *
	 * @since 0.1.0
	 */
	default void moveUp() {
		if(hasState(PlayerState.STANDING) || (hasState(PlayerState.FALLING) && !hasState(PlayerState.VOID_JUMPED))) {
			if(getJumpDelay() <= 0) {
				accelerate(0, -getJumpSpeed());
				setJumpDelay(getJumpDelay());
				if(hasState(PlayerState.FALLING)) {
					setState(PlayerState.VOID_JUMPED, true);
				}
				setState(PlayerState.FALLING, true);
				setState(PlayerState.STANDING, false);
			}
		}
	}
	
	/**
	 * Sets whether this entity can be moved by collisions
	 *
	 * @param value True if the entity can be moved; false otherwise
	 * @since 0.1.0
	 */
	void setCollisionMovable(boolean value);
	
	/**
	 * Sets whether the player has the specified state.
	 *
	 * @param state The state
	 * @param value True if the player should have it
	 * @since 0.1.0
	 */
	void setState(@NotNull PlayerState state, boolean value);
	
	/**
	 * Activates this player's shield
	 *
	 * @since 0.1.0
	 */
	void shield();
	
	/**
	 * Activates the player's special ability
	 *
	 * @since 0.1.0
	 */
	void special();
	
	/**
	 * Activates the player's taunt ability
	 *
	 * @since 0.1.0
	 */
	void taunt();
	
	/**
	 * Makes this player walk in the direction it is facing.
	 *
	 * @since 0.1.0
	 */
	default void walk() {
		if(isFacingLeft()) {
			moveLeft();
		} else {
			moveRight();
		}
	}
	
	/**
	 * The possible states of a player
	 */
	enum PlayerState {
		/**
		 * Indicates a standing player. A standing player is actively colliding with ground below it, and has no special animations showing (e.g. not being grabbed).
		 */
		STANDING,
		/**
		 * Indicates a falling player. A falling player has no ground below it, and has no special animations showing (e.g. not being grabbed).
		 */
		FALLING,
		/**
		 * Indicates a shielded player. Shielded players cannot move.
		 */
		SHIELDED,
		/**
		 * Indicates a player that has its attack animation showing.
		 */
		ATTACKING,
		/**
		 * Indicates a player that has its special ability's animation showing
		 */
		SPECIAL,
		/**
		 * Indicates a player that is grabbing an other player
		 */
		GRABBING,
		/**
		 * Indicates a dead player
		 */
		DEAD,
		/**
		 * Indicates a falling player that has jumped mid-air. This state should only be set when {@link #FALLING} is also set.
		 */
		VOID_JUMPED
	}
}
