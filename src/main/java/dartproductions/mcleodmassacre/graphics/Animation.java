package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.ResourceManager;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.UUID;

public interface Animation extends Cloneable {
	
	public Image getCurrentFrame();
	
	public Shape getCurrentHitbox();
	
	public Area getCurrentHitboxArea();
	
	public String getAnimationName();
	
	public boolean isOver();
	
	public void reset();
	
	public void next();
	
	public Animation clone();
	
	public UUID getId();
	
	public int getLength();
	
	public Dimension getOffset();
	
	public static class StandardAnimation implements Animation {
		protected final String name;
		protected final Image[] frames;
		protected final Shape[] frameShapes;
		protected final Area[] frameAreas;
		protected final UUID id = UUID.randomUUID();
		protected final Dimension offset;
		protected int frame = 0;
		
		
		public StandardAnimation(String name) {
			this(name, new Dimension(0, 0));
		}
		
		public StandardAnimation(String name, Dimension offset) {
			this.name = name;
			this.offset = offset;
			int frameCount = countFrames();
			frames = new Image[frameCount];
			frameShapes = new Shape[frameCount];
			frameAreas = new Area[frameCount];
			fetchFrames();
		}
		
		protected int countFrames() {
			int current = 0;
			while(ResourceManager.getImage(name + "#" + current) != null) {
				current++;
			}
			return current == 0 ? 1 : current;
		}
		
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
		public Image getCurrentFrame() {
			return frames[frame];
		}
		
		@Override
		public Shape getCurrentHitbox() {
			return frameShapes[frame];
		}
		
		@Override
		public Area getCurrentHitboxArea() {
			return frameAreas[frame];
		}
		
		@Override
		public String getAnimationName() {
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
		public UUID getId() {
			return id;
		}
		
		@Override
		public Dimension getOffset() {
			return offset;
		}
		
		
	}
	
	public static class LoopingAnimation extends StandardAnimation {
		
		public LoopingAnimation(String name) {
			this(name, new Dimension(0, 0));
		}
		
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
	
	public static class AnimationWithText extends LoopingAnimation {
		protected final String text;
		protected final Font font;
		protected final Color color;
		protected final int textOffsetX, textOffsetY;
		
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
			for(Image image : frames) {
				Graphics graphics = image.getGraphics();
				graphics.setFont(font);
				graphics.setColor(color);
				graphics.drawString(text, textOffsetX, textOffsetY);
			}
		}
	}
	
	public static class MirrorableAnimation implements Animation {
		protected final Animation animation;
		protected final BufferedImage[] mirroredFrames;
		protected boolean mirrored;
		protected int currentFrame = 0;
		
		public MirrorableAnimation(Animation animation) {
			this(animation, true);
		}
		
		public MirrorableAnimation(Animation animation, boolean mirrored) {
			this.animation = animation;
			this.mirrored = mirrored;
			mirroredFrames = new BufferedImage[animation.getLength()];
			
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
			}
		}
		
		public boolean isMirrored() {
			return mirrored;
		}
		
		public void setMirrored(boolean mirrored) {
			this.mirrored = mirrored;
		}
		
		@Override
		public Image getCurrentFrame() {
			return isMirrored() ? mirroredFrames[currentFrame] : animation.getCurrentFrame();
		}
		
		@Override
		public Shape getCurrentHitbox() {
			return animation.getCurrentHitbox();
		}
		
		@Override
		public Area getCurrentHitboxArea() {
			return animation.getCurrentHitboxArea();
		}
		
		@Override
		public String getAnimationName() {
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
		public UUID getId() {
			return animation.getId();
		}
		
		@Override
		public int getLength() {
			return animation.getLength();
		}
		
		@Override
		public Dimension getOffset() {
			return animation.getOffset();
		}
	}
}
