package com.ruinscraft.powder.model;

import com.ruinscraft.powder.PowdersCreationTask;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

public class SoundEffect implements PowderElement {

	// Sound enum associated with this SoundEffect
	private Sound sound;
	// volume for this SoundEffect
	private double volume;
	// pitch for this SoundEffect (noteblock values)
	private double pitch;
	// is there surround sound?
	// surroundSound fixes how sound tends to sound louder different directions
	private boolean surroundSound;
	// when to start displaying this SoundEffect
	private int startTime;
	// after how many ticks should it repeat?
	private int repeatTime;
	// set maximum iterations (0 if infinite)
	private int lockedIterations;
	// the next tick to iterate
	private int nextTick;

	// iterations so far
	private int iterations;

	public SoundEffect(SoundEffect soundEffect) {
		this.sound = soundEffect.getSound();
		this.volume = soundEffect.getVolume();
		this.pitch = soundEffect.getPitch();
		this.surroundSound = soundEffect.surroundSound();
		this.startTime = soundEffect.getStartTime();
		this.repeatTime = soundEffect.getRepeatTime();
		this.lockedIterations = soundEffect.getLockedIterations();
		this.iterations = 0;
	}

	public SoundEffect(Sound sound, double volume, double pitch, boolean surroundSound,
			int startTime, int repeatTime, int lockedIterations) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.surroundSound = surroundSound;
		this.startTime = startTime;
		this.repeatTime = repeatTime;
		this.lockedIterations = lockedIterations;
		this.iterations = 0;
	}

	public Sound getSound() {
		return sound;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public double getPitch() {
		return pitch;
	}

	public boolean surroundSound() {
		return surroundSound;
	}

	@Override
	public int getStartTime() {
		return startTime;
	}

	@Override
	public void setStartTime(int startTime) {
		this.startTime = startTime;
		this.nextTick = startTime;
	}

	@Override
	public int getRepeatTime() {
		return repeatTime;
	}

	@Override
	public void setRepeatTime(int repeatTime) {
		this.repeatTime = repeatTime;
	}

	@Override
	public int getLockedIterations() {
		return lockedIterations;
	}

	@Override
	public void setLockedIterations(int lockedIterations) {
		this.lockedIterations = lockedIterations;
	}

	@Override
	public int getIterations() {
		return iterations;
	}

	@Override
	public void iterate() {
		iterations++;
		this.nextTick = PowdersCreationTask.getCurrentTick() + getRepeatTime();
	}

	@Override
	public int getNextTick() {
		return nextTick;
	}

	@Override
	public void setStartingTick() {
		this.nextTick = PowdersCreationTask.getCurrentTick() + getStartTime();
	}

	@Override
	public SoundEffect clone() {
		return new SoundEffect(this);
	}

	// creates this SoundEffect at the designated location
	@Override
	public void create(Location location) {
		World world = location.getWorld();
		if (surroundSound) {
			world.playSound(
					location.add(Math.random() - .5, Math.random() - .5, Math.random() - .5),
					sound, (float) volume, (float) pitch);
		} else {
			world.playSound(location, sound, (float) volume, (float) pitch);
		}
		/*/
		 * other method for surroundSound, sounds a little blocky
		 * location.getWorld().playSound(
					location.add(.5, 0, 0), 
					sound, (float) volume / 8, (float) pitch);
			location.getWorld().playSound(
					location.add(-.5, 0, 0), 
					sound, (float) volume / 8, (float) pitch);
			location.getWorld().playSound(
					location.add(0, 0, .5), 
					sound, (float) volume / 8, (float) pitch);
			location.getWorld().playSound(
					location.add(0, 0, -.5), 
					sound, (float) volume / 8, (float) pitch);
		 */
	}

}
