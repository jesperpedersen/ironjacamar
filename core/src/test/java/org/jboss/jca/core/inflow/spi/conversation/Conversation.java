/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jca.core.inflow.spi.conversation;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.transaction.xa.XAResource;

/**
 * A conversation
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Conversation
{
   /**
    * Create an endpoint
    * @param endpoint The endpoint interface
    * @param properties Properties of the endpoint
    * @param xares The associated XAResource instance
    * @return The endpoint
    * @exception ResourceException Thrown if an error occurs
    */
   public ConversationalMessageEndpoint createEndpoint(Class<?> endpoint,
                                                       Map<Serializable, Serializable> properties,
                                                       XAResource xares)
      throws ResourceException;

   /**
    * Get the properties for each available endpoint
    * @param endpoint The endpoint interface
    * @return The properties
    * @exception ResourceException Thrown if an error occurs
    */
   public Set<Map<Serializable, Serializable>> getProperties(Class<?> endpoint)
      throws ResourceException;

   /**
    * Is an endpoint transacted
    * @param endpoint The endpoint interface
    * @param properties Properties of the endpoint
    * @param method The method
    * @return <code>True</code> if the method is transacted, otherwise <code>false</code>
    * @exception ResourceException Thrown if an error occurs
    */
   public boolean isDeliveryTransacted(Class<?> endpoint,
                                       Map<Serializable, Serializable> properties,
                                       Method method)
      throws ResourceException;

   /**
    * Get the available endpoint classes
    * @return The classes
    */
   public Set<Class<?>> getEndpointClasses();

   /**
    * End the conversation
    * @exception ResourceException Thrown if an error occurs
    */
   public void endConversation()
      throws ResourceException;

   /**
    * Cancel the conversation
    * @exception ResourceException Thrown if an error occurs
    */
   public void cancelConversation()
      throws ResourceException;
}
