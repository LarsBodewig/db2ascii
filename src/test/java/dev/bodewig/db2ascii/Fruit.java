package dev.bodewig.db2ascii;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
class Fruit {
	private static final boolean VEGETARIAN = true;
	private static String category = "produce";

	public static final String DISCRIMINATOR = "Fruit";
	public static String pricePer = "unit";

	@Id
	public int id;
	public String name;
	public String color;
	public float price;
}
