<%@ page import="org.springframework.web.util.HtmlUtils" %>
<%@ page import="org.opencms.workplace.*" %><%
	
	// get workplace class from request attribute
	CmsDialog wp = CmsDialog.initCmsDialog(pageContext, request, response);
	wp.setParamAction(CmsDialog.DIALOG_CONFIRMED);

 %><%= HtmlUtils.htmlEscape(HtmlUtils.htmlUnescape(wp.htmlStart())) %>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>

<%= wp.dialogContentStart(wp.getParamTitle()) %>
<%= wp.getParamMessage() %>
<%= wp.dialogContentEnd() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>       
<%= wp.dialogButtonsOkCancel() %>
</form>

<%= wp.dialogEnd() %>  
<%= wp.bodyEnd() %>
<%= HtmlUtils.htmlEscape(HtmlUtils.htmlUnescape(wp.htmlEnd())) %>