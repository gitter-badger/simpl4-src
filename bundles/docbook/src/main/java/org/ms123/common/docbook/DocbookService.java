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
