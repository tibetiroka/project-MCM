package dartproductions.mcleodmassacre.resources.tag;

import dartproductions.mcleodmassacre.GameState;
import dartproductions.mcleodmassacre.resources.ResourceManager;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Tag implementation which marks the resource as required if the current state extends the {@link #stateParent}, or if the current state is {@link GameState#isLoadingState()} and the next state requires the resource.
 *
 * @since 0.1.0
 */
public class GameStateTag implements Tag {
	/**
	 * The id of the tag
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Identifier id;
	
	/**
	 * The parent {@link GameState} that requires this tag
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Class<? extends GameState> stateParent;
	
	/**
	 * Creates a new tag.
	 *
	 * @param id          The identifier of the tag
	 * @param stateParent The parent {@link GameState} that requires this tag
	 * @see GameStateTag
	 * @since 0.1.0
	 */
	public GameStateTag(@NotNull Identifier id, @NotNull Class<? extends GameState> stateParent) {
		this.id = id;
		this.stateParent = stateParent;
		ResourceManager.registerTag(this);
	}
	
	@Override
	public @NotNull Identifier getId() {
		return id;
	}
	
	@Override
	public boolean isRequired(@NotNull GameState state, @Nullable GameState nextState) {
		return stateParent.isAssignableFrom(state.getClass()) || (state.isLoadingState() && stateParent.isAssignableFrom(nextState.getClass()));
	}
}
