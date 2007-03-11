/* Mesquite source code, Treefarm package.  Copyright 1997-2006 W. Maddison, D. Maddison and P. Midford. Version 1.11, June 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.treefarm.AvgTaxonValueAmgTrees;/*~~  */import java.util.*;import java.awt.*;import mesquite.lib.*;import mesquite.lib.duties.*;import mesquite.treefarm.lib.*;public class AvgTaxonValueAmgTrees extends NForTaxonWithTrees {	MesquiteString numberTaskName;	MesquiteCommand ntC;	MesquiteSubmenuSpec mss;	NForTaxonWithTree numberTask;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {		ntC =makeCommand("setNumberTask",  this);		numberTaskName = new MesquiteString();		if (numberTask == null)			numberTask = (NForTaxonWithTree)hireEmployee(commandRec, NForTaxonWithTree.class, "Value for taxa to average over trees");//shouldn't ask as this is an init and might not be needed.  "Value to calculate for character state in taxon"		if (numberTask != null){			numberTask.setHiringCommand(ntC);			numberTaskName.setValue(numberTask.getName());		}		else 			return false;		if (numModulesAvailable(NForTaxonWithTree.class)>0) {			mss = addSubmenu(null, "Values for Taxa", ntC, NForTaxonWithTree.class);			mss.setSelected(numberTaskName);			mss.setEnabled(false);		}		return true;  	}	/*.................................................................................................................*/	/** returns whether this module is requesting to appear as a primary choice */	public boolean requestPrimaryChoice(){		return true;  	}	/*.................................................................................................................*/	public Snapshot getSnapshot(MesquiteFile file) {		Snapshot temp = new Snapshot();		temp.addLine("setNumberTask ", numberTask);  		temp.incorporate(super.getSnapshot(file), false);		return temp;	}	MesquiteInteger pos = new MesquiteInteger(0);	/*.................................................................................................................*/	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {		if (checker.compare(this.getClass(), "Sets the module that calculates numbers to be averaged among trees", "[name of module]", commandName, "setNumberTask")) {			NForTaxonWithTree temp =  (NForTaxonWithTree)replaceEmployee(commandRec, NForTaxonWithTree.class, arguments, "Module to calculate numbers to be averaged among trees", numberTask);			if (temp!=null) {				numberTask = temp;				numberTask.setHiringCommand(ntC);				numberTaskName.setValue(numberTask.getName());				parametersChanged(null, commandRec);				return numberTask;			}		}		else			return super.doCommand(commandName, arguments, commandRec, checker);		return null;	}	NumberArray results2;	/*.................................................................................................................*/	public void calculateNumbers(Taxa taxa, NumberArray results, MesquiteString resultsString, CommandRecord commandRec){		if (results==null|| taxa == null)			return;		int numTaxa = taxa.getNumTaxa();		results.resetSize(numTaxa);		results.zeroArray();		if (results2 == null)			results2 = new NumberArray();		results2.resetSize(numTaxa);		results2.zeroArray();		int numTrees = getNumTrees(taxa, commandRec);		int[] nums = new int[numTaxa];		double[] sums = new double[numTaxa];		for (int itr = 0; itr< numTrees; itr++){ //get tree for comparison			Tree tree = getTree(taxa, itr, commandRec);			commandRec.tick(getVeryShortName() + ": tree " + (itr+1));			numberTask.calculateNumbers(taxa, tree, results2, null, commandRec);//			commandRec.tick(getVeryShortName() + ": done tree " + (itr+1));			for (int it = 0; it<taxa.getNumTaxa(); it++){				if (!results2.isUnassigned(it)){					nums[it]++;					sums[it] += results2.getDouble(it);				}			}		}		double sum= 0;		int num = 0;		for (int it = 0; it<taxa.getNumTaxa(); it++){			if (nums[it] ==0)				results.setToUnassigned(it);			else {				double avg = sums[it]/nums[it];				sum += avg;				num++;				results.setValue(it, avg);			}		}		if (resultsString != null){			if (num == 0)				resultsString.setValue("Avg. " + getVeryShortName() + ": No results obtained");			else						resultsString.setValue("Avg. " + getVeryShortName() + ": " + (sum/num));		}	}	/*.................................................................................................................*/	public String getVeryShortName() {		if (numberTask != null)			return "Avg. " + numberTask.getVeryShortName();		return "Avg. Among Trees";	}	/*.................................................................................................................*/	public String getName() {		return "Average Taxon Value among Trees";	}	/*.................................................................................................................*/	public String getVersion() {		return null;	}	/*.................................................................................................................*/	public boolean isPrerelease() {		return true;	}	/*.................................................................................................................*/	/** returns an explanation of what the module does.*/	public String getExplanation() {		return "Averages among trees a value calculated for a taxon using a tree.";	}}