package com.ruinscraft.powder.models;

import org.bukkit.Location;

import com.ruinscraft.powder.PowdersCreationTask;

public class Dust implements PowderElement {

	// 'A;2;1;3;3;0'
	// 'PowderParticle;radius;height&depth;startTime;repeatTime;iterations'

	// the PowderParticle associated with this Dust
	private PowderParticle powderParticle;
	// the limit radius that this Dust can be spawned in in
	private double radius;
	// the mean height of where the Dust should be spawned
	private double height;
	// the distance in y value up/down from getHeight() where 
	private double span;
	// when to start displaying this Dust
	private int startTime;
	// after how many ticks should it repeat?
	private int repeatTime;
	// set maximum iterations (0 if infinite)
	private int lockedIterations;
	private int nextTick;

	// iterations so far
	private int iterations;

	public Dust(Dust dust) {
		this.powderParticle = dust.getPowderParticle();
		this.radius = dust.getRadius();
		this.height = dust.getHeight();
		this.span = dust.getYSpan();
		this.startTime = dust.getStartTime();
		this.repeatTime = dust.getRepeatTime();
		this.lockedIterations = dust.getLockedIterations();
		this.nextTick = PowdersCreationTask.getTick() + startTime;
		this.iterations = 0;
	}

	public Dust(PowderParticle powderParticle, double radius, double height, 
			double span, int startTime, int repeatTime, int lockedIterations) {
		this.powderParticle = powderParticle;
		this.radius = radius;
		this.height = height;
		this.span = span;
		this.startTime = startTime;
		this.repeatTime = repeatTime;
		this.lockedIterations = lockedIterations;
		this.iterations = 0;
	}

	public PowderParticle getPowderParticle() {
		return powderParticle;
	}

	public double getRadius() {
		return radius;
	}

	public double getHeight() {
		return height;
	}

	public double getYSpan() {
		return span;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getRepeatTime() {
		return repeatTime;
	}

	public int getLockedIterations() {
		return lockedIterations;
	}

	public void setLockedIterations(int lockedIterations) {
		this.lockedIterations = lockedIterations;
	}

	public int getIterations() {
		return iterations;
	}

	public void iterate() {
		iterations++;
		this.nextTick = PowdersCreationTask.getTick() + getRepeatTime();
	}

	public int getNextTick() {
		return nextTick;
	}

	public Dust clone() {
		return new Dust(this);
	}

	// creates this Dust at the designated location
	public void create(Location location) {
		PowderParticle powderParticle = getPowderParticle();
		location.getWorld().spawnParticle(
				powderParticle.getParticle(), location.clone().add(
						(Math.random() - .5) * (2 * getRadius()), 
						((Math.random() - .5) * getYSpan()) + getHeight() - .625,
						(Math.random() - .5) * (2 * getRadius())), 
				powderParticle.getAmount(), 
				powderParticle.getXOff() / 255, powderParticle.getYOff() / 255,
				powderParticle.getZOff() / 255, (double) powderParticle.getData());
	}

}
