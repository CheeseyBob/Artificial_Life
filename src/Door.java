import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.Scanner;

class Door extends WorldObject {
	static Color color = new Color(51, 51, 51);
	
	public static void closeAll(boolean closeForcibly) {
		for(int x = 0; x < ArtificialLife.width; x ++) {
			for(int y = 0; y < ArtificialLife.height; y ++) {
				WorldObject object = ArtificialLife.grid[x][y];
				if(object instanceof Door) {
					((Door)object).close(closeForcibly);
				}
			}
		}
	}
	
	public static Door load(Scanner scanner, String pathname) {
		return new Door();
	}
	
	public static void openAll() {
		for(int x = 0; x < ArtificialLife.width; x ++) {
			for(int y = 0; y < ArtificialLife.height; y ++) {
				WorldObject object = ArtificialLife.grid[x][y];
				if(object instanceof Door) {
					((Door)object).open();
				}
			}
		}
	}
	
	public void close(boolean closeForcibly) {
		for(Direction direction : Direction.values()) {
			Point closeLocation = getAdjacentLocation(direction);
			if(closeForcibly) {
				ArtificialLife.removeObjectAt(closeLocation);
			}
			ArtificialLife.place(new Wall(), closeLocation);
		}
	}
	
	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public String getDisplayName() {
		return "Door";
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
	
	public void open() {
		for(Direction direction : Direction.values()) {
			WorldObject object = ArtificialLife.getObjectAt(getAdjacentLocation(direction));
			if(object != null && object instanceof Wall) {
				object.remove();
			}
		}
	}
	
	@Override
	public void save(PrintWriter pw, String pathname) {
		pw.println("Door");
	}
}