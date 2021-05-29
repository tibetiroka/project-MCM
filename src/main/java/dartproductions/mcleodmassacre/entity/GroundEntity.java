package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.graphics.Animation;

import java.awt.Point;

public class GroundEntity extends Background {
	
	public GroundEntity(Animation animation, Point location) {
		super(animation, location);
	}
	
	@Override
	public boolean hasCollision() {
		return true;
	}
	
	@Override
	public boolean isCollisionMovable() {
		return false;
	}
	
	@Override
	public boolean onCollision(Entity e) {
		return false;
	}
}
