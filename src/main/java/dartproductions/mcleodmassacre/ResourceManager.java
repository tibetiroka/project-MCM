package dartproductions.mcleodmassacre;

import dartproductions.mcleodmassacre.graphics.Animation;
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
import java.util.HashMap;
import java.util.HashSet;

/**
 * Accesses and handles stored game resources.
 */
public class ResourceManager {
	/**
	 * The main data directory where the game's data is stored.
	 */
	private static final File DATA_DIRECTORY = new File("data");
	private static final Logger LOGGER = LogManager.getLogger(ResourceManager.class);
	private static final HashMap<String, Image> IMAGES = new HashMap<>();
	private static final HashMap<String, Shape> HITBOXES = new HashMap<>();
	private static final HashMap<String, Area> HITBOX_AREAS = new HashMap<>();
	private static final HashSet<String> NAMES = new HashSet<>();
	private static final HashMap<String, Animation> ANIMATIONS = new HashMap<>();
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
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch(IOException e) {
				LOGGER.error("Could not create file for settings", e);
			}
		}
		//TODO
		OPTIONS = Options.getDefaultOptions();
	}
	
	public static void loadStandardGraphics() {
		File defaultDirectory = new File(getGraphicsDirectory(), "default");
		loadGraphics(defaultDirectory);
	}
	
	private static void loadGraphics(File directory) {
		File regular = new File(directory, "regular");
		if(regular.exists() && regular.isDirectory()) {
			for(File file : regular.listFiles()) {
				loadAndStoreImage(file);
			}
		}
		File hitboxes = new File(directory, "hitbox");
		if(hitboxes.exists() && hitboxes.isDirectory()) {
			for(File file : hitboxes.listFiles()) {
				BufferedImage[] images = loadAndStoreImage(file);
				if(images != null && images.length > 0) {
					for(BufferedImage image : images) {
						binarizate(image);//black and white fill only
					}
					final String name = getFileName(file);
					if(images.length == 1) {
						ImageHitbox hitbox = ImageHitbox.fromImage(images[0]);
						HITBOXES.put(name, hitbox);
						hitbox.whenDone(() -> HITBOX_AREAS.put(name, hitbox.getArea()));
					} else {
						for(int i = 0; i < images.length; i++) {
							ImageHitbox hitbox = ImageHitbox.fromImage(images[i]);
							HITBOXES.put(name + "-" + i, hitbox);
							final int j = i;
							hitbox.whenDone(() -> HITBOX_AREAS.put(name + "-" + j, hitbox.getArea()));
						}
					}
				}
			}
		}
		//todo load other subdirs
	}
	
	private static BufferedImage[] loadAndStoreImage(File file) {
		try {
			Image image = loadImage(file);
			LOGGER.debug("Loaded image " + file.getPath());
			String name = getFileName(file);
			NAMES.add(name);
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
		for(int x = 0; x < image.getWidth(); x++) {
			for(int y = 0; y < image.getHeight(); y++) {
				if(new Color(image.getRGB(x, y), true).getAlpha() == 0) {
					image.setRGB(x, y, Color.WHITE.getRGB());
				} else {
					image.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
		}
		return image;
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
	
	private static File getGraphicsDirectory() {
		return new File(DATA_DIRECTORY, "grc");
	}
	
	private static File getSettingsFile() {
		return new File(DATA_DIRECTORY, "settings.json");
	}
}
