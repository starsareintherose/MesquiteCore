/* Mesquite source code.  Copyright 1997-2002 W. Maddison & D. Maddison. Version 0.992.  September 2002.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.collab.lib; import java.util.*;import java.awt.*;import mesquite.lib.*;import mesquite.lib.duties.*;import mesquite.lib.characters.*;/** ======================================================================== */public abstract class ImageIndexManager extends DataWindowAssistantI {	/*.................................................................................................................*/   	 public Class getDutyClass() {   	 	return ImageIndexManager.class;   	 } 	public String getDutyName() { 		return "Index manager for images";   	 }  	public abstract void addListener(IndexListener listener);  	public abstract void removeListener(IndexListener listener);   	  	public abstract String getIndexDirectory(CommandRecord commandRec);  	public abstract String getImageRootPath(CommandRecord commandRec);	public abstract boolean useLocalImages();	public abstract void requestLoadIndices(IndexListener listener);}