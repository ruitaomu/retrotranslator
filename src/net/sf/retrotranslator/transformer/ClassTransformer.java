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

import java.util.*;
import net.sf.retrotranslator.runtime.asm.*;
import net.sf.retrotranslator.runtime.impl.BytecodeTransformer;

/**
 * @author Taras Puchko
 */
class ClassTransformer implements BytecodeTransformer {

    private final boolean lazy;
    private final boolean stripsign;
    private final boolean stripannot;
    private final boolean retainflags;
    private final boolean syncvolatile;
    private final boolean syncfinal;
    private final boolean keepclasslit;
    private final ReflectionMode reflectionMode;
    private final EmbeddingConverter converter;
    private final SystemLogger logger;
    private final ReplacementLocatorFactory factory;

    public ClassTransformer(boolean lazy, boolean stripsign, boolean stripannot, boolean retainflags,
                            boolean syncvolatile, boolean syncfinal, boolean keepclasslit,
                            ReflectionMode reflectionMode, SystemLogger logger,
                            EmbeddingConverter converter, ReplacementLocatorFactory factory) {
        this.lazy = lazy;
        this.stripsign = stripsign;
        this.stripannot = stripannot;
        this.retainflags = retainflags;
        this.syncvolatile = syncvolatile;
        this.syncfinal = syncfinal;
        this.keepclasslit = keepclasslit;
        this.reflectionMode = reflectionMode;
        this.converter = converter;
        this.logger = logger;
        this.factory = factory;
    }

    public byte[] transform(byte[] bytes, int offset, int length) {
        ClassVersion target = factory.getMode().getTarget();
        if (lazy && !target.isBefore(TransformerTools.getClassVersion(bytes, offset))) {
            if (offset == 0 && length == bytes.length) return bytes;
            byte[] result = new byte[length];
            System.arraycopy(bytes, offset, result, 0, length);
            return result;
        }
        ReplacementLocator locator = factory.getLocator();
        MethodCounter counter = new MethodCounter();
        Map<String, List<InstantiationPoint>> pointListMap = new HashMap<String, List<InstantiationPoint>>();
        ClassWriter classWriter = new ClassWriter(true);
        ClassVisitor visitor = new InstantiationAnalysisVisitor(classWriter, locator, pointListMap, logger);
        visitor = new DuplicateInterfacesVisitor(new VersionVisitor(visitor, target), logger, counter);
        if (target.isBefore(ClassVersion.VERSION_12)) {
            visitor = new MirandaMethodsVisitor(visitor, locator);
        }
        if (target.isBefore(ClassVersion.VERSION_13)) {
            visitor = new InheritedConstantVisitor(new SynchronizedBlockVisitor(visitor), locator);
        }
        if (target.isBefore(ClassVersion.VERSION_15) && (syncvolatile || syncfinal)) {
            visitor = new MemoryModelVisitor(visitor, locator.getEnvironment(), syncvolatile, syncfinal);
        }
        if (target.isBefore(ClassVersion.VERSION_14)) {
            visitor = new InnerClassVisitor(visitor);
        }
        if (target.isBefore(ClassVersion.VERSION_15)) {
            visitor = new ObjectMethodsVisitor(new ClassLiteralVisitor(visitor), locator);
        }
        if (!factory.isRetainapi()) {
            visitor = new SpecificReplacementVisitor(visitor, target, locator, factory.getMode());
        }
        visitor = new GeneralReplacementVisitor(visitor, locator, keepclasslit);
        new ClassReader(bytes, offset, length).accept(visitor, false);
        if (counter.containsDuplicates()) {
            byte[] bytecode = classWriter.toByteArray();
            classWriter = new ClassWriter(true);
            pointListMap.clear();
            visitor = new InstantiationAnalysisVisitor(classWriter, locator, pointListMap, logger);
            new ClassReader(bytecode).accept(new DuplicateMethodsVisitor(visitor, logger, counter), false);
        }
        if (!pointListMap.isEmpty()) {
            byte[] bytecode = classWriter.toByteArray();
            classWriter = new ClassWriter(true);
            new ClassReader(bytecode).accept(new InstantiationReplacementVisitor(classWriter, pointListMap), false);
        }
        if (converter != null) {
            byte[] bytecode = classWriter.toByteArray();
            classWriter = new ClassWriter(true);
            new ClassReader(bytecode).accept(new PrefixingVisitor(classWriter, converter), false);
        }
        if (reflectionMode == ReflectionMode.SAFE) {
            MemberReplacement replacement = ReflectionInitVisitor.getMethodReplacement(locator);
            if (replacement != null) {
                byte[] bytecode = classWriter.toByteArray();
                ReflectionDataVisitor dataVisitor = new ReflectionDataVisitor();
                new ClassReader(bytecode).accept(dataVisitor, true);
                classWriter = new ClassWriter(true);
                visitor = new ReflectionInitVisitor(classWriter, replacement, converter, dataVisitor.toByteArray());
                new ClassReader(bytecode).accept(visitor, false);
            }
        }
        if (stripsign || stripannot) {
            byte[] bytecode = classWriter.toByteArray();
            classWriter = new ClassWriter(true);
            ClassVisitor stripVisitor = classWriter;
            if (stripsign) {
                stripVisitor = new SignatureStrippingVisitor(stripVisitor);
            }
            if (stripannot) {
                stripVisitor = new AnnotationStrippingVisitor(stripVisitor);
            }
            new ClassReader(bytecode).accept(stripVisitor, false);
        }
        return classWriter.toByteArray(target.isBefore(ClassVersion.VERSION_15) && !retainflags);
    }

}
