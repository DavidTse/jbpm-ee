package org.jbpm.ee.service.remote;

import org.jbpm.ee.support.KieReleaseId;


public interface RemoteCommandExecutor {

	public String execute(KieReleaseId releaseId, RemoteResponseCommand<?> command);
	public Object pollResponse(String correlation);
	
}
