/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.resources.plugin;

/**
 * Mod classes are entry points to mods. Each class is loaded when the corresponding plugin is loaded, and the {@link #init()} method is called afterwards. Every mod class must have a public no-arg constructor declared.
 * <p>
 * Mods are a specific subtypes of plugins. In other words, any plugin can contain a mod entry that defines custom behaviour via this class.
 *
 * @since 0.1.0
 */
public abstract class Mod {
	/**
	 * Default constructor for {@link Mod mods}. Any mod class must have a public no-arg constructor declared.
	 *
	 * @since 0.1.0
	 */
	public Mod() {
	}
	
	/**
	 * Initializes this mod. This method is called after the plugin's resources are loaded.
	 *
	 * @since 0.1.0
	 */
	public abstract void init();
}
