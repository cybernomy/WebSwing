/**
 * @license RequireJS i18n 2.0.6 Copyright (c) 2010-2012, The Dojo Foundation All Rights Reserved.
 * Available via the MIT or new BSD license.
 * see: http://github.com/requirejs/i18n for details
 */
/*jslint regexp: true */
/*global require: false, navigator: false, define: false */

/**
 * This plugin handles i18n! prefixed modules. It does the following:
 *
 * 1) A regular module can have a dependency on an i18n bundle, but the regular
 * module does not want to specify what locale to load. So it just specifies
 * the top-level bundle, like 'i18n!nls/colors'.
 *
 * This plugin will load the i18n bundle at nls/colors, see that it is a root/master
 * bundle since it does not have a locale in its name. It will then try to find
 * the best match locale available in that master bundle, then request all the
 * locale pieces for that best match locale. For instance, if the locale is 'en-us',
 * then the plugin will ask for the 'en-us', 'en' and 'root' bundles to be loaded
 * (but only if they are specified on the master bundle).
 *
 * Once all the bundles for the locale pieces load, then it mixes in all those
 * locale pieces into each other, then finally sets the context.defined value
 * for the nls/colors bundle to be that mixed in locale.
 *
 * 2) A regular module specifies a specific locale to load. For instance,
 * i18n!nls/fr-fr/colors. In this case, the plugin needs to load the master bundle
 * first, at nls/colors, then figure out what the best match locale is for fr-fr,
 * since maybe only fr or just root is defined for that locale. Once that best
 * fit is found, all of its locale pieces need to have their bundles loaded.
 *
 * Once all the bundles for the locale pieces load, then it mixes in all those
 * locale pieces into each other, then finally sets the context.defined value
 * for the nls/fr-fr/colors bundle to be that mixed in locale.
 */
(function () {
    'use strict';

    //regexp for reconstructing the master bundle name from parts of the regexp match
    //nlsRegExp.exec('foo/bar/baz/nls/en-ca/foo') gives:
    //['foo/bar/baz/nls/en-ca/foo', 'foo/bar/baz/nls/', '/', '/', 'en-ca', 'foo']
    //nlsRegExp.exec('foo/bar/baz/nls/foo') gives:
    //['foo/bar/baz/nls/foo', 'foo/bar/baz/nls/', '/', '/', 'foo', '']
    //so, if match[5] is blank, it means this is the top bundle definition.
    var nlsRegExp = /(^.*(^|\/)nls(\/|$))([^\/]*)\/?([^\/]*)/;

    //Helper function to avoid repeating code. Lots of arguments in the
    //desire to stay functional and support RequireJS contexts without having
    //to know about the RequireJS contexts.
    function addPart(locale, master, needed, toLoad, prefix, suffix) {
        needed.push(locale);
        if (typeof master[locale] !== "object") {
            toLoad.push(prefix + locale + '/' + suffix);
        }
    }

    function exists(req, fullName) {
        if (require._fileExists(req.toUrl(fullName + '.js'))) {
            return true;
        } else {
            console.log("Warning: '" + fullName + ".js' does not exist");
        }
    }

    /**
     * Simple function to mix in properties from source into target,
     * but only if target does not already have a property of the same name.
     * This is not robust in IE for transferring methods that match
     * Object.prototype names, but the uses of mixin here seem unlikely to
     * trigger a problem related to that.
     */
    function mixin(target, source, force) {
        var prop;
        for (prop in source) {
            if (source.hasOwnProperty(prop) && (!target.hasOwnProperty(prop) || force)) {
                target[prop] = source[prop];
            } else if (typeof source[prop] === 'object') {
                if (!target[prop] && source[prop]) {
                    target[prop] = {};
                }
                mixin(target[prop], source[prop], force);
            }
        }
    }

    define(['module'], function (module) {
        var masterConfig = module.config ? module.config() : {};

        return {
            version: '2.0.6.optional',
            /**
             * Called when a dependency needs to be loaded.
             */
            load: function (name, req, onLoad, config) {
                config = config || {};

                if (config.locale) {
                    masterConfig.locale = config.locale;
                }

                var masterName,
                    match = nlsRegExp.exec(name),
                    prefix = match[1],
                    locale = match[4],
                    suffix = match[5],
                    parts = locale.split('-'),
                    toLoad = [],
                    value = {},
                    i, part, current = '';

                //If match[5] is blank, it means this is the top bundle definition,
                //so it does not have to be handled. Locale-specific requests
                //will have a match[4] value but no match[5]
                if (match[5]) {
                    //locale-specific bundle
                    prefix = match[1];
                    masterName = prefix + suffix;
                } else {
                    //Top-level bundle.
                    masterName = name;
                    suffix = match[4];
                    locale = masterConfig.locale;
                    if (!locale) {
                        locale = masterConfig.locale =
                            typeof navigator === 'undefined' ? 'root' :
                                ((navigator.languages && navigator.languages[0]) ||
                                navigator.language ||
                                navigator.userLanguage || 'root').toLowerCase();
                    }
                    parts = locale.split('-');
                }

                if (config.isBuild) {
                    //Load only the master locale definition
                    if (exists(req, masterName)) {
                        req([masterName], onLoad);
                    } else {
                        onLoad({});
                    }
                } else {
                    //First, fetch the master bundle, it knows what locales are available.
                    req([masterName], function (master) {
                        //Figure out the best fit
                        var needed = [],
                            part;

                        //Always allow for root, then do the rest of the locale parts.
                        addPart('root', master, needed, toLoad, prefix, suffix);
                        for (i = 0; i < parts.length; i++) {
                            part = parts[i];
                            current += (current ? '-' : '') + part;
                            addPart(current, master, needed, toLoad, prefix, suffix);
                        }

                        //Load all the parts missing.
                        req(toLoad, function () {
                            var i, partBundle, part;
                            for (i = needed.length - 1; i > -1 && needed[i]; i--) {
                                part = needed[i];
                                partBundle = master[part];
                                if (typeof partBundle !== "object") {
                                    partBundle = req(prefix + part + '/' + suffix);
                                }
                                mixin(value, partBundle);
                            }

                            //All done, notify the loader.
                            onLoad(value);
                        }, onLoadFailure);
                    }, onLoadFailure);
                }

                function onLoadFailure(err) {
                    // optional module failed to load.
                    var failedId = err.requireModules && err.requireModules[0];
                    console.warn("Could not load optional module: " + failedId);

                    // Undefine the module to cleanup internal stuff in requireJS
                    var r = window.webswingRequirejs ? window.webswingRequirejs.require : require;
                    r.undef(failedId);

                    // Now define the module instance as a simple empty object
                    // (NOTE: you can return any other value you want here)
                    define(failedId, [], function () {
                        return {};
                    });

                    // Now require the module make sure that requireJS thinks
                    // that is it loaded. Since we've just defined it, requirejs
                    // will not attempt to download any more script files and
                    // will just call the onLoadSuccess handler immediately
                    req([failedId]);
                }

            }
        };
    });
}());