/*
 * This contains the classes for the Cell object for the TA version of the model
 */

import java.util.*;

class TACell{
	public static int maxCycle = 5; // this sets the TAmax to 4 (see type)
	public static Random rand = new Random();
	static Integer[] neighbours = {0,1,2,3,4,5,6,7};

	public boolean canDetach, canGrow,proliferated;
	public int type; // 0 = space, 1 = SC, 2=TA_1....5=TA_4
	public TABoxStatic home;// The box the cell sits in
	public double stain;
	public double scRate=1.0;// Relative SC proliferation rate if scRate = 0.5 SC proliferation rate would be half TA rate
	public int lineage;
	
	public static int[] cellcounts=new int[maxCycle+1];
	public static int[] prolifcounts=new int[maxCycle+1];
	public static double[] stainsums=new double[maxCycle+1];
	public static int totalproliferations=0;
	
	public TACell(TABoxStatic home,int lin){
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
	
	public void maintain(){// Determines if a Cell can detach or grow and sets counters
		//beth: sets which counters?
		canDetach=(type>=maxCycle);// For standard TA model only TA_4 can detach
		canGrow = ((type>0)&&(type<maxCycle));// For standard TA model only TA_4 can't grow
		if(type==1)canGrow=(rand.nextDouble()<scRate);// Rate can be different for SC (see above)
		proliferated = false;
	}
	
	public void maintainandcount(){// Determines if a Cell can detach or grow and sets counters
		cellcounts[type]++;
		stainsums[type] = stainsums[type] + stain;
		canDetach=(type>=maxCycle);// For standard TA model only TA_4 can detach
		canGrow = ((type>0)&&(type<maxCycle));// For standard TA model only TA_4 can't grow
		if(type==1)canGrow=(rand.nextDouble()<scRate);// Rate can be different for SC (see above)
		proliferated = false;
	}
	
	public void growth(TACell cHold){ // Growth occurs into cell. chold is the neighbour that is taking over
			type=cHold.type+1; // Takes on type+1 of cell that is proliferating if SC type = 1+1 if TA_3 type = 3+1 
			if(cHold.type>1)cHold.type++; // If proliferating cell is not an SC it to increases its type
			canGrow=false;// New cell will not proliferate again in this iteration
			cHold.canGrow=false;// Proliferating cell will not proliferate again in this iteration
			cHold.proliferated=true;// The proliferating cell has proliferated
			cHold.stain = cHold.stain/2.0;// Divide the label resting cell between the two cells
			stain = cHold.stain;// As above
			lineage = cHold.lineage;// New cell takes on lineage of proliferating cell 
	}
	public void growthandcount(TACell cHold){
		totalproliferations++;
		prolifcounts[cHold.type]++;
		type=cHold.type+1; // Takes on type+1 of cell that is proliferating if SC type = 1+1 if TA_3 type = 3+1 
		if(cHold.type>1)cHold.type++; // If proliferating cell is not an SC it to increases its type
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
		TACell cHold;
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
		TACell cHold;
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
		TACell cHold;
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
