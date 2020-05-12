<!DOCTYPE html>
<html lang="en">
<head>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1">
    <title>Personal Page</title>
    <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700">
    <link href="https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css" rel="stylesheet">
    <script src="https://unpkg.com/material-components-web@latest/dist/material-components-web.min.js"></script>
    <script src="https://unpkg.com/masonry-layout@4/dist/masonry.pkgd.min.js"></script>

    <style>

        body {
            display: none;
            margin: 0;
            height: 100vh;
        }

        .mdc-top-app-bar--fixed-adjust {
            width: 100%;
        }

        #container {
            max-width: 1200px;
            margin: 0 auto;
            border: 2px dashed rgba(0, 0, 0, 0);
            box-sizing: border-box;
        }

        .mdc-image-list--masonry-js .mdc-image-list__item {
            /* calc((100% / $column-count) - (($gutterpx * ($column-count - 1)) / $column-count)) */
            width: calc((100% / 5) - ((16px * 4) / 5));
            margin: 0 0 16px;
        }

    </style>

</head>
<body>

<header class="mdc-top-app-bar mdc-top-app-bar--fixed">
    <div class="mdc-top-app-bar__row">
        <section class="mdc-top-app-bar__section mdc-top-app-bar__section--align-start">
            <button class="material-icons mdc-top-app-bar__navigation-icon mdc-icon-button">menu</button>
            <span id="username" class="mdc-top-app-bar__title"></span>
        </section>
        <section class="mdc-top-app-bar__section mdc-top-app-bar__section--align-end" role="toolbar">
            <button class="material-icons mdc-top-app-bar__action-item mdc-icon-button" aria-label="Download">
                more_vert
            </button>
        </section>
    </div>
</header>

<div class="mdc-top-app-bar--fixed-adjust">

    <form enctype="multipart/form-data">
        <label for="img">Select image:</label>
        <input type="file" id="img" name="img" accept="image/*">
    </form>

    <button onclick="onSubmit()"></button>

    <div id="container">
        <ul id="image-list" class="mdc-image-list mdc-image-list--masonry-js"></ul>
    </div>

</div>

</body>
<script type="text/javascript">

    // check if there is a token in local storage else, redirect
    const token = localStorage.getItem("token");
    if (token == null) window.location = "${statics["http.HTTPManager"].PAGE_LANDING}";

    function fetchWithAuthentication(input, params) {
        if (params == null) params = {};
        params['headers'] = {'Authorization': 'Bearer ' + token};
        let clone;
        return fetch(input, params)
            .then(res => {
                clone = res.clone();
                return res.json();
            })
            .then(out => {
                if (out.hasOwnProperty('_embedded')
                    && out._embedded.hasOwnProperty('status')
                    && out._embedded.status == '${statics["api.ApiException"].STATUS}') {
                    // remove the token
                    localStorage.removeItem('token')
                    window.location = "${statics["http.HTTPManager"].PAGE_LANDING}";
                }
                return clone;
            });
    }

    const Strings = {
        create: (function () {
            const regexp = /{([^{]+)}/g;
            return function (str, o) {
                return str.replace(regexp, function (ignore, key) {
                    return (key = o[key]) == null ? '' : key;
                });
            }
        })()
    };

    const imageList = document.getElementById("image-list");

    new Masonry(imageList, {
        columnWidth: '.mdc-image-list__item',
        itemSelector: '.mdc-image-list__item',
        percentPosition: true,
        gutter: 16,
        horizontalOrder: true,
    });

    function appendImage(url) {
        const li = document.createElement("li");
        const img = document.createElement("img");
        li.className = 'mdc-image-list__item';
        img.className = 'mdc-image-list__image';
        fetch(url, {headers: {'Authorization': 'Bearer ' + token}})
            .then(res => res.blob())
            .then(blob => img.src = URL.createObjectURL(blob));
        li.appendChild(img);
        imageList.appendChild(li);
    }

    let user; // used to store the user object when retrieved for the first time
    fetchWithAuthentication('${statics["api.APIManager"].API_PROTECTED_USER}')
        .then(res => res.json())
        .then(out => {
            document.body.style['display'] = 'flex';
            user = out;
            document.getElementById('username').innerText = out._embedded.username;
            document.title = 'Personal Page - ' + out._embedded.username;
            const images = out._links.images.href;
            fetchWithAuthentication(images)
                .then(res => res.json())
                .then(out => {
                    for (const image of out._embedded.images) {
                        appendImage(Strings.create(image._links.self.href, {id: image._embedded.id}));
                    }
                });
        });

    function onSubmit() {
        let img = document.getElementById("img").files[0];
        let formData = new FormData();
        formData.append("img", img);
        fetchWithAuthentication(user._links.upload.href, {method: "POST", body: formData})
            .then(res => res.json())
            .then(image => {
                appendImage(Strings.create(image._links.self.href, {id: image._embedded.id}));
            });
    }

</script>
</html>