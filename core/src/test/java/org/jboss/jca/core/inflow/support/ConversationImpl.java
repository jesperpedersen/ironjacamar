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
import org.jboss.jca.core.inflow.spi.conversation.Conversation;
import org.jboss.jca.core.inflow.spi.conversation.ConversationalMessageEndpoint;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * A conversation
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConversationImpl implements Conversation
{
   private static Logger log = Logger.getLogger(ConversationImpl.class);
   private static Set<Class<?>> supportedEndpoints;
   private static Map<Class<?>, Set<Map<Serializable, Serializable>>> supportedProperties;

   private Serializable id;
   private TransactionManager tm;
   private ConversationalMessageEndpointFactoryImpl cmef;
   private Transaction tx;
   private Transaction suspendedTx;
   private Set<XAResource> enlisted;

   static
   {
      supportedEndpoints = new HashSet<Class<?>>();
      supportedProperties = new HashMap<Class<?>, Set<Map<Serializable, Serializable>>>();

      supportedEndpoints.add(ConversationalMessageListener.class);

      // Endpoint B
      Map<Serializable, Serializable> endpointB = new HashMap<Serializable, Serializable>();
      endpointB.put("Endpoint", "B");

      // Endpoint C
      Map<Serializable, Serializable> endpointC = new HashMap<Serializable, Serializable>();
      endpointC.put("Endpoint", "C");

      // Endpoints for ConversationalMessageListener
      Set<Map<Serializable, Serializable>> cmlEndpoints = new HashSet<Map<Serializable, Serializable>>();
      cmlEndpoints.add(endpointB);
      cmlEndpoints.add(endpointC);

      supportedProperties.put(ConversationalMessageListener.class, cmlEndpoints);
   }

   /**
    * Constructor
    * @param id The identifier
    * @param tm The transaction manager
    * @param cmef The factory
    * @exception ResourceException Thrown if an error occurs
    */
   public ConversationImpl(Serializable id, TransactionManager tm, ConversationalMessageEndpointFactoryImpl cmef)
      throws ResourceException
   {
      this.id = id;
      this.tm = tm;
      this.cmef = cmef;
      this.tx = null;
      this.suspendedTx = null;
      this.enlisted = new HashSet<XAResource>();

      try
      {
         if (tx == null && tm != null)
         {
            if (tm.getTransaction() != null)
            {
               suspendedTx = tm.suspend();
            }
         }
      }
      catch (Exception e)
      {
         throw new ResourceException(e);
      }

      log.infof("Conversation begin: %s", id);
   }

   /**
    * {@inheritDoc}
    */
   public ConversationalMessageEndpoint createEndpoint(Class<?> endpoint,
                                                       Map<Serializable, Serializable> properties,
                                                       XAResource xares)
      throws ResourceException
   {
      if (!supportedEndpoints.contains(endpoint))
         throw new ResourceException();

      Set<Map<Serializable, Serializable>> s = supportedProperties.get(endpoint);
      if (!s.contains(properties))
         throw new ResourceException();

      return new ConversationalMessageEndpointImpl(properties, xares, this);
   }

   /**
    * {@inheritDoc}
    */
   public Set<Map<Serializable, Serializable>> getProperties(Class<?> endpoint)
      throws ResourceException
   {
      return Collections.unmodifiableSet(supportedProperties.get(endpoint));
   }

   /**
    * {@inheritDoc}
    */
   public boolean isDeliveryTransacted(Class<?> endpoint,
                                       Map<Serializable, Serializable> properties,
                                       Method method)
      throws ResourceException
   {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public Set<Class<?>> getEndpointClasses()
   {
      Set<Class<?>> realEndpoints = new HashSet<Class<?>>();
      realEndpoints.add(ConversationalMessageEndpointImpl.class);

      return Collections.unmodifiableSet(realEndpoints);
   }

   /**
    * {@inheritDoc}
    */
   public void endConversation() throws ResourceException
   {
      cmef.removeConversation(id);

      try
      {
         if (tx != null)
         {
            for (XAResource xares : enlisted)
            {
               tx.delistResource(xares, XAResource.TMSUCCESS);
            }

            tm.commit();
            tx = null;

            if (suspendedTx != null)
               tm.resume(suspendedTx);
         }
      }
      catch (Exception e)
      {
         throw new ResourceException(e);
      }
      finally
      {
         log.infof("Conversation end: %s", id);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void cancelConversation() throws ResourceException
   {
      cmef.removeConversation(id);

      try
      {
         if (tx != null)
         {
            for (XAResource xares : enlisted)
            {
               tx.delistResource(xares, XAResource.TMFAIL);
            }

            tm.rollback();
            tx = null;

            if (suspendedTx != null)
               tm.resume(suspendedTx);
         }
      }
      catch (Exception e)
      {
         throw new ResourceException(e);
      }
      finally
      {
         log.infof("Conversation cancel: %s", id);
      }
   }

   /**
    * Enlist resource
    * @param xares The XAResource
    * @exception ResourceException Thrown if an error occurs
    */
   void enlistResource(XAResource xares) throws ResourceException
   {
      try
      {
         if (tx == null && xares != null)
         {
            tm.begin();
            tx = tm.getTransaction();
         }

         if (xares != null)
         {
            tx.enlistResource(xares);
            enlisted.add(xares);
         }
      }
      catch (Exception e)
      {
         throw new ResourceException(e);
      }
   }
}
