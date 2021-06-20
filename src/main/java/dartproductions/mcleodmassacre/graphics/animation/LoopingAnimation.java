/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.graphics.animation;

import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;

/**
 * An animation that resets every time it is over, creating a loop.
 *
 * @since 0.1.0
 */
public class LoopingAnimation extends StandardAnimation {
	/**
	 * Creates a new animation with the given name and no offset.
	 *
	 * @param name The name of the animation, used for finding images and hitboxes
	 * @since 0.1.0
	 */
	public LoopingAnimation(@NotNull String name) {
		this(name, new Dimension(0, 0));
	}
	
	/**
	 * Creates a new animation with the given name and offset.
	 *
	 * @param name   The name of the animation, used for finding images and hitboxes
	 * @param offset The offset of the animation
	 * @since 0.1.0
	 */
	public LoopingAnimation(@NotNull String name, @NotNull Dimension offset) {
		super(name, offset);
	}
	
	@Override
	public void next() {
		super.next();
		if(isOver()) {
			frame = 0;
		}
	}
}
