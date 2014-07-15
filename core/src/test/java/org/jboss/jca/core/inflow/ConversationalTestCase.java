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

package org.jboss.jca.core.inflow;

import org.jboss.jca.core.inflow.cra.ConversationalResourceAdapterImpl;
import org.jboss.jca.core.inflow.cra.inflow.ConversationalActivationSpec;
import org.jboss.jca.core.inflow.support.ConversationalMessageEndpointFactoryImpl;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.deployers.fungal.RAActivator;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.util.Set;

import javax.resource.spi.ResourceAdapter;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor;
import org.jboss.shrinkwrap.descriptor.api.connector15.ResourceadapterType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test case for conversational inflow
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConversationalTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(ConversationalTestCase.class);

   // --------------------------------------------------------------------------------||
   // Resource adapter ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Create .rar
    * @return The resource adapter archive
    */
   public ResourceAdapterArchive createRar()
   {
      ConnectorDescriptor raXml = Descriptors.create(ConnectorDescriptor.class, "ra.xml")
         .version("1.5");
      ResourceadapterType rt = raXml.getOrCreateResourceadapter();

      rt.resourceadapterClass(ConversationalResourceAdapterImpl.class.getName());

      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "conversational.rar");
      
      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "conversational.jar");
      ja.addPackages(true, ConversationalResourceAdapterImpl.class.getPackage());
      
      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      Embedded embedded = EmbeddedFactory.create(true);

      ResourceAdapterArchive ra = createRar();

      try
      {
         embedded.startup();

         RAActivator activator = embedded.lookup("RAActivator", RAActivator.class);
         assertNotNull(activator);
         activator.setEnabled(false);

         embedded.deploy(ra);

         ResourceAdapterRepository raRepository = 
            embedded.lookup("ResourceAdapterRepository", ResourceAdapterRepository.class);
         assertNotNull(raRepository);

         TransactionManager tm =
            embedded.lookup("RealTransactionManager", TransactionManager.class);
         assertNotNull(tm);

         Set<String> ids = raRepository.getResourceAdapters();
         assertNotNull(ids);
         assertEquals(1, ids.size());

         String conversationalResourceAdapterId = ids.iterator().next();
         assertNotNull(conversationalResourceAdapterId);

         ResourceAdapter resourceAdapter = raRepository.getResourceAdapter(conversationalResourceAdapterId);
         assertNotNull(resourceAdapter);
         assertTrue(resourceAdapter instanceof ConversationalResourceAdapterImpl);

         ConversationalResourceAdapterImpl conversationalResourceAdapter =
            (ConversationalResourceAdapterImpl)resourceAdapter;

         ConversationalActivationSpec spec = new ConversationalActivationSpec("A", "D", "E", "B", "C");
         spec.setResourceAdapter(conversationalResourceAdapter);

         ConversationalMessageEndpointFactoryImpl cmef = new ConversationalMessageEndpointFactoryImpl(tm);
         
         conversationalResourceAdapter.conversationActivation(cmef, spec);
         conversationalResourceAdapter.conversationDeactivation(cmef, spec);

         assertEquals(1, conversationalResourceAdapter.getConversations());
      }
      finally
      {
         embedded.undeploy(ra);
         embedded.shutdown();
      }
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
   }
}
