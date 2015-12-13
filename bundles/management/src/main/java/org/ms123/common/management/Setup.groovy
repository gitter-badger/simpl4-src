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

/**
 *
 */
@SuppressWarnings("unchecked")
@groovy.transform.CompileStatic
@groovy.transform.TypeChecked
class Setup{
	private Setup(){
	}

	static void doSetup(List<String> createdNamespaces){
		String simpl4Dir = (String) System.getProperty("simpl4.dir");
		File loggingConfig = new File(simpl4Dir, "etc/logging.config");
		boolean firstRun = false;
		if( !loggingConfig.exists()){
			firstRun = true;
			String loggingConfigTpl = new File(simpl4Dir, "etc/logging.config.tpl").toString();
			info("doSetup.loggingConfigTpl:"+loggingConfigTpl);
			String basedir = simpl4Dir.replaceAll("\\\\", "/");
			info("doSetup.basedir:"+basedir);
			File logDir = new File(simpl4Dir,"log");
			if( !logDir.exists()){
				logDir.mkdir();
			}
			info("doSetup.logDir:"+logDir);
			Unix4j.cat(loggingConfigTpl).sed("s!_BASEDIR_!"+basedir+"!g").
				sed("s!_LOGDIR_!"+logDir.toString().replaceAll("\\\\", "/")+"!g").toFile(loggingConfig);

			File logBackTpl = new File(simpl4Dir, "etc/logback.xml.tpl");
			File logBack = new File(simpl4Dir,"etc/logback.xml");
			Unix4j.cat(logBackTpl).sed("s!_BASEDIR_!"+basedir+"!g").
				sed("s!_LOGDIR_!"+logDir.toString().replaceAll("\\\\", "/")+"!g").toFile(logBack);

			File logConfigTpl = new File(simpl4Dir, "etc/config/org/ops4j/pax/logging.config.tpl");
			File logConfig = new File(simpl4Dir,"etc/config/org/ops4j/pax/logging.config");
			Unix4j.cat(logConfigTpl).sed("s!_BASEDIR_!"+basedir+"!g").toFile(logConfig);
		}

		String vardir = (String) System.getProperty("tpso.web.vardir");
		if( vardir == null){
			vardir = simpl4Dir;
		}

		File varSimpl4Dir = new File(vardir, "simpl4");
		info("doSetup.varSimpl4Dir.exists:"+varSimpl4Dir.exists());
		info("doSetup.firstRun:"+firstRun);
		if( firstRun ){
			if( vardir != null){
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
		info("bundledrepos.exists:"+bundledRepos.exists());
		if( bundledRepos.exists()){
			File gitRepos = new File(varSimpl4Dir, "gitrepos");
			File tempDir  = new File(varSimpl4Dir, "tmp");
			if( bundledRepos.exists()){
				try{
					List<String> files = Unix4j.cd(bundledRepos).ls().toStringList();
					info("files:"+files);
					for( String file : files){
						String arch = "file:"+bundledRepos.toString()+"/"+file;
						URI zipUri=null;
						try{
							zipUri = new URI(arch.replaceAll("\\\\", "/"));;
							info("zipUri:"+zipUri);
							ZipfileUnpacker zfu = new ZipfileUnpacker( zipUri);
							zfu.unpack( tempDir);
							Filename fn = new Filename(arch, (char)'/', (char)'.' );
							createdNamespaces.add(fn.filename());	
						}catch(Exception e){
							info("unpack:"+zipUri+":"+e);
							e.printStackTrace();
						}
					}
					for( String name : createdNamespaces){
						File tmpRepo = new File(tempDir, name);
						File destRepo = new File(gitRepos, name);
						if( destRepo.exists()){
							info("deleteDirectory:"+destRepo);
							FileUtils.deleteDirectory(destRepo);
						}
						info("moveDirectory:"+tmpRepo+" -> "+ new File(gitRepos,name));
						FileUtils.moveDirectory(tmpRepo, new File(gitRepos, name));
					}
					for( String name : createdNamespaces){
						File dataRepo = new File(gitRepos, name+"_data");
						if( dataRepo.exists()){
							continue;
						}
						InitCommand ic = Git.init();
						info("jgit.init:"+dataRepo);
						ic.setDirectory(dataRepo);
						try{
							ic.call();
						}catch(Exception e){
							info("data_repo_create("+dataRepo+"):"+e);
							e.printStackTrace();
						}
					}
					info("deleteDirectory:"+bundledRepos);
					FileUtils.deleteDirectory(bundledRepos);
					info("deleteDirectory:"+tempDir);
					FileUtils.deleteDirectory(tempDir);
				}catch(Exception e){
					info("error.copyBundledRepos:"+e);
					e.printStackTrace();
					throw new RuntimeException("OsgiStarter.createGitRepos:",e);
				}finally{
				}
			}
		}
	}
	protected static void info(String msg) {
		System.err.println(msg);
		m_logger.info(msg);
	}
	protected static void debug(String msg) {
		m_logger.debug(msg);
	}

	private static final Logger m_logger = LoggerFactory.getLogger(Setup.class);
}
