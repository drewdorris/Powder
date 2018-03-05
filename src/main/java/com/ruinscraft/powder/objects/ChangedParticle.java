package com.ruinscraft.powder.objects;

import org.bukkit.Particle;

public class ChangedParticle {
	
	private String particleName;
	private Particle particle;
	private int xOff;
	private int yOff;
	private int zOff;
	private Object data;
	
	public ChangedParticle(String particleName, Particle particle, 
			int xOff, int yOff, int zOff, Object data) {
		
		this.particleName = particleName;
		this.particle = particle;
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
		this.data = data;
		
	}
	
	public ChangedParticle(String particleName, Particle particle,
			int xOff, int yOff, int zOff) {
		
		this.particleName = particleName;
		this.particle = particle;
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
		
	}
	
	public String getEnumName() {
		return particleName;
	}
	
	public Particle getParticle() {
		return particle;
	}
	
	public Integer getXOff() {
		return xOff;
	}
	
	public Integer getYOff() {
		return yOff;
	}
	
	public Integer getZOff() {
		return zOff;
	}
	
	public Object getData() {
		return data;
	}
	
}
