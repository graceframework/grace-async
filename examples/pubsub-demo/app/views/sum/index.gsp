<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Grace Async Examples</title>
</head>
<body>
    <div id="content" role="main">
        <div class="container">
            <section class="row">
                <a href="#list-user" class="skip" tabindex="-1"><g:message code="default.link.skip.label" /></a>
                <div class="col-12" role="navigation">
                    <ul class="nav nav-pills">
                        <li class="nav-item">
                            <a class="nav-link" href="${createLink(uri: '/')}">
                                <i class="bi bi-house-fill"></i><g:message code="default.home.label" />
                            </a>
                        </li>
                    </ul>
                </div>
            </section>
            <section class="row">
                <div id="list-user" class="col-12 scaffold scaffold-list" role="main">
                    <h1>Pubsub DEMO</h1>
                    <p>Total: ${total}, sum: ${sum}</p>
                </div>
            </section>
        </div>
    </div>

<script>
    function autoRefresh() {
        window.location = window.location.href;
    }
    setInterval('autoRefresh()', 1000);
</script>
</body>
</html>