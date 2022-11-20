package space.minxie.proxy;

public class MObject {
    protected MInvocationHandler h;

    public MObject(MInvocationHandler h) {
        this.h = h;
    }
}
