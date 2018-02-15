#!/bin/sh

SRCTOPDIR=
DESTTOPDIR=
WAR=
#########################################################
# usage
#########################################################
usage() {
    echo "usage: $0 -s srcdir -d destdir"
}

#########################################################
# parameter
#########################################################
shortoptions='s:d:wa'
longoptions='srcdir:,destdir:,war,appserver'
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
      -a|--appserver)
         WAR=true
         shift
      ;;
      -w|--war)
         WAR=true
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
SERVERDIR="$DESTTOPDIR/server"
export REPOSITORY="$SRCTOPDIR/repository"


#########################################################
# branch
#########################################################
cd $SRCTOPDIR
CURRENTBRANCH=`git rev-parse --abbrev-ref HEAD`

cd $DESTTOPDIR
echo "DEPLOYBRANCH:$DEPLOYBRANCH"
if [ -n "$DEPLOYBRANCH" ] ; then
	git checkout $DEPLOYBRANCH >/dev/null 2>&1
else
	git checkout $CURRENTBRANCH >/dev/null 2>&1
fi

export BUNDLESBUILD="$SRCTOPDIR/build/${CURRENTBRANCH}/bundlesBuild"

#########################################################
# info
#########################################################
echo "================="
echo "Create osgi-env ->"
echo "================="
echo  "\t:SRCTOPDIR=$SRCTOPDIR"
echo  "\t:DESTTOPDIR=$DESTTOPDIR"
echo  "\t:BUNDLEREPOSITORY=$REPOSITORY"
echo  "\t:PAX.DEPLOYBRANCH=$DEPLOYBRANCH"
echo  "\t:PAX.SOURCEBRANCH=$CURRENTBRANCH"
echo  "\t:BUNDLESBUILD=$BUNDLESBUILD"
#########################################################
# main
#########################################################
sed  "s!__REPOSITORY__!$REPOSITORY!" $SRCTOPDIR/etc/felix.tpl.xml >$SRCTOPDIR/etc/felix.xml
rm -fr $SERVERDIR
vmOptions="\
 -Xmx1500m \
 -XX:MaxPermSize=256m \
 -Djava.security.egd=file:/dev/./urandomx -Dworkspace=\$SIMPL4DIR/workspace \
 -Dfile.encoding='UTF-8' \
 -Dorg.apache.felix.eventadmin.Timeout=0 \
 -Dorg.osgi.service.http.port=\$CONTROL_PORT \
 -DdisableCheckForReferencesInContentException=true \
 -Dgit.repos=\$SIMPL4DIR/gitrepos \
 -Dgroovy.target.indy=false \
 -Dsimpl4.dir=\$SIMPL4DIR \
 -Djetty.port=\$JETTY_PORT \
 -Dkaraf.startLocalConsole=true \
 -Dh2.bindAddress=127.0.0.1 \
 -Dkaraf.systemBundlesStartLevel=0 \
 -Dkaraf.startRemoteShell=true \
 -Dkaraf.base=\$SIMPL4DIR \
 -Dfelix.cm.dir=\$SIMPL4DIR/etc/config \
 -Detc.dir=\$SIMPL4DIR/etc \
 -Dkafka.disabled=true \
 -Dfelix.fileinstall.dir=\$SIMPL4DIR/gitrepos/.bundles \
 -Dorg.ops4j.pax.logging.DefaultServiceLog.level=ERROR \
 -Ddrools.dialect.java.compiler=JANINO \
 -Dkaraf.local.roles=admin,manager \
 -DopenfireHome=\$SIMPL4DIR/etc/openfire \
 -Dkaraf.etc=\$SIMPL4DIR/etc/activemq/etc \
 -Dwebconsole.type=properties \
 -Dwebconsole.jms.url=tcp://localhost:61616 \
 -Dwebconsole.jmx.url=service:jmx:rmi:///jndi/rmi://localhost:1098/jmxrmi \
 -Dwebconsole.jmx.user=admin \
 -Dwebconsole.jmx.password=admin \
 -Dwebconsole.jms.user=admin \
 -Dwebconsole.jms.password=admin \
 -DORIENTDB_ROOT_PASSWORD=simpl4 \
 -Dactivemq.data=\$SIMPL4DIR/etc/activemq/data \
 -Dkaraf.shell.init.script=\$SIMPL4DIR/etc/shell.init.script \
"

activitibundles=""
for i in $REPOSITORY/activiti-5.18/*[bj]ar
do
	activitibundles=${activitibundles}"scan-bundle:file:$i "
done

nucleusbundles=""
for i in $REPOSITORY/datanucleus/*[bj]ar
do
	nucleusbundles=${nucleusbundles}"scan-bundle:file:$i "
done

camelbundles=""
for i in $REPOSITORY/camel/*[bj]ar
do
	camelbundles=${camelbundles}"scan-bundle:file:$i "
done

commonsbundles=""
for i in $REPOSITORY/commons/*[bj]ar
do
	commonsbundles=${commonsbundles}"scan-bundle:file:$i "
done

xbeanbundles=""
for i in $REPOSITORY/xbean/*[bj]ar
do
	xbeanbundles=${xbeanbundles}"scan-bundle:file:$i "
done

jdbcbundles=""
for i in $REPOSITORY/jdbc/*[bj]ar
do
	jdbcbundles=${jdbcbundles}"scan-bundle:file:$i "
done

jacksonbundles=""
for i in $REPOSITORY/jackson/*[bj]ar
do
	jacksonbundles=${jacksonbundles}"scan-bundle:file:$i "
done

openfirebundles=""
for i in $REPOSITORY/openfire/*[bj]ar
do
	openfirebundles=${openfirebundles}"scan-bundle:file:$i "
done

jooqbundles=""
for i in $REPOSITORY/jooq/*[bj]ar
do
	jooqbundles=${jooqbundles}"scan-bundle:file:$i "
done

ariesbundles=""
for i in $REPOSITORY/aries/*[bj]ar
do
	ariesbundles=${ariesbundles}"scan-bundle:file:$i "
done

karafbundles=""
for i in $REPOSITORY/karaf4/*[bj]ar
do
	karafbundles=${karafbundles}"scan-bundle:file:$i "
done

springbundles=""
for i in $REPOSITORY/spring/*[bj]ar
do
	springbundles=${springbundles}"scan-bundle:file:$i "
done

vfsbundles=""
for i in $REPOSITORY/vfs/*[bj]ar
do
	vfsbundles=${vfsbundles}"scan-bundle:file:$i "
done

activemqbundles=""
for i in $REPOSITORY/activemq/*[bj]ar
do
	activemqbundles=${activemqbundles}"scan-bundle:file:$i "
done

pdfboxbundles=""
for i in $REPOSITORY/pdfbox/*[bj]ar
do
	pdfboxbundles=${pdfboxbundles}"scan-bundle:file:$i "
done

scalabundles=""
for i in $REPOSITORY/scala/*[bj]ar
do
	scalabundles=${scalabundles}"scan-bundle:file:$i "
done

flowablebundles=""
for i in $REPOSITORY/flowable/*[bj]ar
do
	flowablebundles=${flowablebundles}"scan-bundle:file:$i "
done

camundabundles=""
for i in $REPOSITORY/camunda/*[bj]ar
do
	camundabundles=${camundabundles}"scan-bundle:file:$i "
done

jgitbundles=""
for i in $REPOSITORY/jgit/*[bj]ar
do
	jgitbundles=${jgitbundles}"scan-bundle:file:$i "
done

$SRCTOPDIR/bin/pax-run.sh \
	scan-bundle:file:$REPOSITORY/org.apache.felix.configadmin-1.8.0.jar \
	scan-bundle:file:$REPOSITORY/org.osgi.compendium-5.0.0.jar \
	scan-bundle:file:$REPOSITORY/org.apache.felix.prefs-1.0.4.jar \
	scan-bundle:file:$REPOSITORY/derby-10.5.3.0_1.jar \
	scan-bundle:file:$REPOSITORY/groovy-all-2.4.4-indy.jar \
	scan-bundle:file:$REPOSITORY/javax.mail-1.6.0.jar \
	scan-bundle:file:$REPOSITORY/org.apache.felix.bundlerepository-1.6.4.jar \
	scan-bundle:file:$REPOSITORY/org.apache.felix.eventadmin-1.2.2.jar \
	scan-bundle:file:$REPOSITORY/org.apache.felix.http.bridge-2.0.4.jar \
	scan-bundle:file:$REPOSITORY/org.apache.felix.http.whiteboard-2.0.4.jar \
	scan-bundle:file:$REPOSITORY/org.apache.felix.metatype-1.0.4.jar \
	scan-bundle:file:$REPOSITORY/org.apache.felix.scr-1.6.0.jar \
	scan-bundle:file:$REPOSITORY/org.apache.felix.fileinstall-3.4.2.jar \
	scan-bundle:file:$REPOSITORY/org.apache.felix.webconsole-3.1.2.jar \
	scan-bundle:file:$REPOSITORY/org.apache.felix.webconsole.plugins.event-1.0.2.jar \
	scan-bundle:file:$REPOSITORY/org.apache.felix.webconsole.plugins.memoryusage-1.0.2.jar \
${activitibundles} \
${nucleusbundles} \
${camelbundles} \
${commonsbundles} \
${xbeanbundles} \
${jdbcbundles} \
${jacksonbundles} \
${openfirebundles} \
${jooqbundles} \
${localbundles} \
${ariesbundles} \
${karafbundles} \
${springbundles} \
${vfsbundles} \
${activemqbundles} \
${pdfboxbundles} \
${scalabundles} \
${camundabundles} \
${jgitbundles} \
	scan-bundle:file:$REPOSITORY/pax-web-jetty-bundle-4.1.1.jar@3 \
	scan-bundle:file:$REPOSITORY/pax-web-spi-4.1.1.jar@3 \
	scan-bundle:file:$REPOSITORY/pax-web-jsp-4.1.1.bar@3 \
	scan-bundle:file:$REPOSITORY/javax.servlet.jsp.jstl-1.2.4.bar \
	scan-bundle:file:$REPOSITORY/pax-web-extender-war-4.1.1.jar@3 \
	scan-bundle:file:$REPOSITORY/btm-3.0.0-SNAPSHOT.jar@3 \
	scan-bundle:file:$REPOSITORY/jetty-proxy-9.2.9.v20150224.jar \
  scan-bundle:file:$REPOSITORY/ant-1.8.2.bar \
	scan-bundle:file:$REPOSITORY/antlr.bar \
  scan-bundle:file:$REPOSITORY/antlr-runtime-3.3.bar \
	scan-bundle:file:$REPOSITORY/asm-all-5.0.3.jar \
  scan-bundle:file:$REPOSITORY/aspectjrt-1.8.5.bar \
	scan-bundle:file:$REPOSITORY/biz.aQute.bndlib.jar \
  scan-bundle:file:$REPOSITORY/codemodel-2.4.bar \
  scan-bundle:file:$REPOSITORY/connector-api-1.5.bar \
	scan-bundle:file:$REPOSITORY/DynamicJasper-3.1.8.bar \
	scan-bundle:file:$REPOSITORY/ezmorph-1.0.6.jar \
  scan-bundle:file:$REPOSITORY/flexjson-2.1.bar \
  scan-bundle:file:$REPOSITORY/guava-10.0-rc3.bar \
  scan-bundle:file:$REPOSITORY/guava-18.0.jar@3 \
	scan-bundle:file:$REPOSITORY/h2-1.4.196.jar \
  scan-bundle:file:$REPOSITORY/hibernate-5.0.1.jar \
	scan-bundle:file:$REPOSITORY/hsqldb.jar \
	scan-bundle:file:$REPOSITORY/itext-2.1.7.bar \
  scan-bundle:file:$REPOSITORY/janino.bar \
	scan-bundle:file:$REPOSITORY/jasperreports-4.0.1.jar \
  scan-bundle:file:$REPOSITORY/jaxb-xjc-2.1.9-osgi.jar \
  scan-bundle:file:$REPOSITORY/jdom-1.1.bar \
	scan-bundle:file:$REPOSITORY/jettison-1.2.jar \
  scan-bundle:file:$REPOSITORY/jms-api-1.1-rev-1.bar \
  scan-bundle:file:$REPOSITORY/joda-time-2.8.2.jar \
  scan-bundle:file:$REPOSITORY/jotm-core-2.2.3.jar \
	scan-bundle:file:$REPOSITORY/jsch-0.1.44-1.bar \
	scan-bundle:file:$REPOSITORY/json-lib-2.3-jdk15.jar \
  scan-bundle:file:$REPOSITORY/lucene-core-3.6.2.bar \
	scan-bundle:file:$REPOSITORY/milton-trunk_14-02-12.bar \
  scan-bundle:file:$REPOSITORY/milyn-smooks-all-1.5.jar \
	scan-bundle:file:$REPOSITORY/mina-core-2.0.16.jar \
	scan-bundle:file:$REPOSITORY/ftpserver-core-1.1.0.jar \
	scan-bundle:file:$REPOSITORY/ftplet-api-1.1.0.jar \
  scan-bundle:file:$REPOSITORY/mvel2-2.1.RC1.jar \
	scan-bundle:file:$REPOSITORY/mysql-connector-java-5.1.34-bin.jar \
	scan-bundle:file:$REPOSITORY/object-traverser.bar \
  scan-bundle:file:$REPOSITORY/ops4j-base-lang-1.5.0.jar \
	scan-bundle:file:$REPOSITORY/org.apache.felix.shell-1.4.3.jar \
	scan-bundle:file:$REPOSITORY/jline-3.0.1.jar \
	scan-bundle:file:$REPOSITORY/JavaEWAH-0.7.9.jar \
	scan-bundle:file:$REPOSITORY/org.json_2.0.jar \
	scan-bundle:file:$REPOSITORY/hjson-1.1.4.bar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.auth-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.auth.api-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.data-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.dmn-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.data.api-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.form-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.libhelper-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.store-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.domainobjects-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.domainobjects.api-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.message-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.ea-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.bhs-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.entity-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.entity.api-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.enumeration-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.namespace-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.exporting-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.git-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.rpc-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.importing-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.datamapper-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.jetty-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.nucleus-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.nucleus.api-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.reporting-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.setting-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.setting.api-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.permission-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.permission.api-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.system-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.team-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.team.api-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.utils-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.management-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.stencil.api-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.process-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.process.api-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.workflow-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.workflow.api-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.activiti-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.camel-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.camel.api-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.docbook-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.smtp-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.xmpp-1.0.0.jar \
  scan-bundle:file:$BUNDLESBUILD/org.ms123.common.wamp-1.0.0.jar \
	scan-bundle:file:$REPOSITORY/org.apache.sling.commons.compiler-2.0.2.jar \
	scan-bundle:file:$REPOSITORY/ostermillerutils_1_07_00.jar \
  scan-bundle:file:$REPOSITORY/pax-swissbox-extender-1.8.0.jar \
  scan-bundle:file:$REPOSITORY/pax-swissbox-lifecycle-1.8.0.jar \
  scan-bundle:file:$REPOSITORY/pax-swissbox-core-1.8.0.jar \
  scan-bundle:file:$REPOSITORY/pax-logging-logback-1.9.1.jar@2 \
  scan-bundle:file:$REPOSITORY/pax-logging-api-1.9.1.jar@2 \
  scan-bundle:file:$REPOSITORY/commons.compiler-2.6.1.jar \
  scan-bundle:file:$REPOSITORY/phonet4j-1.0.2.bar \
	scan-bundle:file:$REPOSITORY/poi-3.8beta4-all.bar \
  scan-bundle:file:$REPOSITORY/rngom-20100718.bar \
	scan-bundle:file:$REPOSITORY/scala-library.bar \
  scan-bundle:file:$REPOSITORY/secondstring-20140225.bar \
  scan-bundle:file:$REPOSITORY/shiro-aspectj-1.2.0.jar \
  scan-bundle:file:$REPOSITORY/shiro-core-1.2.0.jar \
	scan-bundle:file:$REPOSITORY/xdocreport-1.0.5.jar \
	scan-bundle:file:$REPOSITORY/freemarker-2.3.33.jar \
	scan-bundle:file:$REPOSITORY/xerces-2.11.0.jar \
	scan-bundle:file:$REPOSITORY/java-rdfa-0.4.2.bar \
	scan-bundle:file:$REPOSITORY/jena-arq-2.13.0.bar \
	scan-bundle:file:$REPOSITORY/jena-core-2.13.0.bar \
	scan-bundle:file:$REPOSITORY/jena-iri-1.1.2.bar \
  scan-bundle:file:$REPOSITORY/itextpdf-5.1.0.jar \
  scan-bundle:file:$REPOSITORY/odfdom-java-0.8.10-incubating.bar \
  scan-bundle:file:$REPOSITORY/org.odftoolkit.odfdom.converter.pdf.itext5-1.0.5.jar \
  scan-bundle:file:$REPOSITORY/org.odftoolkit.odfdom.converter.core-1.0.5.jar \
	scan-bundle:file:$REPOSITORY/velocity-1.7.jar \
	scan-bundle:file:$REPOSITORY/oro-2.0.8.bar \
  scan-bundle:file:$REPOSITORY/sojo-1.0.0.bar \
	scan-bundle:file:$REPOSITORY/sshd-core-0.12.0.jar \
	scan-bundle:file:$REPOSITORY/sshd-core-1.3.0.jar \
	scan-bundle:file:$REPOSITORY/bcpg-jdk15on-1.55.jar \
	scan-bundle:file:$REPOSITORY/bcprov-jdk15on-1.55.jar \
	scan-bundle:file:$REPOSITORY/bcpkix-jdk15on-1.55.jar \
  scan-bundle:file:$REPOSITORY/tika-core-1.5-SNAPSHOT.jar \
  scan-bundle:file:$REPOSITORY/tika-bundle-1.5-SNAPSHOT.jar \
  scan-bundle:file:$REPOSITORY/xom-1.2.7.jar \
	scan-bundle:file:$REPOSITORY/parboiled-all-1.1.4.bar \
	scan-bundle:file:$REPOSITORY/pegdown-1.2.1.bar \
	scan-bundle:file:$REPOSITORY/fop-all.2.0ms.bar \
	scan-bundle:file:$REPOSITORY/saxon94.jar \
	scan-bundle:file:$REPOSITORY/woodstox-core-asl-4.1.5.jar \
	scan-bundle:file:$REPOSITORY/stax2-api-3.1.1.jar \
	scan-bundle:file:$REPOSITORY/avalon-framework-4.2.0.bar \
	scan-bundle:file:$REPOSITORY/sweble-all.1.1.0.bar \
	scan-bundle:file:$REPOSITORY/javassist-3.18.0-GA.jar \
	scan-bundle:file:$REPOSITORY/javassist-3.21.0-GA.jar \
	scan-bundle:file:$REPOSITORY/UserAgentUtils-1.9-snapshot.bar \
	scan-bundle:file:$REPOSITORY/herold-6.1.0.bar  \
	scan-bundle:file:$REPOSITORY/xstream-1.4.7.bar  \
	scan-bundle:file:$REPOSITORY/xpp3-1.1.4c.bar \
	scan-bundle:file:$REPOSITORY/jettison-1.3.5.jar \
	scan-bundle:file:$REPOSITORY/opencsv-2.3.bar \
	scan-bundle:file:$REPOSITORY/gson-2.7.jar \
	scan-bundle:file:$REPOSITORY/xmlbeans-2.6ms.bar \
	scan-bundle:file:$REPOSITORY/httpclient-osgi-4.3.1.jar \
	scan-bundle:file:$REPOSITORY/httpcore-osgi-4.3.jar \
	scan-bundle:file:$REPOSITORY/concurrentlinkedhashmap-lru-1.4.jar  \
	scan-bundle:file:$REPOSITORY/redmine-java-api-1.23.jar  \
	scan-bundle:file:$REPOSITORY/org.everit.osgi.bundles.javax.sql-4.1.0.jar \
	scan-bundle:file:$REPOSITORY/quartz-2.2.1.jar  \
	scan-bundle:file:$REPOSITORY/jcommander-1.35.jar \
	scan-bundle:file:$REPOSITORY/rxjava-1.2.1.jar \
	scan-bundle:file:$REPOSITORY/snakeyaml-1.13.bar \
	scan-bundle:file:$REPOSITORY/asciidoctorj-1.5.2.bar \
	scan-bundle:file:$REPOSITORY/schemacrawler-14.07.05.jar \
	scan-bundle:file:$REPOSITORY/management-api-1.1.bar \
	scan-bundle:file:$REPOSITORY/asciidoctorj-groovy-dsl.bar \
	scan-bundle:file:$REPOSITORY/orientdb-groovy-0.1.2.jar \
	scan-bundle:file:$REPOSITORY/EasyFlow-1.3.2.bar \
	scan-bundle:file:$REPOSITORY/jsoup-1.8.3.jar \
	scan-bundle:file:$REPOSITORY/unix4j-0.3.1.jar \
	scan-bundle:file:$REPOSITORY/jcabi-1.0.jar \
	scan-bundle:file:$REPOSITORY/dom4j-1.6.1.bar \
	scan-bundle:file:$REPOSITORY/imgscalr-lib-4.2.bar \
	scan-bundle:file:$REPOSITORY/hazelcast-3.6.3.jar \
	scan-bundle:file:$REPOSITORY/zookeeper-3.4.9.jar \
	scan-bundle:file:$REPOSITORY/OrientDBService-1.0.0.jar \
	scan-bundle:file:$REPOSITORY/KafkaService-1.0.0.jar \
	scan-bundle:file:$REPOSITORY/joor-0.9.6.jar \
	scan-bundle:file:$REPOSITORY/juel-2.1.0.bar \
	scan-bundle:file:$REPOSITORY/jchronic-0.2.6.bar \
	scan-bundle:file:$REPOSITORY/commonmark-ext-gfm-tables-0.9.0.bar \
	scan-bundle:file:$REPOSITORY/commonmark-0.9.0.bar \
	scan-bundle:file:$REPOSITORY/reflections-0.9.11.bar \
	scan-bundle:file:$REPOSITORY/guava-20.0.jar \
	scan-bundle:file:$REPOSITORY/apache-lucene-5.3.2.jar \
	scan-bundle:file:$REPOSITORY/java-uuid-generator-3.1.3.jar \
	scan-bundle:file:$REPOSITORY/camunda-engine-orientdb-0.0.1.jar \
	scan-bundle:file:$REPOSITORY/jruby-complete-1.7.16.1.jar \
	--executor=script \
	--workingDirectory=${SERVERDIR} \
	--platform=f \
	--nologo=true \
	--shell= \
	--log=ERROR \
	--definitionURL=file:${SRCTOPDIR}/etc/felix.xml \
	--classpath='$SIMPL4DIR/libs/jdt-compiler-3.1.1.jar:$SIMPL4DIR/libs/xml-w3c.jar' \
	--vmOptions="$vmOptions"


rm -f $SRCTOPDIR/etc/felix.xml 
if [ -n "$APPSERVER" ] ; then
	sed -i "s/javax.transaction.xa,/javax.transaction.xa;version=1.1.0,/g" $SERVERDIR/felix/config.ini
	sed -i "s/javax.transaction,/javax.transaction;version=1.1.0,/g" $SERVERDIR/felix/config.ini
	sed -i "/org.apache.geronimo.specs.geronimo-jta/d" $SERVERDIR/felix/config.ini
else
	sed -i "s/javax.transaction.xa/dummy/g" $SERVERDIR/felix/config.ini
	sed -i "s/javax.transaction/dummy/g" $SERVERDIR/felix/config.ini
	sed -i "s/,javax.sql,/,dummy,/g" $SERVERDIR/felix/config.ini
fi
chmod +x $SERVERDIR/run.sh

sed -i 's/startLocalConsole=true/startLocalConsole=$START_CONSOLE/' $SERVERDIR/run.sh
sed -i 's/^java/exec java/' $SERVERDIR/run.sh

sed -i 's/\$SIMPL4DIR/%SIMPL4DIR%/g' $SERVERDIR/run.bat
sed -i 's/\$JETTY_PORT/%JETTY_PORT%/g' $SERVERDIR/run.bat
sed -i 's/\$CONTROL_PORT/%CONTROL_PORT%/g' $SERVERDIR/run.bat
sed -i 's/startLocalConsole=true/startLocalConsole=false/g' $SERVERDIR/run.bat
sed -i 's/org.apache.felix.main.Main.*/org.apache.felix.main.Main 1>..\\log\\run.log 2>\&1/' $SERVERDIR/run.bat

cat >$SERVERDIR/run.bat.tpl <<END-OF-RUNBAT
@echo off
title SIMPFLO 
setx SIMPL4DIR _BASEDIR_
set SIMPL4DIR=_BASEDIR_
set JETTY_PORT=80
set CONTROL_PORT=8070
echo %SIMPL4DIR%\\server
cd %SIMPL4DIR%\\server
rem
END-OF-RUNBAT

cat $SERVERDIR/run.bat >> $SERVERDIR/run.bat.tpl

rm $SERVERDIR/run.bat




