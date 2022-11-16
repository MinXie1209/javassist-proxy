package space.minxie.service.impl;

import space.minxie.proxy.MInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class CusJDKInvocationHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        long start = System.currentTimeMillis();
        System.out.println("invoke:" + proxy.getClass().getSimpleName() + "." + method.getName() + ":" + (System.currentTimeMillis() - start) + "ms");
        switch (method.getName()) {
            case "login":
                return true;
            case "loginV2":
                return false;
            case "loginV3":
                return '1';
            case "loginV4":
                return '0';
            case "loginV5":
                return (byte) 1;
            case "loginV6":
                return (byte) 0;
            case "loginV7":
                return (short) 1;
            case "loginV8":
                return (short) 0;
            case "loginV9":
                return 1;
            case "loginV10":
                return 0;
            case "loginV11":
                return 1L;
            case "loginV12":
                return 0L;
            case "loginV13":
                return 1D;
            case "loginV14":
                return 0D;
            case "loginV15":
                return 1F;
            case "loginV16":
                return 0F;
        }
        return null;
    }
}
