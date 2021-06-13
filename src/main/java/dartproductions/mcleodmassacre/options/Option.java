/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.options;


import org.jetbrains.annotations.Nullable;

/**
 * A single setting in the options.
 *
 * @param <T> The type of the data stored
 * @since 0.1.0
 */
public interface Option<T> {
	
	/**
	 * Gets the value of this setting.
	 *
	 * @return The current setting
	 * @since 0.1.0
	 */
	@Nullable T getValue();
	
	/**
	 * Sets the value of this setting.
	 *
	 * @param t The new value
	 * @since 0.1.0
	 */
	void setValue(@Nullable T t);
	
	/**
	 * Gets the visible name of this setting. This name can be displayed to users.
	 *
	 * @return The name of this setting
	 * @since 0.1.0
	 */
	@Nullable String getVisibleName();
	
	/**
	 * Sets the visible name of this setting. This name can be displayed to users.
	 *
	 * @param name The new name of this setting
	 * @since 0.1.0
	 */
	void setVisibleName(@Nullable String name);
	
	/**
	 * {@link Option} implementation for boolean values
	 *
	 * @since 0.1.0
	 */
	class BooleanOption implements Option<Boolean> {
		/**
		 * The name of the option
		 *
		 * @since 0.1.0
		 */
		protected @Nullable String name;
		/**
		 * The value of the option
		 *
		 * @since 0.1.0
		 */
		protected boolean value;
		
		/**
		 * Creates a new option with no name and false as value.
		 *
		 * @since 0.1.0
		 */
		public BooleanOption() {
			this(null, false);
		}
		
		/**
		 * Creates a new option with no name and the specified value.
		 *
		 * @param value The option's value
		 * @since 0.1.0
		 */
		public BooleanOption(boolean value) {
			this(null, value);
		}
		
		/**
		 * Creates a new option with the specified name and false as value.
		 *
		 * @param name The option's name
		 * @since 0.1.0
		 */
		public BooleanOption(@Nullable String name) {
			this(name, false);
		}
		
		/**
		 * Creates a new option with the specified name and value.
		 *
		 * @param name  The name of the option
		 * @param value The value of the option
		 * @since 0.1.0
		 */
		public BooleanOption(@Nullable String name, boolean value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public @Nullable Boolean getValue() {
			return value;
		}
		
		@Override
		public void setValue(@Nullable Boolean value) {
			this.value = value;
		}
		
		@Override
		public @Nullable String getVisibleName() {
			return name;
		}
		
		@Override
		public void setVisibleName(@Nullable String name) {
			this.name = name;
		}
	}
	
	/**
	 * {@link Option} implementation for double values
	 *
	 * @since 0.1.0
	 */
	class DoubleOption implements Option<Double> {
		/**
		 * The name of the option
		 *
		 * @since 0.1.0
		 */
		protected @Nullable String name;
		/**
		 * The value of the option
		 *
		 * @since 0.1.0
		 */
		protected double value;
		
		/**
		 * Creates a new option with no name and 0 as value.
		 *
		 * @since 0.1.0
		 */
		public DoubleOption() {
			this(null, 0.);
		}
		
		/**
		 * Creates a new option with no name and the specified value.
		 *
		 * @param value The option's value
		 * @since 0.1.0
		 */
		public DoubleOption(@Nullable Double value) {
			this(null, value);
		}
		
		/**
		 * Creates a new option with the specified name and 0 as value.
		 *
		 * @param name The option's name
		 * @since 0.1.0
		 */
		public DoubleOption(@Nullable String name) {
			this(name, 0.);
		}
		
		/**
		 * Creates a new option with the specified name and value.
		 *
		 * @param name  The name of the option
		 * @param value The value of the option
		 * @since 0.1.0
		 */
		public DoubleOption(@Nullable String name, @Nullable Double value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public @Nullable Double getValue() {
			return value;
		}
		
		@Override
		public void setValue(@Nullable Double value) {
			this.value = value;
		}
		
		@Override
		public @Nullable String getVisibleName() {
			return name;
		}
		
		@Override
		public void setVisibleName(@Nullable String name) {
			this.name = name;
		}
	}
	
	/**
	 * {@link Option} implementation for enum values
	 *
	 * @since 0.1.0
	 */
	class EnumOption<T extends Enum<T>> implements Option<T> {
		/**
		 * The name of the option
		 *
		 * @since 0.1.0
		 */
		protected @Nullable String name;
		/**
		 * The value of the option
		 *
		 * @since 0.1.0
		 */
		protected @Nullable T value;
		
		/**
		 * Creates a new option with no name and null as value.
		 *
		 * @since 0.1.0
		 */
		public EnumOption() {
			this(null, null);
		}
		
		/**
		 * Creates a new option with no name and the specified value.
		 *
		 * @param value The option's value
		 * @since 0.1.0
		 */
		public EnumOption(@Nullable T value) {
			this(null, value);
		}
		
		/**
		 * Creates a new option with the specified name and null as value.
		 *
		 * @param name The option's name
		 * @since 0.1.0
		 */
		public EnumOption(@Nullable String name) {
			this(name, null);
		}
		
		/**
		 * Creates a new option with the specified name and value.
		 *
		 * @param name  The name of the option
		 * @param value The value of the option
		 * @since 0.1.0
		 */
		public EnumOption(@Nullable String name, @Nullable T value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public @Nullable T getValue() {
			return value;
		}
		
		@Override
		public void setValue(@Nullable T value) {
			this.value = value;
		}
		
		@Override
		public @Nullable String getVisibleName() {
			return name;
		}
		
		@Override
		public void setVisibleName(@Nullable String name) {
			this.name = name;
		}
	}
	
	/**
	 * {@link Option} implementation for integer values
	 *
	 * @since 0.1.0
	 */
	class IntOption implements Option<Integer> {
		/**
		 * The name of the option
		 *
		 * @since 0.1.0
		 */
		protected @Nullable String name;
		/**
		 * The value of the option
		 *
		 * @since 0.1.0
		 */
		protected int value;
		
		/**
		 * Creates a new option with no name and 0 as value.
		 *
		 * @since 0.1.0
		 */
		public IntOption() {
			this(null, 0);
		}
		
		/**
		 * Creates a new option with no name and the specified value.
		 *
		 * @param value The option's value
		 * @since 0.1.0
		 */
		public IntOption(int value) {
			this(null, value);
		}
		
		/**
		 * Creates a new option with the specified name and 0 as value.
		 *
		 * @param name The option's name
		 * @since 0.1.0
		 */
		public IntOption(@Nullable String name) {
			this(name, 0);
		}
		
		/**
		 * Creates a new option with the specified name and value.
		 *
		 * @param name  The name of the option
		 * @param value The value of the option
		 * @since 0.1.0
		 */
		public IntOption(@Nullable String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public @Nullable Integer getValue() {
			return value;
		}
		
		@Override
		public void setValue(@Nullable Integer value) {
			this.value = value;
		}
		
		@Override
		public @Nullable String getVisibleName() {
			return name;
		}
		
		@Override
		public void setVisibleName(@Nullable String name) {
			this.name = name;
		}
	}
	
	/**
	 * {@link Option} implementation for string values
	 *
	 * @since 0.1.0
	 */
	
	class StringOption implements Option<String> {
		/**
		 * The name of the option
		 *
		 * @since 0.1.0
		 */
		protected @Nullable String name;
		/**
		 * The value of the option
		 *
		 * @since 0.1.0
		 */
		protected @Nullable String value;
		
		/**
		 * Creates a new option with no name and null as value.
		 *
		 * @since 0.1.0
		 */
		public StringOption() {
			this(null, null);
		}
		
		/**
		 * Creates a new option with the specified name and value.
		 *
		 * @param name  The name of the option
		 * @param value The value of the option
		 * @since 0.1.0
		 */
		public StringOption(@Nullable String name, @Nullable String value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public @Nullable String getValue() {
			return value;
		}
		
		@Override
		public void setValue(@Nullable String value) {
			this.value = value;
		}
		
		@Override
		public @Nullable String getVisibleName() {
			return name;
		}
		
		@Override
		public void setVisibleName(@Nullable String name) {
			this.name = name;
		}
	}
}
