/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.engine.GameEngine;
import dartproductions.mcleodmassacre.graphics.Animation;
import dartproductions.mcleodmassacre.graphics.RenderingLayer;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Point;

/**
 * I knew this was a bad idea the moment I thought of it. Anyway suckers, guess what we'll be using here.
 * <br>
 * Welcome to the game where the engine and graphics are inseparable by design and separated in the implementation.
 *
 * @since 0.1.0
 */
public interface Entity {
	/**
	 * Increases this entity's velocity.
	 *
	 * @param accX The change along the x axis
	 * @param accY The change along the y axis
	 * @since 0.1.0
	 */
	default void accelerate(int accX, int accY) {
		getVelocity().width += accX;
		getVelocity().height += accY;
	}
	
	/**
	 * Gets the currently shown animation of the entity.
	 *
	 * @return The animation
	 * @since 0.1.0
	 */
	@NotNull Animation getCurrentAnimation();
	
	/**
	 * Gets the entity's deceleration on the X axis. This value is subtracted from the speed every frame.
	 *
	 * @return The horizontal deceleration
	 * @since 0.1.0
	 */
	default int getDecelerationX() {
		return 0;
	}
	
	/**
	 * Gets the entity's deceleration on the Y axis. This value is subtracted from the speed every frame.
	 *
	 * @return The vertical deceleration
	 * @since 0.1.0
	 */
	default int getDecelerationY() {
		return 0;
	}
	
	/**
	 * Gets the layer where this entity is rendered by default.
	 *
	 * @return The rendering layer
	 * @since 0.1.0
	 */
	@NotNull RenderingLayer getDefaultLayer();
	
	/**
	 * Gets the entity's gravity. This value is used to increase vertical speed at every frame.
	 *
	 * @return The entity's gravity
	 * @since 0.1.0
	 */
	default int getGravity() {
		return 0;
	}
	
	/**
	 * Gets the location of this entity.
	 *
	 * @return The location
	 * @since 0.1.0
	 */
	@NotNull Point getLocation();
	
	/**
	 * Gets the square of the maximum horizontal speed of this entity. If the value is negative, the limit is ignored.
	 *
	 * @return The maximum horizontal speed
	 * @since 0.1.0
	 */
	default int getMaxSpeedX() {
		return isMovable() ? -1 : 0;
	}
	
	/**
	 * Gets the square of the maximum vertical speed of this entity. If the value is negative, the limit is ignored.
	 *
	 * @return The maximum vertical speed
	 * @since 0.1.0
	 */
	default int getMaxSpeedY() {
		return isMovable() ? -1 : 0;
	}
	
	/**
	 * Gets the velocity of this entity.
	 *
	 * @return The velocity
	 * @since 0.1.0
	 */
	default @NotNull Dimension getVelocity() {
		return new Dimension(0, 0);
	}
	
	/**
	 * Checks if this entity can collide with other entities.
	 *
	 * @return True if can collide
	 * @since 0.1.0
	 */
	default boolean hasCollision() {
		return false;
	}
	
	/**
	 * Checks if this entity has mouse collision. Only entities with mouse collision can be hovered or pressed.
	 *
	 * @return True if has mouse collision
	 * @since 0.1.0
	 */
	default boolean hasMouseCollision() {
		return true;
	}
	
	/**
	 * Checks if this entity can be moved by collisions with other entities.
	 *
	 * @return True if can be moved
	 * @since 0.1.0
	 */
	default boolean isCollisionMovable() {
		return hasCollision();
	}
	
	/**
	 * Checks if this entity is hovered by the mouse.
	 *
	 * @return True if hovered
	 * @since 0.1.0
	 */
	boolean isHovered();
	
	/**
	 * Checks if this entity can move.
	 *
	 * @return True if can move
	 * @since 0.1.0
	 */
	default boolean isMovable() {
		return false;
	}
	
	/**
	 * Checks if this entity can be selected.
	 *
	 * @return True if can be selected
	 * @since 0.1.0
	 */
	default boolean isSelectable() {
		return false;
	}
	
	/**
	 * Checks if this entity is selected.
	 *
	 * @return True if selected
	 * @since 0.1.0
	 */
	boolean isSelected();
	
	/**
	 * Moves this entity by the specified distance.
	 *
	 * @param moveByX The distance on the x axis
	 * @param moveByY The distance on the y axis
	 * @since 0.1.0
	 */
	default void move(int moveByX, int moveByY) {
		getLocation().translate(moveByX, moveByY);
	}
	
	/**
	 * Handles the entity's custom movements. It is recommended to call this method from the {@link #process()} method if it is implemented.
	 *
	 * @since 0.1.0
	 */
	default void move() {
		{
			int maxX = getMaxSpeedX();
			int maxY = getMaxSpeedY();
			Dimension vel = getVelocity();
			
			accelerate(0, getGravity());//gravity
			
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
	 * Handles the entity's custom collision behaviour. It is called right before the game engine handles the collision, and can be used to tell the engine not to process the collision.
	 *
	 * @param e The entity this entity is colliding with
	 * @return True if the engine should process this collision, false otherwise
	 * @since 0.1.0
	 */
	default boolean onCollision(@NotNull Entity e) {
		return true;
	}
	
	/**
	 * Runs whenever this entity is hovered. Doesn't run if the entity was hovered in the previous frame.
	 *
	 * @since 0.1.0
	 */
	default void onHover() {
	}
	
	/**
	 * Runs whenever this entity is no longer hovered.
	 *
	 * @since 0.1.0
	 */
	default void onHoverStop() {
	}
	
	/**
	 * Runs whenever the mouse has pressed this entity.
	 *
	 * @since 0.1.0
	 */
	default void onMousePress() {
	}
	
	/**
	 * Runs when the mouse is no longer pressing this entity.
	 *
	 * @since 0.1.0
	 */
	default void onMouseRelease() {
	}
	
	/**
	 * Runs whenever this entity is selected (for example, by clicking on it with a mouse).
	 *
	 * @since 0.1.0
	 */
	default void onSelect() {
	}
	
	/**
	 * Runs whenever this entity gets unselected.
	 *
	 * @since 0.1.0
	 */
	default void onUnselect() {
	}
	
	/**
	 * Handles the entity's own actions in every frame.
	 *
	 * @since 0.1.0
	 */
	default void process() {
		if(isMovable()) {
			move();
		}
		if(getCurrentAnimation().isOver()) {
			unregister();
		}
	}
	
	/**
	 * Registers this entity in the game engine and in the graphics.
	 *
	 * @since 0.1.0
	 */
	default void register() {
		getDefaultLayer().add(this);
		GameEngine.registerEntity(this);
	}
	
	/**
	 * Removes this entity from the game engine and from the graphics.
	 *
	 * @since 0.1.0
	 */
	default void unregister() {
		getDefaultLayer().remove(this);
		GameEngine.unregisterEntity(this);
	}
	
}
