<#-- @ftlvariable name="respond" type="net.colors_wind.nplweb.template.RespondInfo" -->
<#-- @ftlvariable name="pending" type="net.colors_wind.nplweb.template.PendingInfo" -->
<!DOCTYPE html>
<html lang="zh_CN">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>DL-NLP 电影评论分类</title>
    <#if pending??><meta http-equiv="refresh" content="${pending.refresh}"></#if>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:300,400,700" rel="stylesheet">
    <!-- Bulma Version 0.9.0-->
    <link rel="stylesheet" href="../static/css/bulma.min.css"/>
    <link rel="stylesheet" type="text/css" href="../static/css/landing.css">
</head>

<body>
<section class="hero is-info is-fullheight">
    <div class="hero-head">
        <nav class="navbar">
            <div class="container">
                <div class="navbar-brand">
                    <a class="navbar-item" href="..">
                        <img src="static/images/logo.png" alt="Logo">
                    </a>
                    <span class="navbar-burger burger" data-target="navbarMenu">
                            <span></span>
                            <span></span>
                            <span></span>
                        </span>
                </div>
                <div id="navbarMenu" class="navbar-menu">
                    <div class="navbar-end">
                            <span class="navbar-item">
                                <a class="button is-white is-outlined"
                                   href="/">
                                    <span class="icon">
                                        <i class="fa fa-home"></i>
                                    </span>
                                    <span>主页</span>
                                </a>
                            </span>
                        <span class="navbar-item">
                                <a class="button is-white is-outlined"
                                   href="/?text=random">
                                    <span class="icon">
                                        <i class="fa fa-superpowers"></i>
                                    </span>
                                    <span>随机电影</span>
                                </a>
                            </span>
                        <span class="navbar-item">
                                <a class="button is-white is-outlined"
                                   href="https://github.com/ColorsWind/DL-NLP/wiki">
                                    <span class="icon">
                                        <i class="fa fa-book"></i>
                                    </span>
                                    <span>帮助</span>
                                </a>
                            </span>
                        <span class="navbar-item">
                                <a class="button is-white is-outlined"
                                   href="https://github.com/ColorsWind/DL-NLP">
                                    <span class="icon">
                                        <i class="fa fa-github"></i>
                                    </span>
                                    <span>源代码</span>
                                </a>
                            </span>
                    </div>
                </div>
            </div>
        </nav>
    </div>

    <div class="hero-body">
        <div class="container has-text-centered">
            <div class="column is-8 is-offset-2">
                <h1 class="title">
                    电影评论分类
                </h1>
                <#list respond.items as item>
                    <p class="title">${item}</p>
                </#list>
                <h2 class="subtitle">
                    <#if respond.sentenceData??>
                        <table border="1" style="text-align:center">
                            <tr>
                                <th style="background-color:white" width="800">句子</th>
                                <th style="background-color:white" width="100">评分</th>
                            </tr>
                            <#list respond.sentenceData as item>
                                <tr>
                                    <td>${item.sentence}</td>
                                    <td>${item.getRating()}</td>
                                </tr>
                            </#list>
                        </table>
                    </#if>
                </h2>
                <form action="/" method="get" enctype="application/x-www-form-urlencoded">
                    <div class="box">
                        <div class="field is-grouped">

                            <p class="control is-expanded">
                                <label>
                                    <input class="input" type="text" name="text" placeholder="输入你要分析的句子/豆瓣电影id...">
                                </label>
                            </p>
                            <p class="control">
                                <input class="button is-info" type="submit" value="分析"/>
                            </p>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>

</section>
<script async type="text/javascript" src="../static/js/bulma.js"></script>
</body>

</html>

