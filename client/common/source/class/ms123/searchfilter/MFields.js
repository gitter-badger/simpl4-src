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
qx.Mixin.define("ms123.searchfilter.MFields",
{
  members:
  {
    fieldarray: 
			[
				{
					"text": "KurznameFirma", 
					"itemval": "shortname_company", 
					"ops": [
						{
							"text": "enth\u00e4lt", 
							"op": "cn"
						}, 
						{
							"text": "beginnt mit", 
							"op": "bw"
						}, 
						{
							"text": "gleich", 
							"op": "eq"
						}, 
						{
							"text": "ungleich", 
							"op": "ne"
						}
					]
				}, 
				{
					"text": "KurznamePerson", 
					"itemval": "shortname_person", 
					"ops": [
						{
							"text": "enth\u00e4lt", 
							"op": "cn"
						}, 
						{
							"text": "beginnt mit", 
							"op": "bw"
						}, 
						{
							"text": "gleich", 
							"op": "eq"
						}, 
						{
							"text": "ungleich", 
							"op": "ne"
						}
					]
				}, 
				{
					"text": "Firma1", 
					"itemval": "company1", 
					"ops": [
						{
							"text": "enth\u00e4lt", 
							"op": "cn"
						}, 
						{
							"text": "beginnt mit", 
							"op": "bw"
						}, 
						{
							"text": "gleich", 
							"op": "eq"
						}, 
						{
							"text": "ungleich", 
							"op": "ne"
						}
					]
				}, 
				{
					"text": "Firma2", 
					"itemval": "company2", 
					"ops": [
						{
							"text": "enth\u00e4lt", 
							"op": "cn"
						}, 
						{
							"text": "beginnt mit", 
							"op": "bw"
						}, 
						{
							"text": "gleich", 
							"op": "eq"
						}, 
						{
							"text": "ungleich", 
							"op": "ne"
						}
					]
				}, 
				{
					"text": "Vorname", 
					"itemval": "givenname", 
					"ops": [
						{
							"text": "enth\u00e4lt", 
							"op": "cn"
						}, 
						{
							"text": "beginnt mit", 
							"op": "bw"
						}, 
						{
							"text": "gleich", 
							"op": "eq"
						}, 
						{
							"text": "ungleich", 
							"op": "ne"
						}
					]
				}, 
				{
					"text": "Merkmale", 
					"itemval": "traits",
					"ops": [
						{
							"text": "enth\u00e4lt", 
							"op": "cn"
						}, 
						{
							"text": "beginnt mit", 
							"op": "bw"
						}, 
						{
							"text": "gleich", 
							"op": "eq"
						}, 
						{
							"text": "ungleich", 
							"op": "ne"
						}
					],
					"dataValues": [
						{
							"text": "PEV", 
							"value": "100000"
						}, 
						{
							"text": "PEV.0501", 
							"value": "100000.100001"
						}, 
						{
							"text": "PEV.0502", 
							"value": "100000.100002"
						} 
					] 
				}
			]
				}
});
