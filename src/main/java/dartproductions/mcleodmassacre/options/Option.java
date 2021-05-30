package dartproductions.mcleodmassacre.options;


import org.jetbrains.annotations.Nullable;

/**
 * A single setting in the options.
 *
 * @param <T> The type of the data stored
 */
public interface Option<T> {
	
	/**
	 * Gets the value of this setting.
	 *
	 * @return The current setting
	 */
	public @Nullable T getValue();
	
	/**
	 * Sets the value of this setting.
	 *
	 * @param t The new value
	 */
	public void setValue(@Nullable T t);
	
	/**
	 * Gets the visible name of this setting. This name can be displayed to users.
	 *
	 * @return The name of this setting
	 */
	public @Nullable String getVisibleName();
	
	/**
	 * Sets the visible name of this setting. This name can be displayed to users.
	 *
	 * @param name The new name of this setting
	 */
	public void setVisibleName(@Nullable String name);
	
	/**
	 * {@link Option} implementation for integer values
	 */
	public static class IntOption implements Option<Integer> {
		/**
		 * The value of the option
		 */
		protected int value;
		/**
		 * The name of the option
		 */
		protected @Nullable String name;
		
		/**
		 * Creates a new option with no name and 0 as value.
		 */
		public IntOption() {
			this(null, 0);
		}
		
		/**
		 * Creates a new option with no name and the specified value.
		 *
		 * @param value The option's value
		 */
		public IntOption(int value) {
			this(null, value);
		}
		
		/**
		 * Creates a new option with the specified name and 0 as value.
		 *
		 * @param name The option's name
		 */
		public IntOption(@Nullable String name) {
			this(name, 0);
		}
		
		/**
		 * Creates a new option with the specified name and value.
		 *
		 * @param name  The name of the option
		 * @param value The value of the option
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
	 * {@link Option} implementation for double values
	 */
	public static class DoubleOption implements Option<Double> {
		/**
		 * The value of the option
		 */
		protected double value;
		/**
		 * The name of the option
		 */
		protected @Nullable String name;
		
		/**
		 * Creates a new option with no name and 0 as value.
		 */
		public DoubleOption() {
			this(null, 0.);
		}
		
		/**
		 * Creates a new option with no name and the specified value.
		 *
		 * @param value The option's value
		 */
		public DoubleOption(@Nullable Double value) {
			this(null, value);
		}
		
		/**
		 * Creates a new option with the specified name and 0 as value.
		 *
		 * @param name The option's name
		 */
		public DoubleOption(@Nullable String name) {
			this(name, 0.);
		}
		
		/**
		 * Creates a new option with the specified name and value.
		 *
		 * @param name  The name of the option
		 * @param value The value of the option
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
	 * {@link Option} implementation for string values
	 */
	
	public static class StringOption implements Option<String> {
		/**
		 * The value of the option
		 */
		protected @Nullable String value;
		/**
		 * The name of the option
		 */
		protected @Nullable String name;
		
		/**
		 * Creates a new option with no name and null as value.
		 */
		public StringOption() {
			this(null, null);
		}
		
		/**
		 * Creates a new option with the specified name and value.
		 *
		 * @param name  The name of the option
		 * @param value The value of the option
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
	
	/**
	 * {@link Option} implementation for boolean values
	 */
	public static class BooleanOption implements Option<Boolean> {
		/**
		 * The value of the option
		 */
		protected boolean value;
		/**
		 * The name of the option
		 */
		protected @Nullable String name;
		
		/**
		 * Creates a new option with no name and false as value.
		 */
		public BooleanOption() {
			this(null, false);
		}
		
		/**
		 * Creates a new option with no name and the specified value.
		 *
		 * @param value The option's value
		 */
		public BooleanOption(boolean value) {
			this(null, value);
		}
		
		/**
		 * Creates a new option with the specified name and false as value.
		 *
		 * @param name The option's name
		 */
		public BooleanOption(@Nullable String name) {
			this(name, false);
		}
		
		/**
		 * Creates a new option with the specified name and value.
		 *
		 * @param name  The name of the option
		 * @param value The value of the option
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
	 * {@link Option} implementation for enum values
	 */
	public static class EnumOption<T extends Enum<T>> implements Option<T> {
		/**
		 * The value of the option
		 */
		protected @Nullable T value;
		/**
		 * The name of the option
		 */
		protected @Nullable String name;
		
		/**
		 * Creates a new option with no name and null as value.
		 */
		public EnumOption() {
			this(null, null);
		}
		
		/**
		 * Creates a new option with no name and the specified value.
		 *
		 * @param value The option's value
		 */
		public EnumOption(@Nullable T value) {
			this(null, value);
		}
		
		/**
		 * Creates a new option with the specified name and null as value.
		 *
		 * @param name The option's name
		 */
		public EnumOption(@Nullable String name) {
			this(name, null);
		}
		
		/**
		 * Creates a new option with the specified name and value.
		 *
		 * @param name  The name of the option
		 * @param value The value of the option
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
	
	/*public class KeyOption implements Option<KeyStroke>{
		protected KeyStroke value;
		protected String name;
		
		public KeyOption() {
			this(null, null);
		}
		
		public KeyOption(KeyStroke value) {
			this(null, value);
		}
		
		public KeyOption(String name) {
			this(name, null);
		}
		
		public KeyOption(String name, KeyStroke value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public KeyStroke getValue() {
			return value;
		}
		
		@Override
		public void setValue(KeyStroke value) {
			this.value = value;
		}
		
		@Override
		public String getVisibleName() {
			return name;
		}
		
		@Override
		public void setVisibleName(String name) {
			this.name = name;
		}
	}*/
}
