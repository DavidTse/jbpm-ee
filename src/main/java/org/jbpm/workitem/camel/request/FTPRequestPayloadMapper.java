package org.jbpm.workitem.camel.request;

import java.util.Arrays;
import java.util.HashSet;

public class FTPRequestPayloadMapper extends RequestPayloadMapper {
	private static final String[] MESSAGE_HEADERS = new String[]{"CamelFileName", "CamelFileNameProduced", "CamelFileBatchIndex", "CamelFileBatchSize", "CamelFileHost", "CamelFileLocalWorkPath", "CamelFtpReplyCode", "CamelFtpReplyString"};

	public FTPRequestPayloadMapper(String requestLocation) {
		super(requestLocation, new HashSet<String>(Arrays.asList(MESSAGE_HEADERS)));
	}
}
