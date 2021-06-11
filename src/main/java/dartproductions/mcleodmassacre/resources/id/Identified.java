/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.resources.id;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for classes that have unique identifiers. The identifier returned must remain the same during the object's lifetime.
 *
 * @since 0.1.0
 */
public interface Identified {
	/**
	 * Gets the identifier of this object.
	 *
	 * @return The identifier
	 * @since 0.1.0
	 */
	@NotNull Identifier getId();
}
