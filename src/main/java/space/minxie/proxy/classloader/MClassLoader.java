package space.minxie.proxy.classloader;

import java.util.HashMap;
import java.util.Map;

public class MClassLoader extends ClassLoader {
    private final Map<String, byte[]> classMap = new HashMap<String, byte[]>();

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classMap.containsKey(name)) {
            byte[] bytes = classMap.get(name);
            classMap.remove(name);
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name);
    }

    public void add(String name, byte[] bytes) {
        classMap.put(name, bytes);
    }
}
