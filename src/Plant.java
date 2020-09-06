import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.Scanner;

class Plant extends WorldObject implements Stepable {
	static Color fruitingCol = new Color(0, 153, 0);
	static Color idleCol = new Color(102, 255, 102);
	
	boolean fruitsInSummer;
	int stepsToBearFruit = M.randInt(50, 100);
	
	public static Plant load(Scanner scanner, String pathname) {
		String[] data = scanner.next().split(":");
		boolean fruitsInSummer = Boolean.parseBoolean(data[0]);
		int stepsToBearFruit = Integer.parseInt(data[1]);
		return new Plant(fruitsInSummer, stepsToBearFruit);
	}
	
	Plant(boolean fruitsInSummer){
		this.fruitsInSummer = fruitsInSummer;
	}
	
	private Plant(boolean fruitsInSummer, int stepsToBearFruit){
		this.fruitsInSummer = fruitsInSummer;
		this.stepsToBearFruit = stepsToBearFruit;
	}
	
	private void produceFruit() {
		int x = M.randInt(location.x - 1, location.x + 1);
		int y = M.randInt(location.y - 1, location.y + 1);
		ArtificialLife.place(new Food(), x, y);
	}
	
	@Override
	public Color getColor() {
		return isFruiting() ? fruitingCol : idleCol;
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
		info += "Produces in: "+(fruitsInSummer ? "summer" : "winter")+"<br>";
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
	
	private boolean isFruiting() {
		return (ArtificialLife.isSummer && fruitsInSummer) || (!ArtificialLife.isSummer && !fruitsInSummer);
	}
	
	@Override
	public void save(PrintWriter pw, String pathname) {
		pw.println("Plant");
		pw.println(fruitsInSummer+":"+stepsToBearFruit);
	}
	
	@Override
	public void step(){
		if(isFruiting()) {
			produceFruit();
		}
	}
}