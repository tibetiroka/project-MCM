package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.graphics.Animation.MirrorableAnimation;

public interface PlayerEntity extends Entity {//todo
	
	@Override
	public MirrorableAnimation getCurrentAnimation();
	
	/**
	 * Moves this player to the left.
	 */
	public default void moveLeft() {
	
	}
	
	/**
	 * Moves this player to the right
	 */
	public void moveRight();
	
	/**
	 * Activates this player's shield
	 */
	public void shield();
	
	/**
	 * Moves this player upwards
	 */
	public void moveUp();
	
	/**
	 * Moves this player downwards
	 */
	public void moveDown();
	
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
	 * Gets the current states of the player.
	 *
	 * @return The states
	 */
	public PlayerState[] getState();
	
	/**
	 * Gets the delay before the player can change its movement. The player can move if this value is not positive.
	 *
	 * @return The movement delay
	 */
	public int getMovementDelay();
	
	/**
	 * Gets the delay before the player can jump again. The player can jump if this value is not positive.
	 *
	 * @return The jump delay
	 */
	public int getJumpDelay();
	
	/**
	 * Gets the delay before the player can attack again. The player can attack if this value is not positive.
	 *
	 * @return The attack delay
	 */
	public int getAttackDelay();
	
	/**
	 * Gets the delay before the player can use its special ability again. The player can uts it if this value is not positive.
	 *
	 * @return The special delay
	 */
	public int getSpecialDelay();
	
	/**
	 * Gets the delay before the player can grab again. The player can grab if this value is not positive.
	 *
	 * @return The grab delay
	 */
	public int getGrabDelay();
	
	/**
	 * Gets the delay before the player can taunt again. The player can taunt if this value is not positive.
	 *
	 * @return The taunt delay
	 */
	public int getTauntDelay();
	
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
	 * The possible states of a player
	 */
	public static enum PlayerState {
		STANDING, FALLING, SHIELDED, ATTACKING, SPECIAL, GRABBING, DEAD
	}
}
