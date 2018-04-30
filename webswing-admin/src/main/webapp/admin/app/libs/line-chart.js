var __extends = this && this.__extends || function(t, i) {
    function n() {
        this.constructor = t
    }
    for (var e in i)
        i.hasOwnProperty(e) && (t[e] = i[e]);
    t.prototype = null === i ? Object.create(i) : (n.prototype = i.prototype,
    new n)
}
, n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        function i() {
            function t(t, i, n) {
                t.attr("transform", function(t) {
                    var e = i(t.value);
                    return "translate(" + (isFinite(e) ? e : n(t.value)) + ",0)"
                })
            }
            function i(t, i, n) {
                t.attr("transform", function(t) {
                    var e = i(t.value);
                    return "translate(0," + (isFinite(e) ? e : n(t.value)) + ")"
                })
            }
            function n(t) {
                var i = t[0]
                  , n = t[t.length - 1];
                return n > i ? [i, n] : [n, i]
            }
            function e(t) {
                return t.rangeExtent ? t.rangeExtent() : n(t.range())
            }
            var o, r = ([].slice,
            "bottom"), s = {
                top: 1,
                right: 1,
                bottom: 1,
                left: 1
            }, a = d3.scale.linear(), c = r, u = 6, p = 24, h = -6, l = 0, d = 5, f = null , y = null , g = function(n, r, s, a, f) {
                var y, g = "major" === r, v = g ? p : l, m = g ? h : d, x = f.selectAll(".tick." + r).data(n, function(t) {
                    return a(t.value)
                }), S = x.enter().insert("g", ".domain").attr("class", "tick " + r).style("opacity", 1e-6), O = d3.transition(x.exit()).style("opacity", 1e-6).remove(), M = d3.transition(x.order()).style("opacity", 1), b = Math.max(v, 0) + m, A = e(a), D = f.selectAll(".domain").data([0]), w = (D.enter().append("path").attr("class", "domain"),
                D.transition());
                S.append("line"),
                S.append("text");
                var C, E, F, k, T = S.select("line"), z = M.select("line"), _ = x.select("text").text(function(t, i) {
                    return o ? o(t, i) : t.label
                }), B = S.select("text"), U = M.select("text"), I = "top" === c || "left" === c ? -1 : 1;
                "bottom" === c || "top" === c ? (y = t,
                C = "x",
                F = "y",
                E = "x2",
                k = "y2",
                _.attr({
                    dy: 0 > I ? "0em" : ".8em",
                    dx: "5px"
                }).style("text-anchor", "left"),
                w.attr("d", "M" + A[0] + "," + I * u + "V0H" + A[1] + "V" + I * u)) : (y = i,
                C = "y",
                F = "x",
                E = "y2",
                k = "x2",
                _.attr({
                    dy: ".32em",
                    dx: 0 > I ? -b + "px" : b + "px"
                }).style("text-anchor", 0 > I ? "end" : "start"),
                w.attr("d", "M" + I * u + "," + A[0] + "H0V" + A[1] + "H" + I * u)),
                T.attr(k, I * v),
                B.attr(F, I * b),
                z.attr(E, 0).attr(k, I * v),
                U.attr(C, 0).attr(F, I * b),
                O.call(y, a, s),
                S.call(y, s, a),
                M.call(y, a, a)
            }
            , v = function(t) {
                t.each(function() {
                    var t = d3.select(this)
                      , i = this.__chart__ || a
                      , n = this.__chart__ = a.copy()
                      , e = f(n.domain())
                      , o = e.major
                      , r = e.minor;
                    g(o, "major", i, n, t),
                    g(r, "minor", i, n, t)
                })
            }
            ;
            return v.scale = function(t) {
                return arguments.length ? (a = t,
                v) : a
            }
            ,
            v.orient = function(t) {
                return arguments.length ? (c = t in s ? t + "" : r,
                v) : c
            }
            ,
            v.ticks = function(t) {
                return arguments.length ? (f = t,
                v) : f
            }
            ,
            v.tickValues = function(t) {
                return arguments.length ? (y = t,
                v) : y
            }
            ,
            v.tickFormat = function(t) {
                return arguments.length ? (o = t,
                v) : o
            }
            ,
            v.tickSize = function(t) {
                var i = arguments.length;
                return i ? (p = +t,
                u = +arguments[i - 1],
                l = +t,
                v) : p
            }
            ,
            v.innerTickSize = function(t) {
                return arguments.length ? (p = +t,
                v) : p
            }
            ,
            v.outerTickSize = function(t) {
                return arguments.length ? (u = +t,
                v) : u
            }
            ,
            v.tickPadding = function(t) {
                return arguments.length ? (h = +t,
                v) : h
            }
            ,
            v
        }
        t.twoSpeedAxis = i
    }(i = t.svg || (t.svg = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(i) {
        "use strict";
        var n = function() {
            function n(i) {
                void 0 === i && (i = {}),
                this.axis = "y",
                this.type = ["line"],
                this.visible = !0,
                this.defined = function(t) {
                    return !0
                }
                ;
                var n = this.sanitizeOptions(i);
                this.id = n.id || t.Utils.UUID.generate(),
                this.axis = n.axis,
                this.interpolation = n.interpolation,
                this.dataset = n.dataset,
                this.key = n.key,
                this.color = n.color,
                this.visible = n.visible,
                this.label = n.label || n.id,
                n.defined && (this.defined = n.defined),
                n.type.length > 0 && (this.type = this.sanitizeType(n.type))
            }
            return n.prototype.sanitizeOptions = function(t) {
                var n = {};
                return angular.extend(n, this, t),
                n.axis = this.sanitizeAxis(n.axis),
                n.interpolation = this.sanitizeInterpolation(n.interpolation),
                n.id = i.Options.getString(n.id),
                n.type = i.Options.getArray(n.type),
                n.dataset = i.Options.getString(n.dataset),
                n.key = this.sanitizeKeys(n.key),
                n.color = i.Options.getString(n.color),
                n.label = i.Options.getString(n.label),
                n.visible = i.Options.getBoolean(n.visible),
                n.defined = i.Options.getFunction(n.defined),
                n
            }
            ,
            n.prototype.sanitizeInterpolation = function(t) {
                return t ? {
                    mode: i.Options.getString(t.mode, "linear"),
                    tension: i.Options.getNumber(t.tension, .7)
                } : {
                    mode: "linear",
                    tension: .7
                }
            }
            ,
            n.prototype.sanitizeKeys = function(t) {
                return t ? "string" == typeof t ? {
                    y1: i.Options.getString(t)
                } : {
                    y0: i.Options.getString(t.y0),
                    y1: i.Options.getString(t.y1)
                } : {
                    y1: void 0
                }
            }
            ,
            n.prototype.getToggledVisibility = function() {
                return !this.visible
            }
            ,
            n.prototype.sanitizeType = function(t) {
                return t.filter(function(t) {
                    return n.isValidType(t) ? !0 : (console.warn("Unknow series type : " + t),
                    !1)
                })
            }
            ,
            n.prototype.sanitizeAxis = function(t) {
                if (-1 === ["y", "y2"].indexOf(t))
                    throw TypeError(t + " is not a valid series option for axis.");
                return t
            }
            ,
            n.prototype.isAColumn = function() {
                return this.hasType(n.TYPE.COLUMN)
            }
            ,
            n.prototype.isDashed = function() {
                return -1 !== this.type.indexOf(n.TYPE.DASHED_LINE)
            }
            ,
            n.prototype.hasType = function(t) {
                return t === n.TYPE.LINE ? -1 !== this.type.indexOf(t) || -1 !== this.type.indexOf(n.TYPE.DASHED_LINE) : -1 !== this.type.indexOf(t)
            }
            ,
            n.prototype.hasTwoKeys = function() {
                return void 0 !== this.key.y0
            }
            ,
            n.isValidType = function(t) {
                return -1 !== d3.values(n.TYPE).indexOf(t)
            }
            ,
            n.TYPE = {
                DOT: "dot",
                LINE: "line",
                DASHED_LINE: "dashed-line",
                AREA: "area",
                COLUMN: "column"
            },
            n
        }();
        i.SeriesOptions = n
    }(i = t.Options || (t.Options = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function() {
            function i(t) {
                void 0 === t && (t = {}),
                this.includeZero = !1,
                this.type = "linear",
                this.key = "x",
                this.padding = {
                    min: 0,
                    max: 0
                },
                this.ticksShift = {
                    x: 0,
                    y: 0
                },
                this.parse(t)
            }
            return i.prototype.parse = function(n) {
                this.type = t.Options.getString(n.type, "linear"),
                this.key = n.key,
                this.padding = t.Options.getObject(n.padding || {}, this.padding),
                this.includeZero = t.Options.getBoolean(n.includeZero, !1),
                this.tickFormat = t.Options.getFunction(n.tickFormat),
                this.ticks = n.ticks,
                n.ticksShift && (this.ticksShift = {
                    x: t.Options.getNumber(n.ticksShift.x, 0),
                    y: t.Options.getNumber(n.ticksShift.y, 0)
                }),
                this.type === i.TYPE.LINEAR ? (this.min = t.Options.getNumber(n.min, void 0),
                this.max = t.Options.getNumber(n.max, void 0)) : this.type === i.TYPE.DATE && (this.min = t.Options.getDate(n.min, void 0),
                this.max = t.Options.getDate(n.max, void 0))
            }
            ,
            i.isValidSide = function(t) {
                return -1 !== d3.values(i.SIDE).indexOf(t)
            }
            ,
            i.prototype.hasDynamicTicks = function() {
                return this.ticks instanceof Function
            }
            ,
            i.prototype.configure = function(t) {
                return t.tickFormat(this.tickFormat),
                this.ticks instanceof Array ? t.tickValues(this.ticks) : "number" == typeof this.ticks ? t.ticks(this.ticks) : this.ticks instanceof Function && t.ticks(this.ticks),
                t
            }
            ,
            i.SIDE = {
                X: "x",
                X2: "x2",
                Y: "y",
                Y2: "y2"
            },
            i.TYPE = {
                LINEAR: "linear",
                DATE: "date",
                LOG: "log"
            },
            i
        }();
        t.AxisOptions = i
    }(i = t.Options || (t.Options = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function() {
            function i() {
                this.width = 600,
                this.height = 200,
                this.innerWidth = 560,
                this.innerHeight = 160,
                this.margin = i.getDefaultMargins()
            }
            return i.getDefaultMargins = function() {
                return {
                    top: 0,
                    left: 40,
                    bottom: 40,
                    right: 40
                }
            }
            ,
            i.prototype.updateMargins = function(i) {
                var n = this;
                if (i && i.margin) {
                    var e = function(e) {
                        return n.margin[e] = t.Options.getNumber(i.margin[e], n.margin[e])
                    }
                    ;
                    e("top"),
                    e("bottom"),
                    e("left"),
                    e("right")
                }
            }
            ,
            i.prototype.getDimensionByProperty = function(t, i) {
                var n = window.getComputedStyle(t, null );
                return +n.getPropertyValue(i).replace(/px$/, "")
            }
            ,
            i.prototype.fromParentElement = function(t) {
                if (t) {
                    var i = this.getDimensionByProperty(t, "padding-left") + this.getDimensionByProperty(t, "padding-right")
                      , n = this.getDimensionByProperty(t, "padding-top") + this.getDimensionByProperty(t, "padding-bottom");
                    this.width = t.clientWidth - i,
                    this.height = t.clientHeight - n,
                    this.innerHeight = this.height - this.margin.top - this.margin.bottom,
                    this.innerWidth = this.width - this.margin.left - this.margin.right
                }
            }
            ,
            i
        }();
        t.Dimensions = i
    }(i = t.Options || (t.Options = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function() {
            function i(n) {
                this.doubleClickEnabled = !0,
                this.series = [],
                this.pan = {
                    x: !1,
                    y: !1
                },
                this.zoom = {
                    x: !1,
                    y: !1
                },
                this.axes = {
                    x: {},
                    y: {}
                },
                this.margin = t.Dimensions.getDefaultMargins(),
                this.grid = {
                    x: !1,
                    y: !0
                };
                var e = angular.extend({}, this, n);
                this.margin = this.sanitizeMargin(i.getObject(e.margin, this.margin)),
                this.series = this.sanitizeSeries(i.getArray(e.series)),
                this.axes = this.sanitizeAxes(i.getObject(e.axes, this.axes)),
                this.grid = this.sanitizeTwoAxesOptions(e.grid, this.grid),
                this.pan = this.sanitizeTwoAxesOptions(e.pan, this.pan),
                this.zoom = this.sanitizeTwoAxesOptions(e.zoom, this.zoom),
                this.tooltipHook = i.getFunction(e.tooltipHook),
                this.doubleClickEnabled = i.getBoolean(e.doubleClickEnabled, !1)
            }
            return i.prototype.sanitizeMargin = function(t) {
                return {
                    top: i.getNumber(t.top, 0),
                    left: i.getNumber(t.left, 0),
                    bottom: i.getNumber(t.bottom, 0),
                    right: i.getNumber(t.right, 0)
                }
            }
            ,
            i.prototype.sanitizeSeries = function(i) {
                return i.map(function(i) {
                    return new t.SeriesOptions(i)
                })
            }
            ,
            i.prototype.sanitizeTwoAxesOptions = function(t, n) {
                var e = {
                    x: i.getBoolean(t.x, n.x),
                    y: i.getBoolean(t.y, n.y)
                };
                return e
            }
            ,
            i.prototype.sanitizeAxes = function(i) {
                return Object.keys(i).reduce(function(n, e) {
                    return n[e] = new t.AxisOptions(i[e]),
                    n
                }, {})
            }
            ,
            i.prototype.getAbsKey = function() {
                if (!this.axes[t.AxisOptions.SIDE.X])
                    throw new TypeError("Cannot find abs key : " + t.AxisOptions.SIDE.X);
                return this.axes[t.AxisOptions.SIDE.X].key
            }
            ,
            i.prototype.getVisibleDatasets = function() {
                var t = [];
                return this.series.forEach(function(i) {
                    i.visible && -1 === t.indexOf(i.dataset) && t.push(i.dataset)
                }),
                t
            }
            ,
            i.prototype.getVisibleSeriesBySide = function(t) {
                return this.series.filter(function(i) {
                    return i.visible && i.axis === t
                })
            }
            ,
            i.prototype.getSeriesAndDatasetBySide = function(i) {
                if (!t.AxisOptions.isValidSide(i))
                    throw new TypeError("Cannot get axis side : " + i);
                i !== t.AxisOptions.SIDE.Y2 || this.axes[i] || (i = t.AxisOptions.SIDE.Y);
                var n = []
                  , e = {};
                return this.series.forEach(function(t) {
                    t.visible && t.axis === i && (n.push(t.dataset),
                    e[t.dataset] || (e[t.dataset] = []),
                    e[t.dataset].push(t))
                }),
                {
                    seriesForDataset: e,
                    datasetsForSide: n
                }
            }
            ,
            i.prototype.getByAxisSide = function(i) {
                if (!t.AxisOptions.isValidSide(i))
                    throw new TypeError("Cannot get axis side : " + i);
                if (!this.axes[i]) {
                    if (i === t.AxisOptions.SIDE.Y2)
                        return this.axes[t.AxisOptions.SIDE.Y];
                    if (i === t.AxisOptions.SIDE.X2)
                        return this.axes[t.AxisOptions.SIDE.X]
                }
                return this.axes[i]
            }
            ,
            i.prototype.getSeriesByType = function(i) {
                if (!t.SeriesOptions.isValidType(i))
                    throw new TypeError("Unknown series type: " + i);
                return this.series.filter(function(t) {
                    return t.hasType(i)
                })
            }
            ,
            i.getBoolean = function(t, i) {
                return void 0 === i && (i = !0),
                "boolean" == typeof t ? t : i
            }
            ,
            i.getNumber = function(t, i) {
                var n = parseFloat(t);
                return isNaN(n) ? i : n
            }
            ,
            i.getDate = function(t, i) {
                return t instanceof Date ? t : i
            }
            ,
            i.getFunction = function(t) {
                return t instanceof Function ? t : void 0
            }
            ,
            i.getString = function(t, i) {
                return t ? String(t) : i
            }
            ,
            i.getIdentifier = function(t) {
                var n = i.getString(t);
                return n.replace(/[^a-zA-Z0-9\-_]/gi, "")
            }
            ,
            i.getObject = function(t, i) {
                if (void 0 === i && (i = {}),
                !angular.isObject(t))
                    throw TypeError(t + " option must be an object.");
                var n = {};
                return angular.extend(n, i, t),
                n
            }
            ,
            i.getArray = function(t, i) {
                return void 0 === i && (i = []),
                i.concat(t)
            }
            ,
            i
        }();
        t.Options = i
    }(i = t.Options || (t.Options = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function() {
            function t() {
                this.strictMode = !0
            }
            return t.prototype.init = function(t) {
                var i = this;
                this._dispatch = d3.dispatch.apply(this, t);
                var n = (new Date).getTime();
                return d3.select(window).on("mouseup." + n, function() {
                    //d3.event.preventDefault(),
                    i.trigger("window-mouseup")
                }),
                d3.select(window).on("mousemove." + n, function() {
                   // d3.event.preventDefault(),
                    i.trigger("window-mousemove")
                }),
                this
            }
            ,
            t.prototype.update = function(t, i) {
                this.data = t,
                this.options = i
            }
            ,
            t.prototype.on = function(i, n) {
                if (this.strictMode && -1 === t.EVENTS.indexOf(i.split(".")[0]))
                    throw new Error("Unknown event: " + i);
                return this._dispatch.on(i, n),
                this
            }
            ,
            t.prototype.trigger = function(t) {
                for (var i = [], n = 1; n < arguments.length; n++)
                    i[n - 1] = arguments[n];
                return this._dispatch[t].apply(this, i),
                this
            }
            ,
            t.prototype.triggerDataAndOptions = function(t) {
                for (var i = [], n = 1; n < arguments.length; n++)
                    i[n - 1] = arguments[n];
                return i.push(this.data),
                i.push(this.options),
                this._dispatch[t].apply(this, i),
                this
            }
            ,
            t.prototype.datumEnter = function(t, i) {
                var n = this;
                return function(e) {
                    return e.on("mouseenter", function(e, o) {
                        n.trigger("enter", e, o, t, i)
                    })
                }
            }
            ,
            t.prototype.datumOver = function(t, i) {
                var n = this;
                return function(e) {
                    return e.on("mouseover", function(e, o) {
                        n.trigger("over", e, o, t, i)
                    })
                }
            }
            ,
            t.prototype.datumMove = function(t, i) {
                var n = this;
                return function(e) {
                    return e.on("mousemove", function(e, o) {
                        n.trigger("over", e, o, t, i)
                    })
                }
            }
            ,
            t.prototype.datumLeave = function(t, i) {
                var n = this;
                return function(e) {
                    return e.on("mouseleave", function(e, o) {
                        n.trigger("leave", e, o, t, i)
                    })
                }
            }
            ,
            t.prototype.listenForDblClick = function(t, i, n) {
                var e, o, r = this, s = 5, a = null , c = function(t, i) {
                    return Math.sqrt(Math.pow(t[0] - i[0], 2) + Math.pow(t[1] - i[1], 2))
                }
                ;
                return t.on("mousedown.dbl." + n, function() {
                    e = d3.mouse(document.body),
                    o = (new Date).getTime()
                }),
                t.on("mouseup.dbl." + n, function() {
                    !e || c(e, d3.mouse(document.body)) > s || (a && r.options.doubleClickEnabled ? (window.clearTimeout(a),
                    a = null ,
                    i(d3.event)) : a = window.setTimeout(function(t) {
                        return function() {
                            a = null
                        }
                    }(d3.event), 300))
                }),
                t
            }
            ,
            t.EVENTS = ["create", "update", "data-update", "resize", "destroy", "enter", "over", "move", "leave", "click", "dblclick", "legend-click", "legend-over", "legend-out", "container-over", "container-move", "container-out", "focus", "toggle", "outer-world-hover", "outer-world-domain-change", "pan", "pan-end", "zoom", "zoom-end", "zoom-pan-reset", "window-mouseup", "window-mousemove"],
            t
        }();
        t.EventManager = i
    }(i = t.Utils || (t.Utils = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function() {
            function t() {
                this._factoryStack = []
            }
            return t.prototype.index = function(t) {
                return this._factoryStack.map(function(t) {
                    return t.key
                }).indexOf(t)
            }
            ,
            t.prototype.getBoundFunction = function(t, i) {
                var n = this.get(t);
                return n ? n[i].bind(n) : null
            }
            ,
            t.prototype.get = function(t) {
                var i = this.index(t);
                return i > -1 ? this._factoryStack[i].instance : null
            }
            ,
            t.prototype.all = function() {
                return this._factoryStack
            }
            ,
            t.prototype.turnFactoriesOff = function(t) {
                var i = this
                  , n = [];
                return t.forEach(function(t) {
                    var e = i.get(t);
                    e.isOn() && (e.off(),
                    n.push(t))
                }),
                function() {
                    return i.turnFactoriesOn(n)
                }
            }
            ,
            t.prototype.turnFactoriesOn = function(t) {
                var i = this
                  , n = [];
                return t.forEach(function(t) {
                    var e = i.get(t);
                    e.isOff() && (e.on(),
                    n.push(t))
                }),
                function() {
                    return i.turnFactoriesOff(n)
                }
            }
            ,
            t.prototype.registerMany = function(t) {
                var i = this;
                return t.forEach(function(t) {
                    i.register.apply(i, t)
                }),
                this
            }
            ,
            t.prototype.register = function(t, i) {
                for (var n = [], e = 2; e < arguments.length; e++)
                    n[e - 2] = arguments[e];
                var o = i.bind.apply(i, [null ].concat(n))
                  , r = new o;
                return this._factoryStack.push({
                    key: t,
                    instance: r
                }),
                r
            }
            ,
            t.prototype.unregister = function(t) {
                var i = this.index(t);
                return i > -1 && delete this._factoryStack[i],
                this
            }
            ,
            t
        }();
        t.FactoryManager = i
    }(i = t.Utils || (t.Utils = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function() {
            function t(t, i) {
                this.fromJS(t, i)
            }
            return t.prototype.fromJS = function(t, i) {
                this.id = i,
                this.values = t
            }
            ,
            t
        }();
        t.Dataset = i
    }(i = t.Utils || (t.Utils = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function() {
            function i(t) {
                t && this.fromJS(t)
            }
            return i.prototype.fromJS = function(i) {
                var n = {};
                for (var e in i)
                    i.hasOwnProperty(e) && (n[e] = new t.Dataset(i[e],e));
                this.sets = n
            }
            ,
            i.prototype.getDatasets = function(t, i) {
                var n = this;
                return t.map(function(t) {
                    return n.getDatasetValues(t, i)
                })
            }
            ,
            i.prototype.getDatasetValues = function(t, i) {
                if (!this.sets || !this.sets[t.dataset].values)
                    return [];
                var n, e = i.getAbsKey();
                return n = t.key.y0 ? function(i) {
                    return {
                        x: i[e],
                        y1: i[t.key.y1],
                        y0: i[t.key.y0]
                    }
                }
                : function(i) {
                    return {
                        x: i[e],
                        y1: i[t.key.y1],
                        y0: 0
                    }
                }
                ,
                this.sets[t.dataset].values.map(n)
            }
            ,
            i.getMinDistance = function(t, i, n, e) {
                return void 0 === n && (n = "x"),
                d3.min(t.map(function(t) {
                    return t.map(function(t) {
                        return i.scale(t[n])
                    }).filter(function(t) {
                        return e ? t >= e[0] && t <= e[1] : !0
                    }).reduce(function(t, i, n, e) {
                        var o = n > 0 ? i - e[n - 1] : Number.MAX_VALUE;
                        return t > o ? o : t
                    }, Number.MAX_VALUE)
                }))
            }
            ,
            i
        }();
        t.Data = i
    }(i = t.Utils || (t.Utils = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function() {
            function t() {}
            return t.debounce = function(t, i) {
                var n = this
                  , e = null ;
                return function() {
                    for (var o = [], r = 0; r < arguments.length; r++)
                        o[r - 0] = arguments[r];
                    e && window.clearTimeout(e),
                    e = window.setTimeout(function() {
                        return t.apply(n, o)
                    }, i)
                }
            }
            ,
            t
        }();
        t.FunctionUtils = i;
        var n = function() {
            function t() {}
            return t.generate = function() {
                return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function(t) {
                    var i = 16 * Math.random() | 0
                      , n = "x" === t ? i : 3 & i | 8;
                    return n.toString(16)
                })
            }
            ,
            t
        }();
        t.UUID = n
    }(i = t.Utils || (t.Utils = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function() {
            function t() {
                this.enabled = !0
            }
            return t.prototype.init = function(t, i, n) {
                this.key = t,
                this.eventMgr = i,
                this.factoryMgr = n,
                this.eventMgr.on("create." + this.key, this.create.bind(this)),
                this.eventMgr.on("update." + this.key, this.update.bind(this)),
                this.eventMgr.on("destroy." + this.key, this.destroy.bind(this))
            }
            ,
            t.prototype.on = function() {
                this.enabled = !0
            }
            ,
            t.prototype.off = function() {
                this.enabled = !1
            }
            ,
            t.prototype.isOn = function() {
                return this.enabled === !0
            }
            ,
            t.prototype.isOff = function() {
                return this.enabled === !1
            }
            ,
            t.prototype.create = function(t) {}
            ,
            t.prototype.update = function(t, i) {}
            ,
            t.prototype.destroy = function() {}
            ,
            t
        }();
        t.BaseFactory = i
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(i) {
        "use strict";
        var n = function(i) {
            function n(n) {
                i.call(this),
                this.element = n,
                this.dim = new t.Options.Dimensions
            }
            return __extends(n, i),
            n.prototype.create = function(t) {
                var i = this;
                this.dim.updateMargins(t),
                this.listenToElement(this.element, t),
                this.createRoot(),
                this.createContainer(),
                this.dim.fromParentElement(this.element.parentElement),
                this.eventMgr.on("resize", function() {
                    i.dim.fromParentElement(i.element.parentElement),
                    i.update()
                }),
                this.eventMgr.listenForDblClick(this.svg, function() {
                    i.eventMgr.trigger("zoom-pan-reset", !0)
                }, this.key),
                this.eventMgr.on("zoom-pan-reset." + this.key, function(t) {
                    i.eventMgr.triggerDataAndOptions("update")
                })
            }
            ,
            n.prototype.listenToElement = function(t, i) {
                var n = this.eventMgr;
                t.addEventListener("mouseover", function(t) {
                    n.triggerDataAndOptions.apply(n, ["container-over", t])
                }),
                t.addEventListener("mousemove", function(t) {
                    n.triggerDataAndOptions.apply(n, ["container-move", t])
                }),
                t.addEventListener("mouseout", function(t) {
                    n.triggerDataAndOptions.apply(n, ["container-out", t])
                })
            }
            ,
            n.prototype.getCoordinatesFromEvent = function(t) {
                var i = this.getDimensions()
                  , n = t.currentTarget.getBoundingClientRect()
                  , e = n.left
                  , o = n.top
                  , r = this.factoryMgr.get("x-axis")
                  , s = r.invert(t.clientX - e - i.margin.left)
                  , a = this.factoryMgr.get("y-axis")
                  , c = a.invert(t.clientY - o - i.margin.top);
                return {
                    y: c,
                    x: s
                }
            }
            ,
            n.prototype.update = function() {
                this.updateRoot(),
                this.updateContainer()
            }
            ,
            n.prototype.destroy = function() {
                this.destroyRoot()
            }
            ,
            n.prototype.createRoot = function() {
                this.svg = d3.select(this.element).append("svg").attr("class", "chart"),
                this.defs = this.svg.append("defs")
            }
            ,
            n.prototype.updateRoot = function() {
                this.svg.attr("width", this.dim.width).attr("height", this.dim.height)
            }
            ,
            n.prototype.destroyRoot = function() {
                this.svg.remove()
            }
            ,
            n.prototype.createContainer = function() {
                this.vis = this.svg.append("g").attr("class", "container"),
                this.axes = this.vis.append("g").attr("class", "axes"),
                this.clippingPathId = "clipping-path-" + t.Utils.UUID.generate(),
                this.defs.append("svg:clipPath").attr("id", this.clippingPathId).append("svg:rect").attr("id", "clipping-rect"),
                this.data = this.vis.append("g").attr({
                    "class": "data",
                    "clip-path": "url(#" + this.clippingPathId + ")"
                }),
                this.overlay = this.vis.append("g").attr("class", "overlay")
            }
            ,
            n.prototype.updateContainer = function() {
                this.vis.attr({
                    width: this.dim.innerWidth,
                    height: Math.max(this.dim.innerHeight, 0),
                    transform: "translate(" + this.dim.margin.left + ", " + this.dim.margin.top + ")"
                }),
                d3.select(this.element).select("#clipping-rect").attr({
                    width: Math.max(this.dim.innerWidth, 0),
                    height: Math.max(this.dim.innerHeight, 0)
                })
            }
            ,
            n.prototype.getDimensions = function() {
                return this.dim
            }
            ,
            n
        }(i.BaseFactory);
        i.Container = n
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(i) {
        "use strict";
        var n = function(i) {
            function n(t) {
                i.call(this),
                this.element = t
            }
            return __extends(n, i),
            n.prototype.off = function() {
                i.prototype.off.call(this),
                this.hide()
            }
            ,
            n.prototype.create = function(t) {
                this.options = t,
                this.createTooltip(),
                this.eventMgr.on("container-move.tooltip", this.show.bind(this)),
                this.eventMgr.on("container-out.tooltip", this.hide.bind(this)),
                this.eventMgr.on("outer-world-hover.tooltip", this.showFromCoordinates.bind(this)),
                this.hide()
            }
            ,
            n.prototype.update = function(t, i) {
                this.options = i
            }
            ,
            n.prototype.createTooltip = function() {
                var t = this.svg = d3.select(this.element).append("div").attr("class", "chart-tooltip");
                t.append("div").attr("class", "abscissas"),
                this.line = this.factoryMgr.get("container").overlay.append("line").attr("class", "tooltip-line"),
                this.dots = this.factoryMgr.get("container").overlay.append("g").attr("class", "tooltip-dots")
            }
            ,
            n.prototype.destroy = function() {
                this.svg.remove()
            }
            ,
            n.prototype.getClosestRows = function(t, i, n) {
                for (var e = n.series.filter(function(t) {
                    return t.visible
                }), o = e.map(function(t) {
                    return i.getDatasetValues(t, n).filter(t.defined)
                }), r = [], s = -1, a = Number.POSITIVE_INFINITY, c = [], u = 0; u < o.length; u++)
                    for (var p = 0; p < o[u].length; p++) {
                        if ("date" === n.axes.x.type)
                            var h = Math.abs(o[u][p].x.getTime() - t);
                        else
                            var h = Math.abs(o[u][p].x - t);
                        var l = e[u];
                        h === a && -1 === c.indexOf(l) ? (r.push({
                            series: l,
                            row: o[u][p]
                        }),
                        c.push(l)) : a > h && (a = h,
                        r = [{
                            series: e[u],
                            row: o[u][p]
                        }],
                        c = [l],
                        s = p)
                    }
                return {
                    rows: r,
                    index: s
                }
            }
            ,
            n.prototype.showFromCoordinates = function(t, i, n) {
                if (!this.isOff()) {
                    var e = t.x
                      , o = t.y;
                    if (void 0 === e || void 0 === o)
                        return void this.hide(void 0, i, n);
                    e instanceof Date && (e = e.getTime());
                    var r = this.getClosestRows(e, i, n)
                      , s = r.rows
                      , a = r.index;
                    if (0 === s.length)
                        return void this.hide(void 0, i, n);
                    this.updateTooltipDots(s),
                    this.dots.style("opacity", "1"),
                    this.updateLinePosition(s),
                    this.line.style("opacity", "1");
                    var c = this.getTooltipContent(s, a, n);
                    n.tooltipHook && (c = n.tooltipHook(s)),
                    c && (this.updateTooltipContent(c, a, n),
                    this.updateTooltipPosition(s),
                    this.svg.style("display", null ))
                }
            }
            ,
            n.prototype.show = function(t, i, n) {
                if (!this.isOff()) {
                    var e = this.factoryMgr.get("container")
                      , o = e.getCoordinatesFromEvent(t);
                    this.showFromCoordinates(o, i, n)
                }
            }
            ,
            n.prototype.hide = function(t, i, n) {
                this.svg.style("display", "none"),
                this.line.style("opacity", "0"),
                this.dots.style("opacity", "0"),
                n && n.tooltipHook && n.tooltipHook(void 0)
            }
            ,
            n.prototype.getTooltipContent = function(i, n, e) {
                var o = e.getByAxisSide(t.Options.AxisOptions.SIDE.X).tickFormat
                  , r = function(t) {
                    return e.getByAxisSide(t).tickFormat
                }
                  , s = function(t) {
                    var i = r(t.series.axis)
                      , e = i ? function(t) {
                        return i(t, n)
                    }
                    : function(t) {
                        return t
                    }
                      , o = e(t.row.y1);
                    return t.series.hasTwoKeys() ? "[" + e(t.row.y0) + ", " + o + "]" : o
                }
                ;
                return {
                    abscissas: o ? o(i[0].row.x, n) : i[0].row.x,
                    rows: i.map(function(t) {
                        return {
                            label: t.series.label,
                            value: s(t),
                            color: t.series.color,
                            id: t.series.id
                        }
                    })
                }
            }
            ,
            n.prototype.updateTooltipContent = function(t, i, n) {
                this.svg.select(".abscissas").text(t.abscissas);
                var e = function(t) {
                    return t.attr({
                        "class": "tooltip-item"
                    }),
                    t.append("div").attr({
                        "class": "color-dot"
                    }).style({
                        "background-color": function(t) {
                            return t.color
                        }
                    }),
                    t.append("div").attr({
                        "class": "series-label"
                    }),
                    t.append("div").attr({
                        "class": "y-value"
                    }),
                    t
                }
                  , o = function(t) {
                    return t.select(".series-label").text(function(t) {
                        return t.label
                    }),
                    t.select(".y-value").text(function(t) {
                        return t.value
                    }),
                    t
                }
                  , r = this.svg.selectAll(".tooltip-item").data(t.rows, function(t, i) {
                    return t.id ? t.id : i
                });
                r.enter().append("div").call(e).call(o),
                r.call(o),
                r.exit().remove()
            }
            ,
            n.prototype.updateTooltipDots = function(t) {
                var i = this
                  , n = this.factoryMgr.get("x-axis").scale
                  , e = function(t) {
                    return i.factoryMgr.get(t + "-axis").scale
                }
                  , o = 3
                  , r = function(t, i, n) {
                    return "M " + i + " " + n + " m -" + t + ", 0 a " + t + "," + t + " 0 1,0 " + 2 * t + ",0 a " + t + "," + t + " 0 1,0 -" + 2 * t + ",0 "
                }
                  , s = function(t) {
                    t.attr("class", "tooltip-dots-group"),
                    t.append("path").attr({
                        "class": "tooltip-dot y1"
                    }).on("click", function(t, n) {
                        i.eventMgr.trigger("click", t.row, n, t.series, i.options)
                    }),
                    t.append("path").attr({
                        "class": "tooltip-dot y0"
                    }).style({
                        display: function(t) {
                            return t.series.hasTwoKeys() ? null : "none"
                        }
                    }).on("click", function(t, n) {
                        i.eventMgr.trigger("click", t.row, n, t.series, i.options)
                    })
                }
                  , a = function(t) {
                    t.select(".tooltip-dot.y1").attr({
                        d: function(t) {
                            return r(o, n(t.row.x), e(t.series.axis)(t.row.y1))
                        },
                        stroke: function(t) {
                            return t.series.color
                        }
                    }),
                    t.select(".tooltip-dot.y0").attr({
                        d: function(t) {
                            return t.series.hasTwoKeys() ? r(o, n(t.row.x), e(t.series.axis)(t.row.y0)) : ""
                        },
                        stroke: function(t) {
                            return t.series.color
                        }
                    })
                }
                  , c = this.dots.selectAll(".tooltip-dots-group").data(t);
                c.enter().append("g").call(s).call(a),
                c.call(a),
                c.exit().remove()
            }
            ,
            n.prototype.updateTooltipPosition = function(t) {
                var i = t.slice(-1)[0]
                  , n = this.factoryMgr.get("x-axis")
                  , e = (this.factoryMgr.get("y-axis").scale,
                this.factoryMgr.get("container").getDimensions().margin)
                  , o = this.element.offsetLeft
                  , r = this.element.offsetTop
                  , s = 0
                  , a = "";
                n.isInLastHalf(i.row.x) ? (a = "translate(-100%, 0)",
                s = -10) : s = 10,
                this.svg.style({
                    left: o + e.left + n.scale(i.row.x) + s + "px",
                    top: r + e.top + "px",
                    transform: a
                })
            }
            ,
            n.prototype.updateLinePosition = function(t) {
                var i = this.factoryMgr.get("container")
                  , n = i.getDimensions()
                  , e = t.slice(-1)[0]
                  , o = this.factoryMgr.get("x-axis")
                  , r = o.scale(e.row.x);
                this.line.attr({
                    x1: r,
                    x2: r,
                    y1: -n.margin.top,
                    y2: n.innerHeight
                })
            }
            ,
            n
        }(i.BaseFactory);
        i.Tooltip = n
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function(t) {
            function i(i) {
                t.call(this),
                this.element = i
            }
            return __extends(i, t),
            i.prototype.create = function() {
                this.createLegend()
            }
            ,
            i.prototype.createLegend = function() {
                this.div = d3.select(this.element).append("div").attr("class", "chart-legend").style("position", "absolute")
            }
            ,
            i.prototype.legendClick = function() {
                var t = this;
                return function(i) {
                    return i.on("click", function(i) {
                        t.eventMgr.trigger("legend-click", i)
                    })
                }
            }
            ,
            i.prototype.update = function(t, i) {
                var n = this
                  , e = this.factoryMgr.get("container")
                  , o = (e.getDimensions(),
                function(t) {
                    var i = t.append("div").attr({
                        "class": "item"
                    }).call(n.legendClick());
                    i.append("div").attr({
                        "class": "icon"
                    }),
                    i.append("div").attr({
                        "class": "legend-label"
                    })
                }
                )
                  , r = function(t) {
                    t.attr("class", function(t) {
                        return "item " + t.type.join(" ")
                    }).classed("legend-hidden", function(t) {
                        return !t.visible
                    }),
                    t.select(".icon").style("background-color", function(t) {
                        return t.color
                    }),
                    t.select(".legend-label").text(function(t) {
                        return t.label
                    })
                }
                  , s = this.div.selectAll(".item").data(i.series);
                s.enter().call(o),
                s.call(r),
                s.exit().remove()
            }
            ,
            i.prototype.destroy = function() {
                this.div.remove()
            }
            ,
            i
        }(t.BaseFactory);
        t.Legend = i
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(i) {
        "use strict";
        var n = function(i) {
            function n(n) {
                var e = this;
                if (i.call(this),
                this.side = n,
                !t.Options.AxisOptions.isValidSide(n))
                    throw new TypeError("Wrong axis side : " + n);
                this.scale = function(t) {
                    return e._scale.call(e, t)
                }
            }
            return __extends(n, i),
            n.prototype.range = function() {
                return this._scale.range()
            }
            ,
            n.prototype.getDomain = function() {
                return this._scale.domain()
            }
            ,
            n.prototype.setDomain = function(t) {
                return this._scale.domain.call(this, t)
            }
            ,
            n.prototype.create = function() {
                var t = this.factoryMgr.get("container").axes;
                this.createAxis(t),
                this.eventMgr.on("pan." + this.key, this.softUpdate.bind(this)),
                this.eventMgr.on("zoom-end." + this.key, this.softUpdate.bind(this)),
                this.eventMgr.on("outer-world-domain-change." + this.key, this.updateFromOuterWorld.bind(this)),
                this.eventMgr.on("resize." + this.key, this.onResize.bind(this))
            }
            ,
            n.prototype.updateFromOuterWorld = function(t) {
                this.updateScaleDomain(t[this.side[0]]),
                this.softUpdate()
            }
            ,
            n.prototype.softUpdate = function() {
                this.factoryMgr.get("transitions").isOn() ? this.svg.transition().call(this.factoryMgr.getBoundFunction("transitions", "edit")).call(this.d3axis) : this.svg.call(this.d3axis)
            }
            ,
            n.prototype.onResize = function() {
                var t = this.factoryMgr.get("container")
                  , i = t.getDimensions();
                this.updateScaleRange(i, this.options),
                this.updateAxisContainer(i),
                this.softUpdate()
            }
            ,
            n.prototype.getDimensions = function() {
                var t = this.factoryMgr.get("container");
                return t.getDimensions()
            }
            ,
            n.prototype.update = function(t, i) {
                var n = this.getDimensions()
                  , e = this.getExtent(t, i);
                this.options = i.getByAxisSide(this.side),
                this._scale = this.getScale(),
                this.updateScaleRange(n, this.options),
                this.updateScaleDomain(e),
                this.d3axis = this.getAxis(this._scale, this.options),
                this.updateAxisOrientation(this.d3axis),
                this.updateAxisContainer(n),
                this.shiftAxisTicks(this.options)
            }
            ,
            n.prototype.shiftAxisTicks = function(t) {
                var i = t.ticksShift
                  , n = i.x
                  , e = i.y;
                this.svg.selectAll("text").attr("transform", "translate(" + n + ", " + e + ")")
            }
            ,
            n.prototype.destroy = function() {
                this.destroyAxis()
            }
            ,
            n.prototype.updateScaleRange = function(t, i) {
                this.isAbscissas() ? this._scale.range([i.padding.min, t.innerWidth - i.padding.max]) : this._scale.range([t.innerHeight - i.padding.min, i.padding.max])
            }
            ,
            n.prototype.updateScaleDomain = function(t) {
                this._scale.domain(t)
            }
            ,
            n.prototype.getScaleDomain = function() {
                return this._scale ? this._scale.domain() : [0, 1]
            }
            ,
            n.prototype.getExtent = function(t, i) {
                var n = i.getByAxisSide(this.side)
                  , e = void 0;
                if (this.isAbscissas()) {
                    var o = i.getVisibleDatasets()
                      , r = i.getAbsKey()
                      , s = [];
                    o.forEach(function(i) {
                        var n = t.sets[i].values;
                        s = s.concat(n.map(function(t) {
                            return t[r]
                        }))
                    }),
                    e = d3.extent(s)
                } else {
                    var a = n.includeZero ? [0] : []
                      , c = n.includeZero ? [0] : [];
                    i.getVisibleSeriesBySide(this.side).forEach(function(n) {
                        var e = t.getDatasetValues(n, i);
                        e.forEach(function(t) {
                            n.defined && !n.defined(t) || (a.push(t.y0 || t.y1),
                            c.push(t.y1))
                        })
                    }),
                    e = [d3.min(a), d3.max(c)],
                    0 === e[0] && 0 === e[1] && (e = [0, 1])
                }
                return void 0 !== n.min && (e[0] = n.min),
                void 0 !== n.max && (e[1] = n.max),
                e
            }
            ,
            n.prototype.isAbscissas = function() {
                return -1 !== [t.Options.AxisOptions.SIDE.X, t.Options.AxisOptions.SIDE.X2].indexOf(this.side)
            }
            ,
            n.prototype.isInLastHalf = function(t) {
                var i = function(t) {
                    return t
                }
                ;
                t instanceof Date && (i = function(t) {
                    return t.getTime()
                }
                );
                var n = this._scale.domain()
                  , e = n[0]
                  , o = n[1];
                return i(t) > i(e) + (i(o) - i(e)) / 2
            }
            ,
            n.prototype.createAxis = function(t) {
                this.svg = t.append("g").attr("class", "axis " + this.side + "-axis")
            }
            ,
            n.prototype.updateAxisOrientation = function(i) {
                this.isAbscissas() ? this.side === t.Options.AxisOptions.SIDE.X ? i.orient("bottom") : i.orient("top") : this.side === t.Options.AxisOptions.SIDE.Y ? i.orient("left") : i.orient("right")
            }
            ,
            n.prototype.updateAxisContainer = function(i) {
                this.isAbscissas() ? this.side === t.Options.AxisOptions.SIDE.X ? this.svg.attr("transform", "translate(0, " + i.innerHeight + ")") : this.svg.attr("transform", "translate(0, 0)") : this.side === t.Options.AxisOptions.SIDE.Y ? this.svg.attr("transform", "translate(0, 0)") : this.svg.attr("transform", "translate(" + i.innerWidth + ", 0)"),
                this.softUpdate()
            }
            ,
            n.prototype.destroyAxis = function() {
                this.svg.remove()
            }
            ,
            n.prototype.invert = function(t) {
                return this._scale.invert(t)
            }
            ,
            n.prototype.isTimeAxis = function() {
                return this.options.type === t.Options.AxisOptions.TYPE.DATE
            }
            ,
            n.prototype.getScale = function() {
                return this.options && this.options.type === t.Options.AxisOptions.TYPE.DATE ? d3.time.scale() : this.options && this.options.type === t.Options.AxisOptions.TYPE.LOG ? d3.scale.log() : d3.scale.linear()
            }
            ,
            n.prototype.getAxis = function(i, n) {
                var e;
                return e = n.hasDynamicTicks() ? t.svg.twoSpeedAxis().scale(i) : d3.svg.axis().scale(i),
                n.configure(e),
                e
            }
            ,
            n.prototype.cloneAxis = function() {
                var i;
                return i = this.options && this.options.hasDynamicTicks() ? t.svg.twoSpeedAxis().ticks(this.d3axis.ticks()) : d3.svg.axis().ticks(this.d3axis.ticks()[0]),
                i.scale(this.d3axis.scale()).orient(this.d3axis.orient()).tickValues(this.d3axis.tickValues()).tickSize(this.d3axis.tickSize())
            }
            ,
            n
        }(i.BaseFactory);
        i.Axis = n
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function(t) {
            function i() {
                t.apply(this, arguments)
            }
            return __extends(i, t),
            i.prototype.create = function() {
                this.svg = this.factoryMgr.get("container").axes.insert("g", ":first-child").attr("class", "grid"),
                this.svg.append("g").classed("x-grid", !0),
                this.svg.append("g").classed("y-grid", !0),
                this.eventMgr.on("resize." + this.key, this.softUpdate.bind(this)),
                this.eventMgr.on("pan." + this.key, this.softUpdate.bind(this)),
                this.eventMgr.on("zoom-end." + this.key, this.softUpdate.bind(this)),
                this.eventMgr.on("outer-world-domain-change." + this.key, this.softUpdate.bind(this))
            }
            ,
            i.prototype.softUpdate = function() {
                var t = this.factoryMgr.get("container")
                  , i = t.getDimensions();
                if (this.xAxis) {
                    var n = this.svg.select(".x-grid");
                    this.factoryMgr.get("transitions").isOn() && (n = n.transition().call(this.factoryMgr.getBoundFunction("transitions", "edit"))),
                    n.attr("transform", "translate(0, " + i.innerHeight + ")").call(this.xAxis.tickSize(-i.innerHeight, 0));
                }
                if (this.yAxis) {
                    var n = this.svg.select(".y-grid");
                    this.factoryMgr.get("transitions").isOn() && (n = n.transition().call(this.factoryMgr.getBoundFunction("transitions", "edit"))),
                    n.call(this.yAxis.tickSize(-i.innerWidth, 0))
                }
            }
            ,
            i.prototype.update = function(t, i) {
                var n = this.factoryMgr.get("container")
                  , e = n.getDimensions();
                i.grid.x && (this.xAxis = this.factoryMgr.get("x-axis").cloneAxis().tickSize(-e.innerHeight, 0),
                this.svg.select(".x-grid").transition().call(this.factoryMgr.getBoundFunction("transitions", "edit")).attr("transform", "translate(0, " + e.innerHeight + ")").call(this.xAxis)),
                i.grid.y && (this.yAxis = this.factoryMgr.get("y-axis").cloneAxis(),
                this.svg.select(".y-grid").transition().call(this.factoryMgr.getBoundFunction("transitions", "edit")).call(this.yAxis.tickSize(-e.innerWidth, 0)))
            }
            ,
            i.prototype.destroy = function() {
                this.svg.remove()
            }
            ,
            i
        }(t.BaseFactory);
        t.Grid = i
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function(t) {
            function i() {
                t.apply(this, arguments),
                this.isActive = !1
            }
            return __extends(i, t),
            i.prototype.create = function() {
                this.rect = this.factoryMgr.get("container").svg.append("rect").attr("class", "chart-brush")
            }
            ,
            i.prototype.constrainOutgoingDomains = function(t) {
                this.zoomOnX || delete t.x,
                this.zoomOnY || delete t.y
            }
            ,
            i.prototype.update = function(t, i) {
                var n = this.factoryMgr.get("container").getDimensions()
                  , e = n.margin
                  , o = e.left
                  , r = e.top;
                this.zoomOnX = i.zoom.x,
                this.zoomOnY = i.zoom.y,
                (this.zoomOnX || this.zoomOnY) && (this.xStartFn = this.zoomOnX ? function(t) {
                    return t
                }
                : function(t) {
                    return o
                }
                ,
                this.xEndFn = this.zoomOnX ? function(t) {
                    return t
                }
                : function(t) {
                    return n.innerWidth + o
                }
                ,
                this.yStartFn = this.zoomOnY ? function(t) {
                    return t
                }
                : function(t) {
                    return r
                }
                ,
                this.yEndFn = this.zoomOnY ? function(t) {
                    return t
                }
                : function(t) {
                    return n.innerHeight + r
                }
                ,
                this.registerEvents(this.factoryMgr.get("container")))
            }
            ,
            i.prototype.show = function(t) {
                var i = t.xStart
                  , n = t.xEnd
                  , e = t.yStart
                  , o = t.yEnd;
                r = i > n ? [n, i] : [i, n],
                i = r[0],
                n = r[1],
                s = e > o ? [o, e] : [e, o],
                e = s[0],
                o = s[1],
                this.rect.attr({
                    x: i,
                    width: n - i,
                    y: e,
                    height: o - e
                }).style("opacity", "1");
                var r, s
            }
            ,
            i.prototype.hide = function() {
                this.rect.style("opacity", "0")
            }
            ,
            i.prototype.updateAxes = function(t) {
                var i = t.xStart
                  , n = t.xEnd
                  , e = t.yStart
                  , o = t.yEnd;
                d = i > n ? [n, i] : [i, n],
                i = d[0],
                n = d[1],
                f = e > o ? [o, e] : [e, o],
                e = f[0],
                o = f[1];
                var r = this.factoryMgr.get("container").getDimensions()
                  , s = r.margin
                  , a = s.left
                  , c = s.top
                  , u = this.factoryMgr.get("x-axis")
                  , p = this.factoryMgr.get("x2-axis");
                u.setDomain([u.invert(i - a), u.invert(n - a)]),
                p.setDomain(u.getDomain());
                var h = this.factoryMgr.get("y-axis")
                  , l = this.factoryMgr.get("y2-axis");
                h.setDomain([h.invert(o - c), h.invert(e - c)]),
                l.setDomain([l.invert(o - c), l.invert(e - c)]);
                var d, f
            }
            ,
            i.prototype.registerEvents = function(t) {
                var i, n, e, o, r, s = this, a = function(t) {
                    return t + "." + s.key
                }
                , c = function() {
                    s.isActive = !1,
                    s.hide(),
                    void 0 !== n && void 0 !== o && (s.updateAxes({
                        xStart: i,
                        xEnd: n,
                        yStart: e,
                        yEnd: o
                    }),
                    s.eventMgr.trigger("zoom-end"),
                    i = n = e = o = void 0,
                    r()),
                    s.eventMgr.on(a("window-mouseup"), null )
                }
                ;
                t.svg.on(a("mousedown"), function() {
                    var t = d3.event;
                    t.altKey && (r = s.factoryMgr.turnFactoriesOff(["tooltip"]),
                    s.isActive = !0,
                    s.eventMgr.on(a("window-mouseup"), c),
                    n = d3.mouse(t.currentTarget),
                    i = n[0],
                    e = n[1],
                    i = s.xStartFn(i),
                    e = s.yStartFn(e));
                    var n
                }).on(a("mousemove"), function() {
                    s.isActive && (t = d3.mouse(d3.event.currentTarget),
                    n = t[0],
                    o = t[1],
                    n = s.xEndFn(n),
                    o = s.yEndFn(o),
                    s.show({
                        xStart: i,
                        xEnd: n,
                        yStart: e,
                        yEnd: o
                    }),
                    s.eventMgr.trigger("zoom"));
                    var t
                })
            }
            ,
            i
        }(t.BaseFactory);
        t.Zoom = i
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function(t) {
            function i() {
                t.apply(this, arguments),
                this.isActive = !1,
                this.hasMoved = !1
            }
            return __extends(i, t),
            i.prototype.constrainOutgoingDomains = function(t) {
                this.panOnX || delete t.x,
                this.panOnY || delete t.y
            }
            ,
            i.prototype.move = function(t, i) {
                if (0 !== t) {
                    var n = this.factoryMgr.get("x-axis")
                      , e = this.factoryMgr.get("x2-axis");
                    n.setDomain(n.range().map(function(i) {
                        return i + t
                    }).map(n.invert, n)),
                    e.setDomain(n.getDomain())
                }
                if (0 !== i) {
                    var o = this.factoryMgr.get("y-axis")
                      , r = this.factoryMgr.get("y2-axis");
                    o.setDomain(o.range().map(function(t) {
                        return t + i
                    }).map(o.invert, o)),
                    r.setDomain(r.range().map(function(t) {
                        return t + i
                    }).map(r.invert, r))
                }
            }
            ,
            i.prototype.update = function(t, i) {
                var n = this;
                this.panOnX = i.pan.x,
                this.panOnY = i.pan.y;
                var e, o, r, s = this.factoryMgr.get("container"), a = function(t) {
                    return t + "." + n.key
                }
                , c = function() {
                    n.hasMoved && n.eventMgr.trigger("pan-end"),
                    r && r(),
                    n.isActive = n.hasMoved = !1,
                    r = void 0,
                    n.eventMgr.on(a("window-mouseup"), null ),
                    n.eventMgr.on(a("window-mousemove"), null )
                }
                , u = function() {
                    if (n.isActive) {
                        var t = d3.mouse(s.svg.node())
                          , i = t[0]
                          , a = t[1]
                          , c = n.panOnX ? e - i : 0
                          , u = n.panOnY ? o - a : 0;
                        0 === c && 0 === u || (r || (r = n.factoryMgr.turnFactoriesOff(["tooltip", "transitions"])),
                        n.hasMoved = !0,
                        n.move(c, u),
                        n.eventMgr.trigger("pan")),
                        p = [i, a],
                        e = p[0],
                        o = p[1]
                    }
                    var p
                }
                ;
                s.svg.on(a("mousedown"), function() {
                    var t = d3.event;
                    t.altKey || (n.isActive = !0,
                    i = d3.mouse(t.currentTarget),
                    e = i[0],
                    o = i[1],
                    n.eventMgr.on(a("window-mouseup"), c),
                    n.eventMgr.on(a("window-mousemove"), u));
                    var i
                })
            }
            ,
            i
        }(t.BaseFactory);
        t.Pan = i
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function(t) {
            function i() {
                t.apply(this, arguments),
                this.duration = i.defaultDuration,
                this.ease = "cubic"
            }
            return __extends(i, t),
            i.prototype.off = function() {
                t.prototype.off.call(this),
                this.duration = 0
            }
            ,
            i.prototype.on = function() {
                t.prototype.on.call(this),
                this.duration = i.defaultDuration
            }
            ,
            i.prototype.enter = function(t) {
                var i = this.duration
                  , n = this.ease
                  , e = t[0].length
                  , o = function(t, n) {
                    return e ? n / e * i : 0
                }
                ;
                t.duration(i).delay(o).ease(n)
            }
            ,
            i.prototype.edit = function(t) {
                var i = this.duration
                  , n = this.ease
                  , e = 0;
                t.duration(i).delay(e).ease(n)
            }
            ,
            i.prototype.exit = function(t) {
                var i = this.duration
                  , n = this.ease
                  , e = 0;
                t.duration(i).delay(e).ease(n)
            }
            ,
            i.defaultDuration = 250,
            i
        }(t.BaseFactory);
        t.Transition = i
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(i) {
        var n;
        !function(i) {
            "use strict";
            var n = function(t) {
                function i() {
                    t.apply(this, arguments)
                }
                return __extends(i, t),
                i.prototype.create = function() {
                    this.createContainer(this.factoryMgr.get("container").data),
                    this.eventMgr.on("data-update." + this.type, this.update.bind(this)),
                    this.eventMgr.on("pan." + this.type, this.softUpdate.bind(this)),
                    this.eventMgr.on("zoom-end." + this.type, this.softUpdate.bind(this)),
                    this.eventMgr.on("outer-world-domain-change." + this.key, this.softUpdate.bind(this)),
                    this.eventMgr.on("resize." + this.type, this.softUpdate.bind(this))
                }
                ,
                i.prototype.update = function(t, i) {
                    this.data = t,
                    this.options = i,
                    this.softUpdate()
                }
                ,
                i.prototype.getAxes = function(t) {
                    return {
                        xAxis: this.factoryMgr.get("x-axis"),
                        yAxis: this.factoryMgr.get(t.axis + "-axis")
                    }
                }
                ,
                i.prototype.softUpdate = function() {
                    var t = this.options.getSeriesByType(this.type).filter(function(t) {
                        return t.visible
                    });
                    this.updateSeriesContainer(t)
                }
                ,
                i.prototype.destroy = function() {
                    this.svg.remove()
                }
                ,
                i.prototype.createContainer = function(t) {
                    this.svg = t.append("g").attr("class", this.type + i.containerClassSuffix)
                }
                ,
                i.prototype.updateSeriesContainer = function(t) {
                    var n = this
                      , e = this.svg.selectAll("." + this.type + i.seriesClassSuffix).data(t, function(t) {
                        return t.id
                    });
                    e.enter().append("g").attr({
                        "class": function(t) {
                            return n.type + i.seriesClassSuffix + " " + t.id
                        }
                    }),
                    this.styleSeries(e),
                    this.updateSeries(e, t),
                    e.exit().remove()
                }
                ,
                i.prototype.updateSeries = function(t, i) {
                    var n = this;
                    t.each(function(t, e) {
                        var o = d3.select(this);
                        n.updateData(o, t, e, i.length)
                    })
                }
                ,
                i.prototype.updateData = function(t, i, n, e) {}
                ,
                i.prototype.styleSeries = function(t) {}
                ,
                i.containerClassSuffix = "-data",
                i.seriesClassSuffix = "-series",
                i
            }(t.Factory.BaseFactory);
            i.SeriesFactory = n
        }(n = i.Series || (i.Series = {}))
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(i) {
        var n;
        !function(n) {
            "use strict";
            var e = function(i) {
                function n() {
                    i.apply(this, arguments),
                    this.type = t.Options.SeriesOptions.TYPE.DOT
                }
                return __extends(n, i),
                n.prototype.updateData = function(t, i, n, e) {
                    var o = this.getAxes(i)
                      , r = o.xAxis
                      , s = o.yAxis
                      , a = this.data.getDatasetValues(i, this.options).filter(i.defined)
                      , c = 2
                      , u = t.selectAll("." + this.type).data(a, function(t) {
                        return "" + t.x
                    })
                      , p = function(t) {
                        t.attr({
                            r: function(t) {
                                return c
                            },
                            cx: function(t) {
                                return r.scale(t.x)
                            },
                            cy: function(t) {
                                return s.range()[0]
                            }
                        })
                    }
                      , h = function(t) {
                        t.attr({
                            cx: function(t) {
                                return r.scale(t.x)
                            },
                            cy: function(t) {
                                return s.scale(t.y1)
                            }
                        }).style("opacity", i.visible ? 1 : 0)
                    }
                    ;
                    this.factoryMgr.get("transitions").isOn() ? (u.enter().append("circle").attr("class", this.type).call(this.eventMgr.datumEnter(i, this.options)).call(this.eventMgr.datumOver(i, this.options)).call(this.eventMgr.datumMove(i, this.options)).call(this.eventMgr.datumLeave(i, this.options)).call(p).transition().call(this.factoryMgr.getBoundFunction("transitions", "enter")).call(h),
                    u.transition().call(this.factoryMgr.getBoundFunction("transitions", "edit")).call(h),
                    u.exit().transition().call(this.factoryMgr.getBoundFunction("transitions", "exit")).call(p).each("end", function() {
                        d3.select(this).remove()
                    })) : (u.enter().append("circle").attr("class", this.type).call(this.eventMgr.datumEnter(i, this.options)).call(this.eventMgr.datumOver(i, this.options)).call(this.eventMgr.datumMove(i, this.options)).call(this.eventMgr.datumLeave(i, this.options)).call(h),
                    u.call(h),
                    u.exit().remove())
                }
                ,
                n.prototype.styleSeries = function(t) {
                    t.style({
                        stroke: function(t) {
                            return t.color
                        }
                    })
                }
                ,
                n
            }(i.Series.SeriesFactory);
            n.Dot = e
        }(n = i.Series || (i.Series = {}))
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(i) {
        var n;
        !function(n) {
            "use strict";
            var e = function(i) {
                function n() {
                    i.apply(this, arguments),
                    this.type = t.Options.SeriesOptions.TYPE.LINE
                }
                return __extends(n, i),
                n.prototype.updateData = function(t, i, n, e) {
                    t.classed("dashed", i.isDashed());
                    var o = this.getAxes(i)
                      , r = o.xAxis
                      , s = o.yAxis
                      , a = this.data.getDatasetValues(i, this.options)
                      , c = d3.svg.line().defined(i.defined).x(function(t) {
                        return r.scale(t.x)
                    }).y(s.range()[0]).interpolate(i.interpolation.mode).tension(i.interpolation.tension)
                      , u = d3.svg.line().defined(i.defined).x(function(t) {
                        return r.scale(t.x)
                    }).y(function(t) {
                        return s.scale(t.y1)
                    }).interpolate(i.interpolation.mode).tension(i.interpolation.tension)
                      , p = t.selectAll("." + this.type).data([a]);
                    this.factoryMgr.get("transitions").isOn() ? (p.enter().append("path").attr("class", this.type).attr("d", function(t) {
                        return c(t)
                    }).transition().call(this.factoryMgr.getBoundFunction("transitions", "enter")).attr("d", function(t) {
                        return u(t)
                    }),
                    p.transition().call(this.factoryMgr.getBoundFunction("transitions", "edit")).attr("d", function(t) {
                        return u(t)
                    }).style("opacity", i.visible ? 1 : 0),
                    p.exit().transition().call(this.factoryMgr.getBoundFunction("transitions", "exit")).attr("d", function(t) {
                        return c(t)
                    }).each("end", function() {
                        d3.select(this).remove()
                    })) : (p.enter().append("path").attr("class", this.type).attr("d", function(t) {
                        return u(t)
                    }),
                    p.attr("d", function(t) {
                        return u(t)
                    }).style("opacity", i.visible ? 1 : 0),
                    p.exit().remove())
                }
                ,
                n.prototype.styleSeries = function(t) {
                    t.style({
                        fill: "none",
                        stroke: function(t) {
                            return t.color
                        },
                        "stroke-dasharray": function(t) {
                            return t.isDashed() ? "10,3" : void 0
                        }
                    })
                }
                ,
                n
            }(i.Series.SeriesFactory);
            n.Line = e
        }(n = i.Series || (i.Series = {}))
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(i) {
        var n;
        !function(n) {
            "use strict";
            var e = function(i) {
                function n() {
                    i.apply(this, arguments),
                    this.type = t.Options.SeriesOptions.TYPE.AREA
                }
                return __extends(n, i),
                n.prototype.updateData = function(t, i, n, e) {
                    var o = this.getAxes(i)
                      , r = o.xAxis
                      , s = o.yAxis
                      , a = this.data.getDatasetValues(i, this.options)
                      , c = d3.svg.area().defined(i.defined).x(function(t) {
                        return r.scale(t.x)
                    }).y0(s.range()[0]).y1(s.range()[0]).interpolate(i.interpolation.mode).tension(i.interpolation.tension)
                      , u = d3.svg.area().defined(i.defined).x(function(t) {
                        return r.scale(t.x)
                    }).y0(function(t) {
                        return isNaN(s.scale(t.y0)) ? s.range()[0] : s.scale(t.y0)
                    }).y1(function(t) {
                        return s.scale(t.y1)
                    }).interpolate(i.interpolation.mode).tension(i.interpolation.tension)
                      , p = t.selectAll("." + this.type).data([a]);
                    this.factoryMgr.get("transitions").isOn() ? (p.enter().append("path").attr("class", this.type).attr("d", function(t) {
                        return c(t)
                    }).transition().call(this.factoryMgr.getBoundFunction("transitions", "enter")).attr("d", function(t) {
                        return u(t)
                    }),
                    p.transition().call(this.factoryMgr.getBoundFunction("transitions", "edit")).attr("d", function(t) {
                        return u(t)
                    }).style("opacity", i.visible ? 1 : 0),
                    p.exit().transition().call(this.factoryMgr.getBoundFunction("transitions", "exit")).attr("d", function(t) {
                        return c(t)
                    }).each("end", function() {
                        d3.select(this).remove()
                    })) : (p.enter().append("path").attr("class", this.type).attr("d", function(t) {
                        return u(t)
                    }),
                    p.attr("d", function(t) {
                        return u(t)
                    }).style("opacity", i.visible ? 1 : 0),
                    p.exit().remove())
                }
                ,
                n.prototype.styleSeries = function(t) {
                    t.style({
                        fill: function(t) {
                            return t.color
                        },
                        stroke: function(t) {
                            return t.color
                        }
                    })
                }
                ,
                n
            }(i.Series.SeriesFactory);
            n.Area = e
        }(n = i.Series || (i.Series = {}))
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(i) {
        var n;
        !function(n) {
            "use strict";
            var e = function(i) {
                function n() {
                    i.apply(this, arguments),
                    this.type = t.Options.SeriesOptions.TYPE.COLUMN,
                    this.gapFactor = .2,
                    this.outerPadding = this.gapFactor / 2 * 3,
                    this.columnsWidth = 0
                }
                return __extends(n, i),
                n.prototype.softUpdate = function() {
                    var t = this.options.getSeriesByType(this.type).filter(function(t) {
                        return t.visible
                    });
                    this.updateColumnsWidth(t, this.options),
                    this.updateColumnScale(t, this.options),
                    this.updateSeriesContainer(t)
                }
                ,
                n.prototype.update = function(t, i) {
                    this.data = t,
                    this.options = i;
                    var n = i.getSeriesByType(this.type).filter(function(t) {
                        return t.visible
                    });
                    this.updateColumnsWidth(n, i),
                    this.updateColumnScale(n, i),
                    this.updateSeriesContainer(n)
                }
                ,
                n.prototype.updateColumnsWidth = function(i, n) {
                    var e = this.factoryMgr.get("x-axis")
                      , o = this.data.getDatasets(i, n)
                      , r = t.Utils.Data.getMinDistance(o, e, "x");
                    this.columnsWidth = r < Number.MAX_VALUE ? r / i.length : 10
                }
                ,
                n.prototype.updateColumnScale = function(t, i) {
                    var n = this.columnsWidth * t.length / 2;
                    this.innerXScale = d3.scale.ordinal().domain(t.map(function(t) {
                        return t.id
                    })).rangeBands([-n, n], 0, .1)
                }
                ,
                n.prototype.getTooltipPosition = function(t) {
                    return this.innerXScale(t.id) + this.innerXScale.rangeBand() / 2
                }
                ,
                n.prototype.updateData = function(t, i, n, e) {
                    var o = this
                      , r = this.getAxes(i)
                      , s = r.xAxis
                      , a = r.yAxis
                      , c = this.data.getDatasetValues(i, this.options).filter(i.defined)
                      , u = function(t) {
                        return s.scale(t.x) + o.innerXScale(i.id)
                    }
                      , p = function(t) {
                        t.attr({
                            x: u,
                            y: function(t) {
                                return a.scale(t.y0)
                            },
                            width: o.innerXScale.rangeBand(),
                            height: 0
                        })
                    }
                      , h = function(t) {
                        t.attr({
                            x: u,
                            y: function(t) {
                                return t.y1 > 0 ? a.scale(t.y1) : a.scale(t.y0)
                            },
                            width: o.innerXScale.rangeBand(),
                            height: function(t) {
                                return Math.abs(a.scale(t.y0) - a.scale(t.y1))
                            }
                        }).style("opacity", i.visible ? 1 : 0)
                    }
                      , l = t.selectAll("." + this.type).data(c, function(t) {
                        return "" + t.x
                    });
                    this.factoryMgr.get("transitions").isOn() ? (l.enter().append("rect").attr("class", this.type).call(this.eventMgr.datumEnter(i, this.options)).call(this.eventMgr.datumOver(i, this.options)).call(this.eventMgr.datumMove(i, this.options)).call(this.eventMgr.datumLeave(i, this.options)).call(p).transition().call(this.factoryMgr.getBoundFunction("transitions", "enter")).call(h),
                    l.transition().call(this.factoryMgr.getBoundFunction("transitions", "edit")).call(h),
                    l.exit().transition().call(this.factoryMgr.getBoundFunction("transitions", "exit")).call(p).each("end", function() {
                        d3.select(this).remove()
                    })) : (l.enter().append("rect").attr("class", this.type).call(this.eventMgr.datumEnter(i, this.options)).call(this.eventMgr.datumOver(i, this.options)).call(this.eventMgr.datumMove(i, this.options)).call(this.eventMgr.datumLeave(i, this.options)).call(h),
                    l.call(h),
                    l.exit().remove())
                }
                ,
                n.prototype.styleSeries = function(t) {
                    t.style({
                        fill: function(t) {
                            return t.color
                        },
                        stroke: function(t) {
                            return t.color
                        },
                        "stroke-width": 1
                    })
                }
                ,
                n
            }(i.Series.SeriesFactory);
            n.Column = e
        }(n = i.Series || (i.Series = {}))
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    var i;
    !function(t) {
        "use strict";
        var i = function(t) {
            function i(i, n, e) {
                t.call(this),
                this.scope = i,
                this.attributes = n,
                this.$parse = e
            }
            return __extends(i, t),
            i.prototype.create = function() {
                this.unregisteringFunctions = [],
                this.sanitizeAttributes(),
                this.syncTooltips(),
                this.syncDomainsChange(),
                this.syncDatumEvents()
            }
            ,
            i.prototype.sanitizeAttributes = function() {
                var t = this.attributes
                  , i = t.tooltipSyncKey
                  , n = t.domainsSyncKey;
                if (i && n && i === n)
                    throw new Error("Heterogeneous sync keys can't have the same value.")
            }
            ,
            i.prototype.syncDatumEvents = function() {
                var t = this
                  , i = this.eventMgr;
                if (this.attributes.onClick) {
                    var n = this.$parse(this.attributes.onClick);
                    i.on("click.sync-layer", function(i, e, o, r) {
                        n(t.scope.$parent, {
                            row: i,
                            index: e,
                            series: o,
                            options: r
                        })
                    })
                }
            }
            ,
            i.prototype.syncTooltips = function() {
                var t = this
                  , i = this.eventMgr;
                this.attributes.tooltipSyncKey && (this.unregisteringFunctions.push(this.scope.$root.$on(this.attributes.tooltipSyncKey, function(t, n) {
                    i.triggerDataAndOptions("outer-world-hover", n)
                })),
                i.on("container-move.sync-layer", function(i) {
                    t.scope.$emit(t.attributes.tooltipSyncKey, t.factoryMgr.get("container").getCoordinatesFromEvent(i))
                }),
                i.on("container-out.sync-layer", function() {
                    t.scope.$emit(t.attributes.tooltipSyncKey, {
                        x: void 0,
                        y: void 0
                    })
                }))
            }
            ,
            i.prototype.syncDomainsChange = function() {
                var t = this
                  , i = this.eventMgr
                  , n = []
                  , e = this.factoryMgr.get("x-axis")
                  , o = this.factoryMgr.get("y-axis");
                if (this.attributes.onDomainsChange) {
                    var r = this.$parse(this.attributes.onDomainsChange);
                    n.push(function(i, n) {
                        var e = n.isEndEvent;
                        e && r(t.scope.$parent, {
                            $domains: i
                        })
                    })
                }
                this.attributes.domainsSyncKey && (this.unregisteringFunctions.push(this.scope.$root.$on(this.attributes.domainsSyncKey, function(n, r, s) {
                    n.targetScope !== t.scope && (r.x && r.y || (r = angular.copy(r)),
                    r.x || (r.x = e.getScaleDomain()),
                    r.y || (r.y = o.getScaleDomain()),
                    "zoom-end" === s ? (i.trigger("outer-world-domain-change", r),
                    t.factoryMgr.turnFactoriesOn(["tooltip"])) : "zoom" === s ? t.factoryMgr.turnFactoriesOff(["tooltip"]) : "pan" === s || "pan-end" === s ? (t.factoryMgr.turnFactoriesOff(["transitions", "tooltip"]),
                    i.trigger("outer-world-domain-change", r),
                    "pan-end" === s && t.factoryMgr.turnFactoriesOn(["transitions", "tooltip"])) : "zoom-pan-reset" === s && i.trigger("zoom-pan-reset", !1))
                })),
                n.push(function(i, n) {
                    var e = n.type;
                    t.scope.$emit(t.attributes.domainsSyncKey, i, e)
                }));
                var s = function() {
                    return {
                        x: e.getScaleDomain(),
                        y: o.getScaleDomain()
                    }
                }
                  , a = function(t, i) {
                    return n.forEach(function(n) {
                        return n(t, i)
                    })
                }
                ;
                i.on("pan.sync-layer", function() {
                    var i = s();
                    t.factoryMgr.get("pan").constrainOutgoingDomains(i),
                    a(i, {
                        type: "pan"
                    })
                }),
                i.on("pan-end.sync-layer", function() {
                    var i = s();
                    t.factoryMgr.get("pan").constrainOutgoingDomains(i),
                    a(i, {
                        type: "pan-end",
                        isEndEvent: !0
                    })
                }),
                i.on("zoom.sync-layer", function() {
                    var i = s();
                    t.factoryMgr.get("zoom").constrainOutgoingDomains(i),
                    a(i, {
                        type: "zoom",
                        isEndEvent: !1
                    })
                }),
                i.on("zoom-end.sync-layer", function() {
                    var i = s();
                    t.factoryMgr.get("zoom").constrainOutgoingDomains(i),
                    a(i, {
                        type: "zoom-end",
                        isEndEvent: !0
                    })
                }),
                i.on("zoom-pan-reset.sync-layer", function(t) {
                    t && a(s(), {
                        type: "zoom-pan-reset",
                        isEndEvent: !0
                    })
                })
            }
            ,
            i.prototype.destroy = function() {
                for (var t; t = this.unregisteringFunctions.pop(); )
                    t()
            }
            ,
            i
        }(t.BaseFactory);
        t.SyncLayer = i
    }(i = t.Factory || (t.Factory = {}))
}(n3Charts || (n3Charts = {}));
var n3Charts;
!function(t) {
    "use strict";
    var i = function() {
        function i(i, n, e, o) {
            var r = this;
            this.$window = i,
            this.$parse = n,
            this.$timeout = e,
            this.$rootScope = o,
            this.scope = {
                data: "=",
                options: "=",
                styles: "=",
                hoveredCoordinates: "="
            },
            this.restrict = "E",
            this.replace = !0,
            this.template = "<div></div>",
            this.link = function(i, n, e) {
                var o, s, a = new t.Utils.EventManager, c = new t.Utils.FactoryManager;
                a.init(t.Utils.EventManager.EVENTS),
                c.registerMany([["container", t.Factory.Container, n[0]], ["tooltip", t.Factory.Tooltip, n[0]], ["legend", t.Factory.Legend, n[0]], ["transitions", t.Factory.Transition], ["x-axis", t.Factory.Axis, t.Options.AxisOptions.SIDE.X], ["x2-axis", t.Factory.Axis, t.Options.AxisOptions.SIDE.X2], ["y-axis", t.Factory.Axis, t.Options.AxisOptions.SIDE.Y], ["y2-axis", t.Factory.Axis, t.Options.AxisOptions.SIDE.Y2], ["grid", t.Factory.Grid], ["pan", t.Factory.Pan], ["zoom", t.Factory.Zoom], ["sync-layer", t.Factory.SyncLayer, i, e, r.$parse], ["series-area", t.Factory.Series.Area], ["series-column", t.Factory.Series.Column], ["series-line", t.Factory.Series.Line], ["series-dot", t.Factory.Series.Dot]]),
                c.all().forEach(function(t) {
                    return t.instance.init(t.key, a, c)
                });
                var u = void 0 === i.options
                  , p = function() {
                    s = new t.Options.Options(angular.copy(i.options)),
                    o = new t.Utils.Data(i.data),
                    u && (u = !1,
                    a.trigger("create", s)),
                    a.update(o, s),
                    a.trigger("update", o, s)
                }
                ;
                u || a.trigger("create", new t.Options.Options(angular.copy(i.options)));
                var h = function(t) {
                    t && (o.fromJS(t),
                    c.turnFactoriesOff(["transitions"]),
                    a.trigger("data-update", o, s),
                    c.turnFactoriesOn(["transitions"]),
                    a.trigger("update", o, s))
                }
                ;
                i.$watch("options", p, !0),
                i.$watch("data", h, !0),
                a.on("legend-click.directive", function(t) {
                    var n = i.options.series.filter(function(i) {
                        return i.id === t.id
                    })[0];
                    n.visible = t.getToggledVisibility(),
                    i.$apply()
                }),
                a.on("pan.directive", function() {
                    c.get("container").svg.classed("panning", !0)
                }),
                a.on("pan-end.directive", function() {
                    c.get("container").svg.classed("panning", !1)
                });
                var l = function() {
                    if (!n || !n[0])
                        return {};
                    var t = n[0].parentElement.getBoundingClientRect();
                    return {
                        height: t.height,
                        width: t.width,
                        left: t.left,
                        right: t.right,
                        bottom: t.bottom,
                        top: t.top
                    }
                }
                  , d = t.Utils.FunctionUtils.debounce(function() {
                    a.trigger("resize", n[0].parentElement)
                }, 50);
                i.$watch(l, d, !0);
                var f = t.Utils.FunctionUtils.debounce(function() {
                    return i.$apply()
                }, 50);
                angular.element(r.$window).on("resize", f),
                i.$on("$destroy", function() {
                    a.trigger("destroy"),
                    angular.element(r.$window).off("resize", f)
                })
            }
        }
        return i
    }();
    t.LineChart = i
}(n3Charts || (n3Charts = {})),
angular.module("n3-line-chart", []).directive("linechart", ["$window", "$parse", "$timeout", "$rootScope", function(t, i, n, e) {
    return new n3Charts.LineChart(t,i,n,e)
}
]);
