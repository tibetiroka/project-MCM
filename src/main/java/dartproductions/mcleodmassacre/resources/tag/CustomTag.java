/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.resources.tag;

import dartproductions.mcleodmassacre.GameState;
import dartproductions.mcleodmassacre.resources.ResourceManager;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

/**
 * Tag implementation where a user-defined predicate determines whether a resource is required.
 */
public class CustomTag implements Tag {
	/**
	 * The id of the tag
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Identifier id;
	
	/**
	 * The predicate that determines whether the resource is required
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull BiPredicate<GameState, GameState> predicate;
	
	/**
	 * Creates a new tag
	 *
	 * @param id        The identifier of the tag
	 * @param predicate The predicate that determines whether the resource is required
	 * @see GameStateTag
	 * @since 0.1.0
	 */
	public CustomTag(@NotNull Identifier id, @NotNull BiPredicate<GameState, GameState> predicate) {
		this.id = id;
		this.predicate = predicate;
		ResourceManager.registerTag(this);
	}
	
	@Override
	public @NotNull Identifier getId() {
		return id;
	}
	
	@Override
	public boolean isRequired(@NotNull GameState state, @Nullable GameState nextState) {
		return predicate.test(state, nextState);
	}
}
