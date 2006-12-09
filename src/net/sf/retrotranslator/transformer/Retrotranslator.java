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

import java.io.File;
import java.util.*;

/**
 * @author Taras Puchko
 */
public class Retrotranslator implements MessageLogger {

    private LinkedList<FileContainer> src = new LinkedList<FileContainer>();
    private FileContainer dest;
    private boolean stripsign;
    private boolean retainapi;
    private boolean retainflags;
    private boolean verbose;
    private boolean lazy;
    private boolean advanced;
    private boolean verify;
    private List<File> classpath = new ArrayList<File>();
    private MessageLogger logger = this;
    private SourceMask sourceMask = new SourceMask(null);
    private String embed;
    private String backport;
    private ClassLoader classLoader;

    public Retrotranslator() {
    }

    public void addSrcdir(File srcdir) {
        if (!srcdir.isDirectory()) throw new IllegalArgumentException("Invalid srcdir: " + srcdir);
        src.add(new FolderFileContainer(srcdir));
    }

    public void addSrcjar(File srcjar) {
        if (!srcjar.isFile()) throw new IllegalArgumentException("Invalid srcjar: " + srcjar);
        src.add(new JarFileContainer(srcjar));
    }

    public void addSourceFiles(File srcdir, List<String> fileNames) {
        if (!srcdir.isDirectory()) throw new IllegalArgumentException("Invalid srcdir: " + srcdir);
        src.add(new FolderFileContainer(srcdir, fileNames));
    }

    public void setDestdir(File destdir) {
        if (!destdir.isDirectory()) throw new IllegalArgumentException("Invalid destdir: " + destdir);
        dest = new FolderFileContainer(destdir);
    }

    public void setDestjar(File destjar) {
        if (destjar.isDirectory()) throw new IllegalArgumentException("Invalid destjar: " + destjar);
        dest = new JarFileContainer(destjar);
    }

    public void setStripsign(boolean stripsign) {
        this.stripsign = stripsign;
    }

    public void setRetainapi(boolean retainapi) {
        this.retainapi = retainapi;
    }

    public void setRetainflags(boolean retainflags) {
        this.retainflags = retainflags;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }

    public void addClasspathElement(File classpathElement) {
        this.classpath.add(classpathElement);
    }

    public void addClasspath(String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            addClasspathElement(new File(tokenizer.nextToken()));
        }
    }

    public void setSrcmask(String srcmask) {
        sourceMask = new SourceMask(srcmask);
    }

    public void setEmbed(String embed) {
        this.embed = embed;
    }

    public void setBackport(String backport) {
        this.backport = backport;
    }

    public void setLogger(MessageLogger logger) {
        this.logger = logger;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void log(Message message) {
        System.out.println(message);
    }

    public boolean run() {
        if (src.isEmpty()) throw new IllegalArgumentException("Source not set.");
        EmbeddingConverter converter = null;
        if (embed != null) {
            if (dest == null) throw new IllegalArgumentException("Destination required for embedding!");
            if (lazy) throw new IllegalArgumentException("Embedding cannot be lazy!");
            converter = new EmbeddingConverter(embed);
        }
        FileInfoLogger infoLogger = new FileInfoLogger(logger, verbose);
        BackportLocatorFactory locatorFactory = new BackportLocatorFactory(backport);
        ClassTransformer classTransformer = new ClassTransformer(
                lazy, advanced, stripsign, retainapi, retainflags, converter, infoLogger, locatorFactory);
        TextFileTransformer fileTransformer = new TextFileTransformer(locatorFactory);
        FileTranslator translator = new FileTranslator(
                classTransformer, fileTransformer, converter, infoLogger, sourceMask);
        for (FileContainer container : src) {
            translator.transform(container, dest != null ? dest : container);
        }
        if (converter != null) {
            translator.embed(dest);
        }
        if (dest != null) dest.flush();
        if (!verify) return true;
        ClassLoader loader = classLoader;
        if (loader == null && classpath.isEmpty()) {
            loader = this.getClass().getClassLoader();
        }
        ClassReaderFactory factory = new ClassReaderFactory(loader);
        try {
            return verify(factory);
        } finally {
            factory.close();
        }
    }

    private boolean verify(ClassReaderFactory factory) {
        if (dest != null) {
            factory.appendPath(dest.getLocation());
        } else {
            for (FileContainer container : src) {
                factory.appendPath(container.getLocation());
            }
        }
        for (File file : classpath) {
            factory.appendPath(file);
        }
        if (dest != null) {
            return verify(factory, dest);
        }
        boolean verified = true;
        for (FileContainer container : src) {
            verified &= verify(factory, container);
        }
        return verified;
    }

    private boolean verify(ClassReaderFactory factory, final FileContainer container) {
        logger.log(new Message(Level.INFO,
                "Verifying " + container.getFileCount() + " file(s) in " + container + "."));
        int warningCount = 0;
        for (final FileEntry entry : container.getEntries()) {
            if (sourceMask.matches(entry.getName())) {
                byte[] content = entry.getContent();
                if (TransformerTools.isClassFile(content)) {
                    if (verbose) logger.log(new Message(Level.VERBOSE,
                            "Verification", container.getLocation(), entry.getName()));
                    warningCount += new ReferenceVerifyingVisitor(factory, logger,
                            container.getLocation(), entry.getName()).verify(content);
                }
            }
        }
        String result = warningCount != 0 ? " with " + warningCount + " warning(s)." : " successfully.";
        logger.log(new Message(Level.INFO,
                "Verification of " + container.getFileCount() + " file(s) completed" + result));
        return warningCount == 0;
    }

    private boolean execute(String[] args) {
        int i = 0;
        while (i < args.length) {
            String string = args[i++];
            if (string.equals("-srcdir") && i < args.length) {
                addSrcdir(new File(args[i++]));
            } else if (string.equals("-srcjar") && i < args.length) {
                addSrcjar(new File(args[i++]));
            } else if (string.equals("-destdir") && i < args.length) {
                setDestdir(new File(args[i++]));
            } else if (string.equals("-destjar") && i < args.length) {
                setDestjar(new File(args[i++]));
            } else if (string.equals("-stripsign")) {
                setStripsign(true);
            } else if (string.equals("-retainapi")) {
                setRetainapi(true);
            } else if (string.equals("-retainflags")) {
                setRetainflags(true);
            } else if (string.equals("-verbose")) {
                setVerbose(true);
            } else if (string.equals("-lazy")) {
                setLazy(true);
            } else if (string.equals("-advanced")) {
                setAdvanced(true);
            } else if (string.equals("-verify")) {
                setVerify(true);
            } else if (string.equals("-classpath") && i < args.length) {
                addClasspath(args[i++]);
            } else if (string.equals("-srcmask") && i < args.length) {
                setSrcmask(args[i++]);
            } else if (string.equals("-embed") && i < args.length) {
                setEmbed(args[i++]);
            } else if (string.equals("-backport") && i < args.length) {
                setBackport(args[i++]);
            } else {
                throw new IllegalArgumentException("Unknown option: " + string);
            }
        }
        return run();
    }

    private static void printUsage() {
        String version = Retrotranslator.class.getPackage().getImplementationVersion();
        String suffix = (version == null) ? "" : "-" + version;
        System.out.println("Usage: java -jar retrotranslator-transformer" + suffix + ".jar" +
                " [-srcdir <path> | -srcjar <file>] [-destdir <path> | -destjar <file>]" +
                " [-stripsign] [-verbose] [-lazy] [-advanced] [-retainapi] [-retainflags] [-verify]" +
                " [-classpath <classpath>] [-srcmask <mask>] [-embed <package>] [-backport <packages>]");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        try {
            if (!new Retrotranslator().execute(args)) System.exit(2);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            printUsage();
            System.exit(1);
        }
    }

}
