<!DOCTYPE html>
<html lang="en">
<head>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1">
    <title>Redirecting...</title>

</head>

<body></body>
<script type="text/javascript">

    localStorage.setItem('token', '${token}');
    window.location = '${statics["http.HTTPManager"].PAGE_PROTECTED_USER}'

</script>
</html>