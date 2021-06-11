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
	public @NotNull Identifier getId();
}
