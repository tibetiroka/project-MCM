package dartproductions.mcleodmassacre.resources.tag;

import dartproductions.mcleodmassacre.GameState;
import dartproductions.mcleodmassacre.resources.ResourceManager;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link Tag} implementation which marks the resource as required for any game state. Useful for menu resources.
 *
 * @since 0.1.0
 */
public class GreedyTag implements Tag {
	/**
	 * The id of the tag
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Identifier id;
	
	/**
	 * Creates a new greedy tag.
	 *
	 * @param id The identifier of the tag
	 * @see GreedyTag
	 * @since 0.1.0
	 */
	public GreedyTag(@NotNull Identifier id) {
		this.id = id;
		ResourceManager.registerTag(this);
	}
	
	@Override
	public @NotNull Identifier getId() {
		return id;
	}
	
	@Override
	public boolean isRequired(@NotNull GameState state, @Nullable GameState nextState) {
		return true;
	}
}
