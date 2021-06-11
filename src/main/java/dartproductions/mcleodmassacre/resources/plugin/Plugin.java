package dartproductions.mcleodmassacre.resources.plugin;

import com.google.gson.Gson;
import dartproductions.mcleodmassacre.resources.id.Identified;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;

/**
 * A class that describes a resource plugin. These plugins are basically separate resource roots, and have the ability to create or modify existing resources.
 * <p>
 * All plugins must have a separate folder placed in the game's installation folder. They must define a file inside their folder called 'PLUGIN' (without extension), which describes the {@link PluginConfiguration} in a JSON form.
 *
 * @since 0.1.0
 */
public class Plugin implements Identified {
	/**
	 * The plugin configuration loaded from the config file.ű
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull PluginConfiguration config;
	/**
	 * The base directory of the plugin
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull File directory;
	/**
	 * The identifier of the plugin; the group name is always {@link Identifier#DEFAULT_PLUGIN_GROUP}
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Identifier id;
	/**
	 * The plugins to load after this plugin
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull HashSet<Identifier> loadafter;
	/**
	 * The plugins to load before this plugin
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull HashSet<Identifier> loadbefore;
	
	/**
	 * Creates a new plugin.
	 *
	 * @param directory The base directory of the plugin
	 * @throws IOException
	 * @since 0.1.0
	 */
	public Plugin(@NotNull File directory) throws IOException {
		PluginManager.LOGGER.info("Registering plugin from directory " + directory);
		this.directory = directory;
		config = new Gson().fromJson(Files.readString(new File(directory, "PLUGIN").toPath()), PluginConfiguration.class);
		config.verify();
		id = Identifier.fromString(Identifier.DEFAULT_PLUGIN_GROUP, config.name);
		loadbefore = new HashSet<>();
		loadafter = new HashSet<>();
		//
		if(config.loadafter != null) {
			for(String s : config.loadafter) {
				loadafter.add(Identifier.fromString(Identifier.DEFAULT_PLUGIN_GROUP, s));
			}
		}
		//
		if(config.loadbefore != null) {
			for(String s : config.loadbefore) {
				loadbefore.add(Identifier.fromString(Identifier.DEFAULT_PLUGIN_GROUP, s));
			}
		}
		PluginManager.LOGGER.info("Read config for plugin " + this);
	}
	
	/**
	 * Gets the base directory of the plugin.
	 *
	 * @return The plugin's folder
	 * @since 0.1.0
	 */
	public @NotNull File getBaseDirectory() {
		return directory;
	}
	
	/**
	 * Gets the configuration of this plugin.
	 *
	 * @return The config
	 * @since 0.1.0
	 */
	public @NotNull PluginConfiguration getConfiguration() {
		return config;
	}
	
	@Override
	public @NotNull Identifier getId() {
		return id;
	}
	
	/**
	 * Gets the plugins to load after this plugin.
	 *
	 * @return The plugins to load after
	 * @since 0.1.0
	 */
	public @NotNull HashSet<Identifier> getLoadAfter() {
		return loadafter;
	}
	
	/**
	 * Gets the id of the plugins that must be loaded before this plugin.
	 *
	 * @return The plugins to load before
	 * @since 0.1.0
	 */
	public @NotNull HashSet<Identifier> getLoadBefore() {
		return loadbefore;
	}
	
	/**
	 * Gets the name of the plugin, as defined in the {@link PluginConfiguration} class.
	 *
	 * @return The name of the plugin
	 * @since 0.1.0
	 */
	public @NotNull String getName() {
		return config.name;
	}
	
	/**
	 * Gets the version of the plugin, as defined in the {@link PluginConfiguration} class.
	 *
	 * @return The version of the plugin
	 * @since 0.1.0
	 */
	public @NotNull String getVersion() {
		return config.version;
	}
	
	public @NotNull String toString() {
		return config.name + ":" + config.version;
	}
	
	/**
	 * JSON-parsable configuration for plugins
	 *
	 * @since 0.1.0
	 */
	public static final class PluginConfiguration {
		/**
		 * The jar files to load inside the plugin. These are relative paths from the base directory.
		 *
		 * @since 0.1.0
		 */
		protected @Nullable String[] jar;
		/**
		 * The id of the plugins to load after this one
		 *
		 * @since 0.1.0
		 */
		protected @Nullable String[] loadafter;
		/**
		 * The id of the plugins to load before this one
		 *
		 * @since 0.1.0
		 */
		protected @Nullable String[] loadbefore;
		/**
		 * The entry to the plugin's mod class. This must be a fully qualified class name of a class present in the loaded jars.
		 *
		 * @since 0.1.0
		 */
		protected @Nullable String modEntry;
		/**
		 * The name of this plugin
		 *
		 * @since 0.1.0
		 */
		protected @Nullable String name;
		/**
		 * The version of this plugin
		 *
		 * @since 0.1.0
		 */
		protected @Nullable String version;
		
		/**
		 * Verifies that this configuration is ready for use.
		 *
		 * @since 0.1.0
		 */
		protected void verify() {
			if(name == null) {
				throw new IllegalStateException("Plugin name must be specified");
			}
			if(version == null) {
				throw new IllegalStateException("Plugin version must be specified");
			}
		}
	}
}
