import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.Scanner;

class Tuber extends WorldObject {
	static Color color = new Color(102, 102, 61);
	
	public static Tuber load(Scanner scanner) {
		return new Tuber();
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public String getDisplayName() {
		return "Tuber";
	}
	
	@Override
	public BufferedImage getImage() {
		return Tileset.tuberImage;
	}

	@Override
	public String getInfo() {
		return "";
	}

	@Override
	public boolean interact(WorldObject interacter, Interaction interactionType, Object[] data) {
		switch (interactionType) {
		case PULL:
			Point originalLocation = new Point(location);
			boolean wasPulled = pull(interacter, this);
			if(wasPulled) {
				remove();
				ArtificialLife.place(new Food(), originalLocation);
			}
			return wasPulled;
		default:
			return false;
		}
	}
	
	@Override
	public void save(PrintWriter pw) {
		pw.println("Tuber");
	}
}