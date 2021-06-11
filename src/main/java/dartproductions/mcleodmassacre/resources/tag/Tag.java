/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.resources.tag;

import dartproductions.mcleodmassacre.GameState;
import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.resources.ResourceManager;
import dartproductions.mcleodmassacre.resources.id.Identified;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Tags can be attached to any resource (any object with an identifier in any cache) to determine loading/unloading strategies based on game states. Any tag created must be registered via {@link ResourceManager#registerTag(Tag)}.
 *
 * @since 0.1.0
 */
public interface Tag extends Identified {
	/**
	 * Tag that indicates an audio resource. Doesn't change the loading/unloading methods specified by other tags. This tag is used for identifying audio resources during automatic registration.
	 *
	 * @since 0.0.1
	 */
	@NotNull Tag AUDIO = new IgnorantTag(Identifier.fromString("tags/audio"));
	/**
	 * Tag that indicates a 'default resource'. These resources are loaded on startup and are never unloaded.
	 *
	 * @since 0.1.0
	 */
	@NotNull Tag DEFAULT_RESOURCE = new GreedyTag(Identifier.fromString("tags/default_resource"));
	/**
	 * Tag that indicates a graphics resource. Doesn't change the loading/unloading methods specified by other tags. This tag is used for identifying graphics resources during automatic registration.
	 *
	 * @since 0.0.1
	 */
	@NotNull Tag GRAPHICS = new IgnorantTag(Identifier.fromString("tags/graphics"));
	/**
	 * Tag that indicates graphics resources that are used for hitbox creation. Doesn't change the loading/unloading methods specified by other tags.
	 *
	 * @since 0.1.0
	 */
	@NotNull Tag HITBOX_SOURCE = new IgnorantTag(Identifier.fromString("tags/hitboxed"));
	
	/**
	 * Tag that indicates a resource usable in menus.
	 *
	 * @since 0.1.0
	 */
	@NotNull Tag MENU_RESOURCE = new GameStateTag(Identifier.fromString("tags/menu_resource"), GameState.Menu.class);
	
	/**
	 * Checks if the resource is required for the specified game state. If a resource is not required, it might get unloaded. Any required resource will be loaded.
	 *
	 * @param state     The game state to check
	 * @param nextState A possible next game state, as defined by {@link Main#setGameState(GameState, GameState)}
	 * @return True if the resource is required
	 * @since 0.1.0
	 */
	boolean isRequired(@NotNull GameState state, @Nullable GameState nextState);
}
