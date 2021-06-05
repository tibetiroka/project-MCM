package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.graphics.Animation;
import org.jetbrains.annotations.NotNull;

import java.awt.Point;

/**
 * Entity representing 'ground' or 'floor' -like structures in a level. Isn't interactive. Collides but cannot be moved by collisions.
 *
 * @since 0.1.0
 */
public class GroundEntity extends Background {
	
	/**
	 * Creates a new ground entity.
	 *
	 * @param animation The animation to show
	 * @param location  The location of the entity
	 * @since 0.1.0
	 */
	public GroundEntity(@NotNull Animation animation, @NotNull Point location) {
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
	public boolean onCollision(@NotNull Entity e) {
		return false;
	}
}
