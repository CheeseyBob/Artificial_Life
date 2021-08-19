import java.awt.*;

import javax.swing.*;

import general.Util;

class NeuralNetworkViewer extends JFrame {
	private static final long serialVersionUID = 1L;
	
	static final int width = 400, height = 400;
	static String windowTitle = "Neural Network Viewer";
	static JPanel panel = new JPanel(){
		private static final long serialVersionUID = 1L;
		
		@Override
		public void paint(Graphics g){
			draw((Graphics2D)g);
		}
	};
	
	static Color backgroundColor = new Color(100, 100, 255);
	static Color labelColor = Color.black;
	static int neuronColorDepth = 100;
	static Color[] neuronColors_negative = neuronColors_negative(neuronColorDepth);
	static Color[] neuronColors_positive = neuronColors_positive(neuronColorDepth);
	
	public static void draw(Graphics2D g){
		Rectangle panelBounds = panel.getBounds();
		
		//Draw background
		g.setColor(backgroundColor);
		g.fillRect(0, 0, panelBounds.width, panelBounds.height);
		
		if(ArtificialLife.selectedCell == null) {
			g.setColor(labelColor);
			Util.drawStringCenteredXY("No cell selected", panel.getWidth()/2, panel.getHeight()/2, g);
		} else if(ArtificialLife.selectedCell instanceof MatrixCell) {
			MatrixCell cell = (MatrixCell)ArtificialLife.selectedCell;
			
			int neuronGap = 16;
			int inputNeuronX = 32;
			int inputLabelX = 16;
			int sensoryNeuronY = 32;
			int memoryInNeuronY = inputNeuronX + cell.sensoryNeurons.length*neuronGap + 2*neuronGap;
			int conceptNeuronX = panel.getWidth()/2;
			int conceptNeuronY = 32;
			int outputNeuronX = panel.getWidth() - 32;
			int motorLabelX = outputNeuronX + 16;
			int motorNeuronY = 32;
			int memoryOutNeuronY = motorNeuronY + cell.motorNeurons.length*neuronGap + 2*neuronGap;
			
			String[] senseLabelList = {"age", "hun", "hp", "D", "R", "G", "B", "acD", "acR", "acD", "acB", "cwD", "cwR", "cwG", "cwB"};
			String[] motorLabelList = {"spd", "mov", "ac", "cw", "att", "eat", "mat", "dis", "pul", "spn", "fd", "wl"};
			
			// Sensory -> Concept //
			for(int s = 0; s < cell.sensoryNeurons.length; s ++) {
				for(int c = 0; c < cell.conceptNeurons.length; c ++) {
					g.setColor(getNeuronColor(cell.sensoryNeurons[s]*cell.sensoryConceptConnections[c][s]));
					g.drawLine(inputNeuronX, sensoryNeuronY + s*neuronGap, conceptNeuronX, conceptNeuronY + c*neuronGap);
				}
			}
			// Memory -> Concept //
			for(int m = 0; m < cell.memoryNeurons.length; m ++) {
				for(int c = 0; c < cell.conceptNeurons.length; c ++) {
					g.setColor(getNeuronColor(cell.memoryNeurons[m]*cell.memoryConceptConnections[c][m]));
					g.drawLine(inputNeuronX, memoryInNeuronY + m*neuronGap, conceptNeuronX, conceptNeuronY + c*neuronGap);
				}
			}
			// Concept -> Motor //
			for(int m = 0; m < cell.motorNeurons.length; m ++) {
				for(int c = 0; c < cell.conceptNeurons.length; c ++) {
					g.setColor(getNeuronColor(cell.motorNeurons[m]*cell.conceptMotorConnections[m][c]));
					g.drawLine(conceptNeuronX, conceptNeuronY + c*neuronGap, outputNeuronX, motorNeuronY + m*neuronGap);
				}
			}
			// Concept -> Memory //
			for(int m = 0; m < cell.memoryNeurons.length; m ++) {
				for(int c = 0; c < cell.conceptNeurons.length; c ++) {
					g.setColor(getNeuronColor(cell.memoryNeurons[m]*cell.conceptMemoryConnections[m][c]));
					g.drawLine(conceptNeuronX, conceptNeuronY + c*neuronGap, outputNeuronX, memoryOutNeuronY + m*neuronGap);
				}
			}
			
			// Sensory Neurons //
			for(int i = 0; i < cell.sensoryNeurons.length; i ++) {
				g.setColor(getNeuronColor(cell.sensoryNeurons[i]));
				Util.fillCircleCentered(inputNeuronX, sensoryNeuronY + i*neuronGap, 4, g);
				g.setColor(labelColor);
				Util.drawStringCenteredXY(senseLabelList[i], inputLabelX, sensoryNeuronY + i*neuronGap, g);
				Util.drawCircleCentered(inputNeuronX, sensoryNeuronY + i*neuronGap, 4, g);
			}
			// Memory Input Neurons //
			for(int i = 0; i < cell.memoryNeurons.length; i ++) {
				g.setColor(getNeuronColor(cell.memoryNeurons[i]));
				Util.fillCircleCentered(inputNeuronX, memoryInNeuronY + i*neuronGap, 4, g);
				g.setColor(labelColor);
				Util.drawCircleCentered(inputNeuronX, memoryInNeuronY + i*neuronGap, 4, g);
			}
			// Concept Neurons //
			for(int i = 0; i < cell.conceptNeurons.length; i ++) {
				g.setColor(getNeuronColor(cell.conceptNeurons[i]));
				Util.fillCircleCentered(conceptNeuronX, conceptNeuronY + i*neuronGap, 4, g);
				g.setColor(labelColor);
				Util.drawCircleCentered(conceptNeuronX, conceptNeuronY + i*neuronGap, 4, g);
			}
			// Motor Neurons //
			for(int i = 0; i < cell.motorNeurons.length; i ++) {
				g.setColor(getNeuronColor(cell.motorNeurons[i]));
				Util.fillCircleCentered(outputNeuronX, motorNeuronY + i*neuronGap, 4, g);
				g.setColor(labelColor);
				Util.drawStringCenteredXY(motorLabelList[i], motorLabelX, motorNeuronY + i*neuronGap, g);
				Util.drawCircleCentered(outputNeuronX, motorNeuronY + i*neuronGap, 4, g);
			}
			// Memory Output Neurons //
			for(int i = 0; i < cell.memoryNeurons.length; i ++) {
				g.setColor(getNeuronColor(cell.memoryNeurons[i]));
				Util.fillCircleCentered(outputNeuronX, memoryOutNeuronY + i*neuronGap, 4, g);
				g.setColor(labelColor);
				Util.drawCircleCentered(outputNeuronX, memoryOutNeuronY + i*neuronGap, 4, g);
			}
			
			// TODO - memory neurons //
			
			// TODO //
			// TODO //
			// TODO //
			
		}
	}
	
	public static Color getNeuronColor(double value) {
		int colorIndex = (int)(neuronColorDepth*value);
		if(colorIndex >= 0) {
			return neuronColors_positive[colorIndex];
		} else {
			return neuronColors_negative[-colorIndex];
		}
	}
	
	public static Color[] neuronColors_negative(int n) {
		Color[] colorList = new Color[n+1];
		for(int i = 0; i <= n; i ++) {
			float alpha = i*1.0f/n;
			colorList[i] = new Color(0.0f, 0.0f, 0.0f, alpha);
		}
		return colorList;
	}
	
	public static Color[] neuronColors_positive(int n) {
		Color[] colorList = new Color[n+1];
		for(int i = 0; i <= n; i ++) {
			float alpha = i*1.0f/n;
			colorList[i] = new Color(1.0f, 1.0f, 1.0f, alpha);
		}
		return colorList;
	}
	
	NeuralNetworkViewer(){
		setResizable(true);
		setSize(width, height);
		setTitle(windowTitle);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		setVisible(true);
		createBufferStrategy(2);
	}
	
	public void update() {
		repaint();
	}
}