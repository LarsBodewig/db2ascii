package dev.bodewig.db2ascii;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
class Fruit {
	@Id
	public int id;
	public String name;
	public String color;
	public float price;
}
