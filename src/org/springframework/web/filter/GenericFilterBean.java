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

package org.springframework.web.filter;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResourceLoader;
import org.springframework.web.util.NestedServletException;

/**
 * Simple base implementation of <code>javax.servlet.Filter</code> that treats
 * its config parameters as bean properties. Unknown parameters are ignored.
 *
 * <p>A very handy superclass for any type of filter. Type conversion of
 * config parameters is automatic. It is also possible for subclasses to
 * specify required properties.
 *
 * <p>This filter leaves actual filtering to subclasses, which have to
 * implement the {@link javax.servlet.Filter#doFilter} method.
 *
 * <p>This filter superclass has no dependency on a Spring application context.
 * Filters usually don't load their own context but rather access beans from
 * the root application context, accessible via the ServletContext.
 *
 * @author Juergen Hoeller
 * @since 06.12.2003
 * @see #addRequiredProperty
 * @see #initFilterBean
 * @see #doFilter
 * @see org.springframework.web.context.support.WebApplicationContextUtils#getWebApplicationContext
 */
public abstract class GenericFilterBean implements
		Filter, BeanNameAware, ServletContextAware, InitializingBean, DisposableBean {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Set of required properties (Strings) that must be supplied as
	 * config parameters to this filter.
	 */
	private final Set requiredProperties = new HashSet();

	/* The FilterConfig of this filter */
	private FilterConfig filterConfig;


	private String beanName;

	private ServletContext servletContext;


	/**
	 * Stores the bean name as defined in the Spring bean factory.
	 * <p>Only relevant in case of initialization as bean, to have a name as
	 * fallback to the filter name usually provided by a FilterConfig instance.
	 * @see org.springframework.beans.factory.BeanNameAware
	 * @see #getFilterName()
	 */
	public final void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Stores the ServletContext that the bean factory runs in.
	 * <p>Only relevant in case of initialization as bean, to have a ServletContext
	 * as fallback to the context usually provided by a FilterConfig instance.
	 * @see org.springframework.web.context.ServletContextAware
	 * @see #getServletContext()
	 */
	public final void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * Calls the <code>initFilterBean()</code> method that might
	 * contain custom initialization of a subclass.
	 * <p>Only relevant in case of initialization as bean, where the
	 * standard <code>init(FilterConfig)</code> method won't be called.
	 * @see #initFilterBean()
	 * @see #init(javax.servlet.FilterConfig)
	 */
	public void afterPropertiesSet() throws ServletException {
		initFilterBean();
	}


	/**
	 * Subclasses can invoke this method to specify that this property
	 * (which must match a JavaBean property they expose) is mandatory,
	 * and must be supplied as a config parameter. This should be called
	 * from the constructor of a subclass.
	 * <p>This method is only relevant in case of traditional initialization
	 * driven by a FilterConfig instance.
	 * @param property name of the required property
	 */
	protected final void addRequiredProperty(String property) {
		this.requiredProperties.add(property);
	}

	/**
	 * Standard way of initializing this filter.
	 * Map config parameters onto bean properties of this filter, and
	 * invoke subclass initialization.
	 * @param filterConfig the configuration for this filter
	 * @throws ServletException if bean properties are invalid (or required
	 * properties are missing), or if subclass initialization fails.
	 * @see #initFilterBean
	 */
	public final void init(FilterConfig filterConfig) throws ServletException {
		Assert.notNull(filterConfig, "FilterConfig must not be null");
		if (logger.isInfoEnabled()) {
			logger.info("Initializing filter '" + filterConfig.getFilterName() + "'");
		}

		this.filterConfig = filterConfig;

		// Set bean properties from init parameters.
		try {
			PropertyValues pvs = new FilterConfigPropertyValues(filterConfig, this.requiredProperties);
			BeanWrapper bw = new BeanWrapperImpl(this);
			ResourceLoader resourceLoader = new ServletContextResourceLoader(filterConfig.getServletContext());
			bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader));
			initBeanWrapper(bw);
			bw.setPropertyValues(pvs, true);
		}
		catch (BeansException ex) {
			String msg = "Failed to set bean properties on filter '" +
			    filterConfig.getFilterName() + "': " + ex.getMessage();
			logger.error(msg, ex);
			throw new NestedServletException(msg, ex);
		}

		// Let subclasses do whatever initialization they like.
		initFilterBean();

		if (logger.isInfoEnabled()) {
			logger.info("Filter '" + filterConfig.getFilterName() + "' configured successfully");
		}
	}

	/**
	 * Alternative way of initializing this filter.
	 * Used by Servlet Filter version that shipped with WebLogic 6.1.
	 * @param filterConfig the configuration for this filter
	 * @throws BeanInitializationException wrapping a ServletException
	 * thrown by the <code>init</code> method
	 * @see #init(javax.servlet.FilterConfig)
	 */
	public final void setFilterConfig(FilterConfig filterConfig) {
		try {
			init(filterConfig);
		}
		catch (ServletException ex) {
			throw new BeanInitializationException("Could not initialize filter bean", ex);
		}
	}

	/**
	 * Initialize the BeanWrapper for this GenericFilterBean,
	 * possibly with custom editors.
	 * <p>This default implementation is empty.
	 * @param bw the BeanWrapper to initialize
	 * @throws BeansException if thrown by BeanWrapper methods
	 * @see org.springframework.beans.BeanWrapper#registerCustomEditor
	 */
	protected void initBeanWrapper(BeanWrapper bw) throws BeansException {
	}


	/**
	 * Make the FilterConfig of this filter available, if any.
	 * Analogous to GenericServlet's <code>getServletConfig()</code>.
	 * <p>Public to resemble the <code>getFilterConfig()</code> method
	 * of the Servlet Filter version that shipped with WebLogic 6.1.
	 * @return the FilterConfig instance, or <code>null</code> if none available
	 * @see javax.servlet.GenericServlet#getServletConfig()
	 */
	public final FilterConfig getFilterConfig() {
		return this.filterConfig;
	}

	/**
	 * Make the name of this filter available to subclasses.
	 * Analogous to GenericServlet's <code>getServletName()</code>.
	 * <p>Takes the FilterConfig's filter name by default.
	 * If initialized as bean in a Spring application context,
	 * it falls back to the bean name as defined in the bean factory.
	 * @return the filter name, or <code>null</code> if none available
	 * @see javax.servlet.GenericServlet#getServletName()
	 * @see javax.servlet.FilterConfig#getFilterName()
	 * @see #setBeanName
	 */
	protected final String getFilterName() {
		return (this.filterConfig != null ? this.filterConfig.getFilterName() : this.beanName);
	}

	/**
	 * Make the ServletContext of this filter available to subclasses.
	 * Analogous to GenericServlet's <code>getServletContext()</code>.
	 * <p>Takes the FilterConfig's ServletContext by default.
	 * If initialized as bean in a Spring application context,
	 * it falls back to the ServletContext that the bean factory runs in.
	 * @return the ServletContext instance, or <code>null</code> if none available
	 * @see javax.servlet.GenericServlet#getServletContext()
	 * @see javax.servlet.FilterConfig#getServletContext()
	 * @see #setServletContext
	 */
	protected final ServletContext getServletContext() {
		return (this.filterConfig != null ? this.filterConfig.getServletContext() : this.servletContext);
	}


	/**
	 * Subclasses may override this to perform custom initialization.
	 * All bean properties of this filter will have been set before this
	 * method is invoked.
	 * <p>Note: This method will be called from standard filter initialization
	 * as well as filter bean initialization in a Spring application context.
	 * Filter name and ServletContext will be available in both cases.
	 * <p>This default implementation is empty.
	 * @throws ServletException if subclass initialization fails
	 * @see #getFilterName()
	 * @see #getServletContext()
	 */
	protected void initFilterBean() throws ServletException {
	}

	/**
	 * Subclasses may override this to perform custom filter shutdown.
	 * <p>Note: This method will be called from standard filter destruction
	 * as well as filter bean destruction in a Spring application context.
	 * <p>This default implementation is empty.
	 */
	public void destroy() {
	}


	/**
	 * PropertyValues implementation created from FilterConfig init parameters.
	 */
	private static class FilterConfigPropertyValues extends MutablePropertyValues {

		/**
		 * Create new FilterConfigPropertyValues.
		 * @param config FilterConfig we'll use to take PropertyValues from
		 * @param requiredProperties set of property names we need, where
		 * we can't accept default values
		 * @throws ServletException if any required properties are missing
		 */
		public FilterConfigPropertyValues(FilterConfig config, Set requiredProperties)
		    throws ServletException {

			Set missingProps = (requiredProperties != null && !requiredProperties.isEmpty()) ?
					new HashSet(requiredProperties) : null;

			Enumeration en = config.getInitParameterNames();
			while (en.hasMoreElements()) {
				String property = (String) en.nextElement();
				Object value = config.getInitParameter(property);
				addPropertyValue(new PropertyValue(property, value));
				if (missingProps != null) {
					missingProps.remove(property);
				}
			}

			// Fail if we are still missing properties.
			if (missingProps != null && missingProps.size() > 0) {
				throw new ServletException(
				    "Initialization from FilterConfig for filter '" + config.getFilterName() +
				    "' failed; the following required properties were missing: " +
				    StringUtils.collectionToDelimitedString(missingProps, ", "));
			}
		}
	}

}
