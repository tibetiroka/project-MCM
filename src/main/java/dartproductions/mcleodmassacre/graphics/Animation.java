/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.hitbox.ImageHitbox;
import dartproductions.mcleodmassacre.resources.ResourceManager;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.IntFunction;

/**
 * Animations are used for describing an entity's hitbox and visual properties. They are updated every frame.
 *
 * @since 0.1.0
 */
public interface Animation extends Cloneable {
	
	@Nullable Animation clone();
	
	/**
	 * Gets the name of this animation. The returned value doesn't have to match the base name for the images or hitboxes used.
	 *
	 * @return The name of this animation
	 * @since 0.1.0
	 */
	@NotNull String getAnimationName();
	
	/**
	 * Gets the image that this animation is currently showing.
	 *
	 * @return The current frame
	 * @since 0.1.0
	 */
	@NotNull Image getCurrentFrame();
	
	/**
	 * Gets the current hitbox of this animation.
	 *
	 * @return The hitbox
	 * @since 0.1.0
	 */
	@Nullable Area getCurrentHitbox();
	
	/**
	 * Gets the unique id of this animation.
	 *
	 * @return The animation's id
	 * @since 0.1.0
	 */
	@NotNull UUID getId();
	
	/**
	 * Gets the length of the animation. The length is the amount of frames an animation can show without being reset.
	 *
	 * @return The length
	 * @since 0.1.0
	 */
	int getLength();
	
	/**
	 * Gets the offset of this animation. This is the distance between any 'p' point on the screen (for example, an entity's location which has this animation) and the point where this animation should be drawn.
	 *
	 * @return The offset of the animation
	 * @since 0.1.0
	 */
	@NotNull Dimension getOffset();
	
	/**
	 * Checks if this animation is over. An animation is over if it can't use its {@link #next()} method safely, or return the appropriate {@link #getCurrentFrame() images} or {@link #getCurrentHitbox() hitboxes}.
	 *
	 * @return True if over
	 * @since 0.1.0
	 */
	boolean isOver();
	
	/**
	 * Changes the animation to show its next frame and the corresponding hitboxes.
	 *
	 * @since 0.1.0
	 */
	void next();
	
	/**
	 * Resets this animation. The animation must return to its first frame.
	 *
	 * @since 0.1.0
	 */
	void reset();
	
	/**
	 * An animation that can render text over its images. The text doesn't change the hitbox.
	 *
	 * @since 0.1.0
	 */
	class AnimationWithText extends LoopingAnimation {
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
	
	/**
	 * Animation for displaying formatted text. The text may change at every frame. This animation has no hitbox and loops continuously.
	 *
	 * @since 0.1.0
	 */
	class FormattedTextAnimation implements Animation {
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
		public @Nullable FormattedTextAnimation clone() {
			try {
				return (FormattedTextAnimation) super.clone();
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
	
	/**
	 * An animation that resets every time it is over, creating a loop.
	 *
	 * @since 0.1.0
	 */
	class LoopingAnimation extends StandardAnimation {
		/**
		 * Creates a new animation with the given name and no offset.
		 *
		 * @param name The name of the animation, used for finding images and hitboxes
		 * @since 0.1.0
		 */
		public LoopingAnimation(@NotNull String name) {
			this(name, new Dimension(0, 0));
		}
		
		/**
		 * Creates a new animation with the given name and offset.
		 *
		 * @param name   The name of the animation, used for finding images and hitboxes
		 * @param offset The offset of the animation
		 * @since 0.1.0
		 */
		public LoopingAnimation(@NotNull String name, @NotNull Dimension offset) {
			super(name, offset);
		}
		
		@Override
		public void next() {
			super.next();
			if(isOver()) {
				frame = 0;
			}
		}
	}
	
	/**
	 * An animation that can be mirrored along the Y axis.
	 *
	 * @since 0.1.0
	 */
	class MirrorableAnimation implements Animation {
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
				e.printStackTrace();
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
	
	/**
	 * Animation implementation for simple animations. The images and hitboxes are automatically queried based on the animation's name.
	 *
	 * @since 0.1.0
	 */
	class StandardAnimation implements Animation {
		/**
		 * The images to show
		 *
		 * @see #getCurrentFrame()
		 * @since 0.1.0
		 */
		protected final @NotNull Image[] frames;
		/**
		 * The hitboxes as areas
		 *
		 * @since 0.1.0
		 */
		protected final @NotNull Area[] hitboxes;
		/**
		 * The id of the animation
		 *
		 * @see #getId()
		 * @since 0.1.0
		 */
		protected final @NotNull UUID id = UUID.randomUUID();
		/**
		 * The name of the animation
		 *
		 * @see #getAnimationName()
		 * @since 0.1.0
		 */
		protected final @NotNull String name;
		/**
		 * The offset of the animation
		 *
		 * @see #getOffset()
		 * @since 0.1.0
		 */
		protected final @NotNull Dimension offset;
		/**
		 * The index of the current frame
		 *
		 * @see #getLength()
		 * @see #getCurrentFrame()
		 * @see #reset()
		 * @since 0.1.0
		 */
		protected int frame = 0;
		
		
		/**
		 * Creates a new animation with the given name and no offset.
		 *
		 * @param name The name of the animation, used for finding images and hitboxes
		 * @since 0.1.0
		 */
		public StandardAnimation(@NotNull String name) {
			this(name, new Dimension(0, 0));
		}
		
		/**
		 * Creates a new animation with the given name and offset.
		 *
		 * @param name   The name of the animation, used for finding images and hitboxes
		 * @param offset The offset of the animation
		 * @since 0.1.0
		 */
		public StandardAnimation(@NotNull String name, @NotNull Dimension offset) {
			this.name = name;
			this.offset = offset;
			int frameCount = countFrames();
			frames = new Image[frameCount];
			hitboxes = new Area[frameCount];
			fetchFrames();
		}
		
		@Override
		public @Nullable Animation clone() {
			try {
				return (Animation) super.clone();
			} catch(Exception e) {
				return null;
			}
		}
		
		@Override
		public @NotNull String getAnimationName() {
			return name;
		}
		
		@Override
		public @NotNull Image getCurrentFrame() {
			return frames[frame];
		}
		
		@Override
		public @Nullable Area getCurrentHitbox() {
			return hitboxes[frame];
		}
		
		@Override
		public @NotNull UUID getId() {
			return id;
		}
		
		@Override
		public int getLength() {
			return frames.length;
		}
		
		@Override
		public @NotNull Dimension getOffset() {
			return offset;
		}
		
		@Override
		public boolean isOver() {
			return frame >= frames.length;
		}
		
		@Override
		public void next() {
			frame++;
		}
		
		@Override
		public void reset() {
			frame = 0;
		}
		
		/**
		 * Counts the amount of images that can be found with the animation's name.
		 *
		 * @return The amount of frames
		 * @since 0.1.0
		 */
		protected int countFrames() {
			int current = 0;
			while(ResourceManager.getImage(Identifier.fromString(name + "#" + current)) != null) {
				current++;
			}
			return current == 0 ? 1 : current;
		}
		
		/**
		 * Sets the values in the {@link #frames} and {@link #hitboxes} arrays.
		 *
		 * @since 0.1.0
		 */
		protected void fetchFrames() {
			if(frames.length == 1) {
				frames[0] = ResourceManager.getImage(Identifier.fromString(name));
				ImageHitbox hitbox = ResourceManager.getHitbox(Identifier.fromString(name + "/hitbox"));
				hitboxes[0] = hitbox == null ? null : hitbox.getArea();
			} else {
				for(int i = 0; i < frames.length; i++) {
					frames[i] = ResourceManager.getImage(Identifier.fromString(name + "#" + i));
					ImageHitbox hitbox = ResourceManager.getHitbox(Identifier.fromString(name + "#" + i + "/hitbox"));
					hitboxes[i] = hitbox == null ? null : hitbox.getArea();
				}
			}
		}
		
		
	}
}
