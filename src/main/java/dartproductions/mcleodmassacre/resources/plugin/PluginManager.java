/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.resources.plugin;

import dartproductions.mcleodmassacre.resources.IllegalConfigurationException;
import dartproductions.mcleodmassacre.resources.ResourceManager;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class for finding, loading and managing plugin instances in general.
 *
 * @since 0.1.0
 */
public class PluginManager {
	protected static final Logger LOGGER = LogManager.getLogger(PluginManager.class);
	/**
	 * The class loader used for plugins. Null if the plugins are not loaded yet.
	 *
	 * @since 0.1.0
	 */
	private static @Nullable ClassLoader PLUGIN_CLASS_LOADER;
	
	/**
	 * Finds and initializes all plugins. For any plugin, there will be a {@link Plugin} instance created and registered in the plugin cache.
	 *
	 * @see ResourceManager#registerAssets(Plugin)
	 * @see ResourceManager#registerPlugin(Plugin)
	 * @since 0.1.0
	 */
	public static void findPlugins() {
		LOGGER.info("Searching for plugins...");
		File root = new File(".");
		if(root.exists()) {
			for(File folder : root.listFiles()) {
				if(folder.isDirectory()) {
					File pluginFile = new File(folder, "PLUGIN");
					if(pluginFile.exists()) {
						try {
							Plugin plugin = new Plugin(folder);
							ResourceManager.registerPlugin(plugin);
							LOGGER.info("Registered plugin " + plugin);
						} catch(Exception e) {
							LOGGER.warn("Could not load plugin in folder " + folder.getPath(), e);
						}
					}
				}
			}
		}
		ResourceManager.waitForLoading();
	}
	
	/**
	 * Loads all registered plugins via {@link #loadPlugin(Plugin)}. The loading order respects the plugins' {@link Plugin#getLoadBefore() loadbefore} and {@link Plugin#getLoadAfter() loadafter} specifications.
	 * <br>
	 * If multiple plugins specify each other or themselves as loadbefore/loadafter, the loading might not be possible. In these cases an {@link IllegalStateException} is thrown. There is no guarantee made to the loading of other plugins outside of this deadlock.
	 *
	 * @see #loadPlugin(Plugin)
	 * @since 0.1.0
	 */
	public static void loadPlugins() {
		ArrayList<Plugin> loadingOrder = new ArrayList<>();
		HashSet<URL> resourceLocations = new HashSet<>();
		HashMap<Identifier, HashSet<Identifier>> loadbefores = new HashMap<>();
		for(Identifier id : ResourceManager.getRegisteredPlugins()) {
			Plugin plugin = ResourceManager.getPlugin(id);
			loadbefores.put(plugin.getId(), (HashSet<Identifier>) plugin.getLoadBefore().clone());
		}
		for(Identifier id : ResourceManager.getRegisteredPlugins()) {
			Plugin plugin = ResourceManager.getPlugin(id);
			for(Identifier after : plugin.getLoadAfter()) {
				if(loadbefores.containsKey(after)) {
					loadbefores.get(after).add(plugin.getId());
				}
			}
		}
		while(!loadbefores.isEmpty()) {
			loadbefores.forEach((id, value) -> {//loading plugins without loadbefores
				if(value.isEmpty()) {
					Plugin plugin = ResourceManager.getPlugin(id);
					if(plugin != null) {
						LOGGER.info("Loading plugin " + plugin.getName() + ":" + plugin.getVersion());
						ResourceManager.registerAssets(plugin);
						loadingOrder.add(plugin);
						resourceLocations.addAll(getPaths(plugin));
						LOGGER.info("Loaded plugin " + plugin.getName() + ":" + plugin.getVersion());
					} else {
						LOGGER.error("Couldn't load plugin " + id + " (plugin doesn't exist)");
					}
				}
			});
			boolean changed = loadbefores.entrySet().removeIf(e -> e.getValue().isEmpty());//remove newly loaded plugins
			loadbefores.forEach((id, value) -> value.removeIf(i -> !loadbefores.containsKey(i)));//
			if(!changed) {
				throw new IllegalConfigurationException("Plugins cannot be loaded due to loadbefore/loadafter deadlock");
			}
		}
		createClassLoader(resourceLocations);
		for(Plugin plugin : loadingOrder) {
			loadPlugin(plugin);
		}
	}
	
	/**
	 * Loads a plugin. The plugins' resources must already be registered via {@link ResourceManager#registerPlugin(Plugin)} (or {@link ResourceManager#registerAssets(Plugin)}).
	 * <p>
	 * If this plugin contains resource jars and a mod entry, a new {@link ClassLoader} is created to make these resources available. If one or more of the resource jars are not available, they are excluded from the class loader, but the process doesn't terminate.
	 * <br>
	 * After the class loader is created, the mod's entry class is loaded from it. A new instance of the class is created assuming the mod has a no-arg constructor available, and the {@link Mod#init()} method is invoked.
	 *
	 * @param plugin The plugin to load
	 * @see Mod#init()
	 * @since 0.1.0
	 */
	private static void loadPlugin(@NotNull Plugin plugin) {
		if(plugin.getConfiguration().jar != null && plugin.getConfiguration().modEntry != null) {
			try {
				Class<? extends Mod> modClass = (Class<? extends Mod>) PLUGIN_CLASS_LOADER.loadClass(plugin.getConfiguration().modEntry);
				modClass.getDeclaredConstructor().newInstance().init();
			} catch(ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				LOGGER.warn("Could not load mod entry for plugin " + plugin.getConfiguration().name, e);
			}
		}
	}
	
	/**
	 * Gets the paths to the jar files as specified in the plugin's configuration.
	 *
	 * @param plugin The plugin to get the resources of
	 * @return The list of {@link URL URLs}
	 * @since 0.1.0
	 */
	private static ArrayList<URL> getPaths(@NotNull Plugin plugin) {
		ArrayList<URL> urls = new ArrayList<>();
		if(plugin.getConfiguration().jar != null && plugin.getConfiguration().modEntry != null) {
			for(int i = 0; i < plugin.getConfiguration().jar.length; i++) {
				try {
					urls.add(new File(plugin.getBaseDirectory(), plugin.getConfiguration().jar[i]).toURI().toURL());
				} catch(Exception e) {
					LOGGER.warn("Could not create URL to plugin resource jar for plugin " + plugin.getConfiguration().name, e);
				}
			}
		}
		return urls;
	}
	
	/**
	 * Creates the {@link #PLUGIN_CLASS_LOADER} using the specified {@link URL URLs} and the thread's current context class loader. Also sets this class loader as the context class loader of the application.
	 *
	 * @param urls The list of resources the loader needs to be able to load
	 * @since 0.1.0
	 */
	private static void createClassLoader(@NotNull Collection<URL> urls) {
		PLUGIN_CLASS_LOADER = new URLClassLoader("Class loader for plugins", urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(PLUGIN_CLASS_LOADER);
	}
}
