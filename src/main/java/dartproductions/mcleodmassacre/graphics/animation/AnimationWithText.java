/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.graphics.animation;

import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * An animation that can render text over its images. The text doesn't change the hitbox.
 *
 * @since 0.1.0
 */
public class AnimationWithText extends LoopingAnimation {
	/**
	 * The color of the text
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Color color;
	/**
	 * The font of the text
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Font font;
	/**
	 * The text to show
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull String text;
	/**
	 * The offset of the text along the x axis
	 */
	protected final int textOffsetX;
	/**
	 * The offset of the text along the y axis
	 */
	protected final int textOffsetY;
	
	/**
	 * Creates a new animation with text rendered over each frame.
	 *
	 * @param name  The name of the animation
	 * @param text  The text to render
	 * @param font  The font of the text
	 * @param color The color of the text
	 * @since 0.1.0
	 */
	public AnimationWithText(@NotNull String name, @NotNull String text, @NotNull Font font, @NotNull Color color) {
		this(name, new Dimension(0, 0), text, font, color, 0, 0);
	}
	
	/**
	 * Creates a new animation with text rendered over each frame.
	 *
	 * @param name        The name of the animation
	 * @param text        The text to render
	 * @param font        The font of the text
	 * @param color       The color of the text
	 * @param offset      The offset of the animation
	 * @param textOffsetX The offset of the text from the animation along the x axis
	 * @param textOffsetY The offset of the text from the animation along the y axis
	 * @since 0.1.0
	 */
	public AnimationWithText(@NotNull String name, @NotNull Dimension offset, @NotNull String text, @NotNull Font font, @NotNull Color color, int textOffsetX, int textOffsetY) {
		super(name, offset);
		this.text = text;
		this.font = font;
		this.color = color;
		this.textOffsetX = textOffsetX;
		this.textOffsetY = textOffsetY;
	}
	
	@Override
	protected void fetchFrames() {
		super.fetchFrames();
		for(int i = 0; i < frames.length; i++) {
			Image image = frames[i];
			BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			Graphics graphics = bimage.getGraphics();//copy image
			graphics.drawImage(image, 0, 0, GraphicsManager.WINDOW);
			graphics.setFont(font);//draw text
			graphics.setColor(color);
			graphics.drawString(text, textOffsetX, textOffsetY);
			graphics.dispose();
			
			frames[i] = bimage;//set new image
		}
	}
}
