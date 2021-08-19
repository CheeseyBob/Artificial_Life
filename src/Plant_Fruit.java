import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.Scanner;

class Plant_Fruit extends WorldObject {
	static Color color = new Color(102, 255, 102);
	static Color color_hasFruit = new Color(0, 153, 0);
	
	int lastPicked = 0;
	int stepsToBearFruit = M.randInt(400, 800);
	
	public static Plant_Fruit load(Scanner scanner, String pathname) {
		String[] data = scanner.next().split(":");
		Plant_Fruit plant = new Plant_Fruit();
		plant.lastPicked = Integer.parseInt(data[0]);
		plant.stepsToBearFruit = Integer.parseInt(data[1]);
		return plant;
	}
	
	private void produceFruit() {
		for(Direction direction : Direction.values()) {
			ArtificialLife.place(new Food(), getAdjacentLocation(direction));
			lastPicked = ArtificialLife.stepCounter;
		}
	}

	@Override
	public Color getColor() {
		return hasFruit() ? color_hasFruit : color;
	}

	@Override
	public String getDisplayName() {
		return "Fruiting Plant";
	}
	
	@Override
	public BufferedImage getImage() {
		return Tileset.plantImage;
	}

	@Override
	public String getInfo() {
		String info = "";
		info += "Produces every: "+stepsToBearFruit+" steps"+"<br>";
		info += "Has fruit: "+(hasFruit() ? "yes" : "no")+"<br>";
		return info;
	}
	
	private boolean hasFruit() {
		return (ArtificialLife.stepCounter >= lastPicked + stepsToBearFruit);
	}

	@Override
	public boolean interact(WorldObject interacter, Interaction interactionType, Object[] data) {
		switch (interactionType) {
		case ATTACK:
			if(hasFruit()) {
				produceFruit();
				return true;
			} else {
				return false;
			}
		default:
			return false;
		}
	}
	
	@Override
	public void save(PrintWriter pw, String pathname) {
		pw.println("Plant_Fruit");
		pw.println(lastPicked+":"+stepsToBearFruit);
	}
}