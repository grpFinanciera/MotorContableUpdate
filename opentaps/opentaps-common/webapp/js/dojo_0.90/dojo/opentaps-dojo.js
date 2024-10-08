/*
	Copyright (c) 2004-2007, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
 */

/*
 This is a compiled version of Dojo, built for deployment and not for
 development. To get an editable version, please visit:

 http://dojotoolkit.org

 for documentation and information on getting the source.
 */

if (!dojo._hasResource["dojo.fx"]) {
	dojo._hasResource["dojo.fx"] = true;
	dojo.provide("dojo.fx");
	dojo.provide("dojo.fx.Toggler");
	dojo.fx.chain = function(_1) {
		var _2 = _1.shift();
		var _3 = _2;
		dojo.forEach(_1, function(_4) {
			dojo.connect(_3, "onEnd", _4, "play");
			_3 = _4;
		});
		return _2;
	};
	dojo.fx.combine = function(_5) {
		var _6 = _5.shift();
		dojo.forEach(_5, function(_7) {
			dojo.forEach([ "play", "pause", "stop" ], function(_8) {
				if (_7[_8]) {
					dojo.connect(_6, _8, _7, _8);
				}
			}, this);
		});
		return _6;
	};
	dojo.declare("dojo.fx.Toggler", null, {
		constructor : function(_9) {
			var _t = this;
			dojo.mixin(_t, _9);
			_t.node = _9.node;
			_t._showArgs = dojo.mixin({}, _9);
			_t._showArgs.node = _t.node;
			_t._showArgs.duration = _t.showDuration;
			_t.showAnim = _t.showFunc(_t._showArgs);
			_t._hideArgs = dojo.mixin({}, _9);
			_t._hideArgs.node = _t.node;
			_t._hideArgs.duration = _t.hideDuration;
			_t.hideAnim = _t.hideFunc(_t._hideArgs);
			dojo.connect(_t.showAnim, "beforeBegin", dojo.hitch(_t.hideAnim,
					"stop", true));
			dojo.connect(_t.hideAnim, "beforeBegin", dojo.hitch(_t.showAnim,
					"stop", true));
		},
		node : null,
		showFunc : dojo.fadeIn,
		hideFunc : dojo.fadeOut,
		showDuration : 200,
		hideDuration : 200,
		_showArgs : null,
		_showAnim : null,
		_hideArgs : null,
		_hideAnim : null,
		_isShowing : false,
		_isHiding : false,
		show : function(_b) {
			_b = _b || 0;
			return this.showAnim.play(_b);
		},
		hide : function(_c) {
			_c = _c || 0;
			return this.hideAnim.play(_c);
		}
	});
	dojo.fx.wipeIn = function(_d) {
		_d.node = dojo.byId(_d.node);
		var _e = _d.node, s = _e.style;
		var _10 = dojo.animateProperty(dojo.mixin({
			properties : {
				height : {
					start : function() {
						s.overflow = "hidden";
						if (s.visibility == "hidden" || s.display == "none") {
							s.height = "1px";
							s.display = "";
							s.visibility = "";
							return 1;
						} else {
							var _11 = dojo.style(_e, "height");
							return Math.max(_11, 1);
						}
					},
					end : function() {
						return _e.scrollHeight;
					}
				}
			}
		}, _d));
		dojo.connect(_10, "onEnd", _10, function() {
			s.height = "auto";
		});
		return _10;
	};
	dojo.fx.wipeOut = function(_12) {
		var _13 = (_12.node = dojo.byId(_12.node));
		var _14 = dojo.animateProperty(dojo.mixin({
			properties : {
				height : {
					end : 1
				}
			}
		}, _12));
		dojo.connect(_14, "beforeBegin", _14, function() {
			var s = _13.style;
			s.overflow = "hidden";
			s.display = "";
		});
		dojo.connect(_14, "onEnd", _14, function() {
			var s = this.node.style;
			s.height = "auto";
			s.display = "none";
		});
		return _14;
	};
	dojo.fx.slideTo = function(_17) {
		var _18 = _17.node = dojo.byId(_17.node);
		var _19 = dojo.getComputedStyle;
		var top = null;
		var _1b = null;
		var _1c = (function() {
			var _1d = _18;
			return function() {
				var pos = _19(_1d).position;
				top = (pos == "absolute" ? _18.offsetTop
						: parseInt(_19(_18).top) || 0);
				_1b = (pos == "absolute" ? _18.offsetLeft
						: parseInt(_19(_18).left) || 0);
				if (pos != "absolute" && pos != "relative") {
					var ret = dojo.coords(_1d, true);
					top = ret.y;
					_1b = ret.x;
					_1d.style.position = "absolute";
					_1d.style.top = top + "px";
					_1d.style.left = _1b + "px";
				}
			};
		})();
		_1c();
		var _20 = dojo.animateProperty(dojo.mixin({
			properties : {
				top : {
					start : top,
					end : _17.top || 0
				},
				left : {
					start : _1b,
					end : _17.left || 0
				}
			}
		}, _17));
		dojo.connect(_20, "beforeBegin", _20, _1c);
		return _20;
	};
}
if (!dojo._hasResource["dojo.dnd.common"]) {
	dojo._hasResource["dojo.dnd.common"] = true;
	dojo.provide("dojo.dnd.common");
	dojo.dnd._copyKey = navigator.appVersion.indexOf("Macintosh") < 0 ? "ctrlKey"
			: "metaKey";
	dojo.dnd.getCopyKeyState = function(e) {
		return e[dojo.dnd._copyKey];
	};
	dojo.dnd._uniqueId = 0;
	dojo.dnd.getUniqueId = function() {
		var id;
		do {
			id = "dojoUnique" + (++dojo.dnd._uniqueId);
		} while (dojo.byId(id));
		return id;
	};
	dojo.dnd._empty = {};
	dojo.dnd.isFormElement = function(e) {
		var t = e.target;
		if (t.nodeType == 3) {
			t = t.parentNode;
		}
		return " button textarea input select option ".indexOf(" "
				+ t.tagName.toLowerCase() + " ") >= 0;
	};
}
if (!dojo._hasResource["dojo.date.stamp"]) {
	dojo._hasResource["dojo.date.stamp"] = true;
	dojo.provide("dojo.date.stamp");
	dojo.date.stamp.fromISOString = function(_25, _26) {
		if (!dojo.date.stamp._isoRegExp) {
			dojo.date.stamp._isoRegExp = /^(?:(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?)?(?:T(\d{2}):(\d{2})(?::(\d{2})(.\d+)?)?((?:[+-](\d{2}):(\d{2}))|Z)?)?$/;
		}
		var _27 = dojo.date.stamp._isoRegExp.exec(_25);
		var _28 = null;
		if (_27) {
			_27.shift();
			_27[1] && _27[1]--;
			_27[6] && (_27[6] *= 1000);
			if (_26) {
				_26 = new Date(_26);
				dojo.map(
						[ "FullYear", "Month", "Date", "Hours", "Minutes",
								"Seconds", "Milliseconds" ], function(_29) {
							return _26["get" + _29]();
						}).forEach(function(_2a, _2b) {
					if (_27[_2b] === undefined) {
						_27[_2b] = _2a;
					}
				});
			}
			_28 = new Date(_27[0] || 1970, _27[1] || 0, _27[2] || 0,
					_27[3] || 0, _27[4] || 0, _27[5] || 0, _27[6] || 0);
			var _2c = 0;
			var _2d = _27[7] && _27[7].charAt(0);
			if (_2d != "Z") {
				_2c = ((_27[8] || 0) * 60) + (Number(_27[9]) || 0);
				if (_2d != "-") {
					_2c *= -1;
				}
			}
			if (_2d) {
				_2c -= _28.getTimezoneOffset();
			}
			if (_2c) {
				_28.setTime(_28.getTime() + _2c * 60000);
			}
		}
		return _28;
	};
	dojo.date.stamp.toISOString = function(_2e, _2f) {
		var _ = function(n) {
			return (n < 10) ? "0" + n : n;
		};
		_2f = _2f || {};
		var _32 = [];
		var _33 = _2f.zulu ? "getUTC" : "get";
		var _34 = "";
		if (_2f.selector != "time") {
			_34 = [ _2e[_33 + "FullYear"](), _(_2e[_33 + "Month"]() + 1),
					_(_2e[_33 + "Date"]()) ].join("-");
		}
		_32.push(_34);
		if (_2f.selector != "date") {
			var _35 = [ _(_2e[_33 + "Hours"]()), _(_2e[_33 + "Minutes"]()),
					_(_2e[_33 + "Seconds"]()) ].join(":");
			var _36 = _2e[_33 + "Milliseconds"]();
			if (_2f.milliseconds) {
				_35 += "." + (_36 < 100 ? "0" : "") + _(_36);
			}
			if (_2f.zulu) {
				_35 += "Z";
			} else {
				var _37 = _2e.getTimezoneOffset();
				var _38 = Math.abs(_37);
				_35 += (_37 > 0 ? "-" : "+") + _(Math.floor(_38 / 60)) + ":"
						+ _(_38 % 60);
			}
			_32.push(_35);
		}
		return _32.join("T");
	};
}
if (!dojo._hasResource["dojo.parser"]) {
	dojo._hasResource["dojo.parser"] = true;
	dojo.provide("dojo.parser");
	dojo.parser = new function() {
		var d = dojo;
		function val2type(_3a) {
			if (d.isString(_3a)) {
				return "string";
			}
			if (typeof _3a == "number") {
				return "number";
			}
			if (typeof _3a == "boolean") {
				return "boolean";
			}
			if (d.isFunction(_3a)) {
				return "function";
			}
			if (d.isArray(_3a)) {
				return "array";
			}
			if (_3a instanceof Date) {
				return "date";
			}
			if (_3a instanceof d._Url) {
				return "url";
			}
			return "object";
		}
		;
		function str2obj(_3b, _3c) {
			switch (_3c) {
			case "string":
				return _3b;
			case "number":
				return _3b.length ? Number(_3b) : NaN;
			case "boolean":
				return typeof _3b == "boolean" ? _3b
						: !(_3b.toLowerCase() == "false");
			case "function":
				if (d.isFunction(_3b)) {
					_3b = _3b.toString();
					_3b = d.trim(_3b.substring(_3b.indexOf("{") + 1,
							_3b.length - 1));
				}
				try {
					if (_3b.search(/[^\w\.]+/i) != -1) {
						_3b = d.parser._nameAnonFunc(new Function(_3b), this);
					}
					return d.getObject(_3b, false);
				} catch (e) {
					return new Function();
				}
			case "array":
				return _3b.split(/\s*,\s*/);
			case "date":
				switch (_3b) {
				case "":
					return new Date("");
				case "now":
					return new Date();
				default:
					return d.date.stamp.fromISOString(_3b);
				}
			case "url":
				return d.baseUrl + _3b;
			default:
				return d.fromJson(_3b);
			}
		}
		;
		var _3d = {};
		function getClassInfo(_3e) {
			if (!_3d[_3e]) {
				var cls = d.getObject(_3e);
				if (!d.isFunction(cls)) {
					throw new Error(
							"Could not load class '"
									+ _3e
									+ "'. Did you spell the name correctly and use a full path, like 'dijit.form.Button'?");
				}
				var _40 = cls.prototype;
				var _41 = {};
				for ( var _42 in _40) {
					if (_42.charAt(0) == "_") {
						continue;
					}
					var _43 = _40[_42];
					_41[_42] = val2type(_43);
				}
				_3d[_3e] = {
					cls : cls,
					params : _41
				};
			}
			return _3d[_3e];
		}
		;
		this._functionFromScript = function(_44) {
			var _45 = "";
			var _46 = "";
			var _47 = _44.getAttribute("args");
			if (_47) {
				d.forEach(_47.split(/\s*,\s*/), function(_48, idx) {
					_45 += "var " + _48 + " = arguments[" + idx + "]; ";
				});
			}
			var _4a = _44.getAttribute("with");
			if (_4a && _4a.length) {
				d.forEach(_4a.split(/\s*,\s*/), function(_4b) {
					_45 += "with(" + _4b + "){";
					_46 += "}";
				});
			}
			return new Function(_45 + _44.innerHTML + _46);
		};
		this._wireUpMethod = function(_4c, _4d) {
			var nf = this._functionFromScript(_4d);
			var _4f = _4d.getAttribute("event");
			if (_4f) {
				var _50 = _4d.getAttribute("type");
				if (_50 && (_50 == "dojo/connect")) {
					d.connect(_4c, _4f, null, nf);
				} else {
					_4c[_4f] = nf;
				}
			} else {
				nf.call(_4c);
			}
		};
		this.instantiate = function(_51) {
			var _52 = [];
			d.forEach(_51, function(_53) {
				if (!_53) {
					return;
				}
				var _54 = _53.getAttribute("dojoType");
				if ((!_54) || (!_54.length)) {
					return;
				}
				var _55 = getClassInfo(_54);
				var _56 = _55.cls;
				var ps = _56._noScript || _56.prototype._noScript;
				var _58 = {};
				var _59 = _53.attributes;
				for ( var _5a in _55.params) {
					var _5b = _59.getNamedItem(_5a);
					if (!_5b
							|| (!_5b.specified && (!dojo.isIE || _5a
									.toLowerCase() != "value"))) {
						continue;
					}
					var _5c = _55.params[_5a];
					_58[_5a] = str2obj(_5b.value, _5c);
				}
				if (!ps) {
					var _5d = d.query(
							"> script[type='dojo/method'][event='preamble']",
							_53).orphan();
					if (_5d.length) {
						_58.preamble = d.parser._functionFromScript(_5d[0]);
					}
					var _5e = d.query("> script[type^='dojo/']", _53).orphan();
				}
				var _5f = _56["markupFactory"];
				if (!_5f && _56["prototype"]) {
					_5f = _56.prototype["markupFactory"];
				}
				var _60 = _5f ? _5f(_58, _53, _56) : new _56(_58, _53);
				_52.push(_60);
				var _61 = _53.getAttribute("jsId");
				if (_61) {
					d.setObject(_61, _60);
				}
				if (!ps) {
					_5e.forEach(function(_62) {
						d.parser._wireUpMethod(_60, _62);
					});
				}
			});
			d.forEach(_52, function(_63) {
				if (_63 && (_63.startup)
						&& ((!_63.getParent) || (!_63.getParent()))) {
					_63.startup();
				}
			});
			return _52;
		};
		this.parse = function(_64) {
			var _65 = d.query("[dojoType]", _64);
			var _66 = this.instantiate(_65);
			return _66;
		};
	}();
	(function() {
		var _67 = function() {
			if (djConfig["parseOnLoad"] == true) {
				dojo.parser.parse();
			}
		};
		if (dojo.exists("dijit.wai.onload")
				&& (dijit.wai.onload === dojo._loaders[0])) {
			dojo._loaders.splice(1, 0, _67);
		} else {
			dojo._loaders.unshift(_67);
		}
	})();
	dojo.parser._anonCtr = 0;
	dojo.parser._anon = {};
	dojo.parser._nameAnonFunc = function(_68, _69) {
		var jpn = "$joinpoint";
		var nso = (_69 || dojo.parser._anon);
		if (dojo.isIE) {
			var cn = _68["__dojoNameCache"];
			if (cn && nso[cn] === _68) {
				return _68["__dojoNameCache"];
			}
		}
		var ret = "__" + dojo.parser._anonCtr++;
		while (typeof nso[ret] != "undefined") {
			ret = "__" + dojo.parser._anonCtr++;
		}
		nso[ret] = _68;
		return ret;
	};
}
if (!dojo._hasResource["dojo.dnd.container"]) {
	dojo._hasResource["dojo.dnd.container"] = true;
	dojo.provide("dojo.dnd.container");
	dojo
			.declare(
					"dojo.dnd.Container",
					null,
					{
						constructor : function(_6e, _6f) {
							this.node = dojo.byId(_6e);
							this.creator = _6f && _6f.creator || null;
							this.defaultCreator = dojo.dnd
									._defaultCreator(this.node);
							this.map = {};
							this.current = null;
							this.containerState = "";
							dojo.addClass(this.node, "dojoDndContainer");
							if (!(_6f && _6f._skipStartup)) {
								this.startup();
							}
							this.events = [
									dojo.connect(this.node, "onmouseover",
											this, "onMouseOver"),
									dojo.connect(this.node, "onmouseout", this,
											"onMouseOut"),
									dojo.connect(this.node, "ondragstart",
											dojo, "stopEvent"),
									dojo.connect(this.node, "onselectstart",
											dojo, "stopEvent") ];
						},
						creator : function() {
						},
						getItem : function(key) {
							return this.map[key];
						},
						setItem : function(key, _72) {
							this.map[key] = _72;
						},
						delItem : function(key) {
							delete this.map[key];
						},
						forInItems : function(f, o) {
							o = o || dojo.global;
							var m = this.map, e = dojo.dnd._empty;
							for ( var i in this.map) {
								if (i in e) {
									continue;
								}
								f.call(o, m[i], i, m);
							}
						},
						clearItems : function() {
							this.map = {};
						},
						getAllNodes : function() {
							return dojo.query("> .dojoDndItem", this.parent);
						},
						insertNodes : function(_79, _7a, _7b) {
							if (!this.parent.firstChild) {
								_7b = null;
							} else {
								if (_7a) {
									if (!_7b) {
										_7b = this.parent.firstChild;
									}
								} else {
									if (_7b) {
										_7b = _7b.nextSibling;
									}
								}
							}
							if (_7b) {
								for ( var i = 0; i < _79.length; ++i) {
									var t = this._normalizedCreator(_79[i]);
									this.setItem(t.node.id, {
										data : t.data,
										type : t.type
									});
									this.parent.insertBefore(t.node, _7b);
								}
							} else {
								for ( var i = 0; i < _79.length; ++i) {
									var t = this._normalizedCreator(_79[i]);
									this.setItem(t.node.id, {
										data : t.data,
										type : t.type
									});
									this.parent.appendChild(t.node);
								}
							}
							return this;
						},
						destroy : function() {
							dojo.forEach(this.events, dojo.disconnect);
							this.clearItems();
							this.node = this.parent = this.current;
						},
						markupFactory : function(_7e, _7f) {
							_7e._skipStartup = true;
							return new dojo.dnd.Container(_7f, _7e);
						},
						startup : function() {
							this.parent = this.node;
							if (this.parent.tagName.toLowerCase() == "table") {
								var c = this.parent
										.getElementsByTagName("tbody");
								if (c && c.length) {
									this.parent = c[0];
								}
							}
							dojo
									.query("> .dojoDndItem", this.parent)
									.forEach(
											function(_81) {
												if (!_81.id) {
													_81.id = dojo.dnd
															.getUniqueId();
												}
												var _82 = _81
														.getAttribute("dndType"), _83 = _81
														.getAttribute("dndData");
												this.setItem(_81.id, {
													data : _83 ? _83
															: _81.innerHTML,
													type : _82 ? _82
															.split(/\s*,\s*/)
															: [ "text" ]
												});
											}, this);
						},
						onMouseOver : function(e) {
							var n = e.relatedTarget;
							while (n) {
								if (n == this.node) {
									break;
								}
								try {
									n = n.parentNode;
								} catch (x) {
									n = null;
								}
							}
							if (!n) {
								this._changeState("Container", "Over");
								this.onOverEvent();
							}
							n = this._getChildByEvent(e);
							if (this.current == n) {
								return;
							}
							if (this.current) {
								this._removeItemClass(this.current, "Over");
							}
							if (n) {
								this._addItemClass(n, "Over");
							}
							this.current = n;
						},
						onMouseOut : function(e) {
							for ( var n = e.relatedTarget; n;) {
								if (n == this.node) {
									return;
								}
								try {
									n = n.parentNode;
								} catch (x) {
									n = null;
								}
							}
							if (this.current) {
								this._removeItemClass(this.current, "Over");
								this.current = null;
							}
							this._changeState("Container", "");
							this.onOutEvent();
						},
						onOverEvent : function() {
						},
						onOutEvent : function() {
						},
						_changeState : function(_88, _89) {
							var _8a = "dojoDnd" + _88;
							var _8b = _88.toLowerCase() + "State";
							dojo.removeClass(this.node, _8a + this[_8b]);
							dojo.addClass(this.node, _8a + _89);
							this[_8b] = _89;
						},
						_addItemClass : function(_8c, _8d) {
							dojo.addClass(_8c, "dojoDndItem" + _8d);
						},
						_removeItemClass : function(_8e, _8f) {
							dojo.removeClass(_8e, "dojoDndItem" + _8f);
						},
						_getChildByEvent : function(e) {
							var _91 = e.target;
							if (_91) {
								for ( var _92 = _91.parentNode; _92; _91 = _92, _92 = _91.parentNode) {
									if (_92 == this.parent
											&& dojo
													.hasClass(_91,
															"dojoDndItem")) {
										return _91;
									}
								}
							}
							return null;
						},
						_normalizedCreator : function(_93, _94) {
							var t = (this.creator ? this.creator
									: this.defaultCreator)(_93, _94);
							if (!dojo.isArray(t.type)) {
								t.type = [ "text" ];
							}
							if (!t.node.id) {
								t.node.id = dojo.dnd.getUniqueId();
							}
							dojo.addClass(t.node, "dojoDndItem");
							return t;
						}
					});
	dojo.dnd._createNode = function(tag) {
		if (!tag) {
			return dojo.dnd._createSpan;
		}
		return function(_97) {
			var n = dojo.doc.createElement(tag);
			n.innerHTML = _97;
			return n;
		};
	};
	dojo.dnd._createTrTd = function(_99) {
		var tr = dojo.doc.createElement("tr");
		var td = dojo.doc.createElement("td");
		td.innerHTML = _99;
		tr.appendChild(td);
		return tr;
	};
	dojo.dnd._createSpan = function(_9c) {
		var n = dojo.doc.createElement("span");
		n.innerHTML = _9c;
		return n;
	};
	dojo.dnd._defaultCreatorNodes = {
		ul : "li",
		ol : "li",
		div : "div",
		p : "div"
	};
	dojo.dnd._defaultCreator = function(_9e) {
		var tag = _9e.tagName.toLowerCase();
		var c = tag == "table" ? dojo.dnd._createTrTd : dojo.dnd
				._createNode(dojo.dnd._defaultCreatorNodes[tag]);
		return function(_a1, _a2) {
			var _a3 = dojo.isObject(_a1) && _a1;
			var _a4 = (_a3 && _a1.data) ? _a1.data : _a1;
			var _a5 = (_a3 && _a1.type) ? _a1.type : [ "text" ];
			var t = String(_a4), n = (_a2 == "avatar" ? dojo.dnd._createSpan
					: c)(t);
			n.id = dojo.dnd.getUniqueId();
			return {
				node : n,
				data : _a4,
				type : _a5
			};
		};
	};
}
if (!dojo._hasResource["dojo.dnd.selector"]) {
	dojo._hasResource["dojo.dnd.selector"] = true;
	dojo.provide("dojo.dnd.selector");
	dojo.declare("dojo.dnd.Selector", dojo.dnd.Container,
			{
				constructor : function(_a8, _a9) {
					this.singular = _a9 && _a9.singular;
					this.selection = {};
					this.anchor = null;
					this.simpleSelection = false;
					this.events.push(dojo.connect(this.node, "onmousedown",
							this, "onMouseDown"), dojo.connect(this.node,
							"onmouseup", this, "onMouseUp"));
				},
				singular : false,
				getSelectedNodes : function() {
					var t = new dojo.NodeList();
					var e = dojo.dnd._empty;
					for ( var i in this.selection) {
						if (i in e) {
							continue;
						}
						t.push(dojo.byId(i));
					}
					return t;
				},
				selectNone : function() {
					return this._removeSelection()._removeAnchor();
				},
				selectAll : function() {
					this.forInItems(function(_ad, id) {
						this._addItemClass(dojo.byId(id), "Selected");
						this.selection[id] = 1;
					}, this);
					return this._removeAnchor();
				},
				deleteSelectedNodes : function() {
					var e = dojo.dnd._empty;
					for ( var i in this.selection) {
						if (i in e) {
							continue;
						}
						var n = dojo.byId(i);
						this.delItem(i);
						dojo._destroyElement(n);
					}
					this.anchor = null;
					this.selection = {};
					return this;
				},
				insertNodes : function(_b2, _b3, _b4, _b5) {
					var _b6 = this._normalizedCreator;
					this._normalizedCreator = function(_b7, _b8) {
						var t = _b6.call(this, _b7, _b8);
						if (_b2) {
							if (!this.anchor) {
								this.anchor = t.node;
								this._removeItemClass(t.node, "Selected");
								this._addItemClass(this.anchor, "Anchor");
							} else {
								if (this.anchor != t.node) {
									this._removeItemClass(t.node, "Anchor");
									this._addItemClass(t.node, "Selected");
								}
							}
							this.selection[t.node.id] = 1;
						} else {
							this._removeItemClass(t.node, "Selected");
							this._removeItemClass(t.node, "Anchor");
						}
						return t;
					};
					dojo.dnd.Selector.superclass.insertNodes.call(this, _b3,
							_b4, _b5);
					this._normalizedCreator = _b6;
					return this;
				},
				destroy : function() {
					dojo.dnd.Selector.superclass.destroy.call(this);
					this.selection = this.anchor = null;
				},
				markupFactory : function(_ba, _bb) {
					_ba._skipStartup = true;
					return new dojo.dnd.Selector(_bb, _ba);
				},
				onMouseDown : function(e) {
					if (!this.current) {
						return;
					}
					if (!this.singular && !dojo.dnd.getCopyKeyState(e)
							&& !e.shiftKey
							&& (this.current.id in this.selection)) {
						this.simpleSelection = true;
						dojo.stopEvent(e);
						return;
					}
					if (!this.singular && e.shiftKey) {
						if (!dojo.dnd.getCopyKeyState(e)) {
							this._removeSelection();
						}
						var c = dojo.query("> .dojoDndItem", this.parent);
						if (c.length) {
							if (!this.anchor) {
								this.anchor = c[0];
								this._addItemClass(this.anchor, "Anchor");
							}
							this.selection[this.anchor.id] = 1;
							if (this.anchor != this.current) {
								var i = 0;
								for (; i < c.length; ++i) {
									var _bf = c[i];
									if (_bf == this.anchor
											|| _bf == this.current) {
										break;
									}
								}
								for (++i; i < c.length; ++i) {
									var _bf = c[i];
									if (_bf == this.anchor
											|| _bf == this.current) {
										break;
									}
									this._addItemClass(_bf, "Selected");
									this.selection[_bf.id] = 1;
								}
								this._addItemClass(this.current, "Selected");
								this.selection[this.current.id] = 1;
							}
						}
					} else {
						if (this.singular) {
							if (this.anchor == this.current) {
								if (dojo.dnd.getCopyKeyState(e)) {
									this.selectNone();
								}
							} else {
								this.selectNone();
								this.anchor = this.current;
								this._addItemClass(this.anchor, "Anchor");
								this.selection[this.current.id] = 1;
							}
						} else {
							if (dojo.dnd.getCopyKeyState(e)) {
								if (this.anchor == this.current) {
									delete this.selection[this.anchor.id];
									this._removeAnchor();
								} else {
									if (this.current.id in this.selection) {
										this._removeItemClass(this.current,
												"Selected");
										delete this.selection[this.current.id];
									} else {
										if (this.anchor) {
											this._removeItemClass(this.anchor,
													"Anchor");
											this._addItemClass(this.anchor,
													"Selected");
										}
										this.anchor = this.current;
										this._addItemClass(this.current,
												"Anchor");
										this.selection[this.current.id] = 1;
									}
								}
							} else {
								if (!(this.current.id in this.selection)) {
									this.selectNone();
									this.anchor = this.current;
									this._addItemClass(this.current, "Anchor");
									this.selection[this.current.id] = 1;
								}
							}
						}
					}
					dojo.stopEvent(e);
				},
				onMouseUp : function(e) {
					if (!this.simpleSelection) {
						return;
					}
					this.simpleSelection = false;
					this.selectNone();
					if (this.current) {
						this.anchor = this.current;
						this._addItemClass(this.anchor, "Anchor");
						this.selection[this.current.id] = 1;
					}
				},
				onMouseMove : function(e) {
					this.simpleSelection = false;
				},
				onOverEvent : function() {
					this.onmousemoveEvent = dojo.connect(this.node,
							"onmousemove", this, "onMouseMove");
				},
				onOutEvent : function() {
					dojo.disconnect(this.onmousemoveEvent);
					delete this.onmousemoveEvent;
				},
				_removeSelection : function() {
					var e = dojo.dnd._empty;
					for ( var i in this.selection) {
						if (i in e) {
							continue;
						}
						var _c4 = dojo.byId(i);
						if (_c4) {
							this._removeItemClass(_c4, "Selected");
						}
					}
					this.selection = {};
					return this;
				},
				_removeAnchor : function() {
					if (this.anchor) {
						this._removeItemClass(this.anchor, "Anchor");
						this.anchor = null;
					}
					return this;
				}
			});
}
if (!dojo._hasResource["dojo.dnd.autoscroll"]) {
	dojo._hasResource["dojo.dnd.autoscroll"] = true;
	dojo.provide("dojo.dnd.autoscroll");
	dojo.dnd.getViewport = function() {
		var d = dojo.doc, dd = d.documentElement, w = window, b = dojo.body();
		if (dojo.isMozilla) {
			return {
				w : dd.clientWidth,
				h : w.innerHeight
			};
		} else {
			if (!dojo.isOpera && w.innerWidth) {
				return {
					w : w.innerWidth,
					h : w.innerHeight
				};
			} else {
				if (!dojo.isOpera && dd && dd.clientWidth) {
					return {
						w : dd.clientWidth,
						h : dd.clientHeight
					};
				} else {
					if (b.clientWidth) {
						return {
							w : b.clientWidth,
							h : b.clientHeight
						};
					}
				}
			}
		}
		return null;
	};
	dojo.dnd.V_TRIGGER_AUTOSCROLL = 32;
	dojo.dnd.H_TRIGGER_AUTOSCROLL = 32;
	dojo.dnd.V_AUTOSCROLL_VALUE = 16;
	dojo.dnd.H_AUTOSCROLL_VALUE = 16;
	dojo.dnd.autoScroll = function(e) {
		var v = dojo.dnd.getViewport(), dx = 0, dy = 0;
		if (e.clientX < dojo.dnd.H_TRIGGER_AUTOSCROLL) {
			dx = -dojo.dnd.H_AUTOSCROLL_VALUE;
		} else {
			if (e.clientX > v.w - dojo.dnd.H_TRIGGER_AUTOSCROLL) {
				dx = dojo.dnd.H_AUTOSCROLL_VALUE;
			}
		}
		if (e.clientY < dojo.dnd.V_TRIGGER_AUTOSCROLL) {
			dy = -dojo.dnd.V_AUTOSCROLL_VALUE;
		} else {
			if (e.clientY > v.h - dojo.dnd.V_TRIGGER_AUTOSCROLL) {
				dy = dojo.dnd.V_AUTOSCROLL_VALUE;
			}
		}
		window.scrollBy(dx, dy);
	};
	dojo.dnd._validNodes = {
		"div" : 1,
		"p" : 1,
		"td" : 1
	};
	dojo.dnd._validOverflow = {
		"auto" : 1,
		"scroll" : 1
	};
	dojo.dnd.autoScrollNodes = function(e) {
		for ( var n = e.target; n;) {
			if (n.nodeType == 1
					&& (n.tagName.toLowerCase() in dojo.dnd._validNodes)) {
				var s = dojo.getComputedStyle(n);
				if (s.overflow.toLowerCase() in dojo.dnd._validOverflow) {
					var b = dojo._getContentBox(n, s), t = dojo._abs(n, true);
					console
							.debug(b.l, b.t, t.x, t.y, n.scrollLeft,
									n.scrollTop);
					b.l += t.x + n.scrollLeft;
					b.t += t.y + n.scrollTop;
					var w = Math.min(dojo.dnd.H_TRIGGER_AUTOSCROLL, b.w / 2), h = Math
							.min(dojo.dnd.V_TRIGGER_AUTOSCROLL, b.h / 2), rx = e.pageX
							- b.l, ry = e.pageY - b.t, dx = 0, dy = 0;
					if (rx > 0 && rx < b.w) {
						if (rx < w) {
							dx = -dojo.dnd.H_AUTOSCROLL_VALUE;
						} else {
							if (rx > b.w - w) {
								dx = dojo.dnd.H_AUTOSCROLL_VALUE;
							}
						}
					}
					if (ry > 0 && ry < b.h) {
						if (ry < h) {
							dy = -dojo.dnd.V_AUTOSCROLL_VALUE;
						} else {
							if (ry > b.h - h) {
								dy = dojo.dnd.V_AUTOSCROLL_VALUE;
							}
						}
					}
					var _d8 = n.scrollLeft, _d9 = n.scrollTop;
					n.scrollLeft = n.scrollLeft + dx;
					n.scrollTop = n.scrollTop + dy;
					if (dx || dy) {
						console.debug(_d8 + ", " + _d9 + "\n" + dx + ", " + dy
								+ "\n" + n.scrollLeft + ", " + n.scrollTop);
					}
					if (_d8 != n.scrollLeft || _d9 != n.scrollTop) {
						return;
					}
				}
			}
			try {
				n = n.parentNode;
			} catch (x) {
				n = null;
			}
		}
		dojo.dnd.autoScroll(e);
	};
}
if (!dojo._hasResource["dojo.dnd.avatar"]) {
	dojo._hasResource["dojo.dnd.avatar"] = true;
	dojo.provide("dojo.dnd.avatar");
	dojo.dnd.Avatar = function(_da) {
		this.manager = _da;
		this.construct();
	};
	dojo.extend(dojo.dnd.Avatar, {
		construct : function() {
			var a = dojo.doc.createElement("table");
			a.className = "dojoDndAvatar";
			a.style.position = "absolute";
			a.style.zIndex = 1999;
			a.style.margin = "0px";
			var b = dojo.doc.createElement("tbody");
			var tr = dojo.doc.createElement("tr");
			tr.className = "dojoDndAvatarHeader";
			var td = dojo.doc.createElement("td");
			td.innerHTML = this._generateText();
			tr.appendChild(td);
			dojo.style(tr, "opacity", 0.9);
			b.appendChild(tr);
			var k = Math.min(5, this.manager.nodes.length);
			var _e0 = this.manager.source;
			for ( var i = 0; i < k; ++i) {
				tr = dojo.doc.createElement("tr");
				tr.className = "dojoDndAvatarItem";
				td = dojo.doc.createElement("td");
				var _e2 = _e0.creator ? _e2 = _e0._normalizedCreator(_e0
						.getItem(this.manager.nodes[i].id).data, "avatar").node
						: _e2 = this.manager.nodes[i].cloneNode(true);
				_e2.id = "";
				td.appendChild(_e2);
				tr.appendChild(td);
				dojo.style(tr, "opacity", (9 - i) / 10);
				b.appendChild(tr);
			}
			a.appendChild(b);
			this.node = a;
		},
		destroy : function() {
			dojo._destroyElement(this.node);
			this.node = false;
		},
		update : function() {
			dojo[(this.manager.canDropFlag ? "add" : "remove") + "Class"](
					this.node, "dojoDndAvatarCanDrop");
			var t = this.node.getElementsByTagName("td");
			for ( var i = 0; i < t.length; ++i) {
				var n = t[i];
				if (dojo.hasClass(n.parentNode, "dojoDndAvatarHeader")) {
					n.innerHTML = this._generateText();
					break;
				}
			}
		},
		_generateText : function() {
			return this.manager.nodes.length.toString();
		}
	});
}
if (!dojo._hasResource["dojo.dnd.manager"]) {
	dojo._hasResource["dojo.dnd.manager"] = true;
	dojo.provide("dojo.dnd.manager");
	dojo.dnd.Manager = function() {
		this.avatar = null;
		this.source = null;
		this.nodes = [];
		this.copy = true;
		this.target = null;
		this.canDropFlag = false;
		this.events = [];
	};
	dojo.extend(dojo.dnd.Manager, {
		OFFSET_X : 16,
		OFFSET_Y : 16,
		overSource : function(_e6) {
			if (this.avatar) {
				this.target = (_e6 && _e6.targetState != "Disabled") ? _e6
						: null;
				this.avatar.update();
			}
			dojo.publish("/dnd/source/over", [ _e6 ]);
		},
		outSource : function(_e7) {
			if (this.avatar) {
				if (this.target == _e7) {
					this.target = null;
					this.canDropFlag = false;
					this.avatar.update();
					dojo.publish("/dnd/source/over", [ null ]);
				}
			} else {
				dojo.publish("/dnd/source/over", [ null ]);
			}
		},
		startDrag : function(_e8, _e9, _ea) {
			this.source = _e8;
			this.nodes = _e9;
			this.copy = Boolean(_ea);
			this.avatar = this.makeAvatar();
			dojo.body().appendChild(this.avatar.node);
			dojo.publish("/dnd/start", [ _e8, _e9, this.copy ]);
			this.events = [
					dojo.connect(dojo.doc, "onmousemove", this, "onMouseMove"),
					dojo.connect(dojo.doc, "onmouseup", this, "onMouseUp"),
					dojo.connect(dojo.doc, "onkeydown", this, "onKeyDown"),
					dojo.connect(dojo.doc, "onkeyup", this, "onKeyUp") ];
			var c = "dojoDnd" + (_ea ? "Copy" : "Move");
			dojo.addClass(dojo.body(), c);
		},
		canDrop : function(_ec) {
			var _ed = this.target && _ec;
			if (this.canDropFlag != _ed) {
				this.canDropFlag = _ed;
				this.avatar.update();
			}
		},
		stopDrag : function() {
			dojo.removeClass(dojo.body(), "dojoDndCopy");
			dojo.removeClass(dojo.body(), "dojoDndMove");
			dojo.forEach(this.events, dojo.disconnect);
			this.events = [];
			this.avatar.destroy();
			this.avatar = null;
			this.source = null;
			this.nodes = [];
		},
		makeAvatar : function() {
			return new dojo.dnd.Avatar(this);
		},
		updateAvatar : function() {
			this.avatar.update();
		},
		onMouseMove : function(e) {
			var a = this.avatar;
			if (a) {
				dojo.dnd.autoScroll(e);
				dojo.marginBox(a.node, {
					l : e.pageX + this.OFFSET_X,
					t : e.pageY + this.OFFSET_Y
				});
				var _f0 = Boolean(this.source.copyState(dojo.dnd
						.getCopyKeyState(e)));
				if (this.copy != _f0) {
					this._setCopyStatus(_f0);
				}
			}
		},
		onMouseUp : function(e) {
			if (this.avatar) {
				if (this.target && this.canDropFlag) {
					dojo.publish("/dnd/drop", [
							this.source,
							this.nodes,
							Boolean(this.source.copyState(dojo.dnd
									.getCopyKeyState(e))) ]);
				} else {
					dojo.publish("/dnd/cancel");
				}
				this.stopDrag();
			}
		},
		onKeyDown : function(e) {
			if (this.avatar) {
				switch (e.keyCode) {
				case dojo.keys.CTRL:
					var _f3 = Boolean(this.source.copyState(true));
					if (this.copy != _f3) {
						this._setCopyStatus(_f3);
					}
					break;
				case dojo.keys.ESCAPE:
					dojo.publish("/dnd/cancel");
					this.stopDrag();
					break;
				}
			}
		},
		onKeyUp : function(e) {
			if (this.avatar && e.keyCode == dojo.keys.CTRL) {
				var _f5 = Boolean(this.source.copyState(false));
				if (this.copy != _f5) {
					this._setCopyStatus(_f5);
				}
			}
		},
		_setCopyStatus : function(_f6) {
			this.copy = _f6;
			this.source._markDndStatus(this.copy);
			this.updateAvatar();
			dojo.removeClass(dojo.body(), "dojoDnd"
					+ (this.copy ? "Move" : "Copy"));
			dojo.addClass(dojo.body(), "dojoDnd"
					+ (this.copy ? "Copy" : "Move"));
		}
	});
	dojo.dnd._manager = null;
	dojo.dnd.manager = function() {
		if (!dojo.dnd._manager) {
			dojo.dnd._manager = new dojo.dnd.Manager();
		}
		return dojo.dnd._manager;
	};
}
if (!dojo._hasResource["dojo.dnd.source"]) {
	dojo._hasResource["dojo.dnd.source"] = true;
	dojo.provide("dojo.dnd.source");
	dojo
			.declare(
					"dojo.dnd.Source",
					dojo.dnd.Selector,
					{
						isSource : true,
						horizontal : false,
						copyOnly : false,
						skipForm : false,
						accept : [ "text" ],
						constructor : function(_f7, _f8) {
							if (!_f8) {
								_f8 = {};
							}
							this.isSource = typeof _f8.isSource == "undefined" ? true
									: _f8.isSource;
							var _f9 = _f8.accept instanceof Array ? _f8.accept
									: [ "text" ];
							this.accept = null;
							if (_f9.length) {
								this.accept = {};
								for ( var i = 0; i < _f9.length; ++i) {
									this.accept[_f9[i]] = 1;
								}
							}
							this.horizontal = _f8.horizontal;
							this.copyOnly = _f8.copyOnly;
							this.skipForm = _f8.skipForm;
							this.isDragging = false;
							this.mouseDown = false;
							this.targetAnchor = null;
							this.targetBox = null;
							this.before = true;
							this.sourceState = "";
							if (this.isSource) {
								dojo.addClass(this.node, "dojoDndSource");
							}
							this.targetState = "";
							if (this.accept) {
								dojo.addClass(this.node, "dojoDndTarget");
							}
							if (this.horizontal) {
								dojo.addClass(this.node, "dojoDndHorizontal");
							}
							this.topics = [
									dojo.subscribe("/dnd/source/over", this,
											"onDndSourceOver"),
									dojo.subscribe("/dnd/start", this,
											"onDndStart"),
									dojo.subscribe("/dnd/drop", this,
											"onDndDrop"),
									dojo.subscribe("/dnd/cancel", this,
											"onDndCancel") ];
						},
						checkAcceptance : function(_fb, _fc) {
							if (this == _fb) {
								return true;
							}
							for ( var i = 0; i < _fc.length; ++i) {
								var _fe = _fb.getItem(_fc[i].id).type;
								var _ff = false;
								for ( var j = 0; j < _fe.length; ++j) {
									if (_fe[j] in this.accept) {
										_ff = true;
										break;
									}
								}
								if (!_ff) {
									return false;
								}
							}
							return true;
						},
						copyState : function(_101) {
							return this.copyOnly || _101;
						},
						destroy : function() {
							dojo.dnd.Source.superclass.destroy.call(this);
							dojo.forEach(this.topics, dojo.unsubscribe);
							this.targetAnchor = null;
						},
						markupFactory : function(_102, node) {
							_102._skipStartup = true;
							return new dojo.dnd.Source(node, _102);
						},
						onMouseMove : function(e) {
							if (this.isDragging
									&& this.targetState == "Disabled") {
								return;
							}
							dojo.dnd.Source.superclass.onMouseMove
									.call(this, e);
							var m = dojo.dnd.manager();
							if (this.isDragging) {
								var _106 = false;
								if (this.current) {
									if (!this.targetBox
											|| this.targetAnchor != this.current) {
										this.targetBox = {
											xy : dojo
													.coords(this.current, true),
											w : this.current.offsetWidth,
											h : this.current.offsetHeight
										};
									}
									if (this.horizontal) {
										_106 = (e.pageX - this.targetBox.xy.x) < (this.targetBox.w / 2);
									} else {
										_106 = (e.pageY - this.targetBox.xy.y) < (this.targetBox.h / 2);
									}
								}
								if (this.current != this.targetAnchor
										|| _106 != this.before) {
									this._markTargetAnchor(_106);
									m
											.canDrop(!this.current
													|| m.source != this
													|| !(this.current.id in this.selection));
								}
							} else {
								if (this.mouseDown && this.isSource) {
									var _107 = this.getSelectedNodes();
									if (_107.length) {
										m.startDrag(this, _107, this
												.copyState(dojo.dnd
														.getCopyKeyState(e)));
									}
								}
							}
						},
						onMouseDown : function(e) {
							if (!this.skipForm || !dojo.dnd.isFormElement(e)) {
								this.mouseDown = true;
								dojo.dnd.Source.superclass.onMouseDown.call(
										this, e);
							}
						},
						onMouseUp : function(e) {
							if (this.mouseDown) {
								this.mouseDown = false;
								dojo.dnd.Source.superclass.onMouseUp.call(this,
										e);
							}
						},
						onDndSourceOver : function(_10a) {
							if (this != _10a) {
								this.mouseDown = false;
								if (this.targetAnchor) {
									this._unmarkTargetAnchor();
								}
							} else {
								if (this.isDragging) {
									var m = dojo.dnd.manager();
									m
											.canDrop(this.targetState != "Disabled"
													&& (!this.current
															|| m.source != this || !(this.current.id in this.selection)));
								}
							}
						},
						onDndStart : function(_10c, _10d, copy) {
							if (this.isSource) {
								this._changeState("Source",
										this == _10c ? (copy ? "Copied"
												: "Moved") : "");
							}
							var _10f = this.accept
									&& this.checkAcceptance(_10c, _10d);
							this._changeState("Target", _10f ? "" : "Disabled");
							if (_10f) {
								dojo.dnd.manager().overSource(this);
							}
							this.isDragging = true;
						},
						onDndDrop : function(_110, _111, copy) {
							do {
								if (this.containerState != "Over") {
									break;
								}
								var _113 = this._normalizedCreator;
								if (this != _110) {
									if (this.creator) {
										this._normalizedCreator = function(
												node, hint) {
											return _113.call(this, _110
													.getItem(node.id).data,
													hint);
										};
									} else {
										if (copy) {
											this._normalizedCreator = function(
													node, hint) {
												var t = _110.getItem(node.id);
												var n = node.cloneNode(true);
												n.id = dojo.dnd.getUniqueId();
												return {
													node : n,
													data : t.data,
													type : t.type
												};
											};
										} else {
											this._normalizedCreator = function(
													node, hint) {
												var t = _110.getItem(node.id);
												_110.delItem(node.id);
												return {
													node : node,
													data : t.data,
													type : t.type
												};
											};
										}
									}
								} else {
									if (this.current
											&& this.current.id in this.selection) {
										break;
									}
									if (this.creator) {
										if (copy) {
											this._normalizedCreator = function(
													node, hint) {
												return _113.call(this, _110
														.getItem(node.id).data,
														hint);
											};
										} else {
											this._normalizedCreator = function(
													node, hint) {
												var t = _110.getItem(node.id);
												return {
													node : node,
													data : t.data,
													type : t.type
												};
											};
										}
									} else {
										if (copy) {
											this._normalizedCreator = function(
													node, hint) {
												var t = _110.getItem(node.id);
												var n = node.cloneNode(true);
												n.id = dojo.dnd.getUniqueId();
												return {
													node : n,
													data : t.data,
													type : t.type
												};
											};
										} else {
											this._normalizedCreator = function(
													node, hint) {
												var t = _110.getItem(node.id);
												return {
													node : node,
													data : t.data,
													type : t.type
												};
											};
										}
									}
								}
								this._removeSelection();
								if (this != _110) {
									this._removeAnchor();
								}
								if (this != _110 && !copy && !this.creator) {
									_110.selectNone();
								}
								this.insertNodes(true, _111, this.before,
										this.current);
								if (this != _110 && !copy && this.creator) {
									_110.deleteSelectedNodes();
								}
								this._normalizedCreator = _113;
							} while (false);
							this.onDndCancel();
						},
						onDndCancel : function() {
							if (this.targetAnchor) {
								this._unmarkTargetAnchor();
								this.targetAnchor = null;
							}
							this.before = true;
							this.isDragging = false;
							this.mouseDown = false;
							this._changeState("Source", "");
							this._changeState("Target", "");
						},
						onOverEvent : function() {
							dojo.dnd.Source.superclass.onOverEvent.call(this);
							dojo.dnd.manager().overSource(this);
						},
						onOutEvent : function() {
							dojo.dnd.Source.superclass.onOutEvent.call(this);
							dojo.dnd.manager().outSource(this);
						},
						_markTargetAnchor : function(_129) {
							if (this.current == this.targetAnchor
									&& this.before == _129) {
								return;
							}
							if (this.targetAnchor) {
								this._removeItemClass(this.targetAnchor,
										this.before ? "Before" : "After");
							}
							this.targetAnchor = this.current;
							this.targetBox = null;
							this.before = _129;
							if (this.targetAnchor) {
								this._addItemClass(this.targetAnchor,
										this.before ? "Before" : "After");
							}
						},
						_unmarkTargetAnchor : function() {
							if (!this.targetAnchor) {
								return;
							}
							this._removeItemClass(this.targetAnchor,
									this.before ? "Before" : "After");
							this.targetAnchor = null;
							this.targetBox = null;
							this.before = true;
						},
						_markDndStatus : function(copy) {
							this._changeState("Source", copy ? "Copied"
									: "Moved");
						}
					});
	dojo.declare("dojo.dnd.Target", dojo.dnd.Source, {
		constructor : function(node, _12c) {
			this.isSource = false;
			dojo.removeClass(this.node, "dojoDndSource");
		},
		markupFactory : function(_12d, node) {
			_12d._skipStartup = true;
			return new dojo.dnd.Target(node, _12d);
		}
	});
}
if (!dojo._hasResource["dojo.dnd.move"]) {
	dojo._hasResource["dojo.dnd.move"] = true;
	dojo.provide("dojo.dnd.move");
	dojo.dnd.Mover = function(node, e) {
		this.node = dojo.byId(node);
		this.marginBox = {
			l : e.pageX,
			t : e.pageY
		};
		var d = node.ownerDocument, _132 = dojo.connect(d, "onmousemove", this,
				"onFirstMove");
		this.events = [ dojo.connect(d, "onmousemove", this, "onMouseMove"),
				dojo.connect(d, "onmouseup", this, "destroy"),
				dojo.connect(d, "ondragstart", dojo, "stopEvent"),
				dojo.connect(d, "onselectstart", dojo, "stopEvent"), _132 ];
		dojo.publish("/dnd/move/start", [ this.node ]);
		dojo.addClass(dojo.body(), "dojoMove");
		dojo.addClass(this.node, "dojoMoveItem");
	};
	dojo.extend(dojo.dnd.Mover, {
		onMouseMove : function(e) {
			dojo.dnd.autoScroll(e);
			var m = this.marginBox;
			dojo.marginBox(this.node, {
				l : m.l + e.pageX,
				t : m.t + e.pageY
			});
		},
		onFirstMove : function() {
			this.node.style.position = "absolute";
			var m = dojo.marginBox(this.node);
			m.l -= this.marginBox.l;
			m.t -= this.marginBox.t;
			this.marginBox = m;
			dojo.disconnect(this.events.pop());
		},
		destroy : function() {
			dojo.forEach(this.events, dojo.disconnect);
			dojo.publish("/dnd/move/stop", [ this.node ]);
			dojo.removeClass(dojo.body(), "dojoMove");
			dojo.removeClass(this.node, "dojoMoveItem");
			this.events = this.node = null;
		}
	});
	dojo.dnd.Moveable = function(node, _137) {
		this.node = dojo.byId(node);
		this.handle = (_137 && _137.handle) ? dojo.byId(_137.handle) : null;
		if (!this.handle) {
			this.handle = this.node;
		}
		this.delay = (_137 && _137.delay > 0) ? _137.delay : 0;
		this.skip = _137 && _137.skip;
		this.mover = (_137 && _137.mover) ? _137.mover : dojo.dnd.Mover;
		this.events = [
				dojo.connect(this.handle, "onmousedown", this, "onMouseDown"),
				dojo.connect(this.handle, "ondragstart", dojo, "stopEvent"),
				dojo.connect(this.handle, "onselectstart", dojo, "stopEvent") ];
	};
	dojo.extend(dojo.dnd.Moveable, {
		handle : "",
		delay : 0,
		skip : false,
		markupFactory : function(_138, node) {
			return new dojo.dnd.Moveable(node, _138);
		},
		destroy : function() {
			dojo.forEach(this.events, dojo.disconnect);
			this.events = this.node = this.handle = null;
		},
		onMouseDown : function(e) {
			if (this.skip && dojo.dnd.isFormElement(e)) {
				return;
			}
			if (this.delay) {
				this.events.push(dojo.connect(this.handle, "onmousemove", this,
						"onMouseMove"));
				this.events.push(dojo.connect(this.handle, "onmouseup", this,
						"onMouseUp"));
				this._lastX = e.pageX;
				this._lastY = e.pageY;
			} else {
				new this.mover(this.node, e);
			}
			dojo.stopEvent(e);
		},
		onMouseMove : function(e) {
			if (Math.abs(e.pageX - this._lastX) > this.delay
					|| Math.abs(e.pageY - this._lastY) > this.delay) {
				this.onMouseUp(e);
				new this.mover(this.node, e);
			}
			dojo.stopEvent(e);
		},
		onMouseUp : function(e) {
			dojo.disconnect(this.events.pop());
			dojo.disconnect(this.events.pop());
		}
	});
	dojo.dnd.constrainedMover = function(fun, _13e) {
		var _13f = function(node, e) {
			dojo.dnd.Mover.call(this, node, e);
		};
		dojo.extend(_13f, dojo.dnd.Mover.prototype);
		dojo
				.extend(
						_13f,
						{
							onMouseMove : function(e) {
								var m = this.marginBox, c = this.constraintBox, l = m.l
										+ e.pageX, t = m.t + e.pageY;
								l = l < c.l ? c.l : c.r < l ? c.r : l;
								t = t < c.t ? c.t : c.b < t ? c.b : t;
								dojo.marginBox(this.node, {
									l : l,
									t : t
								});
							},
							onFirstMove : function() {
								dojo.dnd.Mover.prototype.onFirstMove.call(this);
								var c = this.constraintBox = fun.call(this), m = this.marginBox;
								c.r = c.l + c.w - (_13e ? m.w : 0);
								c.b = c.t + c.h - (_13e ? m.h : 0);
							}
						});
		return _13f;
	};
	dojo.dnd.boxConstrainedMover = function(box, _14a) {
		return dojo.dnd.constrainedMover(function() {
			return box;
		}, _14a);
	};
	dojo.dnd.parentConstrainedMover = function(area, _14c) {
		var fun = function() {
			var n = this.node.parentNode, s = dojo.getComputedStyle(n), mb = dojo
					._getMarginBox(n, s);
			if (area == "margin") {
				return mb;
			}
			var t = dojo._getMarginExtents(n, s);
			mb.l += t.l, mb.t += t.t, mb.w -= t.w, mb.h -= t.h;
			if (area == "border") {
				return mb;
			}
			t = dojo._getBorderExtents(n, s);
			mb.l += t.l, mb.t += t.t, mb.w -= t.w, mb.h -= t.h;
			if (area == "padding") {
				return mb;
			}
			t = dojo._getPadExtents(n, s);
			mb.l += t.l, mb.t += t.t, mb.w -= t.w, mb.h -= t.h;
			return mb;
		};
		return dojo.dnd.constrainedMover(fun, _14c);
	};
}
if (!dojo._hasResource["dojo.i18n"]) {
	dojo._hasResource["dojo.i18n"] = true;
	dojo.provide("dojo.i18n");
	dojo.i18n.getLocalization = function(_152, _153, _154) {
		_154 = dojo.i18n.normalizeLocale(_154);
		var _155 = _154.split("-");
		var _156 = [ _152, "nls", _153 ].join(".");
		var _157 = dojo._loadedModules[_156];
		if (_157) {
			var _158;
			for ( var i = _155.length; i > 0; i--) {
				var loc = _155.slice(0, i).join("_");
				if (_157[loc]) {
					_158 = _157[loc];
					break;
				}
			}
			if (!_158) {
				_158 = _157.ROOT;
			}
			if (_158) {
				var _15b = function() {
				};
				_15b.prototype = _158;
				return new _15b();
			}
		}
		throw new Error("Bundle not found: " + _153 + " in " + _152
				+ " , locale=" + _154);
	};
	dojo.i18n.normalizeLocale = function(_15c) {
		var _15d = _15c ? _15c.toLowerCase() : dojo.locale;
		if (_15d == "root") {
			_15d = "ROOT";
		}
		return _15d;
	};
	dojo.i18n._requireLocalization = function(_15e, _15f, _160, _161) {
		var _162 = dojo.i18n.normalizeLocale(_160);
		var _163 = [ _15e, "nls", _15f ].join(".");
		var _164 = "";
		if (_161) {
			var _165 = _161.split(",");
			for ( var i = 0; i < _165.length; i++) {
				if (_162.indexOf(_165[i]) == 0) {
					if (_165[i].length > _164.length) {
						_164 = _165[i];
					}
				}
			}
			if (!_164) {
				_164 = "ROOT";
			}
		}
		var _167 = _161 ? _164 : _162;
		var _168 = dojo._loadedModules[_163];
		var _169 = null;
		if (_168) {
			if (djConfig.localizationComplete && _168._built) {
				return;
			}
			var _16a = _167.replace(/-/g, "_");
			var _16b = _163 + "." + _16a;
			_169 = dojo._loadedModules[_16b];
		}
		if (!_169) {
			_168 = dojo["provide"](_163);
			var syms = dojo._getModuleSymbols(_15e);
			var _16d = syms.concat("nls").join("/");
			var _16e;
			dojo.i18n._searchLocalePath(_167, _161, function(loc) {
				var _170 = loc.replace(/-/g, "_");
				var _171 = _163 + "." + _170;
				var _172 = false;
				if (!dojo._loadedModules[_171]) {
					dojo["provide"](_171);
					var _173 = [ _16d ];
					if (loc != "ROOT") {
						_173.push(loc);
					}
					_173.push(_15f);
					var _174 = _173.join("/") + ".js";
					_172 = dojo._loadPath(_174, null, function(hash) {
						var _176 = function() {
						};
						_176.prototype = _16e;
						_168[_170] = new _176();
						for ( var j in hash) {
							_168[_170][j] = hash[j];
						}
					});
				} else {
					_172 = true;
				}
				if (_172 && _168[_170]) {
					_16e = _168[_170];
				} else {
					_168[_170] = _16e;
				}
				if (_161) {
					return true;
				}
			});
		}
		if (_161 && _162 != _164) {
			_168[_162.replace(/-/g, "_")] = _168[_164.replace(/-/g, "_")];
		}
	};
	(function() {
		var _178 = djConfig.extraLocale;
		if (_178) {
			if (!_178 instanceof Array) {
				_178 = [ _178 ];
			}
			var req = dojo.i18n._requireLocalization;
			dojo.i18n._requireLocalization = function(m, b, _17c, _17d) {
				req(m, b, _17c, _17d);
				if (_17c) {
					return;
				}
				for ( var i = 0; i < _178.length; i++) {
					req(m, b, _178[i], _17d);
				}
			};
		}
	})();
	dojo.i18n._searchLocalePath = function(_17f, down, _181) {
		_17f = dojo.i18n.normalizeLocale(_17f);
		var _182 = _17f.split("-");
		var _183 = [];
		for ( var i = _182.length; i > 0; i--) {
			_183.push(_182.slice(0, i).join("-"));
		}
		_183.push(false);
		if (down) {
			_183.reverse();
		}
		for ( var j = _183.length - 1; j >= 0; j--) {
			var loc = _183[j] || "ROOT";
			var stop = _181(loc);
			if (stop) {
				break;
			}
		}
	};
	dojo.i18n._preloadLocalizations = function(_188, _189) {
		function preload(_18a) {
			_18a = dojo.i18n.normalizeLocale(_18a);
			dojo.i18n._searchLocalePath(_18a, true, function(loc) {
				for ( var i = 0; i < _189.length; i++) {
					if (_189[i] == loc) {
						dojo["require"](_188 + "_" + loc);
						return true;
					}
				}
				return false;
			});
		}
		;
		preload();
		var _18d = djConfig.extraLocale || [];
		for ( var i = 0; i < _18d.length; i++) {
			preload(_18d[i]);
		}
	};
}
if (!dojo._hasResource["dojo.string"]) {
	dojo._hasResource["dojo.string"] = true;
	dojo.provide("dojo.string");
	dojo.string.pad = function(text, size, ch, end) {
		var out = String(text);
		if (!ch) {
			ch = "0";
		}
		while (out.length < size) {
			if (end) {
				out += ch;
			} else {
				out = ch + out;
			}
		}
		return out;
	};
	dojo.string.substitute = function(_194, map, _196, _197) {
		return _194.replace(/\$\{([^\s\:\}]+)(?:\:([^\s\:\}]+))?\}/g, function(
				_198, key, _19a) {
			var _19b = dojo.getObject(key, false, map);
			if (_19a) {
				_19b = dojo.getObject(_19a, false, _197)(_19b);
			}
			if (_196) {
				_19b = _196(_19b, key);
			}
			return _19b.toString();
		});
	};
	dojo.string.trim = function(str) {
		str = str.replace(/^\s+/, "");
		for ( var i = str.length - 1; i > 0; i--) {
			if (/\S/.test(str.charAt(i))) {
				str = str.substring(0, i + 1);
				break;
			}
		}
		return str;
	};
}
if (!dojo._hasResource["dojo.regexp"]) {
	dojo._hasResource["dojo.regexp"] = true;
	dojo.provide("dojo.regexp");
	dojo.regexp.escapeString = function(str, _19f) {
		return str.replace(/([\.$?*!=:|{}\(\)\[\]\\\/^])/g, function(ch) {
			if (_19f && _19f.indexOf(ch) != -1) {
				return ch;
			}
			return "\\" + ch;
		});
	};
	dojo.regexp.buildGroupRE = function(a, re, _1a3) {
		if (!(a instanceof Array)) {
			return re(a);
		}
		var b = [];
		for ( var i = 0; i < a.length; i++) {
			b.push(re(a[i]));
		}
		return dojo.regexp.group(b.join("|"), _1a3);
	};
	dojo.regexp.group = function(_1a6, _1a7) {
		return "(" + (_1a7 ? "?:" : "") + _1a6 + ")";
	};
}
if (!dojo._hasResource["dojo.number"]) {
	dojo._hasResource["dojo.number"] = true;
	dojo.provide("dojo.number");
	dojo.number.format = function(_1a8, _1a9) {
		_1a9 = dojo.mixin({}, _1a9 || {});
		var _1aa = dojo.i18n.normalizeLocale(_1a9.locale);
		var _1ab = dojo.i18n.getLocalization("dojo.cldr", "number", _1aa);
		_1a9.customs = _1ab;
		var _1ac = _1a9.pattern || _1ab[(_1a9.type || "decimal") + "Format"];
		if (isNaN(_1a8)) {
			return null;
		}
		return dojo.number._applyPattern(_1a8, _1ac, _1a9);
	};
	dojo.number._numberPatternRE = /[#0,]*[#0](?:\.0*#*)?/;
	dojo.number._applyPattern = function(_1ad, _1ae, _1af) {
		_1af = _1af || {};
		var _1b0 = _1af.customs.group;
		var _1b1 = _1af.customs.decimal;
		var _1b2 = _1ae.split(";");
		var _1b3 = _1b2[0];
		_1ae = _1b2[(_1ad < 0) ? 1 : 0] || ("-" + _1b3);
		if (_1ae.indexOf("%") != -1) {
			_1ad *= 100;
		} else {
			if (_1ae.indexOf("\u2030") != -1) {
				_1ad *= 1000;
			} else {
				if (_1ae.indexOf("\xa4") != -1) {
					_1b0 = _1af.customs.currencyGroup || _1b0;
					_1b1 = _1af.customs.currencyDecimal || _1b1;
					_1ae = _1ae.replace(/\u00a4{1,3}/,
							function(_1b4) {
								var prop = [ "symbol", "currency",
										"displayName" ][_1b4.length - 1];
								return _1af[prop] || _1af.currency || "";
							});
				} else {
					if (_1ae.indexOf("E") != -1) {
						throw new Error("exponential notation not supported");
					}
				}
			}
		}
		var _1b6 = dojo.number._numberPatternRE;
		var _1b7 = _1b3.match(_1b6);
		if (!_1b7) {
			throw new Error("unable to find a number expression in pattern: "
					+ _1ae);
		}
		return _1ae.replace(_1b6, dojo.number._formatAbsolute(_1ad, _1b7[0], {
			decimal : _1b1,
			group : _1b0,
			places : _1af.places
		}));
	};
	dojo.number.round = function(_1b8, _1b9, _1ba) {
		var _1bb = String(_1b8).split(".");
		var _1bc = (_1bb[1] && _1bb[1].length) || 0;
		if (_1bc > _1b9) {
			var _1bd = Math.pow(10, _1b9);
			if (_1ba > 0) {
				_1bd *= 10 / _1ba;
				_1b9++;
			}
			_1b8 = Math.round(_1b8 * _1bd) / _1bd;
			_1bb = String(_1b8).split(".");
			_1bc = (_1bb[1] && _1bb[1].length) || 0;
			if (_1bc > _1b9) {
				_1bb[1] = _1bb[1].substr(0, _1b9);
				_1b8 = Number(_1bb.join("."));
			}
		}
		return _1b8;
	};
	dojo.number._formatAbsolute = function(_1be, _1bf, _1c0) {
		_1c0 = _1c0 || {};
		if (_1c0.places === true) {
			_1c0.places = 0;
		}
		if (_1c0.places === Infinity) {
			_1c0.places = 6;
		}
		var _1c1 = _1bf.split(".");
		var _1c2 = (_1c0.places >= 0) ? _1c0.places
				: (_1c1[1] && _1c1[1].length) || 0;
		if (!(_1c0.round < 0)) {
			_1be = dojo.number.round(_1be, _1c2, _1c0.round);
		}
		var _1c3 = String(Math.abs(_1be)).split(".");
		var _1c4 = _1c3[1] || "";
		if (_1c0.places) {
			_1c3[1] = dojo.string.pad(_1c4.substr(0, _1c0.places), _1c0.places,
					"0", true);
		} else {
			if (_1c1[1] && _1c0.places !== 0) {
				var pad = _1c1[1].lastIndexOf("0") + 1;
				if (pad > _1c4.length) {
					_1c3[1] = dojo.string.pad(_1c4, pad, "0", true);
				}
				var _1c6 = _1c1[1].length;
				if (_1c6 < _1c4.length) {
					_1c3[1] = _1c4.substr(0, _1c6);
				}
			} else {
				if (_1c3[1]) {
					_1c3.pop();
				}
			}
		}
		var _1c7 = _1c1[0].replace(",", "");
		pad = _1c7.indexOf("0");
		if (pad != -1) {
			pad = _1c7.length - pad;
			if (pad > _1c3[0].length) {
				_1c3[0] = dojo.string.pad(_1c3[0], pad);
			}
			if (_1c7.indexOf("#") == -1) {
				_1c3[0] = _1c3[0].substr(_1c3[0].length - pad);
			}
		}
		var _1c8 = _1c1[0].lastIndexOf(",");
		var _1c9, _1ca;
		if (_1c8 != -1) {
			_1c9 = _1c1[0].length - _1c8 - 1;
			var _1cb = _1c1[0].substr(0, _1c8);
			_1c8 = _1cb.lastIndexOf(",");
			if (_1c8 != -1) {
				_1ca = _1cb.length - _1c8 - 1;
			}
		}
		var _1cc = [];
		for ( var _1cd = _1c3[0]; _1cd;) {
			var off = _1cd.length - _1c9;
			_1cc.push((off > 0) ? _1cd.substr(off) : _1cd);
			_1cd = (off > 0) ? _1cd.slice(0, off) : "";
			if (_1ca) {
				_1c9 = _1ca;
				delete _1ca;
			}
		}
		_1c3[0] = _1cc.reverse().join(_1c0.group || ",");
		return _1c3.join(_1c0.decimal || ".");
	};
	dojo.number.regexp = function(_1cf) {
		return dojo.number._parseInfo(_1cf).regexp;
	};
	dojo.number._parseInfo = function(_1d0) {
		_1d0 = _1d0 || {};
		var _1d1 = dojo.i18n.normalizeLocale(_1d0.locale);
		var _1d2 = dojo.i18n.getLocalization("dojo.cldr", "number", _1d1);
		var _1d3 = _1d0.pattern || _1d2[(_1d0.type || "decimal") + "Format"];
		var _1d4 = _1d2.group;
		var _1d5 = _1d2.decimal;
		var _1d6 = 1;
		if (_1d3.indexOf("%") != -1) {
			_1d6 /= 100;
		} else {
			if (_1d3.indexOf("\u2030") != -1) {
				_1d6 /= 1000;
			} else {
				var _1d7 = _1d3.indexOf("\xa4") != -1;
				if (_1d7) {
					_1d4 = _1d2.currencyGroup || _1d4;
					_1d5 = _1d2.currencyDecimal || _1d5;
				}
			}
		}
		var _1d8 = _1d3.split(";");
		if (_1d8.length == 1) {
			_1d8.push("-" + _1d8[0]);
		}
		var re = dojo.regexp.buildGroupRE(_1d8, function(_1da) {
			_1da = "(?:" + dojo.regexp.escapeString(_1da, ".") + ")";
			return _1da.replace(dojo.number._numberPatternRE, function(_1db) {
				var _1dc = {
					signed : false,
					separator : _1d0.strict ? _1d4 : [ _1d4, "" ],
					fractional : _1d0.fractional,
					decimal : _1d5,
					exponent : false
				};
				var _1dd = _1db.split(".");
				var _1de = _1d0.places;
				if (_1dd.length == 1 || _1de === 0) {
					_1dc.fractional = false;
				} else {
					if (typeof _1de == "undefined") {
						_1de = _1dd[1].lastIndexOf("0") + 1;
					}
					if (_1de && _1d0.fractional == undefined) {
						_1dc.fractional = true;
					}
					if (!_1d0.places && (_1de < _1dd[1].length)) {
						_1de += "," + _1dd[1].length;
					}
					_1dc.places = _1de;
				}
				var _1df = _1dd[0].split(",");
				if (_1df.length > 1) {
					_1dc.groupSize = _1df.pop().length;
					if (_1df.length > 1) {
						_1dc.groupSize2 = _1df.pop().length;
					}
				}
				return "(" + dojo.number._realNumberRegexp(_1dc) + ")";
			});
		}, true);
		if (_1d7) {
			re = re
					.replace(/(\s*)(\u00a4{1,3})(\s*)/g,
							function(_1e0, _1e1, _1e2, _1e3) {
								var prop = [ "symbol", "currency",
										"displayName" ][_1e2.length - 1];
								var _1e5 = dojo.regexp.escapeString(_1d0[prop]
										|| _1d0.currency || "");
								_1e1 = _1e1 ? "\\s" : "";
								_1e3 = _1e3 ? "\\s" : "";
								if (!_1d0.strict) {
									if (_1e1) {
										_1e1 += "*";
									}
									if (_1e3) {
										_1e3 += "*";
									}
									return "(?:" + _1e1 + _1e5 + _1e3 + ")?";
								}
								return _1e1 + _1e5 + _1e3;
							});
		}
		return {
			regexp : re.replace(/[\xa0 ]/g, "[\\s\\xa0]"),
			group : _1d4,
			decimal : _1d5,
			factor : _1d6
		};
	};
	dojo.number.parse = function(_1e6, _1e7) {
		var info = dojo.number._parseInfo(_1e7);
		var _1e9 = (new RegExp("^" + info.regexp + "$")).exec(_1e6);
		if (!_1e9) {
			return NaN;
		}
		var _1ea = _1e9[1];
		if (!_1e9[1]) {
			if (!_1e9[2]) {
				return NaN;
			}
			_1ea = _1e9[2];
			info.factor *= -1;
		}
		_1ea = _1ea.replace(
				new RegExp("[" + info.group + "\\s\\xa0" + "]", "g"), "")
				.replace(info.decimal, ".");
		return Number(_1ea) * info.factor;
	};
	dojo.number._realNumberRegexp = function(_1eb) {
		_1eb = _1eb || {};
		if (typeof _1eb.places == "undefined") {
			_1eb.places = Infinity;
		}
		if (typeof _1eb.decimal != "string") {
			_1eb.decimal = ".";
		}
		if (typeof _1eb.fractional == "undefined" || /^0/.test(_1eb.places)) {
			_1eb.fractional = [ true, false ];
		}
		if (typeof _1eb.exponent == "undefined") {
			_1eb.exponent = [ true, false ];
		}
		if (typeof _1eb.eSigned == "undefined") {
			_1eb.eSigned = [ true, false ];
		}
		var _1ec = dojo.number._integerRegexp(_1eb);
		var _1ed = dojo.regexp.buildGroupRE(_1eb.fractional, function(q) {
			var re = "";
			if (q && (_1eb.places !== 0)) {
				re = "\\" + _1eb.decimal;
				if (_1eb.places == Infinity) {
					re = "(?:" + re + "\\d+)?";
				} else {
					re += "\\d{" + _1eb.places + "}";
				}
			}
			return re;
		}, true);
		var _1f0 = dojo.regexp.buildGroupRE(_1eb.exponent, function(q) {
			if (q) {
				return "([eE]" + dojo.number._integerRegexp({
					signed : _1eb.eSigned
				}) + ")";
			}
			return "";
		});
		var _1f2 = _1ec + _1ed;
		if (_1ed) {
			_1f2 = "(?:(?:" + _1f2 + ")|(?:" + _1ed + "))";
		}
		return _1f2 + _1f0;
	};
	dojo.number._integerRegexp = function(_1f3) {
		_1f3 = _1f3 || {};
		if (typeof _1f3.signed == "undefined") {
			_1f3.signed = [ true, false ];
		}
		if (typeof _1f3.separator == "undefined") {
			_1f3.separator = "";
		} else {
			if (typeof _1f3.groupSize == "undefined") {
				_1f3.groupSize = 3;
			}
		}
		var _1f4 = dojo.regexp.buildGroupRE(_1f3.signed, function(q) {
			return q ? "[-+]" : "";
		}, true);
		var _1f6 = dojo.regexp.buildGroupRE(_1f3.separator, function(sep) {
			if (!sep) {
				return "(?:0|[1-9]\\d*)";
			}
			sep = dojo.regexp.escapeString(sep);
			if (sep == " ") {
				sep = "\\s";
			} else {
				if (sep == "\xa0") {
					sep = "\\s\\xa0";
				}
			}
			var grp = _1f3.groupSize, grp2 = _1f3.groupSize2;
			if (grp2) {
				var _1fa = "(?:0|[1-9]\\d{0," + (grp2 - 1) + "}(?:[" + sep
						+ "]\\d{" + grp2 + "})*[" + sep + "]\\d{" + grp + "})";
				return ((grp - grp2) > 0) ? "(?:" + _1fa + "|(?:0|[1-9]\\d{0,"
						+ (grp - 1) + "}))" : _1fa;
			}
			return "(?:0|[1-9]\\d{0," + (grp - 1) + "}(?:[" + sep + "]\\d{"
					+ grp + "})*)";
		}, true);
		return _1f4 + _1f6;
	};
}
if (!dojo._hasResource["dojo.cldr.monetary"]) {
	dojo._hasResource["dojo.cldr.monetary"] = true;
	dojo.provide("dojo.cldr.monetary");
	dojo.cldr.monetary.getData = function(code) {
		var _1fc = {
			ADP : 0,
			BHD : 3,
			BIF : 0,
			BYR : 0,
			CLF : 0,
			CLP : 0,
			DJF : 0,
			ESP : 0,
			GNF : 0,
			IQD : 3,
			ITL : 0,
			JOD : 3,
			JPY : 0,
			KMF : 0,
			KRW : 0,
			KWD : 3,
			LUF : 0,
			LYD : 3,
			MGA : 0,
			MGF : 0,
			OMR : 3,
			PYG : 0,
			RWF : 0,
			TND : 3,
			TRL : 0,
			VUV : 0,
			XAF : 0,
			XOF : 0,
			XPF : 0
		};
		var _1fd = {
			CHF : 5
		};
		var _1fe = _1fc[code], _1ff = _1fd[code];
		if (typeof _1fe == "undefined") {
			_1fe = 2;
		}
		if (typeof _1ff == "undefined") {
			_1ff = 0;
		}
		return {
			places : _1fe,
			round : _1ff
		};
	};
}
if (!dojo._hasResource["dojo.currency"]) {
	dojo._hasResource["dojo.currency"] = true;
	dojo.provide("dojo.currency");
	dojo.currency._mixInDefaults = function(_200) {
		_200 = _200 || {};
		_200.type = "currency";
		var _201 = dojo.i18n.getLocalization("dojo.cldr", "currency",
				_200.locale)
				|| {};
		var iso = _200.currency;
		var data = dojo.cldr.monetary.getData(iso);
		dojo.forEach([ "displayName", "symbol", "group", "decimal" ], function(
				prop) {
			data[prop] = _201[iso + "_" + prop];
		});
		data.fractional = [ true, false ];
		return dojo.mixin(data, _200);
	};
	dojo.currency.format = function(_205, _206) {
		return dojo.number.format(_205, dojo.currency._mixInDefaults(_206));
	};
	dojo.currency.regexp = function(_207) {
		return dojo.number.regexp(dojo.currency._mixInDefaults(_207));
	};
	dojo.currency.parse = function(_208, _209) {
		return dojo.number.parse(_208, dojo.currency._mixInDefaults(_209));
	};
}
if (!dojo._hasResource["dojo.data.util.filter"]) {
	dojo._hasResource["dojo.data.util.filter"] = true;
	dojo.provide("dojo.data.util.filter");
	dojo.data.util.filter.patternToRegExp = function(_20a, _20b) {
		var rxp = "^";
		var c = null;
		for ( var i = 0; i < _20a.length; i++) {
			c = _20a.charAt(i);
			switch (c) {
			case "\\":
				rxp += c;
				i++;
				rxp += _20a.charAt(i);
				break;
			case "*":
				rxp += ".*";
				break;
			case "?":
				rxp += ".";
				break;
			case "$":
			case "^":
			case "/":
			case "+":
			case ".":
			case "|":
			case "(":
			case ")":
			case "{":
			case "}":
			case "[":
			case "]":
				rxp += "\\";
			default:
				rxp += c;
			}
		}
		rxp += "$";
		if (_20b) {
			return new RegExp(rxp, "i");
		} else {
			return new RegExp(rxp);
		}
	};
}
if (!dojo._hasResource["dojo.data.util.sorter"]) {
	dojo._hasResource["dojo.data.util.sorter"] = true;
	dojo.provide("dojo.data.util.sorter");
	dojo.data.util.sorter.basicComparator = function(a, b) {
		var ret = 0;
		if (a > b || typeof a === "undefined") {
			ret = 1;
		} else {
			if (a < b || typeof b === "undefined") {
				ret = -1;
			}
		}
		return ret;
	};
	dojo.data.util.sorter.createSortFunction = function(_212, _213) {
		var _214 = [];
		function createSortFunction(attr, dir) {
			return function(_217, _218) {
				var a = _213.getValue(_217, attr);
				var b = _213.getValue(_218, attr);
				var _21b = null;
				if (_213.comparatorMap) {
					if (typeof attr !== "string") {
						attr = _213.getIdentity(attr);
					}
					_21b = _213.comparatorMap[attr]
							|| dojo.data.util.sorter.basicComparator;
				}
				_21b = _21b || dojo.data.util.sorter.basicComparator;
				return dir * _21b(a, b);
			};
		}
		;
		for ( var i = 0; i < _212.length; i++) {
			sortAttribute = _212[i];
			if (sortAttribute.attribute) {
				var _21d = (sortAttribute.descending) ? -1 : 1;
				_214.push(createSortFunction(sortAttribute.attribute, _21d));
			}
		}
		return function(rowA, rowB) {
			var i = 0;
			while (i < _214.length) {
				var ret = _214[i++](rowA, rowB);
				if (ret !== 0) {
					return ret;
				}
			}
			return 0;
		};
	};
}
if (!dojo._hasResource["dojo.data.util.simpleFetch"]) {
	dojo._hasResource["dojo.data.util.simpleFetch"] = true;
	dojo.provide("dojo.data.util.simpleFetch");
	dojo.data.util.simpleFetch.fetch = function(_222) {
		_222 = _222 || {};
		if (!_222.store) {
			_222.store = this;
		}
		var self = this;
		var _224 = function(_225, _226) {
			if (_226.onError) {
				var _227 = _226.scope || dojo.global;
				_226.onError.call(_227, _225, _226);
			}
		};
		var _228 = function(_229, _22a) {
			var _22b = _22a.abort || null;
			var _22c = false;
			var _22d = _22a.start ? _22a.start : 0;
			var _22e = _22a.count ? (_22d + _22a.count) : _229.length;
			_22a.abort = function() {
				_22c = true;
				if (_22b) {
					_22b.call(_22a);
				}
			};
			var _22f = _22a.scope || dojo.global;
			if (!_22a.store) {
				_22a.store = self;
			}
			if (_22a.onBegin) {
				_22a.onBegin.call(_22f, _229.length, _22a);
			}
			if (_22a.sort) {
				_229.sort(dojo.data.util.sorter.createSortFunction(_22a.sort,
						self));
			}
			if (_22a.onItem) {
				for ( var i = _22d; (i < _229.length) && (i < _22e); ++i) {
					var item = _229[i];
					if (!_22c) {
						_22a.onItem.call(_22f, item, _22a);
					}
				}
			}
			if (_22a.onComplete && !_22c) {
				var _232 = null;
				if (!_22a.onItem) {
					_232 = _229.slice(_22d, _22e);
				}
				_22a.onComplete.call(_22f, _232, _22a);
			}
		};
		this._fetchItems(_222, _228, _224);
		return _222;
	};
}
if (!dojo._hasResource["dojo.data.JsonItemStore"]) {
	dojo._hasResource["dojo.data.JsonItemStore"] = true;
	dojo.provide("dojo.data.JsonItemStore");
	dojo
			.declare(
					"dojo.data.JsonItemStore",
					null,
					{
						constructor : function(_233) {
							this._arrayOfAllItems = [];
							this._loadFinished = false;
							this._jsonFileUrl = _233.url;
							this._jsonData = _233.data;
							this._features = {
								"dojo.data.api.Read" : true
							};
							this._itemsByIdentity = null;
							this._storeRef = "_S";
							this._itemId = "_0";
						},
						url : "",
						_assertIsItem : function(item) {
							if (!this.isItem(item)) {
								throw new Error(
										"dojo.data.JsonItemStore: a function was passed an item argument that was not an item");
							}
						},
						_assertIsAttribute : function(_235) {
							if (typeof _235 !== "string") {
								throw new Error(
										"dojo.data.JsonItemStore: a function was passed an attribute argument that was not an attribute name string");
							}
						},
						getValue : function(item, _237, _238) {
							var _239 = this.getValues(item, _237);
							return (_239.length > 0) ? _239[0] : _238;
						},
						getValues : function(item, _23b) {
							this._assertIsItem(item);
							this._assertIsAttribute(_23b);
							return item[_23b] || [];
						},
						getAttributes : function(item) {
							this._assertIsItem(item);
							var _23d = [];
							for ( var key in item) {
								if ((key !== this._storeRef)
										&& (key !== this._itemId)) {
									_23d.push(key);
								}
							}
							return _23d;
						},
						hasAttribute : function(item, _240) {
							return this.getValues(item, _240).length > 0;
						},
						containsValue : function(item, _242, _243) {
							var _244 = undefined;
							if (typeof _243 === "string") {
								_244 = dojo.data.util.filter.patternToRegExp(
										_243, false);
							}
							return this._containsValue(item, _242, _243, _244);
						},
						_containsValue : function(item, _246, _247, _248) {
							var _249 = this.getValues(item, _246);
							for ( var i = 0; i < _249.length; ++i) {
								var _24b = _249[i];
								if (typeof _24b === "string" && _248) {
									return (_24b.match(_248) !== null);
								} else {
									if (_247 === _24b) {
										return true;
									}
								}
							}
							return false;
						},
						isItem : function(_24c) {
							if (_24c && _24c[this._storeRef] === this) {
								if (this._arrayOfAllItems[_24c[this._itemId]] === _24c) {
									return true;
								}
							}
							return false;
						},
						isItemLoaded : function(_24d) {
							return this.isItem(_24d);
						},
						loadItem : function(_24e) {
							this._assertIsItem(_24e.item);
						},
						getFeatures : function() {
							if (!this._loadFinished) {
								this._forceLoad();
							}
							return this._features;
						},
						getLabel : function(item) {
							if (this._labelAttr && this.isItem(item)) {
								return this.getValue(item, this._labelAttr);
							}
							return undefined;
						},
						getLabelAttributes : function(item) {
							if (this._labelAttr) {
								return [ this._labelAttr ];
							}
							return null;
						},
						_fetchItems : function(_251, _252, _253) {
							var self = this;
							var _255 = function(_256, _257) {
								var _258 = null;
								if (_256.query) {
									var _259 = _256.queryOptions ? _256.queryOptions.ignoreCase
											: false;
									_258 = [];
									var _25a = {};
									for ( var key in _256.query) {
										var _25c = _256.query[key];
										if (typeof _25c === "string") {
											_25a[key] = dojo.data.util.filter
													.patternToRegExp(_25c, _259);
										}
									}
									for ( var i = 0; i < _257.length; ++i) {
										var _25e = true;
										var _25f = _257[i];
										for ( var key in _256.query) {
											var _25c = _256.query[key];
											if (!self._containsValue(_25f, key,
													_25c, _25a[key])) {
												_25e = false;
											}
										}
										if (_25e) {
											_258.push(_25f);
										}
									}
									_252(_258, _256);
								} else {
									if (self._arrayOfAllItems.length > 0) {
										_258 = self._arrayOfAllItems.slice(0,
												self._arrayOfAllItems.length);
									}
									_252(_258, _256);
								}
							};
							if (this._loadFinished) {
								_255(_251, this._arrayOfAllItems);
							} else {
								if (this._jsonFileUrl) {
									var _260 = {
										url : self._jsonFileUrl,
										handleAs : "json-comment-optional"
									};
									var _261 = dojo.xhrGet(_260);
									_261
											.addCallback(function(data) {
												self._loadFinished = true;
												try {
													self._arrayOfAllItems = self
															._getItemsFromLoadedData(data);
													_255(
															_251,
															self._arrayOfAllItems);
												} catch (e) {
													_253(e, _251);
												}
											});
									_261.addErrback(function(_263) {
										_253(_263, _251);
									});
								} else {
									if (this._jsonData) {
										try {
											this._loadFinished = true;
											this._arrayOfAllItems = this
													._getItemsFromLoadedData(this._jsonData);
											this._jsonData = null;
											_255(_251, this._arrayOfAllItems);
										} catch (e) {
											_253(e, _251);
										}
									} else {
										_253(
												new Error(
														"dojo.data.JsonItemStore: No JSON source data was provided as either URL or a nested Javascript object."),
												_251);
									}
								}
							}
						},
						close : function(_264) {
						},
						_getItemsFromLoadedData : function(_265) {
							var _266 = _265.items;
							var i;
							var item;
							var _269 = {};
							this._labelAttr = _265.label;
							for (i = 0; i < _266.length; ++i) {
								item = _266[i];
								for ( var key in item) {
									var _26b = item[key];
									if (_26b !== null) {
										if (!dojo.isArray(_26b)) {
											item[key] = [ _26b ];
										}
									} else {
										item[key] = [ null ];
									}
									_269[key] = key;
								}
							}
							while (_269[this._storeRef]) {
								this._storeRef += "_";
							}
							while (_269[this._itemId]) {
								this._itemId += "_";
							}
							var _26c = _265.identifier;
							var _26d = null;
							if (_26c) {
								this._features["dojo.data.api.Identity"] = _26c;
								this._itemsByIdentity = {};
								for ( var i = 0; i < _266.length; ++i) {
									item = _266[i];
									_26d = item[_26c];
									identity = _26d[0];
									if (!this._itemsByIdentity[identity]) {
										this._itemsByIdentity[identity] = item;
									} else {
										if (this._jsonFileUrl) {
											throw new Error(
													"dojo.data.JsonItemStore:  The json data as specified by: ["
															+ this._jsonFileUrl
															+ "] is malformed.  Items within the list have identifier: ["
															+ _26c
															+ "].  Value collided: ["
															+ identity + "]");
										} else {
											if (this._jsonData) {
												throw new Error(
														"dojo.data.JsonItemStore:  The json data provided by the creation arguments is malformed.  Items within the list have identifier: ["
																+ _26c
																+ "].  Value collided: ["
																+ identity
																+ "]");
											}
										}
									}
								}
							}
							for (i = 0; i < _266.length; ++i) {
								item = _266[i];
								item[this._storeRef] = this;
								item[this._itemId] = i;
								for (key in item) {
									_26d = item[key];
									for ( var j = 0; j < _26d.length; ++j) {
										_26b = _26d[j];
										if (_26b !== null
												&& typeof _26b == "object"
												&& _26b.reference) {
											var _26f = _26b.reference;
											if (dojo.isString(_26f)) {
												_26d[j] = this._itemsByIdentity[_26f];
											} else {
												for ( var k = 0; k < _266.length; ++k) {
													var _271 = _266[k];
													var _272 = true;
													for ( var _273 in _26f) {
														if (_271[_273] != _26f[_273]) {
															_272 = false;
														}
													}
													if (_272) {
														_26d[j] = _271;
													}
												}
											}
										}
									}
								}
							}
							return _266;
						},
						getIdentity : function(item) {
							var _275 = this._features["dojo.data.api.Identity"];
							var _276 = item[_275];
							if (_276) {
								return _276[0];
							}
							return null;
						},
						fetchItemByIdentity : function(_277) {
							if (!this._loadFinished) {
								var self = this;
								if (this._jsonFileUrl) {
									var _279 = {
										url : self._jsonFileUrl,
										handleAs : "json-comment-optional"
									};
									var _27a = dojo.xhrGet(_279);
									_27a
											.addCallback(function(data) {
												var _27c = _277.scope ? _277.scope
														: dojo.global;
												try {
													self._arrayOfAllItems = self
															._getItemsFromLoadedData(data);
													self._loadFinished = true;
													var item = self
															._getItemByIdentity(_277.identity);
													if (_277.onItem) {
														_277.onItem.call(_27c,
																item);
													}
												} catch (error) {
													if (_277.onError) {
														_277.onError.call(_27c,
																error);
													}
												}
											});
									_27a.addErrback(function(_27e) {
										if (_277.onError) {
											var _27f = _277.scope ? _277.scope
													: dojo.global;
											_277.onError.call(_27f, _27e);
										}
									});
								} else {
									if (this._jsonData) {
										self._arrayOfAllItems = self
												._getItemsFromLoadedData(self._jsonData);
										self._jsonData = null;
										self._loadFinished = true;
										var item = self
												._getItemByIdentity(_277.identity);
										if (_277.onItem) {
											var _281 = _277.scope ? _277.scope
													: dojo.global;
											_277.onItem.call(_281, item);
										}
									}
								}
							} else {
								var item = this
										._getItemByIdentity(_277.identity);
								if (_277.onItem) {
									var _281 = _277.scope ? _277.scope
											: dojo.global;
									_277.onItem.call(_281, item);
								}
							}
						},
						_getItemByIdentity : function(_282) {
							var item = null;
							if (this._itemsByIdentity) {
								item = this._itemsByIdentity[_282];
								if (item === undefined) {
									item = null;
								}
							}
							return item;
						},
						getIdentityAttributes : function(item) {
							var _285 = this._features["dojo.data.api.Identity"];
							if (_285) {
								return [ _285 ];
							}
							return null;
						},
						_forceLoad : function() {
							var self = this;
							if (this._jsonFileUrl) {
								var _287 = {
									url : self._jsonFileUrl,
									handleAs : "json-comment-optional",
									sync : true
								};
								var _288 = dojo.xhrGet(_287);
								_288.addCallback(function(data) {
									try {
										self._arrayOfAllItems = self
												._getItemsFromLoadedData(data);
										self._loadFinished = true;
									} catch (e) {
										console.log(e);
										throw e;
									}
								});
								_288.addErrback(function(_28a) {
									throw _28a;
								});
							} else {
								if (this._jsonData) {
									self._arrayOfAllItems = self
											._getItemsFromLoadedData(self._jsonData);
									self._jsonData = null;
									self._loadFinished = true;
								}
							}
						}
					});
	dojo.extend(dojo.data.JsonItemStore, dojo.data.util.simpleFetch);
}
if (!dojo._hasResource["dojo.data.JsonItemStoreAutoComplete"]) {
	dojo._hasResource["dojo.data.JsonItemStoreAutoComplete"] = true;
	dojo.provide("dojo.data.JsonItemStoreAutoComplete");
	dojo
			.declare(
					"dojo.data.JsonItemStoreAutoComplete",
					dojo.data.JsonItemStore,
					{
						_fetchItems : function(_28b, _28c, _28d) {
							var self = this;
							var _28f = function(_290, _291) {
								var _292 = null;
								if (_290.query) {
									var _293 = _290.queryOptions ? _290.queryOptions.ignoreCase
											: false;
									_292 = [];
									var _294 = {};
									for ( var key in _290.query) {
										var _296 = _290.query[key];
										if (typeof _296 === "string") {
											_294[key] = dojo.data.util.filter
													.patternToRegExp(_296, _293);
										}
									}
									for ( var i = 0; i < _291.length; ++i) {
										var _298 = true;
										var _299 = _291[i];
										if (_298) {
											_292.push(_299);
										}
									}
									_28c(_292, _290);
								} else {
									if (self._arrayOfAllItems.length > 0) {
										_292 = self._arrayOfAllItems.slice(0,
												self._arrayOfAllItems.length);
									}
									_28c(_292, _290);
								}
							};
							var _29a = null;
							if (_28b.query) {
								_29a = _28b.query["name"];
							}
							if (_29a) {
								var pos = _29a.lastIndexOf("*");
								_29a = _29a.substr(0, pos);
							}
							if (_29a) {
								if (this._jsonFileUrl) {
									var _29c = {
										url : self._jsonFileUrl + "?keyword="
												+ _29a,
										handleAs : "json-comment-optional"
									};
									var _29d = dojo.xhrGet(_29c);
									_29d
											.addCallback(function(data) {
												self._loadFinished = true;
												try {
													self._arrayOfAllItems = self
															._getItemsFromLoadedData(data);
													_28f(
															_28b,
															self._arrayOfAllItems);
												} catch (e) {
													_28d(e, _28b);
												}
											});
									_29d.addErrback(function(_29f) {
										_28d(_29f, _28b);
									});
								} else {
									if (this._jsonData) {
										try {
											this._loadFinished = true;
											this._arrayOfAllItems = this
													._getItemsFromLoadedData(this._jsonData);
											this._jsonData = null;
											_28f(_28b, this._arrayOfAllItems);
										} catch (e) {
											_28d(e, _28b);
										}
									} else {
										_28d(
												new Error(
														"dojo.data.JsonItemStoreAutoComplete: No JSON source data was provided as either URL or a nested Javascript object."),
												_28b);
									}
								}
							}
						}
					});
	dojo
			.extend(dojo.data.JsonItemStoreAutoComplete,
					dojo.data.util.simpleFetch);
}
if (!dojo._hasResource["dijit._base.focus"]) {
	dojo._hasResource["dijit._base.focus"] = true;
	dojo.provide("dijit._base.focus");
	dojo
			.mixin(
					dijit,
					{
						_curFocus : null,
						_prevFocus : null,
						isCollapsed : function() {
							var _2a0 = dojo.global;
							var _2a1 = dojo.doc;
							if (_2a1.selection) {
								return !_2a1.selection.createRange().text;
							} else {
								if (_2a0.getSelection) {
									var _2a2 = _2a0.getSelection();
									if (dojo.isString(_2a2)) {
										return !_2a2;
									} else {
										return _2a2.isCollapsed
												|| !_2a2.toString();
									}
								}
							}
						},
						getBookmark : function() {
							var _2a3, _2a4 = dojo.doc.selection;
							if (_2a4) {
								var _2a5 = _2a4.createRange();
								if (_2a4.type.toUpperCase() == "CONTROL") {
									_2a3 = _2a5.length ? dojo._toArray(_2a5)
											: null;
								} else {
									_2a3 = _2a5.getBookmark();
								}
							} else {
								if (dojo.global.getSelection) {
									_2a4 = dojo.global.getSelection();
									if (_2a4) {
										var _2a5 = _2a4.getRangeAt(0);
										_2a3 = _2a5.cloneRange();
									}
								} else {
									console
											.debug("No idea how to store the current selection for this browser!");
								}
							}
							return _2a3;
						},
						moveToBookmark : function(_2a6) {
							var _2a7 = dojo.doc;
							if (_2a7.selection) {
								var _2a8;
								if (dojo.isArray(_2a6)) {
									_2a8 = _2a7.body.createControlRange();
									dojo.forEach(_2a6, _2a8.addElement);
								} else {
									_2a8 = _2a7.selection.createRange();
									_2a8.moveToBookmark(_2a6);
								}
								_2a8.select();
							} else {
								var _2a9 = dojo.global.getSelection
										&& dojo.global.getSelection();
								if (_2a9 && _2a9.removeAllRanges) {
									_2a9.removeAllRanges();
									_2a9.addRange(_2a6);
								} else {
									console
											.debug("No idea how to restore selection for this browser!");
								}
							}
						},
						getFocus : function(menu, _2ab) {
							return {
								node : menu
										&& dojo.isDescendant(dijit._curFocus,
												menu.domNode) ? dijit._prevFocus
										: dijit._curFocus,
								bookmark : !dojo.withGlobal(
										_2ab || dojo.global, dijit.isCollapsed) ? dojo
										.withGlobal(_2ab || dojo.global,
												dijit.getBookmark)
										: null,
								openedForWindow : _2ab
							};
						},
						focus : function(_2ac) {
							if (!_2ac) {
								return;
							}
							var node = "node" in _2ac ? _2ac.node : _2ac, _2ae = _2ac.bookmark, _2af = _2ac.openedForWindow;
							if (node) {
								var _2b0 = (node.tagName.toLowerCase() == "iframe") ? node.contentWindow
										: node;
								if (_2b0 && _2b0.focus) {
									try {
										_2b0.focus();
									} catch (e) {
									}
								}
								dijit._onFocusNode(node);
							}
							if (_2ae
									&& dojo.withGlobal(_2af || dojo.global,
											dijit.isCollapsed)) {
								if (_2af) {
									_2af.focus();
								}
								try {
									dojo.withGlobal(_2af || dojo.global,
											moveToBookmark, null, [ _2ae ]);
								} catch (e) {
								}
							}
						},
						_activeStack : [],
						registerWin : function(_2b1) {
							if (!_2b1) {
								_2b1 = window;
							}
							dojo.connect(_2b1.document, "onmousedown", null,
									function(evt) {
										dijit._ignoreNextBlurEvent = true;
										setTimeout(function() {
											dijit._ignoreNextBlurEvent = false;
										}, 0);
										dijit._onTouchNode(evt.target
												|| evt.srcElement);
									});
							var body = _2b1.document.body
									|| _2b1.document
											.getElementsByTagName("body")[0];
							if (body) {
								if (dojo.isIE) {
									body
											.attachEvent(
													"onactivate",
													function(evt) {
														if (evt.srcElement.tagName
																.toLowerCase() != "body") {
															dijit
																	._onFocusNode(evt.srcElement);
														}
													});
									body.attachEvent("ondeactivate", function(
											evt) {
										dijit._onBlurNode();
									});
								} else {
									body.addEventListener("focus",
											function(evt) {
												dijit._onFocusNode(evt.target);
											}, true);
									body.addEventListener("blur",
											function(evt) {
												dijit._onBlurNode();
											}, true);
								}
							}
						},
						_onBlurNode : function() {
							if (dijit._ignoreNextBlurEvent) {
								dijit._ignoreNextBlurEvent = false;
								return;
							}
							dijit._prevFocus = dijit._curFocus;
							dijit._curFocus = null;
							if (dijit._blurAllTimer) {
								clearTimeout(dijit._blurAllTimer);
							}
							dijit._blurAllTimer = setTimeout(function() {
								delete dijit._blurAllTimer;
								dijit._setStack([]);
							}, 100);
						},
						_onTouchNode : function(node) {
							if (dijit._blurAllTimer) {
								clearTimeout(dijit._blurAllTimer);
								delete dijit._blurAllTimer;
							}
							var _2b9 = [];
							try {
								while (node) {
									if (node.dijitPopupParent) {
										node = dijit
												.byId(node.dijitPopupParent).domNode;
									} else {
										if (node.tagName
												&& node.tagName.toLowerCase() == "body") {
											if (node === dojo.body()) {
												break;
											}
											node = dojo
													.query("iframe")
													.filter(
															function(_2ba) {
																return _2ba.contentDocument.body === node;
															})[0];
										} else {
											var id = node.getAttribute
													&& node
															.getAttribute("widgetId");
											if (id) {
												_2b9.unshift(id);
											}
											node = node.parentNode;
										}
									}
								}
							} catch (e) {
							}
							dijit._setStack(_2b9);
						},
						_onFocusNode : function(node) {
							if (node && node.tagName
									&& node.tagName.toLowerCase() == "body") {
								return;
							}
							dijit._onTouchNode(node);
							if (node == dijit._curFocus) {
								return;
							}
							dijit._prevFocus = dijit._curFocus;
							dijit._curFocus = node;
							dojo.publish("focusNode", [ node ]);
							var w = dijit.byId(node.id);
							if (w && w._setStateClass) {
								w._focused = true;
								w._setStateClass();
								var _2be = dojo.connect(node, "onblur",
										function() {
											w._focused = false;
											w._setStateClass();
											dojo.disconnect(_2be);
										});
							}
						},
						_setStack : function(_2bf) {
							var _2c0 = dijit._activeStack;
							dijit._activeStack = _2bf;
							for ( var _2c1 = 0; _2c1 < Math.min(_2c0.length,
									_2bf.length); _2c1++) {
								if (_2c0[_2c1] != _2bf[_2c1]) {
									break;
								}
							}
							for ( var i = _2c0.length - 1; i >= _2c1; i--) {
								var _2c3 = dijit.byId(_2c0[i]);
								if (_2c3) {
									dojo.publish("widgetBlur", [ _2c3 ]);
									if (_2c3._onBlur) {
										_2c3._onBlur();
									}
								}
							}
							for ( var i = _2c1; i < _2bf.length; i++) {
								var _2c3 = dijit.byId(_2bf[i]);
								if (_2c3) {
									dojo.publish("widgetFocus", [ _2c3 ]);
									if (_2c3._onFocus) {
										_2c3._onFocus();
									}
								}
							}
						}
					});
	dojo.addOnLoad(dijit.registerWin);
}
if (!dojo._hasResource["dijit._base.manager"]) {
	dojo._hasResource["dijit._base.manager"] = true;
	dojo.provide("dijit._base.manager");
	dojo.declare("dijit.WidgetSet", null, {
		constructor : function() {
			this._hash = {};
		},
		add : function(_2c4) {
			this._hash[_2c4.id] = _2c4;
		},
		remove : function(id) {
			delete this._hash[id];
		},
		forEach : function(func) {
			for ( var id in this._hash) {
				func(this._hash[id]);
			}
		},
		filter : function(_2c8) {
			var res = new dijit.WidgetSet();
			this.forEach(function(_2ca) {
				if (_2c8(_2ca)) {
					res.add(_2ca);
				}
			});
			return res;
		},
		byId : function(id) {
			return this._hash[id];
		},
		byClass : function(cls) {
			return this.filter(function(_2cd) {
				return _2cd.declaredClass == cls;
			});
		}
	});
	dijit.registry = new dijit.WidgetSet();
	dijit._widgetTypeCtr = {};
	dijit.getUniqueId = function(_2ce) {
		var id;
		do {
			id = _2ce
					+ "_"
					+ (dijit._widgetTypeCtr[_2ce] !== undefined ? ++dijit._widgetTypeCtr[_2ce]
							: dijit._widgetTypeCtr[_2ce] = 0);
		} while (dijit.byId(id));
		return id;
	};
	if (dojo.isIE) {
		dojo.addOnUnload(function() {
			dijit.registry.forEach(function(_2d0) {
				_2d0.destroy();
			});
		});
	}
	dijit.byId = function(id) {
		return (dojo.isString(id)) ? dijit.registry.byId(id) : id;
	};
	dijit.byNode = function(node) {
		return dijit.registry.byId(node.getAttribute("widgetId"));
	};
}
if (!dojo._hasResource["dijit._base.place"]) {
	dojo._hasResource["dijit._base.place"] = true;
	dojo.provide("dijit._base.place");
	dijit.getViewport = function() {
		var _2d3 = dojo.global;
		var _2d4 = dojo.doc;
		var w = 0, h = 0;
		if (dojo.isMozilla) {
			w = _2d4.documentElement.clientWidth;
			h = _2d3.innerHeight;
		} else {
			if (!dojo.isOpera && _2d3.innerWidth) {
				w = _2d3.innerWidth;
				h = _2d3.innerHeight;
			} else {
				if (dojo.isIE && _2d4.documentElement
						&& _2d4.documentElement.clientHeight) {
					w = _2d4.documentElement.clientWidth;
					h = _2d4.documentElement.clientHeight;
				} else {
					if (dojo.body().clientWidth) {
						w = dojo.body().clientWidth;
						h = dojo.body().clientHeight;
					}
				}
			}
		}
		var _2d7 = dojo._docScroll();
		return {
			w : w,
			h : h,
			l : _2d7.x,
			t : _2d7.y
		};
	};
	dijit.placeOnScreen = function(node, pos, _2da, _2db) {
		var _2dc = dojo.map(_2da, function(_2dd) {
			return {
				corner : _2dd,
				pos : pos
			};
		});
		return dijit._place(node, _2dc);
	};
	dijit._place = function(node, _2df, _2e0) {
		var view = dijit.getViewport();
		if (!node.parentNode
				|| String(node.parentNode.tagName).toLowerCase() != "body") {
			dojo.body().appendChild(node);
		}
		var best = null;
		for ( var i = 0; i < _2df.length; i++) {
			var _2e4 = _2df[i].corner;
			var pos = _2df[i].pos;
			if (_2e0) {
				_2e0(_2e4);
			}
			var _2e6 = node.style.display;
			var _2e7 = node.style.visibility;
			node.style.visibility = "hidden";
			node.style.display = "";
			var mb = dojo.marginBox(node);
			node.style.display = _2e6;
			node.style.visibility = _2e7;
			var _2e9 = (_2e4.charAt(1) == "L" ? pos.x : Math.max(view.l, pos.x
					- mb.w)), _2ea = (_2e4.charAt(0) == "T" ? pos.y : Math.max(
					view.t, pos.y - mb.h)), endX = (_2e4.charAt(1) == "L" ? Math
					.min(view.l + view.w, _2e9 + mb.w)
					: pos.x), endY = (_2e4.charAt(0) == "T" ? Math.min(view.t
					+ view.h, _2ea + mb.h) : pos.y), _2ed = endX - _2e9, _2ee = endY
					- _2ea, _2ef = (mb.w - _2ed) + (mb.h - _2ee);
			if (best == null || _2ef < best.overflow) {
				best = {
					corner : _2e4,
					aroundCorner : _2df[i].aroundCorner,
					x : _2e9,
					y : _2ea,
					w : _2ed,
					h : _2ee,
					overflow : _2ef
				};
			}
			if (_2ef == 0) {
				break;
			}
		}
		node.style.left = best.x + "px";
		node.style.top = best.y + "px";
		return best;
	};
	dijit.placeOnScreenAroundElement = function(node, _2f1, _2f2, _2f3) {
		_2f1 = dojo.byId(_2f1);
		var _2f4 = _2f1.style.display;
		_2f1.style.display = "";
		var _2f5 = _2f1.offsetWidth;
		var _2f6 = _2f1.offsetHeight;
		var _2f7 = dojo.coords(_2f1, true);
		_2f1.style.display = _2f4;
		var _2f8 = [];
		for ( var _2f9 in _2f2) {
			_2f8.push({
				aroundCorner : _2f9,
				corner : _2f2[_2f9],
				pos : {
					x : _2f7.x + (_2f9.charAt(1) == "L" ? 0 : _2f5),
					y : _2f7.y + (_2f9.charAt(0) == "T" ? 0 : _2f6)
				}
			});
		}
		return dijit._place(node, _2f8, _2f3);
	};
}
if (!dojo._hasResource["dijit._base.window"]) {
	dojo._hasResource["dijit._base.window"] = true;
	dojo.provide("dijit._base.window");
	dijit.getDocumentWindow = function(doc) {
		if (dojo.isSafari && !doc._parentWindow) {
			var fix = function(win) {
				win.document._parentWindow = win;
				for ( var i = 0; i < win.frames.length; i++) {
					fix(win.frames[i]);
				}
			};
			fix(window.top);
		}
		if (dojo.isIE && window !== document.parentWindow && !doc._parentWindow) {
			doc.parentWindow.execScript("document._parentWindow = window;",
					"Javascript");
			var win = doc._parentWindow;
			doc._parentWindow = null;
			return win;
		}
		return doc._parentWindow || doc.parentWindow || doc.defaultView;
	};
}
if (!dojo._hasResource["dijit._base.popup"]) {
	dojo._hasResource["dijit._base.popup"] = true;
	dojo.provide("dijit._base.popup");
	dijit.popup = new function() {
		var _2ff = [], _300 = 1000, _301 = 1;
		this.open = function(args) {
			var _303 = args.popup, _304 = args.orient || {
				"BL" : "TL",
				"TL" : "BL"
			}, _305 = args.around, id = (args.around && args.around.id) ? (args.around.id + "_dropdown")
					: ("popup_" + _301++);
			if (!args.submenu) {
				this.closeAll();
			}
			var _307 = dojo.doc.createElement("div");
			_307.id = id;
			_307.className = "dijitPopup";
			_307.style.zIndex = _300 + _2ff.length;
			if (args.parent) {
				_307.dijitPopupParent = args.parent.id;
			}
			dojo.body().appendChild(_307);
			_303.domNode.style.display = "";
			_307.appendChild(_303.domNode);
			var _308 = new dijit.BackgroundIframe(_307);
			var best = _305 ? dijit.placeOnScreenAroundElement(_307, _305,
					_304, _303.orient ? dojo.hitch(_303, "orient") : null)
					: dijit.placeOnScreen(_307, args, _304 == "R" ? [ "TR",
							"BR", "TL", "BL" ] : [ "TL", "BL", "TR", "BR" ]);
			var _30a = [];
			_30a.push(dojo.connect(_307, "onkeypress", this, function(evt) {
				if (evt.keyCode == dojo.keys.ESCAPE) {
					args.onCancel();
				}
			}));
			if (_303.onCancel) {
				_30a.push(dojo.connect(_303, "onCancel", null, args.onCancel));
			}
			_30a.push(dojo.connect(_303, _303.onExecute ? "onExecute"
					: "onChange", null, function() {
				if (_2ff[0] && _2ff[0].onExecute) {
					_2ff[0].onExecute();
				}
			}));
			_2ff.push({
				wrapper : _307,
				iframe : _308,
				widget : _303,
				onExecute : args.onExecute,
				onCancel : args.onCancel,
				onClose : args.onClose,
				handlers : _30a
			});
			if (_303.onOpen) {
				_303.onOpen(best);
			}
			return best;
		};
		this.close = function() {
			var _30c = _2ff[_2ff.length - 1].widget;
			if (_30c.onClose) {
				_30c.onClose();
			}
			if (!_2ff.length) {
				return;
			}
			var top = _2ff.pop();
			var _30e = top.wrapper, _30f = top.iframe, _30c = top.widget, _310 = top.onClose;
			dojo.forEach(top.handlers, dojo.disconnect);
			if (!_30c || !_30c.domNode) {
				return;
			}
			dojo.style(_30c.domNode, "display", "none");
			dojo.body().appendChild(_30c.domNode);
			_30f.destroy();
			dojo._destroyElement(_30e);
			if (_310) {
				_310();
			}
		};
		this.closeAll = function() {
			while (_2ff.length) {
				this.close();
			}
		};
		this.closeTo = function(_311) {
			while (_2ff.length && _2ff[_2ff.length - 1].widget.id != _311.id) {
				this.close();
			}
		};
	}();
	dijit._frames = new function() {
		var _312 = [];
		this.pop = function() {
			var _313;
			if (_312.length) {
				_313 = _312.pop();
				_313.style.display = "";
			} else {
				if (dojo.isIE) {
					var html = "<iframe src='javascript:\"\"'"
							+ " style='position: absolute; left: 0px; top: 0px;"
							+ "z-index: -1; filter:Alpha(Opacity=\"0\");'>";
					_313 = dojo.doc.createElement(html);
				} else {
					var _313 = dojo.doc.createElement("iframe");
					_313.src = "javascript:\"\"";
					_313.className = "dijitBackgroundIframe";
				}
				_313.tabIndex = -1;
				dojo.body().appendChild(_313);
			}
			return _313;
		};
		this.push = function(_315) {
			_315.style.display = "";
			if (dojo.isIE) {
				_315.style.removeExpression("width");
				_315.style.removeExpression("height");
			}
			_312.push(_315);
		};
	}();
	if (dojo.isIE && dojo.isIE < 7) {
		dojo.addOnLoad(function() {
			var f = dijit._frames;
			dojo.forEach([ f.pop() ], f.push);
		});
	}
	dijit.BackgroundIframe = function(node) {
		if (!node.id) {
			throw new Error("no id");
		}
		if ((dojo.isIE && dojo.isIE < 7)
				|| (dojo.isFF && dojo.isFF < 3 && dojo.hasClass(dojo.body(),
						"dijit_a11y"))) {
			var _318 = dijit._frames.pop();
			node.appendChild(_318);
			if (dojo.isIE) {
				_318.style.setExpression("width", "document.getElementById('"
						+ node.id + "').offsetWidth");
				_318.style.setExpression("height", "document.getElementById('"
						+ node.id + "').offsetHeight");
			}
			this.iframe = _318;
		}
	};
	dojo.extend(dijit.BackgroundIframe, {
		destroy : function() {
			if (this.iframe) {
				dijit._frames.push(this.iframe);
				delete this.iframe;
			}
		}
	});
}
if (!dojo._hasResource["dijit._base.scroll"]) {
	dojo._hasResource["dijit._base.scroll"] = true;
	dojo.provide("dijit._base.scroll");
	dijit.scrollIntoView = function(node) {
		if (dojo.isIE) {
			if (dojo.marginBox(node.parentNode).h <= node.parentNode.scrollHeight) {
				node.scrollIntoView(false);
			}
		} else {
			if (dojo.isMozilla) {
				node.scrollIntoView(false);
			} else {
				var _31a = node.parentNode;
				var _31b = _31a.scrollTop + dojo.marginBox(_31a).h;
				var _31c = node.offsetTop + dojo.marginBox(node).h;
				if (_31b < _31c) {
					_31a.scrollTop += (_31c - _31b);
				} else {
					if (_31a.scrollTop > node.offsetTop) {
						_31a.scrollTop -= (_31a.scrollTop - node.offsetTop);
					}
				}
			}
		}
	};
}
if (!dojo._hasResource["dijit._base.sniff"]) {
	dojo._hasResource["dijit._base.sniff"] = true;
	dojo.provide("dijit._base.sniff");
	(function() {
		var d = dojo;
		var ie = d.isIE;
		var _31f = d.isOpera;
		var maj = Math.floor;
		var _321 = {
			dj_ie : ie,
			dj_ie6 : maj(ie) == 6,
			dj_ie7 : maj(ie) == 7,
			dj_iequirks : ie && d.isQuirks,
			dj_opera : _31f,
			dj_opera8 : maj(_31f) == 8,
			dj_opera9 : maj(_31f) == 9,
			dj_khtml : d.isKhtml,
			dj_safari : d.isSafari,
			dj_gecko : d.isMozilla
		};
		for ( var p in _321) {
			if (_321[p]) {
				var html = dojo.doc.documentElement;
				if (html.className) {
					html.className += " " + p;
				} else {
					html.className = p;
				}
			}
		}
	})();
}
if (!dojo._hasResource["dijit._base.bidi"]) {
	dojo._hasResource["dijit._base.bidi"] = true;
	dojo.provide("dijit._base.bidi");
	dojo.addOnLoad(function() {
		if (!dojo._isBodyLtr()) {
			dojo.addClass(dojo.body(), "dijitRtl");
		}
	});
}
if (!dojo._hasResource["dijit._base.typematic"]) {
	dojo._hasResource["dijit._base.typematic"] = true;
	dojo.provide("dijit._base.typematic");
	dijit.typematic = {
		_fireEventAndReload : function() {
			this._timer = null;
			this._callback(++this._count, this._node, this._evt);
			this._currentTimeout = (this._currentTimeout < 0) ? this._initialDelay
					: ((this._subsequentDelay > 1) ? this._subsequentDelay
							: Math.round(this._currentTimeout
									* this._subsequentDelay));
			this._timer = setTimeout(dojo.hitch(this, "_fireEventAndReload"),
					this._currentTimeout);
		},
		trigger : function(evt, _325, node, _327, obj, _329, _32a) {
			if (obj != this._obj) {
				this.stop();
				this._initialDelay = _32a ? _32a : 500;
				this._subsequentDelay = _329 ? _329 : 0.9;
				this._obj = obj;
				this._evt = evt;
				this._node = node;
				this._currentTimeout = -1;
				this._count = -1;
				this._callback = dojo.hitch(_325, _327);
				this._fireEventAndReload();
			}
		},
		stop : function() {
			if (this._timer) {
				clearTimeout(this._timer);
				this._timer = null;
			}
			if (this._obj) {
				this._callback(-1, this._node, this._evt);
				this._obj = null;
			}
		},
		addKeyListener : function(node, _32c, _32d, _32e, _32f, _330) {
			var ary = [];
			ary
					.push(dojo
							.connect(
									node,
									"onkeypress",
									this,
									function(evt) {
										if (evt.keyCode == _32c.keyCode
												&& (!_32c.charCode || _32c.charCode == evt.charCode)
												&& ((typeof _32c.ctrlKey == "undefined") || _32c.ctrlKey == evt.ctrlKey)
												&& ((typeof _32c.altKey == "undefined") || _32c.altKey == evt.ctrlKey)
												&& ((typeof _32c.shiftKey == "undefined") || _32c.shiftKey == evt.ctrlKey)) {
											dojo.stopEvent(evt);
											dijit.typematic.trigger(_32c, _32d,
													node, _32e, _32c, _32f,
													_330);
										} else {
											if (dijit.typematic._obj == _32c) {
												dijit.typematic.stop();
											}
										}
									}));
			ary.push(dojo.connect(node, "onkeyup", this, function(evt) {
				if (dijit.typematic._obj == _32c) {
					dijit.typematic.stop();
				}
			}));
			return ary;
		},
		addMouseListener : function(node, _335, _336, _337, _338) {
			var ary = [];
			ary.push(dojo.connect(node, "mousedown", this, function(evt) {
				dojo.stopEvent(evt);
				dijit.typematic
						.trigger(evt, _335, node, _336, node, _337, _338);
			}));
			ary.push(dojo.connect(node, "mouseup", this, function(evt) {
				dojo.stopEvent(evt);
				dijit.typematic.stop();
			}));
			ary.push(dojo.connect(node, "mouseout", this, function(evt) {
				dojo.stopEvent(evt);
				dijit.typematic.stop();
			}));
			ary.push(dojo.connect(node, "mousemove", this, function(evt) {
				dojo.stopEvent(evt);
			}));
			ary.push(dojo.connect(node, "dblclick", this, function(evt) {
				dojo.stopEvent(evt);
				if (dojo.isIE) {
					dijit.typematic.trigger(evt, _335, node, _336, node, _337,
							_338);
					setTimeout("dijit.typematic.stop()", 50);
				}
			}));
			return ary;
		},
		addListener : function(_33f, _340, _341, _342, _343, _344, _345) {
			return this
					.addKeyListener(_340, _341, _342, _343, _344, _345)
					.concat(this.addMouseListener(_33f, _342, _343, _344, _345));
		}
	};
}
if (!dojo._hasResource["dijit._base.wai"]) {
	dojo._hasResource["dijit._base.wai"] = true;
	dojo.provide("dijit._base.wai");
	dijit.waiNames = [ "waiRole", "waiState" ];
	dijit.wai = {
		waiRole : {
			name : "waiRole",
			"namespace" : "http://www.w3.org/TR/xhtml2",
			alias : "x2",
			prefix : "wairole:"
		},
		waiState : {
			name : "waiState",
			"namespace" : "http://www.w3.org/2005/07/aaa",
			alias : "aaa",
			prefix : ""
		},
		setAttr : function(node, ns, attr, _349) {
			if (dojo.isIE) {
				node.setAttribute(this[ns].alias + ":" + attr, this[ns].prefix
						+ _349);
			} else {
				node.setAttributeNS(this[ns]["namespace"], attr,
						this[ns].prefix + _349);
			}
		},
		getAttr : function(node, ns, attr) {
			if (dojo.isIE) {
				return node.getAttribute(this[ns].alias + ":" + attr);
			} else {
				return node.getAttributeNS(this[ns]["namespace"], attr);
			}
		},
		removeAttr : function(node, ns, attr) {
			var _350 = true;
			if (dojo.isIE) {
				_350 = node.removeAttribute(this[ns].alias + ":" + attr);
			} else {
				node.removeAttributeNS(this[ns]["namespace"], attr);
			}
			return _350;
		},
		onload : function() {
			var div = document.createElement("div");
			div.id = "a11yTestNode";
			div.style.cssText = "border: 1px solid;"
					+ "border-color:red green;" + "position: absolute;"
					+ "left: -999px;" + "top: -999px;"
					+ "background-image: url(\""
					+ dojo.moduleUrl("dijit", "form/templates/blank.gif")
					+ "\");";
			dojo.body().appendChild(div);
			function check() {
				var cs = dojo.getComputedStyle(div);
				if (cs) {
					var _353 = cs.backgroundImage;
					var _354 = (cs.borderTopColor == cs.borderRightColor)
							|| (_353 != null && (_353 == "none" || _353 == "url(invalid-url:)"));
					dojo[_354 ? "addClass" : "removeClass"](dojo.body(),
							"dijit_a11y");
				}
			}
			;
			check();
			if (dojo.isIE) {
				setInterval(check, 4000);
			}
		}
	};
	if (dojo.isIE || dojo.isMoz) {
		dojo._loaders.unshift(dijit.wai.onload);
	}
}
if (!dojo._hasResource["dijit._base"]) {
	dojo._hasResource["dijit._base"] = true;
	dojo.provide("dijit._base");
}
if (!dojo._hasResource["dijit._Widget"]) {
	dojo._hasResource["dijit._Widget"] = true;
	dojo.provide("dijit._Widget");
	dojo
			.declare(
					"dijit._Widget",
					null,
					{
						constructor : function(_355, _356) {
							this.create(_355, _356);
						},
						id : "",
						lang : "",
						dir : "",
						srcNodeRef : null,
						domNode : null,
						create : function(_357, _358) {
							this.srcNodeRef = dojo.byId(_358);
							this._connects = [];
							this._attaches = [];
							if (this.srcNodeRef
									&& (typeof this.srcNodeRef.id == "string")) {
								this.id = this.srcNodeRef.id;
							}
							if (_357) {
								dojo.mixin(this, _357);
							}
							this.postMixInProperties();
							if (!this.id) {
								this.id = dijit.getUniqueId(this.declaredClass
										.replace(/\./g, "_"));
							}
							dijit.registry.add(this);
							this.buildRendering();
							if (this.domNode) {
								this.domNode.setAttribute("widgetId", this.id);
								if (this.srcNodeRef && this.srcNodeRef.dir) {
									this.domNode.dir = this.srcNodeRef.dir;
								}
							}
							this.postCreate();
							if (this.srcNodeRef && !this.srcNodeRef.parentNode) {
								delete this.srcNodeRef;
							}
						},
						postMixInProperties : function() {
						},
						buildRendering : function() {
							this.domNode = this.srcNodeRef;
						},
						postCreate : function() {
						},
						startup : function() {
						},
						destroyRecursive : function(_359) {
							this.destroyDescendants();
							this.destroy();
						},
						destroy : function(_35a) {
							this.uninitialize();
							dojo.forEach(this._connects, function(_35b) {
								dojo.forEach(_35b, dojo.disconnect);
							});
							this.destroyRendering(_35a);
							dijit.registry.remove(this.id);
						},
						destroyRendering : function(_35c) {
							if (this.bgIframe) {
								this.bgIframe.destroy();
								delete this.bgIframe;
							}
							if (this.domNode) {
								dojo._destroyElement(this.domNode);
								delete this.domNode;
							}
							if (this.srcNodeRef) {
								dojo._destroyElement(this.srcNodeRef);
								delete this.srcNodeRef;
							}
						},
						destroyDescendants : function() {
							dojo.forEach(this.getDescendants(), function(_35d) {
								_35d.destroy();
							});
						},
						uninitialize : function() {
							return false;
						},
						toString : function() {
							return "[Widget " + this.declaredClass + ", "
									+ (this.id || "NO ID") + "]";
						},
						getDescendants : function() {
							var list = dojo.query("[widgetId]", this.domNode);
							return list.map(dijit.byNode);
						},
						nodesWithKeyClick : [ "input", "button" ],
						connect : function(obj, _360, _361) {
							var _362 = [];
							if (_360 == "ondijitclick") {
								var w = this;
								if (!this.nodesWithKeyClick[obj.nodeName]) {
									_362
											.push(dojo
													.connect(
															obj,
															"onkeydown",
															this,
															function(e) {
																if (e.keyCode == dojo.keys.ENTER) {
																	return (dojo
																			.isString(_361)) ? w[_361]
																			(e)
																			: _361
																					.call(
																							w,
																							e);
																} else {
																	if (e.keyCode == dojo.keys.SPACE) {
																		dojo
																				.stopEvent(e);
																	}
																}
															}));
									_362
											.push(dojo
													.connect(
															obj,
															"onkeyup",
															this,
															function(e) {
																if (e.keyCode == dojo.keys.SPACE) {
																	return dojo
																			.isString(_361) ? w[_361]
																			(e)
																			: _361
																					.call(
																							w,
																							e);
																}
															}));
								}
								_360 = "onclick";
							}
							_362.push(dojo.connect(obj, _360, this, _361));
							this._connects.push(_362);
							return _362;
						},
						disconnect : function(_366) {
							for ( var i = 0; i < this._connects.length; i++) {
								if (this._connects[i] == _366) {
									dojo.forEach(_366, dojo.disconnect);
									this._connects.splice(i, 1);
									return;
								}
							}
						},
						isLeftToRight : function() {
							if (typeof this._ltr == "undefined") {
								this._ltr = (this.dir || dojo
										.getComputedStyle(this.domNode).direction) != "rtl";
							}
							return this._ltr;
						}
					});
}
if (!dojo._hasResource["dijit._Templated"]) {
	dojo._hasResource["dijit._Templated"] = true;
	dojo.provide("dijit._Templated");
	dojo.declare("dijit._Templated", null, {
		templateNode : null,
		templateString : null,
		templatePath : null,
		widgetsInTemplate : false,
		containerNode : null,
		buildRendering : function() {
			var _368 = dijit._Templated.getCachedTemplate(this.templatePath,
					this.templateString);
			var node;
			if (dojo.isString(_368)) {
				var _36a = this.declaredClass, _36b = this;
				var tstr = dojo.string.substitute(_368, this, function(_36d,
						key) {
					if (key.charAt(0) == "!") {
						_36d = _36b[key.substr(1)];
					}
					if (typeof _36d == "undefined") {
						throw new Error(_36a + " template:" + key);
					}
					return key.charAt(0) == "!" ? _36d : _36d.toString()
							.replace(/"/g, "&quot;");
				}, this);
				node = dijit._Templated._createNodesFromText(tstr)[0];
			} else {
				node = _368.cloneNode(true);
			}
			this._attachTemplateNodes(node);
			if (this.srcNodeRef) {
				dojo.style(this.styleNode || node, "cssText",
						this.srcNodeRef.style.cssText);
				if (this.srcNodeRef.className) {
					node.className += " " + this.srcNodeRef.className;
				}
			}
			this.domNode = node;
			if (this.srcNodeRef && this.srcNodeRef.parentNode) {
				this.srcNodeRef.parentNode.replaceChild(this.domNode,
						this.srcNodeRef);
			}
			if (this.widgetsInTemplate) {
				var _36f = dojo.parser.parse(this.domNode);
				this._attachTemplateNodes(_36f, function(n, p) {
					return n[p];
				});
			}
			this._fillContent(this.srcNodeRef);
		},
		_fillContent : function(_372) {
			var dest = this.containerNode;
			if (_372 && dest) {
				while (_372.hasChildNodes()) {
					dest.appendChild(_372.firstChild);
				}
			}
		},
		_attachTemplateNodes : function(_374, _375) {
			_375 = _375 || function(n, p) {
				return n.getAttribute(p);
			};
			var _378 = dojo.isArray(_374) ? _374 : (_374.all || _374
					.getElementsByTagName("*"));
			var x = dojo.isArray(_374) ? 0 : -1;
			for (; x < _378.length; x++) {
				var _37a = (x == -1) ? _374 : _378[x];
				if (this.widgetsInTemplate && _375(_37a, "dojoType")) {
					continue;
				}
				var _37b = _375(_37a, "dojoAttachPoint");
				if (_37b) {
					var _37c, _37d = _37b.split(/\s*,\s*/);
					while (_37c = _37d.shift()) {
						if (dojo.isArray(this[_37c])) {
							this[_37c].push(_37a);
						} else {
							this[_37c] = _37a;
						}
					}
				}
				var _37e = _375(_37a, "dojoAttachEvent");
				if (_37e) {
					var _37f, _380 = _37e.split(/\s*,\s*/);
					var trim = dojo.trim;
					while (_37f = _380.shift()) {
						if (_37f) {
							var _382 = null;
							if (_37f.indexOf(":") != -1) {
								var _383 = _37f.split(":");
								_37f = trim(_383[0]);
								_382 = trim(_383[1]);
							} else {
								_37f = trim(_37f);
							}
							if (!_382) {
								_382 = _37f;
							}
							this.connect(_37a, _37f, _382);
						}
					}
				}
				var name, _385 = [ "waiRole", "waiState" ];
				while (name = _385.shift()) {
					var wai = dijit.wai[name];
					var _387 = _375(_37a, wai.name);
					if (_387) {
						var role = "role";
						var val;
						_387 = _387.split(/\s*,\s*/);
						while (val = _387.shift()) {
							if (val.indexOf("-") != -1) {
								var _38a = val.split("-");
								role = _38a[0];
								val = _38a[1];
							}
							dijit.wai.setAttr(_37a, wai.name, role, val);
						}
					}
				}
			}
		}
	});
	dijit._Templated._templateCache = {};
	dijit._Templated.getCachedTemplate = function(_38b, _38c) {
		var _38d = dijit._Templated._templateCache;
		var key = _38c || _38b;
		var _38f = _38d[key];
		if (_38f) {
			return _38f;
		}
		if (!_38c) {
			_38c = dijit._Templated
					._sanitizeTemplateString(dojo._getText(_38b));
		}
		_38c = dojo.string.trim(_38c);
		if (_38c.match(/\$\{([^\}]+)\}/g)) {
			return (_38d[key] = _38c);
		} else {
			return (_38d[key] = dijit._Templated._createNodesFromText(_38c)[0]);
		}
	};
	dijit._Templated._sanitizeTemplateString = function(_390) {
		if (_390) {
			_390 = _390.replace(
					/^\s*<\?xml(\s)+version=[\'\"](\d)*.(\d)*[\'\"](\s)*\?>/im,
					"");
			var _391 = _390.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
			if (_391) {
				_390 = _391[1];
			}
		} else {
			_390 = "";
		}
		return _390;
	};
	if (dojo.isIE) {
		dojo.addOnUnload(function() {
			var _392 = dijit._Templated._templateCache;
			for ( var key in _392) {
				var _394 = _392[key];
				if (!isNaN(_394.nodeType)) {
					dojo._destroyElement(_394);
				}
				_392[key] = null;
			}
		});
	}
	(function() {
		var _395 = {
			cell : {
				re : /^<t[dh][\s\r\n>]/i,
				pre : "<table><tbody><tr>",
				post : "</tr></tbody></table>"
			},
			row : {
				re : /^<tr[\s\r\n>]/i,
				pre : "<table><tbody>",
				post : "</tbody></table>"
			},
			section : {
				re : /^<(thead|tbody|tfoot)[\s\r\n>]/i,
				pre : "<table>",
				post : "</table>"
			}
		};
		var tn;
		dijit._Templated._createNodesFromText = function(text) {
			if (!tn) {
				tn = dojo.doc.createElement("div");
				tn.style.display = "none";
			}
			var _398 = "none";
			var _399 = text.replace(/^\s+/, "");
			for ( var type in _395) {
				var map = _395[type];
				if (map.re.test(_399)) {
					_398 = type;
					text = map.pre + text + map.post;
					break;
				}
			}
			tn.innerHTML = text;
			dojo.body().appendChild(tn);
			if (tn.normalize) {
				tn.normalize();
			}
			var tag = {
				cell : "tr",
				row : "tbody",
				section : "table"
			}[_398];
			var _39d = (typeof tag != "undefined") ? tn
					.getElementsByTagName(tag)[0] : tn;
			var _39e = [];
			while (_39d.firstChild) {
				_39e.push(_39d.removeChild(_39d.firstChild));
			}
			tn.innerHTML = "";
			return _39e;
		};
	})();
	dojo.extend(dijit._Widget, {
		dojoAttachEvent : "",
		dojoAttachPoint : "",
		waiRole : "",
		waiState : ""
	});
}
if (!dojo._hasResource["dijit._Container"]) {
	dojo._hasResource["dijit._Container"] = true;
	dojo.provide("dijit._Container");
	dojo.declare("dijit._Contained", null, {
		getParent : function() {
			for ( var p = this.domNode.parentNode; p; p = p.parentNode) {
				var id = p.getAttribute && p.getAttribute("widgetId");
				if (id) {
					var _3a1 = dijit.byId(id);
					return _3a1.isContainer ? _3a1 : null;
				}
			}
			return null;
		},
		_getSibling : function(_3a2) {
			var node = this.domNode;
			do {
				node = node[_3a2 + "Sibling"];
			} while (node && node.nodeType != 1);
			if (!node) {
				return null;
			}
			var id = node.getAttribute("widgetId");
			return dijit.byId(id);
		},
		getPreviousSibling : function() {
			return this._getSibling("previous");
		},
		getNextSibling : function() {
			return this._getSibling("next");
		}
	});
	dojo.declare("dijit._Container", null, {
		isContainer : true,
		addChild : function(_3a5, _3a6) {
			if (typeof _3a6 == "undefined") {
				_3a6 = "last";
			}
			dojo.place(_3a5.domNode, this.containerNode || this.domNode, _3a6);
			if (this._started && !_3a5._started) {
				_3a5.startup();
			}
		},
		removeChild : function(_3a7) {
			var node = _3a7.domNode;
			node.parentNode.removeChild(node);
		},
		_nextElement : function(node) {
			do {
				node = node.nextSibling;
			} while (node && node.nodeType != 1);
			return node;
		},
		_firstElement : function(node) {
			node = node.firstChild;
			if (node && node.nodeType != 1) {
				node = this._nextElement(node);
			}
			return node;
		},
		getChildren : function() {
			return dojo.query("> [widgetId]",
					this.containerNode || this.domNode).map(dijit.byNode);
		},
		hasChildren : function() {
			var cn = this.containerNode || this.domNode;
			return !!this._firstElement(cn);
		}
	});
}
if (!dojo._hasResource["dijit._tree.Controller"]) {
	dojo._hasResource["dijit._tree.Controller"] = true;
	dojo.provide("dijit._tree.Controller");
	dojo
			.declare(
					"dijit._tree.Controller",
					[ dijit._Widget ],
					{
						treeId : "",
						postMixInProperties : function() {
							if (this.store._features["dojo.data.api.Notification"]) {
								dojo
										.connect(this.store, "onNew", this,
												"onNew");
								dojo.connect(this.store, "onDelete", this,
										"onDelete");
								dojo
										.connect(this.store, "onSet", this,
												"onSet");
							}
							dojo.subscribe(this.treeId, this, "_listener");
						},
						_listener : function(_3ac) {
							var _3ad = _3ac.event;
							var _3ae = "on" + _3ad.charAt(0).toUpperCase()
									+ _3ad.substr(1);
							if (this[_3ae]) {
								this[_3ae](_3ac);
							}
						},
						onBeforeTreeDestroy : function(_3af) {
							dojo.unsubscribe(_3af.tree.id);
						},
						onExecute : function(_3b0) {
							_3b0.node.tree.focusNode(_3b0.node);
							console.log("execute message for " + _3b0.node
									+ ": ", _3b0);
						},
						onNext : function(_3b1) {
							var _3b2 = this._navToNextNode(_3b1.node);
							if (_3b2 && _3b2.isTreeNode) {
								_3b2.tree.focusNode(_3b2);
								return _3b2;
							}
						},
						onNew : function(item, _3b4) {
							if (_3b4) {
								var _3b5 = this._itemNodeMap[this.store
										.getIdentity(_3b4.item)];
							}
							var _3b6 = {
								item : item
							};
							if (_3b5) {
								if (!_3b5.isFolder) {
									_3b5.makeFolder();
								}
								if (_3b5.state == "LOADED" || _3b5.isExpanded) {
									var _3b7 = _3b5.addChildren([ _3b6 ]);
								}
							} else {
								var _3b7 = this.tree.addChildren([ _3b6 ]);
							}
							if (_3b7) {
								dojo.mixin(this._itemNodeMap, _3b7);
							}
						},
						onDelete : function(_3b8) {
							var _3b9 = this.store.getIdentity(_3b8);
							var node = this._itemNodeMap[_3b9];
							if (node) {
								parent = node.getParent();
								parent.deleteNode(node);
								this._itemNodeMap[_3b9] = null;
							}
						},
						onSet : function(_3bb) {
							var _3bc = this.store.getIdentity(_3bb);
							var node = this._itemNodeMap[_3bc];
							node.setLabelNode(this.store.getLabel(_3bb));
						},
						_navToNextNode : function(node) {
							var _3bf;
							if (node.isFolder && node.isExpanded
									&& node.hasChildren()) {
								_3bf = node.getChildren()[0];
							} else {
								while (node.isTreeNode) {
									_3bf = node.getNextSibling();
									if (_3bf) {
										break;
									}
									node = node.getParent();
								}
							}
							return _3bf;
						},
						onPrevious : function(_3c0) {
							var _3c1 = _3c0.node;
							var _3c2 = _3c1;
							var _3c3 = _3c1.getPreviousSibling();
							if (_3c3) {
								_3c1 = _3c3;
								while (_3c1.isFolder && _3c1.isExpanded
										&& _3c1.hasChildren()) {
									_3c2 = _3c1;
									var _3c4 = _3c1.getChildren();
									_3c1 = _3c4[_3c4.length - 1];
								}
							} else {
								_3c1 = _3c1.getParent();
							}
							if (_3c1 && _3c1.isTreeNode) {
								_3c2 = _3c1;
							}
							if (_3c2 && _3c2.isTreeNode) {
								_3c2.tree.focusNode(_3c2);
								return _3c2;
							}
						},
						onZoomIn : function(_3c5) {
							var _3c6 = _3c5.node;
							var _3c7 = _3c6;
							if (_3c6.isFolder && !_3c6.isExpanded) {
								this._expand(_3c6);
							} else {
								if (_3c6.hasChildren()) {
									_3c6 = _3c6.getChildren()[0];
								}
							}
							if (_3c6 && _3c6.isTreeNode) {
								_3c7 = _3c6;
							}
							if (_3c7 && _3c7.isTreeNode) {
								_3c7.tree.focusNode(_3c7);
								return _3c7;
							}
						},
						onZoomOut : function(_3c8) {
							var node = _3c8.node;
							var _3ca = node;
							if (node.isFolder && node.isExpanded) {
								this._collapse(node);
							} else {
								node = node.getParent();
							}
							if (node && node.isTreeNode) {
								_3ca = node;
							}
							if (_3ca && _3ca.isTreeNode) {
								_3ca.tree.focusNode(_3ca);
								return _3ca;
							}
						},
						onFirst : function(_3cb) {
							var _3cc = this._navToFirstNode(_3cb.tree);
							if (_3cc) {
								_3cc.tree.focusNode(_3cc);
								return _3cc;
							}
						},
						_navToFirstNode : function(tree) {
							var _3ce;
							if (tree) {
								_3ce = tree.getChildren()[0];
								if (_3ce && _3ce.isTreeNode) {
									return _3ce;
								}
							}
						},
						onLast : function(_3cf) {
							var _3d0 = _3cf.node.tree;
							var _3d1 = _3d0;
							while (_3d1.isExpanded) {
								var c = _3d1.getChildren();
								_3d1 = c[c.length - 1];
								if (_3d1.isTreeNode) {
									_3d0 = _3d1;
								}
							}
							if (_3d0 && _3d0.isTreeNode) {
								_3d0.tree.focusNode(_3d0);
								return _3d0;
							}
						},
						onToggleOpen : function(_3d3) {
							var node = _3d3.node;
							if (node.isExpanded) {
								this._collapse(node);
							} else {
								this._expand(node);
							}
						},
						onLetterKeyNav : function(_3d5) {
							var node = startNode = _3d5.node;
							var tree = _3d5.tree;
							var key = _3d5.key;
							do {
								node = this._navToNextNode(node);
								if (!node) {
									node = this._navToFirstNode(tree);
								}
							} while (node !== startNode
									&& (node.label.charAt(0).toLowerCase() != key));
							if (node && node.isTreeNode) {
								if (node !== startNode) {
									node.tree.focusNode(node);
								}
								return node;
							}
						},
						_expand : function(node) {
							if (node.isFolder) {
								node.expand();
								var t = node.tree;
								if (t.lastFocused) {
									t.focusNode(t.lastFocused);
								}
							}
						},
						_collapse : function(node) {
							if (node.isFolder) {
								if (dojo.query("[tabindex=0]", node.domNode).length > 0) {
									node.tree.focusNode(node);
								}
								node.collapse();
							}
						}
					});
	dojo.declare("dijit._tree.DataController", dijit._tree.Controller,
			{
				onAfterTreeCreate : function(_3dc) {
					var tree = this.tree = _3dc.tree;
					this._itemNodeMap = {};
					var _3de = this;
					function onComplete(_3df) {
						var _3e0 = dojo.map(_3df, function(item) {
							return {
								item : item,
								isFolder : _3de.store.hasAttribute(item,
										_3de.childrenAttr)
							};
						});
						_3de._itemNodeMap = tree.setChildren(_3e0);
					}
					;
					this.store.fetch({
						query : this.query,
						onComplete : onComplete
					});
				},
				_expand : function(node) {
					var _3e3 = this.store;
					var _3e4 = this.store.getValue;
					switch (node.state) {
					case "LOADING":
						return;
					case "UNCHECKED":
						var _3e5 = node.item;
						var _3e6 = _3e3.getValues(_3e5, this.childrenAttr);
						var _3e7 = 0;
						dojo.forEach(_3e6, function(item) {
							if (!_3e3.isItemLoaded(item)) {
								_3e7++;
							}
						});
						if (_3e7 == 0) {
							this._onLoadAllItems(node, _3e6);
						} else {
							node.markProcessing();
							var _3e9 = this;
							function onItem(item) {
								if (--_3e7 == 0) {
									node.unmarkProcessing();
									_3e9._onLoadAllItems(node, _3e6);
								}
							}
							;
							dojo.forEach(_3e6, function(item) {
								if (!_3e3.isItemLoaded(item)) {
									_3e3.loadItem({
										item : item,
										onItem : onItem
									});
								}
							});
						}
						break;
					default:
						dijit._tree.Controller.prototype._expand.apply(this,
								arguments);
						break;
					}
				},
				_onLoadAllItems : function(node, _3ed) {
					var _3ee = dojo.map(_3ed, function(item) {
						return {
							item : item,
							isFolder : this.store.hasAttribute(item,
									this.childrenAttr)
						};
					}, this);
					dojo.mixin(this._itemNodeMap, node.setChildren(_3ee));
					dijit._tree.Controller.prototype._expand.apply(this,
							arguments);
				},
				_collapse : function(node) {
					if (node.state == "LOADING") {
						return;
					}
					dijit._tree.Controller.prototype._collapse.apply(this,
							arguments);
				}
			});
}
if (!dojo._hasResource["dijit.Tree"]) {
	dojo._hasResource["dijit.Tree"] = true;
	dojo.provide("dijit.Tree");
	dojo.declare("dijit._TreeBase", [ dijit._Widget, dijit._Templated,
			dijit._Container, dijit._Contained ], {
		state : "UNCHECKED",
		locked : false,
		lock : function() {
			this.locked = true;
		},
		unlock : function() {
			if (!this.locked) {
				throw new Error(this.declaredClass + " unlock: not locked");
			}
			this.locked = false;
		},
		isLocked : function() {
			var node = this;
			while (true) {
				if (node.lockLevel) {
					return true;
				}
				if (!node.getParent() || node.isTree) {
					break;
				}
				node = node.getParent();
			}
			return false;
		},
		setChildren : function(_3f2) {
			this.destroyDescendants();
			this.state = "LOADED";
			var _3f3 = {};
			if (_3f2 && _3f2.length > 0) {
				this.isFolder = true;
				if (!this.containerNode) {
					this.containerNode = this.tree.containerNodeTemplate
							.cloneNode(true);
					this.domNode.appendChild(this.containerNode);
				}
				dojo.forEach(_3f2, function(_3f4) {
					var _3f5 = new dijit._TreeNode(dojo.mixin({
						tree : this.tree,
						label : this.tree.store.getLabel(_3f4.item)
					}, _3f4));
					this.addChild(_3f5);
					_3f3[this.tree.store.getIdentity(_3f4.item)] = _3f5;
				}, this);
				dojo.forEach(this.getChildren(), function(_3f6, idx) {
					_3f6._updateLayout();
				});
			} else {
				this.isFolder = false;
			}
			if (this.isTree) {
				var fc = this.getChildren()[0];
				var _3f9 = fc ? fc.labelNode : this.domNode;
				_3f9.setAttribute("tabIndex", "0");
			}
			return _3f3;
		},
		addChildren : function(_3fa) {
			var _3fb = {};
			if (_3fa && _3fa.length > 0) {
				dojo.forEach(_3fa, function(_3fc) {
					var _3fd = new dijit._TreeNode(dojo.mixin({
						tree : this.tree,
						label : this.tree.store.getLabel(_3fc.item)
					}, _3fc));
					this.addChild(_3fd);
					_3fb[this.tree.store.getIdentity(_3fc.item)] = _3fd;
				}, this);
				dojo.forEach(this.getChildren(), function(_3fe, idx) {
					_3fe._updateLayout();
				});
			}
			return _3fb;
		},
		deleteNode : function(node) {
			node.destroy();
			dojo.forEach(this.getChildren(), function(_401, idx) {
				_401._updateLayout();
			});
		},
		makeFolder : function() {
			this.isFolder = true;
			this._setExpando(false);
		}
	});
	dojo
			.declare(
					"dijit.Tree",
					dijit._TreeBase,
					{
						store : null,
						query : null,
						childrenAttr : "children",
						templateString : "<div class=\"dijitTreeContainer\" style=\"\" waiRole=\"tree\"\n\tdojoAttachEvent=\"onclick:_onClick,onkeypress:_onKeyPress\"\n></div>\n",
						isExpanded : true,
						isTree : true,
						_publish : function(_403, _404) {
							dojo.publish(this.id, [ dojo.mixin({
								tree : this,
								event : _403
							}, _404 || {}) ]);
						},
						postMixInProperties : function() {
							this.tree = this;
							var _405 = {};
							_405[dojo.keys.ENTER] = "execute";
							_405[dojo.keys.LEFT_ARROW] = "zoomOut";
							_405[dojo.keys.RIGHT_ARROW] = "zoomIn";
							_405[dojo.keys.UP_ARROW] = "previous";
							_405[dojo.keys.DOWN_ARROW] = "next";
							_405[dojo.keys.HOME] = "first";
							_405[dojo.keys.END] = "last";
							this._keyTopicMap = _405;
						},
						postCreate : function() {
							this.containerNode = this.domNode;
							var div = document.createElement("div");
							div.style.display = "none";
							div.className = "dijitTreeContainer";
							dijit.wai.setAttr(div, "waiRole", "role",
									"presentation");
							this.containerNodeTemplate = div;
							this._controller = new dijit._tree.DataController({
								store : this.store,
								treeId : this.id,
								query : this.query,
								childrenAttr : this.childrenAttr
							});
							this._publish("afterTreeCreate");
						},
						destroy : function() {
							this._publish("beforeTreeDestroy");
							return dijit._Widget.prototype.destroy.apply(this,
									arguments);
						},
						toString : function() {
							return "[" + this.declaredClass + " ID:" + this.id
									+ "]";
						},
						getIconClass : function(item) {
						},
						_domElement2TreeNode : function(_408) {
							var ret;
							do {
								ret = dijit.byNode(_408);
							} while (!ret && (_408 = _408.parentNode));
							return ret;
						},
						_onClick : function(e) {
							var _40b = e.target;
							var _40c = this._domElement2TreeNode(_40b);
							if (!_40c || !_40c.isTreeNode) {
								return;
							}
							if (_40b == _40c.expandoNode
									|| _40b == _40c.expandoNodeText) {
								if (_40c.isFolder) {
									this._publish("toggleOpen", {
										node : _40c
									});
								}
							} else {
								this._publish("execute", {
									item : _40c.item,
									node : _40c
								});
								this.onClick(_40c.item, _40c);
							}
							dojo.stopEvent(e);
						},
						onClick : function(item) {
							console.log("default onclick handler", item);
						},
						_onKeyPress : function(e) {
							if (e.altKey) {
								return;
							}
							var _40f = this._domElement2TreeNode(e.target);
							if (!_40f) {
								return;
							}
							if (e.charCode) {
								var _410 = e.charCode;
								if (!e.altKey && !e.ctrlKey && !e.shiftKey
										&& !e.metaKey) {
									_410 = (String.fromCharCode(_410))
											.toLowerCase();
									this._publish("letterKeyNav", {
										node : _40f,
										key : _410
									});
									dojo.stopEvent(e);
								}
							} else {
								if (this._keyTopicMap[e.keyCode]) {
									this._publish(this._keyTopicMap[e.keyCode],
											{
												node : _40f,
												item : _40f.item
											});
									dojo.stopEvent(e);
								}
							}
						},
						blurNode : function() {
							var node = this.lastFocused;
							if (!node) {
								return;
							}
							var _412 = node.labelNode;
							dojo.removeClass(_412, "dijitTreeLabelFocused");
							_412.setAttribute("tabIndex", "-1");
							this.lastFocused = null;
						},
						focusNode : function(node) {
							this.blurNode();
							var _414 = node.labelNode;
							_414.setAttribute("tabIndex", "0");
							this.lastFocused = node;
							dojo.addClass(_414, "dijitTreeLabelFocused");
							_414.focus();
						},
						_onBlur : function() {
							if (this.lastFocused) {
								var _415 = this.lastFocused.labelNode;
								dojo.removeClass(_415, "dijitTreeLabelFocused");
							}
						},
						_onFocus : function() {
							if (this.lastFocused) {
								var _416 = this.lastFocused.labelNode;
								dojo.addClass(_416, "dijitTreeLabelFocused");
							}
						}
					});
	dojo
			.declare(
					"dijit._TreeNode",
					dijit._TreeBase,
					{
						templateString : "<div class=\"dijitTreeNode dijitTreeExpandLeaf dijitTreeChildrenNo\" waiRole=\"presentation\"\n\t><span dojoAttachPoint=\"expandoNode\" class=\"dijitTreeExpando\" waiRole=\"presentation\"\n\t></span\n\t><span dojoAttachPoint=\"expandoNodeText\" class=\"dijitExpandoText\" waiRole=\"presentation\"\n\t></span\n\t>\n\t<div dojoAttachPoint=\"contentNode\" class=\"dijitTreeContent\" waiRole=\"presentation\">\n\t\t<div dojoAttachPoint=\"iconNode\" class=\"dijitInline dijitTreeIcon\" waiRole=\"presentation\"></div>\n\t\t<span dojoAttachPoint=labelNode class=\"dijitTreeLabel\" wairole=\"treeitem\" expanded=\"true\" tabindex=\"-1\"></span>\n\t</div>\n</div>\n",
						item : null,
						isTreeNode : true,
						label : "",
						isFolder : null,
						isExpanded : false,
						postCreate : function() {
							this.labelNode.innerHTML = "";
							this.labelNode.appendChild(document
									.createTextNode(this.label));
							this._setExpando();
							dojo.addClass(this.iconNode, this.tree
									.getIconClass(this.item));
						},
						markProcessing : function() {
							this.state = "LOADING";
							this._setExpando(true);
						},
						unmarkProcessing : function() {
							this._setExpando(false);
						},
						_updateLayout : function() {
							dojo.removeClass(this.domNode, "dijitTreeIsRoot");
							if (this.getParent()["isTree"]) {
								dojo.addClass(this.domNode, "dijitTreeIsRoot");
							}
							dojo.removeClass(this.domNode, "dijitTreeIsLast");
							if (!this.getNextSibling()) {
								dojo.addClass(this.domNode, "dijitTreeIsLast");
							}
						},
						_setExpando : function(_417) {
							var _418 = [ "dijitTreeExpandoLoading",
									"dijitTreeExpandoOpened",
									"dijitTreeExpandoClosed",
									"dijitTreeExpandoLeaf" ];
							var idx = _417 ? 0
									: (this.isFolder ? (this.isExpanded ? 1 : 2)
											: 3);
							dojo.forEach(_418, function(s) {
								dojo.removeClass(this.expandoNode, s);
							}, this);
							dojo.addClass(this.expandoNode, _418[idx]);
							this.expandoNodeText.innerHTML = _417 ? "*"
									: (this.isFolder ? (this.isExpanded ? "-"
											: "+") : "*");
						},
						setChildren : function(_41b) {
							var ret = dijit.Tree.superclass.setChildren.apply(
									this, arguments);
							this._wipeIn = dojo.fx.wipeIn({
								node : this.containerNode,
								duration : 250
							});
							dojo.connect(this.wipeIn, "onEnd", dojo.hitch(this,
									"_afterExpand"));
							this._wipeOut = dojo.fx.wipeOut({
								node : this.containerNode,
								duration : 250
							});
							dojo.connect(this.wipeOut, "onEnd", dojo.hitch(
									this, "_afterCollapse"));
							return ret;
						},
						expand : function() {
							if (this.isExpanded) {
								return;
							}
							if (this._wipeOut.status() == "playing") {
								this._wipeOut.stop();
							}
							this.isExpanded = true;
							dijit.wai.setAttr(this.labelNode, "waiState",
									"expanded", "true");
							dijit.wai.setAttr(this.containerNode, "waiRole",
									"role", "group");
							this._setExpando();
							this._wipeIn.play();
						},
						_afterExpand : function() {
							this.onShow();
							this._publish("afterExpand", {
								node : this
							});
						},
						collapse : function() {
							if (!this.isExpanded) {
								return;
							}
							if (this._wipeIn.status() == "playing") {
								this._wipeIn.stop();
							}
							this.isExpanded = false;
							dijit.wai.setAttr(this.labelNode, "waiState",
									"expanded", "false");
							this._setExpando();
							this._wipeOut.play();
						},
						_afterCollapse : function() {
							this.onHide();
							this._publish("afterCollapse", {
								node : this
							});
						},
						setLabelNode : function(_41d) {
							this.labelNode.innerHTML = "";
							this.labelNode.appendChild(document
									.createTextNode(_41d));
						},
						toString : function() {
							return "[" + this.declaredClass + ", " + this.label
									+ "]";
						}
					});
}
if (!dojo._hasResource["dijit.layout.ContentPane"]) {
	dojo._hasResource["dijit.layout.ContentPane"] = true;
	dojo.provide("dijit.layout.ContentPane");
	dojo
			.declare(
					"dijit.layout.ContentPane",
					dijit._Widget,
					{
						href : "",
						extractContent : false,
						parseOnLoad : true,
						preventCache : false,
						preload : false,
						refreshOnShow : false,
						loadingMessage : "<span class='dijitContentPaneLoading'>${loadingState}</span>",
						errorMessage : "<span class='dijitContentPaneError'>${errorState}</span>",
						isLoaded : false,
						"class" : "dijitContentPane",
						postCreate : function() {
							this.domNode.title = "";
							if (this.preload) {
								this._loadCheck();
							}
							var _41e = dojo.i18n.getLocalization("dijit",
									"loading", this.lang);
							this.loadingMessage = dojo.string.substitute(
									this.loadingMessage, _41e);
							this.errorMessage = dojo.string.substitute(
									this.errorMessage, _41e);
							dojo.addClass(this.domNode, this["class"]);
						},
						startup : function() {
							if (!this._started) {
								this._loadCheck();
								this._started = true;
							}
						},
						refresh : function() {
							return this._prepareLoad(true);
						},
						setHref : function(href) {
							this.href = href;
							return this._prepareLoad();
						},
						setContent : function(data) {
							if (!this._isDownloaded) {
								this.href = "";
								this._onUnloadHandler();
							}
							this._setContent(data || "");
							this._isDownloaded = false;
							if (this.parseOnLoad) {
								this._createSubWidgets();
							}
							this._onLoadHandler();
						},
						cancel : function() {
							if (this._xhrDfd && (this._xhrDfd.fired == -1)) {
								this._xhrDfd.cancel();
							}
							delete this._xhrDfd;
						},
						destroy : function() {
							if (this._beingDestroyed) {
								return;
							}
							this._onUnloadHandler();
							this._beingDestroyed = true;
							dijit.layout.ContentPane.superclass.destroy
									.call(this);
						},
						resize : function(size) {
							dojo.marginBox(this.domNode, size);
						},
						_prepareLoad : function(_422) {
							this.cancel();
							this.isLoaded = false;
							this._loadCheck(_422);
						},
						_loadCheck : function(_423) {
							var _424 = ((this.open !== false) && (this.domNode.style.display != "none"));
							if (this.href
									&& (_423
											|| (this.preload && !this._xhrDfd)
											|| (this.refreshOnShow && _424 && !this._xhrDfd) || (!this.isLoaded
											&& _424 && !this._xhrDfd))) {
								this._downloadExternalContent();
							}
						},
						_downloadExternalContent : function() {
							this._onUnloadHandler();
							this._setContent(this.onDownloadStart.call(this));
							var self = this;
							var _426 = {
								preventCache : (this.preventCache || this.refreshOnShow),
								url : this.href,
								handleAs : "text"
							};
							if (dojo.isObject(this.ioArgs)) {
								dojo.mixin(_426, this.ioArgs);
							}
							var hand = this._xhrDfd = (this.ioMethod || dojo.xhrGet)
									(_426);
							hand.addCallback(function(html) {
								try {
									self.onDownloadEnd.call(self);
									self._isDownloaded = true;
									self.setContent.call(self, html);
								} catch (err) {
									self._onError.call(self, "Content", err);
								}
								delete self._xhrDfd;
								return html;
							});
							hand.addErrback(function(err) {
								if (!hand.cancelled) {
									self._onError.call(self, "Download", err);
								}
								delete self._xhrDfd;
								return err;
							});
						},
						_onLoadHandler : function() {
							this.isLoaded = true;
							try {
								this.onLoad.call(this);
							} catch (e) {
								console.error("Error " + this.widgetId
										+ " running custom onLoad code");
							}
						},
						_onUnloadHandler : function() {
							this.isLoaded = false;
							this.cancel();
							try {
								this.onUnload.call(this);
							} catch (e) {
								console.error("Error " + this.widgetId
										+ " running custom onUnload code");
							}
						},
						_setContent : function(cont) {
							this.destroyDescendants();
							try {
								var node = this.containerNode || this.domNode;
								while (node.firstChild) {
									dojo._destroyElement(node.firstChild);
								}
								if (typeof cont == "string") {
									if (this.extractContent) {
										match = cont
												.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
										if (match) {
											cont = match[1];
										}
									}
									node.innerHTML = cont;
								} else {
									if (cont.nodeType) {
										node.appendChild(cont);
									} else {
										dojo.forEach(cont,
												function(n) {
													node.appendChild(n
															.cloneNode(true));
												});
									}
								}
							} catch (e) {
								var _42d = this.onContentError(e);
								try {
									node.innerHTML = _42d;
								} catch (e) {
									console
											.error(
													"Fatal "
															+ this.id
															+ " could not change content due to "
															+ e.message, e);
								}
							}
						},
						_onError : function(type, err, _430) {
							var _431 = this["on" + type + "Error"].call(this,
									err);
							if (_430) {
								console.error(_430, err);
							} else {
								if (_431) {
									this._setContent.call(this, _431);
								}
							}
						},
						_createSubWidgets : function() {
							var _432 = this.containerNode || this.domNode;
							try {
								dojo.parser.parse(_432, true);
							} catch (e) {
								this._onError("Content", e,
										"Couldn't create widgets in "
												+ this.id
												+ (this.href ? " from "
														+ this.href : ""));
							}
						},
						onLoad : function(e) {
						},
						onUnload : function(e) {
						},
						onDownloadStart : function() {
							return this.loadingMessage;
						},
						onContentError : function(_435) {
						},
						onDownloadError : function(_436) {
							return this.errorMessage;
						},
						onDownloadEnd : function() {
						}
					});
}
if (!dojo._hasResource["dijit.form.Form"]) {
	dojo._hasResource["dijit.form.Form"] = true;
	dojo.provide("dijit.form.Form");
	dojo
			.declare(
					"dijit.form._FormMixin",
					null,
					{
						execute : function(_437) {
						},
						onCancel : function() {
						},
						onExecute : function() {
						},
						templateString : "<form dojoAttachPoint='containerNode' dojoAttachEvent='onsubmit:_onSubmit' enctype='multipart/form-data'></form>",
						_onSubmit : function(e) {
							dojo.stopEvent(e);
							this.onExecute();
							this.execute(this.getValues());
						},
						submit : function() {
							this.containerNode.submit();
						},
						setValues : function(obj) {
							var map = {};
							dojo.forEach(this.getDescendants(), function(_43b) {
								if (!_43b.name) {
									return;
								}
								var _43c = map[_43b.name]
										|| (map[_43b.name] = []);
								_43c.push(_43b);
							});
							for ( var name in map) {
								var _43e = map[name], _43f = dojo.getObject(
										name, false, obj);
								if (!dojo.isArray(_43f)) {
									_43f = [ _43f ];
								}
								if (_43e[0].setChecked) {
									dojo.forEach(_43e, function(w, i) {
										w.setChecked(dojo
												.indexOf(_43f, w.value) != -1);
									});
								} else {
									dojo.forEach(_43e, function(w, i) {
										w.setValue(_43f[i]);
									});
								}
							}
						},
						getValues : function() {
							var obj = {};
							dojo.forEach(this.getDescendants(), function(_445) {
								var _446 = _445.getValue ? _445.getValue()
										: _445.value;
								var name = _445.name;
								if (!name) {
									return;
								}
								if (_445.setChecked) {
									if (/Radio/.test(_445.declaredClass)) {
										if (_445.checked) {
											dojo.setObject(name, _446, obj);
										}
									} else {
										var ary = dojo.getObject(name, false,
												obj);
										if (!ary) {
											ary = [];
											dojo.setObject(name, ary, obj);
										}
										if (_445.checked) {
											ary.push(_446);
										}
									}
								} else {
									dojo.setObject(name, _446, obj);
								}
							});
							return obj;
						},
						isValid : function() {
							return dojo.every(this.getDescendants(), function(
									_449) {
								return !_449.isValid || _449.isValid();
							});
						}
					});
	dojo.declare("dijit.form.Form", [ dijit._Widget, dijit._Templated,
			dijit.form._FormMixin ], null);
}
if (!dojo._hasResource["dijit.Dialog"]) {
	dojo._hasResource["dijit.Dialog"] = true;
	dojo.provide("dijit.Dialog");
	dojo
			.declare(
					"dijit.DialogUnderlay",
					[ dijit._Widget, dijit._Templated ],
					{
						templateString : "<div class=dijitDialogUnderlayWrapper id='${id}_underlay'><div class=dijitDialogUnderlay dojoAttachPoint='node'></div></div>",
						postCreate : function() {
							dojo.body().appendChild(this.domNode);
							this.bgIframe = new dijit.BackgroundIframe(
									this.domNode);
						},
						layout : function() {
							var _44a = dijit.getViewport();
							var is = this.node.style, os = this.domNode.style;
							os.top = _44a.t + "px";
							os.left = _44a.l + "px";
							is.width = _44a.w + "px";
							is.height = _44a.h + "px";
							var _44d = dijit.getViewport();
							if (_44a.w != _44d.w) {
								is.width = _44d.w + "px";
							}
							if (_44a.h != _44d.h) {
								is.height = _44d.h + "px";
							}
						},
						show : function() {
							this.domNode.style.display = "block";
							this.layout();
							if (this.bgIframe.iframe) {
								this.bgIframe.iframe.style.display = "block";
							}
							this._resizeHandler = this.connect(window,
									"onresize", "layout");
						},
						hide : function() {
							this.domNode.style.display = "none";
							this.domNode.style.width = this.domNode.style.height = "1px";
							if (this.bgIframe.iframe) {
								this.bgIframe.iframe.style.display = "none";
							}
							this.disconnect(this._resizeHandler);
						},
						uninitialize : function() {
							if (this.bgIframe) {
								this.bgIframe.destroy();
							}
						}
					});
	dojo
			.declare(
					"dijit.Dialog",
					[ dijit.layout.ContentPane, dijit._Templated,
							dijit.form._FormMixin ],
					{
						templateString : null,
						templateString : "<div class=\"dijitDialog\">\n\t\t<div dojoAttachPoint=\"titleBar\" class=\"dijitDialogTitleBar\" tabindex=\"0\" waiRole=\"dialog\" title=\"${title}\">\n\t\t<span dojoAttachPoint=\"titleNode\" class=\"dijitDialogTitle\">${title}</span>\n\t\t<span dojoAttachPoint=\"closeButtonNode\" class=\"dijitDialogCloseIcon\" dojoAttachEvent=\"onclick: hide\">\n\t\t\t<span dojoAttachPoint=\"closeText\" class=\"closeText\">x</span>\n\t\t</span>\n\t</div>\n\t\t<div dojoAttachPoint=\"containerNode\" class=\"dijitDialogPaneContent\"></div>\n\t<span dojoAttachPoint=\"tabEnd\" dojoAttachEvent=\"onfocus:_cycleFocus\" tabindex=\"0\"></span>\n</div>\n",
						title : "",
						duration : 400,
						_lastFocusItem : null,
						postCreate : function() {
							dojo.body().appendChild(this.domNode);
							dijit.Dialog.superclass.postCreate.apply(this,
									arguments);
							this.domNode.style.display = "none";
							this.connect(this, "onExecute", "hide");
							this.connect(this, "onCancel", "hide");
						},
						onLoad : function() {
							this._position();
							dijit.Dialog.superclass.onLoad.call(this);
						},
						_setup : function() {
							this._modalconnects = [];
							if (this.titleBar) {
								this._moveable = new dojo.dnd.Moveable(
										this.domNode, {
											handle : this.titleBar
										});
							}
							this._underlay = new dijit.DialogUnderlay();
							var node = this.domNode;
							this._fadeIn = dojo.fx.combine([ dojo.fadeIn({
								node : node,
								duration : this.duration
							}), dojo.fadeIn({
								node : this._underlay.domNode,
								duration : this.duration,
								onBegin : dojo.hitch(this._underlay, "show")
							}) ]);
							this._fadeOut = dojo.fx.combine([ dojo.fadeOut({
								node : node,
								duration : this.duration,
								onEnd : function() {
									node.style.display = "none";
								}
							}), dojo.fadeOut({
								node : this._underlay.domNode,
								duration : this.duration,
								onEnd : dojo.hitch(this._underlay, "hide")
							}) ]);
						},
						uninitialize : function() {
							if (this._underlay) {
								this._underlay.destroy();
							}
						},
						_position : function() {
							var _44f = dijit.getViewport();
							var mb = dojo.marginBox(this.domNode);
							var _451 = this.domNode.style;
							_451.left = (_44f.l + (_44f.w - mb.w) / 2) + "px";
							_451.top = (_44f.t + (_44f.h - mb.h) / 2) + "px";
						},
						_findLastFocus : function(evt) {
							this._lastFocused = evt.target;
						},
						_cycleFocus : function(evt) {
							if (!this._lastFocusItem) {
								this._lastFocusItem = this._lastFocused;
							}
							this.titleBar.focus();
						},
						_onKey : function(evt) {
							if (evt.keyCode) {
								var node = evt.target;
								if (node == this.titleBar && evt.shiftKey
										&& evt.keyCode == dojo.keys.TAB) {
									if (this._lastFocusItem) {
										this._lastFocusItem.focus();
									}
									dojo.stopEvent(evt);
								} else {
									while (node) {
										if (node == this.domNode) {
											if (evt.keyCode == dojo.keys.ESCAPE) {
												this.hide();
											} else {
												return;
											}
										}
										node = node.parentNode;
									}
									if (evt.keyCode != dojo.keys.TAB) {
										dojo.stopEvent(evt);
									} else {
										if (!dojo.isOpera) {
											try {
												this.titleBar.focus();
											} catch (e) {
											}
										}
									}
								}
							}
						},
						show : function() {
							if (!this._alreadyInitialized) {
								this._setup();
								this._alreadyInitialized = true;
							}
							if (this._fadeOut.status() == "playing") {
								this._fadeOut.stop();
							}
							this._modalconnects.push(dojo.connect(window,
									"onscroll", this, "layout"));
							this._modalconnects.push(dojo.connect(
									document.documentElement, "onkeypress",
									this, "_onKey"));
							var ev = typeof (document.ondeactivate) == "object" ? "ondeactivate"
									: "onblur";
							this._modalconnects.push(dojo.connect(
									this.containerNode, ev, this,
									"_findLastFocus"));
							dojo.style(this.domNode, "opacity", 0);
							this.domNode.style.display = "block";
							this._loadCheck();
							this._position();
							this._fadeIn.play();
							this._savedFocus = dijit.getFocus(this);
							setTimeout(dojo.hitch(this, function() {
								dijit.focus(this.titleBar);
							}), 50);
						},
						hide : function() {
							if (!this._alreadyInitialized) {
								return;
							}
							if (this._fadeIn.status() == "playing") {
								this._fadeIn.stop();
							}
							this._fadeOut.play();
							if (this._scrollConnected) {
								this._scrollConnected = false;
							}
							dojo.forEach(this._modalconnects, dojo.disconnect);
							this._modalconnects = [];
							dijit.focus(this._savedFocus);
						},
						layout : function() {
							if (this.domNode.style.display == "block") {
								this._underlay.layout();
								this._position();
							}
						}
					});
	dojo
			.declare(
					"dijit.TooltipDialog",
					[ dijit.layout.ContentPane, dijit._Templated,
							dijit.form._FormMixin ],
					{
						title : "",
						_lastFocusItem : null,
						templateString : null,
						templateString : "<div id=\"${id}\" class=\"dijitTooltipDialog\" >\n\t<div class=\"dijitTooltipContainer\">\n\t\t<div  class =\"dijitTooltipContents dijitTooltipFocusNode\" dojoAttachPoint=\"containerNode\" tabindex=\"0\" waiRole=\"dialog\"></div>\n\t</div>\n\t<span dojoAttachPoint=\"tabEnd\" tabindex=\"0\" dojoAttachEvent=\"focus:_cycleFocus\"></span>\n\t<div class=\"dijitTooltipConnector\" ></div>\n</div>\n",
						postCreate : function() {
							dijit.TooltipDialog.superclass.postCreate.apply(
									this, arguments);
							this.connect(this.containerNode, "onkeypress",
									"_onKey");
							var ev = typeof (document.ondeactivate) == "object" ? "ondeactivate"
									: "onblur";
							this.connect(this.containerNode, ev,
									"_findLastFocus");
							this.containerNode.title = this.title;
						},
						orient : function(_458) {
							this.domNode.className = "dijitTooltipDialog "
									+ " dijitTooltipAB"
									+ (_458.charAt(1) == "L" ? "Left" : "Right")
									+ " dijitTooltip"
									+ (_458.charAt(0) == "T" ? "Below"
											: "Above");
						},
						onOpen : function(pos) {
							this.orient(pos.corner);
							this._loadCheck();
							this.containerNode.focus();
						},
						_onKey : function(evt) {
							if (evt.keyCode == dojo.keys.ESCAPE) {
								this.onCancel();
							} else {
								if (evt.target == this.containerNode
										&& evt.shiftKey
										&& evt.keyCode == dojo.keys.TAB) {
									if (this._lastFocusItem) {
										this._lastFocusItem.focus();
									}
									dojo.stopEvent(evt);
								}
							}
						},
						_findLastFocus : function(evt) {
							this._lastFocused = evt.target;
						},
						_cycleFocus : function(evt) {
							if (!this._lastFocusItem) {
								this._lastFocusItem = this._lastFocused;
							}
							this.containerNode.focus();
						}
					});
}
if (!dojo._hasResource["dijit.form._FormWidget"]) {
	dojo._hasResource["dijit.form._FormWidget"] = true;
	dojo.provide("dijit.form._FormWidget");
	dojo
			.declare(
					"dijit.form._FormWidget",
					[ dijit._Widget, dijit._Templated ],
					{
						baseClass : "",
						value : "",
						name : "",
						id : "",
						alt : "",
						type : "text",
						tabIndex : "0",
						disabled : false,
						intermediateChanges : false,
						setDisabled : function(_45d) {
							this.domNode.disabled = this.disabled = _45d;
							if (this.focusNode) {
								this.focusNode.disabled = _45d;
							}
							if (_45d) {
								this._hovering = false;
								this._active = false;
							}
							dijit.wai.setAttr(this.focusNode || this.domNode,
									"waiState", "disabled", _45d);
							this._setStateClass();
						},
						_onMouse : function(_45e) {
							var _45f = _45e.target;
							if (!this.disabled) {
								switch (_45e.type) {
								case "mouseover":
									this._hovering = true;
									var _460, node = _45f;
									while (node.nodeType === 1
											&& !(_460 = node
													.getAttribute("baseClass"))
											&& node != this.domNode) {
										node = node.parentNode;
									}
									this.baseClass = _460
											|| "dijit"
											+ this.declaredClass.replace(
													/.*\./g, "");
									break;
								case "mouseout":
									this._hovering = false;
									this.baseClass = null;
									break;
								case "mousedown":
									this._active = true;
									var self = this;
									var _463 = this.connect(dojo.body(),
											"onmouseup", function() {
												self._active = false;
												self._setStateClass();
												self.disconnect(_463);
											});
									break;
								}
								this._setStateClass();
							}
						},
						focus : function() {
							dijit.focus(this.focusNode);
						},
						_setStateClass : function(base) {
							var _465 = (this.styleNode || this.domNode).className;
							var base = this.baseClass
									|| this.domNode.getAttribute("baseClass")
									|| "dijitFormWidget";
							_465 = _465
									.replace(
											new RegExp(
													"\\b"
															+ base
															+ "(Checked)?(Selected)?(Disabled|Active|Focused|Hover)?\\b\\s*",
													"g"), "");
							var _466 = [ base ];
							function multiply(_467) {
								_466 = _466.concat(dojo.map(_466, function(c) {
									return c + _467;
								}));
							}
							;
							if (this.checked) {
								multiply("Checked");
							}
							if (this.selected) {
								multiply("Selected");
							}
							if (this.disabled) {
								multiply("Disabled");
							} else {
								if (this._active) {
									multiply("Active");
								} else {
									if (this._focused) {
										multiply("Focused");
									} else {
										if (this._hovering) {
											multiply("Hover");
										}
									}
								}
							}
							(this.styleNode || this.domNode).className = _465
									+ " " + _466.join(" ");
						},
						onChange : function(_469) {
						},
						postCreate : function() {
							this.setValue(this.value, true);
							this.setDisabled(this.disabled);
							this._setStateClass();
						},
						setValue : function(_46a, _46b) {
							this._lastValue = _46a;
							dijit.wai.setAttr(this.focusNode || this.domNode,
									"waiState", "valuenow", this
											.forWaiValuenow());
							if ((this.intermediateChanges || _46b)
									&& _46a != this._lastValueReported) {
								this._lastValueReported = _46a;
								this.onChange(_46a);
							}
						},
						getValue : function() {
							return this._lastValue;
						},
						undo : function() {
							this.setValue(this._lastValueReported, false);
						},
						_onKeyPress : function(e) {
							if (e.keyCode == dojo.keys.ESCAPE && !e.shiftKey
									&& !e.ctrlKey && !e.altKey) {
								var v = this.getValue();
								var lv = this._lastValueReported;
								if (lv != undefined
										&& v.toString() != lv.toString()) {
									this.undo();
									dojo.stopEvent(e);
									return false;
								}
							}
							return true;
						},
						forWaiValuenow : function() {
							return this.getValue();
						}
					});
}
if (!dojo._hasResource["dijit.form.Button"]) {
	dojo._hasResource["dijit.form.Button"] = true;
	dojo.provide("dijit.form.Button");
	dojo
			.declare(
					"dijit.form.Button",
					dijit.form._FormWidget,
					{
						label : "",
						showLabel : true,
						iconClass : "",
						type : "button",
						baseClass : "dijitButton",
						templateString : "<div class=\"dijit dijitLeft dijitInline dijitButton\" baseClass=\"${baseClass}\"\n\tdojoAttachEvent=\"onclick:_onButtonClick,onmouseover:_onMouse,onmouseout:_onMouse,onmousedown:_onMouse\"\n\t><div class='dijitRight'\n\t><button class=\"dijitStretch dijitButtonNode dijitButtonContents\" dojoAttachPoint=\"focusNode,titleNode\"\n\t\ttabIndex=\"${tabIndex}\" type=\"${type}\" id=\"${id}\" name=\"${name}\" waiRole=\"button\" waiState=\"labelledby-${id}_label\"\n\t\t><div class=\"dijitInline ${iconClass}\"></div\n\t\t><span class=\"dijitButtonText\" id=\"${id}_label\" dojoAttachPoint=\"containerNode\">${label}</span\n\t></button\n></div></div>\n",
						_onButtonClick : function(e) {
							dojo.stopEvent(e);
							if (this.disabled) {
								return;
							}
							return this.onClick(e);
						},
						postCreate : function() {
							if (this.showLabel == false) {
								var _470 = "";
								this.label = this.containerNode.innerHTML;
								_470 = dojo.trim(this.containerNode.innerText
										|| this.containerNode.textContent);
								this.titleNode.title = _470;
								dojo.addClass(this.containerNode,
										"dijitDisplayNone");
							}
							dijit.form._FormWidget.prototype.postCreate.apply(
									this, arguments);
						},
						onClick : function(e) {
							if (this.type == "submit") {
								for ( var node = this.domNode; node; node = node.parentNode) {
									var _473 = dijit.byNode(node);
									if (_473 && _473._onSubmit) {
										_473._onSubmit(e);
										break;
									}
									if (node.tagName.toLowerCase() == "form") {
										node.submit();
										break;
									}
								}
							}
						},
						setLabel : function(_474) {
							this.containerNode.innerHTML = this.label = _474;
							if (dojo.isMozilla) {
								var _475 = dojo.getComputedStyle(this.domNode).display;
								this.domNode.style.display = "none";
								var _476 = this;
								setTimeout(function() {
									_476.domNode.style.display = _475;
								}, 1);
							}
							if (this.showLabel == false) {
								this.titleNode.title = dojo
										.trim(this.containerNode.innerText
												|| this.containerNode.textContent);
							}
						}
					});
	dojo
			.declare(
					"dijit.form.DropDownButton",
					[ dijit.form.Button, dijit._Container ],
					{
						baseClass : "dijitDropDownButton",
						templateString : "<div class=\"dijit dijitLeft dijitInline dijitDropDownButton\" baseClass=\"dijitDropDownButton\"\n\tdojoAttachEvent=\"onmouseover:_onMouse,onmouseout:_onMouse,onmousedown:_onMouse,onclick:_onArrowClick,onkeypress:_onKey\"\n\t><div class='dijitRight'>\n\t<button tabIndex=\"${tabIndex}\" class=\"dijitStretch dijitButtonNode dijitButtonContents\" type=\"${type}\" id=\"${id}\" name=\"${name}\"\n\t\tdojoAttachPoint=\"focusNode,titleNode\" waiRole=\"button\" waiState=\"haspopup-true,labelledby-${id}_label\"\n\t\t><div class=\"dijitInline ${iconClass}\"></div\n\t\t><span class=\"dijitButtonText\" \tdojoAttachPoint=\"containerNode,popupStateNode\"\n\t\tid=\"${id}_label\">${label}</span\n\t\t><span class='dijitA11yDownArrow'>&#9660;</span>\n\t</button>\n</div></div>\n",
						_fillContent : function() {
							if (this.srcNodeRef) {
								var _477 = dojo.query("*", this.srcNodeRef);
								dijit.form.DropDownButton.superclass._fillContent
										.call(this, _477[0]);
								this.dropDownContainer = this.srcNodeRef;
							}
						},
						startup : function() {
							if (!this.dropDown) {
								var _478 = dojo.query("[widgetId]",
										this.dropDownContainer)[0];
								this.dropDown = dijit.byNode(_478);
								delete this.dropDownContainer;
							}
							dojo.body().appendChild(this.dropDown.domNode);
							this.dropDown.domNode.style.display = "none";
						},
						_onArrowClick : function(e) {
							if (this.disabled) {
								return;
							}
							this._toggleDropDown();
						},
						_onKey : function(e) {
							if (this.disabled) {
								return;
							}
							if (e.keyCode == dojo.keys.DOWN_ARROW) {
								if (!this.dropDown
										|| this.dropDown.domNode.style.display == "none") {
									dojo.stopEvent(e);
									return this._toggleDropDown();
								}
							}
						},
						_onBlur : function() {
							dijit.popup.closeAll();
						},
						_toggleDropDown : function() {
							if (this.disabled) {
								return;
							}
							dijit.focus(this.popupStateNode);
							var _47b = this.dropDown;
							if (!_47b) {
								return false;
							}
							if (!_47b.isShowingNow) {
								if (_47b.href && !_47b.isLoaded) {
									var self = this;
									var _47d = dojo.connect(_47b, "onLoad",
											function() {
												dojo.disconnect(_47d);
												self._openDropDown();
											});
									_47b._loadCheck(true);
									return;
								} else {
									this._openDropDown();
								}
							} else {
								dijit.popup.closeAll();
								this._opened = false;
							}
						},
						_openDropDown : function() {
							var _47e = this.dropDown;
							var _47f = _47e.domNode.style.width;
							var self = this;
							dijit.popup.open({
								parent : this,
								popup : _47e,
								around : this.domNode,
								orient : this.isLeftToRight() ? {
									"BL" : "TL",
									"BR" : "TR",
									"TL" : "BL",
									"TR" : "BR"
								} : {
									"BR" : "TR",
									"BL" : "TL",
									"TR" : "BR",
									"TL" : "BL"
								},
								onExecute : function() {
									dijit.popup.closeAll();
									self.focus();
								},
								onCancel : function() {
									dijit.popup.closeAll();
									self.focus();
								},
								onClose : function() {
									_47e.domNode.style.width = _47f;
									self.popupStateNode
											.removeAttribute("popupActive");
								}
							});
							if (this.domNode.offsetWidth > _47e.domNode.offsetWidth) {
								var _481 = null;
								if (!this.isLeftToRight()) {
									_481 = _47e.domNode.parentNode;
									var _482 = _481.offsetLeft
											+ _481.offsetWidth;
								}
								dojo.marginBox(_47e.domNode, {
									w : this.domNode.offsetWidth
								});
								if (_481) {
									_481.style.left = _482
											- this.domNode.offsetWidth + "px";
								}
							}
							this.popupStateNode.setAttribute("popupActive",
									"true");
							this._opened = true;
							if (_47e.focus) {
								_47e.focus();
							}
						}
					});
	dojo
			.declare(
					"dijit.form.ComboButton",
					dijit.form.DropDownButton,
					{
						templateString : "<table class='dijit dijitReset dijitInline dijitLeft dijitComboButton'  baseClass='dijitComboButton'\n\tid=\"${id}\" name=\"${name}\" cellspacing='0' cellpadding='0'\n\tdojoAttachEvent=\"onmouseover:_onMouse,onmouseout:_onMouse,onmousedown:_onMouse\">\n\t<tr>\n\t\t<td\tclass=\"dijitStretch dijitButtonContents dijitButtonNode\"\n\t\t\ttabIndex=\"${tabIndex}\"\n\t\t\tdojoAttachEvent=\"ondijitclick:_onButtonClick\"  dojoAttachPoint=\"titleNode\"\n\t\t\twaiRole=\"button\" waiState=\"labelledby-${id}_label\">\n\t\t\t<div class=\"dijitInline ${iconClass}\"></div>\n\t\t\t<span class=\"dijitButtonText\" id=\"${id}_label\" dojoAttachPoint=\"containerNode\">${label}</span>\n\t\t</td>\n\t\t<td class='dijitReset dijitRight dijitButtonNode dijitDownArrowButton'\n\t\t\tdojoAttachPoint=\"popupStateNode,focusNode\"\n\t\t\tdojoAttachEvent=\"onmouseover:_onMouse,onmouseout:_onMouse,onmousedown:_onMouse,ondijitclick:_onArrowClick, onkeypress:_onKey\"\n\t\t\tbaseClass=\"dijitComboButtonDownArrow\"\n\t\t\ttitle=\"${optionsTitle}\"\n\t\t\ttabIndex=\"${tabIndex}\"\n\t\t\twaiRole=\"button\" waiState=\"haspopup-true\"\n\t\t><div waiRole=\"presentation\">&#9660;</div>\n\t</td></tr>\n</table>\n",
						optionsTitle : "",
						baseClass : "dijitComboButton"
					});
	dojo.declare("dijit.form.ToggleButton", dijit.form.Button, {
		baseClass : "dijitToggleButton",
		checked : false,
		onClick : function(evt) {
			this.setChecked(!this.checked);
		},
		setChecked : function(_484) {
			this.checked = _484;
			this._setStateClass();
			this.onChange(_484);
		}
	});
}
if (!dojo._hasResource["dojo.data.ItemFileReadStore"]) {
	dojo._hasResource["dojo.data.ItemFileReadStore"] = true;
	dojo.provide("dojo.data.ItemFileReadStore");
	dojo
			.declare(
					"dojo.data.ItemFileReadStore",
					null,
					{
						constructor : function(_485) {
							this._arrayOfAllItems = [];
							this._arrayOfTopLevelItems = [];
							this._loadFinished = false;
							this._jsonFileUrl = _485.url;
							this._jsonData = _485.data;
							this._datatypeMap = _485.typeMap || {};
							if (!this._datatypeMap["Date"]) {
								this._datatypeMap["Date"] = {
									type : Date,
									deserialize : function(_486) {
										return dojo.date.stamp
												.fromISOString(_486);
									}
								};
							}
							this._features = {
								"dojo.data.api.Read" : true,
								"dojo.data.api.Identity" : true
							};
							this._itemsByIdentity = null;
							this._storeRefPropName = "_S";
							this._itemNumPropName = "_0";
							this._rootItemPropName = "_RI";
							this._loadInProgress = false;
							this._queuedFetches = [];
						},
						url : "",
						_assertIsItem : function(item) {
							if (!this.isItem(item)) {
								throw new Error(
										"dojo.data.ItemFileReadStore: a function was passed an item argument that was not an item");
							}
						},
						_assertIsAttribute : function(_488) {
							if (typeof _488 !== "string") {
								throw new Error(
										"dojo.data.ItemFileReadStore: a function was passed an attribute argument that was not an attribute name string");
							}
						},
						getValue : function(item, _48a, _48b) {
							var _48c = this.getValues(item, _48a);
							return (_48c.length > 0) ? _48c[0] : _48b;
						},
						getValues : function(item, _48e) {
							this._assertIsItem(item);
							this._assertIsAttribute(_48e);
							return item[_48e] || [];
						},
						getAttributes : function(item) {
							this._assertIsItem(item);
							var _490 = [];
							for ( var key in item) {
								if ((key !== this._storeRefPropName)
										&& (key !== this._itemNumPropName)
										&& (key !== this._rootItemPropName)) {
									_490.push(key);
								}
							}
							return _490;
						},
						hasAttribute : function(item, _493) {
							return this.getValues(item, _493).length > 0;
						},
						containsValue : function(item, _495, _496) {
							var _497 = undefined;
							if (typeof _496 === "string") {
								_497 = dojo.data.util.filter.patternToRegExp(
										_496, false);
							}
							return this._containsValue(item, _495, _496, _497);
						},
						_containsValue : function(item, _499, _49a, _49b) {
							var _49c = this.getValues(item, _499);
							for ( var i = 0; i < _49c.length; ++i) {
								var _49e = _49c[i];
								if (typeof _49e === "string" && _49b) {
									return (_49e.match(_49b) !== null);
								} else {
									if (_49a === _49e) {
										return true;
									}
								}
							}
							return false;
						},
						isItem : function(_49f) {
							if (_49f && _49f[this._storeRefPropName] === this) {
								if (this._arrayOfAllItems[_49f[this._itemNumPropName]] === _49f) {
									return true;
								}
							}
							return false;
						},
						isItemLoaded : function(_4a0) {
							return this.isItem(_4a0);
						},
						loadItem : function(_4a1) {
							this._assertIsItem(_4a1.item);
						},
						getFeatures : function() {
							return this._features;
						},
						getLabel : function(item) {
							if (this._labelAttr && this.isItem(item)) {
								return this.getValue(item, this._labelAttr);
							}
							return undefined;
						},
						getLabelAttributes : function(item) {
							if (this._labelAttr) {
								return [ this._labelAttr ];
							}
							return null;
						},
						_fetchItems : function(_4a4, _4a5, _4a6) {
							var self = this;
							var _4a8 = function(_4a9, _4aa) {
								var _4ab = [];
								if (_4a9.query) {
									var _4ac = _4a9.queryOptions ? _4a9.queryOptions.ignoreCase
											: false;
									var _4ad = {};
									for ( var key in _4a9.query) {
										var _4af = _4a9.query[key];
										if (typeof _4af === "string") {
											_4ad[key] = dojo.data.util.filter
													.patternToRegExp(_4af, _4ac);
										}
									}
									for ( var i = 0; i < _4aa.length; ++i) {
										var _4b1 = true;
										var _4b2 = _4aa[i];
										if (_4b2 === null) {
											_4b1 = false;
										} else {
											for ( var key in _4a9.query) {
												var _4af = _4a9.query[key];
												if (!self._containsValue(_4b2,
														key, _4af, _4ad[key])) {
													_4b1 = false;
												}
											}
										}
										if (_4b1) {
											_4ab.push(_4b2);
										}
									}
									_4a5(_4ab, _4a9);
								} else {
									for ( var i = 0; i < _4aa.length; ++i) {
										var item = _4aa[i];
										if (item !== null) {
											_4ab.push(item);
										}
									}
									_4a5(_4ab, _4a9);
								}
							};
							if (this._loadFinished) {
								_4a8(_4a4, this
										._getItemsArray(_4a4.queryOptions));
							} else {
								if (this._jsonFileUrl) {
									if (this._loadInProgress) {
										this._queuedFetches.push({
											args : _4a4,
											filter : _4a8
										});
									} else {
										this._loadInProgress = true;
										var _4b4 = {
											url : self._jsonFileUrl,
											handleAs : "json-comment-optional"
										};
										var _4b5 = dojo.xhrGet(_4b4);
										_4b5
												.addCallback(function(data) {
													try {
														self
																._getItemsFromLoadedData(data);
														self._loadFinished = true;
														self._loadInProgress = false;
														_4a8(
																_4a4,
																self
																		._getItemsArray(_4a4.queryOptions));
														self
																._handleQueuedFetches();
													} catch (e) {
														self._loadFinished = true;
														self._loadInProgress = false;
														_4a6(e, _4a4);
													}
												});
										_4b5.addErrback(function(_4b7) {
											self._loadInProgress = false;
											_4a6(_4b7, _4a4);
										});
									}
								} else {
									if (this._jsonData) {
										try {
											this._loadFinished = true;
											this
													._getItemsFromLoadedData(this._jsonData);
											this._jsonData = null;
											_4a8(
													_4a4,
													this
															._getItemsArray(_4a4.queryOptions));
										} catch (e) {
											_4a6(e, _4a4);
										}
									} else {
										_4a6(
												new Error(
														"dojo.data.ItemFileReadStore: No JSON source data was provided as either URL or a nested Javascript object."),
												_4a4);
									}
								}
							}
						},
						_handleQueuedFetches : function() {
							if (this._queuedFetches.length > 0) {
								for ( var i = 0; i < this._queuedFetches.length; i++) {
									var _4b9 = this._queuedFetches[i];
									var _4ba = _4b9.args;
									var _4bb = _4b9.filter;
									if (_4bb) {
										_4bb(
												_4ba,
												this
														._getItemsArray(_4ba.queryOptions));
									} else {
										this.fetchItemByIdentity(_4ba);
									}
								}
								this._queuedFetches = [];
							}
						},
						_getItemsArray : function(_4bc) {
							if (_4bc && _4bc.deep) {
								return this._arrayOfAllItems;
							}
							return this._arrayOfTopLevelItems;
						},
						close : function(_4bd) {
						},
						_getItemsFromLoadedData : function(_4be) {
							function valueIsAnItem(_4bf) {
								var _4c0 = ((_4bf != null)
										&& (typeof _4bf == "object")
										&& (!dojo.isArray(_4bf))
										&& (!dojo.isFunction(_4bf))
										&& (_4bf.constructor == Object)
										&& (typeof _4bf._reference == "undefined")
										&& (typeof _4bf._type == "undefined") && (typeof _4bf._value == "undefined"));
								return _4c0;
							}
							;
							var self = this;
							function addItemAndSubItemsToArrayOfAllItems(_4c2) {
								self._arrayOfAllItems.push(_4c2);
								for ( var _4c3 in _4c2) {
									var _4c4 = _4c2[_4c3];
									if (_4c4) {
										if (dojo.isArray(_4c4)) {
											var _4c5 = _4c4;
											for ( var k = 0; k < _4c5.length; ++k) {
												var _4c7 = _4c5[k];
												if (valueIsAnItem(_4c7)) {
													addItemAndSubItemsToArrayOfAllItems(_4c7);
												}
											}
										} else {
											if (valueIsAnItem(_4c4)) {
												addItemAndSubItemsToArrayOfAllItems(_4c4);
											}
										}
									}
								}
							}
							;
							this._labelAttr = _4be.label;
							var i;
							var item;
							this._arrayOfAllItems = [];
							this._arrayOfTopLevelItems = _4be.items;
							for (i = 0; i < this._arrayOfTopLevelItems.length; ++i) {
								item = this._arrayOfTopLevelItems[i];
								addItemAndSubItemsToArrayOfAllItems(item);
								item[this._rootItemPropName] = true;
							}
							var _4ca = {};
							var key;
							for (i = 0; i < this._arrayOfAllItems.length; ++i) {
								item = this._arrayOfAllItems[i];
								for (key in item) {
									if (key !== this._rootItemPropName) {
										var _4cc = item[key];
										if (_4cc !== null) {
											if (!dojo.isArray(_4cc)) {
												item[key] = [ _4cc ];
											}
										} else {
											item[key] = [ null ];
										}
									}
									_4ca[key] = key;
								}
							}
							while (_4ca[this._storeRefPropName]) {
								this._storeRefPropName += "_";
							}
							while (_4ca[this._itemNumPropName]) {
								this._itemNumPropName += "_";
							}
							var _4cd;
							var _4ce = _4be.identifier;
							this._itemsByIdentity = {};
							if (_4ce) {
								this._features["dojo.data.api.Identity"] = _4ce;
								for (i = 0; i < this._arrayOfAllItems.length; ++i) {
									item = this._arrayOfAllItems[i];
									_4cd = item[_4ce];
									var _4cf = _4cd[0];
									if (!this._itemsByIdentity[_4cf]) {
										this._itemsByIdentity[_4cf] = item;
									} else {
										if (this._jsonFileUrl) {
											throw new Error(
													"dojo.data.ItemFileReadStore:  The json data as specified by: ["
															+ this._jsonFileUrl
															+ "] is malformed.  Items within the list have identifier: ["
															+ _4ce
															+ "].  Value collided: ["
															+ _4cf + "]");
										} else {
											if (this._jsonData) {
												throw new Error(
														"dojo.data.ItemFileReadStore:  The json data provided by the creation arguments is malformed.  Items within the list have identifier: ["
																+ _4ce
																+ "].  Value collided: ["
																+ _4cf + "]");
											}
										}
									}
								}
							} else {
								this._features["dojo.data.api.Identity"] = Number;
							}
							for (i = 0; i < this._arrayOfAllItems.length; ++i) {
								item = this._arrayOfAllItems[i];
								item[this._storeRefPropName] = this;
								item[this._itemNumPropName] = i;
							}
							for (i = 0; i < this._arrayOfAllItems.length; ++i) {
								item = this._arrayOfAllItems[i];
								for (key in item) {
									_4cd = item[key];
									for ( var j = 0; j < _4cd.length; ++j) {
										_4cc = _4cd[j];
										if (_4cc !== null
												&& typeof _4cc == "object") {
											if (_4cc._type && _4cc._value) {
												var type = _4cc._type;
												var _4d2 = this._datatypeMap[type];
												if (!_4d2) {
													throw new Error(
															"dojo.data.ItemFileReadStore: in the typeMap constructor arg, no object class was specified for the datatype '"
																	+ type
																	+ "'");
												} else {
													if (dojo.isFunction(_4d2)) {
														_4cd[j] = new _4d2(
																_4cc._value);
													} else {
														if (dojo
																.isFunction(_4d2.deserialize)) {
															_4cd[j] = _4d2
																	.deserialize(_4cc._value);
														} else {
															throw new Error(
																	"dojo.data.ItemFileReadStore: Value provided in typeMap was neither a constructor, nor a an object with a deserialize function");
														}
													}
												}
											}
											if (_4cc._reference) {
												var _4d3 = _4cc._reference;
												if (dojo.isString(_4d3)) {
													_4cd[j] = this._itemsByIdentity[_4d3];
												} else {
													for ( var k = 0; k < this._arrayOfAllItems.length; ++k) {
														var _4d5 = this._arrayOfAllItems[k];
														var _4d6 = true;
														for ( var _4d7 in _4d3) {
															if (_4d5[_4d7] != _4d3[_4d7]) {
																_4d6 = false;
															}
														}
														if (_4d6) {
															_4cd[j] = _4d5;
														}
													}
												}
											}
										}
									}
								}
							}
						},
						getIdentity : function(item) {
							var _4d9 = this._features["dojo.data.api.Identity"];
							if (_4d9 === Number) {
								return item[this._itemNumPropName];
							} else {
								var _4da = item[_4d9];
								if (_4da) {
									return _4da[0];
								}
							}
							return null;
						},
						fetchItemByIdentity : function(_4db) {
							if (!this._loadFinished) {
								var self = this;
								if (this._jsonFileUrl) {
									if (this._loadInProgress) {
										this._queuedFetches.push({
											args : _4db
										});
									} else {
										var _4dd = {
											url : self._jsonFileUrl,
											handleAs : "json-comment-optional"
										};
										var _4de = dojo.xhrGet(_4dd);
										_4de
												.addCallback(function(data) {
													var _4e0 = _4db.scope ? _4db.scope
															: dojo.global;
													try {
														self
																._getItemsFromLoadedData(data);
														self._loadFinished = true;
														self._loadInProgress = false;
														var item = self
																._getItemByIdentity(_4db.identity);
														if (_4db.onItem) {
															_4db.onItem.call(
																	_4e0, item);
														}
														self
																._handleQueuedFetches();
													} catch (error) {
														self._loadInProgress = false;
														if (_4db.onError) {
															_4db.onError
																	.call(_4e0,
																			error);
														}
													}
												});
										_4de
												.addErrback(function(_4e2) {
													self._loadInProgress = false;
													if (_4db.onError) {
														var _4e3 = _4db.scope ? _4db.scope
																: dojo.global;
														_4db.onError.call(_4e3,
																_4e2);
													}
												});
									}
								} else {
									if (this._jsonData) {
										self
												._getItemsFromLoadedData(self._jsonData);
										self._jsonData = null;
										self._loadFinished = true;
										var item = self
												._getItemByIdentity(_4db.identity);
										if (_4db.onItem) {
											var _4e5 = _4db.scope ? _4db.scope
													: dojo.global;
											_4db.onItem.call(_4e5, item);
										}
									}
								}
							} else {
								var item = this
										._getItemByIdentity(_4db.identity);
								if (_4db.onItem) {
									var _4e5 = _4db.scope ? _4db.scope
											: dojo.global;
									_4db.onItem.call(_4e5, item);
								}
							}
						},
						_getItemByIdentity : function(_4e6) {
							var item = null;
							if (this._itemsByIdentity) {
								item = this._itemsByIdentity[_4e6];
								if (item === undefined) {
									item = null;
								}
							} else {
								this._arrayOfAllItems[_4e6];
							}
							return item;
						},
						getIdentityAttributes : function(item) {
							var _4e9 = this._features["dojo.data.api.Identity"];
							if (_4e9 === Number) {
								return null;
							} else {
								return [ _4e9 ];
							}
						},
						_forceLoad : function() {
							var self = this;
							if (this._jsonFileUrl) {
								var _4eb = {
									url : self._jsonFileUrl,
									handleAs : "json-comment-optional",
									sync : true
								};
								var _4ec = dojo.xhrGet(_4eb);
								_4ec.addCallback(function(data) {
									try {
										if (self._loadInProgress !== true
												&& !self._loadFinished) {
											self._getItemsFromLoadedData(data);
											self._loadFinished = true;
										}
									} catch (e) {
										console.log(e);
										throw e;
									}
								});
								_4ec.addErrback(function(_4ee) {
									throw _4ee;
								});
							} else {
								if (this._jsonData) {
									self
											._getItemsFromLoadedData(self._jsonData);
									self._jsonData = null;
									self._loadFinished = true;
								}
							}
						}
					});
	dojo.extend(dojo.data.ItemFileReadStore, dojo.data.util.simpleFetch);
}
if (!dojo._hasResource["dijit.form._DropDownTextBox"]) {
	dojo._hasResource["dijit.form._DropDownTextBox"] = true;
	dojo.provide("dijit.form._DropDownTextBox");
	dojo
			.declare(
					"dijit.form._DropDownTextBox",
					null,
					{
						templateString : "<table class=\"dijit dijitReset dijitInline dijitLeft\" baseClass=\"${baseClass}\" cellspacing=\"0\" cellpadding=\"0\"\n\tid=\"widget_${id}\" name=\"${name}\" dojoAttachEvent=\"onmouseover:_onMouse,onmouseout:_onMouse\" waiRole=\"presentation\"\n\t><tr\n\t\t><td class='dijitReset dijitStretch dijitComboBoxInput'\n\t\t\t><input class='XdijitInputField' type=\"text\" autocomplete=\"off\" name=\"${name}\"\n\t\t\tdojoAttachEvent=\"onkeypress, onkeyup, onfocus, onblur, compositionend\"\n\t\t\tdojoAttachPoint=\"textbox,focusNode\" id='${id}'\n\t\t\ttabIndex='${tabIndex}' size='${size}' maxlength='${maxlength}'\n\t\t\twaiRole=\"combobox\"\n\t\t></td\n\t\t><td class='dijitReset dijitRight dijitButtonNode dijitDownArrowButton'\n\t\t\tdojoAttachPoint=\"downArrowNode\"\n\t\t\tdojoAttachEvent=\"ondijitclick:_onArrowClick,onmousedown:_onMouse,onmouseup:_onMouse,onmouseover:_onMouse,onmouseout:_onMouse\"\n\t\t><div class=\"dijitDownArrowButtonInner\" waiRole=\"presentation\" tabIndex=\"-1\">\n\t\t\t<div class=\"dijit_a11y dijitDownArrowButtonChar\">&#9660;</div>\n\t\t</div>\n\t</td></tr>\n</table>\n",
						baseClass : "dijitComboBox",
						hasDownArrow : true,
						_popupWidget : null,
						_hasMasterPopup : false,
						_popupClass : "",
						_popupArgs : {},
						_hasFocus : false,
						_arrowPressed : function() {
							if (!this.disabled && this.hasDownArrow) {
								dojo.addClass(this.downArrowNode,
										"dijitArrowButtonActive");
							}
						},
						_arrowIdle : function() {
							if (!this.disabled && this.hasDownArrow) {
								dojo.removeClass(this.downArrowNode,
										"dojoArrowButtonPushed");
							}
						},
						makePopup : function() {
							var _4ef = this;
							function _createNewPopup() {
								var node = document.createElement("div");
								document.body.appendChild(node);
								var _4f1 = dojo.getObject(_4ef._popupClass,
										false);
								return new _4f1(_4ef._popupArgs, node);
							}
							;
							if (!this._popupWidget) {
								if (this._hasMasterPopup) {
									var _4f2 = dojo.getObject(
											this.declaredClass, false);
									if (!_4f2.prototype._popupWidget) {
										_4f2.prototype._popupWidget = _createNewPopup();
									}
									this._popupWidget = _4f2.prototype._popupWidget;
								} else {
									this._popupWidget = _createNewPopup();
								}
							}
						},
						_onArrowClick : function() {
							if (this.disabled) {
								return;
							}
							this.focus();
							this.makePopup();
							if (this._isShowingNow) {
								this._hideResultList();
							} else {
								this._openResultList();
							}
						},
						_hideResultList : function() {
							if (this._isShowingNow) {
								dijit.popup.close();
								this._arrowIdle();
								this._isShowingNow = false;
							}
						},
						_openResultList : function() {
							this._showResultList();
						},
						onfocus : function() {
							this._hasFocus = true;
						},
						onblur : function() {
							this._arrowIdle();
							this._hasFocus = false;
							dojo.removeClass(this.nodeWithBorder,
									"dijitInputFieldFocused");
							this.validate(false);
						},
						onkeypress : function(evt) {
							if (evt.ctrlKey || evt.altKey) {
								return;
							}
							switch (evt.keyCode) {
							case dojo.keys.PAGE_DOWN:
							case dojo.keys.DOWN_ARROW:
								if (!this._isShowingNow || this._prev_key_esc) {
									this.makePopup();
									this._arrowPressed();
									this._openResultList();
								}
								dojo.stopEvent(evt);
								this._prev_key_backspace = false;
								this._prev_key_esc = false;
								break;
							case dojo.keys.PAGE_UP:
							case dojo.keys.UP_ARROW:
							case dojo.keys.ENTER:
								dojo.stopEvent(evt);
							case dojo.keys.ESCAPE:
							case dojo.keys.TAB:
								if (this._isShowingNow) {
									this._prev_key_backspace = false;
									this._prev_key_esc = (evt.keyCode == dojo.keys.ESCAPE);
									this._hideResultList();
								}
								break;
							}
						},
						compositionend : function(evt) {
							this.onkeypress({
								charCode : -1
							});
						},
						_showResultList : function() {
							this._hideResultList();
							var _4f5 = this._popupWidget.getListLength ? this._popupWidget
									.getItems()
									: [ this._popupWidget.domNode ];
							if (_4f5.length) {
								var _4f6 = Math.min(_4f5.length,
										this.maxListLength);
								with (this._popupWidget.domNode.style) {
									display = "";
									width = "";
									height = "";
								}
								this._arrowPressed();
								this._displayMessage("");
								var best = this.open();
								var _4f8 = dojo
										.marginBox(this._popupWidget.domNode);
								this._popupWidget.domNode.style.overflow = ((best.h == _4f8.h) && (best.w == _4f8.w)) ? "hidden"
										: "auto";
								dojo.marginBox(this._popupWidget.domNode, {
									h : best.h,
									w : Math.max(best.w,
											this.domNode.offsetWidth)
								});
							}
						},
						getDisplayedValue : function() {
							return this.textbox.value;
						},
						setDisplayedValue : function(_4f9) {
							this.textbox.value = _4f9;
						},
						uninitialize : function() {
							if (this._popupWidget) {
								this._hideResultList();
								this._popupWidget.destroy();
							}
						},
						open : function() {
							this.makePopup();
							var self = this;
							self._isShowingNow = true;
							return dijit.popup.open({
								popup : this._popupWidget,
								around : this.domNode,
								parent : this
							});
						},
						_onBlur : function() {
							this._hideResultList();
						},
						postMixInProperties : function() {
							this.baseClass = this.hasDownArrow ? this.baseClass
									: this.baseClass + "NoArrow";
						}
					});
}
if (!dojo._hasResource["dijit.form.TextBox"]) {
	dojo._hasResource["dijit.form.TextBox"] = true;
	dojo.provide("dijit.form.TextBox");
	dojo
			.declare(
					"dijit.form.TextBox",
					dijit.form._FormWidget,
					{
						trim : false,
						uppercase : false,
						lowercase : false,
						propercase : false,
						size : "20",
						maxlength : "999999",
						templateString : "<input dojoAttachPoint='textbox,focusNode' dojoAttachEvent='onfocus,onkeyup,onkeypress:_onKeyPress' autocomplete=\"off\"\n\tid='${id}' name='${name}' class=\"dijitInputField\" type='${type}' size='${size}' maxlength='${maxlength}' tabIndex='${tabIndex}'>\n",
						getTextValue : function() {
							return this.filter(this.textbox.value);
						},
						getValue : function() {
							return this.parse(this.getTextValue(),
									this.constraints);
						},
						setValue : function(_4fb, _4fc, _4fd) {
							if (_4fb == null) {
								_4fb = "";
							}
							_4fb = this.filter(_4fb);
							if (typeof _4fd == "undefined") {
								_4fd = (typeof _4fb == "undefined"
										|| _4fb == null || _4fb == NaN) ? null
										: this.format(_4fb, this.constraints);
							}
							if (_4fd != null) {
								var _4fe = this;
								this.textbox.value = _4fd;
							}
							dijit.form.TextBox.superclass.setValue.call(this,
									_4fb, _4fc);
						},
						forWaiValuenow : function() {
							return this.getTextValue();
						},
						format : function(_4ff, _500) {
							return _4ff;
						},
						parse : function(_501, _502) {
							return _501;
						},
						postCreate : function() {
							if (typeof this.nodeWithBorder != "object") {
								this.nodeWithBorder = this.textbox;
							}
							this.textbox.setAttribute("value", this
									.getTextValue());
							this.inherited("postCreate", arguments);
						},
						filter : function(val) {
							if (val == null) {
								return null;
							}
							if (this.trim) {
								val = dojo.trim(val);
							}
							if (this.uppercase) {
								val = val.toUpperCase();
							}
							if (this.lowercase) {
								val = val.toLowerCase();
							}
							if (this.propercase) {
								val = val.replace(/[^\s]+/g, function(word) {
									return word.substring(0, 1).toUpperCase()
											+ word.substring(1);
								});
							}
							return val;
						},
						onfocus : function() {
							dojo.addClass(this.nodeWithBorder,
									"dijitInputFieldFocused");
						},
						_onBlur : function() {
							dojo.removeClass(this.nodeWithBorder,
									"dijitInputFieldFocused");
							this.setValue(this.getValue(), true);
						},
						onkeyup : function() {
						}
					});
}
if (!dojo._hasResource["dijit.Tooltip"]) {
	dojo._hasResource["dijit.Tooltip"] = true;
	dojo.provide("dijit.Tooltip");
	dojo
			.declare(
					"dijit._MasterTooltip",
					[ dijit._Widget, dijit._Templated ],
					{
						duration : 200,
						templateString : "<div class=\"dijitTooltip dijitTooltipLeft\" id=\"dojoTooltip\">\n\t<div class=\"dijitTooltipContainer dijitTooltipContents\" dojoAttachPoint=\"containerNode\" waiRole='alert'></div>\n\t<div class=\"dijitTooltipConnector\"></div>\n</div>\n",
						postCreate : function() {
							dojo.body().appendChild(this.domNode);
							this.bgIframe = new dijit.BackgroundIframe(
									this.domNode);
							this.fadeIn = dojo.fadeIn({
								node : this.domNode,
								duration : this.duration,
								onEnd : dojo.hitch(this, "_onShow")
							}), this.fadeOut = dojo.fadeOut({
								node : this.domNode,
								duration : this.duration,
								onEnd : dojo.hitch(this, "_onHide")
							});
						},
						show : function(_505, _506) {
							if (this.fadeOut.status() == "playing") {
								this._onDeck = arguments;
								return;
							}
							this.containerNode.innerHTML = _505;
							this.domNode.style.top = (this.domNode.offsetTop + 1)
									+ "px";
							var _507 = this.isLeftToRight() ? {
								"BR" : "BL",
								"BL" : "BR"
							} : {
								"BL" : "BR",
								"BR" : "BL"
							};
							var pos = dijit.placeOnScreenAroundElement(
									this.domNode, _506, _507);
							this.domNode.className = "dijitTooltip dijitTooltip"
									+ (pos.corner == "BL" ? "Right" : "Left");
							dojo.style(this.domNode, "opacity", 0);
							this.fadeIn.play();
							this.isShowingNow = true;
						},
						_onShow : function() {
							if (dojo.isIE) {
								this.domNode.style.filter = "";
							}
						},
						hide : function() {
							if (this._onDeck) {
								this._onDeck = null;
								return;
							}
							this.fadeIn.stop();
							this.isShowingNow = false;
							this.fadeOut.play();
						},
						_onHide : function() {
							this.domNode.style.cssText = "";
							if (this._onDeck) {
								this.show.apply(this, this._onDeck);
								this._onDeck = null;
							}
						}
					});
	dojo.addOnLoad(function() {
		dijit.MasterTooltip = new dijit._MasterTooltip();
	});
	dojo.declare("dijit.Tooltip", dijit._Widget, {
		label : "",
		showDelay : 400,
		connectId : "",
		postCreate : function() {
			this.srcNodeRef.style.display = "none";
			this._connectNode = dojo.byId(this.connectId);
			dojo.forEach(
					[ "onMouseOver", "onHover", "onMouseOut", "onUnHover" ],
					function(_509) {
						this.connect(this._connectNode, _509.toLowerCase(), "_"
								+ _509);
					}, this);
		},
		_onMouseOver : function(e) {
			this._onHover(e);
		},
		_onMouseOut : function(e) {
			if (dojo.isDescendant(e.relatedTarget, this._connectNode)) {
				return;
			}
			this._onUnHover(e);
		},
		_onHover : function(e) {
			if (this._hover) {
				return;
			}
			this._hover = true;
			if (!this.isShowingNow && !this._showTimer) {
				this._showTimer = setTimeout(dojo.hitch(this, "open"),
						this.showDelay);
			}
		},
		_onUnHover : function(e) {
			if (!this._hover) {
				return;
			}
			this._hover = false;
			if (this._showTimer) {
				clearTimeout(this._showTimer);
				delete this._showTimer;
			} else {
				this.close();
			}
		},
		open : function() {
			if (this.isShowingNow) {
				return;
			}
			if (this._showTimer) {
				clearTimeout(this._showTimer);
				delete this._showTimer;
			}
			dijit.MasterTooltip.show(this.label || this.domNode.innerHTML,
					this._connectNode);
			this.isShowingNow = true;
		},
		close : function() {
			if (!this.isShowingNow) {
				return;
			}
			dijit.MasterTooltip.hide();
			this.isShowingNow = false;
		},
		uninitialize : function() {
			this.close();
		}
	});
}
if (!dojo._hasResource["dijit.form.ValidationTextBox"]) {
	dojo._hasResource["dijit.form.ValidationTextBox"] = true;
	dojo.provide("dijit.form.ValidationTextBox");
	dojo
			.declare(
					"dijit.form.ValidationTextBox",
					dijit.form.TextBox,
					{
						required : false,
						promptMessage : "",
						invalidMessage : "",
						constraints : {},
						regExp : ".*",
						regExpGen : function(_50e) {
							return this.regExp;
						},
						setValue : function() {
							this.inherited("setValue", arguments);
							this.validate(false);
						},
						validator : function(_50f, _510) {
							return (new RegExp("^(" + this.regExpGen(_510)
									+ ")" + (this.required ? "" : "?") + "$"))
									.test(_50f)
									&& (!this.required || !this._isEmpty(_50f));
						},
						isValid : function(_511) {
							return this.validator(this.textbox.value,
									this.constraints);
						},
						_isEmpty : function(_512) {
							return /^\s*$/.test(_512);
						},
						getErrorMessage : function(_513) {
							return this.invalidMessage;
						},
						getPromptMessage : function(_514) {
							return this.promptMessage;
						},
						validate : function(_515) {
							var _516 = "";
							var _517 = this.isValid(_515);
							var _518 = _517 ? "Normal" : "Error";
							if (!dojo.hasClass(this.nodeWithBorder,
									"dijitInputFieldValidation" + _518)) {
								dojo.removeClass(this.nodeWithBorder,
										"dijitInputFieldValidation"
												+ ((_518 == "Normal") ? "Error"
														: "Normal"));
								dojo.addClass(this.nodeWithBorder,
										"dijitInputFieldValidation" + _518);
							}
							dijit.wai.setAttr(this.focusNode, "waiState",
									"invalid", (_517 ? "false" : "true"));
							if (_515) {
								if (this._isEmpty(this.textbox.value)) {
									_516 = this.getPromptMessage(true);
								}
								if (!_516 && !_517) {
									_516 = this.getErrorMessage(true);
								}
							}
							this._displayMessage(_516);
						},
						_message : "",
						_displayMessage : function(_519) {
							if (this._message == _519) {
								return;
							}
							this._message = _519;
							this.displayMessage(_519);
						},
						displayMessage : function(_51a) {
							if (_51a) {
								dijit.MasterTooltip.show(_51a, this.domNode);
							} else {
								dijit.MasterTooltip.hide();
							}
						},
						_onBlur : function(evt) {
							this.validate(false);
							this.inherited("_onBlur", arguments);
						},
						onfocus : function(evt) {
							this.inherited("onfocus", arguments);
							this.validate(true);
						},
						onkeyup : function(evt) {
							this.onfocus(evt);
						},
						postMixInProperties : function() {
							if (this.constraints == dijit.form.ValidationTextBox.prototype.constraints) {
								this.constraints = {};
							}
							this.inherited("postMixInProperties", arguments);
							this.constraints.locale = this.lang;
							this.messages = dojo.i18n.getLocalization(
									"dijit.form", "validate", this.lang);
							dojo.forEach(
									[ "invalidMessage", "missingMessage" ],
									function(prop) {
										if (!this[prop]) {
											this[prop] = this.messages[prop];
										}
									}, this);
							var p = this.regExpGen(this.constraints);
							this.regExp = p;
						}
					});
	dojo.declare("dijit.form.MappedTextBox", dijit.form.ValidationTextBox, {
		serialize : function(val) {
			return val.toString();
		},
		toString : function() {
			var val = this.getValue();
			return (val != null) ? ((typeof val == "string") ? val : this
					.serialize(val, this.constraints)) : "";
		},
		validate : function() {
			this.valueNode.value = this.toString();
			this.inherited("validate", arguments);
		},
		postCreate : function() {
			var _522 = this.textbox;
			var _523 = (this.valueNode = document.createElement("input"));
			_523.setAttribute("type", _522.type);
			_523.setAttribute("value", this.toString());
			dojo.style(_523, "display", "none");
			_523.name = this.textbox.name;
			this.textbox.removeAttribute("name");
			dojo.place(_523, _522, "after");
			this.inherited("postCreate", arguments);
		}
	});
	dojo
			.declare(
					"dijit.form.RangeBoundTextBox",
					dijit.form.MappedTextBox,
					{
						rangeMessage : "",
						compare : function(val1, val2) {
							return val1 - val2;
						},
						rangeCheck : function(_526, _527) {
							var _528 = (typeof _527.min != "undefined");
							var _529 = (typeof _527.max != "undefined");
							if (_528 || _529) {
								return (!_528 || this.compare(_526, _527.min) >= 0)
										&& (!_529 || this.compare(_526,
												_527.max) <= 0);
							} else {
								return true;
							}
						},
						isInRange : function(_52a) {
							return this.rangeCheck(this.getValue(),
									this.constraints);
						},
						isValid : function(_52b) {
							return this.inherited("isValid", arguments)
									&& ((this._isEmpty(this.textbox.value) && !this.required) || this
											.isInRange(_52b));
						},
						getErrorMessage : function(_52c) {
							if (dijit.form.RangeBoundTextBox.superclass.isValid
									.call(this, false)
									&& !this.isInRange(_52c)) {
								return this.rangeMessage;
							} else {
								return this.inherited("getErrorMessage",
										arguments);
							}
						},
						postMixInProperties : function() {
							this.inherited("postMixInProperties", arguments);
							if (!this.rangeMessage) {
								this.messages = dojo.i18n.getLocalization(
										"dijit.form", "validate", this.lang);
								this.rangeMessage = this.messages.rangeMessage;
							}
						},
						postCreate : function() {
							this.inherited("postCreate", arguments);
							if (typeof this.constraints.min != "undefined") {
								dijit.wai.setAttr(this.domNode, "waiState",
										"valuemin", this.constraints.min);
							}
							if (typeof this.constraints.max != "undefined") {
								dijit.wai.setAttr(this.domNode, "waiState",
										"valuemax", this.constraints.max);
							}
						}
					});
}
if (!dojo._hasResource["dijit.form.ComboBox"]) {
	dojo._hasResource["dijit.form.ComboBox"] = true;
	dojo.provide("dijit.form.ComboBox");
	dojo
			.declare(
					"dijit.form.ComboBoxMixin",
					dijit.form._DropDownTextBox,
					{
						pageSize : 30,
						store : null,
						query : {},
						autoComplete : true,
						searchDelay : 100,
						searchAttr : "name",
						ignoreCase : true,
						_hasMasterPopup : true,
						_popupClass : "dijit.form._ComboBoxMenu",
						getValue : function() {
							return dijit.form.TextBox.superclass.getValue
									.apply(this, arguments);
						},
						setDisplayedValue : function(_52d) {
							this.setValue(_52d, true);
						},
						_getCaretPos : function(_52e) {
							if (typeof (_52e.selectionStart) == "number") {
								return _52e.selectionStart;
							} else {
								if (dojo.isIE) {
									var tr = document.selection.createRange()
											.duplicate();
									var ntr = _52e.createTextRange();
									tr.move("character", 0);
									ntr.move("character", 0);
									try {
										ntr.setEndPoint("EndToEnd", tr);
										return String(ntr.text).replace(/\r/g,
												"").length;
									} catch (e) {
										return 0;
									}
								}
							}
						},
						_setCaretPos : function(_531, _532) {
							_532 = parseInt(_532);
							this._setSelectedRange(_531, _532, _532);
						},
						_setSelectedRange : function(_533, _534, end) {
							if (!end) {
								end = _533.value.length;
							}
							if (_533.setSelectionRange) {
								dijit.focus(_533);
								_533.setSelectionRange(_534, end);
							} else {
								if (_533.createTextRange) {
									var _536 = _533.createTextRange();
									with (_536) {
										collapse(true);
										moveEnd("character", end);
										moveStart("character", _534);
										select();
									}
								} else {
									_533.value = _533.value;
									_533.blur();
									dijit.focus(_533);
									var dist = parseInt(_533.value.length)
											- end;
									var _538 = String.fromCharCode(37);
									var tcc = _538.charCodeAt(0);
									for ( var x = 0; x < dist; x++) {
										var te = document
												.createEvent("KeyEvents");
										te.initKeyEvent("keypress", true, true,
												null, false, false, false,
												false, tcc, tcc);
										_533.dispatchEvent(te);
									}
								}
							}
						},
						onkeypress : function(evt) {
							if (evt.ctrlKey || evt.altKey) {
								return;
							}
							var _53d = false;
							if (this._isShowingNow) {
								this._popupWidget.handleKey(evt);
							}
							switch (evt.keyCode) {
							case dojo.keys.PAGE_DOWN:
							case dojo.keys.DOWN_ARROW:
								if (!this._isShowingNow || this._prev_key_esc) {
									this._arrowPressed();
									_53d = true;
								} else {
									this._announceOption(this._popupWidget
											.getHighlightedOption());
								}
								dojo.stopEvent(evt);
								this._prev_key_backspace = false;
								this._prev_key_esc = false;
								break;
							case dojo.keys.PAGE_UP:
							case dojo.keys.UP_ARROW:
								if (this._isShowingNow) {
									this._announceOption(this._popupWidget
											.getHighlightedOption());
								}
								dojo.stopEvent(evt);
								this._prev_key_backspace = false;
								this._prev_key_esc = false;
								break;
							case dojo.keys.ENTER:
								if (this._isShowingNow) {
									var _53e = this._popupWidget
											.getHighlightedOption();
									if (_53e == this._popupWidget.nextButton) {
										this._nextSearch(1);
										dojo.stopEvent(evt);
										break;
									} else {
										if (_53e == this._popupWidget.previousButton) {
											this._nextSearch(-1);
											dojo.stopEvent(evt);
											break;
										}
									}
								}
							case dojo.keys.TAB:
								if (this._isShowingNow) {
									this._prev_key_backspace = false;
									this._prev_key_esc = false;
									if (this._isShowingNow
											&& this._popupWidget
													.getHighlightedOption()) {
										this._popupWidget.setValue({
											target : this._popupWidget
													.getHighlightedOption()
										}, true);
									} else {
										this.setDisplayedValue(this
												.getDisplayedValue());
									}
									this._hideResultList();
								} else {
									this.setDisplayedValue(this
											.getDisplayedValue());
								}
								break;
							case dojo.keys.SPACE:
								this._prev_key_backspace = false;
								this._prev_key_esc = false;
								if (this._isShowingNow
										&& this._popupWidget
												.getHighlightedOption()) {
									dojo.stopEvent(evt);
									this._selectOption();
									this._hideResultList();
								} else {
									_53d = true;
								}
								break;
							case dojo.keys.ESCAPE:
								this._prev_key_backspace = false;
								this._prev_key_esc = true;
								this._hideResultList();
								this.setValue(this.getValue());
								break;
							case dojo.keys.DELETE:
							case dojo.keys.BACKSPACE:
								this._prev_key_esc = false;
								this._prev_key_backspace = true;
								_53d = true;
								break;
							case dojo.keys.RIGHT_ARROW:
							case dojo.keys.LEFT_ARROW:
								this._prev_key_backspace = false;
								this._prev_key_esc = false;
								break;
							default:
								this._prev_key_backspace = false;
								this._prev_key_esc = false;
								if (evt.charCode != 0) {
									_53d = true;
								}
							}
							if (this.searchTimer) {
								clearTimeout(this.searchTimer);
							}
							if (_53d) {
								this.searchTimer = setTimeout(dojo.hitch(this,
										this._startSearchFromInput),
										this.searchDelay);
							}
						},
						_autoCompleteText : function(text) {
							this._setSelectedRange(this.focusNode,
									this.focusNode.value.length,
									this.focusNode.value.length);
							if (new RegExp("^" + escape(this.focusNode.value),
									this.ignoreCase ? "i" : "")
									.test(escape(text))) {
								var cpos = this._getCaretPos(this.focusNode);
								if ((cpos + 1) > this.focusNode.value.length) {
									this.focusNode.value = text;
									this._setSelectedRange(this.focusNode,
											cpos, this.focusNode.value.length);
								}
							} else {
								this.focusNode.value = text;
								this._setSelectedRange(this.focusNode, 0,
										this.focusNode.value.length);
							}
						},
						_openResultList : function(_541, _542) {
							if (this.disabled
									|| _542.query[this.searchAttr] != this._lastQuery) {
								return;
							}
							this._popupWidget.clearResultList();
							if (!_541.length) {
								this._hideResultList();
								return;
							}
							var _543 = new String(this.store.getValue(_541[0],
									this.searchAttr));
							if (_543 && (this.autoComplete)
									&& (!this._prev_key_backspace)
									&& (_542.query[this.searchAttr] != "*")) {
								this._autoCompleteText(_543);
								dijit.wai.setAttr(this.focusNode
										|| this.domNode, "waiState",
										"valuenow", _543);
							}
							this._popupWidget.createOptions(_541, _542, dojo
									.hitch(this, this._getMenuLabelFromItem));
							this._showResultList();
						},
						onfocus : function() {
							dijit.form._DropDownTextBox.prototype.onfocus
									.apply(this, arguments);
							this.inherited("onfocus", arguments);
						},
						onblur : function() {
							dijit.form._DropDownTextBox.prototype.onblur.apply(
									this, arguments);
							if (!this._isShowingNow) {
								this
										.setDisplayedValue(this
												.getDisplayedValue());
							}
						},
						_announceOption : function(node) {
							if (node == null) {
								return;
							}
							var _545;
							if (node == this._popupWidget.nextButton
									|| node == this._popupWidget.previousButton) {
								_545 = node.innerHTML;
							} else {
								_545 = this.store.getValue(node.item,
										this.searchAttr);
							}
							this.focusNode.value = this.focusNode.value
									.substring(0, this
											._getCaretPos(this.focusNode));
							this._autoCompleteText(_545);
						},
						_selectOption : function(evt) {
							var tgt = null;
							if (!evt) {
								evt = {
									target : this._popupWidget
											.getHighlightedOption()
								};
							}
							if (!evt.target) {
								this
										.setDisplayedValue(this
												.getDisplayedValue());
								return;
							} else {
								tgt = evt.target;
							}
							if (!evt.noHide) {
								this._hideResultList();
								this._setCaretPos(this.focusNode,
										this.store.getValue(tgt.item,
												this.searchAttr).length);
							}
							this._doSelect(tgt);
						},
						_doSelect : function(tgt) {
							this.setValue(this.store.getIdentity(tgt.item));
						},
						_onArrowClick : function() {
							if (this.disabled) {
								return;
							}
							this.focus();
							this.makePopup();
							if (this._isShowingNow) {
								this._hideResultList();
							} else {
								this._startSearch("");
							}
						},
						_startSearchFromInput : function() {
							this._startSearch(this.focusNode.value);
						},
						_startSearch : function(key) {
							this.makePopup();
							var _54a = this.query;
							this._lastQuery = _54a[this.searchAttr] = key + "*";
							var _54b = this.store.fetch({
								queryOptions : {
									ignoreCase : this.ignoreCase,
									deep : true
								},
								query : _54a,
								onComplete : dojo
										.hitch(this, "_openResultList"),
								start : 0,
								count : this.pageSize
							});
							function nextSearch(_54c, _54d) {
								_54c.start += _54c.count * _54d;
								_54c.store.fetch(_54c);
							}
							;
							this._nextSearch = this._popupWidget.onPage = dojo
									.hitch(this, nextSearch, _54b);
						},
						_getValueField : function() {
							return this.searchAttr;
						},
						postMixInProperties : function() {
							dijit.form._DropDownTextBox.prototype.postMixInProperties
									.apply(this, arguments);
							if (!this.store) {
								var _54e = dojo.query("> option",
										this.srcNodeRef).map(function(node) {
									node.style.display = "none";
									return {
										value : node.getAttribute("value"),
										name : String(node.innerHTML)
									};
								});
								this.store = new dojo.data.ItemFileReadStore({
									data : {
										identifier : this._getValueField(),
										items : _54e
									}
								});
								if (_54e && _54e.length && !this.value) {
									this.value = _54e[this.srcNodeRef.selectedIndex != -1 ? this.srcNodeRef.selectedIndex
											: 0][this._getValueField()];
								}
							}
							if (this.query == dijit.form.ComboBoxMixin.prototype.query) {
								this.query = {};
							}
						},
						postCreate : function() {
							this.inherited("postCreate", arguments);
						},
						_getMenuLabelFromItem : function(item) {
							return {
								html : false,
								label : this.store.getValue(item,
										this.searchAttr)
							};
						},
						open : function() {
							this._popupWidget.onChange = dojo.hitch(this,
									this._selectOption);
							this._popupWidget._onkeypresshandle = this._popupWidget
									.connect(this._popupWidget.domNode,
											"onkeypress", dojo.hitch(this,
													this.onkeypress));
							return dijit.form._DropDownTextBox.prototype.open
									.apply(this, arguments);
						}
					});
	dojo
			.declare(
					"dijit.form._ComboBoxMenu",
					[ dijit._Widget, dijit._Templated ],
					{
						templateString : "<div class='dijitMenu' dojoAttachEvent='onclick,onmouseover,onmouseout' tabIndex='-1' style='display:none; position:absolute; overflow:\"auto\";'>"
								+ "<div class='dijitMenuItem' dojoAttachPoint='previousButton'></div>"
								+ "<div class='dijitMenuItem' dojoAttachPoint='nextButton'></div>"
								+ "</div>",
						_onkeypresshandle : null,
						_messages : null,
						_comboBox : null,
						postMixInProperties : function() {
							this._messages = dojo.i18n.getLocalization(
									"dijit.form", "ComboBox", this.lang);
							this.inherited("postMixInProperties", arguments);
						},
						setValue : function(_551) {
							this.value = _551;
							this.onChange(_551);
						},
						onChange : function(_552) {
						},
						onPage : function(_553) {
						},
						postCreate : function() {
							this.previousButton.innerHTML = this._messages["previousMessage"];
							this.nextButton.innerHTML = this._messages["nextMessage"];
							this.inherited("postCreate", arguments);
						},
						onClose : function() {
							this.disconnect(this._onkeypresshandle);
							this._blurOptionNode();
						},
						_createOption : function(item, _555) {
							var _556 = _555(item);
							var _557 = document.createElement("div");
							if (_556.html) {
								_557.innerHTML = _556.label;
							} else {
								_557.appendChild(document
										.createTextNode(_556.label));
							}
							if (_557.innerHTML == "") {
								_557.innerHTML = "&nbsp;";
							}
							_557.item = item;
							return _557;
						},
						createOptions : function(_558, _559, _55a) {
							this.previousButton.style.display = _559.start == 0 ? "none"
									: "";
							var _55b = this;
							dojo.forEach(_558, function(item) {
								var _55d = _55b._createOption(item, _55a);
								_55d.className = "dijitMenuItem";
								_55b.domNode
										.insertBefore(_55d, _55b.nextButton);
							});
							this.nextButton.style.display = _559.count == _558.length ? ""
									: "none";
						},
						clearResultList : function() {
							while (this.domNode.childNodes.length > 2) {
								this.domNode
										.removeChild(this.domNode.childNodes[this.domNode.childNodes.length - 2]);
							}
						},
						getItems : function() {
							return this.domNode.childNodes;
						},
						getListLength : function() {
							return this.domNode.childNodes.length - 2;
						},
						onclick : function(evt) {
							if (evt.target === this.domNode) {
								return;
							} else {
								if (evt.target == this.previousButton) {
									this.onPage(-1);
								} else {
									if (evt.target == this.nextButton) {
										this.onPage(1);
									} else {
										var tgt = evt.target;
										while (!tgt.item) {
											tgt = tgt.parentNode;
										}
										this.setValue({
											target : tgt
										}, true);
									}
								}
							}
						},
						onmouseover : function(evt) {
							if (evt.target === this.domNode) {
								return;
							}
							this._focusOptionNode(evt.target);
						},
						onmouseout : function(evt) {
							if (evt.target === this.domNode) {
								return;
							}
							this._blurOptionNode();
						},
						_focusOptionNode : function(node) {
							if (this._highlighted_option != node) {
								this._blurOptionNode();
								this._highlighted_option = node;
								dojo.addClass(this._highlighted_option,
										"dijitMenuItemHover");
							}
						},
						_blurOptionNode : function() {
							if (this._highlighted_option) {
								dojo.removeClass(this._highlighted_option,
										"dijitMenuItemHover");
								this._highlighted_option = null;
							}
						},
						_highlightNextOption : function() {
							if (!this.getHighlightedOption()) {
								this
										._focusOptionNode(this.domNode.firstChild.style.display == "none" ? this.domNode.firstChild.nextSibling
												: this.domNode.firstChild);
							} else {
								if (this._highlighted_option.nextSibling
										&& this._highlighted_option.nextSibling.style.display != "none") {
									this
											._focusOptionNode(this._highlighted_option.nextSibling);
								}
							}
							dijit.scrollIntoView(this._highlighted_option);
						},
						_highlightPrevOption : function() {
							if (!this.getHighlightedOption()) {
								this
										._focusOptionNode(this.domNode.lastChild.style.display == "none" ? this.domNode.lastChild.previousSibling
												: this.domNode.lastChild);
							} else {
								if (this._highlighted_option.previousSibling
										&& this._highlighted_option.previousSibling.style.display != "none") {
									this
											._focusOptionNode(this._highlighted_option.previousSibling);
								}
							}
							dijit.scrollIntoView(this._highlighted_option);
						},
						_page : function(up) {
							var _564 = 0;
							var _565 = this.domNode.scrollTop;
							var _566 = parseInt(dojo
									.getComputedStyle(this.domNode).height);
							if (!this.getHighlightedOption()) {
								this._highlightNextOption();
							}
							while (_564 < _566) {
								if (up) {
									if (!this.getHighlightedOption().previousSibling
											|| this._highlighted_option.previousSibling.style.display == "none") {
										break;
									}
									this._highlightPrevOption();
								} else {
									if (!this.getHighlightedOption().nextSibling
											|| this._highlighted_option.nextSibling.style.display == "none") {
										break;
									}
									this._highlightNextOption();
								}
								var _567 = this.domNode.scrollTop;
								_564 += (_567 - _565) * (up ? -1 : 1);
								_565 = _567;
							}
						},
						pageUp : function() {
							this._page(true);
						},
						pageDown : function() {
							this._page(false);
						},
						getHighlightedOption : function() {
							return this._highlighted_option
									&& this._highlighted_option.parentNode ? this._highlighted_option
									: null;
						},
						handleKey : function(evt) {
							switch (evt.keyCode) {
							case dojo.keys.DOWN_ARROW:
								this._highlightNextOption();
								break;
							case dojo.keys.PAGE_DOWN:
								this.pageDown();
								break;
							case dojo.keys.UP_ARROW:
								this._highlightPrevOption();
								break;
							case dojo.keys.PAGE_UP:
								this.pageUp();
								break;
							}
						}
					});
	dojo.declare("dijit.form.ComboBox", [ dijit.form.ValidationTextBox,
			dijit.form.ComboBoxMixin ], {});
}
({
	loadingState : "Loading...",
	errorState : "Sorry, an error occurred"
});
dojo.i18n._preloadLocalizations("dojo.nls.opentaps-dojo", [ "ROOT", "es-es",
		"es", "it-it", "pt-br", "de", "fr-fr", "zh-cn", "pt", "en-us", "zh",
		"fr", "zh-tw", "it", "en-gb", "xx", "de-de", "ko-kr", "ja-jp", "ko",
		"en", "ja" ]);
