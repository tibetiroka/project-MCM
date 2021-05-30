package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.engine.GameEngine;
import dartproductions.mcleodmassacre.graphics.Animation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * An entity representing a player-like character (a player or a bot). Isn't interactive, but can move, collide, and can be moved by collisions.
 */
public interface PlayerEntity extends Entity {//todo
	
	@Override
	public @NotNull Animation getCurrentAnimation();
	
	/**
	 * Moves this player to the left.
	 */
	public default void moveLeft() {
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
	 */
	public default void moveRight() {
		if(hasState(PlayerState.STANDING) || hasState(PlayerState.FALLING)) {
			if(getMovementDelay() <= 0) {
				setFacingLeft(false);
				accelerate(getMovementSpeed(), 0);
				setMovementDelay(getMovementDelay());
			}
		}
	}
	
	/**
	 * Activates this player's shield
	 */
	public void shield();
	
	/**
	 * Moves this player upwards
	 */
	public default void moveUp() {
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
	 * Moves this player downwards
	 */
	public default void moveDown() {
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
	 * Makes this player attack
	 */
	public void attack();
	
	/**
	 * Activates the player's special ability
	 */
	public void special();
	
	/**
	 * Makes the player use its grab ability
	 */
	public void grab();
	
	/**
	 * Activates the player's taunt ability
	 */
	public void taunt();
	
	/**
	 * Makes this player walk in the direction it is facing.
	 */
	public default void walk() {
		if(isFacingLeft()) {
			moveLeft();
		} else {
			moveRight();
		}
	}
	
	/**
	 * Gets the horizontal movement speed of the player.
	 *
	 * @return The movement speed
	 */
	public int getMovementSpeed();
	
	/**
	 * Gets the vertical movement speed of the player.
	 *
	 * @return The jump speed
	 */
	public int getJumpSpeed();
	
	/**
	 * Gets the current states of the player.
	 *
	 * @return The states
	 */
	public @NotNull ArrayList<PlayerState> getStates();
	
	/**
	 * Sets whether the player has the specified state.
	 *
	 * @param state The state
	 * @param value True if the player should have it
	 */
	public void setState(@NotNull PlayerState state, boolean value);
	
	/**
	 * Checks if the player has the specified state.
	 *
	 * @param state The state to check
	 * @return True if the player has it
	 */
	public default boolean hasState(@Nullable PlayerState state) {
		return getStates().contains(state);
	}
	
	/**
	 * Gets the delay before the player can change its movement. The player can move if this value is not positive.
	 *
	 * @return The movement delay
	 */
	public int getMovementDelay();
	
	/**
	 * Sets the delay before the player can move again.
	 *
	 * @param delay The new movement delay
	 */
	public void setMovementDelay(int delay);
	
	/**
	 * Gets the delay before the player can jump again. The player can jump if this value is not positive.
	 *
	 * @return The jump delay
	 */
	public int getJumpDelay();
	
	/**
	 * Sets the delay before the player can jump again.
	 *
	 * @param delay The new jump delay
	 */
	public void setJumpDelay(int delay);
	
	/**
	 * Gets the delay before the player can attack again. The player can attack if this value is not positive.
	 *
	 * @return The attack delay
	 */
	public int getAttackDelay();
	
	/**
	 * Sets the delay before the player can attack again.
	 *
	 * @param delay The new attack delay
	 */
	public void setAttackDelay(int delay);
	
	/**
	 * Gets the delay before the player can use its special ability again. The player can uts it if this value is not positive.
	 *
	 * @return The special delay
	 */
	public int getSpecialDelay();
	
	/**
	 * Sets the delay before the player can use its special ability again.
	 *
	 * @param delay The new special ability delay
	 */
	public void setSpecialDelay(int delay);
	
	/**
	 * Gets the delay before the player can grab again. The player can grab if this value is not positive.
	 *
	 * @return The grab delay
	 */
	public int getGrabDelay();
	
	/**
	 * Sets the delay before the player can grab again.
	 *
	 * @param delay The new grab delay
	 */
	public void setGrabDelay(int delay);
	
	/**
	 * Gets the delay before the player can taunt again. The player can taunt if this value is not positive.
	 *
	 * @return The taunt delay
	 */
	public int getTauntDelay();
	
	/**
	 * Sets the delay before the player can taunt again.
	 *
	 * @param delay The new taunt delay
	 */
	public void setTauntDelay(int delay);
	
	/**
	 * Checks if the player is facing towards the left side of the map.
	 *
	 * @return True if left, false if right
	 */
	public boolean isFacingLeft();
	
	/**
	 * Sets whether the player is facing towards the left or the right of the map.
	 *
	 * @param isLeft True if left, false if right
	 */
	public void setFacingLeft(boolean isLeft);
	
	/**
	 * Gets the maximum (default) health of this player.
	 *
	 * @return The max health
	 */
	public int getMaxHealth();
	
	/**
	 * Gets the current health of this player.
	 *
	 * @return The current health
	 */
	public int getHealth();
	
	/**
	 * Sets the current health of the player.
	 *
	 * @param i The player's new health
	 */
	public void setHealth(int i);
	
	/**
	 * Deals damage to the player.
	 *
	 * @param i The amount of health to lose
	 */
	public default void damage(int i) {
		setHealth(getHealth() - i);
	}
	
	/**
	 * Sets whether this entity can be moved by collisions
	 *
	 * @param value True if the entity can be moved; false otherwise
	 */
	public void setCollisionMovable(boolean value);
	
	@Override
	public default boolean onCollision(Entity e) {
		return getVelocity().height <= 0;
	}
	
	/**
	 * The possible states of a player
	 */
	public static enum PlayerState {
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
