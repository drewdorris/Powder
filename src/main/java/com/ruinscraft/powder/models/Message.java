package com.ruinscraft.powder.models;

public enum Message {
	
	// COMMAND_ MESSAGE_DESCRIPTION (_HOVER)
	// placeholders are the optional placeholders for locale
	
	PREFIX("{command}"),
	PREFIX_HOVER("{command}"),
	MAIN_NO_PERMISSION("{user}");
	
	private String[] placeholders;
	
	public String[] getPlaceholders() {
		return placeholders;
	}
	
	private Message(String... placeholders) {
		this.placeholders = placeholders;
	}

}
