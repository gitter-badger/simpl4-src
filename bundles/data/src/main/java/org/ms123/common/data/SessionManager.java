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
package org.ms123.common.data;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import javax.jdo.PersistenceManager;
import javax.transaction.UserTransaction;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.store.StoreDesc;
import javax.transaction.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class SessionManager implements org.ms123.common.system.thread.ThreadFinalizer{

	private NucleusService m_nucleusService;
	private Map m_userProperties;

	private Map<StoreDesc, PersistenceManager> m_pmMap = new HashMap();
	private Map<String, Map> m_permittedFieldsMap = new HashMap();
	private Map<String, List<Map>> m_primaryKeyFieldsMap = new HashMap<String, List<Map>>();

	public SessionManager(NucleusService ns){
		m_nucleusService = ns;
	}
	public void setUserProperties(Map data) {
		m_userProperties = data;
	}

	public Map getUserProperties() {
		return m_userProperties;
	}
	public String getUserName() {
		return getThreadContext().getUserName();
	}

	public void finalize(Throwable t) {
		if( t == null){
			handleFinish();
		}else{
		handleException(t);
		}
	}
	public synchronized void handleFinish() {
		info("-> SessionManager.handleFinish");
		UserTransaction ut = m_nucleusService.getUserTransaction();
		if (ut != null) {
			try {
				info("\tstatus:" + ut.getStatus());
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (ut.getStatus() == Status.STATUS_ACTIVE) {
					info("###Transaction aktive:rollback");
					ut.rollback();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		for (StoreDesc key : m_pmMap.keySet()) {
			PersistenceManager pm = m_pmMap.get(key);
			System.err.println("\tclose:" + key);
			pm.close();
		}
		m_pmMap = new HashMap();
	}

	public void handleException(Throwable e) {
		handleException(null, e);
	}

	public void handleException(UserTransaction ut, Throwable e) {
		error("\n--> SessionManager.handleException:"+e.getClass()+"/"+getStatus(ut));
		if (ut != null) {
			if (!(e instanceof javax.transaction.RollbackException)) {
				try {
					error("\thandleException.status:" + ut);
					if (ut.getStatus() == Status.STATUS_ACTIVE) {
						ut.rollback();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		String msg = e.getMessage();
		while (e.getCause() != null) {
			e = e.getCause();
			msg += "\n" +e.getMessage();
		}
		error("---------------------------ExecptionMessage------------------");
		error(msg);
		error("-------------------------------------------------------------\n");

		if (e instanceof RuntimeException) {
			throw (RuntimeException) e;
		} else {
			throw new RuntimeException(e);
		}
	}
	public Object getStatus(UserTransaction ut){
		try{
			if( ut != null) return ut.toString()+":status:"+ut.getStatus();
		}catch(Exception e){
		}
		return "No Transaction active";
	}

	public PersistenceManager getPM(StoreDesc sdesc) {
		PersistenceManager pm = m_pmMap.get(sdesc);
		if (pm == null) {
			pm = m_nucleusService.getPersistenceManagerFactory(sdesc).getPersistenceManager();
			m_pmMap.put(sdesc, pm);
		}
		return pm;
	}
	protected List<Map> getPrimaryKeyFields(String entityName){
		return m_primaryKeyFieldsMap.get(entityName);
	}

	protected void  setPrimaryKeyFields(String entityName, List<Map> pkList){
		m_primaryKeyFieldsMap.put(entityName,pkList);
	}
	protected Map<String,Map> getPermittedFieldsMap(String entityName){
		return m_permittedFieldsMap.get(entityName);
	}

	protected void  setPermittedFieldsMap(String entityName, Map<String,Object> map){
		m_permittedFieldsMap.put(entityName,map);
	}
	private org.ms123.common.system.thread.ThreadContext getThreadContext() {
		return org.ms123.common.system.thread.ThreadContext.getThreadContext();
	}
	protected void info(String message) {
		m_logger.info(message);
	}
	protected void error(String message) {
		m_logger.error(message);
		System.err.println(message);
	}
	private static final Logger m_logger = LoggerFactory.getLogger(SessionManager.class);
}
