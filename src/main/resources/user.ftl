<!DOCTYPE html>
<html lang="en">
<head>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1">
    <title>Personal page - ${username}</title>
    <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700">
    <link href="https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css" rel="stylesheet">
    <script src="https://unpkg.com/material-components-web@latest/dist/material-components-web.min.js"></script>

    <style>

        body {
            display: flex;
            margin: 0;
            height: 100vh;
        }

        .my-masonry-image-list {
            column-count: 4;
        }

    </style>

</head>
<body>

<header class="mdc-top-app-bar mdc-top-app-bar--fixed">
    <div class="mdc-top-app-bar__row">
        <section class="mdc-top-app-bar__section mdc-top-app-bar__section--align-start">
            <button class="material-icons mdc-top-app-bar__navigation-icon mdc-icon-button">menu</button>
            <span class="mdc-top-app-bar__title">${username}</span>
        </section>
        <section class="mdc-top-app-bar__section mdc-top-app-bar__section--align-end" role="toolbar">
            <button class="material-icons mdc-top-app-bar__action-item mdc-icon-button" aria-label="Download">
                more_vert
            </button>
        </section>
    </div>
</header>

<div class="mdc-top-app-bar--fixed-adjust">

    <form action="${statics["api.APIManager"].API_PROTECTED_UPLOAD}" method="post" enctype="multipart/form-data">
        <label for="img">Select image:</label>
        <input type="file" id="img" name="img" accept="image/*">
    </form>

    <button onclick="onSubmit()"></button>

    <ul id="image-list" class="mdc-image-list mdc-image-list--masonry my-masonry-image-list"></ul>

</div>

</body>
<script type="text/javascript">

    let token = '${token}';
    let oauth = '?${statics["api.APIManager"].REQUEST_PARAM_OAUTH}=';

    function onSubmit() {
        let img = document.getElementById("img").files[0];
        let formData = new FormData();
        formData.append("img", img);
        fetch('${statics["api.APIManager"].API_PROTECTED_UPLOAD}' + oauth + token, {method: "POST", body: formData})
            .then(res => res.json())
            .then(out => {
                const li = document.createElement("li");
                const img = document.createElement("img");
                li.className = 'mdc-image-list__item';
                img.className = 'mdc-image-list__image';
                img.src = '${statics["api.APIManager"].API_PROTECTED_IMAGE}/' + out.url + oauth + token;
                li.appendChild(img);
                imageList.appendChild(li);
            });
    }

    const imageListUrl = '${statics["api.APIManager"].API_PROTECTED_IMAGES}' + oauth + token;
    const imageList = document.getElementById("image-list");

    fetch(imageListUrl)
        .then(res => res.json())
        .then(out => {
            for (const image of out) {
                const li = document.createElement("li");
                const img = document.createElement("img");
                li.className = 'mdc-image-list__item';
                img.className = 'mdc-image-list__image';
                img.src = '${statics["api.APIManager"].API_PROTECTED_IMAGE}/' + image.url + oauth + token;
                li.appendChild(img);
                imageList.appendChild(li);
            }
        });

</script>
</html>