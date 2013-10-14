package org.jbpm.ee.jms;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.jbpm.ee.remote.RemoteCommandExecutor;
import org.jbpm.ee.remote.RemoteResponseCommand;
import org.jbpm.ee.support.KieReleaseId;
import org.mvel2.sh.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Remote(RemoteCommandExecutor.class)
@Stateless
public class AsyncCommandExecutorBean implements RemoteCommandExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(AsyncCommandExecutorBean.class);
	
	@Resource
	private ConnectionFactory connectionFactory;

	@Resource(mappedName = "jms/JBPMCommandRequestQueue")
	private Queue requestQueue;

	@Inject
	private KieReleaseId releaseId;

	@Resource(mappedName = "jms/JBPMCommandResponseQueue")
	private Queue responseQueue;

	private Connection connection;
	private Session session;
	private MessageProducer producer;
	

    @PostConstruct
    public void init() throws JMSException {
        connection = connectionFactory.createConnection();
        session = connection.createSession();
        producer = session.createProducer(requestQueue); 
    }
	
	
	@Override
	public String execute(KieReleaseId kieReleaseId, RemoteResponseCommand<?> command) {
		this.releaseId = kieReleaseId;
		
		String uuid = UUID.randomUUID().toString();
		try {
			ObjectMessage request = session.createObjectMessage();
			request.setJMSCorrelationID(uuid);
			request.setObject(command);
			request.setJMSReplyTo(responseQueue);
			producer.send(request);
			
			request.setObjectProperty("groupId", releaseId.getGroupId());
			request.setObjectProperty("artifactId", releaseId.getArtifactId());
			request.setObjectProperty("version", releaseId.getVersion());
			
			return uuid.toString();
		} catch (JMSException e) {
			throw new CommandException("Exception sending Command Message.", e);
		}
	}

	@Override
	public Object pollResponse(String correlation) {
		final String correlationSelector = "JMSCorrelationID='" + correlation + "'";
		
		try {
			MessageConsumer consumer = session.createConsumer(responseQueue, correlationSelector);
			Message response = consumer.receive(10000);

			if(response == null) {
				LOG.debug("Message not yet recieved: "+correlation);
				return null;
			}
			else {
				LOG.debug("Recieved message for correlation: "+correlation);
				return ((ObjectMessage)response).getObject();
			}
		}
		catch (JMSException e) {
			throw new CommandException("Exception receiving Command Message Response.", e);
		}
	}

}
