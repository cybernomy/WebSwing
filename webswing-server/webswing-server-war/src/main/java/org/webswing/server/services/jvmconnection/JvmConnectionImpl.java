package org.webswing.server.services.jvmconnection;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.model.exception.WsException;

public class JvmConnectionImpl implements MessageListener, JvmConnection {

	private static final Logger log = LoggerFactory.getLogger(JvmConnectionImpl.class);
	private static ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");

	private final JvmListener listener;
	private Connection connection;
	private Session session;
	private Queue producerQueue;
	private Queue consumerQueue;
	private MessageProducer producer;
	private MessageConsumer consumer;

	private boolean jmsOpen = false;

	public JvmConnectionImpl(String clientId, JvmListener listener) throws WsException {
		try {
			this.listener = listener;
			initialize(clientId);
		} catch (Exception e) {
			log.error("Failed to connect to Jvm.", e);
			throw new WsException("Failed to connect to Jvm.", e);
		}
	}

	private void initialize(final String clientId) throws JMSException {
		synchronized (this) {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producerQueue = session.createQueue(clientId + Constants.SERVER2SWING);
			consumerQueue = session.createQueue(clientId + Constants.SWING2SERVER);
			consumer = session.createConsumer(consumerQueue);
			consumer.setMessageListener(this);
			producer = session.createProducer(producerQueue);
			jmsOpen = true;
			connection.setExceptionListener(new ExceptionListener() {

				@Override
				public void onException(JMSException paramJMSException) {
					log.warn("JMS encountered an exception: " + paramJMSException.getMessage());
					try {
						synchronized (JvmConnectionImpl.this) {
							consumer.close();
							producer.close();
							session.close();
							connection.close();
							jmsOpen = false;
						}
					} catch (JMSException e) {
						log.error("SwingJvmConnection:initialize1", e);
					}
					try {
						JvmConnectionImpl.this.initialize(clientId);
					} catch (JMSException e) {
						log.error("SwingJvmConnection:initialize2", e);

					}
				}
			});
		}
	}

	public synchronized void send(Serializable o) {
		if (jmsOpen) {
			try {
				if (o instanceof String) {
					producer.send(session.createTextMessage((String) o));
				} else {
					producer.send(session.createObjectMessage(o));
				}
			} catch (JMSException e) {
				log.debug("SwingJvmConnection:send", e);
			}
		}
	}

	public void onMessage(Message m) {
		try {
			if (m instanceof ObjectMessage) {
				Serializable o = ((ObjectMessage) m).getObject();
				listener.onJvmMessage(o);
			}
		} catch (Exception e) {
			log.error("SwingJvmConnection:onMessage", e);
		}
	}

	public void close() {
		try {
			synchronized (this) {
				try {
					consumer.close();
					producer.close();
					session.close();
				} finally {
					((ActiveMQConnection) connection).destroyDestination((ActiveMQDestination) consumerQueue);
					((ActiveMQConnection) connection).destroyDestination((ActiveMQDestination) consumerQueue);
					connection.close();
					jmsOpen = false;
				}
			}
		} catch (Exception e) {
			log.error("SwingJvmConnection:close", e);
			jmsOpen = false;
		}
	}
}
