package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.engine.GameEngine;
import dartproductions.mcleodmassacre.graphics.Animation;
import dartproductions.mcleodmassacre.graphics.RenderingLayer;

import java.awt.Dimension;
import java.awt.Point;

/**
 * I knew this was a bad idea the moment I thought of it. Anyway suckers, guess what we'll be using here.
 * <br>
 * Welcome to the game where the engine and graphics are inseparable by design and separated in the implementation.
 */
public interface Entity {
	/**
	 * Gets the currently shown animation of the entity.
	 *
	 * @return The animation
	 */
	public Animation getCurrentAnimation();
	
	/**
	 * Gets the layer where this entity is rendered by default.
	 *
	 * @return The rendering layer
	 */
	public RenderingLayer getDefaultLayer();
	
	/**
	 * Runs whenever this entity is hovered. Doesn't run if the entity was hovered in the previous frame.
	 */
	public default void onHover() {
	}
	
	/**
	 * Runs whenever this entity is no longer hovered.
	 */
	public default void onHoverStop() {
	}
	
	/**
	 * Checks if this entity is hovered by the mouse.
	 *
	 * @return True if hovered
	 */
	public boolean isHovered();
	
	/**
	 * Runs whenever the mouse has pressed this entity.
	 */
	public default void onMousePress() {
	}
	
	/**
	 * Runs when the mouse is no longer pressing this entity.
	 */
	public default void onMouseRelease() {
	}
	
	/**
	 * Checks if this entity has mouse collision. Only entities with mouse collision can be hovered or pressed.
	 *
	 * @return True if has mouse collision
	 */
	public default boolean hasMouseCollision() {
		return true;
	}
	
	/**
	 * Gets the location of this entity.
	 *
	 * @return The location
	 */
	public Point getLocation();
	
	/**
	 * Gets the velocity of this entity.
	 *
	 * @return The velocity
	 */
	public default Dimension getVelocity() {
		return new Dimension(0, 0);
	}
	
	/**
	 * Handles the entity's own actions in every frame.
	 */
	public default void process() {
		if(isMovable()) {
			move();
		}
	}
	
	/**
	 * Checks if this entity can move.
	 *
	 * @return True if can move
	 */
	public default boolean isMovable() {
		return false;
	}
	
	/**
	 * Moves this entity by the specified distance.
	 *
	 * @param moveByX The distance on the x axis
	 * @param moveByY The distance on the y axis
	 */
	public default void move(int moveByX, int moveByY) {
		getLocation().translate(moveByX, moveByY);
	}
	
	/**
	 * Handles the entity's custom movements. It is recommended to call this method from the {@link #process()} method if it is implemented.
	 */
	public default void move() {
		{
			int maxX = getMaxSpeedX();
			int maxY = getMaxSpeedY();
			Dimension vel = getVelocity();
			
			vel.height += getGravity();//gravity
			
			if(vel.width != 0) {//deceleration
				if(vel.width > 0) {
					vel.width -= getDecelerationX();
					if(vel.width < 0) {
						vel.width = 0;
					}
				} else {
					vel.width += getDecelerationX();
					if(vel.width > 0) {
						vel.width = 0;
					}
				}
			}
			if(vel.height != 0) {
				if(vel.height > 0) {
					vel.height -= getDecelerationY();
					if(vel.height < 0) {
						vel.height = 0;
					}
				} else {
					vel.height += getDecelerationY();
					if(vel.height > 0) {
						vel.height = 0;
					}
				}
			}
			
			if(maxX > Math.abs(vel.width)) {//speed limit
				vel.width = (vel.width < 0 ? -1 : 1) * maxX;
			}
			if(maxY > Math.abs(vel.height)) {
				vel.height = (vel.height < 0 ? -1 : 1) * maxY;
			}
		}
		
		getLocation().translate(getVelocity().width, getVelocity().height);//movement
	}
	
	/**
	 * Gets the entity's deceleration on the X axis. This value is subtracted from the speed every frame.
	 *
	 * @return The horizontal deceleration
	 */
	public default int getDecelerationX() {
		return 0;
	}
	
	/**
	 * Gets the entity's deceleration on the Y axis. This value is subtracted from the speed every frame.
	 *
	 * @return The vertical deceleration
	 */
	public default int getDecelerationY() {
		return 0;
	}
	
	/**
	 * Gets the square of the maximum horizontal speed of this entity. If the value is negative, the limit is ignored.
	 *
	 * @return The maximum horizontal speed
	 */
	public default int getMaxSpeedX() {
		return isMovable() ? -1 : 0;
	}
	
	/**
	 * Gets the square of the maximum vertical speed of this entity. If the value is negative, the limit is ignored.
	 *
	 * @return The maximum vertical speed
	 */
	public default int getMaxSpeedY() {
		return isMovable() ? -1 : 0;
	}
	
	/**
	 * Gets the entity's gravity. This value is used to increase vertical speed at every frame.
	 *
	 * @return The entity's gravity
	 */
	public default int getGravity() {
		return 0;
	}
	
	/**
	 * Runs whenever this entity is selected (for example, by clicking on it with a mouse).
	 */
	public default void onSelect() {
	}
	
	/**
	 * Runs whenever this entity gets unselected.
	 */
	public default void onUnselect() {
	}
	
	/**
	 * Checks if this entity can be selected.
	 *
	 * @return True if can be selected
	 */
	public default boolean isSelectable() {
		return false;
	}
	
	/**
	 * Checks if this entity is selected.
	 *
	 * @return True if selected
	 */
	public boolean isSelected();
	
	/**
	 * Checks if this entity can collide with other entities.
	 *
	 * @return True if can collide
	 */
	public default boolean hasCollision() {
		return false;
	}
	
	/**
	 * Checks if this entity can be moved by collisions with other entities.
	 *
	 * @return True if can be moved
	 */
	public default boolean isCollisionMovable() {
		return hasCollision();
	}
	
	/**
	 * Handles the entity's custom collision behaviour. It is called right before the game engine handles the collision, and can be used to tell the engine not to process the collision.
	 *
	 * @param e The entity this entity is colliding with
	 * @return True if the engine should process this collision, false otherwise
	 */
	public default boolean onCollision(Entity e) {
		return true;
	}
	
	/**
	 * Registers this entity in the game engine and in the graphics.
	 */
	public default void register() {
		getDefaultLayer().add(this);
		GameEngine.registerEntity(this);
	}
	
	/**
	 * Removes this entity from the game engine and from the graphics.
	 */
	public default void unregister() {
		getDefaultLayer().remove(this);
		GameEngine.unregisterEntity(this);
	}
	
}
