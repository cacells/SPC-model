/*
 * This is the Class that call the TAGrid class and runs simulations as a batch
 * for the TA model that looks a just the lineage
 */

import java.io.*;



	
//beth
	
	/*
	 * This is a class that contains main and
	 * will call the TAGrid simulation, also displaying the results 
	 * Graphically in a window
	 * TA is for Transit Amplifying
	 */


	import java.awt.*;
	import java.awt.image.BufferedImage;
	import java.io.BufferedWriter;
	import java.io.File;
	import java.io.FileWriter;
	import java.io.IOException;

	import javax.imageio.ImageIO;
	import javax.swing.*;

	import java.util.*;
	 
	public class TABatchLin extends JFrame implements Runnable {
		

		int celltypes = 6;
		int replicates =20; // number of replicates for each run
	    int maxIters = 100; // number of experiment iterations per replicate
	    //beth just adding a comment to this TABatchLin method
		
		int lin=64*64;//max number of different cell lines equals cells in grid
		double dlin = (double) lin;

		double lineageSum[] = new double[maxIters];
		double lineageSqrSum[] = new double[maxIters];
		
		
		double frac; // calculate fraction of SC

		static String fileprefix = "test";

	    TAGridStatic experiment;
	    Random rand = new Random();    
		Thread runner;
	    Container mainWindow;
		CAImagePanel CApicture;
		Image backImg1;
		Graphics backGr1;
		Image backImg2;
		Graphics backGr2;
		JProgressBar progressBarIt,progressBarRep;
		JTextField progressMsgIt,progressMsgRep,fracMsg,fracVal;
		JPanel buttonHolderlow;
		
		int scale = 20;//beth: could set to 1. Makes the colour transitions better?
		int border = 20;
		int iterations;
		int gSize;
	    Colour palette = new Colour();
		int[] colorindices = {45,1,5,4,2,54};//45 is orange

//	    Color[] colours = {Color.white,Color.black,Color.green,Color.blue,Color.yellow,Color.red,Color.pink};
	    Color[] javaColours;
	    double[][] epsColours;
	    boolean writeImages = true;

		public TABatchLin(int size) {
			//if size ne 64 we are in trouble
		    gSize=size;
			setVisible(true);
			backImg1 = createImage(scale * size, scale * size);
			backGr1 = backImg1.getGraphics();
			backImg2 = createImage(scale * size, scale * size);
			backGr2 = backImg2.getGraphics();
			setpalette();
			
		    int wscale = 6;//scale for main panel
		    int btnHeight = 480-384;//found by trial and error - must be a better way!
		    //although no buttons yet
		    int wh = (gSize*1)*wscale + 2*border;// +btnHeight;//mainWindow height
		    int ww = (gSize*2)*wscale + 3*border;//mainWindow width   
		    
			mainWindow = getContentPane();
			mainWindow.setLayout(new BorderLayout());
			setSize(ww,wh+btnHeight);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setVisible(true);
	        CApicture = new CAImagePanel(ww,wh);
	        CApicture.setBorder(border);
	        CApicture.rowstoShow = gSize;
	        mainWindow.add(CApicture,BorderLayout.CENTER);
			setVisible(true);
			
			buttonHolderlow = new JPanel();
			buttonHolderlow.setLayout(new GridLayout(2,3,10,1));
			
		    
		    progressBarIt = new JProgressBar(0,maxIters);
		    progressBarIt.setValue(0);
		    progressBarIt.setStringPainted(true);   
		    
		    progressBarRep = new JProgressBar(0,replicates);
		    progressBarRep.setValue(0);
		    progressBarRep.setStringPainted(true);
		    
	        progressMsgIt = new JTextField("Iterations % of "+maxIters);
	        progressMsgIt.setEditable(false);
		    
		    
	        progressMsgRep = new JTextField("Replicates % of "+replicates);
	        progressMsgRep.setEditable(false);
		    
	        fracMsg = new JTextField("SC fraction");
	        fracMsg.setEditable(false);

	        
	        frac = 1.0/(double)maxIters;
	        fracVal = new JTextField(""+frac);
	        fracVal.setEditable(false);

		    
	        progressMsgRep = new JTextField("Replicates finished out of "+replicates);
	        progressMsgRep.setEditable(false);
	        
		    buttonHolderlow.add(progressMsgIt);
		    buttonHolderlow.add(progressMsgRep);
		    buttonHolderlow.add(fracMsg);

		    buttonHolderlow.add(progressBarIt);
		    buttonHolderlow.add(progressBarRep);
		    buttonHolderlow.add(fracVal);
		    
	        fracMsg.setVisible(false);
	        fracVal.setVisible(false);
		    
		    mainWindow.add(buttonHolderlow, BorderLayout.SOUTH);
			setVisible(true);
			
			
		}
		//new ones
		public void drawCA() {
			int a;
			for (TACell c : experiment.tissue){
				a = c.type;
				CApicture.drawCircleAt(c.home.x, c.home.y, javaColours[a], 1);
			}
		    CApicture.updateGraphic();
		}
		public void drawCAstain() {
			double cstain;
			for (TACell c : experiment.tissue){
				//if ((c.stain < minstain) && (c.type>0)) minstain = c.stain;
				cstain = c.stain;
				if (c.type==1) 
					CApicture.drawCircleAt2(c.home.x, c.home.y, palette.Javashades(cstain), 2);
				else 
					CApicture.drawCircleAt(c.home.x, c.home.y, palette.Javashades(cstain), 2);
			}
		    //outputImage();
		    CApicture.updateGraphic();
		}
		public void drawCALineage() {
			double celllin;
			for (TACell c : experiment.tissue){
				//if ((c.stain < minstain) && (c.type>0)) minstain = c.stain;
				if (c.type==0){
					CApicture.drawCircleAt(c.home.x, c.home.y, javaColours[0], 2);
				}
				else{
				celllin = (double)c.lineage/(dlin+1.0);
				if (c.type==1) 
					CApicture.drawCircleAt2(c.home.x, c.home.y, palette.Javagrey(celllin), 2);
				else 
					CApicture.drawCircleAt(c.home.x, c.home.y, palette.Javagrey(celllin), 2);
				}
			}
		    //outputImage();
		    CApicture.updateGraphic();
		}

		public void initialise(){
				CApicture.setScale(gSize,gSize,scale,gSize,gSize,scale);
	      	    CApicture.clearCAPanel(1);
	      	    CApicture.clearCAPanel(2);
	      	    CApicture.clearParent();
			    iterations=0;
		}
		
		
		public void start() {
			initialise();
			if (runner == null) {
				runner = new Thread(this);
			}
			runner.start();
		}


		public void run() {
			int countLineage;
			int countClone;
			double avLineage;

			int lineage[];


			if (runner == Thread.currentThread()) {
				int exp = 1;//cludgy way of defining SC fraction
				frac = (double)(exp+1)/(double)(maxIters); // calculate fraction of SC

					System.out.print(exp);
			        fracMsg.setVisible(true);
			        fracVal.setVisible(true);
					fracVal.setText(""+frac);
					for(int r=0; r<replicates; r++){
						progressBarRep.setValue(r+1);
						experiment = new TAGridStatic(64, celltypes-2, frac,false);//new experiment
						lin = TAGridStatic.maxlineage;
						dlin = (double) lin;
						
						if (r==0){
						drawCA();
						drawCALineage();
						if (writeImages) CApicture.writeImage(0);
						}

						progressBarIt.setValue(0);

						for(iterations=0; iterations<(maxIters); iterations++){
							progressBarIt.setValue(iterations+1);
							if(iterations==89)experiment.stain();// stain all cells 10 iterations before end
							lineage = new int[lin];
							countLineage =0;
							countClone =0;
							experiment.iterate();
							//cell count for each each cell line
							for (TACell c : experiment.tissue){
								lineage[c.lineage]++;
							}
							//lineage[0] is the spaces so not j=0
							for(int j=1; j<lin; j++){
								//countClone counts number of different cell lines (clones)
								if(lineage[j]>0)countClone++;
								//countLineage would be cell count
								countLineage+=lineage[j];
							}
							//total cell count div by number of clones
							avLineage = (double)countLineage/(countClone*1.0);
							//sum the average cells per clone at this iteration for all replicates
							lineageSum[iterations]+=avLineage;
							//and its square at this iteration for all replicates
							lineageSqrSum[iterations]+=(avLineage*avLineage);
							if (r==0){
							drawCA();
							drawCALineage();
							if (writeImages) CApicture.writeImage(iterations+1);
							}
						}
						//setOfIterations(exp,r); 
						System.out.print("["+r+"]");

					}


				outputDataLin();
			}
		}
		public void outputDataLin(){
			try{
				BufferedWriter bufLineage = new BufferedWriter(new FileWriter(fileprefix+"Lineage.txt"));
				double mean;
				bufLineage.write("iteration Clones stdev");
				bufLineage.newLine();
				for(int i=0; i< 100; i++){
					mean = getMean(lineageSum[i]);
					bufLineage.write(i+" "+mean+" "+getStandardDeviationGvnMean(mean, lineageSqrSum[i])+" ");
					System.out.println(i+" "+mean+" "+getStandardDeviationGvnMean(mean, lineageSqrSum[i])+" ");
					bufLineage.newLine();	
				}
				bufLineage.newLine();
				bufLineage.newLine();
				bufLineage.close();
			}catch(IOException e){
			}
		}

		public void setOfIterations(int exp,int rep){

			int countLineage;
			int countClone;
			double avLineage;

			int lineage[];

			frac = (double)(exp+1)/(double)(maxIters); // calculate fraction of SC

			experiment = new TAGridStatic(64, celltypes-2, frac,false);//new experiment

			progressBarIt.setValue(0);
			for(iterations=0; iterations<(maxIters); iterations++){
				progressBarIt.setValue(iterations+1);
				if(iterations==89)experiment.stain();// stain all cells 10 iterations before end
				lineage = new int[lin];
				countLineage =0;
				countClone =0;
				experiment.iterate();
				//cell count for each each cell line
				for (TACell c : experiment.tissue){
					lineage[c.lineage]++;
				}
				//lineage[0] is the spaces so not j=0
				for(int j=1; j<lin; j++){
					//countClone counts number of different cell lines (clones)
					if(lineage[j]>0)countClone++;
					//countLineage would be cell count
					countLineage+=lineage[j];
				}
				//total cell count div by number of clones
				avLineage = (double)countLineage/(countClone*1.0);
				//sum the average cells per clone at this iteration for all replicates
				lineageSum[iterations]+=avLineage;
				//and its square at this iteration for all replicates
				lineageSqrSum[iterations]+=(avLineage*avLineage);
			}
			
			
		}


	    public double getMean(double sum) {
			 return sum / replicates;
		}
		
		
		public double getStandardDeviationGvnMean(double mean, double sqrSum) {
				double dev = (sqrSum / replicates) - (mean * mean);
				if(dev>0){
		       	return Math.sqrt(dev);
				}
				return 0.0;
		   }
		

		
	    public void setpalette(){
	    	int ind = colorindices.length;
	    	javaColours = new Color[ind];
	    	epsColours = new double[ind][3];
	    	for (int i=0;i<ind;i++){
	    		//System.out.println("color index "+colorindices[i]);
	    		javaColours[i] = palette.chooseJavaColour(colorindices[i]);
	    		epsColours[i] = palette.chooseEPSColour(colorindices[i]);
	    	}
	    }

		public static void main(String args[]) {
			if(args.length>0){
				fileprefix=args[0];
				TABatchLin s = new TABatchLin(64);
				s.start();
			}else{
				TABatchLin s = new TABatchLin(64);
				s.start();
			}
		}
	}


	
//beth

	
		



	

