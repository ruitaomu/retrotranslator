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
package net.sf.retrotranslator.runtime.java.lang;

import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _IntegerTestCase extends TestCase {

    public void testValueOf() throws Exception {
        Integer integer = -5;
        assertEquals(-5, integer.intValue());
    }

    public void testSignum() throws Exception {
        assertEquals(1, Integer.signum(123));
        assertEquals(0, Integer.signum(0));
        assertEquals(-1, Integer.signum(-345));
    }

    public void testHighestOneBit() throws Exception {
        assertEquals(0, Integer.highestOneBit(0));
        assertEquals(1, Integer.highestOneBit(1));
        assertEquals(2, Integer.highestOneBit(2));
        assertEquals(2, Integer.highestOneBit(3));
        assertEquals(0x80000000, Integer.highestOneBit(-1));
        assertEquals(0x80000000, Integer.highestOneBit(-2));
        assertEquals(0x80000000, Integer.highestOneBit(-3));
        assertEquals(0x08000000, Integer.highestOneBit(0x0FFFFFFF));
        assertEquals(0x08000000, Integer.highestOneBit(0x0F0F0F04));
    }

    public void testLowestOneBit() throws Exception {
        assertEquals(0, Integer.lowestOneBit(0));
        assertEquals(1, Integer.lowestOneBit(1));
        assertEquals(2, Integer.lowestOneBit(2));
        assertEquals(1, Integer.lowestOneBit(3));
        assertEquals(1, Integer.lowestOneBit(-1));
        assertEquals(2, Integer.lowestOneBit(-2));
        assertEquals(1, Integer.lowestOneBit(-3));
        assertEquals(1, Integer.lowestOneBit(0x0FFFFFFF));
        assertEquals(4, Integer.lowestOneBit(0x0F0F0F04));
    }

}