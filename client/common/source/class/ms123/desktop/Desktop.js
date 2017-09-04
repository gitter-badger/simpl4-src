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
qx.Class.define( 'ms123.desktop.Desktop', {
	extend: qx.ui.window.Desktop,
	include: [ ms123.desktop.MDesktopPersist ],

	construct: function( namespace, manager,bg ) {
		this.base( arguments, manager );

		var am = qx.util.AliasManager.getInstance();
		var file = am.resolve( "resource/ms123/wallpaper1.png" );
		var deco = new ms123.desktop.Background(bg);
		this.setDecorator( deco );

		this._namespace = namespace;

	},

	/*******************************************************************************
	 EVENTS
	 ***************************************************************************** */
	events: {
		windowAdded: "qx.event.type.Data",
		windowRemoved: "qx.event.type.Data"
	},
	/*******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_createBlocker: function() {
			return new ms123.desktop.Blocker( this );
		},
		add: function( element, opt ) {
			this.base( arguments, element, opt );

			if ( element instanceof ms123.desktop.Window ) {
				this.fireDataEvent( 'windowAdded', element );
				element.addListener( 'move', function() {
					var bounds = this.getBounds();

					if ( bounds.top < 0 ) {
						this.moveTo( bounds.left, 0 );
					}
				} );

				element.addListener( 'close', function() {
					this.fireDataEvent( 'windowRemoved', element );
					this.remove( element );
					delete element;
				}, this );
			}
		}
	}
} );
