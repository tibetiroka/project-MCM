package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.ResourceManager;
import dartproductions.mcleodmassacre.options.QualityOption;

import java.awt.*;
import java.awt.image.BufferedImage;

import static dartproductions.mcleodmassacre.graphics.GraphicsManager.*;

public class ResolutionManager {
	
	private static final double ratio;
	private static final Rectangle screenRect;
	private static final Dimension originalScreen;
	private static final Dimension bufferSize;
	private static final Point origin;
	
	static {
		originalScreen = new Dimension(1280, 1024);
		
		{
			Dimension screen = getLocalScreenSize();
			Dimension myScreen = getDefaultScreenDimension();
			double ratioX = screen.getWidth() / myScreen.getWidth();
			double ratioY = screen.getHeight() / myScreen.getHeight();
			ratio = Math.min(ratioX, ratioY);
		}
		
		bufferSize = new Dimension((int) getDefaultScreenDimension().getWidth() * 3, (int) getDefaultScreenDimension().getHeight() * 3);
		origin = new Point((int) getDefaultScreenDimension().getWidth(), (int) getDefaultScreenDimension().getHeight());
		{
			double minWidth = getDefaultScreenDimension().getWidth() * getScreenRatio();//scaled size
			double minHeight = getDefaultScreenDimension().getHeight() * getScreenRatio();
			double actualWidth = Math.max(minWidth, getLocalScreenSize().getWidth());//actual size (either the scaled size or the local screen's size)
			double actualHeight = Math.max(minHeight, getLocalScreenSize().getHeight());
			double offsetX = (actualWidth - minWidth) / 2.0;//offset in scaled size
			double offsetY = (actualHeight - minHeight) / 2.0;
			double widthBefore = actualWidth / ratio;
			double heightBefore = actualHeight / ratio;
			screenRect = new Rectangle((int) (origin.x - offsetX / ratio),
			                           (int) (origin.y - offsetY / ratio),
			                           (int) widthBefore,
			                           (int) heightBefore);
		}
	}
	
	public static BufferedImage bufferToScreenImage() {
		BufferedImage output = new BufferedImage(screenRect.width, screenRect.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = output.createGraphics();
		//
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, output.getWidth(), output.getHeight());
		//
		BufferedImage visible = BUFFER.getSubimage(screenRect.x, screenRect.y, screenRect.width, screenRect.height);
		Image scaled = visible.getScaledInstance((int) (screenRect.width * getScreenRatio()), (int) (screenRect.height * getScreenRatio()), switch((QualityOption) ResourceManager.getOptions().getSetting("Quality").getValue()) {
			case LOW -> BufferedImage.SCALE_FAST;
			case NORMAL -> BufferedImage.SCALE_DEFAULT;
			case HIGH -> BufferedImage.SCALE_AREA_AVERAGING;
		});
		//
		g.drawImage(scaled, 0, 0, null);
		//
		g.dispose();
		return output;
	}
	
	public static BufferedImage createBufferImage() {
		return new BufferedImage(getBufferSize().width, getBufferSize().height, BufferedImage.TYPE_INT_ARGB);
	}
	
	public static Dimension getLocalScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}
	
	public static double getScreenRatio() {
		return ratio;
	}
	
	public static Rectangle getBufferAreaOnScreen() {
		return (Rectangle) screenRect.clone();
	}
	
	public static Dimension getDefaultScreenDimension() {
		return (Dimension) originalScreen.clone();
	}
	
	public static Dimension getBufferSize() {
		return (Dimension) bufferSize.clone();
	}
	
	public static Point getOriginOnBuffer() {
		return (Point) origin.clone();
	}
	
	public static void drawImageOnScreen(int x, int y, Image image) {
		if(x <= screenRect.x + screenRect.width && y <= screenRect.y + screenRect.height) {
			BUFFER_GRAPHICS.drawImage(image, x + origin.x, y +origin.y, null);
		}
	}
	
	public static void drawImageAnywhere(int x, int y, Image image) {
		if(x <= screenRect.x + screenRect.width && y <= screenRect.y + screenRect.height) {
			BUFFER_GRAPHICS.drawImage(image, x, y, null);
		}
	}
	
	public static void drawImageOnScreen(int x, int y, BufferedImage image) {
		if(screenRect.intersects(x, y, image.getWidth(), image.getHeight())) {
			BUFFER_GRAPHICS.drawImage(image, x + origin.x, y + origin.y, null);
		}
	}
	
	public static void drawImageAnywhere(int x, int y, BufferedImage image) {
		if(screenRect.intersects(x, y, image.getWidth(), image.getHeight())) {
			BUFFER_GRAPHICS.drawImage(image, x, y, null);
		}
	}
	
	public static void fillRectOnScreen(int x, int y, int width, int height) {
		x += origin.x;
		y += origin.y;
		if(screenRect.intersects(x, y, width, height)) {
			BUFFER_GRAPHICS.fillRect(x, y, width, height);
		}
	}
	
	public static void drawRectOnScreen(int x, int y, int width, int height) {
		x += origin.x;
		y += origin.y;
		if(screenRect.intersects(x, y, width, height)) {
			BUFFER_GRAPHICS.drawRect(x, y, width, height);
		}
	}
}
