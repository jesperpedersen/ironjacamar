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
package org.jboss.jca.core.inflow.cra;

import org.jboss.jca.core.inflow.cra.inflow.ConversationalActivation;
import org.jboss.jca.core.inflow.cra.inflow.ConversationalActivationSpec;

import org.jboss.jca.core.inflow.spi.ConversationalResourceAdapter;
import org.jboss.jca.core.inflow.spi.conversation.ConversationalMessageEndpointFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * ConversationalResourceAdapter
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConversationalResourceAdapterImpl implements ConversationalResourceAdapter
{
   /** The logger */
   private static Logger log = Logger.getLogger(ConversationalResourceAdapterImpl.class);

   /** The activations by activation spec */
   private Map<ConversationalActivationSpec, ConversationalActivation> activations;

   /** Conversations */
   private int conversations;

   /**
    * Default constructor
    */
   public ConversationalResourceAdapterImpl()
   {
      this.activations =
         Collections.synchronizedMap(new HashMap<ConversationalActivationSpec, ConversationalActivation>());
      this.conversations = 0;
   }

   /**
    * {@inheritDoc}
    */
   public void endpointActivation(MessageEndpointFactory endpointFactory,
                                  ActivationSpec spec) throws ResourceException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void endpointDeactivation(MessageEndpointFactory endpointFactory,
                                    ActivationSpec spec)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void conversationActivation(ConversationalMessageEndpointFactory cmef, ActivationSpec as)
      throws ResourceException
   {
      ConversationalActivation activation =
         new ConversationalActivation(this, cmef, (ConversationalActivationSpec)as);
      activations.put((ConversationalActivationSpec)as, activation);
      activation.start();

      log.trace("conversationActivation()");
   }

   /**
    * {@inheritDoc}
    */
   public void conversationDeactivation(ConversationalMessageEndpointFactory cmef, ActivationSpec as)
   {
      ConversationalActivation activation = activations.remove((ConversationalActivationSpec)as);
      if (activation != null)
         activation.stop();

      log.trace("conversationDeactivation()");
   }

   /**
    * {@inheritDoc}
    */
   public void start(BootstrapContext ctx) throws ResourceAdapterInternalException
   {
      log.trace("start()");
   }

   /**
    * {@inheritDoc}
    */
   public void stop()
   {
      log.trace("stop()");
   }

   /**
    * {@inheritDoc}
    */
   public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException
   {
      log.trace("getXAResources()");
      return null;
   }

   /**
    * Increase conversations
    */
   public void increaseConversations()
   {
      conversations++;
   }

   /**
    * Get the number of completed conversations
    * @return The value
    */
   public int getConversations()
   {
      return conversations;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      int result = 17;
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object other)
   {
      if (other == null)
         return false;

      if (other == this)
         return true;

      if (!(other instanceof ConversationalResourceAdapterImpl))
         return false;

      ConversationalResourceAdapterImpl obj = (ConversationalResourceAdapterImpl)other;
      boolean result = true; 
      return result;
   }
}
