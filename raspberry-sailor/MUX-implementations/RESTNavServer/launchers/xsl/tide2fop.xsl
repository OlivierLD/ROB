<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY copy    "&#169;">
    <!ENTITY Delta   "&#916;">
    <!ENTITY delta   "&#948;">
    <!ENTITY epsilon "&#949;">
    <!ENTITY psi     "&#968;">
    <!ENTITY micro   "&#181;">
    <!ENTITY pi      "&#960;">
    <!ENTITY Pi      "&#928;">
    <!ENTITY frac12  "&#189;">
    <!ENTITY Agrave  "&#192;">
    <!ENTITY Aacute  "&#193;">
    <!ENTITY Acirc   "&#194;">
    <!ENTITY Atilde  "&#195;">
    <!ENTITY Auml    "&#196;">
    <!ENTITY Aring   "&#197;">
    <!ENTITY AElig   "&#198;">
    <!ENTITY Ccedil  "&#199;">
    <!ENTITY Egrave  "&#200;">
    <!ENTITY Eacute  "&#201;">
    <!ENTITY Ecirc   "&#202;">
    <!ENTITY Euml    "&#203;">
    <!ENTITY Igrave  "&#204;">
    <!ENTITY Iacute  "&#205;">
    <!ENTITY Icirc   "&#206;">
    <!ENTITY Iuml    "&#207;">
    <!ENTITY ETH     "&#208;">
    <!ENTITY Ntilde  "&#209;">
    <!ENTITY Ograve  "&#210;">
    <!ENTITY Oacute  "&#211;">
    <!ENTITY Ocirc   "&#212;">
    <!ENTITY Otilde  "&#213;">
    <!ENTITY Ouml    "&#214;">
    <!ENTITY times   "&#215;">
    <!ENTITY Oslash  "&#216;">
    <!ENTITY Ugrave  "&#217;">
    <!ENTITY Uacute  "&#218;">
    <!ENTITY Ucirc   "&#219;">
    <!ENTITY Uuml    "&#220;">
    <!ENTITY Yacute  "&#221;">
    <!ENTITY THORN   "&#222;">
    <!ENTITY szlig   "&#223;">
    <!ENTITY agrave  "&#224;">
    <!ENTITY aacute  "&#225;">
    <!ENTITY acirc   "&#226;">
    <!ENTITY atilde  "&#227;">
    <!ENTITY auml    "&#228;">
    <!ENTITY aring   "&#229;">
    <!ENTITY aelig   "&#230;">
    <!ENTITY ccedil  "&#231;">
    <!ENTITY egrave  "&#232;">
    <!ENTITY eacute  "&#233;">
    <!ENTITY ecirc   "&#234;">
    <!ENTITY euml    "&#235;">
    <!ENTITY igrave  "&#236;">
    <!ENTITY iacute  "&#237;">
    <!ENTITY icirc   "&#238;">
    <!ENTITY iuml    "&#239;">
    <!ENTITY eth     "&#240;">
    <!ENTITY ntilde  "&#241;">
    <!ENTITY ograve  "&#242;">
    <!ENTITY oacute  "&#243;">
    <!ENTITY ocirc   "&#244;">
    <!ENTITY otilde  "&#245;">
    <!ENTITY ouml    "&#246;">
    <!ENTITY divide  "&#247;">
    <!ENTITY oslash  "&#248;">
    <!ENTITY ugrave  "&#249;">
    <!ENTITY uacute  "&#250;">
    <!ENTITY ucirc   "&#251;">
    <!ENTITY uuml    "&#252;">
    <!ENTITY yacute  "&#253;">
    <!ENTITY apos    "&#39;">
    <!ENTITY deg     "&#176;">
    ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:fo="http://www.w3.org/1999/XSL/Format"
        xmlns:fox="http://xml.apache.org/fop/extensions" version="2.0">
  <!--xsl:import href="literals.xsl"/-->
  <xsl:import href="page.xsl"/>
  <xsl:param name="language">EN</xsl:param>

  <xsl:template match="/">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="portrait-page"
                               page-width="{$page-width}"
                               page-height="{$page-height}">
          <!-- Portrait, Letter -->
          <fo:region-body margin="0in"/>
          <fo:region-after region-name="footer" extent="20mm"/>
        </fo:simple-page-master>
        <fo:simple-page-master master-name="landscape-page"
                               page-height="{$page-width}"
                               page-width="{$page-height}">
          <!-- Portrait -->
          <fo:region-body margin="0in"/>
          <fo:region-after region-name="footer" extent="20mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="portrait-page">
        <fo:static-content flow-name="footer">
          <!--fo:block text-align="center">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block-->
          <!--fo:block text-align="center" font-size="8pt">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block-->
        </fo:static-content>
        <fo:flow flow-name="xsl-region-body">
          <!-- First Page -->
          <!--fo:block break-after="page">
          </fo:block-->
          <fo:block margin="0.1in">
            <xsl:for-each select="//period">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
          </fo:block>
          <!--fo:block id="last-page"/-->
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
  <xsl:template match="period" name="one-month-tide">
    <fo:block text-align="center" font-family="Courier" font-size="10pt"
          break-after="page" margin="0.1in">
      <fo:block margin="0.15in">
        <fo:table>
          <!-- border="0.5pt solid black"> -->
          <!-- 3 Columns -->
          <fo:table-column column-width="2.5in"/>
          <fo:table-column column-width="2.5in"/>
          <fo:table-column column-width="2.5in"/>
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="3" padding="medium"
                       border="0.5pt solid black">
                <fo:block text-align="left" font-family="Courier"
                      font-size="9pt">
                  <xsl:choose>
                    <xsl:when test="$language = 'FR'">
                      <xsl:value-of select="concat('Mar&eacute;e &agrave; ', ../@station, ', ', ../@station-lat, ' / ', ../@station-lng)"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="concat('Tide at ', ../@station, ', ', ../@station-lat, ' / ', ../@station-lng)"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="3" padding="medium"
                       border="0.5pt solid black">
                <fo:block text-align="left" font-family="Courier"
                      font-size="9pt">Time Zone: <xsl:value-of select="../@print-time-zone"/>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="3" padding="medium"
                       border="0.5pt solid black">
                <fo:block text-align="center" font-family="Courier" font-weight="bold"
                      font-size="9pt">
                  <xsl:choose>
                    <xsl:when test="./@month = '1' and $language = 'EN'">January</xsl:when>
                    <xsl:when test="./@month = '1' and $language = 'FR'">Janvier</xsl:when>
                    <xsl:when test="./@month = '2' and $language = 'EN'">February</xsl:when>
                    <xsl:when test="./@month = '2' and $language = 'FR'">F&eacute;vrier</xsl:when>
                    <xsl:when test="./@month = '3' and $language = 'EN'">March</xsl:when>
                    <xsl:when test="./@month = '3' and $language = 'FR'">Mars</xsl:when>
                    <xsl:when test="./@month = '4' and $language = 'EN'">April</xsl:when>
                    <xsl:when test="./@month = '4' and $language = 'FR'">Avril</xsl:when>
                    <xsl:when test="./@month = '5' and $language = 'EN'">May</xsl:when>
                    <xsl:when test="./@month = '5' and $language = 'FR'">Mai</xsl:when>
                    <xsl:when test="./@month = '6' and $language = 'EN'">June</xsl:when>
                    <xsl:when test="./@month = '6' and $language = 'FR'">Juin</xsl:when>
                    <xsl:when test="./@month = '7' and $language = 'EN'">July</xsl:when>
                    <xsl:when test="./@month = '7' and $language = 'FR'">Juillet</xsl:when>
                    <xsl:when test="./@month = '8' and $language = 'EN'">August</xsl:when>
                    <xsl:when test="./@month = '8' and $language = 'FR'">Ao&ucirc;t</xsl:when>
                    <xsl:when test="./@month = '9' and $language = 'EN'">September</xsl:when>
                    <xsl:when test="./@month = '9' and $language = 'FR'">Septembre</xsl:when>
                    <xsl:when test="./@month = '10' and $language = 'EN'">October</xsl:when>
                    <xsl:when test="./@month = '10' and $language = 'FR'">Octobre</xsl:when>
                    <xsl:when test="./@month = '11' and $language = 'EN'">November</xsl:when>
                    <xsl:when test="./@month = '11' and $language = 'FR'">Novembre</xsl:when>
                    <xsl:when test="./@month = '12' and $language = 'EN'">December</xsl:when>
                    <xsl:when test="./@month = '12' and $language = 'FRN'">D&eacute;cembre</xsl:when>
                  </xsl:choose>
                  <xsl:text disable-output-escaping="yes">&nbsp;</xsl:text>
                  <xsl:value-of select="./@year"/>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body font-size="7pt">
            <xsl:for-each select="./date">
              <xsl:if test="(position() - 1) mod 3 = 0">
                <xsl:text disable-output-escaping="yes">&lt;fo:table-row&gt;</xsl:text>
              </xsl:if>
              <!--fo:table-row-->
              <fo:table-cell border="0.5pt solid black"
                       text-align="center">
                <xsl:if test="./@specBG = 'y'">
                  <xsl:attribute name="background-color">silver</xsl:attribute>
                </xsl:if>
                <fo:block>
                  <xsl:call-template name="one-day-tide">
                    <xsl:with-param name="data" select="."/>
                  </xsl:call-template>
                </fo:block>
              </fo:table-cell>
              <!--/fo:table-row-->
              <xsl:if test="(position() - 1) mod 3 = 2 or position() = last()">
                <xsl:text disable-output-escaping="yes">&lt;/fo:table-row&gt;</xsl:text>
              </xsl:if>
            </xsl:for-each>
          </fo:table-body>
          <!--fo:table-footer>
                      That's it
                    </fo:table-footer-->
        </fo:table>
        <fo:block text-align="left" font-family="Book Antiqua" font-size="8pt" font-weight="normal" font-style="italic" margin="0.1in">by <!--OlivSoft-->Passe-Coque</fo:block>
      </fo:block>
    </fo:block>
  </xsl:template>

  <xsl:template match="date" name="one-day-tide">
    <xsl:param name="data"/>
    <!-- Create new table here -->
    <fo:table>
      <fo:table-column column-width="0.5in"/>   <!-- was 0.35in. HW/LW/ME/MF/Slack, with coeff when exists -->
      <fo:table-column column-width="0.65in"/>   <!-- HH:MM ZZZ -->
      <fo:table-column column-width="0.35in"/>   <!-- Height -->
      <fo:table-column column-width="0.55in"/>   <!-- Unit -->
      <fo:table-column column-width="0.4in"/>    <!-- Moon image -->
      <fo:table-body>
        <fo:table-row>
          <!-- Date -->
          <fo:table-cell number-columns-spanned="5">
            <fo:block text-align="left" font-weight="bold">
              <xsl:choose>
                <xsl:when test="$language = 'FR'">
                  <xsl:variable name="wday" select='substring($data/@val, 1, 3)'/>
                  <xsl:variable name="mday" select='substring($data/@val, 5, 2)'/>
                  <xsl:variable name="month-name" select='substring($data/@val, 8, 3)'/>
                  <xsl:variable name="date-year" select='substring($data/@val, 12, 4)'/>

                  <xsl:if test="$wday = 'Mon'">
                    <xsl:value-of select="concat('Lun', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$wday = 'Tue'">
                    <xsl:value-of select="concat('Mar', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$wday = 'Wed'">
                    <xsl:value-of select="concat('Mer', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$wday = 'Thu'">
                    <xsl:value-of select="concat('Jeu', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$wday = 'Fri'">
                    <xsl:value-of select="concat('Ven', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$wday = 'Sat'">
                    <xsl:value-of select="concat('Sam', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$wday = 'Sun'">
                    <xsl:value-of select="concat('Dim', ' ')"/>
                  </xsl:if>

                  <xsl:value-of select="concat($mday, ' ')"/>

                  <xsl:if test="$month-name = 'Jan'">
                    <xsl:value-of select="concat('Jan', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$month-name = 'Feb'">
                    <xsl:value-of select="concat('F&eacute;v', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$month-name = 'Mar'">
                    <xsl:value-of select="concat('Mar', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$month-name = 'Apr'">
                    <xsl:value-of select="concat('Avr', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$month-name = 'May'">
                    <xsl:value-of select="concat('Mai', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$month-name = 'Jun'">
                    <xsl:value-of select="concat('Juin', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$month-name = 'Jul'">
                    <xsl:value-of select="concat('Juil', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$month-name = 'Aug'">
                    <xsl:value-of select="concat('Ao&ucirc;t', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$month-name = 'Sep'">
                    <xsl:value-of select="concat('Sep', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$month-name = 'Oct'">
                    <xsl:value-of select="concat('Oct', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$month-name = 'Nov'">
                    <xsl:value-of select="concat('Nov', ' ')"/>
                  </xsl:if>
                  <xsl:if test="$month-name = 'Dec'">
                    <xsl:value-of select="concat('D&eacute;', ' ')"/>
                  </xsl:if>

                  <xsl:value-of select="$date-year"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$data/@val"/>
                </xsl:otherwise>
              </xsl:choose>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <!-- Sunrise, sunset -->
          <fo:table-cell number-columns-spanned="5">
            <fo:block text-align="left" font-weight="bold">
              <xsl:choose>
                <xsl:when test="$language = 'FR'">
                  <xsl:value-of select="concat('Soleil : ', $data/@sun-rise, ' - ', $data/@sun-set)"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="concat('Sun rise:', $data/@sun-rise, ', Sun set:', $data/@sun-set)"/>
                </xsl:otherwise>
              </xsl:choose>
            </fo:block>
            <fo:block text-align="left" font-weight="bold">
              <xsl:value-of select="concat('  Z rise: ', $data/@sun-rise-Z, '°, Z set: ', $data/@sun-set-Z, '°')"/>
            </fo:block>
            <fo:block text-align="left" font-weight="bold">
              <xsl:value-of select="concat(' Transit: ', $data/@sun-transit, ' (El ', $data/@sun-elev-at-transit, '°)')"/>
            </fo:block>
            <!--fo:block text-align="left" font-weight="bold">
              <xsl:value-of select="concat('Moon rise:', $data/@moon-rise, ', Moon set:', $data/@moon-set)"/>
            </fo:block-->
          </fo:table-cell>
        </fo:table-row>
        <xsl:for-each select="$data/plot">
          <fo:table-row>
            <fo:table-cell padding="medium">
              <fo:block text-align="left">
                <xsl:variable name="type" select="./@type"/>
                <xsl:choose>
                  <!-- Coeff ? -->
                  <xsl:when test="./@coeff">
                    <xsl:if test="$language = 'FR' and $type = 'HW'">
                      <xsl:value-of select="concat('PM', ' (', ./@coeff, ')')"/>
                    </xsl:if>
                    <xsl:if test="$language = 'FR' and $type = 'LW'">
                      <xsl:value-of select="concat('BM', ' (', ./@coeff, ')')"/>
                    </xsl:if>
                    <xsl:if test="$language != 'FR'">
                      <xsl:value-of select="concat($type, ' (', ./@coeff, ')')"/>
                    </xsl:if>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:if test="$language = 'FR' and $type = 'HW'">
                      <xsl:value-of select="'PM'"/>
                    </xsl:if>
                    <xsl:if test="$language = 'FR' and $type = 'LW'">
                      <xsl:value-of select="'BM'"/>
                    </xsl:if>
                    <xsl:if test="$language != 'FR'">
                      <xsl:value-of select="$type"/>
                    </xsl:if>
                  </xsl:otherwise>
                </xsl:choose>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="medium">
              <fo:block text-align="left">
                <xsl:value-of select="./@date"/>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="medium">
              <fo:block text-align="right">
                <xsl:value-of select="./@height"/>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="medium">
              <fo:block text-align="center">
                <xsl:if test="$language = 'FR' and ./@unit = 'meters'">
                  <xsl:value-of select="'m&egrave;tres'"/>
                </xsl:if>
                <xsl:if test="$language = 'FR' and ./@unit = 'feet'">
                  <xsl:value-of select="'pieds'"/>
                </xsl:if>
                <xsl:if test="$language != 'FR'">
                  <xsl:value-of select="./@unit"/>
                </xsl:if>
              </fo:block>
            </fo:table-cell>
            <!-- Coeff ? -->
            <!--xsl:if test="./@coeff">
              <fo:table-cell padding="medium">
                <fo:block text-align="center">
                  <xsl:value-of select="./@coeff"/>
                </fo:block>
              </fo:table-cell>
            </xsl:if-->
            <xsl:if test="position() = 1">
              <fo:table-cell number-rows-spanned="4" padding="medium"
                       vertical-align="middle" horizontal-align="center">
                <fo:block vertical-align="middle">
                  <xsl:choose>
                    <xsl:when test="$data/@moon-phase = '01' or $data/@moon-phase = '00'">
                      <fo:external-graphic src="url('phase01.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '02'">
                      <fo:external-graphic src="url('phase02.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '03'">
                      <fo:external-graphic src="url('phase03.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '04'">
                      <fo:external-graphic src="url('phase04.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '05'">
                      <fo:external-graphic src="url('phase05.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '06'">
                      <fo:external-graphic src="url('phase06.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '07'">
                      <fo:external-graphic src="url('phase07.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '08'">
                      <fo:external-graphic src="url('phase08.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '09'">
                      <fo:external-graphic src="url('phase09.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '10'">
                      <fo:external-graphic src="url('phase10.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '11'">
                      <fo:external-graphic src="url('phase11.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '12'">
                      <fo:external-graphic src="url('phase12.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '13'">
                      <fo:external-graphic src="url('phase13.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '14'">
                      <fo:external-graphic src="url('phase14.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '15'">
                      <fo:external-graphic src="url('phase15.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '16'">
                      <fo:external-graphic src="url('phase16.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '17'">
                      <fo:external-graphic src="url('phase17.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '18'">
                      <fo:external-graphic src="url('phase18.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '19'">
                      <fo:external-graphic src="url('phase19.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '20'">
                      <fo:external-graphic src="url('phase20.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '21'">
                      <fo:external-graphic src="url('phase21.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '22'">
                      <fo:external-graphic src="url('phase22.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '23'">
                      <fo:external-graphic src="url('phase23.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '24'">
                      <fo:external-graphic src="url('phase24.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '25'">
                      <fo:external-graphic src="url('phase25.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '26'">
                      <fo:external-graphic src="url('phase26.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '27'">
                      <fo:external-graphic src="url('phase27.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                    <xsl:when test="$data/@moon-phase = '28' or $data/@moon-phase = '29'">
                      <fo:external-graphic src="url('phase28.gif')"
                                 vertical-align="middle" horizontal-align="center"/>
                    </xsl:when>
                  </xsl:choose>
                </fo:block>
              </fo:table-cell>
            </xsl:if>
          </fo:table-row>
        </xsl:for-each>
      </fo:table-body>
    </fo:table>
  </xsl:template>
</xsl:stylesheet>
