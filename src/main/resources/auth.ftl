<!DOCTYPE html>
<html lang="en">
<head>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1">
    <title>Login</title>
    <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700">
    <link href="https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css" rel="stylesheet">
    <script src="https://unpkg.com/material-components-web@latest/dist/material-components-web.min.js"></script>

    <style>

        html {
            height: 100%;
            padding: 0;
        }

        body {
            height: 100%;
            padding: 0;
            margin: 0;
            justify-content: center;
            display: flex;
        }

        .card-content {
            width: fit-content;
            height: fit-content;
            align-items: center;
            padding: 32px;
            margin: 0;
            position: absolute;
            top: 50%;
            -ms-transform: translateY(-50%);
            transform: translateY(-50%);
        }

        .login-text-field {
            margin-bottom: 16px;
        }

        .login-button {
            margin-right: 8px;
            margin-left: 8px;
        }

    </style>

</head>

<body>

<div class="mdc-card card-content">

    <body class="mdc-typography">
    <h1 id="message" class="mdc-typography--body1" style="color: #000000;">${message}</h1>
    </body>

    <#if privileges??>
        <ul style="width: 100%">
            <#list privileges as privilege>
                <li id="message" class="mdc-typography--body1" style="color: #000000;">${privilege}</li>
            </#list>
        </ul>
    </#if>

    <div style="display: ${display}">

        <body class="mdc-typography">
        <h1 id="error_message" class="mdc-typography--body2" style="color: #ff0000;"></h1>
        </body>

        <form method="post">

            <div class="mdc-text-field login-text-field">
                <input type="text" id="username" name="username" class="mdc-text-field__input">
                <label class="mdc-floating-label" for="username">Username</label>
                <div class="mdc-line-ripple"></div>
            </div>

            <br>

            <div class="mdc-text-field login-text-field">
                <input type="password" id="password" name="password" class="mdc-text-field__input" required minlength=8>
                <label for="password" class="mdc-floating-label">Password</label>
                <div class="mdc-line-ripple"></div>
            </div>

        </form>

        <div>

            <button class="mdc-button login-button" onclick="authenticate('login')">
                <span class="mdc-button__ripple"></span>
                Login
            </button>

            <button type="submit" onclick="authenticate('signup')"
                    class="mdc-button mdc-button--raised login-button">
                <span class="mdc-button__ripple"></span>
                Signup
            </button>

        </div>

    </div>

</div>

</body>
<script type="text/javascript">

    const Strings = {
        create: (function () {
            const regexp = /:([^:]+)/g;
            return function (str, o) {
                return str.replace(regexp, function (ignore, key) {
                    return (key = o[key]) == null ? '' : key;
                });
            }
        })()
    };

    function encodeQueryData(data) {
        const ret = [];
        for (let d in data)
            ret.push(encodeURIComponent(d) + '=' + encodeURIComponent(data[d]));
        return ret.join('&');
    }

    function authenticate(method) {
        let username = document.getElementById('username').value;
        let password = document.getElementById('password').value;
        const form = {username: username, password: password};
        const queryString = encodeQueryData(form) // create the query string
        const url = Strings.create("${statics["api.APIManager"].API_CODE}", {method: method})
        fetch(url + "\?" + queryString, {method: "POST"})
            .then(res => res.json())
            .then(out => {
                if (out.hasOwnProperty('_embedded')
                    && out._embedded.hasOwnProperty('status')
                    && out._embedded.status == '${statics["api.ApiException"].STATUS}') {
                    // error
                    document.getElementById('error_message').innerHTML = out._embedded.message;
                } else {
                    document.getElementById('message').innerHTML = 'Authentication went fine, please wait...';
                    window.location = out.redirect_uri;
                }
            });
    }

    const fields = document.querySelectorAll('.mdc-text-field');
    fields.forEach(textfield => mdc.textField.MDCTextField.attachTo(textfield));

</script>
</html>