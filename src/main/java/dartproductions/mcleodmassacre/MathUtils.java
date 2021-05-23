package dartproductions.mcleodmassacre;

import java.awt.Point;

public class MathUtils {
	public static long intsToLong(int first, int second) {
		return (((long) first) << 32) | (second & 0xffffffffL);
	}
	
	public static int getFirstInt(long l) {
		return (int) (l >> 32);
	}
	
	public static int getSecondInt(long l) {
		return (int) l;
	}
	
	public static boolean contains(double x, double y, double width, double height, Point p) {
		return p.getX() >= x && p.getX() < x + width && p.getY() >= y && p.getY() < y + height;
	}
}
