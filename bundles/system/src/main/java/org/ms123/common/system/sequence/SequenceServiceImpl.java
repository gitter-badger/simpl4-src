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
package org.ms123.common.system.sequence;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.ms123.common.git.GitService;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.store.StoreDesc;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.ms123.common.system.orientdb.OrientDBService;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/** SequenceService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=sequence" })
public class SequenceServiceImpl extends BaseSequenceServiceImpl implements SequenceService {

	private String GLOBAL_KEYSPACE = "global";

	public SequenceServiceImpl() {
	}

	protected void activate(BundleContext bc, Map<?, ?> props) {
		System.out.println("SequenceEventHandlerService.activate.props:" + props);
		try {
			Bundle b = bc.getBundle();
			this.bundleContext = bc;
			initSequence();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(Map<String, Object> props) {
		info(this,"SequenceServiceImpl.updated:" + props);
	}

	protected void deactivate() throws Exception {
		info(this,"SequenceServiceImpl.deactivate");
	}

	//@RequiresRoles("admin")
	public void  set(
		@PName("key") String key, 
		@PName("value") String value,
		@PName("attributes") @POptional Map attributes
					) throws RpcException {
		OrientGraph orientGraph = null;
		try{
			orientGraph = getGraph();
			orientGraph.begin();
			_set(key,value,attributes);
			orientGraph.commit();
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "SequenceService.set:", e);
		} finally {
			if( orientGraph!=null)orientGraph.shutdown();
		}
	}

	//@RequiresRoles("admin")
	public String  get(
		@PName("key") String key ) throws RpcException {
		OrientGraph orientGraph = null;
		try{
			orientGraph = getGraph();
			return _get(key);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "SequenceService.get:", e);
		} finally {
			if( orientGraph!=null)orientGraph.shutdown();
		}
	}

	public List<Map>  getAll(
		@PName("attributes") Map attributes ) throws RpcException {
		OrientGraph orientGraph = null;
		try{
			orientGraph = getGraph();
			return _getAll(attributes);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "SequenceService.getAll:", e);
		} finally {
			if( orientGraph!=null)orientGraph.shutdown();
		}
	}

	public List<String>  getKeys(
		@PName("attributes") Map attributes ) throws RpcException {
		OrientGraph orientGraph = null;
		try{
			orientGraph = getGraph();
			return _getKeys(attributes);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "SequenceService.getKeys:", e);
		} finally {
			if( orientGraph!=null)orientGraph.shutdown();
		}
	}

	@RequiresRoles("admin")
	public void  delete(
		@PName("key") String key ) throws RpcException {
		OrientGraph orientGraph = null;
		try{
			orientGraph = getGraph();
			orientGraph.begin();
			_delete(key);
			orientGraph.commit();
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "SequenceService.delete:", e);
		} finally {
			if( orientGraph!=null)orientGraph.shutdown();
		}
	}

	private void initSequence() {
		this.sequenceClass = getSequenceClass();
		info(this,"initSequence:"+this.sequenceClass);
		
	}
	private OrientGraph getGraph(){
		OrientGraph orientGraph = this.orientdbService.getOrientGraph("global");
		return orientGraph;
	}

	@Reference
	public void setOrientDBService(OrientDBService paramEntityService) {
		this.orientdbService = paramEntityService;
		info(this, "OrientDBLayer.setOrientDBService:" + paramEntityService);
	}
}

