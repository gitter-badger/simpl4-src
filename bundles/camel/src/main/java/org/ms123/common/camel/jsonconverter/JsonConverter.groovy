/*
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
package org.ms123.common.camel.jsonconverter;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.model.ExpressionSubElementDefinition;
import org.apache.camel.model.WhenDefinition;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.impl.DefaultRouteContext;
import org.apache.camel.Expression;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import groovy.text.SimpleTemplateEngine;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import groovy.lang.Script;
import java.net.URLEncoder;
import java.net.URL;
import java.net.InetAddress;
import org.apache.camel.model.language.*;
import org.apache.camel.util.IntrospectionSupport;
import static org.apache.camel.util.ObjectHelper.isEmpty;
import static org.apache.camel.util.ObjectHelper.isNotEmpty;
import org.ms123.common.libhelper.Utils;
import flexjson.*;
import java.util.Collection;
import javax.xml.namespace.QName;
import org.ms123.common.system.compile.java.JavaCompiler;
import org.osgi.framework.BundleContext;
import static org.apache.commons.io.FileUtils.readFileToString;

class JsonConverterVisitor {
	def m_ctx;
	void visit(JsonConverter j) {
		j.convertToCamel(m_ctx);
		j.children.each { visit(it) }
	}
	void visit(MessageChoiceJsonConverter j) {
		j.convertToCamel(m_ctx);
		j.children.each { visit(it) }
		j.finishToCamel(m_ctx);
	}
	/*void visit(MessageAggregateJsonConverter j) {
		j.convertToCamel(m_ctx);
		j.children.each { visit(it) }
		j.finishToCamel(m_ctx);
	}*/
}

interface JsonConverter {
	def getChildren()
	void convertToCamel(ctx);
	void finishToCamel(ctx);
}

abstract class JsonConverterImpl implements JsonConverter{
	public def  rootProperties;
	public def  shapeProperties;
	public def  resourceId;
	public def  branding;
	public def  bundleContext;;
	def children = []
	def engine = new SimpleTemplateEngine();
	def serializer = new JSONSerializer();
	def deSerializer = new JSONDeserializer();
	void finishToCamel(ctx){}
	def constructUri(ctx){
		def uriValueMap = createMap(ctx,"urivalue_");
		def uriParamMap = createMap(ctx,"uriparam_");
		def extraParams = shapeProperties.extraParams;
		if( extraParams != null && extraParams.totalCount>0){
			def items = extraParams.items;
			items.each(){item->
				if( isNotEmpty(item.name)  && isNotEmpty(item.value)){
					uriParamMap[item.name] = item.value;
				}
			}
		}
		def uri=shapeProperties.uri;
		if( shapeProperties.uri_template ){
			uri = engine.createTemplate(shapeProperties.uri_template).make(uriValueMap).toString();
		}
		uri = ctx.buildEnvSubstitutor.replace( uri );
		if( uriParamMap.size() > 0){
			def delim = "?";
			uriParamMap.each(){key,value->
				if( isNotEmpty(value)){
					uri+=delim+key+"=RAW("+value+")";
					delim = "&";
				}
			}
		}
		return uri;
	}
	def createExpression(exprText, language){
		def expr=null;
		if(language == "simple"){
			expr = org.apache.camel.builder.SimpleBuilder.simple(exprText);
		}else if(language == "constant"){
			expr = new ConstantExpression(exprText);
		}else if(language == "groovy"){
			expr = new GroovyExpression(exprText);
		}else if(language == "header"){
			expr = new HeaderExpression(exprText);
	//	}else if(language == "property"){
	//		expr = new PropertyExpression(exprText);
		}else if(language == "el"){
			expr = new ELExpression(exprText);
		}else if(language == "ognl"){
			expr = new OgnlExpression(exprText);
		}else if(language == "javascript"){
			expr = new JavaScriptExpression(exprText);
		}else if(language == "xpath"){
			expr = new XPathExpression(exprText);
		}else if(language == "sql"){
			expr = new SqlExpression(exprText);
		}else if(language == "mvel"){
			expr = new MvelExpression(exprText);
		}else if(language == "bean"){
			def m = exprText.split(",");
			if( m.length == 2){
				expr = new MethodCallExpression(m[0],m[1]);
			}else{
				expr = new MethodCallExpression(exprText);
			}
		}else if(language == "tokenize"){
			expr = new TokenizerExpression();
			expr.setToken(exprText);
		}else{
			expr = org.apache.camel.builder.SimpleBuilder.simple(exprText);
		}
		return expr;
	}
	
	def getDataformat(ctx){
		def format = shapeProperties.format;
		
		def map = createMap(ctx,format+"_");
		def dataFormatDef = null;
		if( format == "univocity-fixed" ){
			univocityFixedParameterConvert( map );
		}
		if( map.library == "flexjson"){
			def ff = new org.ms123.common.camel.components.FlexJsonDataFormat();
			ff.setPrettyPrinting(shapeProperties.json_prettyPrint);
			dataFormatDef = new DataFormatDefinition(ff);	
		} else if( format  == "zip"){
			def ff = new org.ms123.common.camel.components.ZipFileDataFormat();
			dataFormatDef = new DataFormatDefinition(ff);	
		}else{
			dataFormatDef = new DataFormatDefinition(getFormatName());	
		}
		def routeContext = new DefaultRouteContext(ctx.modelCamelContext);
		def dataFormat = dataFormatDef.getDataFormat(routeContext);
		IntrospectionSupport.setProperties(dataFormat,map);
		prettyPrint("dataFormatType",dataFormat);
		return dataFormat;
	}
	def getFormatName(){
		if( shapeProperties.format=='json'){
			return "json-"+shapeProperties.json_library;
		}
		return shapeProperties.format;
	}

	def createMap(ctx,prefix){
		def map=[:];
		def extraOptions = shapeProperties.extraOptions;
		if( extraOptions != null && extraOptions.totalCount>0){
			def items = extraOptions.items;
			items.each(){item->
				if( isNotEmpty(item.name)  && isNotEmpty(item.value)){
					map[item.name] = item.value;
				}
			}
		}
		shapeProperties.each(){key,value->
			if(key.startsWith(prefix)){
				if( value instanceof Map || value instanceof Collection){
					value = serializer.deepSerialize(value);
				}
				map[key.substring(prefix.length())] = value!=null ? ctx.buildEnvSubstitutor.replace(value.toString()) : "";
			}
		}	
		return map;
	}
	def createMapNoEmptyValues(prefix){
		def map=[:];
		shapeProperties.each(){key,value->
			if(key.startsWith(prefix) && isNotEmpty(value)){
				map[key.substring(prefix.length())] = value;
			}
		}	
		return map;
	}

	def univocityFixedParameterConvert( map ){
		try{
			if( map.fieldLengths){
				def value = deSerializer.deserialize(map.fieldLengths);
				map.fieldLengths = itemsToIntArray( value.items, "length");
				map.headers = itemsToStringArray( value.items,"name");
			}
			if( map.padding && map.padding.length()>0){
				map.padding = map.padding.charAt(0);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("getDataformat.fieldLengths:", e);
		}
	}

	def itemsToIntArray(items,key){
		if( items == null || items.size()==0) return null;
		int[] a = new int[items.size()];
		items.eachWithIndex(){item,i->
			a[i] = Integer.parseInt(item[key]);
		}
		return a;
	}

	def itemsToStringArray(items,key){
		if( items == null || items.size()==0) return null;
		if( isEmpty(items[0][key])) return null;
		String[] a = new String[items.size()];
		items.eachWithIndex(){item,i->
			a[i] = item[key]+"";
		}
		return a;
	}
	
	def createOptionMap(){
		def optionsMap = [:];
		def extraOptions = shapeProperties.extraOptions;
		if( extraOptions != null && extraOptions.totalCount>0){
			def items = extraOptions.items;
			items.each(){item->
				if( isNotEmpty(item.name)  && isNotEmpty(item.value)){
					optionsMap[item.name] = item.value;
				}
			}
		}
		optionsMap.putAll(createMapNoEmptyValues("option_"));
		return optionsMap;
	}

	def getClassPath(namespace){
		URL url1 = 	new URL( "file:"+System.getProperty("workspace") + "/java/" + namespace+"/");
		URL url2 = 	new URL( "file:"+System.getProperty("workspace") + "/java/global/");
		URL url3 = 	new URL( "file:"+System.getProperty("workspace") + "/groovy/" + namespace+"/");
		URL url4 = 	new URL( "file:"+System.getProperty("git.repos") + "/"+namespace+"/.etc/jooq/build/");
		URL[] urls = new URL[4];
		urls[0] = url1;
		urls[1] = url2;
		urls[2] = url3;
		urls[3] = url4;
		return urls;
	}

	def createProcessorGroovy(processMethodStr,importStr,namespace, completeClass) {
		def code = null;
		if( completeClass){
			code = processMethodStr;
			String pattern = "class\\s+(\\w+)\\s+implements\\s+Processor";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(code);
			if (m.find()) {
				String clazz = m.group(1);
				code += "\nreturn "+clazz+".class\n";
			}
		}else{
			code = buildScript(processMethodStr,importStr,true);

		}
		try {
			URLClassLoader classLoader = new URLClassLoader( getClassPath(namespace), getClass().getClassLoader() )
			def gs = new GroovyShell(classLoader);
			def clazz  = (Class) gs.evaluate(code);
			def obj = clazz.newInstance();
			if( fieldExists(clazz,"orientdbFactory")){
				injectField( clazz, obj, "orientdbFactory", getOrientdbFactory(namespace))
			}

			if( fieldExists(clazz,"orientGraph")){
				injectField( clazz, obj, "orientGraph", getOrientGraph(namespace))
			}

			if( fieldExists(clazz,"orientGraphRoot")){
				injectField( clazz, obj, "orientGraphRoot", getOrientGraphRoot(namespace))
			}
			return obj;
		} catch (Throwable e) {
			String msg = Utils.formatGroovyException(e,code);
			throw new RuntimeException(msg);
		}
	}
  def fieldExists(clazz, fieldname) {
    try{
      println("fieldExists("+clazz+","+fieldname+")");
      def field = clazz.getDeclaredField(fieldname);
      println("fieldExists("+fieldname+"):true");
      return true;
    }catch(Exception e){
      println("fieldExists("+fieldname+"):false");
      return false;
    }
  }

	def injectField(clazz,obj, fieldname, value) {
		try{
			def field = clazz.getDeclaredField(fieldname);
			field.setAccessible(true)
			field.set(obj, value );
		}catch(Exception e){
			println("injectField:"+e);
		}
	}

	def getClassName(code) {
		String pattern = "public class\\s+(\\w+)\\s+implements\\s+Processor";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(code);
		if (m.find()) {
			String clazz = m.group(1);
			return clazz;
		}
		return "MyProcessor";
	}

	def createProcessorJava(processMethodStr,importStr, namespace, completeClass) {
		def code = null;
		def clazz = null;
		if( completeClass){
			code = processMethodStr;
		}else{
			code = buildScript(processMethodStr,importStr,false);
		}
		try {
			clazz = JavaCompiler.compile(namespace, bundleContext.getBundle(), getClassName(code), code);
			return clazz.newInstance();
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("createProcessorJava.clazz:"+clazz);
			System.err.println("createProcessorJava.code:"+code);
			throw new RuntimeException(e.getMessage() as String);
		}
	}
	def getEnrich() {
		if( "enrich".equals(shapeProperties.get("enrich"))){
			return "enrich";
		}
		if( "pollEnrich".equals(shapeProperties.get("enrich"))){
			return "pollEnrich";
		}
		return null;
	}
	int getEnrichTimeout() {
		def t = shapeProperties.get("enrich_timeout");
		if( t == null){
			t =0;
		}
		try{
			return Integer.parseInt(t);	
		}catch(Exception e){
		}
		return 0;
	}

	def getSharedLinkRef() {
		if( "link".equals(shapeProperties.get("shared"))){
			return shapeProperties.get("shareRef");
		}
		return null;
	}
	def getOrigLinkRef() {
		if( "origin".equals(shapeProperties.get("shared"))){
			return shapeProperties.get("shareRef");
		}
		return null;
	}

	def prettyPrint(msg, obj){
		def js = new JSONSerializer();
		js.prettyPrint(true);
		//println(msg+js.deepSerialize(obj));
	}

	def buildImport(addImport) {
		def ret = "";
		for( def line : addImport){
			ret += "import "+ line["import"]+";\n";
		}
		return ret;
	}
	def buildScript(processMethodStr,importStr,isGroovy) {
		def script = "import org.apache.camel.*;\n";
		script += "import org.apache.camel.impl.*;\n";
		script += "import org.apache.camel.builder.*;\n";
		script += "import org.apache.camel.model.dataformat.*;\n";
		script += "import org.ms123.common.data.api.SessionContext;\n";
		script += "import org.ms123.common.data.api.DataLayer;\n";
		script += "import org.ms123.common.git.GitService;\n";
		script += "import org.ms123.common.auth.api.AuthService;\n";
		script += "import org.ms123.common.nucleus.api.NucleusService;\n";
		script += "import org.ms123.common.store.StoreDesc;\n";
		script += "import org.ms123.common.permission.api.PermissionService;\n";
		script += "import org.ms123.common.team.api.TeamService;\n";
		script += "import org.ms123.common.system.thread.ThreadContext;\n";
		script += "import org.ms123.common.permission.api.PermissionException;\n";
		script += "import org.ms123.common.libhelper.Inflector;\n";
		script += "import static org.apache.commons.io.FileUtils.*;\n";
		script += "import static org.apache.commons.lang3.StringUtils.*;\n";
		script += "import static org.apache.commons.io.FilenameUtils.*;\n";
		script += "import static org.apache.commons.io.IOUtils.*;\n";
		script += "import static org.apache.commons.beanutils.PropertyUtils.*;\n";
		script += "import java.util.*;\n";
		script += "import flexjson.JSONSerializer;\n";
		script += "import flexjson.JSONDeserializer;\n";
		script += importStr;
		script += "class MyProcessor implements Processor{\n";
		script += processMethodStr;
		script += "};\n";
		if( isGroovy){
			script += "return MyProcessor.class;\n";
		}
		return script;
	}


	def getDefaultAggregationStrategy(key){
		if( key == "useLatest") return new UseLatestAggregationStrategy();
		if( key == "useOriginal") return new UseOriginalAggregationStrategy();
		if( key == "groupedExchange") return new GroupedExchangeAggregationStrategy();
		return null;
	}

	def setConstants(routeDefinition, properties){
		def constList = properties?.settings?.items;
		if( constList != null){
			for( def item : constList){
				def dest = item.destination;
				def constant = item["const"];
				def name = item.name;
				def source = item.source;
				if( isEmpty(constant) || isEmpty(name)) continue;
				if( "branding".equals(source)){
					constant = branding.get(name);
				}
				def constExpr = new ConstantExpression(constant as String);
				if( "property".equals(dest)){
					routeDefinition.setProperty(name, constExpr);
				}else{
					routeDefinition.setHeader(name, constExpr);
				}
			}
		}
	}
	def getRepoFile(namespace,name,type){
		def gitService=null;
		def sr = bundleContext.getServiceReference("org.ms123.common.git.GitService");
		if (sr != null) {
			gitService = bundleContext.getService(sr);
		}
		if (gitService == null) {
			throw new RuntimeException("JsonConverter.Cannot resolve GitService");
		}
		return gitService.searchFile(namespace, name, type);
	}
	def getOrientdbFactory(namespace){
		def oService=null;
		def sr = bundleContext.getServiceReference("org.ms123.common.system.orientdb.OrientDBService");
		if (sr != null) {
			oService = bundleContext.getService(sr);
		}
		if (oService == null) {
			throw new RuntimeException("JsonConverter.Cannot resolve OrientDBService");
		}
		return oService.getFactory(namespace);
	}
	def getOrientGraph(namespace){
		def oService=null;
		def sr = bundleContext.getServiceReference("org.ms123.common.system.orientdb.OrientDBService");
		if (sr != null) {
			oService = bundleContext.getService(sr);
		}
		if (oService == null) {
			throw new RuntimeException("JsonConverter.Cannot resolve OrientDBService");
		}
		return oService.getOrientGraph(namespace);
	}
	def getOrientGraphRoot(namespace){
		def oService=null;
		def sr = bundleContext.getServiceReference("org.ms123.common.system.orientdb.OrientDBService");
		if (sr != null) {
			oService = bundleContext.getService(sr);
		}
		if (oService == null) {
			throw new RuntimeException("JsonConverter.Cannot resolve OrientDBService");
		}
		return oService.getOrientGraphRoot(namespace);
	}
	def getScriptEngine(namespace, name){
		def scriptEngineService=null;
		def sr = bundleContext.getServiceReference("org.ms123.common.system.script.ScriptEngineService");
		if (sr != null) {
			scriptEngineService = bundleContext.getService(sr);
		}
		if (scriptEngineService == null) {
			throw new RuntimeException("JsonConverter.Cannot resolve scriptEngineService");
		}
		return scriptEngineService.getEngineByName(namespace, name);
	}
	def getService(serviceName){
		def service=null;
		def sr = bundleContext.getServiceReference(serviceName);
		if (sr != null) {
			service = bundleContext.getService(sr);
		}
		if (service == null) {
			throw new RuntimeException("JsonConverter.Cannot resolve :"+serviceName);
		}
		return service;
	}
	def getDataLayer(){
		def service=null;
		def srList	=	bundleContext.getServiceReferences("org.ms123.common.data.api.DataLayer", "(kind=orientdb)");
		def orient = null;
		if (srList != null && srList.size()>=1) {
			orient = bundleContext.getService(srList.iterator().next());
		}
		if (orient == null) {
			throw new RuntimeException("JsonConverter.Cannot resolve service:org.ms123.common.camel.api.DataLayer(Orientdb)");
		}
		return orient;
	}
	def getDataLayerJdo(){
		def service=null;
		def srList	=	bundleContext.getServiceReferences("org.ms123.common.data.api.DataLayer", "(kind=jdo)");
		def jdo = null;
		if (srList != null && srList.size()>=1) {
			jdo = bundleContext.getService(srList.iterator().next());
		}
		if (jdo == null) {
			throw new RuntimeException("JsonConverter.Cannot resolve service:org.ms123.common.camel.api.DataLayer(JDO)");
		}
		return jdo;
	}
}

class OnExceptionJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		Set exList = [];
		def items = shapeProperties.exceptions?.items;
		if( items!=null){
			for( def item : items){
				try{
					exList.add(Class.forName(item.exception));	
				}catch(Exception e){
					throw new RuntimeException("JsonConverter.OnException:cannot convert \""+item.exception+"\" to a ExceptionClass");
				}
			}
		}
		if( exList.size()==0){
			exList.add(java.lang.Exception.class);
		}
		RouteDefinition rd = ctx.routesDefinition.getRoutes().get(0);
		ctx.current = rd.onException(exList  as Class[]);
		ctx.current.id(resourceId);
		if(isNotEmpty(shapeProperties.continued)){
			ctx.current.setContinued(new ExpressionSubElementDefinition((Expression)createExpression(shapeProperties.continued,shapeProperties.continuedLanguage)));
		}
		if(isNotEmpty(shapeProperties.handled)){
			ctx.current.setHandled(new ExpressionSubElementDefinition((Expression)createExpression(shapeProperties.handled,shapeProperties.handledLanguage)));
		}
		if(isNotEmpty(shapeProperties.retryWhile)){
			ctx.current.setRetryWhile(new ExpressionSubElementDefinition((Expression)createExpression(shapeProperties.retryWhile,shapeProperties.retryWhileLanguage)));
		}
	}
}
class OnCompletionJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		RouteDefinition rd = ctx.routesDefinition.getRoutes().get(0);
		ctx.current = rd.onCompletion();
		ctx.current.id(resourceId);
	}
}

class EndpointJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		def link = getSharedLinkRef();
		if( link == null){
			link = getOrigLinkRef();
		}
		def sharedEndpoint = null;
		if( link != null){
			sharedEndpoint = ctx.sharedEndpoints[link];
			if( sharedEndpoint == null) throw new RuntimeException("EndpointJsonConverter:sharedEnpoint("+link+") not found.");
		}
		def enrich = getEnrich();
		if( ctx.routesDefinition == null){
			ctx.routesDefinition = new RoutesDefinition();
			ctx.routesDefinition.setCamelContext( ctx.modelCamelContext);
			def routeDefinition = ctx.routesDefinition.from(sharedEndpoint ? sharedEndpoint : constructUri(ctx));
			setConstants(routeDefinition, rootProperties);
			ctx.current = routeDefinition;
			ctx.current.getInputs().get(0).id(resourceId);
			ctx.routeStart = false;
		}else if(ctx.routeStart==true){
			ctx.routeStart = false;
			ctx.current = ctx.routesDefinition.from(sharedEndpoint ? sharedEndpoint : constructUri(ctx));
			//ctx.current.getInputs().get(0).id(resourceId);
		}else{
			if( enrich =="pollEnrich"){
				ctx.current = ctx.current.pollEnrich(sharedEndpoint ? sharedEndpoint : constructUri(ctx), getEnrichTimeout());
			}else if( enrich == "enrich"){
				ctx.current = ctx.current.enrich(sharedEndpoint ? sharedEndpoint : constructUri(ctx));
			}else{
				ctx.current = ctx.current.to(sharedEndpoint ? sharedEndpoint : constructUri(ctx));
			}
			ctx.current.id(resourceId);
		}
	}
}

class FileEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		def path = shapeProperties.urivalue_path;
		if( path.startsWith("workspace:")){
			String dir = System.getProperty("workspace")
			shapeProperties.urivalue_path = new File(dir, path.substring("workspace:".length())).toString();
		}
		if( path.startsWith("git.repos:")){
			String dir = System.getProperty("git.repos")
			shapeProperties.urivalue_path = new File(dir, path.substring("git.repos:".length())).toString();
		}
		super.convertToCamel(ctx);
	}
}

class DirectEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class WebsocketEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class EventBusEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class SedaEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class WampEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class VMEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class FtpEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		if( shapeProperties.uriparam_protocol != "ftp"){
			shapeProperties.remove("uriparam_ftpClient.defaultTimeout");
		}
		super.convertToCamel(ctx);
	}
}

class SqlEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class RepoEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class XDocReportEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class TemplateEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class AsciidoctorEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class HttpClientEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class MailEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class XmppEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(ctx){
		super.convertToCamel(ctx);
	}
}

class JmsEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(JsonConverterContext ctx){
		super.convertToCamel(ctx);
	}
}
class LocaldataEndpointJsonConverter extends EndpointJsonConverter{
	void convertToCamel(JsonConverterContext ctx){
		super.convertToCamel(ctx);
	}
}

class DelayJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		String charset = shapeProperties.charset!= null ? shapeProperties.charset : "utf-8";
		def expr = createExpression(shapeProperties.expression, shapeProperties.language);
		ctx.current = ctx.current.delay(expr);
		ctx.current.id(resourceId);
	}
}

class TransactedJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		ctx.current = ctx.current.transacted(shapeProperties.propagationBehavior);
		ctx.current.id(resourceId);
	}
}

class RollbackJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		ctx.current = ctx.current.rollback();
		ctx.current.id(resourceId);
		if( shapeProperties.markRollbackOnly){
			ctx.current = ctx.current.markRollbackOnly();
		}
		if( shapeProperties.markRollbackOnlyLast){
			ctx.current = ctx.current.markRollbackOnlyLast();
		}
	}
}

class ConvertBodyToJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		String charset = shapeProperties.charset!= null ? shapeProperties.charset : "utf-8";
		Class type = null;
		try{
			if( shapeProperties.type == "byte[]"){
				type = byte[].class;
			}else{
				type = Class.forName(shapeProperties.type!=null ? shapeProperties.type : "java.lang.String");
			}
		}catch(Exception e){
			throw new RuntimeException("ConvertBodyToJsonConverter:Class.forName("+shapeProperties.type+"):"+e.getMessage());
		}
		if( charset == "") charset=null;
		ctx.current = ctx.current.convertBodyTo(type,charset);
		ctx.current.id(resourceId);
	}
}

class MarshalJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		ctx.current = ctx.current.marshal(getDataformat(ctx));
		ctx.current.id(resourceId);
	}
}
class UnmarshalJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		ctx.current = ctx.current.unmarshal(getDataformat(ctx));
		ctx.current.id(resourceId);
	}
}

class SimpleConnectionJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
	}
}

class MessageChoiceJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		ctx.current = ctx.current.choice();
		ctx.current.setId(resourceId);
		//def cb = new JsonConverterContext.ChoiceBlock();
		//ctx.choiceStack.push(cb);
	}
	void finishToCamel(ctx){
		ctx.current = ctx.current.endChoice();
		//def cb = ctx.choiceStack.pop();
	}
}

class WhenConnectionJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		if( shapeProperties.defaultflow == "none"){
			ctx.current = ctx.current.when(createExpression(shapeProperties.expression,shapeProperties.language));
			def whenList = ctx.current.getWhenClauses();
			whenList[whenList.size()-1].id(resourceId);
		}else{
			ctx.current = ctx.current.otherwise();
			ctx.current.getOtherwise().id(resourceId);
		}
	}
}

class MessageFilterJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		ctx.current = ctx.current.filter(createExpression(shapeProperties.expression,shapeProperties.language));
		def options = createOptionMap();
		IntrospectionSupport.setProperties(ctx.current,options);
		prettyPrint("FilterDefinition:", ctx.current);
		ctx.current.id(resourceId);
	}
}

class RecipientListJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		ctx.current = ctx.current.recipientList(createExpression(shapeProperties.expression,shapeProperties.language));
		def options = createOptionMap();
		IntrospectionSupport.setProperties(ctx.current,options);
		prettyPrint("RecipientListDefinition:", ctx.current);
		ctx.current.id(resourceId);
	}
}

class LoopJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		ctx.current = ctx.current.loop(createExpression(shapeProperties.expression,shapeProperties.language));
		ctx.current.id(resourceId);
	}
}

class LoopDoWhileJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		ctx.current = ctx.current.loopDoWhile(createExpression(shapeProperties.expression,shapeProperties.language));
		ctx.current.id(resourceId);
	}
}

class EndJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		ctx.current = ctx.current.end();
	}
}


class MessageAggregateJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		if( isEmpty(shapeProperties.option_strategyRef)){
			ctx.current = ctx.current.aggregate(createExpression(shapeProperties.correlationExpression,shapeProperties.correlationLanguage), getDefaultAggregationStrategy(shapeProperties.defaultAggregationStrategy));
		}else{
			ctx.current = ctx.current.aggregate(createExpression(shapeProperties.correlationExpression,shapeProperties.correlationLanguage));
		}

		if(isNotEmpty(shapeProperties.completionPredicate)){
			ctx.current.setCompletionPredicate(new ExpressionSubElementDefinition((Predicate)createExpression(shapeProperties.completionPredicate,shapeProperties.completionLanguage)));
		}
		if(isNotEmpty(shapeProperties.completionSizeExpression)){
			ctx.current.setCompletionSizeExpression(new ExpressionSubElementDefinition((Expression)createExpression(shapeProperties.completionSizeExpression,shapeProperties.completionSizeLanguage)));
		}
		if(isNotEmpty(shapeProperties.completionTimeoutExpression)){
			ctx.current.setCompletionTimeoutExpression(new ExpressionSubElementDefinition((Expression)createExpression(shapeProperties.completionTimeoutExpression,shapeProperties.completionTimeoutLanguage)));
		}
		def options = createOptionMap();
		IntrospectionSupport.setProperties(ctx.current,options);
		//prettyPrint("AggregateDefinition:", ctx.current);
		ctx.current.id(resourceId);
	}
	void finishToCamel(ctx){
		ctx.current = ctx.current.end();
	}
}

class MessageSplitterJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		ctx.current = ctx.current.split(createExpression(shapeProperties.expression,shapeProperties.language));
		def options = createOptionMap();
		IntrospectionSupport.setProperties(ctx.current,options);
		//prettyPrint("SplitDefinition:", ctx.current);
		if( shapeProperties.loggingOff == true){
			ctx.current.setProperty( "__loggingOff",new ConstantExpression("true"));
		}
		ctx.current.id(resourceId);
	}
}

class SetHeaderJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		def items = shapeProperties?.headers?.items;
		if( items != null){
			items.each(){item->
				def expr = createExpression(item.value, item.language);
				ctx.current = ctx.current.setHeader(item.name,expr);
			}
		}
		ctx.current.id(resourceId);
	}
}

class SetPropertyJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		def items = shapeProperties?.properties?.items;
		if( items != null){
			items.each(){item->
				def expr = createExpression(item.value, item.language);
				ctx.current = ctx.current.setProperty(item.name,expr);
			}
		}
		ctx.current.id(resourceId);
	}
}
class ProcessorJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		def addImport = shapeProperties.addImport;
		def codeLanguage = shapeProperties.codeLanguage;
		def codeKind = shapeProperties.codeKind;
		def isEndpoint = shapeProperties.isEndpoint;
		def ref = shapeProperties.ref;
		if( codeLanguage == "java" || codeLanguage == "groovy"){
			def code = shapeProperties.code;
			def srcFile = shapeProperties.srcFile;
			if( isNotEmpty(ref)){
				if( isEndpoint){
					ctx.current = ctx.current.to(ref);
				}else{
					ctx.current = ctx.current.processRef(ref);
				}
			}else if( isNotEmpty(srcFile) && codeLanguage == "groovy" ){
				def namespace = ctx.modelCamelContext.getRegistry().lookup("namespace");
				def file  = getRepoFile(namespace, srcFile, "sw."+codeLanguage);
				def script = readFileToString(file);

				def sp = new GroovyProcessor(script,file, namespace, this);
				ctx.current = ctx.current.process(sp);
			}else if( code != null && code.length()> 10){
				def codeImport = "";
				if( addImport != null && addImport.size()> 0){
					codeImport = buildImport(addImport);	
				}
				def namespace = ctx.modelCamelContext.getRegistry().lookup("namespace");
				def processor=null;
				if( "java".equals(codeLanguage)){
					processor = createProcessorJava(code,codeImport,namespace, codeKind=="completeClass");
				}else{
					processor = createProcessorGroovy(code,codeImport,namespace, codeKind=="completeClass");
				}
				ctx.current = ctx.current.process(processor);
			}
		}else{
			def namespace = ctx.modelCamelContext.getRegistry().lookup("namespace");
			def script = shapeProperties.script;
			def srcFile = shapeProperties.srcFile;
			def file = null;
			if( srcFile ){
				file  = getRepoFile(namespace, srcFile, "sw."+codeLanguage);
				script = readFileToString(file);
			}
			def sp = new ScriptProcessor(script,file, namespace, this);
			ctx.current = ctx.current.process(sp);
		}
		ctx.current.id(resourceId);
	}
}

class DatamapperJsonConverter extends JsonConverterImpl{
	void convertToCamel(ctx){
		def strategy = shapeProperties.strategy;
		if( isNotEmpty(strategy)){
			def processor = new Processor(){
				public void process(Exchange ex){
					def dm = ctx.modelCamelContext.getRegistry().lookupByName("datamapper");
					def answer = dm.transform(ex.getIn().getBody(),strategy, ex);
					ex.getIn().setBody(answer);
				}
				public String toString(){
					return "Datamapper:"+strategy;
				}
			};
			ctx.current = ctx.current.process(processor);
			ctx.current.id(resourceId);
		}
	}
}

class ScriptProcessor implements Processor {
	def file;
	def main;
	def namespace;
	def lastMod = null;
	def scriptEngine;
	def compiledScript;
	public ScriptProcessor(script, f, ns, main ){
		this.file = f;
		if( f){
			this.lastMod = f.lastModified();
		}
		this.namespace = ns
		this.main = main
		this.scriptEngine = main.getScriptEngine(this.namespace,"nashorn");
		this.compiledScript = this.scriptEngine.compile(script);
	}

	def testModified(){
		if( this.file == null) return;
		def curMod = file.lastModified();
		if( curMod > this.lastMod){
			def script = readFileToString(this.file);
			this.scriptEngine = this.main.getScriptEngine(namespace,"nashorn")
			this.compiledScript = this.scriptEngine.compile(script);
			this.lastMod = curMod;
		}
	}
	public void process(Exchange ex) {
		testModified();

		def params = this.scriptEngine.createBindings();
		params.put("exchange", ex);
		def ctx = ex.getContext();
		def registry = ctx.getRegistry();
		params.put("headers", ex.in.headers);
		params.put("h", ex.in.headers);
		params.put("min", ex.in);
		params.put("msg", ex.in);
		params.put("registry", registry);
		params.put("properties", ex.properties);
		params.put("p", ex.properties);
		def env = [
			gitRepos: System.getProperty("git.repos"),
			simpl4Dir: System.getProperty("simpl4.dir"),
			homeDir: System.getProperty("git.repos")+ "/" + this.namespace,
			homeDataDir: System.getProperty("git.repos")+ "/" + this.namespace+"_data",
			namespace: this.namespace
		]
		params.put("env", env);
		compiledScript.eval(params);
	}
}

class GroovyProcessor implements Processor {
	def file;
	def main;
	def namespace;
	def lastMod = null;
	def scriptClazz;
	def scriptSource;
	def classLoader;
	public GroovyProcessor(script, f, ns, main ){
		this.file = f;
		this.lastMod = f.lastModified();
		this.namespace = ns;
		this.main = main;
		parse(script, this.file.getName());
	}

	def testModified(){
		if( this.file == null) return;
		def curMod = this.file.lastModified();
		if( curMod > this.lastMod){
			def script = readFileToString(this.file);
			parse(script, this.file.getName());
			this.lastMod = curMod;
		}
	}

	private void parse(String scriptStr,String scriptName) {
		println("GroovyProcessor.parse("+scriptName+"):"+scriptStr);
		if( scriptStr == null) return null;
		this.scriptSource = scriptStr;

		def parentLoader = new URLClassLoader( main.getClassPath(this.namespace), this.getClass().getClassLoader() )

		CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass(org.ms123.common.camel.jsonconverter.GroovyBase.class.getName());
		def importCustomizer = new ImportCustomizer();
		importCustomizer.addStarImports("org.apache.camel");
		importCustomizer.addStarImports("groovy.transform");
		config.addCompilationCustomizers(importCustomizer);
		//GroovyClassLoader loader =  new CollectorClassLoader(parentLoader,config);
		GroovyClassLoader loader =  new GroovyClassLoader(parentLoader,config);
		this.classLoader = loader;

		try{
			GroovyCodeSource gcs = new GroovyCodeSource( scriptStr, "Script_"+scriptName, "/groovy/shell");
			this.scriptClazz = loader.parseClass(gcs,false);
		}catch(Throwable e){
			String msg = Utils.formatGroovyException(e,scriptStr);
			throw new RuntimeException("GroovyProcessor.parse("+scriptName+"):"+msg);
		}
	}

	private Object run(Script script, Map vars, String scriptName) {
		println("GroovyProcessor.run("+scriptName+"):"+vars);

		Thread.currentThread().setContextClassLoader(classLoader);//needed orientdb-groovy/OrientGraphHelper.groovy

		script.setBinding(new Binding(vars));
		try{
			return script.run();
		}catch(groovy.lang.MissingMethodException e){
			e.printStackTrace();
			Object[] args = e.getArguments();
			String a = "";
			String k = "";
			for(int i=0; i< args.length;i++){
				a += k+args[i];
				k = ",";
			}
			throw new RuntimeException("GroovyProcessor.run("+scriptName+"):"+e.getMethod() + "("+ a + ") not found");
		}catch(Exception ex){
			ex.printStackTrace();
			String msg = Utils.formatGroovyException(ex,scriptSource);
			throw new RuntimeException("GroovyProcessor.run("+scriptName+"):"+msg );
		}
	}

	public void process(Exchange ex) {
		testModified();

		def params = [:];
		params.put("exchange", ex);
		def ctx = ex.getContext();
		def registry = ctx.getRegistry();
		params.put("headers", ex.in.headers);
		params.put("h", ex.in.headers);
		params.put("min", ex.in);
		params.put("msg", ex.in);
		params.put("registry", registry);
		params.put("properties", ex.properties);
		params.put("p", ex.properties);
		def env = [
			gitRepos: System.getProperty("git.repos"),
			simpl4Dir: System.getProperty("simpl4.dir"),
			homeDir: System.getProperty("git.repos")+ "/" + this.namespace,
			homeDataDir: System.getProperty("git.repos")+ "/" + this.namespace+"_data",
			namespace: this.namespace,
			hostname: InetAddress.getLocalHost().getHostName()
		]
		def script = 	this.scriptClazz.newInstance();
		params.put("env", env);
		if( main.fieldExists(this.scriptClazz,"entityService")){
			main.injectField( this.scriptClazz, script, "entityService", main.getService( "org.ms123.common.entity.api.EntityService"))
		}
		if( main.fieldExists(this.scriptClazz,"permissionService")){
			main.injectField( this.scriptClazz, script, "permissionService", main.getService( "org.ms123.common.permission.api.PermissionService"))
		}
		if( main.fieldExists(this.scriptClazz,"processService")){
			main.injectField( this.scriptClazz, script, "processService", main.getService( "org.ms123.common.process.api.ProcessService"))
		}
		if( main.fieldExists(this.scriptClazz,"authService")){
			main.injectField( this.scriptClazz, script, "authService", main.getService( "org.ms123.common.auth.api.AuthService"))
		}
		if( main.fieldExists(this.scriptClazz,"dataLayer")){
			main.injectField( this.scriptClazz, script, "dataLayer", main.getDataLayer())
		}
		if( main.fieldExists(this.scriptClazz,"dataLayerJdo")){
			main.injectField( this.scriptClazz, script, "dataLayerJdo", main.getDataLayerJdo())
		}
		if( main.fieldExists(this.scriptClazz,"messageService")){
			main.injectField( this.scriptClazz, script, "messageService", main.getService( "org.ms123.common.message.MessageService"))
		}
		if( main.fieldExists(this.scriptClazz,"settingService")){
			main.injectField( this.scriptClazz, script, "settingService", main.getService( "org.ms123.common.setting.api.SettingService"))
		}
		if( main.fieldExists(this.scriptClazz,"registryService")){
			main.injectField( this.scriptClazz, script, "registryService", main.getService( "org.ms123.common.system.registry.RegistryService"))
		}
		if( main.fieldExists(this.scriptClazz,"callService")){
			main.injectField( this.scriptClazz, script, "callService", main.getService( "org.ms123.common.rpc.CallService"))
		}
		if( main.fieldExists(this.scriptClazz,"orientGraph")){
			main.injectField( this.scriptClazz, script, "orientGraph", main.getOrientGraph( this.namespace))
		}
		if( main.fieldExists(this.scriptClazz,"orientGraphRoot")){
			main.injectField( this.scriptClazz, script, "orientGraphRoot", main.getOrientGraphRoot( this.namespace))
		}
		run(script,params,this.file.getName());
	}
}



