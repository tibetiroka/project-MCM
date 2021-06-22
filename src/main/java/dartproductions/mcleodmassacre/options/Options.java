/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.options;

import dartproductions.mcleodmassacre.options.Option.BooleanOption;
import dartproductions.mcleodmassacre.options.Option.EnumOption;
import dartproductions.mcleodmassacre.options.Option.IntOption;
import org.jetbrains.annotations.NotNull;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An option listing interface that resembles the user-friendly visual display type.
 *
 * @since 0.1.0
 */
public interface Options extends OptionGroup {
	
	/**
	 * Gets the default settings for the game.
	 *
	 * @return The default settings
	 * @since 0.1.0
	 */
	static @NotNull Options getDefaultOptions() {
		return new StandardOptions();
	}
	
	/**
	 * Gets a list of the option groups. These groups can be shown to the user as individual option tabs.
	 *
	 * @return The option groups
	 * @since 0.1.0
	 */
	@NotNull List<OptionGroup> getGroups();
	
	/**
	 * The standard game options
	 *
	 * @since 0.1.0
	 */
	class StandardOptions implements Options {
		public static final String SOUND_OPTIONS = "Sounds", GRAPHICS_OPTIONS = "Graphics", CONTROLS = "Controls", WIDTH = "Width", HEIGHT = "Height", FULLSCREEN = "Fullscreen", QUALITY = "Quality", MUSIC_VOLUME = "Music", SFX_VOLUME = "Sound FX";
		
		public final @NotNull ArrayList<OptionGroup> groups = new ArrayList<>();
		public final @NotNull String name = "Settings";
		
		public StandardOptions() {
			{
				StandardOptionGroup graphics = new StandardOptionGroup(GRAPHICS_OPTIONS);
				graphics.setOption(WIDTH, new IntOption((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()));
				graphics.setOption(HEIGHT, new IntOption((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()));
				graphics.setOption(FULLSCREEN, new BooleanOption(false));
				graphics.setOption(QUALITY, new EnumOption<>(QualityOption.HIGH));
				groups.add(graphics);
			}
			{
				StandardOptionGroup sound = new StandardOptionGroup(SOUND_OPTIONS);
				sound.setOption(MUSIC_VOLUME, new IntOption(50));
				sound.setOption(SFX_VOLUME, new IntOption(50));
				groups.add(sound);
			}
		}
		
		@Override
		public @NotNull HashMap<String, Option<?>> getAllSettings() {
			HashMap<String, Option<?>> options = new HashMap<>();
			for(OptionGroup group : getGroups()) {
				options.putAll(group.getAllSettings());
			}
			return options;
		}
		
		@Override
		public @NotNull String getGroupName() {
			return name;
		}
		
		@Override
		public @NotNull List<OptionGroup> getGroups() {
			return groups;
		}
	}
}
