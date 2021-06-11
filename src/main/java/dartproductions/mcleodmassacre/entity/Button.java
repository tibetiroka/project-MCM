/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.graphics.Animation;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.graphics.RenderingLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Point;

/**
 * A button entity for triggering user actions with the mouse. Reacts to being pressed/hovered/selected, but can't move or collide. The special animations (selected, pressed, hovered etc.) fall back to less specific ones if they are null.
 *
 * @since 0.1.0
 */
public class Button implements Entity {
	/**
	 * The default animation of the button
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Animation defaultAnimation;
	/**
	 * The location of the button
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Point location;
	/**
	 * The animation shown when the button is hovered
	 *
	 * @since 0.1.0
	 */
	protected final @Nullable Animation onHoverAnimation;
	/**
	 * The animation shown when the button is pressed
	 *
	 * @since 0.1.0
	 */
	protected final @Nullable Animation onPressAnimation;
	/**
	 * The action to run when the mouse is released on the button
	 *
	 * @since 0.1.0
	 */
	protected final @Nullable Runnable onRelease;
	/**
	 * The animation shown when the button is selected
	 *
	 * @since 0.1.0
	 */
	protected final @Nullable Animation onSelectedAnimation;
	/**
	 * The button's hovered state
	 *
	 * @since 0.1.0
	 */
	protected boolean hovered = false;
	/**
	 * The button's pressed state
	 *
	 * @since 0.1.0
	 */
	protected boolean pressed = false;
	/**
	 * The button's selected state
	 *
	 * @since 0.1.0
	 */
	protected boolean selected = false;
	
	/**
	 * Creates a new button entity.
	 *
	 * @param def        The default animation
	 * @param onHover    Animation shown when the button is hovered
	 * @param onPress    Animation shown when the button is pressed
	 * @param onSelected Animation shown when the button is selected
	 * @param location   The location of the button
	 * @param onRelease  The action to run when the mouse is released on the button
	 * @since 0.1.0
	 */
	public Button(@NotNull Animation def, @Nullable Animation onHover, @Nullable Animation onPress, @Nullable Animation onSelected, @NotNull Point location, @Nullable Runnable onRelease) {
		defaultAnimation = def;
		onHoverAnimation = onHover;
		onPressAnimation = onPress;
		onSelectedAnimation = onSelected;
		this.location = location;
		this.onRelease = onRelease;
	}
	
	@Override
	public @NotNull Animation getCurrentAnimation() {
		if(selected && onSelectedAnimation != null) {
			return onSelectedAnimation;
		} else if(pressed && onPressAnimation != null) {
			return onPressAnimation;
		} else if(hovered && onHoverAnimation != null) {
			return onHoverAnimation;
		}
		return defaultAnimation;
	}
	
	@Override
	public @NotNull RenderingLayer getDefaultLayer() {
		return GraphicsManager.getLayer(GraphicsManager.LAYER_GUI);
	}
	
	@Override
	public @NotNull Point getLocation() {
		return location;
	}
	
	@Override
	public boolean hasMouseCollision() {
		return true;
	}
	
	@Override
	public boolean isHovered() {
		return false;//todo
	}
	
	@Override
	public boolean isSelectable() {
		return true;
	}
	
	@Override
	public boolean isSelected() {
		return false;//todo
	}
	
	@Override
	public void onHover() {
		hovered = true;
		if(onHoverAnimation != null) {
			onHoverAnimation.reset();
		}
	}
	
	@Override
	public void onHoverStop() {
		hovered = false;
		pressed = false;
	}
	
	@Override
	public void onMousePress() {
		pressed = true;
		hovered = true;
		if(onPressAnimation != null) {
			onPressAnimation.reset();
		}
	}
	
	@Override
	public void onMouseRelease() {
		pressed = false;
		if(onPressAnimation != null) {
			onPressAnimation.reset();
		}
		if(onHoverAnimation != null) {
			onHoverAnimation.reset();
		}
		if(onRelease != null) {
			onRelease.run();
		}
	}
	
	@Override
	public void onSelect() {
		selected = true;
		hovered = true;
		if(onSelectedAnimation != null) {
			onSelectedAnimation.reset();
		}
	}
	
	@Override
	public void onUnselect() {
		selected = false;
	}
}
