package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.util.Pair;

import java.awt.Point;
import java.util.concurrent.ConcurrentHashMap;

import static dartproductions.mcleodmassacre.graphics.ResolutionManager.*;

public class RenderingLayer {
	protected ConcurrentHashMap<String, Pair<Point, Animation>> animations = new ConcurrentHashMap<>();
	
	
	public void paint() {
		animations.values().forEach(p -> BUFFER_GRAPHICS.drawImage(p.second().getCurrentFrame(), p.first().x, p.first.y, GraphicsManager.WINDOW));
		animations.values().forEach(p -> p.second().next());
		animations.entrySet().removeIf(name -> name.getValue().second().isOver());
	}
	
	public Pair<Point, Animation> remove(String name) {
		return animations.remove(name);
	}
	
	public Pair<Point, Animation> get(String name) {
		return animations.get(name);
	}
	
	public Pair<Point, Animation> add(String name, Animation animation, Point location) {
		return animations.put(name, new Pair<>(location, animation));
	}
	
	public Pair<Point, Animation> add(String name, Pair<Point, Animation> animation) {
		return animations.put(name, animation);
	}
	
	public Point changeLocation(String name, Point newLocation) {
		return get(name).setFirst(newLocation);
	}
}
