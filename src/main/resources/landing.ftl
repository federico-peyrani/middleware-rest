<!DOCTYPE html>
<html lang="en">
<head>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1">
    <title>Landing</title>

    <script src="/script.js"></script>

</head>

<body></body>
<script type="text/javascript">

    const token = localStorage.getItem("token");
    if (token == null) {
        const form = {response_type: "code", client_id: "REST", redirect_uri: "${redirect_uri}", privilege: "UPLOAD"};
        const queryString = encodeQueryData(form);
        window.location = "${statics["http.HTTPManager"].PAGE_AUTH}\?" + queryString;
    } else {
        window.location = "${statics["http.HTTPManager"].PAGE_PROTECTED_USER}";
    }

</script>
</html>