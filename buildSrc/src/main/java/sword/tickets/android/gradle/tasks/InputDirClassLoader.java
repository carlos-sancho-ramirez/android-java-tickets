package sword.tickets.android.gradle.tasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class InputDirClassLoader extends ClassLoader {

    private final File _directory;

    InputDirClassLoader(File directory, ClassLoader parent) {
        super(parent);
        _directory = directory;
    }

    private String findFile(String className) {
        final String suffix = File.separator + className.replace('.', File.separatorChar) + ".class";
        final String file = _directory.toString() + suffix;
        if (new File(file).exists()) {
            return file;
        }

        throw new RuntimeException("File not found for class " + className);
    }

    private byte[] loadClassFromFile(String className) {
        final String fileName = findFile(className);

        try {
            try (FileInputStream inStream = new FileInputStream(fileName)) {
                final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

                int nextValue = 0;
                while ((nextValue = inStream.read()) != -1) {
                    byteStream.write(nextValue);
                }

                return byteStream.toByteArray();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to load class " + className, e);
        }
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            final byte[] buffer = loadClassFromFile(name);
            return defineClass(name, buffer, 0, buffer.length);
        }
        catch (Throwable t) {
            throw new ClassNotFoundException("Unable to find class " + name, t);
        }
    }
}
