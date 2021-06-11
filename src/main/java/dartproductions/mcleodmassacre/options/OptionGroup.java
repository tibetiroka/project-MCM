/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * A simple interface for grouping and accessing multiple {@link Option settings}.
 *
 * @since 0.1.0
 */
public interface OptionGroup {
	
	/**
	 * Gets all options in this group.
	 *
	 * @return The options
	 * @since 0.1.0
	 */
	@NotNull HashMap<String, Option<?>> getAllSettings();
	
	/**
	 * Gets the name of the option group.
	 *
	 * @return The name
	 * @since 0.1.0
	 */
	@NotNull String getGroupName();
	
	/**
	 * Gets the specified setting from {@link #getAllSettings()}. The setting is cast to the specified type.
	 *
	 * @param name The name of the setting
	 * @param type The class of the option's value
	 * @param <T>  The type of the option's value
	 * @return The option or null if not found
	 * @since 0.1.0
	 */
	default @Nullable <T> Option<T> getSetting(String name, Class<T> type) {
		return (Option<T>) getSetting(name);
	}
	
	/**
	 * Gets the specified setting from {@link #getAllSettings()}
	 *
	 * @param name The name of the option
	 * @return The option or null if not found
	 * @since 0.1.0
	 */
	default @Nullable Option<?> getSetting(String name) {
		return getAllSettings().get(name);
	}
	
	/**
	 * Gets the value of an option.
	 *
	 * @param name The name of the option
	 * @param type The class of the value
	 * @param <T>  The type of the value
	 * @return The value
	 * @throws NullPointerException If the option is not found
	 * @since 0.1.0
	 */
	default @Nullable <T> T getSettingValue(@NotNull String name, @NotNull Class<T> type) throws NullPointerException {
		return getSetting(name, type).getValue();
	}
	
	class StandardOptionGroup implements OptionGroup {
		public final @NotNull HashMap<String, Option<?>> options = new HashMap<>();
		public @NotNull String name;
		
		public StandardOptionGroup(@NotNull String name) {
			this.name = name;
		}
		
		
		@Override
		public @NotNull HashMap<String, Option<?>> getAllSettings() {
			return options;
		}
		
		@Override
		public @NotNull String getGroupName() {
			return name;
		}
		
		public void setGroupName(@NotNull String name) {
			this.name = name;
		}
		
		public void setOption(@NotNull String name, @NotNull Option<?> option) {
			options.put(name, option);
			if(option.getVisibleName() == null) {
				option.setVisibleName(name);
			}
		}
	}
}
