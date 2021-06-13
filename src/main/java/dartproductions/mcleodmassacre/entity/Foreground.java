/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.graphics.Animation;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.graphics.RenderingLayer;
import org.jetbrains.annotations.NotNull;

import java.awt.Point;

/**
 * Same as the background but appears on high priority rendering layers ({@link GraphicsManager#LAYER_GUI}).
 *
 * @since 0.1.0
 */
public class Foreground extends Background {
	
	/**
	 * Creates a new foreground entity.
	 *
	 * @param animation The animation to show
	 * @param location  The location of the entity
	 * @since 0.1.0
	 */
	public Foreground(@NotNull Animation animation, @NotNull Point location) {
		super(animation, location);
	}
	
	/**
	 * Creates a new foreground entity at the default location.
	 *
	 * @param animation The animation to show
	 * @since 0.1.0
	 */
	public Foreground(@NotNull Animation animation) {
		super(animation);
	}
	
	@Override
	public @NotNull RenderingLayer getDefaultLayer() {
		return GraphicsManager.getLayer(GraphicsManager.LAYER_GUI);
	}
}
