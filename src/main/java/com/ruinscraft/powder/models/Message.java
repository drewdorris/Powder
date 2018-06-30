package com.ruinscraft.powder.models;

public enum Message {

	/*/
	 * Format:
	 * (COMMAND)_(DESCRIPTION)_(IS A HOVER MESSAGE OR CLICKED COMMAND)
	 * Placeholders are listed for each message; usable in locale
	 */

	PREFIX("{command}"),
	PREFIX_HOVER("{command}"),
	PREFIX_CLICK("{command}"),
	GENERAL_NO_PERMISSION("{user}"),
	LOADING_ALERT("{user}"),
	LOADING_ALREADY("{user}"),
	LOADING_START("{user}"),
	LOADING_FINISH("{user}", "{total}", "{list}"),
	LOADING_FINISH_HOVER("{user}", "{total}", "{list}"),
	STAR_USE_CANCEL("{user}"),
	STAR_NO_ACTIVE("{user}"),
	STAR_CANCEL_SUCCESS("{user}", "{total}"),
	CANCEL_SPECIFY("{user}"),
	CANCEL_NO_ACTIVE("{user}", "{powder}"),
	CANCEL_UNKNOWN_SPECIFY("{user}", "{powder}"),
	CANCEL_SUCCESS("{user}", "{powder}"),
	CANCEL_FAILURE("{user}", "{powder}"),
	ACTIVE_PREFIX("{user}"),
	ACTIVE_POWDER("{user}", "{command}", "{powder}"),
	ACTIVE_POWDER_HOVER("{user}", "{command}", "{powder}"),
	ACTIVE_POWDER_CLICK("{user}", "{command}", "{powder}"),
	CATEGORIES_NOT_ENABLED("{user}"),
	CATEGORY_SYNTAX("{user}", "{command}"),
	CATEGORY_UNKNOWN("{user}", "{category}"),
	SEARCH_SYNTAX("{user}", "{command}"),
	ATTACH_SYNTAX("{user}", "{command}"),
	ATTACH_UNKNOWN("{user}", "{powder}"),
	ATTACH_NO_ENTITY("{user}"),
	ATTACH_SUCCESS_PLAYER("{user}", "{powder}", "{player}"),
	ATTACH_SUCCESS_ENTITY("{user}", "{powder}", "{entity}"),
	CREATE_SYNTAX("{user}", "{command}"),
	CREATE_ALREADY_EXISTS("{user}", "{name}", "{powder}"),
	CREATE_UNKNOWN("{user}", "{powder}"),
	CREATE_SUCCESS("{user}", "{name}", "{powder}"),
	ADDTO_SYNTAX("{user}", "{command}"),
	ADDTO_DOES_NOT_EXIST("{user}", "{name}"),
	ADDTO_UNKNOWN("{user}", "{powder}"),
	ADDTO_SUCCESS("{user}", "{powder}", "{name}"),
	ADDTO_FAILURE("{user}", "{powder}", "{name}"),
	REMOVEFROM_SYNTAX("{user}", "{command}"),
	REMOVEFROM_DOES_NOT_EXIST("{user}", "{name}"),
	REMOVEFROM_UNKNOWN("{user}", "{powder}"),
	REMOVEFROM_SUCCESS("{user}", "{powder}", "{name}"),
	REMOVEFROM_FAILURE("{user}", "{powder}", "{name}"),
	REMOVE_SYNTAX("{user}", "{command}"),
	REMOVE_INVALID_PLAYER("{user}"),
	REMOVE_USER_REMOVE_SUCCESS("{user}", "{player}"),
	REMOVE_USER_REMOVE_FAILURE("{user}", "{player}"),
	REMOVE_USER_REMOVED_BY("{user}", "{player}"),
	REMOVE_NO_USER_SYNTAX("{user}", "{command}"),
	REMOVE_NO_USER_DOES_NOT_EXIST("{user}", "{name}"),
	REMOVE_NO_USER_SUCCESS("{user}", "{name}"),
	REMOVE_NO_USER_FAILURE("{user}", "{name}"),
	NEARBY_PREFIX(),
	NEARBY("{name}", "{distance}"),
	NEARBY_HOVER("{name}", "{powders}"),
	NEARBY_CLICK("{command}", "{name}"),
	POWDER_NO_PERMISSION("{powder}"),
	POWDER_CANCEL_SUCCESS("{user}", "{powder}", "{total}"),
	POWDER_CANCEL_FAILURE("{user}", "{powder}"),
	POWDER_CANCEL_SINGLE_SUCCESS("{user}", "{powder}"),
	POWDER_CANCEL_SINGLE_FAILURE("{user}", "{powder}"),
	POWDER_MAX_PREFIX("{user}", "{powder}", "{total}"),
	POWDER_WAIT("{user}", "{powder}", "{seconds}"),
	POWDER_CREATED("{user}", "{command}", "{powder}"),
	POWDER_CREATED_HOVER("{user}", "{command}", "{powder}"),
	POWDER_CREATED_CLICK("{user}", "{command}", "{powder}"),
	POWDER_CREATED_TIP("{user}", "{command}"),
	POWDER_CREATED_WITHOUT_HOVER("{user}", "{command}", "{powder}"),
	HELP_PREFIX(),
	HELP_POWDER(),
	HELP_POWDER_CANCEL(),
	HELP_POWDER_STAR_CANCEL(),
	HELP_ACTIVE(),
	HELP_LIST(),
	HELP_CATEGORIES(),
	HELP_CATEGORY(),
	HELP_SEARCH(),
	HELP_RELOAD(),
	HELP_NEARBY(),
	HELP_CREATE(),
	HELP_REMOVE(),
	HELP_ATTACH(),
	HELP_ADDTO(),
	HELP_REMOVEFROM(),
	HELP_CANCEL(),
	HELP_EXTRA(),
	HELP_TIP("{command}"),
	HELP_TIP_HOVER("{command}"),
	HELP_TIP_CLICK("{command}"),
	LIST_GENERAL_ITEM("{item}"),
	LIST_GENERAL_LEFT("{command}", "{input}", "{page}"),
	LIST_GENERAL_LEFT_HOVER("{command}", "{input}", "{page}"),
	LIST_GENERAL_LEFT_CLICK("{command}", "{input}", "{page}"),
	LIST_GENERAL_MIDDLE(),
	LIST_GENERAL_RIGHT("{command}", "{input}", "{page}"),
	LIST_GENERAL_RIGHT_HOVER("{command}", "{input}", "{page}"),
	LIST_GENERAL_RIGHT_CLICK("{command}", "{input}", "{page}"),
	LIST_NO_ELEMENTS(),
	LIST_UNKNOWN_POWDER("{user}", "{powder}"),
	LIST_POWDER_NO_PERM("{powder}"),
	LIST_POWDER_NO_PERM_HOVER("{powder}"),
	LIST_POWDER_ACTIVE("{command}", "{powder}"),
	LIST_POWDER_ACTIVE_HOVER("{command}", "{powder}"),
	LIST_POWDER_ACTIVE_CLICK("{command}", "{powder}"),
	LIST_POWDER_REGULAR("{command}", "{powder}"),
	LIST_POWDER_REGULAR_HOVER("{command}", "{powder}"),
	LIST_POWDER_REGULAR_CLICK("{command}", "{powder}"),
	LIST_CATEGORY_NO_PERM("{command}", "{category}", "{desc}"),
	LIST_CATEGORY_NO_PERM_HOVER_EMPTY("{command}", "{category}", "{desc}"),
	LIST_CATEGORY_NO_PERM_HOVER_PERM("{command}", "{category}", "{desc}"),
	LIST_CATEGORY_NO_PERM_CLICK("{command}", "{category}", "{desc}"),
	LIST_CATEGORY_ACTIVE("{command}", "{category}", "{desc}"),
	LIST_CATEGORY_ACTIVE_HOVER("{command}", "{category}", "{desc}"),
	LIST_CATEGORY_ACTIVE_CLICK("{command}", "{category}", "{desc}"),
	LIST_CATEGORY_REGULAR("{command}", "{category}", "{desc}"),
	LIST_CATEGORY_REGULAR_HOVER("{command}", "{category}", "{desc}"),
	LIST_CATEGORY_REGULAR_CLICK("{command}", "{category}", "{desc}");

	private String[] placeholders;

	public String[] getPlaceholders() {
		return placeholders;
	}

	private Message(String... placeholders) {
		this.placeholders = placeholders;
	}

}
