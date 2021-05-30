package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.UUID;

/**
 * Animations are used for describing an entity's hitbox and visual properties. They are updated every frame.
 */
public interface Animation extends Cloneable {
	
	/**
	 * Gets the image that this animation is currently showing.
	 *
	 * @return The current frame
	 */
	public @NotNull Image getCurrentFrame();
	
	/**
	 * Gets the current hitbox of this animation.
	 *
	 * @return The hitbox
	 */
	public @Nullable Shape getCurrentHitbox();
	
	/**
	 * Gets the current hitbox of this animation as an {@link Area}.
	 *
	 * @return The hitbox
	 */
	public @Nullable Area getCurrentHitboxArea();
	
	/**
	 * Gets the name of this animation. The returned value doesn't have to match the base name for the images or hitboxes used.
	 *
	 * @return The name of this animation
	 */
	public @NotNull String getAnimationName();
	
	/**
	 * Checks if this animation is over. An animation is over if it can't use its {@link #next()} method safely, or return the appropriate {@link #getCurrentFrame() images} or {@link #getCurrentHitbox() hitboxes} / {@link #getCurrentHitboxArea() hitbox areas}.
	 *
	 * @return True if over
	 */
	public boolean isOver();
	
	/**
	 * Resets this animation. The animation must return to its first frame.
	 */
	public void reset();
	
	/**
	 * Changes the animation to show its next frame and the corresponding hitboxes.
	 */
	public void next();
	
	public @Nullable Animation clone();
	
	/**
	 * Gets the unique id of this animation.
	 *
	 * @return The animation's id
	 */
	public @NotNull UUID getId();
	
	/**
	 * Gets the length of the animation. The length is the amount of frames an animation can show without being reset.
	 *
	 * @return The length
	 */
	public int getLength();
	
	/**
	 * Gets the offset of this animation. This is the distance between any 'p' point on the screen (for example, an entity's location which has this animation) and the point where this animation should be drawn.
	 *
	 * @return The offset of the animation
	 */
	public @NotNull Dimension getOffset();
	
	/**
	 * Animation implementation for simple animations. The images and hitboxes are automatically queried based on the animation's name.
	 */
	public static class StandardAnimation implements Animation {
		/**
		 * The name of the animation
		 *
		 * @see #getAnimationName()
		 */
		protected final @NotNull String name;
		/**
		 * The images to show
		 *
		 * @see #getCurrentFrame()
		 */
		protected final @NotNull Image[] frames;
		/**
		 * The hitboxes of the animation
		 *
		 * @see #getCurrentHitbox()
		 */
		protected final @NotNull Shape[] frameShapes;
		/**
		 * The hitboxes as areas
		 *
		 * @see #getCurrentHitboxArea()
		 */
		protected final @NotNull Area[] frameAreas;
		/**
		 * The id of the animation
		 *
		 * @see #getId()
		 */
		protected final @NotNull UUID id = UUID.randomUUID();
		/**
		 * The offset of the animation
		 *
		 * @see #getOffset()
		 */
		protected final @NotNull Dimension offset;
		/**
		 * The index of the current frame
		 *
		 * @see #getLength()
		 * @see #getCurrentFrame()
		 * @see #reset()
		 */
		protected int frame = 0;
		
		
		/**
		 * Creates a new animation with the given name and no offset.
		 *
		 * @param name The name of the animation, used for finding images and hitboxes
		 */
		public StandardAnimation(@NotNull String name) {
			this(name, new Dimension(0, 0));
		}
		
		/**
		 * Creates a new animation with the given name and offset.
		 *
		 * @param name   The name of the animation, used for finding images and hitboxes
		 * @param offset The offset of the animation
		 */
		public StandardAnimation(@NotNull String name, @NotNull Dimension offset) {
			this.name = name;
			this.offset = offset;
			int frameCount = countFrames();
			frames = new Image[frameCount];
			frameShapes = new Shape[frameCount];
			frameAreas = new Area[frameCount];
			fetchFrames();
		}
		
		/**
		 * Counts the amount of images that can be found with the animation's name.
		 *
		 * @return The amount of frames
		 */
		protected int countFrames() {
			int current = 0;
			while(ResourceManager.getImage(name + "#" + current) != null) {
				current++;
			}
			return current == 0 ? 1 : current;
		}
		
		/**
		 * Sets the values in the {@link #frames}, {@link #frameShapes} and {@link #frameAreas} arrays.
		 */
		protected void fetchFrames() {
			if(frames.length == 1) {
				frames[0] = ResourceManager.getImage(name);
				frameShapes[0] = ResourceManager.getHitbox(name);
				frameAreas[0] = ResourceManager.getHitboxArea(name);
			} else {
				for(int i = 0; i < frames.length; i++) {
					frames[i] = ResourceManager.getImage(name + "#" + i);
					frameShapes[i] = ResourceManager.getHitbox(name + "#" + i);
					frameAreas[i] = ResourceManager.getHitboxArea(name + "#" + i);
				}
			}
		}
		
		@Override
		public @NotNull Image getCurrentFrame() {
			return frames[frame];
		}
		
		@Override
		public @Nullable Shape getCurrentHitbox() {
			return frameShapes[frame];
		}
		
		@Override
		public @Nullable Area getCurrentHitboxArea() {
			return frameAreas[frame];
		}
		
		@Override
		public @NotNull String getAnimationName() {
			return name;
		}
		
		@Override
		public boolean isOver() {
			return frame >= frames.length;
		}
		
		@Override
		public void reset() {
			frame = 0;
		}
		
		@Override
		public void next() {
			frame++;
		}
		
		@Override
		public Animation clone() {
			try {
				return (Animation) super.clone();
			} catch(Exception e) {
				return null;
			}
		}
		
		@Override
		public int getLength() {
			return frames.length;
		}
		
		@Override
		public @NotNull UUID getId() {
			return id;
		}
		
		@Override
		public @NotNull Dimension getOffset() {
			return offset;
		}
		
		
	}
	
	/**
	 * An animation that resets every time it is over, creating a loop.
	 */
	public static class LoopingAnimation extends StandardAnimation {
		/**
		 * Creates a new animation with the given name and no offset.
		 *
		 * @param name The name of the animation, used for finding images and hitboxes
		 */
		public LoopingAnimation(String name) {
			this(name, new Dimension(0, 0));
		}
		
		/**
		 * Creates a new animation with the given name and offset.
		 *
		 * @param name   The name of the animation, used for finding images and hitboxes
		 * @param offset The offset of the animation
		 */
		public LoopingAnimation(String name, Dimension offset) {
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
	 * An animation that can render text over its images. The text doesn't change the hitbox.
	 */
	public static class AnimationWithText extends LoopingAnimation {
		/**
		 * The text to show
		 */
		protected final @NotNull String text;
		/**
		 * The font of the text
		 */
		protected final @NotNull Font font;
		/**
		 * The color of the text
		 */
		protected final @NotNull Color color;
		protected final int textOffsetX;
		protected final int textOffsetY;
		
		public AnimationWithText(String name, String text, Font font, Color color) {
			this(name, new Dimension(0, 0), text, font, color, 0, 0);
		}
		
		public AnimationWithText(String name, Dimension offset, String text, Font font, Color color, int textOffsetX, int textOffsetY) {
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
	 * An animation that can be mirrored along the Y axis.
	 */
	public static class MirrorableAnimation implements Animation {
		/**
		 * The underlying animation instance
		 */
		protected final @NotNull Animation animation;
		/**
		 * The mirrored images of the animation
		 */
		protected final @NotNull BufferedImage[] mirroredFrames;
		/**
		 * The mirrored hitboxes of the animation
		 */
		protected final @NotNull Shape[] mirroredHitboxes;
		/**
		 * The mirrored hitbox areas
		 */
		protected final @NotNull Area[] mirroredAreas;
		/**
		 * True if the animation is mirrored
		 */
		protected boolean mirrored;
		/**
		 * The current frame
		 */
		protected int currentFrame = 0;
		
		/**
		 * Creates a new mirrored animation.
		 *
		 * @param animation The animation to use
		 */
		public MirrorableAnimation(@NotNull Animation animation) {
			this(animation, true);
		}
		
		/**
		 * Creates a new mirrorable animation
		 *
		 * @param animation The animation to use
		 * @param mirrored  True if the animation should be mirrored
		 */
		public MirrorableAnimation(@NotNull Animation animation, boolean mirrored) {
			this.animation = animation;
			this.mirrored = mirrored;
			mirroredFrames = new BufferedImage[animation.getLength()];
			mirroredAreas = new Area[animation.getLength()];
			mirroredHitboxes = new Shape[animation.getLength()];
			
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
				mirroredHitboxes[i] = tx.createTransformedShape(animation.getCurrentHitbox());
				mirroredAreas[i] = new Area(mirroredHitboxes[i]);
			}
		}
		
		/**
		 * Checks if this animation is mirrored.
		 *
		 * @return True if mirrored
		 */
		public boolean isMirrored() {
			return mirrored;
		}
		
		/**
		 * Sets whether this animation is mirrored or not.
		 *
		 * @param mirrored True if mirrored
		 */
		public void setMirrored(boolean mirrored) {
			this.mirrored = mirrored;
		}
		
		@Override
		public @NotNull Image getCurrentFrame() {
			return isMirrored() ? mirroredFrames[currentFrame] : animation.getCurrentFrame();
		}
		
		@Override
		public @Nullable Shape getCurrentHitbox() {
			return isMirrored() ? mirroredHitboxes[currentFrame] : animation.getCurrentHitbox();
		}
		
		@Override
		public @Nullable Area getCurrentHitboxArea() {
			return isMirrored() ? mirroredAreas[currentFrame] : animation.getCurrentHitboxArea();
		}
		
		@Override
		public @NotNull String getAnimationName() {
			return animation.getAnimationName();
		}
		
		@Override
		public boolean isOver() {
			return animation.isOver();
		}
		
		@Override
		public void reset() {
			currentFrame = 0;
			animation.reset();
		}
		
		@Override
		public void next() {
			currentFrame++;
			animation.next();
		}
		
		@Override
		public Animation clone() {
			try {
				return (Animation) super.clone();
			} catch(CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return null;
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
	}
}
