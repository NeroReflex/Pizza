/*
 *    Pizza IRC Bot (for pierotofy.it community)
 *    Copyright (C) 2016 Benato Denis
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pizza;

import java.util.Vector;
import org.python.util.PythonInterpreter; 
import org.python.core.*;

/**
 * Un trancio creato da uno script in python
 *
 * @author Benato Denis <benato.denis96@gmail.com>
 */
public class TrancioPython extends Trancio {
    
    String src;
    
    public TrancioPython(String sorgente) {
        this.src = sorgente;
    }
    
    protected String onCall(String user, Vector<String> args) {
        //crea l'interprete che eseguirà lo script
        PythonInterpreter instance = new PythonInterpreter();
        instance.compile(this.src);
        
        
        return "";
    }
}
