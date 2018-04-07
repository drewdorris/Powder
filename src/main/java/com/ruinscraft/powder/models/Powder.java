package com.ruinscraft.powder.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.PowdersCreationTask;
import com.ruinscraft.powder.util.PowderUtil;

public class Powder {

	// name of the Powder
	private String name;
	// list of categories the Powder is in
	private List<String> categories;
	// the default spacing for the Powder
	private float spacing;
	// the list of particle matrices for the Powder
	private List<ParticleMatrix> matrices;
	// list of SoundEffects for the Powder
	private List<SoundEffect> soundEffects;
	// list of Dusts for the Powder
	private List<Dust> dusts;
	// list of changed ParticleNames for Dusts/ParticleMatrices
	private List<PowderParticle> powderParticles;
	// is the Powder hidden from lists if you don't have permission for it?
	private boolean hidden;
	// if unspecified in each ParticleMatrix, how far left should the player/location be from the start of creating the Powder?
	private int defaultLeft;
	// same, except how far up
	private int defaultUp;

	// initialize lists
	public Powder() {
		this.categories = new ArrayList<String>();
		this.matrices = new ArrayList<ParticleMatrix>();
		this.soundEffects = new ArrayList<SoundEffect>();
		this.dusts = new ArrayList<Dust>();
		this.powderParticles = new ArrayList<PowderParticle>();
	}

	public Powder(String name, List<String> categories, float spacing, List<ParticleMatrix> matrices, 
			List<SoundEffect> soundEffects, List<Dust> dusts, List<PowderParticle> powderParticles, 
			boolean hidden, int defaultLeft, int defaultUp) {

		this.name = name;
		this.categories = categories;
		this.spacing = spacing;
		this.matrices = matrices;
		this.soundEffects = soundEffects;
		this.dusts = dusts;
		this.powderParticles = powderParticles;
		this.hidden = hidden;
		this.defaultLeft = defaultLeft;
		this.defaultUp = defaultUp;

	}

	public Powder(String name, List<String> categories, float spacing, List<ParticleMatrix> matrices, 
			boolean hidden, int defaultLeft, int defaultUp) {

		this.name = name;
		this.categories = categories;
		this.spacing = spacing;
		this.matrices = matrices;
		this.soundEffects = new ArrayList<SoundEffect>();
		this.dusts = new ArrayList<Dust>();
		this.powderParticles = new ArrayList<PowderParticle>();
		this.hidden = hidden;
		this.defaultLeft = defaultLeft;
		this.defaultUp = defaultUp;

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

	public float getDefaultSpacing() {
		return spacing;
	}

	public void setDefaultSpacing(float spacing) {
		this.spacing = spacing;
	}

	public List<ParticleMatrix> getMatrices() {
		return matrices;
	}

	public void setMatrices(List<ParticleMatrix> particleMatrices) {
		this.matrices = particleMatrices;
	}

	public void addMatrix(ParticleMatrix particleMatrix) {
		matrices.add(particleMatrix);
	}

	public List<SoundEffect> getSoundEffects() {
		return soundEffects;
	}

	public void setSoundEffects(List<SoundEffect> soundEffects) {
		this.soundEffects = soundEffects;
	}

	public void addSoundEffect(SoundEffect soundEffect) {
		soundEffects.add(soundEffect);
	}

	public List<Dust> getDusts() {
		return dusts;
	}

	public void setDusts(List<Dust> dusts) {
		this.dusts = dusts;
	}

	public void addDust(Dust dust) {
		dusts.add(dust);
	}
	
	public List<PowderElement> getOriginalPowderElements() {
		List<PowderElement> powderElements = new ArrayList<PowderElement>();
		powderElements.addAll(getDusts());
		powderElements.addAll(getSoundEffects());
		powderElements.addAll(getMatrices());
		return powderElements;
	}
	
	public List<PowderElement> getNewPowderElements() {
		List<PowderElement> powderElements = new ArrayList<PowderElement>();
		for (Dust dust : getDusts()) {
			Dust newDust = new Dust(dust);
			powderElements.add(newDust);
		}
		for (SoundEffect soundEffect : getSoundEffects()) {
			SoundEffect newSoundEffect = new SoundEffect(soundEffect);
			powderElements.add(newSoundEffect);
		}
		for (ParticleMatrix particleMatrix : getMatrices()) {
			ParticleMatrix newParticleMatrix = new ParticleMatrix(particleMatrix);
			powderElements.add(newParticleMatrix);
		}
		return powderElements;
	}

	public List<PowderParticle> getPowderParticles() {
		return powderParticles;
	}

	// get the PowderParticle assigned with the given enumName
	public PowderParticle getPowderParticle(String enumName) {
		for (PowderParticle powderParticle : powderParticles) {
			if (powderParticle.getEnumName() == null) {
				continue;
			}
			if (powderParticle.getEnumName().equals(enumName)) {
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

	public Integer getDefaultLeft() {
		return defaultLeft;
	}

	public void setDefaultLeft(int defaultLeft) {
		this.defaultLeft = defaultLeft;
	}

	public Integer getDefaultUp() {
		return defaultUp;
	}

	public void setDefaultUp(int defaultUp) {
		this.defaultUp = defaultUp;
	}
	
	public boolean hasMovement() {
		for (PowderElement element : getOriginalPowderElements()) {
			if (!element.getStartTime().equals(0) || element.getLockedIterations() > 1) {
				return true;
			}
		}
		return false;
	}

	public void spawn(final Location location) {
		PowderTask powderTask = new PowderTask(location, this);
		spawn(powderTask);
	}

	// spawns a given Powder for the given user
	public void spawn(final Player player) {
		PowderTask powderTask = new PowderTask(player.getUniqueId(), this);
		spawn(powderTask);
		PowderUtil.savePowdersForPlayer(player.getUniqueId());
	}

	public void spawn(PowderTask powderTask) {
		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();
		// create a PowderTask, add taskIDs to it
		powderTask.addElements(getNewPowderElements());
		if (powderHandler.getPowderTasks().isEmpty()) {
			powderHandler.addPowderTask(powderTask);
			new PowdersCreationTask().runTaskTimer(PowderPlugin.getInstance(), 0L, 1L);
		} else {
			powderHandler.addPowderTask(powderTask);
		}
	}

	// cancels a given Powder for the given player
	public boolean cancelPowder(UUID uuid) {
		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();

		boolean success = false;

		for (PowderTask powderTask : powderHandler.getPowderTasks(uuid, this)) {
			powderHandler.removePowderTask(powderTask);
			success = true;
		}

		if (success && PowderPlugin.getInstance().useStorage()) {
			PowderUtil.savePowdersForPlayer(uuid);
		}

		return success;
	}

}
