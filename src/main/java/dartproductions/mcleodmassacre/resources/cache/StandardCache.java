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
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * A cache that support loading and unloading resources. Resources are not loaded by default.
 *
 * @since 0.1.0
 */
public class StandardCache<T> implements Cache<T> {
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
	 * The resource loaders
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull HashMap<Identifier, Callable<T>> loaders = new HashMap<>();
	
	/**
	 * Creates a new standard cache.
	 *
	 * @param id The id of the cache
	 * @since 0.1.0
	 */
	public StandardCache(@NotNull Identifier id) {
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
		return loaders.keySet();
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
				cache.put(id, loaders.get(id).call());
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
		if(loader != null || !loaders.containsKey(id)) {
			loaders.put(id, loader);
		}
	}
	
	@Override
	public boolean unload(@NotNull Identifier id) {
		if(!loaders.containsKey(id)) {
			return false;
		}
		if(isLoaded(id)) {
			cache.remove(id);
		}
		return true;
	}
	
	@Override
	public @NotNull Identifier getId() {
		return id;
	}
}
