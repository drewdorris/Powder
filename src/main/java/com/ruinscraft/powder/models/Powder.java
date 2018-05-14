package com.ruinscraft.powder.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.PowdersCreationTask;
import com.ruinscraft.powder.models.trackers.EntityTracker;
import com.ruinscraft.powder.models.trackers.PlayerTracker;
import com.ruinscraft.powder.models.trackers.StationaryTracker;
import com.ruinscraft.powder.util.PowderUtil;

public class Powder implements Cloneable {

	// name of the Powder
	private String name;
	// list of categories the Powder is in
	private List<String> categories;
	// the spacing for ParticleMatrices in the Powder if not specified
	private double defaultSpacing;
	// the start time for PowderElements in the Powder if not specified
	private int defaultStartTime;
	// the repeat time for PowderElements in the Powder if not specified
	private int defaultRepeatTime;
	// the maximum iterations for PowderElements in the Powder if not specified
	private int defaultLockedIterations;
	// the added pitch for ParticleMatrices in the Powder if not specified
	private double defaultAddedPitch;
	// the added rotation for ParticleMatrices in the Powder if not specified
	private double defaultAddedRotation;
	// the added tilt for ParticleMatrices in the Powder if not specified
	private double defaultAddedTilt;
	// list of PowderElements (Dusts, SoundEffects, ParticleMatrices)
	private List<PowderElement> powderElements;
	// list of PowderElements used while iterating
	private List<PowderElement> duePowderElements;
	// list of changed ParticleNames for Dusts/ParticleMatrices
	private List<PowderParticle> powderParticles;
	// is the Powder hidden from lists if you don't have permission for it?
	private boolean hidden;
	// if unspecified in each ParticleMatrix, 
	// how far left should the player/location be from the start of creating the Powder?
	private int defaultLeft;
	// same, except how far up
	private int defaultUp;

	// initialize lists
	public Powder() {
		this.categories = new ArrayList<String>();
		this.powderElements = new ArrayList<PowderElement>();
		this.powderParticles = new ArrayList<PowderParticle>();
	}

	public Powder(Powder powder) {
		name = powder.getName();
		categories = powder.getCategories();
		defaultLeft = powder.getDefaultLeft();
		defaultUp = powder.getDefaultUp();
		defaultSpacing = powder.getDefaultSpacing();
		defaultStartTime = powder.getDefaultStartTime();
		defaultRepeatTime = powder.getDefaultRepeatTime();
		defaultAddedPitch = powder.getDefaultAddedPitch();
		defaultAddedRotation = powder.getDefaultAddedRotation();
		defaultAddedTilt = powder.getDefaultAddedTilt();
		defaultLockedIterations = powder.getDefaultLockedIterations();
		powderElements = powder.getClonedPowderElements();
		powderParticles = powder.getPowderParticles();
		hidden = powder.isHidden();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void addCategory(String category) {
		categories.add(category);
	}

	public double getDefaultSpacing() {
		return defaultSpacing;
	}

	public void setDefaultSpacing(double defaultSpacing) {
		this.defaultSpacing = defaultSpacing;
	}

	public int getDefaultStartTime() {
		return defaultStartTime;
	}

	public void setDefaultStartTime(int defaultStartTime) {
		this.defaultStartTime = defaultStartTime;
	}

	public int getDefaultRepeatTime() {
		return defaultRepeatTime;
	}

	public void setDefaultRepeatTime(int defaultRepeatTime) {
		this.defaultRepeatTime = defaultRepeatTime;
	}

	public int getDefaultLockedIterations() {
		return defaultLockedIterations;
	}

	public void setDefaultLockedIterations(int defaultLockedIterations) {
		this.defaultLockedIterations = defaultLockedIterations;
	}

	public double getDefaultAddedPitch() {
		return defaultAddedPitch;
	}

	public void setDefaultAddedPitch(double defaultAddedPitch) {
		this.defaultAddedPitch = defaultAddedPitch;
	}

	public double getDefaultAddedRotation() {
		return defaultAddedRotation;
	}

	public void setDefaultAddedRotation(double defaultAddedRotation) {
		this.defaultAddedRotation = defaultAddedRotation;
	}

	public double getDefaultAddedTilt() {
		return defaultAddedTilt;
	}

	public void setDefaultAddedTilt(double defaultAddedTilt) {
		this.defaultAddedTilt = defaultAddedTilt;
	}

	public List<PowderElement> getPowderElements() {
		return powderElements;
	}

	public List<PowderElement> getDuePowderElements(int tick) {
		if (duePowderElements == null || (tick % 100) == 0) {
			updateDuePowderElements(tick);
		}
		return duePowderElements;
	}

	public void updateDuePowderElements(int tick) {
		duePowderElements = new ArrayList<PowderElement>();
		for (PowderElement powderElement : powderElements) {
			if (powderElement.getNextTick() - 100 <= tick) {
				duePowderElements.add(powderElement);
			}
		}
	}

	public List<PowderElement> getClonedPowderElements() {
		List<PowderElement> powderElements = new ArrayList<PowderElement>();
		for (PowderElement powderElement : this.powderElements) {
			PowderElement newPowderElement = powderElement.clone();
			powderElements.add(newPowderElement);
		}
		return powderElements;
	}

	public void addPowderElement(PowderElement powderElement) {
		if (powderElement.getLockedIterations() == 0) {
			powderElement.setLockedIterations(Integer.MAX_VALUE);
		}
		powderElements.add(powderElement);
	}

	public void addPowderElements(List<PowderElement> powderElements) {
		for (PowderElement element : powderElements) {
			addPowderElement(element);
		}
	}

	public void removePowderElement(PowderElement powderElement) {
		powderElements.remove(powderElement);
	}

	public List<PowderParticle> getPowderParticles() {
		return powderParticles;
	}

	// get the PowderParticle assigned with the given enumName
	public PowderParticle getPowderParticle(char enumName) {
		for (PowderParticle powderParticle : powderParticles) {
			if (powderParticle.getCharacter() == enumName) {
				return powderParticle;
			}
		}
		return null;
	}

	public void setPowderParticles(List<PowderParticle> powderParticles) {
		this.powderParticles = powderParticles;
	}

	public void addPowderParticle(PowderParticle powderParticle) {
		powderParticles.add(powderParticle);
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public int getDefaultLeft() {
		return defaultLeft;
	}

	public void setDefaultLeft(int defaultLeft) {
		this.defaultLeft = defaultLeft;
	}

	public int getDefaultUp() {
		return defaultUp;
	}

	public void setDefaultUp(int defaultUp) {
		this.defaultUp = defaultUp;
	}

	public boolean hasMovement() {
		for (PowderElement element : getPowderElements()) {
			if (!(element.getStartTime() == 0) || element.getLockedIterations() > 1) {
				return true;
			}
		}
		return false;
	}

	public void spawn(Entity entity) {
		PowderTask powderTask = new PowderTask(PowderUtil.cleanEntityName(entity) + "--" +
				PowderUtil.generateID(8), this, new EntityTracker(entity));
		spawn(powderTask);
		PowderUtil.savePowdersForUUID(entity.getUniqueId());
	}

	public void spawn(String name, Location location) {
		PowderTask powderTask = new PowderTask(name, this, new StationaryTracker(location));
		spawn(powderTask);
	}

	// spawns a given Powder for the given user
	public void spawn(Player player) {
		PowderTask powderTask = new PowderTask(player.getName() + "--" + PowderUtil.generateID(6), 
				this, new PlayerTracker(player.getUniqueId()));
		spawn(powderTask);
		PowderUtil.savePowdersForUUID(player.getUniqueId());
	}

	// spawns a given Powder for the given user
	public void spawn(String name, Player player) {
		PowderTask powderTask = new PowderTask(name, this, new PlayerTracker(player.getUniqueId()));
		spawn(powderTask);
		PowderUtil.savePowdersForUUID(player.getUniqueId());
	}

	public void spawn(PowderTask powderTask) {
		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();
		// create a PowderTask, add taskIDs to it
		if (powderHandler.getPowderTasks().isEmpty()) {
			powderHandler.addPowderTask(powderTask);
			new PowdersCreationTask().runTaskTimer(PowderPlugin.getInstance(), 0L, 1L);
		} else {
			powderHandler.addPowderTask(powderTask);
		}
	}

	// cancels a given Powder for the given player
	public boolean cancel(UUID uuid) {
		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();

		boolean success = false;

		for (PowderTask powderTask : powderHandler.getPowderTasks(uuid, this)) {
			powderTask.removePowder(this);
			success = true;
		}

		if (success) {
			PowderUtil.savePowdersForUUID(uuid);
		}

		return success;
	}

	@Override
	public Powder clone() {
		return new Powder(this);
	}

}
