/***
 * Retrotranslator: a Java bytecode transformer that translates Java classes
 * compiled with JDK 5.0 into classes that can be run on JVM 1.4.
 * 
 * Copyright (c) 2005 - 2008 Taras Puchko
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
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.sf.retrotranslator.transformer;

import java.util.StringTokenizer;
import java.util.regex.Pattern;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

/**
 * @author Taras Puchko
 */
class SourceMask {

    private Pattern pattern;

    public SourceMask(String srcmask) {
        if (srcmask == null) {
            pattern = null;
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (String s : srcmask.split(";")) {
            if (builder.length() > 0) builder.append('|');
            builder.append("((./)?");
            StringTokenizer tokenizer = new StringTokenizer(s, "*?", true);
            while (tokenizer.hasMoreTokens()) {
                builder.append(wildcardToRegex(tokenizer.nextToken()));
            }
            builder.append(")");
        }
        pattern = Pattern.compile(builder.toString());
    }

    private static String wildcardToRegex(String s) {
        if (s.equals("*")) return ".*";
        if (s.equals("?")) return ".";
        return Pattern.quote(s);
    }

    public boolean matches(String name) {
        return pattern != null ? pattern.matcher(name).matches() : name.endsWith(RuntimeTools.CLASS_EXTENSION);
    }

}
