/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.citadel.lite;

/**
 *
 * @author matt
 */
public interface CitadelCallback {
    void message(String msgNum, short isNew);
    void finishedList();
}
