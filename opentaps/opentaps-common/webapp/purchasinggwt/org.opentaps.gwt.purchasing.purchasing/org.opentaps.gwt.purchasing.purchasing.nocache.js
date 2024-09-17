function org_opentaps_gwt_purchasing_purchasing(){
  var $wnd_0 = window, $doc_0 = document, $stats = $wnd_0.__gwtStatsEvent?function(a){
    return $wnd_0.__gwtStatsEvent(a);
  }
  :null, $sessionId_0 = $wnd_0.__gwtStatsSessionId?$wnd_0.__gwtStatsSessionId:null, scriptsDone, loadDone, bodyDone, base = '', metaProps = {}, values = [], providers = [], answers = [], softPermutationId = 0, onLoadErrorFunc, propertyErrorFunc;
  $stats && $stats({moduleName:'org.opentaps.gwt.purchasing.purchasing', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'begin'});
  if (!$wnd_0.__gwt_stylesLoaded) {
    $wnd_0.__gwt_stylesLoaded = {};
  }
  if (!$wnd_0.__gwt_scriptsLoaded) {
    $wnd_0.__gwt_scriptsLoaded = {};
  }
  function isHostedMode(){
    var result = false;
    try {
      var query = $wnd_0.location.search;
      return (query.indexOf('gwt.codesvr=') != -1 || (query.indexOf('gwt.hosted=') != -1 || $wnd_0.external && $wnd_0.external.gwtOnLoad)) && query.indexOf('gwt.hybrid') == -1;
    }
     catch (e) {
    }
    isHostedMode = function(){
      return result;
    }
    ;
    return result;
  }

  function maybeStartModule(){
    if (scriptsDone && loadDone) {
      var iframe = $doc_0.getElementById('org.opentaps.gwt.purchasing.purchasing');
      var frameWnd = iframe.contentWindow;
      if (isHostedMode()) {
        frameWnd.__gwt_getProperty = function(name_0){
          return computePropValue(name_0);
        }
        ;
      }
      org_opentaps_gwt_purchasing_purchasing = null;
      frameWnd.gwtOnLoad(onLoadErrorFunc, 'org.opentaps.gwt.purchasing.purchasing', base, softPermutationId);
      $stats && $stats({moduleName:'org.opentaps.gwt.purchasing.purchasing', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'end'});
    }
  }

  function computeScriptBase(){
    if (metaProps['baseUrl']) {
      base = metaProps['baseUrl'];
      return base;
    }
    var thisScript;
    var scriptTags = $doc_0.getElementsByTagName('script');
    for (var i = 0; i < scriptTags.length; ++i) {
      if (scriptTags[i].src.indexOf('org.opentaps.gwt.purchasing.purchasing.nocache.js') != -1) {
        thisScript = scriptTags[i];
      }
    }
    if (!thisScript) {
      var markerId = '__gwt_marker_org.opentaps.gwt.purchasing.purchasing';
      var markerScript;
      $doc_0.write('<script id="' + markerId + '"><\/script>');
      markerScript = $doc_0.getElementById(markerId);
      thisScript = markerScript && markerScript.previousSibling;
      while (thisScript && thisScript.tagName != 'SCRIPT') {
        thisScript = thisScript.previousSibling;
      }
    }
    function getDirectoryOfFile(path){
      var hashIndex = path.lastIndexOf('#');
      if (hashIndex == -1) {
        hashIndex = path.length;
      }
      var queryIndex = path.indexOf('?');
      if (queryIndex == -1) {
        queryIndex = path.length;
      }
      var slashIndex = path.lastIndexOf('/', Math.min(queryIndex, hashIndex));
      return slashIndex >= 0?path.substring(0, slashIndex + 1):'';
    }

    ;
    if (thisScript && thisScript.src) {
      base = getDirectoryOfFile(thisScript.src);
    }
    if (base == '') {
      var baseElements = $doc_0.getElementsByTagName('base');
      if (baseElements.length > 0) {
        base = baseElements[baseElements.length - 1].href;
      }
       else {
        base = getDirectoryOfFile($doc_0.location.href);
      }
    }
     else if (base.match(/^\w+:\/\//)) {
    }
     else {
      var img = $doc_0.createElement('img');
      img.src = base + 'clear.cache.gif';
      base = getDirectoryOfFile(img.src);
    }
    if (markerScript) {
      markerScript.parentNode.removeChild(markerScript);
    }
    return base;
  }

  function processMetas(){
    var metas = document.getElementsByTagName('meta');
    for (var i = 0, n = metas.length; i < n; ++i) {
      var meta = metas[i], name_0 = meta.getAttribute('name'), content_0;
      if (name_0) {
        name_0 = name_0.replace('org.opentaps.gwt.purchasing.purchasing::', '');
        if (name_0.indexOf('::') >= 0) {
          continue;
        }
        if (name_0 == 'gwt:property') {
          content_0 = meta.getAttribute('content');
          if (content_0) {
            var value, eq = content_0.indexOf('=');
            if (eq >= 0) {
              name_0 = content_0.substring(0, eq);
              value = content_0.substring(eq + 1);
            }
             else {
              name_0 = content_0;
              value = '';
            }
            metaProps[name_0] = value;
          }
        }
         else if (name_0 == 'gwt:onPropertyErrorFn') {
          content_0 = meta.getAttribute('content');
          if (content_0) {
            try {
              propertyErrorFunc = eval(content_0);
            }
             catch (e) {
              alert('Bad handler "' + content_0 + '" for "gwt:onPropertyErrorFn"');
            }
          }
        }
         else if (name_0 == 'gwt:onLoadErrorFn') {
          content_0 = meta.getAttribute('content');
          if (content_0) {
            try {
              onLoadErrorFunc = eval(content_0);
            }
             catch (e) {
              alert('Bad handler "' + content_0 + '" for "gwt:onLoadErrorFn"');
            }
          }
        }
      }
    }
  }

  function __gwt_isKnownPropertyValue(propName, propValue){
    return propValue in values[propName];
  }

  function __gwt_getMetaProperty(name_0){
    var value = metaProps[name_0];
    return value == null?null:value;
  }

  function unflattenKeylistIntoAnswers(propValArray, value){
    var answer = answers;
    for (var i = 0, n = propValArray.length - 1; i < n; ++i) {
      answer = answer[propValArray[i]] || (answer[propValArray[i]] = []);
    }
    answer[propValArray[n]] = value;
  }

  function computePropValue(propName){
    var value = providers[propName](), allowedValuesMap = values[propName];
    if (value in allowedValuesMap) {
      return value;
    }
    var allowedValuesList = [];
    for (var k in allowedValuesMap) {
      allowedValuesList[allowedValuesMap[k]] = k;
    }
    if (propertyErrorFunc) {
      propertyErrorFunc(propName, allowedValuesList, value);
    }
    throw null;
  }

  var frameInjected;
  function maybeInjectFrame(){
    if (!frameInjected) {
      frameInjected = true;
      var iframe = $doc_0.createElement('iframe');
      iframe.src = "javascript:''";
      iframe.id = 'org.opentaps.gwt.purchasing.purchasing';
      iframe.style.cssText = 'position:absolute;width:0;height:0;border:none';
      iframe.tabIndex = -1;
      $doc_0.body.appendChild(iframe);
      $stats && $stats({moduleName:'org.opentaps.gwt.purchasing.purchasing', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'moduleRequested'});
      iframe.contentWindow.location.replace(base + initialHtml);
    }
  }

  providers['locale'] = function(){
    try {
      var locale;
      var defaultLocale = 'default' || 'default';
      if (locale == null) {
        var args = location.search;
        var startLang = args.indexOf('locale=');
        if (startLang >= 0) {
          var language = args.substring(startLang);
          var begin = language.indexOf('=') + 1;
          var end = language.indexOf('&');
          if (end == -1) {
            end = language.length;
          }
          locale = language.substring(begin, end);
        }
      }
      if (locale == null) {
        locale = __gwt_getMetaProperty('locale');
      }
      if (locale == null) {
        locale = $wnd_0['__gwt_Locale'];
      }
       else {
        $wnd_0['__gwt_Locale'] = locale || defaultLocale;
      }
      if (locale == null) {
        return defaultLocale;
      }
      while (!__gwt_isKnownPropertyValue('locale', locale)) {
        var lastIndex = locale.lastIndexOf('_');
        if (lastIndex == -1) {
          locale = defaultLocale;
          break;
        }
         else {
          locale = locale.substring(0, lastIndex);
        }
      }
      return locale;
    }
     catch (e) {
      alert('Unexpected exception in locale detection, using default: ' + e);
      return 'default';
    }
  }
  ;
  values['locale'] = {bg:0, 'default':1, en:2, es:3, fr:4, it:5, nl:6, pt:7, ru:8, zh:9};
  providers['user.agent'] = function(){
    var ua = navigator.userAgent.toLowerCase();
    var makeVersion = function(result){
      return parseInt(result[1]) * 1000 + parseInt(result[2]);
    }
    ;
    if (ua.indexOf('opera') != -1) {
      return 'opera';
    }
     else if (ua.indexOf('webkit') != -1) {
      return 'safari';
    }
     else if (ua.indexOf('msie') != -1) {
      if (document.documentMode >= 8) {
        return 'ie8';
      }
       else {
        var result_0 = /msie ([0-9]+)\.([0-9]+)/.exec(ua);
        if (result_0 && result_0.length == 3) {
          var v = makeVersion(result_0);
          if (v >= 6000) {
            return 'ie6';
          }
        }
      }
    }
     else if (ua.indexOf('gecko') != -1) {
      var result_0 = /rv:([0-9]+)\.([0-9]+)/.exec(ua);
      if (result_0 && result_0.length == 3) {
        if (makeVersion(result_0) >= 1008)
          return 'gecko1_8';
      }
      return 'gecko';
    }
    return 'unknown';
  }
  ;
  values['user.agent'] = {gecko:0, gecko1_8:1, ie6:2, ie8:3, opera:4, safari:5};
  org_opentaps_gwt_purchasing_purchasing.onScriptLoad = function(){
    if (frameInjected) {
      loadDone = true;
      maybeStartModule();
    }
  }
  ;
  org_opentaps_gwt_purchasing_purchasing.onInjectionDone = function(){
    scriptsDone = true;
    $stats && $stats({moduleName:'org.opentaps.gwt.purchasing.purchasing', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'end'});
    maybeStartModule();
  }
  ;
  processMetas();
  computeScriptBase();
  var strongName;
  var initialHtml;
  if (isHostedMode()) {
    if ($wnd_0.external && ($wnd_0.external.initModule && $wnd_0.external.initModule('org.opentaps.gwt.purchasing.purchasing'))) {
      $wnd_0.location.reload();
      return;
    }
    initialHtml = 'hosted.html?org_opentaps_gwt_purchasing_purchasing';
    strongName = '';
  }
  $stats && $stats({moduleName:'org.opentaps.gwt.purchasing.purchasing', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'selectingPermutation'});
  if (!isHostedMode()) {
    try {
      unflattenKeylistIntoAnswers(['fr', 'gecko'], '06FE4C61ABBA38084584C57D0B4FA481');
      unflattenKeylistIntoAnswers(['default', 'opera'], '11B7B8226452515F829A5F4A9D3C3EFE');
      unflattenKeylistIntoAnswers(['pt', 'gecko'], '19F8346BAEB12BEA06D3607C07332DF3');
      unflattenKeylistIntoAnswers(['en', 'opera'], '28B400A54EA18811EED4E63E7AF26192');
      unflattenKeylistIntoAnswers(['es', 'gecko1_8'], '28C3E53C0F0F9E9C5881630E3D968628');
      unflattenKeylistIntoAnswers(['en', 'safari'], '2FD91449E20FD7C7130F5E44004580DD');
      unflattenKeylistIntoAnswers(['zh', 'gecko1_8'], '398DA985AFCC0F79A214C61DE6B6F301');
      unflattenKeylistIntoAnswers(['fr', 'gecko1_8'], '3F800740DD3B8D926211C3DE49957C9D');
      unflattenKeylistIntoAnswers(['nl', 'safari'], '3FB0A20B9239510C9C6F901634729670');
      unflattenKeylistIntoAnswers(['bg', 'safari'], '490477DF9E1E460913433CC07A6553FB');
      unflattenKeylistIntoAnswers(['bg', 'ie6'], '4D66E7469500B8C691B3070BA2CED644');
      unflattenKeylistIntoAnswers(['pt', 'safari'], '4EF182898F91D575194236F79F38C112');
      unflattenKeylistIntoAnswers(['es', 'ie6'], '5242276A0D96ED8A7C93E905E1A7F61F');
      unflattenKeylistIntoAnswers(['fr', 'ie6'], '5DF022484F9CF22A828F165835EE7125');
      unflattenKeylistIntoAnswers(['nl', 'opera'], '5FA37B1B576E0E5275C5EFBC1C72B612');
      unflattenKeylistIntoAnswers(['zh', 'ie6'], '61D6EBE7364FB4528DF4B7178047ABDA');
      unflattenKeylistIntoAnswers(['nl', 'gecko'], '62A9ADB68FCB1F09A6AB25F3F17EED39');
      unflattenKeylistIntoAnswers(['zh', 'gecko'], '62DC88743F32F443B16A8B49C33BB8EA');
      unflattenKeylistIntoAnswers(['pt', 'gecko1_8'], '68D53FDE41256D29496C11E49BE765F9');
      unflattenKeylistIntoAnswers(['ru', 'ie6'], '693BCD0D92900C551454B84931DAF947');
      unflattenKeylistIntoAnswers(['nl', 'ie6'], '6F08D14A872E64D339246684227862C6');
      unflattenKeylistIntoAnswers(['zh', 'safari'], '71188EB3FD9527CB588A730F40B1256A');
      unflattenKeylistIntoAnswers(['ru', 'gecko1_8'], '770247CD13BA16C8483A7B831DD185E2');
      unflattenKeylistIntoAnswers(['default', 'ie6'], '81F6C9FB38A879FDC3531EAD55CDE5FB');
      unflattenKeylistIntoAnswers(['pt', 'opera'], '88C49D332DD3592D95B377EBC9C8D1E4');
      unflattenKeylistIntoAnswers(['bg', 'gecko'], '8B79117E2AF6E45F5DB7CD082C4C5254');
      unflattenKeylistIntoAnswers(['default', 'gecko'], '95FB21394F6280520A72ED0DF557ABFB');
      unflattenKeylistIntoAnswers(['fr', 'safari'], '9835E8AC41881FD64FDBF196B8AD6709');
      unflattenKeylistIntoAnswers(['bg', 'opera'], '9F20E251D5AE0164FAC84F2A106419BC');
      unflattenKeylistIntoAnswers(['nl', 'gecko1_8'], 'A31A993354D634593877350E9F0F204D');
      unflattenKeylistIntoAnswers(['en', 'gecko1_8'], 'A548DDF496C6F2721B77EA9C7B32FC14');
      unflattenKeylistIntoAnswers(['es', 'gecko'], 'BB97D2B5E135F3AD36A195364239F34F');
      unflattenKeylistIntoAnswers(['bg', 'gecko1_8'], 'BEC627EAEB7A823A0334564EAA715324');
      unflattenKeylistIntoAnswers(['default', 'safari'], 'C0672DA67B80694E38C4DD23C4FB0195');
      unflattenKeylistIntoAnswers(['ru', 'safari'], 'D0C9AC9024018B2409DBE616E1103C1C');
      unflattenKeylistIntoAnswers(['es', 'opera'], 'D224C1DDD3419B98CE43261913AAE07A');
      unflattenKeylistIntoAnswers(['en', 'gecko'], 'D2697F19E144C1344A681AEBDBF054C6');
      unflattenKeylistIntoAnswers(['default', 'gecko1_8'], 'D3B7676299C757EEEE4F64A7869C4798');
      unflattenKeylistIntoAnswers(['it', 'gecko'], 'D4094D84A99EE6E15D725D5EA2881BA7');
      unflattenKeylistIntoAnswers(['it', 'ie6'], 'D8B5D57D2324AE11758FB7DF5B33A19C');
      unflattenKeylistIntoAnswers(['ru', 'opera'], 'DAFCAEE884A0D00234D6E8FACAF37E1F');
      unflattenKeylistIntoAnswers(['it', 'safari'], 'E1896592E3A42465947B38A34DF4D711');
      unflattenKeylistIntoAnswers(['en', 'ie6'], 'E3D1DB93B834C4564FD050825462ACD5');
      unflattenKeylistIntoAnswers(['es', 'safari'], 'E40559562835BEE15512848BF7A60C90');
      unflattenKeylistIntoAnswers(['zh', 'opera'], 'ED94B52702C1DB31F13440968EFB05AA');
      unflattenKeylistIntoAnswers(['fr', 'opera'], 'EE61248104C0AA52A1A65AE810021102');
      unflattenKeylistIntoAnswers(['it', 'gecko1_8'], 'EF2103C4A7384A5C81FB1134BB2E532F');
      unflattenKeylistIntoAnswers(['it', 'opera'], 'F6E1578660D2DA6241750D006561B5E1');
      unflattenKeylistIntoAnswers(['pt', 'ie6'], 'FC77D7A36DFF87EBAA6B546900FB4EC0');
      unflattenKeylistIntoAnswers(['ru', 'gecko'], 'FEF92A8AFCA7F5FFF6EE29FC87CFC778');
      strongName = answers[computePropValue('locale')][computePropValue('user.agent')];
      var idx = strongName.indexOf(':');
      if (idx != -1) {
        softPermutationId = Number(strongName.substring(idx + 1));
        strongName = strongName.substring(0, idx);
      }
      initialHtml = strongName + '.cache.html';
    }
     catch (e) {
      return;
    }
  }
  var onBodyDoneTimerId;
  function onBodyDone(){
    if (!bodyDone) {
      bodyDone = true;
      if (!__gwt_stylesLoaded['standard.css']) {
        var l = $doc_0.createElement('link');
        __gwt_stylesLoaded['standard.css'] = l;
        l.setAttribute('rel', 'stylesheet');
        l.setAttribute('href', base + 'standard.css');
        $doc_0.getElementsByTagName('head')[0].appendChild(l);
      }
      if (!__gwt_stylesLoaded['GwtExt.css']) {
        var l = $doc_0.createElement('link');
        __gwt_stylesLoaded['GwtExt.css'] = l;
        l.setAttribute('rel', 'stylesheet');
        l.setAttribute('href', base + 'GwtExt.css');
        $doc_0.getElementsByTagName('head')[0].appendChild(l);
      }
      maybeStartModule();
      if ($doc_0.removeEventListener) {
        $doc_0.removeEventListener('DOMContentLoaded', onBodyDone, false);
      }
      if (onBodyDoneTimerId) {
        clearInterval(onBodyDoneTimerId);
      }
    }
  }

  if ($doc_0.addEventListener) {
    $doc_0.addEventListener('DOMContentLoaded', function(){
      maybeInjectFrame();
      onBodyDone();
    }
    , false);
  }
  var onBodyDoneTimerId = setInterval(function(){
    if (/loaded|complete/.test($doc_0.readyState)) {
      maybeInjectFrame();
      onBodyDone();
    }
  }
  , 50);
  $stats && $stats({moduleName:'org.opentaps.gwt.purchasing.purchasing', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'end'});
  $stats && $stats({moduleName:'org.opentaps.gwt.purchasing.purchasing', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'begin'});
  if (!__gwt_scriptsLoaded['adapter/ext/ext-base.js']) {
    __gwt_scriptsLoaded['adapter/ext/ext-base.js'] = true;
    document.write('<script language="javascript" src="' + base + 'adapter/ext/ext-base.js"><\/script>');
  }
  if (!__gwt_scriptsLoaded['ext-all.js']) {
    __gwt_scriptsLoaded['ext-all.js'] = true;
    document.write('<script language="javascript" src="' + base + 'ext-all.js"><\/script>');
  }
  $doc_0.write('<script defer="defer">org_opentaps_gwt_purchasing_purchasing.onInjectionDone(\'org.opentaps.gwt.purchasing.purchasing\')<\/script>');
}

org_opentaps_gwt_purchasing_purchasing();
