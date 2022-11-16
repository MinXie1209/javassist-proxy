import space.minxie.proxy.MProxy;
import space.minxie.service.LoginService;
import space.minxie.service.impl.CusJDKInvocationHandler;
import space.minxie.service.impl.CusMInvocationHandler;

import java.lang.reflect.Proxy;

public class Main {
    public static void main(String[] args) throws Throwable {
        // JDK proxy ---------------------------------------
        System.out.println("JDK proxy ---------------------------------------");
        LoginService loginService = (LoginService) Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[]{LoginService.class}, new CusJDKInvocationHandler());
        System.out.println(loginService.login("admin", "admin"));
        System.out.println(loginService.loginV2("admin", "admin"));
        System.out.println(loginService.loginV3("admin", "admin"));
        System.out.println(loginService.loginV4("admin", "admin"));
        System.out.println(loginService.loginV5("admin", "admin"));
        System.out.println(loginService.loginV6("admin", "admin"));
        System.out.println(loginService.loginV7("admin", "admin"));
        System.out.println(loginService.loginV8("admin", "admin"));
        System.out.println(loginService.loginV9("admin", "admin"));
        System.out.println(loginService.loginV10("admin", "admin"));
        System.out.println(loginService.loginV11("admin", "admin"));
        System.out.println(loginService.loginV12("admin", "admin"));
        System.out.println(loginService.loginV13("admin", "admin"));
        System.out.println(loginService.loginV14("admin", "admin"));
        System.out.println(loginService.loginV15("admin", "admin"));
        System.out.println(loginService.loginV16("admin", "admin"));

        // Javassist proxy ---------------------------------------
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("Javassist proxy ---------------------------------------");

        LoginService loginService2 = (LoginService) MProxy.newProxyInstance(new Class[]{LoginService.class}, new CusMInvocationHandler());
        System.out.println(loginService2.login("admin", "admin"));
        System.out.println(loginService2.loginV2("admin", "admin"));
        System.out.println(loginService2.loginV3("admin", "admin"));
        System.out.println(loginService2.loginV4("admin", "admin"));
        System.out.println(loginService2.loginV5("admin", "admin"));
        System.out.println(loginService2.loginV6("admin", "admin"));
        System.out.println(loginService2.loginV7("admin", "admin"));
        System.out.println(loginService2.loginV8("admin", "admin"));
        System.out.println(loginService2.loginV9("admin", "admin"));
        System.out.println(loginService2.loginV10("admin", "admin"));
        System.out.println(loginService2.loginV11("admin", "admin"));
        System.out.println(loginService2.loginV12("admin", "admin"));
        System.out.println(loginService2.loginV13("admin", "admin"));
        System.out.println(loginService2.loginV14("admin", "admin"));
        System.out.println(loginService2.loginV15("admin", "admin"));
        System.out.println(loginService2.loginV16("admin", "admin"));
    }
}
