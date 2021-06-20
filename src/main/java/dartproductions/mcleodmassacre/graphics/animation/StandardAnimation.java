/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.graphics.animation;

import dartproductions.mcleodmassacre.hitbox.ImageHitbox;
import dartproductions.mcleodmassacre.resources.ResourceManager;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.geom.Area;
import java.util.UUID;

/**
 * Animation implementation for simple animations. The images and hitboxes are automatically queried based on the animation's name.
 *
 * @since 0.1.0
 */
public class StandardAnimation implements Animation {
	/**
	 * The images to show
	 *
	 * @see #getCurrentFrame()
	 * @since 0.1.0
	 */
	protected final @NotNull Image[] frames;
	/**
	 * The hitboxes as areas
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Area[] hitboxes;
	/**
	 * The id of the animation
	 *
	 * @see #getId()
	 * @since 0.1.0
	 */
	protected final @NotNull UUID id = UUID.randomUUID();
	/**
	 * The name of the animation
	 *
	 * @see #getAnimationName()
	 * @since 0.1.0
	 */
	protected final @NotNull String name;
	/**
	 * The offset of the animation
	 *
	 * @see #getOffset()
	 * @since 0.1.0
	 */
	protected final @NotNull Dimension offset;
	/**
	 * The index of the current frame
	 *
	 * @see #getLength()
	 * @see #getCurrentFrame()
	 * @see #reset()
	 * @since 0.1.0
	 */
	protected int frame = 0;
	
	
	/**
	 * Creates a new animation with the given name and no offset.
	 *
	 * @param name The name of the animation, used for finding images and hitboxes
	 * @since 0.1.0
	 */
	public StandardAnimation(@NotNull String name) {
		this(name, new Dimension(0, 0));
	}
	
	/**
	 * Creates a new animation with the given name and offset.
	 *
	 * @param name   The name of the animation, used for finding images and hitboxes
	 * @param offset The offset of the animation
	 * @since 0.1.0
	 */
	public StandardAnimation(@NotNull String name, @NotNull Dimension offset) {
		this.name = name;
		this.offset = offset;
		int frameCount = countFrames();
		frames = new Image[frameCount];
		hitboxes = new Area[frameCount];
		fetchFrames();
	}
	
	@Override
	public @Nullable Animation clone() {
		try {
			return (Animation) super.clone();
		} catch(Exception e) {
			return null;
		}
	}
	
	@Override
	public @NotNull String getAnimationName() {
		return name;
	}
	
	@Override
	public @NotNull Image getCurrentFrame() {
		return frames[frame];
	}
	
	@Override
	public @Nullable Area getCurrentHitbox() {
		return hitboxes[frame];
	}
	
	@Override
	public @NotNull UUID getId() {
		return id;
	}
	
	@Override
	public int getLength() {
		return frames.length;
	}
	
	@Override
	public @NotNull Dimension getOffset() {
		return offset;
	}
	
	@Override
	public boolean isOver() {
		return frame >= frames.length;
	}
	
	@Override
	public void next() {
		frame++;
	}
	
	@Override
	public void reset() {
		frame = 0;
	}
	
	/**
	 * Counts the amount of images that can be found with the animation's name.
	 *
	 * @return The amount of frames
	 * @since 0.1.0
	 */
	protected int countFrames() {
		int current = 0;
		while(ResourceManager.getImage(Identifier.fromString(name + "#" + current)) != null) {
			current++;
		}
		return current == 0 ? 1 : current;
	}
	
	/**
	 * Sets the values in the {@link #frames} and {@link #hitboxes} arrays.
	 *
	 * @since 0.1.0
	 */
	protected void fetchFrames() {
		if(frames.length == 1) {
			frames[0] = ResourceManager.getImage(Identifier.fromString(name));
			ImageHitbox hitbox = ResourceManager.getHitbox(Identifier.fromString(name + "/hitbox"));
			hitboxes[0] = hitbox == null ? null : hitbox.getArea();
		} else {
			for(int i = 0; i < frames.length; i++) {
				frames[i] = ResourceManager.getImage(Identifier.fromString(name + "#" + i));
				ImageHitbox hitbox = ResourceManager.getHitbox(Identifier.fromString(name + "#" + i + "/hitbox"));
				hitboxes[i] = hitbox == null ? null : hitbox.getArea();
			}
		}
	}
	
	
}
