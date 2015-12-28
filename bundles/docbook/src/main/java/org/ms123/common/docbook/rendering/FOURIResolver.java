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
package org.ms123.common.docbook.rendering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.*;
import java.util.*;
import java.net.URI;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParserFactory;
import com.ctc.wstx.sax.*;
import com.ctc.wstx.api.ReaderConfig;
import org.ms123.common.git.GitService;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.apache.xmlgraphics.io.Resource;

public class FOURIResolver implements ResourceResolver {

	protected GitService m_gitService;

	protected String m_namespace;
	protected String simpl4Dir;

	public FOURIResolver(GitService gs, String namespace) {
		m_gitService = gs;
		m_namespace = namespace;
		simpl4Dir = (String) System.getProperty("simpl4.dir");
	}

	private static final Logger log = LoggerFactory.getLogger(FOURIResolver.class);

	private String docbookXslBase;

	public Resource getResource(URI href) throws IOException {
		System.out.println("[FOURIResolver.resolve: href=" + href + "]");
		String path = href.toString();
		if( path.startsWith("file:/")){
			
			path = href.toString().toLowerCase().substring(("file:"+this.simpl4Dir+"/server/").length());
		}
		String namespace = m_namespace;
		int delim = path.indexOf("!");
		if( delim > 0){
			namespace = path.substring(0,delim);
			path = path.substring(delim+1);
		}
		String file = path.startsWith("repo:") ? path.substring(5) : path;
		String type = null;
		if (path.endsWith(".svg")) {
			type = "image/svg+xml";
		}
		if (path.endsWith(".png")) {
			type = "image/png";
		}
		if (path.endsWith(".jpg")) {
			type = "image/jpg";
		}
		if (path.endsWith(".jepg")) {
			type = "image/jpg";
		}
		if (path.endsWith(".swf")) {
			type = "image/swf";
		}
		if (path.endsWith(".pdf")) {
			type = "image/pdf";
		}
		try {
			System.out.println("FOURIResolver.searchFile:"+namespace+"|"+file );
			File _file = m_gitService.searchFile(namespace, file, type);
			return new Resource(type, new FileInputStream(_file));
		} catch (Exception e) {
			System.out.println("FOURIResolver.getResource:("+href+"):"+e);
		}
		return null;
	}
	public OutputStream getOutputStream(URI uri) throws IOException{
		throw new RuntimeException("ResourceResolver.getOutputStream:not implemented");
	}
}
