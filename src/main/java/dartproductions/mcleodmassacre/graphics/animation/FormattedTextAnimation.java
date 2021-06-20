/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.graphics.animation;

import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.IntFunction;

/**
 * Animation for displaying formatted text. The text may change at every frame. This animation has no hitbox and loops continuously.
 *
 * @since 0.1.0
 */
public class FormattedTextAnimation implements Animation {
	/**
	 * The empty area used as hitbox.
	 *
	 * @since 0.1.0
	 */
	public static final @NotNull Area EMPTY = new Area();
	/**
	 * The unique ID of this animation
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull UUID id = UUID.randomUUID();
	/**
	 * The rendered version of the texts
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull BufferedImage[] images;
	/**
	 * True if the animation should loop
	 *
	 * @since 0.1.0
	 */
	protected final boolean loop;
	/**
	 * The name of the animation
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull String name;
	/**
	 * Generator function for the text offset in each frame
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull IntFunction<Dimension> offsetGenerator;
	/**
	 * The current frame
	 *
	 * @since 0.1.0
	 */
	protected int frame = 0;
	
	/**
	 * Creates a new text animation.
	 *
	 * @param name            The name of the animation
	 * @param defaultFont     The default font to be modified for formatted texts
	 * @param colorGenerator  Generator function for the text's color in each frame.
	 * @param offsetGenerator Generator function for the text's offset in each frame.
	 * @param textGenerator   Generator function for the text in each frame.
	 * @param length          The amount of frames to display
	 * @param loop            True if the animation should loop when done
	 * @since 0.1.0
	 */
	public FormattedTextAnimation(@NotNull String name, @NotNull Font defaultFont, int length, boolean loop, @NotNull IntFunction<Color> colorGenerator, @NotNull IntFunction<List<String>> textGenerator, @NotNull IntFunction<Dimension> offsetGenerator) {
		this.offsetGenerator = offsetGenerator;
		this.name = name;
		this.loop = loop;
		images = new BufferedImage[length];
		createImages(defaultFont, textGenerator, colorGenerator);
	}
	
	@Override
	public @Nullable dartproductions.mcleodmassacre.graphics.animation.FormattedTextAnimation clone() {
		try {
			return (dartproductions.mcleodmassacre.graphics.animation.FormattedTextAnimation) super.clone();
		} catch(CloneNotSupportedException e) {
			return null;
		}
	}
	
	@Override
	public @NotNull String getAnimationName() {
		return name;
	}
	
	@Override
	public @NotNull Image getCurrentFrame() {
		return images[frame];
	}
	
	@Override
	public @Nullable Area getCurrentHitbox() {
		return EMPTY;
	}
	
	@Override
	public @NotNull UUID getId() {
		return id;
	}
	
	@Override
	public int getLength() {
		return images.length;
	}
	
	@Override
	public @NotNull Dimension getOffset() {
		return offsetGenerator.apply(frame);
	}
	
	@Override
	public boolean isOver() {
		return frame >= images.length;
	}
	
	@Override
	public void next() {
		frame++;
		if(loop && frame >= images.length) {
			frame = 0;
		}
	}
	
	@Override
	public void reset() {
		frame = 0;
	}
	
	/**
	 * Creates the images containing text for display purposes.
	 *
	 * @param defaultFont    The default font to use for text rendering
	 * @param textGenerator  The generator function for getting the text of each frame
	 * @param colorGenerator The generator function for getting the color of the text of each frame
	 * @since 0.1.0
	 */
	protected void createImages(@NotNull Font defaultFont, @NotNull IntFunction<List<String>> textGenerator, @NotNull IntFunction<Color> colorGenerator) {
		FontMetrics defaultMetrics = GraphicsManager.PANEL.getFontMetrics(defaultFont);
		for(int i = 0; i < images.length; i++) {
			List<String> textLines = textGenerator.apply(i);
			int maxWidth = 0;
			int height = 0;
			ArrayList<BufferedImage> lineImages = new ArrayList<>();
			//
			for(String textLine : textLines) {
				textLine = textLine.trim();
				Font font = defaultFont;
				FontMetrics metrics = defaultMetrics;
				int lineWidth, lineHeight;
				//
				if(textLine.isEmpty()) {
					lineHeight = defaultMetrics.getHeight();
					lineWidth = 1;
				} else if(textLine.startsWith("»")) {
					font = new Font(defaultFont.getName(), defaultFont.getStyle(), (int) (defaultFont.getSize() * 1.5));
					textLine = textLine.substring(1).trim();
					metrics = GraphicsManager.PANEL.getFontMetrics(font);
					lineHeight = metrics.getHeight();
					lineWidth = metrics.stringWidth(textLine);
				} else {
					lineHeight = defaultMetrics.getHeight();
					lineWidth = defaultMetrics.stringWidth(textLine);
				}
				//
				height += lineHeight;
				maxWidth = Math.max(lineWidth, maxWidth);
				//
				BufferedImage image = new BufferedImage(lineWidth, lineHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = image.createGraphics();
				//
				g2.setColor(colorGenerator.apply(i));
				g2.setFont(font);
				g2.drawString(textLine, 0, metrics.getAscent());
				//
				g2.dispose();
				//
				lineImages.add(image);
			}
			BufferedImage image = (images[i] = new BufferedImage(maxWidth, height, BufferedImage.TYPE_INT_ARGB));
			{
				Graphics2D g2 = image.createGraphics();
				//
				int y = 0;
				for(BufferedImage lineImage : lineImages) {
					g2.drawImage(lineImage, 0, y, null);
					y += lineImage.getHeight();
				}
				g2.dispose();
			}
		}
	}
}
