<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Redirect to the HomeServlet to follow the MVC flow
    response.sendRedirect(request.getContextPath() + "/home");
%>
