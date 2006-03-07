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
package net.sf.retrotranslator.transformer;

import net.sf.retrotranslator.runtime.impl.EmptyVisitor;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;
import net.sf.retrotranslator.runtime.asm.Opcodes;
import net.sf.retrotranslator.runtime.asm.Type;
import net.sf.retrotranslator.runtime.asm.ClassReader;

import java.util.LinkedHashSet;
import java.util.Set;
import java.io.File;

/**
 * @author Taras Puchko
 */
class ReferenceVerifyingVisitor extends GenericClassVisitor {

    private ClassReaderFactory factory;
    private MessageLogger logger;
    private File location;
    private String name;
    private Set<String> warnings;

    public ReferenceVerifyingVisitor(ClassReaderFactory factory, MessageLogger logger, File location, String name) {
        super(new EmptyVisitor());
        this.factory = factory;
        this.logger = logger;
        this.location = location;
        this.name = name;
    }

    public int verify(byte[] bytes) {
        warnings = new LinkedHashSet<String>();
        new ClassReader(bytes).accept(this, true);
        return warnings.size();
    }

    protected String visitInternalName(String name) {
        try {
            factory.getClassReader(name);
        } catch (ClassNotFoundException e) {
            println("Class not found: " + getClassInfo(e.getMessage()));
        }
        return name;
    }

    protected void visitFieldRef(int opcode, String owner, String name, String desc) {
        boolean stat = (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC);
        try {
            int found = new MemberFinder(factory, false, stat, name, desc).findIn(owner, null);
            if (found == 0) {
                println(getFieldInfo(owner, stat, name, desc, "not found"));
            } else if (found > 1) {
                println(getFieldInfo(owner, stat, name, desc, "duplicated"));
            }
        } catch (ClassNotFoundException e) {
            cannotVerify(getFieldInfo(owner, stat, name, desc, "not verified"), e);
        }
    }

    protected void visitMethodRef(int opcode, String owner, String name, String desc) {
        if (owner.startsWith("[")) return;
        boolean stat = (opcode == Opcodes.INVOKESTATIC);
        try {
            int found = new MemberFinder(factory, true, stat, name, desc).findIn(owner, null);
            if (found == 0) {
                println(getMethodInfo(owner, stat, name, desc, "not found"));
            } else if (found > 1) {
                println(getMethodInfo(owner, stat, name, desc, "duplicated"));
            }
        } catch (ClassNotFoundException e) {
            cannotVerify(getMethodInfo(owner, stat, name, desc, "not verified"), e);
        }
    }

    private void cannotVerify(String text, ClassNotFoundException e) {
        println(text + " (class not found: " + getClassInfo(e.getMessage()) + ")");
    }

    private void println(String text) {
        if (!warnings.contains(text)) {
            warnings.add(text);
            logger.log(new Message(Level.WARNING, text, location, name));
        }
    }

    private static String getClassInfo(String name) {
        return name.replace('/', '.');
    }

    private static String getFieldInfo(String owner, boolean stat, String name, String desc, String message) {
        StringBuffer buffer = new StringBuffer("Field ").append(message).append(": ");
        if (stat) buffer.append("static ");
        buffer.append(Type.getType(desc).getClassName()).append(' ');
        buffer.append(getClassInfo(owner)).append('.').append(name);
        return buffer.toString();
    }

    private static String getMethodInfo(String owner, boolean stat, String name, String desc, String message) {
        StringBuffer buffer = new StringBuffer();
        if (name.equals(RuntimeTools.CONSTRUCTOR_NAME)) {
            buffer.append("Constructor ").append(message).append(": ");
            buffer.append(getClassInfo(owner));
        } else {
            buffer.append("Method ").append(message).append(": ");
            if (stat) buffer.append("static ");
            buffer.append(Type.getReturnType(desc).getClassName());
            buffer.append(' ').append(getClassInfo(owner)).append('.').append(name);
        }
        buffer.append('(');
        boolean first = true;
        for (Type type : Type.getArgumentTypes(desc)) {
            buffer.append(first ? "" : ",").append(type.getClassName());
            first = false;
        }
        return buffer.append(')').toString();
    }
}
