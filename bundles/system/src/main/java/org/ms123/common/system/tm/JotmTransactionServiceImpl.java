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
package org.ms123.common.system.tm;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.objectweb.jotm.Jotm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.ms123.common.system.tm.TransactionService;

/**
 *
 */
public class JotmTransactionServiceImpl implements TransactionService{

	protected TransactionTemplate transactionTemplate;

	protected Jotm m_jotm;

	protected JtaTransactionManager m_jta;

	public JotmTransactionServiceImpl() throws Exception{
			m_jotm = new org.objectweb.jotm.Jotm(true, false);
			m_jta = new JtaTransactionManager(m_jotm.getTransactionManager());
	}
	public UserTransaction getUserTransaction() {
		UserTransaction utx = m_jotm.getUserTransaction();
		return utx;
	}
	public String getJtaLocator(){
		return "jotm";
	}

	public TransactionManager getTransactionManager() {
		TransactionManager tm = m_jotm.getTransactionManager();
		return tm;
	}

	public PlatformTransactionManager getPlatformTransactionManager() {
		return m_jta;
	}

	public Jotm getJotm() {
		return m_jotm;
	}

	public TransactionTemplate getTransactionTemplate() {
		return getTransactionTemplate(false);
	}

	public TransactionTemplate getTransactionTemplate(boolean _new) {
		DefaultTransactionDefinition d = _new ? new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW) : new DefaultTransactionDefinition();
		System.err.println("TransactionTemplate:" + d);
		return new TransactionTemplate(m_jta, d);
	}

	protected static void debug(String msg) {
		System.out.println(msg);
		m_logger.debug(msg);
	}

	protected static void info(String msg) {
		System.out.println(msg);
		m_logger.info(msg);
	}

	private static final Logger m_logger = LoggerFactory.getLogger(JotmTransactionServiceImpl.class);
}
