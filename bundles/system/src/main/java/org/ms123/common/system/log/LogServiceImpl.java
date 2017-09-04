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
package org.ms123.common.system.log;

import java.io.FileInputStream;
import java.io.File;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import aQute.bnd.annotation.metatype.*;
import aQute.bnd.annotation.component.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.store.StoreDesc;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.transaction.UserTransaction;
import javax.transaction.Status;
import static org.apache.commons.beanutils.PropertyUtils.setProperty;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.info;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.commons.io.input.ReversedLinesFileReader;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/** LogService implementation
 */
@SuppressWarnings({"unchecked","deprecation"})
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=log" })
public class LogServiceImpl extends BaseLogServiceImpl implements LogService, EventHandler {

	private static final Logger m_logger = LoggerFactory.getLogger(LogServiceImpl.class);
	private  ServiceRegistration m_serviceRegistration;

	private EventAdmin m_eventAdmin;

	public LogServiceImpl() {
	}

	static final String[] topics = new String[] { "log" };

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		try {
			Bundle b = bundleContext.getBundle();
			m_bundleContext = bundleContext;
			Dictionary d = new Hashtable();
			d.put(EventConstants.EVENT_TOPIC, topics);
			m_serviceRegistration = b.getBundleContext().registerService(EventHandler.class.getName(), this, d);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handleEvent(Event event) {
		debug(this,"LogServiceImpl.handleEvent: " + event + ",key:" + event.getProperty(LOG_KEY) + ",type:" + event.getProperty(LOG_TYPE));
		StoreDesc sdesc = StoreDesc.getGlobalData();
		PersistenceManager pm = m_nucleusService.getPersistenceManagerFactory(sdesc).getPersistenceManager();
		try {
			Object logMessage = m_nucleusService.getClass(sdesc, "logmessage").newInstance();
			setProperty(logMessage, LOG_KEY, event.getProperty(LOG_KEY));
			if (event.getProperty(LOG_HINT) != null) {
				setProperty(logMessage, LOG_HINT, event.getProperty(LOG_HINT));
			}
			if (event.getProperty(LOG_TYPE) != null) {
				setProperty(logMessage, LOG_TYPE, event.getProperty(LOG_TYPE));
			}
			setProperty(logMessage, LOG_MSG, event.getProperty(LOG_MSG));
			setProperty(logMessage, LOG_TIME, new Date());
			pm.makePersistent(logMessage);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pm.close();
		}
		debug(this,"LogServiceImpl.end");
	}

	public void update(Map<String, Object> props) {
		info(this,"LogServiceImpl.updated:" + props);
	}

	protected void deactivate() throws Exception {
		info(this,"LogServiceImpl.deactivate");
		m_serviceRegistration.unregister();
	}

	@RequiresRoles("admin")
	public String getLastNLinesFromStdout( @PName("lines")          Integer lines ) throws RpcException {
		try {
			if( lines <= 0 ) return "";
			if( lines > 10000) lines = 10000;
			StringBuilder sb = new StringBuilder(4096);
			String simpl4Dir = (String) System.getProperty("simpl4.dir");
			File file = new File( simpl4Dir, "log/stdout.log" );
			int counter = 0; 
			ReversedLinesFileReader object = new ReversedLinesFileReader(file);
			while(counter < lines) {
				String line = object.readLine();
				if( isEmpty(line)){
					break;
				}
				sb.insert(0, line+"\n");
				counter++;
			}
			object.close();
			return sb.toString();
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "LogServiceImpl.getLastNLinesFromStdout:", e);
		}
	}

	public Map<String, List<Map>> getLogKeyList(
			@PName("keyList")          List<String> keyList, 
			@PName(LOG_TYPE)             @POptional String type) throws RpcException {
		try {
			Map<String, List<Map>> retMap = new HashMap();
			for (String key : keyList) {
				List<Map> logs = _getLog(key, type,null,null,null,null);
				if (logs.size() > 0) {
					retMap.put(key, logs);
				}
			}
			return retMap;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "LogServiceImpl.getLogKeyList:", e);
		}
	}

	public List<Map> getLog(
			@PName(LOG_KEY)              String key, 
			@PName(LOG_TYPE)             @POptional String type, 
			@PName("projection")       		@POptional String projection, 
			@PName("orderby")       		@POptional String orderby, 
			@PName("startTime")        @POptional Long startTime, 
			@PName("endTime")          @POptional Long endTime) throws RpcException {
		try {
			return _getLog(key, type, projection, orderby,startTime, endTime);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "LogServiceImpl.getLog:", e);
		}
	}

	@Reference(dynamic = true)
	public void setEventAdmin(EventAdmin paramEventAdmin) {
		System.out.println("LogServiceImpl.setEventAdmin:" + paramEventAdmin);
		this.m_eventAdmin = paramEventAdmin;
	}

	@Reference
	public void setNucleusService(NucleusService paramNucleusService) {
		this.m_nucleusService = paramNucleusService;
		System.out.println("LogServiceImpl.setNucleusService:" + paramNucleusService);
	}
}
