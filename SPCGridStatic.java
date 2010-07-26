/*
 * This is the main part of the simulation of the TA version of the model 
 */

import java.util.*;
 
public class TAGridStatic {

	public ArrayList <TACell> tissue;// List of cells that make up the tissue
	private Random rand = new Random();
	public static int maxlineage;//probably static is  unnecessary
	public int gsize;
	
	public TAGridStatic(int size, int maxC, double frac, boolean justSC) {//beth: this is the constructor
		// Create new instance of simulation with size of grid maximum TA cycle and fraction of stem cells 
	    TACell.maxCycle = maxC+1;// (see TACell)
	    gsize = size;
		TABoxStatic[][] grid = new TABoxStatic[size][size];
        //beth: matrix of dimensions sizexsize containing homes, and called 'grid'
		// Temporary 2D array to hold boxes in Cartesian grid so that connections can be made

		tissue = new ArrayList<TACell>();// Creates the list structure for the cells that constitute the tissue
        if (justSC) placecolonies(grid,maxC,frac);
        else fillwithcells(grid,maxC,frac);
        //note grid contents will be changed by method bcos I am passing a pointer to the grid 
        //although the grid pointer can't be changed (not that I want to!)
		//beth: at this point there are no holes (type 0 cells).
		for (int x = 0; x < size; x++) { //  Loop through all the boxes in the grid 
			for (int y = 0; y < size; y++) {
		        for (int xx = x - 1; xx <= x + 1; xx++) {
			        for (int yy = y - 1; yy <= y + 1; yy++) {
						if((y!=yy)||(x!=xx)) // Form links with their 8 immediate neighbours
			            grid[x][y].addNeighbour(grid[bounds(xx,size)][bounds(yy,size)]);
						//This maintains the cartesian relationship between each of the boxes without having to maintain the array
			        }
			    }
			}
	    } 
		//reset the values in the static cell counting arrays
		TACell.resetstaticcounters();
	}//beth: end of constructor
	private void fillwithcells(TABoxStatic[][] grid,int maxC,double frac){
		int x,y,k;
		int lineage =0;
		TACell cell;//just a name to use for each cell as it is placed in a home in the grid
		int sc = (int)(gsize*gsize*frac);//beth: 64 hard coded.could use size
		for (k = 0; k < gsize; k++) {
			x = k;
        	for(y=0;y<k;y++){
			    grid[x][y] = new TABoxStatic(x,y);// New instance of TABox created and added to 2D grid
				cell = new TACell(grid[x][y],lineage);// New instance of TACell created and given unique lineage id
				grid[x][y].occupant = cell;// The new cell is added to the TABox
				cell.type=rand.nextInt(maxC)+2;// Cell type set randomly to either TA_1,TA_2,TA_3 or TA_4
				tissue.add(cell);// Add new cell to list of cells that constitute the tissue
				lineage++;
			    grid[y][x] = new TABoxStatic(y,x);// New instance of TABox created and added to 2D grid
				cell = new TACell(grid[y][x],lineage);// New instance of TACell created and given unique lineage id
				grid[y][x].occupant = cell;// The new cell is added to the TABox
				cell.type=rand.nextInt(maxC)+2;// Cell type set randomly to either TA_1,TA_2,TA_3 or TA_4
				tissue.add(cell);// Add new cell to list of cells that constitute the tissue
				lineage++;
			}
		    grid[k][k] = new TABoxStatic(k,k);// New instance of TABox created and added to 2D grid
			cell = new TACell(grid[k][k],lineage);// New instance of TACell created and given unique lineage id
			grid[k][k].occupant = cell;// The new cell is added to the TABox
			cell.type=rand.nextInt(maxC)+2;// Cell type set randomly to either TA_1,TA_2,TA_3 or TA_4
			tissue.add(cell);// Add new cell to list of cells that constitute the tissue
			lineage++;
		}
		
		while(sc>0){ // while not enough stem cells allocated
			cell = tissue.get(rand.nextInt(tissue.size())); // Pick a cell at random from tissue list
			if (cell.type!=1){ // If that cell is not already a stem cell type 1  
				cell.type=1;// Chance to an SC
				sc--;
			}
		}
		maxlineage = lineage;
		
	}
	
	private void placecolonies(TABoxStatic[][] grid,int maxC,double frac){
		int x,y,k;
		int lineage = 0;
		TACell cell;//just a name to use for each cell as it is placed in a home in the grid
		int sc = (int)(gsize*gsize*frac);//beth: 64 hard coded.could use size
		//System.out.println("sc "+sc);
		for (k = 0; k < gsize; k++) {
			x = k;
        	for(y=0;y<k;y++){
			    grid[x][y] = new TABoxStatic(x,y);// New instance of TABox created and added to 2D grid
				cell = new TACell(grid[x][y],lineage);// New instance of TACell created and given unique lineage id
				grid[x][y].occupant = cell;// The new cell is added to the TABox
				cell.type=0;// space
				tissue.add(cell);// Add new cell to list of cells that constitute the tissue
			    grid[y][x] = new TABoxStatic(y,x);// New instance of TABox created and added to 2D grid
				cell = new TACell(grid[y][x],lineage);// New instance of TACell created and given unique lineage id
				grid[y][x].occupant = cell;// The new cell is added to the TABox
				cell.type=0;// Cell type set randomly to either TA_1,TA_2,TA_3 or TA_4
				tissue.add(cell);// Add new cell to list of cells that constitute the tissue
			}
		    grid[k][k] = new TABoxStatic(k,k);// New instance of TABox created and added to 2D grid
			cell = new TACell(grid[k][k],lineage);// New instance of TACell created and given unique lineage id
			grid[k][k].occupant = cell;// The new cell is added to the TABox
			cell.type=0;// Cell type set randomly to either TA_1,TA_2,TA_3 or TA_4
			tissue.add(cell);// Add new cell to list of cells that constitute the tissue
		}
		maxlineage = sc+1;//stem cells and spaces
		while(sc>0){ // while not enough stem cells allocated
			cell = tissue.get(rand.nextInt(tissue.size())); // Pick a cell at random from tissue list
			if (cell.type!=1){ // If that cell is not already a stem cell type 1 
				lineage++;
				cell.type=1;// Chance to an SC
				cell.lineage = lineage;
				//System.out.println("lineage "+lineage);
				sc--;
			}
		}

	}

	private int bounds(int a,int size) {  // Creates the toroidal links between top and bottom and left and right
		if (a < 0) return size + a;
		if (a >= size) return a - size;
		return a;
	}
	
	public void stain(){ // Stains all cells in the tissue list
		for (TACell c : tissue) {
		    if(c.type>0){
				c.stain=1.0;
			}
	    }
	}

	public void iterate() { // The main iterative loop of the simulation
		//beth: 
        TACell cHold;
		// Create a list to hold cells that are spaces or have the capacity to detach
        ArrayList<TACell> growArray = new ArrayList<TACell>();
        for (TACell c : tissue) { // loop through the tissue (ArrayList of cells)
		    c.maintain(); // Calls each cell to maintain its state re: detach and/or grow
			if(c.type==0)growArray.add(c); // If cell is a space add to grow list
			if(c.canDetach)growArray.add(c);// If cell can detach add to grow list
	    }
        //beth: go through the list and see if anything grows into those spots
		while(growArray.size()>0){ // Randomly loop through the grow list
			cHold=growArray.remove(rand.nextInt(growArray.size()));
			cHold.grow();// Test to see if cell can be replaced by new proliferation
		}
	}
	public void iterateandcount() {
		//beth: 
        TACell cHold;
		// Create a list to hold cells that are spaces or have the capacity to detach
        ArrayList<TACell> growArray = new ArrayList<TACell>();
        for (TACell c : tissue) { // loop through the tissue (ArrayList of cells)
		    c.maintainandcount(); // Calls each cell to maintain its state re: detach and/or grow
			if(c.type==0)growArray.add(c); // If cell is a space add to grow list
			if(c.canDetach)growArray.add(c);// If cell can detach add to grow list
	    }
        //beth: go through the list and see if anything grows into those spots
		while(growArray.size()>0){ // Randomly loop through the grow list
			cHold=growArray.remove(rand.nextInt(growArray.size()));
			cHold.growandcount();// Test to see if cell can be replaced by new proliferation
		}
	}

}
