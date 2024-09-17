function org_opentaps_gwt_warehouse_inventory_inventory(){
  var $wnd_0 = window, $doc_0 = document, $stats = $wnd_0.__gwtStatsEvent?function(a){
    return $wnd_0.__gwtStatsEvent(a);
  }
  :null, $sessionId_0 = $wnd_0.__gwtStatsSessionId?$wnd_0.__gwtStatsSessionId:null, scriptsDone, loadDone, bodyDone, base = '', metaProps = {}, values = [], providers = [], answers = [], softPermutationId = 0, onLoadErrorFunc, propertyErrorFunc;
  $stats && $stats({moduleName:'org.opentaps.gwt.warehouse.inventory.inventory', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'begin'});
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
      var iframe = $doc_0.getElementById('org.opentaps.gwt.warehouse.inventory.inventory');
      var frameWnd = iframe.contentWindow;
      if (isHostedMode()) {
        frameWnd.__gwt_getProperty = function(name_0){
          return computePropValue(name_0);
        }
        ;
      }
      org_opentaps_gwt_warehouse_inventory_inventory = null;
      frameWnd.gwtOnLoad(onLoadErrorFunc, 'org.opentaps.gwt.warehouse.inventory.inventory', base, softPermutationId);
      $stats && $stats({moduleName:'org.opentaps.gwt.warehouse.inventory.inventory', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'end'});
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
      if (scriptTags[i].src.indexOf('org.opentaps.gwt.warehouse.inventory.inventory.nocache.js') != -1) {
        thisScript = scriptTags[i];
      }
    }
    if (!thisScript) {
      var markerId = '__gwt_marker_org.opentaps.gwt.warehouse.inventory.inventory';
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
        name_0 = name_0.replace('org.opentaps.gwt.warehouse.inventory.inventory::', '');
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
      iframe.id = 'org.opentaps.gwt.warehouse.inventory.inventory';
      iframe.style.cssText = 'position:absolute;width:0;height:0;border:none';
      iframe.tabIndex = -1;
      $doc_0.body.appendChild(iframe);
      $stats && $stats({moduleName:'org.opentaps.gwt.warehouse.inventory.inventory', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'moduleRequested'});
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
  org_opentaps_gwt_warehouse_inventory_inventory.onScriptLoad = function(){
    if (frameInjected) {
      loadDone = true;
      maybeStartModule();
    }
  }
  ;
  org_opentaps_gwt_warehouse_inventory_inventory.onInjectionDone = function(){
    scriptsDone = true;
    $stats && $stats({moduleName:'org.opentaps.gwt.warehouse.inventory.inventory', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'end'});
    maybeStartModule();
  }
  ;
  processMetas();
  computeScriptBase();
  var strongName;
  var initialHtml;
  if (isHostedMode()) {
    if ($wnd_0.external && ($wnd_0.external.initModule && $wnd_0.external.initModule('org.opentaps.gwt.warehouse.inventory.inventory'))) {
      $wnd_0.location.reload();
      return;
    }
    initialHtml = 'hosted.html?org_opentaps_gwt_warehouse_inventory_inventory';
    strongName = '';
  }
  $stats && $stats({moduleName:'org.opentaps.gwt.warehouse.inventory.inventory', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'selectingPermutation'});
  if (!isHostedMode()) {
    try {
      unflattenKeylistIntoAnswers(['zh', 'ie6'], '031945940DEF7A3710A1EC349A71A284');
      unflattenKeylistIntoAnswers(['es', 'gecko1_8'], '04B20ACFC4EF0C262BC0F1289A840807');
      unflattenKeylistIntoAnswers(['it', 'gecko1_8'], '057AD60DB1F2F68580DD866B8E5CE823');
      unflattenKeylistIntoAnswers(['en', 'opera'], '06E88C72E49AC457799F4DEFDF5F727B');
      unflattenKeylistIntoAnswers(['es', 'opera'], '094F69469C0EBEB3F5FE4CC2CF62B479');
      unflattenKeylistIntoAnswers(['bg', 'safari'], '0F9FD1150CE658D644A0F70D09F45C2D');
      unflattenKeylistIntoAnswers(['es', 'safari'], '12138204720F67F262A63537B3E52943');
      unflattenKeylistIntoAnswers(['ru', 'safari'], '1FB3CE55F10172C1447259FFBF476630');
      unflattenKeylistIntoAnswers(['es', 'ie6'], '21AC09805FAC8EE53EAE0970D90B3986');
      unflattenKeylistIntoAnswers(['fr', 'gecko1_8'], '21D0E61103C598C0D538EB62A893877D');
      unflattenKeylistIntoAnswers(['zh', 'gecko'], '3050EB206DC73943E7FD9113DADE44D8');
      unflattenKeylistIntoAnswers(['fr', 'gecko'], '411A2E55FD6A708E669F686C488B0EEF');
      unflattenKeylistIntoAnswers(['zh', 'safari'], '449002CF6D38BF04814BBB1ECAA14AE5');
      unflattenKeylistIntoAnswers(['pt', 'ie6'], '50F64895C55798DE2F85147646D5F47B');
      unflattenKeylistIntoAnswers(['ru', 'opera'], '52CF2D11DA4FFCDC7F43A183E491AA88');
      unflattenKeylistIntoAnswers(['zh', 'gecko1_8'], '53188AEE2F239DC6E50D45DEE07413EA');
      unflattenKeylistIntoAnswers(['default', 'gecko'], '539F57499DC3A24A7590D34A5B207ECC');
      unflattenKeylistIntoAnswers(['en', 'gecko1_8'], '548F6B25A40FC745A279714FB16A7E88');
      unflattenKeylistIntoAnswers(['default', 'ie6'], '5A24E52C43CD8DFD8EC39BFC1A9588B7');
      unflattenKeylistIntoAnswers(['it', 'safari'], '5F19DFC0E93E85D7A91174F54E69DABB');
      unflattenKeylistIntoAnswers(['ru', 'gecko1_8'], '60035121A2B6E1C045E53BCB454AEB22');
      unflattenKeylistIntoAnswers(['fr', 'opera'], '68BEEF227C36A844891422FB61FD8805');
      unflattenKeylistIntoAnswers(['pt', 'gecko'], '693A6D1974654FB10A122224BE23B694');
      unflattenKeylistIntoAnswers(['es', 'gecko'], '7225C84F9BE1D0AE53D89080055E2220');
      unflattenKeylistIntoAnswers(['en', 'gecko'], '75F4D4EB620EF7D5842CE07C25949A2F');
      unflattenKeylistIntoAnswers(['bg', 'gecko1_8'], '76B8103D08A494D3D6772CDDBBDA8C29');
      unflattenKeylistIntoAnswers(['pt', 'safari'], '8227B202D8668D1F269375F939F1CCD4');
      unflattenKeylistIntoAnswers(['nl', 'opera'], '828D6FF4BB25FC1948E20781E4CBCF68');
      unflattenKeylistIntoAnswers(['nl', 'gecko1_8'], '82FEA586A08F11ACA63121E78156BA53');
      unflattenKeylistIntoAnswers(['default', 'opera'], '89CF1F7E91EDF2322ED6478D3D120312');
      unflattenKeylistIntoAnswers(['pt', 'opera'], '8E812ECFC176DAE69E342A681B56A44C');
      unflattenKeylistIntoAnswers(['it', 'ie6'], '93D4A097213F598080EFB6EE29B8C763');
      unflattenKeylistIntoAnswers(['nl', 'ie6'], '987CDAA0289AE81918C42537C548A75E');
      unflattenKeylistIntoAnswers(['fr', 'ie6'], '9F687BC8D8240F33967C0AB83AF3E836');
      unflattenKeylistIntoAnswers(['en', 'safari'], 'A99C23FD7C3E3F4BFA8B44C0E28A0C56');
      unflattenKeylistIntoAnswers(['ru', 'ie6'], 'AA81FD462791FF20AB58CAB046566A06');
      unflattenKeylistIntoAnswers(['nl', 'gecko'], 'AB61940690877A9A06520587368F5633');
      unflattenKeylistIntoAnswers(['nl', 'safari'], 'B12829D58A3BC37AB9D7CDE22FD1F0BD');
      unflattenKeylistIntoAnswers(['en', 'ie6'], 'B774004FB934911C04EA229747F60945');
      unflattenKeylistIntoAnswers(['ru', 'gecko'], 'B7D1970D19BC0CFB55C6ACA111256934');
      unflattenKeylistIntoAnswers(['pt', 'gecko1_8'], 'BE3757A45406605D48DFBE41A2C5A5D8');
      unflattenKeylistIntoAnswers(['bg', 'gecko'], 'C6482A23D6D62668FD691A88D035F6FB');
      unflattenKeylistIntoAnswers(['it', 'opera'], 'D3AB767BF6A00C513BF9834C9E75578B');
      unflattenKeylistIntoAnswers(['zh', 'opera'], 'E0DBE91E595D9F2CF27FD28F65C76F81');
      unflattenKeylistIntoAnswers(['it', 'gecko'], 'E4E4CC4C2A321C86338622F59C57931B');
      unflattenKeylistIntoAnswers(['default', 'gecko1_8'], 'E849F0C019191E1017A52F5399272C1C');
      unflattenKeylistIntoAnswers(['fr', 'safari'], 'EF6970C5F88A01AC520A9C60B3B01528');
      unflattenKeylistIntoAnswers(['bg', 'ie6'], 'F004D74325AF6BE95A5108C7701AA38E');
      unflattenKeylistIntoAnswers(['default', 'safari'], 'F28B2A46FFDE56EA561898B4BF3AD08A');
      unflattenKeylistIntoAnswers(['bg', 'opera'], 'FF2C7660A72720281C3819BC65EA148F');
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
  $stats && $stats({moduleName:'org.opentaps.gwt.warehouse.inventory.inventory', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'end'});
  $stats && $stats({moduleName:'org.opentaps.gwt.warehouse.inventory.inventory', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'begin'});
  if (!__gwt_scriptsLoaded['adapter/ext/ext-base.js']) {
    __gwt_scriptsLoaded['adapter/ext/ext-base.js'] = true;
    document.write('<script language="javascript" src="' + base + 'adapter/ext/ext-base.js"><\/script>');
  }
  if (!__gwt_scriptsLoaded['ext-all.js']) {
    __gwt_scriptsLoaded['ext-all.js'] = true;
    document.write('<script language="javascript" src="' + base + 'ext-all.js"><\/script>');
  }
  $doc_0.write('<script defer="defer">org_opentaps_gwt_warehouse_inventory_inventory.onInjectionDone(\'org.opentaps.gwt.warehouse.inventory.inventory\')<\/script>');
}

org_opentaps_gwt_warehouse_inventory_inventory();
