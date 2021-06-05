package dartproductions.mcleodmassacre.graphics;

import dartproductions.mcleodmassacre.ResourceManager;
import dartproductions.mcleodmassacre.options.QualityOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Manages graphics for different screen sizes using a buffer.
 *
 * @since 0.1.0
 */
public class ResolutionManager {
	
	/**
	 * The buffer to use for painting
	 *
	 * @since 0.1.0
	 */
	protected static final BufferedImage BUFFER;
	/**
	 * Graphics of the {@link #BUFFER}
	 *
	 * @since 0.1.0
	 */
	protected static final @NotNull Graphics2D BUFFER_GRAPHICS;
	/**
	 * The ratio of the current screen compared to the original screen.
	 *
	 * @since 0.1.0
	 */
	private static final double ratio;
	/**
	 * The area representing the current screen on the buffer.
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Rectangle screenRect;
	/**
	 * The size of the original screen
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Dimension originalScreen;
	/**
	 * The size of the buffer
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Dimension bufferSize;
	/**
	 * The point where the original screen would start on the buffer
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Point origin;
	/**
	 * The buffer's output image (this is what's drawn to the frame and what the buffer is eventually drawn to)
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull BufferedImage OUTPUT;
	/**
	 * Graphics for {@link #OUTPUT}
	 *
	 * @since 0.1.0
	 */
	private static final @NotNull Graphics2D OUTPUT_GRAPHICS;
	
	/**
	 * True if visible areas outside of the original screen's area should be filled with black.
	 *
	 * @since 0.1.0
	 */
	private static volatile boolean FILL_VISIBLE_AREAS = true;
	
	static {
		//originalScreen = new Dimension(1280, 1024);
		//originalScreen = new Dimension(1500, 845);
		originalScreen = new Dimension(1000, 563);
		
		{
			Dimension screen = getLocalScreenSize();
			Dimension myScreen = getDefaultScreenSize();
			double ratioX = screen.getWidth() / myScreen.getWidth();
			double ratioY = screen.getHeight() / myScreen.getHeight();
			ratio = Math.min(ratioX, ratioY);
		}
		
		bufferSize = new Dimension((int) getDefaultScreenSize().getWidth() * 3, (int) getDefaultScreenSize().getHeight() * 3);
		origin = new Point((int) getDefaultScreenSize().getWidth(), (int) getDefaultScreenSize().getHeight());
		{
			double minWidth = getDefaultScreenSize().getWidth() * getScreenRatio();//scaled size
			double minHeight = getDefaultScreenSize().getHeight() * getScreenRatio();
			double actualWidth = Math.max(minWidth, getLocalScreenSize().getWidth());//actual size (either the scaled size or the local screen's size)
			double actualHeight = Math.max(minHeight, getLocalScreenSize().getHeight());
			double offsetX = (actualWidth - minWidth) / 2.0;//offset in scaled size
			double offsetY = (actualHeight - minHeight) / 2.0;
			screenRect = new Rectangle((int) Math.ceil(origin.x - offsetX / ratio),
			                           (int) Math.ceil(origin.y - offsetY / ratio),
			                           getLocalScreenSize().width,
			                           getLocalScreenSize().height);
		}
		OUTPUT = new BufferedImage(screenRect.width, screenRect.height, BufferedImage.TYPE_INT_ARGB);
		OUTPUT_GRAPHICS = OUTPUT.createGraphics();
		//
		BUFFER = createBufferImage();
		BUFFER_GRAPHICS = BUFFER.createGraphics();
	}
	
	/**
	 * Draws the appropriate parts of the {@link #BUFFER} to the {@link #OUTPUT} image.
	 *
	 * @return The output image ({@link #OUTPUT})
	 * @since 0.1.0
	 */
	public static @NotNull BufferedImage bufferToScreenImage() {
		//
		OUTPUT_GRAPHICS.setColor(Color.BLACK);
		OUTPUT_GRAPHICS.fillRect(0, 0, OUTPUT.getWidth(), OUTPUT.getHeight());
		//
		BufferedImage visible = BUFFER.getSubimage(screenRect.x, screenRect.y, screenRect.width, screenRect.height);
		Image scaled = visible.getScaledInstance((int) (screenRect.width * getScreenRatio()), (int) (screenRect.height * getScreenRatio()), switch((QualityOption) ResourceManager.getOptions().getSetting("Quality").getValue()) {
			case LOW -> BufferedImage.SCALE_FAST;
			case NORMAL -> BufferedImage.SCALE_DEFAULT;
			case HIGH -> BufferedImage.SCALE_AREA_AVERAGING;
		});
		//
		OUTPUT_GRAPHICS.drawImage(scaled, 0, 0, null);
		//
		return OUTPUT;
	}
	
	/**
	 * Creates an image to server as {@link #BUFFER}
	 *
	 * @return The created buffer image
	 * @since 0.1.0
	 */
	private static @NotNull BufferedImage createBufferImage() {
		return new BufferedImage(getBufferSize().width, getBufferSize().height, BufferedImage.TYPE_INT_ARGB);
	}
	
	/**
	 * Gets the size of the screen on the machine running the game
	 *
	 * @return The screen size
	 * @since 0.1.0
	 */
	public static @NotNull Dimension getLocalScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}
	
	/**
	 * Gets the ratio of the current screen compared to the original screen
	 *
	 * @return The screen ratio
	 * @since 0.1.0
	 */
	public static double getScreenRatio() {
		return ratio;
	}
	
	/**
	 * Gets the area of the buffer that represents the original screen on the buffer. The returned rectangle is a clone of {@link #screenRect}
	 *
	 * @return The always visible area of the buffer
	 * @since 0.1.0
	 */
	public static @NotNull Rectangle getBufferAreaOnScreen() {
		return (Rectangle) screenRect.clone();
	}
	
	/**
	 * Gets the offset of the screen on the x axis. The offset is the distance of the {@link #origin} and {@link #screenRect} - origin is {@link #screenRect}'s top-left corner on the original screen, but not on every screen.
	 *
	 * @return The x offset
	 * @since 0.1.0
	 */
	public static int getScreenOffsetX() {
		return origin.x - screenRect.x;
	}
	
	/**
	 * Gets the offset of the screen on the y axis. The offset is the distance of the {@link #origin} and {@link #screenRect} - origin is {@link #screenRect}'s top-left corner on the original screen, but not on every screen.
	 *
	 * @return The y offset
	 * @since 0.1.0
	 */
	public static int getScreenOffsetY() {
		return origin.y - screenRect.y;
	}
	
	/**
	 * gets the center of the buffer, which is also the center of the original and the local screen (in the buffer's coordinate system). The center may be offset by 0.5 pixels if the screen has an even size.
	 *
	 * @return The center point
	 * @since 0.1.0
	 */
	public static @NotNull Point getCenter() {
		return new Point(bufferSize.width / 2, bufferSize.height / 2);
	}
	
	/**
	 * Gets the size of the original screen.
	 *
	 * @return The size
	 * @since 0.1.0
	 */
	public static @NotNull Dimension getDefaultScreenSize() {
		return (Dimension) originalScreen.clone();
	}
	
	/**
	 * Gets the size of the buffer image
	 *
	 * @return The buffer's size
	 * @since 0.1.0
	 */
	public static @NotNull Dimension getBufferSize() {
		return (Dimension) bufferSize.clone();
	}
	
	/**
	 * Gets the point that when drawn to the buffer appears at (0,0) on the original screen.
	 *
	 * @return The origin point
	 * @since 0.1.0
	 */
	public static @NotNull Point getOriginOnBuffer() {
		return (Point) origin.clone();
	}
	
	/**
	 * Returns the area representing the local screen on the buffer. The returned rectangle is a clone of {@link #screenRect}.
	 *
	 * @return The currently visible area of the buffer
	 * @since 0.1.0
	 */
	public static @NotNull Rectangle getLocalScreenOnBuffer() {
		return (Rectangle) screenRect.clone();
	}
	
	/**
	 * Draws an image to the part of the buffer that appears on the screen. The coordinates are adjusted by the {@link #origin}'s coordinates to translate them to the buffer's coordinate system. Doesn't draw images that would be entirely off-screen.
	 *
	 * @param x     The x coordinate of the image
	 * @param y     The y coordinate of the image
	 * @param image The image to draw
	 * @since 0.1.0
	 */
	public static void drawImageOnScreen(int x, int y, @NotNull Image image) {
		if(x <= screenRect.width && y <= screenRect.height) {
			BUFFER_GRAPHICS.drawImage(image, x + origin.x, y + origin.y, GraphicsManager.PANEL);
		}
	}
	
	/**
	 * Draws an image to the buffer. Doesn't draw images that would be entirely off-screen.
	 *
	 * @param x     The x coordinate of the image
	 * @param y     The y coordinate of the image
	 * @param image The image to draw
	 * @since 0.1.0
	 */
	public static void drawImageAnywhere(int x, int y, @NotNull Image image) {
		if(x <= screenRect.x + screenRect.width && y <= screenRect.y + screenRect.height) {
			BUFFER_GRAPHICS.drawImage(image, x, y, GraphicsManager.PANEL);
		}
	}
	
	/**
	 * Draws an image to the part of the buffer that appears on the screen. The coordinates are adjusted by the {@link #origin}'s coordinates to translate them to the buffer's coordinate system. Doesn't draw images that would be entirely off-screen.
	 *
	 * @param x     The x coordinate of the image
	 * @param y     The y coordinate of the image
	 * @param image The image to draw
	 * @since 0.1.0
	 */
	public static void drawImageOnScreen(int x, int y, @NotNull BufferedImage image) {
		if(screenRect.intersects(x + screenRect.x, y + screenRect.y, image.getWidth(), image.getHeight())) {
			BUFFER_GRAPHICS.drawImage(image, x + origin.x, y + origin.y, GraphicsManager.PANEL);
		}
	}
	
	/**
	 * Fills a specified shape on the screen. This method should only be used for debugging due to its excessive resource usage.
	 *
	 * @param x     The x offset of the shape
	 * @param y     The y offset of the shape
	 * @param shape The shape to fill
	 * @since 0.1.0
	 */
	public static void fillShapeOnScreen(int x, int y, @Nullable Shape shape) {
		if(shape != null) {
			AffineTransform transform = AffineTransform.getTranslateInstance(x + origin.x, y + origin.y);
			BUFFER_GRAPHICS.fill(transform.createTransformedShape(shape));
		}
	}
	
	/**
	 * Draws an image to the buffer. Doesn't draw images that would be entirely off-screen.
	 *
	 * @param x     The x coordinate of the image
	 * @param y     The y coordinate of the image
	 * @param image The image to draw
	 * @since 0.1.0
	 */
	public static void drawImageAnywhere(int x, int y, @Nullable BufferedImage image) {
		if(screenRect.intersects(x, y, image.getWidth(), image.getHeight())) {
			BUFFER_GRAPHICS.drawImage(image, x, y, GraphicsManager.PANEL);
		}
	}
	
	/**
	 * Fills a rectangle on the visible area of the buffer. Doesn't fill parts of the rectangle outside of the visible area. The coordinates are adjusted by the {@link #origin}'s coordinates to translate them to the buffer's coordinate system.
	 *
	 * @param x      The x coordinate of the rectangle
	 * @param y      The y coordinate of the rectangle
	 * @param width  The width of the rectangle
	 * @param height The height of the rectangle
	 * @since 0.1.0
	 */
	protected static void fillRectOnScreen(int x, int y, int width, int height) {
		x += origin.x;
		y += origin.y;
		Rectangle r = screenRect.intersection(new Rectangle(x, y, width, height));
		if(!r.isEmpty()) {
			BUFFER_GRAPHICS.fillRect(r.x, r.y, r.width, r.height);
		}
	}
	
	/**
	 * Draws a rectangle on the visible area of the buffer. Doesn't fill parts of the rectangle outside of the visible area. The coordinates are adjusted by the {@link #origin}'s coordinates to translate them to the buffer's coordinate system.
	 *
	 * @param x      The x coordinate of the rectangle
	 * @param y      The y coordinate of the rectangle
	 * @param width  The width of the rectangle
	 * @param height The height of the rectangle
	 * @since 0.1.0
	 */
	protected static void drawRectOnScreen(int x, int y, int width, int height) {
		x += origin.x;
		y += origin.y;
		if(screenRect.intersects(x, y, width, height)) {
			BUFFER_GRAPHICS.drawRect(x, y, width, height);
		}
	}
	
	/**
	 * Fills all visible but unpainted areas with black if {@link #isFillVisibleAreas()} is true.
	 *
	 * @since 0.1.0
	 */
	public static void fillVisibleAreas() {
		if(!isFillVisibleAreas()) {
			return;
		}
		BUFFER_GRAPHICS.setColor(Color.BLACK);
		int x = getScreenOffsetX();
		int y = getScreenOffsetY();
		fillRectOnScreen(-x, -y, screenRect.width, y);//top
		fillRectOnScreen(-x, -y, x, screenRect.height);//left
		fillRectOnScreen(-x, originalScreen.height, originalScreen.width, y);//bottom
		fillRectOnScreen(originalScreen.width, -y, x, originalScreen.height);//right
	}
	
	/**
	 * Gets whether visible areas that are not guaranteed to be painted in every frame are filled with black.
	 *
	 * @return True if black fill, false otherwise
	 * @since 0.1.0
	 */
	public static boolean isFillVisibleAreas() {
		return FILL_VISIBLE_AREAS;
	}
	
	/**
	 * Sets whether visible areas that are not guaranteed to be painted in every frame are filled with black. If set to false, the caller has to guarantee that either the area is never painted to, or it is always filled, otherwise unexpected visuals can appear.
	 *
	 * @param fillVisibleAreas Whether or not to fill all visible areas
	 * @since 0.1.0
	 */
	public static void setFillVisibleAreas(boolean fillVisibleAreas) {
		FILL_VISIBLE_AREAS = fillVisibleAreas;
	}
	
	/**
	 * Fills the local screen with black.
	 *
	 * @since 0.1.0
	 */
	public static void fillLocalScreen() {
		BUFFER_GRAPHICS.setColor(Color.BLACK);
		//fillRectOnScreen(-getScreenOffsetX(),-getScreenOffsetY(),screenRect.width,screenRect.height);
		BUFFER_GRAPHICS.fillRect(screenRect.x, screenRect.y, screenRect.width, screenRect.height);
	}
}
