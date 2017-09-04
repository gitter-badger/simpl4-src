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
import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.BitronixTransactionManager;
import java.util.Hashtable;
import javax.naming.*;

/**
 *
 */
public class BitronixTransactionServiceImpl implements TransactionService{

	protected TransactionTemplate transactionTemplate;

	public static  BitronixTransactionManager m_btm;

	protected JtaTransactionManager m_jta;

  @SuppressWarnings("unchecked")
	public BitronixTransactionServiceImpl() throws Exception{
		TransactionManagerServices.getConfiguration().setDefaultTransactionTimeout(36000);;
		TransactionManagerServices.getConfiguration().setWarnAboutZeroResourceTransaction(false);
		m_btm = TransactionManagerServices.getTransactionManager();
		m_jta = new JtaTransactionManager((UserTransaction)m_btm,(TransactionManager)m_btm);
	}

	public UserTransaction getUserTransaction() {
		UserTransaction utx = m_btm;
		return utx;
	}
	public String getJtaLocator(){
		return "bitronix";
	}

	public TransactionManager getTransactionManager() {
		TransactionManager tm = m_btm;
		return tm;
	}

	public PlatformTransactionManager getPlatformTransactionManager() {
		return m_jta;
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
		m_logger.debug(msg);
	}

	protected static void info(String msg) {
		m_logger.info(msg);
	}

	private static final Logger m_logger = LoggerFactory.getLogger(BitronixTransactionServiceImpl.class);
}
