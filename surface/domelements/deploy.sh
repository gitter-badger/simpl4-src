#!/bin/sh
make-all.sh
#html-beautify -r domelements.html
gzip -f domelements.html
gzip -f domelements.js
#scp domelements.html.gz  ms:$simpl4first/client/surface/
#scp domelements.html.gz  vz5:$wawidev/client/surface/
scp domelements.html.gz domelements.js.gz vz5:$flowabledev/client/surface/
