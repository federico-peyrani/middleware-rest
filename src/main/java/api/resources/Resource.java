package api.resources;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates an object that is meant to be represented as a resource, that is, a json-serialized version of the same
 * that follows the HATEOAS standard.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Resource {

    /**
     * @return the URL where the resource can be found
     */
    String href();

    /**
     * @return whether the url follows some type of parameterized template
     */
    boolean templated() default false;

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @interface Property {

        String key();

        boolean external() default false;

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Method {

        String key();

        String href();

    }

}
