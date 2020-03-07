package api.resources;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceObj {

    static final String LINKS = "_links";
    static final String LINKS_SELF = "self";
    static final String LINKS_HREF = "href";
    static final String EMBEDDED = "_embedded";
    private static final Pattern PATTERN = Pattern.compile("\\$\\{(?<name>[a-zA-Z]+)}");
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

        // create a list of all the properties of the object, that includes every field and every
        // method annotated with Resource.Property
        List<PropertyObj> propertyObjList = new ArrayList<>();
        Arrays.stream(cls.getDeclaredFields())
                .filter(it -> it.isAnnotationPresent(Resource.Property.class))
                .map(it -> new PropertyObj(object, it))
                .forEach(propertyObjList::add);
        Arrays.stream(cls.getDeclaredMethods())
                .filter(it -> it.isAnnotationPresent(Resource.Property.class))
                .map(it -> new PropertyObj(object, it))
                .forEach(propertyObjList::add);

        // if the href of the resource is parameterized, use a regex to swap the parameter
        // with the actual value from the matching property
        String href = resource.href();
        Matcher matcher = PATTERN.matcher(resource.href());
        while (matcher.find()) {
            String paramName = matcher.group("name");
            String group = matcher.group(0);
            href = href.replace(group, propertyObjList.stream()
                    .filter(it -> it.annotation.key().equals(paramName))
                    .findFirst()
                    .orElseThrow(() -> new SerializationException("Parameter " + paramName + " was not found"))
                    .value.toString());
        }

        // add the '_links' object and inflate it with the self reference
        output.put(LINKS, links);
        links.put(LINKS_SELF, new JSONObject()
                .put(LINKS_HREF, href)
                .put("templated", resource.templated()));

        Arrays.stream(cls.getDeclaredMethods())
                .filter(it -> it.isAnnotationPresent(Resource.Method.class))
                .forEach(it -> {
                    Resource.Method annotation = it.getAnnotation(Resource.Method.class);
                    links.put(annotation.key(), new JSONObject().put(LINKS_HREF, annotation.href()));
                });

        // if any property is present, and the caller wants to add the property to the object, then
        // add the '_embedded' object and inflate it
        if (includeEmbedded && !propertyObjList.isEmpty()) {
            output.put(EMBEDDED, new JSONObject());
            JSONObject embedded = output.getJSONObject(EMBEDDED);
            propertyObjList.forEach(it -> it.addToJSON(links, embedded));
        }

        return output;
    }

    static class SerializationException extends RuntimeException {

        SerializationException(String message) {
            super(message);
        }

        static class NotAnnotatedException extends SerializationException {

            <T extends Annotation> NotAnnotatedException(Object object, Class<T> annotation) {
                super(object.getClass().getTypeName()
                        + " '"
                        + object
                        + "' is not annotated with "
                        + annotation.getName());
            }

        }

    }

}
