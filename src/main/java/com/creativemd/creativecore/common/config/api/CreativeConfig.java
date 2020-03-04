package com.creativemd.creativecore.common.config.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.creativemd.creativecore.common.config.sync.ConfigSynchronization;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD })
public @interface CreativeConfig {
	
	String name() default "";
	
	ConfigSynchronization type() default ConfigSynchronization.UNIVERSAL;
	
	boolean requiresRestart() default false;
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value = { ElementType.FIELD })
	@interface IntRange {
		
		public int min();
		
		public int max();
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value = { ElementType.FIELD })
	@interface DoubleRange {
		
		public double min();
		
		public double max();
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value = { ElementType.FIELD })
	@interface FloatRange {
		
		public float min();
		
		public float max();
		
	}
	
}
