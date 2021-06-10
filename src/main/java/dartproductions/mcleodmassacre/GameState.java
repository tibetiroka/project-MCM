package dartproductions.mcleodmassacre;

/**
 * All of the supported states of the application
 *
 * @since 0.1.0
 */
public enum GameState {
	/**
	 * Loading state between two 'normal' states. The next game state should be specified when changing state to loading.
	 *
	 * @since 0.1.0
	 */
	LOADING,
	/**
	 * State for the main menu. This is the first screen the player sees after the initial loading.
	 *
	 * @since 0.1.0
	 */
	MAIN_MENU,
	/**
	 * This state is used when the game is paused while on a map, fighting with other characters.
	 *
	 * @since 0.1.0
	 */
	IN_GAME_PAUSED,
	/**
	 * The general settings menu
	 *
	 * @since 0.1.0
	 */
	SETTINGS_MENU,
	/**
	 * The sound settings menu
	 *
	 * @since 0.1.0
	 */
	SOUND_SETTINGS,
	/**
	 * The control settings menu
	 *
	 * @since 0.1.0
	 */
	CONTROL_SETTINGS,
	/**
	 * The quality settings menu
	 *
	 * @since 0.1.0
	 */
	QUALITY_SETTINGS,
	/**
	 * The roster screen
	 *
	 * @since 0.1.0
	 */
	ROSTER,
	/**
	 * Indicates that the player is playing on a map against other characters.
	 *
	 * @since 0.1.0
	 */
	IN_GAME,
	/**
	 * The gallery menu
	 *
	 * @since 0.1.0
	 */
	GALLERY,
	/**
	 * The data menu
	 *
	 * @since 0.1.0
	 */
	DATA_MENU,
	/**
	 * The menu for playing agains other players
	 *
	 * @since 0.1.0
	 */
	VERSUS_MENU,
	/**
	 * The credits screen
	 *
	 * @since 0.1.0
	 */
	CREDITS
}
