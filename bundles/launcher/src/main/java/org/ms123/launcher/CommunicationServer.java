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

import java.util.Arrays;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import org.cojen.dirmi.Environment;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class CommunicationServer {
	private int port;
	public CommunicationServer(int p){
		this.port = p;
		init();
	}

public interface RemoteExample extends Remote {
     String talk(String name) throws RemoteException;
 }

public class RemoteExampleServer implements RemoteExample{
 public String talk(String name) {
         return "Hello " + name;
     }
}

	public void init() {
		try{
System.out.println("Dirmi.start");
			Environment env = new Environment(new ScheduledThreadPoolExecutor(3));
			RemoteExample server = new RemoteExampleServer();
			env.newSessionAcceptor(new InetSocketAddress("127.0.0.1", 11112)).acceptAll(server);
System.out.println("Dirmi.started");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
