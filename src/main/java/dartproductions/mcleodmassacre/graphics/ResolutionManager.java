package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.ResourceManager;
import dartproductions.mcleodmassacre.options.QualityOption;

import java.awt.*;
import java.awt.image.BufferedImage;

import static dartproductions.mcleodmassacre.graphics.GraphicsManager.BUFFER;
import static dartproductions.mcleodmassacre.graphics.GraphicsManager.BUFFER_GRAPHICS;

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
			int minWidth = (int) (getDefaultScreenDimension().getWidth() * getScreenRatio());//scaled size
			int minHeight = (int) (getDefaultScreenDimension().getHeight() * getScreenRatio());
			int actualWidth = (int) Math.max(minWidth, getLocalScreenSize().getWidth());//actual size (either the scaled size or the local screen's size)
			int actualHeight = (int) Math.max(minHeight, getLocalScreenSize().getHeight());
			int offsetX = actualWidth - minWidth;
			int offsetY = actualHeight - minHeight;
			screenRect = new Rectangle(origin.x - offsetX, origin.y - offsetY, actualWidth, actualHeight);
		}
	}
	
	public static BufferedImage bufferToScreenImage() {
		BufferedImage output = new BufferedImage(getBufferAreaOnScreen().width, getBufferAreaOnScreen().height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = output.createGraphics();
		//
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, output.getWidth(), output.getHeight());
		//
		BufferedImage visible = BUFFER.getSubimage(getBufferAreaOnScreen().x, getBufferAreaOnScreen().y, getBufferAreaOnScreen().width, getBufferAreaOnScreen().height);
		Image scaled = visible.getScaledInstance((int) (getBufferAreaOnScreen().width * getScreenRatio()), (int) (getBufferAreaOnScreen().height * getScreenRatio()), switch((QualityOption) ResourceManager.getOptions().getSetting("Quality").getValue()) {
			case LOW -> BufferedImage.SCALE_FAST;
			case NORMAL -> BufferedImage.SCALE_DEFAULT;
			case HIGH -> BufferedImage.SCALE_AREA_AVERAGING;
		});
		//
		g.drawImage(scaled, 0, 0, null);
		//
		g.dispose();
		return output;//TODO
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
		BUFFER_GRAPHICS.drawImage(image, x + getOriginOnBuffer().x, y + getOriginOnBuffer().y, null);
	}
	
	public static void drawImageAnywhere(int x, int y, Image image) {
		BUFFER_GRAPHICS.drawImage(image, x, y, null);
	}
	
	public static void fillRectOnScreen(int x, int y, int width, int height) {
		x += getOriginOnBuffer().x;
		y += getOriginOnBuffer().y;
		BUFFER_GRAPHICS.fillRect(x, y, width, height);
	}
	
	public static void drawRectOnScreen(int x, int y, int width, int height) {
		x += getOriginOnBuffer().x;
		y += getOriginOnBuffer().y;
		BUFFER_GRAPHICS.drawRect(x, y, width, height);
	}
}
