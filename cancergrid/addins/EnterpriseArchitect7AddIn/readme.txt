CancerGrid Enterprise Architect Query Service Addin
===================================================

Important: The addin is configure by default to communicate with cgMDR insall and run on localhost. Refer to
the corresponding "Changing...location" sections for instructions to change the default URL.


Prerequisites
-------------

- Sparx Exterprise Architect 6.5 or above


Installation
------------ 

- Execute setup.exe

Note: The set up will check whether .Net 3.5 is available. If not, it will install it from the 
official Microsoft site.


Usage - Creating new class from CDE
-----------------------------------

- (Re)Start EA

- Create/Open a project

- Create/Open a diagram

- right-click on the diagram area (or from the menu bar), choose "Add-Ins => CancerGrid => Add Element from CDE"

- Select a resource from the drop-down list to query

- Type in a search term in the text box 
  (note that if you choose caDSR or EVS, you need to add "*" in your search term)

- Click on search to start the query

- Select any one item on the "Results" pane to see a detail view on the "Details" pane

- Click "Use" to add the selected CDE as a new class in the current diagram

- The query window will remain open and you can continue to add more CDEs into the active diagram

- At any time you can switch to another diagram and continue using the query window to insert CDEs


Changing the Query Service Manager's location
---------------------------------------------
To setup the add-in to communicate with other instances of Query Service Manager, follow the steps below:

1. Open C:\Program Files\Sparx Systems\EA\EA.exe.config (default install location)
2. Under <setting name="QueryServiceControl_QueryServiceManager_QueryServiceManager" serializeAs="String">, 
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

- Start EA

- Create/Open a project

- Create/Open a diagram

- right-click on the diagram area (or from the menu bar), choose "Add-Ins => CancerGrid => Create CDE"

- This will open a windows form for submitting new CDE
  Note: This may take some time to load as the submission form need to retreive information from cgMDR
        for assisting filling in the form

- Fill in the required information and click submit

- If the submission is successful, a new dialog window is shown with the new id assigned to the new CDE

- You can choose to now use/insert the new CDE, which creates a class in the active diagram, or

- Choose "Close" to close the dialog



Changing the DER's Service location
-----------------------------------
To setup the add-in to communicate with other instances of DER, follow the steps below:

- Open C:\Program Files\Sparx Systems\EA\EA.exe.config (default install location)
- Under <client>, change all "address" attribute to use the url of the new DER service

...
<client>
...
      <endpoint address="https://localhost:8443/exist/services/DataElementReduced"
          binding="customBinding" bindingConfiguration="DataElementReducedSOAP12Binding"
          contract="DataElementReduced.DataElementReducedPortType" name="DataElementReducedSOAP12port_https" />
...
</client>
...

