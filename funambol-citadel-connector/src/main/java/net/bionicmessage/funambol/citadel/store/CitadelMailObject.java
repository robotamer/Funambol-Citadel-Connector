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

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.collections.ArrayList4;
import com.db4o.ta.Activatable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.citadel.lite.CtdlMessage;
import sun.awt.image.BufImgSurfaceData;

/**
 *
 * @author matt
 */
public class CitadelMailObject implements Activatable {

    protected long ctdlMessagePointer;
    protected String ctdlMessageRoom;
    protected Hashtable properties = null;
    protected String data;
    protected Map downloadableParts;
    protected List<CitadelPart> attachedParts;
    transient Activator _activator;

    public CitadelMailObject() {
        properties = new Hashtable();
        attachedParts = new ArrayList4();
    }

    public CitadelMailObject(CtdlMessage fromMsg) {
        setProperties(fromMsg.getAttributes());
        setData(fromMsg.getContent());
        generateParts(fromMsg.getPartList());
    }

    /**
     * 
     * @return A string containing the 'from' address for this message
     */
    public String getFrom() {
        activate(ActivationPurpose.READ);
        return (String) properties.get("from");
    }

    /**
     * 
     * @return A message-received date for this message.
     */
    public Date getTime() {
        activate(ActivationPurpose.READ);
        long dtLong = Long.parseLong((String) properties.get("time")) * 1000;
        Date dt = new Date();
        dt.setTime(dtLong);
        return dt;
    }

    public boolean hasData() {
        activate(ActivationPurpose.READ);
        if (data == null) {
            return false;
        } else if (data.length() <= 0) {
            return false;
        }
        return true;
    }

    public String getData() {
        activate(ActivationPurpose.READ);
        return data;
    }

    public void setData(String data) {
        activate(ActivationPurpose.WRITE);
        try {
            StringBuffer altered = new StringBuffer();
            // Delete any RFC822 header lines at the start
            StringReader sr = new StringReader(data);
            BufferedReader br = new BufferedReader(sr);
            String line = null;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Content-type") ||
                        line.startsWith("Content-length") ||
                        line.startsWith("Content-transfer-encoding") ||
                        line.startsWith("X-Citadel-MSG4-Partnum")) {
                } else {
                    altered.append(line);
                    altered.append("\r\n");
                }
            }
            this.data = altered.toString();
        } catch (Exception e) {
            System.err.println("WARNING: Problems removing MSG4 metadata");
            e.printStackTrace(System.err);
            this.data = data;
        }
    }

    public String getSubject() {
        activate(ActivationPurpose.READ);
        return (String) properties.get("subj");
    }

    public Hashtable getProperties() {
        activate(ActivationPurpose.READ);
        return properties;
    }

    public void setProperties(Hashtable properties) {
        activate(ActivationPurpose.WRITE);
        this.properties = properties;
    }

    public long getCtdlMessagePointer() {
        activate(ActivationPurpose.READ);
        return ctdlMessagePointer;
    }

    public String getCtdlMessageRoom() {
        activate(ActivationPurpose.READ);
        return ctdlMessageRoom;
    }

    public void setCtdlMessagePointer(long ctdlMessagePointer) {
        activate(ActivationPurpose.WRITE);
        this.ctdlMessagePointer = ctdlMessagePointer;
    }

    public void setCtdlMessageRoom(String ctdlMessageRoom) {
        activate(ActivationPurpose.WRITE);
        this.ctdlMessageRoom = ctdlMessageRoom;
    }

    public void generateParts(List<String> rawPartList) {
        activate(ActivationPurpose.WRITE);
        this.attachedParts = new ArrayList4<CitadelPart>();
        Iterator<String> partIterator = rawPartList.iterator();
        while (partIterator.hasNext()) {
            String partInfo = partIterator.next();
            String[] partData = partInfo.split("\\|");
            CitadelPart ctdlPart = new CitadelPart();
            ctdlPart.setPartName(partData[1]);
            ctdlPart.setPartSize(Integer.parseInt(partData[5]));
            ctdlPart.setPartType(partData[4]);
            ctdlPart.setPartNum(partData[2]);
            attachedParts.add(ctdlPart);
        }
    }

    public List<CitadelPart> getAttachedParts() {
        activate(ActivationPurpose.READ);
        return this.attachedParts;
    }

    public void bind(Activator activator) {
        if (_activator == activator) {
            return;
        }
        if (activator != null && _activator != null) {
            throw new IllegalStateException();
        }
        _activator = activator;
    }

    public void activate(ActivationPurpose purpose) {
        if (_activator == null) {
            return;
        }
        _activator.activate(purpose);
    }
}
