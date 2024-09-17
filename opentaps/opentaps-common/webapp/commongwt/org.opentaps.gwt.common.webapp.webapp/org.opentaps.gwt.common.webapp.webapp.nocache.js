function org_opentaps_gwt_common_webapp_webapp(){
  var $wnd_0 = window, $doc_0 = document, $stats = $wnd_0.__gwtStatsEvent?function(a){
    return $wnd_0.__gwtStatsEvent(a);
  }
  :null, $sessionId_0 = $wnd_0.__gwtStatsSessionId?$wnd_0.__gwtStatsSessionId:null, scriptsDone, loadDone, bodyDone, base = '', metaProps = {}, values = [], providers = [], answers = [], softPermutationId = 0, onLoadErrorFunc, propertyErrorFunc;
  $stats && $stats({moduleName:'org.opentaps.gwt.common.webapp.webapp', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'begin'});
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
      var iframe = $doc_0.getElementById('org.opentaps.gwt.common.webapp.webapp');
      var frameWnd = iframe.contentWindow;
      if (isHostedMode()) {
        frameWnd.__gwt_getProperty = function(name_0){
          return computePropValue(name_0);
        }
        ;
      }
      org_opentaps_gwt_common_webapp_webapp = null;
      frameWnd.gwtOnLoad(onLoadErrorFunc, 'org.opentaps.gwt.common.webapp.webapp', base, softPermutationId);
      $stats && $stats({moduleName:'org.opentaps.gwt.common.webapp.webapp', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'end'});
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
      if (scriptTags[i].src.indexOf('org.opentaps.gwt.common.webapp.webapp.nocache.js') != -1) {
        thisScript = scriptTags[i];
      }
    }
    if (!thisScript) {
      var markerId = '__gwt_marker_org.opentaps.gwt.common.webapp.webapp';
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
        name_0 = name_0.replace('org.opentaps.gwt.common.webapp.webapp::', '');
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
      iframe.id = 'org.opentaps.gwt.common.webapp.webapp';
      iframe.style.cssText = 'position:absolute;width:0;height:0;border:none';
      iframe.tabIndex = -1;
      $doc_0.body.appendChild(iframe);
      $stats && $stats({moduleName:'org.opentaps.gwt.common.webapp.webapp', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'moduleRequested'});
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
  org_opentaps_gwt_common_webapp_webapp.onScriptLoad = function(){
    if (frameInjected) {
      loadDone = true;
      maybeStartModule();
    }
  }
  ;
  org_opentaps_gwt_common_webapp_webapp.onInjectionDone = function(){
    scriptsDone = true;
    $stats && $stats({moduleName:'org.opentaps.gwt.common.webapp.webapp', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'end'});
    maybeStartModule();
  }
  ;
  processMetas();
  computeScriptBase();
  var strongName;
  var initialHtml;
  if (isHostedMode()) {
    if ($wnd_0.external && ($wnd_0.external.initModule && $wnd_0.external.initModule('org.opentaps.gwt.common.webapp.webapp'))) {
      $wnd_0.location.reload();
      return;
    }
    initialHtml = 'hosted.html?org_opentaps_gwt_common_webapp_webapp';
    strongName = '';
  }
  $stats && $stats({moduleName:'org.opentaps.gwt.common.webapp.webapp', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'selectingPermutation'});
  if (!isHostedMode()) {
    try {
      unflattenKeylistIntoAnswers(['es', 'gecko'], '010A126FA04830F9760CF5B9F0F57466');
      unflattenKeylistIntoAnswers(['bg', 'ie6'], '01B841B2AA41BE0543C1CACEBD74FD09');
      unflattenKeylistIntoAnswers(['es', 'gecko1_8'], '01C25A16D5C0AA8917D7D9F19FEE1C90');
      unflattenKeylistIntoAnswers(['ru', 'safari'], '0370DF0E995A1B5D9B88777ED6C2EBF7');
      unflattenKeylistIntoAnswers(['bg', 'gecko'], '0638C7F02D2B3B0658CF788480B2FB9B');
      unflattenKeylistIntoAnswers(['it', 'ie6'], '07E5C51B3197558DCCAC8FF80ADE7EB5');
      unflattenKeylistIntoAnswers(['zh', 'safari'], '087D39EE7D58AEDE82C453FC22A2813A');
      unflattenKeylistIntoAnswers(['fr', 'gecko'], '14FD19936874FDB200F1A19656F280A1');
      unflattenKeylistIntoAnswers(['es', 'ie6'], '15FBFC5A1EE03AEF5BC5C23018A3C66B');
      unflattenKeylistIntoAnswers(['fr', 'safari'], '23B655F9B1B7F88063DA3B0936E67E7D');
      unflattenKeylistIntoAnswers(['zh', 'ie6'], '2B13ADAA61E8CF8EC85A42C7D44816B0');
      unflattenKeylistIntoAnswers(['default', 'opera'], '2D280C57CAA080748505661F832DEDED');
      unflattenKeylistIntoAnswers(['en', 'gecko'], '2D97316D8BD948931FEB167EB483706B');
      unflattenKeylistIntoAnswers(['fr', 'ie6'], '309CD023ADE25D1C4390323198D5C219');
      unflattenKeylistIntoAnswers(['bg', 'gecko1_8'], '3D5E238797A2A9CF8AE512B4330B68AE');
      unflattenKeylistIntoAnswers(['fr', 'opera'], '3DC9F330F65ADD788D288F74CB7A5A4F');
      unflattenKeylistIntoAnswers(['en', 'opera'], '4644BDA85DC1AF5DF7F717EF72816F2E');
      unflattenKeylistIntoAnswers(['pt', 'gecko1_8'], '485374A583374C058BA95D4E0F559E19');
      unflattenKeylistIntoAnswers(['nl', 'safari'], '486DCAF54974CF8E6B7937C830EF4633');
      unflattenKeylistIntoAnswers(['bg', 'safari'], '4BF73234C8C04FEF4FF45E72516BB0E8');
      unflattenKeylistIntoAnswers(['pt', 'safari'], '57EC3D6528A1F95E6128C248812FD4AE');
      unflattenKeylistIntoAnswers(['zh', 'gecko1_8'], '638D30F9CC3EF90BD3348A60D3A714B7');
      unflattenKeylistIntoAnswers(['en', 'ie6'], '734F63140FBAD00FD8C084433F559043');
      unflattenKeylistIntoAnswers(['en', 'safari'], '75AC4524CEBAAF467D5AC0F4B6785B4D');
      unflattenKeylistIntoAnswers(['ru', 'opera'], '76730151483159229E9B31CC479A827E');
      unflattenKeylistIntoAnswers(['default', 'gecko'], '7B0B50DEE7CD106DD3A4610432F94533');
      unflattenKeylistIntoAnswers(['nl', 'ie6'], '7DBB79D322062B5A066317656777F729');
      unflattenKeylistIntoAnswers(['pt', 'opera'], '7EFF21B2D69D7A3F3FF42E01947F6853');
      unflattenKeylistIntoAnswers(['nl', 'gecko'], '8131E5DB8D0EC23C1301A6DFEC3D65DC');
      unflattenKeylistIntoAnswers(['it', 'opera'], '849D3E36E3FE9CD25112917C04CBB39D');
      unflattenKeylistIntoAnswers(['es', 'safari'], '88922FB88536E504A7D97990AD612570');
      unflattenKeylistIntoAnswers(['en', 'gecko1_8'], '98BD50C938F9E972C77FA6F4F37AF882');
      unflattenKeylistIntoAnswers(['it', 'gecko'], '98D55604BB52982F619077B7A05C721F');
      unflattenKeylistIntoAnswers(['fr', 'gecko1_8'], '9943BBB5278A45613528076023556273');
      unflattenKeylistIntoAnswers(['default', 'gecko1_8'], 'BEBC0D8444E7B959508CBEA69B433C3D');
      unflattenKeylistIntoAnswers(['ru', 'gecko'], 'BF0B240FB33EA9608635EFA241BE2D10');
      unflattenKeylistIntoAnswers(['ru', 'gecko1_8'], 'BFC01DBE57B7F2250BF7C3F77BA62261');
      unflattenKeylistIntoAnswers(['es', 'opera'], 'C0A346F09BF3940CC3DD961501867C5B');
      unflattenKeylistIntoAnswers(['it', 'safari'], 'CE53E384F4274DBF7FA1B60E14962815');
      unflattenKeylistIntoAnswers(['pt', 'ie6'], 'D17697A76FFB5AE8F645DC66446A059E');
      unflattenKeylistIntoAnswers(['nl', 'gecko1_8'], 'D50E8AE06A5D62AD8874C2F47448677B');
      unflattenKeylistIntoAnswers(['it', 'gecko1_8'], 'D5BF542602C6B6422405D2C238E577F8');
      unflattenKeylistIntoAnswers(['nl', 'opera'], 'D935932A96E186064B9B42DDF68F6136');
      unflattenKeylistIntoAnswers(['bg', 'opera'], 'DC9E876F14C519EC7B69C56B58DC03A7');
      unflattenKeylistIntoAnswers(['default', 'ie6'], 'E7403797B075B43133575FB343B1A6D1');
      unflattenKeylistIntoAnswers(['zh', 'gecko'], 'E746296A5B70E62683DDB4D43DD4DEDD');
      unflattenKeylistIntoAnswers(['default', 'safari'], 'E76FDFF7DCC26FBC089FDABB192E0894');
      unflattenKeylistIntoAnswers(['zh', 'opera'], 'F3314B7823B5EB6950A538415F4614B3');
      unflattenKeylistIntoAnswers(['ru', 'ie6'], 'F39665E6FC785855804AE977A41A0EDA');
      unflattenKeylistIntoAnswers(['pt', 'gecko'], 'FBD7E84AB37C1BDEC7AB82B6ED007315');
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
  $stats && $stats({moduleName:'org.opentaps.gwt.common.webapp.webapp', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'end'});
  $stats && $stats({moduleName:'org.opentaps.gwt.common.webapp.webapp', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'begin'});
  if (!__gwt_scriptsLoaded['adapter/ext/ext-base.js']) {
    __gwt_scriptsLoaded['adapter/ext/ext-base.js'] = true;
    document.write('<script language="javascript" src="' + base + 'adapter/ext/ext-base.js"><\/script>');
  }
  if (!__gwt_scriptsLoaded['ext-all.js']) {
    __gwt_scriptsLoaded['ext-all.js'] = true;
    document.write('<script language="javascript" src="' + base + 'ext-all.js"><\/script>');
  }
  $doc_0.write('<script defer="defer">org_opentaps_gwt_common_webapp_webapp.onInjectionDone(\'org.opentaps.gwt.common.webapp.webapp\')<\/script>');
}

org_opentaps_gwt_common_webapp_webapp();
