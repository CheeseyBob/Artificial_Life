import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.Scanner;

class Hazard extends WorldObject {
	static Color color = Color.RED;
	
	public static Hazard load(Scanner scanner) {
		return new Hazard();
	}
	
	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public String getDisplayName() {
		return "Hazard";
	}
	
	@Override
	public BufferedImage getImage() {
		return Tileset.hazardImage;
	}

	@Override
	public String getInfo() {
		return "";
	}
	
	@Override
	public boolean interact(WorldObject interacter, Interaction interactionType, Object[] data) {
		switch (interactionType) {
		case PUSH:
			interacter.interact(this, Interaction.KILL, new Object[] {CauseOfDeath.HAZARD});
			return true;
		case EAT:
			interacter.interact(this, Interaction.KILL, new Object[] {CauseOfDeath.HAZARD});
			return true;
		case PULL:
			interacter.interact(this, Interaction.KILL, new Object[] {CauseOfDeath.HAZARD});
		case DISPLACE:
			interacter.interact(this, Interaction.KILL, new Object[] {CauseOfDeath.HAZARD});
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public void save(PrintWriter pw) {
		pw.println("Hazard");
	}
}