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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Accesses and handles stored game resources.
 */
public class ResourceManager {
	/**
	 * The main data directory where the game's data is stored.
	 */
	private static final File DATA_DIRECTORY = new File("data");
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
			getPaths(regular).forEach(path -> {
				if(path.toFile().isFile()) {
					loadAndStoreImage(path.toFile());
				}
			});
		} catch(Exception e) {
			LOGGER.error("Could not load default graphics from \"" + regular + "\"", e);
		}
		
		String hitboxes = directory + "/hitbox";
		try {
			getPaths(hitboxes).forEach(path -> {
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
								HITBOXES.put(name + "-" + i, hitbox);
								final int j = i;
								hitbox.whenDone(() -> HITBOX_AREAS.put(name + "-" + j, hitbox.getArea()));
							}
						}
					}
				}
			});
		} catch(Exception e) {
			LOGGER.error("Could not load default graphics from \"" + hitboxes + "\"", e);
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
							IMAGES.put(name + "-" + i, images[i]);
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
			return gif.getFrames().toArray(new BufferedImage[0]);
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
	
	private static Stream<Path> getPaths(String location) throws URISyntaxException, IOException {
		URI uri = Thread.currentThread().getContextClassLoader().getResource(location).toURI();
		Path myPath;
		if(uri.getScheme().equalsIgnoreCase("jar")) {
			FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
			myPath = fileSystem.getPath(location);
			fileSystem.close();
		} else {
			myPath = Paths.get(uri);
		}
		Stream<Path> walk = Files.walk(myPath, 6);
		return walk;
	}
}
