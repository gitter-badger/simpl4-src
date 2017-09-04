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
 */
qx.Class.define("ms123.util.Rpc", {
	extend: qx.io.remote.Rpc,

/*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

	properties: {},


/* 
  *****************************************************************************
     MEMBERS 
  *****************************************************************************
  */

	members: {
		createRpcData: function (id, method, parameters, serverData) {
			var requestObject;
			var service;

			// Create a protocol-dependent request object
			if (this.getProtocol() == "qx1") {
				// Create a qooxdoo-modified version 1.0 rpc data object
				console.warn("--> method("+method+"):"+qx.util.Serializer.toJson(parameters));
				requestObject = {
					"service": method == "refreshSession" ? null : this.getServiceName(),
					"method": method,
					"id": id,
					"params": parameters.pop()
				};

				// Only add the server_data member if there is actually server data
				if (serverData) {
					requestObject.server_data = serverData;
				}
			} else {
				// If there's a service name, we'll prepend it to the method name
				service = this.getServiceName();
				if (service && service != "") {
					service += ".";
				} else {
					service = "";
				}

				// Create a standard version 2.0 rpc data object
				requestObject = {
					"jsonrpc": "2.0",
					"method": service + method,
					"id": id,
					"params": parameters
				};
			}

			return requestObject;
		}
	}
});
