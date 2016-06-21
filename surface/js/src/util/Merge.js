/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 *  Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
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
/**
 */
require( "./Construct" );
clazz.construct.extend( "simpl4.util.Merge", {
	deepmerge: function( target, src ) {
		var array = Array.isArray( src );
		var dst = array && [] || {};

		if ( array ) {
			target = target || [];
			dst = dst.concat( target );
			src.forEach( function( e, i ) {
				if ( typeof dst[ i ] === 'undefined' ) {
					dst[ i ] = e;
				} else if ( typeof e === 'object' ) {
					dst[ i ] = simpl4.util.Merge.deepmerge( target[ i ], e );
				} else {
					if ( target.indexOf( e ) === -1 ) {
						dst.push( e );
					}
				}
			} );
		} else {
			if ( target && typeof target === 'object' ) {
				Object.keys( target ).forEach( function( key ) {
					dst[ key ] = target[ key ];
				} )
			}
			Object.keys( src ).forEach( function( key ) {
				if ( typeof src[ key ] !== 'object' || !src[ key ] ) {
					dst[ key ] = src[ key ];
				} else {
					if ( !target[ key ] ) {
						dst[ key ] = src[ key ];
					} else {
						dst[ key ] = simpl4.util.Merge.deepmerge( target[ key ], src[ key ] );
					}
				}
			} );
		}

		return dst;
	},

	/* Other merge variant, with multiple input objects, always deep*/
	clonemerge: function( argv ) {
		[].splice.call( arguments, 0, 0, true );
		return simpl4.util.Merge.merge.apply( null, arguments );
	},

	merge: function( clone, argv ) {
		var firstarg = 1
		var result = arguments[ firstarg ];
		var size = arguments.length - firstarg;

		if ( clone || simpl4.util.Merge._typeof( result ) !== 'object' ) {
			result = {};
		}
		for ( var index = 0; index < size; ++index ) {
			var item = arguments[ index + firstarg ],
				type = simpl4.util.Merge._typeof( item );

			if ( type !== 'object' ) continue;

			for ( var key in item ) {
				var sitem = clone ? simpl4.util.Merge.clone( item[ key ] ) : item[ key ];
				result[ key ] = simpl4.util.Merge._mergeRecursive( result[ key ], sitem );
			}
		}
		return result;
	},

	clone: function( input ) {
		var output = input,
			type = simpl4.util.Merge._typeof( input ),
			index, size;

		if ( type === 'array' ) {
			output = [];
			size = input.length;
			for ( index = 0; index < size; ++index ) {
				output[ index ] = simpl4.util.Merge.clone( input[ index ] );
			}
		} else if ( type === 'object' ) {
			output = {};
			for ( index in input ) {
				output[ index ] = simpl4.util.Merge.clone( input[ index ] );
			}
		}
		return output;
	},

	_mergeRecursive: function( base, extend ) {
		if ( simpl4.util.Merge._typeof( base ) !== 'object' ) {
			return extend;
		}
		for ( var key in extend ) {
			if ( simpl4.util.Merge._typeof( base[ key ] ) === 'object' && simpl4.util.Merge._typeof( extend[ key ] ) === 'object' ) {
				base[ key ] = simpl4.util.Merge._mergeRecursive( base[ key ], extend[ key ] );
			} else {
				base[ key ] = extend[ key ];
			}
		}
		return base;
	},

	_typeof: function( input ) {
		return ( {} ).toString.call( input ).slice( 8, -1 ).toLowerCase();
	}
}, {} );

/*var obj1 = {
	key1: "value1"
};
var obj2 = {
	key2: "value2"
};

var clone = simpl4.util.Merge.clonemerge( obj1, obj2 );
console.log( "clone:", clone );
console.log( "obj1:", obj1 );*/
