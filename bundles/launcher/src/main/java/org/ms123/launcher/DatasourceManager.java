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
package org.ms123.launcher;

import java.sql.*;
import javax.sql.*;
import java.io.*;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Properties;
import javax.naming.*;
import commonj.work.*;

/**
 */
public class DatasourceManager implements javax.sql.DataSource {

	private String m_schema = "tt_etk";
	private String m_database;
	private DataSource m_datasource = null;

	private String m_jndiName = "jdbc/EtkPool";

	public DatasourceManager() {
	}

	Context ctx = null;

	public synchronized boolean initialize() {
		try {
			ctx = new InitialContext();
			if (ctx == null){
				throw new Exception("JNDI could not create InitalContext ");
			}
			System.out.println( "\nJNDI-Context-Listing:\n" );
			String val = (String) System.getProperty("tpso.jdbc.schema");
			if (val != null) {
				m_schema = val;
			}
			val = (String) System.getProperty("tpso.jdbc.jndiname");
			if (val != null) {
				m_jndiName = val;
			}
			info("schema:"+m_schema);
			info("jndiname:"+m_jndiName);
			m_datasource = (DataSource) ctx.lookup(m_jndiName);
			info("DataSource:"+m_datasource);
			try {
				WorkManager wm = (WorkManager)ctx.lookup("wm/jettyWorkManger");
				info("wm1:"+wm);
			}catch(Exception e){
				info("wm:"+e);
				e.printStackTrace();
			}
			try {
				Connection con = m_datasource.getConnection();
				DatabaseMetaData dmd = con.getMetaData();
				m_database = dmd.getDatabaseProductName();
				info("Databasename:" + m_database);
				con.close();
			} catch (Throwable t) {
				error("initialize DBAdaptor:", t);
			}
		} catch (Exception e) {
			e.printStackTrace();
			error("initialize", e);
			return false;
		}
		info("initialize:" + m_datasource);
   //   showJndiContext( ctx, "", "" );
		return true;
	}

	public static void showJndiContext( Context ctx, String name, String space ) {
		if( null == name  ) name  = "";
		if( null == space ) space = "";
		try {
			NamingEnumeration<NameClassPair> en = ctx.list( name );
			while( en != null && en.hasMoreElements() ) {
				String delim = ( name.length() > 0 ) ? "/" : "";
				NameClassPair ncp = en.next();
				System.out.println( space + name + delim + ncp );
				if( space.length() < 40 )
					showJndiContext( ctx, ncp.getName(), "    " + space );
			}
		} catch( javax.naming.NamingException ex ) {
			// Normalerweise zu ignorieren
		}
	}
	public String getSchema() {
		return m_schema;
	}

	public String getDBName() {
		return m_database;
	}

	public void destroy() {
	}

	//DataSource implementation
	public java.io.PrintWriter getLogWriter() {
		throw new RuntimeException("ConnectionManager.getLogWriter:not implemented");
	}

	public void setLogWriter(java.io.PrintWriter pw) {
		throw new RuntimeException("ConnectionManager.setLogWriter:not implemented");
	}

	public int getLoginTimeout() {
		throw new RuntimeException("ConnectionManager.getLoginTimeout:not implemented");
	}

	public void setLoginTimeout(int timeout) {
		throw new RuntimeException("ConnectionManager.setLoginTimeout:not implemented");
	}

	public synchronized java.sql.Connection getConnection(String user, String passwd) {
		try {
			return m_datasource.getConnection(user, passwd);
		} catch (SQLException e) {
			error("getConnection1", e);
		}
		return null;
	}

	public synchronized java.sql.Connection getConnection() {
		try {
			return m_datasource.getConnection();
		} catch (SQLException e) {
			error("getConnection2", e);
		}
		return null;
	}

	public boolean isInitialized() {
		return true;
	}

	public synchronized void close(java.sql.Connection con) {
		try {
			con.close();
		} catch (SQLException e) {
			error("close", e);
		}
	}

	public void printPool() {
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException{
		return null;
	}

	public synchronized String getLastMessage() {
		return "";
	}

	public synchronized void TimerEvent(Object object) {
	}

 /**
	* @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	*/
	@Override
	public boolean isWrapperFor(Class<?> targetClass) throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> targetClass) throws SQLException {
		return null; 
	}
	private static void error(String msg,Throwable t) {
		System.out.println("DatasourceManager:" + msg);
		t.printStackTrace();
	}
	private static void error(String msg) {
		System.out.println("DatasourceManager:" + msg);
	}
	private static void info(String msg) {
		System.out.println("DatasourceManager:" + msg);
	}
}
