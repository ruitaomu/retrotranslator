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
package net.sf.retrotranslator.runtime.java.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import net.sf.retrotranslator.runtime.asm.Opcodes;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
public class _Method {

    public static Annotation getAnnotation(Method method, Class annotationType) {
        return MethodDescriptor.getInstance(method).getAnnotation(annotationType);
    }

    public static Annotation[] getAnnotations(Method method) {
        return MethodDescriptor.getInstance(method).getAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(Method method) {
        return MethodDescriptor.getInstance(method).getDeclaredAnnotations();
    }

    public static Object getDefaultValue(Method method) {
        return MethodDescriptor.getInstance(method).getDefaultValue();
    }

    public static Type[] getGenericExceptionTypes(Method method) {
        return RuntimeTools.getTypes(method.getExceptionTypes(),
                MethodDescriptor.getInstance(method).getGenericExceptionTypes());
    }

    public static Type[] getGenericParameterTypes(Method method) {
        return RuntimeTools.getTypes(method.getParameterTypes(),
                MethodDescriptor.getInstance(method).getGenericParameterTypes());
    }

    public static Type getGenericReturnType(Method method) {
        return RuntimeTools.getType(method.getReturnType(),
                MethodDescriptor.getInstance(method).getGenericReturnType());
    }

    public static Annotation[][] getParameterAnnotations(Method method) {
        return MethodDescriptor.getInstance(method).getParameterAnnotations();
    }

    public static TypeVariable[] getTypeParameters(Method method) {
        return MethodDescriptor.getInstance(method).getTypeParameters();
    }

    public static boolean isAnnotationPresent(Method method, Class annotationType) {
        return MethodDescriptor.getInstance(method).isAnnotationPresent(annotationType);
    }

    public static boolean isBridge(Method method) {
        return MethodDescriptor.getInstance(method).isAccess(Opcodes.ACC_BRIDGE);
    }

    public static boolean isSynthetic(Method method) {
        return MethodDescriptor.getInstance(method).isAccess(Opcodes.ACC_SYNTHETIC);
    }

    public static boolean isVarArgs(Method method) {
        return MethodDescriptor.getInstance(method).isAccess(Opcodes.ACC_VARARGS);
    }

    public static String toGenericString(Method method) {
        StringBuilder builder = new StringBuilder();
        if (method.getModifiers() != 0) {
            builder.append(Modifier.toString(method.getModifiers())).append(' ');
        }
        TypeVariable[] typeParameters = getTypeParameters(method);
        if (typeParameters.length > 0) {
            RuntimeTools.append(builder.append('<'), typeParameters).append("> ");
        }
        builder.append(RuntimeTools.getString(getGenericReturnType(method))).append(' ');
        builder.append(RuntimeTools.getString(method.getDeclaringClass())).append('.').append(method.getName());
        RuntimeTools.append(builder.append('('), getGenericParameterTypes(method)).append(')');
        Type[] exceptionTypes = getGenericExceptionTypes(method);
        if (exceptionTypes.length > 0) {
            RuntimeTools.append(builder.append(" throws "), exceptionTypes);
        }
        return builder.toString();
    }

}
