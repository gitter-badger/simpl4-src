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
package org.ms123.common.nucleus.api;

import javax.jdo.PersistenceManagerFactory;
import javax.jdo.JDOEnhancer;
import java.io.File;
import javax.transaction.UserTransaction;
import javax.transaction.TransactionManager;
import org.ms123.common.store.StoreDesc;

public interface NucleusService {

	public PersistenceManagerFactory getPersistenceManagerFactory(StoreDesc desc);

	public JDOEnhancer getEnhancer(StoreDesc sdesc);

	public ClassLoader getClassLoader(StoreDesc desc);

	public Class getClass(StoreDesc desc, String className);

	public void close(StoreDesc desc);

	public UserTransaction getUserTransaction();

	public TransactionManager getTransactionManager();

	public java.sql.Connection getJdbcConnection(StoreDesc sdesc);
}
