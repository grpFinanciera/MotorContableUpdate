<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />

<@inputTextareaRow title=uiLabelMap.CommonComment name="comentario" default="" />
<tr>
<td>
</td>
<td>
<table>
	<tr>
		<td>					    														
			<@displayLink href="javascript:accionWorkFlow('autorizar', '${estatusAprobado}');" text="${uiLabelMap.Autorizar}" class="subMenuButton"/>
		</td>
		<td>
			<@displayLink href="javascript:accionWorkFlow('rechazar', '${estatusRechazado}');" text="${uiLabelMap.Rechazar}" class="subMenuButtonDangerous"/>
		</td>
		<td>
			<@displayLink href="javascript:accionWorkFlow('comentar', '${estatusComentado}');" text="${uiLabelMap.Comentar}" class="subMenuButton"/>
		</td>
	</tr>
</table>
</td>
</tr>