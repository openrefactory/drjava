/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import edu.rice.cs.drjava.model.definitions.InvalidPackageException; 
import edu.rice.cs.util.swing.Utilities;
import java.io.*;

import javax.swing.text.BadLocationException;

/** Tests to ensure that compilation succeeds when expected.  Every test in this class is run for *each* of the
  * available compilers.
  * @version $Id: GlobalModelCompileSuccessTest.java 5716 2012-09-28 02:40:33Z wdforson $
  */
public final class GlobalModelCompileSuccessTest extends GlobalModelCompileSuccessTestCase {

  /** Tests calling compileAll with different source roots works.  
    * NOTE: this "feature" is no longer supported; multiple source roots is flagged as a compilation error.  This test 
    * has been revised to expect an error.*/
  public void testCompileAllDifferentSourceRoots() throws BadLocationException, IOException, InterruptedException {
//    System.err.println("testCompileAllDifferentSourceRoots() compiler =" + _name());
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file1 = new File(aDir, "DrScalaTestFoo.scala");
    saveFile(doc, new FileSelector(file1));
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    final File file2 = new File(bDir, "DrScalaTestBar.scala");
    saveFile(doc2, new FileSelector(file2));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    _model.getCompilerModel().compileAll();
    
    Utilities.clearEventQueue();
    
    // Compile should succeed with multiple source roots
    assertEquals("compile succeeded despite multiple source roots", 0, _model.getCompilerModel().getNumErrors());
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();

    // Make sure .class files exist for the first file in expected place
    File compiled = classForScala(file2, "DrScalaTestBar");
    System.err.println("Class file for DrScalaTestBar = " + compiled);
    assertTrue(_name() + "Bar Class file exists after compile", compiled.exists());
    // Scalac does not respond to null destination by placing each class file in corresponding source file's folder
//    File compiled2 = classForScala(file2, "DrSclaTestFoo");
//    assertTrue(_name() + "Foo Class file doesn't exist after compile", ! compiled2.exists());
    _model.removeListener(listener);
  }
  

  /** Test that one compiled file can depend on the other and that when a keyword
    * is part of a field name, the file will compile.
    * We compile DrScalaTestFoo and then DrScalaTestFoo2 (which extends
    * DrScalaTestFoo). This shows that the compiler successfully found
    * DrScalaTestFoo2 when compiling DrScalaTestFoo.
    * Doesn't reset interactions because no interpretations are performed.
    */
  public void testCompileClassPathOKDefaultPackage() throws BadLocationException, IOException, InterruptedException {
//    System.out.println("testCompileClasspathOKDefaultPackage()");
    // Create/compile foo, assuming it works
    OpenDefinitionsDocument doc1 = setupDocument(FOO_PACKAGE_AS_PART_OF_FIELD);
    final File fooFile = new File(_tempDir, "DrScalaTestFoo.scala");
    
    saveFile(doc1, new FileSelector(fooFile));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    testStartCompile(doc1);
    listener.waitCompileDone();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    _model.removeListener(listener);

    OpenDefinitionsDocument doc2 = setupDocument(FOO2_EXTENDS_FOO_TEXT);
    final File foo2File = new File(_tempDir, "DrScalaTestFoo2.scala");
    saveFile(doc2, new FileSelector(foo2File));

    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener();
    _model.addListener(listener2);
    testStartCompile(doc2);
    listener2.waitCompileDone();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertCompileErrorsPresent(_name(), false);
    listener2.checkCompileOccurred();

    // Make sure .class exists
    File compiled = classForScala(foo2File, "DrScalaTestFoo2");
    assertTrue(_name() + "Class file doesn't exist after compile",
               compiled.exists());
    _model.removeListener(listener2);
  }

  /** Test that one compiled file can depend on the other.
   * We compile a.DrScalaTestFoo and then b.DrScalaTestFoo2 (which extends
   * DrScalaTestFoo). This shows that the compiler successfully found
   * DrScalaTestFoo2 when compiling DrScalaTestFoo.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileClassPathOKDifferentPackages() throws BadLocationException, IOException, InterruptedException,
    InvalidPackageException {
//    System.out.println("testCompileClasspathOKDifferentPackages()");
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();

    // Create/compile foo, assuming it works
    // foo must be public and in DrScalaTestFoo.java!
    OpenDefinitionsDocument doc1 = setupDocument("package a\n" + FOO_TEXT);
    final File fooFile = new File(aDir, "DrScalaTestFoo.scala");
//    System.err.println("fooFile = " + fooFile.getCanonicalPath());
    saveFile(doc1, new FileSelector(fooFile));
    // _packageName must be updated on save
    assertEquals("Check package name of doc1", "a", ((AbstractGlobalModel.ConcreteOpenDefDoc) doc1)._packageName); 
//    System.err.println("doc1 = " + doc1);
//    System.err.println("doc1 has source root " + doc1.getSourceRoot());
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    
    testStartCompile(doc1);
    
    listener.waitCompileDone();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    _model.removeListener(listener);
    
    OpenDefinitionsDocument doc2 =
      setupDocument("package b\n" + "import a.DrScalaTestFoo\n" + FOO2_EXTENDS_FOO_TEXT);
    final File foo2File = new File(bDir, "DrScalaTestFoo2.scala");
//    System.err.println("foo2File = " + foo2File.getCanonicalPath());
    saveFile(doc2, new FileSelector(foo2File));
    // _packageName must be updated on save
    assertEquals("Check packangeName of doc2", "b", ((AbstractGlobalModel.ConcreteOpenDefDoc) doc2)._packageName); 
//    System.err.println("doc2 = " + doc2);

    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener();
    _model.addListener(listener2);
    
    testStartCompile(doc2);
    listener2.waitCompileDone();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertCompileErrorsPresent(_name(), false);
    listener2.checkCompileOccurred();
    _model.removeListener(listener2);

    // Make sure .class exists
    File compiled = classForScala(foo2File, "DrScalaTestFoo2");
    assertTrue(_name() + "Class file doesn't exist after compile",
               compiled.exists());
  }
}
