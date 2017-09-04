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
package org.ms123.common.git;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.*;


/**
 * Includes tree entries only if they match the configured type.
 */
public class TypeFilter extends TreeFilter {
	private final List<String> m_typeList;
	private File m_basedir;

	/**
	 */
	public static TypeFilter create(File base,List<String> typeList) {
		if (typeList.size() == 0) {
			throw new IllegalArgumentException(JGitText.get().emptyPathNotPermitted);
		}
		return new TypeFilter(base,typeList);
	}

	private TypeFilter(File basedir,final List<String> typeList) {
		if (typeList.size() == 0) {
			throw new IllegalArgumentException(JGitText.get().cannotMatchOnEmptyString);
		}
		m_typeList = typeList;
		m_basedir = basedir;
	}

	@Override
	public TreeFilter clone() {
		return this;
	}

	@Override
	public boolean include(TreeWalk walker) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		String path = walker.getPathString();
		File file = new File(m_basedir,path);
		FileHolder fr = new FileHolder( new File(m_basedir,path) );
		try{
			String type = GitServiceImpl._getFileType(file);
			boolean b = m_typeList.contains(type);
			return b;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean shouldBeRecursive() {
		return true;
	}

}
