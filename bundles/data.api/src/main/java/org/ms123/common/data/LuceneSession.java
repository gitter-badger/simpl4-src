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
package org.ms123.common.data.api;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.apache.lucene.index.IndexWriter;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.ms123.common.store.StoreDesc;

public interface LuceneSession {

	public void setTransactionManager(TransactionManager tm);

	public void addToIndex(Object obj);

	public void deleteFromIndex(Object obj);

	public void setSessionContext(SessionContext sc);
 
	public SessionContext  getSessionContext();

	public IndexWriter getIndexWriter();

	public StoreDesc getStoreDesc();

	public void addId(String[] doc);

	public List<String[]> getIdList();


	public void commit(Xid xid, boolean b) throws XAException;

	public void rollback(Xid xid) throws XAException;

	public int prepare(Xid xid) throws XAException;

	public void end(Xid xid, int i) throws XAException;

	public void forget(Xid xid) throws XAException;

	public int getTransactionTimeout() throws XAException;

	public boolean isSameRM(XAResource xaResource) throws XAException;

	public Xid[] recover(int i) throws XAException;

	public boolean setTransactionTimeout(int i) throws XAException;

	public void start(Xid xid, int i) throws XAException;
}
