package dartproductions.mcleodmassacre.entity;

import dartproductions.mcleodmassacre.graphics.Animation;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.graphics.RenderingLayer;

import java.awt.Point;

public class Button implements Entity {
	protected final Animation defaultAnimation;
	protected final Animation onHoverAnimation;
	protected final Animation onPressAnimation;
	protected final Animation onSelectedAnimation;
	protected final Point location;
	
	protected boolean pressed = false, hovered = false, selected = false;
	
	public Button(Animation def, Animation onHover, Animation onPress, Animation onSelected, Point location) {
		defaultAnimation = def;
		onHoverAnimation = onHover;
		onPressAnimation = onPress;
		onSelectedAnimation = onSelected;
		this.location = location;
	}
	
	@Override
	public Animation getCurrentAnimation() {
		if(selected && onSelectedAnimation != null) {
			return onSelectedAnimation;
		} else if(pressed && onPressAnimation != null) {
			return onPressAnimation;
		} else if(hovered && onHoverAnimation != null) {
			return onHoverAnimation;
		}
		return defaultAnimation;
	}
	
	@Override
	public RenderingLayer getDefaultLayer() {
		return GraphicsManager.getLayer(GraphicsManager.LAYER_GUI);
	}
	
	@Override
	public void onHover() {
		hovered = true;
		onHoverAnimation.reset();
	}
	
	@Override
	public void onHoverStop() {
		hovered = false;
		pressed = false;
	}
	
	@Override
	public boolean isHovered() {
		return false;//todo
	}
	
	@Override
	public void onMousePress() {
		pressed = true;
		hovered = true;
		onPressAnimation.reset();
	}
	
	@Override
	public void onMouseRelease() {
		pressed = false;
		onPressAnimation.reset();
		onHoverAnimation.reset();
	}
	
	@Override
	public Point getLocation() {
		return location;
	}
	
	@Override
	public void onSelect() {
		selected = true;
		hovered = true;
		onSelectedAnimation.reset();
	}
	
	@Override
	public void onUnselect() {
		selected = false;
	}
	
	@Override
	public boolean isSelectable() {
		return true;
	}
	
	@Override
	public boolean isSelected() {
		return false;//todo
	}
}
