package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.ResourceManager;

import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Area;

public interface Animation extends Cloneable {
	
	public Image getCurrentFrame();
	
	public Shape getCurrentHitbox();
	
	public Area getCurrentHitboxArea();
	
	public String getAnimationName();
	
	public boolean isOver();
	
	public void reset();
	
	public void next();
	
	public Animation clone();
	
	public int getLength();
	
	public static class StandardAnimation implements Animation {
		protected final String name;
		protected final Image[] frames;
		protected final Shape[] frameShapes;
		protected final Area[] frameAreas;
		protected int frame = 0;
		
		
		public StandardAnimation(String name) {
			this.name = name;
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
			return current;
		}
		
		protected void fetchFrames() {
			for(int i = 0; i < frames.length; i++) {
				frames[i] = ResourceManager.getImage(name + "#" + i);
				frameShapes[i] = ResourceManager.getHitbox(name + "#" + i);
				frameAreas[i] = ResourceManager.getHitboxArea(name + "#" + i);
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
	}
	
	public static class LoopingAnimation extends StandardAnimation {
		
		public LoopingAnimation(String name) {
			super(name);
		}
		
		@Override
		public void next() {
			super.next();
			if(isOver()) {
				frame = 0;
			}
		}
	}
	
	public static class ReplacingAnimation extends StandardAnimation {
		protected Animation replacement;
		
		
		public ReplacingAnimation(String name, Animation replacement) {
			super(name);
			this.replacement = replacement;
		}
		
		public Animation getReplacement() {
			return replacement;
		}
		
		public void setReplacement(Animation replacement) {
			this.replacement = replacement;
		}
		
		@Override
		public void next() {
			super.next();
			if(isOver()) {
				if(replacement != null) {
					for(RenderingLayer layer : GraphicsManager.LAYERS) {
						if(layer.get(getAnimationName())!=null) {
							layer.get(getAnimationName()).setSecond(replacement);
							break;
						}
					}
				}
			}
		}
	}
}
