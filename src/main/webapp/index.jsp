<%--
    Cua vao cua app. Chua co trang chu that (home.jsp thuoc pham vi Tan),
    nen tam dieu huong thang vao gio hang de co cho ma bam vao.

    Dung <c:redirect> chu khong response.sendRedirect trong scriptlet:
    project cam scriptlet trong JSP (Rule 1).
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:redirect url="/cart" />
