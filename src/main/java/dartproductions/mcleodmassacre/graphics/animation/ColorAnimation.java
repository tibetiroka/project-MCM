/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.graphics.animation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.UUID;
import java.util.function.IntFunction;

/**
 * An animation where the hitbox of the animation is filled with a color in each frame.
 *
 * @since 0.1.0
 */
public class ColorAnimation implements Animation {
	
	/**
	 * Generator function for getting the hitbox of the animation
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull IntFunction<Area> hitbox;
	
	/**
	 * Generator function for getting the color
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull IntFunction<Color> colorGenerator;
	
	/**
	 * The amount of frames in this animation
	 *
	 * @since 0.1.0
	 */
	protected final int length;
	/**
	 * The name of the animation
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull String name;
	/**
	 * The id of the animation
	 *
	 * @since 0.1.0
	 */
	protected final UUID id = UUID.randomUUID();
	/**
	 * The offset of the animation
	 *
	 * @since 0.1.0
	 */
	protected final Dimension offset;
	/**
	 * True if the animation should loop
	 *
	 * @since 0.1.0
	 */
	protected final boolean looping;
	/**
	 * The current frame of the animation
	 *
	 * @since 0.1.0
	 */
	protected int frame = 0;
	
	public ColorAnimation(@NotNull String name, boolean looping, @NotNull IntFunction<Area> hitbox, @NotNull IntFunction<Color> colorGenerator, int length, @NotNull Dimension offset) {
		this.hitbox = hitbox;
		this.colorGenerator = colorGenerator;
		this.length = length;
		this.name = name;
		this.offset = offset;
		this.looping = looping;
	}
	
	@Override
	public @Nullable Animation clone() {
		try {
			return (Animation) super.clone();
		} catch(CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public @NotNull String getAnimationName() {
		return name;
	}
	
	@Override
	public void paint(@NotNull Graphics2D graphics, @NotNull Point entityLocation) {
		graphics.setColor(colorGenerator.apply(frame));
		graphics.fill(hitbox.apply(frame).createTransformedArea(AffineTransform.getTranslateInstance(offset.width + entityLocation.x, offset.height + entityLocation.y)));
	}
	
	@Override
	public @Nullable Area getCurrentHitbox() {
		return hitbox.apply(frame);
	}
	
	@Override
	public @NotNull UUID getId() {
		return id;
	}
	
	@Override
	public int getLength() {
		return length;
	}
	
	@Override
	public @NotNull Dimension getOffset() {
		return offset;
	}
	
	@Override
	public boolean isOver() {
		return frame >= length;
	}
	
	@Override
	public void next() {
		frame++;
		if(looping && frame >= length) {
			frame = 0;
		}
	}
	
	@Override
	public void reset() {
		frame = 0;
	}
}
