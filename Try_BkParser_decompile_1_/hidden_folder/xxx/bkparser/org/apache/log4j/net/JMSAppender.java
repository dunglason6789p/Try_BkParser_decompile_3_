package org.apache.log4j.net;

import java.util.Properties;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

public class JMSAppender extends AppenderSkeleton {
   String securityPrincipalName;
   String securityCredentials;
   String initialContextFactoryName;
   String urlPkgPrefixes;
   String providerURL;
   String topicBindingName;
   String tcfBindingName;
   String userName;
   String password;
   boolean locationInfo;
   TopicConnection topicConnection;
   TopicSession topicSession;
   TopicPublisher topicPublisher;

   public JMSAppender() {
   }

   public void setTopicConnectionFactoryBindingName(String tcfBindingName) {
      this.tcfBindingName = tcfBindingName;
   }

   public String getTopicConnectionFactoryBindingName() {
      return this.tcfBindingName;
   }

   public void setTopicBindingName(String topicBindingName) {
      this.topicBindingName = topicBindingName;
   }

   public String getTopicBindingName() {
      return this.topicBindingName;
   }

   public boolean getLocationInfo() {
      return this.locationInfo;
   }

   public void activateOptions() {
      try {
         LogLog.debug("Getting initial context.");
         InitialContext jndi;
         if (this.initialContextFactoryName != null) {
            Properties env = new Properties();
            env.put("java.naming.factory.initial", this.initialContextFactoryName);
            if (this.providerURL != null) {
               env.put("java.naming.provider.url", this.providerURL);
            } else {
               LogLog.warn("You have set InitialContextFactoryName option but not the ProviderURL. This is likely to cause problems.");
            }

            if (this.urlPkgPrefixes != null) {
               env.put("java.naming.factory.url.pkgs", this.urlPkgPrefixes);
            }

            if (this.securityPrincipalName != null) {
               env.put("java.naming.security.principal", this.securityPrincipalName);
               if (this.securityCredentials != null) {
                  env.put("java.naming.security.credentials", this.securityCredentials);
               } else {
                  LogLog.warn("You have set SecurityPrincipalName option but not the SecurityCredentials. This is likely to cause problems.");
               }
            }

            jndi = new InitialContext(env);
         } else {
            jndi = new InitialContext();
         }

         LogLog.debug("Looking up [" + this.tcfBindingName + "]");
         TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory)this.lookup(jndi, this.tcfBindingName);
         LogLog.debug("About to create TopicConnection.");
         if (this.userName != null) {
            this.topicConnection = topicConnectionFactory.createTopicConnection(this.userName, this.password);
         } else {
            this.topicConnection = topicConnectionFactory.createTopicConnection();
         }

         LogLog.debug("Creating TopicSession, non-transactional, in AUTO_ACKNOWLEDGE mode.");
         this.topicSession = this.topicConnection.createTopicSession(false, 1);
         LogLog.debug("Looking up topic name [" + this.topicBindingName + "].");
         Topic topic = (Topic)this.lookup(jndi, this.topicBindingName);
         LogLog.debug("Creating TopicPublisher.");
         this.topicPublisher = this.topicSession.createPublisher(topic);
         LogLog.debug("Starting TopicConnection.");
         this.topicConnection.start();
         jndi.close();
      } catch (JMSException var4) {
         super.errorHandler.error("Error while activating options for appender named [" + super.name + "].", var4, 0);
      } catch (NamingException var5) {
         super.errorHandler.error("Error while activating options for appender named [" + super.name + "].", var5, 0);
      } catch (RuntimeException var6) {
         super.errorHandler.error("Error while activating options for appender named [" + super.name + "].", var6, 0);
      }

   }

   protected Object lookup(Context ctx, String name) throws NamingException {
      try {
         return ctx.lookup(name);
      } catch (NameNotFoundException var4) {
         LogLog.error("Could not find name [" + name + "].");
         throw var4;
      }
   }

   protected boolean checkEntryConditions() {
      String fail = null;
      if (this.topicConnection == null) {
         fail = "No TopicConnection";
      } else if (this.topicSession == null) {
         fail = "No TopicSession";
      } else if (this.topicPublisher == null) {
         fail = "No TopicPublisher";
      }

      if (fail != null) {
         super.errorHandler.error(fail + " for JMSAppender named [" + super.name + "].");
         return false;
      } else {
         return true;
      }
   }

   public synchronized void close() {
      if (!super.closed) {
         LogLog.debug("Closing appender [" + super.name + "].");
         super.closed = true;

         try {
            if (this.topicSession != null) {
               this.topicSession.close();
            }

            if (this.topicConnection != null) {
               this.topicConnection.close();
            }
         } catch (JMSException var2) {
            LogLog.error("Error while closing JMSAppender [" + super.name + "].", var2);
         } catch (RuntimeException var3) {
            LogLog.error("Error while closing JMSAppender [" + super.name + "].", var3);
         }

         this.topicPublisher = null;
         this.topicSession = null;
         this.topicConnection = null;
      }
   }

   public void append(LoggingEvent event) {
      if (this.checkEntryConditions()) {
         try {
            ObjectMessage msg = this.topicSession.createObjectMessage();
            if (this.locationInfo) {
               event.getLocationInformation();
            }

            msg.setObject(event);
            this.topicPublisher.publish(msg);
         } catch (JMSException var3) {
            super.errorHandler.error("Could not publish message in JMSAppender [" + super.name + "].", var3, 0);
         } catch (RuntimeException var4) {
            super.errorHandler.error("Could not publish message in JMSAppender [" + super.name + "].", var4, 0);
         }

      }
   }

   public String getInitialContextFactoryName() {
      return this.initialContextFactoryName;
   }

   public void setInitialContextFactoryName(String initialContextFactoryName) {
      this.initialContextFactoryName = initialContextFactoryName;
   }

   public String getProviderURL() {
      return this.providerURL;
   }

   public void setProviderURL(String providerURL) {
      this.providerURL = providerURL;
   }

   String getURLPkgPrefixes() {
      return this.urlPkgPrefixes;
   }

   public void setURLPkgPrefixes(String urlPkgPrefixes) {
      this.urlPkgPrefixes = urlPkgPrefixes;
   }

   public String getSecurityCredentials() {
      return this.securityCredentials;
   }

   public void setSecurityCredentials(String securityCredentials) {
      this.securityCredentials = securityCredentials;
   }

   public String getSecurityPrincipalName() {
      return this.securityPrincipalName;
   }

   public void setSecurityPrincipalName(String securityPrincipalName) {
      this.securityPrincipalName = securityPrincipalName;
   }

   public String getUserName() {
      return this.userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public void setLocationInfo(boolean locationInfo) {
      this.locationInfo = locationInfo;
   }

   protected TopicConnection getTopicConnection() {
      return this.topicConnection;
   }

   protected TopicSession getTopicSession() {
      return this.topicSession;
   }

   protected TopicPublisher getTopicPublisher() {
      return this.topicPublisher;
   }

   public boolean requiresLayout() {
      return false;
   }
}
