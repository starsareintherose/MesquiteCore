/* Mesquite source code.  Copyright 1997-2005 W. Maddison and D. Maddison. Version 1.06, September 2005.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.wayne.ListSpectrum; import java.util.*;import java.awt.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;import mesquite.categ.lib.*;/* ======================================================================== */public class ListSpectrum extends DataWindowAssistantI {	long A = CategoricalState.makeSet(0);	long C = CategoricalState.makeSet(1);	long G = CategoricalState.makeSet(2);	long T = CategoricalState.makeSet(3);		DNAData dData;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName){		addMenuItem("Show Spectrum", makeCommand("showSpectrum", this));		return true;	}	public void setActiveColors(boolean active, CommandRecord commandRec){	}	/*.................................................................................................................*/   	 public boolean isSubstantive(){   	 	return false;   	 }	/*.................................................................................................................*/   	public void viewChanged(CommandRecord commandRec){   	}	/*.................................................................................................................*/	public void setTableAndData(MesquiteTable table, CharacterData data, CommandRecord commandRec){		dData = (DNAData)data;	}		/*.................................................................................................................*/    	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {    	 	if (checker.compare(this.getClass(),  "Show spectrum", null, commandName, "showSpectrum")) {			long[] spectrum = new long[dData.getNumTaxa()];//	Debugg.println("stad===================");			spectrum[0] = A;			countSpectrum(spectrum, "A", 1, dData.getNumTaxa(), dData);			spectrum[0] = C;			countSpectrum(spectrum, "C", 1, dData.getNumTaxa(), dData);			spectrum[0] = G;			countSpectrum(spectrum, "G", 1, dData.getNumTaxa(), dData);			spectrum[0] = T;			countSpectrum(spectrum, "T", 1, dData.getNumTaxa(), dData);    	 	}    	 	else super.doCommand(commandName, arguments, commandRec, checker);    	 	return null;	}	private boolean patternMatches(long[] spectrum, DNAData dData, int ic){		for (int it = 0; it<dData.getNumTaxa(); it++)			if (spectrum[it] != dData.getState(ic, it))				return false;		return true;	}	private void countSpectrum(long[] spectrum, String seqString, int it, int total, DNAData dData){//Debugg.println("seq " + seqString + "  " + it + "  " + total);		if (it == total ){			int count = 0;//Debugg.println("COUNT");			for (int ic = 0; ic < dData.getNumChars(); ic++){				if (patternMatches(spectrum, dData, ic))					count++;			}			if (count>0)				Debugg.println(Integer.toString(count) + "  " + seqString);		}		else {			spectrum[it] = A;			countSpectrum(spectrum, seqString + "A", it+1, total, dData);			spectrum[it] = C;			countSpectrum(spectrum, seqString + "C", it+1, total, dData);			spectrum[it] = G;			countSpectrum(spectrum, seqString + "G", it+1, total, dData);			spectrum[it] = T;			countSpectrum(spectrum, seqString + "T", it+1, total, dData);		}			}	/*.................................................................................................................*/    	 public String getName() {		return "List Spectrum";   	 }	/*.................................................................................................................*/  	 public String getExplanation() {		return ".";   	 }	/*.................................................................................................................*/ 	/*.................................................................................................................*/	public String getCellString(int ic, int it){		return null;	}		/*.................................................................................................................*/    	 public boolean isPrerelease() {		return true;   	 }	public CompatibilityTest getCompatibilityTest(){		return new DNAStateOnlyTest();	}}