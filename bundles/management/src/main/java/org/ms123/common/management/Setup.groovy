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
package org.ms123.common.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.util.*;
import org.eclipse.jgit.lib.*;
import org.unix4j.Unix4j;
import org.unix4j.builder.Unix4jCommandBuilder;
import org.unix4j.io.Output;
import org.unix4j.io.StreamOutput;
import org.unix4j.unix.Ls;
import org.unix4j.unix.Sort;
import org.unix4j.unix.grep.GrepOption;
import org.unix4j.variable.Arg;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;

/**
 *
 */
@SuppressWarnings("unchecked")
@groovy.transform.CompileStatic
@groovy.transform.TypeChecked
class Setup{
	private Setup(){
	}

	static void doSetup(List<String> createdNamespaces,boolean firstRun){
		String simpl4Dir = (String) System.getProperty("simpl4.dir");
		String vardir = getVarDir();
		if( vardir == null){
			vardir = simpl4Dir;
		}

		File varSimpl4Dir = new File(vardir, "simpl4");
		info(Setup.class, "doSetup.vardir:"+vardir);
		info(Setup.class, "doSetup.varSimpl4Dir.exists:"+varSimpl4Dir.exists());
		info(Setup.class, "doSetup.firstRun:"+firstRun);
		if( firstRun ){
			if( !vardir.equals(simpl4Dir) ){
				varSimpl4Dir.mkdirs();
				try{
					FileUtils.copyDirectory(new File(simpl4Dir,"workspace"), new File(varSimpl4Dir,"workspace"));
					FileUtils.copyDirectory(new File(simpl4Dir,"gitrepos"), new File(varSimpl4Dir,"gitrepos"));
				}catch(Exception e){
					e.printStackTrace();
					return;
				}
			}
		}

		File bundledRepos = new File(simpl4Dir, "bundledrepos");
		info(Setup.class, "bundledrepos.exists:"+bundledRepos.exists());
		if( bundledRepos.exists()){
			File gitRepos = new File(varSimpl4Dir, "gitrepos");
			File tempDir  = new File(varSimpl4Dir, "tmp");
			if( bundledRepos.exists()){
				try{
					List<String> files = Unix4j.cd(bundledRepos).ls().toStringList();
					info(Setup.class, "files:"+files);
					for( String file : files){
						String arch = "file:"+bundledRepos.toString()+"/"+file;
						URI zipUri=null;
						try{
							zipUri = new URI(arch.replaceAll("\\\\", "/"));;
							info(Setup.class, "zipUri:"+zipUri);
							ZipfileUnpacker zfu = new ZipfileUnpacker( zipUri);
							zfu.unpack( tempDir);
							Filename fn = new Filename(arch, (char)'/', (char)'.' );
							createdNamespaces.add(fn.filename());
						}catch(Exception e){
							error(Setup.class, "unpack:"+zipUri+":%[exception]s",e);
							e.printStackTrace();
						}
					}
					for( String name : createdNamespaces){
						File tmpRepo = new File(tempDir, name);
						File destRepo = new File(gitRepos, name);
						if( destRepo.exists()){
							error(Setup.class, "deleteDirectory:"+destRepo);
							FileUtils.deleteDirectory(destRepo);
						}
						info(Setup.class, "moveDirectory:"+tmpRepo+" -> "+ new File(gitRepos,name));
						FileUtils.moveDirectory(tmpRepo, new File(gitRepos, name));
					}
					for( String name : createdNamespaces){
						File dataRepo = new File(gitRepos, name+"_data");
						if( dataRepo.exists()){
							continue;
						}
						InitCommand ic = Git.init();
						info(Setup.class, "jgit.init:"+dataRepo);
						ic.setDirectory(dataRepo);
						try{
							ic.call();
						}catch(Exception e){
							error(Setup.class, "data_repo_create("+dataRepo+"):%[exception]s",e);
							e.printStackTrace();
						}
					}
					info(Setup.class, "deleteDirectory:"+bundledRepos);
					FileUtils.deleteDirectory(bundledRepos);
					info(Setup.class, "deleteDirectory:"+tempDir);
					FileUtils.deleteDirectory(tempDir);
				}catch(Exception e){
					error(Setup.class, "error.copyBundledRepos:%[exception]s",e);
					e.printStackTrace();
					throw new RuntimeException("OsgiStarter.createGitRepos:",e);
				}finally{
				}
			}
		}
	}
	private static String getVarDir(){
		String varDir = (String) System.getProperty("tpso.web.vardir");
		if( varDir == null){
			return (String) System.getProperty("simpl4.vardir");
		}
		return varDir;
	}
}

