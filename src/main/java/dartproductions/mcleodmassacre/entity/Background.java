package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.graphics.Animation;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.graphics.RenderingLayer;

import java.awt.Point;

public class Background implements Entity {
	protected final Animation animation;
	protected final Point location;
	
	public Background(Animation animation, Point location) {
		this.animation = animation;
		this.location = location;
	}
	
	@Override
	public Animation getCurrentAnimation() {
		return animation;
	}
	
	@Override
	public RenderingLayer getDefaultLayer() {
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
	public Point getLocation() {
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
}
