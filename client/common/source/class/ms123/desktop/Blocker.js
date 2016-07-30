qx.Class.define( "ms123.desktop.Blocker", {
	extend: qx.ui.core.Blocker,

	construct: function( widget ) {
		this.base( arguments, widget );
	},

	members: {
		__onWidgetAppear: function() {
			this._updateBlockerBounds( this._widget.getBounds() );
			console.log( "myBlocker" );
			this._widget.getContentElement().add( this.getBlockerElement() );
		}
	}

} );
