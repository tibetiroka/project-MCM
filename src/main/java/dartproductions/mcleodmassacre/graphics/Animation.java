package dartproductions.mcleodmassacre.graphics;

import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Area;

public interface Animation {
	
	public Image getCurrentFrame();
	
	public Shape getCurrentHitbox();
	
	public Area getCurrentHitboxArea();
	
	public String getAnimationName();
	
	public void next();
	
	public static class StandardAnimation implements Animation {
		private final String name;
		private int frame = 0;
		private Image[] frames;
		private Shape[] frameShapes;
		private Area[] frameHitboxes;
		
		
		public StandardAnimation(String name) {
			this.name = name;
		}
		
		@Override
		public Image getCurrentFrame() {
			return null;
		}
		
		@Override
		public Shape getCurrentHitbox() {
			return null;
		}
		
		@Override
		public Area getCurrentHitboxArea() {
			return null;
		}
		
		@Override
		public String getAnimationName() {
			return null;
		}
		
		@Override
		public void next() {
		
		}
	}
}
