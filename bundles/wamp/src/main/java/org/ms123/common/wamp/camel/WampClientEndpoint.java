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
package org.ms123.common.wamp.camel;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.core.osgi.utils.BundleContextUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.ms123.common.wamp.WampService;
import org.ms123.common.wamp.WampClientSession;
import org.ms123.common.wamp.WampServiceImpl;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


/**
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@UriEndpoint(scheme = "wampclient", title = "WampClient", syntax = "wampclient:topic", consumerClass = WampClientConsumer.class)
public class WampClientEndpoint extends DefaultEndpoint {


	private JSONDeserializer ds = new JSONDeserializer();
	// common options
	@UriParam
	private String mode;


	// producer(publisher) options
	//@UriParam(label = "producer")
	//private String procedure;

	// consumer(rpc) options
	@UriParam
	private String procedure;
	private String topic;
	private String topic2;
	private String match;
	private String startableGroups;
	private String startableUsers;
	private String publish;
	private String rpcReturn;
	private String rpcParameter;
	private String publishHeaders;
	private String rpcReturnHeaders;

	private BundleContext m_bundleContext;
	private WampService wampService;

	public WampClientEndpoint(WampClientComponent component, String uri, String remaining) {
		super(uri, component);
		this.mode = remaining;
	}

	@Override
	public WampClientComponent getComponent() {
		return (WampClientComponent) super.getComponent();
	}

	public Producer createProducer() throws Exception {
		return new WampClientProducer(this);
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		WampClientConsumer consumer = new WampClientConsumer(this, processor);
		configureConsumer(consumer);
		return consumer;
	}

	public boolean isSingleton() {
		return true;
	}

	public WampClientSession createWampClientSession(String realm) {
		return WampServiceImpl.createWampClientSession(realm);
	}

	public BundleContext getBundleContext() {
		return m_bundleContext;
	}

	public String getProcedure() {
		return this.procedure;
	}

	public void setProcedure(String procedure) {
		this.procedure = procedure;
	}
	public String getTopic2() {
		return this.topic2;
	}

	public void setTopic2(String topic) {
		this.topic2 = topic;
	}

	public String getTopic() {
		return this.topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getMatch() {
		return this.match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	public String getStartableUsers() {
		return this.startableUsers;
	}

	public void setStartableUsers(String s) {
		this.startableUsers = s;
	}

	public String getStartableGroups() {
		return this.startableGroups;
	}

	public void setStartableGroups(String s) {
		this.startableGroups = s;
	}

	public String getRpcParameter() {
		return this.rpcParameter;
	}

	public void setRpcParameter(String s) {
		this.rpcParameter = s;
	}

	public String getRpcReturn() {
		return this.rpcReturn;
	}

	public void setRpcReturn(String s) {
		this.rpcReturn = s;
	}

	public String getRpcReturnHeaders() {
		return this.rpcReturnHeaders;
	}

	public void setRpcReturnHeaders(String s) {
		this.rpcReturnHeaders = s;
	}



	public String getPublish() {
		return this.publish;
	}

	public void setPublish(String s) {
		this.publish = s;
	}

	public String getPublishHeaders() {
		return this.publishHeaders;
	}

	public void setPublishHeaders(String s) {
		this.publishHeaders = s;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public List<String> getPermittedUsers() {
		return getStringList(this.startableUsers);
	}
	public List<String> getPermittedRoles() {
		return getStringList(this.startableGroups);
	}
	public List<Map> getParamList() {
		if( this.rpcParameter==null) return new ArrayList<Map>();
		Map  res =  (Map)ds.deserialize(this.rpcParameter);
		return (List<Map>)res.get("items");
	}
	private List<Map> _getReturnHeaders() {
		if( this.rpcReturnHeaders==null) return new ArrayList<Map>();
		Map  res =  (Map)ds.deserialize(this.rpcReturnHeaders);
		return (List<Map>)res.get("items");
	}

	public List<String> getReturnHeaderList(){
		List<String> returnHeaderList = new ArrayList();
		List<Map> rh = this._getReturnHeaders();
		if (rh != null) {
			for (Map<String, String> m : rh) {
				returnHeaderList.add(m.get("name"));
			}
		}
		return returnHeaderList;
	}

	private List<Map> _getPublishHeaders() {
		if( this.publishHeaders==null) return new ArrayList<Map>();
		Map  res =  (Map)ds.deserialize(this.publishHeaders);
		return (List<Map>)res.get("items");
	}

	public List<String> getPublishHeaderList(){
		List<String> publishHeaderList = new ArrayList();
		List<Map> rh = this._getPublishHeaders();
		if (rh != null) {
			for (Map<String, String> m : rh) {
				publishHeaderList.add(m.get("name"));
			}
		}
		return publishHeaderList;
	}

	protected List<String> getStringList(String s) {
		if( s==null) return new ArrayList<String>();
		return Arrays.asList(s.split(","));
	}

}

