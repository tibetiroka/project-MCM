package dartproductions.mcleodmassacre.hitbox;

import dartproductions.mcleodmassacre.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageHitbox implements Shape {
	protected static final HashMap<BufferedImage, ImageHitbox> HITBOXES = new HashMap<>();
	protected static final AtomicInteger PROCESSING_COUNT = new AtomicInteger();
	private static final Logger LOGGER = LogManager.getLogger(ImageHitbox.class);
	
	protected Area outline;
	
	protected ImageHitbox(final BufferedImage image) {
		HITBOXES.put(image, this);
		PROCESSING_COUNT.incrementAndGet();
		Main.getExecutors().execute(() -> {
			outline = createOutline(Color.WHITE, image);
			synchronized(PROCESSING_COUNT) {
				if(PROCESSING_COUNT.decrementAndGet() == 0) {
					PROCESSING_COUNT.notify();
				}
			}
			LOGGER.debug("Created image hitbox (" + image.hashCode() + ")");
		});
	}
	
	public static ImageHitbox fromImage(BufferedImage image) {
		if(HITBOXES.containsKey(image)) {
			return HITBOXES.get(image);
		} else {
			return new ImageHitbox(image);
		}
	}
	
	/**
	 * Waits until all created hitboxes have been processed. Also waits for hitboxes that are created during the wait, but doesn't stop creating new hitboxes after the wait.
	 */
	public static void waitForProcessing() {
		synchronized(PROCESSING_COUNT) {
			if(PROCESSING_COUNT.get() > 0) {
				try {
					PROCESSING_COUNT.wait();
				} catch(InterruptedException e) {
					LOGGER.warn("Wait for hitbox processing was interrupted", e);
				}
			}
		}
	}
	
	public Area getArea() {
		return outline;
	}
	
	public Area createOutline(Color target, BufferedImage bi) {
		// construct the GeneralPath
		GeneralPath gp = new GeneralPath();
		
		boolean cont = false;
		int targetRGB = target.getRGB();
		for(int xx = 0; xx < bi.getWidth(); xx++) {
			for(int yy = 0; yy < bi.getHeight(); yy++) {
				if(bi.getRGB(xx, yy) == targetRGB) {
					if(cont) {
						gp.lineTo(xx, yy);
						gp.lineTo(xx, yy + 1);
						gp.lineTo(xx + 1, yy + 1);
						gp.lineTo(xx + 1, yy);
						gp.lineTo(xx, yy);
					} else {
						gp.moveTo(xx, yy);
					}
					cont = true;
				} else {
					cont = false;
				}
			}
			cont = false;
		}
		gp.closePath();
		
		// construct the Area from the GP & return it
		return new Area(gp);
	}
	
	@Override
	public Rectangle getBounds() {
		return outline.getBounds();
	}
	
	@Override
	public Rectangle2D getBounds2D() {
		return outline.getBounds2D();
	}
	
	@Override
	public boolean contains(double x, double y) {
		return outline.contains(x, y);
	}
	
	@Override
	public boolean contains(Point2D p) {
		return outline.contains(p);
	}
	
	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return outline.intersects(x, y, w, h);
	}
	
	@Override
	public boolean intersects(Rectangle2D r) {
		return outline.intersects(r);
	}
	
	@Override
	public boolean contains(double x, double y, double w, double h) {
		return outline.contains(x, y, w, h);
	}
	
	@Override
	public boolean contains(Rectangle2D r) {
		return outline.contains(r);
	}
	
	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return outline.getPathIterator(at);
	}
	
	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return outline.getPathIterator(at, flatness);
	}
}
