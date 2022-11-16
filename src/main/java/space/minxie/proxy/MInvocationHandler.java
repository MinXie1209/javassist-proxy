package space.minxie.proxy;

import java.lang.reflect.Method;

public interface MInvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable;
}
