using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using Excel = Microsoft.Office.Interop.Excel;
using Office = Microsoft.Office.Core;

namespace ExcelQueryServiceAddIn
{
    public partial class ThisAddIn
    {
        private ExcelQueryServiceControl queryServiceControl;
        private Microsoft.Office.Tools.CustomTaskPane myCustomTaskPane;

        private void ThisAddIn_Startup(object sender, System.EventArgs e)
        {
        }

        private void ThisAddIn_Shutdown(object sender, System.EventArgs e)
        {
            RemoveQueryServiceTaskPane();
        }

        public void AddQueryServiceTaskPane()
        {
            if (myCustomTaskPane == null)
            {
                queryServiceControl = new ExcelQueryServiceControl();
                myCustomTaskPane = this.CustomTaskPanes.Add(queryServiceControl, "Query Service Control");
                myCustomTaskPane.DockPosition = Microsoft.Office.Core.MsoCTPDockPosition.msoCTPDockPositionRight;
                myCustomTaskPane.Width = 300;
                myCustomTaskPane.Visible = true;
            }
            else if (myCustomTaskPane.Visible == false)
            {
                myCustomTaskPane.Visible = true;
            }
        }

        public void RemoveQueryServiceTaskPane()
        {
            try
            {
                if (myCustomTaskPane != null && this.CustomTaskPanes.Contains(myCustomTaskPane))
                {
                    this.CustomTaskPanes.Remove(myCustomTaskPane);
                }
            }
            catch (Exception) { }
            myCustomTaskPane = null;
        }

        protected override Microsoft.Office.Core.IRibbonExtensibility CreateRibbonExtensibilityObject()
        {
            return new ExcelQueryServiceRibbon();
        }

        #region VSTO generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InternalStartup()
        {
            this.Startup += new System.EventHandler(ThisAddIn_Startup);
            this.Shutdown += new System.EventHandler(ThisAddIn_Shutdown);
        }
        
        #endregion
    }
}
