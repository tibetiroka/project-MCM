/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Generic interface for storing pairs
 *
 * @param <First>  The type of the first value
 * @param <Second> The type of the second value
 * @since 0.1.0
 */
public interface Pair<First, Second> {
	/**
	 * Gets the first value of the pair
	 *
	 * @return The first value
	 * @since 0.1.0
	 */
	@Nullable First first();
	
	/**
	 * Gets the first value of the pair
	 *
	 * @return The first value
	 * @since 0.1.0
	 */
	default @Nullable First getFirst() {
		return first();
	}
	
	/**
	 * Gets the second value of the pair
	 *
	 * @return The first value
	 * @since 0.1.0
	 */
	default @Nullable Second getSecond() {
		return second();
	}
	
	/**
	 * Gets the second value of the pair
	 *
	 * @return The first value
	 * @since 0.1.0
	 */
	@Nullable Second second();
	
	/**
	 * Immutable pair implementation
	 *
	 * @since 0.1.0
	 */
	class ImmutablePair<First, Second> implements Pair<First, Second> {
		/**
		 * The first value
		 *
		 * @since 0.1.0
		 */
		protected @Nullable First first;
		/**
		 * The second value
		 *
		 * @since 0.1.0
		 */
		protected @Nullable Second second;
		
		/**
		 * Creates a new pair.
		 *
		 * @param first  The first value
		 * @param second The second value
		 * @since 0.1.0
		 */
		public ImmutablePair(@Nullable First first, @Nullable Second second) {
			this.first = first;
			this.second = second;
		}
		
		/**
		 * Creates a new pair with null values.
		 *
		 * @since 0.1.0
		 */
		public ImmutablePair() {
		}
		
		@Override
		public @Nullable First first() {
			return first;
		}
		
		@Override
		public @Nullable Second second() {
			return second;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(getFirst(), getSecond());
		}
		
		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;
			ImmutablePair<?, ?> that = (ImmutablePair<?, ?>) o;
			return Objects.equals(getFirst(), that.getFirst()) && Objects.equals(getSecond(), that.getSecond());
		}
		
		/**
		 * Null-safe immutable pair implementation
		 *
		 * @since 0.1.0
		 */
		public static class ImmutableNullsafePair<First, Second> extends ImmutablePair<First, Second> {
			/**
			 * Creates a new pair.
			 *
			 * @param first  The first value
			 * @param second The second value
			 * @since 0.1.0
			 */
			public ImmutableNullsafePair(@NotNull First first, @NotNull Second second) {
				super(Objects.requireNonNull(first), Objects.requireNonNull(second));
			}
			
			@Override
			public @NotNull First first() {
				return Objects.requireNonNull(first);
			}
			
			@Override
			public @NotNull Second second() {
				return Objects.requireNonNull(second);
			}
		}
	}
	
	/**
	 * Mutable pair implementation
	 *
	 * @since 0.1.0
	 */
	class MutablePair<First, Second> implements Pair<First, Second> {
		/**
		 * The first value
		 *
		 * @since 0.1.0
		 */
		protected @Nullable First first;
		/**
		 * The second value
		 *
		 * @since 0.1.0
		 */
		protected @Nullable Second second;
		
		/**
		 * Creates a new pair.
		 *
		 * @param first  The first value
		 * @param second The second value
		 * @since 0.1.0
		 */
		public MutablePair(@Nullable First first, @Nullable Second second) {
			this.first = first;
			this.second = second;
		}
		
		/**
		 * Creates a new pair with null values.
		 *
		 * @since 0.1.0
		 */
		public MutablePair() {
		}
		
		@Override
		public @Nullable First first() {
			return first;
		}
		
		@Override
		public @Nullable Second second() {
			return second;
		}
		
		/**
		 * Sets the first value of the pair
		 *
		 * @param first The new first value
		 * @return The previous first value
		 * @since 0.1.0
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
		 * @since 0.1.0
		 */
		public @Nullable Second setSecond(@Nullable Second second) {
			Second prev = this.second;
			this.second = second;
			return prev;
		}
		
		/**
		 * Null-safe immutable pair implementation
		 *
		 * @since 0.1.0
		 */
		public static class MutableNullsafePair<First, Second> extends MutablePair<First, Second> {
			/**
			 * Creates a new pair.
			 *
			 * @param first  The first value
			 * @param second The second value
			 * @since 0.1.0
			 */
			public MutableNullsafePair(@NotNull First first, @NotNull Second second) {
				super(Objects.requireNonNull(first), Objects.requireNonNull(second));
			}
			
			@Override
			public @NotNull First first() {
				return Objects.requireNonNull(super.first());
			}
			
			@Override
			public @NotNull Second second() {
				return Objects.requireNonNull(super.second());
			}
			
			/**
			 * Sets the first value of the pair
			 *
			 * @param first The new first value
			 * @return The previous first value
			 * @since 0.1.0
			 */
			public @NotNull First setFirst(@Nullable First first) {
				Objects.requireNonNull(first);
				return Objects.requireNonNull(super.setFirst(first));
			}
			
			/**
			 * Sets the second value of the pair
			 *
			 * @param second The new second value
			 * @return The previous second value
			 * @since 0.1.0
			 */
			public @NotNull Second setSecond(@Nullable Second second) {
				Objects.requireNonNull(second);
				return Objects.requireNonNull(super.setSecond(second));
			}
		}
	}
}