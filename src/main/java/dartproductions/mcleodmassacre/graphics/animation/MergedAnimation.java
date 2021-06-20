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
import java.util.ArrayList;
import java.util.UUID;

/**
 * An animation that is made of multiple animations, overlaying them on top of each other. The animation is over if any of its animations are over.
 *
 * @since 0.1.0
 */
public class MergedAnimation extends ArrayList<Animation> implements Animation {
	/**
	 * The id of the animation
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull UUID id = UUID.randomUUID();
	/**
	 * The name of the animation
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull String name;
	/**
	 * The offset of the animation from the entity's position
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Dimension offset;
	
	/**
	 * Creates a new animation with the given name and no offset.
	 *
	 * @param name The name of the animation, used for finding images and hitboxes
	 * @since 0.1.0
	 */
	public MergedAnimation(@NotNull String name) {
		this(name, new Dimension(0, 0));
	}
	
	/**
	 * Creates a new animation with the given name and offset.
	 *
	 * @param name   The name of the animation, used for finding images and hitboxes
	 * @param offset The offset of the animation
	 * @since 0.1.0
	 */
	public MergedAnimation(@NotNull String name, @NotNull Dimension offset) {
		this.name = name;
		this.offset = offset;
	}
	
	@Override
	public @NotNull String getAnimationName() {
		return name;
	}
	
	@Override
	public void paint(@NotNull Graphics2D graphics, @NotNull Point entityLocation) {
		forEach(animation -> animation.paint(graphics, entityLocation));
	}
	
	@Override
	public @Nullable Area getCurrentHitbox() {
		return null;
	}
	
	@Override
	public @NotNull UUID getId() {
		return null;
	}
	
	@Override
	public int getLength() {
		return 0;
	}
	
	@Override
	public @NotNull Dimension getOffset() {
		return null;
	}
	
	@Override
	public boolean isOver() {
		return false;
	}
	
	@Override
	public void next() {
	
	}
	
	@Override
	public void reset() {
	
	}
	
	@Override
	public MergedAnimation clone() {
		return (MergedAnimation) super.clone();
	}
}
