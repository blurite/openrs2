package dev.openrs2.deob.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ ElementType.LOCAL_VARIABLE })
public @interface Pc {
	int value();
}