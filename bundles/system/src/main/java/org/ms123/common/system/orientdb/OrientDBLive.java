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
package org.ms123.common.system.orientdb;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.db.record.ORecordOperation;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OLiveQuery;
import com.orientechnologies.orient.core.sql.query.OLiveResultListener;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.rpc.CallService;
import org.ms123.common.system.thread.ThreadContext;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

@SuppressWarnings("unchecked")
public class OrientDBLive implements OLiveResultListener  {
	private Map<String,Object> liveMap;
	private Object token;
	private CallService callService;
	private PermissionService permissionService;

	public OrientDBLive( OrientGraph graph, Map<String,Object> liveMap, CallService cs,PermissionService ps) {
		this.liveMap = liveMap;
		this.callService = cs;
		this.permissionService = ps;
		executeLiveQuery( graph, (String)liveMap.get("sql"));
	}

	@Override
	public void onLiveResult(int iLiveToken, ORecordOperation iOp) throws OException {
		try{
			info(this,"New result from server for live query "+iLiveToken);
			info(this,"operation: "+iOp.type);
			info(this,"content: "+iOp.getRecord().toJSON("rid,class,version"));
			info(this,"content: "+((ODocument)iOp.getRecord()).toMap());
			Map params = new HashMap();
			params.put("record", ((ODocument)iOp.getRecord()).toMap());
			new ExecThread( (String)this.liveMap.get("call"), params).start();

		}catch(Exception e){
			error(this, "OrientDBLive.onLiveResult.error:%[exception]s", e);
		}
	}

	public void onError(int iLiveToken) {
		info(this,"Live query terminate due to error:"+iLiveToken);
	}

	public void onUnsubscribe(int iLiveToken) {
		info(this,"Live query terminate with unsubscribe:"+iLiveToken);
	}

	public String getDatabaseName(){
		return (String)this.liveMap.get("database");
	}

	public String getName(){
		return (String)this.liveMap.get("name");
	}

	public void unsubscribe(OrientGraph graph) {
		graph.getRawGraph().command(new OCommandSQL("live unsubscribe "+this.token)).execute();
	}


	private void executeLiveQuery(OrientGraph graph, String sql, Object... args) {
		List<ODocument> result = 	graph.getRawGraph().query(new OLiveQuery<ODocument>(sql, this));

		this.token = result.get(0).field("token"); // 1234567
		info(this,"executeLiveQuery("+sql+"):"+this.token);
	}

	private class ExecThread extends Thread {
		String call;
		Map params;

		public ExecThread(String call, Map params) {
			this.call = call;
			this.params = params;
		}

		public void run() {
			try {
				permissionService.loginInternal("global");
				Object answer = callService.callCamel( call, params);
				info(this, "OrientDBLive.ExecThread.callCamel.answer:" + answer);
			} catch (Exception e) {
				ThreadContext.getThreadContext().finalize(e);
				error(this, "OrientDBLive.ExecThread:%[exception]s", e);
			} finally {
				ThreadContext.getThreadContext().finalize(null);
			}
		}
	}
}

