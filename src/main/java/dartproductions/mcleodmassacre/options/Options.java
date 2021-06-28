/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.options;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;
import dartproductions.mcleodmassacre.options.Option.EnumOption;
import dartproductions.mcleodmassacre.options.Option.IntOption;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
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
		/**
		 * Group name for the sound options group
		 *
		 * @since 0.1.0
		 */
		public static final String SOUND_OPTIONS = "Sounds";
		/**
		 * Group name for the graphics options group
		 *
		 * @since 0.1.0
		 */
		public static final String GRAPHICS_OPTIONS = "Graphics";
		/**
		 * Group name for the controls options group
		 *
		 * @since 0.1.0
		 */
		public static final String CONTROLS = "Controls";
		/**
		 * Group name for the game quality options group
		 *
		 * @since 0.1.0
		 */
		public static final String QUALITY = "Quality";
		/**
		 * Name of the music volume setting
		 *
		 * @since 0.1.0
		 */
		public static final String MUSIC_VOLUME = "Music";
		/**
		 * Name of the sfx volume setting
		 *
		 * @since 0.1.0
		 */
		public static final String SFX_VOLUME = "Sound FX";
		@JsonAdapter(OptionGroupListAdapter.class)
		/**
		 * The option groups
		 * @since 0.1.0
		 */
		public final @NotNull ArrayList<OptionGroup> groups = new ArrayList<>();
		/**
		 * The name of this group
		 *
		 * @since 0.1.0
		 */
		public final @NotNull String name = "Settings";
		
		/**
		 * Creates a new {@link StandardOptions} instance with the default option groups and settings.
		 *
		 * @since 0.1.0
		 */
		public StandardOptions() {
			{
				StandardOptionGroup graphics = new StandardOptionGroup(GRAPHICS_OPTIONS);
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
		
		/**
		 * Json serializer and deserializer for the list of option groups ({@link #groups}).
		 *
		 * @see com.google.gson.JsonDeserializer
		 * @see com.google.gson.JsonSerializer
		 * @see com.google.gson.annotations.JsonAdapter
		 * @since 0.1.0
		 */
		private static final class OptionGroupListAdapter implements JsonSerializer<ArrayList<OptionGroup>>, JsonDeserializer<ArrayList<OptionGroup>> {
			
			@Override
			public ArrayList<OptionGroup> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				ArrayList<OptionGroup> options = new ArrayList<>();
				for(JsonElement jsonElement : json.getAsJsonArray()) {
					options.add(context.deserialize(jsonElement, StandardOptionGroup.class));
				}
				return options;
			}
			
			@Override
			public JsonElement serialize(ArrayList<OptionGroup> src, Type typeOfSrc, JsonSerializationContext context) {
				return context.serialize(src.toArray(new OptionGroup[0]));
			}
		}
	}
}
