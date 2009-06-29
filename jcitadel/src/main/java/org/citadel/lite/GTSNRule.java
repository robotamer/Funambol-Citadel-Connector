/*
 * JCitadel
 * Copyright (c) 2009 Mathew McBride <matt@mcbridematt.dhs.org>
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
 *
 * @author matt
 */
public class GTSNRule {

    long startnum = 0;
    long endnum = 0;

    public GTSNRule(String inRule) {
        String rule = inRule.replace("*", "0");
        String[] params = rule.split(":");
        startnum = Long.valueOf(params[0]);
        if (params.length > 1) {
            endnum = Long.valueOf(params[1]);
        } else {
            endnum = startnum;
        }
    }

    public boolean isInRange(long num) {
        return (num >= startnum && num <= endnum);
    }

    public String toString() {
        String str = Long.toString(startnum);
        if (startnum != endnum) {
            str = str + ":" + Long.toString(endnum);
        }
        return str;
    }
}
