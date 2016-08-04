/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ms123.common.system.jdbc;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;

public class JTDSDataSourceFactory implements DataSourceFactory {

    private static final String JTDS_DRIVER_FQCN = "net.sourceforge.jtds.jdbc.Driver";
    private static final String JTDS_DATASOURCE_FQCN = "net.sourceforge.jtds.jdbcx.JtdsDataSource";
    private static final String JTDS_CONNECTIONPOOL_DATASOURCE_FQCN = "net.sourceforge.jtds.jdbcx.JtdsDataSource";
    private static final String JTDS_XA_DATASOURCE_FQCN = "net.sourceforge.jtds.jdbcx.JtdsDataSource";
    private final Class<?> jtdsDriverClass;
    private final Class<?> jtdsDataSourceClass;
    private final Class<?> jtdsConnectionPoolDataSourceClass;
    private final Class<?> jtdsXADataSourceClass;

    public JTDSDataSourceFactory() throws ClassNotFoundException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        this.jtdsDriverClass = classLoader.loadClass(JTDS_DRIVER_FQCN);
        this.jtdsDataSourceClass = classLoader.loadClass(JTDS_DATASOURCE_FQCN);
        this.jtdsConnectionPoolDataSourceClass = classLoader.loadClass(JTDS_CONNECTIONPOOL_DATASOURCE_FQCN);
        this.jtdsXADataSourceClass = classLoader.loadClass(JTDS_XA_DATASOURCE_FQCN);
    }


    private void setProperties(CommonDataSource ds, Class<?> clazz, Properties properties) throws Exception {
        Properties props = (Properties) properties.clone();

        String databaseName = (String) props.remove(DataSourceFactory.JDBC_DATABASE_NAME);
        if (databaseName == null) {
            throw new SQLException("missing required property " + DataSourceFactory.JDBC_DATABASE_NAME);
        }
        clazz.getMethod("setDatabaseName", String.class).invoke(ds, databaseName);

        String serverName = (String) props.remove(DataSourceFactory.JDBC_SERVER_NAME);
        clazz.getMethod("setServerName", String.class).invoke(ds, serverName);

        String user = (String) props.remove(DataSourceFactory.JDBC_USER);
        clazz.getMethod("setUser", String.class).invoke(ds, user);

        String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
        clazz.getMethod("setPassword", String.class).invoke(ds, password);
    }

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            DataSource ds = DataSource.class.cast(jtdsDataSourceClass.newInstance());
            setProperties(ds, jtdsDataSourceClass, props);
            return ds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props) throws SQLException {
        try {
            ConnectionPoolDataSource ds = ConnectionPoolDataSource.class.cast(jtdsConnectionPoolDataSourceClass.newInstance());
            setProperties(ds, jtdsXADataSourceClass, props);
            return ds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        try {
            XADataSource ds = XADataSource.class.cast(jtdsXADataSourceClass.newInstance());
            setProperties(ds, jtdsXADataSourceClass, props);
            return ds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        try {
					Driver driver = Driver.class.cast(jtdsDriverClass.newInstance());
            return driver;
        }
        catch (InstantiationException ex) {
            throw new SQLException(ex);
        }
        catch (IllegalAccessException ex) {
            throw new SQLException(ex);
        }
    }

}
