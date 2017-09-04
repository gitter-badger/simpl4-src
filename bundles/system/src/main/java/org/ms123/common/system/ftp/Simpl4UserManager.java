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

package org.ms123.common.system.ftp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.ftplet.UserManager;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 */
public class Simpl4UserManager implements  UserManager {
		private Map<String,Map<String,String>> userMap;
    /**
     */
    public Simpl4UserManager(Map<String,Map<String,String>> userMap) {
			this.userMap = userMap;
    }


    public User getUserByName(String username) throws FtpException{
			info(this, "getUserByName:"+username);
			Map<String,String> um = this.userMap.get(username);
			if( um != null){
				info(this, "getUserByName:"+username+" found");
				return new Simpl4User(um);
			}
			info(this, "getUserByName:"+username+" not found");
			return null;
		}

    public String[] getAllUserNames() throws FtpException{
			info(this, "getAllUserNames");
			return null;
		}

    public void delete(String username) throws FtpException{
			throw new RuntimeException("Simpl4UserManager.delete: not implemented");
		}

    public void save(User user) throws FtpException{
			throw new RuntimeException("Simpl4UserManager.save: not implemented");
		}

    public boolean doesExist(String username) throws FtpException{
			Map<String,String> um = userMap.get(username);
			return um != null ? true : false;
		}

		public User authenticate(Authentication authentication) throws AuthenticationFailedException{
			if (authentication instanceof UsernamePasswordAuthentication) {
				UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;

				String username = upauth.getUsername();
				String password = upauth.getPassword();

				info(this, "authenticate:"+username+","+password);
				if (username == null) {
					throw new AuthenticationFailedException("Authentication failed");
				}

				if (password == null) {
					password = "";
				}
				Map<String,String> umap = this.userMap.get(username);
				if( umap == null){
					throw new AuthenticationFailedException("Authentication failed:User not found");
				}
				String pw = umap.get("password");
				info(this, "password.authenticate(" + username + "," + password + ","+ pw+ ")");
				if( password.equals(pw)){
					return new Simpl4User( umap);
				}
				throw new AuthenticationFailedException("Authentication failed:Password wrong");
			}else{
				throw new AuthenticationFailedException("Only UsernamePasswordAuthentication allowed");
			}
		}

    public String getAdminName() throws FtpException{
			return "No Admin";
		}

    public boolean isAdmin(String username) throws FtpException{
			return false;
		}

}
