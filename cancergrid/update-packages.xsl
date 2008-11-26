<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:param name="id"></xsl:param>
    <xsl:param name="file"></xsl:param>
    
    <xsl:template match="/"> 
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="item[@id=$id]/file">
        <file><xsl:value-of select="$file"/></file>
    </xsl:template>
    
    <xsl:template match="*|@*|comment()">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()|comment()"/> 
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet> 