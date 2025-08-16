package main.java.com.etu2728.modele;

import java.net.URL;
import java.io.File;
import java.util.ArrayList;

import main.java.com.etu2728.annotation.Param;
import main.java.com.etu2728.annotation.ParamObject;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Scanner {

    public static ArrayList<Class<?>> getAllClasses(String packageName) throws ClassNotFoundException, IOException {
        ArrayList<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');

        URL resource = classLoader.getResource(path);

        if (resource == null) {
            return classes;
        }

        File packageDir = new File(resource.getFile().replace("%20", " "));

        for (File file : packageDir.listFiles()) {
            if (file.isDirectory()) {
                classes.addAll(Scanner.getAllClasses(packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }

        return classes;
    }

    public static ArrayList<Class<?>> getControllerClasses(String packageName,
            Class<? extends Annotation> annotationController)
            throws ClassNotFoundException, IOException {
        ArrayList<Class<?>> classes = getAllClasses(packageName);

        ArrayList<Class<?>> result = new ArrayList<Class<?>>();

        for (Class<?> classe : classes) {
            if (classe.isAnnotationPresent(annotationController)) {
                result.add(classe);
            }
        }

        return result;
    }
    
    public static Object processObjectParam(Class<?> classe, String paramObjectName, HttpServletRequest request) throws IllegalArgumentException, IllegalAccessException, Exception {
        Object object = classe.getDeclaredConstructor().newInstance();

        for (Field attribut : classe.getDeclaredFields()) {
            attribut.setAccessible(true);
            String paramName = attribut.getName();
            String paramValue = request.getParameter(paramObjectName + "." + paramName);
            if (paramValue != null) {
                attribut.set(object, convertValue(attribut.getType(), paramValue));
            }
            else {
                throw new IllegalArgumentException("L'attribut' " + paramName + " de la classe " + classe.getName() + " est null!");
            }
        }

        return object;
    }
    
    private static Object convertValue(Class<?> type, String value) throws Exception {
        if (type.equals(String.class)) {
            return value;
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            return Float.parseFloat(value);
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            return Long.parseLong(value);
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            return Short.parseShort(value);
        } else if (type.equals(byte.class) || type.equals(Byte.class)) {
            return Byte.parseByte(value);
        } else {
            throw new IllegalArgumentException("Type non supporté: " + type.getName());
        }
    }

    public static ArrayList<Object> parameterMethod(Method method, HttpServletRequest request) throws Exception {
        ArrayList<Object> parameterValues = new ArrayList<>();
        
        // Récupérer les paramètres de la méthode
        Parameter[] parameters = method.getParameters();
        
        for (Parameter parameter : parameters) {
            Object value = null;
    
            if (parameter.getType().equals(MySession.class)) {
                HttpSession session = request.getSession();
                MySession mySession = new MySession(session);

                value = mySession;
            }
            else {
                if (parameter.isAnnotationPresent(ParamObject.class)) {
                    ParamObject paramObject = parameter.getAnnotation(ParamObject.class);
                    String paramObjectName = paramObject.value();
    
                    value = processObjectParam(parameter.getType(), paramObjectName, request);
                } else if (parameter.isAnnotationPresent(Param.class)) {
                    Param paramAnnotation = parameter.getAnnotation(Param.class);
                    String paramName = paramAnnotation.name();
                    String paramValue = request.getParameter(paramName);
    
                    value = convertValue(parameter.getType(), paramValue);
                } else {
                    // Si l'annotation @Param n'est pas présente, on prend le nom du paramètre
                    String paramName = parameter.getName();
                    String paramValue = request.getParameter(paramName);
                    
                    value = convertValue(parameter.getType(), paramValue);
                }
            }
    
            if (value == null) {
                throw new IllegalArgumentException("Paramètre manquant ou invalide: " + parameter.getName());
            }

            parameterValues.add(value);
        }
        
        return parameterValues;
    }

}
