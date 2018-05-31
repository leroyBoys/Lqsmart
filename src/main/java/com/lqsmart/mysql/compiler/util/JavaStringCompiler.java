package com.lqsmart.mysql.compiler.util;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * In-memory compile Java source code as String.
 * Created by leroy:656515489@qq.com
 * 2018/5/2.
 */
public class JavaStringCompiler {

    JavaCompiler compiler;
    StandardJavaFileManager stdManager;
    final Map<String, byte[]> classBytes = new HashMap<String, byte[]>();
    public JavaStringCompiler() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.stdManager = compiler.getStandardFileManager(null, null, null);
    }

    public void close(){
        classBytes.clear();
    }

    /**
     *
     * @param javaFiles
     * @throws IOException
     */
    public void compile(List<JavaFile> javaFiles) throws IOException {
        try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
            List<JavaFileObject> jfiles = new ArrayList<>();
            for(JavaFile javaFile:javaFiles){
                jfiles.add(manager.makeStringSource(javaFile.getName(), javaFile.getCode()));
            }
            CompilationTask task = compiler.getTask(null, manager, null, null, null, jfiles);
            Boolean result = task.call();
            if (result == null || !result.booleanValue()) {
                throw new RuntimeException("Compilation failed.");
            }

        }
    }

    /**
     * Load class from compiled classes.
     * @param classFullname
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public Class<?> loadClass(String classFullname) throws ClassNotFoundException, IOException {
        try (MemoryClassLoader classLoader = new MemoryClassLoader()) {
            return classLoader.loadClass(classFullname);
        }
    }

    public <T> T instanceClass(String classFullname) throws Exception {
        try (MemoryClassLoader classLoader = new MemoryClassLoader()) {
            return (T) classLoader.loadClass(classFullname).newInstance();
        }
    }

    class MemoryClassLoader extends URLClassLoader {

        // class name to class bytes:
       // Map<String, byte[]> classBytes = new HashMap<String, byte[]>();

        public MemoryClassLoader() {
            super(new URL[0], MemoryClassLoader.class.getClassLoader());
          //  this.classBytes.putAll(classBytes);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] buf = classBytes.get(name);
            if (buf == null) {
                return super.findClass(name);
            }
            classBytes.remove(name);
            return defineClass(name, buf, 0, buf.length);
        }
    }

    class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        // compiled classes in bytes:
        MemoryJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
            //classBytes.clear();
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
                                                   FileObject sibling) throws IOException {
            if (kind == JavaFileObject.Kind.CLASS) {
                return new MemoryOutputJavaFileObject(className);
            } else {
                return super.getJavaFileForOutput(location, className, kind, sibling);
            }
        }

        JavaFileObject makeStringSource(String name, String code) {
            return new MemoryInputJavaFileObject(name+".java", code);
        }

        class MemoryInputJavaFileObject extends SimpleJavaFileObject {

            final String code;

            MemoryInputJavaFileObject(String name, String code) {
                super(URI.create("string:///" + name), Kind.SOURCE);
                this.code = code;
            }

            @Override
            public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
                return CharBuffer.wrap(code);
            }
        }

        class MemoryOutputJavaFileObject extends SimpleJavaFileObject {
            final String name;

            MemoryOutputJavaFileObject(String name) {
                super(URI.create("string:///" + name), Kind.CLASS);
                this.name = name;
            }

            @Override
            public OutputStream openOutputStream() {
                return new FilterOutputStream(new ByteArrayOutputStream()) {
                    @Override
                    public void close() throws IOException {
                        out.close();
                        ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
                        classBytes.put(name, bos.toByteArray());
                    }
                };
            }

        }
    }
}
