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

/**
 * A Citadel exception is thrown when an operation cannot
 * return the expected results from the server
 * @author matt
 */
public class CitadelException extends Exception {
    int code = 0;
    String reason = null;
    String formattedMessage = null;
    public CitadelException(String citadelOutput) {
        code = Integer.parseInt(citadelOutput.substring(0,3));
        reason = citadelOutput.substring(3);
        formattedMessage = String.format("Error %d:%s", code,reason);
    }

    @Override
    public String getMessage() {
        return formattedMessage;
    }

    public String getReason() {
        if (code == 575)
            return "Message ID either does not exist or has been deleted from server";
        else
            return formattedMessage;
    }
    
}
