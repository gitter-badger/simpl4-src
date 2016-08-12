var hydrolysis = require( 'hydrolysis' );
//var escape = require('html-escape');

var showList = [];
showList.push( "simpl-websocket" );
showList.push( "simpl-rpc" );
showList.push( "simpl-datatables" );
showList.push( "simpl-form" );

runHydrolysis();
/*
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
	if ( level > 400 ) return;
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
*/

function runHydrolysis() {
	hydrolysis.Analyzer.analyze( 'docindex.html' )
		.then( function( analyzer ) {
			var sections = analyzer.elements;
			sections = sections.concat( analyzer.behaviors );

			for ( var i = 0; i < sections.length; i++ ) {
				var sectionName = sections[ i ][ 'is' ];
				var section = cleanUpSectionDocs( sections[ i ] );
				if ( showList.indexOf( section.is ) == -1 ) {
					section.show = false;
				} else {
					section.show = true;
				}

			}


			var elemNames = Object.keys( analyzer.elementsByTagName );
			for ( var i = 0; i < elemNames.length; i++ ) {
				var name = elemNames[ i ];
				var elem = analyzer.elementsByTagName[ name ];
				cleanUpSectionDocs( elem );
			}
			var json = JSON.stringify( {
				elements: sections,
				elementsByTagName: analyzer.elementsByTagName,
				behaviors: []
			}, null, 2 );
			console.log( json )
		}, function( e ) {
			console.log( 'Could not run hydrolysis', e );
		} );
}

function cleanUpSectionDocs( section ) {
	section.scriptElement = undefined;
	section.behaviors && section.behaviors.forEach( function( behavior ) {
		behavior.javascriptNode = undefined;
	} );
	section.properties && section.properties.forEach( function( property ) {
		property.javascriptNode = undefined;
	} );
	return section;
}
