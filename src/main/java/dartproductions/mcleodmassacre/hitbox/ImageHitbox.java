/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.hitbox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Shape implementation that is created from an image.
 *
 * @since 0.1.0
 */
public class ImageHitbox implements Shape {
	private static final @NotNull Logger LOGGER = LogManager.getLogger(ImageHitbox.class);
	/**
	 * The actual hitbox
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Area hitbox;
	
	/**
	 * Creates a new image hitbox. Uses the current thread for calculations, which might cause serious blocks for high-resolution images.
	 *
	 * @param image The image to create the hitbox from
	 * @since 0.1.0
	 */
	public ImageHitbox(final @NotNull BufferedImage image) {
		hitbox = createOutline(Color.BLACK, image);
	}
	
	/**
	 * Creates an {@link Area} from the image based on the locations that match and doesn't match the specified color.
	 *
	 * @param target The target color
	 * @param bi     The image
	 * @return The created area
	 * @since 0.1.0
	 */
	public @NotNull Area createOutline(@NotNull Color target, @NotNull BufferedImage bi) {
		GeneralPath gp = new GeneralPath();
		
		boolean cont = false;
		int targetRGB = target.getRGB();
		for(int xx = 0; xx < bi.getWidth(); xx++) {
			for(int yy = 0; yy < bi.getHeight(); yy++) {
				if(bi.getRGB(xx, yy) == targetRGB) {
					if(cont) {
						gp.lineTo(xx, yy);
						gp.lineTo(xx, yy + 1);
						gp.lineTo(xx + 1, yy + 1);
						gp.lineTo(xx + 1, yy);
						gp.lineTo(xx, yy);
					} else {
						gp.moveTo(xx, yy);
					}
					cont = true;
				} else {
					cont = false;
				}
			}
			cont = false;
		}
		gp.closePath();
		
		return new Area(gp);
	}
	
	/**
	 * Gets the hitbox area of this hitbox.
	 *
	 * @return The hitbox area
	 * @since 0.1.0
	 */
	public @NotNull Area getArea() {
		return hitbox;
	}
	
	@Override
	public Rectangle getBounds() {
		return hitbox.getBounds();
	}
	
	@Override
	public Rectangle2D getBounds2D() {
		return hitbox.getBounds2D();
	}
	
	@Override
	public boolean contains(double x, double y) {
		return hitbox.contains(x, y);
	}
	
	@Override
	public boolean contains(Point2D p) {
		return hitbox.contains(p);
	}
	
	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return hitbox.intersects(x, y, w, h);
	}
	
	@Override
	public boolean intersects(Rectangle2D r) {
		return hitbox.intersects(r);
	}
	
	@Override
	public boolean contains(double x, double y, double w, double h) {
		return hitbox.contains(x, y, w, h);
	}
	
	@Override
	public boolean contains(Rectangle2D r) {
		return hitbox.contains(r);
	}
	
	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return hitbox.getPathIterator(at);
	}
	
	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return hitbox.getPathIterator(at, flatness);
	}
	
	/**
	 * Functional interface for executing actions on image hitboxes.
	 *
	 * @since 0.1.0
	 */
	@FunctionalInterface
	public interface HitboxCreatedRunnable {
		/**
		 * Runs whenever the hitbox is created.
		 *
		 * @param hitbox The created hitbox
		 * @since 0.1.0
		 */
		void whenFinished(@NotNull ImageHitbox hitbox);
	}
}
