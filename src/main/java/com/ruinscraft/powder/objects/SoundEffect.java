package com.ruinscraft.powder.objects;

import org.bukkit.Sound;

public class SoundEffect {

	private Sound sound;
	private float volume;
	private float pitch;
	private float wait;

	public SoundEffect(Sound sound, float volume, float pitch, float wait) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.wait = wait;
	}

	public Sound getSound() {
		return sound;
	}

	public float getVolume() {
		return volume;
	}

	public float getPitch() {
		return pitch;
	}

	public float getWaitTime() {
		return wait;
	}

}
