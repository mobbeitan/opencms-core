<%@ page import="org.springframework.web.util.HtmlUtils" %>
<%@ page import="org.opencms.workplace.galleries.CmsOpenGallery" %>
<%	
	// initialize the workplace class
	CmsOpenGallery wp = new CmsOpenGallery(pageContext, request, response);	
%>
<%= HtmlUtils.htmlEscape(HtmlUtils.htmlUnescape(wp.htmlStart(null))) %>
<script >
<!--
	<%= wp.openGallery() %>
//-->
</script>
<% wp.actionCloseDialog(); %>
<%= HtmlUtils.htmlEscape(HtmlUtils.htmlUnescape(wp.htmlEnd())) %>
