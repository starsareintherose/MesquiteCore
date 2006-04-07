/* Mesquite source code.  Copyright 1997-2005 W. Maddison and D. Maddison. Version 1.06, August 2005.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.distance.DistanceContainedTaxa;/*~~  */import java.util.*;import java.awt.*;import mesquite.lib.*;import mesquite.lib.duties.*;import mesquite.distance.lib.*;import mesquite.assoc.lib.*;/* ======================================================================== */public class DistanceContainedTaxa extends IncTaxaDistanceSource implements Incrementable {	Taxa containedTaxa = null;	AssociationSource associationTask;	TaxaAssociation association;	TaxaDistanceSource distanceTask;	Class hiredAs = null;	ListableVector choices;	int mode = ContainedDistances.CLOSEST;	StringArray modes;	MesquiteString modeName;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {		associationTask = (AssociationSource)hireEmployee(commandRec, AssociationSource.class, "Source of taxon associations");		if (associationTask == null)			return sorry(commandRec, getName() + " couldn't start because no source of taxon associations obtained."); 		hiredAs = getHiredAs(); 		distanceTask = (TaxaDistanceSource)hireEmployee(commandRec, hiredAs, "Distance between contained taxa"); 		if (distanceTask == null) { 			return sorry(commandRec, getName() + " couldn't start because a contained taxa distance calculator was not obtained."); 		} 		MesquiteSubmenuSpec modeSubmenu = addSubmenu(null, "Contained Distance Calculation");		modes = new StringArray(2);		modes.setValue(ContainedDistances.CLOSEST, "Closest");		modes.setValue(ContainedDistances.ARITHMETIC, "Arithmetic Mean");		//modes.setValue(ContainedDistances.GEOMETRIC, "Geometric Mean");  not yet turned on		modeName = new MesquiteString(modes.getValue(mode));		MesquiteSubmenuSpec mss = addSubmenu(null, "Contained Distance Calculation", makeCommand("setMode",  this), modes);		mss.setSelected(modeName); 		if (!commandRec.scripting()){ 			int tempMode = ListDialog.queryList(containerOfModule(), "Contained Distance Calculation", "Counting distances among contained taxa", null, modes.getStrings(), mode); 			if (MesquiteInteger.isCombinable(tempMode)) { 				mode = tempMode; 				logln("Use mode: " + modes.getValue(mode)); 			} 		} 		 		return true;  	 }  	    	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/   	public void initialize(Taxa taxa, CommandRecord commandRec){   		if (taxa ==null)   			return;        	if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {        		association = associationTask.getCurrentAssociation(taxa, commandRec);         		if (association.getTaxa(0)== taxa)        			containedTaxa = association.getTaxa(1);        		else        			containedTaxa = association.getTaxa(0);        	}        	distanceTask.initialize(containedTaxa, commandRec);   	}	public TaxaDistance getTaxaDistance(Taxa taxa, CommandRecord commandRec){        	//initialize(taxa, commandRec);        	if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {        		association = associationTask.getCurrentAssociation(taxa, commandRec);         		if (association.getTaxa(0)== taxa)        			containedTaxa = association.getTaxa(1);        		else        			containedTaxa = association.getTaxa(0);        	} 		TaxaDistance dist = distanceTask.getTaxaDistance(containedTaxa, commandRec); //get distances among contained taxa 				ContainedDistances cc = new ContainedDistances(this, taxa, containedTaxa, dist, association, mode);		return cc;	}	/*.................................................................................................................*/  	 public Snapshot getSnapshot(MesquiteFile file) {    	 	Snapshot temp = new Snapshot();		temp.addLine("setDistanceSource ", distanceTask);   	 	temp.addLine("setMode " + mode);  	 	return temp;  	 }	/*.................................................................................................................*/    	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {    	 	 if (checker.compare(this.getClass(), "Sets the source of distances for use in " + getName(), "[name of module]", commandName, "setDistanceSource")) {     	 		TaxaDistanceSource temp=  (TaxaDistanceSource)replaceEmployee(commandRec, hiredAs, arguments, "Source of distance for among contained taxa", distanceTask); 			if (temp!=null) { 				distanceTask= temp; 				if (!commandRec.scripting()) 					parametersChanged(null, commandRec);  			} 			return distanceTask; 		}    	 	else if (checker.compare(this.getClass(), "Sets how contained distances are counted", "[0 = use closest distance among contained taxa, " +    	 				"1 = use arithmetic mean of distances among contained taxa, 2 = use geometric mean of distances among contained taxa]", commandName, "setMode")) {			String name = parser.getFirstToken(arguments);			int newMode = modes.indexOf(name);			if (newMode <0)				newMode = MesquiteInteger.fromString(name);			if (newMode >=0 && newMode!=mode){				mode = newMode;	    			modeName.setValue(modes.getValue(mode));				logln("Mode of DistanceContainedTaxa set to " + modeName);		    	 		if (!commandRec.scripting())	    	 			parametersChanged(null, commandRec);    	 		}    	 	}    	 	else    	 		return super.doCommand(commandName, arguments, commandRec, checker);    	 	return null;	}	/*.................................................................................................................*/ 	public void setCurrent(long i, CommandRecord commandRec){  //SHOULD NOT notify (e.g., parametersChanged) 		if (distanceTask instanceof Incrementable) 			((Incrementable)distanceTask).setCurrent(i, commandRec); 	} 	public long getCurrent(CommandRecord commandRec){ 		if (distanceTask instanceof Incrementable) 			return ((Incrementable)distanceTask).getCurrent(commandRec); 		return 0; 	} 	public String getItemTypeName(){ 		if (distanceTask instanceof Incrementable) 			return ((Incrementable)distanceTask).getItemTypeName(); 		return ""; 	} 	public long getMin(CommandRecord commandRec){ 		if (distanceTask instanceof Incrementable) 			return ((Incrementable)distanceTask).getMin(commandRec);		return 0; 	} 	public long getMax(CommandRecord commandRec){ 		if (distanceTask instanceof Incrementable) 			return ((Incrementable)distanceTask).getMax(commandRec);		return 0; 	} 	public long toInternal(long i){ 		if (distanceTask instanceof Incrementable) 			return ((Incrementable)distanceTask).toInternal(i); 		return i-1; 	} 	public long toExternal(long i){ //return whether 0 based or 1 based counting 		if (distanceTask instanceof Incrementable) 			return ((Incrementable)distanceTask).toExternal(i); 		return i+1; 	}	/*.................................................................................................................*/   	public String getParameters() {		return "Distances of contained from " +  distanceTask.getName() + "; mode: " + modes.getValue(mode);   	}	/*.................................................................................................................*/    	 public String getName() {		return "Distance of Contained Taxa";     	 }   	 	/*.................................................................................................................*/   	  	/** returns an explanation of what the module does.*/ 	public String getExplanation() { 		return "Distances among taxa according to distances among contained taxa (e.g., genes)." ;   	 }   	    	 public boolean isPrerelease(){   	 	return false;   	 }}class ContainedDistances extends TaxaDistance {	double[][] distances;	int numTaxa;	static final int CLOSEST = 0;	static final int ARITHMETIC = 1;	static final int GEOMETRIC = 2;	DistanceContainedTaxa ownerModule;	public ContainedDistances(DistanceContainedTaxa ownerModule, Taxa taxa, Taxa containedTaxa, TaxaDistance containedDistances, TaxaAssociation association, int mode){		super(taxa);		this.ownerModule = ownerModule;		numTaxa = taxa.getNumTaxa();		distances = new double[numTaxa][numTaxa];		Double2DArray.deassignArray(distances);				for (int i=0; i<numTaxa; i++){			for (int j=0; j<i; j++){  //for taxa i and j, find contained taxa			 	Taxon[] containedI = association.getAssociates(taxa.getTaxon(i));			 	Taxon[] containedJ = association.getAssociates(taxa.getTaxon(j));						 		double d = 0;		 		if (ownerModule.mode == CLOSEST)		 			d = MesquiteDouble.unassigned;		 		else if (ownerModule.mode == GEOMETRIC) //not yet used!		 			d = 1.0;		 					 		int count = 0;			 	if (containedI!=null && containedJ !=null) {			 		for (int cI = 0; cI<containedI.length; cI++)  //survey distances among contained taxa				 		for (int cJ = 0; cJ<containedJ.length; cJ++) {				 			int taxI = containedTaxa.whichTaxonNumber(containedI[cI]);				 			int taxJ = containedTaxa.whichTaxonNumber(containedJ[cJ]);				 			double coal =containedDistances.getDistance(taxI, taxJ);				 			if (ownerModule.mode == CLOSEST){					 			if (MesquiteDouble.lessThan(coal, d, 0))  //d records closest distance among contained					 				d = coal;				 			}				 			else if (ownerModule.mode == GEOMETRIC){ //here we are just counting number of valid distances				 				if (MesquiteDouble.isCombinable(coal)) {				 					d *= coal;				 					count++;				 				}				 			}				 			else{				 				if (MesquiteDouble.isCombinable(coal)){ //d is sum of distances among contained				 					d += coal;				 					count++;				 				}				 			}				 		}			 	}			 	else {			 		MesquiteMessage.warnProgrammer("WARNING: Associates (contained taxa) null");			 	}	 			if (ownerModule.mode == GEOMETRIC){ //have product; calculate geometric mean //not yet used!				 	if (count>0 && containedI!=null && containedJ !=null) {				 		d = Math.pow(d, 1.0/count);				 	}	 			}	 			else if (ownerModule.mode == ARITHMETIC){  //have sum and number; calculate mean	 				if (MesquiteDouble.isCombinable(d) && count >0){	 					d  /= count;	 				}	 			}			 	distances[i][j] = d;			 				}					}		for (int i=0; i<numTaxa; i++){			for (int j=i; j<numTaxa; j++){			 	distances[i][j]=distances[j][i];			}					}	}	public double getDistance(int taxon1, int taxon2){		if (taxon1>=0 && taxon1<numTaxa && taxon2>=0 && taxon2<numTaxa)			return distances[taxon1][taxon2];		else			return MesquiteDouble.unassigned;			}	public double[][] getMatrix(){		return distances;	}	public boolean isSymmetrical(){		return true;	}}