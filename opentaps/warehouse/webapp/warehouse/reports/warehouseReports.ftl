<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<div class="tabletext" style="margin-bottom: 30px;">
<#assign hostname = request.getServerName()/>
<table style="width: 100%;">
<tr>
  <td style="vertical-align: top; width: 35%;">
	
    <@displayReportGroup group="WRHS_ALMACEN" nameOnly=true>
    
	</@displayReportGroup> 
	

</table>
</div>