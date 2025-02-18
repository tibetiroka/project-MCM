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

/**
 * Ignorant tags are tags that never interfere with the loading/unloading specifications of other tags. Their {@link #isRequired(GameState, GameState)} method always returns false.
 *
 * @since 0.1.0
 */
public class IgnorantTag implements Tag {
	/**
	 * The id of the tag
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Identifier id;
	
	/**
	 * Creates a new ignorant tag.
	 *
	 * @param id The identifier of the tag
	 * @see IgnorantTag
	 * @since 0.1.0
	 */
	public IgnorantTag(@NotNull Identifier id) {
		this.id = id;
		ResourceManager.registerTag(this);
	}
	
	@Override
	public @NotNull Identifier getId() {
		return id;
	}
	
	@Override
	public boolean isRequired(@NotNull GameState state, @Nullable GameState nextState) {
		return false;
	}
	
	@Override
	public double getUnloadingThreshold(@NotNull GameState state, @Nullable GameState nextState) {
		return 1;
	}
	
	@Override
	public String toString() {
		return id.toString();
	}
}
