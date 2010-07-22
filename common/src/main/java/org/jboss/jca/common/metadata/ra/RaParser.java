/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.metadata.ra;

import org.jboss.jca.common.metadata.MetadataParser;
import org.jboss.jca.common.metadata.ParserException;
import org.jboss.jca.common.metadata.ra.common.Adminobject;
import org.jboss.jca.common.metadata.ra.common.AuthenticationMechanism;
import org.jboss.jca.common.metadata.ra.common.ConfigProperty;
import org.jboss.jca.common.metadata.ra.common.ConnectionDefinition;
import org.jboss.jca.common.metadata.ra.common.Connector;
import org.jboss.jca.common.metadata.ra.common.CredentialInterfaceEnum;
import org.jboss.jca.common.metadata.ra.common.InboundResourceAdapter;
import org.jboss.jca.common.metadata.ra.common.LicenseType;
import org.jboss.jca.common.metadata.ra.common.LocalizedXsdString;
import org.jboss.jca.common.metadata.ra.common.MessageAdapter;
import org.jboss.jca.common.metadata.ra.common.MessageListener;
import org.jboss.jca.common.metadata.ra.common.OutboundResourceAdapter;
import org.jboss.jca.common.metadata.ra.common.ResourceAdapter1516;
import org.jboss.jca.common.metadata.ra.common.SecurityPermission;
import org.jboss.jca.common.metadata.ra.common.TransactionSupportEnum;
import org.jboss.jca.common.metadata.ra.common.XsdString;
import org.jboss.jca.common.metadata.ra.ra10.Connector10;
import org.jboss.jca.common.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.metadata.ra.ra15.Activationspec15;
import org.jboss.jca.common.metadata.ra.ra15.Connector15;
import org.jboss.jca.common.metadata.ra.ra16.Activationspec16;
import org.jboss.jca.common.metadata.ra.ra16.ConfigProperty16;
import org.jboss.jca.common.metadata.ra.ra16.Connector16;
import org.jboss.jca.common.metadata.ra.ra16.Icon;
import org.jboss.jca.common.metadata.ra.ra16.Path;
import org.jboss.jca.common.metadata.ra.ra16.RequiredConfigProperty;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 *
 * A RaParser.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class RaParser implements MetadataParser<Connector>
{

   @Override
   public Connector parse(InputStream xmlInputStream) throws Exception
   {
      XMLStreamReader reader = null;
      Connector connector = null;

      try
      {
         XMLInputFactory inputFactory = XMLInputFactory.newInstance();
         reader = inputFactory.createXMLStreamReader(xmlInputStream);

         //iterate over tags
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               // should mean we're done, so ignore it.
               break;
            }
            case START_ELEMENT : {
               if (Connector16.VERSION.equals(reader.getAttributeValue(null, "version")))
               {
                  switch (Tag.forName(reader.getLocalName()))
                  {
                     case CONNECTOR : {
                        connector = parseConnector16(reader);
                        break;
                     }
                     default :
                        throw new ParserException("Unexpected element:" + reader.getLocalName());
                  }

               }
               else if (Connector15.VERSION.equals(reader.getAttributeValue(null, "version")))
               {
                  switch (Tag.forName(reader.getLocalName()))
                  {
                     case CONNECTOR : {
                        connector = parseConnector15(reader);
                        break;
                     }
                     default :
                        throw new ParserException("Unexpected element:" + reader.getLocalName());
                  }
               }
               else
               {
                  switch (Tag.forName(reader.getLocalName()))
                  {
                     case CONNECTOR : {
                        connector = parseConnector10(reader);
                        break;
                     }
                     default :
                        throw new ParserException("Unexpected element:" + reader.getLocalName());
                  }
               }

               break;
            }
            default :
               throw new IllegalStateException();
         }
      }
      catch (XMLStreamException e)
      {
         //ignore it. It is just saying that it isn't a tag
      }
      finally
      {
         if (reader != null)
            reader.close();
      }
      return connector;

   }

   private Connector parseConnector10(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      LicenseType license = null;
      String id = null;
      ArrayList<Icon> icon = new ArrayList<Icon>();
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();
      XsdString eisType = null;
      ResourceAdapter10 resourceadapter = null;
      XsdString vendorName = null;
      String moduleName = null;
      XsdString displayName = null;
      XsdString resourceadapterVersion = null;
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.CONNECTOR)
               {

                  //trimming collections
                  icon.trimToSize();
                  description.trimToSize();

                  //building and returning object
                  return new Connector10(moduleName, description, displayName, icon, vendorName, eisType,
                        resourceadapterVersion, license, resourceadapter, id);

               }
               else
               {
                  if (Connector10.Tag.forName(reader.getLocalName()) == Connector10.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.CONNECTOR)
               {
                  //getting attributes
                  id = reader.getAttributeValue(null, Connector16.Attribute.ID.getLocalName());
               }
               switch (Connector10.Tag.forName(reader.getLocalName()))
               {
                  case MODULENAME : {
                     moduleName = reader.getElementText();
                     break;
                  }
                  case VENDORNAME : {
                     vendorName = elementAsXsdString(reader);
                     break;
                  }
                  case EIS_TYPE : {
                     eisType = elementAsXsdString(reader);
                     break;
                  }
                  case LICENSE : {
                     license = parseLicense(reader);
                     break;
                  }
                  case RESOURCEADPTER_VERSION : {
                     resourceadapterVersion = elementAsXsdString(reader);
                  }
                     break;
                  case RESOURCEADAPTER : {
                     resourceadapter = parseResourceAdapter10(reader);
                     break;
                  }
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case DISPLAY_NAME : {
                     displayName = elementAsLocalizedXsdString(reader);
                     break;
                  }
                  case ICON : {
                     icon.add(parseIcon(reader));
                     break;
                  }
                  case SPEC_VERSION :
                  case VERSION : {
                     //ignore
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private Connector parseConnector15(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      LicenseType license = null;
      String id = null;
      XsdString eisType = null;
      ResourceAdapter1516 resourceadapter = null;
      XsdString vendorName = null;
      XsdString resourceadapterVersion = null;
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.CONNECTOR)
               {

                  //building and returning object
                  return new Connector15(vendorName, eisType, resourceadapterVersion, license, resourceadapter, id);

               }
               else
               {
                  if (Connector15.Tag.forName(reader.getLocalName()) == Connector15.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.CONNECTOR)
               {
                  //getting attributes
                  id = reader.getAttributeValue(null, Connector16.Attribute.ID.getLocalName());
               }
               switch (Connector15.Tag.forName(reader.getLocalName()))
               {
                  case VENDORNAME : {
                     vendorName = elementAsXsdString(reader);
                     break;
                  }
                  case EIS_TYPE : {
                     eisType = elementAsXsdString(reader);
                     break;
                  }
                  case LICENSE : {
                     license = parseLicense(reader);
                     break;
                  }
                  case RESOURCEADPTER_VERSION : {
                     resourceadapterVersion = elementAsXsdString(reader);
                  }
                     break;
                  case RESOURCEADAPTER : {
                     resourceadapter = parseResourceAdapter(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private Connector parseConnector16(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Boolean metadataComplete = null;
      LicenseType license = null;
      String id = null;
      ArrayList<Icon> icon = new ArrayList<Icon>();
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();
      XsdString eisType = null;
      ResourceAdapter1516 resourceadapter = null;
      XsdString vendorName = null;
      String moduleName = null;
      ArrayList<LocalizedXsdString> displayName = new ArrayList<LocalizedXsdString>();
      ArrayList<String> requiredWorkContext = new ArrayList<String>();
      XsdString resourceadapterVersion = null;
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.CONNECTOR)
               {

                  //trimming collections
                  icon.trimToSize();
                  description.trimToSize();
                  displayName.trimToSize();
                  requiredWorkContext.trimToSize();

                  //building and returning object
                  return new Connector16(moduleName, description, displayName, icon, vendorName, eisType,
                        resourceadapterVersion, license, resourceadapter, requiredWorkContext, metadataComplete, id);

               }
               else
               {
                  if (Connector16.Tag.forName(reader.getLocalName()) == Connector16.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.CONNECTOR)
               {
                  //getting attributes
                  metadataComplete = Boolean.valueOf(reader.getAttributeValue(null,
                        Connector16.Attribute.METADATA_COMPLETE.getLocalName()));
                  id = reader.getAttributeValue(null, Connector16.Attribute.ID.getLocalName());
               }
               switch (Connector16.Tag.forName(reader.getLocalName()))
               {
                  case MODULENAME : {
                     moduleName = reader.getElementText();
                     break;
                  }
                  case VENDORNAME : {
                     vendorName = elementAsXsdString(reader);
                     break;
                  }
                  case EIS_TYPE : {
                     eisType = elementAsXsdString(reader);
                     break;
                  }
                  case LICENSE : {
                     license = parseLicense(reader);
                     break;
                  }
                  case RESOURCEADPTER_VERSION : {
                     resourceadapterVersion = elementAsXsdString(reader);
                  }
                     break;
                  case RESOURCEADAPTER : {
                     resourceadapter = parseResourceAdapter(reader);
                     break;
                  }
                  case REQUIRED_WORK_CONTEXT : {
                     requiredWorkContext.add(reader.getElementText());
                     break;
                  }
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case DISPLAY_NAME : {
                     displayName.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case ICON : {
                     icon.add(parseIcon(reader));
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private Icon parseIcon(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Path largeIcon = null;
      Path smallIcon = null;

      //getting attributes
      String id = reader.getAttributeValue(null, Icon.Attribute.ID.getLocalName());
      String lang = reader.getAttributeValue(null, Icon.Attribute.ID.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Connector16.Tag.forName(reader.getLocalName()) == Connector16.Tag.ICON)
               {
                  //building and returning object

                  return new Icon(smallIcon, largeIcon, lang, id);

               }
               else
               {
                  if (Icon.Tag.forName(reader.getLocalName()) == Icon.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Icon.Tag.forName(reader.getLocalName()))
               {
                  case SMALL_ICON : {
                     smallIcon = Path.valueOf(reader.getElementText());
                     break;
                  }
                  case LARGE_ICON : {
                     largeIcon = Path.valueOf(reader.getElementText());
                     break;
                  }

                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private ResourceAdapter1516 parseResourceAdapter(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<ConfigProperty> configProperty = new ArrayList<ConfigProperty>();
      String resourceadapterClass = null;
      OutboundResourceAdapter outboundResourceadapter = null;
      ArrayList<SecurityPermission> securityPermission = new ArrayList<SecurityPermission>();
      InboundResourceAdapter inboundResourceadapter = null;
      ArrayList<Adminobject> adminobject = new ArrayList<Adminobject>();
      String id = reader.getAttributeValue(null, ResourceAdapter1516.Attribute.ID.getLocalName());
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Connector16.Tag.forName(reader.getLocalName()) == Connector16.Tag.RESOURCEADAPTER)
               {
                  //trimming collections
                  configProperty.trimToSize();
                  securityPermission.trimToSize();
                  adminobject.trimToSize();

                  //building and returning object
                  return new ResourceAdapter1516(resourceadapterClass, configProperty, outboundResourceadapter,
                        inboundResourceadapter, adminobject, securityPermission, id);

               }
               else
               {
                  if (ResourceAdapter1516.Tag.forName(reader.getLocalName()) == ResourceAdapter1516.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {

               switch (ResourceAdapter1516.Tag.forName(reader.getLocalName()))
               {
                  case RESOURCEADAPTER_CLASS : {
                     resourceadapterClass = reader.getElementText();
                     break;
                  }
                  case CONFIG_PROPERTY : {
                     configProperty.add(parseConfigProperty(reader));
                     break;
                  }

                  case OUTBOUND_RESOURCEADAPTER : {
                     outboundResourceadapter = parseOutboundResourceadapter(reader);
                     break;
                  }
                  case INBOUND_RESOURCEADAPTER : {
                     inboundResourceadapter = parseInboundResourceadapter(reader);
                     break;
                  }
                  case ADMINOBJECT : {
                     adminobject.add(parseAdminObject(reader));
                     break;
                  }
                  case SECURITY_PERMISSION : {
                     securityPermission.add(parseSecurityPermission(reader));
                     break;
                  }

                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private ResourceAdapter10 parseResourceAdapter10(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      XsdString managedConnectionFactoryClass = null;
      XsdString connectionFactoryInterface = null;
      XsdString connectionFactoryImplClass = null;
      XsdString connectionInterface = null;
      XsdString connectionImplClass = null;
      TransactionSupportEnum transactionSupport = null;
      ArrayList<AuthenticationMechanism> authenticationMechanism = new ArrayList<AuthenticationMechanism>();
      ArrayList<ConfigProperty> configProperties = new ArrayList<ConfigProperty>();
      Boolean reauthenticationSupport = null;
      ArrayList<SecurityPermission> securityPermission = new ArrayList<SecurityPermission>();
      String id = reader.getAttributeValue(null, ResourceAdapter1516.Attribute.ID.getLocalName());
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Connector10.Tag.forName(reader.getLocalName()) == Connector10.Tag.RESOURCEADAPTER)
               {
                  //trimming collections
                  authenticationMechanism.trimToSize();
                  configProperties.trimToSize();
                  securityPermission.trimToSize();

                  //building and returning object
                  return new ResourceAdapter10(managedConnectionFactoryClass, connectionFactoryInterface,
                        connectionFactoryImplClass, connectionInterface, connectionImplClass, transactionSupport,
                        authenticationMechanism, configProperties, reauthenticationSupport, securityPermission, id);

               }
               else
               {
                  if (ResourceAdapter10.Tag.forName(reader.getLocalName()) == ResourceAdapter10.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {

               switch (ResourceAdapter10.Tag.forName(reader.getLocalName()))
               {
                  case CONFIG_PROPERTY : {
                     configProperties.add(parseConfigProperty(reader));
                     break;
                  }
                  case AUTHENTICATION_MECHANISM : {
                     authenticationMechanism.add(parseAuthenticationMechanism(reader));
                     break;
                  }
                  case MANAGED_CONNECTIONFACTORY_CLASS : {
                     managedConnectionFactoryClass = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTION_INTERFACE : {
                     connectionInterface = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTION_IMPL_CLASS : {
                     connectionImplClass = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTIONFACTORY_INTERFACE : {
                     connectionFactoryInterface = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTIONFACTORY_IMPL_CLASS : {
                     connectionFactoryImplClass = elementAsXsdString(reader);
                     break;
                  }
                  case REAUTHENTICATION_SUPPORT : {
                     reauthenticationSupport = elementAsBoolean(reader);
                     break;
                  }
                  case SECURITY_PERMISSION : {
                     securityPermission.add(parseSecurityPermission(reader));
                     break;
                  }
                  case TRANSACTION_SUPPORT : {
                     transactionSupport = TransactionSupportEnum.valueOf(reader.getElementText());
                     break;
                  }

                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private InboundResourceAdapter parseInboundResourceadapter(XMLStreamReader reader) throws XMLStreamException,
      ParserException
   {
      MessageAdapter messageadapter = null;
      String id = reader.getAttributeValue(null, InboundResourceAdapter.Attribute.ID.getLocalName());

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapter1516.Tag.
                     forName(reader.getLocalName()) == ResourceAdapter1516.Tag.INBOUND_RESOURCEADAPTER)
               {

                  //building and returning object
                  return new InboundResourceAdapter(messageadapter, id);

               }
               else
               {
                  if (InboundResourceAdapter.Tag.forName(reader.getLocalName()) == InboundResourceAdapter.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (InboundResourceAdapter.Tag.forName(reader.getLocalName()))
               {
                  case MESSAGEADAPTER : {
                     messageadapter = parseMessageAdapter(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private MessageAdapter parseMessageAdapter(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<MessageListener> messagelistener = new ArrayList<MessageListener>();
      String id = reader.getAttributeValue(null, MessageAdapter.Attribute.ID.getLocalName());

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (InboundResourceAdapter.Tag.
                     forName(reader.getLocalName()) == InboundResourceAdapter.Tag.MESSAGEADAPTER)
               {
                  //trimming collections
                  messagelistener.trimToSize();

                  //building and returning object
                  return new MessageAdapter(messagelistener, id);

               }
               else
               {
                  if (MessageAdapter.Tag.forName(reader.getLocalName()) == MessageAdapter.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (MessageAdapter.Tag.forName(reader.getLocalName()))
               {
                  case MESSAGELISTENER : {
                     messagelistener.add(parseMessageListener(reader));
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private MessageListener parseMessageListener(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Activationspec15 activationspec = null;
      XsdString messagelistenerType = null;
      //getting attributes
      String id = reader.getAttributeValue(null, MessageListener.Attribute.ID.getLocalName());

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (MessageAdapter.Tag.forName(reader.getLocalName()) == MessageAdapter.Tag.MESSAGELISTENER)
               {

                  //trimming collections

                  //building and returning object
                  return new MessageListener(messagelistenerType, activationspec, id);

               }
               else
               {
                  if (MessageListener.Tag.forName(reader.getLocalName()) == MessageListener.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (MessageListener.Tag.forName(reader.getLocalName()))
               {
                  case MESSAGELISTENER_TYPE : {
                     messagelistenerType = elementAsXsdString(reader);
                     break;
                  }
                  case ACTIVATIONSPEC : {
                     activationspec = parseActivationspec(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private Activationspec15 parseActivationspec(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<RequiredConfigProperty> requiredConfigProperty = new ArrayList<RequiredConfigProperty>();
      XsdString activationspecClass = null;
      ArrayList<ConfigProperty> configProperty = new ArrayList<ConfigProperty>();
      //getting attributes
      String id = reader.getAttributeValue(null, Activationspec16.Attribute.ID.getLocalName());

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (MessageListener.Tag.forName(reader.getLocalName()) == MessageListener.Tag.ACTIVATIONSPEC)
               {

                  //trimming collections
                  requiredConfigProperty.trimToSize();
                  configProperty.trimToSize();

                  //building and returning object
                  if (configProperty.size() != 0)
                  {
                     return new Activationspec16(activationspecClass, requiredConfigProperty, configProperty, id);
                  }
                  else
                  {
                     return new Activationspec15(activationspecClass, requiredConfigProperty, id);
                  }

               }
               else
               {
                  if (Activationspec16.Tag.forName(reader.getLocalName()) == Activationspec16.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Activationspec16.Tag.forName(reader.getLocalName()))
               {
                  case ACTIVATIONSPEC_CLASS : {
                     activationspecClass = elementAsXsdString(reader);
                     break;
                  }
                  case REQUIRED_CONFIG_PROPERTY : {
                     requiredConfigProperty.add(parseRequiredConfigProperty(reader));
                     break;
                  }
                  case CONFIG_PROPERTY : {
                     configProperty.add(parseConfigProperty(reader));
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private RequiredConfigProperty parseRequiredConfigProperty(XMLStreamReader reader) throws XMLStreamException,
      ParserException
   {
      XsdString configPropertyName = null;
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();
      //getting attributes
      String id = reader.getAttributeValue(null, RequiredConfigProperty.Attribute.ID.getLocalName());

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Activationspec16.Tag.forName(reader.getLocalName()) == Activationspec16.Tag.REQUIRED_CONFIG_PROPERTY)
               {

                  //trimming collections
                  description.trimToSize();

                  //building and returning object
                  return new RequiredConfigProperty(description, configPropertyName, id);

               }
               else
               {
                  if (RequiredConfigProperty.Tag.forName(reader.getLocalName()) == RequiredConfigProperty.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (RequiredConfigProperty.Tag.forName(reader.getLocalName()))
               {
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case CONFIG_PROPERTY_NAME : {
                     configPropertyName = elementAsXsdString(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private OutboundResourceAdapter parseOutboundResourceadapter(XMLStreamReader reader) throws XMLStreamException,
      ParserException
   {
      Boolean reauthenticationSupport = null;
      TransactionSupportEnum transactionSupport = null;
      ArrayList<ConnectionDefinition> connectionDefinition = new ArrayList<ConnectionDefinition>();
      ArrayList<AuthenticationMechanism> authenticationMechanism = new ArrayList<AuthenticationMechanism>();
      //getting attributes
      String id = reader.getAttributeValue(null, OutboundResourceAdapter.Attribute.ID.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapter1516.Tag.
                     forName(reader.getLocalName()) == ResourceAdapter1516.Tag.OUTBOUND_RESOURCEADAPTER)
               {

                  //trimming collections
                  authenticationMechanism.trimToSize();
                  connectionDefinition.trimToSize();

                  //building and returning object
                  return new OutboundResourceAdapter(connectionDefinition, transactionSupport, authenticationMechanism,
                        reauthenticationSupport, id);

               }
               else
               {
                  if (OutboundResourceAdapter.Tag.forName(reader.getLocalName()) == OutboundResourceAdapter.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (OutboundResourceAdapter.Tag.forName(reader.getLocalName()))
               {
                  case CONNECTION_DEFINITION : {
                     connectionDefinition.add(parseConncetionDefinition(reader));
                     break;
                  }
                  case AUTHENTICATION_MECHANISM : {
                     authenticationMechanism.add(parseAuthenticationMechanism(reader));
                     break;
                  }
                  case TRANSACTION_SUPPORT : {
                     transactionSupport = TransactionSupportEnum.valueOf(reader.getElementText());
                     break;
                  }
                  case REAUTHENTICATION_SUPPORT : {
                     reauthenticationSupport = elementAsBoolean(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private ConnectionDefinition parseConncetionDefinition(XMLStreamReader reader) throws XMLStreamException,
      ParserException
   {
      XsdString managedconnectionfactoryClass = null;
      ArrayList<ConfigProperty> configProperty = new ArrayList<ConfigProperty>();
      XsdString connectionImplClass = null;
      XsdString connectionInterface = null;
      XsdString connectionfactoryImplClass = null;
      XsdString connectionfactoryInterface = null;
      //getting attributes
      String id = reader.getAttributeValue(null, AuthenticationMechanism.Attribute.ID.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (OutboundResourceAdapter.Tag.
                     forName(reader.getLocalName()) == OutboundResourceAdapter.Tag.CONNECTION_DEFINITION)
               {

                  //trimming collections
                  configProperty.trimToSize();

                  //building and returning object
                  return new ConnectionDefinition(managedconnectionfactoryClass, configProperty,
                        connectionfactoryInterface, connectionfactoryImplClass, connectionInterface,
                        connectionImplClass, id);

               }
               else
               {
                  if (ConnectionDefinition.Tag.forName(reader.getLocalName()) == ConnectionDefinition.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ConnectionDefinition.Tag.forName(reader.getLocalName()))
               {
                  case CONFIG_PROPERTY : {
                     configProperty.add(parseConfigProperty(reader));
                     break;
                  }
                  case MANAGED_CONNECTIONFACTORY_CLASS : {
                     managedconnectionfactoryClass = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTIONFACTORY_INTERFACE : {
                     connectionfactoryInterface = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTIONFACTORY_IMPL_CLASS : {
                     connectionfactoryImplClass = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTION_INTERFACE : {
                     connectionInterface = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTION_IMPL_CLASS : {
                     connectionImplClass = elementAsXsdString(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private AuthenticationMechanism parseAuthenticationMechanism(XMLStreamReader reader) throws XMLStreamException,
      ParserException
   {
      XsdString authenticationMechanismType = null;
      CredentialInterfaceEnum credentialInterface = null;
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();

      //getting attributes
      String id = reader.getAttributeValue(null, AuthenticationMechanism.Attribute.ID.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (OutboundResourceAdapter.Tag.
                     forName(reader.getLocalName()) == OutboundResourceAdapter.Tag.AUTHENTICATION_MECHANISM)
               {

                  //trimming collections
                  description.trimToSize();

                  //building and returning object

                  return new AuthenticationMechanism(description, authenticationMechanismType, credentialInterface, id);

               }
               else
               {
                  if (AuthenticationMechanism.Tag.forName(reader.getLocalName()) == AuthenticationMechanism.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (AuthenticationMechanism.Tag.forName(reader.getLocalName()))
               {
                  case AUTHENTICATION_MECHANISM_TYPE : {
                     authenticationMechanismType = elementAsXsdString(reader);
                     break;
                  }
                  case CREDENTIAL_INTERFACE : {
                     credentialInterface = CredentialInterfaceEnum.forName(reader.getElementText());
                     break;
                  }
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private Adminobject parseAdminObject(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<ConfigProperty> configProperty = new ArrayList<ConfigProperty>();
      XsdString adminobjectInterface = null;
      XsdString adminobjectClass = null;

      //getting attributes
      String id = reader.getAttributeValue(null, Adminobject.Attribute.ID.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapter1516.Tag.forName(reader.getLocalName()) == ResourceAdapter1516.Tag.ADMINOBJECT)
               {
                  //trimming collections
                  configProperty.trimToSize();

                  //building and returning object
                  return new Adminobject(adminobjectInterface, adminobjectClass, configProperty, id);

               }
               else
               {
                  if (Adminobject.Tag.forName(reader.getLocalName()) == Adminobject.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Adminobject.Tag.forName(reader.getLocalName()))
               {
                  case ADMINOBJECT_CLASS : {
                     adminobjectClass = elementAsXsdString(reader);
                     break;
                  }
                  case ADMINOBJECT_INTERFACE : {
                     adminobjectInterface = elementAsXsdString(reader);
                     break;
                  }
                  case CONFIG_PROPERTY : {
                     configProperty.add(parseConfigProperty(reader));
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private ConfigProperty parseConfigProperty(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();
      XsdString configPropertyType = null;
      XsdString configPropertyValue = null;
      Boolean configPropertyIgnore = null;
      XsdString configPropertyName = null;
      Boolean configPropertySupportsDynamicUpdates = null;
      Boolean configPropertyConfidential = null;

      //getting attributes
      String id = reader.getAttributeValue(null, ConfigProperty16.Attribute.ID.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapter1516.Tag.forName(reader.getLocalName()) == ResourceAdapter1516.Tag.CONFIG_PROPERTY)
               {
                  //trimming collections
                  description.trimToSize();

                  //building and returning object

                  if (configPropertyIgnore != null || configPropertySupportsDynamicUpdates != null
                        || configPropertyConfidential != null)
                  {
                     return new ConfigProperty16(description, configPropertyName, configPropertyType,
                           configPropertyValue,
                           configPropertyIgnore, configPropertySupportsDynamicUpdates, configPropertyConfidential, id);
                  }
                  else
                  {
                     return new ConfigProperty(description, configPropertyName, configPropertyType,
                           configPropertyValue, id);
                  }

               }
               else
               {
                  if (ConfigProperty16.Tag.forName(reader.getLocalName()) == ConfigProperty16.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ConfigProperty16.Tag.forName(reader.getLocalName()))
               {
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case CONFIG_PROPERTY_NAME : {
                     configPropertyName = elementAsXsdString(reader);
                     break;
                  }
                  case CONFIG_PROPERTY_TYPE : {
                     configPropertyType = elementAsXsdString(reader);
                     break;
                  }
                  case CONFIG_PROPERTY_VALUE : {
                     configPropertyValue = elementAsXsdString(reader);
                     break;
                  }
                  case CONFIG_PROPERTY_IGNORE : {
                     configPropertyIgnore = elementAsBoolean(reader);
                     break;
                  }
                  case CONFIG_PROPERTY_CONFIDENTIAL : {
                     configPropertyConfidential = elementAsBoolean(reader);
                     break;
                  }
                  case CONFIG_PROPERTY_SUPPORT_DYNAMIC_UPDATE : {
                     configPropertySupportsDynamicUpdates = elementAsBoolean(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private SecurityPermission parseSecurityPermission(XMLStreamReader reader) throws XMLStreamException,
      ParserException
   {
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();
      XsdString securityPermissionSpec = null;

      //getting attributes
      String id = reader.getAttributeValue(null, SecurityPermission.Attribute.ID.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapter1516.Tag.
                     forName(reader.getLocalName()) == ResourceAdapter1516.Tag.SECURITY_PERMISSION)
               {
                  //trimming collections
                  description.trimToSize();

                  //building and returning object
                  return new SecurityPermission(description, securityPermissionSpec, id);

               }
               else
               {
                  if (SecurityPermission.Tag.forName(reader.getLocalName()) == SecurityPermission.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (SecurityPermission.Tag.forName(reader.getLocalName()))
               {
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case SECURITY_PERMISSION_SPEC : {
                     securityPermissionSpec = elementAsXsdString(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private LicenseType parseLicense(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      String id = reader.getAttributeValue(null, LicenseType.Attribute.ID.getLocalName());;
      boolean licenseRequired = false;
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Connector16.Tag.forName(reader.getLocalName()) == Connector16.Tag.LICENSE)
               {
                  description.trimToSize();
                  return new LicenseType(description, licenseRequired, id);

               }
               else
               {
                  if (LicenseType.Tag.forName(reader.getLocalName()) == LicenseType.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (LicenseType.Tag.forName(reader.getLocalName()))
               {
                  case LICENSE_REQUIRED : {
                     licenseRequired = elementAsBoolean(reader);
                     break;
                  }
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private XsdString elementAsXsdString(XMLStreamReader reader) throws XMLStreamException
   {
      String id = reader.getAttributeValue(null, "id");
      return new XsdString(reader.getElementText(), id);
   }

   private LocalizedXsdString elementAsLocalizedXsdString(XMLStreamReader reader) throws XMLStreamException
   {
      String id = reader.getAttributeValue(null, "id");
      String lang = reader.getAttributeValue(null, "lang");
      return new LocalizedXsdString(reader.getElementText(), id, lang);
   }

   private boolean elementAsBoolean(XMLStreamReader reader) throws XMLStreamException
   {
      return Boolean.valueOf(reader.getElementText());
   }

   /**
   *
   * A Tag.
   *
   * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
   *
   */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /** connector tag name
       *
       */
      CONNECTOR("connector");

      private final String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Tag(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
       */
      public String getLocalName()
      {
         return name;
      }

      private static final Map<String, Tag> MAP;

      static
      {
         final Map<String, Tag> map = new HashMap<String, Tag>();
         for (Tag element : values())
         {
            final String name = element.getLocalName();
            if (name != null)
               map.put(name, element);
         }
         MAP = map;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN : element;
      }

   }

}
