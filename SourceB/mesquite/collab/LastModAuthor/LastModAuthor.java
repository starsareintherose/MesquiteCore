/* Mesquite source code.  Copyright 1997-2002 W. Maddison & D. Maddison. Version 0.992.  September 2002.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.collab.LastModAuthor; import java.applet.*;import java.util.*;import java.awt.*;import java.text.Collator;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;/*   to do:	- deal with Option cursor, Shift cursor	- pass taxaAreRows into this*//** ======================================================================== */public class LastModAuthor extends DataWindowAssistantI implements CellColorer {	CharacterData data;	MesquiteTable table;	Color[] colors;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName){		colors = new Color[]{		ColorDistribution.lightGreen,		ColorDistribution.straw,		ColorDistribution.lightBlue,		Color.orange,		Color.cyan,		ColorDistribution.lightRed,		Color.magenta,		ColorDistribution.sienna,		Color.green,		Color.red,		Color.yellow,		Color.pink,		Color.blue,		Color.gray};				return true;	}   	 public void setActiveColors(boolean active, CommandRecord commandRec){   	 }   	 public void viewChanged(CommandRecord commandRec){   	 }	/*.................................................................................................................*/   	 public boolean isSubstantive(){   	 	return true;   	 }   	 public boolean isPrerelease(){   	 	return true;   	 }	public void setTableAndData(MesquiteTable table, CharacterData data, CommandRecord commandRec){		this.table = table;		this.data = data;	}	/*.................................................................................................................*/   	public boolean hasDisplayModifications(){   		return false;   	}   	ColorRecord[] legend;   		NameReference historyNameRef = NameReference.getNameReference("ChangeHistory");   	public ColorRecord[] getLegendColors(CommandRecord commandRec){   		ListableVector authors = getProject().getAuthors();		ColorRecord[] legend = new ColorRecord[authors.size()+1];		for (int i=0; i<legend.length; i++) //MAKE SURE enough colors			legend[i] = new ColorRecord(colors[i], authors.nameOfElementAt(i));		legend[legend.length-1] = new ColorRecord(Color.white, "Not Recorded");   		return legend;   	}   	   	public String getColorsExplanation(CommandRecord commandRec){   		return null;   	}   	private Author getAuthor(int ic, int it){   		if (data == null)   			return null;   		ChangeHistory ch = (ChangeHistory)data.getCellObject(historyNameRef, ic, it);   		if (ch == null)   			return null;   		ChangeEvent ce = ch.getLastEvent();   		if (ce == null)   			return null;   		return ce.getAuthor();   	}   	public String getCellString(int ic, int it){   		Author a = getAuthor(ic, it);   		if (a == null)   			return "Last Modifying Author: Not Recorded";   		return "Last Modifying Author: " + a.getName();   	}   	   	public Color getCellColor(int ic, int it){   		ListableVector authors = getProject().getAuthors();   		Author author = getAuthor(ic, it);   		if (author == null)   			return Color.white;   		return colors[authors.indexOf(author)];   	}	/*.................................................................................................................*/    	 public String getName() {		return "Last Modified Author";   	 }	/*.................................................................................................................*/    	 public String getNameForMenuItem() {		return "*Last Modified Author";   	 }	/*.................................................................................................................*/  	 public String getVersion() {		return null;   	 }   	 	/*.................................................................................................................*/  	 public String getExplanation() {		return "Shows who last modified each cell.";   	 }}