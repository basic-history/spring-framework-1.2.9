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

package org.springframework.beans.factory.config;

import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.core.CollectionFactory;

/**
 * Simple factory for shared Map instances. Allows for central setup
 * of Maps via the "map" element in XML bean definitions.
 *
 * @author Juergen Hoeller
 * @since 09.12.2003
 * @see SetFactoryBean
 * @see ListFactoryBean
 */
public class MapFactoryBean extends AbstractFactoryBean {

	private Map sourceMap;

	private Class targetMapClass;


	/**
	 * Set the source Map, typically populated via XML "map" elements.
	 */
	public void setSourceMap(Map sourceMap) {
		this.sourceMap = sourceMap;
	}

	/**
	 * Set the class to use for the target Map. Can be populated with a fully
	 * qualified class name when defined in a Spring application context.
	 * <p>Default is a linked HashMap, keeping the registration order.
	 * If no linked Map implementation is available, a plain HashMap will
	 * be used as fallback (not keeping the registration order).
	 * @see org.springframework.core.CollectionFactory#createLinkedMapIfPossible
	 */
	public void setTargetMapClass(Class targetMapClass) {
		if (targetMapClass == null) {
			throw new IllegalArgumentException("'targetMapClass' must not be null");
		}
		if (!Map.class.isAssignableFrom(targetMapClass)) {
			throw new IllegalArgumentException("'targetMapClass' must implement [java.util.Map]");
		}
		this.targetMapClass = targetMapClass;
	}


	public Class getObjectType() {
		return Map.class;
	}

	protected Object createInstance() {
		if (this.sourceMap == null) {
			throw new IllegalArgumentException("'sourceMap' is required");
		}
		Map result = null;
		if (this.targetMapClass != null) {
			result = (Map) BeanUtils.instantiateClass(this.targetMapClass);
		}
		else {
			result = CollectionFactory.createLinkedMapIfPossible(this.sourceMap.size());
		}
		result.putAll(this.sourceMap);
		return result;
	}

}
