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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.IntFunction;

/**
 * Math utilities for the game.
 *
 * @since 0.1.0
 */
public class MathUtils {
	
	/**
	 * Checks if any of the specified values are true.
	 *
	 * @param values Generator function for getting the values
	 * @param amount The amount of values
	 * @param offset The initial values to use in {@link IntFunction#apply(int)}
	 * @return True if there is a true values; false otherwise (false if the amount is 0).
	 * @since 0.1.0
	 */
	public static boolean or(@NotNull IntFunction<Boolean> values, int amount, int offset) {
		for(int i = offset; i < offset + amount; i++) {
			if(values.apply(i)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if neither of the specified values are true.
	 *
	 * @param values Generator function for getting the values
	 * @param amount The amount of values
	 * @param offset The initial values to use in {@link IntFunction#apply(int)}
	 * @return True if there are no true values; true otherwise (true if the amount is 0).
	 * @since 0.1.0
	 */
	public static boolean nor(@NotNull IntFunction<Boolean> values, int amount, int offset) {
		for(int i = offset; i < offset + amount; i++) {
			if(values.apply(i)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if exactly one of the specified values is true.
	 *
	 * @param values Generator function for getting the values
	 * @param amount The amount of values
	 * @param offset The initial values to use in {@link IntFunction#apply(int)}
	 * @return True if there is exactly one true value; false otherwise (false if the amount is 0).
	 * @since 0.1.0
	 */
	public static boolean xor(@NotNull IntFunction<Boolean> values, int amount, int offset) {
		boolean has = false;
		for(int i = offset; i < offset + amount; i++) {
			if(values.apply(i)) {
				if(has) {
					return false;
				}
				has = true;
			}
		}
		return has;
	}
	
	/**
	 * Gets the minimum of the specified values.
	 *
	 * @param numbers Generator function for getting the values
	 * @param amount  The amount of values to get the minimum of
	 * @param offset  The initial value to use in {@link IntFunction#apply(int)}
	 * @return The lowest value or {@link Long#MAX_VALUE} if the amount is 0
	 * @since 0.1.0
	 */
	public static long minLong(@NotNull IntFunction<Long> numbers, int amount, int offset) {
		long min = Long.MAX_VALUE;
		for(int i = offset; i < offset + amount; i++) {
			long value = numbers.apply(i);
			if(value < min) {
				min = value;
			}
		}
		return min;
	}
	
	/**
	 * Gets the minimum of the specified values.
	 *
	 * @param numbers Generator function for getting the values
	 * @param amount  The amount of values to get the minimum of
	 * @param offset  The initial value to use in {@link IntFunction#apply(int)}
	 * @return The lowest value or {@link Integer#MAX_VALUE} if the amount is 0
	 * @since 0.1.0
	 */
	public static int minInt(@NotNull IntFunction<Integer> numbers, int amount, int offset) {
		int min = Integer.MAX_VALUE;
		for(int i = offset; i < offset + amount; i++) {
			int value = numbers.apply(i);
			if(value < min) {
				min = value;
			}
		}
		return min;
	}
	
	/**
	 * Gets the maximum of the specified values.
	 *
	 * @param numbers Generator function for getting the values
	 * @param amount  The amount of values to get the maximum of
	 * @param offset  The initial value to use in {@link IntFunction#apply(int)}
	 * @return The highest value or {@link Long#MAX_VALUE} if the amount is 0
	 * @since 0.1.0
	 */
	public static long maxLong(@NotNull IntFunction<Long> numbers, int amount, int offset) {
		long max = Long.MIN_VALUE;
		for(int i = offset; i < offset + amount; i++) {
			long value = numbers.apply(i);
			if(value > max) {
				max = value;
			}
		}
		return max;
	}
	
	/**
	 * Gets the maximum of the specified values.
	 *
	 * @param numbers Generator function for getting the values
	 * @param amount  The amount of values to get the maximum of
	 * @param offset  The initial value to use in {@link IntFunction#apply(int)}
	 * @return The highest value or {@link Integer#MAX_VALUE} if the amount is 0
	 * @since 0.1.0
	 */
	public static int maxInt(@NotNull IntFunction<Integer> numbers, int amount, int offset) {
		int max = Integer.MIN_VALUE;
		for(int i = offset; i < offset + amount; i++) {
			int value = numbers.apply(i);
			if(value > max) {
				max = value;
			}
		}
		return max;
	}
	
	/**
	 * Gets the minimum of the specified values.
	 *
	 * @param numbers Generator function for getting the values
	 * @param amount  The amount of values to get the minimum of
	 * @param offset  The initial value to use in {@link IntFunction#apply(int)}
	 * @return The lowest value or {@link Double#MAX_VALUE} if the amount is 0
	 * @since 0.1.0
	 */
	public static double minDouble(@NotNull IntFunction<Double> numbers, int amount, int offset) {
		double min = Double.MAX_VALUE;
		for(int i = offset; i < offset + amount; i++) {
			double value = numbers.apply(i);
			if(value < min) {
				min = value;
			}
		}
		return min;
	}
	
	/**
	 * Gets the maximum of the specified values.
	 *
	 * @param numbers Generator function for getting the values
	 * @param amount  The amount of values to get the maximum of
	 * @param offset  The initial value to use in {@link IntFunction#apply(int)}
	 * @return The highest value or {@link Double#MAX_VALUE} if the amount is 0
	 * @since 0.1.0
	 */
	public static double maxDouble(@NotNull IntFunction<Double> numbers, int amount, int offset) {
		double max = Long.MIN_VALUE;
		for(int i = offset; i < offset + amount; i++) {
			double value = numbers.apply(i);
			if(value > max) {
				max = value;
			}
		}
		return max;
	}
	
	/**
	 * Gets the average of the specified values. This method uses {@link BigInteger} for avoiding numeric overflow.
	 *
	 * @param numbers Generator function for getting the values
	 * @param amount  The amount of values to get the average of
	 * @param offset  The initial value to use in {@link IntFunction#apply(int)}
	 * @return The average of the values or 0 if the amount is 0
	 * @since 0.1.0
	 */
	public static long avgLong(@NotNull IntFunction<Long> numbers, int amount, int offset) {
		BigInteger sum = BigInteger.ZERO;
		for(int i = amount; i < amount + offset; i++) {
			sum = sum.add(BigInteger.valueOf(numbers.apply(i)));
		}
		return sum.divide(BigInteger.valueOf(amount)).longValue();
	}
	
	/**
	 * Gets the average of the specified values. This method uses long to avoid numeric overflow.
	 *
	 * @param numbers Generator function for getting the values
	 * @param amount  The amount of values to get the average of
	 * @param offset  The initial value to use in {@link IntFunction#apply(int)}
	 * @return The average of the values or 0 if the amount is 0
	 * @since 0.1.0
	 */
	public static int avgInt(@NotNull IntFunction<Integer> numbers, int amount, int offset) {
		long sum = 0;
		for(int i = amount; i < amount + offset; i++) {
			sum += numbers.apply(i);
		}
		return (int) (sum / amount);
	}
	
	/**
	 * Gets the average of the specified values. This method uses {@link BigDecimal} for avoiding numeric overflow.
	 *
	 * @param numbers Generator function for getting the values
	 * @param amount  The amount of values to get the average of
	 * @param offset  The initial value to use in {@link IntFunction#apply(int)}
	 * @return The average of the values or 0 if the amount is 0
	 * @since 0.1.0
	 */
	public static double avgDouble(@NotNull IntFunction<Double> numbers, int amount, int offset) {
		BigDecimal sum = BigDecimal.ZERO;
		for(int i = amount; i < amount + offset; i++) {
			sum = sum.add(BigDecimal.valueOf(numbers.apply(i)));
		}
		return sum.divide(BigDecimal.valueOf(amount)).doubleValue();
	}
	
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
	public static @NotNull Animation setToCenterOffset(@NotNull Animation animation, @NotNull Dimension offset) {
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
	
	public static @NotNull Dimension getSize(@NotNull Animation animation) {
		if(animation instanceof StandardAnimation anim) {
			return new Dimension(anim.getCurrentFrame().getWidth(GraphicsManager.WINDOW), anim.getCurrentFrame().getHeight(GraphicsManager.WINDOW));
		} else {
			Rectangle r = animation.getCurrentHitbox().getBounds();
			return new Dimension(r.width, r.height);
		}
	}
}
