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

import java.beans.PropertyEditorSupport;
import java.util.Properties;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.propertyeditors.PropertiesEditor;

/**
 * 属性值对象的编辑器。不是图形用户界面编辑器。
 * <br>注意：这个编辑器必须在它之前注册到JavaBeans API将可用.此包中的编辑器是由BeanWrapperImpl注册.
 * <br>所需格式在java.util.properties文档中定义.每个属性必须位于新行上
 * <br>
 * 当前的实现依赖于属性编辑器。.
 *
 * @author Rod Johnson
 */
public class PropertyValuesEditor extends PropertyEditorSupport {
	
	
	/**
	 * @see java.beans.PropertyEditor#setAsText(java.lang.String)
	 */
	public void setAsText(String s) throws IllegalArgumentException {
		PropertiesEditor pe = new PropertiesEditor();
		pe.setAsText(s);
		Properties props = (Properties) pe.getValue();
		setValue(new MutablePropertyValues(props));
	}

}

