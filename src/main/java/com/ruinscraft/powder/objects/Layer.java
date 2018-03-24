package com.ruinscraft.powder.objects;

import java.util.ArrayList;
import java.util.List;

public class Layer {

	private List<List<PowderParticle>> rows;
	private float spacing;
	private int position;
	
	public Layer() {
		this.rows = new ArrayList<List<PowderParticle>>();
	}
	
	public Layer(List<List<PowderParticle>> rows, float spacing, int position) {
		this.rows = rows;
		this.spacing = spacing;
		this.position = position;
	}
	
	public List<List<PowderParticle>> getRows() {
		return rows;
	}
	
	public void setRows(List<List<PowderParticle>> rows) {
		this.rows = rows;
	}
	
	public void addRows(List<List<PowderParticle>> rows) {
		this.rows.addAll(rows);
	}
	
	public void addRow(List<PowderParticle> row) {
		rows.add(row);
	}
	
	public Float getSpacing() {
		return spacing;
	}
	
	public void setSpacing(float spacing) {
		this.spacing = spacing;
	}
	
	public Integer getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
}
