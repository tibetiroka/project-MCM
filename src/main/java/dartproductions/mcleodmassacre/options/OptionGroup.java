/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.options;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import dartproductions.mcleodmassacre.options.OptionGroup.StandardOptionGroup.StringOptionMapAdapter.Wrapper.WrapperAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A simple interface for grouping and accessing multiple {@link Option settings}.
 *
 * @since 0.1.0
 */
public interface OptionGroup {
	
	/**
	 * Gets all options in this group.
	 *
	 * @return The options
	 * @since 0.1.0
	 */
	@NotNull HashMap<String, Option<?>> getAllSettings();
	
	/**
	 * Gets the name of the option group.
	 *
	 * @return The name
	 * @since 0.1.0
	 */
	@NotNull String getGroupName();
	
	/**
	 * Gets the specified setting from {@link #getAllSettings()}. The setting is cast to the specified type.
	 *
	 * @param name The name of the setting
	 * @param type The class of the option's value
	 * @param <T>  The type of the option's value
	 * @return The option or null if not found
	 * @since 0.1.0
	 */
	default @Nullable <T> Option<T> getSetting(String name, Class<T> type) {
		return (Option<T>) getSetting(name);
	}
	
	/**
	 * Gets the specified setting from {@link #getAllSettings()}
	 *
	 * @param name The name of the option
	 * @return The option or null if not found
	 * @since 0.1.0
	 */
	default @Nullable Option<?> getSetting(String name) {
		return getAllSettings().get(name);
	}
	
	/**
	 * Gets the value of an option.
	 *
	 * @param name The name of the option
	 * @param type The class of the value
	 * @param <T>  The type of the value
	 * @return The value
	 * @throws NullPointerException If the option is not found
	 * @since 0.1.0
	 */
	default @Nullable <T> T getSettingValue(@NotNull String name, @NotNull Class<T> type) throws NullPointerException {
		return getSetting(name, type).getValue();
	}
	
	/**
	 * Standard implementation of {@link OptionGroup}.
	 *
	 * @since 0.1.0
	 */
	class StandardOptionGroup implements OptionGroup {
		/**
		 * The map of settings. The key is the name of the option
		 *
		 * @since 0.1.0
		 */
		@JsonAdapter(StringOptionMapAdapter.class)
		public final @NotNull HashMap<String, Option<?>> options = new HashMap<>();
		/**
		 * The name of this option group
		 *
		 * @since 0.1.0
		 */
		public @NotNull String name;
		
		/**
		 * Creates a new {@link StandardOptionGroup} with the specified name.
		 *
		 * @param name The name of the group
		 * @since 0.1.0
		 */
		public StandardOptionGroup(@NotNull String name) {
			this.name = name;
		}
		
		
		@Override
		public @NotNull HashMap<String, Option<?>> getAllSettings() {
			return options;
		}
		
		@Override
		public @NotNull String getGroupName() {
			return name;
		}
		
		/**
		 * Sets the name of this option group.
		 *
		 * @param name The new name of the group
		 * @since 0.1.0
		 */
		public void setGroupName(@NotNull String name) {
			this.name = name;
		}
		
		/**
		 * Adds the specified option to this option group. The visible name of the option is changed to the specified name if it is not yet specified in the option.
		 *
		 * @param name   The name of the option to use
		 * @param option The option
		 * @since 0.1.0
		 */
		public void setOption(@NotNull String name, @NotNull Option<?> option) {
			options.put(name, option);
			if(option.getVisibleName() == null) {
				option.setVisibleName(name);
			}
		}
		
		/**
		 * JSON serializer and deserializer for string-option maps (such as {@link #options}).
		 *
		 * @since 0.1.0
		 */
		protected static final class StringOptionMapAdapter implements JsonSerializer<HashMap<String, Option<?>>>, JsonDeserializer<HashMap<String, Option<?>>> {
			@Override
			public HashMap<String, Option<?>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				HashMap<String, Option<?>> options = new HashMap<>();
				for(Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
					String name = entry.getKey();
					JsonObject element = entry.getValue().getAsJsonObject();
					Wrapper wrapper = context.deserialize(element, Wrapper.class);
					options.put(name, (Option<?>) wrapper.getData());
				}
				return options;
			}
			
			@Override
			public JsonElement serialize(HashMap<String, Option<?>> src, Type typeOfSrc, JsonSerializationContext context) {
				HashMap<String, Wrapper> wrapped = new HashMap<>();
				src.forEach((key, value) -> wrapped.put(key, new Wrapper(value.getClass().getName(), value)));
				return context.serialize(wrapped, Map.class);
			}
			
			/**
			 * General wrapper class for storing objects of unknown types in JSON form. It stores the class name of the object and its data as JSON. This class can be used to deserialize unknown subclasses of a class.
			 *
			 * @since 0.1.0
			 */
			@JsonAdapter(WrapperAdapter.class)
			protected static final class Wrapper {
				/**
				 * The fully qualified class name of the {@link #data} object's class. The value of this field is null if and only if the value of {@link #data} field is null.
				 *
				 * @since 0.1.0
				 */
				@SerializedName("class")
				public String type;
				/**
				 * The wrapped object
				 *
				 * @see #type
				 * @since 0.1.0
				 */
				@SerializedName("data")
				public Object data;
				
				/**
				 * Creates a new {@link Wrapper} with no type and data specified. These values will default to 'null'.
				 */
				public Wrapper() {
					this(null, null);
				}
				
				/**
				 * Constructs a new {@link Wrapper} with the specified type and data. The type should match the fully qualified name of the data object's class, or be null if the data object is null.
				 *
				 * @param type The type of the data object
				 * @param data The wrapped object
				 * @since 0.1.0
				 */
				public Wrapper(@Nullable String type, @Nullable Object data) {
					this.type = type;
					this.data = data;
				}
				
				/**
				 * Constructs a new {@link Wrapper} with the specified data. The type is chosen automatically based on the object's class.
				 *
				 * @param data The data object to wrap
				 * @since 0.1.0
				 */
				public Wrapper(@Nullable Object data) {
					this(data == null ? null : data.getClass().getName(), data);
				}
				
				/**
				 * Gets the fully qualified name of the class of the wrapped data object
				 *
				 * @return The name of the {@link #data}'s class
				 * @since 0.1.0
				 */
				public @Nullable String getType() {
					return type;
				}
				
				/**
				 * Sets the fully qualified name of the class of the wrapped data object.
				 *
				 * @param type The new name of the data object's type
				 * @since 0.1.0
				 */
				public void setType(@Nullable String type) {
					this.type = type;
				}
				
				/**
				 * Gets the wrapped data object.
				 *
				 * @return The {@link #data} object
				 * @since 0.1.0
				 */
				public @Nullable Object getData() {
					return data;
				}
				
				/**
				 * Sets the wrapped data object. Doesn't change the {@link #type} specified.
				 *
				 * @param data The new data object
				 * @since 0.1.0
				 */
				public void setData(@Nullable Object data) {
					this.data = data;
				}
				
				/**
				 * JSON serializer and deserializer for {@link Wrapper} objects.
				 *
				 * @since 0.1.0
				 */
				protected static final class WrapperAdapter implements JsonDeserializer<Wrapper>, JsonSerializer<Wrapper> {
					
					@Override
					public Wrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
						Wrapper wrapper = new Wrapper();
						try {
							Class<?> c = Class.forName(json.getAsJsonObject().get("class").getAsString());
							wrapper.type = c.getName();
							wrapper.data = context.deserialize(json.getAsJsonObject().get("data"), c);
						} catch(ClassNotFoundException e) {
							e.printStackTrace();
						}
						return wrapper;
					}
					
					@Override
					public JsonElement serialize(Wrapper src, Type typeOfSrc, JsonSerializationContext context) {
						JsonObject object = new JsonObject();
						object.add("class", context.serialize(src.type));
						try {
							object.add("data", context.serialize(src.data, Class.forName(src.type)));
						} catch(ClassNotFoundException e) {
							e.printStackTrace();
						}
						return object;
					}
				}
			}
		}
	}
}
