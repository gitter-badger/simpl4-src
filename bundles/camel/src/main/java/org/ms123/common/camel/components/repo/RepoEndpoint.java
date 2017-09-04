/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ms123.common.camel.components.repo;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.ms123.common.camel.components.repo.producer.RepoDelProducer;
import org.ms123.common.camel.components.repo.producer.RepoGetProducer;
import org.ms123.common.camel.components.repo.producer.RepoMoveProducer;
import org.ms123.common.camel.components.repo.producer.RepoPutProducer;
import org.apache.camel.impl.DefaultEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"unchecked","deprecation"})
public class RepoEndpoint extends DefaultEndpoint {

	private static final transient Logger LOG = LoggerFactory.getLogger(RepoEndpoint.class);

	private RepoConfiguration m_configuration;

	public RepoEndpoint() {
	}

	public RepoEndpoint(String uri, RepoComponent component, RepoConfiguration configuration) {
		super(uri, component);
		m_configuration = configuration;
	}

	public RepoEndpoint(String endpointUri) {
		super(endpointUri);
	}

	public Producer createProducer() throws Exception {
		LOG.info("resolve producer repo endpoint {" + m_configuration.getOperation().toString() + "}");
		if (m_configuration.getOperation() == RepoOperation.put) {
			return new RepoPutProducer(this, m_configuration);
		} else if (m_configuration.getOperation() == RepoOperation.del) {
			return new RepoDelProducer(this, m_configuration);
		} else if (m_configuration.getOperation() == RepoOperation.get) {
			return new RepoGetProducer(this, m_configuration);
		} else if (m_configuration.getOperation() == RepoOperation.move) {
			return new RepoMoveProducer(this, m_configuration);
		} else {
			throw new RuntimeException("operation specified is not valid for producer!");
		}
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		throw new RuntimeException("operation specified is not valid for consumer!");
	}

	public boolean isSingleton() {
		return true;
	}
}
