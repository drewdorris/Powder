package com.ruinscraft.powder.models;

import org.bukkit.Location;
import org.bukkit.Sound;

public class SoundEffect implements PowderElement {

	// 'BLOCK_NOTE_PLING;4.0;1.50;2;10;200'
	// 'sound;volume;pitch;startTime;repeatTime;iterations'

	// Sound enum associated with this SoundEffect
	private Sound sound;
	// volume for this SoundEffect
	private float volume;
	// pitch for this SoundEffect (noteblock values)
	private float pitch;
	// when to start displaying this SoundEffect
	private int startTime;
	// after how many ticks should it repeat?
	private int repeatTime;
	// set maximum iterations (0 if infinite)
	private int lockedIterations;

	// iterations so far
	private int iterations;

	public SoundEffect(Sound sound, float volume, float pitch, int startTime, int repeatTime, int lockedIterations) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.startTime = startTime;
		this.repeatTime = repeatTime;
		this.lockedIterations = lockedIterations;
	}

	public Sound getSound() {
		return sound;
	}

	public Float getVolume() {
		return volume;
	}

	public Float getPitch() {
		return pitch;
	}

	public Integer getStartTime() {
		return startTime;
	}

	public Integer getRepeatTime() {
		return repeatTime;
	}

	public Integer getLockedIterations() {
		return lockedIterations;
	}

	public void setLockedIterations(int lockedIterations) {
		this.lockedIterations = lockedIterations;
	}

	public Integer getIterations() {
		return iterations;
	}

	// creates this SoundEffect at the designated location
	public void create(Location location) {
		location.getWorld().playSound(location, getSound(), getVolume(), getPitch());
		iterations++;
	}

}
