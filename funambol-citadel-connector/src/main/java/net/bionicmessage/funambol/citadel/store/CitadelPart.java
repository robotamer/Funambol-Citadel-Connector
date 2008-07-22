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
import com.db4o.ta.Activatable;

/**
 *
 * @author matt
 */
public class CitadelPart implements Activatable {

    protected byte[] partData = null;
    protected String partType = null;
    protected int partSize = 0;
    protected String partName = null;
    protected String partNum = null;
    transient Activator _activator;

    public CitadelPart() {
    }

    public byte[] getPartData() {
        activate(ActivationPurpose.READ);
        return partData;
    }

    public void setPartData(byte[] partData) {
        activate(ActivationPurpose.WRITE);
        this.partData = partData;
    }

    public String getPartType() {
        activate(ActivationPurpose.READ);
        return partType;
    }

    public void setPartType(String partType) {
        activate(ActivationPurpose.WRITE);
        this.partType = partType;
    }

    public String getPartName() {
        activate(ActivationPurpose.READ);
        return partName;
    }

    public void setPartName(String partName) {
        activate(ActivationPurpose.WRITE);
        this.partName = partName;
    }

    public int getPartSize() {
        activate(ActivationPurpose.READ);
        return partSize;
    }

    public void setPartSize(int partSize) {
        activate(ActivationPurpose.WRITE);
        this.partSize = partSize;
    }

    public void setPartNum(String partNum) {
        activate(ActivationPurpose.WRITE);
        this.partNum = partNum;
    }

    public String getPartNum() {
        activate(ActivationPurpose.READ);
        return partNum;
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
