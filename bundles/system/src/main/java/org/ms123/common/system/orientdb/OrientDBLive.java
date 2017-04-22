/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ms123.common.system.orientdb;

import com.orientechnologies.common.util.OCallable;
import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OLiveResultListener;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.db.record.ORecordOperation;
import com.orientechnologies.orient.core.sql.query.OLiveResultListener;
import com.orientechnologies.orient.core.sql.query.OLiveQuery;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;


import com.orientechnologies.orient.server.OServerMain;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import org.ms123.common.rpc.CallService;
import org.ms123.common.permission.api.PermissionService;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings("unchecked")
public class OrientDBLive implements OLiveResultListener  {
	private BundleContext bundleContext;
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
			this.permissionService.loginInternal("global");
			this.callService.callCamel( (String)this.liveMap.get("call"), params);

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

}

