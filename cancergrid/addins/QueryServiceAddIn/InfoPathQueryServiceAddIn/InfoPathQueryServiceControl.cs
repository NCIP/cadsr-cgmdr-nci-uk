using System;
using System.IO;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Xml;
using System.Xml.XPath;
using System.Xml.Xsl;
using InfoPath = Microsoft.Office.Interop.InfoPath;
using InfoPathXml = Microsoft.Office.Interop.InfoPath.Xml;

namespace InfoPathQueryServiceAddIn
{
    public partial class InfoPathQueryServiceControl : QueryServiceControl.QueryServiceControl
    {

        protected override void use(object sender, EventArgs e)
        {
            try
            {
                InfoPath.XDocument xDoc = Globals.ThisAddIn.Application.XDocuments[0];
            
                /*
                 * xDoc.View.GetContextNodes returns 3 objects (2, if root node): 
                 * - xDoc.View.GetContextNodes(Type.Missing, Type.Missing)[0] => current node
                 * - xDoc.View.GetContextNodes(Type.Missing, Type.Missing)[1] => immediate parent of current node
                 * - xDoc.View.GetContextNodes(Type.Missing, Type.Missing)[2] => document node
                 */
                InfoPathXml.IXMLDOMNode n = xDoc.View.GetContextNodes(Type.Missing, Type.Missing)[0];

                if (n.baseName != "data-element" && n.baseName != "concept")
                {
                    MessageBox.Show("No valid context node selected");
                    return;
                }

                XmlNodeList nodeList = lastResult.DocumentElement.SelectNodes("/rs:result-set/rs:data-element", nsmanager);
                if (nodeList.Count != 0 && n.baseName == "data-element")
                {
                    string swap = swapNamespace(lastResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = \"" + getSelectedItem(lstResults).ID + "\"]", nsmanager));

                    InfoPathXml.IXMLDOMDocument dom = xDoc.CreateDOM();
                    dom.loadXML(swap);
                    InfoPathXml.IXMLDOMNode newNode = dom.selectSingleNode("/*");
                    n.parentNode.replaceChild(newNode, n);
                    return;
                }
                else if (nodeList.Count == 0 && n.baseName == "data-element")
                {
                    MessageBox.Show("Fail to replace context node with concept. Selected context node is of type data-element");
                    return;
                }

                nodeList = lastResult.DocumentElement.SelectNodes("/rs:result-set/rs:concept", nsmanager);
                if (nodeList.Count != 0 && n.baseName == "concept")
                {
                    string swap = swapNamespace(lastResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = \"" + getSelectedItem(lstResults).ID + "\"]", nsmanager));

                    InfoPathXml.IXMLDOMDocument dom = xDoc.CreateDOM();
                    dom.loadXML(swap);
                    InfoPathXml.IXMLDOMNode newNode = dom.selectSingleNode("/*");
                    n.parentNode.replaceChild(newNode, n);
                    return;    
                }
                else if (nodeList.Count == 0 && n.baseName == "concept")
                {
                    MessageBox.Show("Fail to replace context node with data-element. Selected context node is of type concept");
                    return;
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error inserting data element or concept: " + ex.Message);
                return;
            }            
        }

        protected override void useCLS(object sender, EventArgs e)
        {
            try
            {
                InfoPath.XDocument xDoc = Globals.ThisAddIn.Application.XDocuments[0];

                /*
                 * xDoc.View.GetContextNodes returns 3 objects (2, if root node): 
                 * - xDoc.View.GetContextNodes(Type.Missing, Type.Missing)[0] => current node
                 * - xDoc.View.GetContextNodes(Type.Missing, Type.Missing)[1] => immediate parent of current node
                 * - xDoc.View.GetContextNodes(Type.Missing, Type.Missing)[2] => document node
                 */
                InfoPathXml.IXMLDOMNode n = xDoc.View.GetContextNodes(Type.Missing, Type.Missing)[0];

                if (n.baseName != "data-element")
                {
                    MessageBox.Show("No valid context node selected");
                    return;
                }

                string swap = swapNamespace(lastClassificationQueryResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = \"" + getSelectedItem(lstClassificationQueryResult).ID + "\"]", nsmanager));
                InfoPathXml.IXMLDOMDocument dom = xDoc.CreateDOM();
                dom.loadXML(swap);
                InfoPathXml.IXMLDOMNode newNode = dom.selectSingleNode("/*");
                n.parentNode.replaceChild(newNode, n);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error inserting data element or concept: " + ex.Message);
                return;
            }
        }

        private string swapNamespace(XmlNode xml)
        {
            String xsl =
                @"<xsl:stylesheet xmlns:xsl=""http://www.w3.org/1999/XSL/Transform"" version=""1.0"">
                    <xsl:output omit-xml-declaration=""yes"" indent=""yes""/>
                    
                    <xsl:template match=""*"">
                        <xsl:element name=""{local-name()}"" namespace=""http://www.cancergrid.org/result/set/3/2008"">
                            <xsl:apply-templates select=""@*|node()""/>
                        </xsl:element>
                    </xsl:template>
                </xsl:stylesheet>";

            StringReader rdr = new StringReader(xml.OuterXml);
            XPathDocument xdoc = new XPathDocument(rdr);
            XslCompiledTransform transform = new XslCompiledTransform();
            StringReader xslStringReader = new StringReader(xsl);
            XmlTextReader xslReader = new XmlTextReader(xslStringReader);
            transform.Load(xslReader);
            StringWriter w = new StringWriter();
            XPathNavigator nav = xdoc.CreateNavigator();
            transform.Transform(nav, null, w);
            w.Flush();
            return w.ToString();
        }
    }
}
