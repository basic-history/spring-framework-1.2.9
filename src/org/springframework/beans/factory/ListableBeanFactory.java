/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory;

import java.util.Map;

import org.springframework.beans.BeansException;

/**
 * Extension of the BeanFactory interface to be implemented by bean factories
 * that can enumerate all their bean instances, rather than attempting bean lookup
 * by name one by one as requested by clients. BeanFactory implementations that
 * preload all their beans (for example, DOM-based XML factories) may implement
 * this interface. This interface is discussed in "Expert One-on-One J2EE Design
 * and Development", by Rod Johnson.
 *
 * <p>If this is a HierarchicalBeanFactory, the return values will <i>not</i> take
 * any BeanFactory hierarchy into account, but will relate only to the beans defined
 * in the current factory. Use the BeanFactoryUtils helper class to consider beans
 * in ancestor factories too.
 * 
 * <p>The methods in this interface will just respect bean definitions of this factory.
 * They will ignore any singleton beans that have been registered by other means like
 * ConfigurableBeanFactory's <code>registerSingleton</code> method, with the exception
 * of <code>getBeanNamesOfType</code> and <code>getBeansOfType</code> which will check
 * such manually registered singletons too. Of course, BeanFactory's <code>getBean</code>
 * does allow transparent access to such special beans as well. However, in typical
 * scenarios, all beans will be defined by external bean definitions anyway, so most
 * applications don't need to worry about this differentation.
 *
 * <p>With the exception of <code>getBeanDefinitionCount</code> and
 * <code>containsBeanDefinition</code>, the methods in this interface are
 * not designed for frequent invocation. Implementations may be slow.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16 April 2001
 * @see HierarchicalBeanFactory
 * @see BeanFactoryUtils
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#registerSingleton
 */
public interface ListableBeanFactory extends BeanFactory {

	/**
	 * Check if this bean factory contains a bean definition with the given name.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use <code>containsBean</code> to check ancestor factories too.
	 * <p>Note: Ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * @param beanName the name of the bean to look for
	 * @return if this bean factory contains a bean definition with the given name
	 * @see #containsBean
	 */
	boolean containsBeanDefinition(String beanName);

	/**
	 * Return the number of beans defined in the factory.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' <code>countBeansIncludingAncestors</code>
	 * to include beans in ancestor factories too.
	 * <p>Note: Ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * @return the number of beans defined in the factory
	 * @see BeanFactoryUtils#countBeansIncludingAncestors
	 */
	int getBeanDefinitionCount();

	/**
	 * Return the names of all beans defined in this factory.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' <code>beanNamesIncludingAncestors</code>
	 * to include beans in ancestor factories too.
	 * <p>Note: Ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * @return the names of all beans defined in this factory,
	 * or an empty array if none defined
	 * @see BeanFactoryUtils#beanNamesIncludingAncestors(ListableBeanFactory)
	 */
	String[] getBeanDefinitionNames();
	
	/**
	 * Return the names of beans matching the given type (including subclasses),
	 * judging from the bean definitions. Merges child bean definition with
	 * their parent before checking the type.
	 * <p>Does <i>not</i> consider objects created by FactoryBeans but rather the
	 * FactoryBean classes themselves, avoiding instantiation of any beans. Use
	 * <code>getBeanNamesForType</code> to match objects created by FactoryBeans.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' <code>beanNamesIncludingAncestors</code>
	 * to include beans in ancestor factories too.
	 * <p>Note: Ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * @param type the class or interface to match, or <code>null</code> for all bean names
	 * @return the names of beans matching the given object type 
	 * (including subclasses), or an empty array if none
	 * @deprecated in favor of getBeanNamesForType.
	 * This method will be removed as of Spring 2.0.
	 * @see #getBeanNamesForType
	 * @see BeanFactoryUtils#beanNamesIncludingAncestors(ListableBeanFactory, Class)
	 */
	String[] getBeanDefinitionNames(Class type);

	/**
	 * Return the names of beans matching the given type (including subclasses),
	 * judging from either bean definitions or the value of <code>getObjectType</code>
	 * in the case of FactoryBeans.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' <code>beanNamesForTypeIncludingAncestors</code>
	 * to include beans in ancestor factories too.
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 * <p>This version of getBeanNamesForType matches all kinds of beans, be it
	 * singletons, prototypes, or FactoryBeans. In most implementations, the
	 * result will be the same as for <code>getBeanNamesOfType(type, true, true)</code>.
	 * <p>Bean names returned by this method should always return bean names <i>in the
	 * order of definition</i> in the backend configuration, as far as possible.
	 * @param type the class or interface to match, or <code>null</code> for all bean names
	 * @return the names of beans (or objects created by FactoryBeans) matching
	 * the given object type (including subclasses), or an empty array if none
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	String[] getBeanNamesForType(Class type);

	/**
	 * Return the names of beans matching the given type (including subclasses),
	 * judging from either bean definitions or the value of <code>getObjectType</code>
	 * in the case of FactoryBeans.
	 * <p>Does consider objects created by FactoryBeans if the "includeFactoryBeans"
	 * flag is set, which means that FactoryBeans will get initialized. If the
	 * object created by the FactoryBean doesn't match, the raw FactoryBean itself
	 * will be matched against the type. If "includeFactoryBeans" is not set,
	 * only raw FactoryBeans will be checked (which doesn't require initialization
	 * of each FactoryBean).
$	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' <code>beanNamesForTypeIncludingAncestors</code>
	 * to include beans in ancestor factories too.
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 * <p>Bean names returned by this method should always return bean names <i>in the
	 * order of definition</i> in the backend configuration, as far as possible.
	 * @param type the class or interface to match, or <code>null</code> for all bean names
	 * @param includePrototypes whether to include prototype beans too
	 * or just singletons (also applies to FactoryBeans)
	 * @param includeFactoryBeans whether to include <i>objects created by
	 * FactoryBeans</i> (or by factory methods with a "factory-bean" reference)
	 * too, or just conventional beans. Note that FactoryBeans need to be
	 * initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans (and "factory-bean" references).
	 * @return the names of beans (or objects created by FactoryBeans) matching
	 * the given object type (including subclasses), or an empty array if none
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 */
	String[] getBeanNamesForType(Class type, boolean includePrototypes, boolean includeFactoryBeans);

	/**
	 * Return the bean instances that match the given object type (including
	 * subclasses), judging from either bean definitions or the value of
	 * <code>getObjectType</code> in the case of FactoryBeans.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' <code>beansOfTypeIncludingAncestors</code>
	 * to include beans in ancestor factories too.
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 * <p>This version of getBeansOfType matches all kinds of beans, be it
	 * singletons, prototypes, or FactoryBeans. In most implementations, the
	 * result will be the same as for <code>getBeansOfType(type, true, true)</code>.
	 * <p>The Map returned by this method should always return bean names and
	 * corresponding bean instances <i>in the order of definition</i> in the
	 * backend configuration, as far as possible. This will usually mean that
	 * either JDK 1.4 or Commons Collections needs to be available.
	 * @param type the class or interface to match, or <code>null</code> for all concrete beans
	 * @return a Map with the matching beans, containing the bean names as
	 * keys and the corresponding bean instances as values
	 * @throws BeansException if a bean could not be created
	 * @since 1.1.2
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	Map getBeansOfType(Class type) throws BeansException;

	/**
	 * Return the bean instances that match the given object type (including
	 * subclasses), judging from either bean definitions or the value of
	 * <code>getObjectType</code> in the case of FactoryBeans.
	 * <p>Does consider objects created by FactoryBeans if the "includeFactoryBeans"
	 * flag is set, which means that FactoryBeans will get initialized. If the
	 * object created by the FactoryBean doesn't match, the raw FactoryBean itself
	 * will be matched against the type. If "includeFactoryBeans" is not set,
	 * only raw FactoryBeans will be checked (which doesn't require initialization
	 * of each FactoryBean).
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' <code>beansOfTypeIncludingAncestors</code>
	 * to include beans in ancestor factories too.
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 * <p>The Map returned by this method should always return bean names and
	 * corresponding bean instances <i>in the order of definition</i> in the
	 * backend configuration, as far as possible. This will usually mean that
	 * either JDK 1.4 or Commons Collections needs to be available.
	 * @param type the class or interface to match, or <code>null</code> for all concrete beans
	 * @param includePrototypes whether to include prototype beans too
	 * or just singletons (also applies to FactoryBeans)
	 * @param includeFactoryBeans whether to include <i>objects created by
	 * FactoryBeans</i> (or by factory methods with a "factory-bean" reference)
	 * too, or just conventional beans. Note that FactoryBeans need to be
	 * initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans (and "factory-bean" references).
	 * @return a Map with the matching beans, containing the bean names as
	 * keys and the corresponding bean instances as values
	 * @throws BeansException if a bean could not be created
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 */
	Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans)
	    throws BeansException;

}
