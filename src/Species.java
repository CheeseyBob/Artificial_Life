import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.ListIterator;

import files.TextFileHandler;
import general.Tuple;

class Species {
	// These values determine the range for generation zero cells. The caps do not apply to mutations. //
	private static int conceptNeuronMin = 5, conceptNeuronMax = 40;
	private static int memoryNeuronMin = 0, memoryNeuronMax = 20;
	
	// Loading //
	private static LinkedList<Tuple<String, Species>> loadedSpeciesList = new LinkedList<Tuple<String, Species>>();
	
//	private static char[] characterList = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
	
	// Data //
	private String customName = null;
	private String code_generationZero, code_mutationSequence;
	private int conceptNeuronCount, memoryNeuronCount;
	
	
	/**
	 * Load all the species in a save folder, so that cells can be loaded and assigned their species.
	 * @param pathnam
	 */
	public static void load(String pathname) {
		loadedSpeciesList.clear();
		File speciesFolder = new File(pathname+"species"+File.separator);
		for(String speciesFileName : speciesFolder.list()) {
			String speciesReference = speciesFileName.substring(speciesFileName.indexOf("#"), speciesFileName.length() - 4);
			Species species = load(pathname, speciesReference);
			Tuple<String, Species> referencedSpecies = new Tuple<String, Species>(speciesReference, species);
			loadedSpeciesList.add(referencedSpecies);
		}
	}
	
	public static Species load(String pathname, String speciesReference) {
		String speciesFileName = pathname+"species"+File.separator+"species"+speciesReference+".dat";
		LinkedList<String> lineList = TextFileHandler.readEntireFile(speciesFileName);
		return new Species(lineList);
	}
	
	public static Species loadedSpecies(String speciesReference) {
		for(Tuple<String, Species> referencedSpecies : loadedSpeciesList) {
			if(referencedSpecies.e1.equals(speciesReference)) {
				return referencedSpecies.e2;
			}
		}
		System.out.println("COULD NOT FIND LOADED SPECIES: "+speciesReference);
		return null;
	}
	
	/**
	 * 
	 * @param maxListSize
	 * @param minCountInclusion
	 * @return A SpeciesCount list, up to maxListSize long, of the species with at least minCountInclusion members.
	 */
	public static LinkedList<Tuple<Species, Integer>> speciesCountList(int maxListSize, int minCountInclusion) {
		// Get the data. //
		LinkedList<Species> speciesList = new LinkedList<Species>();
		LinkedList<Integer> speciesCellCountList = new LinkedList<Integer>();
		for(Stepable stepable : ArtificialLife.getStepList()){
			if(stepable instanceof Cell){
				Species species = ((Cell)stepable).species;
				int index = speciesList.indexOf(species);
				if(index == -1) {
					speciesList.add(species);
					speciesCellCountList.add(1);
				} else {
					int speciesNewCellCount = speciesCellCountList.get(index) + 1;
					speciesCellCountList.set(index, speciesNewCellCount);
				}
			}
		}
		
		// Put the data into a list. //
		LinkedList<Tuple<Species, Integer>> speciesCountList = new LinkedList<Tuple<Species, Integer>>();
		while(!speciesList.isEmpty() && speciesCountList.size() < maxListSize) {
			// Add the data for the species with the highest population first. //
			Species topSpecies = speciesList.getFirst();
			int topCellCount = speciesCellCountList.getFirst();
			ListIterator<Species> speciesIterator = speciesList.listIterator();
			ListIterator<Integer> cellCountIterator = speciesCellCountList.listIterator();
			while(speciesIterator.hasNext()) {
				Species species = speciesIterator.next();
				int cellCount = cellCountIterator.next();
				if(cellCount > topCellCount) {
					topSpecies = species;
					topCellCount = cellCount;
				}
			}
			
			// Don't display species with one member. //
			if(topCellCount < minCountInclusion) {
				break;
			}
			
			// Remove the top species from the list of data once found. //
			int indexToRemove = speciesList.indexOf(topSpecies);
			speciesList.remove(indexToRemove);
			speciesCellCountList.remove(indexToRemove);
			speciesCountList.add(new Tuple<Species, Integer>(topSpecies, topCellCount));
		}
		return speciesCountList;
	}
	
	Species(){
		conceptNeuronCount = M.randInt(conceptNeuronMin, conceptNeuronMax);
		memoryNeuronCount = M.randInt(memoryNeuronMin, memoryNeuronMax);
		code_generationZero = conceptNeuronCount+"C"+memoryNeuronCount+"M";
		code_mutationSequence = "";
	}
	
	private Species(LinkedList<String> dataList) {
		dataList.remove();
		customName = dataList.remove();
		if(customName.equals(""))
			customName = null;
		dataList.remove();
		code_generationZero = dataList.remove();
		dataList.remove();
		code_mutationSequence = dataList.remove();
		dataList.remove();
		conceptNeuronCount = Integer.parseInt(dataList.remove());
		dataList.remove();
		memoryNeuronCount = Integer.parseInt(dataList.remove());
	}
	
	private Species(Species parentSpecies) {
		conceptNeuronCount = parentSpecies.conceptNeuronCount;
		memoryNeuronCount = parentSpecies.memoryNeuronCount;
		code_generationZero = parentSpecies.code_generationZero;
		code_mutationSequence = parentSpecies.code_mutationSequence;
		if(parentSpecies.customName != null) {
			customName = parentSpecies.customName+"'";
		}
	}
	
	public String getDisplayName() {
		if(customName == null) {
			String displayName = code_generationZero;
			int mutationSequenceLength = code_mutationSequence.length();
			displayName += "-"+mutationSequenceLength;
			if(mutationSequenceLength > 0) {
				if(mutationSequenceLength <= 3) {
					displayName += "-"+code_mutationSequence;
				} else {
					displayName += "-.."+code_mutationSequence.substring(mutationSequenceLength - 3);
				}
			}
			return displayName;
		} else {
			return customName;
		}
	}
	
	public String getFullName() {
		int mutationSequenceLength = code_mutationSequence.length();
		if(mutationSequenceLength > 0) {
			return code_generationZero+"-"+code_mutationSequence.length()+"-"+code_mutationSequence;
		} else {
			return code_generationZero+"-"+code_mutationSequence.length();
		}
	}
	
	public String getInfo() {
		String info = "";
		info += "Concept neurons = "+conceptNeuronCount+"<br>";
		info += "Memory neurons = "+memoryNeuronCount+"<br>";
		return info;
	}
	
	void mutate(MatrixCell cell) {
		cell.species = new Species(this);
		int aspectToMutate = M.randInt(0, 1);
		switch (aspectToMutate) {
		case 0:
			// Concept neuron count. //
			mutate_conceptNeuronCount(cell);
			break;
		case 1:
			// Memory neuron count. //
			mutate_memoryNeuronCount(cell);
			break;
		default:
			throw new RuntimeException("Species mutation failed.");
		}
	}
	
	private void mutate_conceptNeuronCount(MatrixCell cell) {
		// We cannot lower the concept neuron count below 1. //
		double chanceToRemove = (cell.species.conceptNeuronCount <= 1) ? 0.0 : 0.5;
		if(M.roll(chanceToRemove)) {
			cell.species.conceptNeuronCount --;
			cell.species.code_mutationSequence += "c";
			cell.mutate_conceptNeuron_remove();
		} else {
			cell.species.conceptNeuronCount ++;
			cell.species.code_mutationSequence += "C";
			cell.mutate_conceptNeuron_add();
		}
	}
	
	private void mutate_memoryNeuronCount(MatrixCell cell) {
		// We cannot lower the memory neuron count below 0. //
		double chanceToRemove = (cell.species.memoryNeuronCount == 0) ? 0.0 : 0.5;
		if(M.roll(chanceToRemove)) {
			cell.species.memoryNeuronCount --;
			cell.species.code_mutationSequence += "m";
			cell.mutate_memoryNeuron_remove();
		} else {
			cell.species.memoryNeuronCount ++;
			cell.species.code_mutationSequence += "M";
			cell.mutate_memoryNeuron_add();
		}
	}
	
	public int neuronCount_concept() {
		return conceptNeuronCount;
	}
	
	public int neuronCount_memory() {
		return memoryNeuronCount;
	}
	
	public void setCustomName(String customName) {
		this.customName = customName;
	}
	
	public void save(String pathname) {
		// Get the species reference. //
		String speciesReference = "#"+Integer.toHexString(hashCode());
		
		// Start writing to the species data file. //
		String speciesFileName = pathname+"species"+File.separator+"species"+speciesReference+".dat";
		PrintWriter pw = TextFileHandler.startWritingToFile(speciesFileName);
		pw.println("customName=");
		pw.println(customName == null ? "" : customName);
		pw.println("code_generationZero=");
		pw.println(code_generationZero);
		pw.println("code_mutationSequence=");
		pw.println(code_mutationSequence);
		pw.println("conceptNeuronCount=");
		pw.println(conceptNeuronCount);
		pw.println("memoryNeuronCount=");
		pw.println(memoryNeuronCount);
		
		// Done //
		pw.close();
	}
}

