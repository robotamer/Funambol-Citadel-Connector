/*
 * JCitadel 
 * Copyright (c) 2007-2008 Mathew McBride <matt@mcbridematt.dhs.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package org.citadel.lite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author matt
 */
public class CtdlMessage {

    Hashtable attributes;
    List<String> partList = null;
    StringBuffer textContent = new StringBuffer();
    boolean parserInTextMode = false;

    public CtdlMessage() {
        attributes = new Hashtable();
        partList = new ArrayList();
    }

    public void parserFeedLine(String line) {
        if (!parserInTextMode) {
            if (!line.equals("text")) {
                String[] values = line.split("=");
                if (values.length == 2 && !values[0].equals("part")) {
                    attributes.put(values[0], values[1]);
                } else if (values[0].equals("part")) {
                    /* Break the part attribute up, we want the file name
                     * and part size attribs only */
                    String[] partInfo = values[1].split("\\|");
                    if (partInfo.length == 6 && partInfo[1].length() > 0) {
                        /* String fileName = partInfo[1];
                        String num = partInfo[2];
                        String partMeta = fileName+"|"+num;
                        String size = partInfo[5];
                        partList.put(partMeta,size); */
                        partList.add(values[1]);
                    }
                }
            } else {
                parserInTextMode = true;
                textContent = new StringBuffer(65535);
                return;
            }
        } else {
            textContent.append(line);
            textContent.append("\r\n");
        }
    }

    public String getContent() {
        return textContent.toString();
    }

    public Hashtable getAttributes() {
        return attributes;
    }

    public List<String> getPartList() {
        return partList;
    }

    
}
