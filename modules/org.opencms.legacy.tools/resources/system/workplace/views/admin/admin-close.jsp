<%@ page import="org.springframework.web.util.HtmlUtils" %>
<%@ page import="
	org.opencms.workplace.*,
	org.opencms.util.CmsStringUtil,
	org.opencms.jsp.CmsJspActionElement
"%><%
	
CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
CmsDialog wp = new CmsDialog(jsp);
int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

%><html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>External Administration View Tool Bar</title>

<link rel=stylesheet type="text/css" href="<%= HtmlUtils.htmlEscape(HtmlUtils.htmlUnescape(CmsWorkplace.getStyleUri(wp.getJsp())))%>workplace.css">

<script >
<!--
	// Ask user whether he really wants to leave 
	function confirmExit()	{
		if (confirm ("<%= HtmlUtils.htmlEscape(HtmlUtils.htmlUnescape(wp.key("admin.message.exit"))) %>")) {
<% if (wp.isPopup() || CmsStringUtil.isEmpty(wp.getParamCloseLink()) || wp.getParamCloseLink().equals("null")) { 
    // this is a popup window, close it
%>
			top.close();
<%     } else { 
    // no popup, reload parent
%>
			parent.location.href='<%= wp.getParamCloseLink() %>';
<%     } %>
		}
	}
//-->
</script>

</head>
<body class="buttons-head" unselectable="on" >

<table cellspacing="0" cellpadding="0" border="0" width="100%" height="100%">
	<tr>
		<td>

<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 5) %>
<td class="maxwidth">&nbsp;</td>
<%= HtmlUtils.htmlEscape(HtmlUtils.htmlUnescape(wp.button("javascript:confirmExit();", null, "exit", "button.close", buttonStyle))) %>
<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %>

	</td>
</tr>
</table>

</body>
</html>