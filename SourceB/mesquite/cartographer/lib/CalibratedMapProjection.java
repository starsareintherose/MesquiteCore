/* Mesquite.cartographer source code.  Copyright 2005 D. Maddison, W. Maddison. Version 1.0, April 2005.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.cartographer.lib;/*~~  */import java.util.*;import java.awt.*;import java.awt.event.*;import mesquite.lib.*;import mesquite.lib.duties.*;import mesquite.cartographer.lib.*;public abstract class CalibratedMapProjection extends MapProjection  {		protected int maxCalibrations =20;	protected int minCalibrations = 2;	protected CalibrationPoint[] calibrations= new CalibrationPoint[maxCalibrations];	TreeTool calibrationTreeTool;	TaxaTool calibrationTool;	MesquiteCommand touchCalibrateCommand, setCalibrationPointCommand, moveCalibrationCommand;	MesquiteBoolean showCalibration = new MesquiteBoolean(true);	MesquiteBoolean showGrid = new MesquiteBoolean(false);	int currentColor = ColorDistribution.numberOfBlue;	MesquiteNumber latitudeGridStart = new MesquiteNumber(0.0);	MesquiteNumber latitudeGridInterval = new MesquiteNumber(10.0);		MesquiteNumber longitudeGridStart = new MesquiteNumber(0.0);	MesquiteNumber longitudeGridInterval = new MesquiteNumber(10.0);		/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {  		zeroCalibrationPoints();  				touchCalibrateCommand = MesquiteModule.makeCommand("touchCalibration",  this);		moveCalibrationCommand = MesquiteModule.makeCommand("moveCalibration",  this);		if (getTreePlot()) {			calibrationTreeTool = new TreeTool(this, "calibrate", getPackageImagesPath(), "calibrateTool.gif", 8,7,"Calibration Tool", "This tool allows you to set the longitude and latitude of points on a map."); 			calibrationTreeTool.setTouchedFieldCommand(touchCalibrateCommand);			calibrationTreeTool.setMovedCommand(moveCalibrationCommand);			if (containerOfModule() instanceof MesquiteWindow) {				((MesquiteWindow)containerOfModule()).addTool(calibrationTreeTool);				//longLatTool.setPopUpOwner(this);			}		}		else {			calibrationTool = new TaxaTool(this, "calibrate", getPackageImagesPath(), "calibrateTool.gif", 8,7,"Calibration Tool", "This tool allows you to set the longitude and latitude of points on a map."); 			calibrationTool.setTouchedFieldCommand(touchCalibrateCommand);			calibrationTool.setMovedCommand(moveCalibrationCommand);			if (containerOfModule() instanceof MesquiteWindow) {				((MesquiteWindow)containerOfModule()).addTool(calibrationTool);				//longLatTool.setPopUpOwner(this);			}		}		setCalibrationPointCommand = MesquiteModule.makeCommand("setCalibrationPoint",  this); 		if (hasParameters()) 	 		addParameterMenus();	 		  		addMenuItem("Load Parameters & Calibration...", makeCommand("loadCalibration",  this));  		addMenuItem("Save Parameters & Calibration...", makeCommand("saveCalibration",  this));		MesquiteSubmenuSpec calibSubmenu = addSubmenu(null, "Calibration Points");		addCheckMenuItemToSubmenu( null, calibSubmenu, "Show Calibration Points", makeCommand("showCalibration",  this), showCalibration);  		addItemToSubmenu(null, calibSubmenu, "Save Calibration Points...", makeCommand("saveCalibrationPoints",  this));  		addItemToSubmenu(null, calibSubmenu, "Load Calibration Points...", makeCommand("loadCalibrationPoints",  this));  		addItemToSubmenu(null, calibSubmenu, "Discard All Calibration Points", makeCommand("removeCalibration",  this));  				addCheckMenuItem( null, "Show Grid", makeCommand("showGrid",  this), showGrid);		addMenuItem("Grid Parameters...", makeCommand("gridParameters",  this));		MesquiteSubmenuSpec mss = addSubmenu(null, "Grid Color", makeCommand("setGridColor",  this), ColorDistribution.standardColorNames);  		return true;  	 }  	 	/*.................................................................................................................*/	public void addParameterMenus() {	}	/*.................................................................................................................*/	public void endJob() {		if (calibrationTreeTool!=null)			((MesquiteWindow)containerOfModule()).removeTool(calibrationTreeTool);		if (calibrationTool!=null)			((MesquiteWindow)containerOfModule()).removeTool(calibrationTool);		super.endJob();	} 	/*.................................................................................................................*/  	 public Snapshot getSnapshot(MesquiteFile file) {    	 	Snapshot temp = super.getSnapshot(file);  		temp.addLine("showCalibration " + showCalibration.toOffOnString());  		temp.addLine("showGrid " + showGrid.toOffOnString());	 	temp.addLine("gridParameters '" + latitudeGridStart.getDoubleValue() + "' '" + latitudeGridInterval.getDoubleValue() + "' '" + longitudeGridStart.getDoubleValue() + "' '" + longitudeGridInterval.getDoubleValue() + "' ");  	 	temp.addLine("setGridColor " + ColorDistribution.getStandardColorName(ColorDistribution.getStandardColor((int)currentColor)));		for (int j = 0; j<maxCalibrations; j++) {			if (calibrations[j]!=null) 				temp.addLine("setCalibrationPoint " + calibrations[j].toString());		}	 	return temp;  	 }	/*.................................................................................................................*/	public String getXMLParameters(CommandRecord commandRec){		return "";	}	/*.................................................................................................................*/	public String getXMLCalibration(CommandRecord commandRec, boolean saveParameters) {		StringBuffer buffer = new StringBuffer(1000);		buffer.append("<?xml version=\"1.0\"?>\n");		buffer.append("<mesquite>\n");		buffer.append("\t<mapCalibration>\n");		buffer.append("\t\t<version>1</version>\n");		for (int j = 0; j<maxCalibrations; j++) {			if (calibrations[j]!=null) {				buffer.append("\t\t<calibrationPoint>\n");				buffer.append("\t\t\t<longitude>" + calibrations[j].getLongitude().toString() + "</longitude>\n");				buffer.append("\t\t\t<latitude>" + calibrations[j].getLatitude().toString() + "</latitude>\n");				buffer.append("\t\t\t<x>" + calibrations[j].getX().toString() + "</x>\n");				buffer.append("\t\t\t<y>" + calibrations[j].getY().toString() + "</y>\n");				buffer.append("\t\t</calibrationPoint>\n");			}		}		if (saveParameters) {			String s = getXMLParameters(commandRec);			if (!StringUtil.blank(s))				buffer.append(s);		}		buffer.append("\t</mapCalibration>\n");		buffer.append("</mesquite>\n");		return buffer.toString();	}	/*.................................................................................................................*/	public boolean hasParameters() {		return false;	}	/*.................................................................................................................*/	public void saveCalibration(CommandRecord commandRec, boolean saveParameters) {    	 	String path;    	 	if (saveParameters)    	 		path = MesquiteFile.saveFileAsDialog("Save parameters & calibration points", new StringBuffer("parameters.xml"));    	 	else     	 		path = MesquiteFile.saveFileAsDialog("Save calibration points", new StringBuffer("calibration.xml"));	    	if (!StringUtil.blank(path)) {			String s = getXMLCalibration(commandRec, saveParameters);			MesquiteFile.putFileContents(path, s, false);		}	}	/*.................................................................................................................*/   	public void processProjectionParameters(boolean duringOptimization){		super.processProjectionParameters(duringOptimization);   	}	/*.................................................................................................................*/	public boolean readProjectionParameters(String contents, CommandRecord commandRec){		return true;	}	/*.................................................................................................................*/	public boolean readParameterXML(MesquiteString nextTag, String tagContent, CommandRecord commandRec){		return true;	}	/*.................................................................................................................*/	public boolean readCalibration(String contents, CommandRecord commandRec, boolean readParameters) {		zeroCalibrationPoints();		Parser parser = new Parser();		Parser subParser = new Parser();		parser.setString(contents);		boolean acceptableVersion = false; 		MesquiteNumber longitude = new MesquiteNumber(); 		MesquiteNumber latitude = new MesquiteNumber();		int x = MesquiteInteger.unassigned;		int y = MesquiteInteger.unassigned;		if (!parser.isXMLDocument(false))   // check if XML			return false;		if (!parser.resetToMesquiteTagContents())   // check if has mesquite tag			return false;		MesquiteString nextTag = new MesquiteString();		String tagContent = parser.getNextXMLTaggedContent(nextTag);		if ("mapCalibration".equalsIgnoreCase(nextTag.getValue())) {  //make sure it has the right root tag			parser.setString(tagContent);			tagContent = parser.getNextXMLTaggedContent(nextTag);			String subTagContent;			while (!StringUtil.blank(tagContent)) {				if ("version".equalsIgnoreCase(nextTag.getValue())) {					if ("1".equalsIgnoreCase(tagContent))						acceptableVersion = true;					else						return false;				}				else if ("calibrationPoint".equalsIgnoreCase(nextTag.getValue()) && acceptableVersion) {					subParser.setString(tagContent);					subTagContent = subParser.getNextXMLTaggedContent(nextTag);					while (!StringUtil.blank(nextTag.getValue())) {						if ("x".equalsIgnoreCase(nextTag.getValue())) {  // we've found the x, stored in subTagContent							x = MesquiteInteger.fromString(subTagContent);						}						else if ("y".equalsIgnoreCase(nextTag.getValue())) {  // here's the y value, stored in subTagContent							y = MesquiteInteger.fromString(subTagContent);						}						else if ("longitude".equalsIgnoreCase(nextTag.getValue())) {// here's the signed longitude, stored in subTagContent							longitude.setValue(MesquiteDouble.fromString(subTagContent));						}						else if ("latitude".equalsIgnoreCase(nextTag.getValue())) {// here's the signed latitude, stored in subTagContent							latitude.setValue(MesquiteDouble.fromString(subTagContent));						}						subTagContent = subParser.getNextXMLTaggedContent(nextTag);					}					if (MesquiteInteger.isCombinable(x) && MesquiteInteger.isCombinable(y) && longitude.isCombinable() && latitude.isCombinable())						createCalibrationPoint(longitude, latitude, x, y, commandRec, false);				}				else if (readParameters)					if (!readParameterXML(nextTag, tagContent, commandRec))						return false;				tagContent = parser.getNextXMLTaggedContent(nextTag);			}		} else			return false;		return true;	}	/*.................................................................................................................*/	public boolean loadCalibration(CommandRecord commandRec, boolean readParameters) {		MesquiteString directoryName= new MesquiteString();		MesquiteString fileName= new MesquiteString();		String filePath = MesquiteFile.openFileDialog("Choose Calibration File...", directoryName, fileName);		if (filePath==null)			return sorry(commandRec, "Calibration file was not chosen.");		MesquiteFile dataFile =MesquiteFile.open(true, filePath);		if (dataFile==null || StringUtil.blank(dataFile.getFileName())) 			return sorry(commandRec, "Calibration file could not be found.");					String contents = MesquiteFile.getFileContentsAsString(filePath);		if (!StringUtil.blank(contents)) {			if (!readCalibration(contents, commandRec, readParameters))				return sorry(commandRec, "Calibration information could not be read.  It may not be a valid calibration file."); //D!		}   				if (!commandRec.scripting()) {			processProjectionParameters(false);//			calcProjectionScalesAndOffsets();			parametersChanged(null, commandRec);			//			ownerModuleParametersChanged(commandRec);		}		return true;	}	/*.................................................................................................................*/	public boolean nearCalibrationPoint(int x, int y, MesquiteInteger whichPoint) {		for (int j = 0; j<maxCalibrations; j++) {			if (calibrations[j]!=null) {				if (calibrations[j].nearPoint(x, y)) {					whichPoint.setValue(j);					return true;				}			}		}		return false;	}	/*.................................................................................................................*/    	 public void moveCalibrationTool(int x, int y, CommandRecord commandRec) { 		MesquiteInteger whichPoint = new MesquiteInteger();		if (containerOfModule() instanceof MesquiteWindow) {			String s = "";	 		if (nearCalibrationPoint(x-margin, y-margin, whichPoint)) 	 			s = "Calibration point: (" + calibrations[whichPoint.getValue()].getLatitude()+ ", " + calibrations[whichPoint.getValue()].getLongitude()+")";			((MesquiteWindow)containerOfModule()).setExplanation(s);		}		}	/*.................................................................................................................*/    	 public void touchCalibrationTool(int x, int y, CommandRecord commandRec) { 		MesquiteInteger whichPoint = new MesquiteInteger(); 		if (nearCalibrationPoint(x-margin, y-margin, whichPoint)) {			if (!commandRec.scripting() && !AlertDialog.query(containerOfModule(), "Delete calibration point?","Are you sure you want to delete this calibration point?", "Yes", "No")) 				return; 			deleteCalibrationPoint(whichPoint.getValue());			parametersChanged(null, commandRec);		} 		else { 			MesquiteNumber longitude = new MesquiteNumber(0.0); 			MesquiteNumber latitude = new MesquiteNumber(0.0); 			if (queryCalibration(longitude, latitude)) {				createCalibrationPoint(longitude, latitude, x-margin, y-margin, commandRec, true);			}		}		}	/*.................................................................................................................*/	public boolean queryGridParameters() {		MesquiteInteger buttonPressed = new MesquiteInteger(1);		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Grid Parameters",  buttonPressed);		queryDialog.addLabel("Grid parameters", Label.CENTER);		DoubleField latStartField = queryDialog.addDoubleField("A Latitude:", latitudeGridStart.getDoubleValue(),8, -90.0,90.0);		DoubleField  latIntField = queryDialog.addDoubleField("Latitude interval:", latitudeGridInterval.getDoubleValue(),8,0.0,90.0);		DoubleField lonStartField = queryDialog.addDoubleField("A Longitude:", longitudeGridStart.getDoubleValue(),8,-180.0,180.0);		DoubleField  lonIntField = queryDialog.addDoubleField("Longitude interval:", longitudeGridInterval.getDoubleValue(),8,0.0,180.0);		String s = "In this dialog you can control the parameters of a grid of longitude and latitude lines that is shown on the map. ";		s+= "All values are in degrees. 'A Latitide' specifies a latitude at which a grid line is to be drawn; other lines are then to be ";		s+="drawn at intervals as specified in 'Latitude Interval' east and west of that line; similarly for longitude. ";		s+= getStringDescribingLatLongs();		if (!showGrid.getValue())			s+= "\n\n(To show the grid, choose 'Show Grid' in the Plot menu).";		queryDialog.appendToHelpString(s);		queryDialog.completeAndShowDialog(true);		MesquiteBoolean success = new MesquiteBoolean(true);					boolean ok = (queryDialog.query()==0);				if (ok) {			latitudeGridStart.setValue(latStartField.getValue(success));  			latitudeGridInterval.setValue(latIntField.getValue(success));  			longitudeGridStart.setValue(lonStartField.getValue(success));  			longitudeGridInterval.setValue(lonIntField.getValue(success));  		}				queryDialog.dispose();   		 		if (!success.getValue()) {			discreetAlert(CommandRecord.nonscriptingRecord, "Some values are out of bounds; these values will be reset to their previous values.");		}   		return ok;	}	/*.................................................................................................................*/    	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {		if (checker.compare(this.getClass(), "Sets the parameters of the grid", "[top bottom left right]", commandName, "gridParameters")) {    	 		if (StringUtil.blank(arguments) && !commandRec.scripting()) {    	 			if (queryGridParameters()) 	 	 			if ((latitudeGridStart.isCombinable() && latitudeGridInterval.isCombinable() && longitudeGridStart.isCombinable() && longitudeGridInterval.isCombinable()))	    	 				if (showGrid.getValue()) {							parametersChanged(null, commandRec); 						}    	 		}    	 		else {	    	 		latitudeGridStart.setValue(MesquiteDouble.fromString(parser.getFirstToken(arguments)));	     	 		latitudeGridInterval.setValue(MesquiteDouble.fromString(parser.getNextToken()));	     	 		longitudeGridStart.setValue(MesquiteDouble.fromString(parser.getNextToken()));	     	 		longitudeGridInterval.setValue(MesquiteDouble.fromString(parser.getNextToken()));	 	 		if (!(latitudeGridStart.isCombinable() && latitudeGridInterval.isCombinable() && longitudeGridStart.isCombinable() && longitudeGridInterval.isCombinable()))	    	 			if (!commandRec.scripting())	    	 				queryGridParameters();	 	 		if (!(latitudeGridStart.isCombinable() && latitudeGridInterval.isCombinable() && longitudeGridStart.isCombinable() && longitudeGridInterval.isCombinable()))	    	 			return null;	    	 		else 	    	 			if (showGrid.getValue())	    	 				parametersChanged(null, commandRec);     	 		}    	 	}     	 	else if (checker.compare(this.getClass(), "Set Calibration Point", "[long lat x y]", commandName, "setCalibrationPoint")) {	 		MesquiteNumber longitude = new MesquiteNumber(0.0);	 		MesquiteNumber latitude = new MesquiteNumber(0.0);    	 		longitude.setValue(MesquiteDouble.fromString(parser.getFirstToken(arguments)));     	 		latitude.setValue(MesquiteDouble.fromString(parser.getNextToken()));			int x= MesquiteInteger.fromString(parser.getNextToken());			int y= MesquiteInteger.fromString(parser.getNextToken());			createCalibrationPoint(longitude, latitude, x, y, commandRec, true);    	 	}    	 	else if (checker.compare(this.getClass(), "Calibration tool has been touched", "[x coordinate][y coordinate]", commandName, "touchCalibration")) {	 		MesquiteInteger io = new MesquiteInteger(0);			int x= MesquiteInteger.fromString(arguments, io);			int y= MesquiteInteger.fromString(arguments, io);	 		touchCalibrationTool(x,y,commandRec);	    	 	}    	 	else if (checker.compare(this.getClass(), "Calibration tool has been moved", "[x coordinate][y coordinate]", commandName, "moveCalibration")) {	 		MesquiteInteger io = new MesquiteInteger(0);			int x= MesquiteInteger.fromString(arguments, io);			int y= MesquiteInteger.fromString(arguments, io);	 		moveCalibrationTool(x,y,commandRec);	    	 	}      	 	else if (checker.compare(this.getClass(), "Sets whether or not the calibration points are shown.", "[on or off]", commandName, "showCalibration")) {    	 		boolean current = showCalibration.getValue();    	 		showCalibration.toggleValue(parser.getFirstToken(arguments));    	 		if (current!=showCalibration.getValue())    	 			parametersChanged(null, commandRec);    	 	}       	 	else if (checker.compare(this.getClass(), "Sets whether or not the grid is shown.", "[on or off]", commandName, "showGrid")) {    	 		boolean current = showGrid.getValue();    	 		showGrid.toggleValue(parser.getFirstToken(arguments));    	 		if (current!=showGrid.getValue())    	 			parametersChanged(null, commandRec);    	 	}    	 	else	if (checker.compare(this.getClass(), "Sets the color to be used for the grid", "[name of color]", commandName, "setGridColor")) {    	 		int bc = ColorDistribution.standardColorNames.indexOf(parser.getFirstToken(arguments)); 			if (bc >=0 && MesquiteLong.isCombinable(bc) && currentColor!=bc){				currentColor = bc;				parametersChanged(null,commandRec);			}    	 	}        	 else if (checker.compare(this.getClass(), "Removes all calibration points.", "", commandName, "removeCalibration")) {    	 		zeroCalibrationPoints();    	 		parametersChanged(null, commandRec); //   	 		ownerModuleParametersChanged(commandRec);    	 	}       	 	else if (checker.compare(this.getClass(), "Loads parameters and calibration points from file.", "", commandName, "loadCalibration")) {    	 		if (loadCalibration(commandRec, true)) {    	 			//showCalibration.setValue(true);    	 			parametersChanged(null, commandRec);    	 		}    	 	}       	 	else if (checker.compare(this.getClass(), "Save parameters and calibration points to file.", "", commandName, "saveCalibration")) {    	 		saveCalibration(commandRec, true);    	 	}       	 	else if (checker.compare(this.getClass(), "Loads calibration points from file.", "", commandName, "loadCalibrationPoints")) {    	 		if (loadCalibration(commandRec, false)) {    	 			//showCalibration.setValue(true);    	 			parametersChanged(null, commandRec);    	 		}    	 	}       	 	else if (checker.compare(this.getClass(), "Save calibration points to file.", "", commandName, "saveCalibrationPoints")) {    	 		saveCalibration(commandRec, false);    	 	}	 	else       	 		return super.doCommand(commandName, arguments, commandRec, checker);    	 	return null;    	 } 	/*_________________________________________________*/  	public boolean getFullySpecified() { 		return (getNumberOfCalibrationPoints() >= minCalibrations && parametersSpecified()); 	}	/*.................................................................................................................*/  	public abstract boolean parametersSpecified() ;	/*.................................................................................................................*/	public String getStringDescribingLatLongs() {		String s = "Use decimal degrees (e.g., 43.3526) for latitude and longitude values.  ";		s+= "For latitudes in the southern hemisphere or longitudes in the western hemisphere use negative values (e.g., for 79.7513 W use -79.7513). ";		return s;	}	/*.................................................................................................................*/	public boolean queryCalibration(MesquiteNumber longitude, MesquiteNumber latitude) {		MesquiteInteger buttonPressed = new MesquiteInteger(1);		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Calibration Point",  buttonPressed);		queryDialog.addLabel("Calibration Point Longitude/Latitude", Label.CENTER);		DoubleField latField = queryDialog.addDoubleField("Latitude:", 0.0,8, -90.0, 90.0);		DoubleField longField = queryDialog.addDoubleField("Longitude:", 0.0,8, -180.0, 180.0);		String s = "Enter the latitude and longitude of the point touched.  ";		s+= getStringDescribingLatLongs();		queryDialog.appendToHelpString(s);		queryDialog.completeAndShowDialog(true);		MesquiteBoolean success = new MesquiteBoolean(true);					boolean ok = (queryDialog.query()==0);				if (ok) {			longitude.setValue(longField.getValue(success));  			latitude.setValue(latField.getValue(success));  		}				queryDialog.dispose();   		 		if (!success.getValue()) {			discreetAlert(CommandRecord.nonscriptingRecord, "Some values are out of bounds; these values will be reset to their previous values.");		}   		return ok;	}	/*.................................................................................................................*/	public int getNumberOfCalibrationPoints() {		int count = 0;		for (int j = 0; j<maxCalibrations; j++) {			if (calibrations[j]!=null)				count++;		}		return count;	}	/*.................................................................................................................*/	public void zeroCalibrationPoints() {		for (int j = 0; j<maxCalibrations; j++) {			calibrations[j]=null;		}	}	/*.................................................................................................................*/	public void createCalibrationPoint(MesquiteNumber longitude, MesquiteNumber latitude, int x, int y, CommandRecord commandRec, boolean notify) {		boolean madeOne=false;		for (int j = 0; j<maxCalibrations; j++) {			if (calibrations[j]==null) {				calibrations[j] = new CalibrationPoint (this,longitude, latitude, x, y);				madeOne = true;    	 			if (notify)    	 				parametersChanged(null, commandRec);				break;			}		}		if (!madeOne) {    	 			discreetAlert(commandRec, "The maximum number of allowed calibration points is " + maxCalibrations + ".");		}	}	/*.................................................................................................................*/	public void deleteCalibrationPoint(int j) {		calibrations[j] = null;	}	/*_________________________________________________*/ 	  	/** This method does the simple scaling back down from screen coordinates to projection coordinates.  */   	public void scaleFromScreenToProjected(MesquiteNumber x, MesquiteNumber y){		x.setValue((x.getDoubleValue() - xLeftOffset)/xScale);		y.setValue((y.getDoubleValue() - yTopOffset)/yScale);   	}	/*_________________________________________________*/	public double scaleXFromProjectedToScreen (MesquiteNumber x){		return x.getDoubleValue()*xScale + xLeftOffset + margin;	}	/*_________________________________________________*/	public double scaleYFromProjectedToScreen (MesquiteNumber y){		return y.getDoubleValue()*yScale+ yTopOffset + margin;	}	/*_________________________________________________*/	/**    This method calculates the scale used to convert the calculated projected values to the screen coordinates. 	It does this by using the projection formula to convert the calibration points to their projected values, then uses the known pixel values to figure out the 	transformation formula needed to convert the project values to the pixel values.	*/	public boolean calcProjectionScalesAndOffsets () {    		int minPixDiff = 10;    		double minProjDiff = 0.00001;		for (int j = 0; j<maxCalibrations; j++) {			if (calibrations[j]!=null) {				calibrations[j].setProjectedValues();			}		}		double numXDiff = 0.0;		double numYDiff = 0;		double pixDiff;		double xDiff;		double yDiff;		double pixelValue;		double xScaleCumulative = 0.0, yScaleCumulative = 0.0;		for (int j = 0; j<maxCalibrations; j++) {			for (int i =j+1; i<maxCalibrations; i++) {				if (calibrations[j]!=null && calibrations[i]!=null && calibrations[j].projectedXLegal()&& calibrations[j].projectedYLegal()&& calibrations[i].projectedXLegal()&& calibrations[i].projectedYLegal()) {					xDiff = calibrations[j].getProjectedX().getDoubleValue() - calibrations[i].getProjectedX().getDoubleValue();					pixDiff = calibrations[j].getX().getIntValue() - calibrations[i].getX().getIntValue();					if (Math.abs(pixDiff) >= minPixDiff && Math.abs(xDiff)>minProjDiff) {						if (pixDiff!=0) {							numXDiff++;							pixelValue = (pixDiff)/xDiff;							xScaleCumulative += Math.abs(pixelValue);						}					}					yDiff = calibrations[j].getProjectedY().getDoubleValue() - calibrations[i].getProjectedY().getDoubleValue();					pixDiff = calibrations[j].getY().getIntValue() - calibrations[i].getY().getIntValue() ;					if (Math.abs(pixDiff) >= minPixDiff&& Math.abs(yDiff)>minProjDiff) {						if (pixDiff!=0) {							numYDiff++;							pixelValue = (1.0 * pixDiff)/yDiff;							yScaleCumulative += Math.abs(pixelValue);						}					}				}			}		}				if (numXDiff > 0 && numYDiff > 0) {		// calculate scale based upon average of calculated scales	    		xScale = xScaleCumulative / numXDiff;   // calculate average xScale	    		yScale = yScaleCumulative / numYDiff;   // calculate average yScale	    		    	// now look to see what the average offset is between the calculated position of a calibration point and the actual point.	    		int numCalib = 0;	    		double xOffset, yOffset;			double xOffsetCumulative = 0.0, yOffsetCumulative = 0.0;			for (int i =0; i<maxCalibrations; i++) {				if (calibrations[i]!=null && calibrations[i].projectedXLegal()&& calibrations[i].projectedYLegal()) {					numCalib++;  	       // for x offset, take pixel value from margin that is stored in the calibration point, and compare it to the calculated value					xOffset = calibrations[i].getX().getIntValue()*1.0 - calibrations[i].getProjectedX().getDoubleValue()*xScale;  					xOffsetCumulative += xOffset;					yOffset = calibrations[i].getY().getIntValue() *1.0+ calibrations[i].getProjectedY().getDoubleValue()*yScale;					yOffsetCumulative +=yOffset;				}			}	    		xLeftOffset = xOffsetCumulative/numCalib;	    		yTopOffset = yOffsetCumulative/numCalib;//Debugg.println("xScale: "+xScale + ", yScale: " + yScale);	    		//Debugg.println("xLeftOffset: "+xLeftOffset + ", yTopOffset: " + yTopOffset);	    			    			    		return true;    		}    		return false;	}	/*.................................................................................................................*/	public void drawBasicExtras  (Graphics g) {		FontMetrics fm=g.getFontMetrics(g.getFont());		if (showCalibration.getValue()) {			for (int j = 0; j<maxCalibrations; j++) {				if (calibrations[j]!=null) {					calibrations[j].drawPoint(g, margin);				}			}		}		if (showGrid.getValue() && getFullySpecified())			drawGrid(g); 	}	/*.................................................................................................................*/	public void drawExtras  (Graphics g,TreeDisplay treeDisplay) {		super.drawExtras(g,treeDisplay);		drawBasicExtras(g); 	}	/*.................................................................................................................*/	public void drawTaxaExtras  (Graphics g,TaxaDisplay taxaDisplay) {		super.drawTaxaExtras(g,taxaDisplay);		drawBasicExtras(g); 	}	/*.................................................................................................................*/	public boolean onMap (int x, int y) {		return (x>margin && y>margin && x<margin+mapWidth && y< margin+mapHeight);	}	/*.................................................................................................................*/	public double getMinLatitude () {		return -90.0;	}	/*.................................................................................................................*/	public double getMaxLatitude () {		return 90.0;	}	/*.................................................................................................................*/	public double getMinLongitude () {		return -180.0;	}	/*.................................................................................................................*/	public double getMaxLongitude () {		return 180.0;	}	/*.................................................................................................................*/	public void drawGrid (Graphics g) {		MesquiteNumber minLatitude = new MesquiteNumber();		MesquiteNumber maxLatitude = new MesquiteNumber();		MesquiteNumber minLongitude = new MesquiteNumber();		MesquiteNumber maxLongitude = new MesquiteNumber();		findMinMaxLongLat (minLatitude, maxLatitude, minLongitude, maxLongitude);		if (!minLatitude.isCombinable())			minLatitude.setValue(getMinLatitude());		if (!maxLatitude.isCombinable())			maxLatitude.setValue(getMaxLatitude());		if (!minLongitude.isCombinable())			minLongitude.setValue(getMinLongitude());		if (!maxLongitude.isCombinable())			maxLongitude.setValue(getMaxLongitude());//Debugg.println("\nminLatitude: "+minLatitude.getDoubleValue() + ", maxLat: " + maxLatitude.getDoubleValue() + ", minLong: " + minLongitude.getDoubleValue() + ", maxLong: " + maxLongitude.getDoubleValue());				double latStart =  latitudeGridStart.getDoubleValue();//Debugg.println("latitudeGridStart: "+latitudeGridStart.getDoubleValue());		if (latStart>minLatitude.getDoubleValue()) {			for (double d = latitudeGridStart.getDoubleValue(); d>=-90; d-=latitudeGridInterval.getDoubleValue())				if (d>=minLatitude.getDoubleValue())					latStart = d;				else break;		}		else {  // have to go up to find it			for (double d = latitudeGridStart.getDoubleValue(); d<=90; d+=latitudeGridInterval.getDoubleValue())				if (d>=minLatitude.getDoubleValue()) {					latStart = d;					break;				}		}				double latEnd =  latitudeGridStart.getDoubleValue();		if (latEnd<maxLatitude.getDoubleValue()) {			for (double d = latitudeGridStart.getDoubleValue(); d<=90; d+=latitudeGridInterval.getDoubleValue())				if (d<=maxLatitude.getDoubleValue())					latEnd = d;				else break;		}		else {  // have to go down to find it			for (double d = latitudeGridStart.getDoubleValue(); d>=-90; d-=latitudeGridInterval.getDoubleValue())				if (d<=maxLatitude.getDoubleValue()) {					latEnd = d;					break;				}		}		double lonStart =  longitudeGridStart.getDoubleValue();		if (lonStart>minLongitude.getDoubleValue()) {			for (double d = longitudeGridStart.getDoubleValue(); d>=-180; d-=longitudeGridInterval.getDoubleValue())				if (d>=minLongitude.getDoubleValue())					lonStart = d;				else break;		}		else {  // have to go up to find it			for (double d = longitudeGridStart.getDoubleValue(); d<=180; d+=longitudeGridInterval.getDoubleValue())				if (d>=minLongitude.getDoubleValue()) {					lonStart = d;					break;				}		}		double lonEnd =  longitudeGridStart.getDoubleValue();		if (lonEnd<maxLongitude.getDoubleValue()) {			for (double d = longitudeGridStart.getDoubleValue(); d<=180; d+=longitudeGridInterval.getDoubleValue())				if (d<=maxLongitude.getDoubleValue())					lonEnd = d;				else break;		}		else {  // have to go down to find it			for (double d = longitudeGridStart.getDoubleValue(); d>=-180; d-=longitudeGridInterval.getDoubleValue())				if (d<=maxLongitude.getDoubleValue()) {					lonEnd = d;					break;				}		}		//Debugg.println("latStart: "+latStart + ", latEnd: " + latEnd + ", lonStart: " + lonStart + ", lonEnd: " + lonEnd);		MesquiteNumber oldX = new MesquiteNumber();		MesquiteNumber oldY = new MesquiteNumber();		MesquiteNumber x = new MesquiteNumber();		MesquiteNumber y = new MesquiteNumber();		g.setColor(ColorDistribution.getStandardColor(currentColor));		for (double longitude = lonStart; longitude<=lonEnd; longitude+=longitudeGridInterval.getDoubleValue()) {  // deal with other hemispheres			oldX.setToUnassigned();			oldY.setToUnassigned();			for (double latitude= latStart; latitude<=latEnd; latitude+=0.5) {				convertLongLatsToScreenCoordinates(longitude, latitude, x, y);				if (oldX.isCombinable() && oldY.isCombinable() && x.isCombinable() && y.isCombinable()) 					if (onMap(x.getIntValue(), y.getIntValue()) && onMap(oldX.getIntValue(), oldY.getIntValue())) {						g.drawLine(x.getIntValue(), y.getIntValue(), oldX.getIntValue(), oldY.getIntValue());					}				oldX.setValue(x);				oldY.setValue(y);			}		}//		g.setColor(Color.red);		for (double latitude= latStart; latitude<latEnd; latitude+=latitudeGridInterval.getDoubleValue()) {			oldX.setToUnassigned();			oldY.setToUnassigned();			for (double longitude = lonStart; longitude<lonEnd; longitude+=0.5) {				convertLongLatsToScreenCoordinates(longitude, latitude, x, y);				if (oldX.isCombinable() && oldY.isCombinable() && x.isCombinable() && y.isCombinable()) 					if (onMap(x.getIntValue(), y.getIntValue()) && onMap(oldX.getIntValue(), oldY.getIntValue())) {						g.drawLine(x.getIntValue(), y.getIntValue(), oldX.getIntValue(), oldY.getIntValue());//if (x.getIntValue()-oldX.getIntValue()>20) {//Debugg.println("\nx: "+x.getIntValue() + ", oldX: " + oldX.getIntValue() + "|||||  y: " + y.getIntValue() + ", oldY: " + oldY.getIntValue());//Debugg.println("latitude: "+latitude + ", longitude: " + longitude);//}					}				oldX.setValue(x);				oldY.setValue(y);			}		} 	}	/*.................................................................................................................*/   	public String getParameters() { 		return "";   	}	/*.................................................................................................................*/    	 public String getName() {		return "Calibrated Map Projection";   	 }   	public boolean isPrerelease(){   		return true;   	}	/*.................................................................................................................*/   	 public boolean showCitation(){   	 	return false;   	 }   	 }