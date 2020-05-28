<!DOCTYPE html>
<html lang="en">
<head>

    <meta charset="utf-8">
    <title>Redirecting...</title>

</head>

<body></body>
<script type="text/javascript">

    fetch('${statics["api.APIManager"].API_TOKEN}?code=${code}', {method: "POST"})
        .then(res => res.json())
        .then(out => {
            localStorage.setItem('token', out["_embedded"]["oauth"]);
            window.location = '${statics["http.HTTPManager"].PAGE_PROTECTED_USER}'
        });

</script>
</html>