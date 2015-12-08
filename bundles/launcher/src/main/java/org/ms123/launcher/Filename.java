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

public class Filename {
	private String fullPath;
	private char pathSeparator, extensionSeparator;

	public Filename(String str, char sep, char ext) {
		fullPath = str;
		pathSeparator = sep;
		extensionSeparator = ext;
	}

	public String extension() {
		int dot = fullPath.lastIndexOf(extensionSeparator);
		return fullPath.substring(dot + 1);
	}

	public String filename() { // gets filename without extension
		int dot = fullPath.lastIndexOf(extensionSeparator);
		int sep = fullPath.lastIndexOf(pathSeparator);
		return fullPath.substring(sep + 1, dot);
	}

	public String path() {
		int sep = fullPath.lastIndexOf(pathSeparator);
		return fullPath.substring(0, sep);
	}
}

