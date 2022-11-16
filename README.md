# 基于 Javassist 实现 Java 动态代理



想起之前写过一篇 [基于 ASM 实现 Java 动态代理](https://juejin.cn/post/7043401155753279524) 的文章,今天想使用 Javassist 实现同样的效果



## 什么是Javassist?

很多同学估计会对这个词有点陌生,但随着你关注的博主越来越多,知道的也越来越多,马上这篇文章就带你走进 Javassist 的世界

Javassist 和 ASM 一样是操作字节码的框架, Javassist 诞生于 1999 年,多少有点年头

使用 Javassist 可以在运行时定义一个新类,可以在 JVM 加载类文件时修改类文件

而且 Javassist 提供不同类型的API: 源码级别 和 字节码级别

所以你甚至可以在不懂字节码的前提下使用它,入手相对简单

但在性能上略逊于 ASM

## Java 动态代理

话不多说,先来回顾一下我们平时是怎么使用Java 动态代理的

### Proxy

JDK 提供一个类 Proxy 用于生成代理类

调用类方法 newProxyInstance,传入的参数

- ClassLoader loader : 定义代理类的类加载器
- Class<?>[] interfaces : 代理类需要实现的所有接口
- InvocationHandler h : 调用处理程序,方法调用都会分派到这里

```java
public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) throws IllegalArgumentException
```

### InvocationHandler