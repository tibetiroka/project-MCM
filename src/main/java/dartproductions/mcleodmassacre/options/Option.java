package dartproductions.mcleodmassacre.options;


import javax.swing.KeyStroke;

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
	public T getValue();
	
	/**
	 * Sets the value of this setting.
	 *
	 * @param t The new value
	 */
	public void setValue(T t);
	
	/**
	 * Gets the visible name of this setting. This name can be displayed to users.
	 *
	 * @return The name of this setting
	 */
	public String getVisibleName();
	
	/**
	 * Sets the visible name of this setting. This name can be displayed to users.
	 *
	 * @param name The new name of this setting
	 */
	public void setVisibleName(String name);
	
	public static class IntOption implements Option<Integer> {
		protected int value;
		protected String name;
		
		public IntOption() {
			this(null, 0);
		}
		
		public IntOption(int value) {
			this(null, value);
		}
		
		public IntOption(String name) {
			this(name, 0);
		}
		
		public IntOption(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public Integer getValue() {
			return value;
		}
		
		@Override
		public void setValue(Integer value) {
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
	}
	
	public static class DoubleOption implements Option<Double> {
		protected double value;
		protected String name;
		
		public DoubleOption() {
			this(null, 0.);
		}
		
		public DoubleOption(Double value) {
			this(null, value);
		}
		
		public DoubleOption(String name) {
			this(name, 0.);
		}
		
		public DoubleOption(String name, Double value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public Double getValue() {
			return value;
		}
		
		@Override
		public void setValue(Double value) {
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
	}
	
	public static class StringOption implements Option<String> {
		protected String value;
		protected String name;
		
		public StringOption() {
			this(null, null);
		}
		
		public StringOption(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public String getValue() {
			return value;
		}
		
		@Override
		public void setValue(String value) {
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
	}
	
	public static class BooleanOption implements Option<Boolean> {
		protected boolean value;
		protected String name;
		
		public BooleanOption() {
			this(null, false);
		}
		
		public BooleanOption(boolean value) {
			this(null, value);
		}
		
		public BooleanOption(String name) {
			this(name, false);
		}
		
		public BooleanOption(String name, boolean value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public Boolean getValue() {
			return value;
		}
		
		@Override
		public void setValue(Boolean value) {
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
	}
	public static class EnumOption<T extends Enum<T>> implements Option<T> {
		protected T value;
		protected String name;
		
		public EnumOption() {
			this(null, null);
		}
		
		public EnumOption(T value) {
			this(null, value);
		}
		
		public EnumOption(String name) {
			this(name, null);
		}
		
		public EnumOption(String name, T value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public T getValue() {
			return value;
		}
		
		@Override
		public void setValue(T value) {
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
	}
	
	public class KeyOption implements Option<KeyStroke>{
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
	}
}
