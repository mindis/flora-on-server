<%@ tag description="Username mapper from IDs" pageEncoding="UTF-8" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%@ attribute name="idarray" required="true" type="java.lang.String[]" %><%@ attribute name="usermap" required="true" type="java.util.Map" %><%@ attribute name="separator" required="false" %><c:if test="${idarray != null}"><c:if test="${separator == null}"><c:set var="separator" value=", " /></c:if><c:forEach var="id" items="${idarray}" varStatus="loop">${usermap.get(id)}<c:if test="${!loop.last}">${separator}</c:if></c:forEach></c:if>