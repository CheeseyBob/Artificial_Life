import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import files.ImageHandler;
import files.TextFileHandler;
import maths.M;

class ArtificialLife implements Runnable {
	private static final int DEFAULT_MAP_HEIGHT = 200;
	private static final int DEFAULT_MAP_WIDTH = 200;
	private static final double DEFAULT_MAP_HAZARD_DENSITY = 0.01;
	private static final double DEFAULT_MAP_PLANT_DENSITY = 0.05;
	private static final double DEFAULT_MAP_WALL_DENSITY = 0.1;
	
	private static InfoWindow infoWindow;
	private static InfoWindow_Species speciesWindow;
	private static NeuralNetworkViewer neuralNetworkViewer;
	
	static int fpsCap;
	static int width, height;
	static int minCellCount;
	
	static int stepCounter = 0;
	static int totalChildren = 0;
	static int totalChildrenWithTwoParents = 0;
	static int totalDeathsBy[] = new int[CauseOfDeath.values().length];
	
	static Cell selectedCell = null;
	
	static String injectionListOrigin = "";
	static LinkedList<MatrixCell> injectionList = new LinkedList<MatrixCell>();
	
	// The World //
	static WorldObject[][] grid = new WorldObject[width][height];
	static TurnList turnList = new TurnList();
	
	// Seasons //
	static int seasonDuration = 1000;
	static boolean isSummer = true;
	
	// Auto-test variables //
	static boolean isAutotesting = false;
	static AutotestManager autotestManager = null;
	
	private static MatrixCell createCellToSpawn() {
		if(injectionList.isEmpty()) {
			return new MatrixCell();
		} else {
			MatrixCell parent = M.chooseRandom(injectionList);
			MatrixCell newCell = MatrixCell.createChild(parent);
			return newCell;
		}
	}
	
	public static int getCellCount(){
		int cellCount = 0;
		for(Stepable stepable : getStepList()){
			if(stepable instanceof Cell){
				cellCount ++;
			}
		}
		return cellCount;
	}
	
	public static int getCellIndex(Cell cell){
		int index = 0;
		for(Stepable stepable : getStepList()){
			if(stepable instanceof Cell){
				if(stepable == cell){
					return index;
				}
				index ++;
			}
		}
		return -1;
	}
	
	public static double getCellSizeMedian() {
		int cellCount = getCellCount();
		if(cellCount == 0) {
			return 0;
		}
		double[] cellSizeList = new double[cellCount];
		int i = 0;
		for(Stepable stepable : getStepList()){
			if(stepable instanceof Cell){
				cellSizeList[i] = ((Cell)stepable).energyStoreSize;
				i ++;
			}
		}
		return M.median(cellSizeList);
	}
	
	public static double getCellSpeedMedian() {
		int cellCount = getCellCount();
		if(cellCount == 0) {
			return 0;
		}
		double[] cellSpeedList = new double[cellCount];
		int i = 0;
		for(Stepable stepable : getStepList()){
			if(stepable instanceof Cell){
				cellSpeedList[i] = ((Cell)stepable).speed;
				i ++;
			}
		}
		return M.median(cellSpeedList);
	}
	
	public static Cell getFirstCell() {
		for(Stepable stepable : getStepList()){
			if(stepable instanceof Cell){
				return (Cell)stepable;
			}
		}
		return null;
	}
	
	public static Cell getLastCell(){
		Cell lastCell = null;
		for(Stepable stepable : getStepList()){
			if(stepable instanceof Cell){
				lastCell = (Cell)stepable;
			}
		}
		return lastCell;
	}
	
	public static Cell getNextCell(Cell cell) {
		boolean returnNext = false;
		for(Stepable stepable : getStepList()){
			if(returnNext && stepable instanceof Cell){
				return (Cell)stepable;
			} else if(stepable == cell){
				returnNext = true;
			}
		}
		return null;
	}
	
	public static WorldObject getObjectAt(int x, int y) {
		return getObjectAt(new Point(x, y));
	}
	
	public static WorldObject getObjectAt(Point p) {
		ArtificialLife.wrapPoint(p);
		return ArtificialLife.grid[p.x][p.y];
	}
	
	public static WorldObject getObjectAtCursor() {
		return getObjectAt(Display.viewX, Display.viewY);
	}
	
	public static Cell getPreviousCell(Cell cell) {
		Cell previousCell = null;
		for(Stepable stepable : getStepList()){
			if(stepable instanceof Cell){
				if(stepable == cell){
					return previousCell;
				} else {
					previousCell = (Cell)stepable;
				}
			}
		}
		return null;
	}
	
	public static LinkedList<Stepable> getStepList(){
		return turnList.getStepList();
	}
	
	private static void loadMap(String mapFilename) {
		try {
			loadMap_fromFile(mapFilename);
		} catch(Exception e) {
			// If loading the map fails, show an error message and load a default map. //
			String errorMessage = "Error loading map."+"\n"+"Continue with default map?";
			int choice = JOptionPane.showConfirmDialog(null, errorMessage, "", JOptionPane.YES_NO_OPTION);
			if(choice == 0) {
				loadMap_defaultMap();
			} else {
				System.exit(0);
				return;
			}
		}
	}
	
	private static void loadMap_defaultMap() {
		width = DEFAULT_MAP_WIDTH;
		height = DEFAULT_MAP_HEIGHT;
		grid = new WorldObject[width][height];
		
		for(int x = 0; x < width; x ++){
			for(int y = 0; y < height; y ++){
				if(M.roll(DEFAULT_MAP_WALL_DENSITY)) {
					place(new Wall(), x, y);
				}
				if(M.roll(DEFAULT_MAP_HAZARD_DENSITY)) {
					place(new Hazard(), x, y);
				}
				if(M.roll(DEFAULT_MAP_PLANT_DENSITY)) {
					int plantType = M.randInt(4);
					switch(plantType) {
					case 0:
						place(new Plant_Seasonal(true), x, y);
						break;
					case 1:
						place(new Plant_Seasonal(false), x, y);
						break;
					case 2:
						place(new Plant_Fruit(), x, y);
						break;
					case 3:
						place(new Plant_Tuber(), x, y);
						break;
					}
				}
			}
		}
	}
	
	private static void loadMap_fromFile(String mapFilename) {
		BufferedImage mapImage = ImageHandler.loadImage("data/"+mapFilename);
		width = mapImage.getWidth();
		height = mapImage.getHeight();
		grid = new WorldObject[width][height];
		for(int x = 0; x < width; x ++){
			for(int y = 0; y < height; y ++){
				int rgb = mapImage.getRGB(x, y);
				if(rgb == Color.BLACK.getRGB()){
					place(new Wall(), x, y);
				} else if(rgb == Color.RED.getRGB()){
					place(new Hazard(), x, y);
				} else if(rgb == Plant.color.getRGB()){
					place(new Plant(), x, y);
				} else if(rgb == Color.GREEN.getRGB()){
					place(new Plant_Seasonal(true), x, y);
				} else if(rgb == Color.YELLOW.getRGB()){
					place(new Plant_Seasonal(false), x, y);
				} else if(rgb == Plant_Fruit.color.getRGB()){
					place(new Plant_Fruit(), x, y);
				} else if(rgb == Plant_Tuber.color.getRGB()){
					place(new Plant_Tuber(), x, y);
				} else if(rgb == Door.color.getRGB()){
					place(new Door(), x, y);
				}
			}
		}
	}
	
	public static void loadSavedMap() {
		// Choose a save folder to load. //
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File("saves"));
		fileChooser.showOpenDialog(null);
		File folder = fileChooser.getSelectedFile();
		if(folder != null) {
			String path = folder.getPath()+File.separator;
			String mapFileName = path+"map.dat";
			
			// Load the chosen save. //
			Scanner scanner = TextFileHandler.startReadingFromFile(mapFileName);
			width = Integer.parseInt(scanner.next().split(":")[1]);
			height = Integer.parseInt(scanner.next().split(":")[1]);
			grid = new WorldObject[width][height];
			turnList.clear();
			Species.load(path);
			while(scanner.hasNext()) {
				String line = scanner.next();
				if(line.startsWith("@")) {
					String[] data = line.substring(1).split("#"); // data[0]=coords, data[1]=nextStep
					String[] coords = data[0].split(",");
					int x = Integer.parseInt(coords[0]);
					int y = Integer.parseInt(coords[1]);
					WorldObject object = WorldObject.loadObject(scanner, path);
					object.setLocation(x, y);
					if(object instanceof Stepable) {
						int stepsFromNow = Integer.parseInt(data[1]);
						turnList.add((Stepable)object, stepsFromNow);
					}
				}
			}
			scanner.close();
			System.out.println("LOADED MAP");
		}
	}
	
	public static void loadSavedSpecies() {
		// Choose a save folder to load. //
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File("saves"));
		fileChooser.showOpenDialog(null);
		File folder = fileChooser.getSelectedFile();
		if(folder != null) {
			String path = folder.getPath()+File.separator;
			String cellListFileName = path+"cells.dat";
			
			// Load the chosen saved species into the injection list. //
			Scanner scanner = TextFileHandler.startReadingFromFile(cellListFileName);
			Species.load(path);
			injectionList.clear();
			while(scanner.hasNext()) {
				WorldObject object = WorldObject.loadObject(scanner, path);
				MatrixCell cell = (MatrixCell)object;
				injectionList.add(cell);
			}
			scanner.close();
			injectionListOrigin = "Species "+injectionList.getFirst().species.getDisplayName();
			System.out.println("LOADED SPECIES");
		}
	}
	
	public static void main(String[] args) {	
		ArtificialLife.setup();
		Controls.setup();
		Tileset.load();//XXX//
		
		// Auto-testing //
		if(isAutotesting) {
			autotestManager = new AutotestManager();
			autotestManager.setup();
			Controls.setSpeed(Controls.SPEED_SETTING[9]);
		}
		
		// Start Simulation //
		new ArtificialLife().start();
	}
	
	public static boolean place(WorldObject object, int x, int y){
		return place(object, new Point(x, y));
	}
	
	public static boolean place(WorldObject object, Point location){
		wrapPoint(location);
		if(grid[location.x][location.y] == null){
			object.setLocation(location);
			grid[location.x][location.y] = object;
			if(object instanceof Stepable){
				turnList.add((Stepable)object, 1);
			}
			return true;
		} else return false;
	}
	
	public static boolean placeRandomly(WorldObject object){
		int x = M.randInt(width - 1);
		int y = M.randInt(height - 1);
		return place(object, new Point(x, y));
	}
	
	public static void remove(WorldObject object) {
		if(object instanceof Stepable) {
			turnList.remove((Stepable)object);
		}
		ArtificialLife.grid[object.location.x][object.location.y] = null;
	}
	
	public static void removeObjectAt(Point location) {
		WorldObject object = getObjectAt(location);
		if(object != null) {
			object.remove();
		}
	}
	
	public static void removeObjectAtCursor() {
		WorldObject object = getObjectAtCursor();
		if(object != null)
			object.remove();
	}
	
	public static void saveMap() {
		Date date = new Date();
		String pathname = "saves/save-"+date.getTime()+File.separator;
		String mapFileName = pathname+"map.dat";
		PrintWriter pw = TextFileHandler.startWritingToFile(mapFileName, true);
		pw.println("width:"+width);
		pw.println("height:"+height);
		for(int x = 0; x < width; x ++) {
			for(int y = 0; y < height; y ++) {
				WorldObject object = grid[x][y];
				if(object != null) {
					String objectLocatorString = "@"+x+","+y;
					if(object instanceof Stepable) {
						int stepsToNextTurn = turnList.getStepsToNextTurn((Stepable)object);
						objectLocatorString += "#"+stepsToNextTurn;
					}
					pw.println(objectLocatorString);
					object.save(pw, pathname);
				}
			}
		}
		pw.close();
		JOptionPane.showMessageDialog(null, "World saved to:"+"\n"+pathname, "", JOptionPane.INFORMATION_MESSAGE);
		System.out.println("SAVED WORLD TO "+pathname);
	}
	
	public static void saveSpecies(Species species) {
		Date date = new Date();
		String pathname = "saves/species-"+species.getDisplayName()+"-"+date.getTime()+File.separator;
		String cellListFileName = pathname+"cells.dat";
		PrintWriter pw = TextFileHandler.startWritingToFile(cellListFileName, true);
		for(int x = 0; x < width; x ++) {
			for(int y = 0; y < height; y ++) {
				WorldObject object = grid[x][y];
				if(object != null && object instanceof Cell) {
					Cell cell = (Cell)object;
					if(cell.species == species) {
						cell.save(pw, pathname);
					}
				}
			}
		}
		pw.close();
		JOptionPane.showMessageDialog(null, "Species saved to:"+"\n"+pathname, "", JOptionPane.INFORMATION_MESSAGE);
		System.out.println("SAVED SPECIES TO "+pathname);
	}
	
	public static void select(WorldObject selection) {
		if(selection instanceof Cell) {
			selectedCell = (Cell)selection;
		}
	}
	
	public static void selectHoveredObject() {
		select(getObjectAt(Display.viewX, Display.viewY));
	}
	
	public static void setup(){
		// Load parameters from the init file. //
		LinkedList<String> initData = TextFileHandler.readEntireFile("data/init.txt");
		String mapFilename = null;
		for(String line : initData){
			// Ignore comment lines. //
			if(!line.startsWith("//")){
				int dataIndex = line.indexOf("=") + 1;
				if(line.startsWith("autotest=")){
					isAutotesting = line.substring(dataIndex).equals("yes");
				}
				if(line.startsWith("fpsCap=")){
					fpsCap = Integer.parseInt(line.substring(dataIndex));
				}
				if(line.startsWith("map=")){
					mapFilename = line.substring(dataIndex);
				}
				if(line.startsWith("minCellCount=")){
					minCellCount = Integer.parseInt(line.substring(dataIndex));
				}
				if(line.startsWith("drawScale=")){
					Display.tileSize_mapView = Integer.parseInt(line.substring(dataIndex));
				}
				if(line.startsWith("defaultAttackStrength=")){
					Cell.defaultAttackStrength = Integer.parseInt(line.substring(dataIndex));
				}
				if(line.startsWith("defaultBiteSize=")){
					Cell.defaultBiteSize = Integer.parseInt(line.substring(dataIndex));
				}
				if(line.startsWith("defaultBuildStrength=")){
					Cell.defaultBuildStrength = Integer.parseInt(line.substring(dataIndex));
				}
				if(line.startsWith("defaultEnergyStoreSize=")){
					Cell.defaultEnergyStoreSize = Integer.parseInt(line.substring(dataIndex));
				}
				if(line.startsWith("defaultHP=")){
					Cell.defaultHP = Integer.parseInt(line.substring(dataIndex));
				}
				if(line.startsWith("energyGainPerFood=")){
					Food.defaultFoodEnergy = Integer.parseInt(line.substring(dataIndex));
				}
				if(line.startsWith("baseEnergyCost=")){
					Cell.baseEnergyCost = Integer.parseInt(line.substring(dataIndex));
				}
				if(line.startsWith("energyCostMultiplier_attackStrength=")){
					Cell.energyCostMultiplier_attackStrength = Double.parseDouble(line.substring(dataIndex));
				}
				if(line.startsWith("energyCostMultiplier_biteSize=")){
					Cell.energyCostMultiplier_biteSize = Double.parseDouble(line.substring(dataIndex));
				}
				if(line.startsWith("energyCostMultiplier_buildStrength=")){
					Cell.energyCostMultiplier_buildStrength = Double.parseDouble(line.substring(dataIndex));
				}
				if(line.startsWith("energyCostMultiplier_energyStoreSize=")){
					Cell.energyCostMultiplier_energyStoreSize = Double.parseDouble(line.substring(dataIndex));
				}
				if(line.startsWith("energyCostMultiplier_hpMax=")){
					Cell.energyCostMultiplier_hpMax = Double.parseDouble(line.substring(dataIndex));
				}
				if(line.startsWith("energyCostMultiplier_speed=")){
					Cell.energyCostMultiplier_speed = Double.parseDouble(line.substring(dataIndex));
				}
				if(line.startsWith("birthEnergyRequirement=")){
					Cell.birthEnergyRequirement = Integer.parseInt(line.substring(dataIndex));
				}
				if(line.startsWith("energyUponBirth=")){
					Cell.energyUponBirth = Integer.parseInt(line.substring(dataIndex));
				}
			}
		}
		
		// Load the map. //
		loadMap(mapFilename);
		
		// Center the display. //
		Display.viewX = width/2;
		Display.viewY = height/2;
	}
	
	private static void spawnNewCells() {
		int cellCount = getCellCount();
		int failedPlaceAttempts = 0;
		int maxFailedPlaceAttempts = 100;
		while(cellCount < minCellCount) {
			Cell newCell = createCellToSpawn();
			boolean placedSuccessfully = placeRandomly(newCell);
			if(placedSuccessfully) {
				cellCount ++;
			} else {
				failedPlaceAttempts ++;
				if(failedPlaceAttempts > maxFailedPlaceAttempts) {
					break;
				}
			}
		}
	}
	
	public static void step(){
		// Step the things that need to. //
		turnList.step();
		
		// Spawn new cells if the population is too low. //
		if(Controls.spawnNewCells) {
			spawnNewCells();
		}
		
		// Pause simulation if there has been an extinction. //
		if(!Controls.spawnNewCells && getCellCount() == 0) {
			Controls.setSpeed(Controls.SPEED_SETTING[0]);
		}
		
		// Update the view location if we are following a cell. //
		if(selectedCell != null) {
			Display.viewX = ArtificialLife.selectedCell.getX();
			Display.viewY = ArtificialLife.selectedCell.getY();
		}
		
		// Seasons//
		if(stepCounter % seasonDuration == 0) {
			isSummer = !isSummer;
		}
		
		// Finally, increment the step counter. //
		stepCounter ++;
	}
	
	public static void wrapPoint(Point p){//TODO : this should be improved.
		while(p.x < 0) {
			p.x += width;
		}
		while(p.y < 0) {
			p.y += height;
		}
		p.x = p.x%width;
		p.y = p.y%height;
	}
	
	private ArtificialLife(){
		Display.instance.addKeyListener(Controls.instance);
		Display.instance.setVisible(true);
		speciesWindow = new InfoWindow_Species();
		speciesWindow.addKeyListener(Controls.instance);
		speciesWindow.setVisible(true);
		infoWindow = new InfoWindow();
		infoWindow.addKeyListener(Controls.instance);
		infoWindow.setVisible(true);
		neuralNetworkViewer = new NeuralNetworkViewer();
		neuralNetworkViewer.setVisible(true);
	}
	
	public void run() {
		while(true){
			Controls.step();
			if(Controls.isGameRunning){
				step();
			} else if(Controls.stepSimulationOnce){
				Controls.stepSimulationOnce = false;
				step();
			}
			if(stepCounter % Controls.stepsPerDraw == 0){
				Display.instance.draw();
				infoWindow.update();
				speciesWindow.update();
				neuralNetworkViewer.update();
			}
			if(Controls.isFramerateCapped){
				try{
					Thread.sleep(1000/fpsCap);
				} catch(InterruptedException e){
					e.printStackTrace();
				}
			}
			if(isAutotesting) {
				autotestManager.step();
			}
		}
	}
	
	public void start() {
		stepCounter = 0;
		Controls.isGameRunning = true;
		new Thread(this).start();
	}
}