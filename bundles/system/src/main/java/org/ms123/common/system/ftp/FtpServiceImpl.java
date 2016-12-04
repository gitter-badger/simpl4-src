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
package org.ms123.common.system.ftp;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;

import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ftplet.UserManager;

import org.osgi.framework.ServiceReference;
import org.ms123.common.auth.api.AuthService;
import static org.apache.commons.io.FileUtils.readFileToString;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.FrameworkEvent;

import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=ftp" })
public class FtpServiceImpl implements FtpService, FrameworkListener {
	int port = 2124;
	int passvCount = 10;
	private JSONDeserializer ds = new JSONDeserializer();
	private JSONSerializer js = new JSONSerializer();

	private FtpServer ftpServer;
  private List<FtpFileEventListener> fileListeners = new ArrayList<FtpFileEventListener>();
	private BundleContext bundleContext;
	private Map<String,Map<String,String>> userMap = new HashMap<String,Map<String,String>>();

	public FtpServiceImpl() {
	}
	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bundleContext = bundleContext;
		bundleContext.addFrameworkListener(this);
	}

	public void frameworkEvent(FrameworkEvent event) {
		info(this,"FtpServiceImpl.frameworkEvent:"+event);
		if( event.getType() != FrameworkEvent.STARTED){
			return; 
		}
		_activate();
	}
	protected void _activate() {
		try{
			ftpServer = ftpServerStart();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	protected FtpServer ftpServerStart() throws Exception{
		AuthService authService = lookupServiceByClass(AuthService.class);
		List<Map> userList = authService.getUserList();
		info(this,"userList:"+userList);
		for( Map<String,String> user : userList){
			String homedir = user.get("homedir");
			if( homedir != null){
				Map<String,String> umap = new HashMap<String,String>();
				umap.put( "homedir", Paths.get(System.getProperty("git.repos"), homedir).toString());
				umap.put( "password", user.get("password"));
				umap.put( "userid", user.get("userid"));
				this.userMap.put( user.get("userid"), umap );
			}
		}
		info(this,"userMap:"+userMap);
		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setPort(port);

		DataConnectionConfigurationFactory dccFactory = new DataConnectionConfigurationFactory();
		int passivePort = port+1;
		dccFactory.setPassivePorts(passivePort + "-" + (passivePort + passvCount));
		listenerFactory.setDataConnectionConfiguration(dccFactory .createDataConnectionConfiguration());
		serverFactory.addListener("default", listenerFactory.createListener());

		Simpl4UserManagerFactory userManagerFactory = new Simpl4UserManagerFactory();
		userManagerFactory.setUserMap(this.userMap);
		UserManager um = userManagerFactory.createUserManager();
		serverFactory.setUserManager(um);

		FtpServer server = serverFactory.createServer();         
		server.start();

		return server;
	}

	public boolean addFileEventListener(FtpFileEventListener listener){
		return fileListeners.add(listener);
	}

	public boolean removeFileEventListener(FtpFileEventListener listener){
		return fileListeners.remove(listener);
	}

	private <T> T lookupServiceByClass(Class<T> clazz) {
		T service = null;
		ServiceReference sr = this.bundleContext.getServiceReference(clazz);
		if (sr != null) {
			service = (T) this.bundleContext.getService(sr);
		}
		if (service == null) {
		}
		return service;
	}

	private String getString(String key, String def) {
		return def;
	}

	public void update(Map<String, Object> props) {
		info(this, "FtpServiceImpl.updated:" + props);
	}

	protected void deactivate() throws Exception {
		info(this, "FtpServiceImpl.deactivate");
		ftpServer.stop();
	}
	private static  class ClearTextPasswordEncryptor implements PasswordEncryptor {
		public String encrypt(String password) {
			info(this, "encrypt:" + password );
			return password;
		}

		public boolean matches(String passwordToCheck, String storedPassword) {
			info(this, "matches:" + passwordToCheck + " -> " +storedPassword );
			return true;
		}
	}

}

