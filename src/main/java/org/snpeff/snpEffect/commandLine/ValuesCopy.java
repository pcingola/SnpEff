package org.snpeff.snpEffect.commandLine;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.snpeff.util.Gpr;

/**
 * Copies fields having the same value and type from one object to another object
 *
 * Note: This can be done using different types of objects (only field names must match)
 */
public class ValuesCopy {

	boolean debug = false;
	Object src, dst;

	public ValuesCopy(Object src, Object dst) {
		this.src = src;
		this.dst = dst;
	}

	public void copy() {
		Class<?> srcClass = src.getClass();
		Class<?> dstClass = dst.getClass();

		// Get all fields and store them in a hash
		Map<String, Field> srcFieldByKey = new HashMap<>();
		for (Field field : fields(src)) {
			srcFieldByKey.put(key(field), field);
		}

		for (Field dstField : fields(dst)) {
			String dstKey = key(dstField);
			Field srcField = srcFieldByKey.get(dstKey);

			// Source field not found? Ignore
			if (srcField == null) {
				if (debug) Gpr.debug("Source field not found '" + dstKey + "'");
				continue;
			}

			// Copy value
			Object value;
			try {
				srcField.setAccessible(true);
				value = srcField.get(src);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("Cannot get value for field '" + srcClass.getCanonicalName() + "." + srcField.getName() + "'", e);
			}

			try {
				if (debug) Gpr.debug("Setting:" + dstClass.getCanonicalName() + "." + dstField.getName() + "' to '" + value + "'");
				dstField.setAccessible(true);
				dstField.set(dst, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("Cannot set value for field '" + dstClass.getCanonicalName() + "." + dstField.getName() + "'", e);
			}
		}

	}

	/**
	 * Get a list of all fields
	 */
	List<Field> fields(Object o) {
		List<Field> fields = new LinkedList<>();

		// Go up until object, add all declared fields
		for (Class<? extends Object> oclass = o.getClass(); oclass.getSuperclass() != null; oclass = oclass.getSuperclass())
			for (Field f : oclass.getDeclaredFields()) {

				// Ignore some type of fields
				if (Modifier.isStatic(f.getModifiers()) //
						|| Modifier.isFinal(f.getModifiers())) //
					continue;

				fields.add(f);
			}

		return fields;
	}

	String key(Field field) {
		return field.getType() + "\t" + field.getName();
	}

}
