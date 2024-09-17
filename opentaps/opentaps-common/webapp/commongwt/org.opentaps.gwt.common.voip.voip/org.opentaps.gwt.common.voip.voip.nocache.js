function org_opentaps_gwt_common_voip_voip(){
  var $wnd_0 = window, $doc_0 = document, $stats = $wnd_0.__gwtStatsEvent?function(a){
    return $wnd_0.__gwtStatsEvent(a);
  }
  :null, $sessionId_0 = $wnd_0.__gwtStatsSessionId?$wnd_0.__gwtStatsSessionId:null, scriptsDone, loadDone, bodyDone, base = '', metaProps = {}, values = [], providers = [], answers = [], softPermutationId = 0, onLoadErrorFunc, propertyErrorFunc;
  $stats && $stats({moduleName:'org.opentaps.gwt.common.voip.voip', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'begin'});
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
      var iframe = $doc_0.getElementById('org.opentaps.gwt.common.voip.voip');
      var frameWnd = iframe.contentWindow;
      if (isHostedMode()) {
        frameWnd.__gwt_getProperty = function(name_0){
          return computePropValue(name_0);
        }
        ;
      }
      org_opentaps_gwt_common_voip_voip = null;
      frameWnd.gwtOnLoad(onLoadErrorFunc, 'org.opentaps.gwt.common.voip.voip', base, softPermutationId);
      $stats && $stats({moduleName:'org.opentaps.gwt.common.voip.voip', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'end'});
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
      if (scriptTags[i].src.indexOf('org.opentaps.gwt.common.voip.voip.nocache.js') != -1) {
        thisScript = scriptTags[i];
      }
    }
    if (!thisScript) {
      var markerId = '__gwt_marker_org.opentaps.gwt.common.voip.voip';
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
        name_0 = name_0.replace('org.opentaps.gwt.common.voip.voip::', '');
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
      iframe.id = 'org.opentaps.gwt.common.voip.voip';
      iframe.style.cssText = 'position:absolute;width:0;height:0;border:none';
      iframe.tabIndex = -1;
      $doc_0.body.appendChild(iframe);
      $stats && $stats({moduleName:'org.opentaps.gwt.common.voip.voip', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'moduleRequested'});
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
  org_opentaps_gwt_common_voip_voip.onScriptLoad = function(){
    if (frameInjected) {
      loadDone = true;
      maybeStartModule();
    }
  }
  ;
  org_opentaps_gwt_common_voip_voip.onInjectionDone = function(){
    scriptsDone = true;
    $stats && $stats({moduleName:'org.opentaps.gwt.common.voip.voip', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'end'});
    maybeStartModule();
  }
  ;
  processMetas();
  computeScriptBase();
  var strongName;
  var initialHtml;
  if (isHostedMode()) {
    if ($wnd_0.external && ($wnd_0.external.initModule && $wnd_0.external.initModule('org.opentaps.gwt.common.voip.voip'))) {
      $wnd_0.location.reload();
      return;
    }
    initialHtml = 'hosted.html?org_opentaps_gwt_common_voip_voip';
    strongName = '';
  }
  $stats && $stats({moduleName:'org.opentaps.gwt.common.voip.voip', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'selectingPermutation'});
  if (!isHostedMode()) {
    try {
      unflattenKeylistIntoAnswers(['ru', 'ie6'], '0216D62E471472860C4918D2FA1518CE');
      unflattenKeylistIntoAnswers(['bg', 'safari'], '0288DA7CF073AE29A81DA8911A031137');
      unflattenKeylistIntoAnswers(['bg', 'opera'], '074870A56277629FA59FA48FEFA664F7');
      unflattenKeylistIntoAnswers(['es', 'safari'], '0A246C1643A03FE3F079CFFD5682C95A');
      unflattenKeylistIntoAnswers(['fr', 'safari'], '10E4118E1304F71070FEF5B1276F21DD');
      unflattenKeylistIntoAnswers(['it', 'gecko1_8'], '1357C547054C9793B7CB9B10FF9DD33E');
      unflattenKeylistIntoAnswers(['en', 'gecko1_8'], '1A2133663E06E17366C3084B9DD06427');
      unflattenKeylistIntoAnswers(['zh', 'gecko'], '1E4F1E589891C15C0D3711DFB4094E3F');
      unflattenKeylistIntoAnswers(['nl', 'gecko1_8'], '213BFBBE894131EAA25A113B9013477C');
      unflattenKeylistIntoAnswers(['it', 'opera'], '231F3E5B25028ECE5D8558E3647B020F');
      unflattenKeylistIntoAnswers(['ru', 'gecko1_8'], '2A71A2C752312073E386CC63465B31BE');
      unflattenKeylistIntoAnswers(['es', 'gecko'], '31D2162F3A30DA9D32AFAFB6881D658C');
      unflattenKeylistIntoAnswers(['default', 'safari'], '322561173963CB8672CD3C39DC35EABF');
      unflattenKeylistIntoAnswers(['fr', 'ie6'], '378A86A7E2B07833E0A643754E7BB213');
      unflattenKeylistIntoAnswers(['nl', 'opera'], '3BFC0FE5566A8A74B0CFD2F85E0E6C66');
      unflattenKeylistIntoAnswers(['zh', 'gecko1_8'], '3E561ADF0496C514ABBFFCDC46F728AA');
      unflattenKeylistIntoAnswers(['es', 'ie6'], '3FD4449C07C05F9D48FEB73C2E9FF0A9');
      unflattenKeylistIntoAnswers(['zh', 'ie6'], '428524B94B91A42F797AF0C5FE225EF5');
      unflattenKeylistIntoAnswers(['es', 'opera'], '4BE55BD046193852100E9E681BC3FB8E');
      unflattenKeylistIntoAnswers(['en', 'safari'], '4CDD3F1041297805CAABFA2F694849DE');
      unflattenKeylistIntoAnswers(['en', 'ie6'], '510DBAF5AA6B2C23B0D774883F98E9F6');
      unflattenKeylistIntoAnswers(['bg', 'gecko1_8'], '54D6338FAA464C957780E53BCF3BDB32');
      unflattenKeylistIntoAnswers(['zh', 'opera'], '56D846C256FBCA28DFAC9B7A55C8762D');
      unflattenKeylistIntoAnswers(['it', 'gecko'], '5826C2734453D0880A463D86EDA893F5');
      unflattenKeylistIntoAnswers(['default', 'opera'], '5D6E81301E6A2BC2E781726E05F59C08');
      unflattenKeylistIntoAnswers(['pt', 'opera'], '68A3CFF5EE3BF64646D80D1612CCCBB7');
      unflattenKeylistIntoAnswers(['fr', 'gecko1_8'], '6AD2BFAD1AC98FA8F97F7D88998CDF63');
      unflattenKeylistIntoAnswers(['ru', 'safari'], '6CDD76191B336ADF83BE668110719F86');
      unflattenKeylistIntoAnswers(['pt', 'gecko'], '7BFDBD5669E019657C6F51DA8058780F');
      unflattenKeylistIntoAnswers(['default', 'ie6'], '816028DAF808B57ED76902A1E18BAA3C');
      unflattenKeylistIntoAnswers(['pt', 'safari'], '8B59617FC9F566C0082B42253CB34219');
      unflattenKeylistIntoAnswers(['ru', 'opera'], '9209396FF9AE0F5465525A3AA120D205');
      unflattenKeylistIntoAnswers(['nl', 'gecko'], '993B413A343409DA64151197AF163BFD');
      unflattenKeylistIntoAnswers(['default', 'gecko'], 'B1F7C991ADA00E5372A0F3F49768DD2E');
      unflattenKeylistIntoAnswers(['it', 'ie6'], 'B77C9C4A52CB79504FEF7A1CDCD037F1');
      unflattenKeylistIntoAnswers(['nl', 'safari'], 'B7E917ABBEC593432DAFDE6FC7B808F3');
      unflattenKeylistIntoAnswers(['it', 'safari'], 'BB80EEDB1E9BF6CABBDBFA1D21374F5A');
      unflattenKeylistIntoAnswers(['default', 'gecko1_8'], 'C1CEFDAB9CED3C436117745DF302456F');
      unflattenKeylistIntoAnswers(['nl', 'ie6'], 'C3E98A96F1743F31C86308D493684392');
      unflattenKeylistIntoAnswers(['fr', 'opera'], 'C66D118589DD82685F1BE7BBA2C5FD59');
      unflattenKeylistIntoAnswers(['en', 'gecko'], 'C8027184B24A03EFB69493F2F11F17EA');
      unflattenKeylistIntoAnswers(['ru', 'gecko'], 'D628123B3EAC76F55D28C8C733CB01D8');
      unflattenKeylistIntoAnswers(['bg', 'ie6'], 'DFD3E5ED74CC414B83435D497EE190DA');
      unflattenKeylistIntoAnswers(['zh', 'safari'], 'E48F9755CD0FAA55DB269699BC5AD59B');
      unflattenKeylistIntoAnswers(['pt', 'ie6'], 'E5D2A66B783D3F8C869C301D7153BA5F');
      unflattenKeylistIntoAnswers(['en', 'opera'], 'E6C0A562D7D3238149F78E40ADE40019');
      unflattenKeylistIntoAnswers(['es', 'gecko1_8'], 'EE2A982234337203C14F96CA525924D5');
      unflattenKeylistIntoAnswers(['fr', 'gecko'], 'FA5E5278F0FE699E7CDCF2A2420775D1');
      unflattenKeylistIntoAnswers(['bg', 'gecko'], 'FDCE6094B770AD61D596ED15429BD0D8');
      unflattenKeylistIntoAnswers(['pt', 'gecko1_8'], 'FFE14D6B483B47425C05A648C2C5E0BE');
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
  $stats && $stats({moduleName:'org.opentaps.gwt.common.voip.voip', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'end'});
  $stats && $stats({moduleName:'org.opentaps.gwt.common.voip.voip', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'begin'});
  if (!__gwt_scriptsLoaded['adapter/ext/ext-base.js']) {
    __gwt_scriptsLoaded['adapter/ext/ext-base.js'] = true;
    document.write('<script language="javascript" src="' + base + 'adapter/ext/ext-base.js"><\/script>');
  }
  if (!__gwt_scriptsLoaded['ext-all.js']) {
    __gwt_scriptsLoaded['ext-all.js'] = true;
    document.write('<script language="javascript" src="' + base + 'ext-all.js"><\/script>');
  }
  $doc_0.write('<script defer="defer">org_opentaps_gwt_common_voip_voip.onInjectionDone(\'org.opentaps.gwt.common.voip.voip\')<\/script>');
}

org_opentaps_gwt_common_voip_voip();
