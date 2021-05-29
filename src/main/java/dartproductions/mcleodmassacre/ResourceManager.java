package dartproductions.mcleodmassacre;

import dartproductions.mcleodmassacre.graphics.Animation;
import dartproductions.mcleodmassacre.graphics.Animation.StandardAnimation;
import dartproductions.mcleodmassacre.hitbox.ImageHitbox;
import dartproductions.mcleodmassacre.options.Options;
import de.cerus.jgif.GifImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.util.stream.Stream;

/**
 * Accesses and handles stored game resources.
 */
public class ResourceManager {
	private static final Logger LOGGER = LogManager.getLogger(ResourceManager.class);
	private static final ConcurrentHashMap<String, Image> IMAGES = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Shape> HITBOXES = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Area> HITBOX_AREAS = new ConcurrentHashMap<>();
	private static final Set<String> IMAGE_NAMES = Collections.synchronizedSet(new HashSet<>());
	private static final ConcurrentHashMap<String, Animation> ANIMATIONS = new ConcurrentHashMap<>();
	/**
	 * The active game options
	 */
	private static Options OPTIONS;
	
	public static Options getOptions() {
		if(OPTIONS == null) {
			loadSettings();
		}
		return OPTIONS;
	}
	
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
	
	public static void loadStandardGraphics() {
		loadGraphics(getGraphicsDirectory() + "/default");
	}
	
	private static void loadGraphics(String directory) {
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
							ImageHitbox hitbox = ImageHitbox.fromImage(binarizate(images[0]));
							HITBOXES.put(name, hitbox);
							hitbox.whenDone(() -> HITBOX_AREAS.put(name, hitbox.getArea()));
						} else {
							for(int i = 0; i < images.length; i++) {
								ImageHitbox hitbox = ImageHitbox.fromImage(binarizate(images[i]));
								HITBOXES.put(name + "#" + i, hitbox);
								final int j = i;
								hitbox.whenDone(() -> HITBOX_AREAS.put(name + "#" + j, hitbox.getArea()));
							}
						}
					}
				}
			});
		} catch(Exception e) {
			LOGGER.error("Could not load default graphics from \"" + hitboxes + "\"", e);
			Main.setRunning(false);
		}
		//todo load other subdirs
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
		//todo
	}
	
	public static void extractResources() {
		if(checkVersion()) {
			LOGGER.info("Skipping resource extraction: already done for this version");
		} else {
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
	
	public static Animation getAnimation(String name) {
		return ANIMATIONS.containsKey(name) ? ANIMATIONS.get(name).clone() : null;
	}
	
	private static BufferedImage[] loadAndStoreImage(File file) {
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
	
	private static BufferedImage binarizate(BufferedImage image) {
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
	
	private static String getFileName(File file) {
		String name = file.getName();
		if(name.contains(".")) {
			name = name.substring(0, name.lastIndexOf("."));
		}
		return name;
	}
	
	private static Image loadImage(File file) throws MalformedURLException {
		return new ImageIcon(file.toURI().toURL()).getImage();
	}
	
	private static BufferedImage[] getImageFrames(Image image, File file) throws FileNotFoundException {
		if(image instanceof BufferedImage) {
			return new BufferedImage[]{(BufferedImage) image};
		}
		if(file.getName().toLowerCase().endsWith(".gif")) {
			GifImage gif = new GifImage(file);
			int sum = 0;
			int msPerFrame = 20;
			ArrayList<BufferedImage> images = new ArrayList<>();
			for(int i = 0; i < gif.getDecoder().getFrameCount(); i++) {
				sum += gif.getDecoder().getDelay(i);
				while(sum >= msPerFrame) {
					images.add(gif.getFrame(i));
					sum -= msPerFrame;
				}
			}
			if(sum > msPerFrame / 2) {
				images.add(gif.getFrame(gif.getDecoder().getFrameCount() - 1));
			}
			return images.toArray(new BufferedImage[0]);
		} else {
			// Create a buffered image with transparency
			BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			// Draw the image on to the buffered image
			Graphics2D g = bimage.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			
			// Return the buffered image
			return new BufferedImage[]{bimage};
		}
	}
	
	public static Image getImage(String name) {
		return IMAGES.get(name);
	}
	
	public static BufferedImage getBufferedImage(String name) {
		Image i = getImage(name);
		if(i instanceof BufferedImage image) {
			return image;
		}
		return null;
	}
	
	public static Shape getHitbox(String name) {
		return HITBOXES.get(name);
	}
	
	public static Area getHitboxArea(String name) {
		return HITBOX_AREAS.get(name);
	}
	
	private static String getGraphicsDirectory() {
		return "data/grc";
	}
	
	private static File getSettingsFile() {
		return new File("settings.json");
	}
	
	private static synchronized Path getPathToResource(String location, boolean forceJar) throws IOException, URISyntaxException {
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
	
	private static Stream<Path> getPaths(String location, int depth, boolean forceJar) throws IOException, URISyntaxException {
		Path myPath = getPathToResource(location, forceJar);
		return Files.walk(myPath, depth);
	}
	
	private static Stream<Path> getPaths(String location, boolean forceJar) throws IOException, URISyntaxException {
		return getPaths(location, 60, forceJar);
	}
}
