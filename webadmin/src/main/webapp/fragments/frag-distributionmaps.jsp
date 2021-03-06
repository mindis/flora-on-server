<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<c:choose>
<c:when test="${param.maps=='alvo'}">
<div id="allmapholder">
<c:forEach var="taxon" items="${allTaxa}">
    <div>
    <div class="header">${taxon.getName()}</div>
    <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=1&shadow=0&taxon=${taxon._getIDURLEncoded()}" width="200px" height="100px"/>
    <%-- <t:ajaxloadhtml url="http://localhost:8080/api/svgmap?basemap=1&size=10000&border=1&shadow=0&taxon=${taxon._getIDURLEncoded()}" width="200px" height="100px"/> --%>
    </div>
</c:forEach>
</div>
</c:when>

<c:when test="${param.maps=='threats'}">
<h1>Distribuição das plantas ameaçadas</h1>
<p>Coloque o rato numa quadrícula para ver as plantas aí existentes, com a respectiva categoria de ameaça.</p>
<div id="allmapholder" class="big interactive">
    <c:set var="refresh" value="${user.isGuest() ? '' : '&refresh=1'}"/>
    <div>
        <h3>Ameaçadas</h3>
        <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&category=threatened${refresh}" width="400px" height="200px"/>
    </div>
    <div>
        <h3>Potencialmente extintas</h3>
        <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&category=maybeextinct${refresh}" width="400px" height="200px"/>
    </div>
    <div>
        <h3>Criticamente Em Perigo (CR)</h3>
        <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&category=CR${refresh}" width="400px" height="200px"/>
    </div>
    <div>
        <h3>Em Perigo (EN)</h3>
        <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&category=EN${refresh}" width="400px" height="200px"/>
    </div>
    <div>
        <h3>Vulnerável (VU)</h3>
        <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&category=VU${refresh}" width="400px" height="200px"/>
    </div>
</div>
</c:when>

<c:otherwise>
<h1>Maps</h1>
<div class="outer">
    <div class="bigbutton section2">
        <h1><a href="?w=allmaps&maps=alvo">All Lista Alvo</a></h1>
    </div>
    <div class="bigbutton section3">
        <h1><a href="?w=allmaps&maps=threats">By threat category</a></h1>
    </div>
</div>
</c:otherwise>

</c:choose>
</div>
