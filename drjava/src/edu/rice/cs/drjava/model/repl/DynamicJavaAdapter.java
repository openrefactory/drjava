/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.parser.wrapper.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

import edu.rice.cs.util.classloader.StickyClassLoader;
import edu.rice.cs.util.*;
import edu.rice.cs.drjava.DrJava;

/**
 * An implementation of the interpreter for the repl pane.
 * @version $Id$
 */
public class DynamicJavaAdapter implements JavaInterpreter {
  private koala.dynamicjava.interpreter.Interpreter _djInterpreter;

  /**
   * Constructor.
   */
  public DynamicJavaAdapter() {
    _djInterpreter = new InterpreterExtension();
    // Allow access to private fields/methods from interpreter!
    //_djInterpreter.setAccessible(true);
  }

  /**
   * Interprets a string as Java source.
   * @param s the string to interpret
   * @return the Object generated by the running of s
   */
  public Object interpret(String s) throws ExceptionReturnedException {
    boolean print = false;
    /**
     * trims the whitespace from beginning and end of string
     * checks the end to see if it is a semicolon
     * adds a semicolon if necessary
     */
    s = s.trim();
    if (!s.endsWith(";")) {
      s += ";";
      print = true;
    }

    StringReader reader = new StringReader(s);
    try {
      Object result = _djInterpreter.interpret(reader, "DrJava");
      if (print)
        return result; 
      else 
        return JavaInterpreter.NO_RESULT;
    }
    catch (InterpreterException ie) {
      Throwable cause = ie.getException();
      if (cause instanceof ThrownException) {
        cause = ((ThrownException) cause).getException();
      }
      else if (cause instanceof CatchedExceptionError) {
        cause = ((CatchedExceptionError) cause).getException();
      }

      throw new ExceptionReturnedException(cause);
    }
    catch (CatchedExceptionError cee) {
      throw new ExceptionReturnedException(cee.getException());
    }
    catch (InterpreterInterruptedException iie) {
      return JavaInterpreter.NO_RESULT;
    }
    catch (ExitingNotAllowedException enae) {
      return JavaInterpreter.NO_RESULT;
    }
    catch (Throwable ie) {
      //System.err.print(new Date() + ": ");
      //System.err.println(ie);
      //ie.printStackTrace();
      //System.err.println("\n");
      //throw new RuntimeException(ie.toString());

      throw new ExceptionReturnedException(ie);
    }
  }

  /**
   * Adds a path to the current classpath.
   * @param path the path to add
   */
  public void addClassPath(String path) {
    //DrJava.consoleErr().println("Added class path: " + path);
    _djInterpreter.addClassPath(path);
  }

  /**
   * Set the scope for unqualified names to the given package.
   * @param packageName Package to assume scope of.
   */
  public void setPackageScope(String packageName) {
    StringReader reader = new StringReader("package " + packageName + ";");
    _djInterpreter.interpret(reader, "DrJava");
  }
  
  /**
   * Returns the value of the variable with the given name in
   * the interpreter.
   * @param name Name of the variable
   * @return Value of the variable
   */
  public Object getVariable(String name) {
    return _djInterpreter.getVariable(name);
  }
  
  /**
   * Assigns the given value to the given name in the interpreter.
   * If type == null, we assume that the type of this variable
   * has not been loaded so we set it to Object.
   * @param name Name of the variable
   * @param value Value to assign
   * @param type the type of the variable
   */
  public void defineVariable(String name, Object value, Class type) {
    if (type == null) {
      type = java.lang.Object.class;
    }
    ((TreeInterpreter)_djInterpreter).defineVariable(name, value, type);
  }
  
  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value Value to assign
   */
  public void defineVariable(String name, Object value) {
    ((TreeInterpreter)_djInterpreter).defineVariable(name, value);
  }
  
  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value boolean to assign
   */
  public void defineVariable(String name, boolean value) {
    ((TreeInterpreter)_djInterpreter).defineVariable(name, value);
  }
  
  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value byte to assign
   */
  public void defineVariable(String name, byte value) {
    ((TreeInterpreter)_djInterpreter).defineVariable(name, value);
  }

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value char to assign
   */
  public void defineVariable(String name, char value) {
    ((TreeInterpreter)_djInterpreter).defineVariable(name, value);
  }

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value double to assign
   */
  public void defineVariable(String name, double value) {
    ((TreeInterpreter)_djInterpreter).defineVariable(name, value);
  }
  
  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value float to assign
   */
  public void defineVariable(String name, float value) {
    ((TreeInterpreter)_djInterpreter).defineVariable(name, value);
  }

  
  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value int to assign
   */
  public void defineVariable(String name, int value) {
    ((TreeInterpreter)_djInterpreter).defineVariable(name, value);
  }

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value long to assign
   */
  public void defineVariable(String name, long value) {
    ((TreeInterpreter)_djInterpreter).defineVariable(name, value);
  }

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value short to assign
   */
  public void defineVariable(String name, short value) {
    ((TreeInterpreter)_djInterpreter).defineVariable(name, value);
  }
  
  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value Value to assign
   */
  public void defineConstant(String name, Object value) {
    ((InterpreterExtension)_djInterpreter).defineConstant(name, value);
  }

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value boolean to assign
   */
  public void defineConstant(String name, boolean value) {
    ((InterpreterExtension)_djInterpreter).defineConstant(name, value);
  }

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value byte to assign
   */
  public void defineConstant(String name, byte value) {
    ((InterpreterExtension)_djInterpreter).defineConstant(name, value);
  }

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value char to assign
   */
  public void defineConstant(String name, char value) {
    ((InterpreterExtension)_djInterpreter).defineConstant(name, value);
  }

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value double to assign
   */
  public void defineConstant(String name, double value) {
    ((InterpreterExtension)_djInterpreter).defineConstant(name, value);
  }

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value float to assign
   */
  public void defineConstant(String name, float value) {
    ((InterpreterExtension)_djInterpreter).defineConstant(name, value);
  }

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value int to assign
   */
  public void defineConstant(String name, int value) {
    ((InterpreterExtension)_djInterpreter).defineConstant(name, value);
  }

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value long to assign
   */
  public void defineConstant(String name, long value) {
    ((InterpreterExtension)_djInterpreter).defineConstant(name, value);
  }
  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value short to assign
   */
  public void defineConstant(String name, short value) {
    ((InterpreterExtension)_djInterpreter).defineConstant(name, value);
  }
  
  
  /**
   * Sets whether protected and private variables should be accessible in
   * the interpreter.
   * @param accessible Whether protected and private variable are accessible
   */
  public void setPrivateAccessible(boolean accessible) {
    _djInterpreter.setAccessible(accessible);
  }

  /**
   * Factory method to make a new NameVisitor.
   * @param context the context
   * @return visitor the visitor
   */
  public NameVisitor makeNameVisitor(Context context) {
    return new NameVisitor(context);
  }
  
  /**
   * Factory method to make a new TypeChecker.
   * @param context the context
   * @return visitor the visitor
   */
  public TypeChecker makeTypeChecker(Context context) {
    return new TypeChecker(context);
  }
  
  /**
   * Factory method to make a new EvaluationVisitor.
   * @param context the context
   * @return visitor the visitor
   */
  public EvaluationVisitor makeEvaluationVisitor(Context context) {
    return new EvaluationVisitorExtension(context);
  }

  /**
   * Processes the tree before evaluating it, if necessary.
   * @param node Tree to process
   */
  public Node processTree(Node node) {
    return node;
  }
  
  public GlobalContext makeGlobalContext(TreeInterpreter i) {
    return new GlobalContext(i);
  }
  
  /**
   * An extension of DynamicJava's interpreter that makes sure classes are
   * not loaded by the system class loader (when possible) so that future
   * interpreters will be able to reload the classes.  This extension also
   * ensures that classes on "extra.classpath" will be loaded if referenced
   * by user defined classes.  (Without this, classes on "extra.classpath"
   * can only be referred to directly, and cannot be extended, etc.)
   * <p>
   * 
   * We also override the evaluation visitor to allow the interpreter to be
   * interrupted and to return NO_RESULT if there was no result.
   */
  public class InterpreterExtension extends TreeInterpreter {

    /**
     * Constructor.
     */
    public InterpreterExtension() {
      super(new JavaCCParserFactory());
      
      classLoader = new ClassLoaderExtension(this);
      // We have to reinitialize these variables because they automatically
      // fetch pointers to classLoader in their constructors.
      nameVisitorContext = makeGlobalContext(this);
      ClassLoaderContainer clc = new ClassLoaderContainer() {
        public ClassLoader getClassLoader() {
          return classLoader;
        }
      };
      nameVisitorContext.setAdditionalClassLoaderContainer(clc);
      checkVisitorContext = makeGlobalContext(this);
      checkVisitorContext.setAdditionalClassLoaderContainer(clc);
      evalVisitorContext = makeGlobalContext(this);
      evalVisitorContext.setAdditionalClassLoaderContainer(clc);
      //System.err.println("set loader: " + classLoader);
      
    }

    /**
     * Extends the interpret method to deal with possible interrupted
     * exceptions.
     * Unfortunately we have to copy all of this method to override it.
     * @param is    the reader from which the statements are read
     * @param fname the name of the parsed stream
     * @return the result of the evaluation of the last statement
     */
    public Object interpret(Reader r, String fname) throws InterpreterException
    {
      try {
        SourceCodeParser p = parserFactory.createParser(r, fname);
        List    statements = p.parseStream();
        ListIterator    it = statements.listIterator();
        Object result = JavaInterpreter.NO_RESULT;

        while (it.hasNext()) {
          Node n = (Node)it.next();
          
          // Process, if necessary
          n = processTree(n);
          
          Visitor v = makeNameVisitor(nameVisitorContext);
          Object o = n.acceptVisitor(v);
          if (o != null) {
            n = (Node)o;
          }

          v = makeTypeChecker(checkVisitorContext);
          n.acceptVisitor(v);

          evalVisitorContext.defineVariables
            (checkVisitorContext.getCurrentScopeVariables());

          v = makeEvaluationVisitor(evalVisitorContext);
          result = n.acceptVisitor(v);
        }
        
        if (result instanceof String) {
          result = "\"" + result + "\"";
        }
        else if (result instanceof Character) {
          result = "'" + result + "'";
        }

        return result;
      }
      catch (ExecutionError e) {
        throw new InterpreterException(e);
      }
      catch (ParseError e) {
        throw new InteractionsException("There was a syntax error in the " +
                                        "previous input.");
        //throw new InterpreterException(e);
      }
    }
    
    /**
     * Assigns the given value to the given name in the interpreter.
     * @param name Name of the variable
     * @param value Value to assign
     */
    public void defineConstant(String name, Object value) {
      Class c = (value == null) ? null : value.getClass();
      nameVisitorContext.defineConstant(name, c);
      checkVisitorContext.defineConstant(name, c);
      evalVisitorContext.defineConstant(name, value);
    }
    
    /**
     * Assigns the given value to the given name as a constant in the interpreter.
     * @param name Name of the variable
     * @param value boolean to assign
     */
    public void defineConstant(String name, boolean value) {
      Class c = boolean.class;
      nameVisitorContext.defineConstant(name, c);
      checkVisitorContext.defineConstant(name, c);
      evalVisitorContext.defineConstant(name, new Boolean(value));
    }
    
    /**
     * Assigns the given value to the given name as a constant in the interpreter.
     * @param name Name of the variable
     * @param value byte to assign
     */
    public void defineConstant(String name, byte value) {
      Class c = byte.class;
      nameVisitorContext.defineConstant(name, c);
      checkVisitorContext.defineConstant(name, c);
      evalVisitorContext.defineConstant(name, new Byte(value));
    }
    
    /**
     * Assigns the given value to the given name as a constant in the interpreter.
     * @param name Name of the variable
     * @param value char to assign
     */
    public void defineConstant(String name, char value) {
      Class c = char.class;
      nameVisitorContext.defineConstant(name, c);
      checkVisitorContext.defineConstant(name, c);
      evalVisitorContext.defineConstant(name, new Character(value));
    }
    
    /**
     * Assigns the given value to the given name as a constant in the interpreter.
     * @param name Name of the variable
     * @param value double to assign
     */
    public void defineConstant(String name, double value) {
      Class c = double.class;
      nameVisitorContext.defineConstant(name, c);
      checkVisitorContext.defineConstant(name, c);
      evalVisitorContext.defineConstant(name, new Double(value));
    }
    
    /**
     * Assigns the given value to the given name as a constant in the interpreter.
     * @param name Name of the variable
     * @param value float to assign
     */
    public void defineConstant(String name, float value) {
      Class c = float.class;
      nameVisitorContext.defineConstant(name, c);
      checkVisitorContext.defineConstant(name, c);
      evalVisitorContext.defineConstant(name, new Float(value));
    }
    
    /**
     * Assigns the given value to the given name as a constant in the interpreter.
     * @param name Name of the variable
     * @param value int to assign
     */
    public void defineConstant(String name, int value) {
      Class c = int.class;
      nameVisitorContext.defineConstant(name, c);
      checkVisitorContext.defineConstant(name, c);
      evalVisitorContext.defineConstant(name, new Integer(value));
    }
    
    /**
     * Assigns the given value to the given name as a constant in the interpreter.
     * @param name Name of the variable
     * @param value long to assign
     */
    public void defineConstant(String name, long value) {
      Class c = long.class;
      nameVisitorContext.defineConstant(name, c);
      checkVisitorContext.defineConstant(name, c);
      evalVisitorContext.defineConstant(name, new Long(value));
    }
    /**
     * Assigns the given value to the given name as a constant in the interpreter.
     * @param name Name of the variable
     * @param value short to assign
     */
    public void defineConstant(String name, short value) {
      Class c = short.class;
      nameVisitorContext.defineConstant(name, c);
      checkVisitorContext.defineConstant(name, c);
      evalVisitorContext.defineConstant(name, new Short(value));
    }
  }

  /**
   * A class loader for the interpreter.
   */
  public static class ClassLoaderExtension extends TreeClassLoader {
    private static boolean classLoaderCreated = false;
    
    private static StickyClassLoader _stickyLoader;

    /**
     * Constructor.
     * @param         Interpreter i
     */
    public ClassLoaderExtension(koala.dynamicjava.interpreter.Interpreter i) {
      super(i);
      // The protected variable classLoader contains the class loader to use
      // to find classes. When a new class path is added to the loader,
      // it adds on an auxilary classloader and chains the old classLoader
      // onto the end.
      // Here we initialize classLoader to be the system class loader.
      classLoader = getClass().getClassLoader();

      // don't load the dynamic java stuff using the sticky loader!
      // without this, interpreter-defined classes don't work.
      String[] excludes = {
        "edu.rice.cs.drjava.model.repl.DynamicJavaAdapter$InterpreterExtension",
        "edu.rice.cs.drjava.model.repl.DynamicJavaAdapter$ClassLoaderExtension"
      };

      if (!classLoaderCreated) {
        _stickyLoader = new StickyClassLoader(this,
                                              getClass().getClassLoader(),
                                              excludes);
        classLoaderCreated = true;
      }

      // we will use this to getResource classes
    }

    /**
     * Adds an URL to the class path.  DynamicJava's version of this creates a
     * new URLClassLoader with the given URL, using the old loader as a parent.
     * This seems to cause problems for us in certain cases, such as accessing
     * static fields or methods in a class that extends a superclass which is
     * loaded by "child" classloader...
     * 
     * Instead, we'll replace the old URLClassLoader with a new one containing
     * all the known URLs.
     *
     * (I don't know if this really works yet, so I'm not including it in 
     * the current release.  CSR, 3-13-2003)
     *
    public void addURL(URL url) {
      if (classLoader == null) {
        classLoader = new URLClassLoader(new URL[] { url });
      }
      else if (classLoader instanceof URLClassLoader) {
        URL[] oldURLs = ((URLClassLoader)classLoader).getURLs();
        URL[] newURLs = new URL[oldURLs.length + 1];
        System.arraycopy(oldURLs, 0, newURLs, 0, oldURLs.length);
        newURLs[oldURLs.length] = url;
        
        // Create a new class loader with all the URLs
        classLoader = new URLClassLoader(newURLs);
      }
      else {
        classLoader = new URLClassLoader(new URL[] { url }, classLoader);
      }
    }*/

    /*
    public Class defineClass(String name, byte[] code)  {
      File file = new File("debug-" + name + ".class");

      try {
        FileOutputStream out = new FileOutputStream(file);
        out.write(code);
        out.close();
        DrJava.consoleErr().println("debug class " + name + " to " + file.getAbsolutePath());
      }
      catch (Throwable t) {}
      
      Class c = super.defineClass(name, code);
      return c;
    }
    */

    /**
     * Delegates all resource requests to {@link #classLoader}.
     * This method is called by the {@link StickyClassLoader}.
     */
    public URL getResource(String name) {
      return classLoader.getResource(name);
    }

    protected Class loadClass(String name, boolean resolve)
      throws ClassNotFoundException
    {
      Class clazz;

      // check the cache
      if (classes.containsKey(name)) {
        clazz = (Class) classes.get(name);
      }
      else {
        try {
          clazz = _stickyLoader.loadClass(name);
        }
        catch (ClassNotFoundException e) {
          // If it exceptions, just fall through to here to try the interpreter.
          // If all else fails, try loading the class through the interpreter.
          // That's used for classes defined in the interpreter.
          clazz = interpreter.loadClass(name);
        }
      }

      if (resolve) {
        resolveClass(clazz);
      }

      return clazz;
    }
  }
}
