package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.entity.Entity;
import dartproductions.mcleodmassacre.input.InputManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dartproductions.mcleodmassacre.graphics.GraphicsManager.*;

/**
 * Rendering layers are used for specifying the order of painting for some entities while not specifying it for others. Entities within a layer are not guaranteed to be painted in any specific order, but the layers are always painted from lowest to highest (the lowest layer can be painted over by higher layers).
 */
public class RenderingLayer {
	/**
	 * The entities on this layer
	 */
	protected @NotNull List<Entity> entities = Collections.synchronizedList(new ArrayList<>());
	
	protected RenderingLayer() {
	}
	
	/**
	 * Paints the entities of this layer to the buffer.
	 */
	public void paint() {
		if(WINDOW.isActive()) {
			entities.forEach(e -> ResolutionManager.drawImageOnScreen(e.getLocation().x + e.getCurrentAnimation().getOffset().width, e.getLocation().y + e.getCurrentAnimation().getOffset().height, e.getCurrentAnimation().getCurrentFrame()));
			if(Main.isDebug()) {
				ResolutionManager.BUFFER_GRAPHICS.setColor(Color.RED);
				ResolutionManager.fillRectOnScreen(InputManager.getCursorLocation().x - 2, InputManager.getCursorLocation().y - 2, 5, 5);
			}
		}
		entities.forEach(e -> e.getCurrentAnimation().next());
	}
	
	/**
	 * Removes an entity from this layer.
	 *
	 * @param entity The entity to remove
	 */
	public void remove(@Nullable Entity entity) {
		entities.remove(entity);
	}
	
	/**
	 * Adds an entity to this layer
	 *
	 * @param entity The entity to add
	 */
	public void add(@NotNull Entity entity) {
		entities.add(entity);
	}
	
	/**
	 * Gets an entity whose hitbox contains the specified location. If there are multiple such entities, any one of them is returned.
	 *
	 * @param location The location to check
	 * @return The entity or null if not found
	 */
	public @Nullable Entity getEntity(@NotNull Point location) {
		for(Entity entity : entities) {
			if(entity.hasMouseCollision() && entity.getCurrentAnimation().getCurrentHitboxArea() != null) {
				double x = location.x, y = location.y;
				x -= entity.getLocation().x;
				y -= entity.getLocation().y;
				x -= entity.getCurrentAnimation().getOffset().width;
				y -= entity.getCurrentAnimation().getOffset().height;
				if(entity.getCurrentAnimation().getCurrentHitboxArea().contains(x, y)) {
					return entity;
				}
			}
		}
		return null;
	}
}
