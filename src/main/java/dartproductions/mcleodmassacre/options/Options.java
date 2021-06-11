package dartproductions.mcleodmassacre.options;

import dartproductions.mcleodmassacre.options.Option.BooleanOption;
import dartproductions.mcleodmassacre.options.Option.EnumOption;
import dartproductions.mcleodmassacre.options.Option.IntOption;
import org.jetbrains.annotations.NotNull;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An option listing interface that resembles the user-friendly visual display type.
 *
 * @since 0.1.0
 */
public interface Options extends OptionGroup {
	
	/**
	 * Gets the default settings for the game.
	 *
	 * @return The default settings
	 * @since 0.1.0
	 */
	static @NotNull Options getDefaultOptions() {
		return new StandardOptions();
	}
	
	/**
	 * Gets a list of the option groups. These groups can be shown to the user as individual option tabs.
	 *
	 * @return The option groups
	 * @since 0.1.0
	 */
	@NotNull List<OptionGroup> getGroups();
	
	/**
	 * The standard game options
	 *
	 * @since 0.1.0
	 */
	class StandardOptions implements Options {
		public static final String SOUND_OPTIONS = "Sounds", GRAPHICS_OPTIONS = "Graphics", CONTROLS = "Controls", WIDTH = "Width", HEIGHT = "Height", FULLSCREEN = "Fullscreen", QUALITY = "Quality", MUSIC_VOLUME = "Music", SFX_VOLUME = "Sound FX";
		
		public final @NotNull ArrayList<OptionGroup> groups = new ArrayList<>();
		public @NotNull String name = "Settings";
		
		public StandardOptions() {
			{
				StandardOptionGroup graphics = new StandardOptionGroup(GRAPHICS_OPTIONS);
				graphics.setOption(WIDTH, new IntOption((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()));
				graphics.setOption(HEIGHT, new IntOption((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()));
				graphics.setOption(FULLSCREEN, new BooleanOption(false));
				graphics.setOption(QUALITY, new EnumOption<>(QualityOption.HIGH));
				groups.add(graphics);
			}
			{
				StandardOptionGroup sound = new StandardOptionGroup(SOUND_OPTIONS);
				sound.setOption(MUSIC_VOLUME, new IntOption(50));
				sound.setOption(SFX_VOLUME, new IntOption(50));
				groups.add(sound);
			}
			{//todo proper key binds for input manager
				
				//	StandardOptionGroup controls = new StandardOptionGroup(CONTROLS);
				/*for(ActionType value : ActionType.values()) {
					System.out.println((value.isPress?"pressed":"released")+KeyStroke.getKeyStroke(value.keybind,0).getKeyChar());
					controls.setOption(value.name,new KeyOption(KeyStroke.getKeyStroke((value.isPress?"pressed":"released")+KeyStroke.getKeyStroke(value.keybind,0).getKeyChar())));
				}*/
			/*	for(ActionType type : ActionType.values) {
					controls.setOption(type.name, new EnumOption<>(type));
				}*/
				/*controls.setOption(JUMP_LEFT, new KeyOption(KeyStroke.getKeyStroke("pressed q")));
				controls.setOption(JUMP_RIGHT, new KeyOption(KeyStroke.getKeyStroke("pressed e")));
				controls.setOption(MOVE_UP, new KeyOption(KeyStroke.getKeyStroke("pressed w")));
				controls.setOption(MOVE_DOWN, new KeyOption(KeyStroke.getKeyStroke("pressed s")));
				controls.setOption(MOVE_LEFT, new KeyOption(KeyStroke.getKeyStroke("pressed a")));
				controls.setOption(MOVE_RIGHT, new KeyOption(KeyStroke.getKeyStroke("pressed d")));
				controls.setOption(ATTACK, new KeyOption(KeyStroke.getKeyStroke("pressed p")));
				controls.setOption(SPECIAL, new KeyOption(KeyStroke.getKeyStroke("pressed o")));
				controls.setOption(GRAB, new KeyOption(KeyStroke.getKeyStroke("pressed u")));
				controls.setOption(TAUNT, new KeyOption(KeyStroke.getKeyStroke("pressed k")));
				controls.setOption(WALK, new KeyOption(KeyStroke.getKeyStroke("pressed l")));
				controls.setOption(PAUSE, new KeyOption(KeyStroke.getKeyStroke("pressed BACKSPACE")));
				controls.setOption(SHIELD, new KeyOption(KeyStroke.getKeyStroke("pressed i"))); 
 * @since 0.1.0 
 */
				//	groups.add(controls);
			}
		}
		
		@Override
		public @NotNull HashMap<String, Option<?>> getAllSettings() {
			HashMap<String, Option<?>> options = new HashMap<>();
			for(OptionGroup group : getGroups()) {
				options.putAll(group.getAllSettings());
			}
			return options;
		}
		
		@Override
		public @NotNull String getGroupName() {
			return name;
		}
		
		@Override
		public @NotNull List<OptionGroup> getGroups() {
			return groups;
		}
	}
}
