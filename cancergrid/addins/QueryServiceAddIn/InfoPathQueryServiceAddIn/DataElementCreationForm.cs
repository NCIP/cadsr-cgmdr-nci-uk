using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Xml;
using System.Xml.XPath;
using System.Xml.Xsl;
using System.IO;

using InfoPath = Microsoft.Office.Interop.InfoPath;
using InfoPathXml = Microsoft.Office.Interop.InfoPath.Xml;

namespace InfoPathQueryServiceAddIn
{
    public partial class DataElementCreationForm : Form
    {
        public DataElementCreationForm()
        {
            InitializeComponent();
            dataElementCreationControl.parent = this;
            dataElementCreationControl.OnUseResult += new EventHandler(dataElementCreationControl_OnUseResult);
        }

        void dataElementCreationControl_OnUseResult(object sender, EventArgs e)
        {
            if (dataElementCreationControl.LastResult == null)
            {
                MessageBox.Show("Error inserting data element");
                return;
            }

            insertCDE(Globals.ThisAddIn.Application);
        }

        private void insertCDE(InfoPath.Application application)
        {
            try
            {
                XmlDocument doc = new XmlDocument();
                doc.LoadXml(dataElementCreationControl.LastResult);
                XmlNode srcNode = doc.DocumentElement.SelectSingleNode("/data-element");

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

                string swap = swapNamespace(srcNode);

                InfoPathXml.IXMLDOMDocument dom = xDoc.CreateDOM();
                dom.loadXML(swap);
                InfoPathXml.IXMLDOMNode newNode = dom.selectSingleNode("/*");
                n.parentNode.replaceChild(newNode, n);
                return;
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error inserting data element: " + ex.Message);
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
