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
package org.ms123.common.system.ssh;

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

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.file.nativefs.NativeFileSystemFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.common.scp.ScpTransferEventListener;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.apache.sshd.server.subsystem.sftp.SftpEventListener;
import org.apache.sshd.server.subsystem.sftp.AbstractSftpEventListenerAdapter;
import org.apache.sshd.server.subsystem.sftp.Handle;
import org.apache.karaf.shell.ssh.OpenSSHGeneratorFileKeyProvider;
import org.apache.karaf.shell.ssh.UserAuthFactoriesFactory;
import org.apache.karaf.shell.ssh.KarafJaasAuthenticator;
import org.apache.karaf.shell.ssh.ShellFactoryImpl;
import org.apache.karaf.shell.ssh.ShellCommandFactory;
import org.apache.karaf.shell.ssh.SshUtils;
import org.apache.karaf.shell.ssh.SshAction;
import org.apache.karaf.shell.api.action.lifecycle.Manager;
import org.apache.karaf.shell.ssh.KarafAgentFactory;
import org.osgi.framework.ServiceReference;
import org.ms123.common.auth.api.AuthService;
import org.ms123.common.system.tm.TransactionService;
import org.apache.karaf.shell.api.console.SessionFactory;
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
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=ssh" })
public class SshServiceImpl implements SshService, FrameworkListener {
	int port = 2122;
	private static final String KNOWN_HOSTS = "known_hosts";
	private JSONDeserializer ds = new JSONDeserializer();
	private JSONSerializer js = new JSONSerializer();

	private SshServer sshServer;
	private SftpSubsystemFactory sftpSubsystemFactory;
	private ScpCommandFactory scpCommandFactory;
  private List<SshFileEventListener> fileListeners = new ArrayList<SshFileEventListener>();
	private BundleContext bundleContext;
	private Map<String,Map<String,String>> userHomeMap = new HashMap<String,Map<String,String>>();

	public SshServiceImpl() {
	}
	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bundleContext = bundleContext;
		bundleContext.addFrameworkListener(this);
	}

	public void frameworkEvent(FrameworkEvent event) {
		info(this,"SshServiceImpl.frameworkEvent:"+event);
		if( event.getType() != FrameworkEvent.STARTED){
			return; 
		}
		_activate();
	}
	protected void _activate() {
		if (Boolean.parseBoolean(bundleContext.getProperty("karaf.startRemoteShell"))) {
			SessionFactory sessionFactory = null;
			ServiceReference sr = bundleContext.getServiceReference(SessionFactory.class);
			if (sr != null) {
				sessionFactory = (SessionFactory) bundleContext.getService(sr);
				sessionFactory.getRegistry().getService(Manager.class).register(SshAction.class);
			}
			info(this, "SessionFactory:" + sessionFactory);
			sshServer = sshServerStart(sessionFactory);
			//this.bundleContext.registerService(SshServer.class, sshServer, null);
			if (sshServer == null) {
				return; // can result from bad specification.
			}
			try {
				sshServer.start();
			} catch (IOException e) {
				error(this, "Exception caught while starting SSH server:%[exception]s", e);
			}
		}
	}

	protected SshServer sshServerStart(SessionFactory sessionFactory) {
		AuthService authService = lookupServiceByClass(AuthService.class);
		KarafAgentFactory agentFactory = new KarafAgentFactory();
		int sshPort = port;
		String sshHost = getString("sshHost", "0.0.0.0");
		long sshIdleTimeout = 1800000;
		String sshRealm = getString("sshRealm", "karaf");
		String hostKey = getString("hostKey", System.getProperty("etc.dir") + "/host.key");
		String hostKeyFormat = getString("hostKeyFormat", "simple");
		String authMethods = getString("authMethods", "keyboard-interactive,password,publickey");
		int keySize = 4096;
		String algorithm = getString("algorithm", "RSA");
		String macs = getString("macs", "hmac-sha2-512,hmac-sha2-256,hmac-sha1");
		String ciphers = getString("ciphers", "aes128-ctr,arcfour128,aes128-cbc,3des-cbc,blowfish-cbc");
		String kexAlgorithms = getString("kexAlgorithms", "diffie-hellman-group-exchange-sha256,ecdh-sha2-nistp521,ecdh-sha2-nistp384,ecdh-sha2-nistp256,diffie-hellman-group-exchange-sha1,diffie-hellman-group1-sha1");
		String welcomeBanner = getString("welcomeBanner", null);
		String moduliUrl = getString("moduli-url", null);

		AbstractGeneratorHostKeyProvider keyPairProvider;
		if ("simple".equalsIgnoreCase(hostKeyFormat)) {
			keyPairProvider = new SimpleGeneratorHostKeyProvider();
		} else if ("PEM".equalsIgnoreCase(hostKeyFormat)) {
			keyPairProvider = new OpenSSHGeneratorFileKeyProvider();
		} else {
			error(this, "Invalid host key format " + hostKeyFormat);
			return null;
		}

		keyPairProvider.setPath(Paths.get(hostKey));
		if (new File(hostKey).exists()) {
			keyPairProvider.setOverwriteAllowed(true);
		} else {
			keyPairProvider.setKeySize(keySize);
			keyPairProvider.setAlgorithm(algorithm);
		}
		keyPairProvider.setAlgorithm(algorithm);


		UserAuthFactoriesFactory authFactoriesFactory = new UserAuthFactoriesFactory();
		authFactoriesFactory.setAuthMethods(authMethods);

		SshServer server = SshServer.setUpDefaultServer();
		server.setPort(sshPort);
		server.setHost(sshHost);
		server.setMacFactories(SshUtils.buildMacs(macs));
		server.setCipherFactories(SshUtils.buildCiphers(ciphers));
		server.setKeyExchangeFactories(SshUtils.buildKexAlgorithms(kexAlgorithms));
		//	server.setShellFactory(new ShellFactoryImpl(sessionFactory));
		scpCommandFactory = new ScpCommandFactory.Builder().withDelegate(new ShellCommandFactory(sessionFactory)).build();
		server.setCommandFactory(scpCommandFactory);
		sftpSubsystemFactory = new SftpSubsystemFactory();
		server.setSubsystemFactories(Arrays.<NamedFactory<org.apache.sshd.server.Command>> asList(sftpSubsystemFactory));
		server.setKeyPairProvider(keyPairProvider);
		//KarafJaasAuthenticator authenticator = new KarafJaasAuthenticator(sshRealm);
		//server.setPasswordAuthenticator(authenticator);
		//server.setPublickeyAuthenticator(authenticator);

		server.setPasswordAuthenticator(new PasswordAuthenticator() {
			@Override
			public boolean authenticate(String username, String password, ServerSession session) {
				Map<String,String> umap = userHomeMap.get(username);
				String pw = umap.get("password");
				info(this, "password.authenticate(" + username + "," + password + ","+ pw+ ")");
				return password != null && password.equals(pw);
			}
		});
		server.setPublickeyAuthenticator(new PublickeyAuthenticator() {
			@Override
			public boolean authenticate(String username, PublicKey key, ServerSession session) {
				info(this, "pubkey.authenticate(" + username + "," + key + ")");
				return false;
			}
		});

		
		VirtualFileSystemFactory vfs = new VirtualFileSystemFactory(Paths.get(System.getProperty("git.repos")));
		List<Map> userList = authService.getUserList();
		info(this,"userList:"+userList);
		for( Map<String,String> user : userList){
			String homedir = user.get("homedir");
			if( homedir != null){
				vfs.setUserHomeDir(user.get("userid"), Paths.get(System.getProperty("git.repos"), homedir));
				Map<String,String> umap = new HashMap<String,String>();
				umap.put( "homedir", Paths.get(System.getProperty("git.repos"), homedir).toString());
				umap.put( "password", user.get("password"));
				userHomeMap.put( user.get("userid"), umap );
			}
		}

		server.setFileSystemFactory(vfs);
		server.setUserAuthFactories(authFactoriesFactory.getFactories());
		server.setAgentFactory(agentFactory);
		server.getProperties().put(SshServer.IDLE_TIMEOUT, Long.toString(sshIdleTimeout));
		if (moduliUrl != null) {
			server.getProperties().put(SshServer.MODULI_URL, moduliUrl);
		}
		if (welcomeBanner != null) {
			server.getProperties().put(SshServer.WELCOME_BANNER, welcomeBanner);
		}
		SftpEventListener evl = new AbstractSftpEventListenerAdapter(){
			public void close(ServerSession session, String remoteHandle, Handle localHandle){
				info(this, "remoteHandle:"+remoteHandle+"|"+ localHandle.getFile());
				String username = session.getUsername();
				Map<String,String> umap = userHomeMap.get(username);
				Path homedir = Paths.get(umap.get("homedir"));
				Path file = localHandle.getFile();
				for( SshFileEventListener l : fileListeners){
					l.fileCreated( username, file, homedir, null);
				}
			}
		};
		sftpSubsystemFactory.addSftpEventListener(evl);

		scpCommandFactory.addEventListener(new ScpTransferEventListener() {
			@Override
			public void startFileEvent(Session sess, FileOperation fileOperation, Path path, long l, Set<PosixFilePermission> set) {
				info(this,"startFileEvent("+sess+") (" + (fileOperation == FileOperation.SEND ? "SEND" : "RECEIVE") + ") " + path);
			}

			@Override
			public void startFolderEvent(Session sess, FileOperation fileOperation, Path path, Set<PosixFilePermission> set) {
				info(this,"startFolderEvent (" + (fileOperation == FileOperation.SEND ? "SEND" : "RECEIVE") + ") " + path);
			}

			@Override
			public void endFileEvent(Session sess, FileOperation fileOperation, Path path, long length, Set<PosixFilePermission> set, Throwable throwable) {
				info(this,"endFileEvent("+sess+") ("+ sess.getUsername()+") (" + (fileOperation == FileOperation.SEND ? "SEND" : "RECEIVE") + ") " + path);
				String username = sess.getUsername();
				Map<String,String> umap = userHomeMap.get(username);
				Path homedir = Paths.get(umap.get("homedir"));
				if( homedir == null){
					homedir = Paths.get("/");
				}
				for( SshFileEventListener l : fileListeners){
					l.fileCreated( username, path, homedir, null);
				}
			}

			@Override
			public void endFolderEvent(Session sess, FileOperation fileOperation, Path path, Set<PosixFilePermission> set, Throwable throwable) {
				info(this,"endFolderEvent (" + (fileOperation == FileOperation.SEND ? "SEND" : "RECEIVE") + ") " + path);
			}
		});

		return server;
	}

	public boolean addFileEventListener(SshFileEventListener listener){
		return fileListeners.add(listener);
	}

	public boolean removeFileEventListener(SshFileEventListener listener){
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
		info(this, "SshServiceImpl.updated:" + props);
	}

	protected void deactivate() throws Exception {
		info(this, "SshServiceImpl.deactivate");
		sshServer.stop();
	}
}

