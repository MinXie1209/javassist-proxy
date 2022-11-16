package space.minxie.proxy;

import space.minxie.proxy.MInvocationHandler;

public class MObject {
    protected MInvocationHandler h;

    public MObject(MInvocationHandler h) {
        this.h = h;
    }
}
