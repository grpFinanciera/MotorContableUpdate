function org_opentaps_gwt_crmsfa_crmsfa(){
  var $wnd_0 = window, $doc_0 = document, $stats = $wnd_0.__gwtStatsEvent?function(a){
    return $wnd_0.__gwtStatsEvent(a);
  }
  :null, $sessionId_0 = $wnd_0.__gwtStatsSessionId?$wnd_0.__gwtStatsSessionId:null, scriptsDone, loadDone, bodyDone, base = '', metaProps = {}, values = [], providers = [], answers = [], softPermutationId = 0, onLoadErrorFunc, propertyErrorFunc;
  $stats && $stats({moduleName:'org.opentaps.gwt.crmsfa.crmsfa', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'begin'});
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
      var iframe = $doc_0.getElementById('org.opentaps.gwt.crmsfa.crmsfa');
      var frameWnd = iframe.contentWindow;
      if (isHostedMode()) {
        frameWnd.__gwt_getProperty = function(name_0){
          return computePropValue(name_0);
        }
        ;
      }
      org_opentaps_gwt_crmsfa_crmsfa = null;
      frameWnd.gwtOnLoad(onLoadErrorFunc, 'org.opentaps.gwt.crmsfa.crmsfa', base, softPermutationId);
      $stats && $stats({moduleName:'org.opentaps.gwt.crmsfa.crmsfa', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'end'});
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
      if (scriptTags[i].src.indexOf('org.opentaps.gwt.crmsfa.crmsfa.nocache.js') != -1) {
        thisScript = scriptTags[i];
      }
    }
    if (!thisScript) {
      var markerId = '__gwt_marker_org.opentaps.gwt.crmsfa.crmsfa';
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
        name_0 = name_0.replace('org.opentaps.gwt.crmsfa.crmsfa::', '');
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
      iframe.id = 'org.opentaps.gwt.crmsfa.crmsfa';
      iframe.style.cssText = 'position:absolute;width:0;height:0;border:none';
      iframe.tabIndex = -1;
      $doc_0.body.appendChild(iframe);
      $stats && $stats({moduleName:'org.opentaps.gwt.crmsfa.crmsfa', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'moduleRequested'});
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
  org_opentaps_gwt_crmsfa_crmsfa.onScriptLoad = function(){
    if (frameInjected) {
      loadDone = true;
      maybeStartModule();
    }
  }
  ;
  org_opentaps_gwt_crmsfa_crmsfa.onInjectionDone = function(){
    scriptsDone = true;
    $stats && $stats({moduleName:'org.opentaps.gwt.crmsfa.crmsfa', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'end'});
    maybeStartModule();
  }
  ;
  processMetas();
  computeScriptBase();
  var strongName;
  var initialHtml;
  if (isHostedMode()) {
    if ($wnd_0.external && ($wnd_0.external.initModule && $wnd_0.external.initModule('org.opentaps.gwt.crmsfa.crmsfa'))) {
      $wnd_0.location.reload();
      return;
    }
    initialHtml = 'hosted.html?org_opentaps_gwt_crmsfa_crmsfa';
    strongName = '';
  }
  $stats && $stats({moduleName:'org.opentaps.gwt.crmsfa.crmsfa', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'selectingPermutation'});
  if (!isHostedMode()) {
    try {
      unflattenKeylistIntoAnswers(['fr', 'gecko'], '03CDC61C4C2082C0A65E89F084AE4154');
      unflattenKeylistIntoAnswers(['pt', 'safari'], '09E870525485FA31499A5FB54BC0965E');
      unflattenKeylistIntoAnswers(['fr', 'gecko1_8'], '0AAD5B58A6B61C1DB61570ED70AD196D');
      unflattenKeylistIntoAnswers(['es', 'gecko1_8'], '150D3275957BEED1861747CE4D0346B2');
      unflattenKeylistIntoAnswers(['fr', 'safari'], '19674ACF21238656430D6AF34ED0A1F7');
      unflattenKeylistIntoAnswers(['en', 'gecko1_8'], '1E16184D6622E97FDFA270B028674BCE');
      unflattenKeylistIntoAnswers(['en', 'safari'], '299DF1ACC2B8A1B6C60B12E6DAA0D120');
      unflattenKeylistIntoAnswers(['ru', 'opera'], '440C1DEA1658397D13F53A7D8E2D3BBA');
      unflattenKeylistIntoAnswers(['bg', 'gecko1_8'], '4B650A985C232C61D8771C2A9163A0C4');
      unflattenKeylistIntoAnswers(['en', 'gecko'], '4FE5C2560A1C960C25EB782BF41D0F7E');
      unflattenKeylistIntoAnswers(['it', 'gecko'], '51C8ED0DD9D7639B85FB87ED8EC1C9BD');
      unflattenKeylistIntoAnswers(['bg', 'opera'], '5F10AAB50005C82A9FCFC63EF9B482DC');
      unflattenKeylistIntoAnswers(['zh', 'ie6'], '5FA4F05311FDD2920916B8B909776B46');
      unflattenKeylistIntoAnswers(['default', 'opera'], '609F29E6FFB4C3B6402115FED1AB1437');
      unflattenKeylistIntoAnswers(['default', 'ie6'], '6516D0E752B4DA1662AE5D02F02CAEC8');
      unflattenKeylistIntoAnswers(['default', 'safari'], '70230A34F4A58D54D64806AEE0741755');
      unflattenKeylistIntoAnswers(['fr', 'ie6'], '70627C94C8A76CDF3DAF4E3E9EE3A947');
      unflattenKeylistIntoAnswers(['zh', 'gecko1_8'], '727B621668357FC37F41ED210B94908F');
      unflattenKeylistIntoAnswers(['en', 'opera'], '8025583B11C75F291E3212D3B6DC01DF');
      unflattenKeylistIntoAnswers(['fr', 'opera'], '86B8C85CAEC6DB11D19A6B09C838A19F');
      unflattenKeylistIntoAnswers(['nl', 'gecko1_8'], '86BEDBF649E9DA6FD97D6984A82A1802');
      unflattenKeylistIntoAnswers(['bg', 'ie6'], '88ECFB19F10EB006530384CE3B7B3274');
      unflattenKeylistIntoAnswers(['nl', 'gecko'], '8933F868EE101631D10E5CA4B8F8F46E');
      unflattenKeylistIntoAnswers(['bg', 'gecko'], '8A4F3EBA48BE77792C41FA64C04F713C');
      unflattenKeylistIntoAnswers(['pt', 'opera'], '951A758C0A2B28E168785D1551BD33FD');
      unflattenKeylistIntoAnswers(['pt', 'gecko1_8'], '9784CEEBD15515DFDDE70C5E40A8BB28');
      unflattenKeylistIntoAnswers(['nl', 'safari'], '981CF805FC65040F8D5382B435530636');
      unflattenKeylistIntoAnswers(['default', 'gecko'], '99372AB5EEA6F9DD0DA2F268EE1B77D8');
      unflattenKeylistIntoAnswers(['ru', 'gecko'], 'AC53928D279539A0E77835461CDAB9A2');
      unflattenKeylistIntoAnswers(['it', 'ie6'], 'B0A377A571C509C013B159498F85870D');
      unflattenKeylistIntoAnswers(['es', 'ie6'], 'B0EFD00CE6FB4E73A65819DAEC722B0F');
      unflattenKeylistIntoAnswers(['nl', 'opera'], 'B4A512CA16D009AAB11A2BDBEBC58C46');
      unflattenKeylistIntoAnswers(['en', 'ie6'], 'B4B92180FBE3DFC6E7C72090A7C2ACDE');
      unflattenKeylistIntoAnswers(['ru', 'ie6'], 'BDE7662720828718ECF8C0DD7E686D2A');
      unflattenKeylistIntoAnswers(['pt', 'gecko'], 'C084FD1ED69BB2A9B3315B2EB3E9FE8B');
      unflattenKeylistIntoAnswers(['es', 'gecko'], 'C09D126F947167BFC9BB1BF3E8871B49');
      unflattenKeylistIntoAnswers(['pt', 'ie6'], 'C2833751051366D066964401F7DC02D6');
      unflattenKeylistIntoAnswers(['nl', 'ie6'], 'C3C81CEE784954950AEE5F0052D24EF8');
      unflattenKeylistIntoAnswers(['default', 'gecko1_8'], 'C5BD6F6ED827B09FF3D6F07F7F259107');
      unflattenKeylistIntoAnswers(['zh', 'opera'], 'C687146A1B59188C70FD5F9EC8E699CA');
      unflattenKeylistIntoAnswers(['es', 'opera'], 'CA5D77815E72A368C7A02610EA31EDF3');
      unflattenKeylistIntoAnswers(['zh', 'safari'], 'CA9A05424FEF72B4B482D0356248030F');
      unflattenKeylistIntoAnswers(['it', 'opera'], 'CDB8E7F13C3457234EDDCCDCB67C1F4A');
      unflattenKeylistIntoAnswers(['zh', 'gecko'], 'D21A9BB66D1E84CF3BB102772AE653BB');
      unflattenKeylistIntoAnswers(['it', 'safari'], 'E09B9B8263BDD5B22BA708D311885EA0');
      unflattenKeylistIntoAnswers(['es', 'safari'], 'E2B4BAF465516B3D1EE15F762E88A46F');
      unflattenKeylistIntoAnswers(['ru', 'gecko1_8'], 'E5A5736B6B5D792FF5E7B53DF69FA1D9');
      unflattenKeylistIntoAnswers(['ru', 'safari'], 'EE4FDBE1006BFBB8C02DD66E050A9E96');
      unflattenKeylistIntoAnswers(['bg', 'safari'], 'EEF19A6705612DBDBAE55ED6619DCC9C');
      unflattenKeylistIntoAnswers(['it', 'gecko1_8'], 'FD83A7FB838831BDBBDC895D7E9694CE');
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
  $stats && $stats({moduleName:'org.opentaps.gwt.crmsfa.crmsfa', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'end'});
  $stats && $stats({moduleName:'org.opentaps.gwt.crmsfa.crmsfa', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'begin'});
  if (!__gwt_scriptsLoaded['adapter/ext/ext-base.js']) {
    __gwt_scriptsLoaded['adapter/ext/ext-base.js'] = true;
    document.write('<script language="javascript" src="' + base + 'adapter/ext/ext-base.js"><\/script>');
  }
  if (!__gwt_scriptsLoaded['ext-all.js']) {
    __gwt_scriptsLoaded['ext-all.js'] = true;
    document.write('<script language="javascript" src="' + base + 'ext-all.js"><\/script>');
  }
  $doc_0.write('<script defer="defer">org_opentaps_gwt_crmsfa_crmsfa.onInjectionDone(\'org.opentaps.gwt.crmsfa.crmsfa\')<\/script>');
}

org_opentaps_gwt_crmsfa_crmsfa();
