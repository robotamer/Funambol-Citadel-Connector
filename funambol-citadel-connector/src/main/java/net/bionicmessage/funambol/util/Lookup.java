/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bionicmessage.funambol.util;

/**
 *
 * @author matt
 */
public class Lookup {

    public static boolean isInArray(Object[] array, Object obj) {
        if (array == null)
            return false;
        for (int i = 0; i < array.length; i++) {
            Object object = array[i];
            if (obj.equals(object)) {
                return true;
            }
        }
        return false;
    }
}
