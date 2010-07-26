/*
 * This contains the classes for the Cell object for the SPC version of the model
 */

import java.util.*;

class SPCCell{
	public static int maxCycle = 2; // number of different cell types
	public static Random rand = new Random();
	public static double TAFraction = 1.0 - 2.0*0.08;// The probability of a producing EP and PMB
	static Integer[] neighbours = {0,1,2,3,4,5,6,7};

	public boolean canDetach, canGrow,proliferated;
	public int type; // 0 = space, 1 = EP, 2=PMB
	public SPCBoxStatic home;// The box the cell sits in
	public double stain;
	public double scRate=1.0;// Relative SC proliferation rate if scRate = 0.5 SC proliferation rate would be half SPC rate
	public int lineage;
	
	public static int[] cellcounts=new int[maxCycle+1];
	public static int[] prolifcounts=new int[maxCycle+1];
	public static double[] stainsums=new double[maxCycle+1];
	public static int totalproliferations=0;
	
	public SPCCell(SPCBoxStatic home,int lin){
		this.home=home;
		lineage = lin;
		canDetach=false;
		canGrow=false;
		proliferated=false;
		stain = 0.0;
	}
	
	public static void resetstaticcounters(){
		for (int i=0;i<maxCycle+1;i++){
			cellcounts[i]=0;
			prolifcounts[i]=0;
			stainsums[i]=0.0;
			totalproliferations = 0;
		}
	}
	
	public void maintain(){// Determines if a Cell can detach or grow 
		canDetach=(type==maxCycle);// For standard SPC model only PMB can detach
		canGrow = (type==1);// For standard SPC model only EP can grow
		proliferated = false;
	}
	
	public void maintainandcount(){// Determines if a Cell can detach or grow and counts
		cellcounts[type]++;
		stainsums[type] = stainsums[type] + stain;
		canDetach=(type==maxCycle);// For standard SPC model only PMB can detach
		canGrow = (type==1);// For standard SPC model only EP can grow
		proliferated = false;
	}
	
	public void growth(SPCCell cHold){ // Growth occurs into cell. chold is the neighbour that is taking over
		if(cHold.type==1){// only SC can proliferate
			if(rand.nextDouble()<TAFraction){ // The progenitor cell will be a PMB and parent stays as EP
				type=2;
			}else{ // if not
				if(rand.nextDouble()<0.5){ // then there is an equal probability that the cell will be
					type=1; //another EP
				}else{
					type=2;  // or that both cells will be PMB
					cHold.type=2;
				}
			}
		}		
			canGrow=false;// New cell will not proliferate again in this iteration
			cHold.canGrow=false;// Proliferating cell will not proliferate again in this iteration
			cHold.proliferated=true;// The proliferating cell has proliferated
			cHold.stain = cHold.stain/2.0;// Divide the label resting cell between the two cells
			stain = cHold.stain;// As above
			lineage = cHold.lineage;// New cell takes on lineage of proliferating cell 
	}
	public void growthandcount(SPCCell cHold){
		totalproliferations++;
		prolifcounts[cHold.type]++;
		if(cHold.type==1){// only SC can proliferate
			if(rand.nextDouble()<TAFraction){ // The progenitor cell will be a PMB and parent stays as EP
				type=2;
			}else{ // if not
				if(rand.nextDouble()<0.5){ // then there is an equal probability that the cell will be
					type=1; //another EP
				}else{
					type=2;  // or that both cells will be PMB
					cHold.type=2;
				}
			}
		}
		
		canGrow=false;// New cell will not proliferate again in this iteration
		cHold.canGrow=false;// Proliferating cell will not proliferate again in this iteration
		cHold.proliferated=true;// The proliferating cell has proliferated
		cHold.stain = cHold.stain/2.0;// Divide the label resting cell between the two cells
		stain = cHold.stain;// As above
		lineage = cHold.lineage;// New cell takes on lineage of proliferating cell 
	}
	
	public boolean oldgrow(){// old version of grow
		int sizeA = home.neighbours.size();
        int a = rand.nextInt(sizeA);// Pick starting point in list of neighbours
        int b;
		SPCCell cHold;
        for(int i=0;i<sizeA;i++){ // Loop from starting point through list of neighbours
			b = (rand.nextInt(sizeA)+a)%sizeA;//beth: not convinced this gets all neighbours
			cHold = home.getNeighbour(b);
			if(cHold.canGrow){ // If neighbour can proliferate
				growth(cHold);// proliferate
				return true;// and stop search
			}
        }
		return false;// Return false if no proliferating cell can be found 
	}	
	public boolean grow(){//new version of grow
		int sizeA = 8;//always 8 neighbours - otherwise this method needs changing
		ArrayList<Integer> nlist = new ArrayList<Integer>(Arrays.asList(neighbours));//initialise nlist
        int a,b;
		SPCCell cHold;
        for(int i=0;i<sizeA;i++){ // Loop from starting point through list of neighbours
        	a = rand.nextInt(nlist.size());//pick random list index
        	b = nlist.remove(a);//use the value at that index and make the list smaller
			cHold = home.getNeighbour(b);
			if(cHold.canGrow){ // If neighbour can proliferate
				growth(cHold);// proliferate
				return true;// and stop search
			}
        }
		return false;// Return false if no proliferating cell can be found 
	}
	public boolean growandcount(){
		int sizeA = 8;//always 8 neighbours - otherwise this method needs changing
		ArrayList<Integer> nlist = new ArrayList<Integer>(Arrays.asList(neighbours));//initialise nlist
        int a,b;
		SPCCell cHold;
        for(int i=0;i<sizeA;i++){ // Loop from starting point through list of neighbours
        	a = rand.nextInt(nlist.size());//pick random list index
        	b = nlist.remove(a);//use the value at that index and make the list smaller
			cHold = home.getNeighbour(b);
			if(cHold.canGrow){ // If neighbour can proliferate
				growthandcount(cHold);// proliferate
				return true;// and stop search
			}
        }
		return false;// Return false if no proliferating cell can be found 
	}
}
