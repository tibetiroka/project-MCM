/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.resources.cache;

import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * {@link Cache} implementation that doesn't support unloading, but unlike {@link Registry}, doesn't load the resources when they are registered. Every resource is loaded when it is first accessed, and stay in the cache after that. The loader function for the resource is removed after it is loaded.
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
	 * The resource loaders
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull HashMap<Identifier, Callable<T>> loaders = new HashMap<>();
	
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
	public @Nullable T getFromCache(@NotNull Identifier id) {
		return cache.get(id);
	}
	
	@Override
	public @NotNull ParallelizationStrategy getParallelizationStrategy() {
		return ParallelizationStrategy.BALANCED_THREADING;
	}
	
	@Override
	public @NotNull AccessStrategy getReadAccessStrategy() {
		return AccessStrategy.PARALLEL;
	}
	
	@Override
	public @NotNull Set<Identifier> getRegisteredResources() {
		HashSet<Identifier> set = new HashSet<>();
		set.addAll(loaders.keySet());
		set.addAll(cache.keySet());
		return set;
	}
	
	@Override
	public @NotNull AccessStrategy getWriteAccessStrategy() {
		return AccessStrategy.SEQUENTIAL;
	}
	
	@Override
	public boolean isLoaded(@NotNull Identifier id) {
		return cache.get(id) != null;
	}
	
	@Override
	public boolean load(@NotNull Identifier id) {
		if(isLoaded(id)) {
			return true;
		}
		if(loaders.containsKey(id) && loaders.get(id) != null) {
			try {
				cache.put(id, loaders.remove(id).call());
				return true;
			} catch(Exception e) {
				LOGGER.error("Could not call resource loader in cache " + id, e);
				return false;
			}
		}
		return false;
	}
	
	@Override
	public void register(@NotNull Identifier id, @Nullable Callable<T> loader) {
		if(loader != null && !loaders.containsKey(id) && !cache.containsKey(id)) {
			loaders.put(id, loader);
		}
	}
	
	@Override
	public boolean unload(@NotNull Identifier id) {
		return false;
	}
	
	@Override
	public @NotNull Identifier getId() {
		return id;
	}
}
