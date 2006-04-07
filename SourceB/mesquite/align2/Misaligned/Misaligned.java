/* Mesquite source code.  Copyright 1997-2005 W. Maddison and D. Maddison. Version 1.06, September 2005.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.align2.Misaligned; import java.util.*;import java.awt.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;import mesquite.categ.lib.*;/* ======================================================================== */public class Misaligned extends NumberForCharAndTaxon {	int[][] freqs;	long oldID = -1;	long oldStatesVersion = -1;	int distance = 3;	int widthHalf = 2;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName){		return true;	}	long A = CategoricalState.makeSet(0);	long C = CategoricalState.makeSet(1);	long G = CategoricalState.makeSet(2);	long T = CategoricalState.makeSet(3);	public void initialize(CharacterData data,  CommandRecord commandRec){	}	double compareStateToSite(long state, int ic){		int notIn = 0;		if ((A & state) == 0L)			notIn += freqs[ic][0];		if ((C & state) == 0L)			notIn += freqs[ic][1];		if ((G & state) == 0L)			notIn += freqs[ic][2];		if ((T & state) == 0L)			notIn += freqs[ic][3];		int tot = freqs[ic][0] + freqs[ic][1] + freqs[ic][2] + freqs[ic][3];		if (tot == 0)			return 0;		return (notIn*1.0/(tot));	}		//give score of comparison of sequence of states around ic in taxon it to 	double score(DNAData dData, int ic, int it, int offset){		double s = 0;		for (int ic2 = ic-widthHalf; ic2<= ic + widthHalf; ic2++){ //surveying window			if (ic2>=0 && ic2<dData.getNumChars() && ic2+offset>=0 && ic2+offset<dData.getNumChars()){				long state = dData.getState(ic2, it);				s += compareStateToSite(state, ic2+offset);			}		}		return s;			}	public void calculateNumber(CharacterData data, int ic, int it, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec){   	 	if (result == null)   	 		return;   	 	result.setToUnassigned();   	 	if (data == null || !(data instanceof DNAData))   	 		return;   	 	boolean doCalc=false;   	 	DNAData dData = (DNAData)data;   	 	if (freqs == null || freqs.length!=dData.getNumChars()){   	 		freqs = new int[dData.getNumChars()][4];   	 		doCalc = true;   	 	}   	 	   	 	//check version first & only do calculation if versions changed   	 	if (doCalc || dData.getID() != oldID || dData.getStatesVersion()!=oldStatesVersion)   	 		for (int ic2 = 0; ic2 < dData.getNumChars(); ic2++)   	 			for (int it2 = 0; it2 < dData.getNumTaxa(); it2++){   	 				if ((A & dData.getState(ic2, it2)) != 0L)   	 					freqs[ic2][0]++;   	 				if ((C & dData.getState(ic2, it2)) != 0L)   	 					freqs[ic2][1]++;   	 				if ((G & dData.getState(ic2, it2)) != 0L)   	 					freqs[ic2][2]++;   	 				if ((T & dData.getState(ic2, it2)) != 0L)   	 					freqs[ic2][3]++;   	 			}		oldID = dData.getID();		oldStatesVersion = dData.getStatesVersion();		double s = score(dData, ic, it, 0);		double best = s;		for (int offset= -distance; offset<=distance; offset++){			if (ic+offset>=0 && ic+offset<dData.getNumChars()){				double sD = score(dData, ic, it,  offset);				if (sD < s)					best = sD;			}		}		   	 	result.setValue(1.0 - (best - s));   	 }	/*.................................................................................................................*/    	 public String getName() {		return "Misalignment score";   	 }   	 public boolean isPrerelease(){   	 	return true;   	 }	/*.................................................................................................................*/  	 public String getExplanation() {		return "Returns score of whether match is better on adjacent site.";   	 }	public CompatibilityTest getCompatibilityTest(){		return new DNAStateOnlyTest();	}}	