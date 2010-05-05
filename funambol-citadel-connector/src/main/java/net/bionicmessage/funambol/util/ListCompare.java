/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.bionicmessage.funambol.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author matt
 */
public class ListCompare {
    public static List disjunction(List A, List B) {
        List notInB = new ArrayList();
        for(Object a : A) {
            if (!B.contains(a)) {
                notInB.add(a);
            }
        }
        return notInB;
    }
    public static List intersection(List A, List B) {
        List intersect = new ArrayList(A.size());
        for (Object a: A) {
            if (B.contains(a)) {
                intersect.add(a);
            }
        }
        return intersect;
    }
}
