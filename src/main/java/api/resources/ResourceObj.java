package api.resources;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceObj {

    static final String LINKS = "_links";
    static final String LINKS_SELF = "self";
    static final String LINKS_HREF = "href";
    static final String EMBEDDED = "_embedded";

    private final @NotNull Class<?> cls;
    private final @NotNull Object object;
    private final @NotNull Resource resource;

    ResourceObj(@NotNull Class<?> cls, @NotNull Object object) {

        if (!cls.isAssignableFrom(object.getClass())) {
            throw new SerializationException("Class "
                    + cls.getName()
                    + " is not superclass of "
                    + object.getClass().getName());
        }

        if (!cls.isAnnotationPresent(Resource.class)) {
            throw new SerializationException.NotAnnotatedException(object, Resource.class);
        }

        this.cls = cls;
        this.object = object;
        this.resource = cls.getAnnotation(Resource.class);
    }

    public static JSONObject build(Object object) {
        return new ResourceObj(object.getClass(), object).toJSON();
    }

    public static <T> JSONObject build(Class<T> cls, Object object) {
        return new ResourceObj(cls, object).toJSON();
    }

    public JSONObject toJSON() {
        return toJSON(true);
    }

    JSONObject toJSON(boolean includeEmbedded) {

        JSONObject output = new JSONObject();
        JSONObject links = new JSONObject();

        output.put(LINKS, links
                .put(LINKS_SELF, new JSONObject()
                        .put(LINKS_HREF, resource.self())
                        .put("templated", resource.templated())));

        if (!includeEmbedded) return output;

        List<PropertyObj> propertyObjList = new ArrayList<>();
        Arrays.stream(cls.getDeclaredFields())
                .filter(it -> it.isAnnotationPresent(Resource.Property.class))
                .map(it -> new PropertyObj(object, it))
                .forEach(propertyObjList::add);
        Arrays.stream(cls.getDeclaredMethods())
                .filter(it -> it.isAnnotationPresent(Resource.Property.class))
                .map(it -> new PropertyObj(object, it))
                .forEach(propertyObjList::add);

        if (!propertyObjList.isEmpty()) output.put(EMBEDDED, new JSONObject());
        JSONObject embedded = output.getJSONObject(EMBEDDED);

        propertyObjList.forEach(it -> it.addToJSON(links, embedded));

        return output;
    }

    static class SerializationException extends RuntimeException {

        SerializationException(String message) {
            super(message);
        }

        static class NotAnnotatedException extends SerializationException {

            <T extends Annotation> NotAnnotatedException(Object object, Class<T> annotation) {
                super(object.getClass().getTypeName()
                        + " is not annotated with "
                        + annotation.getName());
            }

        }

    }

}
