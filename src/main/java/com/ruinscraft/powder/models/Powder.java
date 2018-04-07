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
	// is pitch (up/down eye position) accounted for when creating this Powder?
	private boolean pitch;
	// is the Powder repeating?
	private boolean repeating;
	// is the Powder hidden from lists if you don't have permission for it?
	private boolean hidden;
	// if the Powder is repeating, at what tick interval should it repeat?
	private long delay;
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
			boolean pitch, boolean repeating, boolean hidden, long delay, int defaultLeft, int defaultUp) {

		this.name = name;
		this.categories = categories;
		this.spacing = spacing;
		this.matrices = matrices;
		this.soundEffects = soundEffects;
		this.dusts = dusts;
		this.powderParticles = powderParticles;
		this.pitch = pitch;
		this.repeating = repeating;
		this.hidden = hidden;
		this.delay = delay;
		this.defaultLeft = defaultLeft;
		this.defaultUp = defaultUp;

	}

	public Powder(String name, List<String> categories, float spacing, List<ParticleMatrix> matrices, 
			boolean pitch, boolean repeating, boolean hidden, long delay, int defaultLeft, int defaultUp) {

		this.name = name;
		this.categories = categories;
		this.spacing = spacing;
		this.matrices = matrices;
		this.soundEffects = new ArrayList<SoundEffect>();
		this.dusts = new ArrayList<Dust>();
		this.powderParticles = new ArrayList<PowderParticle>();
		this.pitch = pitch;
		this.repeating = repeating;
		this.hidden = hidden;
		this.delay = delay;
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

	public boolean hasPitch() {
		return pitch;
	}

	public void setPitch(boolean pitch) {
		this.pitch = pitch;
	}

	public boolean isRepeating() {
		return repeating;
	}

	public void setRepeating(boolean repeating) {
		this.repeating = repeating;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
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
		List<PowderElement> elements = new ArrayList<PowderElement>();
		elements.addAll(getMatrices());
		elements.addAll(getDusts());
		elements.addAll(getSoundEffects());
		for (PowderElement element : elements) {
			powderTask.addElement(element);
		}
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
