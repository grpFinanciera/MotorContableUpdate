function org_opentaps_gwt_financials_invoices_invoices(){
  var $wnd_0 = window, $doc_0 = document, $stats = $wnd_0.__gwtStatsEvent?function(a){
    return $wnd_0.__gwtStatsEvent(a);
  }
  :null, $sessionId_0 = $wnd_0.__gwtStatsSessionId?$wnd_0.__gwtStatsSessionId:null, scriptsDone, loadDone, bodyDone, base = '', metaProps = {}, values = [], providers = [], answers = [], softPermutationId = 0, onLoadErrorFunc, propertyErrorFunc;
  $stats && $stats({moduleName:'org.opentaps.gwt.financials.invoices.invoices', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'begin'});
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
      var iframe = $doc_0.getElementById('org.opentaps.gwt.financials.invoices.invoices');
      var frameWnd = iframe.contentWindow;
      if (isHostedMode()) {
        frameWnd.__gwt_getProperty = function(name_0){
          return computePropValue(name_0);
        }
        ;
      }
      org_opentaps_gwt_financials_invoices_invoices = null;
      frameWnd.gwtOnLoad(onLoadErrorFunc, 'org.opentaps.gwt.financials.invoices.invoices', base, softPermutationId);
      $stats && $stats({moduleName:'org.opentaps.gwt.financials.invoices.invoices', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'end'});
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
      if (scriptTags[i].src.indexOf('org.opentaps.gwt.financials.invoices.invoices.nocache.js') != -1) {
        thisScript = scriptTags[i];
      }
    }
    if (!thisScript) {
      var markerId = '__gwt_marker_org.opentaps.gwt.financials.invoices.invoices';
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
        name_0 = name_0.replace('org.opentaps.gwt.financials.invoices.invoices::', '');
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
      iframe.id = 'org.opentaps.gwt.financials.invoices.invoices';
      iframe.style.cssText = 'position:absolute;width:0;height:0;border:none';
      iframe.tabIndex = -1;
      $doc_0.body.appendChild(iframe);
      $stats && $stats({moduleName:'org.opentaps.gwt.financials.invoices.invoices', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'moduleRequested'});
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
  org_opentaps_gwt_financials_invoices_invoices.onScriptLoad = function(){
    if (frameInjected) {
      loadDone = true;
      maybeStartModule();
    }
  }
  ;
  org_opentaps_gwt_financials_invoices_invoices.onInjectionDone = function(){
    scriptsDone = true;
    $stats && $stats({moduleName:'org.opentaps.gwt.financials.invoices.invoices', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'end'});
    maybeStartModule();
  }
  ;
  processMetas();
  computeScriptBase();
  var strongName;
  var initialHtml;
  if (isHostedMode()) {
    if ($wnd_0.external && ($wnd_0.external.initModule && $wnd_0.external.initModule('org.opentaps.gwt.financials.invoices.invoices'))) {
      $wnd_0.location.reload();
      return;
    }
    initialHtml = 'hosted.html?org_opentaps_gwt_financials_invoices_invoices';
    strongName = '';
  }
  $stats && $stats({moduleName:'org.opentaps.gwt.financials.invoices.invoices', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'selectingPermutation'});
  if (!isHostedMode()) {
    try {
      unflattenKeylistIntoAnswers(['ru', 'gecko1_8'], '011A9A8C6290D4F10BB0D8C14BD3002D');
      unflattenKeylistIntoAnswers(['fr', 'opera'], '05BE1E26B68D24F99336E71F5E37E6A2');
      unflattenKeylistIntoAnswers(['it', 'opera'], '07010883F3DD317A84239D8A2B27AA31');
      unflattenKeylistIntoAnswers(['nl', 'safari'], '0B9355925B5BFA073A9601FBFB514690');
      unflattenKeylistIntoAnswers(['nl', 'ie6'], '175E68D58A57E2DE20B68DFA3C01BF81');
      unflattenKeylistIntoAnswers(['nl', 'gecko1_8'], '23F7ED3A1938072D3930746392C72747');
      unflattenKeylistIntoAnswers(['default', 'safari'], '2938D3518BE55CD2F30C004343A28DCF');
      unflattenKeylistIntoAnswers(['es', 'safari'], '30492C9E903C1C9E0866E0E89D1E8B33');
      unflattenKeylistIntoAnswers(['default', 'gecko1_8'], '3239A23FD7DCEDBC1D0631C94E63B81F');
      unflattenKeylistIntoAnswers(['fr', 'gecko1_8'], '3E78AB9E522289D11EFF348F9AB0A4BE');
      unflattenKeylistIntoAnswers(['es', 'gecko'], '3EC7EE54AF815DBE202E09F0956E426F');
      unflattenKeylistIntoAnswers(['ru', 'opera'], '45998548547EC83A5808D997717B2A2B');
      unflattenKeylistIntoAnswers(['es', 'ie6'], '4ACA0FFB54AAFD2F2A7B6364DA385ADC');
      unflattenKeylistIntoAnswers(['fr', 'safari'], '4BD4D2B99C092BE407A3C2FB58655F15');
      unflattenKeylistIntoAnswers(['en', 'safari'], '53E718AAB7C3A8D0BC8ABA50042C4E36');
      unflattenKeylistIntoAnswers(['bg', 'opera'], '5DECBBD4673A213189E620151B8FE3B4');
      unflattenKeylistIntoAnswers(['it', 'safari'], '63589B86AC6DF7DBF32673FAAF5AC809');
      unflattenKeylistIntoAnswers(['ru', 'gecko'], '747A862F1E20BE320C50403F9CF07CB5');
      unflattenKeylistIntoAnswers(['es', 'opera'], '75E0B4D74D7C535B8350BD5C59C48C29');
      unflattenKeylistIntoAnswers(['en', 'gecko1_8'], '7AE0C510D0A73D0958086A32CC821684');
      unflattenKeylistIntoAnswers(['it', 'gecko1_8'], '7BA07BE876A4F2E093541400C78203D6');
      unflattenKeylistIntoAnswers(['zh', 'gecko'], '89A7543D220F7A873527E7857598FF45');
      unflattenKeylistIntoAnswers(['bg', 'gecko'], '9AED93F9BD721906091C8BF399B9C724');
      unflattenKeylistIntoAnswers(['it', 'gecko'], '9C66DD8F3E2526B34FD3D5DB54C87039');
      unflattenKeylistIntoAnswers(['zh', 'ie6'], '9C7F473F46AF45DF86D27277896A3085');
      unflattenKeylistIntoAnswers(['bg', 'gecko1_8'], 'A2D59B4C5AFAF0E12742AB10635E7D2F');
      unflattenKeylistIntoAnswers(['pt', 'safari'], 'A7A28B55D271E3B1287839B27790B46D');
      unflattenKeylistIntoAnswers(['en', 'opera'], 'AE3E6B8DBB2C9BB9B7E05DE4BB09F331');
      unflattenKeylistIntoAnswers(['en', 'ie6'], 'AF41C47E1AD9807D1A7A87FE5030C6B3');
      unflattenKeylistIntoAnswers(['nl', 'gecko'], 'B152512B1104E566F44AF9A07E024BE4');
      unflattenKeylistIntoAnswers(['fr', 'gecko'], 'B6944BB41BA363CEBFDDE70306BC8355');
      unflattenKeylistIntoAnswers(['fr', 'ie6'], 'BB5E2F85FFCA22D338666E118B3BFD8B');
      unflattenKeylistIntoAnswers(['default', 'opera'], 'C4FA0DDDF72B897501B2826DEA62A18B');
      unflattenKeylistIntoAnswers(['zh', 'gecko1_8'], 'C99BB88F48942A902AE97BE323ED08BA');
      unflattenKeylistIntoAnswers(['pt', 'gecko'], 'CB027A94534A7627A23FA15EF6BEF9F8');
      unflattenKeylistIntoAnswers(['it', 'ie6'], 'CEBCF163375689798BB6DFAAA6F5A29C');
      unflattenKeylistIntoAnswers(['bg', 'ie6'], 'D1D85404F3BEA6868A7FA996993DD3B3');
      unflattenKeylistIntoAnswers(['pt', 'opera'], 'D47AA02B8F417C63791EBD552AEFBE10');
      unflattenKeylistIntoAnswers(['default', 'ie6'], 'D69ED690F29EAE2907E27F09E004F938');
      unflattenKeylistIntoAnswers(['pt', 'ie6'], 'D95448380029BC7C9D358150AB7023E3');
      unflattenKeylistIntoAnswers(['ru', 'ie6'], 'DCAC3AB530BCE9E57C95B37DC41374D1');
      unflattenKeylistIntoAnswers(['zh', 'opera'], 'DDB973E4D77DD132B46F8B6AB70C82C1');
      unflattenKeylistIntoAnswers(['nl', 'opera'], 'DDFAB275099E86F2F09135FE2AE90F6F');
      unflattenKeylistIntoAnswers(['es', 'gecko1_8'], 'DE02738FC30837BCB0EB61A320BB011D');
      unflattenKeylistIntoAnswers(['bg', 'safari'], 'E889695492B3798AFDFB5AAE031B414B');
      unflattenKeylistIntoAnswers(['ru', 'safari'], 'ED869980659E257A62BAD1A9038F8328');
      unflattenKeylistIntoAnswers(['zh', 'safari'], 'EE7445B1DC676E565186BA5A9D7F40A5');
      unflattenKeylistIntoAnswers(['en', 'gecko'], 'F3DDB92B54E36D335540EDD870D12CE2');
      unflattenKeylistIntoAnswers(['pt', 'gecko1_8'], 'F5C8D87B63E038B01662101023FEBB81');
      unflattenKeylistIntoAnswers(['default', 'gecko'], 'F6CFB971A489D1EBD20C2076E65096F1');
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
  $stats && $stats({moduleName:'org.opentaps.gwt.financials.invoices.invoices', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'end'});
  $stats && $stats({moduleName:'org.opentaps.gwt.financials.invoices.invoices', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'begin'});
  if (!__gwt_scriptsLoaded['adapter/ext/ext-base.js']) {
    __gwt_scriptsLoaded['adapter/ext/ext-base.js'] = true;
    document.write('<script language="javascript" src="' + base + 'adapter/ext/ext-base.js"><\/script>');
  }
  if (!__gwt_scriptsLoaded['ext-all.js']) {
    __gwt_scriptsLoaded['ext-all.js'] = true;
    document.write('<script language="javascript" src="' + base + 'ext-all.js"><\/script>');
  }
  $doc_0.write('<script defer="defer">org_opentaps_gwt_financials_invoices_invoices.onInjectionDone(\'org.opentaps.gwt.financials.invoices.invoices\')<\/script>');
}

org_opentaps_gwt_financials_invoices_invoices();
