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
 * @ignore(Hash)
 */
qx.Class.define('ms123.datamapper.MappingCreator', {
	extend: qx.ui.container.Composite,

	construct: function (facade) {
		this.base(arguments);
		this._facade = facade;

		this.setLayout(new qx.ui.layout.Dock());

		var container = new qx.ui.container.Composite(new qx.ui.layout.HBox(2));
		var input = this._createGroupBox(this.tr("datamapper.input"),ms123.datamapper.Config.INPUT);
		var output = this._createGroupBox(this.tr("datamapper.output"),ms123.datamapper.Config.OUTPUT);
		if( this._facade.mainEntity){
			console.error("Step1");
			output.formatSelector.setEnabled(false);
			var pf = new ms123.datamapper.create.PojoFieldsEditor(this._facade,ms123.datamapper.Config.OUTPUT);
			var data = pf.createModel(this._facade.mainEntity);
			data.format= ms123.datamapper.Config.FORMAT_POJO;
			this._outputValue=data;
			this._outputValue.type = ms123.datamapper.Config.NODETYPE_COLLECTION;
		}

		container.add(input.groupBox, {
			width: "50%"
		});
		container.add(output.groupBox, {
			width: "50%"
		});
		this.add(container, {
			edge: "center"
		});
		input.formatSelector.addListener("changeValue", function (ev) {
			this._inputValue = ev.getData();
			if (this._inputValue && this._outputValue) {
				this._createButton.setEnabled(true);
				this._createButton.setBackgroundColor(ms123.datamapper.Config.BG_COLOR_READY);
			}else{
				this._createButton.setEnabled(false);
				this._createButton.setBackgroundColor(ms123.datamapper.Config.BG_COLOR_NOTREADY);
			}
			console.log("MappingCreator.input:"+JSON.stringify(this._inputValue,null,2));
		}, this);
		output.formatSelector.addListener("changeValue", function (ev) {
			this._outputValue = ev.getData();
			if (this._inputValue && this._outputValue) {
				this._createButton.setEnabled(true);
				this._createButton.setBackgroundColor(ms123.datamapper.Config.BG_COLOR_READY);
			}else{
				this._createButton.setEnabled(false);
				this._createButton.setBackgroundColor(ms123.datamapper.Config.BG_COLOR_NOTREADY);
			}
			console.log("MappingCreator.output:",this._outputValue);
		}, this);

		this._createButton = new qx.ui.form.Button(this.tr("datamapper.create_mapping"), "icon/16/actions/dialog-ok.png");
		this._createButton.setDecorator(null);
		this._createButton.addListener("execute", function () {
			if( this._outputValue && this._outputValue.kind == ms123.datamapper.Config.KIND_LIKE_INPUT){
				var format = this._outputValue.format;
				ms123.util.Clone.merge(this._outputValue, this._inputValue);
				this._outputValue.format = format;
				if( this._inputValue.format == ms123.datamapper.Config.FORMAT_CSV ||
						this._outputValue.format == ms123.datamapper.Config.FORMAT_POJO){
					this._outputValue.type = ms123.datamapper.Config.NODETYPE_COLLECTION;
				}
			}
			var data = {
				input: this._inputValue,
				output: this._outputValue
			};
			ms123.datamapper.BaseTree.prepareTreeData(data.input,"");
			ms123.datamapper.BaseTree.prepareTreeData(data.output,"");
			if( this._inputValue.fileId){
				data.fileId = this._inputValue.fileId;
				delete this._inputValue.fileId;
			}
			console.log("MappingCreator.ready:"+JSON.stringify(data,null,2));
			this.fireDataEvent("ready", data, null);
		}, this);
		this._createButton.setEnabled(false);

		this.add(this._createButton, {
			edge: "south"
		});
	},

	events: {
		"ready": "qx.event.type.Data"
	},
	properties: {},

	members: {
		_createGroupBox: function (legend,side) {
			var gb = new qx.ui.groupbox.GroupBox(legend);
			this.add(gb, {
				edge: "center"
			});
			gb.setLayout(new qx.ui.layout.Dock());

			var fs = new ms123.datamapper.create.FormatSelector(this._facade,side,false);
			gb.add(fs, {
				edge: "center"
			});
			return {
				groupBox: gb,
				formatSelector: fs
			};
		}
	}
});
