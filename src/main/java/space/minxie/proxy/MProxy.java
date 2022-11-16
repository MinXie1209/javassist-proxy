package space.minxie.proxy;

import javassist.*;
import space.minxie.proxy.classloader.MClassLoader;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MProxy {
    private static final String CLASS_NAME_PRE = "space.minxie.proxy.MProxy_";

    public static Object newProxyInstance(Class<?>[] interfaces, MInvocationHandler h) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        String clasName = generateClassName(interfaces);
        CtClass ctClass = classPool.makeClass(clasName, classPool.get(MObject.class.getName()));
        CtClass[] classes = new CtClass[interfaces.length];

        // 添加类成员变量
        addStaticFields(interfaces, classPool, ctClass);

        // 实现接口方法 和 添加 接口类
        implMethodsAndAddClasses(interfaces, classPool, ctClass, classes);

        // 设置接口类数组
        ctClass.setInterfaces(classes);

        try {
            ctClass.writeFile("./rewrite");
        } catch (CannotCompileException | IOException e) {
            throw new RuntimeException(e);
        }

        return loadClassAndNewInstance(h, clasName, ctClass);

    }

    /**
     * 加载类并实例化对象
     *
     * @param h
     * @param clasName
     * @param ctClass
     * @return
     * @throws IOException
     * @throws CannotCompileException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static Object loadClassAndNewInstance(MInvocationHandler h, String clasName, CtClass ctClass) throws IOException, CannotCompileException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // 自定义的类加载器
        MClassLoader mClassLoader = new MClassLoader();
        // 将生成的字节码 添加到map中
        mClassLoader.add(clasName, ctClass.toBytecode());

        // 加载类
        Class<?> aClass = mClassLoader.loadClass(clasName);

        // 获取有参构造
        Constructor<?> constructor = aClass.getConstructor(MInvocationHandler.class);
        // 实例化对象
        return constructor.newInstance(h);
    }

    /**
     * 生成类名
     *
     * @param interfaces
     * @return
     */
    private static String generateClassName(Class<?>[] interfaces) {
        String proxyName = Arrays.stream(interfaces).map(item -> item.getName()).collect(Collectors.joining("_"))
                .replace(".", "_");
        return CLASS_NAME_PRE + proxyName;
    }

    /**
     * 实现接口的方法
     *
     * @param interfaces
     * @param classPool
     * @param ctClass
     * @param classes
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private static void implMethodsAndAddClasses(Class<?>[] interfaces, ClassPool classPool, CtClass ctClass, CtClass[] classes) throws NotFoundException, CannotCompileException {
        for (int interfaceIndex = 0; interfaceIndex < interfaces.length; interfaceIndex++) {
            // 添加接口
            classes[interfaceIndex] = classPool.get(interfaces[interfaceIndex].getName());
            String interfaceName = interfaces[interfaceIndex].getName();

            for (int methodIndex = 0; methodIndex < classes[interfaceIndex].getDeclaredMethods().length; methodIndex++) {
                StringBuilder fieldName = getFieldName(methodIndex, interfaceName);
                // add method
                addMethod(fieldName.toString(), classes[interfaceIndex].getDeclaredMethods()[methodIndex], ctClass);
            }
        }
    }

    /**
     * 添加类成员变量
     *
     * @param interfaces
     * @param classPool
     * @param ctClass
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    private static void addStaticFields(Class<?>[] interfaces, ClassPool classPool, CtClass ctClass) throws CannotCompileException, NotFoundException {
        for (int i = 0; i < interfaces.length; i++) {
            for (int j = 0; j < interfaces[i].getDeclaredMethods().length; j++) {
                String interfaceName = interfaces[i].getName();
                StringBuilder fieldName = getFieldName(j, interfaceName);
                CtField ctField = new CtField(classPool.get(Method.class.getName()), fieldName.toString(), ctClass);
                ctField.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
                String methodName = interfaces[i].getDeclaredMethods()[j].getName();

                StringBuilder initializerCode = getFieldInitCode(j, interfaceName, methodName, interfaces[i]);

                CtField.Initializer initializer = CtField.Initializer.byExpr(initializerCode.toString());
                ctClass.addField(ctField, initializer);
            }
        }
    }

    /**
     * 类成员变量初始化代码
     *
     * @param methodIndex
     * @param interfaceName
     * @param methodName
     * @param interfaces
     * @return
     */
    private static StringBuilder getFieldInitCode(int methodIndex, String interfaceName, String methodName, Class<?> interfaces) {
        StringBuilder initializerCode = new StringBuilder();

        initializerCode.append("Class.forName(\"");
        initializerCode.append(interfaceName);
        initializerCode.append("\")");
        initializerCode.append(".getMethod(\"");
        initializerCode.append(methodName);
        initializerCode.append("\", new Class[]{");
        Class<?>[] parameterTypes = interfaces.getDeclaredMethods()[methodIndex].getParameterTypes();
        for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++) {
            if (parameterIndex != 0) {
                initializerCode.append(",");
            }
            initializerCode.append(" Class.forName(\"");
            initializerCode.append(parameterTypes[parameterIndex].getName());
            initializerCode.append("\")");
        }
        initializerCode.append("})");
        return initializerCode;
    }

    private static void addMethod(String fieldName, CtMethod ctMethod, CtClass ctClass) throws CannotCompileException, NotFoundException {
        CtMethod newMethod = CtNewMethod.copy(ctMethod, ctClass, null);
        String bodyCode;
        if (newMethod.getReturnType().isPrimitive()) {
            bodyCode = getMethodBodyCodeByPrimitive(newMethod, fieldName);
        } else {
            bodyCode = getMethodBodyCode(newMethod, fieldName);
        }

        newMethod.setBody(bodyCode);
        newMethod.setModifiers(Modifier.PUBLIC);
        ctClass.addMethod(newMethod);
    }

    /**
     * 方法返回类型是基本数据类型的
     * 如 boolean  -> 主动调用 java.lang.Boolean.parseBoolean() 进行拆箱
     *
     * @param newMethod
     * @param fieldName
     * @return
     * @throws NotFoundException
     */
    private static String getMethodBodyCodeByPrimitive(CtMethod newMethod, String fieldName) throws NotFoundException {
        StringBuilder sb = new StringBuilder();
        sb.append("return ");
        CtPrimitiveType ctPrimitiveType = (CtPrimitiveType) newMethod.getReturnType();
        if ("java.lang.Character".equals(ctPrimitiveType.getWrapperName())) {
            addInvokeSuperCode(newMethod, fieldName, sb);
            return sb.append(".toString().charAt(0);").toString();
        }
        sb.append(ctPrimitiveType.getWrapperName());
        switch (ctPrimitiveType.getWrapperName()) {
            case "java.lang.Boolean":
                sb.append(".parseBoolean(");
                break;
            case "java.lang.Byte":
                sb.append(".parseByte(");
                break;
            case "java.lang.Short":
                sb.append(".parseShort(");
                break;
            case "java.lang.Integer":
                sb.append(".parseInt(");
                break;
            case "java.lang.Long":
                sb.append(".parseLong(");
                break;
            case "java.lang.Float":
                sb.append(".parseFloat(");
                break;
            case "java.lang.Double":
                sb.append(".parseDouble(");
                break;
        }

        addInvokeSuperCode(newMethod, fieldName, sb);
        return sb.append(".toString());").toString();
    }

    /**
     * 调用父类invoke方法
     *
     * @param newMethod
     * @param fieldName
     * @param sb
     * @throws NotFoundException
     */
    private static void addInvokeSuperCode(CtMethod newMethod, String fieldName, StringBuilder sb) throws NotFoundException {
        sb.append("super.h.invoke($0, ");
        sb.append(fieldName);
        sb.append(", new Object[]{");
        for (int parameterIndex = 0; parameterIndex < newMethod.getParameterTypes().length; parameterIndex++) {
            if (parameterIndex != 0) {
                sb.append(", ");
            }
            sb.append("$");
            sb.append(parameterIndex + 1);
        }
        sb.append("})");
    }

    /**
     * 方法返回值不是基本类型的
     * 直接强转 如（Boolean）
     *
     * @param newMethod
     * @param fieldName
     * @return
     * @throws NotFoundException
     */
    private static String getMethodBodyCode(CtMethod newMethod, String fieldName) throws NotFoundException {
        StringBuilder sb = new StringBuilder();

        sb.append("return (");
        sb.append(newMethod.getReturnType().getName());
        sb.append(") ");
        addInvokeSuperCode(newMethod, fieldName, sb);
        return sb.append(";").toString();
    }

    private static StringBuilder getFieldName(int methodIndex, String interfaceName) {
        StringBuilder fieldName = new StringBuilder();
        fieldName.append("_");
        fieldName.append(interfaceName.replace(".", "_"));
        fieldName.append("_");
        fieldName.append(methodIndex);
        return fieldName;
    }
}
