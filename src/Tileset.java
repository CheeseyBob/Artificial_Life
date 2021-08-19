import java.awt.image.BufferedImage;

import files.ImageHandler;

class Tileset {
	public static BufferedImage cellImage = null;
	public static BufferedImage foodImage = null;
	public static BufferedImage hazardImage = null;
	public static BufferedImage plantImage = null;
	public static BufferedImage tuberImage = null;
	public static BufferedImage wallImage = null;
	
	static void load() {
		cellImage = ImageHandler.loadImage("data/tilesets/default/cell.png");
		foodImage = ImageHandler.loadImage("data/tilesets/default/food.png");
		hazardImage = ImageHandler.loadImage("data/tilesets/default/hazard.png");
		plantImage = ImageHandler.loadImage("data/tilesets/default/plant.png");
		tuberImage = ImageHandler.loadImage("data/tilesets/default/tuber.png");
		wallImage = ImageHandler.loadImage("data/tilesets/default/wall.png");
		
		Display.tileSize = cellImage.getHeight();
	}
	
	
}