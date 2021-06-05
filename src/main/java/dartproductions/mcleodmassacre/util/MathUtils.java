package dartproductions.mcleodmassacre.util;

import org.jetbrains.annotations.NotNull;

import java.awt.Point;

/**
 * Math utilities for the game.
 *
 * @since 0.1.0
 */
public class MathUtils {
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
}
