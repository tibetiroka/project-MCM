package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dartproductions.mcleodmassacre.graphics.GraphicsManager.*;

public class RenderingLayer {
	protected List<Entity> animations = Collections.synchronizedList(new ArrayList<>());
	
	protected RenderingLayer() {
	}
	
	public void paint() {
		if(WINDOW.isActive()) {
			animations.forEach(e -> ResolutionManager.drawImageOnScreen(e.getLocation().x + e.getCurrentAnimation().getOffset().width, e.getLocation().y + e.getCurrentAnimation().getOffset().height, e.getCurrentAnimation().getCurrentFrame()));
		}
		animations.forEach(e -> e.getCurrentAnimation().next());
	}
	
	public void remove(Entity entity) {
		animations.remove(entity);
	}
	
	public void add(Entity entity) {
		animations.add(entity);
	}
}
