<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
	<title>Red list info Manager</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link href='http://fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'>
	<link rel="stylesheet" type="text/css" href="/floraon/base.css"/>
	<link rel="stylesheet" type="text/css" href="/floraon/redlist.css"/>
	<script type="text/javascript" src="/floraon/sorttable.js"></script>
	<script type="text/javascript" src="/floraon/basefunctions.js"></script>
	<script type="text/javascript" src="/floraon/ajaxforms.js"></script>
	<script type="text/javascript" src="/floraon/redlistadmin.js"></script>
</head>
<body>
<div id="title">Red List data portal</div>
<div id="main-holder">
    <c:if test="${sessionScope.user != null}">
        <p>You're not authorized to enter this page.</p>
    </c:if>
    <c:if test="${sessionScope.user == null}">
        <div id="left-bar">
            <ul>
                <li><a href="?w=main">Index of taxa</a></li>
            </ul>
        </div>
        <div id="main">
        <c:choose>
        <c:when test="${what=='addterritory'}">
            <h1>There are no red list datasets yet.</h1>
            <h2>Select a territory to create a dataset.</h2>
            <ul>
            <c:forEach var="terr" items="${territories}">
                <li><a href="redlist/api/newdataset?territory=${terr.getShortName()}">${terr.getName()}</a></li>
            </c:forEach>
            </ul>
        </c:when>
        <c:when test="${what=='main'}">
            <div id="filters">
                <h2>Filter</h2>
                <div class="filter">only native</div>
                <div class="filter selected">in Lista A</div>
                <div class="filter">in Lista B</div>
            </div>
            <table id="speciesindex">
            <c:forEach var="taxon" items="${specieslist.iterator()}">
                <c:if test="${taxon.getTaxEnt().isSpecies()}">
                    <tr class="species">
                </c:if>
                <c:if test="${!taxon.getTaxEnt().isSpecies()}">
                    <tr>
                </c:if>
                    <td><input type="checkbox"/></td>
                    <td><a href="?w=taxon&id=${taxon.getTaxEnt().getIDURLEncoded()}">${taxon.getTaxEnt().getFullName(true)}</a></td>
                    <td>${taxon.getInferredStatus().getNativeStatus().toString()}</td>
                </tr>
            </c:forEach>
            </table>
        </c:when>

        <c:when test="${what=='taxon'}">
            <c:if test="${occurrences == null}">
                <div class="warning"><b>Warning</b><br/>This taxon has no correspondence in Flora-On, please contact the checklist administrator</div>
            </c:if>
            <form class="poster" data-path="/floraon/redlist/api/updatedata">
                <input type="hidden" name="rldeid" value="${redlistdataentity.getID()}"/>

                <table class="sheet">
                    <tr class="section1"><td class="number" colspan="3">Section 1 - General info</td></tr>
                    <tr class="section1 textual"><td class="number">1.1</td><td><input type="submit" value="Save"/></td><td colspan="1"><h1><i>${taxon.getName()}</i></h1><p style="text-align:center"><a href="?w=taxonrecords&id=${taxon.getIDURLEncoded()}">view occurrences</a></p></td></tr>
                    <tr><td></td><td>${redlistdataentity.getInferredStatus().getVerbatimNativeStatus()}</td></tr>
                    <tr class="section1"><td class="number">1.2</td><td>Authority</td><td>${taxon.getAuthor()}</td></tr>
                    <tr class="section1"><td class="number">1.3</td><td>Synonyms</td><td>
                        <ul>
                        <c:forEach var="synonym" items="${synonyms}">
                            <li data-key="${synonym.getID()}"><c:out value="${synonym.getFullName()}"></c:out></li>
                        </c:forEach>
                        </ul>
                    </td></tr>
                    <tr class="section1"><td class="number">1.4</td><td>Taxonomic problems</td><td>
                        <table>
                            <tr><td>Has taxonomic problems</td><td>
                            <input type="checkbox" name="hasTaxonomicProblems">
                            </td></tr>
                            <tr><td>Problem description</td><td><input type="text" name="taxonomicProblemDescription"/></td></tr>
                        </table>
                    </td></tr>
                    <tr class="section2"><td class="number" colspan="3">Section 2 - Geographical Distribution</td></tr>
                    <tr class="section2 textual"><td class="number">2.1</td><td>Distribution (textual)</td><td>
                        <textarea name="geographicalDistribution_Description"></textarea>
                    </td></tr>
                    <tr class="section2"><td class="number">2.2</td><td>Extent Of Occurrence<br/>(EOO)</td><td>
                        <c:if test="${occurrences == null}">
                            No correspondence in Flora-On
                        </c:if>
                        <c:if test="${occurrences != null}">
                            <c:if test="${EOO == null}">
                                Not applicable (${occurrences.size()} occurrences)
                            </c:if>
                            <c:if test="${EOO != null}">
                                <b><fmt:formatNumber value="${EOO}" maxFractionDigits="3"/></b> km<sup>2</sup> (${occurrences.size()} occurrences)
                            </c:if>
                        </c:if>
                    </td></tr>
                    <tr class="section2"><td class="number">2.3</td><td>Area Of Occupancy<br/>(AOO)</td><td>
                        <c:if test="${occurrences == null}">
                            No correspondence in Flora-On
                        </c:if>
                        <c:if test="${occurrences != null}">
                            <b><fmt:formatNumber value="${AOO}" maxFractionDigits="4"/></b> km<sup>2</sup> (${nquads} ${sizeofsquare}x${sizeofsquare} km squares)
                        </c:if>
                    </td></tr>
                    <tr class="section2"><td class="number">2.4</td><td>Decline in distribution</td><td>
                        <select name="geographicalDistribution_DeclineDistribution">
                            <c:forEach var="tmp" items="${GeographicalDistribution_DeclineDistribution}">
                                <option value="${tmp}">${tmp}</option>
                            </c:forEach>
                        </select>
                    </td></tr>
                    <tr class="section2"><td class="number">2.5</td><td>Elevation</td><td>
                        <input name="geographicalDistribution_ElevationRange" type="text"/>
                        <input name="geographicalDistribution_ElevationRange" type="text"/>
                    </td></tr>
                    <tr class="section3"><td class="number" colspan="3">Section 3 - Population</td></tr>
                    <tr class="section3 textual"><td class="number">3.1</td><td>Population information (textual)</td><td>
                        <textarea name="population_Description"></textarea>
                    </td></tr>
                    <tr class="section3"><td class="number">3.2</td><td>Nº of mature individuals</td><td>
                        <table>
                            <tr><td>Category</td><td>
                                <select name="population_NrMatureIndividualsCategory">
                                    <c:forEach var="tmp" items="${population_NrMatureIndividualsCategory}">
                                        <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                                    </c:forEach>
                                </select>
                            </td></tr>
                            <tr><td>Exact number</td><td><input type="text" name="population_NrMatureIndividualsExact"/></td></tr>
                            <tr><td>Textual description</td><td><input type="text" name="population_NrMatureIndividualsDescription"/></td></tr>
                        </table>
                    </td></tr>
                    <tr class="section3"><td class="number">3.3</td><td>Type of estimate</td><td>
                        <select name="population_TypeOfEstimate">
                            <c:forEach var="tmp" items="${population_TypeOfEstimate}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:forEach>
                        </select>
                    </td></tr>
                    <tr class="section3"><td class="number">3.4</td><td>Population decline</td><td>
                        <select name="population_PopulationDecline">
                            <c:forEach var="tmp" items="${population_PopulationDecline}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:forEach>
                        </select>
                    </td></tr>
                    <tr class="section3"><td class="number">3.5</td><td>Population trend</td><td>
                        <input type="text" name="population_PopulationTrend"/>
                    </td></tr>
                    <tr class="section3"><td class="number">3.6</td><td>Severely fragmented</td><td>
                        <select name="population_SeverelyFragmented">
                            <c:forEach var="tmp" items="${population_SeverelyFragmented}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:forEach>
                        </select>
                    </td></tr>
                    <tr class="section3"><td class="number">3.7</td><td>Extreme fluctuations</td><td>
                        <select name="population_ExtremeFluctuations">
                            <c:forEach var="tmp" items="${population_ExtremeFluctuations}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:forEach>
                        </select>
                    </td></tr>
                    <tr class="section4"><td class="number" colspan="3">Section 4 - Ecology</td></tr>
                    <tr class="section4 textual"><td class="number">4.1</td><td>Habitats and ecology information (textual)</td><td>
                        <textarea name="verbatimEcology"></textarea>
                    </td></tr>
                    <tr class="section4"><td class="number">4.2</td><td>Habitat types</td><td>
                        <label>A <input type="checkbox" name="A"/></label>
                        <label>B <input type="checkbox" name="b"/></label>
                        <label>C <input type="checkbox" name="c"/></label>
                    </td></tr>
                    <tr class="section4"><td class="number">4.3</td><td>Life form</td><td>(automatico)</td></tr>
                    <tr class="section4"><td class="number">4.4</td><td>Generation length</td><td>
                        <select name="generationLength">
                            <option>No data</option>
                            <option>1 year</option>
                            <option>&gt; x years</option>
                        </select>
                    </td></tr>

                    <tr class="section5"><td class="number" colspan="3">Section 5 - Uses and trade</td></tr>
                    <tr class="section5 textual"><td class="number">5.1</td><td>Uses and trade (textual)</td><td>
                        <textarea name="verbatimUses"></textarea>
                    </td></tr>
                    <tr class="section5"><td class="number">5.2</td><td>Uses</td><td>
                        <label>A <input type="checkbox" name="A"/></label>
                        <label>B <input type="checkbox" name="b"/></label>
                        <label>C <input type="checkbox" name="c"/></label>
                    </td></tr>
                    <tr class="section5"><td class="number">5.3</td><td>Trade</td><td>
                        <input type="checkbox" name="isTraded"/>
                    </td></tr>
                    <tr class="section5"><td class="number">5.4</td><td>Overexploitation</td><td>
                        <select name="overexploitation">
                            <option>No data</option>
                            <option>Not ...</option>
                            <option>Explored but not...</option>
                            <option>Overexploi...</option>
                        </select>
                    </td></tr>

                    <tr class="section6"><td class="number" colspan="3">Section 6 - Threats</td></tr>
                    <tr class="section6 textual"><td class="number">6.1</td><td>Threat description (textual)</td><td>
                        <textarea name="verbatimThreats"></textarea>
                    </td></tr>
                    <tr class="section6 textual"><td class="number">6.2</td><td>Threats</td><td>
                        (a fazer...)
                    </td></tr>
                    <tr class="section6 textual"><td class="number">6.3</td><td>Number of locations</td><td>
                        <input type="text" name="numberLocations"/><br/>
                        (nº de subpops sugerido)
                    </td></tr>

                    <tr class="section7"><td class="number" colspan="3">Section 7 - Conservation</td></tr>
                    <tr class="section7 textual"><td class="number">7.1</td><td>Conservation measures (textual)</td><td>
                        <textarea name="verbatimConservationMeasures"></textarea>
                    </td></tr>
                    <tr class="section7"><td class="number">7.2</td><td>Conservation plans</td><td>
                        <select name="conservationPlans">
                            <option>No data</option>
                            <option>Yes</option>
                            <option>No</option>
                        </select>
                    </td></tr>
                    <tr class="section7"><td class="number">7.3</td><td><i>Ex-situ</i> conservation</td><td>
                        <select name="exsituConservation">
                            <option>No data</option>
                            <option>Yes</option>
                            <option>No</option>
                        </select>
                    </td></tr>
                    <tr class="section7"><td class="number">7.4</td><td>Occurrence in protected areas</td><td>
                    (automatico)
                    </td></tr>
                    <tr class="section7"><td class="number">7.5</td><td>Proposed conservation actions</td><td>
                        <select name="conservationPlans">
                            <option>Additional studies or measures are not required</option>
                            <option>Yes</option>
                            <option>No</option>
                        </select>
                    </td></tr>

                    <tr class="section8"><td class="number" colspan="3">Section 8 - Bibliographic references</td></tr>
                    <tr class="section8"><td class="number">8.1</td><td>Reference list</td><td>
                    (a fazer)
                    </td></tr>
                </table>
            </form>
        </c:when>

        <c:when test="${what=='taxonrecords'}">
            <h1>${taxon.getFullName(true)}</h1>
            <c:if test="${occurrences == null}">
                <div class="warning"><b>Warning</b><br/>This taxon has no correspondence in Flora-On, please contact the checklist administrator</div>
            </c:if>
            <h2>${occurrences.size()} occurrences</h2>
            <table class="sortable">
                <thead>
                    <tr><th>Record ID</th><th>Taxon</th><th>Latitude</th><th>Longitude</th><th>Year</th><th>Month</th>
                    <th>Day</th><th>Author</th><th style="width:180px">Notes</th><th>Precision</th><th>ID in doubt?</th><th>In flower?</th></tr>
                </thead>
                <c:forEach var="occ" items="${occurrences.iterator()}">
                    <tr>
                        <td>${occ.getId_reg()}</td>
                        <td><i>${occ.getGenus()} ${occ.getSpecies()} ${occ.getInfrataxon() == null ? '' : occ.getInfrataxon()}</i></td>
                        <td><fmt:formatNumber value="${occ.getLatitude()}" maxFractionDigits="4"/></td>
                        <td><fmt:formatNumber value="${occ.getLongitude()}" maxFractionDigits="4"/></td>
                        <td>${occ.getYear()}</td>
                        <td>${occ.getMonth()}</td>
                        <td>${occ.getDay()}</td>
                        <td>${occ.getAuthor()}</td>
                        <td style="width:180px">${occ.getNotes()}</td>
                        <td>${occ.getPrecision()}</td>
                        <td>${occ.getConfidence() ? '' : 'Yes'}</td>
                        <td>${occ.getFlowering() ? 'Yes' : ''}</td>
                    </tr>
                </c:forEach>
            </table>
        </c:when>
        </c:choose>
        </div>
    </c:if>
</div>

</body>
</html>
