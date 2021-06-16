/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
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
	 * Tag that indicates a tag resource. Tags are never unloaded.
	 *
	 * @since 0.1.0
	 */
	@NotNull Tag TAG = new GreedyTag(Identifier.fromString("tags/tag"));
	
	/**
	 * Checks if the resource is required for the specified game state. If a resource is not required, it might get unloaded. Any required resource will be loaded.
	 *
	 * @param state     The game state to check
	 * @param nextState A possible next game state, as defined by {@link Main#setGameState(GameState, GameState)}
	 * @return True if the resource is required
	 * @since 0.1.0
	 */
	boolean isRequired(@NotNull GameState state, @Nullable GameState nextState);
	
	/**
	 * Gets the threshold for forcing the unloading of the tagged resources. This threshold represents memory usage; if the application's current memory usage (ratio of the currently used and the max memory, linear scale between 0 and 1) is above this threshold, the resources are forcefully unloaded.
	 * <p>
	 * The unloaded resources are not removed from the memory and the space is not freed; however, all references to them from {@link ResourceManager} are removed and they are available for garbage collection.
	 * <p>
	 * Any object holding references to resources should either release them, or make sure that the object itself is ready for garbage collection. For example, entities of any state hold references to resources, but they cannot be accessed after the state changes.
	 * <p>
	 * Returning 1 means the tag never forces the unloading of any resource. Returning 0 means the tag always forces the unloading of these resources.
	 * <p>
	 * A resource is only unloaded if it is not {@link #isRequired(GameState, GameState) required} in the current state. It is enough to have just 1 tag that requires the resource to keep it loaded, and it only takes 1 tag to allow unloading it. The reason for this is that certain tags may be used to indicate rarely used but memory-expensive resources (like audio data), that shouldn't be kept loaded unless necessary.	 *
	 *
	 * @param state     The game state to check
	 * @param nextState A possible next game state, as defined by {@link Main#setGameState(GameState, GameState)}
	 * @return The threshold
	 * @since 0.1.0
	 */
	double getUnloadingThreshold(@NotNull GameState state, @Nullable GameState nextState);
}
