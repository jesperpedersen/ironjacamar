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

import org.jboss.jca.core.inflow.spi.conversation.Conversation;
import org.jboss.jca.core.inflow.spi.conversation.ConversationalMessageEndpointFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;

/**
 * A conversational message endpoint factory
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConversationalMessageEndpointFactoryImpl implements ConversationalMessageEndpointFactory
{
   private static Logger log = Logger.getLogger(ConversationalMessageEndpointFactoryImpl.class);

   private Map<Serializable, Conversation> conversations;
   private TransactionManager tm;

   /**
    * Constructor
    * @param tm The transaction manager
    */
   public ConversationalMessageEndpointFactoryImpl(TransactionManager tm)
   {
      this.conversations = Collections.synchronizedMap(new HashMap<Serializable, Conversation>());
      this.tm = tm;
   }

   /**
    * {@inheritDoc}
    */
   public Conversation beginConversation(Serializable identifier) throws ResourceException
   {
      if (conversations.containsKey(identifier))
         throw new ResourceException();

      Conversation c = new ConversationImpl(identifier, tm, this);
      conversations.put(identifier, c);

      return c;
   }

   /**
    * {@inheritDoc}
    */
   public Conversation getConversation(Serializable identifier) throws ResourceException
   {
      return conversations.get(identifier);
   }

   /**
    * {@inheritDoc}
    */
   public Set<Serializable> activeConversations()
   {
      return Collections.unmodifiableSet(conversations.keySet());
   }

   /**
    * Remove a conversation
    * @param id The identifier
    */
   void removeConversation(Serializable id)
   {
      conversations.remove(id);
   }
}
