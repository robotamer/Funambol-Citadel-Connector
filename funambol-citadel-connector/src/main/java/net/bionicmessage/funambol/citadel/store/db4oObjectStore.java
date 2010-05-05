/*
 * CitadelSourceAdmin.java
 *
 * Created on May 9, 2008, 10:45 PM
 * Funambol Citadel Connector
 * (C) 2007-2008 Mathew McBride
 * http://bionicmessage.net
 * 
 * Portions of code may come from: 
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2003 - 2007 Funambol, Inc.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */
package net.bionicmessage.funambol.citadel.store;

import com.db4o.*;
import com.db4o.config.Configuration;
import com.db4o.query.Predicate;
import com.db4o.query.Query;
import com.db4o.ta.TransparentPersistenceSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author matt
 */
public class db4oObjectStore {

    protected ObjectContainer objContainer = null;
    protected String dbLocation = null;

    public db4oObjectStore(String location) {
        dbLocation = location;
    }

    public void init() throws Exception {
        Configuration db4oconfig = 
                com.db4o.Db4o.newConfiguration();
        db4oconfig.objectClass(CitadelMailObject.class).objectField("ctdlMessagePointer").indexed(true);
        db4oconfig.add(new TransparentPersistenceSupport());
        objContainer = Db4o.openFile(db4oconfig, dbLocation);
    }

    public void storeObject(CitadelMailObject obj) {
        objContainer.store(obj);
    }
    public void deleteObject(CitadelMailObject obj) {
        objContainer.delete(obj);
    }
    /**
     * Return all mail in the specified room
     * @param room The room to search in
     * @return A collection of Citadel mail objects for that room
     */
    public List<CitadelMailObject> getMailInRoom(String room) {
        final String matchRoom = room;
        Predicate<CitadelMailObject> roomPred = new Predicate<CitadelMailObject>() {

            @Override
            public boolean match(CitadelMailObject cmo) {
                return cmo.getCtdlMessageRoom().equals(matchRoom);
            }
        };
        List<CitadelMailObject> objList = (List<CitadelMailObject>) objContainer.query(roomPred);
        return objList;
    }

    public List<CitadelMailObject> getMailInRoomFromTime(String room, long time) {
        final String matchRoom = room;
        final long gtTime = time;
        Predicate<CitadelMailObject> roomTimePred = new Predicate<CitadelMailObject>() {

            @Override
            public boolean match(CitadelMailObject cmo) {
                return (cmo.getCtdlMessageRoom().equals(matchRoom) && (cmo.getTime().getTime() >= gtTime));
            }

        };
        List<CitadelMailObject> objList = (List<CitadelMailObject>) objContainer.query(roomTimePred);
        return objList;
    }
    public List<String> getMailPointersInRoomFromTime(String room, long time) {
        List<CitadelMailObject> mailList = this.getMailInRoomFromTime(room, time);
        ArrayList<String> list = new ArrayList(mailList.size());
        for(CitadelMailObject cmo: mailList) {
            String pointer = Long.toString(cmo.getCtdlMessagePointer());
            list.add(pointer);
        }
        return list;
    }
    /**
     * Return Citadel message pointers (long-ids) for each message in the 
     * specified room
     * @param room The room to search in
     * @return A list of message pointers in the specified room.
     */
    public List<String> getMessagePointersInRoom(String room) {
        List<CitadelMailObject> objsInRoom = getMailInRoom(room);
        ArrayList<String> listOfPointers = new ArrayList<String>(objsInRoom.size());
        Iterator<CitadelMailObject> objList = objsInRoom.iterator();
        while(objList.hasNext()) {
            CitadelMailObject cmo = objList.next();
            String msgPointer = Long.toString(cmo.getCtdlMessagePointer());
            listOfPointers.add(msgPointer);
        }
        return listOfPointers;
    }


    /** Return a list of all the pointers we have */
    public List<String> getAllMessagePointers() {
        List<CitadelMailObject> allObjs =
                (List<CitadelMailObject>) objContainer.query(CitadelMailObject.class);
        Iterator<CitadelMailObject> objList = allObjs.iterator();
        ArrayList<String> listOfPointers = new ArrayList<String>(allObjs.size());
        while(objList.hasNext()) {
            CitadelMailObject cmo = objList.next();
            String msgPointer = Long.toString(cmo.getCtdlMessagePointer());
            listOfPointers.add(msgPointer);
        }
        return listOfPointers;
    }
    /**
     * Return a stored message by its message pointer
     * @param msgId Message pointer, in String form to search for
     * @return Mail object, if it exists, null if not.
     */
    public CitadelMailObject getMailByMessagePointer(String msgId) {
        final long searchMsgId = Long.parseLong(msgId);
        /* Predicate<CitadelMailObject> msgIdPred = new Predicate<CitadelMailObject>() {
            @Override
            public boolean match(CitadelMailObject cmo) {
                return cmo.getCtdlMessagePointer() == searchMsgId;
            }
        };
        List<CitadelMailObject> cmo = objContainer.query(msgIdPred);
        if (cmo.size()>0)
            return cmo.get(0); */
        Query query = objContainer.query();
        query.constrain(CitadelMailObject.class);
        query.descend("ctdlMessagePointer").constrain(searchMsgId);
        ObjectSet<CitadelMailObject> result = query.execute();
        if (result.size() > 0)
            return result.get(0);
        return null;
    }
    public void close() throws Exception {
        objContainer.close();
    }
}
