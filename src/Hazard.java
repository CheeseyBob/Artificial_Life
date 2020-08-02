import java.awt.Color;

class Hazard extends WorldObject {
	static Color color = Color.RED;
	
	@Override
	public Color getColor() {
		return color;
	}
	@Override
	public boolean interact(WorldObject interacter, Interaction interactionType, Object data) {
		switch (interactionType) {
		case PUSH:
			interacter.interact(this, Interaction.KILL, CauseOfDeath.HAZARD);
			return true;
		case EAT:
			interacter.interact(this, Interaction.KILL, CauseOfDeath.HAZARD);
			return true;
		case PULL:
			interacter.interact(this, Interaction.KILL, CauseOfDeath.HAZARD);
		case DISPLACE:
			interacter.interact(this, Interaction.KILL, CauseOfDeath.HAZARD);
			return true;
		default:
			return false;
		}
	}
}