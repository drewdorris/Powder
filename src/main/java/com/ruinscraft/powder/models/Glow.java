package com.ruinscraft.powder.models;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffectType;

import com.ruinscraft.powder.PowdersCreationTask;

public class Glow implements PowderElement {

	// the type (default GLOWING)
	private PotionEffectType type;
	// time (in ticks) that this will occur
	private int duration;
	// amplifies the potioneffect (doesnt matter much)
	private int amplifier;
	// are the particles translucent? (doesnt matter much)
	private boolean ambient;
	// whether to show particles (default false)
	private boolean particles;
	// initial color to show
	private Color firstColor;
	// color to gradientize to
	private Color secondColor;
	// when to start displaying this Glow
	private int startTime;
	// after how many ticks should it repeat?
	private int repeatTime;
	// set maximum iterations (0 if infinite)
	private int lockedIterations;
	// the next tick to iterate
	private int nextTick;

	// iterations so far
	private int iterations;

	public Glow(Glow glow) {
		this.type = glow.getType();
		this.duration = glow.getDuration();
		this.amplifier = glow.getAmplification();
		this.ambient = glow.getAmbiency();
		this.particles = glow.getIfParticles();
		this.firstColor = glow.getFirstColor();
		this.secondColor = glow.getSecondColor();
		this.startTime = glow.getStartTime();
		this.repeatTime = glow.getRepeatTime();
		this.lockedIterations = glow.getLockedIterations();
		this.nextTick = PowdersCreationTask.getCurrentTick() + startTime;
		this.iterations = 0;
	}

	public Glow(PotionEffectType type, int duration, int amplifier, boolean ambient, 
			boolean particles, Color firstColor, Color secondColor, int startTime, 
			int repeatTime, int lockedIterations) {
		this.type = type;
		this.duration = duration;
		this.amplifier = amplifier;
		this.ambient = ambient;
		this.particles = particles;
		this.firstColor = firstColor;
		this.secondColor = secondColor;
		this.startTime = startTime;
		this.repeatTime = repeatTime;
		this.lockedIterations = lockedIterations;
		this.iterations = 0;
	}

	public PotionEffectType getType() {
		return type;
	}

	public int getDuration() {
		return duration;
	}

	public int getAmplification() {
		return amplifier;
	}

	public boolean getAmbiency() {
		return ambient;
	}

	public boolean getIfParticles() {
		return particles;
	}

	public Color getFirstColor() {
		return firstColor;
	}

	public Color getSecondColor() {
		return secondColor;
	}

	@Override
	public int getStartTime() {
		return startTime;
	}

	@Override
	public int getRepeatTime() {
		return repeatTime;
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
	public PowderElement clone() {
		return new Glow(this);
	}

	@Override
	public void create(Location location) {
		// TODO
	}

}
