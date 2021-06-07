package dartproductions.mcleodmassacre.util;

import dartproductions.mcleodmassacre.entity.Entity;
import dartproductions.mcleodmassacre.graphics.Animation;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.graphics.ResolutionManager;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;

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
	
	/**
	 * Centers the image for the default screen. The image's center will be within .5 pixels of the screen's center if its top-left corner is at the specified point.
	 *
	 * @param image The image to center
	 * @return The top-left corner of the centered image
	 * @since 0.1.0
	 */
	public static @NotNull Point center(@NotNull BufferedImage image) {
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
	public static @NotNull Point center(@NotNull Image image) {
		int x = (ResolutionManager.getDefaultScreenSize().width - image.getWidth(GraphicsManager.WINDOW)) / 2;
		int y = (ResolutionManager.getDefaultScreenSize().height - image.getHeight(GraphicsManager.WINDOW)) / 2;
		return new Point(x, y);
	}
	
	/**
	 * Centers the animation's current frame for the default screen. The image's center will be within .5 pixels of the screen's center if its top-left corner is at the specified point. This method IGNORES the animation's current offset.
	 *
	 * @param image The image to center
	 * @return The top-left corner of the centered image
	 * @since 0.1.0
	 */
	public static @NotNull Point center(@NotNull Animation image) {
		int x = (ResolutionManager.getDefaultScreenSize().width - image.getCurrentFrame().getWidth(GraphicsManager.WINDOW)) / 2;
		int y = (ResolutionManager.getDefaultScreenSize().height - image.getCurrentFrame().getHeight(GraphicsManager.WINDOW)) / 2;
		return new Point(x, y);
	}
	
	/**
	 * Centers the given animation by modifying its offset. The current frame of the animation will be centered on the default screen. The animation's offset is modified directly via {@link Animation#getOffset()}.
	 *
	 * @param animation The animation to center
	 * @return The original animation after being moved
	 * @since 0.1.0
	 */
	public static @NotNull Animation setToCenter(@NotNull Animation animation) {
		int x = (ResolutionManager.getDefaultScreenSize().width - animation.getCurrentFrame().getWidth(GraphicsManager.WINDOW)) / 2;
		int y = (ResolutionManager.getDefaultScreenSize().height - animation.getCurrentFrame().getHeight(GraphicsManager.WINDOW)) / 2;
		animation.getOffset().setSize(x, y);
		return animation;
	}
	
	/**
	 * Centers the entity so its animation's current frame is centered on the default screen. The offset of the animation is taken into account in this calculation. The entity's location is modified directly via the return value of {@link Entity#getLocation()}.
	 *
	 * @param entity The entity to center
	 * @return The original entity after being moved
	 * @since 0.1.0
	 */
	public static @NotNull Entity setToCenter(@NotNull Entity entity) {
		int x = (ResolutionManager.getDefaultScreenSize().width - entity.getCurrentAnimation().getCurrentFrame().getWidth(GraphicsManager.WINDOW)) / 2 - entity.getCurrentAnimation().getOffset().width;
		int y = (ResolutionManager.getDefaultScreenSize().height - entity.getCurrentAnimation().getCurrentFrame().getHeight(GraphicsManager.WINDOW)) / 2 - entity.getCurrentAnimation().getOffset().height;
		entity.getLocation().setLocation(x, y);
		return entity;
	}
	
	/**
	 * Centers the dimension for the default screen. The dimension's center will be within .5 pixels of the screen's center if its top-left corner is at the specified point.
	 *
	 * @param size The dimension to center
	 * @return The top-left corner of the centered object
	 * @since 0.1.0
	 */
	public static @NotNull Point center(@NotNull Dimension size) {
		int x = (ResolutionManager.getDefaultScreenSize().width - size.width) / 2;
		int y = (ResolutionManager.getDefaultScreenSize().height - size.height) / 2;
		return new Point(x, y);
	}
}
