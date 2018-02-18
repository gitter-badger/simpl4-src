#!/bin/sh

SRCTOPDIR=
DESTTOPDIR=
#########################################################
# usage
#########################################################
usage() {
    echo "usage: $0 -s srcdir -d destdir"
}

#########################################################
# parameter
#########################################################
shortoptions='s:d:'
longoptions='srcdir:destdir:'
getopt=$(getopt -o $shortoptions --longoptions  $longoptions -- "$@")
if [ $? != 0 ]; then
   usage
   exit 1;
fi

eval set -- "$getopt"
while true; do
   case "$1" in
      -d|--destdir)
         shift
         DESTTOPDIR=$1
         shift
      ;;
      -s|--srcdir)
         shift
         SRCTOPDIR=$1
         shift
      ;;
      *)
				break
      ;;
   esac
done

if [ -z "$SRCTOPDIR" ] ; then
	SRCTOPDIR=$TOPDIR/simpl4-src
fi
if [ -z "$DESTTOPDIR" ] ; then
	DESTTOPDIR=$TOPDIR/simpl4-deployed
fi
echo "================="
echo "Copy files ->"
echo "================="
echo  "\t:SRCTOPDIR=$SRCTOPDIR"
echo  "\t:DESTTOPDIR=$DESTTOPDIR"
#########################################################
# branch
#########################################################
cd $SRCTOPDIR
CURRENTBRANCH=`git rev-parse --abbrev-ref HEAD`

cd $DESTTOPDIR
echo "\t:COPY.DEPLOYBRANCH:$DEPLOYBRANCH"
if [ -n "$DEPLOYBRANCH" ] ; then
	git checkout $DEPLOYBRANCH >/dev/null 2>&1
else
	git checkout $CURRENTBRANCH >/dev/null 2>&1
fi
echo  "\t:COPY.SOURCEBRANCH=$CURRENTBRANCH"
#########################################################
# copy
#########################################################
export BUNDLESBUILD="$SRCTOPDIR/build/$CURRENTBRANCH/bundlesBuild"
export REPOSITORY="$SRCTOPDIR/repository"
export LIBDIRECTORY="$DESTTOPDIR/libs"
export WEBDIRECTORY="$DESTTOPDIR/WEB-INF"
export BINDIRECTORY="$DESTTOPDIR/bin"
export ETCDIRECTORY="$DESTTOPDIR/etc"
export SERVERDIRECTORY="$DESTTOPDIR/server"
export CLIENTDIRECTORY="$DESTTOPDIR/client"
export CLIENTLECACYDIRECTORY="$DESTTOPDIR/client/legacy"
export SURFACEDIRECTORY="$DESTTOPDIR/client/surface"
export FROMCLIENT=$SRCTOPDIR/client
export FROMSURFACE=$SRCTOPDIR/surface

mkdir -p ${LIBDIRECTORY}
mkdir -p ${BINDIRECTORY}
mkdir -p ${ETCDIRECTORY}
mkdir -p ${CLIENTLECACYDIRECTORY}/css/images
mkdir -p ${CLIENTLECACYDIRECTORY}/js
cp $REPOSITORY/jdt-compiler-3.1.1.jar ${LIBDIRECTORY}
cp $REPOSITORY/xml-w3c.jar ${LIBDIRECTORY}
cp $REPOSITORY/org.apache.felix.main-4.6.1.jar ${LIBDIRECTORY}
cp $REPOSITORY/jcommander-1.35.jar ${LIBDIRECTORY}
cp $BUNDLESBUILD/org.ms123.common.utils-1.0.0.jar ${LIBDIRECTORY}
cp $BUNDLESBUILD/org.ms123.common.libhelper-1.0.0.jar ${LIBDIRECTORY}
cp $BUNDLESBUILD/org.ms123.launcher-1.0.0.jar ${LIBDIRECTORY}
cp $REPOSITORY/flexjson-2.1.bar ${LIBDIRECTORY}
cp $BUNDLESBUILD/org.ms123.admin-1.0.0.jar ${LIBDIRECTORY}

mkdir -p ${WEBDIRECTORY}
cp $BUNDLESBUILD/org.ms123.launcher-1.0.0.jar ${WEBDIRECTORY}/lib
cp $REPOSITORY/org.apache.felix.main-4.6.1.jar ${WEBDIRECTORY}/lib
cp $REPOSITORY/jetty-proxy-9.2.9.v20150224.jar ${WEBDIRECTORY}/lib
cp $REPOSITORY/pax-web-jetty-bundle-4.1.1.jar ${WEBDIRECTORY}/lib
cp $REPOSITORY/flexjson-2.1.bar ${WEBDIRECTORY}/lib/flexjson-2.1.jar
cp $REPOSITORY/jsoup-1.8.3.jar ${WEBDIRECTORY}/lib
cp $REPOSITORY/joor-0.9.6.jar ${WEBDIRECTORY}/lib
cp $REPOSITORY/unix4j-0.3.1.jar ${WEBDIRECTORY}/lib
cp $REPOSITORY/jgit/jgit-4.10.jar ${WEBDIRECTORY}/lib
cp $REPOSITORY/slf4j-api-1.7.5.jar ${WEBDIRECTORY}/lib
cp $REPOSITORY/slf4j-simple-1.7.5.jar ${WEBDIRECTORY}/lib
cp $REPOSITORY/commons/commons-io-2.5.jar ${WEBDIRECTORY}/lib
cp $SRCTOPDIR/etc/webinf/web.xml ${WEBDIRECTORY}
zip -d ${WEBDIRECTORY}/lib/pax-web-jetty-bundle-4.1.1.jar "javax/*" >/dev/null 


cp $SRCTOPDIR/bin/sw $BINDIRECTORY
cp $SRCTOPDIR/bin/oetl.sh $BINDIRECTORY
cp $SRCTOPDIR/bin/generate.sh $BINDIRECTORY
cp $SRCTOPDIR/bin/setup.sh $BINDIRECTORY
cp $SRCTOPDIR/bin/setup.bat $BINDIRECTORY
cp $SRCTOPDIR/bin/jooq-codegen.sh $BINDIRECTORY
cp $SRCTOPDIR/bin/bash.exe $BINDIRECTORY
cp $SRCTOPDIR/bin/py.exe $BINDIRECTORY
cp $SRCTOPDIR/bin/start.sh.tpl $BINDIRECTORY
cp $SRCTOPDIR/bin/start.cmd $BINDIRECTORY
cp $SRCTOPDIR/bin/createclasses.sh $BINDIRECTORY
cp $SRCTOPDIR/bin/updatemetadata.sh $BINDIRECTORY
cp $SRCTOPDIR/bin/createdb.sh $BINDIRECTORY
cp $SRCTOPDIR/bin/prunsrv.exe $BINDIRECTORY/Simpl4Service.exe
cp $SRCTOPDIR/bin/installservice.bat $BINDIRECTORY
cp $SRCTOPDIR/bin/uninstallservice.bat $BINDIRECTORY
cp $SRCTOPDIR/bin/abspath.sh $BINDIRECTORY
cp $SRCTOPDIR/bin/simpl4-update.sh $BINDIRECTORY

#cp -r $TOPDIR/simpleworkflow/etc/ea $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/shell.init.script $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/users-init-script.sql $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/branding.example $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/jooq-codegen.xml.example  $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/logo.ico $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/logback.xml.tpl $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/cassandra.yaml $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/logging.config.tpl $ETCDIRECTORY/
cp -r $SRCTOPDIR/etc/config $ETCDIRECTORY/
cp -r $SRCTOPDIR/etc/config-templates $ETCDIRECTORY/
cp -r $SRCTOPDIR/etc/gittemplate $ETCDIRECTORY/
cp -r $SRCTOPDIR/etc/images $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/run.bat.tpl $SERVERDIRECTORY/
cp    $SRCTOPDIR/etc/README-deployed.md $DESTDIRECTORY/README.md
cp    $SRCTOPDIR/etc/warbuild.xml $DESTDIRECTORY/build.xml
cp    $SRCTOPDIR/etc/orientdb-server-config.xml $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/orientdb-client-log.properties $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/security.json $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/zookeeper.properties $ETCDIRECTORY/
cp    $SRCTOPDIR/etc/kafkaserver.properties $ETCDIRECTORY/
#cp -r $SRCTOPDIR/etc/openfire $ETCDIRECTORY/


cp $SRCTOPDIR/client/start.html $CLIENTDIRECTORY
cp $SRCTOPDIR/client/mobile.html $CLIENTDIRECTORY
cp $SRCTOPDIR/client/website.html $CLIENTDIRECTORY

cp $FROMCLIENT/legacy/css/xaddr.css $CLIENTLECACYDIRECTORY/css
cp $FROMCLIENT/legacy/css/style.css $CLIENTLECACYDIRECTORY/css
cp $FROMCLIENT/legacy/css/images/* $CLIENTLECACYDIRECTORY/css/images
cp $FROMCLIENT/legacy/css/codemirror.css $CLIENTLECACYDIRECTORY/css
cp $FROMCLIENT/legacy/css/font-awesome.css $CLIENTLECACYDIRECTORY/css
cp $FROMCLIENT/legacy/css/asciidoc.css $CLIENTLECACYDIRECTORY/css
cp $FROMCLIENT/legacy/css/simple-hint.css $CLIENTLECACYDIRECTORY/css
cp $FROMCLIENT/legacy/css/*.ttf $CLIENTLECACYDIRECTORY/css
cp $FROMCLIENT/legacy/js/ckeditor.js.gz $CLIENTLECACYDIRECTORY/js
cp -r $FROMCLIENT/legacy/js/ckeditor $CLIENTLECACYDIRECTORY/js/
cp $FROMCLIENT/legacy/js/codemirror.js.gz $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/codemirror-mode.js.gz $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/simple-hint.js $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/javascript-hint.js $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/clike.js $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/jquery-2.0.3.js.gz $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/jsPlumb-ms.js.gz $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/etc.js.gz $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/openseadragon-all.js.gz $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/pdf-all.js.gz $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/pdf.worker.js.gz $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/vis.min.js.gz $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/js-yaml.js.gz $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/hjson.js.gz $CLIENTLECACYDIRECTORY/js
cp $FROMCLIENT/legacy/js/jsoneditor.js.gz $CLIENTLECACYDIRECTORY/js

#mkdir -p $SURFACEDIRECTORY/bower_components/fontawesome/fonts/
#cp $FROMSURFACE/elements/bower_components/fontawesome/fonts/fontawesome-webfont.woff $SURFACEDIRECTORY/
