package dartproductions.mcleodmassacre;

import dartproductions.mcleodmassacre.engine.GameEngine;
import dartproductions.mcleodmassacre.graphics.Animation;
import dartproductions.mcleodmassacre.graphics.Animation.StandardAnimation;
import dartproductions.mcleodmassacre.hitbox.ImageHitbox;
import dartproductions.mcleodmassacre.options.Options;
import de.cerus.jgif.GifImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Accesses and handles stored game resources.
 */
public class ResourceManager {
	private static final Logger LOGGER = LogManager.getLogger(ResourceManager.class);
	/**
	 * True if the {@link #loadAllResources()} method has finished loading resources.
	 */
	private static final AtomicBoolean ALL_LOADED = new AtomicBoolean(false);
	/**
	 * The loaded images; they key is the file's name without extension, with possibly a #number attached to it if it is a frame from a GIF.
	 */
	private static final @NotNull ConcurrentHashMap<String, Image> IMAGES = new ConcurrentHashMap<>();
	/**
	 * Hitboxes created from the loaded images; they key is the file's name without extension, with possibly a #number attached to it if it is a frame from a GIF.
	 */
	private static final @NotNull ConcurrentHashMap<String, Shape> HITBOXES = new ConcurrentHashMap<>();
	/**
	 * Hitbox areas created from the loaded images; they key is the file's name without extension, with possibly a #number attached to it if it is a frame from a GIF.
	 */
	private static final @NotNull ConcurrentHashMap<String, Area> HITBOX_AREAS = new ConcurrentHashMap<>();
	/**
	 * The names of the loaded images
	 */
	private static final @NotNull Set<String> IMAGE_NAMES = Collections.synchronizedSet(new HashSet<>());
	/**
	 * The created animations; the key is the file's name without extension
	 */
	private static final @NotNull ConcurrentHashMap<String, Animation> ANIMATIONS = new ConcurrentHashMap<>();
	/**
	 * The loaded sound effects; the key is the file's name without extension, and the value is the audio data.
	 */
	private static final @NotNull ConcurrentHashMap<String, byte[]> SFX = new ConcurrentHashMap<>();
	
	/**
	 * The active game options
	 */
	private static Options OPTIONS;
	
	/**
	 * Gets the active game options. Loads them from the settings file if they are not yet loaded.
	 *
	 * @return The options
	 */
	public static @NotNull Options getOptions() {
		if(OPTIONS == null) {
			loadSettings();
		}
		return OPTIONS;
	}
	
	/**
	 * Loads the game options from the files.
	 */
	private static void loadSettings() {
		File file = getSettingsFile();
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(IOException e) {
				LOGGER.error("Could not create file for settings", e);
			}
		}
		//TODO proper loading
		OPTIONS = Options.getDefaultOptions();
	}
	
	/**
	 * Loads all graphics that are necessary to have in order to open the application window.
	 */
	public static void loadStandardGraphics() {
		loadGraphics(getGraphicsDirectory() + "/default");
	}
	
	/**
	 * Loads graphical elements from the specified directory.
	 *
	 * @param directory The directory to load from
	 */
	private static void loadGraphics(@NotNull String directory) {
		String regular = directory + "/regular";
		try {
			getPaths(regular, false).forEach(path -> {
				
				if(path.toFile().isFile()) {
					loadAndStoreImage(path.toFile());
				}
			});
		} catch(Exception e) {
			LOGGER.error("Could not load default graphics from \"" + regular + "\"", e);
		}
		
		String hitboxes = directory + "/hitbox";
		try {
			getPaths(hitboxes, false).forEach(path -> {
				if(path.toFile().isFile()) {
					BufferedImage[] images = loadAndStoreImage(path.toFile());
					if(images != null && images.length > 0) {
						final String name = getFileName(path.toFile());
						if(images.length == 1) {
							ImageHitbox.createFromImage(binarisate(images[0]), hitbox -> {
								HITBOXES.put(name, hitbox);
								HITBOX_AREAS.put(name, hitbox.getArea());
							});
						} else {
							for(int i = 0; i < images.length; i++) {
								final String name_ = name + "#" + i;
								ImageHitbox.createFromImage(binarisate(images[i]), hitbox -> {
									HITBOXES.put(name_, hitbox);
									HITBOX_AREAS.put(name_, hitbox.getArea());
								});
							}
						}
					}
				}
			});
		} catch(Exception e) {
			LOGGER.error("Could not load default graphics from \"" + hitboxes + "\"", e);
			Main.setRunning(false);
		}
		//todo load other subdirs if needed
	}
	
	/**
	 * Creates animations for all newly loaded images.
	 */
	public static void createAnimations() {
		ImageHitbox.waitForProcessing();
		for(String name : IMAGE_NAMES) {
			if(!ANIMATIONS.containsKey(name)) {
				ANIMATIONS.put(name, new StandardAnimation(name));
			}
		}
	}
	
	/**
	 * Loads all resources not loaded by other methods.
	 */
	public static void loadAllResources() {
		loadMusic();
		ALL_LOADED.set(true);
		synchronized(ALL_LOADED) {
			ALL_LOADED.notifyAll();
		}
		//load other stuff if necessary
	}
	
	/**
	 * Waits until all resources are loaded via {@link #loadAllResources()}.
	 */
	public static void waitForLoading() {
		if(!ALL_LOADED.get()) {
			synchronized(ALL_LOADED) {
				try {
					ALL_LOADED.wait();
				} catch(InterruptedException e) {
					LOGGER.warn("Interrupted wait in resource manager", e);
				}
			}
		}
	}
	
	/**
	 * Loads the game's sound effects and music.
	 */
	private static void loadMusic() {
		File dir = new File("data/music");
		if(dir.exists()) {
			for(File file : dir.listFiles()) {
				try {
					SFX.put(getFileName(file), Files.readAllBytes(file.toPath()));
					LOGGER.debug("Loaded sound " + getFileName(file));
				} catch(IOException e) {
					LOGGER.warn("Could not read music file " + file.getPath(), e);
				}
			}
		}
	}
	
	/**
	 * Extracts all resources from the application jar.
	 */
	public static void extractResources() {
		if(checkVersion()) {
			LOGGER.info("Skipping resource extraction: already done for this version");
		} else {
			LOGGER.info("Extracting resources");
			try {
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
	 * Checks if the extracted resources belong to the latest version of the application. Returns false if there are no extracted resources.
	 *
	 * @return True if latest version, false otherwise.
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
	 * Gets the animation with the specified name.
	 *
	 * @param name The name of the animation
	 * @return The animation or null
	 */
	public static @Nullable Animation getAnimation(@NotNull String name) {
		return ANIMATIONS.containsKey(name) ? ANIMATIONS.get(name).clone() : null;
	}
	
	private static @NotNull BufferedImage[] loadAndStoreImage(@NotNull File file) {
		try {
			Image image = loadImage(file);
			LOGGER.debug("Loaded image " + file.getPath());
			String name = getFileName(file);
			IMAGE_NAMES.add(name);
			try {
				BufferedImage[] images = getImageFrames(image, file);
				if(images.length > 0) {
					if(images.length == 1) {
						IMAGES.put(name, images[0]);
					} else {
						IMAGES.put(name, image);
						for(int i = 0; i < images.length; i++) {
							IMAGES.put(name + "#" + i, images[i]);
						}
					}
				}
				return images;
			} catch(Exception e) {
				LOGGER.warn("Could not get frames from image", e);
			}
		} catch(IOException e) {
			LOGGER.error("Could not load image " + file.getPath(), e);
		}
		return null;
	}
	
	/**
	 * Creates a two-colored (binarised) version of the image. A pixel is black on the result if the image had a non-transparent pixel at that location, all other pixels are white.
	 *
	 * @param image The image to use
	 * @return The binarised image
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
	 * Gets the name of the file without its extension
	 *
	 * @param file The file
	 * @return The name of the file
	 */
	private static @NotNull String getFileName(@NotNull File file) {
		String name = file.getName();
		if(name.contains(".")) {
			name = name.substring(0, name.lastIndexOf("."));
		}
		return name;
	}
	
	/**
	 * Reads the image from the specified file
	 *
	 * @param file The file
	 * @return The image
	 * @throws MalformedURLException If the file's location is not valid
	 */
	private static @NotNull Image loadImage(@NotNull File file) throws MalformedURLException {
		return new ImageIcon(file.toURI().toURL()).getImage();
	}
	
	/**
	 * Gets all of the separate frames from an image. If the images is not animated, the returned array only contains the original image.
	 *
	 * @param image the image
	 * @param file  The file the image was loaded from
	 * @return The frames as separate images
	 * @throws FileNotFoundException If the file is not found
	 */
	private static @NotNull BufferedImage[] getImageFrames(@NotNull Image image, @NotNull File file) throws FileNotFoundException {
		if(image instanceof BufferedImage) {
			return new BufferedImage[]{(BufferedImage) image};
		}
		if(file.getName().toLowerCase().endsWith(".gif")) {
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
	 * Gets the image with the specified name
	 *
	 * @param name The name of the image
	 * @return The image or null if not found
	 */
	public static @Nullable Image getImage(@NotNull String name) {
		return IMAGES.get(name);
	}
	
	/**
	 * Gets the image with the specified name as a {@link BufferedImage}.
	 *
	 * @param name The name of the image
	 * @return The image or null if not found
	 */
	public static @Nullable BufferedImage getBufferedImage(@NotNull String name) {
		Image i = getImage(name);
		if(i instanceof BufferedImage image) {
			return image;
		}
		return null;
	}
	
	/**
	 * Gets the hitbox with the specified name
	 *
	 * @param name The name of the hitbox
	 * @return The hitbox or null if not found
	 */
	public static @Nullable Shape getHitbox(@NotNull String name) {
		return HITBOXES.get(name);
	}
	
	/**
	 * Gets the hitbox area with the specified name
	 *
	 * @param name The name of the hitbox area
	 * @return The hitbox area or null if not found
	 */
	public static @Nullable Area getHitboxArea(@NotNull String name) {
		return HITBOX_AREAS.get(name);
	}
	
	/**
	 * Gets the directory where the graphics resources are stored.
	 *
	 * @return The graphics directory
	 */
	private static @NotNull String getGraphicsDirectory() {
		return "data/grc";
	}
	
	/**
	 * Gets the file that stores the game options.
	 *
	 * @return The settings file
	 */
	private static @NotNull File getSettingsFile() {
		return new File("settings.json");
	}
	
	/**
	 * Gets a Path to the specified location in the file system.
	 *
	 * @param location The location
	 * @param forceJar True if the location must be inside the application jar/classpath
	 * @return The path
	 * @throws IOException        If an exception occurs
	 * @throws URISyntaxException If an exception occurs
	 */
	private static synchronized @NotNull Path getPathToResource(@NotNull String location, boolean forceJar) throws IOException, URISyntaxException {
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
				fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
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
	 */
	private static Stream<Path> getPaths(String location, int depth, boolean forceJar) throws IOException, URISyntaxException {
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
	 */
	private static Stream<Path> getPaths(String location, boolean forceJar) throws IOException, URISyntaxException {
		return getPaths(location, 60, forceJar);
	}
	
	/**
	 * Gets the sound effect with the specified name
	 *
	 * @param name The name of the sound effect
	 * @return The audio data or null if not found
	 */
	public static @Nullable byte[] getSound(@NotNull String name) {
		return SFX.get(name);
	}
}
