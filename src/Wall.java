import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.Scanner;

class Wall extends WorldObject {
	private static Color color = Color.BLACK;
	
	public static Wall load(Scanner scanner) {
		return new Wall();
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public String getDisplayName() {
		return "Wall";
	}
	
	@Override
	public BufferedImage getImage() {
		return Tileset.wallImage;
	}

	@Override
	public String getInfo() {
		return "";
	}

	@Override
	public boolean interact(WorldObject interacter, Interaction interactionType, Object[] data) {
		switch (interactionType) {
		default:
			return false;
		}
	}
	
	@Override
	public void save(PrintWriter pw) {
		pw.println("Wall");
	}
}