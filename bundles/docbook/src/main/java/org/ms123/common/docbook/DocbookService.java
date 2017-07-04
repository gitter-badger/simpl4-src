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
package org.ms123.common.docbook;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
import javax.servlet.http.*;
import org.ms123.common.rpc.RpcException;

public interface DocbookService {
	public void jsonToPdf(String namespace,String json, Map<String,Object> params, OutputStream os) throws Exception;
	public void jsonToPdf(String namespace,InputStream is, Map<String,Object> params, OutputStream os) throws Exception;

	public void wawiToPdf(String namespace,String json, Map<String,Object> params, OutputStream os) throws Exception;
	public String wawiToFo( String namespace,String json, Map<String, Object> params ) throws Exception;

	public void jsonToDocbook( String namespace, String jsonName, Map<String, Object> params,Map<String,String> paramsOut, OutputStream os ) throws Exception;
	public void jsonToDocbook( String namespace, InputStream is, Map<String, Object> params,Map<String,String> paramsOut, OutputStream os ) throws Exception;

	public void docbookToPdf(String namespace, InputStream is, Map<String, String> params, OutputStream os) throws Exception;

	public void website( String namespace, String name, HttpServletRequest request, HttpServletResponse response) throws RpcException;
	public void getAsset( String namespace, String name, String type, HttpServletRequest request, HttpServletResponse response) throws RpcException;
	public void adocToHtml( File adocFile, Writer w) throws Exception;
	public String adocToHtml( String adoc) throws Exception;
	public void adocToHtml( File adocFile, Writer w,Map params) throws Exception;

	public void adocToDocbook( File adocFile, Writer w) throws Exception;
	public String adocToDocbook( String adoc ) throws Exception;
	public void saveStructure( String ns, String path, String data ) throws Exception;

	public String  loadContent(String namespace, String target, String type);
}
