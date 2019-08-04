/*
 * Copyright 2002-2007 the original author or authors.
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

import java.beans.PropertyDescriptor;

/**
 * Spring的低级JavaBeans基础设施的中央接口。
 * 扩展@link propertyaccessor和@link propertyeditorregistry接口。
 *
 * <p>通常不直接使用，而是通过
 * {@link org.springframework.beans.factory.BeanFactory} or a
 * {@link org.springframework.validation.DataBinder}.
 *
 * <p>提供分析和操作标准JavaBeans的操作:
 * 获取和设置属性值（单独或批量）的能力,
 * 获取属性描述符，并查询属性的可读性/可写性.
 *
 * <p>此接口支持启用设置子属性的属性的深度不受限制
 * A <code>BeanWrapper</code> 可以重复使用, with its
 * {@link #setWrappedInstance(Object) target object} (包装的javaBean实例）根据需要更改
 *
 * <p>可以重复使用beanwrapper实例及其目标对象, （封装的JavaBean实例）更改
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13 April 2001
 * @see PropertyAccessor
 * @see PropertyEditorRegistry
 * @see BeanWrapperImpl
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.validation.DataBinder
 */
public interface BeanWrapper extends PropertyAccessor, PropertyEditorRegistry {

	/**
	 * Change the wrapped JavaBean object.
	 * @param obj the bean instance to wrap
	 */
	void setWrappedInstance(Object obj);

	/**
	 * Return the bean instance wrapped by this object, if any.
	 * @return the bean instance, or <code>null</code> if none set
	 */
	Object getWrappedInstance();

	/**
	 * Return the type of the wrapped JavaBean object.
	 * @return the type of the wrapped bean instance,
	 * or <code>null</code> if no wrapped object has been set
	 */
	Class getWrappedClass();

	/**
	 * Set whether to extract the old property value when applying a
	 * property editor to a new value for a property.
	 * <p>Default is "false", avoiding side effects caused by getters.
	 * Turn this to "true" to expose previous property values to custom editors.
	 */
	void setExtractOldValueForEditor(boolean extractOldValueForEditor);

	/**
	 * Return whether to extract the old property value when applying a
	 * property editor to a new value for a property.
	 */
	boolean isExtractOldValueForEditor();


	/**
	 * Obtain the PropertyDescriptors for the wrapped object
	 * (as determined by standard JavaBeans introspection).
	 * @return the PropertyDescriptors for the wrapped object
	 */
	PropertyDescriptor[] getPropertyDescriptors();

	/**
	 * Obtain the property descriptor for a specific property
	 * of the wrapped object.
	 * @param propertyName the property to obtain the descriptor for
	 * (may be a nested path, but no indexed/mapped property)
	 * @return the property descriptor for the specified property
	 * @throws InvalidPropertyException if there is no such property
	 */
	PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException;

	/**
	 * Determine the property type for the specified property,
	 * either checking the property descriptor or checking the value
	 * in case of an indexed or mapped element.
	 * @param propertyName property to check status for
	 * (may be a nested path and/or an indexed/mapped property)
	 * @return the property type for the particular property,
	 * or <code>null</code> if not determinable
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't readable
	 */
	Class getPropertyType(String propertyName) throws BeansException;

	/**
	 * Determine whether the specified property is readable.
	 * <p>Returns <code>false</code> if the property doesn't exist.
	 * @param propertyName property to check status for
	 * (may be a nested path and/or an indexed/mapped property)
	 * @return whether the property is readable
	 */
	boolean isReadableProperty(String propertyName);

	/**
	 * Determine whether the specified property is writable.
	 * <p>Returns <code>false</code> if the property doesn't exist.
	 * @param propertyName property to check status for
	 * (may be a nested path and/or an indexed/mapped property)
	 * @return whether the property is writable
	 */
	boolean isWritableProperty(String propertyName);

}
