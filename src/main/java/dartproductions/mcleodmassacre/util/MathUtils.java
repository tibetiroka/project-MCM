/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.util;

import dartproductions.mcleodmassacre.entity.Entity;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.graphics.ResolutionManager;
import dartproductions.mcleodmassacre.graphics.animation.Animation;
import dartproductions.mcleodmassacre.graphics.animation.StandardAnimation;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Math utilities for the game.
 *
 * @since 0.1.0
 */
public class MathUtils {
	/**
	 * Centers the image for the default screen. The image's center will be within .5 pixels of the screen's center if its top-left corner is at the specified point.
	 *
	 * @param image The image to center
	 * @return The top-left corner of the centered image
	 * @since 0.1.0
	 */
	public static @NotNull Point getCenter(@NotNull BufferedImage image) {
		int x = (ResolutionManager.getDefaultScreenSize().width - image.getWidth()) / 2;
		int y = (ResolutionManager.getDefaultScreenSize().height - image.getHeight()) / 2;
		return new Point(x, y);
	}
	
	/**
	 * Centers the image for the default screen. The image's center will be within .5 pixels of the screen's center if its top-left corner is at the specified point.
	 *
	 * @param image The image to center
	 * @return The top-left corner of the centered image
	 * @since 0.1.0
	 */
	public static @NotNull Point getCenter(@NotNull Image image) {
		int x = (ResolutionManager.getDefaultScreenSize().width - image.getWidth(GraphicsManager.WINDOW)) / 2;
		int y = (ResolutionManager.getDefaultScreenSize().height - image.getHeight(GraphicsManager.WINDOW)) / 2;
		return new Point(x, y);
	}
	
	/**
	 * Centers the dimension for the default screen. The dimension's center will be within .5 pixels of the screen's center if its top-left corner is at the specified point.
	 *
	 * @param size The dimension to center
	 * @return The top-left corner of the centered object
	 * @since 0.1.0
	 */
	public static @NotNull Point getCenter(@NotNull Dimension size) {
		int x = (ResolutionManager.getDefaultScreenSize().width - size.width) / 2;
		int y = (ResolutionManager.getDefaultScreenSize().height - size.height) / 2;
		return new Point(x, y);
	}
	
	/**
	 * Checks if the given rectangle contains the specified point
	 *
	 * @param x      The x coordinate of the rectangle
	 * @param y      The y coordinate of the rectangle
	 * @param width  The width of the rectangle
	 * @param height The height of the rectangle
	 * @param p      The point
	 * @return True if the rectangle contains the point
	 * @since 0.1.0
	 */
	public static boolean contains(double x, double y, double width, double height, @NotNull Point p) {
		return p.getX() >= x && p.getX() < x + width && p.getY() >= y && p.getY() < y + height;
	}
	
	/**
	 * Gets the first int from a long
	 *
	 * @param l The long
	 * @return The first int
	 * @since 0.1.0
	 */
	public static int getFirstInt(long l) {
		return (int) (l >> 32);
	}
	
	/**
	 * Gets the second int from a long
	 *
	 * @param l The long
	 * @return The second int
	 * @since 0.1.0
	 */
	public static int getSecondInt(long l) {
		return (int) l;
	}
	
	/**
	 * Converts two int values to a long.
	 *
	 * @param first  The first int
	 * @param second The second int
	 * @return The long
	 * @since 0.1.0
	 */
	public static long intsToLong(int first, int second) {
		return (((long) first) << 32) | (second & 0xffffffffL);
	}
	
	/**
	 * Centers the given animation by modifying its offset. The current frame of the animation will be centered on the default screen. The animation's offset is modified directly via {@link Animation#getOffset()}.
	 *
	 * @param animation The animation to center
	 * @return The original animation after being moved
	 * @since 0.1.0
	 */
	public static @NotNull Animation setToCenter(@NotNull Animation animation) {
		return setToCenterOffset(animation, new Dimension(0, 0));
	}
	
	/**
	 * Shows the given animation centered around the given point relative to the center of the screen by modifying its offset. The current frame of the animation will be centered on the default screen. The animation's offset is modified directly via {@link Animation#getOffset()}.
	 *
	 * @param animation The animation to center
	 * @param offset    The offset from the center of the screen
	 * @return The original animation after being moved
	 * @since 0.1.0
	 */
	public static @NotNull Animation setToCenterOffset(@NotNull Animation animation, Dimension offset) {
		return centerAroundScreenPart(animation, 0.5, 0.5, offset.width, offset.height);
	}
	
	/**
	 * Moves the specified animation so it is centered around the specified point on the screen. The point is specified as the ratio of the point's coordinates and the screen's size (0;0 is the top-left corner, 1;1 is the bottom-right corner). An additional constant offset can also be specified from this point.
	 *
	 * @param animation The animation to move
	 * @param xRatio    The ratio on the x axis
	 * @param yRatio    The ratio on the y axis
	 * @param xOffset   The offset from the screenWidth*xRatio value
	 * @param yOffset   The offset from the screenHeight*yRatio value
	 * @return The original animation after being moved
	 * @since 0.1.0
	 */
	public static @NotNull Animation centerAroundScreenPart(@NotNull Animation animation, double xRatio, double yRatio, double xOffset, double yOffset) {
		if(animation instanceof StandardAnimation a) {
			double x = (ResolutionManager.getDefaultScreenSize().width - a.getCurrentFrame().getWidth(GraphicsManager.WINDOW)) * xRatio;
			double y = (ResolutionManager.getDefaultScreenSize().height - a.getCurrentFrame().getHeight(GraphicsManager.WINDOW)) * yRatio;
			a.getOffset().setSize(x + xOffset, y + yOffset);
		} else {
			Rectangle r = animation.getCurrentHitbox().getBounds();
			double x = (ResolutionManager.getDefaultScreenSize().width - r.width) * xRatio;
			double y = (ResolutionManager.getDefaultScreenSize().height - r.height) * yRatio;
			animation.getOffset().setSize(x + xOffset, y + yOffset);
		}
		return animation;
	}
	
	/**
	 * Centers the entity so its animation's current frame is centered on the default screen. The offset of the animation is taken into account in this calculation. The entity's location is modified directly via the return value of {@link Animation#getOffset()}.
	 *
	 * @param entity The entity to center
	 * @return The original entity after being moved
	 * @since 0.1.0
	 */
	public static @NotNull Entity setToCenter(@NotNull Entity entity) {
		setToCenterOffset(entity.getCurrentAnimation(), new Dimension(-entity.getLocation().x, -entity.getLocation().y));
		return entity;
	}
	
	public static Dimension getSize(Animation animation) {
		if(animation instanceof StandardAnimation anim) {
			return new Dimension(anim.getCurrentFrame().getWidth(GraphicsManager.WINDOW), anim.getCurrentFrame().getHeight(GraphicsManager.WINDOW));
		} else {
			Rectangle r = animation.getCurrentHitbox().getBounds();
			return new Dimension(r.width, r.height);
		}
	}
}
