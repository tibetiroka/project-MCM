/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rendering layers are used for specifying the order of painting for some entities while not specifying it for others. Entities within a layer are not guaranteed to be painted in any specific order, but the layers are always painted from lowest to highest (the lowest layer can be painted over by higher layers).
 *
 * @since 0.1.0
 */
public class RenderingLayer {
	/**
	 * The entities on this layer
	 *
	 * @since 0.1.0
	 */
	protected @NotNull
	final List<Entity> entities = Collections.synchronizedList(new ArrayList<>());
	
	protected RenderingLayer() {
	}
	
	/**
	 * Adds an entity to this layer
	 *
	 * @param entity The entity to add
	 * @since 0.1.0
	 */
	public void add(@NotNull Entity entity) {
		entities.add(entity);
	}
	
	/**
	 * Gets an entity whose hitbox contains the specified location. If there are multiple such entities, any one of them is returned.
	 *
	 * @param location The location to check
	 * @return The entity or null if not found
	 * @since 0.1.0
	 */
	public @Nullable Entity getEntity(@NotNull Point location) {
		for(Entity entity : entities) {
			if(entity.hasMouseCollision() && entity.getCurrentAnimation().getCurrentHitbox() != null) {
				double x = location.x, y = location.y;
				x -= entity.getLocation().x;
				y -= entity.getLocation().y;
				x -= entity.getCurrentAnimation().getOffset().width;
				y -= entity.getCurrentAnimation().getOffset().height;
				if(entity.getCurrentAnimation().getCurrentHitbox().contains(x, y)) {
					return entity;
				}
			}
		}
		return null;
	}
	
	/**
	 * Paints the entities of this layer to the buffer.
	 *
	 * @since 0.1.0
	 */
	public void paint() {
		entities.forEach(e -> ResolutionManager.drawImageOnScreen(e.getLocation().x + e.getCurrentAnimation().getOffset().width, e.getLocation().y + e.getCurrentAnimation().getOffset().height, e.getCurrentAnimation().getCurrentFrame()));
		entities.forEach(e -> e.getCurrentAnimation().next());
	}
	
	/**
	 * Removes an entity from this layer.
	 *
	 * @param entity The entity to remove
	 * @since 0.1.0
	 */
	public void remove(@Nullable Entity entity) {
		entities.remove(entity);
	}
}
