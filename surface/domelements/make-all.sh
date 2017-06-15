#!/bin/sh

#rm -rf bower_components
if [ ! -d "bower_components" ] ; then
	 if [ -f "bin/bower_components.tgz" ] ; then
		 tar xf bin/bower_components.tgz
	 else
		 echo "No bower"
		 bower install
		 
		 sed -i '/slide-up-scale-down-animation.html/d'  bower_components/neon-animation/neon-animations.html
		 sed -i 's!value: Polymer.IronOverlayManager!value: function(){ return Polymer.IronOverlayManager}!' bower_components/iron-overlay-behavior/*.html
		 sed -e '/target.insertBefore/ {' -e 'r polymer.patch' -e 'd' -e '}' -i bower_components/polymer/polymer.html
		 sed -i '/configure selectedPage animations/a this.animationConfig = [];'  bower_components/neon-animation/neon-animated-pages.html

		 sed -i 's/this.render()/this.async( function() { this.render(); }, 0 )/'  bower_components/polymer/polymer.html

		sed -i 's/console.\(log\|warn\|error\).apply/Function.prototype/' bower_components/polymer/polymer-micro.html
		sed -i '/function saveLightChildrenIfNeeded/a if( node == null){ console.log("saveLightChildrenIfNeeded:node is null"); return; }' bower_components/polymer/polymer-mini.html
		 sed -e '/saveLightChildrenIfNeeded(\s*c.parentNode\s*)/ {' -e 'r polymer2.patch' -e 'd' -e '}' -i bower_components/polymer/polymer-mini.html
		sed -i 's/console.\(log\|warn\|error\).apply/Function.prototype/' bower_components/polymer/polymer-micro.html
		sed -i 's/console.warn.*//' bower_components/polymer/iron-shadow-flex-layout.html
		sed -i 's/console.warn.*//' bower_components/polymer/iron-flex-layout.html
		sed -i 's!<link rel="stylesheet" href="prism.css">!!' bower_components/prism-element/prism-import.html
		sed '/./d' bower_components/font-roboto/roboto.html
	fi 
fi
if [ ! -d "node_modules" ] ; then
	 if [ -f "bin/node_modules.tgz" ] ; then
		 tar xf bin/node_modules.tgz
	 fi
fi

command -v vulcanize
if [ $? -eq 0 ] ; then
	#last running is 1.14.3 #after this, smooth-scrollbar.css is included in simpl-login.html !!!!
	#look here:https://github.com/Polymer/polymer-bundler/blob/master/CHANGELOG.md
	vulcanize  --strip-comments --inline-css --inline-scripts --inline index.html >domelements.html
	crisper -s domelements.html -h out.html -j out.js
	mv out.html domelements.html
	jscomp.sh -o outnew.js out.js
	mv outnew.js out.js
	RESULT=domelements.html
	sed -i 's!src="out.js"!!' $RESULT
	sed -i 's!</html>$!!' $RESULT
	echo "<script>" >>$RESULT
	cat out.js >>$RESULT
	echo "</script></html>" >>$RESULT
	rm out.js
else
	cp bin/_domelements.html.gz domelements.html.gz
fi
