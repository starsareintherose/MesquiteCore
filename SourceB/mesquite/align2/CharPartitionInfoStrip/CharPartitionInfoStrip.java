/* Mesquite (package mesquite.lists).  Copyright 1997-2005 W. Maddison and D. Maddison. Version 1.06, September 2005.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.align2.CharPartitionInfoStrip;/*~~  */import java.awt.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;import mesquite.lists.lib.*;/* ======================================================================== */public class CharPartitionInfoStrip extends DataColumnNamesAssistant {	MesquiteTable table=null;	MesquiteBoolean showString = new MesquiteBoolean(false);	CharacterPartition partition=null;	int element = -1;  	 MesquiteInteger pos = new MesquiteInteger(0);	MesquiteSubmenuSpec mss, mEGC, mEGN;	MesquiteMenuItemSpec mStc, mLine, nNG, mLine2, mLine3, mss2, deleteInfoStripM;		/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {		setUseMenubar(false);				return true;  	 }	/*.................................................................................................................*/ 	public void endJob() {		if (table!=null) {			((ColumnNamesPanel)table.getColumnNamesPanel()).decrementInfoStrips();			table.resetTableSize(false);		} 		super.endJob();   	 }	/*.................................................................................................................*/	public void setTableAndData(MesquiteTable table, CharacterData data, CommandRecord commandRec){		deleteMenuItem(mss);		deleteMenuItem(mss2);		deleteMenuItem(mLine);		deleteMenuItem(mLine2);		deleteMenuItem(nNG);		deleteMenuItem(mEGC);		deleteMenuItem(mEGN);		deleteMenuItem(nNG);		deleteMenuItem(deleteInfoStripM);		deleteMenuItem(mStc);		mss = addSubmenu(null, "Set Group", makeCommand("setGroup", this));		mss.setList((StringLister)getProject().getFileElement(CharactersGroupVector.class, 0));		mss2 = addMenuItem("Remove Group Designation", makeCommand("removeGroup", this));		mLine2 = addMenuItem("-",null);		nNG = addMenuItem("New Group...", makeCommand("newGroup",  this));		mEGC = addSubmenu(null, "Edit Group...", makeCommand("editGroup", this));		mEGC.setList((StringLister)getProject().getFileElement(CharactersGroupVector.class, 0));		mLine = addMenuItem("-",null);		if (data !=null) {			mStc = addSubmenu(null, "Partition", makeCommand("setPartition",  this), data.getSpecSetsVector(CharacterPartition.class)); 		}		mLine3 = addMenuItem("-",null);		nNG = addMenuItem("Delete Info Strip", makeCommand("deleteInfoStrip",  this)); 				this.data = data;		this.table = table;	}	/*.................................................................................................................*/  	 public Snapshot getSnapshot(MesquiteFile file) {    	 	Snapshot temp = new Snapshot();		temp.addLine("setPartition " + StringUtil.tokenize(""+element));  	 	return temp;  	 }	/*.................................................................................................................*/	/*.................................................................................................................*/    	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {    	 	 if (checker.compare(this.getClass(), "Sets the character group of the selected characters", "[name of group]", commandName, "setGroup")) {    	 		setGroup(null, arguments, commandRec);    	 	}    	 	else if (checker.compare(this.getClass(), "Creates a new group for use in character partitions", null, commandName, "newGroup")) {			MesquiteString ms = new MesquiteString("");			CharactersGroup group = CharListPartitionUtil.makeGroup(this,data,containerOfModule(), ms);	 		if (group==null) return null;	 		setGroup(group, ms.getValue(), commandRec);	    	 }    	 	else if (checker.compare(this.getClass(), "Edits the name and color of a character group label", "[name of group]", commandName, "editGroup")) {	 		String name = parser.getFirstToken(arguments);			if (StringUtil.blank(name))				return null;			String num = parser.getNextToken(); 			Object obj = CharListPartitionUtil.editGroup(this, data,containerOfModule(),name, num, commandRec); 			if (obj!=null) { 				outputInvalid(commandRec);				parametersChanged(commandRec);			}    	 	}     	 	else if (checker.compare(this.getClass(), "sets partition to use", "[partition number]", commandName, "setPartition")) {			if (data !=null) { 				int which = MesquiteInteger.fromFirstToken(arguments, stringPos); 				if (MesquiteInteger.isCombinable(which)){ 					setElement(which);					outputInvalid(commandRec);					parametersChanged(commandRec);					table.repaintAll();				}	 		}    	 	}    	 	else if (checker.compare(this.getClass(), "Removes the group designation from the selected characters", null, commandName, "removeGroup")) {			removeGroup(commandRec);	    	 }    	 	else if (checker.compare(this.getClass(), "Deletes the info strip", null, commandName, "deleteInfoStrip")) {			iQuit();	    	 }    	 	else    	 		return super.doCommand(commandName, arguments, commandRec, checker);		return null;   	 }	/*.................................................................................................................*/  	private void setSelectedToGroup(CharactersGroup group, CommandRecord commandRec){ 		if (table !=null && data!=null) { 			boolean changed=false; 			if (partition != null) {	 				for (int i=0; i<data.getNumChars(); i++) {	 					if (table.isColumnNameSelected(i) || table.isColumnSelected(i)) {	 						partition.setProperty(group, i);	 						if (!changed)							outputInvalid(commandRec);	 						changed = true;	 					}	 				}	 			}	 			if (changed)    	 			data.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED), commandRec); //TODO: bogus! should notify via specs not data???			outputInvalid(commandRec); 			parametersChanged(null, commandRec); 			table.repaintAll(); 		}  	}	/*.................................................................................................................*/  	private void setGroup(CharactersGroup group, String arguments, CommandRecord commandRec){ 		if (table !=null && data!=null) { 			boolean changed=false; 			String name = parser.getFirstToken(arguments); 			if (group == null && StringUtil.blank(name)) 				return;			if (group == null){				CharactersGroupVector groups = (CharactersGroupVector)data.getProject().getFileElement(CharactersGroupVector.class, 0);				Object obj = groups.getElement(name);				group = (CharactersGroup)obj;			}			if (group != null) {				setSelectedToGroup(group,commandRec);   	 		} 		}  	}	/*.................................................................................................................*/  	private void removeGroup(CommandRecord commandRec){	 	if (table !=null && data!=null) {			setSelectedToGroup(null,commandRec); 		}  	}	/*.................................................................................................................*/	public void setElement(int which) {		if (MesquiteInteger.isCombinable(which)){			element = which; 			if (element >=0) {  // use current 	 			SpecsSetVector ssv = data.getSpecSetsVector(CharacterPartition.class);				if (ssv!=null) {					 partition = (CharacterPartition)ssv.getSpecsSet(which);		 		}		 		else		 			element = -1;	 		}			if (element<0 || partition ==null) // use current spec set				 partition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);		} 	}	/*.................................................................................................................*/	public boolean canHireMoreThanOnce(){		return true;	}	/*.................................................................................................................*/	public void changed(Object caller, Object obj, Notification notification, CommandRecord commandRec){		if (Notification.appearsCosmetic(notification))			return;		outputInvalid(commandRec);		parametersChanged(notification, commandRec);	}	/*.................................................................................................................*/	public String getTitle() {		return "Group";	}	/*.................................................................................................................*/	public String getStringForCharacter(int ic){		if (data!=null) {			//CharacterPartition partition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);			if (partition != null) {				CharactersGroup group = (CharactersGroup)partition.getProperty(ic);				if (group!=null) {					return group.getName();				}			}		}		return "?";	}	/*.................................................................................................................*/	public String getStringForExplanation(int ic){		if (data!=null) {			if (partition != null) {				CharactersGroup group = (CharactersGroup)partition.getProperty(ic);				if (group!=null) {					String s = group.getName() + "\n";   					MesquiteInteger startBlock = new MesquiteInteger(-1);   					MesquiteInteger endBlock = new MesquiteInteger(-1);   					if (getNextBlock(ic, false, startBlock, endBlock)) {   						if (startBlock.getValue()!=endBlock.getValue())   							s+= "  [next members of group at <- " + startBlock.getValue() + "-" + endBlock.getValue() + "]";   						else    							s+= "  [next member of group at <- " + startBlock.getValue()+ "]";   					}   					if (getNextBlock(ic, true, startBlock, endBlock)) {   						if (startBlock.getValue()!=endBlock.getValue())   							s+= "  [next members of group at -> " + startBlock.getValue() + "-" + endBlock.getValue() + "]";   						else   							s+= "  [next member of group at -> " + startBlock.getValue()  + "]";   					}   											return s;				}			}		}		return "";	}	/*.................................................................................................................*/	public boolean useString(int ic){		return false;	}	/*.................................................................................................................*/	public boolean similarChar(int ic, int ic2) {		if (partition==null)			return false;		CharactersGroup tg = partition.getCharactersGroup(ic);		CharactersGroup tg2 = partition.getCharactersGroup(ic2);		if (tg==null || tg2 == null)			return false;		return tg==tg2;	}	/*.................................................................................................................*/	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){		if (data==null || g==null)			return;//if (ic==1) Debugg.println("     y: " +  y + ", h: " + h);		boolean colored = false;		Color c = g.getColor();		//CharacterPartition part = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);		if (partition!=null) {			CharactersGroup tg = partition.getCharactersGroup(ic);			if (tg!=null){				Color cT = tg.getColor();				if (cT!=null){					g.setColor(cT);					g.fillRect(x+1,y+1,w-1,h-1);					colored = true;				}			}		}		if (!colored){ 			if (selected)				g.setColor(Color.black);			else				g.setColor(Color.white);			g.fillRect(x+1,y+1,w-1,h-1);		}					if (showString.getValue()) {			String s = getStringForCharacter(ic);			if (s!=null){				FontMetrics fm = g.getFontMetrics(g.getFont());				if (fm==null)					return;				int sw = fm.stringWidth(s);				int sh = fm.getMaxAscent()+ fm.getMaxDescent();				if (selected)					g.setColor(Color.white);				else					g.setColor(Color.black);				g.drawString(s, x+(w-sw)/2, y+h-(h-sh)/2);				if (c!=null) g.setColor(c);			}		}	}	/*.................................................................................................................*/	public String getWidestString(){		return "  ";	}	/*.................................................................................................................*/    	 public String getName() {		return "Character Partition";   	 }	/*.................................................................................................................*/	/** returns whether this module is requesting to appear as a primary choice */   	public boolean requestPrimaryChoice(){   		return true;     	}   	 	/*.................................................................................................................*/   	public boolean isPrerelease(){   		return false;     	}	/*.................................................................................................................*/ 	/** returns an explanation of what the module does.*/ 	public String getExplanation() { 		return "Shows and allows changes to group membership in a partition of characters, in the Data Editor." ;   	 }}