<!DOCTYPE html>
<html lang="en">
<head>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1">
    <title>Landing</title>

</head>

<body></body>
<script type="text/javascript">

    function encodeQueryData(data) {
        const ret = [];
        for (let d in data)
            ret.push(encodeURIComponent(d) + '=' + encodeURIComponent(data[d]));
        return ret.join('&');
    }

    const form = {
        "response_type": "code",
        "client_id": "REST",
        "redirect_uri": "${redirect_uri}"
    };
    const queryString = encodeQueryData(form)

    const token = localStorage.getItem("token");
    if (token == null) window.location = "${statics["http.HTTPManager"].PAGE_AUTH}\?" + queryString

</script>
</html>