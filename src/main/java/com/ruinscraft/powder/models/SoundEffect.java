package com.ruinscraft.powder.models;

import org.bukkit.Sound;

public class SoundEffect {

	// Sound enum associated with this SoundEffect
	private Sound sound;
	// volume for this SoundEffect
	private float volume;
	// pitch for this SoundEffect (noteblock values)
	private float pitch;
	// waittime in ticks before playing this SoundEffect
	private long wait;

	public SoundEffect(Sound sound, float volume, float pitch, long wait) {
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

	public long getWaitTime() {
		return wait;
	}

}
