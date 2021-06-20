/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.graphics.animation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Area;
import java.util.UUID;

/**
 * Animations are used for describing an entity's hitbox and visual properties. They are updated every frame.
 *
 * @since 0.1.0
 */
public interface Animation extends Cloneable {
	
	@Nullable Animation clone();
	
	/**
	 * Gets the name of this animation. The returned value doesn't have to match the base name for the images or hitboxes used.
	 *
	 * @return The name of this animation
	 * @since 0.1.0
	 */
	@NotNull String getAnimationName();
	
	/**
	 * Paints the animation's graphics using the specified {@link Graphics2D} instance.
	 *
	 * @param graphics       The graphics for painting
	 * @param entityLocation The location of the entity this animation belongs to
	 * @since 0.1.0
	 */
	void paint(@NotNull Graphics2D graphics, @NotNull Point entityLocation);
	//@NotNull Image getCurrentFrame();
	
	/**
	 * Gets the current hitbox of this animation.
	 *
	 * @return The hitbox
	 * @since 0.1.0
	 */
	@Nullable Area getCurrentHitbox();
	
	/**
	 * Gets the unique id of this animation.
	 *
	 * @return The animation's id
	 * @since 0.1.0
	 */
	@NotNull UUID getId();
	
	/**
	 * Gets the length of the animation. The length is the amount of frames an animation can show without being reset.
	 *
	 * @return The length
	 * @since 0.1.0
	 */
	int getLength();
	
	/**
	 * Gets the offset of this animation. This is the distance between any 'p' point on the screen (for example, an entity's location which has this animation) and the point where this animation should be drawn.
	 *
	 * @return The offset of the animation
	 * @since 0.1.0
	 */
	@NotNull Dimension getOffset();
	
	/**
	 * Checks if this animation is over. An animation is over if it can't use its {@link #next()} method safely.
	 *
	 * @return True if over
	 * @since 0.1.0
	 */
	boolean isOver();
	
	/**
	 * Changes the animation to show its next frame and the corresponding hitboxes.
	 *
	 * @since 0.1.0
	 */
	void next();
	
	/**
	 * Resets this animation. The animation must return to its first frame.
	 *
	 * @since 0.1.0
	 */
	void reset();
	
}
