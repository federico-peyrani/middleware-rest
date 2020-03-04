package api.resources;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class PropertyObj {

    final AccessibleObject accessibleObject;
    final Object value;
    final Resource.Property annotation;

    PropertyObj(Object object, Method method) {

        if (!method.isAnnotationPresent(Resource.Property.class)) {
            throw new ResourceObj.SerializationException.NotAnnotatedException(method, Resource.Property.class);
        }

        this.accessibleObject = method;
        this.annotation = method.getAnnotation(Resource.Property.class);

        try {
            method.setAccessible(true);
            value = method.invoke(object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ResourceObj.SerializationException("Invocation of method "
                    + method.getName()
                    + " of class "
                    + method.getDeclaringClass().getName()
                    + " threw  "
                    + e.getClass() + ": "
                    + e.getMessage());
        }
    }

    PropertyObj(Object object, Field field) {

        if (!field.isAnnotationPresent(Resource.Property.class)) {
            throw new ResourceObj.SerializationException("Field "
                    + field.getName()
                    + " is not annotated with "
                    + Resource.Property.class.getName());
        }

        this.accessibleObject = field;
        this.annotation = field.getAnnotation(Resource.Property.class);

        try {
            field.setAccessible(true);
            value = field.get(object);
        } catch (IllegalAccessException e) {
            throw new ResourceObj.SerializationException("Invocation of method "
                    + field.getName()
                    + " of class "
                    + field.getDeclaringClass().getName()
                    + " threw  "
                    + e.getClass() + ": "
                    + e.getMessage());
        }
    }

    void addToJSON(JSONObject links, JSONObject embedded) {

        Class<?> propertyClass = value.getClass();

        if (annotation.external()) {
            links.put(annotation.key(), new ResourceObj(propertyClass, value)
                    .toJSON(false)
                    .getJSONObject(ResourceObj.LINKS)
                    .getJSONObject(ResourceObj.LINKS_SELF));
            return;
        }

        if (propertyClass.isAnnotationPresent(Resource.class)) {
            embedded.put(annotation.key(), new ResourceObj(propertyClass, value).toJSON());
        } else if (propertyClass.isPrimitive() || propertyClass == String.class) {
            embedded.put(annotation.key(), value);
        } else if (Iterable.class.isAssignableFrom(propertyClass)) {
            JSONArray array = new JSONArray();
            embedded.put(annotation.key(), array);
            Iterable<?> iterable = (Iterable<?>) value;
            iterable.forEach(it -> array.put(new ResourceObj(it.getClass(), it).toJSON()));
        } else {
            throw new ResourceObj.SerializationException.NotAnnotatedException(propertyClass, Resource.class);
        }

    }

}
