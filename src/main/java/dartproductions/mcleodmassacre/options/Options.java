package dartproductions.mcleodmassacre.options;

import dartproductions.mcleodmassacre.options.Option.BooleanOption;
import dartproductions.mcleodmassacre.options.Option.EnumOption;
import dartproductions.mcleodmassacre.options.Option.IntOption;
import dartproductions.mcleodmassacre.options.Option.KeyOption;

import javax.swing.KeyStroke;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An option listing interface that resembles the user-friendly visual display type.
 */
public interface Options extends OptionGroup {
	
	public static Options getDefaultOptions() {
		return new StandardOptions();
	}
	
	/**
	 * Gets a list of the option groups. These groups can be shown to the user as individual option tabs.
	 *
	 * @return The option groups
	 */
	public List<OptionGroup> getGroups();
	
	public static class StandardOptions implements Options {
		public static final String SOUND_OPTIONS = "Sounds", GRAPHICS_OPTIONS = "Graphics", CONTROLS = "Controls", WIDTH = "Width", HEIGHT = "Height", FULLSCREEN = "Fullscreen", QUALITY = "Quality", MUSIC_VOLUME = "Music", SFX_VOLUME = "Sound FX", JUMP_LEFT = "Jump Left", JUMP_RIGHT = "Jump Right", MOVE_UP = "Up", MOVE_DOWN = "Down", MOVE_LEFT = "Left", MOVE_RIGHT = "Right", ATTACK = "Attack", SPECIAL = "Special", GRAB = "Grab", TAUNT = "Taunt", PAUSE = "Pause", WALK = "Walk", SHIELD = "Shield";
		
		public final ArrayList<OptionGroup> groups = new ArrayList<>();
		public String name = "Settings";
		
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
			{
				StandardOptionGroup controls = new StandardOptionGroup(CONTROLS);
				controls.setOption(JUMP_LEFT, new KeyOption(KeyStroke.getKeyStroke("pressed q")));
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
				groups.add(controls);
			}
		}
		
		@Override
		public HashMap<String, Option<?>> getAllSettings() {
			HashMap<String, Option<?>> options = new HashMap<>();
			for(OptionGroup group : getGroups()) {
				options.putAll(group.getAllSettings());
			}
			return options;
		}
		
		@Override
		public String getGroupName() {
			return name;
		}
		
		@Override
		public List<OptionGroup> getGroups() {
			return groups;
		}
	}
}
