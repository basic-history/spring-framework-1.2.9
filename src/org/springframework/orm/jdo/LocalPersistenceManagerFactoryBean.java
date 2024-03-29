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

package org.springframework.orm.jdo;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;

import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * FactoryBean that creates a local JDO EntityManagerFactory instance.
 * Behaves like a PersistenceManagerFactory instance when used as bean
 * reference, e.g. for JdoTemplate's "persistenceManagerFactory" property.
 * Note that switching to a JndiObjectFactoryBean or a bean-style
 * PersistenceManagerFactory instance is just a matter of configuration!
 *
 * <p>The typical usage will be to register this as singleton factory
 * (for a certain underlying data source) in an application context,
 * and give bean references to application services that need it.
 *
 * <p>Configuration settings can either be read from a properties file,
 * specified as "configLocation", or completely via this class. Properties
 * specified as "jdoProperties" here will override any settings in a file.
 *
 * <p>This PersistenceManager handling strategy is most appropriate for
 * applications that solely use JDO for data access. In this case,
 * JdoTransactionManager is much more convenient than setting up your
 * JDO provider for JTA transactions (which might involve JCA).
 *
 * <p><b>NOTE:</b> This class is compatible with both JDO 1.0 and JDO 2.0,
 * as far as possible. It uses reflection to adapt to the actual API present
 * on the class path (concretely: for the <code>getPersistenceManagerFactory</code>
 * method with either a <code>Properties</code> or a <code>Map</code> argument).
 * Make sure that the JDO API jar on your class path matches the one that
 * your JDO provider has been compiled against!
 *
 * <p>As alternative to the properties-driven approach that this FactoryBean offers
 * (which is analogous to using the standard JDOHelper class with a Properties
 * object that is populated with standard JDO properties), you can set up an
 * instance of your PersistenceManagerFactory implementation class directly.
 *
 * <p>Like a DataSource, a PersistenceManagerFactory is encouraged to
 * support bean-style configuration, which makes it very easy to set up as
 * Spring-managed bean. The implementation class becomes the bean class;
 * the remaining properties are applied as bean properties (starting with
 * lower-case characters, in contrast to the corresponding JDO properties).
 *
 * <p>For example, in case of <a href="http://www.jpox.org">JPOX</a>:
 *
 * <p><pre>
 * &lt;bean id="persistenceManagerFactory" class="org.jpox.PersistenceManagerFactoryImpl" destroy-method="close"&gt;
 *   &lt;property name="connectionFactory" ref="dataSource"/&gt;
 *   &lt;property name="nontransactionalRead" value="true"/&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <p>Note that such direct setup of a PersistenceManagerFactory implementation
 * is the only way to pass an external connection factory (i.e. a JDBC DataSource)
 * into a JDO PersistenceManagerFactory. With the standard properties-driven approach,
 * you can only use an internal connection pool or a JNDI DataSource.
 *
 * <p>The <code>close()</code> method is standardized as of JDO 1.0.1; don't forget to
 * specify it as "destroy-method" for any PersistenceManagerFactory instance.
 * Note that this FactoryBean will automatically invoke <code>close</code> for
 * the PersistenceManagerFactory it creates, without any special configuration.
 *
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see JdoTemplate#setPersistenceManagerFactory
 * @see JdoTransactionManager#setPersistenceManagerFactory
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see javax.jdo.JDOHelper#getPersistenceManagerFactory
 * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory
 * @see javax.jdo.PersistenceManagerFactory#close()
 */
public class LocalPersistenceManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

		// Determine whether the JDO 1.0 getPersistenceManagerFactory(Properties) method
		// is available, for use in the "newPersistenceManagerFactory" implementation.
	private final static Method getPersistenceManagerFactoryMethod = ClassUtils.getMethodIfAvailable(
			JDOHelper.class, "getPersistenceManagerFactory", new Class[] {Properties.class});


	protected final Log logger = LogFactory.getLog(getClass());

	private Resource configLocation;

	private Properties jdoProperties;

	private PersistenceManagerFactory persistenceManagerFactory;


	/**
	 * Set the location of the JDO properties config file, for example
	 * as classpath resource "classpath:kodo.properties".
	 * <p>Note: Can be omitted when all necessary properties are
	 * specified locally via this bean.
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set JDO properties, such as"javax.jdo.PersistenceManagerFactoryClass".
	 * <p>Can be used to override values in a JDO properties config file,
	 * or to specify all necessary properties locally.
	 */
	public void setJdoProperties(Properties jdoProperties) {
		this.jdoProperties = jdoProperties;
	}


	/**
	 * Initialize the PersistenceManagerFactory for the given location.
	 * @throws IllegalArgumentException in case of illegal property values
	 * @throws IOException if the properties could not be loaded from the given location
	 * @throws JDOException in case of JDO initialization errors
	 */
	public void afterPropertiesSet() throws IllegalArgumentException, IOException, JDOException {
		if (this.configLocation == null && this.jdoProperties == null) {
			throw new IllegalArgumentException("Either configLocation or jdoProperties must be set");
		}

		Properties props = new Properties();

		if (this.configLocation != null) {
			// Load JDO properties from the given location.
			InputStream is = this.configLocation.getInputStream();
			try {
				props.load(is);
			}
			finally {
				is.close();
			}
		}

		if (this.jdoProperties != null) {
			// Use propertyNames enumeration to also catch default properties.
			for (Enumeration en = this.jdoProperties.propertyNames(); en.hasMoreElements();) {
				String key = (String) en.nextElement();
				props.setProperty(key, this.jdoProperties.getProperty(key));
			}
		}

		// Build PersistenceManagerFactory instance.
		logger.info("Building new JDO PersistenceManagerFactory");
		this.persistenceManagerFactory = newPersistenceManagerFactory(props);
	}

	/**
	 * Subclasses can override this to perform custom initialization of the
	 * PersistenceManagerFactory instance, creating it via the given Properties
	 * that got prepared by this LocalPersistenceManagerFactoryBean.
	 * <p>The default implementation invokes JDOHelper's
	 * <code>getPersistenceManagerFactory</code> method.
	 * A custom implementation could prepare the instance in a specific way,
	 * or use a custom PersistenceManagerFactory implementation.
	 * <p>Implemented to work with either the JDO 1.0
	 * <code>getPersistenceManagerFactory(java.util.Properties)</code> method or
	 * the JDO 2.0 <code>getPersistenceManagerFactory(java.util.Map)</code> method,
	 * detected through reflection.
	 * @param props the merged Properties prepared by this LocalPersistenceManagerFactoryBean
	 * @return the PersistenceManagerFactory instance
	 * @see javax.jdo.JDOHelper#getPersistenceManagerFactory(java.util.Properties)
	 * @see javax.jdo.JDOHelper#getPersistenceManagerFactory(java.util.Map)
	 */
	protected PersistenceManagerFactory newPersistenceManagerFactory(Properties props) {
		// Use JDO 1.0 getPersistenceManagerFactory(Properties) method, if available.
		if (getPersistenceManagerFactoryMethod != null) {
			return (PersistenceManagerFactory) ReflectionUtils.invokeMethod(
					getPersistenceManagerFactoryMethod, null, new Object[] {props});
		}
		// Use JDO 2.0 getPersistenceManagerFactory(Map) method else.
		return JDOHelper.getPersistenceManagerFactory(props);
	}


	/**
	 * Return the singleton PersistenceManagerFactory.
	 */
	public Object getObject() {
		return this.persistenceManagerFactory;
	}

	public Class getObjectType() {
		return (this.persistenceManagerFactory != null) ?
		    this.persistenceManagerFactory.getClass() : PersistenceManagerFactory.class;
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Close the PersistenceManagerFactory on bean factory shutdown.
	 */
	public void destroy() {
		logger.info("Closing JDO PersistenceManagerFactory");
		this.persistenceManagerFactory.close();
	}

}
