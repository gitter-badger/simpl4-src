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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;
import static com.jcabi.log.Logger.info;

/**
 */

public class BaseUser implements User {

	private String name = null;
	private String password = null;
	private int maxIdleTimeSec = 0; // no limit
	private String homeDir = null;
	private boolean isEnabled = true;

	private List<? extends Authority> authorities = new ArrayList<Authority>();

	public BaseUser() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String pass) {
		password = pass;
	}

	public List<Authority> getAuthorities() {
		if (authorities != null) {
			return Collections.unmodifiableList(authorities);
		} else {
			return null;
		}
	}

	public void setAuthorities(List<Authority> authorities) {
		if (authorities != null) {
			this.authorities = Collections.unmodifiableList(authorities);
		} else {
			this.authorities = null;
		}
	}

	public int getMaxIdleTime() {
		return maxIdleTimeSec;
	}

	public void setMaxIdleTime(int idleSec) {
		maxIdleTimeSec = idleSec;
		if (maxIdleTimeSec < 0) {
			maxIdleTimeSec = 0;
		}
	}

	public boolean getEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean enb) {
		isEnabled = enb;
	}

	public String getHomeDirectory() {
		return homeDir;
	}

	public void setHomeDirectory(String home) {
		homeDir = home;
	}

	@Override
	public String toString() {
		return name;
	}

	public AuthorizationRequest authorize(AuthorizationRequest request) {
		return request;
	}

	public List<Authority> getAuthorities(Class<? extends Authority> clazz) {
		List<Authority> selected = new ArrayList<Authority>();

		for (Authority authority : authorities) {
			if (authority.getClass().equals(clazz)) {
				selected.add(authority);
			}
		}

		return selected;
	}
}

