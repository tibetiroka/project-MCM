/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.resources.id;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Identifier for resources, tags, or anything else. Useful for creating registries.
 * <p>An identifier is made up of two parts: the group name and the name. None of these is case-sensitive, and they cannot contain any whitespace characters. For more info see {@link #isValidGroup(String)} and {@link #isValidName(String)}.
 * <br>The group name defines the group that created the identifier - this is useful for avoiding the use of the same id by different plugins.
 * <br>The 'name' is the name of the object the identifier refers to - it should be a meaningful name.
 * <p>The combination of the group and the name must be unique, otherwise resources may get overwritten. This may be the expected behaviour of some extensions, and this use is supported, but there is no guarantee made to the order of the extensions loading.
 * <p>Neither the group or the name is case-sensitive.
 * <p>By contract, an identifier must override its {@link Object#hashCode()} and {@link Object#equals(Object)} method in a way that makes identifiers equal if and only if their group and name match.
 *
 * @since 0.1.0
 */
public interface Identifier {
	/**
	 * The default group of an identifier. This is used for game resources.
	 *
	 * @since 0.1.0
	 */
	String DEFAULT_GROUP = "mcleod_massacre";
	/**
	 * The default group for a plugin's identifier.
	 *
	 * @since 0.1.0
	 */
	String DEFAULT_PLUGIN_GROUP = "plugin";
	
	/**
	 * Creates a new {@link Identifier} from the specified string.
	 *
	 * @param string The group and the name separated with a color
	 * @return The identifier if the input is valid, null otherwise
	 * @throws IllegalArgumentException If the string is null, if it contains more than one colon or if the group or the name is invalid.
	 * @since 0.1.0
	 */
	static @NotNull Identifier fromString(@Nullable String string) throws IllegalArgumentException {
		if(string == null) {
			throw new IllegalArgumentException("Input string cannot be null!");
		}
		String[] parts = string.split(":");
		
		if(parts.length == 1) {
			parts = new String[]{DEFAULT_GROUP, parts[0]};
		} else if(parts.length != 2) {
			throw new IllegalArgumentException("Input string cannot contain more that one colon (input: '" + string + "')!");
		}
		
		if(isValidGroup(parts[0]) && isValidName(parts[1])) {
			return new StandardIdentifier(parts[0], parts[1]);
		}
		throw new IllegalArgumentException("Illegal group or name specified for tag (input: '" + string + "')");
	}
	
	/**
	 * Creates a new {@link Identifier} from the specified group and name.
	 *
	 * @param group The group of the id
	 * @param name  The name of the id
	 * @return The identifier if the input is valid, null otherwise
	 * @throws IllegalArgumentException If the group or the name is invalid
	 * @since 0.1.0
	 */
	static @NotNull Identifier fromString(@Nullable String group, @Nullable String name) {
		if(isValidGroup(group) && isValidName(name)) {
			return new StandardIdentifier(group, name);
		}
		throw new IllegalArgumentException("The group or the name is invalid (group: '" + group + "', name: '" + name + "'");
	}
	
	/**
	 * Checks if the given string is valid as a group name for an identifier.
	 *
	 * @param group The group name
	 * @return True if valid
	 * @since 0.1.0
	 */
	static boolean isValidGroup(@Nullable String group) {
		return group != null && group.length() > 0 && !Pattern.matches(".*\\s+.*", group) && !group.contains(":");
	}
	
	/**
	 * Checks if the given string is valid as an element name for an identifier.
	 *
	 * @param name The name
	 * @return True if valid
	 * @since 0.1.0
	 */
	static boolean isValidName(@Nullable String name) {
		return name != null && name.length() > 0 && !Pattern.matches(".*\\s+.*", name) && !name.contains(":");
	}
	
	/**
	 * Gets the identifier's group.
	 *
	 * @return The group
	 * @see Identifier
	 * @since 0.1.0
	 */
	@NotNull String getGroup();
	
	/**
	 * Gets the {@link String} representation of this identifier. This consists of the group and the name separated by a colon.
	 *
	 * @return The string id
	 * @see Identifier
	 * @since 0.1.0
	 */
	default @NotNull String getId() {
		return getGroup() + ":" + getName();
	}
	
	/**
	 * Gets the name of the identifier.
	 *
	 * @return The name
	 * @see Identifier
	 * @since 0.1.0
	 */
	@NotNull String getName();
}
