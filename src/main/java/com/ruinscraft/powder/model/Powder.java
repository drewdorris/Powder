package com.ruinscraft.powder.model;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.model.particle.PositionedPowderParticle;
import com.ruinscraft.powder.model.particle.PowderParticle;
import com.ruinscraft.powder.model.tracker.EntityTracker;
import com.ruinscraft.powder.model.tracker.StationaryTracker;
import com.ruinscraft.powder.util.PowderUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Powder implements Cloneable {

	// name of the Powder
	private String name;
	// path to the Powder in configuration
	private String path;
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
	public List<PowderElement> powderElements;
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
		this.categories = new ArrayList<>();
		this.powderElements = new ArrayList<>();
		this.powderParticles = new ArrayList<>();
	}

	public Powder(String path) {
		this.categories = new ArrayList<>();
		this.powderElements = new ArrayList<>();
		this.powderParticles = new ArrayList<>();
		this.path = path;
	}

	public Powder(Powder powder) {
		name = powder.getName();
		path = powder.getPath();
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

	public String getPath() {
		return path;
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

	public boolean hasPowderElements() {
		return powderElements.size() != 0;
	}

	public List<PowderElement> getPowderElements() {
		return powderElements;
	}

	public List<PowderElement> getClonedPowderElements() {
		List<PowderElement> powderElements = new ArrayList<>();
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
		this.powderElements.add(powderElement);
	}

	public void addPowderElements(Collection<? extends PowderElement> powderElements) {
		for (PowderElement powderElement : powderElements) {
			addPowderElement(powderElement);
		}
	}

	public void removePowderElement(PowderElement powderElement) {
		boolean worked = this.powderElements.remove(powderElement);
		if (!worked) {
			Bukkit.getLogger().info("uhhh");
			PowderElement elementToRemove = null;
			for (PowderElement otherElement : this.powderElements) {
				if (otherElement.equals(powderElement)) {
					elementToRemove = otherElement;
				}
			}
			this.powderElements.remove(elementToRemove);
		}
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

	public void spawn(Entity entity, UUID creator) {
		Bukkit.getScheduler().runTaskAsynchronously(PowderPlugin.get(), () -> {
			PowderTask powderTask = new PowderTask(PowderUtil.cleanEntityName(entity) + "--" +
					PowderUtil.generateID(8), this.clone(), new EntityTracker(entity, creator));
			spawn(powderTask);
		});
	}

	public void spawn(Entity entity, Player creator) {
		Bukkit.getScheduler().runTaskAsynchronously(PowderPlugin.get(), () -> {
			PowderTask powderTask = new PowderTask(PowderUtil.cleanEntityName(entity) + "--" +
					PowderUtil.generateID(8), this.clone(), new EntityTracker(
							entity, creator, Bukkit.getPlayer(entity.getUniqueId()) != null,
							entity instanceof LivingEntity));
			spawn(powderTask);
		});
	}

	public void spawn(String name, Location location, UUID owner) {
		PowderTask powderTask = new PowderTask(name, this.clone(), new StationaryTracker(location, owner));
		spawn(powderTask);
	}

	// spawns a given Powder for the given user
	public void spawn(Player player) {
		Bukkit.getScheduler().runTaskAsynchronously(PowderPlugin.get(), () -> {
			PowderTask powderTask = new PowderTask(player.getName() + "--" + PowderUtil.generateID(6),
					this.clone(), new EntityTracker(player.getUniqueId(), player.getUniqueId(), true, true));
			spawn(powderTask);
		});
	}

	public void spawn(PowderTask powderTask) {
		// create a PowderTask, add taskIDs to it
		PowderPlugin.get().getPowderHandler().runPowderTask(powderTask);
	}

	// cancels a given Powder for the given player
	public boolean cancel(UUID uuid) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();
		return powderHandler.cancelPowderTasks(powderHandler.getPowderTasks(uuid, this));
	}

	public Powder loop() {
		Powder powder = this.clone();

		int greatestStartTime = 0;
		for (PowderElement element : powder.getPowderElements()) {
			element.setLockedIterations(0);
			if (element.getStartTime() > greatestStartTime) {
				greatestStartTime = element.getStartTime() + 1;
			}
		}

		if (greatestStartTime <= 1) greatestStartTime = 20;

		for (PowderElement element : powder.getPowderElements()) {
			if (element.getRepeatTime() == 0) {
				element.setRepeatTime(greatestStartTime);
			}
		}

		return powder;
	}

	// this makes the powder play for a couple secs and then the particles/etc fade out
	// limit of 8 seconds; 20 * 8 = 160
	public Powder arrowFadeout(int timeBeforeFadeout) {
		Powder powder = this.clone();

		List<PowderElement> newElements = new ArrayList<>();
		for (int i = 0; i < powder.getPowderElements().size(); i++) {
			PowderElement element = powder.getPowderElements().get(i).clone();
			// no more elements past 8 seconds!
			if (element.getStartTime() >= timeBeforeFadeout + 60) {
				continue;
			}

			int repeatTime = element.getRepeatTime();
			if (repeatTime != 0) {
				for (int j = element.getStartTime() + repeatTime; j <= timeBeforeFadeout + 60; j+= repeatTime) {
					PowderElement newElement = element.clone();
					newElement.setStartTime(j);
					newElement.setLockedIterations(1);

					newElement = powder.makeFadeout(newElement, timeBeforeFadeout);
					if (newElement == null) continue;

					newElements.add(newElement);
				}
			}
			element.setLockedIterations(1);
			element = powder.makeFadeout(element, timeBeforeFadeout);
			if (element == null) {
				continue;
			}
			newElements.add(element);
		}
		powder.powderElements.clear();
		powder.powderElements.addAll(newElements);

		return powder;
	}

	// 160 is limit; 8 seconds!
	// last 3 seconds are fadeout; this is how much fadeout left
	private double percentageLeft(int startTime, int timeBeforeFadeout) {
		if (startTime <= timeBeforeFadeout) return 1;
		if (startTime >= timeBeforeFadeout + 60) return 0;
		double formula = .0003 * (startTime - timeBeforeFadeout - 60) * (startTime - timeBeforeFadeout - 160);
		if (formula >= 1) return 1;
		if (formula <= 0) return 0;
		return formula;
	}

	private PowderElement makeFadeout(PowderElement element, int timeBeforeFadeout) {
		double percent = percentageLeft(element.getStartTime(), timeBeforeFadeout);
		if (element instanceof SoundEffect) {
			SoundEffect soundEffect = (SoundEffect) element;
			soundEffect.setVolume(soundEffect.getVolume() * percent);
			return soundEffect;
		} else if (element instanceof ParticleMatrix) {
			// had some stuff that didnt work
			return element;
		} else {
			if (PowderUtil.getRandom().nextDouble() > percent) element = null;
		}
		return element;
	}

	public boolean isLooping() {
		for (PowderElement element : this.getClonedPowderElements()) {
			if (element.getLockedIterations() != 0 && element.getLockedIterations() != Integer.MAX_VALUE) {
				return false;
			}
		}
		return true;
	}

	public double maxWidthDistance() {
		double max = 0;
		for (PowderElement element : this.getPowderElements()) {
			if (element instanceof ParticleMatrix) {
				ParticleMatrix matrix = (ParticleMatrix) element;
				double spacing = matrix.getSpacing();
				for (PositionedPowderParticle particle : matrix.getParticles()) {
					int dist = particle.getX();
					if (max < dist * spacing) max = dist * spacing;
				}
			} else if (element instanceof Dust) {
				Dust dust = (Dust) element;
				if (max < dust.getRadius()) max = dust.getRadius();
			}
		}
		return max;
	}

	@Override
	public Powder clone() {
		return new Powder(this);
	}

}
