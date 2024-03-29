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

package org.springframework.orm.hibernate3.support;

import java.io.Serializable;
import java.util.Map;

import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.MergeEvent;
import org.hibernate.event.def.DefaultMergeEventListener;
import org.hibernate.persister.entity.EntityPersister;

/**
 * Extension of Hibernate's DefaultMergeEventListener, transferring the ids
 * of newly saved objects to the corresponding original objects (that are part
 * of the detached object graph passed into the <code>merge</code> method).
 *
 * <p>Transferring newly assigned ids to the original graph allows for continuing
 * to use the original object graph, despite merged copies being registered with
 * the current Hibernate Session. This is particularly useful for web applications
 * that might want to store an object graph and then render it in a web view,
 * with links that include the id of certain (potentially newly saved) objects.
 *
 * <p>The merge behavior given by this MergeEventListener is nearly identical
 * to TopLink's merge behavior. See PetClinic for an example, which relies on
 * ids being available for newly saved objects: the <code>HibernateClinic</code>
 * and <code>TopLinkClinic</code> DAO implementations both use straight merge
 * calls, with the Hibernate SessionFactory configuration specifying an
 * <code>IdTransferringMergeEventListener</code>.
 *
 * <p>Typically specified as entry for LocalSessionFactoryBean's "eventListeners"
 * map, with key "merge".
 *
 * <p><b>NOTE:</b> Due to incompatible changes in the Hibernate 3.1 event listener API
 * (according to the Hibernate 3.1 RC1 release that was available at the time of
 * this writing), this merge event listener will currently only work as-is with
 * Hibernate 3.0. Consider copying this implementation and adapting it to the changed
 * API if you want to run it against Hibernate 3.1.
 *
 * <p>Spring 2.0 is likely to update its default implementation of this class to
 * Hibernate 3.1, even if the remainder of Spring 2.0's Hibernate3 support will stay
 * compatible with Hibernate 3.0.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see org.springframework.orm.hibernate3.LocalSessionFactoryBean#setEventListeners(java.util.Map)
 */
public class IdTransferringMergeEventListener extends DefaultMergeEventListener {

	/**
	 * Hibernate 3.0 implementation of ID transferral.
	 * Comment this out and the below in for a Hibernate 3.1 version of this class.
	 */
	protected Object entityIsTransient(MergeEvent event, Map copyCache) {
		Object mergedCopy = super.entityIsTransient(event, copyCache);
		SessionImplementor session = event.getSession();
		EntityPersister persister = session.getEntityPersister(event.getEntityName(), event.getEntity());
		// Extract id from merged copy (which is currently registered with Session).
		Serializable id = persister.getIdentifier(mergedCopy, session.getEntityMode());
		// Set id on original object (which remains detached).
		persister.setIdentifier(event.getOriginal(), id, session.getEntityMode());
		return mergedCopy;
	}

	/**
	 * Hibernate 3.1 implementation of ID transferral.
	 * Comment this in and the above out for a Hibernate 3.1 version of this class.
	 */
	/*
	protected void entityIsTransient(MergeEvent event, Map copyCache) {
		super.entityIsTransient(event, copyCache);
		SessionImplementor session = event.getSession();
		EntityPersister persister = session.getEntityPersister(event.getEntityName(), event.getEntity());
		// Extract id from merged copy (which is currently registered with Session).
		Serializable id = persister.getIdentifier(event.getResult(), session.getEntityMode());
		// Set id on original object (which remains detached).
		persister.setIdentifier(event.getOriginal(), id, session.getEntityMode());
	}
	*/

}
