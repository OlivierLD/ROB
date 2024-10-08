<?xml version="1.0" encoding="utf-8"?>
<!--
 ! Publishes the Altitude Correction Tables.
 ! Warning: the page format is Letter. Can be changed to A4 (8.27" x 11.69", or 210mm x 297mm) if needed...
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:fox="http://xml.apache.org/fop/extensions" 
                xmlns:xsl-util="http://www.oracle.com/XSL/Transform/java/nauticalalmanac.xsl.XSLUtil"
                exclude-result-prefixes="xsl-util"
                version="1.0">
  <xsl:import href="page.xsl"/>

  <xsl:variable name="GEOMUTIL.HTML"   select="0"/>
  <xsl:variable name="GEOMUTIL.SHELL"  select="1"/>
  <xsl:variable name="GEOMUTIL.SWING"  select="2"/>
  <xsl:variable name="GEOMUTIL.NO_DEG" select="3"/>

  <xsl:variable name="GEOMUTIL.NONE" select="0"/>
  <xsl:variable name="GEOMUTIL.NS"   select="1"/>
  <xsl:variable name="GEOMUTIL.EW"   select="2"/>
  
  <xsl:variable name="GEOMUTIL.LEADING_SIGN"  select="0"/>
  <xsl:variable name="GEOMUTIL.TRAILING_SIGN" select="1"/>
  
  <xsl:variable name="moon.break" select="45"/>

  <xsl:template match="/">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="my-page"
                               page-width="{$page-height}"
                               page-height="{$page-width}"> <!-- Landscape, USLetter or A4 -->
          <fo:region-body margin="0in"/>
          <fo:region-after region-name="footer" extent="10mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="my-page">
        <fo:static-content flow-name="footer">
          <fo:block text-align="center">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block>
        </fo:static-content>
        <fo:flow flow-name="xsl-region-body">
          <fo:block break-after="page"> <!-- background-image="url('bg.jpg')"-->
            <fo:block text-align="center" font-family="Book Antiqua" font-size="60pt" font-weight="bold" margin="1in">
         <!-- Altitude Correction Tables -->
              Tables de correction des hauteurs
            </fo:block>
            <fo:block text-align="center">
              <fo:external-graphic src="url('sextant.gif')"/>
            </fo:block>
            <fo:block text-align="left" font-family="Book Antiqua" font-size="8pt" margin="0.5in">
              <fo:inline font-family="Symbol">p</fo:inline> est la parallaxe horizontale, obtenue dans les &#233;ph&#233;m&#233;rides.
            </fo:block>
            <fo:block text-align="left" font-family="Courier" font-size="8pt" font-style="italic" margin="1in">
              &#169; Passe-Coque <!-- Oliv Cool Stuff Soft -->
            </fo:block>
          </fo:block>
          <fo:block margin="0.2in">
            <xsl:for-each select="/altitude-corrections/sun-corrections">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
          </fo:block>
          <fo:block margin="0.2in">
            <xsl:for-each select="/altitude-corrections/moon-corrections">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
          </fo:block>
          <fo:block margin="0.2in">
            <xsl:for-each select="/altitude-corrections/planets-stars-corrections">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
          </fo:block>
          <fo:block id="last-page"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
  
  <xsl:template match="sun-corrections">
    <fo:block text-align="center" font-family="Courier" font-size="8pt" break-after="page">
      <fo:block>
        <fo:table border="0.5pt solid black">
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="14" padding="medium"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="12pt">Soleil</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="14" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Premi&#232;re Correction (- horizon, - r&#233;fraction, + parallaxe + demi-diam&#232;tre) &#224; ajouter alg&#233;briquement &#224; la hauteur</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Hauteur</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">0m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">2m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">4m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">6m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">8m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">10m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">12m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">14m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">16m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">18m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">20m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">22m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">24m</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body>
            <xsl:for-each select="./obs-altitude">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
          </fo:table-body>
          <!--fo:table-footer>
            That's it
          </fo:table-footer-->
        </fo:table>
      </fo:block>
      <!-- Deuxieme Correction -->
      <fo:block margin="0.2in">
        <fo:table border="0.5pt solid black">
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="12" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Deuxi&#232;me Correction (bord inf&#233;rieur) &#224; ajouter alg&#233;briquement &#224; la hauteur</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Janvier</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">F&#233;vrier</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Mars</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Avril</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Mai</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Juin</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Juillet</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Ao&#251;t</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Septembre</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Octobre</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Novembre</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">D&#233;cembre</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">+0'.3</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">+0'.2</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">+0'.1</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"> 0'.0</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-0'.2</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-0'.2</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-0'.2</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-0'.2</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-0'.1</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">+0'.1</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">+0'.2</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">+0'.3</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-body>
          <!--fo:table-footer>
            That's it
          </fo:table-footer-->
        </fo:table>
      </fo:block>
      <fo:block margin="0.2in">
        <!-- Bord Superieur -->
        <fo:table border="0.5pt solid black">
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="12" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Deuxi&#232;me Correction (bord sup&#233;rieur) &#224; ajouter alg&#233;briquement &#224; la hauteur</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Janvier</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">F&#233;vrier</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Mars</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Avril</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Mai</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Juin</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Juillet</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Ao&#251;t</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Septembre</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Octobre</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Novembre</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">D&#233;cembre</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-32'.3</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-32'.2</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-32'.1</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-32'.0</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-31'.8</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-31'.8</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-31'.8</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-31'.8</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">-31'.9</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">+32'.1</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">+32'.2</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">+32'.3</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-body>
          <!--fo:table-footer>
            That's it
          </fo:table-footer-->
        </fo:table>
      </fo:block>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="moon-corrections">
   
    <fo:block text-align="center" font-family="Courier" font-size="8pt" break-after="page">
      <fo:block>
        <fo:table border="0.5pt solid black">
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="14" padding="medium"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="12pt">Lune</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="14" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Premi&#232;re Correction (horizon) &#224; soustraire de la hauteur</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"> - </fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">0m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">2m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">4m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">6m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">8m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">10m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">12m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">14m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">16m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">18m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">20m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">22m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">24m</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body>
            <fo:table-row>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"> </fo:block></fo:table-cell>
              <xsl:for-each select="/altitude-corrections/horizon-dips/horizon-dip">                
                <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="./@dip"/>'</fo:block></fo:table-cell>
              </xsl:for-each>
            </fo:table-row>
          </fo:table-body>
          <!--fo:table-footer>
            That's it
          </fo:table-footer-->
        </fo:table>
      </fo:block>
    <!--/fo:block-->
   <!-- Deuxieme correction -->
    <!--fo:block text-align="center" font-family="Courier" font-size="8pt" break-after="page"-->
      <fo:block>
        <fo:table border="0.5pt solid black">
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="14" padding="medium"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="12pt"> <!-- Lune --> </fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="14" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Bord inf&#233;rieur, Deuxi&#232;me Correction (- r&#233;fraction, + parallaxe + demi-diam&#232;tre) &#224; ajouter alg&#233;briquement &#224; la hauteur</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Hauteur</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><!--&#960;--><fo:inline font-family="Symbol">p</fo:inline> 54'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 55'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 55.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 56'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 56.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 57'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 57.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 58'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 58.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 59'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 59.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 60'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 61'</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body>
            <xsl:for-each select="./obs-altitude[position() &lt; $moon.break]">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">&#216;</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">29.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">30'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">30.3'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">30.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">30.8'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">31.1'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">31.4'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">31.6'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">31.9'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">32.2'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">32.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">32.7'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">33.3'</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-body>
          <!--fo:table-footer>
            That's it
          </fo:table-footer-->
        </fo:table>
      </fo:block>
   </fo:block>
   <fo:block text-align="center" font-family="Courier" font-size="8pt" break-after="page">
      <fo:block>
        <fo:table border="0.5pt solid black">
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="14" padding="medium"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="12pt">Lune</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="14" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Bord inf&#233;rieur, Deuxi&#232;me Correction (- r&#233;fraction, + parallaxe + demi-diam&#232;tre) &#224; ajouter alg&#233;briquement &#224; la hauteur</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Hauteur</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 54'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 55'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 55.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 56'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 56.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 57'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 57.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 58'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 58.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 59'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 59.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 60'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 61'</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body>
            <xsl:for-each select="./obs-altitude[position() >= $moon.break]">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">&#216;</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">29.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">30'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">30.3'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">30.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">30.8'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">31.1'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">31.4'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">31.6'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">31.9'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">32.2'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">32.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">32.7'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">33.3'</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </fo:table>
      </fo:block>
      <!-- Troisieme Correction -->
      <fo:block margin="0.2in">
        <fo:table border="0.5pt solid black">
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <!--fo:table-header>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="12" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Troisi&#232;me Correction (bord sup&#233;rieur), soustraire le diam&#232;tre de la hauteur.</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header-->
          <fo:table-body>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="12" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Troisi&#232;me Correction (bord sup&#233;rieur), soustraire le diam&#232;tre de la hauteur.</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-body>
          <!--fo:table-footer>
            That's it
          </fo:table-footer-->
        </fo:table>
      </fo:block>
    </fo:block>
  </xsl:template>

  <xsl:template match="planets-stars-corrections">
    <fo:block text-align="center" font-family="Courier" font-size="8pt" break-after="page">
      <fo:block>
        <fo:table border="0.5pt solid black">
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="14" padding="medium"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="12pt">Plan&#232;tes et &#233;toiles</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="14" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Correction (- horizon, - r&#233;fraction) &#224; ajouter alg&#233;briquement &#224; la hauteur</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Hauteur</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">0m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">2m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">4m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">6m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">8m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">10m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">12m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">14m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">16m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">18m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">20m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">22m</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">24m</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body>
            <xsl:for-each select="./obs-altitude">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
          </fo:table-body>
          <!--fo:table-footer>
            That's it
          </fo:table-footer-->
        </fo:table>
      </fo:block>
      <!-- Deuxieme Correction (planets only) -->
      <fo:block margin="0.2in">
        <fo:table border="0.5pt solid black">
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-column column-width="0.75in"/>
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="11" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Deuxi&#232;me Correction (+ parallaxe) pour les plan&#232;tes seulement, &#224; ajouter alg&#233;briquement &#224; la hauteur</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt">Hauteur</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 0.1'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 0.2'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 0.3'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 0.4'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 0.5'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 0.6'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 0.7'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 0.8'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 0.9'</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><fo:inline font-family="Symbol">p</fo:inline> 1.0'</fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body>
            <xsl:for-each select="/altitude-corrections/planet-parallax/alt">          
              <xsl:variable name="alt" select="./@value"/>
              <fo:table-row>
                <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><xsl:value-of select="$alt"/>&#176;</fo:block></fo:table-cell>
                <xsl:for-each select="./corr">
                  <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold" font-family="Courier" font-size="8pt"><xsl:value-of select="xsl-util:formatX1(.)"/></fo:block></fo:table-cell>
                </xsl:for-each>
              </fo:table-row>
            </xsl:for-each>
          </fo:table-body>
          <!--fo:table-footer>
            That's it
          </fo:table-footer-->
        </fo:table>
      </fo:block>
    </fo:block>
  </xsl:template>  
  
  <xsl:template match="obs-altitude">
    <fo:table-row>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center"><xsl:value-of select="xsl-util:decToSexTrunc(./@value, $GEOMUTIL.SWING, $GEOMUTIL.NONE)" disable-output-escaping="yes"/></fo:block></fo:table-cell>
      <xsl:for-each select="./corr"> <!-- Sun + planetes and stars -->
        <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="."/>'</fo:block></fo:table-cell>
      </xsl:for-each>
      <xsl:for-each select="./corr-ref-pa"> <!-- for the Moon -->
        <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="."/>'</fo:block></fo:table-cell>
      </xsl:for-each>
    </fo:table-row>
  </xsl:template>

</xsl:stylesheet>
