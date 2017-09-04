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
package org.ms123.common.camel.components.repo.producer;

import org.apache.camel.component.file.*;
import org.apache.camel.Exchange;
import org.apache.camel.CamelContext;
import org.ms123.common.camel.components.repo.RepoConfiguration;
import org.ms123.common.camel.components.repo.RepoEndpoint;
import org.ms123.common.camel.api.CamelService;
import java.io.File;
import org.slf4j.LoggerFactory;
import org.ms123.common.git.GitService;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import org.apache.camel.util.FileUtil;
import static com.jcabi.log.Logger.info;
import org.ms123.common.camel.api.ExchangeUtils;

public class RepoGetProducer extends RepoProducer {
	private CamelService camelService;

	public RepoGetProducer(RepoEndpoint endpoint, RepoConfiguration configuration) {
		super(endpoint, configuration);
		this.camelService = (CamelService) endpoint.getCamelContext().getRegistry().lookupByName(CamelService.class.getName());
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		String path = ExchangeUtils.getParameter(configuration.getPath(),exchange, String.class, "Path");
		String repo = ExchangeUtils.getParameter(configuration.getRepo(),exchange, String.class, "Repo");
		if( repo == null || repo == "" || "-".equals(repo)) repo = exchange.getProperty("_namespace", String.class);
		String type = configuration.getType();
		GitService gitService = getGitService();
		File file = gitService.searchFile(repo, path, type);
		String fileType = gitService.getFileType(file);
		String destination = configuration.getDestination();
		info(this, "producer --> get:repo: " + repo + ",path:" + path + ",type:" + type + "/destination:" + destination+"/realtype:"+fileType);
		if( fileType.startsWith("sw.")){
			String content = gitService.getFileContent(file);
			ExchangeUtils.setDestination(destination,content , exchange);
		}else{
			ExchangeUtils.setDestination(destination,file , exchange);
		}
	}
}

