/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.graphics.RenderingLayer;
import dartproductions.mcleodmassacre.graphics.animation.Animation;
import org.jetbrains.annotations.NotNull;

import java.awt.Point;

/**
 * Entity implementation for standard background-like features. Doesn't interact with the mouse, can't collide or move. Doesn't react to being hovered/pressed/selected.
 *
 * @since 0.1.0
 */
public class Background implements Entity {
	/**
	 * The animation of this entity
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Animation animation;
	/**
	 * The location of the entity
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Point location;
	
	/**
	 * Creates a new background entity.
	 *
	 * @param animation The animation to show
	 * @param location  The location of the entity
	 * @since 0.1.0
	 */
	public Background(@NotNull Animation animation, @NotNull Point location) {
		this.animation = animation;
		this.location = location;
	}
	
	/**
	 * Creates a new background entity at (0,0).
	 *
	 * @param animation The animation to show
	 * @since 0.1.0
	 */
	public Background(@NotNull Animation animation) {
		this(animation, new Point(0, 0));
	}
	
	@Override
	public @NotNull Animation getCurrentAnimation() {
		return animation;
	}
	
	@Override
	public @NotNull RenderingLayer getDefaultLayer() {
		return GraphicsManager.getLayer(GraphicsManager.LAYER_BACKGROUND);
	}
	
	//oh yes, interaction
	
	@Override
	public @NotNull Point getLocation() {
		return location;
	}
	
	@Override
	public boolean hasMouseCollision() {
		return false;
	}
	
	@Override
	public boolean isHovered() {
		return false;
	}
	
	@Override
	public boolean isSelectable() {
		return false;
	}
	
	@Override
	public boolean isSelected() {
		return false;
	}
	
	@Override
	public void onHover() {
	}
	
	@Override
	public void onHoverStop() {
	}
	
	@Override
	public void onMousePress() {
	}
	
	@Override
	public void onMouseRelease() {
	}
	
	@Override
	public void onSelect() {
	}
	
	@Override
	public void onUnselect() {
	}
	
}
