/***
 * Retrotranslator: a Java bytecode transformer that translates Java classes
 * compiled with JDK 5.0 into classes that can be run on JVM 1.4.
 *
 * Copyright (c) 2005, 2006 Taras Puchko
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

import java.io.Serializable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Taras Puchko
 */
public abstract class Enum_<E extends Enum_<E>> implements Comparable<E>, Serializable {

    private static final Map<Class, Enum_[]> map = new WeakHashMap<Class, Enum_[]>();

    private final String name;

    private final int ordinal;

    protected Enum_(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    public final String name() {
        return name;
    }

    public final int ordinal() {
        return ordinal;
    }

    public String toString() {
        return name;
    }

    public final boolean equals(Object other) {
        return this == other;
    }

    public final int hashCode() {
        return super.hashCode();
    }

    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public final int compareTo(E other) {
        if (this.getClass() != other.getClass() && this.getDeclaringClass() != other.getDeclaringClass()) {
            throw new ClassCastException();
        }
        return this.ordinal - other.ordinal;
    }

    public final Class<E> getDeclaringClass() {
        Class clazz = getClass();
        Class zuper = clazz.getSuperclass();
        return (zuper == Enum_.class) ? clazz : zuper;
    }

    public static <T extends Enum_<T>> T valueOf(Class<T> enumType, String name) {
        if (name == null) throw new NullPointerException("Name is null");
        T[] enums = (T[]) getEnumConstants(enumType);
        if (enums == null) throw new IllegalArgumentException(enumType.getName() + " is not an enum type");
        for (T currentEnum : enums) {
            if (currentEnum.name.equals(name)) return currentEnum;
        }
        throw new IllegalArgumentException("No enum const " + enumType + "." + name);
    }

    protected Object readResolve() {
        return valueOf(getClass(), name);
    }

    protected static Enum_[] getEnumConstants(Class aClass) {
        if (aClass.getSuperclass() != Enum_.class) return null;
        try {
            Class.forName(aClass.getName(), true, aClass.getClassLoader());
        } catch (Exception e) {
            //ignore
        }
        synchronized (map) {
            return map.get(aClass);
        }
    }

    protected static void setEnumConstants(Class aClass, Enum_[] enumConstants) {
        synchronized (map) {
            map.put(aClass, enumConstants);
        }
    }
}