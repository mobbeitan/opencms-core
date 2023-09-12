<%@ page import="org.springframework.web.util.HtmlUtils" %>
<%@ page import="org.opencms.workplace.list.*"%>
<%	
	// initialize the workplace class
	CmsListCsvExportDialog wp = new CmsListCsvExportDialog(pageContext, request, response);        
%>
<%= HtmlUtils.htmlEscape(HtmlUtils.htmlUnescape(wp.generateCsv())) %>
