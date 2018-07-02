package com.ruinscraft.powder.model;

import java.util.ArrayList;
import java.util.List;

import com.ruinscraft.powder.model.particle.PowderParticle;

public class Layer {

	// list of rows containing PowderParticles; the matrix itself
	private List<List<PowderParticle>> rows;
	// the front-back position of the Layer
	private double position;

	public Layer() {
		this.rows = new ArrayList<>();
		position = 0;
	}

	public Layer(List<List<PowderParticle>> rows, int position) {
		this.rows = rows;
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

	public void addRow(int index, List<PowderParticle> row) {
		rows.add(index, row);
	}

	public void putRow(int index, List<PowderParticle> row) {
		rows.add(index, row);
		rows.remove(index + 1);
	}

	public double getPosition() {
		return position;
	}

	public void setPosition(double position) {
		this.position = position;
	}

}
