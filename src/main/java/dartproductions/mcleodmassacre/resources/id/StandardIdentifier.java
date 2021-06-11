package dartproductions.mcleodmassacre.resources.id;

import dartproductions.mcleodmassacre.util.Pair.ImmutablePair.ImmutableNullsafePair;
import org.jetbrains.annotations.NotNull;

/**
 * Standard implementation of {@link Identifier}.
 *
 * @since 0.1.0
 */
final class StandardIdentifier extends ImmutableNullsafePair<String, String> implements Identifier {
	
	/**
	 * Creates a new standard identifier.
	 *
	 * @param group The group
	 * @param name  The name
	 * @see Identifier
	 * @since 0.1.0
	 */
	public StandardIdentifier(@NotNull String group, @NotNull String name) {
		super(group.toLowerCase(), name.toLowerCase());
	}
	
	@Override
	public @NotNull String getGroup() {
		return first();
	}
	
	@Override
	public @NotNull String getName() {
		return second();
	}
	
	@Override
	public String toString() {
		return getId();
	}
}
