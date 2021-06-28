/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dartproductions.mcleodmassacre.GameState;
import dartproductions.mcleodmassacre.Main;
import dartproductions.mcleodmassacre.engine.GameEngine;
import dartproductions.mcleodmassacre.hitbox.ImageHitbox;
import dartproductions.mcleodmassacre.map.Map;
import dartproductions.mcleodmassacre.options.Options;
import dartproductions.mcleodmassacre.options.Options.StandardOptions;
import dartproductions.mcleodmassacre.resources.cache.Cache;
import dartproductions.mcleodmassacre.resources.cache.Registry;
import dartproductions.mcleodmassacre.resources.cache.StandardCache;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import dartproductions.mcleodmassacre.resources.plugin.Plugin;
import dartproductions.mcleodmassacre.resources.tag.GameStateTag;
import dartproductions.mcleodmassacre.resources.tag.GreedyTag;
import dartproductions.mcleodmassacre.resources.tag.IgnorantTag;
import dartproductions.mcleodmassacre.resources.tag.Tag;
import dartproductions.mcleodmassacre.util.Pair.ImmutablePair.ImmutableNullsafePair;
import de.cerus.jgif.GifImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Accesses and handles stored game resources.
 *
 * @since 0.1.0
 */
public class ResourceManager {
	protected static final Logger LOGGER = LogManager.getLogger(ResourceManager.class);
	/**
	 * The loaded sound effects; the id is the file's name without extension. The group of the id is the name of the plugin the effect is loaded from
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Cache<byte[]> AUDIO = new StandardCache<>(Identifier.fromString("resources/sfx"));
	/**
	 * List of resources that are available for unloading, but at the time of checking their unloading threshold was not reached.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull HashSet<Identifier> AVAILABLE_UNLOADS = new HashSet<>();
	/**
	 * The list of created caches. Caches are not required to be registered here, but it is recommended.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Cache<Cache<?>> CACHES = new Registry<>(Identifier.fromString("resources/caches"));
	/**
	 * Hitboxes created from the loaded images; the id is the file's name without extension, with possibly a #number attached to it if it is a frame from a GIF. The group of the id is the name of the plugin the image is loaded from
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Cache<ImageHitbox> HITBOXES = new Registry<>(Identifier.fromString("resources/hitboxes"));
	/**
	 * The loaded images; the id is the file's name without extension, with possibly a #number attached to it if it is a frame from a GIF. The group of the image's id is the name of the plugin the image is loaded from
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Cache<Image> IMAGES = new StandardCache<>(Identifier.fromString("resources/images"));
	/**
	 * The amount of loading operations currently running.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull AtomicInteger LOADING_OPERATIONS = new AtomicInteger(0);
	/**
	 * The loaded plugins; the id is the plugins' id - the group is the {@link Identifier#DEFAULT_PLUGIN_GROUP}.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Cache<Plugin> PLUGINS = new Registry<>(Identifier.fromString("resources/plugins"));
	/**
	 * The tags of all registered resources. Some resources may not have any tags assigned to them. The id is the resources' id, and the values are the id's of the tags.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Cache<HashSet<Identifier>> RESOURCE_TAGS = new Registry<>(Identifier.fromString("resources/resource_tags"));
	/**
	 * The loaded maps
	 */
	private static final @NotNull Cache<Map> MAPS = new Registry<>(Identifier.fromString("resources/maps"));
	/**
	 * The created tags; the id is the plugin's id.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Cache<Tag> TAGS = new Registry<>(Identifier.fromString("resources/tags"));
	/**
	 * The list of resources associated with each tag. The id is the tag's id, and the cached values are the id's of resources associated with that tag.
	 */
	private static final @NotNull Cache<HashSet<Identifier>> TAG_RESOURCES = new Registry<>(Identifier.fromString("resources/tag_resources"));
	/**
	 * The active game options
	 *
	 * @since 0.1.0
	 */
	private static volatile @Nullable Options OPTIONS;
	
	static {
		CACHES.register(AUDIO);
		CACHES.register(CACHES);
		CACHES.register(HITBOXES);
		CACHES.register(IMAGES);
		CACHES.register(PLUGINS);
		CACHES.register(RESOURCE_TAGS);
		CACHES.register(TAGS);
		CACHES.register(TAG_RESOURCES);
	}
	
	/**
	 * Extracts all resources from the application jar.
	 *
	 * @since 0.1.0
	 */
	public static void extractResources() {
		if(checkVersion()) {
			LOGGER.info("Skipping resource extraction: already done for this version");
		} else {
			LOGGER.info("Extracting resources");
			try {
				{//deleting old data directory
					File file = new File("data");
					if(file.exists()) {
						delete(file);
					}
					file = new File("lib");
					if(file.exists()) {
						delete(file);
					}
				}
				Path p = getPathToResource("extract", true);
				Stream<Path> paths = getPaths("extract", true);
				paths.forEach(path -> {
					if(!path.equals(p)) {
						String newPath = path.toString();
						newPath = newPath.substring(newPath.indexOf("extract") + "extract".length() + 1);
						try {
							Files.copy(path, new File(newPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
							LOGGER.debug("Extracted file " + newPath);
						} catch(DirectoryNotEmptyException e) {
						} catch(Exception e) {
							LOGGER.error("Error during file extraction", e);
						}
					}
				});
			} catch(Exception e) {
				LOGGER.error("Error during resource extraction", e);
			}
		}
	}
	
	/**
	 * Gets the audio resource associated with the specified id.
	 *
	 * @param id The identifier of the audio
	 * @return The audio resource
	 * @since 0.1.0
	 */
	public static @Nullable byte[] getAudio(@NotNull Identifier id) {
		return AUDIO.get(id);
	}
	
	/**
	 * Gets the image with the specified id as a {@link BufferedImage}.
	 *
	 * @param id The id of the image
	 * @return The image or null if not found
	 * @since 0.1.0
	 */
	public static @Nullable BufferedImage getBufferedImage(@NotNull Identifier id) {
		Image i = getImage(id);
		if(i instanceof BufferedImage image) {
			return image;
		}
		return null;
	}
	
	/**
	 * Gets the cache where the resource is stored.
	 *
	 * @param resourceId The id of the resource
	 * @return The cache of the resource; doesn't return null for valid graphics/audio entries
	 * @since 0.1.0
	 */
	public static @Nullable Cache<?> getCacheOfResource(@NotNull Identifier resourceId) {
		if(hasTag(resourceId, Tag.GRAPHICS.getId())) {
			return IMAGES;
		}
		if(hasTag(resourceId, Tag.AUDIO.getId())) {
			return AUDIO;
		}
		if(hasTag(resourceId, Tag.MAP.getId())) {
			return MAPS;
		}
		if(hasTag(resourceId, Tag.TAG.getId())) {
			return TAGS;
		}
		return null;//shouldn't happen due to NPE during registration, but I don't want to add an else clause
	}
	
	/**
	 * Gets all registered caches. Any changes made to this cache might reflect on the behaviour of {@link ResourceManager}. It is possible that other caches exists outside of the registered cache, but they are not tracked by {@link ResourceManager}, and although this behaviour is allowed, it is not considered good practice. Any non-greedy cache outside of this cache will act like a greedy cache, unless it is manually handled on every state change.
	 *
	 * @return The cache of caches
	 * @since 0.1.0
	 */
	public static @NotNull Cache<Cache<?>> getCaches() {
		return CACHES;
	}
	
	/**
	 * Gets the hitbox area with the specified id
	 *
	 * @param id The id of the hitbox area
	 * @return The hitbox area or null if not found
	 * @since 0.1.0
	 */
	public static @Nullable ImageHitbox getHitbox(@NotNull Identifier id) {
		return HITBOXES.get(id);
	}
	
	/**
	 * Gets the image with the specified id
	 *
	 * @param id The id of the image
	 * @return The image or null if not found
	 * @since 0.1.0
	 */
	public static @Nullable Image getImage(@NotNull Identifier id) {
		return IMAGES.get(id);
	}
	
	/**
	 * Gets the active game options. Loads them from the settings file if they are not yet loaded.
	 *
	 * @return The options
	 * @since 0.1.0
	 */
	public static @NotNull Options getOptions() {
		if(OPTIONS == null) {
			loadSettings();
		}
		return OPTIONS;
	}
	
	/**
	 * Gets a Path to the specified location in the file system.
	 *
	 * @param location The location
	 * @param forceJar True if the location must be inside the application jar/classpath
	 * @return The path
	 * @throws IOException        If an exception occurs
	 * @throws URISyntaxException If an exception occurs
	 * @since 0.1.0
	 */
	public static synchronized @NotNull Path getPathToResource(@NotNull String location, boolean forceJar) throws IOException, URISyntaxException {
		if(!forceJar && new File(location).exists()) {
			return new File(location).toPath();
		}
		URI uri = Thread.currentThread().getContextClassLoader().getResource(location).toURI();
		Path myPath;
		if(uri.getScheme().equalsIgnoreCase("jar")) {
			FileSystem fileSystem = null;
			try {
				fileSystem = FileSystems.getFileSystem(uri);
				if(fileSystem == null || !fileSystem.isOpen()) {
					fileSystem = null;
				}
			} catch(Exception e) {
			}
			if(fileSystem == null) {
				fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
			}
			myPath = fileSystem.getPath(location);
		} else {
			myPath = Paths.get(uri);
		}
		return myPath;
	}
	
	/**
	 * Gets all paths to resources in the specified location.
	 *
	 * @param location The location
	 * @param depth    The depth of the search
	 * @param forceJar True if the location must be inside the application jar/classpath
	 * @return The paths
	 * @throws IOException        If an exception occurs
	 * @throws URISyntaxException If an exception occurs
	 * @since 0.1.0
	 */
	public static @NotNull Stream<Path> getPaths(@NotNull String location, int depth, boolean forceJar) throws IOException, URISyntaxException {
		Path myPath = getPathToResource(location, forceJar);
		return Files.walk(myPath, depth);
	}
	
	/**
	 * Gets all paths to resources in the specified location. The default search depth is 60.
	 *
	 * @param location The location
	 * @param forceJar True if the location must be inside the application jar/classpath
	 * @return The paths
	 * @throws IOException        If an exception occurs
	 * @throws URISyntaxException If an exception occurs
	 * @since 0.1.0
	 */
	public static @NotNull Stream<Path> getPaths(@NotNull String location, boolean forceJar) throws IOException, URISyntaxException {
		return getPaths(location, 60, forceJar);
	}
	
	/**
	 * Gets the plugin with the specified id.
	 *
	 * @param id The id of the plugin
	 * @return The plugin or null if not found
	 * @since 0.1.0
	 */
	public static @Nullable Plugin getPlugin(Identifier id) {
		return PLUGINS.get(id);
	}
	
	/**
	 * Gets a random audio resource which has the specified tag.
	 *
	 * @param tag The tag' id
	 * @return A pair containing the resource's id and the resource itself; or null if no valid values are found
	 * @since 0.1.0
	 */
	public static @Nullable ImmutableNullsafePair<Identifier, byte[]> getRandomAudio(@Nullable Identifier tag) {
		if(tag == null) {
			return null;
		}
		ArrayList<Identifier> options = new ArrayList<>();
		for(Identifier resource : AUDIO.getRegisteredResources()) {
			if(hasTag(resource, tag)) {
				options.add(resource);
			}
		}
		if(options.isEmpty()) {
			return null;
		}
		int choice = (int) (Math.random() * options.size());
		return new ImmutableNullsafePair<>(options.get(choice), AUDIO.get(options.get(choice)));
	}
	
	/**
	 * Gets the list of registered plugins
	 *
	 * @return The plugins' id
	 * @since 0.1.0
	 */
	public static @NotNull Set<Identifier> getRegisteredPlugins() {
		return PLUGINS.getRegisteredResources();
	}
	
	/**
	 * Checks if the resource has the specified tag attached to it.
	 *
	 * @param resource The id of the resource
	 * @param tag      The id of the tag
	 * @return True if the tag is present
	 * @since 0.1.0
	 */
	public static boolean hasTag(@Nullable Identifier resource, @Nullable Identifier tag) {
		if(tag == null || resource == null) {
			return false;
		}
		return RESOURCE_TAGS.isLoaded(resource) && RESOURCE_TAGS.isLoaded(resource) && RESOURCE_TAGS.get(resource).contains(tag);
	}
	
	/**
	 * Runs whenever the game's state changes. This method handles resource loading/unloading for the state. Calling this method may block the current thread for a significant time.
	 *
	 * @param newState     The new game state
	 * @param newNextState The new next game state
	 * @since 0.1.0
	 */
	public static void onStateChange(@NotNull GameState newState, @Nullable GameState newNextState) {
		waitForLoading();
		double memoryUsage = getMemoryUsage();
		synchronized(AVAILABLE_UNLOADS) {
			AVAILABLE_UNLOADS.clear();
			for(Identifier resourceId : RESOURCE_TAGS.getRegisteredResources()) {
				HashSet<Identifier> tags = RESOURCE_TAGS.get(resourceId);
				Cache<?> cache = getCacheOfResource(resourceId);
				if(cache != null) {
					boolean loaded = cache.isLoaded(resourceId);
					if(tags.stream().map(TAGS::get).parallel().anyMatch(tag -> tag.isRequired(newState, newNextState))) {
						if(!loaded) {
							//LOGGER.debug("Loaded " + resourceId);
							cache.load(resourceId);
						}
					} else if(loaded) {
						if(tags.stream().map(TAGS::get).parallel().anyMatch(tag -> tag.getUnloadingThreshold(newState, newNextState) < memoryUsage)) {
							//LOGGER.debug("Unloaded " + resourceId);
							cache.unload(resourceId);
						} else {
							//LOGGER.debug("Scheduled " + resourceId);
							AVAILABLE_UNLOADS.add(resourceId);
						}
					}
				}
			}
		}
		//Main.getExecutors().execute(new LoadingOperation(() -> unloadAll(0)));
	}
	
	/**
	 * Unloads all resources that can be unloaded but are still loaded in the cache, if the memory usage is above the specified threshold.
	 *
	 * @param threshold The ratio of used memory required to start unloading
	 * @since 0.1.0
	 */
	public static void unloadAll(double threshold) {
		synchronized(AVAILABLE_UNLOADS) {
			if(!AVAILABLE_UNLOADS.isEmpty() && getMemoryUsage() > threshold) {
				int count = AVAILABLE_UNLOADS.size();
				for(Identifier resource : AVAILABLE_UNLOADS) {
					getCacheOfResource(resource).unload(resource);
				}
				AVAILABLE_UNLOADS.clear();
				LOGGER.debug("Manually unloaded " + count + " resources");
			}
		}
	}
	
	/**
	 * Reads the contents of a text file.
	 *
	 * @param path The path to the text file
	 * @return The contents of the file, separated at each line
	 */
	public static List<String> readTextFile(String path) throws IOException {
		return Files.readAllLines(new File(path).toPath());
	}
	
	/**
	 * Registers all assets from the specified plugin. The assets' identifiers are registered in the appropriate caches, and their tags are attached to the resources. The name of the assets (the 'name' parameter of their ID's) is the name of the files containing them without the file extension.
	 * <p>
	 * This method does not block the calling thread. To ensure the plugin is loaded one must call the {@link #waitForLoading()} method.
	 *
	 * @param plugin The plugin to load assets from
	 * @since 0.1.0
	 */
	public static void registerAssets(@NotNull final Plugin plugin) {
		waitForLoading();
		try {
			getPaths(plugin.getBaseDirectory().getPath(), false).filter(path -> "tags".equalsIgnoreCase(getFileExtension(path.toFile()))).forEach(path -> {
				new LoadingOperation(() -> {
					Identifier resourceId = Identifier.fromString(plugin, getFileName(path.toFile()));
					HashSet<Identifier> tags = new HashSet<>();
					File resourceFile = getResourceFileFromTags(path);
					try {
						for(String line : Files.readAllLines(path)) {
							line = line.strip().trim();
							if(line.isEmpty()) {
								continue;
							}
							if(Pattern.matches(".+::.+", line)) {//contains :: -> not just a tag entry
								String[] parts = line.split("::");
								switch(parts[0]) {//the operation or something
									case "id" -> resourceId = Identifier.fromString(parts[1]);//changes the resource's id
									case "location" -> resourceFile = new File(plugin.getBaseDirectory(), line.substring("location::".length()).trim().strip());//changes the resource's location
									default -> LOGGER.warn("Illegal entry for asset: '" + line + "'");
								}
							} else {
								tags.add(Identifier.fromString(line));
							}
						}
					} catch(IOException | IllegalArgumentException e) {
						LOGGER.warn("Could not read tags for file " + path + " in plugin " + plugin, e);
					}
					for(Identifier tag : tags) {
						registerResourceTag(resourceId, tag);
					}
					registerResource(resourceId, resourceFile);
				});
			});
		} catch(IOException | URISyntaxException | NullPointerException e) {
			LOGGER.warn("Could not register assets for plugin " + plugin, e);
		}
		waitForLoading();
	}
	
	/**
	 * Registers the specified plugin.
	 *
	 * @param plugin The plugin to register
	 * @since 0.1.0
	 */
	public static void registerPlugin(@NotNull Plugin plugin) {
		PLUGINS.register(plugin.getId(), () -> plugin);
	}
	
	/**
	 * Registers a new tag.
	 *
	 * @param tag The tag to register
	 * @since 0.1.0
	 */
	public static void registerTag(Tag tag) {
		TAGS.register(tag);
		LOGGER.debug("Registered tag " + tag);
	}
	
	/**
	 * Waits until all async loading operations are done. While this makes sure that there is no loading operations active on other executors, it is possible that other threads use blocking methods for loading, or that by the time this thread handles the return of this method a new async loading has started.
	 *
	 * @since 0.1.0
	 */
	public static void waitForLoading() {
		synchronized(LOADING_OPERATIONS) {
			if(LOADING_OPERATIONS.get() > 0) {
				try {
					LOADING_OPERATIONS.wait();
				} catch(InterruptedException e) {
					LOGGER.info("Interrupted wait for resource loading", e);
				}
			}
		}
	}
	
	/**
	 * Gets the map with the specified id.
	 *
	 * @param id The id of the map
	 * @return The map or null if not found
	 * @since 0.1.0
	 */
	public static @Nullable Map getMap(@NotNull Identifier id) {
		return MAPS.get(id);
	}
	
	/**
	 * Saves the game options to a file.
	 *
	 * @since 0.1.0
	 */
	public static void saveSettings() {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			FileWriter writer = new FileWriter("settings.json");
			gson.toJson(getOptions(), writer);
			writer.close();
		} catch(Exception e) {
			LOGGER.warn("Could not save settings", e);
		}
	}
	
	/**
	 * Gets the current memory usage of the application. The returned value represents the ratio of the used and available memory.
	 *
	 * @return The memory usage between 0 and 1
	 * @since 0.1.0
	 */
	private static double getMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		long max = runtime.totalMemory();
		long free = runtime.freeMemory();
		return ((double) (max - free)) / (double) max;
	}
	
	/**
	 * Creates a two-colored (binarised) version of the image. A pixel is black on the result if the image had a non-transparent pixel at that location, all other pixels are white. No changes are made to the original image.
	 *
	 * @param image The image to use
	 * @return The binarised image
	 * @since 0.1.0
	 */
	private static @NotNull BufferedImage binarisate(@NotNull BufferedImage image) {
		BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		for(int x = 0; x < image.getWidth(); x++) {
			for(int y = 0; y < image.getHeight(); y++) {
				if(new Color(image.getRGB(x, y), true).getAlpha() == 0) {
					newImage.setRGB(x, y, Color.WHITE.getRGB());
				} else {
					newImage.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
		}
		return newImage;
	}
	
	/**
	 * Checks if the extracted resources belong to the latest version of the application. Returns false if there are no extracted resources.
	 *
	 * @return True if latest version, false otherwise.
	 * @since 0.1.0
	 */
	private static boolean checkVersion() {
		try {
			File file = new File("version");
			if(!file.exists()) {
				return false;
			}
			byte[] extractedVersionData = Files.readAllBytes(file.toPath());
			Path jarVersion = getPathToResource("extract/version", true);
			byte[] jarVersionData = Files.readAllBytes(jarVersion);
			return Arrays.equals(extractedVersionData, jarVersionData);
		} catch(IOException | URISyntaxException e) {
			return false;
		}
	}
	
	/**
	 * Counts the amount of frames in a graphics resource. A still image has 1 frame, and an animated (GIF) image has {@code length/FRAME_LENGTH} frames (+- 1 frame).
	 *
	 * @param file The file containing the image
	 * @return The amount of frames
	 * @throws FileNotFoundException If an I/O exception occurs
	 * @see GameEngine#FRAME_LENGTH
	 * @since 0.1.0
	 */
	private static int countImageFrames(@NotNull File file) throws FileNotFoundException {
		if(getFileExtension(file).equalsIgnoreCase("gif")) {
			GifImage gif = new GifImage(file);
			int sum = 0;
			for(int i = 0; i < gif.getDecoder().getFrameCount(); i++) {
				sum += gif.getDecoder().getDelay(i);
			}
			int count = sum / GameEngine.FRAME_LENGTH;
			sum -= count * GameEngine.FRAME_LENGTH;
			if(sum > GameEngine.FRAME_LENGTH / 2) {
				count++;
			}
			return count;
		}
		return 1;
	}
	
	/**
	 * Deletes the specified file with all of its contents. If this file is a directory, all of its sub-directories and files are deleted first.
	 *
	 * @param f The file to delete
	 * @since 0.1.0
	 */
	private static void delete(@NotNull File f) {
		if(!f.exists()) {
			return;
		}
		if(f.isDirectory()) {
			for(File c : f.listFiles())
				delete(c);
		}
		if(!f.delete()) {
			LOGGER.info("Failed to delete file: " + f.getPath());
		}
	}
	
	/**
	 * Gets the extension of the file without its name
	 *
	 * @param file The file
	 * @return The extension of the file
	 * @since 0.1.0
	 */
	private static @NotNull String getFileExtension(@NotNull File file) {
		String name = file.getName();
		if(name.contains(".")) {
			return name.substring(name.lastIndexOf(".") + 1);
		}
		return "";
	}
	
	/**
	 * Gets the name of the file without its extension
	 *
	 * @param file The file
	 * @return The name of the file
	 * @since 0.1.0
	 */
	private static @NotNull String getFileName(@NotNull File file) {
		String name = file.getName();
		if(name.contains(".")) {
			name = name.substring(0, name.lastIndexOf("."));
		}
		return name;
	}
	
	/**
	 * Gets all of the separate frames from an image. If the images is not animated, the returned array only contains the original image.
	 *
	 * @param image the image
	 * @param file  The file the image was loaded from
	 * @return The frames as separate images
	 * @throws FileNotFoundException If the file is not found
	 * @since 0.1.0
	 */
	private static @NotNull BufferedImage[] getImageFrames(@NotNull Image image, @NotNull File file) throws FileNotFoundException {
		if(image instanceof BufferedImage) {
			return new BufferedImage[]{(BufferedImage) image};
		}
		if(getFileExtension(file).equalsIgnoreCase("gif")) {
			GifImage gif = new GifImage(file);
			int sum = 0;
			ArrayList<BufferedImage> images = new ArrayList<>();
			for(int i = 0; i < gif.getDecoder().getFrameCount(); i++) {
				sum += gif.getDecoder().getDelay(i);
				while(sum >= GameEngine.FRAME_LENGTH) {
					images.add(gif.getFrame(i));
					sum -= GameEngine.FRAME_LENGTH;
				}
			}
			if(sum > GameEngine.FRAME_LENGTH / 2) {
				images.add(gif.getFrame(gif.getDecoder().getFrameCount() - 1));
			}
			return images.toArray(new BufferedImage[0]);
		} else {
			BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = bimage.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			return new BufferedImage[]{bimage};
		}
	}
	
	/**
	 * Gets the resource file's default location for a .tags file at the specified path.
	 *
	 * @param path The path of the .tags file
	 * @return The resource's default location, or null if the file is not found
	 * @since 0.1.0
	 */
	private static @Nullable File getResourceFileFromTags(@NotNull Path path) {
		File file = path.toFile().getAbsoluteFile();
		String name = getFileName(file);
		Optional<File> resource = Arrays.stream(file.getParentFile().listFiles()).filter(f -> !getFileExtension(f).equalsIgnoreCase("tags")).filter(f -> getFileName(f).equalsIgnoreCase(name)).findAny();
		return resource.orElse(null);
	}
	
	/**
	 * Reads the image from the specified file
	 *
	 * @param file The file
	 * @return The image
	 * @throws MalformedURLException If the file's location is not valid
	 * @since 0.1.0
	 */
	private static @NotNull Image loadImage(@NotNull File file) throws MalformedURLException {
		return new ImageIcon(file.toURI().toURL()).getImage();
	}
	
	/**
	 * Loads a graphics or audio resource from the specified location.
	 *
	 * @param resource The id of the resource
	 * @param location The location of the resource
	 * @return The loaded resource
	 * @since 0.1.0
	 */
	private static @Nullable Object loadResource(@NotNull Identifier resource, @NotNull File location) {
		if(hasTag(resource, Tag.AUDIO.getId())) {
			try {
				return Files.readAllBytes(location.toPath());
			} catch(IOException e) {
				LOGGER.warn("Could not load resource " + resource, e);
			}
		} else if(hasTag(resource, Tag.GRAPHICS.getId())) {
			try {
				return new ImageIcon(location.toURI().toURL()).getImage();
			} catch(MalformedURLException e) {
				LOGGER.warn("Could not load resource " + resource, e);
			}
		}
		LOGGER.warn("Could not find appropriate loader for resource " + resource);
		return null;
	}
	
	/**
	 * Loads the game options from the files.
	 *
	 * @since 0.1.0
	 */
	private static void loadSettings() {
		try {
			File file = new File("settings.json");
			if(!file.exists()) {
				OPTIONS = Options.getDefaultOptions();
			} else {
				Gson gson = new Gson();
				OPTIONS = gson.fromJson(new FileReader(file), StandardOptions.class);
			}
			//TODO proper loading
		} catch(Exception e) {
			LOGGER.warn("Could not load game options", e);
		}
	}
	
	/**
	 * Registers the specified graphics or audio resource. If the resource is an image and it has the {@link Tag#HITBOX_SOURCE} tag, its hitboxes are also registered. If the image is animated, all of its frames are registered as well.
	 *
	 * @param resource The id of the resource
	 * @param location The location of the resource
	 * @see #loadResource(Identifier, File)
	 * @since 0.1.0
	 */
	private static void registerResource(@NotNull final Identifier resource, @NotNull final File location) {
		try {
			if(hasTag(resource, Tag.GRAPHICS.getId())) {//graphics resource
				final boolean isHitboxImage = hasTag(resource, Tag.HITBOX_SOURCE.getId());
				//
				final Image image = loadImage(location);
				final BufferedImage[] images = getImageFrames(image, location);
				IMAGES.register(resource, () -> images[0]);//registering basic image
				IMAGES.register(Identifier.fromString(resource.getGroup(), resource.getName() + "/raw"), () -> image);
				if(isHitboxImage) {
					if(getImage(resource) instanceof BufferedImage) {
						HITBOXES.register(Identifier.fromString(resource.getGroup(), resource.getName() + "/hitbox"), () -> new ImageHitbox(binarisate(getBufferedImage(resource))));
					}
				}
				//
				for(int i = 0; i < images.length; i++) {//register all frames
					final int index = i;
					final Identifier id = Identifier.fromString(resource.getGroup(), resource.getName() + "#" + index);
					IMAGES.register(id, () -> getImageFrames(getImage(Identifier.fromString(resource.getGroup(), resource.getName() + "/raw")), location)[index]);
					if(isHitboxImage) {
						HITBOXES.register(Identifier.fromString(id.getGroup(), id.getName() + "/hitbox"), () -> new ImageHitbox(binarisate(getBufferedImage(id))));
					}
				}
				LOGGER.debug("Registered resource " + resource);
			} else if(hasTag(resource, Tag.AUDIO.getId())) {//audio resource
				AUDIO.register(resource, () -> Files.readAllBytes(location.toPath()));
				LOGGER.debug("Registered resource " + resource);
			} else if(hasTag(resource, Tag.TAG.getId())) {//tag
				Gson gson = new Gson();
				JsonObject root = JsonParser.parseReader(gson.newJsonReader(new FileReader(location))).getAsJsonObject();
				String type = root.get("type").getAsString();
				switch(type.toLowerCase()) {
					case "greedy" -> new GreedyTag(resource);
					case "ingorant" -> new IgnorantTag(resource);
					case "gamestate" -> {
						JsonArray classes = root.get("classes").getAsJsonArray();
						ArrayList<Class<? extends GameState>> classNames = new ArrayList<>();
						for(JsonElement aClass : classes) {
							try {
								classNames.add((Class<? extends GameState>) Class.forName(aClass.getAsString()));
							} catch(Exception e) {
								LOGGER.warn("Could not get class for tag element", e);
							}
						}
						if(root.has("threshold")) {
							new GameStateTag(resource, root.get("threshold").getAsDouble(), classNames.toArray(new Class[0]));
						} else {
							new GameStateTag(resource, classNames.toArray(new Class[0]));
						}
					}
					default -> throw new IllegalConfigurationException("Illegal tag type in tag resource " + resource + " (" + type + ")");
				}
			} else if(hasTag(resource, Tag.MAP.getId())) {
				Map map = new Map(location, resource);
				MAPS.register(map);
			} else {
				throw new IllegalArgumentException("Resource doesn't belong to any resource type");
			}
		} catch(Exception e) {
			LOGGER.warn("Could not register resource " + resource, e);
		}
	}
	
	/**
	 * Registers a tag for a resource. Doesn't require the tag nor the resource to be registered in any cache.
	 *
	 * @param resource The id of the resource
	 * @param tag      The id of the tag
	 * @since 0.1.0
	 */
	private static synchronized void registerResourceTag(@NotNull Identifier resource, @NotNull Identifier tag) {
		if(!RESOURCE_TAGS.isLoaded(resource)) {
			RESOURCE_TAGS.register(resource, HashSet::new);
		}
		RESOURCE_TAGS.get(resource).add(tag);
		if(!TAG_RESOURCES.isLoaded(tag)) {
			TAG_RESOURCES.register(tag, HashSet::new);
		}
		TAG_RESOURCES.get(tag).add(resource);
	}
	
	/**
	 * Wrapper for async loading operations. Instances of this class are executed on the {@link Main#getExecutors()} automatically, and they respect the use of {@link #LOADING_OPERATIONS}.
	 *
	 * @since 0.1.0
	 */
	private static final class LoadingOperation implements Runnable {
		/**
		 * The runnable to run
		 *
		 * @since 0.1.0
		 */
		private final @NotNull Runnable runnable;
		
		/**
		 * Creates and executes a new loading operation.
		 *
		 * @param r The runnable to run
		 * @since 0.1.0
		 */
		public LoadingOperation(@NotNull Runnable r) {
			synchronized(LOADING_OPERATIONS) {
				LOADING_OPERATIONS.incrementAndGet();
			}
			this.runnable = r;
			Main.getExecutors().execute(this);
		}
		
		@Override
		public void run() {
			runnable.run();
			synchronized(LOADING_OPERATIONS) {
				if(LOADING_OPERATIONS.decrementAndGet() == 0) {
					LOADING_OPERATIONS.notifyAll();
				}
			}
		}
	}
}
