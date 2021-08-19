import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.Scanner;

class Plant extends WorldObject implements Stepable {
	static Color color = new Color(0, 200, 0);
	
	int stepsToBearFruit = M.randInt(50, 100);
	
	public static Plant load(Scanner scanner, String pathname) {
		String[] data = scanner.next().split(":");
		int stepsToBearFruit = Integer.parseInt(data[1]);
		return new Plant(stepsToBearFruit);
	}
	
	Plant(){
		
	}
	
	private Plant(int stepsToBearFruit){
		this.stepsToBearFruit = stepsToBearFruit;
	}
	
	private void produceFruit() {
		int x = M.randInt(location.x - 1, location.x + 1);
		int y = M.randInt(location.y - 1, location.y + 1);
		ArtificialLife.place(new Food(), x, y);
	}
	
	@Override
	public Color getColor() {
		return color;
	}
	
	@Override
	public String getDisplayName() {
		return "Plant";
	}
	
	@Override
	public BufferedImage getImage() {
		return Tileset.plantImage;
	}

	@Override
	public String getInfo() {
		String info = "";
		info += "Produces every: "+stepsToBearFruit+" steps"+"<br>";
		return info;
	}
	
	@Override
	public int getStepsToNextTurn() {
		return stepsToBearFruit;
	}
	
	@Override
	public boolean interact(WorldObject interacter, Interaction interactionType, Object[] data) {
		switch (interactionType) {
		default:
			return false;
		}
	}
	
	@Override
	public void save(PrintWriter pw, String pathname) {
		pw.println("Plant");
		pw.println(stepsToBearFruit);
	}
	
	@Override
	public void step(){
		produceFruit();
	}
}