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

package org.jboss.jca.core.inflow.support;

import org.jboss.jca.core.inflow.cra.inflow.ConversationalMessageListener;
import org.jboss.jca.core.inflow.spi.conversation.ConversationalMessageEndpoint;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import javax.resource.ResourceException;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * A conversational message endpoint
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConversationalMessageEndpointImpl implements ConversationalMessageEndpoint, ConversationalMessageListener
{
   private static Logger log = Logger.getLogger(ConversationalMessageEndpointImpl.class);

   private Map<Serializable, Serializable> properties;
   private XAResource xares;
   private ConversationImpl conversation;
   private Object message;

   /**
    * Constructor
    * @param properties Properties for the endpoint
    * @param xares The XAResource
    * @param conversation The conversation
    */
   public ConversationalMessageEndpointImpl(Map<Serializable, Serializable> properties, XAResource xares,
                                            ConversationImpl conversation)
   {
      this.properties = properties;
      this.xares = xares;
      this.conversation = conversation;
      this.message = null;
   }

   /**
    * {@inheritDoc}
    */
   public void onMessage(Object message)
   {
      this.message = message;
      log.info(properties + " => " + message);
   }

   /**
    * Get the message
    * @return The value
    */
   public Object getMessage()
   {
      return message;
   }

   /**
    * {@inheritDoc}
    */
   public void beforeDelivery(Method method)
      throws ResourceException
   {
      conversation.enlistResource(xares);
   }

   /**
    * {@inheritDoc}
    */
   public void afterDelivery()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void release()
   {
   }
}
