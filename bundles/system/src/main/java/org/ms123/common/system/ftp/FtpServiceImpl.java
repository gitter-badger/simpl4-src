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
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;

import org.ms123.common.auth.api.AuthService;
import org.ms123.common.namespace.NamespaceService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;

import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.io.FileUtils.readFileToString;

@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=ftp" })
public class FtpServiceImpl implements FtpService, FrameworkListener {
	int port = 2124;
	int dataport = 2125;
	int passvCount = 10;
	private JSONDeserializer ds = new JSONDeserializer();
	private JSONSerializer js = new JSONSerializer();

	private FtpServer ftpServer;
	private List<FtpFileEventListener> fileListeners = new ArrayList<FtpFileEventListener>();
	private BundleContext bundleContext;
	private Map<String, Map<String, String>> userMap = new HashMap<String, Map<String, String>>();

	public FtpServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bundleContext = bundleContext;
		bundleContext.addFrameworkListener(this);
	}

	public void frameworkEvent(FrameworkEvent event) {
		info(this, "FtpServiceImpl.frameworkEvent:" + event);
		if (event.getType() != FrameworkEvent.STARTED) {
			return;
		}
		_activate();
	}

	protected void _activate() {
		try {
			ftpServer = ftpServerStart();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected FtpServer ftpServerStart() throws Exception {
		AuthService authService = lookupServiceByClass(AuthService.class);
		NamespaceService nsService = lookupServiceByClass(NamespaceService.class);
		Map<String, String> branding = nsService.getBranding();
		String externalHost = branding.get("externalHost");
		info(this, "externalHost:" + externalHost);
		List<Map> userList = authService.getUserList();
		info(this, "userList:" + userList);
		for (Map<String, String> user : userList) {
			String homedir = user.get("ftphomedir");
			if (homedir != null) {
				String access = "rw";
				String a[] = homedir.split(",");
				if( a.length == 2){
					homedir = a[0];
					access = a[1];
				}
				Map<String, String> umap = new HashMap<String, String>();
				umap.put("homedir", Paths.get(System.getProperty("git.repos"), homedir).toString());
				umap.put("password", user.get("password"));
				umap.put("userid", user.get("userid"));
				umap.put("access", access);
				this.userMap.put(user.get("userid"), umap);
			}
		}
		info(this, "userMap:" + userMap);
		FtpServerFactory serverFactory = new FtpServerFactory();

		Map<String, Ftplet> ftplets = new LinkedHashMap<String, Ftplet>();
		ftplets.put("ftplet1", new MyFtplet());
		serverFactory.setFtplets(ftplets);

		ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setPort(port);

		DataConnectionConfigurationFactory dccFactory = new DataConnectionConfigurationFactory();
		int passivePort = dataport + 1;
		dccFactory.setPassivePorts(passivePort + "-" + (passivePort + passvCount));
		dccFactory.setPassiveExternalAddress(externalHost);
		dccFactory.setActiveLocalPort(dataport);
		listenerFactory.setDataConnectionConfiguration(dccFactory.createDataConnectionConfiguration());
		serverFactory.addListener("default", listenerFactory.createListener());

		Simpl4UserManagerFactory userManagerFactory = new Simpl4UserManagerFactory();
		userManagerFactory.setUserMap(this.userMap);
		UserManager um = userManagerFactory.createUserManager();
		serverFactory.setUserManager(um);

		FtpServer server = serverFactory.createServer();
		server.start();

		return server;
	}

	public boolean addFileEventListener(FtpFileEventListener listener) {
		return fileListeners.add(listener);
	}

	public boolean removeFileEventListener(FtpFileEventListener listener) {
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

	private class MyFtplet extends DefaultFtplet {

		@Override
		public FtpletResult onUploadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
			info(this, "onUploadEnd("+session.getUser().getName()+"):" + request.getCommand()+ " -> " + request.getArgument());
			String username = session.getUser().getName();
			Map<String,String> umap = userMap.get(username);
			Path homedir = Paths.get(umap.get("homedir"));
			Map<String,Object> args = new HashMap<String,Object>();
			args.put("fromFtp", true );
			for( FtpFileEventListener l : fileListeners){
				l.fileCreated( username, Paths.get(request.getArgument()), homedir, args);
			}
			return FtpletResult.DEFAULT;
		}
	}
}

