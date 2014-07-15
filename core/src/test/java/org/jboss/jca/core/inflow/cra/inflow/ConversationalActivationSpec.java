/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import org.jboss.logging.Logger;

/**
 * ConversationalActivationSpec
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConversationalActivationSpec implements ActivationSpec
{
   /** The logger */
   private static Logger log = Logger.getLogger(ConversationalActivationSpec.class);

   /** The resource adapter */
   private ResourceAdapter ra;

   /** The start state */
   private String startState;

   /** The end state */
   private String endState;

   /** The cancel state */
   private String cancelState;

   /** Message type 1 */
   private String endpointTypeOne;

   /** Message type 2 */
   private String endpointTypeTwo;

   /**
    * Constructor
    * @param startState startState
    * @param endState endState
    * @param cancelState cancelState
    * @param endpointTypeOne endpointTypeOne
    * @param endpointTypeTwo endpointTypeTwo
    */
   public ConversationalActivationSpec(String startState, String endState, String cancelState,
                                       String endpointTypeOne, String endpointTypeTwo)
   {
      this.startState = startState;
      this.endState = endState;
      this.cancelState = cancelState;
      this.endpointTypeOne = endpointTypeOne;
      this.endpointTypeTwo = endpointTypeTwo;
   }

   /**
    * Get the start state
    * @return The value
    */
   public String getStartState()
   {
      return startState;
   }

   /**
    * Get the end state
    * @return The value
    */
   public String getEndState()
   {
      return endState;
   }

   /**
    * Get the cancel state
    * @return The value
    */
   public String getCancelState()
   {
      return cancelState;
   }

   /**
    * Get the endpoint type one
    * @return The value
    */
   public String getEndpointTypeOne()
   {
      return endpointTypeOne;
   }

   /**
    * Get the message type two
    * @return The value
    */
   public String getEndpointTypeTwo()
   {
      return endpointTypeTwo;
   }

   /**
    * {@inheritDoc}
    */
   public void validate() throws InvalidPropertyException
   {
      log.trace("validate()");
   }

   /**
    * {@inheritDoc}
    */
   public ResourceAdapter getResourceAdapter()
   {
      log.trace("getResourceAdapter()");
      return ra;
   }

   /**
    * {@inheritDoc}
    */
   public void setResourceAdapter(ResourceAdapter ra)
   {
      log.trace("setResourceAdapter()");
      this.ra = ra;
   }
}
