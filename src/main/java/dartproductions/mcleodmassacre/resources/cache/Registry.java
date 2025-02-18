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
 * {@link Cache} implementation that doesn't support unloading, and loads all resources as soon as they are registered. Useful for storing resources that can be used by the game at any time.
 *
 * @param <T> The type of the stored resource
 * @since 0.1.0
 */
public class Registry<T> implements Cache<T> {
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
	 * Creates a new registry.
	 *
	 * @param id The identifier of the cache
	 * @see Registry
	 * @since 0.1.0
	 */
	public Registry(@NotNull Identifier id) {
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
		return cache.keySet();
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
		return isLoaded(id);
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
	public boolean unload(@NotNull Identifier id) {
		return false;
	}
	
	@Override
	public @NotNull Identifier getId() {
		return id;
	}
}
