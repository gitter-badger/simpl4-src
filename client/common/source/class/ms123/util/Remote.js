/*
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
/**
 * Specific data cell renderer for dates.
 */
/**
 * @ignore(jQuery.*)
 */
qx.Class.define("ms123.util.Remote", {

/*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {
		_username: "",
		_password: "",
		_lastuse: 0,
		_sessionTimeout:60*1000*30,
		_getPassword: function () {
			if( !this.isEmpty(ms123.util.Remote._passwordTmp) ){
				var ret = ms123.util.Remote._passwordTmp;
				ms123.util.Remote._passwordTmp = null;
				return ret;
			}
			var now = new Date().getTime();
			var time = now - ms123.util.Remote._lastuse;
			if( ms123.util.Remote._lastuse!=0 && time  > ms123.util.Remote._sessionTimeout){
				ms123.form.Dialog.alert(qx.locale.Manager.tr('util.remote.session-timeout'),function(e){
					window.location.reload();
				});

			}else{
				ms123.util.Remote._lastuse = new Date().getTime();
			}
			return ms123.util.Remote._password;
		},
		_getUserName: function () {
			if( !this.isEmpty(ms123.util.Remote._usernameTmp) ){
				var ret = ms123.util.Remote._usernameTmp;
				ms123.util.Remote._usernameTmp = null;
				return ret;
			}
			return ms123.util.Remote._username;
		},
		getPrefix:function(){
			var i = location.pathname.indexOf("/sw/start");
			return location.pathname.substring(0,i);
		},
		isEmpty: function (s) {
			if (!s || jQuery.trim(s) == '') return true;
			return false;
		},
		sendSync: function (url, method, type, data, msg) {
			var params = {
				url: url,
				method: method,
				type: type,
				data: data,
				async: false,
				msg: msg
			}
			return ms123.util.Remote.send(params);
		},

		rpcSync: function (dummy,parameter) {
			if (arguments.length < 1) {
				throw "RpcSync not enough args";
			}
			var s = arguments[0].split(":");
			if( s.length != 2 ){
				throw "RpcSync wrong arg(service:method)";
			}
			var service = s[0];
			var method = s[1];
			var newArgs = [];
			newArgs.push(method);
			for (var i = 1; i < arguments.length; i++) {
				newArgs.push(arguments[i]);
			}
				var namespace = "xyz";
				if(  parameter && parameter.namespace ){
					namespace = parameter.namespace;	
				}
			var rpc = new ms123.util.Rpc(this.getPrefix()+"/rpc/"+namespace, service);
//			rpc.setProtocol("2.0");
			rpc.setUsername(ms123.util.Remote._getUserName());
			rpc.setPassword(ms123.util.Remote._getPassword());
			rpc.setUseBasicHttpAuth(true);
			rpc.setTimeout( 5000000 );
			if( newArgs.length == 0)newArgs.push({});
			var result = rpc.callSync.apply(rpc, newArgs);
			return result;
		},

		rpcAsync:function(params){
			if( params.async!=null && params.async == false ){
				try{
					var result = ms123.util.Remote.rpcSync( params.service+":"+params.method, params.parameter);
					if( params.completed ) params.completed.call(params.context,result);
					return result;
				}catch(ex){
					if( params.failed ) params.failed.call(params.context,ex);
					else throw ex;
				}
			}else{
				var namespace = "xyz";
				if(  params.parameter && params.parameter.namespace ){
					namespace = params.parameter.namespace;	
				}
				var rpc = new ms123.util.Rpc(this.getPrefix()+"/rpc/"+namespace, params.service);
				rpc.setUsername(ms123.util.Remote._getUserName());
				rpc.setPassword(ms123.util.Remote._getPassword());
				rpc.setUseBasicHttpAuth(true);
				rpc.setTimeout( 5000000 );
				var handler = function(result, ex) {
					if (ex == null) {
						params.completed.call(params.context,result);
					} else {
						params.failed.call(params.context,ex);
					}
				};
				rpc.callAsync(handler, params.method, params.parameter);	
			}
		},

		send: function (params) {
			var ret = null;
			var url = params.url;
			var method = (params.method != null) ? params.method : "GET";
			var type = (params.type != null) ? params.type : "application/json";
			var async = (params.async != null) ? params.async : false;
			var req = new qx.io.remote.Request(url, method, type);
			if (params.data != null) {
				req.setData(params.data);
			}
			if (params.timeoutvalue != null) {
				req.setTimeout(params.timeoutvalue);
			}
			if (params.contenttype != null) {
				req.setRequestHeader("Content-Type", params.contenttype);
			}
			req.setAsynchronous(async);
			if (params.completed == null) {
				req.addListener("completed", function (e) {
					ret = e.getContent();
					if (params.msg != null) {
						ms123.form.Dialog.alert(params.msg);
					}
				}, this);
			} else {
				req.addListener("completed", params.completed, params.context);
			}
			if (params.failed != null) {
				req.addListener("failed", params.failed, params.context);
			}
			if (params.timeout != null) {
				req.addListener("timeout", params.timeout, params.context);
			}
			req.setUsername(ms123.util.Remote._getUserName());
			req.setPassword(ms123.util.Remote._getPassword());
			req.setUseBasicHttpAuth(true);
			req.send();
			return ret;
		},
		setSessionTimeout:function(timeout){
			if( timeout == undefined ) timeout = 60*1000*30;
			if( timeout == -1) timeout = 60*1000*30000;
			ms123.util.Remote._sessionTimeout = timeout;
		},
		setCredentials: function (username, password) {
			ms123.util.Remote._username = username;
			ms123.util.Remote._password = password;
			ms123.util.Remote._lastuse = new Date().getTime();
		},
		setCredentialsTmp: function (username, password) {
			ms123.util.Remote._usernameTmp = username;
			ms123.util.Remote._passwordTmp = password;
		}
	}

});
