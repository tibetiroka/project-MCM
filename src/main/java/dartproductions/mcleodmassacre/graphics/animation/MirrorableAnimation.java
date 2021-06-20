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

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.UUID;

/**
 * An animation that can be mirrored along the Y axis.
 *
 * @since 0.1.0
 */
public class MirrorableAnimation implements Animation {
	/**
	 * The underlying animation instance
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Animation animation;
	/**
	 * The mirrored images of the animation
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull BufferedImage[] mirroredFrames;
	/**
	 * The mirrored hitbox areas
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Area[] mirroredHitboxes;
	/**
	 * The current frame
	 *
	 * @since 0.1.0
	 */
	protected int currentFrame = 0;
	/**
	 * True if the animation is mirrored
	 *
	 * @since 0.1.0
	 */
	protected boolean mirrored;
	
	/**
	 * Creates a new mirrored animation.
	 *
	 * @param animation The animation to use
	 * @since 0.1.0
	 */
	public MirrorableAnimation(@NotNull Animation animation) {
		this(animation, true);
	}
	
	/**
	 * Creates a new mirrorable animation
	 *
	 * @param animation The animation to use
	 * @param mirrored  True if the animation should be mirrored
	 * @since 0.1.0
	 */
	public MirrorableAnimation(@NotNull Animation animation, boolean mirrored) {
		this.animation = animation;
		this.mirrored = mirrored;
		mirroredFrames = new BufferedImage[animation.getLength()];
		mirroredHitboxes = new Area[animation.getLength()];
		
		animation.reset();
		for(int i = 0; i < animation.getLength(); i++) {
			BufferedImage image = (BufferedImage) animation.getCurrentFrame();
			AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-image.getWidth(), 0);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			
			BufferedImage dest = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
			{
				Graphics2D g2d = dest.createGraphics();
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
				g2d.fillRect(0, 0, 256, 256);
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
				g2d.dispose();
			}
			mirroredFrames[i] = op.filter(image, dest);
			mirroredHitboxes[i] = new Area(tx.createTransformedShape(animation.getCurrentHitbox()));
		}
	}
	
	@Override
	public @Nullable Animation clone() {
		try {
			return (Animation) super.clone();
		} catch(CloneNotSupportedException e) {
			GraphicsManager.LOGGER.warn("Could not clone animation", e);
		}
		return null;
	}
	
	@Override
	public @NotNull String getAnimationName() {
		return animation.getAnimationName();
	}
	
	@Override
	public @NotNull Image getCurrentFrame() {
		return isMirrored() ? mirroredFrames[currentFrame] : animation.getCurrentFrame();
	}
	
	@Override
	public @Nullable Area getCurrentHitbox() {
		return isMirrored() ? mirroredHitboxes[currentFrame] : animation.getCurrentHitbox();
	}
	
	@Override
	public @NotNull UUID getId() {
		return animation.getId();
	}
	
	@Override
	public int getLength() {
		return animation.getLength();
	}
	
	@Override
	public @NotNull Dimension getOffset() {
		return animation.getOffset();
	}
	
	@Override
	public boolean isOver() {
		return animation.isOver();
	}
	
	@Override
	public void next() {
		currentFrame++;
		animation.next();
	}
	
	@Override
	public void reset() {
		currentFrame = 0;
		animation.reset();
	}
	
	/**
	 * Checks if this animation is mirrored.
	 *
	 * @return True if mirrored
	 * @since 0.1.0
	 */
	public boolean isMirrored() {
		return mirrored;
	}
	
	/**
	 * Sets whether this animation is mirrored or not.
	 *
	 * @param mirrored True if mirrored
	 * @since 0.1.0
	 */
	public void setMirrored(boolean mirrored) {
		this.mirrored = mirrored;
	}
}
