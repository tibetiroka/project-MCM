package dartproductions.mcleodmassacre.options;

import java.util.HashMap;

/**
 * A simple interface for grouping and accessing multiple {@link Option settings}.
 */
public interface OptionGroup {
	
	/**
	 * Gets all options in this group.
	 *
	 * @return The options
	 */
	public HashMap<String, Option<?>> getAllSettings();
	
	/**
	 * Gets the specified setting from {@link #getAllSettings()}
	 *
	 * @param name The name of the option
	 * @return The option or null if not found
	 */
	public default Option<?> getSetting(String name) {
		return getAllSettings().get(name);
	}
	
	/**
	 * Gets the specified setting from {@link #getAllSettings()}. The setting is cast to the specified type.
	 *
	 * @param name The name of the setting
	 * @param type The class of the option's value
	 * @param <T>  The type of the option's value
	 * @return The option or null if not found
	 */
	public default <T> Option<T> getSetting(String name, Class<T> type) {
		return (Option<T>) getSetting(name);
	}
	
	/**
	 * Gets the name of the option group.
	 *
	 * @return The name
	 */
	public String getGroupName();
	
	/**
	 * Gets the value of an option.
	 *
	 * @param name The name of the option
	 * @param type The class of the value
	 * @param <T>  The type of the value
	 * @return The value
	 * @throws NullPointerException If the option is not found
	 */
	public default <T> T getSettingValue(String name, Class<T> type) throws NullPointerException {
		return getSetting(name, type).getValue();
	}
	
	public static class StandardOptionGroup implements OptionGroup {
		public String name;
		public HashMap<String, Option<?>> options = new HashMap<>();
		
		public StandardOptionGroup(String name) {
			this.name = name;
		}
		
		
		@Override
		public HashMap<String, Option<?>> getAllSettings() {
			return options;
		}
		
		@Override
		public String getGroupName() {
			return name;
		}
		
		public void setGroupName(String name) {
			this.name = name;
		}
		
		public void setOption(String name, Option<?> option) {
			options.put(name, option);
			if(option.getVisibleName() == null) {
				option.setVisibleName(name);
			}
		}
	}
}
