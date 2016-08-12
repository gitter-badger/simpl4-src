var hydrolysis = require( 'hydrolysis' );

var showList = [];
showList.push("simpl-websocket");
showList.push("simpl-rpc");
showList.push("simpl-datatables");
showList.push("simpl-form");

//hydrolysis.Analyzer.analyze( '/opt/dev/simpl4-src/surface/domelements/bower_components/iron-collapse/iron-collapse.html' )
hydrolysis.Analyzer.analyze( 'docindex.html' )
	.then( function( analyzer ) {
		try {
			removeProps( analyzer, [ 'javascriptNode', 'scriptElement', 'html', 'parsedDocuments', 'parsedScripts', "_content", 'loader', 'contentHref', 'observerNode' ], 0 );

			for( var i=0; i< analyzer.elements.length; i++){
				var e = analyzer.elements[i];
				if( showList.indexOf(e.is) == -1){
					e.show = false;
				}else{
					e.show = true;
				}
			}
			console.log( JSON.stringify( analyzer, null, 2 ) )
		} catch ( e ) {
			console.log( e.stack );
		}
	} );


function removeProps( obj, keys, level ) {
	if ( level > 40 ) return;
	if ( obj instanceof Array ) {
		obj.forEach( function( item ) {
			removeProps( item, keys, level++ )
		} );
	} else if ( typeof obj === 'object' && obj != null ) {
		Object.getOwnPropertyNames( obj ).forEach( function( key ) {
			if ( keys.indexOf( key ) !== -1 ) {
				delete obj[ key ];
			} else {
				removeProps( obj[ key ], keys, level++ );
			}
		} );
	}
}
