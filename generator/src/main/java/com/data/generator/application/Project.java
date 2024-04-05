package com.data.generator.application;

import jakarta.servlet.http.HttpServletRequest;
import javassist.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Controller
@RequestMapping("/project")
public class Project {
    @ResponseBody
    @PostMapping("/define-class")
    public String defineClass(HttpServletRequest request) {
        Map<String, String[]> parameters = request.getParameterMap();

        ClassPool classPool = ClassPool.getDefault();
        String className = parameters.get("className")[0];
        String[] classesToImport = parameters.get("importClass");

        for(int i = 0; i < (classesToImport != null ? classesToImport.length : 0); i++) {
            classPool.importPackage(classesToImport[i]);
        }

        CtClass newClass = classPool.makeClass(className);
        newClass.defrost();

        String response = "package: " + newClass.getPackageName()
                + "class: " + newClass.getName();

        return response;
    }

    @ResponseBody
    @PostMapping("/define-method")
    public String defineMethod(HttpServletRequest request) throws CannotCompileException, NotFoundException {
        Map<String, String[]> parameters = request.getParameterMap();
        String className = parameters.get("className")[0];
        String method = parameters.get("method")[0];

        ClassPool classPool = ClassPool.getDefault();
        CtClass existingClass = classPool.getCtClass(className);
        existingClass.defrost();

        CtMethod newMethod = CtNewMethod.make(method, existingClass);
        existingClass.addMethod(newMethod);

        return "Method Defined.";
    }

    @ResponseBody
    @PostMapping("/execute-method")
    public String executeMethod(HttpServletRequest request) throws NotFoundException, IOException, CannotCompileException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Map<String, String[]> parameters = request.getParameterMap();
        String className = parameters.get("className")[0];
        String method = parameters.get("methodName")[0];

        ClassPool classPool = ClassPool.getDefault();
        CtClass existingClass = classPool.getCtClass(className);

        CustomClassLoader loader = new CustomClassLoader();
        Class<?> definedClass = loader.defineClass(existingClass.getName(), existingClass.toBytecode());

        Object obj = definedClass.newInstance();
        Method methodIn = definedClass.getMethod(method);

        methodIn.invoke(obj);
        System.gc();
        existingClass.defrost();
        return "Method Executed";
    }

    static class CustomClassLoader extends ClassLoader {
        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
}
