/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.resources.plugin;

import dartproductions.mcleodmassacre.resources.ResourceManager;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
						} catch(IOException e) {
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
					loadPlugin(ResourceManager.getPlugin(id));
				}
			});
			boolean changed = loadbefores.entrySet().removeIf(e -> e.getValue().isEmpty());//remove newly loaded plugins
			loadbefores.forEach((id, value) -> value.removeIf(i -> !loadbefores.containsKey(i)));//
			if(!changed) {
				throw new IllegalStateException("Plugins cannot be loaded due to loadbefore/loadafter deadlock");
			}
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
			URL[] urls = new URL[plugin.getConfiguration().jar.length];
			for(int i = 0; i < plugin.getConfiguration().jar.length; i++) {
				try {
					urls[i] = new File(plugin.getBaseDirectory(), plugin.getConfiguration().jar[i]).toURI().toURL();
				} catch(MalformedURLException e) {
					LOGGER.warn("Could not create URL to plugin resource jar for plugin " + plugin.getConfiguration().name, e);
				}
			}
			final URLClassLoader loader = new URLClassLoader(urls);
			try {
				Class<? extends Mod> modClass = (Class<? extends Mod>) loader.loadClass(plugin.getConfiguration().modEntry);
				modClass.getDeclaredConstructor().newInstance().init();
			} catch(ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				LOGGER.warn("Could not load mod entry for plugin " + plugin.getConfiguration().name, e);
			}
		}
	}
}
