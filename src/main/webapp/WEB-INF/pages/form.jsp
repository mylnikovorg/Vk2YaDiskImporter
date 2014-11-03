<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
<title>Vk to Yandex.Disk Importer</title>
</head>

<body>
<h2>What groups would you like to import?</h2>

<form:form method="POST" commandName="groupsnamessubmit">
    <table>
        <tbody>
        <tr>
            <td>
                <ul>
                    <form:checkboxes element="li" items="${groupnames}" path="groups"/>
                </ul>

            </td>
        </tr>
        <tr>
            <td>
                <input value="Begin import!" type="submit">
            </td>
        </tr>
        </tbody>
    </table>
</form:form>

</body>
</html>