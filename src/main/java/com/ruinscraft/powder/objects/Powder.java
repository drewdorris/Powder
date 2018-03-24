package com.ruinscraft.powder.objects;

import java.util.ArrayList;
import java.util.List;

public class Powder {

	private String name;
	private float spacing;
	private List<ParticleMatrix> matrices;
	private List<SoundEffect> soundEffects;
	private List<Dust> dusts;
	private List<PowderParticle> powderParticles;
	private boolean pitch;
	private boolean repeating;
	private boolean hidden;
	private long delay;
	
	public Powder() {
		// empty
	}

	public Powder(String name, float spacing, List<ParticleMatrix> matrices, List<SoundEffect> soundEffects, List<Dust> dusts,
			List<PowderParticle> powderParticles, boolean pitch, boolean repeating, boolean hidden, long delay) {

		this.name = name;
		this.spacing = spacing;
		this.matrices = matrices;
		this.soundEffects = soundEffects;
		this.dusts = dusts;
		this.powderParticles = powderParticles;
		this.pitch = pitch;
		this.repeating = repeating;
		this.hidden = hidden;
		this.delay = delay;

	}

	public Powder(String name, float spacing, List<ParticleMatrix> matrices, 
			boolean pitch, boolean repeating, boolean hidden, long delay) {

		this.name = name;
		this.spacing = spacing;
		this.matrices = matrices;
		this.soundEffects = new ArrayList<SoundEffect>();
		this.dusts = new ArrayList<Dust>();
		this.powderParticles = new ArrayList<PowderParticle>();
		this.pitch = pitch;
		this.repeating = repeating;
		this.hidden = hidden;
		this.delay = delay;

	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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

}
