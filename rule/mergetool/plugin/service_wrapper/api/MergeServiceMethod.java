package top.fifthlight.mergetools.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks an implementation method to be wrapped by the merged service wrapper; the annotated
/// methods define the wrapper's surface. Inherited methods need an explicit super delegation.
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface MergeServiceMethod {
    /// ASM descriptor of this annotation, for build-time scanners.
    String DESCRIPTOR = "Ltop/fifthlight/mergetools/api/MergeServiceMethod;";
}
