package com.ruinscraft.powder.model;

import com.ruinscraft.powder.PowdersCreationTask;
import com.ruinscraft.powder.model.particle.PowderParticle;
import org.bukkit.Location;

public class Dust implements PowderElement {

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
	// the next tick to iterate
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
	public Dust clone() {
		return new Dust(this);
	}

	// creates this Dust at the designated location
	@Override
	public void create(Location location) {
		if (powderParticle.getData() != null && powderParticle.getData() instanceof Double) {
			double extra = (Double) powderParticle.getData();
			location.getWorld().spawnParticle(
					powderParticle.getParticle(), location.add(
							(Math.random() - .5) * (2 * getRadius()),
							((Math.random() - .5) * getYSpan()) + getHeight() - .625,
							(Math.random() - .5) * (2 * getRadius())),
					powderParticle.getAmount(),
					powderParticle.getXOff() / 255, powderParticle.getYOff() / 255,
					powderParticle.getZOff() / 255, extra);
		} else {
			location.getWorld().spawnParticle(
					powderParticle.getParticle(), location.add(
							(Math.random() - .5) * (2 * getRadius()),
							((Math.random() - .5) * getYSpan()) + getHeight() - .625,
							(Math.random() - .5) * (2 * getRadius())),
					powderParticle.getAmount(),
					powderParticle.getXOff() / 255, powderParticle.getYOff() / 255,
					powderParticle.getZOff() / 255, powderParticle.getData());
		}
	}

}
