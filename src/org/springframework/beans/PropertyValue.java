/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans;

import java.io.Serializable;

import org.springframework.util.ObjectUtils;

/**
 * 
 * 类来保存单个属性的信息和值。在此处使用对象，而不仅仅是将所有属性存储在按属性名称键控的映射，允许更大的灵活性，并且如有必要，能够以特殊方式处理索引属性等。
 *<p>请注意，该值不需要是最终所需的类型：beanwrapper实现应该处理任何必要的转换，如此对象不知道它将应用到的对象的任何信息。
 * 
 * @author Rod Johnson
 * @since 13 May 2001
 * @see PropertyValues
 * @see BeanWrapper
 */
public class PropertyValue implements Serializable {

	private final String name;

	private final Object value;

	/**
	 * Create a new PropertyValue instance.
	 * 
	 * @param name
	 *            name of the property
	 * @param value
	 *            value of the property (possibly before type conversion)
	 */
	public PropertyValue(String name, Object value) {
		if (name == null) {
			throw new IllegalArgumentException("Property name cannot be null");
		}
		this.name = name;
		this.value = value;
	}

	/**
	 * Return the name of the property.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the value of the property.
	 * <p>
	 * Note that type conversion will <i>not</i> have occurred here. It is the
	 * responsibility of the BeanWrapper implementation to perform type
	 * conversion.
	 */
	public Object getValue() {
		return value;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PropertyValue)) {
			return false;
		}
		PropertyValue otherPv = (PropertyValue) other;
		return (this.name.equals(otherPv.name) && ObjectUtils.nullSafeEquals(this.value, otherPv.value));
	}

	public int hashCode() {
		return this.name.hashCode() * 29 + (this.value == null ? 0 : this.value.hashCode());
	}

	public String toString() {
		return "PropertyValue: name='" + this.name + "', value=[" + this.value + "]";
	}

}
