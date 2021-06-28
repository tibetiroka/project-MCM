/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.map;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import dartproductions.mcleodmassacre.GameState;
import dartproductions.mcleodmassacre.entity.Background;
import dartproductions.mcleodmassacre.entity.GroundEntity;
import dartproductions.mcleodmassacre.entity.PlayerEntity;
import dartproductions.mcleodmassacre.graphics.ResolutionManager;
import dartproductions.mcleodmassacre.graphics.animation.StandardAnimation;
import dartproductions.mcleodmassacre.map.Map.MapConfiguration.MapObjectConfiguration;
import dartproductions.mcleodmassacre.resources.IllegalConfigurationException;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import dartproductions.mcleodmassacre.util.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * A game state implementation that represents maps.
 *
 * @since 0.1.0
 */
public class Map implements GameState {
	/**
	 * The id of the map
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull Identifier id;
	/**
	 * The configuration of the map
	 *
	 * @since 0.1.0
	 */
	protected final @NotNull MapConfiguration config;
	/**
	 * The identifier of the tag for valid background music
	 *
	 * @since 0.1.0
	 */
	protected final @Nullable Identifier musicTag;
	/**
	 * The objects present on this map. Only contains the objects when this game state is active.
	 *
	 * @since 0.1.0
	 */
	protected final ArrayList<GroundEntity> objects = new ArrayList<>();
	
	/**
	 * Creates a new map from a configuration.
	 *
	 * @param file The config file
	 * @param id   The id of the map
	 * @throws FileNotFoundException If the config file doesn't exist
	 * @since 0.1.0
	 */
	public Map(@NotNull File file, @NotNull Identifier id) throws FileNotFoundException {
		this.id = id;
		config = new Gson().fromJson(new FileReader(file), MapConfiguration.class);
		config.verify();
		musicTag = config.backgroundMusicTag == null ? null : Identifier.fromString(config.backgroundMusicTag);
	}
	
	@Override
	public @Nullable Identifier getBackgroundMusicTag(@Nullable GameState nextState) {
		return musicTag;
	}
	
	@Override
	public void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
		GameState.super.onStateActivation(previousState, previousNextState, nextState);
		int centerX = ResolutionManager.getCenter().x;
		int centerY = ResolutionManager.getCenter().y;
		if(config.centerLocation != null) {
			if(!config.centerLocation.equalsIgnoreCase("default")) {
				String[] parts = config.centerLocation.split("\\s+");
				centerX += Integer.parseInt(parts[0]);
				centerY += Integer.parseInt(parts[1]);
			}
		}
		for(MapObjectConfiguration object : config.objects) {
			GroundEntity entity = new GroundEntity(new StandardAnimation(object.hitbox), new Point(object.x + centerX, object.y + centerY));
			entity.register();
			objects.add(entity);
		}
		Background background = new Background(new StandardAnimation(config.background));
		Point center = MathUtils.getCenter(MathUtils.getSize(background.getCurrentAnimation()));
		background.move(center.x, center.y);
		background.register();
		//todo character spawning
	}
	
	@Override
	public void onStateDeactivation(@Nullable GameState previousNextState, @NotNull GameState newGameState, @Nullable GameState newNextState) {
		GameState.super.onStateDeactivation(previousNextState, newGameState, newNextState);
		objects.clear();
	}
	
	@Override
	public @NotNull Identifier getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return id.toString();
	}
	
	/**
	 * Class that provides a JSON-parsable format for map configurations.
	 *
	 * @since 0.1.0
	 */
	protected static final class MapConfiguration {
		/**
		 * The name of the map
		 *
		 * @since 0.1.0
		 */
		@SerializedName(value = "name", alternate = {"displayname", "display name", "map name"})
		protected @Nullable String name;
		/**
		 * The objects on the map that have collisions.
		 *
		 * @see MapObjectConfiguration
		 * @since 0.1.0
		 */
		@SerializedName(value = "objects", alternate = {"map objects", "mapObjects"})
		protected @Nullable MapObjectConfiguration[] objects;
		/**
		 * The center of the map. This point has the (0,0) coordinates in the map's coordinate system. The location of the objects are specified relative to this position. If null or 'default', the coordinates of the center of the screen are used. Otherwise it consists of the x and y integer coordinates, separated by one or more whitespace characters.
		 *
		 * @since 0.1.0
		 */
		@SerializedName(value = "center", alternate = {"centerLocation", "mapCenter", "origin"})
		protected @Nullable String centerLocation;
		/**
		 * The ID of the background image for the map.
		 *
		 * @since 0.1.0
		 */
		@SerializedName(value = "background", alternate = {"map background", "mapBackground"})
		protected @Nullable String background;
		/**
		 * The list of positions where players ({@link PlayerEntity player entities} controlled by real people) can spawn. The coordinates are relative to the {@link #centerLocation center} of the map.
		 *
		 * @since 0.1.0
		 */
		@SerializedName(value = "playerSpawns", alternate = {"player spawns"})
		protected @Nullable Point[] playerSpawns;
		/**
		 * The list of positions where NPCs ({@link PlayerEntity player entities} NOT controlled by real people) can spawn. The coordinates are relative to the {@link #centerLocation center} of the map.
		 *
		 * @since 0.1.0
		 */
		@SerializedName(value = "npcSpawns", alternate = {"npc spawns"})
		protected @Nullable Point[] npcSpawns;
		/**
		 * The tag that describes valid background music
		 *
		 * @since 0.1.0
		 */
		@SerializedName(value = "backgroundMusicTag", alternate = {"music", "musicTag", "background music", "backgroundMusic"})
		protected @Nullable String backgroundMusicTag;
		
		/**
		 * Checks whether every value is configured properly.
		 *
		 * @since 0.1.0
		 */
		public void verify() {
			if(name == null) {
				throw new IllegalConfigurationException("Map name must be specified");
			}
			if(background == null) {
				throw new IllegalConfigurationException("Map background image must be specified");
			}
			if(objects == null || objects.length == 0) {
				throw new IllegalConfigurationException("Map must have objects specified");
			} else {
				for(MapObjectConfiguration object : objects) {
					if(object == null) {
						throw new IllegalConfigurationException("Null objects are not allowed in maps");
					}
					if(object.hitbox == null) {
						throw new IllegalConfigurationException("All objects must have a hitbox specified");
					}
					try {
						Identifier.fromString(object.hitbox);
					} catch(Exception e) {
						throw new IllegalConfigurationException("Invalid hitbox for object", e);
					}
				}
			}
			if(centerLocation != null) {
				if(!centerLocation.equalsIgnoreCase("default")) {
					String[] s = centerLocation.split("\\s+");
					if(s.length != 2) {
						throw new IllegalConfigurationException("Center location must be null, 'default', or 2 coordinates");
					}
					try {
						Integer.parseInt(s[0]);
						Integer.parseInt(s[1]);
					} catch(Exception e) {
						throw new IllegalConfigurationException("Illegal coordinates for map center location", e);
					}
				}
			}
			if(playerSpawns == null || playerSpawns.length == 0) {
				throw new IllegalConfigurationException("At least one player spawn must be specified");
			}
			if(npcSpawns == null || npcSpawns.length == 0) {
				throw new IllegalConfigurationException("At least one npc spawn must be specified");
			}
			if(backgroundMusicTag != null) {
				try {
					Identifier.fromString(backgroundMusicTag);
				} catch(Exception e) {
					throw new IllegalStateException("Invalid background music tag", e);
				}
			}
		}
		
		/**
		 * A class that provides JSON-parsable format for map objects.
		 *
		 * @since 0.1.0
		 */
		protected static final class MapObjectConfiguration {
			/**
			 * The x coordinate of the object, relative to {@link MapConfiguration#centerLocation}
			 *
			 * @since 0.1.0
			 */
			@SerializedName(value = "x", alternate = {"x coordinate", "x distance", "distance x", "coordinate x", "x offset", "offset x"})
			protected int x;
			/**
			 * The y coordinate of the object, relative to {@link MapConfiguration#centerLocation}
			 *
			 * @since 0.1.0
			 */
			@SerializedName(value = "y", alternate = {"y coordinate", "y distance", "distance y", "coordinate y", "y offset", "offset y"})
			protected int y;
			/**
			 * The id of the hitbox of this object.
			 *
			 * @since 0.1.0
			 */
			@SerializedName(value = "hitbox", alternate = {"image", "graphics", "image hitbox", "imageHitbox", "bounds"})
			protected @Nullable String hitbox;
		}
	}
}
