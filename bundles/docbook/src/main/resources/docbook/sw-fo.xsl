<!--
  Generates single FO document from DocBook XML source using DocBook XSL
  stylesheets.

  See xsl-stylesheets/fo/param.xsl for all parameters.

  NOTE: The URL reference to the current DocBook XSL stylesheets is
  rewritten to point to the copy on the local disk drive by the XML catalog
  rewrite directives so it doesn't need to go out to the Internet for the
  stylesheets. This means you don't need to edit the <xsl:import> elements on
  a machine by machine basis.
-->
<xsl:stylesheet version="1.0"
                xmlns:d="http://docbook.org/ns/docbook"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">
<xsl:import href="docbook/fo/docbook.xsl"/>

<xsl:param name="body.font.family" select="'serif'"/>
<xsl:param name="body.font.master">8</xsl:param>
<xsl:param name="body.font.size">
 <xsl:value-of select="$body.font.master"/><xsl:text>pt</xsl:text>
</xsl:param>

<xsl:template match="d:para[starts-with(@role , 'hf_')]">
  <fo:block>
    <xsl:attribute name="id">
      <xsl:value-of select="@role" />
    </xsl:attribute>
    <xsl:apply-templates />
  </fo:block>
</xsl:template>

<xsl:template match="d:nullpara[starts-with(@role , 'hf_')]">
  <fo:block>
    <xsl:attribute name="id">
      <xsl:value-of select="@role" />
    </xsl:attribute>
    <xsl:apply-templates />
  </fo:block>
</xsl:template>

<xsl:template match="d:nullpara">
  <fo:block>
    <xsl:apply-templates />
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'marker']">
  <fo:block>
    <fo:marker>
      <xsl:attribute name="marker-class-name">
        <xsl:value-of select="@name" />
      </xsl:attribute>
      <xsl:apply-templates />
    </fo:marker>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'retrieve']">
  <fo:block>
    <fo:retrieve-marker retrieve-position="first-including-carryover" retrieve-boundary="page-sequence">
      <xsl:attribute name="retrieve-class-name">
        <xsl:value-of select="@name" />
      </xsl:attribute>
    </fo:retrieve-marker>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'pagenum']">
  <fo:block xsl:use-attribute-sets="normal.para.spacing">
    <xsl:apply-templates/>
  	<fo:page-number/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'narrowBold']">
  <fo:block font-size="{@size}" font-weight="bold" xsl:use-attribute-sets="small.para.spacing">
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'narrow']">
  <fo:block font-size="{@size}" font-weight="normal" xsl:use-attribute-sets="small.para.spacing">
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'wideBold']">
  <fo:block font-size="{@size}" font-weight="bold" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'wide']">
  <fo:block font-size="{@size}" font-weight="normal" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'small']">
  <fo:block font-size="75%" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'big']">
  <fo:block font-size="125%" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'big2']">
  <fo:block font-size="140%" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'big3']">
  <fo:block font-size="165%" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'bold']">
  <fo:block font-weight="bold" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'smallbold']">
  <fo:block font-size="75%" font-weight="bold" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'bigbold']">
  <fo:block font-size="125%" font-weight="bold" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'big2bold']">
  <fo:block font-size="140%" font-weight="bold" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'big3bold']">
  <fo:block font-size="165%" font-weight="bold" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'italic']">
  <fo:block font-style="italic" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'smallitalic']">
  <fo:block font-size="75%" font-style="italic" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'bigitalic']">
  <fo:block font-size="125%" font-style="italic" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'big2italic']">
  <fo:block font-size="140%" font-style="italic" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="d:para[@role = 'big3italic']">
  <fo:block font-size="165%" font-style="italic" xsl:use-attribute-sets="normal.para.spacing">
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<xsl:attribute-set name="small.para.spacing">
  <xsl:attribute name="space-before.optimum">0.125em</xsl:attribute>
  <xsl:attribute name="space-before.minimum">0.1em</xsl:attribute>
  <xsl:attribute name="space-before.maximum">0.2em</xsl:attribute>
</xsl:attribute-set>
<xsl:template match="d:smallpara">
  <xsl:variable name="keep.together">
    <xsl:call-template name="pi.dbfo_keep-together"/>
  </xsl:variable>
  <fo:block xsl:use-attribute-sets="small.para.spacing">
    <xsl:if test="$keep.together != ''">
      <xsl:attribute name="keep-together.within-column"><xsl:value-of
                      select="$keep.together"/></xsl:attribute>
    </xsl:if>
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:attribute-set name="section.title.level1.properties">
  <xsl:attribute name="font-size">
    <xsl:value-of select="$body.font.master * 1.3"/>
    <xsl:text>pt</xsl:text>
  </xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="section.title.level2.properties">
  <xsl:attribute name="font-size">
    <xsl:value-of select="$body.font.master * 1.15"/>
    <xsl:text>pt</xsl:text>
  </xsl:attribute>
</xsl:attribute-set>

<xsl:param name="body.start.indent">0pt</xsl:param>


<xsl:param name="header.left.content"/>
<xsl:param name="header.right.content"/>
<xsl:template name="header.content">  
  <xsl:param name="pageclass" select="''"/>
  <xsl:param name="sequence" select="''"/>
  <xsl:param name="position" select="''"/>
  <xsl:param name="gentext-key" select="''"/>

  <fo:block font-size="120%" font-weight="bold">  
    <xsl:choose>
      <xsl:when test="$position='left'">
     		<xsl:copy-of select="$header.left.content"/>
      </xsl:when>
      <xsl:when test="$position='right'">
     		<xsl:copy-of select="$header.right.content"/>
      </xsl:when>
    </xsl:choose>
  </fo:block>
</xsl:template>



<xsl:param name="paper.type">A4</xsl:param>


<xsl:param name="table.cell.border.style" select="'dotted'"/>
<xsl:param name="table.cell.border.thickness" select="'0.4pt'"/>
<xsl:param name="table.cell.border.color" select="'#777777'"/>

<xsl:param name="table.frame.border.color" select="'#777777'"/>
<xsl:param name="table.frame.border.style" select="'solid'"/>
<xsl:param name="table.frame.border.thickness" select="'0.6pt'"/>
<xsl:param name="tablecolumns.extension" select="'1'"/>

<xsl:attribute-set name="table.cell.padding">
  <xsl:attribute name="padding-start">1.5pt</xsl:attribute>
  <xsl:attribute name="padding-end">1pt</xsl:attribute>
  <xsl:attribute name="padding-top">1pt</xsl:attribute>
  <xsl:attribute name="padding-bottom">1pt</xsl:attribute>
</xsl:attribute-set>

<!--xsl:attribute-set name="informal.object.properties">
  <xsl:attribute name="space-before.minimum">0em</xsl:attribute>
  <xsl:attribute name="space-before.optimum">1em</xsl:attribute>
  <xsl:attribute name="space-before.maximum">1em</xsl:attribute>
  <xsl:attribute name="space-after.minimum">0em</xsl:attribute>
  <xsl:attribute name="space-after.optimum">1em</xsl:attribute>
  <xsl:attribute name="space-after.maximum">1em</xsl:attribute>
</xsl:attribute-set-->

<xsl:template name="table.cell.block.properties">
  <!-- highlight this entry? -->
  <xsl:choose>
    <xsl:when test="ancestor::d:thead or ancestor::d:tfoot">
      <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:when>
    <!-- Make row headers bold too -->
    <xsl:when test="ancestor::d:tbody and 
                    (ancestor::d:table[@rowheader = 'firstcol'] or
                    ancestor::d:informaltable[@rowheader = 'firstcol']) and
                    ancestor-or-self::d:entry[1][count(preceding-sibling::d:entry) = 0]">
      <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:attribute-set name="table.properties">
  <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
</xsl:attribute-set>

<xsl:template match="processing-instruction('hard-pagebreak')">
   <fo:block break-before='page'/>
 </xsl:template>

<!--Pagemaster with different dimensions @@@MS-->
<xsl:param name="region.before.extent">0pt</xsl:param>
<xsl:param name="region.after.extent">0pt</xsl:param>
<xsl:param name="page.margin.top">0.10in</xsl:param>
<xsl:param name="page.margin.bottom">0.10in</xsl:param>
<xsl:param name="body.margin.top">0.25in</xsl:param>
<xsl:param name="body.margin.bottom">0.25in</xsl:param>

<xsl:param name="region.before.extent.odd">0pt</xsl:param>
<xsl:param name="region.after.extent.odd">0pt</xsl:param>
<xsl:param name="page.margin.top.odd">0.10in</xsl:param>
<xsl:param name="page.margin.bottom.odd">0.10in</xsl:param>
<xsl:param name="body.margin.top.odd">0.25in</xsl:param>
<xsl:param name="body.margin.bottom.odd">0.25in</xsl:param>

<xsl:param name="region.before.extent.even">0pt</xsl:param>
<xsl:param name="region.after.extent.even">0pt</xsl:param>
<xsl:param name="page.margin.top.even">0.10in</xsl:param>
<xsl:param name="page.margin.bottom.even">0.10in</xsl:param>
<xsl:param name="body.margin.top.even">0.25in</xsl:param>
<xsl:param name="body.margin.bottom.even">0.25in</xsl:param>

<xsl:param name="first.region.body.border"></xsl:param>
<xsl:param name="first.region.body.border.left"></xsl:param>
<xsl:param name="first.region.body.border.right"></xsl:param>
<xsl:param name="first.region.body.border.top"></xsl:param>
<xsl:param name="first.region.body.border.bottom"></xsl:param>

<xsl:param name="first.region.before.border"></xsl:param>
<xsl:param name="first.region.before.border.left"></xsl:param>
<xsl:param name="first.region.before.border.right"></xsl:param>
<xsl:param name="first.region.before.border.top"></xsl:param>
<xsl:param name="first.region.before.border.bottom"></xsl:param>

<xsl:param name="first.region.after.border"></xsl:param>
<xsl:param name="first.region.after.border.left"></xsl:param>
<xsl:param name="first.region.after.border.right"></xsl:param>
<xsl:param name="first.region.after.border.top"></xsl:param>
<xsl:param name="first.region.after.border.bottom"></xsl:param>

<xsl:param name="odd.region.body.border"></xsl:param>
<xsl:param name="odd.region.body.border.left"></xsl:param>
<xsl:param name="odd.region.body.border.right"></xsl:param>
<xsl:param name="odd.region.body.border.top"></xsl:param>
<xsl:param name="odd.region.body.border.bottom"></xsl:param>

<xsl:param name="odd.region.before.border"></xsl:param>
<xsl:param name="odd.region.before.border.left"></xsl:param>
<xsl:param name="odd.region.before.border.right"></xsl:param>
<xsl:param name="odd.region.before.border.top"></xsl:param>
<xsl:param name="odd.region.before.border.bottom"></xsl:param>

<xsl:param name="odd.region.after.border"></xsl:param>
<xsl:param name="odd.region.after.border.left"></xsl:param>
<xsl:param name="odd.region.after.border.right"></xsl:param>
<xsl:param name="odd.region.after.border.top"></xsl:param>
<xsl:param name="odd.region.after.border.bottom"></xsl:param>

<xsl:param name="even.region.body.border"></xsl:param>
<xsl:param name="even.region.body.border.left"></xsl:param>
<xsl:param name="even.region.body.border.right"></xsl:param>
<xsl:param name="even.region.body.border.top"></xsl:param>
<xsl:param name="even.region.body.border.bottom"></xsl:param>

<xsl:param name="even.region.before.border"></xsl:param>
<xsl:param name="even.region.before.border.left"></xsl:param>
<xsl:param name="even.region.before.border.right"></xsl:param>
<xsl:param name="even.region.before.border.top"></xsl:param>
<xsl:param name="even.region.before.border.bottom"></xsl:param>

<xsl:param name="even.region.after.border"></xsl:param>
<xsl:param name="even.region.after.border.left"></xsl:param>
<xsl:param name="even.region.after.border.right"></xsl:param>
<xsl:param name="even.region.after.border.top"></xsl:param>
<xsl:param name="even.region.after.border.bottom"></xsl:param>

<xsl:param name="first.region.before.display.align">before</xsl:param>
<xsl:param name="first.region.after.display.align">before</xsl:param>

<xsl:param name="odd.region.before.display.align">before</xsl:param>
<xsl:param name="odd.region.after.display.align">before</xsl:param>

<xsl:param name="even.region.before.display.align">before</xsl:param>
<xsl:param name="even.region.after.display.align">before</xsl:param>
<xsl:param name="show.bookmarks">0</xsl:param>
<xsl:param name="marker.section.level">0</xsl:param>

<xsl:attribute-set name="informal.object.properties">
  <xsl:attribute name="space-before.minimum">0.0em</xsl:attribute>
  <xsl:attribute name="space-before.optimum">0em</xsl:attribute>
  <xsl:attribute name="space-before.maximum">0em</xsl:attribute>
  <xsl:attribute name="space-after.minimum">0em</xsl:attribute>
  <xsl:attribute name="space-after.optimum">0em</xsl:attribute>
  <xsl:attribute name="space-after.maximum">0em</xsl:attribute>
</xsl:attribute-set>

<xsl:template name="select.user.pagemaster">
  <xsl:param name="element" />
  <xsl:param name="pageclass" />
  <xsl:param name="default-pagemaster" />
  <xsl:value-of select="'body-ms'" />
</xsl:template>

<xsl:template name="user.pagemasters">
	<fo:page-sequence-master master-name="body-ms">
		<fo:repeatable-page-master-alternatives>
			<fo:conditional-page-master-reference master-reference="blank" blank-or-not-blank="blank" />
			<fo:conditional-page-master-reference master-reference="body-first-ms" page-position="first" />
			<fo:conditional-page-master-reference master-reference="body-odd-ms" odd-or-even="odd" />
			<fo:conditional-page-master-reference odd-or-even="even">
				<xsl:attribute name="master-reference">
					<xsl:choose>
						<xsl:when test="$double.sided != 0">body-even-ms</xsl:when>
						<xsl:otherwise>body-odd-ms</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
			</fo:conditional-page-master-reference>
		</fo:repeatable-page-master-alternatives>
	</fo:page-sequence-master>

  <!-- body pages -->
  <fo:simple-page-master master-name="body-first-ms" page-width="{$page.width}" page-height="{$page.height}" margin-top="{$page.margin.top}" margin-bottom="{$page.margin.bottom}">
    <xsl:attribute name="margin-{$direction.align.start}">
      <xsl:value-of select="$page.margin.inner" />
    </xsl:attribute>
    <xsl:attribute name="margin-{$direction.align.end}">
      <xsl:value-of select="$page.margin.outer" />
    </xsl:attribute>
    <fo:region-body 
					border="{$first.region.body.border}" 
					border-left="{$first.region.body.border.left}" 
					border-right="{$first.region.body.border.right}" 
					border-top="{$first.region.body.border.top}" 
					border-bottom="{$first.region.body.border.bottom}" 
				margin-bottom="{$body.margin.bottom}" margin-top="{$body.margin.top}" column-gap="{$column.gap.body}" column-count="{$column.count.body}">
      <xsl:attribute name="margin-{$direction.align.start}">
        <xsl:value-of select="$body.margin.inner" />
      </xsl:attribute>
      <xsl:attribute name="margin-{$direction.align.end}">
        <xsl:value-of select="$body.margin.outer" />
      </xsl:attribute>
    </fo:region-body>
    <fo:region-before 
					border="{$first.region.before.border}" 
					border-left="{$first.region.before.border.left}" 
					border-right="{$first.region.before.border.right}" 
					border-top="{$first.region.before.border.top}" 
					border-bottom="{$first.region.before.border.bottom}" 
					region-name="xsl-region-before-first" extent="{$region.before.extent}" precedence="{$region.before.precedence}" display-align="{$first.region.before.display.align}" />
    <fo:region-after 
					border="{$first.region.after.border}" 
					border-left="{$first.region.after.border.left}" 
					border-right="{$first.region.after.border.right}" 
					border-top="{$first.region.after.border.top}" 
					border-bottom="{$first.region.after.border.bottom}" 
					region-name="xsl-region-after-first" extent="{$region.after.extent}" precedence="{$region.after.precedence}" display-align="{$first.region.after.display.align}" />
    <xsl:call-template name="region.inner">
      <xsl:with-param name="sequence">first</xsl:with-param>
      <xsl:with-param name="pageclass">body</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="region.outer">
      <xsl:with-param name="sequence">first</xsl:with-param>
      <xsl:with-param name="pageclass">body</xsl:with-param>
    </xsl:call-template>
  </fo:simple-page-master>

  <fo:simple-page-master master-name="body-odd-ms" page-width="{$page.width}" page-height="{$page.height}" margin-top="{$page.margin.top.odd}" margin-bottom="{$page.margin.bottom.odd}">
    <xsl:attribute name="margin-{$direction.align.start}">
      <xsl:value-of select="$page.margin.inner" />
    </xsl:attribute>
    <xsl:attribute name="margin-{$direction.align.end}">
      <xsl:value-of select="$page.margin.outer" />
    </xsl:attribute>
    <fo:region-body 
					border="{$odd.region.body.border}" 
					border-left="{$odd.region.body.border.left}" 
					border-right="{$odd.region.body.border.right}" 
					border-top="{$odd.region.body.border.top}" 
					border-bottom="{$odd.region.body.border.bottom}" 
					margin-bottom="{$body.margin.bottom.odd}" margin-top="{$body.margin.top.odd}" column-gap="{$column.gap.body}" column-count="{$column.count.body}">
      <xsl:attribute name="margin-{$direction.align.start}">
        <xsl:value-of select="$body.margin.inner" />
      </xsl:attribute>
      <xsl:attribute name="margin-{$direction.align.end}">
        <xsl:value-of select="$body.margin.outer" />
      </xsl:attribute>
    </fo:region-body>
    <fo:region-before 
					border="{$odd.region.before.border}" 
					border-left="{$odd.region.before.border.left}" 
					border-right="{$odd.region.before.border.right}" 
					border-top="{$odd.region.before.border.top}" 
					border-bottom="{$odd.region.before.border.bottom}" 
					region-name="xsl-region-before-odd" extent="{$region.before.extent.odd}" precedence="{$region.before.precedence}" display-align="{$odd.region.before.display.align}" />
    <fo:region-after 
					border="{$odd.region.after.border}" 
					border-left="{$odd.region.after.border.left}" 
					border-right="{$odd.region.after.border.right}" 
					border-top="{$odd.region.after.border.top}" 
					border-bottom="{$odd.region.after.border.bottom}" 
					region-name="xsl-region-after-odd" extent="{$region.after.extent.odd}" precedence="{$region.after.precedence}" display-align="{$odd.region.after.display.align}" />
    <xsl:call-template name="region.inner">
      <xsl:with-param name="pageclass">body</xsl:with-param>
      <xsl:with-param name="sequence">odd</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="region.outer">
      <xsl:with-param name="pageclass">body</xsl:with-param>
      <xsl:with-param name="sequence">odd</xsl:with-param>
    </xsl:call-template>
  </fo:simple-page-master>

  <fo:simple-page-master master-name="body-even-ms" page-width="{$page.width}" page-height="{$page.height}" margin-top="{$page.margin.top.even}" margin-bottom="{$page.margin.bottom.even}">
    <xsl:attribute name="margin-{$direction.align.start}">
      <xsl:value-of select="$page.margin.outer" />
    </xsl:attribute>
    <xsl:attribute name="margin-{$direction.align.end}">
      <xsl:value-of select="$page.margin.inner" />
    </xsl:attribute>
    <fo:region-body  
					border="{$even.region.body.border}" 
					border-left="{$even.region.body.border.left}" 
					border-right="{$even.region.body.border.right}" 
					border-top="{$even.region.body.border.top}" 
					border-bottom="{$even.region.body.border.bottom}" 
					margin-bottom="{$body.margin.bottom.even}" margin-top="{$body.margin.top.even}" column-gap="{$column.gap.body}" column-count="{$column.count.body}">
      <xsl:attribute name="margin-{$direction.align.start}">
        <xsl:value-of select="$body.margin.outer" />
      </xsl:attribute>
      <xsl:attribute name="margin-{$direction.align.end}">
        <xsl:value-of select="$body.margin.inner" />
      </xsl:attribute>
    </fo:region-body>
    <fo:region-before 
					border="{$even.region.before.border}" 
					border-left="{$even.region.before.border.left}" 
					border-right="{$even.region.before.border.right}" 
					border-top="{$even.region.before.border.top}" 
					border-bottom="{$even.region.before.border.bottom}" 
					region-name="xsl-region-before-even" extent="{$region.before.extent.even}" precedence="{$region.before.precedence}" display-align="{$even.region.before.display.align}" />
    <fo:region-after 
					border="{$even.region.after.border}" 
					border-left="{$even.region.after.border.left}" 
					border-right="{$even.region.after.border.right}" 
					border-top="{$even.region.after.border.top}" 
					border-bottom="{$even.region.after.border.bottom}" 
					region-name="xsl-region-after-even" extent="{$region.after.extent.even}" precedence="{$region.after.precedence}" display-align="{$even.region.after.display.align}" />
    <xsl:call-template name="region.outer">
      <xsl:with-param name="pageclass">body</xsl:with-param>
      <xsl:with-param name="sequence">even</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="region.inner">
      <xsl:with-param name="pageclass">body</xsl:with-param>
      <xsl:with-param name="sequence">even</xsl:with-param>
    </xsl:call-template>
  </fo:simple-page-master>
</xsl:template>



<xsl:attribute-set name="table.cell.nopadding">
  <xsl:attribute name="padding-start">0pt</xsl:attribute>
  <xsl:attribute name="padding-end">0pt</xsl:attribute>
  <xsl:attribute name="padding-top">0pt</xsl:attribute>
  <xsl:attribute name="padding-bottom">0pt</xsl:attribute>
</xsl:attribute-set>


<xsl:template match="d:entry[@role = 'nopadding']" name="entry">
  <xsl:param name="col" select="1"/>
  <xsl:param name="spans"/>

  <xsl:variable name="row" select="parent::d:row"/>
  <xsl:variable name="group" select="$row/parent::*[1]"/>
  <xsl:variable name="frame" select="ancestor::d:tgroup/parent::*/@frame"/>

  <xsl:variable name="empty.cell" select="count(node()) = 0"/>

  <xsl:variable name="named.colnum">
    <xsl:call-template name="entry.colnum"/>
  </xsl:variable>

  <xsl:variable name="entry.colnum">
    <xsl:choose>
      <xsl:when test="$named.colnum &gt; 0">
        <xsl:value-of select="$named.colnum"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$col"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="entry.colspan">
    <xsl:choose>
      <xsl:when test="@spanname or @namest">
        <xsl:call-template name="calculate.colspan"/>
      </xsl:when>
      <xsl:otherwise>1</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="following.spans">
    <xsl:call-template name="calculate.following.spans">
      <xsl:with-param name="colspan" select="$entry.colspan"/>
      <xsl:with-param name="spans" select="$spans"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="rowsep">
    <xsl:choose>
      <!-- If this is the last row, rowsep never applies (except when 
           the ancestor tgroup has a following sibling tgroup) -->
      <xsl:when test="not(ancestor-or-self::d:row[1]/following-sibling::d:row
                          or ancestor-or-self::d:thead/following-sibling::d:tbody
                          or ancestor-or-self::d:tbody/preceding-sibling::d:tfoot)
                          and not(ancestor::d:tgroup/following-sibling::d:tgroup)">
        <xsl:value-of select="0"/>
      </xsl:when>
      <!-- Check for morerows too -->
      <xsl:when test="(@morerows and count(ancestor-or-self::d:row[1]/
                       following-sibling::d:row) = @morerows )
                      and not (ancestor-or-self::d:thead/following-sibling::d:tbody
                       or ancestor-or-self::d:tbody/preceding-sibling::d:tfoot)
                       and not(ancestor::d:tgroup/following-sibling::d:tgroup)">
        <xsl:value-of select="0"/>
      </xsl:when>

      <xsl:otherwise>
        <xsl:call-template name="inherited.table.attribute">
          <xsl:with-param name="entry" select="."/>
          <xsl:with-param name="colnum" select="$entry.colnum"/>
          <xsl:with-param name="attribute" select="'rowsep'"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

<!--
  <xsl:message><xsl:value-of select="."/>: <xsl:value-of select="$rowsep"/></xsl:message>
-->

  <xsl:variable name="colsep">
    <xsl:choose>
      <!-- If this is the last column, colsep never applies. -->
      <xsl:when test="$following.spans = ''">0</xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="inherited.table.attribute">
          <xsl:with-param name="entry" select="."/>
          <xsl:with-param name="colnum" select="$entry.colnum"/>
          <xsl:with-param name="attribute" select="'colsep'"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="valign">
    <xsl:call-template name="inherited.table.attribute">
      <xsl:with-param name="entry" select="."/>
      <xsl:with-param name="colnum" select="$entry.colnum"/>
      <xsl:with-param name="attribute" select="'valign'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="align">
    <xsl:call-template name="inherited.table.attribute">
      <xsl:with-param name="entry" select="."/>
      <xsl:with-param name="colnum" select="$entry.colnum"/>
      <xsl:with-param name="attribute" select="'align'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="char">
    <xsl:call-template name="inherited.table.attribute">
      <xsl:with-param name="entry" select="."/>
      <xsl:with-param name="colnum" select="$entry.colnum"/>
      <xsl:with-param name="attribute" select="'char'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="charoff">
    <xsl:call-template name="inherited.table.attribute">
      <xsl:with-param name="entry" select="."/>
      <xsl:with-param name="colnum" select="$entry.colnum"/>
      <xsl:with-param name="attribute" select="'charoff'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="$spans != '' and not(starts-with($spans,'0:'))">
      <xsl:call-template name="entry">
        <xsl:with-param name="col" select="$col+1"/>
        <xsl:with-param name="spans" select="substring-after($spans,':')"/>
      </xsl:call-template>
    </xsl:when>

    <xsl:when test="number($entry.colnum) &gt; $col">
      <xsl:call-template name="empty.table.cell">
        <xsl:with-param name="colnum" select="$col"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
        <xsl:with-param name="col" select="$col+1"/>
        <xsl:with-param name="spans" select="substring-after($spans,':')"/>
      </xsl:call-template>
    </xsl:when>

    <xsl:otherwise>
      <xsl:variable name="cell.content">
        <fo:block>
          <xsl:call-template name="table.cell.block.properties"/>

          <!-- are we missing any indexterms? -->
          <xsl:if test="not(preceding-sibling::d:entry)
                        and not(parent::d:row/preceding-sibling::d:row)">
            <!-- this is the first entry of the first row -->
            <xsl:if test="ancestor::d:thead or
                          (ancestor::d:tbody
                           and not(ancestor::d:tbody/preceding-sibling::d:thead
                                   or ancestor::d:tbody/preceding-sibling::d:tbody))">
              <!-- of the thead or the first tbody -->
              <xsl:apply-templates select="ancestor::d:tgroup/preceding-sibling::d:indexterm"/>
            </xsl:if>
          </xsl:if>

          <!--
          <xsl:text>(</xsl:text>
          <xsl:value-of select="$rowsep"/>
          <xsl:text>,</xsl:text>
          <xsl:value-of select="$colsep"/>
          <xsl:text>)</xsl:text>
          -->
          <xsl:choose>
            <xsl:when test="$empty.cell">
              <xsl:text>&#160;</xsl:text>
            </xsl:when>
            <xsl:when test="self::d:entrytbl">
              <xsl:variable name="prop-columns"
                            select=".//d:colspec[contains(@colwidth, '*')]"/>
              <fo:table xsl:use-attribute-sets="table.table.properties">
                <xsl:if test="count($prop-columns) != 0">
                  <xsl:attribute name="table-layout">fixed</xsl:attribute>
                </xsl:if>
                <xsl:call-template name="tgroup"/>
              </fo:table>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates/>
            </xsl:otherwise>
          </xsl:choose>
        </fo:block>
      </xsl:variable>

      <xsl:variable name="cell-orientation">
        <xsl:call-template name="pi.dbfo_orientation">
          <xsl:with-param name="node" select="ancestor-or-self::d:entry"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:variable name="row-orientation">
        <xsl:call-template name="pi.dbfo_orientation">
          <xsl:with-param name="node" select="ancestor-or-self::d:row"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:variable name="cell-width">
        <xsl:call-template name="pi.dbfo_rotated-width">
          <xsl:with-param name="node" select="ancestor-or-self::d:entry"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:variable name="row-width">
        <xsl:call-template name="pi.dbfo_rotated-width">
          <xsl:with-param name="node" select="ancestor-or-self::d:row"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:variable name="orientation">
        <xsl:choose>
          <xsl:when test="$cell-orientation != ''">
            <xsl:value-of select="$cell-orientation"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$row-orientation"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="rotated-width">
        <xsl:choose>
          <xsl:when test="$cell-width != ''">
            <xsl:value-of select="$cell-width"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$row-width"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="bgcolor">
        <xsl:call-template name="pi.dbfo_bgcolor">
          <xsl:with-param name="node" select="ancestor-or-self::d:entry"/>
        </xsl:call-template>
      </xsl:variable>

      <fo:table-cell xsl:use-attribute-sets="table.cell.nopadding">
        <xsl:call-template name="table.cell.properties">
          <xsl:with-param name="bgcolor.pi" select="$bgcolor"/>
          <xsl:with-param name="rowsep.inherit" select="$rowsep"/>
          <xsl:with-param name="colsep.inherit" select="$colsep"/>
          <xsl:with-param name="col" select="$col"/>
          <xsl:with-param name="valign.inherit" select="$valign"/>
          <xsl:with-param name="align.inherit" select="$align"/>
          <xsl:with-param name="char.inherit" select="$char"/>
        </xsl:call-template>

        <xsl:call-template name="anchor"/>

        <xsl:if test="@morerows">
          <xsl:attribute name="number-rows-spanned">
            <xsl:value-of select="@morerows+1"/>
          </xsl:attribute>
        </xsl:if>

        <xsl:if test="$entry.colspan &gt; 1">
          <xsl:attribute name="number-columns-spanned">
            <xsl:value-of select="$entry.colspan"/>
          </xsl:attribute>
        </xsl:if>

<!--
        <xsl:if test="@charoff">
          <xsl:attribute name="charoff">
            <xsl:value-of select="@charoff"/>
          </xsl:attribute>
        </xsl:if>
-->

        <xsl:choose>
          <xsl:when test="$fop.extensions = 0
                          and $orientation != ''">
            <fo:block-container reference-orientation="{$orientation}">
              <xsl:if test="$rotated-width != ''">
                <xsl:attribute name="width">
                  <xsl:value-of select="$rotated-width"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:copy-of select="$cell.content"/>
            </fo:block-container>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="$cell.content"/>
          </xsl:otherwise>
        </xsl:choose>
      </fo:table-cell>

      <xsl:choose>
        <xsl:when test="following-sibling::d:entry|following-sibling::d:entrytbl">
          <xsl:apply-templates select="(following-sibling::d:entry
                                       |following-sibling::d:entrytbl)[1]">
            <xsl:with-param name="col" select="$col+$entry.colspan"/>
            <xsl:with-param name="spans" select="$following.spans"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="finaltd">
            <xsl:with-param name="spans" select="$following.spans"/>
            <xsl:with-param name="col" select="$col+$entry.colspan"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
