package dartproductions.mcleodmassacre.resources.cache;

import dartproductions.mcleodmassacre.resources.id.Identified;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiPredicate;

/**
 * Wrapper for caches that can load/unload resources on demand.
 *
 * @param <T> The type of the stored resources
 * @since 0.1.0
 */
public interface Cache<T> extends Identified {
	Logger LOGGER = LogManager.getLogger(Cache.class);
	
	/**
	 * Loads all registered resources.
	 *
	 * @since 0.1.0
	 */
	default void loadAll() {
		switch(getParallelizationStrategy()) {
			case FORCE_SINGLE_THREAD: {
				getRegisteredResources().stream().filter(id -> !isLoaded(id)).forEach(this::load);
			}
			break;
			case BALANCED_THREADING: {
				if(getRegisteredResources().size() < 16) {
					getRegisteredResources().stream().filter(id -> !isLoaded(id)).forEach(this::load);
					break;
				}
			}
			case FORCE_MULTI_THREAD: {
				getRegisteredResources().parallelStream().filter(id -> !isLoaded(id)).forEach(this::load);
			}
		}
		
	}
	
	/**
	 * Unloads all registered resources.
	 *
	 * @since 0.1.0
	 */
	default void unloadAll() {
		for(Identifier id : getRegisteredResources()) {
			unload(id);
		}
	}
	
	/**
	 * Registers a new resource without providing a specific loader. This can not be used to clear the loader of an existing resource.
	 *
	 * @param id The identifier of the resource
	 * @see #register(Identifier, Callable)
	 * @since 0.1.0
	 */
	default void register(@NotNull Identifier id) {
		register(id, null);
	}
	
	/**
	 * Registers a new resource. The resource is specified by the loader function that is called on every {@link #load(Identifier)} method. The 'loader' parameter can be used to replace existing loaders, or it can be null to specify that this resource cannot be loaded yet. However, null values cannot 'clear' existing loaders for the resource.
	 *
	 * @param id     The identifier of the resource
	 * @param loader The loader function
	 * @since 0.1.0
	 */
	void register(@NotNull Identifier id, @Nullable Callable<T> loader);
	
	/**
	 * Registers an {@link Identified} value, assuming it is also an instance of {@link T}. This method does NOT check if the value is a valid {@link T} - passing invalid values could result in runtime errors or undefined behaviour.
	 *
	 * @param value The value to register
	 * @since 0.1.0
	 */
	default void register(@NotNull Identified value) {
		register(value.getId(), () -> (T) value);
	}
	
	/**
	 * Gets a set of identifiers that corresponds to the registered resource. There is no guarantee made that changes made to this set are ignored or reflected on the cache.
	 *
	 * @return The registered identifiers
	 * @since 0.1.0
	 */
	@NotNull Set<Identifier> getRegisteredResources();
	
	/**
	 * Loads the specified resource. Fails silently if the identifier isn't registered (returns false). Returns true if the resource is registered and loaded when calling this method.
	 *
	 * @param id The identifier of the resource
	 * @return True if the id is registered and the resource is loaded
	 * @since 0.1.0
	 */
	boolean load(@NotNull Identifier id);
	
	/**
	 * Gets the resource with the specified ID. The resource is loaded if it is not loaded in the cache.
	 *
	 * @param id The identifier of the resource
	 * @return The resource or null if it isn't registered or has no loader
	 * @since 0.1.0
	 */
	default @Nullable T get(@NotNull Identifier id) {
		if(!isLoaded(id)) {
			load(id);
		}
		return getFromCache(id);
	}
	
	/**
	 * Gets the specified resource from the cache. Returns null if the identifier is not registered of it is not loaded
	 *
	 * @param id The identifier of the resource
	 * @return The cached data
	 * @since 0.1.0
	 */
	@Nullable T getFromCache(@NotNull Identifier id);
	
	/**
	 * Checks if the specified resource is loaded.
	 *
	 * @param id The identifier of the resource
	 * @return True if loaded
	 * @since 0.1.0
	 */
	boolean isLoaded(@NotNull Identifier id);
	
	/**
	 * Unloads the specified resource. Fails silently if the identifier is not registered (returns false). Returns true if the resource is already unloaded when calling this method. May return false for existing resources if the cache doesn't support unloading.
	 *
	 * @param id The ID of the resource
	 * @return True if the resource is unloaded
	 * @since 0.1.0
	 */
	boolean unload(@NotNull Identifier id);
	
	/**
	 * Unloads any resources that meet the specified predicate. The predicate is given the id and the cached value of the resource. The predicate is only tested on loaded resources.
	 *
	 * @param predicate The predicate to test
	 * @since 0.1.0
	 */
	default void unloadIf(@NotNull BiPredicate<Identifier, T> predicate) {
		ArrayList<Identifier> list = new ArrayList<>();
		for(Identifier id : getRegisteredResources()) {
			if(isLoaded(id) && predicate.test(id, getFromCache(id))) {
				list.add(id);
			}
		}
		for(Identifier identifier : list) {
			unload(identifier);
		}
	}
	
	/**
	 * Loads any resources that meet the specified predicate. The predicate is given the id and the cached value of the resource. The predicate is only tested on unloaded resources.
	 *
	 * @param predicate The predicate to test
	 * @since 0.1.0
	 */
	default void loadIf(@NotNull BiPredicate<Identifier, T> predicate) {
		ArrayList<Identifier> list = new ArrayList<>();
		for(Identifier id : getRegisteredResources()) {
			if(!isLoaded(id) && predicate.test(id, getFromCache(id))) {
				list.add(id);
			}
		}
		for(Identifier identifier : list) {
			load(identifier);
		}
	}
	
	/**
	 * Gets the access strategy used for reading cached values. Although this data is visible to potential callers, implementations are not required to (and generally don't) support changing this strategy. The return value of this method can be used to optimize the calls made to this cache, however, enforcing the specified behaviour is up to the cache.
	 *
	 * @return The used access strategy
	 * @since 0.1.0
	 */
	@NotNull AccessStrategy getReadAccessStrategy();
	
	/**
	 * Gets the access strategy used for writing to the cache. This includes registering new identifiers and loading resources. Although this data is visible to potential callers, implementations are not required to (and generally don't) support changing this strategy. The return value of this method can be used to optimize the calls made to this cache, however, enforcing the specified behaviour is up to the cache.
	 *
	 * @return The used access strategy
	 * @since 0.1.0
	 */
	@NotNull AccessStrategy getWriteAccessStrategy();
	
	/**
	 * Gets the parallelization strategy used for loading resources, or for any other long tasks. Although this data is visible to potential callers, implementations are not required to (and generally don't) support changing this strategy. The return value of this method can be used to optimize the calls made to this cache, however, enforcing the specified behaviour is up to the cache.
	 *
	 * @return The used parallelization strategy
	 * @since 0.1.0
	 */
	@NotNull ParallelizationStrategy getParallelizationStrategy();
	
	/**
	 * Specifies different specifications for accessing cached resources. Implementations should take these values into account or fall back to {@link #SEQUENTIAL} if they cannot support all values.
	 *
	 * @since 0.1.0
	 */
	enum AccessStrategy {
		/**
		 * The cache can be accessed from multiple threads, but only one thread can access it at any time. Any other thread may be forced to wait until the operation completes.
		 *
		 * @since 0.1.0
		 */
		SEQUENTIAL,
		/**
		 * Cached values can be accessed from multiple threads at the same time. Accessing threads must not be delayed.
		 *
		 * @since 0.1.0
		 */
		PARALLEL,
		/**
		 * Only a single thread has access to the resources. Any other thread that tries to do so may get faulty results or fail to query the data.
		 *
		 * @since 0.1.0
		 */
		SINGLE_THREAD_ONLY
	}
	
	/**
	 * Specifies different specifications for splitting tasks with high resource usage to multiple threads. Implementations should take these values into account or fall back to {@link #FORCE_SINGLE_THREAD} if they cannot support all values.
	 *
	 * @since 0.1.0
	 */
	enum ParallelizationStrategy {
		/**
		 * All operations are executed on a single thread.
		 *
		 * @since 0.1.0
		 */
		FORCE_SINGLE_THREAD,
		/**
		 * Short operations are executed on a single thread, but longer ones are parallelized.
		 *
		 * @since 0.1.0
		 */
		BALANCED_THREADING,
		/**
		 * All operations are executed on multiple threads.
		 *
		 * @since 0.1.0
		 */
		FORCE_MULTI_THREAD
	}
}
