// var BASE_URL = "https://localhost:9800"
// var BASE_URL = "http://localhost:9801"
// var BASE_URL = "https://139.196.102.109:9800"
var BASE_URL = "https://www.starix.top:9800"

// 自动签到功能页使用百度地图api，页面用https的话会导致不能正常使用定位，所以这个页面使用http方式来访问
var BASE_HTTP_URL = "http://www.starix.top:9801"


function getQueryVariable(variable) {
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i=0;i<vars.length;i++) {
        var pair = vars[i].split("=");
        if(pair[0] == variable){return pair[1];}
    }
    return "";
}
