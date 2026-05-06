<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP - Hello World</title>
</head>
<body>
<h1>${msg}</h1>
<br/>
<div class="hello"></div>
<div class="ski"></div>
<a >Hello Servlet 111</a>
<%--<a href="googleservlet">Đăng nhập bằng Google</a>--%>
<br/>
<a href="login">Login</a>
<br/>
<a href="register">Register</a>

</body>
    <script>
        const divElement = document.querySelector(".hello")
        const divElement2 = document.querySelector(".ski")
        fetch("http://localhost:8080/api/map")
            .then((res) =>{
                return res.json();
            })
            .then((data) =>{
                console.log(data)
                const p1 = document.createElement("p")
                p1.textContent= data.name
                const p2 = document.createElement("p")
                p2.textContent= data.age
                divElement.appendChild(p1)
                divElement.appendChild(p2)
                let htmls =''
                data.skills.forEach(function(skill){
                    htmls+= `<p>\${skill}</p>`
                } )
                divElement2.innerHTML =htmls
            })
            .catch((err) => console.log(err))
    </script>
</html>