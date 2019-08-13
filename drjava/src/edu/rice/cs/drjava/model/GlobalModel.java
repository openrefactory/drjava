/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2019, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the names of its contributors may be used 
 *      to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software. Open Source Initative Approved is a trademark
 * of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/ or 
 * http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/
package edu.rice.cs.drjava.model;

import java.awt.print.PageFormat;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rice.cs.util.AbsRelFile;
import edu.rice.cs.drjava.model.compiler.CompilerModel;
import edu.rice.cs.drjava.model.debug.Debugger;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.definitions.DefinitionsEditorKit;
import edu.rice.cs.drjava.model.junit.JUnitModel;
import edu.rice.cs.drjava.model.repl.DefaultInteractionsModel;
import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsScriptModel;
import edu.rice.cs.drjava.model.javadoc.JavadocModel;
import edu.rice.cs.drjava.project.DocumentInfoGetter;
import edu.rice.cs.drjava.project.MalformedProjectFileException;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.docnavigation.IDocumentNavigator;
import edu.rice.cs.util.swing.DocumentIterator;
import edu.rice.cs.util.text.AbstractDocumentInterface;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.drjava.config.OptionParser;

/** Handles the bulk of DrJava's program logic.  The UI components interface with the GlobalModel through its 
  * public methods, and GlobalModel responds via the GlobalModelListener interface.  This removes the dependency 
  * on the UI for the logical flow of the program's features.  With the current implementation, we can finally test 
  * the compile an unit testing functionality of DrJava, along with many other things.  An ongoing refactoring effort 
  * will be moving many GlobalModel functions into more specific sub-interfaces for particular behaviors:
  * 
  * @see DefaultGlobalModel
  * @see ILoadDocuments
  * @see CompilerModel
  * @see JUnitModel
  * @see JavadocModel
  *
  * @version $Id$
  */
public interface GlobalModel extends ILoadDocuments {
  
  //-------------------------- Listener Management --------------------------//
  
  /** Add a listener to this global model.
    * @param listener a listener that reacts on events generated by the GlobalModel
    */
  public void addListener(GlobalModelListener listener);
  
  /** Remove a listener from this global model.
    * @param listener a listener that reacts on events generated by the GlobalModel
    */
  public void removeListener(GlobalModelListener listener);
  
  //------------------------ Feature Model Accessors ------------------------//
  
  /** @return the interactions model. */
  public DefaultInteractionsModel getInteractionsModel();
  
  /** @return the CompilerModel, which provides all methods relating to compilers. */
  public CompilerModel getCompilerModel();
  
  /** @return the JUnitModel, which provides all methods relating to JUnit testing. */
  public JUnitModel getJUnitModel();
  
  /** @return the JavadocModel, which provides all methods relating to Javadoc. */
  public JavadocModel getJavadocModel();
  
  /** @return the Debugger, which interfaces with the integrated debugger. */
  public Debugger getDebugger();
  
  /** @return the DocumentNavigator, which controls the document view. */
  public IDocumentNavigator<OpenDefinitionsDocument> getDocumentNavigator();
  
  public void setDocumentNavigator(IDocumentNavigator<OpenDefinitionsDocument> newnav);
  
  /** @return manager for breakpoint regions. */
  public RegionManager<Breakpoint> getBreakpointManager();
  
  /** @return manager for bookmark regions. */
  public RegionManager<MovingDocumentRegion> getBookmarkManager();
  
//  /** @return managers for find result regions. */
//  public List<RegionManager<MovingDocumentRegion>> getFindResultsManagers();
  
  /** @return new manager for find result regions. */
  public RegionManager<MovingDocumentRegion> createFindResultsManager();
  
  /** Dispose a manager for find result regions. 
   * @param rm the manager to be dispose of
   */
  public void removeFindResultsManager(RegionManager<MovingDocumentRegion> rm);
  
  /** @return manager for browser history regions. */
  public BrowserHistoryManager getBrowserHistoryManager();
  
  /** Add the current location to the browser history. */
  public void addToBrowserHistory();
  
//  //---------------------------- Interpreter --------------------------------//
//  /** Updates the security manager in DrJava. */
//  public void enableSecurityManager();
//  
//  /** Updates the security manager in DrJava. */
//  public void disableSecurityManager();
//  
  //---------------------------- File Management ----------------------------//
  
  /** Creates a new document in the definitions pane and adds it to the list of open documents.
    * @return The new open document
    */
  public OpenDefinitionsDocument newFile();

  /** Creates a new document in the definitions pane, containing the specified text, and
    * adds it to the list of open documents.
    * @param text for the new document
    * @return The new open document
    */
  public OpenDefinitionsDocument newFile(String text);
  
  /** Creates a new junit test case.
   * TODO: Move to JUnitModel?
   * @param name the name of the new test case
   * @param makeSetUp true iff an empty setUp() method should be included
   * @param makeTearDown true iff an empty tearDown() method should be included
   * @return the new open test case
   */
  public OpenDefinitionsDocument newTestCase(String name, boolean makeSetUp, boolean makeTearDown);
  
  /** Closes an open definitions document, prompting to save if
   * the document has been changed.  Returns whether the file
   * was successfully closed.
   * @param doc the document to be closed
   * @return true if the document was closed
   */
  public boolean closeFile(OpenDefinitionsDocument doc);
  
  /** Closes an open definitions document, without prompting to save if
   * the document has been changed.  Returns whether the file
   * was successfully closed.
   * @param doc the document to be closed
   * @return true if the document was closed
   */
  public boolean closeFileWithoutPrompt(OpenDefinitionsDocument doc);
  
  /** Attempts to close all open documents.
   * @return true if all documents were closed
   */
  public boolean closeAllFiles();

  /** This function closes a group of files assuming that the files are contiguous in the enumeration
    * provided by the document navigator. This assumption is used in selecting which remaining document
    * (if any) to activate.
    * <p>
    * The corner cases in which the file that is being closed had been externally
    * deleted have been addressed in a few places, namely DefaultGlobalModel.canAbandonFile()
    * and MainFrame.ModelListener.canAbandonFile().  If the DefinitionsDocument for the
    * OpenDefinitionsDocument being closed is not in the cache (see model.cache.DocumentCache)
    * then it is closed without prompting the user to save it.  If it is in the cache, then
    * we can successfully notify the user that the file is selected for closing and ask whether to
    * saveAs, close, or cancel.
    * @param docs the list od OpenDefinitionsDocuments to close
    * @return whether all files were closed
    */
  public boolean closeFiles(List<OpenDefinitionsDocument> docs);

  /** Opens all files in specified folder. If rec is true, open all files in 
   * the tree rooted at dir. 
   * @param dir the folder in which to open files
   * @param rec true if the open should be recursive; false otherwise
   * @param ext extension
   * @throws IOException if an IO operation fails
   * @throws OperationCanceledException if the operation was canceled
   * @throws AlreadyOpenException if the folder was already open
   */
  public void openFolder(File dir, boolean rec, String ext)
    throws IOException, OperationCanceledException, AlreadyOpenException;
  
  /** Saves all open documents, prompting when necessary. 
   * @param com a FileSaveSelector
   * @throws IOException if an IO operation fails
   */
  public void saveAllFiles(FileSaveSelector com) throws IOException;
  
  /**Creates a new project with specified project file and default values for other properties.
    * @param projFile the new project file (which does not yet exist in the file system).
    */
  public void createNewProject(File projFile);
  
  /** Configures a new project (created by createNewProject) and saves it to disk. 
   * @throws IOException if an IO operation fails
   */
  public void configNewProject() throws IOException;
  
  /** Writes the project file to disk
   * @param f where to save the project
   * @param info Extra view-related information that should be included in the project file
   * @throws IOException if an IO operation fails
   */
  public void saveProject(File f, HashMap<OpenDefinitionsDocument,DocumentInfoGetter> info) throws IOException;
  
  /** Reloads a project without writing to disk.
   * @param f project file; does not actually get touched
   * @param info Extra view-related information that should be included in the project file
   * @throws IOException if an IO operation fails
   */
  public void reloadProject(File f, HashMap<OpenDefinitionsDocument,DocumentInfoGetter> info) throws IOException;
  
  /** Formats a string pathname for use in the document navigator. 
   * @param path the path to be fixed
   * @return the re-formatted path
   * @throws IOException if an IO operation fails
   */
  public String fixPathForNavigator(String path) throws IOException;
  
  /** Gives the title of the source bin for the navigator
    * @return The text used for the source bin in the tree navigator
    */
  public String getSourceBinTitle();
  
  /** Gives the title of the external files bin for the navigator
    * @return The text used for the external files bin in the tree navigator
    */
  public String getExternalBinTitle();
  
  /** Gives the title of the aux files bin for the navigator
    * @return The text used for the aux files bin in the tree navigator
    */
  public String getAuxiliaryBinTitle();
  
  /** Adds a document to the list of auxiliary files. 
   * @param doc document to be added
   */
  public void addAuxiliaryFile(OpenDefinitionsDocument doc);
  
  /** Removes a document from the list of auxiliary files. 
   * @param doc document to be removed
   */
  public void removeAuxiliaryFile(OpenDefinitionsDocument doc);
  
  /** Parses out the given project file, sets up the state and other configurations
   * such as the Navigator and the classpath, and returns an array of files to open.
   * @param file The project file to parse
   * @throws IOException if an IO operation fails
   * @throws MalformedProjectFileException if one of the project files is malformed
   */
  public void openProject(File file) throws IOException, MalformedProjectFileException;
  
  /** Performs any needed operations on the model before closing the project 
   * and its files.  This is not responsible for actually closing the files 
   * since that is handled in MainFrame._closeProject()
   * @param quitting true if we're quitting; false otherwise
   */
  public void closeProject(boolean quitting);
  
  /** Searches for a file with the given name on the current source roots and the augmented classpath.
    * @param fileName Name of the source file to look for
    * @return the file corresponding to the given name, or null if it cannot be found
    */
  public File getSourceFile(String fileName);
  
  /** Searches for a file with the given name on the provided paths. 
   * Returns null if the file is not found.
   * @param fileName Name of the source file to look for
   * @param paths An array of directories to search
   * @return the file with the given name; null if it's not found
   */
  public File findFileInPaths(String fileName, Iterable<File> paths);
  
  /** @return a list of all sourceRoots for the open definitions documents, without duplicates. */
  public Iterable<File> getSourceRootSet();
  
//  /** Return the absolute path of the file with the given index, or "(untitled)" if no file exists. */
//  public String getDisplayFullPath(int index);
  
  /*------------------------------ Definitions ------------------------------*/
  
  /** @return the {@link javax.swing.text.EditorKit} implementation for use in the definitions pane. */
  public DefinitionsEditorKit getEditorKit();
  
  /** @return a DocumentIterator to allow navigating through open swing Documents.
   * TODO: remove ugly swing dependency.
   */
  public DocumentIterator getDocumentIterator();
  
  /** Re-runs the global listeners on the active document. */
  public void refreshActiveDocument();
  
  /*---------------------------------- I/O ----------------------------------*/
  
  /** @return the console document. */
  public ConsoleDocument getConsoleDocument();
  
  /** TODO: remove this swing dependency.
    * @return InteractionsDJDocument in use by the ConsoleDocument.
    */
  public InteractionsDJDocument getSwingConsoleDocument();
  
  /** Resets the console. Fires consoleReset() event. */
  public void resetConsole();
  
  /** Prints System.out to the DrJava console. This method may be safely 
   * called from outside the event thread. 
   * @param s string to be printed
   */
  public void systemOutPrint(String s);
  
  /** Prints System.err to the DrJava console. This method may be safely
   * called from outside the event thread.
   * @param s string to be printed
   */
  public void systemErrPrint(String s);
  
  /** Prints the given string to the DrJava console as an echo of System.in.  
   * This method may be safely called from outside the event thread.  
   * @param s string to be printed
   */
  public void systemInEcho(String s);
  
  //----------------------------- Interactions -----------------------------//
  
  /** @return the (toolkit-independent) interactions document. */
  public InteractionsDocument getInteractionsDocument();
  
  /** TODO: remove this swing dependency.
    * @return InteractionsDJDocument in use by the InteractionsDocument.
    */
  public InteractionsDJDocument getSwingInteractionsDocument();
  
  /** Clears and resets the interactions pane in the specified working 
   * directory. Convenience form of the following method. 
   * @param wd the working directory 
   */
  public void resetInteractions(File wd);
  
  /** Clears and resets the interactions pane in the specified working 
   * directory. Usually called by preceding method. 
   * Invoked by "Reset interactions" command (in MainFrame) and as part of 
   * other actions such as the run, project loading, compilation and unit 
   * testing commands.
   * @param wd the working directory
   * @param forceReset true if the reset should be forced (regardless of
   *        the state of things), false otherwise
   */
  public void resetInteractions(File wd, boolean forceReset);
  
  /** Interprets the current given text at the prompt in the interactions pane. */
  public void interpretCurrentInteraction();
  
  /** Returns the current classpath in use by the Interpreter JVM.
   * @return the current classpath in use by the Interpreter JVM
   */
  public Iterable<File> getInteractionsClassPath();
  
  // TODO: Move history methods to a more appropriate home.
  
  /** Interprets file selected in the FileOpenSelector. Assumes all strings 
   * have no trailing whitespace. Interprets the list of interactions as a 
   * single transaction so the first error aborts all processing.
   * @param selector a FileOpenSelector
   * @throws IOException if an IO operation fails
   */
  public void loadHistory(FileOpenSelector selector) throws IOException;
  
  /** Loads the history/histories from the given selector. 
   * @param selector a FileOpenSelector
   * @return the history in the form of an InteractionsScriptModel
   * @throws IOException if an IO operation fails
   * @throws OperationCanceledException if the operation was canceled
   */
  public InteractionsScriptModel loadHistoryAsScript(FileOpenSelector selector)
    throws IOException, OperationCanceledException;
  
  /** Clears the interactions history. */
  public void clearHistory();

  /** Save copy of Console or Interactions Pane to text file. 
   * @param doc the document to which to save the console
   * @param selector a FileSaveSelector
   * @throws IOException if an IO operation fails
   */
  public void saveConsoleCopy(ConsoleDocument doc, FileSaveSelector selector) throws IOException;
  
  /** Saves the unedited version of the current history to a file
   * @param selector File to save to
   * @throws IOException if an IO operation fails
   */
  public void saveHistory(FileSaveSelector selector) throws IOException;
  
  /** Saves the edited version of the current history to a file
   * @param selector File to save to
   * @param editedVersion Edited verison of the history which will be saved 
   *        to file instead of the lines saved in the history. The saved file 
   *        will still include any tags needed to recognize it as a saved 
   *        interactions file.
   * @throws IOException if an IO operation fails
   */
  public void saveHistory(FileSaveSelector selector, String editedVersion) throws IOException;
  
  /** @return the entire history as a String with semicolons as needed. */
  public String getHistoryAsStringWithSemicolons();
  
  /** @return the entire history as a String. */
  public String getHistoryAsString();
  
  //------------------------------- Debugger -------------------------------//
  
  /** Called when the debugger wants to print a message. 
   * @param s message to print
   */
  public void printDebugMessage(String s);
  
  /** @return an available port number to use for debugging the interactions JVM.
   * @throws IOException if unable to get a valid port number.
   */
  public int getDebugPort() throws IOException;
  
  //--------------------------------- Misc ---------------------------------//
  
  /** @return the class path to be used in all class-related operations.
   * TODO: Insure that this is used wherever appropriate.
   */
  public Iterable<File> getClassPath();

  /** @return the page format */
  public PageFormat getPageFormat();
  
  /** Sets the page format to the specified format.
   * @param format format to set
   */
  public void setPageFormat(PageFormat format);
  
  /** Exits the program.  Only quits if all documents are successfully closed. */
  public void quit();
  
  /** Halts the program immediately. */
  public void forceQuit();
  
  /** @return the document count */
  public int getDocumentCount();
  
  /** @return the number of compiler errors produced by the last compilation. */
  public int getNumCompilerErrors();
  
  /** Sets the number of compiler errors produced by the last compilation. 
   * @param num the number of compiler errors
   */
  public void setNumCompilerErrors(int num); 
  
  /** @param doc the document for which to get the ODD
   * @return an OOD given an AbstractDocumentInterface
   * CHECK IF NEEDED! 
   */
  public OpenDefinitionsDocument getODDForDocument(AbstractDocumentInterface doc);
  
  /** Returns a list of OpenDefinitionsDocuments that do not belong to the currently active project.<br>
   * If no project is active, all documents are returned.
   * @return list of all ODDs that do not belong to the current active project
   */
  public List<OpenDefinitionsDocument> getNonProjectDocuments();
  
  /** Returns a list of OpenDefinitionsDocuments that do belong to the currently active project.<br>
   * If no project is active, no documents are returned.
   * @return list of all ODDs that belong to the current active project
   */
  public List<OpenDefinitionsDocument> getProjectDocuments();
  
//  /** Compiles all open files (all files in project (??) in project mode) */
//  public void compileAll() throws IOException;
  
  /** @return true if the model has a project open, false otherwise. */
  public boolean isProjectActive();
  
//  /** junits all the appropriate files */
//  public void junitAll();
  
  /** @return the file that points to the current project file. Null if not currently in project view */
  public File getProjectFile();
  
  /** @return the directory that the class files should be stored after compilation. */
  public File[] getProjectFiles();
  
  /** @return the source root for the project. */
  public File getProjectRoot();
  
  /** Sets project file to specifed value; used in "Save Project As ..." 
   * command in MainFrame. 
   * @param f the file to set as the project file
   */
  public void setProjectFile(File f);
  
  /** Sets the source root for the project. 
   * @param f the file to set as the source root
   */
  public void setProjectRoot(File f);
  
  /** @return the directory that the class files should be stored after compilation. */
  public File getBuildDirectory();
  
  /** Sets the current build directory. 
   * @param f the file to set as the source root
   */
  public void setBuildDirectory(File f);
  
  /** @return autorefresh status of the project */
  public boolean getAutoRefreshStatus();
  
  /** Sets autorefresh status of the project 
   * @param b the autorefresh status to be set
   */
  public void setAutoRefreshStatus(boolean b);
  
  /** @return the stored preferences. */
  public Map<OptionParser<?>,String> getPreferencesStoredInProject();
  
  /** Set the preferences stored in the project. 
   * @param sp the preferences to be set
   */
  public void setPreferencesStoredInProject(Map<OptionParser<?>,String> sp);
  
  /** @return the working directory for the Master JVM. */
  public File getMasterWorkingDirectory();
  
  /** @return the working directory for the Slave JVM (only applied to project mode). */
  public File getWorkingDirectory();
  
  /** Sets the working directory for the Slave JVM (only applies to project mode). 
   * @param f the working directory to be set
   */
  public void setWorkingDirectory(File f);
  
  /** Sets the main file of the project. 
   * @param f the main class file to be set
   */
  public void setMainClass(String f);
  
  /** @return the main file for the project If not in project mode, returns null. */
  public String getMainClass();
  
  /** @return the File that contains the Main-Class. */
  public File getMainClassContainingFile();
  
  /** Returns only the project's extra classpaths.
    * @return The classpath entries loaded along with the project
    */
  public Iterable<AbsRelFile> getExtraClassPath();
  
  /** Sets the set of classpath entries to use as the projects set of classpath 
   * entries.  This is normally used by the project preferences.
   * @param cp the extra class path to be set
   */
  public void setExtraClassPath(Iterable<AbsRelFile> cp);
  
  /** Sets the create jar file of the project. 
   * @param f the jar file to be set
   */
  public void setCreateJarFile(File f);
  
  /** @return the create jar file for the project. If not in project mode, returns null. */
  public File getCreateJarFile();
  
  /** Sets the create jar flags of the project.
   * @param f the jar flags to be set
   */
  public void setCreateJarFlags(int f);
  
  /** @return the create jar file for the project. If not in project mode, returns 0. */
  public int getCreateJarFlags();
  
  /** @param f the file to be searched for
   * @return true the given file is in the current project file. 
   */
  public boolean inProject(File f);
  
  /** A file is in the project if the source root is the same as the project 
   * root. This means that project files must be saved in the source root. 
   * (we query the model through the model's state)
   * @param doc the doc to search for in the project path
   * @return true if doc is in the project path; false otherwise
   */
  public boolean inProjectPath(OpenDefinitionsDocument doc);
  
  /** Notifies the project state that the project has been changed. 
   * @param changed true if the project has been changed; false otherwise 
   */
  public void setProjectChanged(boolean changed);
  
  /** @return true if the project state has been changed */
  public boolean isProjectChanged();
  
  /** @return true iff no open document is out of sync with its primary class file. */
  public boolean hasOutOfSyncDocuments();
  
  /** @param lod the list of open documents to check 
   * @return true iff no document in given list is out of sync with its primary class file. 
   */
  public boolean hasOutOfSyncDocuments(List<OpenDefinitionsDocument> lod);
  
  /** @return list of open documents that are out of sync with their primary class files. */
  public List<OpenDefinitionsDocument> getOutOfSyncDocuments();
  
  /** @param lod the list of open documents from which to get those that are out of sync
   * @return list of open documents in given list that are out of sync with their primary class files. 
   */
  public List<OpenDefinitionsDocument> getOutOfSyncDocuments(List<OpenDefinitionsDocument> lod);
  
  /** Cleans the build directory. */
  public void cleanBuildDirectory();
  
  /** @return a list of class files. */
  public List<File> getClassFiles();
  
  /** Returns a collection of all documents currently open for editing.  This is equivalent to the results of 
    * getDocumentForFile for the set of all files for which isAlreadyOpen returns true.  The order of documents 
    * is the same as in the display of documents in the view.
    * @return a List of the open definitions documents.
    */
  public List<OpenDefinitionsDocument> getOpenDefinitionsDocuments();

  /** @return a collection of language level documents. */
  public List<OpenDefinitionsDocument> getLLOpenDefinitionsDocuments();

  /** @return a collection of auxiliary documents. */
  public List<OpenDefinitionsDocument> getAuxiliaryDocuments();  

  /** Checks if any open definitions documents have been modified since last being saved.
   * @return whether any documents have been modified
   */
  public boolean hasModifiedDocuments();
  
  /** Checks if any of the given documents have been modified since last being saved.
   * @param lod the list of documents to check for modifications
   * @return whether any documents have been modified
   */
  public boolean hasModifiedDocuments(List<OpenDefinitionsDocument> lod);
  
  /** Checks if any open definitions documents are untitled.
    * @return whether any documents are untitled
    */
  public boolean hasUntitledDocuments();
  
  /** Returns the OpenDefinitionsDocument for the specified File, opening a new 
   * copy if one is not already open.
   * @param file File contained by the document to be returned
   * @return OpenDefinitionsDocument containing file
   * @throws IOException if an IO operation fails
   */
  public OpenDefinitionsDocument getDocumentForFile(File file) throws IOException;
  
  /** @return the GlobalEventModifier attached to global model. */
  public GlobalEventNotifier getNotifier();
  
  /** @return the text of the custom manifest supplied for this project. */
  public String getCustomManifest();
  
  /** Sets the text of the custom manifest supplied for this project. 
   * @param manifest the text of the manifest to be set
   */
  public void setCustomManifest(String manifest);
}
