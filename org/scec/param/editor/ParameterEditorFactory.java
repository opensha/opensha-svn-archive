package org.scec.param.editor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.scec.param.ParameterAPI;
import org.scec.exceptions.ParameterException;
import org.scec.param.ParameterConstraintAPI;

// Fix - Needs more comments

/**
 * <b>Title:</b> ParameterEditorFactory<p>
 *
 * <b>Description:</b> This factory is used to create the appropiate Editor for a Parameter based on the
 * String type. This class uses a few rules to generate the complete package and class name of the
 * editor. This class is used by the ParameterListEditor and makes it so you can create new Parameter
 * classes without having to recompile the ParameterListEditor or this Foactory class.<p>
 *
 * <b>Note:</b> This class is currently uses only static functions and variables. We may need to change this
 * in the future if many clients try to set the searchPaths. No synchronization has been built in yet. Susceptable
 * to multiple threads changing the searchPaths.<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class ParameterEditorFactory {

    /* *******************/
    /** @todo  Variables */
    /* *******************/

    /* Debbuging variables */
    protected final static String C = "ParameterEditorFactory";
    protected final static boolean D = false;

    protected final static String DEFAULT_PATH = "org.scec.param.editor";
    protected static String[] searchPaths;


    private ParameterEditorFactory() { }

    public static void setSearchPaths(String[] searchPaths){
        ParameterEditorFactory.searchPaths = searchPaths;
    }
    public static String[] getSearchPaths(){ return searchPaths; }

    public static ParameterEditor getEditor(ParameterAPI param) throws ParameterException {

        // Debugging
        String S = C + ": getEditor(): ";
        if(D) System.out.println('\n' + S + "Starting");

        // Initial Type
        String type = param.getType();
        String name = param.getName();

        if(D) System.out.println(S + "Type = " + type);
        if(D) System.out.println(S + "Name = " + name);


        // Build full classname of the editor
        Class c = getClass(type);

        // Instantiate instance
        Object obj = getClassInstance(c);

        // Cast to ParameterEditor and add param to editor
        if(obj instanceof ParameterEditor){
            ParameterEditor editor = (ParameterEditor)obj;
            editor.setParameter(param);
            return editor;
        }
        else{ throw new ParameterException(S + "Created class doesn't extend AbstractParameterEditor: " + c.getName()); }


    }

    /**
     * The Class class of the editor found on the
     * search path is instantiated with a no-argument constructor
     */
    private static Object getClassInstance(Class c) throws ParameterException{

        // Debugging
        String S = C + ": getClassInstance(): ";

        try{

            // Create Constructor class instance and instantiate new class
            // equivalent to new org.scec.sha.param.editor.StringEditor()
            Constructor con = c.getConstructor( new Class[]{} );
            Object obj = con.newInstance( new Object[]{} );

            return obj;
        }
        catch(NoSuchMethodException e){ throw new ParameterException(S + e.toString() ); }
        catch(InvocationTargetException e){ throw new ParameterException(S + e.toString() ); }
        catch(IllegalAccessException e){ throw new ParameterException(S + e.toString() ); }
        catch(InstantiationException e){ throw new ParameterException(S + e.toString() ); }


    }

    /**
     * Locates Editors by prepending a package name to the type, then adding Editor to
     * end. Default path checked is org.scec.param.editor. If the editor is not
     * found in here, an attempt is made in each path set in the user defined
     * searchPaths array. The first match is returned. If none found a Parameter
     * Exception is thrown
     */
    private static Class getClass(String shortClassName) throws ParameterException{

        // Debugging
        String S = C + ": getClass(): ";
        String classname = "";

        if( ( searchPaths != null) || (searchPaths.length > 0) ){

            for(int i = 0; i < searchPaths.length; i++){

                classname = searchPaths[i]  + '.' + shortClassName + "Editor";

                try{

                    // Create the Class class - i.e. static reflector class
                    // into Editor class methods, constructors, fields
                    Class c = Class.forName(classname);
                    if(D) System.out.println(S + "Class = " + classname);

                    return c;

                }
                catch(ClassNotFoundException e){} // Can't find in path, try next path

            }

        }

        StringBuffer b = new StringBuffer();
        b.append(S + "Failed package names search path\n");

        if( ( searchPaths != null) || (searchPaths.length > 0) ){
            for(int i = 0; i < searchPaths.length; i++){
                b.append(searchPaths[i] + '\n');
            }
        }


        System.out.println( b.toString() );

        // Can't find in any paths at all
        throw new ParameterException(S + "Unable to locate editor in search paths: " + shortClassName + "Editor");

    }
}
