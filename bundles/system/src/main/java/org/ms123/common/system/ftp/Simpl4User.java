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

import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import java.util.List;
import java.util.Map;

/**
 */
public class Simpl4User extends BaseUser {
		private Map<String,String> user;
		public Simpl4User( Map<String,String> u ){
			this.user = u;
		}

    public String getName(){
			return this.user.get("userid");
		}

    public String getPassword(){
			return this.user.get("password");
		}


    public String getHomeDirectory(){
			return this.user.get("homedir");
		}
}
