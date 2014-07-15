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
package org.jboss.jca.core.inflow.cra.inflow;

import org.jboss.jca.core.inflow.cra.ConversationalResourceAdapterImpl;
import org.jboss.jca.core.inflow.spi.conversation.Conversation;
import org.jboss.jca.core.inflow.spi.conversation.ConversationalMessageEndpoint;
import org.jboss.jca.core.inflow.spi.conversation.ConversationalMessageEndpointFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.resource.ResourceException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Conversational activation
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConversationalActivation
{
   /** onMessage */
   private static Method onMessage;

   /** The resource adapter */
   private ConversationalResourceAdapterImpl ra;

   /** The conversational message endpoint factory */
   private ConversationalMessageEndpointFactory endpointFactory;

   /** Activation spec */
   private ConversationalActivationSpec spec;

   static
   {
      try
      {
         onMessage = ConversationalMessageListener.class.getMethod("onMessage", Object.class);
      }
      catch (Exception e)
      {
         throw new RuntimeException();
      }
   }


   /**
    * Default constructor
    * @exception ResourceException Thrown if an error occurs
    */
   public ConversationalActivation() throws ResourceException
   {
      this(null, null, null);
   }

   /**
    * Constructor
    * @param ra ConversationalResourceAdapterImpl
    * @param endpointFactory ConversationalMessageEndpointFactory
    * @param spec ConversationalActivationSpec
    * @exception ResourceException Thrown if an error occurs
    */
   public ConversationalActivation(ConversationalResourceAdapterImpl ra, 
                                   ConversationalMessageEndpointFactory endpointFactory,
                                   ConversationalActivationSpec spec)
      throws ResourceException
   {
      this.ra = ra;
      this.endpointFactory = endpointFactory;
      this.spec = spec;
   }

   /**
    * Get activation spec class
    * @return Activation spec
    */
   public ConversationalActivationSpec getActivationSpec()
   {
      return spec;
   }

   /**
    * Get message endpoint factory
    * @return Message endpoint factory
    */
   public ConversationalMessageEndpointFactory getMessageEndpointFactory()
   {
      return endpointFactory;
   }

   /**
    * Start the activation
    * @throws ResourceException Thrown if an error occurs
    */
   public void start() throws ResourceException
   {
      // Simulate data from Enterprise Information System
      List<Data> data = new ArrayList<Data>();
      data.add(new Data("A", 1, null));
      data.add(new Data("B", 1, "Hello"));
      data.add(new Data("C", 1, "World"));
      data.add(new Data("D", 1, null));

      for (Data d : data)
      {
         if (spec.getStartState().equals(d.getType()))
         {
            Conversation c = endpointFactory.getConversation(d.getSession());

            if (c != null)
               throw new ResourceException();

            c = endpointFactory.beginConversation(d.getSession());
         }
         else if (spec.getEndpointTypeOne().equals(d.getType()))
         {
            Conversation c = endpointFactory.getConversation(d.getSession());

            if (c == null)
               throw new ResourceException();

            Map<Serializable, Serializable> properties = null;
            for (Map<Serializable, Serializable> m : c.getProperties(ConversationalMessageListener.class))
            {
               if (m.containsValue(spec.getEndpointTypeOne()))
                  properties = m;
            }

            if (properties == null)
               throw new ResourceException();

            ConversationalMessageEndpoint cme = c.createEndpoint(ConversationalMessageListener.class,
                                                                 properties, new NoopXAResource());
            try
            {
               cme.beforeDelivery(onMessage);
               ((ConversationalMessageListener)cme).onMessage(d.getPayload());
               cme.afterDelivery();
            }
            catch (Exception e)
            {
               throw new ResourceException(e);
            }
            finally
            {
               cme.release();
            }
         }
         else if (spec.getEndpointTypeTwo().equals(d.getType()))
         {
            Conversation c = endpointFactory.getConversation(d.getSession());

            if (c == null)
               throw new ResourceException();

            Map<Serializable, Serializable> properties = null;
            for (Map<Serializable, Serializable> m : c.getProperties(ConversationalMessageListener.class))
            {
               if (m.containsValue(spec.getEndpointTypeTwo()))
                  properties = m;
            }

            if (properties == null)
               throw new ResourceException();

            ConversationalMessageEndpoint cme = c.createEndpoint(ConversationalMessageListener.class,
                                                                 properties, new NoopXAResource());
            try
            {
               cme.beforeDelivery(onMessage);
               ((ConversationalMessageListener)cme).onMessage(d.getPayload());
               cme.afterDelivery();
            }
            catch (Exception e)
            {
               throw new ResourceException(e);
            }
            finally
            {
               cme.release();
            }
         }
         else if (spec.getEndState().equals(d.getType()))
         {
            Conversation c = endpointFactory.getConversation(d.getSession());

            if (c == null)
               throw new ResourceException();

            c.endConversation();
            ra.increaseConversations();
         }
         else if (spec.getCancelState().equals(d.getType()))
         {
            Conversation c = endpointFactory.getConversation(d.getSession());

            if (c == null)
               throw new ResourceException();

            c.cancelConversation();
            ra.increaseConversations();
         }
         else
         {
            throw new ResourceException();
         }
      }
   }

   /**
    * Stop the activation
    */
   public void stop()
   {

   }

   /**
    * Data
    */
   static class Data
   {
      private String type;
      private int session;
      private Object payload;

      /**
       * Constructor
       * @param type The type
       * @param session The session
       * @param payload The payload
       */
      Data(String type, int session, Object payload)
      {
         this.type = type;
         this.session = session;
         this.payload = payload;
      }

      /**
       * Get the type
       * @return The value
       */
      String getType()
      {
         return type;
      }

      /**
       * Get the session
       * @return The value
       */
      int getSession()
      {
         return session;
      }

      /**
       * Get the payload
       * @return The value
       */
      Object getPayload()
      {
         return payload;
      }
   }

   /**
    * Noop XAResource
    */
   static class NoopXAResource implements XAResource
   {
      /**
       * Constructor
       */
      NoopXAResource()
      {
      }

      /**
       * {@inheritDoc}
       */
      public void commit(Xid xid, boolean onePhase) throws XAException
      {
      }

      /**
       * {@inheritDoc}
       */
      public void end(Xid xid, int flags) throws XAException
      {
      }

      /**
       * {@inheritDoc}
       */
      public void forget(Xid xid) throws XAException
      {
      }

      /**
       * {@inheritDoc}
       */
      public int getTransactionTimeout() throws XAException
      {
         return 0;
      }

      /**
       * {@inheritDoc}
       */
      public boolean isSameRM(XAResource resource) throws XAException
      {
         return this == resource;
      }

      /**
       * {@inheritDoc}
       */
      public int prepare(Xid xid) throws XAException
      {
         return XAResource.XA_OK;
      }

      /**
       * {@inheritDoc}
       */
      public Xid[] recover(int flag) throws XAException
      {
         return new Xid[] {};
      }

      /**
       * {@inheritDoc}
       */
      public void rollback(Xid xid) throws XAException
      {
      }

      /**
       * {@inheritDoc}
       */
      public boolean setTransactionTimeout(int flag) throws XAException
      {
         return true;
      }

      /**
       * {@inheritDoc}
       */
      public void start(Xid xid, int flags) throws XAException
      {
      }
   }
}
