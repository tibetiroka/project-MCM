package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.graphics.Animation;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.graphics.RenderingLayer;
import org.jetbrains.annotations.NotNull;

import java.awt.Point;

/**
 * Entity implementation for standard background-like features. Doesn't interact with the mouse, can't collide or move. Doesn't react to being hovered/pressed/selected.
 *
 * @since 0.1.0
 */
public class Background implements Entity {
	/**
	 * The animation of this entity
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Animation animation;
	/**
	 * The location of the entity
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Point location;
	
	/**
	 * Creates a new background entity.
	 *
	 * @param animation The animation to show
	 * @param location  The location of the entity
	 * @since 0.1.0
	 */
	public Background(@NotNull Animation animation, @NotNull Point location) {
		this.animation = animation;
		this.location = location;
	}
	
	/**
	 * Creates a new background entity at (0,0).
	 *
	 * @param animation The animation to show
	 * @since 0.1.0
	 */
	public Background(@NotNull Animation animation) {
		this(animation, new Point(0, 0));
	}
	
	@Override
	public @NotNull Animation getCurrentAnimation() {
		return animation;
	}
	
	@Override
	public @NotNull RenderingLayer getDefaultLayer() {
		return GraphicsManager.getLayer(GraphicsManager.LAYER_BACKGROUND);
	}
	
	//oh yes, interaction
	
	@Override
	public void onHover() {
	}
	
	@Override
	public void onHoverStop() {
	}
	
	@Override
	public boolean isHovered() {
		return false;
	}
	
	@Override
	public void onMousePress() {
	}
	
	@Override
	public void onMouseRelease() {
	}
	
	@Override
	public @NotNull Point getLocation() {
		return location;
	}
	
	@Override
	public void onSelect() {
	}
	
	@Override
	public void onUnselect() {
	}
	
	@Override
	public boolean isSelectable() {
		return false;
	}
	
	@Override
	public boolean isSelected() {
		return false;
	}
	
	@Override
	public boolean hasMouseCollision() {
		return false;
	}
	
	/**
	 * Same as the background but appears on high priority rendering layers ({@link GraphicsManager#LAYER_GUI}).
	 *
	 * @since 0.1.0
	 */
	public static class Foreground extends Background {
		
		/**
		 * Creates a new foreground entity.
		 *
		 * @param animation The animation to show
		 * @param location  The location of the entity
		 * @since 0.1.0
		 */
		public Foreground(@NotNull Animation animation, @NotNull Point location) {
			super(animation, location);
		}
		
		/**
		 * Creates a new foreground entity at the default location.
		 *
		 * @param animation The animation to show
		 * @since 0.1.0
		 */
		public Foreground(@NotNull Animation animation) {
			super(animation);
		}
		
		@Override
		public @NotNull RenderingLayer getDefaultLayer() {
			return GraphicsManager.getLayer(GraphicsManager.LAYER_GUI);
		}
	}
}
