package dartproductions.mcleodmassacre;

import dartproductions.mcleodmassacre.engine.GameEngine;
import dartproductions.mcleodmassacre.entity.Background;
import dartproductions.mcleodmassacre.entity.Button;
import dartproductions.mcleodmassacre.entity.Foreground;
import dartproductions.mcleodmassacre.graphics.Animation;
import dartproductions.mcleodmassacre.graphics.Animation.FormattedTextAnimation;
import dartproductions.mcleodmassacre.graphics.Animation.LoopingAnimation;
import dartproductions.mcleodmassacre.graphics.GraphicsManager;
import dartproductions.mcleodmassacre.resources.ResourceManager;
import dartproductions.mcleodmassacre.resources.id.Identifier;
import dartproductions.mcleodmassacre.resources.plugin.Plugin;
import dartproductions.mcleodmassacre.resources.tag.CustomTag;
import dartproductions.mcleodmassacre.resources.tag.Tag;
import dartproductions.mcleodmassacre.sound.SoundManager;
import dartproductions.mcleodmassacre.util.MathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static dartproductions.mcleodmassacre.graphics.ResolutionManager.*;

/**
 * All of the supported states of the application
 *
 * @since 0.1.0
 */
public interface GameState {
	
	/**
	 * This state is used when the game is paused while on a map, fighting with other characters.
	 *
	 * @since 0.1.0
	 */
	@NotNull GameState IN_GAME_PAUSED = new Pause() {
		/**
		 * Tag for resources used in this state
		 *
		 * @since 0.1.0
		 */
		public static final @NotNull Tag RESOURCE_TAG = new CustomTag(Identifier.fromString("tags/pause_resource"), (s1, s2) -> s1 == IN_GAME_PAUSED || (s1.isLoadingState() && s2 == IN_GAME_PAUSED));
	};
	/**
	 * Indicates that the player is playing on a map against other characters.
	 *
	 * @since 0.1.0
	 */
	@NotNull GameState IN_GAME = new GameState() {
		/**
		 * Tag for resources used in this state
		 *
		 * @since 0.1.0
		 */
		public static final @NotNull Tag RESOURCE_TAG = new CustomTag(Identifier.fromString("tags/ingame_resource"), (s1, s2) -> s1 == IN_GAME || (s1.isLoadingState() && s2 == IN_GAME));
		
		@Override
		public @Nullable Identifier getBackgroundMusicTag(@Nullable GameState nextState) {
			return null;
		}
		
		@Override
		public void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			GameState.super.onStateActivation(previousState, previousNextState, nextState);
		}
	};
	/**
	 * The gallery menu
	 *
	 * @since 0.1.0
	 */
	@NotNull GameState GALLERY = new GameState() {
		/**
		 * Tag for resources used in this state
		 * @since 0.1.0
		 */
		public static final @NotNull Tag RESOURCE_TAG = new CustomTag(Identifier.fromString("tags/gallery_resource"), (s1, s2) -> s1 == GALLERY || (s1.isLoadingState() && s2 == GALLERY));
		
		@Override
		public @Nullable Identifier getBackgroundMusicTag(@Nullable GameState nextState) {
			return null;
		}
		
		@Override
		public void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			GameState.super.onStateActivation(previousState, previousNextState, nextState);
		}
	};
	/**
	 * Loading state between two 'normal' states. The next game state should be specified when changing state to loading.
	 *
	 * @since 0.1.0
	 */
	@NotNull GameState LOADING = new Loading() {
		/**
		 * Tag for resources used in this state
		 *
		 * @since 0.1.0
		 */
		public static final @NotNull Tag RESOURCE_TAG = new CustomTag(Identifier.fromString("tags/loading_resource"), (s1, s2) -> s1 == LOADING || (s1.isLoadingState() && s2 == LOADING));
		
		@Override
		public void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			Loading.super.onStateActivation(previousState, previousNextState, nextState);
			/*setToCenter(new Background(new LoopingAnimation("loading"))).register();
			synchronized(GraphicsManager.GRAPHICS_LOCK) {
				ResolutionManager.fillLocalScreen();
			}*/
		}
		
		@Override
		public void onStateDeactivation(@Nullable GameState previousNextState, @NotNull GameState newGameState, @Nullable GameState newNextState) {
		
		}
	};
	@NotNull Logger LOGGER = LogManager.getLogger(GameState.class);
	/**
	 * The roster screen
	 *
	 * @since 0.1.0
	 */
	@NotNull GameState ROSTER = new Menu() {
		/**
		 * Tag for resources used in this state
		 * @since 0.1.0
		 */
		public static final @NotNull Tag RESOURCE_TAG = new CustomTag(Identifier.fromString("tags/roster_resource"), (s1, s2) -> s1 == ROSTER || (s1.isLoadingState() && s2 == ROSTER));
		
		@Override
		public void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			Menu.super.onStateActivation(previousState, previousNextState, nextState);
			Menu.addBackButton(MAIN_MENU);
			//
			//
			//
			new Background(new LoopingAnimation("css_playerboxes")).register();
			new Foreground(new LoopingAnimation("css_top")).register();
			//
			{
				final String[] characters = {"blade", "blue", "azrael", "spikeman", "ronin", "korah", "TODO", "ryder", "cleodbot", "LS", "terro", "sab", "eton", "mitsu", "daichi", "glitch", "pat", "damaus", "meikiru", "dracobot", "internet", "sakuro", "hackensaw", "yjf", "ycoldsteel", "redwolf", "random", "boner", "kfm"};
				final int rowSize = 8;
				final int imageWidth = ResourceManager.getBufferedImage(Identifier.fromString("roster_character_background")).getWidth();
				final int imageHeight = ResourceManager.getBufferedImage(Identifier.fromString("roster_character_background")).getHeight();
				final int topOffset = (int) (imageHeight * 1.5);
				final int spacingHeight = 10;
				final int spacingWidth = -10;
				for(int i = 0; i < characters.length; i++) {
					final String name = characters[i];
					int row = i / rowSize;
					int col = i % rowSize;
					int rowCharacters = Math.min(rowSize, characters.length - row * rowSize);
					int offsetY = topOffset + imageHeight * row + spacingHeight * row;
					int offsetX;
					{
						int rowWidth = rowCharacters * (spacingWidth + imageWidth) - spacingWidth;//one less spacing than image
						int firstOffsetX = (getDefaultScreenSize().width - rowWidth) / 2;
						offsetX = firstOffsetX + col * (spacingWidth + imageWidth);
					}
					Image image = ResourceManager.getImage(Identifier.fromString("roster_mug" + name));
					Foreground character = new Foreground(new LoopingAnimation("roster_mug" + (image == null ? "random" : name), new Dimension(offsetX, offsetY)));
					Foreground border = new Foreground(new LoopingAnimation("roster_character_border", new Dimension(offsetX, offsetY)));
					Button button = new Button(new LoopingAnimation("roster_character_background"), null, null, null, new Point(offsetX, offsetY), () -> {
						//todo
						System.out.println(name);
					});
					button.register();
					character.register();
					border.register();
				}
			}
		}
	};
	/**
	 * The general settings menu
	 *
	 * @since 0.1.0
	 */
	@NotNull GameState SETTINGS_MENU = new Menu() {
		/**
		 * Tag for resources used in this state
		 * @since 0.1.0
		 */
		public static final @NotNull Tag RESOURCE_TAG = new CustomTag(Identifier.fromString("tags/settings_resource"), (s1, s2) -> s1 == SETTINGS_MENU || (s1.isLoadingState() && s2 == SETTINGS_MENU));
		
		@Override
		public void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			Menu.super.onStateActivation(previousState, previousNextState, nextState);
			Menu.addBackButton(MAIN_MENU);
			//todo
		}
	};
	/**
	 * The control settings menu
	 *
	 * @since 0.1.0
	 */
	@NotNull GameState CONTROL_SETTINGS = new Menu() {
		/**
		 * Tag for resources used in this state
		 * @since 0.1.0
		 */
		public static final @NotNull Tag RESOURCE_TAG = new CustomTag(Identifier.fromString("tags/control_settings_resource"), (s1, s2) -> s1 == CONTROL_SETTINGS || (s1.isLoadingState() && s2 == CONTROL_SETTINGS));
		
		@Override
		public void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			Menu.super.onStateActivation(previousState, previousNextState, nextState);
			Menu.addBackButton(SETTINGS_MENU);
			//todo
		}
	};
	/**
	 * The quality settings menu
	 *
	 * @since 0.1.0
	 */
	@NotNull GameState QUALITY_SETTINGS = new Menu() {
		/**
		 * Tag for resources used in this state
		 * @since 0.1.0
		 */
		public static final @NotNull Tag RESOURCE_TAG = new CustomTag(Identifier.fromString("tags/quality_settings_resource"), (s1, s2) -> s1 == QUALITY_SETTINGS || (s1.isLoadingState() && s2 == QUALITY_SETTINGS));
		
		@Override
		public void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			Menu.super.onStateActivation(previousState, previousNextState, nextState);
			Menu.addBackButton(SETTINGS_MENU);
			//todo
		}
	};
	/**
	 * The sound settings menu
	 *
	 * @since 0.1.0
	 */
	@NotNull GameState SOUND_SETTINGS = new Menu() {
		/**
		 * Tag for resources used in this state
		 * @since 0.1.0
		 */
		public static final @NotNull Tag RESOURCE_TAG = new CustomTag(Identifier.fromString("tags/sound_settings_resource"), (s1, s2) -> s1 == SOUND_SETTINGS || (s1.isLoadingState() && s2 == SOUND_SETTINGS));
		
		@Override
		public void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			Menu.super.onStateActivation(previousState, previousNextState, nextState);
			Menu.addBackButton(SETTINGS_MENU);
			//todo
		}
	};
	/**
	 * The menu for playing against other players
	 *
	 * @since 0.1.0
	 */
	@NotNull GameState VERSUS_MENU = new Menu() {
		/**
		 * Tag for resources used in this state
		 * @since 0.1.0
		 */
		public static final @NotNull Tag RESOURCE_TAG = new CustomTag(Identifier.fromString("tags/versus_menu_resource"), (s1, s2) -> s1 == VERSUS_MENU || (s1.isLoadingState() && s2 == VERSUS_MENU));
		
		@Override
		public void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			Menu.super.onStateActivation(previousState, previousNextState, nextState);
			Menu.addBackButton(MAIN_MENU);
			//todo
		}
	};
	/**
	 * State for the main menu. This is the first screen the player sees after the initial loading.
	 *
	 * @since 0.1.0
	 */
	@NotNull GameState MAIN_MENU = new Menu() {
		/**
		 * Tag for resources used in this state
		 * @since 0.1.0
		 */
		public static final @NotNull Tag RESOURCE_TAG = new CustomTag(Identifier.fromString("tags/main_menu_resource"), (s1, s2) -> s1 == MAIN_MENU || (s1.isLoadingState() && s2 == MAIN_MENU));
		
		@Override
		public void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			Menu.super.onStateActivation(previousState, previousNextState, nextState);
			//
			new Button(new LoopingAnimation("mm_singleplayer_button"), null, null, null, new Point(0, 0), () -> Main.setGameState(LOADING, ROSTER)).register();
			new Button(new LoopingAnimation("mm_gallery_button"), null, null, null, new Point(0, 0), () -> Main.setGameState(LOADING, GALLERY)).register();
			new Button(new LoopingAnimation("mm_options_button"), null, null, null, new Point(0, 0), () -> Main.setGameState(LOADING, SETTINGS_MENU)).register();
			new Button(new LoopingAnimation("mm_data_button"), null, null, null, new Point(0, 0), () -> Main.setGameState(LOADING, DATA_MENU)).register();
			new Button(new LoopingAnimation("mm_versus_button"), null, null, null, new Point(0, 0), () -> Main.setGameState(LOADING, VERSUS_MENU)).register();
			//
			new Foreground(new LoopingAnimation("mm_data")).register();
			new Foreground(new LoopingAnimation("mm_gallery")).register();
			new Foreground(new LoopingAnimation("mm_options")).register();
			new Foreground(new LoopingAnimation("mm_solo_placeholder")).register();
			new Foreground(new LoopingAnimation("mm_versus_placeholder")).register();
		}
	};
	/**
	 * The data menu
	 *
	 * @since 0.1.0
	 */
	@NotNull GameState DATA_MENU = new Menu() {
		/**
		 * Tag for resources used in this state
		 * @since 0.1.0
		 */
		public static final @NotNull Tag RESOURCE_TAG = new CustomTag(Identifier.fromString("tags/data_menu_resource"), (s1, s2) -> s1 == DATA_MENU || (s1.isLoadingState() && s2 == DATA_MENU));
		
		@Override
		public void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			Menu.super.onStateActivation(previousState, previousNextState, nextState);
			Menu.addBackButton(MAIN_MENU);
			//
			try {
				List<String> credits = ResourceManager.readTextFile("credits.txt");
				Animation creditsAnim = new FormattedTextAnimation("credits", new Font(Font.SERIF, Font.PLAIN, 12), 1, true, i -> Color.BLACK, i -> credits, i -> new Dimension(0, 0));
				MathUtils.setToCenter(new Foreground(creditsAnim)).register();
				Animation pluginsAnim = new FormattedTextAnimation("plugins", new Font(Font.SERIF, Font.PLAIN, 12), 1, true, i -> Color.BLACK, i -> {
					ArrayList<String> list = new ArrayList<>();
					list.add("» Plugins");
					for(Identifier id : ResourceManager.getRegisteredPlugins()) {
						Plugin plugin = ResourceManager.getPlugin(id);
						list.add(plugin.getName() + " " + plugin.getVersion());
					}
					return list;
				}, i -> new Dimension(0, 0));
				Foreground plugins = new Foreground(pluginsAnim);
				MathUtils.setToCenter(plugins);
				plugins.getLocation().x = getDefaultScreenSize().width - plugins.getCurrentAnimation().getCurrentFrame().getWidth(GraphicsManager.WINDOW) - 20;
				plugins.register();
			} catch(IOException e) {
				LOGGER.error("Could not read credits file", e);
			}
		}
	};
	
	/**
	 * Gets the identifier of the tag that identifies valid background audio for this state.
	 *
	 * @param nextState The next game state as specified by {@link Main#setGameState(GameState, GameState)}
	 * @return The background audio's tag
	 * @since 0.1.0
	 */
	@Nullable Identifier getBackgroundMusicTag(@Nullable GameState nextState);
	
	/**
	 * Checks if this state is a loading state. Loading states have special handling - after resource loading, the state is automatically updated to the next state. For loading states the next state cannot be null.
	 *
	 * @return True if this is a loading state
	 * @since 0.1.0
	 */
	default boolean isLoadingState() {
		return false;
	}
	
	/**
	 * Checks if this state is a 'pause' state. If the active state is a pausing state, the game engine might produce different behaviour.
	 *
	 * @return True if pause
	 * @since 0.1.0
	 */
	default boolean isPausingState() {
		return false;
	}
	
	/**
	 * Runs whenever {@link Main#setGameState(GameState, GameState)} is called with this state as the first parameter. This method is supposed to handle any updates made to entities registered in the game engine or in the rendering engine, together with any action that has to be executed when this state is activated.
	 *
	 * @param previousState     The previous game state
	 * @param previousNextState The expected state after the previous state
	 * @param nextState         The next state after this state as specified by {@link Main#setGameState(GameState, GameState)}
	 * @since 0.1.0
	 */
	default void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
		SoundManager.updateBackgroundMusic();
	}
	
	/**
	 * Runs whenever {@link Main#setGameState(GameState, GameState)} is called when this is the current state. This method runs before {@link #onStateActivation(GameState, GameState, GameState)}.
	 *
	 * @param previousNextState The expected state after this state
	 * @param newGameState      The new game state
	 * @param newNextState      The expected state after the previous state
	 * @see #onStateActivation(GameState, GameState, GameState)
	 * @since 0.1.0
	 */
	default void onStateDeactivation(@Nullable GameState previousNextState, @NotNull GameState newGameState, @Nullable GameState newNextState) {
		GraphicsManager.clearLayers();
		GameEngine.unregisterAllEntities();
		SoundManager.stopAllSfx();
	}
	
	/**
	 * A game state interface for loading screens. Implements {@link #isLoadingState()} to return true, and {@link #getBackgroundMusicTag(GameState)} to return the tag of the next state.
	 *
	 * @since 0.1.0
	 */
	interface Loading extends GameState {
		@Override
		default @Nullable Identifier getBackgroundMusicTag(@Nullable GameState nextState) {
			if(nextState == null) {
				return null;
			}
			return nextState.getBackgroundMusicTag(null);
		}
		
		@Override
		default boolean isLoadingState() {
			return true;
		}
	}
	
	/**
	 * A game state interface for menus. Implements {@link #getBackgroundMusicTag(GameState)} to return {@link Tag#MENU_RESOURCE}.
	 *
	 * @since 0.1.0
	 */
	interface Menu extends GameState {
		
		/**
		 * Adds the 'previous menu' button.
		 *
		 * @param state The state to change to when the button is pressed
		 * @since 0.1.0
		 */
		static void addBackButton(@NotNull GameState state) {
			new Button(new LoopingAnimation("mm_back_button"), null, null, null, new Point(0, 0), () -> Main.setGameState(LOADING, state)).register();
		}
		
		@Override
		@NotNull
		default Identifier getBackgroundMusicTag(@Nullable GameState nextState) {
			return Tag.MENU_RESOURCE.getId();
		}
		
		@Override
		default void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			GameState.super.onStateActivation(previousState, previousNextState, nextState);
			new Background(new LoopingAnimation("menu_background")).register();
			new Background(new LoopingAnimation("mm_border")).register();
		}
	}
	
	/**
	 * A game state interface for pause screens. Implements {@link #isPausingState()} to return true.
	 */
	interface Pause extends GameState {
		@Override
		@NotNull
		default Identifier getBackgroundMusicTag(@Nullable GameState nextState) {
			return Tag.MENU_RESOURCE.getId();
		}
		
		@Override
		default boolean isPausingState() {
			return true;
		}
		
		@Override
		default void onStateActivation(@NotNull GameState previousState, @Nullable GameState previousNextState, @Nullable GameState nextState) {
			GameState.super.onStateActivation(previousState, previousNextState, nextState);
		}
		
		@Override
		default void onStateDeactivation(@Nullable GameState previousNextState, @NotNull GameState newGameState, @Nullable GameState newNextState) {
			GameState.super.onStateDeactivation(previousNextState, newGameState, newNextState);
		}
	}
}
