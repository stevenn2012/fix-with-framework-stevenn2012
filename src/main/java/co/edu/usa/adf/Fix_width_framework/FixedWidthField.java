package co.edu.usa.adf.Fix_width_framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

public @interface FixedWidthField {
	int posicion() default 0;
	int width() default 0;
}
