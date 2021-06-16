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
 * Tag implementation which marks the resource as required if the current state extends one of the specified {@link #stateParents}, or if the current state is {@link GameState#isLoadingState()} and the next state requires the resource.
 *
 * @since 0.1.0
 */
public class GameStateTag implements Tag {
	/**
	 * The id of the tag
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Identifier id;
	
	/**
	 * The parent {@link GameState} that requires this tag
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Class<? extends GameState>[] stateParents;
	
	/**
	 * The unloading threshold
	 *
	 * @see #getUnloadingThreshold(GameState, GameState)
	 * @since 0.1.0
	 */
	protected final double threshold;
	
	/**
	 * Creates a new tag with the default threshold of 0.8.
	 *
	 * @param id           The identifier of the tag
	 * @param stateParents The parent {@link GameState game states} that require this tag
	 * @see GameStateTag
	 * @see #getUnloadingThreshold(GameState, GameState)
	 * @since 0.1.0
	 */
	public GameStateTag(@NotNull Identifier id, @NotNull Class<? extends GameState>... stateParents) {
		this(id, 0.8, stateParents);
	}
	
	/**
	 * Creates a new tag.
	 *
	 * @param id           The identifier of the tag
	 * @param stateParents The parent {@link GameState game states} that require this tag
	 * @param threshold    The unloading threshold
	 * @see GameStateTag
	 * @see #getUnloadingThreshold(GameState, GameState)
	 * @since 0.1.0
	 */
	public GameStateTag(@NotNull Identifier id, double threshold, @NotNull Class<? extends GameState>... stateParents) {
		this.id = id;
		this.threshold = threshold;
		this.stateParents = stateParents;
		ResourceManager.registerTag(this);
	}
	
	@Override
	public @NotNull Identifier getId() {
		return id;
	}
	
	@Override
	public boolean isRequired(@NotNull GameState state, @Nullable GameState nextState) {
		for(Class<? extends GameState> stateParent : stateParents) {
			if(stateParent.isAssignableFrom(state.getClass()) || (state.isLoadingState() && stateParent.isAssignableFrom(nextState.getClass()))) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public double getUnloadingThreshold(@NotNull GameState state, @Nullable GameState nextState) {
		return threshold;
	}
}
