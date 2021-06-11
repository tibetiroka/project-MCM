package dartproductions.mcleodmassacre.resources.cache;

import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Cache implementation that doesn't support unloading, and loads all resources as soon as they are registered. Useful for storing resources that can be used by the game at any time. It can also be used as a global registry.
 *
 * @param <T> The type of the stored resource
 * @since 0.1.0
 */
public class GreedyCache<T> implements Cache<T> {
	/**
	 * The cached resources
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull HashMap<Identifier, T> cache = new HashMap<>();
	
	/**
	 * The identifier of the cache
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Identifier id;
	
	/**
	 * Creates a new greedy cache.
	 *
	 * @param id The identifier of the cache
	 * @see GreedyCache
	 * @since 0.1.0
	 */
	public GreedyCache(@NotNull Identifier id) {
		this.id = id;
	}
	
	@Override
	public void register(@NotNull Identifier id, @Nullable Callable<T> loader) {
		try {
			if(!isLoaded(id) || loader != null) {
				cache.put(id, loader == null ? null : loader.call());
			}
		} catch(Exception e) {
			LOGGER.warn("Could not load resource to cache", e);
		}
	}
	
	@Override
	public @NotNull Set<Identifier> getRegisteredResources() {
		return cache.keySet();
	}
	
	@Override
	public boolean load(@NotNull Identifier id) {
		return isLoaded(id);
	}
	
	@Override
	public @Nullable T getFromCache(@NotNull Identifier id) {
		return cache.get(id);
	}
	
	@Override
	public boolean isLoaded(@NotNull Identifier id) {
		return cache.get(id) != null;
	}
	
	@Override
	public boolean unload(@NotNull Identifier id) {
		return false;
	}
	
	@Override
	public @NotNull AccessStrategy getReadAccessStrategy() {
		return AccessStrategy.PARALLEL;
	}
	
	@Override
	public @NotNull AccessStrategy getWriteAccessStrategy() {
		return AccessStrategy.SEQUENTIAL;
	}
	
	@Override
	public @NotNull ParallelizationStrategy getParallelizationStrategy() {
		return ParallelizationStrategy.BALANCED_THREADING;
	}
	
	@Override
	public @NotNull Identifier getId() {
		return id;
	}
}
