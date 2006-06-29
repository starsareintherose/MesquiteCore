/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.Version 1.11, June 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.lib;import java.awt.*;import java.util.*;import mesquite.lib.duties.*;/* еееееееееееееееееееееееееее commands еееееееееееееееееееееееееееееее *//* includes commands,  buttons, miniscrolls/*====================== Mesquite command.  ======================*//** Each MesquiteCommand object stores a single sort of command that can be made, including its name(e.g. "setEdgeWidth") and the Commandable object to which the command is to be sent (e.g., an instantiationof a module).  To be commandable, objects must claim to implement the Commandableinterface. These MesquiteCommands are currentlyused mostly with Menus and Buttons, and with scripting.  For instance, to each menu item is attached a command, and when the menu item is selected, thecommand is executed.  By "executing" the command, we mean that the command's doIt method is called, which in turn callsthe doCommand method of the Commandable object to which the command applies.*/public class MesquiteCommand  implements Listable, MesquiteListener {	String commandName;	Commandable ownerObject;  	String defaultArguments = null;	boolean disposed = false;	boolean letMe = true;	public static ListableVector currentThreads;	boolean bypassQueue = false;	boolean hideInList = false; 	boolean suppressLogging = false;	boolean dontDuplicate = false; //if true then the same command on to the same object will not be put on the queue more than once	//to find memory leaks	public static int totalCreated =0;	public static int totalFinalized =0;	public static int totalDisposed =0;	public static Vector classesLinked, classesUnlinked, countsOfClasses; //to detect memory leaks	private Logger logger = null;		static {		currentThreads = new ListableVector(10);		if (MesquiteTrunk.checkMemory) {			classesLinked = new Vector();			classesUnlinked = new Vector();			countsOfClasses = new Vector();		}	}	public MesquiteCommand(String commandName,  Commandable ownerObject) {		this.commandName = commandName;		this.ownerObject = ownerObject;		if (ownerObject!=null && MesquiteTrunk.checkMemory){			if (classesLinked.indexOf(ownerObject.getClass())<0) {				classesLinked.addElement(ownerObject.getClass());				countsOfClasses.addElement(new MesquiteInteger(1));			}			else {				MesquiteInteger c = (MesquiteInteger)countsOfClasses.elementAt(classesLinked.indexOf(ownerObject.getClass()));				if (c!=null)					c.increment();			}		}		totalCreated++;	}		public void setLogger(Logger logger){		this.logger = logger;	}		public void notifyDoing(String s){		if (logger !=null)			logger.logln("Doing command " + commandName + " (" + s + ")");	}	public void notifyDone(String s){		if (logger !=null)			logger.logln("Done command " + commandName + " (" + s + ")");	}	private void stampLog(String arguments, boolean separateThread){		String logString = " > " + commandName;		if (arguments!=null && !arguments.equals(""))			logString +=  " \"" + arguments + "\"";		if (ownerObject instanceof Listable)			logString = ((Listable)ownerObject).getName() + logString;		else			logString = "[" + ownerObject.getClass().getName() + "]"  + logString;		if (separateThread)			logString += " -- on separate thread ";					/*		if (ownerObject instanceof Logger)			((Logger)ownerObject).logln(logString);		else*/			MesquiteModule.mesquiteTrunk.logln(logString);	}	/** Do the command, passing the given arguments.  This will call the commanded object's doCommand method.  */	public Object doIt(String arguments) {		return doIt(arguments, null);	}	/** Do the command, passing the given arguments.  This will call the commanded object's doCommand method.  */	public Object doIt(String arguments, CommandRecord commandRec) {		if (disposed)			return null;		if (ownerObject == null) {			MesquiteMessage.warnProgrammer("Warning: Command given to null object (" + commandName + "  " + arguments + ") MesquiteCommand");			return null;		}		if (StringUtil.blank(arguments))			arguments = defaultArguments;		if (!suppressLogging)			stampLog(arguments, false);		CommandRecord previous = MesquiteThread.getCurrentCommandRecord();		CommandRecord record = commandRec;		if (record == null)			record = new CommandRecord(false);		MesquiteThread.setCurrentCommandRecord(record);		Object returned = ownerObject.doCommand(commandName, arguments, record, CommandChecker.defaultChecker);  //if a command is executed in this way, assumed that not scripting		if (ownerObject instanceof FileDirtier)			((FileDirtier)ownerObject).fileDirtiedByCommand(this);		ProgressIndicator pi = record.getProgressIndicator();		if (pi !=null)			pi.goAway();		MesquiteThread.setCurrentCommandRecord(previous);		return returned;	}	/** Do the command, passing the given arguments.  This will call the commanded object's doCommand method.  */	public void doItNewThread(String arguments, String uiCallInformation) {		doItNewThread(arguments, uiCallInformation, true, true);	}		/** Do the command, passing the given arguments.  This will call the commanded object's doCommand method.  */	public void doItNewThread(String arguments, String uiCallInformation, boolean showWaitCursors, boolean logCommand) {		if (bypassQueue) {			doIt(arguments);			return;		}		if (StringUtil.blank(arguments))			arguments = defaultArguments;		if (false && CommandThread.waitCursorDepth==0 && showWaitCursors){			MesquiteWindow.setAllWaitCursors();		}		if (logCommand && !suppressLogging)			stampLog(arguments, true);		CommandThread thread = new CommandThread(this, arguments, uiCallInformation, false && showWaitCursors, logCommand && !suppressLogging);		currentThreads.addElement(thread, false);		thread.start();	}	/** Do the command, passing the given arguments.  This will call the commanded object's doCommand method.  */	public void doItMainThread(String arguments, String uiCallInformation, Object c) {		doItMainThread(arguments, uiCallInformation, true, true, c);	}	/** Do the command, passing the given arguments.  This will call the commanded object's doCommand method.  */	public void doItMainThread(String arguments, String uiCallInformation, boolean showWaitCursors, boolean logCommand) {		doItMainThread(arguments, uiCallInformation, showWaitCursors, logCommand, null);	}	/** Do the command, passing the given arguments.  This will call the commanded object's doCommand method.  Object passed is UI object (if any), such as menu item or button in window, from which command was issued*/	public void doItMainThread(String arguments, String uiCallInformation, boolean showWaitCursors, boolean logCommand, Object c) {		if (dontDuplicate && MainThread.commandAlreadyOnQueue(this))			return;		if (bypassQueue) {			doIt(arguments);			return;		}		if (StringUtil.blank(arguments))			arguments = defaultArguments;		PendingCommand thread = new PendingCommand(this, arguments, uiCallInformation, false && showWaitCursors, logCommand && !suppressLogging);		MainThread.pendingCommands.addElement(thread, false);	}	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/	public void changed(Object caller, Object obj, Notification notification, CommandRecord commandRec){		if (commandRec !=null)		 	commandRec.tick("");		if (obj instanceof CommandRecord && caller instanceof CommandThread && Notification.getCode(notification) == MesquiteListener.COMMAND_CANCELLED){			String s = ((CommandThread)caller).getListName();			((Listened)caller).removeListener(this);			MesquiteTrunk.mesquiteTrunk.alert("A command was cancelled or failed to complete its execution (command: \"" + s + "\").  It is possible that some calculations will remain incomplete, and that other error messages will be shown.");			((Thread)caller).interrupt();		}	}	/** passes which object was disposed*/	public void disposing(Object obj){}	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/	public boolean okToDispose(Object obj, int queryUser){return true;}	/** returns the command name */	public String getName() {		return commandName;	}	/** disposes of this command by setting the commanded object to null */	public void dispose() {		if (disposed)			return;		disposed = true;		if (ownerObject!=null && MesquiteTrunk.checkMemory)			if (classesUnlinked.indexOf(ownerObject.getClass())<0)				classesUnlinked.addElement(ownerObject.getClass());		ownerObject=null;		totalDisposed++;	}	/** to track memory leaks */	public void finalize() throws Throwable {		totalFinalized++;		super.finalize();	}	/** returns the object to be commanded */	public Commandable getOwner() {		return ownerObject;	}	/** sets the object to be commanded */	public void setOwner(Commandable obj) {		ownerObject = obj;	}	/** sets the default arguments to be passed if argument is null */	public MesquiteCommand setDefaultArguments(String defaultArgs) {		defaultArguments = defaultArgs;		return this;	}	/** sets whether the command is to be let finish (i.e. a warning is give on quit if pending) */	public void setLetMeFinish(boolean letMe) {		this.letMe = letMe;	}	/** gets whether the command is to be let finish (i.e. a warning is give on quit if pending) */	public boolean getLetMeFinish() {		return letMe;	}	/** sets whether the command is to bypass the command queue (e.g., for force quit) */	public void setQueueBypass(boolean bypass) {		bypassQueue = bypass;	}	/** sets whether the command is to be suppressed from the command queue if there is already the same command to the same object there*/	public void setDontDuplicate(boolean dd) {		dontDuplicate = dd;	}	/** sets whether the command is to be shown in the log when given by user interface command*/	public void setSuppressLogging(boolean dd) {		suppressLogging = dd;	}	/** Deletes pending references to this command on main queue*/	public void deleteOnQueue() {		MainThread.deleteCommand(this);	}	/** returns whether there are any commands in the command queue */	public static boolean anyQueuedCommands(boolean countCommandsWithoutLetMeFinish) {		if (countCommandsWithoutLetMeFinish)			return MainThread.pendingCommands.size()>0;		else {			for (int i=0;i<MainThread.pendingCommands.size(); i++){				PendingCommand ct = (PendingCommand)MainThread.pendingCommands.elementAt(i);				MesquiteCommand c = ct.getCommand();				if (c.letMe)					return true;			}			return false;		}	}	/** returns whether there are any commands in the command queue that have crashed*/	public static boolean anyCrashedCommands() {			for (int i=0;i<currentThreads.size(); i++){				CommandThread ct = (CommandThread)currentThreads.elementAt(i);				MesquiteCommand c = ct.getCommand();				if (ct.hasCrashed())					return true;			}			return false;	}	/** returns list of commands */	public static String commandList() {		return MainThread.pendingCommands.getList();	}	}