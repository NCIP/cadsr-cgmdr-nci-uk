CancerGrid Microsoft Excel 2007 Query Service Addin
===================================================

Important: The addin is configure by default to communicate with cgMDR insall and run on localhost. Refer to
the corresponding "Changing...location" sections for instructions to change the default URL.


Prerequisites
-------------

- Microsoft Excel 2007


Installation
------------ 

- Execute setup.exe

Note: The set up will check whether all the dependacies is available. If not, it will install it from 
the official Microsoft site.


Usage - Type a worksheet column
-------------------------------

- Open a work sheet in Excel

- Go to the "cancergrid" tab in the Ribbon area and select 

- Click on the "Query Taskpane" button

- This will open a taskpane for querying cgMDR

- Select a resource from the drop-down list to query

- Type in a search term in the text box 
  (note that if you choose caDSR or EVS, you need to add "*" in your search term)

- Click on search to start the query

- Select any one item on the "Results" pane to see a detail view on the "Details" pane

- Highlight/select a column in the worksheet

- Click "Use" to apply validation rules (based on the CDE) to the selected column


Note: 

- The addin supports only CDE with enumerated values and non-enumerated values with XSD based data type
- Not all XSD types are supported because of limitation on Excel built-in types in validation rules


Changing the Query Service Manager's location
---------------------------------------------
To setup the add-in to communicate with other instances of Query Service Manager, follow the steps below:

- Open C:\Documents and Settings\<username>\Local Settings\Apps\2.0\

- You should see a folder with a randomly generated name

- Inside the folder is another folder with a randomly generated name

- In this folder there are folders containing files used by office addin

- Locate the folders (there are 2) with names start with 

  - exce...dll_********
  - exce...dll_********

- In these two folder is a file with name ExcelQueryServiceAddIn.dll.config

- In both files, change <setting name="QueryServiceControl_QueryServiceManager_QueryServiceManager" serializeAs="String">, 
   change <value> to the target web service URL

...
<applicationSettings>
        <QueryServiceControl.Properties.Settings>
            <setting name="QueryServiceControl_QueryServiceManager_QueryServiceManager" serializeAs="String">
		<!-- Change the value below to customize web service location -->
                <value>http://localhost:8080/exist/services/QueryServiceManager</value>
            </setting>
        </QueryServiceControl.Properties.Settings>
</applicationSettings>
...



Usage - Submitting new CDE
--------------------------

- Open Excel

- Go to the "cancergrid" tab in the Ribbon area and select 

- Click on the "Create CDE" button

- This will open a windows form for submitting new CDE
  Note: This may take some time to load as the submission form need to retreive information from cgMDR
        for assisting filling in the form

- Fill in the required information and click submit

- If the submission is successful, a new dialog window is shown with the new id assigned to the new CDE

- You can choose to now use/insert the new CDE (after selecting a column), which will type a column in the work sheet, or

- Choose "Close" to close the dialog



Changing the DER's Service location
-----------------------------------
To setup the add-in to communicate with other instances of DER, follow the steps below:

- Open C:\Documents and Settings\<username>\Local Settings\Apps\2.0\

- You should see a folder with a randomly generated name

- Inside the folder is another folder with a randomly generated name

- In this folder there are folders containing files used by office addin

- Locate the folders (there are 2) with names start with 

  - exce...dll_********
  - exce...dll_********

- In these two folder is a file with name ExcelQueryServiceAddIn.dll.config

- In both files Under <client>, change all "address" attribute to use the url of the new DER service


...
<client>
...
      <endpoint address="https://localhost:8443/exist/services/DataElementReduced"
          binding="customBinding" bindingConfiguration="DataElementReducedSOAP12Binding"
          contract="DataElementReduced.DataElementReducedPortType" name="DataElementReducedSOAP12port_https" />
...
</client>
...

