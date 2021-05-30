package dartproductions.mcleodmassacre.util;

import org.jetbrains.annotations.Nullable;

/**
 * Generic pair implementation
 *
 * @param <First>  The type of the first value
 * @param <Second> The type of the second value
 */
public final class Pair<First, Second> {
	/**
	 * The first value
	 */
	public @Nullable First first;
	/**
	 * The second value
	 */
	public @Nullable Second second;
	
	/**
	 * Creates a new pair.
	 *
	 * @param first  The first value
	 * @param second The second value
	 */
	public Pair(@Nullable First first, @Nullable Second second) {
		this.first = first;
		this.second = second;
	}
	
	/**
	 * Creates a new pair with null values.
	 */
	public Pair() {
	}
	
	/**
	 * Gets the first value of the pair
	 *
	 * @return The first value
	 */
	public @Nullable First first() {
		return first;
	}
	
	/**
	 * Gets the second value of the pair
	 *
	 * @return The second value
	 */
	public @Nullable Second second() {
		return second;
	}
	
	/**
	 * Sets the first value of the pair
	 *
	 * @param first The new first value
	 * @return The previous first value
	 */
	public @Nullable First setFirst(@Nullable First first) {
		First prev = this.first;
		this.first = first;
		return prev;
	}
	
	/**
	 * Sets the second value of the pair
	 *
	 * @param second The new second value
	 * @return The previous second value
	 */
	public @Nullable Second setSecond(@Nullable Second second) {
		Second prev = this.second;
		this.second = second;
		return prev;
	}
}