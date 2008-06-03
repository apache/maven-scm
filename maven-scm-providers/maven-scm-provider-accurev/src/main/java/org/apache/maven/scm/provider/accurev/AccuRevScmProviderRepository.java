package org.apache.maven.scm.provider.accurev;

/*
 * Copyright 2008 AccuRev Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.provider.ScmProviderRepository;

import java.util.Map;
import java.util.HashMap;

public class AccuRevScmProviderRepository extends ScmProviderRepository {
	public static final int DEFAULT_PORT = 5050;

	private String depot;

	private String streamName;

	private String workspaceName;

	private String host;

	private int port = DEFAULT_PORT;

	private String checkoutMethod;

	private Map params = new HashMap();

	public String getDepot() {
		return depot;
	}

	public void setDepot(String depot) {
		this.depot = depot;
	}

	public String getStreamName() {
		return streamName;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getCheckoutMethod() {
		return checkoutMethod;
	}

	public void setCheckoutMethod(String checkoutMethod) {
		this.checkoutMethod = checkoutMethod;
	}

	public Map getParams() {
		return params;
	}

	public void setParams(Map params) {
		this.params = params;
	}

	public String toString() {
		return new StringBuffer()
				.append("host:").append(this.host)
				.append(", port:").append(this.port)
				.append(", depot:").append(this.depot)
				.append(", streamName:").append(this.streamName)
				.append(", workspaceName:").append(this.workspaceName)
				.append(", checkoutMethod:").append(this.checkoutMethod)
				.append(", params:").append(this.params)
				.toString();
	}
}
