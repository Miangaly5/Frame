package modele;

import annotation.Param;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.Paranamer;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.annotation.Annotation;
import jakarta.servlet.http.HttpServletRequest;

public class ListClass {
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
                classes.addAll(ListClass.getAllClasses(packageName + "." + file.getName()));
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

        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(annotationController)) {
                result.add(clazz);
            }
        }

        return result;
    }
    
    public static ArrayList<Object> parameterMethod(Method method, HttpServletRequest request){
        ArrayList<Object> parameterValues = new ArrayList<>();
        Paranamer paranamer = new AdaptiveParanamer();
        String[] parameterNamesArray = paranamer.lookupParameterNames(method, false);
    
        // Récupérer les noms des paramètres de la méthode en utilisant la réflexion
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String value = null;
            if (parameter.isAnnotationPresent(Param.class)) {
                Param argument = parameter.getAnnotation(Param.class);
                String arg_name = argument.value();
                value = request.getParameter(arg_name);
            } else {
                String paramName = parameterNamesArray[i];
                String[] requestParamNames = request.getParameterMap().keySet().toArray(new String[0]);
                boolean found = false;
                for (String requestParamName : requestParamNames) {
                    if (requestParamName.equals(paramName)) {
                        found = true;
                        value = request.getParameter(requestParamName);
                        break;
                    }
                }
                if (!found) {
                    throw new IllegalArgumentException("Le paramètre " + paramName + " n'existe pas dans la méthode");
                }
            }
            if (value == null) {
                throw new IllegalArgumentException("Paramètre manquant ou invalide: " + parameter.getName());
            }
            parameterValues.add(value);
        }
        
        return parameterValues;
    }

    //Pour les arguments de type Object
    public static ArrayList<Object> getParameterValuesForObjects(Method method, HttpServletRequest request) throws Exception {
        ArrayList<Object> parameterValues = new ArrayList<>();
        Paranamer paranamer = new AdaptiveParanamer();
        String[] parameterNamesArray = paranamer.lookupParameterNames(method, false);
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            int index = i;
            Parameter parameter = parameters[i];
            Class<?> paramType = parameter.getType();

            if (!paramType.isPrimitive() && paramType != String.class) {
                @SuppressWarnings("deprecation")
                Object paramObject = paramType.newInstance();

                Map<String, String[]> parameterMap = request.getParameterMap().entrySet().stream()
                        .filter(entry -> entry.getKey().startsWith(parameterNamesArray[index] + "."))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String key = entry.getKey();
                    String attributeName = key.substring(key.lastIndexOf(".") + 1);
                    String value = entry.getValue()[0];
                    try {
                        Field field = paramType.getDeclaredField(attributeName);
                        field.setAccessible(true);
                        field.set(paramObject, convertValue(field.getType(), value));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Impossible de définir la valeur du paramètre " + key, e);
                    }
                }
                parameterValues.add(paramObject);
            }
            else {
                throw new IllegalArgumentException("Le paramètre " + parameter.getName() + " n'est pas un objet.");
            }
        }
        return parameterValues;
    }

    private static Object convertValue(Class<?> type, String value) {
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.valueOf(value);
        }
        else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.valueOf(value);
        }
        else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.valueOf(value);
        }
        else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.valueOf(value);
        }
        else {
            return value;
        }
    }

    public static ArrayList<Object> getParameterValuesCombined(Method method, HttpServletRequest request) throws Exception {
        ArrayList<Object> parameterValues = new ArrayList<>();
        Paranamer paranamer = new AdaptiveParanamer();
        String[] parameterNamesArray = paranamer.lookupParameterNames(method, false);    
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            int index = i;
            Parameter parameter = parameters[i];
            Class<?> paramType = parameter.getType();
    
            if (!paramType.isPrimitive() && paramType != String.class) {
                @SuppressWarnings("deprecation")
                Object paramObject = paramType.newInstance();    
                Map<String, String[]> parameterMap = request.getParameterMap().entrySet().stream()
                        .filter(entry -> entry.getKey().startsWith(parameterNamesArray[index] + "."))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    
                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String key = entry.getKey();
                    String attributeName = key.substring(key.lastIndexOf(".") + 1);
                    String value = entry.getValue()[0];    
                    try {
                        Field field = paramType.getDeclaredField(attributeName);
                        field.setAccessible(true);
                        field.set(paramObject, convertValue(field.getType(), value));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Impossible de définir la valeur du paramètre " + key, e);
                    }
                }    
                parameterValues.add(paramObject);
            }
            else {
                String value = null;
                if (parameter.isAnnotationPresent(Param.class)) {
                    Param argument = parameter.getAnnotation(Param.class);
                    String arg_name = argument.value();
                    value = request.getParameter(arg_name);
                } else {
                    String paramName = parameterNamesArray[i];
                    String[] requestParamNames = request.getParameterMap().keySet().toArray(new String[0]);
                    boolean found = false;
                    for (String requestParamName : requestParamNames) {
                        if (requestParamName.equals(paramName)) {
                            found = true;
                            value = request.getParameter(requestParamName);
                            break;
                        }
                    }
                    if (!found) {
                        value ="null";
                    }
                }
                if (value == null) {
                    throw new IllegalArgumentException("Paramètre manquant ou invalide: " + parameter.getName());
                }
                parameterValues.add(value);
            }
        }
        return parameterValues;
    }
}
