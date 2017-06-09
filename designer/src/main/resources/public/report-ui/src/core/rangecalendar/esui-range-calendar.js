(function(_global){

    /**
     * almond 0.2.5 Copyright (c) 2011-2012, The Dojo Foundation All Rights Reserved.
     * Available via the MIT or new BSD license.
     * see: http://github.com/jrburke/almond for details
     */
//Going sloppy to avoid 'use strict' string cost, but strict practices should
//be followed.
    /*jslint sloppy: true */
    /*global setTimeout: false */

    var requirejs, require, define;
    (function (undef) {
        var main, req, makeMap, handlers,
            defined = {},
            waiting = {},
            config = {},
            defining = {},
            hasOwn = Object.prototype.hasOwnProperty,
            aps = [].slice;

        function hasProp(obj, prop) {
            return hasOwn.call(obj, prop);
        }

        /**
         * Given a relative module name, like ./something, normalize it to
         * a real name that can be mapped to a path.
         * @param {String} name the relative name
         * @param {String} baseName a real name that the name arg is relative
         * to.
         * @returns {String} normalized name
         */
        function normalize(name, baseName) {
            var nameParts, nameSegment, mapValue, foundMap,
                foundI, foundStarMap, starI, i, j, part,
                baseParts = baseName && baseName.split("/"),
                map = config.map,
                starMap = (map && map['*']) || {};

            //Adjust any relative paths.
            if (name && name.charAt(0) === ".") {
                //If have a base name, try to normalize against it,
                //otherwise, assume it is a top-level require that will
                //be relative to baseUrl in the end.
                if (baseName) {
                    //Convert baseName to array, and lop off the last part,
                    //so that . matches that "directory" and not name of the baseName's
                    //module. For instance, baseName of "one/two/three", maps to
                    //"one/two/three.js", but we want the directory, "one/two" for
                    //this normalization.
                    baseParts = baseParts.slice(0, baseParts.length - 1);

                    name = baseParts.concat(name.split("/"));

                    //start trimDots
                    for (i = 0; i < name.length; i += 1) {
                        part = name[i];
                        if (part === ".") {
                            name.splice(i, 1);
                            i -= 1;
                        } else if (part === "..") {
                            if (i === 1 && (name[2] === '..' || name[0] === '..')) {
                                //End of the line. Keep at least one non-dot
                                //path segment at the front so it can be mapped
                                //correctly to disk. Otherwise, there is likely
                                //no path mapping for a path starting with '..'.
                                //This can still fail, but catches the most reasonable
                                //uses of ..
                                break;
                            } else if (i > 0) {
                                name.splice(i - 1, 2);
                                i -= 2;
                            }
                        }
                    }
                    //end trimDots

                    name = name.join("/");
                } else if (name.indexOf('./') === 0) {
                    // No baseName, so this is ID is resolved relative
                    // to baseUrl, pull off the leading dot.
                    name = name.substring(2);
                }
            }

            //Apply map config if available.
            if ((baseParts || starMap) && map) {
                nameParts = name.split('/');

                for (i = nameParts.length; i > 0; i -= 1) {
                    nameSegment = nameParts.slice(0, i).join("/");

                    if (baseParts) {
                        //Find the longest baseName segment match in the config.
                        //So, do joins on the biggest to smallest lengths of baseParts.
                        for (j = baseParts.length; j > 0; j -= 1) {
                            mapValue = map[baseParts.slice(0, j).join('/')];

                            //baseName segment has  config, find if it has one for
                            //this name.
                            if (mapValue) {
                                mapValue = mapValue[nameSegment];
                                if (mapValue) {
                                    //Match, update name to the new value.
                                    foundMap = mapValue;
                                    foundI = i;
                                    break;
                                }
                            }
                        }
                    }

                    if (foundMap) {
                        break;
                    }

                    //Check for a star map match, but just hold on to it,
                    //if there is a shorter segment match later in a matching
                    //config, then favor over this star map.
                    if (!foundStarMap && starMap && starMap[nameSegment]) {
                        foundStarMap = starMap[nameSegment];
                        starI = i;
                    }
                }

                if (!foundMap && foundStarMap) {
                    foundMap = foundStarMap;
                    foundI = starI;
                }

                if (foundMap) {
                    nameParts.splice(0, foundI, foundMap);
                    name = nameParts.join('/');
                }
            }

            return name;
        }

        function makeRequire(relName, forceSync) {
            return function () {
                //A version of a require function that passes a moduleName
                //value for items that may need to
                //look up paths relative to the moduleName
                return req.apply(undef, aps.call(arguments, 0).concat([relName, forceSync]));
            };
        }

        function makeNormalize(relName) {
            return function (name) {
                return normalize(name, relName);
            };
        }

        function makeLoad(depName) {
            return function (value) {
                defined[depName] = value;
            };
        }

        function callDep(name) {
            if (hasProp(waiting, name)) {
                var args = waiting[name];
                delete waiting[name];
                defining[name] = true;
                main.apply(undef, args);
            }

            if (!hasProp(defined, name) && !hasProp(defining, name)) {
                throw new Error('No ' + name);
            }
            return defined[name];
        }

        //Turns a plugin!resource to [plugin, resource]
        //with the plugin being undefined if the name
        //did not have a plugin prefix.
        function splitPrefix(name) {
            var prefix,
                index = name ? name.indexOf('!') : -1;
            if (index > -1) {
                prefix = name.substring(0, index);
                name = name.substring(index + 1, name.length);
            }
            return [prefix, name];
        }

        /**
         * Makes a name map, normalizing the name, and using a plugin
         * for normalization if necessary. Grabs a ref to plugin
         * too, as an optimization.
         */
        makeMap = function (name, relName) {
            var plugin,
                parts = splitPrefix(name),
                prefix = parts[0];

            name = parts[1];

            if (prefix) {
                prefix = normalize(prefix, relName);
                plugin = callDep(prefix);
            }

            //Normalize according
            if (prefix) {
                if (plugin && plugin.normalize) {
                    name = plugin.normalize(name, makeNormalize(relName));
                } else {
                    name = normalize(name, relName);
                }
            } else {
                name = normalize(name, relName);
                parts = splitPrefix(name);
                prefix = parts[0];
                name = parts[1];
                if (prefix) {
                    plugin = callDep(prefix);
                }
            }

            //Using ridiculous property names for space reasons
            return {
                f: prefix ? prefix + '!' + name : name, //fullName
                n: name,
                pr: prefix,
                p: plugin
            };
        };

        function makeConfig(name) {
            return function () {
                return (config && config.config && config.config[name]) || {};
            };
        }

        handlers = {
            require: function (name) {
                return makeRequire(name);
            },
            exports: function (name) {
                var e = defined[name];
                if (typeof e !== 'undefined') {
                    return e;
                } else {
                    return (defined[name] = {});
                }
            },
            module: function (name) {
                return {
                    id: name,
                    uri: '',
                    exports: defined[name],
                    config: makeConfig(name)
                };
            }
        };

        main = function (name, deps, callback, relName) {
            var cjsModule, depName, ret, map, i,
                args = [],
                usingExports;

            //Use name if no relName
            relName = relName || name;

            //Call the callback to define the module, if necessary.
            if (typeof callback === 'function') {

                //Pull out the defined dependencies and pass the ordered
                //values to the callback.
                //Default to [require, exports, module] if no deps
                deps = !deps.length && callback.length ? ['require', 'exports', 'module'] : deps;
                for (i = 0; i < deps.length; i += 1) {
                    map = makeMap(deps[i], relName);
                    depName = map.f;

                    //Fast path CommonJS standard dependencies.
                    if (depName === "require") {
                        args[i] = handlers.require(name);
                    } else if (depName === "exports") {
                        //CommonJS module spec 1.1
                        args[i] = handlers.exports(name);
                        usingExports = true;
                    } else if (depName === "module") {
                        //CommonJS module spec 1.1
                        cjsModule = args[i] = handlers.module(name);
                    } else if (hasProp(defined, depName) ||
                        hasProp(waiting, depName) ||
                        hasProp(defining, depName)) {
                        args[i] = callDep(depName);
                    } else if (map.p) {
                        map.p.load(map.n, makeRequire(relName, true), makeLoad(depName), {});
                        args[i] = defined[depName];
                    } else {
                        throw new Error(name + ' missing ' + depName);
                    }
                }

                ret = callback.apply(defined[name], args);

                if (name) {
                    //If setting exports via "module" is in play,
                    //favor that over return value and exports. After that,
                    //favor a non-undefined return value over exports use.
                    if (cjsModule && cjsModule.exports !== undef &&
                        cjsModule.exports !== defined[name]) {
                        defined[name] = cjsModule.exports;
                    } else if (ret !== undef || !usingExports) {
                        //Use the return value from the function.
                        defined[name] = ret;
                    }
                }
            } else if (name) {
                //May just be an object definition for the module. Only
                //worry about defining if have a module name.
                defined[name] = callback;
            }
        };

        requirejs = require = req = function (deps, callback, relName, forceSync, alt) {
            if (typeof deps === "string") {
                if (handlers[deps]) {
                    //callback in this case is really relName
                    return handlers[deps](callback);
                }
                //Just return the module wanted. In this scenario, the
                //deps arg is the module name, and second arg (if passed)
                //is just the relName.
                //Normalize module name, if it contains . or ..
                return callDep(makeMap(deps, callback).f);
            } else if (!deps.splice) {
                //deps is a config object, not an array.
                config = deps;
                if (callback.splice) {
                    //callback is an array, which means it is a dependency list.
                    //Adjust args if there are dependencies
                    deps = callback;
                    callback = relName;
                    relName = null;
                } else {
                    deps = undef;
                }
            }

            //Support require(['a'])
            callback = callback || function () {};

            //If relName is a function, it is an errback handler,
            //so remove it.
            if (typeof relName === 'function') {
                relName = forceSync;
                forceSync = alt;
            }

            //Simulate async callback;
            if (forceSync) {
                main(undef, deps, callback, relName);
            } else {
                //Using a non-zero value because of concern for what old browsers
                //do, and latest browsers "upgrade" to 4 if lower value is used:
                //http://www.whatwg.org/specs/web-apps/current-work/multipage/timers.html#dom-windowtimers-settimeout:
                //If want a value immediately, use require('id') instead -- something
                //that works in almond on the global level, but not guaranteed and
                //unlikely to work in other AMD implementations.
                setTimeout(function () {
                    main(undef, deps, callback, relName);
                }, 4);
            }

            return req;
        };

        /**
         * Just drops the config on the floor, but returns req in case
         * the config return value is used.
         */
        req.config = function (cfg) {
            config = cfg;
            if (config.deps) {
                req(config.deps, config.callback);
            }
            return req;
        };

        define = function (name, deps, callback) {

            //This module may not have dependencies
            if (!deps.splice) {
                //deps is not an array, so probably means
                //an object literal or factory function for
                //the value. Adjust args.
                callback = deps;
                deps = [];
            }

            if (!hasProp(defined, name) && !hasProp(waiting, name)) {
                waiting[name] = [name, deps, callback];
            }
        };

        define.amd = {
            jQuery: true
        };
    }());

    define('underscore', ['require','exports','module'],function(require, exports, module) {


//     Underscore.js 1.7.0
//     http://underscorejs.org
//     (c) 2009-2014 Jeremy Ashkenas, DocumentCloud and Investigative Reporters & Editors
//     Underscore may be freely distributed under the MIT license.

        (function() {

            // Baseline setup
            // --------------

            // Establish the root object, `window` in the browser, or `exports` on the server.
            var root = this;

            // Save the previous value of the `_` variable.
            var previousUnderscore = root._;

            // Save bytes in the minified (but not gzipped) version:
            var ArrayProto = Array.prototype, ObjProto = Object.prototype, FuncProto = Function.prototype;

            // Create quick reference variables for speed access to core prototypes.
            var
                push             = ArrayProto.push,
                slice            = ArrayProto.slice,
                concat           = ArrayProto.concat,
                toString         = ObjProto.toString,
                hasOwnProperty   = ObjProto.hasOwnProperty;

            // All **ECMAScript 5** native function implementations that we hope to use
            // are declared here.
            var
                nativeIsArray      = Array.isArray,
                nativeKeys         = Object.keys,
                nativeBind         = FuncProto.bind;

            // Create a safe reference to the Underscore object for use below.
            var _ = function(obj) {
                if (obj instanceof _) return obj;
                if (!(this instanceof _)) return new _(obj);
                this._wrapped = obj;
            };

            // Export the Underscore object for **Node.js**, with
            // backwards-compatibility for the old `require()` API. If we're in
            // the browser, add `_` as a global object.
            if (typeof exports !== 'undefined') {
                if (typeof module !== 'undefined' && module.exports) {
                    exports = module.exports = _;
                }
                exports._ = _;
            } else {
                root._ = _;
            }

            // Current version.
            _.VERSION = '1.7.0';

            // Internal function that returns an efficient (for current engines) version
            // of the passed-in callback, to be repeatedly applied in other Underscore
            // functions.
            var createCallback = function(func, context, argCount) {
                if (context === void 0) return func;
                switch (argCount == null ? 3 : argCount) {
                    case 1: return function(value) {
                        return func.call(context, value);
                    };
                    case 2: return function(value, other) {
                        return func.call(context, value, other);
                    };
                    case 3: return function(value, index, collection) {
                        return func.call(context, value, index, collection);
                    };
                    case 4: return function(accumulator, value, index, collection) {
                        return func.call(context, accumulator, value, index, collection);
                    };
                }
                return function() {
                    return func.apply(context, arguments);
                };
            };

            // A mostly-internal function to generate callbacks that can be applied
            // to each element in a collection, returning the desired result — either
            // identity, an arbitrary callback, a property matcher, or a property accessor.
            _.iteratee = function(value, context, argCount) {
                if (value == null) return _.identity;
                if (_.isFunction(value)) return createCallback(value, context, argCount);
                if (_.isObject(value)) return _.matches(value);
                return _.property(value);
            };

            // Collection Functions
            // --------------------

            // The cornerstone, an `each` implementation, aka `forEach`.
            // Handles raw objects in addition to array-likes. Treats all
            // sparse array-likes as if they were dense.
            _.each = _.forEach = function(obj, iteratee, context) {
                if (obj == null) return obj;
                iteratee = createCallback(iteratee, context);
                var i, length = obj.length;
                if (length === +length) {
                    for (i = 0; i < length; i++) {
                        iteratee(obj[i], i, obj);
                    }
                } else {
                    var keys = _.keys(obj);
                    for (i = 0, length = keys.length; i < length; i++) {
                        iteratee(obj[keys[i]], keys[i], obj);
                    }
                }
                return obj;
            };

            // Return the results of applying the iteratee to each element.
            _.map = _.collect = function(obj, iteratee, context) {
                if (obj == null) return [];
                iteratee = _.iteratee(iteratee, context);
                var keys = obj.length !== +obj.length && _.keys(obj),
                    length = (keys || obj).length,
                    results = Array(length),
                    currentKey;
                for (var index = 0; index < length; index++) {
                    currentKey = keys ? keys[index] : index;
                    results[index] = iteratee(obj[currentKey], currentKey, obj);
                }
                return results;
            };

            var reduceError = 'Reduce of empty array with no initial value';

            // **Reduce** builds up a single result from a list of values, aka `inject`,
            // or `foldl`.
            _.reduce = _.foldl = _.inject = function(obj, iteratee, memo, context) {
                if (obj == null) obj = [];
                iteratee = createCallback(iteratee, context, 4);
                var keys = obj.length !== +obj.length && _.keys(obj),
                    length = (keys || obj).length,
                    index = 0, currentKey;
                if (arguments.length < 3) {
                    if (!length) throw new TypeError(reduceError);
                    memo = obj[keys ? keys[index++] : index++];
                }
                for (; index < length; index++) {
                    currentKey = keys ? keys[index] : index;
                    memo = iteratee(memo, obj[currentKey], currentKey, obj);
                }
                return memo;
            };

            // The right-associative version of reduce, also known as `foldr`.
            _.reduceRight = _.foldr = function(obj, iteratee, memo, context) {
                if (obj == null) obj = [];
                iteratee = createCallback(iteratee, context, 4);
                var keys = obj.length !== + obj.length && _.keys(obj),
                    index = (keys || obj).length,
                    currentKey;
                if (arguments.length < 3) {
                    if (!index) throw new TypeError(reduceError);
                    memo = obj[keys ? keys[--index] : --index];
                }
                while (index--) {
                    currentKey = keys ? keys[index] : index;
                    memo = iteratee(memo, obj[currentKey], currentKey, obj);
                }
                return memo;
            };

            // Return the first value which passes a truth test. Aliased as `detect`.
            _.find = _.detect = function(obj, predicate, context) {
                var result;
                predicate = _.iteratee(predicate, context);
                _.some(obj, function(value, index, list) {
                    if (predicate(value, index, list)) {
                        result = value;
                        return true;
                    }
                });
                return result;
            };

            // Return all the elements that pass a truth test.
            // Aliased as `select`.
            _.filter = _.select = function(obj, predicate, context) {
                var results = [];
                if (obj == null) return results;
                predicate = _.iteratee(predicate, context);
                _.each(obj, function(value, index, list) {
                    if (predicate(value, index, list)) results.push(value);
                });
                return results;
            };

            // Return all the elements for which a truth test fails.
            _.reject = function(obj, predicate, context) {
                return _.filter(obj, _.negate(_.iteratee(predicate)), context);
            };

            // Determine whether all of the elements match a truth test.
            // Aliased as `all`.
            _.every = _.all = function(obj, predicate, context) {
                if (obj == null) return true;
                predicate = _.iteratee(predicate, context);
                var keys = obj.length !== +obj.length && _.keys(obj),
                    length = (keys || obj).length,
                    index, currentKey;
                for (index = 0; index < length; index++) {
                    currentKey = keys ? keys[index] : index;
                    if (!predicate(obj[currentKey], currentKey, obj)) return false;
                }
                return true;
            };

            // Determine if at least one element in the object matches a truth test.
            // Aliased as `any`.
            _.some = _.any = function(obj, predicate, context) {
                if (obj == null) return false;
                predicate = _.iteratee(predicate, context);
                var keys = obj.length !== +obj.length && _.keys(obj),
                    length = (keys || obj).length,
                    index, currentKey;
                for (index = 0; index < length; index++) {
                    currentKey = keys ? keys[index] : index;
                    if (predicate(obj[currentKey], currentKey, obj)) return true;
                }
                return false;
            };

            // Determine if the array or object contains a given value (using `===`).
            // Aliased as `include`.
            _.contains = _.include = function(obj, target) {
                if (obj == null) return false;
                if (obj.length !== +obj.length) obj = _.values(obj);
                return _.indexOf(obj, target) >= 0;
            };

            // Invoke a method (with arguments) on every item in a collection.
            _.invoke = function(obj, method) {
                var args = slice.call(arguments, 2);
                var isFunc = _.isFunction(method);
                return _.map(obj, function(value) {
                    return (isFunc ? method : value[method]).apply(value, args);
                });
            };

            // Convenience version of a common use case of `map`: fetching a property.
            _.pluck = function(obj, key) {
                return _.map(obj, _.property(key));
            };

            // Convenience version of a common use case of `filter`: selecting only objects
            // containing specific `key:value` pairs.
            _.where = function(obj, attrs) {
                return _.filter(obj, _.matches(attrs));
            };

            // Convenience version of a common use case of `find`: getting the first object
            // containing specific `key:value` pairs.
            _.findWhere = function(obj, attrs) {
                return _.find(obj, _.matches(attrs));
            };

            // Return the maximum element (or element-based computation).
            _.max = function(obj, iteratee, context) {
                var result = -Infinity, lastComputed = -Infinity,
                    value, computed;
                if (iteratee == null && obj != null) {
                    obj = obj.length === +obj.length ? obj : _.values(obj);
                    for (var i = 0, length = obj.length; i < length; i++) {
                        value = obj[i];
                        if (value > result) {
                            result = value;
                        }
                    }
                } else {
                    iteratee = _.iteratee(iteratee, context);
                    _.each(obj, function(value, index, list) {
                        computed = iteratee(value, index, list);
                        if (computed > lastComputed || computed === -Infinity && result === -Infinity) {
                            result = value;
                            lastComputed = computed;
                        }
                    });
                }
                return result;
            };

            // Return the minimum element (or element-based computation).
            _.min = function(obj, iteratee, context) {
                var result = Infinity, lastComputed = Infinity,
                    value, computed;
                if (iteratee == null && obj != null) {
                    obj = obj.length === +obj.length ? obj : _.values(obj);
                    for (var i = 0, length = obj.length; i < length; i++) {
                        value = obj[i];
                        if (value < result) {
                            result = value;
                        }
                    }
                } else {
                    iteratee = _.iteratee(iteratee, context);
                    _.each(obj, function(value, index, list) {
                        computed = iteratee(value, index, list);
                        if (computed < lastComputed || computed === Infinity && result === Infinity) {
                            result = value;
                            lastComputed = computed;
                        }
                    });
                }
                return result;
            };

            // Shuffle a collection, using the modern version of the
            // [Fisher-Yates shuffle](http://en.wikipedia.org/wiki/Fisher–Yates_shuffle).
            _.shuffle = function(obj) {
                var set = obj && obj.length === +obj.length ? obj : _.values(obj);
                var length = set.length;
                var shuffled = Array(length);
                for (var index = 0, rand; index < length; index++) {
                    rand = _.random(0, index);
                    if (rand !== index) shuffled[index] = shuffled[rand];
                    shuffled[rand] = set[index];
                }
                return shuffled;
            };

            // Sample **n** random values from a collection.
            // If **n** is not specified, returns a single random element.
            // The internal `guard` argument allows it to work with `map`.
            _.sample = function(obj, n, guard) {
                if (n == null || guard) {
                    if (obj.length !== +obj.length) obj = _.values(obj);
                    return obj[_.random(obj.length - 1)];
                }
                return _.shuffle(obj).slice(0, Math.max(0, n));
            };

            // Sort the object's values by a criterion produced by an iteratee.
            _.sortBy = function(obj, iteratee, context) {
                iteratee = _.iteratee(iteratee, context);
                return _.pluck(_.map(obj, function(value, index, list) {
                    return {
                        value: value,
                        index: index,
                        criteria: iteratee(value, index, list)
                    };
                }).sort(function(left, right) {
                    var a = left.criteria;
                    var b = right.criteria;
                    if (a !== b) {
                        if (a > b || a === void 0) return 1;
                        if (a < b || b === void 0) return -1;
                    }
                    return left.index - right.index;
                }), 'value');
            };

            // An internal function used for aggregate "group by" operations.
            var group = function(behavior) {
                return function(obj, iteratee, context) {
                    var result = {};
                    iteratee = _.iteratee(iteratee, context);
                    _.each(obj, function(value, index) {
                        var key = iteratee(value, index, obj);
                        behavior(result, value, key);
                    });
                    return result;
                };
            };

            // Groups the object's values by a criterion. Pass either a string attribute
            // to group by, or a function that returns the criterion.
            _.groupBy = group(function(result, value, key) {
                if (_.has(result, key)) result[key].push(value); else result[key] = [value];
            });

            // Indexes the object's values by a criterion, similar to `groupBy`, but for
            // when you know that your index values will be unique.
            _.indexBy = group(function(result, value, key) {
                result[key] = value;
            });

            // Counts instances of an object that group by a certain criterion. Pass
            // either a string attribute to count by, or a function that returns the
            // criterion.
            _.countBy = group(function(result, value, key) {
                if (_.has(result, key)) result[key]++; else result[key] = 1;
            });

            // Use a comparator function to figure out the smallest index at which
            // an object should be inserted so as to maintain order. Uses binary search.
            _.sortedIndex = function(array, obj, iteratee, context) {
                iteratee = _.iteratee(iteratee, context, 1);
                var value = iteratee(obj);
                var low = 0, high = array.length;
                while (low < high) {
                    var mid = low + high >>> 1;
                    if (iteratee(array[mid]) < value) low = mid + 1; else high = mid;
                }
                return low;
            };

            // Safely create a real, live array from anything iterable.
            _.toArray = function(obj) {
                if (!obj) return [];
                if (_.isArray(obj)) return slice.call(obj);
                if (obj.length === +obj.length) return _.map(obj, _.identity);
                return _.values(obj);
            };

            // Return the number of elements in an object.
            _.size = function(obj) {
                if (obj == null) return 0;
                return obj.length === +obj.length ? obj.length : _.keys(obj).length;
            };

            // Split a collection into two arrays: one whose elements all satisfy the given
            // predicate, and one whose elements all do not satisfy the predicate.
            _.partition = function(obj, predicate, context) {
                predicate = _.iteratee(predicate, context);
                var pass = [], fail = [];
                _.each(obj, function(value, key, obj) {
                    (predicate(value, key, obj) ? pass : fail).push(value);
                });
                return [pass, fail];
            };

            // Array Functions
            // ---------------

            // Get the first element of an array. Passing **n** will return the first N
            // values in the array. Aliased as `head` and `take`. The **guard** check
            // allows it to work with `_.map`.
            _.first = _.head = _.take = function(array, n, guard) {
                if (array == null) return void 0;
                if (n == null || guard) return array[0];
                if (n < 0) return [];
                return slice.call(array, 0, n);
            };

            // Returns everything but the last entry of the array. Especially useful on
            // the arguments object. Passing **n** will return all the values in
            // the array, excluding the last N. The **guard** check allows it to work with
            // `_.map`.
            _.initial = function(array, n, guard) {
                return slice.call(array, 0, Math.max(0, array.length - (n == null || guard ? 1 : n)));
            };

            // Get the last element of an array. Passing **n** will return the last N
            // values in the array. The **guard** check allows it to work with `_.map`.
            _.last = function(array, n, guard) {
                if (array == null) return void 0;
                if (n == null || guard) return array[array.length - 1];
                return slice.call(array, Math.max(array.length - n, 0));
            };

            // Returns everything but the first entry of the array. Aliased as `tail` and `drop`.
            // Especially useful on the arguments object. Passing an **n** will return
            // the rest N values in the array. The **guard**
            // check allows it to work with `_.map`.
            _.rest = _.tail = _.drop = function(array, n, guard) {
                return slice.call(array, n == null || guard ? 1 : n);
            };

            // Trim out all falsy values from an array.
            _.compact = function(array) {
                return _.filter(array, _.identity);
            };

            // Internal implementation of a recursive `flatten` function.
            var flatten = function(input, shallow, strict, output) {
                if (shallow && _.every(input, _.isArray)) {
                    return concat.apply(output, input);
                }
                for (var i = 0, length = input.length; i < length; i++) {
                    var value = input[i];
                    if (!_.isArray(value) && !_.isArguments(value)) {
                        if (!strict) output.push(value);
                    } else if (shallow) {
                        push.apply(output, value);
                    } else {
                        flatten(value, shallow, strict, output);
                    }
                }
                return output;
            };

            // Flatten out an array, either recursively (by default), or just one level.
            _.flatten = function(array, shallow) {
                return flatten(array, shallow, false, []);
            };

            // Return a version of the array that does not contain the specified value(s).
            _.without = function(array) {
                return _.difference(array, slice.call(arguments, 1));
            };

            // Produce a duplicate-free version of the array. If the array has already
            // been sorted, you have the option of using a faster algorithm.
            // Aliased as `unique`.
            _.uniq = _.unique = function(array, isSorted, iteratee, context) {
                if (array == null) return [];
                if (!_.isBoolean(isSorted)) {
                    context = iteratee;
                    iteratee = isSorted;
                    isSorted = false;
                }
                if (iteratee != null) iteratee = _.iteratee(iteratee, context);
                var result = [];
                var seen = [];
                for (var i = 0, length = array.length; i < length; i++) {
                    var value = array[i];
                    if (isSorted) {
                        if (!i || seen !== value) result.push(value);
                        seen = value;
                    } else if (iteratee) {
                        var computed = iteratee(value, i, array);
                        if (_.indexOf(seen, computed) < 0) {
                            seen.push(computed);
                            result.push(value);
                        }
                    } else if (_.indexOf(result, value) < 0) {
                        result.push(value);
                    }
                }
                return result;
            };

            // Produce an array that contains the union: each distinct element from all of
            // the passed-in arrays.
            _.union = function() {
                return _.uniq(flatten(arguments, true, true, []));
            };

            // Produce an array that contains every item shared between all the
            // passed-in arrays.
            _.intersection = function(array) {
                if (array == null) return [];
                var result = [];
                var argsLength = arguments.length;
                for (var i = 0, length = array.length; i < length; i++) {
                    var item = array[i];
                    if (_.contains(result, item)) continue;
                    for (var j = 1; j < argsLength; j++) {
                        if (!_.contains(arguments[j], item)) break;
                    }
                    if (j === argsLength) result.push(item);
                }
                return result;
            };

            // Take the difference between one array and a number of other arrays.
            // Only the elements present in just the first array will remain.
            _.difference = function(array) {
                var rest = flatten(slice.call(arguments, 1), true, true, []);
                return _.filter(array, function(value){
                    return !_.contains(rest, value);
                });
            };

            // Zip together multiple lists into a single array -- elements that share
            // an index go together.
            _.zip = function(array) {
                if (array == null) return [];
                var length = _.max(arguments, 'length').length;
                var results = Array(length);
                for (var i = 0; i < length; i++) {
                    results[i] = _.pluck(arguments, i);
                }
                return results;
            };

            // Converts lists into objects. Pass either a single array of `[key, value]`
            // pairs, or two parallel arrays of the same length -- one of keys, and one of
            // the corresponding values.
            _.object = function(list, values) {
                if (list == null) return {};
                var result = {};
                for (var i = 0, length = list.length; i < length; i++) {
                    if (values) {
                        result[list[i]] = values[i];
                    } else {
                        result[list[i][0]] = list[i][1];
                    }
                }
                return result;
            };

            // Return the position of the first occurrence of an item in an array,
            // or -1 if the item is not included in the array.
            // If the array is large and already in sort order, pass `true`
            // for **isSorted** to use binary search.
            _.indexOf = function(array, item, isSorted) {
                if (array == null) return -1;
                var i = 0, length = array.length;
                if (isSorted) {
                    if (typeof isSorted == 'number') {
                        i = isSorted < 0 ? Math.max(0, length + isSorted) : isSorted;
                    } else {
                        i = _.sortedIndex(array, item);
                        return array[i] === item ? i : -1;
                    }
                }
                for (; i < length; i++) if (array[i] === item) return i;
                return -1;
            };

            _.lastIndexOf = function(array, item, from) {
                if (array == null) return -1;
                var idx = array.length;
                if (typeof from == 'number') {
                    idx = from < 0 ? idx + from + 1 : Math.min(idx, from + 1);
                }
                while (--idx >= 0) if (array[idx] === item) return idx;
                return -1;
            };

            // Generate an integer Array containing an arithmetic progression. A port of
            // the native Python `range()` function. See
            // [the Python documentation](http://docs.python.org/library/functions.html#range).
            _.range = function(start, stop, step) {
                if (arguments.length <= 1) {
                    stop = start || 0;
                    start = 0;
                }
                step = step || 1;

                var length = Math.max(Math.ceil((stop - start) / step), 0);
                var range = Array(length);

                for (var idx = 0; idx < length; idx++, start += step) {
                    range[idx] = start;
                }

                return range;
            };

            // Function (ahem) Functions
            // ------------------

            // Reusable constructor function for prototype setting.
            var Ctor = function(){};

            // Create a function bound to a given object (assigning `this`, and arguments,
            // optionally). Delegates to **ECMAScript 5**'s native `Function.bind` if
            // available.
            _.bind = function(func, context) {
                var args, bound;
                if (nativeBind && func.bind === nativeBind) return nativeBind.apply(func, slice.call(arguments, 1));
                if (!_.isFunction(func)) throw new TypeError('Bind must be called on a function');
                args = slice.call(arguments, 2);
                bound = function() {
                    if (!(this instanceof bound)) return func.apply(context, args.concat(slice.call(arguments)));
                    Ctor.prototype = func.prototype;
                    var self = new Ctor;
                    Ctor.prototype = null;
                    var result = func.apply(self, args.concat(slice.call(arguments)));
                    if (_.isObject(result)) return result;
                    return self;
                };
                return bound;
            };

            // Partially apply a function by creating a version that has had some of its
            // arguments pre-filled, without changing its dynamic `this` context. _ acts
            // as a placeholder, allowing any combination of arguments to be pre-filled.
            _.partial = function(func) {
                var boundArgs = slice.call(arguments, 1);
                return function() {
                    var position = 0;
                    var args = boundArgs.slice();
                    for (var i = 0, length = args.length; i < length; i++) {
                        if (args[i] === _) args[i] = arguments[position++];
                    }
                    while (position < arguments.length) args.push(arguments[position++]);
                    return func.apply(this, args);
                };
            };

            // Bind a number of an object's methods to that object. Remaining arguments
            // are the method names to be bound. Useful for ensuring that all callbacks
            // defined on an object belong to it.
            _.bindAll = function(obj) {
                var i, length = arguments.length, key;
                if (length <= 1) throw new Error('bindAll must be passed function names');
                for (i = 1; i < length; i++) {
                    key = arguments[i];
                    obj[key] = _.bind(obj[key], obj);
                }
                return obj;
            };

            // Memoize an expensive function by storing its results.
            _.memoize = function(func, hasher) {
                var memoize = function(key) {
                    var cache = memoize.cache;
                    var address = hasher ? hasher.apply(this, arguments) : key;
                    if (!_.has(cache, address)) cache[address] = func.apply(this, arguments);
                    return cache[address];
                };
                memoize.cache = {};
                return memoize;
            };

            // Delays a function for the given number of milliseconds, and then calls
            // it with the arguments supplied.
            _.delay = function(func, wait) {
                var args = slice.call(arguments, 2);
                return setTimeout(function(){
                    return func.apply(null, args);
                }, wait);
            };

            // Defers a function, scheduling it to run after the current call stack has
            // cleared.
            _.defer = function(func) {
                return _.delay.apply(_, [func, 1].concat(slice.call(arguments, 1)));
            };

            // Returns a function, that, when invoked, will only be triggered at most once
            // during a given window of time. Normally, the throttled function will run
            // as much as it can, without ever going more than once per `wait` duration;
            // but if you'd like to disable the execution on the leading edge, pass
            // `{leading: false}`. To disable execution on the trailing edge, ditto.
            _.throttle = function(func, wait, options) {
                var context, args, result;
                var timeout = null;
                var previous = 0;
                if (!options) options = {};
                var later = function() {
                    previous = options.leading === false ? 0 : _.now();
                    timeout = null;
                    result = func.apply(context, args);
                    if (!timeout) context = args = null;
                };
                return function() {
                    var now = _.now();
                    if (!previous && options.leading === false) previous = now;
                    var remaining = wait - (now - previous);
                    context = this;
                    args = arguments;
                    if (remaining <= 0 || remaining > wait) {
                        clearTimeout(timeout);
                        timeout = null;
                        previous = now;
                        result = func.apply(context, args);
                        if (!timeout) context = args = null;
                    } else if (!timeout && options.trailing !== false) {
                        timeout = setTimeout(later, remaining);
                    }
                    return result;
                };
            };

            // Returns a function, that, as long as it continues to be invoked, will not
            // be triggered. The function will be called after it stops being called for
            // N milliseconds. If `immediate` is passed, trigger the function on the
            // leading edge, instead of the trailing.
            _.debounce = function(func, wait, immediate) {
                var timeout, args, context, timestamp, result;

                var later = function() {
                    var last = _.now() - timestamp;

                    if (last < wait && last > 0) {
                        timeout = setTimeout(later, wait - last);
                    } else {
                        timeout = null;
                        if (!immediate) {
                            result = func.apply(context, args);
                            if (!timeout) context = args = null;
                        }
                    }
                };

                return function() {
                    context = this;
                    args = arguments;
                    timestamp = _.now();
                    var callNow = immediate && !timeout;
                    if (!timeout) timeout = setTimeout(later, wait);
                    if (callNow) {
                        result = func.apply(context, args);
                        context = args = null;
                    }

                    return result;
                };
            };

            // Returns the first function passed as an argument to the second,
            // allowing you to adjust arguments, run code before and after, and
            // conditionally execute the original function.
            _.wrap = function(func, wrapper) {
                return _.partial(wrapper, func);
            };

            // Returns a negated version of the passed-in predicate.
            _.negate = function(predicate) {
                return function() {
                    return !predicate.apply(this, arguments);
                };
            };

            // Returns a function that is the composition of a list of functions, each
            // consuming the return value of the function that follows.
            _.compose = function() {
                var args = arguments;
                var start = args.length - 1;
                return function() {
                    var i = start;
                    var result = args[start].apply(this, arguments);
                    while (i--) result = args[i].call(this, result);
                    return result;
                };
            };

            // Returns a function that will only be executed after being called N times.
            _.after = function(times, func) {
                return function() {
                    if (--times < 1) {
                        return func.apply(this, arguments);
                    }
                };
            };

            // Returns a function that will only be executed before being called N times.
            _.before = function(times, func) {
                var memo;
                return function() {
                    if (--times > 0) {
                        memo = func.apply(this, arguments);
                    } else {
                        func = null;
                    }
                    return memo;
                };
            };

            // Returns a function that will be executed at most one time, no matter how
            // often you call it. Useful for lazy initialization.
            _.once = _.partial(_.before, 2);

            // Object Functions
            // ----------------

            // Retrieve the names of an object's properties.
            // Delegates to **ECMAScript 5**'s native `Object.keys`
            _.keys = function(obj) {
                if (!_.isObject(obj)) return [];
                if (nativeKeys) return nativeKeys(obj);
                var keys = [];
                for (var key in obj) if (_.has(obj, key)) keys.push(key);
                return keys;
            };

            // Retrieve the values of an object's properties.
            _.values = function(obj) {
                var keys = _.keys(obj);
                var length = keys.length;
                var values = Array(length);
                for (var i = 0; i < length; i++) {
                    values[i] = obj[keys[i]];
                }
                return values;
            };

            // Convert an object into a list of `[key, value]` pairs.
            _.pairs = function(obj) {
                var keys = _.keys(obj);
                var length = keys.length;
                var pairs = Array(length);
                for (var i = 0; i < length; i++) {
                    pairs[i] = [keys[i], obj[keys[i]]];
                }
                return pairs;
            };

            // Invert the keys and values of an object. The values must be serializable.
            _.invert = function(obj) {
                var result = {};
                var keys = _.keys(obj);
                for (var i = 0, length = keys.length; i < length; i++) {
                    result[obj[keys[i]]] = keys[i];
                }
                return result;
            };

            // Return a sorted list of the function names available on the object.
            // Aliased as `methods`
            _.functions = _.methods = function(obj) {
                var names = [];
                for (var key in obj) {
                    if (_.isFunction(obj[key])) names.push(key);
                }
                return names.sort();
            };

            // Extend a given object with all the properties in passed-in object(s).
            _.extend = function(obj) {
                if (!_.isObject(obj)) return obj;
                var source, prop;
                for (var i = 1, length = arguments.length; i < length; i++) {
                    source = arguments[i];
                    for (prop in source) {
                        if (hasOwnProperty.call(source, prop)) {
                            obj[prop] = source[prop];
                        }
                    }
                }
                return obj;
            };

            // Return a copy of the object only containing the whitelisted properties.
            _.pick = function(obj, iteratee, context) {
                var result = {}, key;
                if (obj == null) return result;
                if (_.isFunction(iteratee)) {
                    iteratee = createCallback(iteratee, context);
                    for (key in obj) {
                        var value = obj[key];
                        if (iteratee(value, key, obj)) result[key] = value;
                    }
                } else {
                    var keys = concat.apply([], slice.call(arguments, 1));
                    obj = new Object(obj);
                    for (var i = 0, length = keys.length; i < length; i++) {
                        key = keys[i];
                        if (key in obj) result[key] = obj[key];
                    }
                }
                return result;
            };

            // Return a copy of the object without the blacklisted properties.
            _.omit = function(obj, iteratee, context) {
                if (_.isFunction(iteratee)) {
                    iteratee = _.negate(iteratee);
                } else {
                    var keys = _.map(concat.apply([], slice.call(arguments, 1)), String);
                    iteratee = function(value, key) {
                        return !_.contains(keys, key);
                    };
                }
                return _.pick(obj, iteratee, context);
            };

            // Fill in a given object with default properties.
            _.defaults = function(obj) {
                if (!_.isObject(obj)) return obj;
                for (var i = 1, length = arguments.length; i < length; i++) {
                    var source = arguments[i];
                    for (var prop in source) {
                        if (obj[prop] === void 0) obj[prop] = source[prop];
                    }
                }
                return obj;
            };

            // Create a (shallow-cloned) duplicate of an object.
            _.clone = function(obj) {
                if (!_.isObject(obj)) return obj;
                return _.isArray(obj) ? obj.slice() : _.extend({}, obj);
            };

            // Invokes interceptor with the obj, and then returns obj.
            // The primary purpose of this method is to "tap into" a method chain, in
            // order to perform operations on intermediate results within the chain.
            _.tap = function(obj, interceptor) {
                interceptor(obj);
                return obj;
            };

            // Internal recursive comparison function for `isEqual`.
            var eq = function(a, b, aStack, bStack) {
                // Identical objects are equal. `0 === -0`, but they aren't identical.
                // See the [Harmony `egal` proposal](http://wiki.ecmascript.org/doku.php?id=harmony:egal).
                if (a === b) return a !== 0 || 1 / a === 1 / b;
                // A strict comparison is necessary because `null == undefined`.
                if (a == null || b == null) return a === b;
                // Unwrap any wrapped objects.
                if (a instanceof _) a = a._wrapped;
                if (b instanceof _) b = b._wrapped;
                // Compare `[[Class]]` names.
                var className = toString.call(a);
                if (className !== toString.call(b)) return false;
                switch (className) {
                    // Strings, numbers, regular expressions, dates, and booleans are compared by value.
                    case '[object RegExp]':
                    // RegExps are coerced to strings for comparison (Note: '' + /a/i === '/a/i')
                    case '[object String]':
                        // Primitives and their corresponding object wrappers are equivalent; thus, `"5"` is
                        // equivalent to `new String("5")`.
                        return '' + a === '' + b;
                    case '[object Number]':
                        // `NaN`s are equivalent, but non-reflexive.
                        // Object(NaN) is equivalent to NaN
                        if (+a !== +a) return +b !== +b;
                        // An `egal` comparison is performed for other numeric values.
                        return +a === 0 ? 1 / +a === 1 / b : +a === +b;
                    case '[object Date]':
                    case '[object Boolean]':
                        // Coerce dates and booleans to numeric primitive values. Dates are compared by their
                        // millisecond representations. Note that invalid dates with millisecond representations
                        // of `NaN` are not equivalent.
                        return +a === +b;
                }
                if (typeof a != 'object' || typeof b != 'object') return false;
                // Assume equality for cyclic structures. The algorithm for detecting cyclic
                // structures is adapted from ES 5.1 section 15.12.3, abstract operation `JO`.
                var length = aStack.length;
                while (length--) {
                    // Linear search. Performance is inversely proportional to the number of
                    // unique nested structures.
                    if (aStack[length] === a) return bStack[length] === b;
                }
                // Objects with different constructors are not equivalent, but `Object`s
                // from different frames are.
                var aCtor = a.constructor, bCtor = b.constructor;
                if (
                    aCtor !== bCtor &&
                    // Handle Object.create(x) cases
                    'constructor' in a && 'constructor' in b &&
                    !(_.isFunction(aCtor) && aCtor instanceof aCtor &&
                        _.isFunction(bCtor) && bCtor instanceof bCtor)
                    ) {
                    return false;
                }
                // Add the first object to the stack of traversed objects.
                aStack.push(a);
                bStack.push(b);
                var size, result;
                // Recursively compare objects and arrays.
                if (className === '[object Array]') {
                    // Compare array lengths to determine if a deep comparison is necessary.
                    size = a.length;
                    result = size === b.length;
                    if (result) {
                        // Deep compare the contents, ignoring non-numeric properties.
                        while (size--) {
                            if (!(result = eq(a[size], b[size], aStack, bStack))) break;
                        }
                    }
                } else {
                    // Deep compare objects.
                    var keys = _.keys(a), key;
                    size = keys.length;
                    // Ensure that both objects contain the same number of properties before comparing deep equality.
                    result = _.keys(b).length === size;
                    if (result) {
                        while (size--) {
                            // Deep compare each member
                            key = keys[size];
                            if (!(result = _.has(b, key) && eq(a[key], b[key], aStack, bStack))) break;
                        }
                    }
                }
                // Remove the first object from the stack of traversed objects.
                aStack.pop();
                bStack.pop();
                return result;
            };

            // Perform a deep comparison to check if two objects are equal.
            _.isEqual = function(a, b) {
                return eq(a, b, [], []);
            };

            // Is a given array, string, or object empty?
            // An "empty" object has no enumerable own-properties.
            _.isEmpty = function(obj) {
                if (obj == null) return true;
                if (_.isArray(obj) || _.isString(obj) || _.isArguments(obj)) return obj.length === 0;
                for (var key in obj) if (_.has(obj, key)) return false;
                return true;
            };

            // Is a given value a DOM element?
            _.isElement = function(obj) {
                return !!(obj && obj.nodeType === 1);
            };

            // Is a given value an array?
            // Delegates to ECMA5's native Array.isArray
            _.isArray = nativeIsArray || function(obj) {
                return toString.call(obj) === '[object Array]';
            };

            // Is a given variable an object?
            _.isObject = function(obj) {
                var type = typeof obj;
                return type === 'function' || type === 'object' && !!obj;
            };

            // Add some isType methods: isArguments, isFunction, isString, isNumber, isDate, isRegExp.
            _.each(['Arguments', 'Function', 'String', 'Number', 'Date', 'RegExp'], function(name) {
                _['is' + name] = function(obj) {
                    return toString.call(obj) === '[object ' + name + ']';
                };
            });

            // Define a fallback version of the method in browsers (ahem, IE), where
            // there isn't any inspectable "Arguments" type.
            if (!_.isArguments(arguments)) {
                _.isArguments = function(obj) {
                    return _.has(obj, 'callee');
                };
            }

            // Optimize `isFunction` if appropriate. Work around an IE 11 bug.
            if (typeof /./ !== 'function') {
                _.isFunction = function(obj) {
                    return typeof obj == 'function' || false;
                };
            }

            // Is a given object a finite number?
            _.isFinite = function(obj) {
                return isFinite(obj) && !isNaN(parseFloat(obj));
            };

            // Is the given value `NaN`? (NaN is the only number which does not equal itself).
            _.isNaN = function(obj) {
                return _.isNumber(obj) && obj !== +obj;
            };

            // Is a given value a boolean?
            _.isBoolean = function(obj) {
                return obj === true || obj === false || toString.call(obj) === '[object Boolean]';
            };

            // Is a given value equal to null?
            _.isNull = function(obj) {
                return obj === null;
            };

            // Is a given variable undefined?
            _.isUndefined = function(obj) {
                return obj === void 0;
            };

            // Shortcut function for checking if an object has a given property directly
            // on itself (in other words, not on a prototype).
            _.has = function(obj, key) {
                return obj != null && hasOwnProperty.call(obj, key);
            };

            // Utility Functions
            // -----------------

            // Run Underscore.js in *noConflict* mode, returning the `_` variable to its
            // previous owner. Returns a reference to the Underscore object.
            _.noConflict = function() {
                root._ = previousUnderscore;
                return this;
            };

            // Keep the identity function around for default iteratees.
            _.identity = function(value) {
                return value;
            };

            _.constant = function(value) {
                return function() {
                    return value;
                };
            };

            _.noop = function(){};

            _.property = function(key) {
                return function(obj) {
                    return obj[key];
                };
            };

            // Returns a predicate for checking whether an object has a given set of `key:value` pairs.
            _.matches = function(attrs) {
                var pairs = _.pairs(attrs), length = pairs.length;
                return function(obj) {
                    if (obj == null) return !length;
                    obj = new Object(obj);
                    for (var i = 0; i < length; i++) {
                        var pair = pairs[i], key = pair[0];
                        if (pair[1] !== obj[key] || !(key in obj)) return false;
                    }
                    return true;
                };
            };

            // Run a function **n** times.
            _.times = function(n, iteratee, context) {
                var accum = Array(Math.max(0, n));
                iteratee = createCallback(iteratee, context, 1);
                for (var i = 0; i < n; i++) accum[i] = iteratee(i);
                return accum;
            };

            // Return a random integer between min and max (inclusive).
            _.random = function(min, max) {
                if (max == null) {
                    max = min;
                    min = 0;
                }
                return min + Math.floor(Math.random() * (max - min + 1));
            };

            // A (possibly faster) way to get the current timestamp as an integer.
            _.now = Date.now || function() {
                return new Date().getTime();
            };

            // List of HTML entities for escaping.
            var escapeMap = {
                '&': '&amp;',
                '<': '&lt;',
                '>': '&gt;',
                '"': '&quot;',
                "'": '&#x27;',
                '`': '&#x60;'
            };
            var unescapeMap = _.invert(escapeMap);

            // Functions for escaping and unescaping strings to/from HTML interpolation.
            var createEscaper = function(map) {
                var escaper = function(match) {
                    return map[match];
                };
                // Regexes for identifying a key that needs to be escaped
                var source = '(?:' + _.keys(map).join('|') + ')';
                var testRegexp = RegExp(source);
                var replaceRegexp = RegExp(source, 'g');
                return function(string) {
                    string = string == null ? '' : '' + string;
                    return testRegexp.test(string) ? string.replace(replaceRegexp, escaper) : string;
                };
            };
            _.escape = createEscaper(escapeMap);
            _.unescape = createEscaper(unescapeMap);

            // If the value of the named `property` is a function then invoke it with the
            // `object` as context; otherwise, return it.
            _.result = function(object, property) {
                if (object == null) return void 0;
                var value = object[property];
                return _.isFunction(value) ? object[property]() : value;
            };

            // Generate a unique integer id (unique within the entire client session).
            // Useful for temporary DOM ids.
            var idCounter = 0;
            _.uniqueId = function(prefix) {
                var id = ++idCounter + '';
                return prefix ? prefix + id : id;
            };

            // By default, Underscore uses ERB-style template delimiters, change the
            // following template settings to use alternative delimiters.
            _.templateSettings = {
                evaluate    : /<%([\s\S]+?)%>/g,
                interpolate : /<%=([\s\S]+?)%>/g,
                escape      : /<%-([\s\S]+?)%>/g
            };

            // When customizing `templateSettings`, if you don't want to define an
            // interpolation, evaluation or escaping regex, we need one that is
            // guaranteed not to match.
            var noMatch = /(.)^/;

            // Certain characters need to be escaped so that they can be put into a
            // string literal.
            var escapes = {
                "'":      "'",
                '\\':     '\\',
                '\r':     'r',
                '\n':     'n',
                '\u2028': 'u2028',
                '\u2029': 'u2029'
            };

            var escaper = /\\|'|\r|\n|\u2028|\u2029/g;

            var escapeChar = function(match) {
                return '\\' + escapes[match];
            };

            // JavaScript micro-templating, similar to John Resig's implementation.
            // Underscore templating handles arbitrary delimiters, preserves whitespace,
            // and correctly escapes quotes within interpolated code.
            // NB: `oldSettings` only exists for backwards compatibility.
            _.template = function(text, settings, oldSettings) {
                if (!settings && oldSettings) settings = oldSettings;
                settings = _.defaults({}, settings, _.templateSettings);

                // Combine delimiters into one regular expression via alternation.
                var matcher = RegExp([
                    (settings.escape || noMatch).source,
                    (settings.interpolate || noMatch).source,
                    (settings.evaluate || noMatch).source
                ].join('|') + '|$', 'g');

                // Compile the template source, escaping string literals appropriately.
                var index = 0;
                var source = "__p+='";
                text.replace(matcher, function(match, escape, interpolate, evaluate, offset) {
                    source += text.slice(index, offset).replace(escaper, escapeChar);
                    index = offset + match.length;

                    if (escape) {
                        source += "'+\n((__t=(" + escape + "))==null?'':_.escape(__t))+\n'";
                    } else if (interpolate) {
                        source += "'+\n((__t=(" + interpolate + "))==null?'':__t)+\n'";
                    } else if (evaluate) {
                        source += "';\n" + evaluate + "\n__p+='";
                    }

                    // Adobe VMs need the match returned to produce the correct offest.
                    return match;
                });
                source += "';\n";

                // If a variable is not specified, place data values in local scope.
                if (!settings.variable) source = 'with(obj||{}){\n' + source + '}\n';

                source = "var __t,__p='',__j=Array.prototype.join," +
                    "print=function(){__p+=__j.call(arguments,'');};\n" +
                    source + 'return __p;\n';

                try {
                    var render = new Function(settings.variable || 'obj', '_', source);
                } catch (e) {
                    e.source = source;
                    throw e;
                }

                var template = function(data) {
                    return render.call(this, data, _);
                };

                // Provide the compiled source as a convenience for precompilation.
                var argument = settings.variable || 'obj';
                template.source = 'function(' + argument + '){\n' + source + '}';

                return template;
            };

            // Add a "chain" function. Start chaining a wrapped Underscore object.
            _.chain = function(obj) {
                var instance = _(obj);
                instance._chain = true;
                return instance;
            };

            // OOP
            // ---------------
            // If Underscore is called as a function, it returns a wrapped object that
            // can be used OO-style. This wrapper holds altered versions of all the
            // underscore functions. Wrapped objects may be chained.

            // Helper function to continue chaining intermediate results.
            var result = function(obj) {
                return this._chain ? _(obj).chain() : obj;
            };

            // Add your own custom functions to the Underscore object.
            _.mixin = function(obj) {
                _.each(_.functions(obj), function(name) {
                    var func = _[name] = obj[name];
                    _.prototype[name] = function() {
                        var args = [this._wrapped];
                        push.apply(args, arguments);
                        return result.call(this, func.apply(_, args));
                    };
                });
            };

            // Add all of the Underscore functions to the wrapper object.
            _.mixin(_);

            // Add all mutator Array functions to the wrapper.
            _.each(['pop', 'push', 'reverse', 'shift', 'sort', 'splice', 'unshift'], function(name) {
                var method = ArrayProto[name];
                _.prototype[name] = function() {
                    var obj = this._wrapped;
                    method.apply(obj, arguments);
                    if ((name === 'shift' || name === 'splice') && obj.length === 0) delete obj[0];
                    return result.call(this, obj);
                };
            });

            // Add all accessor Array functions to the wrapper.
            _.each(['concat', 'join', 'slice'], function(name) {
                var method = ArrayProto[name];
                _.prototype[name] = function() {
                    return result.call(this, method.apply(this._wrapped, arguments));
                };
            });

            // Extracts the result from a wrapped and chained object.
            _.prototype.value = function() {
                return this._wrapped;
            };

            // AMD registration happens at the end for compatibility with AMD loaders
            // that may not enforce next-turn semantics on modules. Even though general
            // practice for AMD registration is to be anonymous, underscore registers
            // as a named module because, like jQuery, it is a base library that is
            // popular enough to be bundled in a third party lib, but not be part of
            // an AMD load request. Those cases could generate an error when an
            // anonymous define() is called outside of a loader request.
            if (typeof define === 'function' && define.amd) {
                define('underscore', [], function() {
                    return _;
                });
            }
        }.call(this));


    });
    define("underscore/underscore", function(){});

    /**
     * ESUI (Enterprise Simple UI library)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 字符串相关基础库
     * @author otakustay
     */
    define(
        'esui/lib/string',['require','underscore'],function (require) {
            var u = require('underscore');
            /**
             * @override lib
             */
            var lib = {};

            var WHITESPACE = /^[\s\xa0\u3000]+|[\u3000\xa0\s]+$/g;

            /**
             * 删除目标字符串两端的空白字符
             *
             * @param {string} source 目标字符串
             * @return {string} 删除两端空白字符后的字符串
             */
            lib.trim = function (source) {
                if (!source) {
                    return '';
                }

                return String(source).replace(WHITESPACE, '');
            };

            /**
             * 字符串格式化
             *
             * 简单的格式化使用`${name}`进行占位
             *
             * @param {string} template 原字符串
             * @param {Object} data 用于模板替换的数据
             * @return {string} 格式化后的字符串
             */
            lib.format = function (template, data) {
                if (!template) {
                    return '';
                }

                if (data == null) {
                    return template;
                }

                return template.replace(
                    /\$\{(.+?)\}/g,
                    function (match, key) {
                        var replacer = data[key];
                        if (typeof replacer === 'function') {
                            replacer = replacer(key);
                        }

                        return replacer == null ? '' : replacer;
                    }
                );
            };

            /**
             * 将字符串转换成`camelCase`格式
             *
             * 该方法将横线`-`视为单词的 **唯一分隔符**
             *
             * @param {string} source 源字符串
             * @return {string}
             */
            lib.camelize = function (source) {
                if (!source) {
                    return '';
                }

                return source.replace(
                    /-([a-z])/g,
                    function (alpha) {
                        return alpha.toUpperCase();
                    }
                );
            };

            /**
             * 将字符串转换成`PascalCase`格式
             *
             * 该方法将横线`-`视为单词的 **唯一分隔符**
             *
             * @param {string} source 源字符串
             * @return {string}
             */
            lib.pascalize = function (source) {
                if (!source) {
                    return '';
                }

                return source.charAt(0).toUpperCase()
                    + lib.camelize(source.slice(1));
            };

            /**
             * 将Token列表字符串切分为数组
             *
             * Token列表是使用逗号或空格分隔的字符串
             *
             * @param {string | string[] | null | undefined} input 输入值
             * @return {string[]}
             */
            lib.splitTokenList = function (input) {
                if (!input) {
                    return [];
                }

                if (u.isArray(input)) {
                    return;
                }

                return u.chain(input.split(/[,\s]/))
                    .map(lib.trim)
                    .compact()
                    .value();
            };

            return lib;
        }
    );

    /**
     * ESUI (Enterprise Simple UI library)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file DOM相关基础库
     * @author otakustay
     */
    define(
        'esui/lib/dom',['require','underscore','./string'],function (require) {
            var u = require('underscore');
            var string = require('./string');

            /**
             * @override lib
             */
            var lib = {};

            /**
             * 从文档中获取指定的DOM元素
             *
             * @param {string | HTMLElement} id 元素的id或DOM元素
             * @return {HTMLElement | null} 获取的元素，查找不到时返回null
             */
            lib.g = function (id) {
                if (!id) {
                    return null;
                }

                return typeof id === 'string' ? document.getElementById(id) : id;
            };

            /**
             * 判断一个元素是否输入元素
             *
             * @param {HTMLElement} element 目标元素
             * @return {boolean}
             */
            lib.isInput = function (element) {
                var nodeName = element.nodeName.toLowerCase();
                return nodeName === 'input'
                    || nodeName === 'select'
                    || nodeName === 'textarea';
            };

            /**
             * 移除目标元素
             *
             * @param {HTMLElement} element 目标元素或其id
             */
            lib.removeNode = function (element) {
                if (typeof element === 'string') {
                    element = lib.g(element);
                }

                if (!element) {
                    return;
                }

                var parent = element.parentNode;
                if (parent) {
                    parent.removeChild(element);
                }
            };

            /**
             * 将目标元素添加到基准元素之后
             *
             * @param {HTMLElement} element 被添加的目标元素
             * @param {HTMLElement} reference 基准元素
             * @return {HTMLElement} 被添加的目标元素
             */
            lib.insertAfter = function (element, reference) {
                var parent = reference.parentNode;

                if (parent) {
                    parent.insertBefore(element, reference.nextSibling);
                }
                return element;
            };

            /**
             * 将目标元素添加到基准元素之前
             *
             * @param {HTMLElement} element 被添加的目标元素
             * @param {HTMLElement} reference 基准元素
             * @return {HTMLElement} 被添加的目标元素
             */
            lib.insertBefore = function (element, reference) {
                var parent = reference.parentNode;

                if (parent) {
                    parent.insertBefore(element, reference);
                }

                return element;
            };

            /**
             * 获取子元素
             * @param {HTMLElement} element 目标元素
             * @return {HTMLElement[]} 目标元素的所有子元素
             */
            lib.getChildren = function (element) {
                return u.filter(
                    element.children,
                    function (child) {
                        return child.nodeType === 1;
                    }
                );
            };


            /**
             * 获取计算样式值
             *
             * @param {HTMLElement} element 目标元素
             * @param {string} key 样式名称
             * @return {string}
             */
            lib.getComputedStyle = function (element, key) {
                if (!element) {
                    return '';
                }

                var doc = element.nodeType === 9
                    ? element
                    : element.ownerDocument || element.document;

                if (doc.defaultView && doc.defaultView.getComputedStyle) {
                    var styles = doc.defaultView.getComputedStyle(element, null);
                    if (styles) {
                        return styles[key] || styles.getPropertyValue(key);
                    }
                }
                else if (element && element.currentStyle) {
                    return element.currentStyle[key];
                }
                return '';
            };

            /**
             * 获取元素样式值
             *
             * @param {HTMLElement} element 目标元素
             * @param {string} key 样式名称
             * @return {string} 目标元素的指定样式值
             */
            lib.getStyle = function (element, key) {
                key = string.camelize(key);
                return element.style[key]
                    || (element.currentStyle ? element.currentStyle[key] : '')
                    || lib.getComputedStyle(element, key);
            };

            /**
             * 获取元素在页面中的位置和尺寸信息
             *
             * @param {HTMLElement} element 目标元素
             * @return {Object} 元素的尺寸和位置信息，
             * 包含`top`、`right`、`bottom`、`left`、`width`和`height`属性
             */
            lib.getOffset = function (element) {
                var rect = element.getBoundingClientRect();
                var offset = {
                    top: rect.top,
                    right: rect.right,
                    bottom: rect.bottom,
                    left: rect.left,
                    width: rect.right - rect.left,
                    height: rect.bottom - rect.top
                };
                var clientTop = document.documentElement.clientTop
                    || document.body.clientTop
                    || 0;
                var clientLeft = document.documentElement.clientLeft
                    || document.body.clientLeft
                    || 0;
                var scrollTop = window.pageYOffset
                    || document.documentElement.scrollTop;
                var scrollLeft = window.pageXOffset
                    || document.documentElement.scrollLeft;
                offset.top = offset.top + scrollTop - clientTop;
                offset.bottom = offset.bottom + scrollTop - clientTop;
                offset.left = offset.left + scrollLeft - clientLeft;
                offset.right = offset.right + scrollLeft - clientLeft;

                return offset;
            };

            /**
             * 获取元素内部文本
             *
             * @param {HTMLElement} element 目标元素
             * @return {string}
             */
            lib.getText = function (element) {
                var text = '';

                //  text 和 CDATA 节点，取nodeValue
                if (element.nodeType === 3 || element.nodeType === 4) {
                    text += element.nodeValue;
                }
                // 8 是 comment Node
                else if (element.nodeType !== 8) {
                    u.each(
                        element.childNodes,
                        function (child) {
                            text += lib.getText(child);
                        }
                    );
                }

                return text;
            };

            /**
             * @class lib.dom
             * @singleton
             */
            lib.dom = {};

            /**
             * 获取目标元素的第一个元素节点
             *
             * @param {HTMLElement | string} element 目标元素或其id
             * @return {HTMLElement | null} 目标元素的第一个元素节点，查找不到时返回null
             */
            lib.dom.first = function (element) {
                element = lib.g(element);

                if (element.firstElementChild) {
                    return element.firstElementChild;
                }

                var node = element.firstChild;
                for (; node; node = node.nextSibling) {
                    if (node.nodeType === 1) {
                        return node;
                    }
                }

                return null;
            };

            /**
             * 获取目标元素的最后一个元素节点
             *
             * @param {HTMLElement | string} element 目标元素或其id
             * @return {HTMLElement | null} 目标元素的第一个元素节点，查找不到时返回null
             */
            lib.dom.last = function (element) {
                element = lib.g(element);

                if (element.lastElementChild) {
                    return element.lastElementChild;
                }

                var node = element.lastChild;
                for (; node; node = node.previousSibling) {
                    if (node.nodeType === 1) {
                        return node;
                    }
                }

                return null;
            };

            /**
             * 获取目标元素的下一个兄弟元素节点
             *
             * @param {HTMLElement | string} element 目标元素或其id
             * @return {HTMLElement | null} 目标元素的下一个元素节点，查找不到时返回null
             */
            lib.dom.next = function (element) {
                element = lib.g(element);

                if (element.nextElementSibling) {
                    return element.nextElementSibling;
                }

                var node = element.nextSibling;
                for (; node; node = node.nextSibling) {
                    if (node.nodeType === 1) {
                        return node;
                    }
                }

                return null;
            };

            /**
             * 判断一个元素是否包含另一个元素
             *
             * @param {HTMLElement | string} container 包含元素或元素的 id
             * @param {HTMLElement | string} contained 被包含元素或元素的 id
             * @return {boolean} `contained`元素是否被包含于`container`元素的DOM节点上
             */
            lib.dom.contains = function (container, contained) {
                container = lib.g(container);
                contained = lib.g(contained);

                //fixme: 无法处理文本节点的情况(IE)
                return container.contains
                    ? container !== contained && container.contains(contained)
                    : !!(container.compareDocumentPosition(contained) & 16);
            };

            return lib;
        }
    );

    /**
     * ESUI (Enterprise Simple UI library)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file DOM属性相关基础库
     * @author otakustay
     */
    define(
        'esui/lib/attribute',['require','./dom'],function (require) {
            var dom = require('./dom');

            /**
             * @override lib
             */
            var lib = {};

            /**
             * 检查元素是否有指定的属性
             *
             * @param {HTMLElement} element 指定元素
             * @param {string} name 指定属性名称
             * @return {boolean}
             */
            lib.hasAttribute = function (element, name) {
                if (element.hasAttribute) {
                    return element.hasAttribute(name);
                }
                else {
                    return element.attributes
                        && element.attributes[name]
                        && element.attributes[name].specified;
                }
            };

            // 提供给 setAttribute 与 getAttribute 方法作名称转换使用
            var ATTRIBUTE_NAME_MAPPING = (function () {
                var result = {
                    cellpadding: 'cellPadding',
                    cellspacing: 'cellSpacing',
                    colspan: 'colSpan',
                    rowspan: 'rowSpan',
                    valign: 'vAlign',
                    usemap: 'useMap',
                    frameborder: 'frameBorder'
                };

                var div = document.createElement('div');
                div.innerHTML = '<label for="test" class="test"></label>';
                var label = div.getElementsByTagName('label')[0];

                if (label.getAttribute('className') === 'test') {
                    result['class'] = 'className';
                }
                else {
                    result.className = 'class';
                }

                if (label.getAttribute('for') === 'test') {
                    result.htmlFor = 'for';
                }
                else {
                    result['for'] = 'htmlFor';
                }

                return result;
            }());


            /**
             * 设置元素属性，会对某些值做转换
             *
             * @param {HTMLElement | string} element 目标元素或其id
             * @param {string} key 要设置的属性名
             * @param {string} value 要设置的属性值
             * @return {HTMLElement} 目标元素
             */
            lib.setAttribute = function (element, key, value) {
                element = dom.g(element);

                if (key === 'style') {
                    element.style.cssText = value;
                }
                else {
                    key = ATTRIBUTE_NAME_MAPPING[key] || key;
                    element.setAttribute(key, value);
                }

                return element;
            };

            /**
             * 获取目标元素的属性值
             *
             * @param {HTMLElement | string} element 目标元素或其id
             * @param {string} key 要获取的属性名称
             * @return {string | null} 目标元素的attribute值，获取不到时返回 null
             */
            lib.getAttribute = function (element, key) {
                element = dom.g(element);

                if (key === 'style') {
                    return element.style.cssText;
                }

                key = ATTRIBUTE_NAME_MAPPING[key] || key;
                return element.getAttribute(key);
            };

            /**
             * 移除元素属性
             *
             * @param {HTMLElement | string} element 目标元素或其id
             * @param {string} key 属性名称
             */
            lib.removeAttribute = function (element, key) {
                element = dom.g(element);

                key = ATTRIBUTE_NAME_MAPPING[key] || key;
                element.removeAttribute(key);
            };

            return lib;
        }
    );

    /**
     * ESUI (Enterprise Simple UI library)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file DOM class基础库
     * @author otakustay
     */
    define(
        'esui/lib/class',['require','underscore','./dom'],function (require) {
            var u = require('underscore');
            var dom = require('./dom');

            /**
             * @override lib
             */
            var lib = {};

            function getClassList(element) {
                return element.className
                    ? element.className.split(/\s+/)
                    : [];
            }

            /**
             * 判断元素是否拥有指定的class
             *
             * @param {HTMLElement | string} element 目标元素或其id
             * @param {string} className 要判断的class名称
             * @return {boolean} 是否拥有指定的class
             */
            lib.hasClass = function (element, className) {
                element = dom.g(element);

                if (className === '') {
                    throw new Error('className must not be empty');
                }

                if (!element || !className) {
                    return false;
                }

                if (element.classList) {
                    return element.classList.contains(className);
                }

                var classes = getClassList(element);
                return u.contains(classes, className);
            };

            /**
             * 为目标元素添加class
             *
             * @param {HTMLElement | string} element 目标元素或其id
             * @param {string} className 要添加的class名称
             * @return {HTMLElement} 目标元素
             */
            lib.addClass = function (element, className) {
                element = dom.g(element);

                if (className === '') {
                    throw new Error('className must not be empty');
                }

                if (!element || !className) {
                    return element;
                }

                if (element.classList) {
                    element.classList.add(className);
                    return element;
                }

                var classes = getClassList(element);
                if (u.contains(classes, className)) {
                    return element;
                }

                classes.push(className);
                element.className = classes.join(' ');

                return element;
            };

            /**
             * 批量添加class
             *
             * @param {HTMLElement | string} element 目标元素或其id
             * @param {string[]} classes 需添加的class名称
             * @return {HTMLElement} 目标元素
             */
            lib.addClasses = function (element, classes) {
                element = dom.g(element);

                if (!element || !classes) {
                    return element;
                }

                if (element.classList) {
                    u.each(
                        classes,
                        function (className) {
                            element.classList.add(className);
                        }
                    );
                    return element;
                }

                var originalClasses = getClassList(element);
                var newClasses = u.union(originalClasses, classes);

                if (newClasses.length > originalClasses.length) {
                    element.className = newClasses.join(' ');
                }

                return element;
            };

            /**
             * 移除目标元素的class
             *
             * @param {HTMLElement | string} element 目标元素或目标元素的 id
             * @param {string} className 要移除的class名称
             * @return {HTMLElement} 目标元素
             */
            lib.removeClass = function (element, className) {
                element = dom.g(element);

                if (className === '') {
                    throw new Error('className must not be empty');
                }

                if (!element || !className) {
                    return element;
                }

                if (element.classList) {
                    element.classList.remove(className);
                    return element;
                }

                var classes = getClassList(element);
                var changed = false;
                // 这个方法比用`u.diff`要快
                for (var i = 0; i < classes.length; i++) {
                    if (classes[i] === className) {
                        classes.splice(i, 1);
                        i--;
                        changed = true;
                    }
                }

                if (changed) {
                    element.className = classes.join(' ');
                }

                return element;
            };

            /**
             * 批量移除class
             *
             * @param {HTMLElement | string} element 目标元素或其id
             * @param {string[]} classes 需移除的class名称
             * @return {HTMLElement} 目标元素
             */
            lib.removeClasses = function (element, classes) {
                element = dom.g(element);

                if (!element || !classes) {
                    return element;
                }

                if (element.classList) {
                    u.each(
                        classes,
                        function (className) {
                            element.classList.remove(className);
                        }
                    );
                    return element;
                }

                var originalClasses = getClassList(element);
                var newClasses = u.difference(originalClasses, classes);

                if (newClasses.length < originalClasses.length) {
                    element.className = newClasses.join(' ');
                }

                return element;
            };

            /**
             * 切换目标元素的class
             *
             * @param {HTMLElement} element 目标元素或目标元素的 id
             * @param {string} className 要切换的class名称
             * @return {HTMLElement} 目标元素
             */
            lib.toggleClass = function (element, className) {
                element = dom.g(element);

                if (className === '') {
                    throw new Error('className must not be empty');
                }

                if (!element || !className) {
                    return element;
                }

                if (element.classList) {
                    element.classList.toggle(className);
                    return element;
                }

                var classes = getClassList(element);
                var containsClass = false;
                for (var i = 0; i < classes.length; i++) {
                    if (classes[i] === className) {
                        classes.splice(i, 1);
                        containsClass = true;
                        i--;
                    }
                }

                if (!containsClass) {
                    classes.push(className);
                }
                element.className = classes.join(' ');

                return element;
            };

            return lib;
        }
    );

//! moment.js
//! version : 2.9.0
//! authors : Tim Wood, Iskren Chernev, Moment.js contributors
//! license : MIT
//! momentjs.com

    (function (undefined) {
        /************************************
         Constants
         ************************************/

        var moment,
            VERSION = '2.9.0',
        // the global-scope this is NOT the global object in Node.js
            globalScope = (typeof global !== 'undefined' && (typeof window === 'undefined' || window === global.window)) ? global : this,
            oldGlobalMoment,
            round = Math.round,
            hasOwnProperty = Object.prototype.hasOwnProperty,
            i,

            YEAR = 0,
            MONTH = 1,
            DATE = 2,
            HOUR = 3,
            MINUTE = 4,
            SECOND = 5,
            MILLISECOND = 6,

        // internal storage for locale config files
            locales = {},

        // extra moment internal properties (plugins register props here)
            momentProperties = [],

        // check for nodeJS
            hasModule = (typeof module !== 'undefined' && module && module.exports),

        // ASP.NET json date format regex
            aspNetJsonRegex = /^\/?Date\((\-?\d+)/i,
            aspNetTimeSpanJsonRegex = /(\-)?(?:(\d*)\.)?(\d+)\:(\d+)(?:\:(\d+)\.?(\d{3})?)?/,

        // from http://docs.closure-library.googlecode.com/git/closure_goog_date_date.js.source.html
        // somewhat more in line with 4.4.3.2 2004 spec, but allows decimal anywhere
            isoDurationRegex = /^(-)?P(?:(?:([0-9,.]*)Y)?(?:([0-9,.]*)M)?(?:([0-9,.]*)D)?(?:T(?:([0-9,.]*)H)?(?:([0-9,.]*)M)?(?:([0-9,.]*)S)?)?|([0-9,.]*)W)$/,

        // format tokens
            formattingTokens = /(\[[^\[]*\])|(\\)?(Mo|MM?M?M?|Do|DDDo|DD?D?D?|ddd?d?|do?|w[o|w]?|W[o|W]?|Q|YYYYYY|YYYYY|YYYY|YY|gg(ggg?)?|GG(GGG?)?|e|E|a|A|hh?|HH?|mm?|ss?|S{1,4}|x|X|zz?|ZZ?|.)/g,
            localFormattingTokens = /(\[[^\[]*\])|(\\)?(LTS|LT|LL?L?L?|l{1,4})/g,

        // parsing token regexes
            parseTokenOneOrTwoDigits = /\d\d?/, // 0 - 99
            parseTokenOneToThreeDigits = /\d{1,3}/, // 0 - 999
            parseTokenOneToFourDigits = /\d{1,4}/, // 0 - 9999
            parseTokenOneToSixDigits = /[+\-]?\d{1,6}/, // -999,999 - 999,999
            parseTokenDigits = /\d+/, // nonzero number of digits
            parseTokenWord = /[0-9]*['a-z\u00A0-\u05FF\u0700-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]+|[\u0600-\u06FF\/]+(\s*?[\u0600-\u06FF]+){1,2}/i, // any word (or two) characters or numbers including two/three word month in arabic.
            parseTokenTimezone = /Z|[\+\-]\d\d:?\d\d/gi, // +00:00 -00:00 +0000 -0000 or Z
            parseTokenT = /T/i, // T (ISO separator)
            parseTokenOffsetMs = /[\+\-]?\d+/, // 1234567890123
            parseTokenTimestampMs = /[\+\-]?\d+(\.\d{1,3})?/, // 123456789 123456789.123

        //strict parsing regexes
            parseTokenOneDigit = /\d/, // 0 - 9
            parseTokenTwoDigits = /\d\d/, // 00 - 99
            parseTokenThreeDigits = /\d{3}/, // 000 - 999
            parseTokenFourDigits = /\d{4}/, // 0000 - 9999
            parseTokenSixDigits = /[+-]?\d{6}/, // -999,999 - 999,999
            parseTokenSignedNumber = /[+-]?\d+/, // -inf - inf

        // iso 8601 regex
        // 0000-00-00 0000-W00 or 0000-W00-0 + T + 00 or 00:00 or 00:00:00 or 00:00:00.000 + +00:00 or +0000 or +00)
            isoRegex = /^\s*(?:[+-]\d{6}|\d{4})-(?:(\d\d-\d\d)|(W\d\d$)|(W\d\d-\d)|(\d\d\d))((T| )(\d\d(:\d\d(:\d\d(\.\d+)?)?)?)?([\+\-]\d\d(?::?\d\d)?|\s*Z)?)?$/,

            isoFormat = 'YYYY-MM-DDTHH:mm:ssZ',

            isoDates = [
                ['YYYYYY-MM-DD', /[+-]\d{6}-\d{2}-\d{2}/],
                ['YYYY-MM-DD', /\d{4}-\d{2}-\d{2}/],
                ['GGGG-[W]WW-E', /\d{4}-W\d{2}-\d/],
                ['GGGG-[W]WW', /\d{4}-W\d{2}/],
                ['YYYY-DDD', /\d{4}-\d{3}/]
            ],

        // iso time formats and regexes
            isoTimes = [
                ['HH:mm:ss.SSSS', /(T| )\d\d:\d\d:\d\d\.\d+/],
                ['HH:mm:ss', /(T| )\d\d:\d\d:\d\d/],
                ['HH:mm', /(T| )\d\d:\d\d/],
                ['HH', /(T| )\d\d/]
            ],

        // timezone chunker '+10:00' > ['10', '00'] or '-1530' > ['-', '15', '30']
            parseTimezoneChunker = /([\+\-]|\d\d)/gi,

        // getter and setter names
            proxyGettersAndSetters = 'Date|Hours|Minutes|Seconds|Milliseconds'.split('|'),
            unitMillisecondFactors = {
                'Milliseconds' : 1,
                'Seconds' : 1e3,
                'Minutes' : 6e4,
                'Hours' : 36e5,
                'Days' : 864e5,
                'Months' : 2592e6,
                'Years' : 31536e6
            },

            unitAliases = {
                ms : 'millisecond',
                s : 'second',
                m : 'minute',
                h : 'hour',
                d : 'day',
                D : 'date',
                w : 'week',
                W : 'isoWeek',
                M : 'month',
                Q : 'quarter',
                y : 'year',
                DDD : 'dayOfYear',
                e : 'weekday',
                E : 'isoWeekday',
                gg: 'weekYear',
                GG: 'isoWeekYear'
            },

            camelFunctions = {
                dayofyear : 'dayOfYear',
                isoweekday : 'isoWeekday',
                isoweek : 'isoWeek',
                weekyear : 'weekYear',
                isoweekyear : 'isoWeekYear'
            },

        // format function strings
            formatFunctions = {},

        // default relative time thresholds
            relativeTimeThresholds = {
                s: 45,  // seconds to minute
                m: 45,  // minutes to hour
                h: 22,  // hours to day
                d: 26,  // days to month
                M: 11   // months to year
            },

        // tokens to ordinalize and pad
            ordinalizeTokens = 'DDD w W M D d'.split(' '),
            paddedTokens = 'M D H h m s w W'.split(' '),

            formatTokenFunctions = {
                M    : function () {
                    return this.month() + 1;
                },
                MMM  : function (format) {
                    return this.localeData().monthsShort(this, format);
                },
                MMMM : function (format) {
                    return this.localeData().months(this, format);
                },
                D    : function () {
                    return this.date();
                },
                DDD  : function () {
                    return this.dayOfYear();
                },
                d    : function () {
                    return this.day();
                },
                dd   : function (format) {
                    return this.localeData().weekdaysMin(this, format);
                },
                ddd  : function (format) {
                    return this.localeData().weekdaysShort(this, format);
                },
                dddd : function (format) {
                    return this.localeData().weekdays(this, format);
                },
                w    : function () {
                    return this.week();
                },
                W    : function () {
                    return this.isoWeek();
                },
                YY   : function () {
                    return leftZeroFill(this.year() % 100, 2);
                },
                YYYY : function () {
                    return leftZeroFill(this.year(), 4);
                },
                YYYYY : function () {
                    return leftZeroFill(this.year(), 5);
                },
                YYYYYY : function () {
                    var y = this.year(), sign = y >= 0 ? '+' : '-';
                    return sign + leftZeroFill(Math.abs(y), 6);
                },
                gg   : function () {
                    return leftZeroFill(this.weekYear() % 100, 2);
                },
                gggg : function () {
                    return leftZeroFill(this.weekYear(), 4);
                },
                ggggg : function () {
                    return leftZeroFill(this.weekYear(), 5);
                },
                GG   : function () {
                    return leftZeroFill(this.isoWeekYear() % 100, 2);
                },
                GGGG : function () {
                    return leftZeroFill(this.isoWeekYear(), 4);
                },
                GGGGG : function () {
                    return leftZeroFill(this.isoWeekYear(), 5);
                },
                e : function () {
                    return this.weekday();
                },
                E : function () {
                    return this.isoWeekday();
                },
                a    : function () {
                    return this.localeData().meridiem(this.hours(), this.minutes(), true);
                },
                A    : function () {
                    return this.localeData().meridiem(this.hours(), this.minutes(), false);
                },
                H    : function () {
                    return this.hours();
                },
                h    : function () {
                    return this.hours() % 12 || 12;
                },
                m    : function () {
                    return this.minutes();
                },
                s    : function () {
                    return this.seconds();
                },
                S    : function () {
                    return toInt(this.milliseconds() / 100);
                },
                SS   : function () {
                    return leftZeroFill(toInt(this.milliseconds() / 10), 2);
                },
                SSS  : function () {
                    return leftZeroFill(this.milliseconds(), 3);
                },
                SSSS : function () {
                    return leftZeroFill(this.milliseconds(), 3);
                },
                Z    : function () {
                    var a = this.utcOffset(),
                        b = '+';
                    if (a < 0) {
                        a = -a;
                        b = '-';
                    }
                    return b + leftZeroFill(toInt(a / 60), 2) + ':' + leftZeroFill(toInt(a) % 60, 2);
                },
                ZZ   : function () {
                    var a = this.utcOffset(),
                        b = '+';
                    if (a < 0) {
                        a = -a;
                        b = '-';
                    }
                    return b + leftZeroFill(toInt(a / 60), 2) + leftZeroFill(toInt(a) % 60, 2);
                },
                z : function () {
                    return this.zoneAbbr();
                },
                zz : function () {
                    return this.zoneName();
                },
                x    : function () {
                    return this.valueOf();
                },
                X    : function () {
                    return this.unix();
                },
                Q : function () {
                    return this.quarter();
                }
            },

            deprecations = {},

            lists = ['months', 'monthsShort', 'weekdays', 'weekdaysShort', 'weekdaysMin'],

            updateInProgress = false;

        // Pick the first defined of two or three arguments. dfl comes from
        // default.
        function dfl(a, b, c) {
            switch (arguments.length) {
                case 2: return a != null ? a : b;
                case 3: return a != null ? a : b != null ? b : c;
                default: throw new Error('Implement me');
            }
        }

        function hasOwnProp(a, b) {
            return hasOwnProperty.call(a, b);
        }

        function defaultParsingFlags() {
            // We need to deep clone this object, and es5 standard is not very
            // helpful.
            return {
                empty : false,
                unusedTokens : [],
                unusedInput : [],
                overflow : -2,
                charsLeftOver : 0,
                nullInput : false,
                invalidMonth : null,
                invalidFormat : false,
                userInvalidated : false,
                iso: false
            };
        }

        function printMsg(msg) {
            if (moment.suppressDeprecationWarnings === false &&
                typeof console !== 'undefined' && console.warn) {
                console.warn('Deprecation warning: ' + msg);
            }
        }

        function deprecate(msg, fn) {
            var firstTime = true;
            return extend(function () {
                if (firstTime) {
                    printMsg(msg);
                    firstTime = false;
                }
                return fn.apply(this, arguments);
            }, fn);
        }

        function deprecateSimple(name, msg) {
            if (!deprecations[name]) {
                printMsg(msg);
                deprecations[name] = true;
            }
        }

        function padToken(func, count) {
            return function (a) {
                return leftZeroFill(func.call(this, a), count);
            };
        }
        function ordinalizeToken(func, period) {
            return function (a) {
                return this.localeData().ordinal(func.call(this, a), period);
            };
        }

        function monthDiff(a, b) {
            // difference in months
            var wholeMonthDiff = ((b.year() - a.year()) * 12) + (b.month() - a.month()),
            // b is in (anchor - 1 month, anchor + 1 month)
                anchor = a.clone().add(wholeMonthDiff, 'months'),
                anchor2, adjust;

            if (b - anchor < 0) {
                anchor2 = a.clone().add(wholeMonthDiff - 1, 'months');
                // linear across the month
                adjust = (b - anchor) / (anchor - anchor2);
            } else {
                anchor2 = a.clone().add(wholeMonthDiff + 1, 'months');
                // linear across the month
                adjust = (b - anchor) / (anchor2 - anchor);
            }

            return -(wholeMonthDiff + adjust);
        }

        while (ordinalizeTokens.length) {
            i = ordinalizeTokens.pop();
            formatTokenFunctions[i + 'o'] = ordinalizeToken(formatTokenFunctions[i], i);
        }
        while (paddedTokens.length) {
            i = paddedTokens.pop();
            formatTokenFunctions[i + i] = padToken(formatTokenFunctions[i], 2);
        }
        formatTokenFunctions.DDDD = padToken(formatTokenFunctions.DDD, 3);


        function meridiemFixWrap(locale, hour, meridiem) {
            var isPm;

            if (meridiem == null) {
                // nothing to do
                return hour;
            }
            if (locale.meridiemHour != null) {
                return locale.meridiemHour(hour, meridiem);
            } else if (locale.isPM != null) {
                // Fallback
                isPm = locale.isPM(meridiem);
                if (isPm && hour < 12) {
                    hour += 12;
                }
                if (!isPm && hour === 12) {
                    hour = 0;
                }
                return hour;
            } else {
                // thie is not supposed to happen
                return hour;
            }
        }

        /************************************
         Constructors
         ************************************/

        function Locale() {
        }

        // Moment prototype object
        function Moment(config, skipOverflow) {
            if (skipOverflow !== false) {
                checkOverflow(config);
            }
            copyConfig(this, config);
            this._d = new Date(+config._d);
            // Prevent infinite loop in case updateOffset creates new moment
            // objects.
            if (updateInProgress === false) {
                updateInProgress = true;
                moment.updateOffset(this);
                updateInProgress = false;
            }
        }

        // Duration Constructor
        function Duration(duration) {
            var normalizedInput = normalizeObjectUnits(duration),
                years = normalizedInput.year || 0,
                quarters = normalizedInput.quarter || 0,
                months = normalizedInput.month || 0,
                weeks = normalizedInput.week || 0,
                days = normalizedInput.day || 0,
                hours = normalizedInput.hour || 0,
                minutes = normalizedInput.minute || 0,
                seconds = normalizedInput.second || 0,
                milliseconds = normalizedInput.millisecond || 0;

            // representation for dateAddRemove
            this._milliseconds = +milliseconds +
                seconds * 1e3 + // 1000
                minutes * 6e4 + // 1000 * 60
                hours * 36e5; // 1000 * 60 * 60
            // Because of dateAddRemove treats 24 hours as different from a
            // day when working around DST, we need to store them separately
            this._days = +days +
                weeks * 7;
            // It is impossible translate months into days without knowing
            // which months you are are talking about, so we have to store
            // it separately.
            this._months = +months +
                quarters * 3 +
                years * 12;

            this._data = {};

            this._locale = moment.localeData();

            this._bubble();
        }

        /************************************
         Helpers
         ************************************/


        function extend(a, b) {
            for (var i in b) {
                if (hasOwnProp(b, i)) {
                    a[i] = b[i];
                }
            }

            if (hasOwnProp(b, 'toString')) {
                a.toString = b.toString;
            }

            if (hasOwnProp(b, 'valueOf')) {
                a.valueOf = b.valueOf;
            }

            return a;
        }

        function copyConfig(to, from) {
            var i, prop, val;

            if (typeof from._isAMomentObject !== 'undefined') {
                to._isAMomentObject = from._isAMomentObject;
            }
            if (typeof from._i !== 'undefined') {
                to._i = from._i;
            }
            if (typeof from._f !== 'undefined') {
                to._f = from._f;
            }
            if (typeof from._l !== 'undefined') {
                to._l = from._l;
            }
            if (typeof from._strict !== 'undefined') {
                to._strict = from._strict;
            }
            if (typeof from._tzm !== 'undefined') {
                to._tzm = from._tzm;
            }
            if (typeof from._isUTC !== 'undefined') {
                to._isUTC = from._isUTC;
            }
            if (typeof from._offset !== 'undefined') {
                to._offset = from._offset;
            }
            if (typeof from._pf !== 'undefined') {
                to._pf = from._pf;
            }
            if (typeof from._locale !== 'undefined') {
                to._locale = from._locale;
            }

            if (momentProperties.length > 0) {
                for (i in momentProperties) {
                    prop = momentProperties[i];
                    val = from[prop];
                    if (typeof val !== 'undefined') {
                        to[prop] = val;
                    }
                }
            }

            return to;
        }

        function absRound(number) {
            if (number < 0) {
                return Math.ceil(number);
            } else {
                return Math.floor(number);
            }
        }

        // left zero fill a number
        // see http://jsperf.com/left-zero-filling for performance comparison
        function leftZeroFill(number, targetLength, forceSign) {
            var output = '' + Math.abs(number),
                sign = number >= 0;

            while (output.length < targetLength) {
                output = '0' + output;
            }
            return (sign ? (forceSign ? '+' : '') : '-') + output;
        }

        function positiveMomentsDifference(base, other) {
            var res = {milliseconds: 0, months: 0};

            res.months = other.month() - base.month() +
                (other.year() - base.year()) * 12;
            if (base.clone().add(res.months, 'M').isAfter(other)) {
                --res.months;
            }

            res.milliseconds = +other - +(base.clone().add(res.months, 'M'));

            return res;
        }

        function momentsDifference(base, other) {
            var res;
            other = makeAs(other, base);
            if (base.isBefore(other)) {
                res = positiveMomentsDifference(base, other);
            } else {
                res = positiveMomentsDifference(other, base);
                res.milliseconds = -res.milliseconds;
                res.months = -res.months;
            }

            return res;
        }

        // TODO: remove 'name' arg after deprecation is removed
        function createAdder(direction, name) {
            return function (val, period) {
                var dur, tmp;
                //invert the arguments, but complain about it
                if (period !== null && !isNaN(+period)) {
                    deprecateSimple(name, 'moment().' + name  + '(period, number) is deprecated. Please use moment().' + name + '(number, period).');
                    tmp = val; val = period; period = tmp;
                }

                val = typeof val === 'string' ? +val : val;
                dur = moment.duration(val, period);
                addOrSubtractDurationFromMoment(this, dur, direction);
                return this;
            };
        }

        function addOrSubtractDurationFromMoment(mom, duration, isAdding, updateOffset) {
            var milliseconds = duration._milliseconds,
                days = duration._days,
                months = duration._months;
            updateOffset = updateOffset == null ? true : updateOffset;

            if (milliseconds) {
                mom._d.setTime(+mom._d + milliseconds * isAdding);
            }
            if (days) {
                rawSetter(mom, 'Date', rawGetter(mom, 'Date') + days * isAdding);
            }
            if (months) {
                rawMonthSetter(mom, rawGetter(mom, 'Month') + months * isAdding);
            }
            if (updateOffset) {
                moment.updateOffset(mom, days || months);
            }
        }

        // check if is an array
        function isArray(input) {
            return Object.prototype.toString.call(input) === '[object Array]';
        }

        function isDate(input) {
            return Object.prototype.toString.call(input) === '[object Date]' ||
                input instanceof Date;
        }

        // compare two arrays, return the number of differences
        function compareArrays(array1, array2, dontConvert) {
            var len = Math.min(array1.length, array2.length),
                lengthDiff = Math.abs(array1.length - array2.length),
                diffs = 0,
                i;
            for (i = 0; i < len; i++) {
                if ((dontConvert && array1[i] !== array2[i]) ||
                    (!dontConvert && toInt(array1[i]) !== toInt(array2[i]))) {
                    diffs++;
                }
            }
            return diffs + lengthDiff;
        }

        function normalizeUnits(units) {
            if (units) {
                var lowered = units.toLowerCase().replace(/(.)s$/, '$1');
                units = unitAliases[units] || camelFunctions[lowered] || lowered;
            }
            return units;
        }

        function normalizeObjectUnits(inputObject) {
            var normalizedInput = {},
                normalizedProp,
                prop;

            for (prop in inputObject) {
                if (hasOwnProp(inputObject, prop)) {
                    normalizedProp = normalizeUnits(prop);
                    if (normalizedProp) {
                        normalizedInput[normalizedProp] = inputObject[prop];
                    }
                }
            }

            return normalizedInput;
        }

        function makeList(field) {
            var count, setter;

            if (field.indexOf('week') === 0) {
                count = 7;
                setter = 'day';
            }
            else if (field.indexOf('month') === 0) {
                count = 12;
                setter = 'month';
            }
            else {
                return;
            }

            moment[field] = function (format, index) {
                var i, getter,
                    method = moment._locale[field],
                    results = [];

                if (typeof format === 'number') {
                    index = format;
                    format = undefined;
                }

                getter = function (i) {
                    var m = moment().utc().set(setter, i);
                    return method.call(moment._locale, m, format || '');
                };

                if (index != null) {
                    return getter(index);
                }
                else {
                    for (i = 0; i < count; i++) {
                        results.push(getter(i));
                    }
                    return results;
                }
            };
        }

        function toInt(argumentForCoercion) {
            var coercedNumber = +argumentForCoercion,
                value = 0;

            if (coercedNumber !== 0 && isFinite(coercedNumber)) {
                if (coercedNumber >= 0) {
                    value = Math.floor(coercedNumber);
                } else {
                    value = Math.ceil(coercedNumber);
                }
            }

            return value;
        }

        function daysInMonth(year, month) {
            return new Date(Date.UTC(year, month + 1, 0)).getUTCDate();
        }

        function weeksInYear(year, dow, doy) {
            return weekOfYear(moment([year, 11, 31 + dow - doy]), dow, doy).week;
        }

        function daysInYear(year) {
            return isLeapYear(year) ? 366 : 365;
        }

        function isLeapYear(year) {
            return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0;
        }

        function checkOverflow(m) {
            var overflow;
            if (m._a && m._pf.overflow === -2) {
                overflow =
                        m._a[MONTH] < 0 || m._a[MONTH] > 11 ? MONTH :
                        m._a[DATE] < 1 || m._a[DATE] > daysInMonth(m._a[YEAR], m._a[MONTH]) ? DATE :
                        m._a[HOUR] < 0 || m._a[HOUR] > 24 ||
                    (m._a[HOUR] === 24 && (m._a[MINUTE] !== 0 ||
                        m._a[SECOND] !== 0 ||
                        m._a[MILLISECOND] !== 0)) ? HOUR :
                        m._a[MINUTE] < 0 || m._a[MINUTE] > 59 ? MINUTE :
                        m._a[SECOND] < 0 || m._a[SECOND] > 59 ? SECOND :
                        m._a[MILLISECOND] < 0 || m._a[MILLISECOND] > 999 ? MILLISECOND :
                    -1;

                if (m._pf._overflowDayOfYear && (overflow < YEAR || overflow > DATE)) {
                    overflow = DATE;
                }

                m._pf.overflow = overflow;
            }
        }

        function isValid(m) {
            if (m._isValid == null) {
                m._isValid = !isNaN(m._d.getTime()) &&
                    m._pf.overflow < 0 &&
                    !m._pf.empty &&
                    !m._pf.invalidMonth &&
                    !m._pf.nullInput &&
                    !m._pf.invalidFormat &&
                    !m._pf.userInvalidated;

                if (m._strict) {
                    m._isValid = m._isValid &&
                        m._pf.charsLeftOver === 0 &&
                        m._pf.unusedTokens.length === 0 &&
                        m._pf.bigHour === undefined;
                }
            }
            return m._isValid;
        }

        function normalizeLocale(key) {
            return key ? key.toLowerCase().replace('_', '-') : key;
        }

        // pick the locale from the array
        // try ['en-au', 'en-gb'] as 'en-au', 'en-gb', 'en', as in move through the list trying each
        // substring from most specific to least, but move to the next array item if it's a more specific variant than the current root
        function chooseLocale(names) {
            var i = 0, j, next, locale, split;

            while (i < names.length) {
                split = normalizeLocale(names[i]).split('-');
                j = split.length;
                next = normalizeLocale(names[i + 1]);
                next = next ? next.split('-') : null;
                while (j > 0) {
                    locale = loadLocale(split.slice(0, j).join('-'));
                    if (locale) {
                        return locale;
                    }
                    if (next && next.length >= j && compareArrays(split, next, true) >= j - 1) {
                        //the next array item is better than a shallower substring of this one
                        break;
                    }
                    j--;
                }
                i++;
            }
            return null;
        }

        function loadLocale(name) {
            var oldLocale = null;
            if (!locales[name] && hasModule) {
                try {
                    oldLocale = moment.locale();
                    require('./locale/' + name);
                    // because defineLocale currently also sets the global locale, we want to undo that for lazy loaded locales
                    moment.locale(oldLocale);
                } catch (e) { }
            }
            return locales[name];
        }

        // Return a moment from input, that is local/utc/utcOffset equivalent to
        // model.
        function makeAs(input, model) {
            var res, diff;
            if (model._isUTC) {
                res = model.clone();
                diff = (moment.isMoment(input) || isDate(input) ?
                    +input : +moment(input)) - (+res);
                // Use low-level api, because this fn is low-level api.
                res._d.setTime(+res._d + diff);
                moment.updateOffset(res, false);
                return res;
            } else {
                return moment(input).local();
            }
        }

        /************************************
         Locale
         ************************************/


        extend(Locale.prototype, {

            set : function (config) {
                var prop, i;
                for (i in config) {
                    prop = config[i];
                    if (typeof prop === 'function') {
                        this[i] = prop;
                    } else {
                        this['_' + i] = prop;
                    }
                }
                // Lenient ordinal parsing accepts just a number in addition to
                // number + (possibly) stuff coming from _ordinalParseLenient.
                this._ordinalParseLenient = new RegExp(this._ordinalParse.source + '|' + /\d{1,2}/.source);
            },

            _months : 'January_February_March_April_May_June_July_August_September_October_November_December'.split('_'),
            months : function (m) {
                return this._months[m.month()];
            },

            _monthsShort : 'Jan_Feb_Mar_Apr_May_Jun_Jul_Aug_Sep_Oct_Nov_Dec'.split('_'),
            monthsShort : function (m) {
                return this._monthsShort[m.month()];
            },

            monthsParse : function (monthName, format, strict) {
                var i, mom, regex;

                if (!this._monthsParse) {
                    this._monthsParse = [];
                    this._longMonthsParse = [];
                    this._shortMonthsParse = [];
                }

                for (i = 0; i < 12; i++) {
                    // make the regex if we don't have it already
                    mom = moment.utc([2000, i]);
                    if (strict && !this._longMonthsParse[i]) {
                        this._longMonthsParse[i] = new RegExp('^' + this.months(mom, '').replace('.', '') + '$', 'i');
                        this._shortMonthsParse[i] = new RegExp('^' + this.monthsShort(mom, '').replace('.', '') + '$', 'i');
                    }
                    if (!strict && !this._monthsParse[i]) {
                        regex = '^' + this.months(mom, '') + '|^' + this.monthsShort(mom, '');
                        this._monthsParse[i] = new RegExp(regex.replace('.', ''), 'i');
                    }
                    // test the regex
                    if (strict && format === 'MMMM' && this._longMonthsParse[i].test(monthName)) {
                        return i;
                    } else if (strict && format === 'MMM' && this._shortMonthsParse[i].test(monthName)) {
                        return i;
                    } else if (!strict && this._monthsParse[i].test(monthName)) {
                        return i;
                    }
                }
            },

            _weekdays : 'Sunday_Monday_Tuesday_Wednesday_Thursday_Friday_Saturday'.split('_'),
            weekdays : function (m) {
                return this._weekdays[m.day()];
            },

            _weekdaysShort : 'Sun_Mon_Tue_Wed_Thu_Fri_Sat'.split('_'),
            weekdaysShort : function (m) {
                return this._weekdaysShort[m.day()];
            },

            _weekdaysMin : 'Su_Mo_Tu_We_Th_Fr_Sa'.split('_'),
            weekdaysMin : function (m) {
                return this._weekdaysMin[m.day()];
            },

            weekdaysParse : function (weekdayName) {
                var i, mom, regex;

                if (!this._weekdaysParse) {
                    this._weekdaysParse = [];
                }

                for (i = 0; i < 7; i++) {
                    // make the regex if we don't have it already
                    if (!this._weekdaysParse[i]) {
                        mom = moment([2000, 1]).day(i);
                        regex = '^' + this.weekdays(mom, '') + '|^' + this.weekdaysShort(mom, '') + '|^' + this.weekdaysMin(mom, '');
                        this._weekdaysParse[i] = new RegExp(regex.replace('.', ''), 'i');
                    }
                    // test the regex
                    if (this._weekdaysParse[i].test(weekdayName)) {
                        return i;
                    }
                }
            },

            _longDateFormat : {
                LTS : 'h:mm:ss A',
                LT : 'h:mm A',
                L : 'MM/DD/YYYY',
                LL : 'MMMM D, YYYY',
                LLL : 'MMMM D, YYYY LT',
                LLLL : 'dddd, MMMM D, YYYY LT'
            },
            longDateFormat : function (key) {
                var output = this._longDateFormat[key];
                if (!output && this._longDateFormat[key.toUpperCase()]) {
                    output = this._longDateFormat[key.toUpperCase()].replace(/MMMM|MM|DD|dddd/g, function (val) {
                        return val.slice(1);
                    });
                    this._longDateFormat[key] = output;
                }
                return output;
            },

            isPM : function (input) {
                // IE8 Quirks Mode & IE7 Standards Mode do not allow accessing strings like arrays
                // Using charAt should be more compatible.
                return ((input + '').toLowerCase().charAt(0) === 'p');
            },

            _meridiemParse : /[ap]\.?m?\.?/i,
            meridiem : function (hours, minutes, isLower) {
                if (hours > 11) {
                    return isLower ? 'pm' : 'PM';
                } else {
                    return isLower ? 'am' : 'AM';
                }
            },


            _calendar : {
                sameDay : '[Today at] LT',
                nextDay : '[Tomorrow at] LT',
                nextWeek : 'dddd [at] LT',
                lastDay : '[Yesterday at] LT',
                lastWeek : '[Last] dddd [at] LT',
                sameElse : 'L'
            },
            calendar : function (key, mom, now) {
                var output = this._calendar[key];
                return typeof output === 'function' ? output.apply(mom, [now]) : output;
            },

            _relativeTime : {
                future : 'in %s',
                past : '%s ago',
                s : 'a few seconds',
                m : 'a minute',
                mm : '%d minutes',
                h : 'an hour',
                hh : '%d hours',
                d : 'a day',
                dd : '%d days',
                M : 'a month',
                MM : '%d months',
                y : 'a year',
                yy : '%d years'
            },

            relativeTime : function (number, withoutSuffix, string, isFuture) {
                var output = this._relativeTime[string];
                return (typeof output === 'function') ?
                    output(number, withoutSuffix, string, isFuture) :
                    output.replace(/%d/i, number);
            },

            pastFuture : function (diff, output) {
                var format = this._relativeTime[diff > 0 ? 'future' : 'past'];
                return typeof format === 'function' ? format(output) : format.replace(/%s/i, output);
            },

            ordinal : function (number) {
                return this._ordinal.replace('%d', number);
            },
            _ordinal : '%d',
            _ordinalParse : /\d{1,2}/,

            preparse : function (string) {
                return string;
            },

            postformat : function (string) {
                return string;
            },

            week : function (mom) {
                return weekOfYear(mom, this._week.dow, this._week.doy).week;
            },

            _week : {
                dow : 0, // Sunday is the first day of the week.
                doy : 6  // The week that contains Jan 1st is the first week of the year.
            },

            firstDayOfWeek : function () {
                return this._week.dow;
            },

            firstDayOfYear : function () {
                return this._week.doy;
            },

            _invalidDate: 'Invalid date',
            invalidDate: function () {
                return this._invalidDate;
            }
        });

        /************************************
         Formatting
         ************************************/


        function removeFormattingTokens(input) {
            if (input.match(/\[[\s\S]/)) {
                return input.replace(/^\[|\]$/g, '');
            }
            return input.replace(/\\/g, '');
        }

        function makeFormatFunction(format) {
            var array = format.match(formattingTokens), i, length;

            for (i = 0, length = array.length; i < length; i++) {
                if (formatTokenFunctions[array[i]]) {
                    array[i] = formatTokenFunctions[array[i]];
                } else {
                    array[i] = removeFormattingTokens(array[i]);
                }
            }

            return function (mom) {
                var output = '';
                for (i = 0; i < length; i++) {
                    output += array[i] instanceof Function ? array[i].call(mom, format) : array[i];
                }
                return output;
            };
        }

        // format date using native date object
        function formatMoment(m, format) {
            if (!m.isValid()) {
                return m.localeData().invalidDate();
            }

            format = expandFormat(format, m.localeData());

            if (!formatFunctions[format]) {
                formatFunctions[format] = makeFormatFunction(format);
            }

            return formatFunctions[format](m);
        }

        function expandFormat(format, locale) {
            var i = 5;

            function replaceLongDateFormatTokens(input) {
                return locale.longDateFormat(input) || input;
            }

            localFormattingTokens.lastIndex = 0;
            while (i >= 0 && localFormattingTokens.test(format)) {
                format = format.replace(localFormattingTokens, replaceLongDateFormatTokens);
                localFormattingTokens.lastIndex = 0;
                i -= 1;
            }

            return format;
        }


        /************************************
         Parsing
         ************************************/


            // get the regex to find the next token
        function getParseRegexForToken(token, config) {
            var a, strict = config._strict;
            switch (token) {
                case 'Q':
                    return parseTokenOneDigit;
                case 'DDDD':
                    return parseTokenThreeDigits;
                case 'YYYY':
                case 'GGGG':
                case 'gggg':
                    return strict ? parseTokenFourDigits : parseTokenOneToFourDigits;
                case 'Y':
                case 'G':
                case 'g':
                    return parseTokenSignedNumber;
                case 'YYYYYY':
                case 'YYYYY':
                case 'GGGGG':
                case 'ggggg':
                    return strict ? parseTokenSixDigits : parseTokenOneToSixDigits;
                case 'S':
                    if (strict) {
                        return parseTokenOneDigit;
                    }
                /* falls through */
                case 'SS':
                    if (strict) {
                        return parseTokenTwoDigits;
                    }
                /* falls through */
                case 'SSS':
                    if (strict) {
                        return parseTokenThreeDigits;
                    }
                /* falls through */
                case 'DDD':
                    return parseTokenOneToThreeDigits;
                case 'MMM':
                case 'MMMM':
                case 'dd':
                case 'ddd':
                case 'dddd':
                    return parseTokenWord;
                case 'a':
                case 'A':
                    return config._locale._meridiemParse;
                case 'x':
                    return parseTokenOffsetMs;
                case 'X':
                    return parseTokenTimestampMs;
                case 'Z':
                case 'ZZ':
                    return parseTokenTimezone;
                case 'T':
                    return parseTokenT;
                case 'SSSS':
                    return parseTokenDigits;
                case 'MM':
                case 'DD':
                case 'YY':
                case 'GG':
                case 'gg':
                case 'HH':
                case 'hh':
                case 'mm':
                case 'ss':
                case 'ww':
                case 'WW':
                    return strict ? parseTokenTwoDigits : parseTokenOneOrTwoDigits;
                case 'M':
                case 'D':
                case 'd':
                case 'H':
                case 'h':
                case 'm':
                case 's':
                case 'w':
                case 'W':
                case 'e':
                case 'E':
                    return parseTokenOneOrTwoDigits;
                case 'Do':
                    return strict ? config._locale._ordinalParse : config._locale._ordinalParseLenient;
                default :
                    a = new RegExp(regexpEscape(unescapeFormat(token.replace('\\', '')), 'i'));
                    return a;
            }
        }

        function utcOffsetFromString(string) {
            string = string || '';
            var possibleTzMatches = (string.match(parseTokenTimezone) || []),
                tzChunk = possibleTzMatches[possibleTzMatches.length - 1] || [],
                parts = (tzChunk + '').match(parseTimezoneChunker) || ['-', 0, 0],
                minutes = +(parts[1] * 60) + toInt(parts[2]);

            return parts[0] === '+' ? minutes : -minutes;
        }

        // function to convert string input to date
        function addTimeToArrayFromToken(token, input, config) {
            var a, datePartArray = config._a;

            switch (token) {
                // QUARTER
                case 'Q':
                    if (input != null) {
                        datePartArray[MONTH] = (toInt(input) - 1) * 3;
                    }
                    break;
                // MONTH
                case 'M' : // fall through to MM
                case 'MM' :
                    if (input != null) {
                        datePartArray[MONTH] = toInt(input) - 1;
                    }
                    break;
                case 'MMM' : // fall through to MMMM
                case 'MMMM' :
                    a = config._locale.monthsParse(input, token, config._strict);
                    // if we didn't find a month name, mark the date as invalid.
                    if (a != null) {
                        datePartArray[MONTH] = a;
                    } else {
                        config._pf.invalidMonth = input;
                    }
                    break;
                // DAY OF MONTH
                case 'D' : // fall through to DD
                case 'DD' :
                    if (input != null) {
                        datePartArray[DATE] = toInt(input);
                    }
                    break;
                case 'Do' :
                    if (input != null) {
                        datePartArray[DATE] = toInt(parseInt(
                            input.match(/\d{1,2}/)[0], 10));
                    }
                    break;
                // DAY OF YEAR
                case 'DDD' : // fall through to DDDD
                case 'DDDD' :
                    if (input != null) {
                        config._dayOfYear = toInt(input);
                    }

                    break;
                // YEAR
                case 'YY' :
                    datePartArray[YEAR] = moment.parseTwoDigitYear(input);
                    break;
                case 'YYYY' :
                case 'YYYYY' :
                case 'YYYYYY' :
                    datePartArray[YEAR] = toInt(input);
                    break;
                // AM / PM
                case 'a' : // fall through to A
                case 'A' :
                    config._meridiem = input;
                    // config._isPm = config._locale.isPM(input);
                    break;
                // HOUR
                case 'h' : // fall through to hh
                case 'hh' :
                    config._pf.bigHour = true;
                /* falls through */
                case 'H' : // fall through to HH
                case 'HH' :
                    datePartArray[HOUR] = toInt(input);
                    break;
                // MINUTE
                case 'm' : // fall through to mm
                case 'mm' :
                    datePartArray[MINUTE] = toInt(input);
                    break;
                // SECOND
                case 's' : // fall through to ss
                case 'ss' :
                    datePartArray[SECOND] = toInt(input);
                    break;
                // MILLISECOND
                case 'S' :
                case 'SS' :
                case 'SSS' :
                case 'SSSS' :
                    datePartArray[MILLISECOND] = toInt(('0.' + input) * 1000);
                    break;
                // UNIX OFFSET (MILLISECONDS)
                case 'x':
                    config._d = new Date(toInt(input));
                    break;
                // UNIX TIMESTAMP WITH MS
                case 'X':
                    config._d = new Date(parseFloat(input) * 1000);
                    break;
                // TIMEZONE
                case 'Z' : // fall through to ZZ
                case 'ZZ' :
                    config._useUTC = true;
                    config._tzm = utcOffsetFromString(input);
                    break;
                // WEEKDAY - human
                case 'dd':
                case 'ddd':
                case 'dddd':
                    a = config._locale.weekdaysParse(input);
                    // if we didn't get a weekday name, mark the date as invalid
                    if (a != null) {
                        config._w = config._w || {};
                        config._w['d'] = a;
                    } else {
                        config._pf.invalidWeekday = input;
                    }
                    break;
                // WEEK, WEEK DAY - numeric
                case 'w':
                case 'ww':
                case 'W':
                case 'WW':
                case 'd':
                case 'e':
                case 'E':
                    token = token.substr(0, 1);
                /* falls through */
                case 'gggg':
                case 'GGGG':
                case 'GGGGG':
                    token = token.substr(0, 2);
                    if (input) {
                        config._w = config._w || {};
                        config._w[token] = toInt(input);
                    }
                    break;
                case 'gg':
                case 'GG':
                    config._w = config._w || {};
                    config._w[token] = moment.parseTwoDigitYear(input);
            }
        }

        function dayOfYearFromWeekInfo(config) {
            var w, weekYear, week, weekday, dow, doy, temp;

            w = config._w;
            if (w.GG != null || w.W != null || w.E != null) {
                dow = 1;
                doy = 4;

                // TODO: We need to take the current isoWeekYear, but that depends on
                // how we interpret now (local, utc, fixed offset). So create
                // a now version of current config (take local/utc/offset flags, and
                // create now).
                weekYear = dfl(w.GG, config._a[YEAR], weekOfYear(moment(), 1, 4).year);
                week = dfl(w.W, 1);
                weekday = dfl(w.E, 1);
            } else {
                dow = config._locale._week.dow;
                doy = config._locale._week.doy;

                weekYear = dfl(w.gg, config._a[YEAR], weekOfYear(moment(), dow, doy).year);
                week = dfl(w.w, 1);

                if (w.d != null) {
                    // weekday -- low day numbers are considered next week
                    weekday = w.d;
                    if (weekday < dow) {
                        ++week;
                    }
                } else if (w.e != null) {
                    // local weekday -- counting starts from begining of week
                    weekday = w.e + dow;
                } else {
                    // default to begining of week
                    weekday = dow;
                }
            }
            temp = dayOfYearFromWeeks(weekYear, week, weekday, doy, dow);

            config._a[YEAR] = temp.year;
            config._dayOfYear = temp.dayOfYear;
        }

        // convert an array to a date.
        // the array should mirror the parameters below
        // note: all values past the year are optional and will default to the lowest possible value.
        // [year, month, day , hour, minute, second, millisecond]
        function dateFromConfig(config) {
            var i, date, input = [], currentDate, yearToUse;

            if (config._d) {
                return;
            }

            currentDate = currentDateArray(config);

            //compute day of the year from weeks and weekdays
            if (config._w && config._a[DATE] == null && config._a[MONTH] == null) {
                dayOfYearFromWeekInfo(config);
            }

            //if the day of the year is set, figure out what it is
            if (config._dayOfYear) {
                yearToUse = dfl(config._a[YEAR], currentDate[YEAR]);

                if (config._dayOfYear > daysInYear(yearToUse)) {
                    config._pf._overflowDayOfYear = true;
                }

                date = makeUTCDate(yearToUse, 0, config._dayOfYear);
                config._a[MONTH] = date.getUTCMonth();
                config._a[DATE] = date.getUTCDate();
            }

            // Default to current date.
            // * if no year, month, day of month are given, default to today
            // * if day of month is given, default month and year
            // * if month is given, default only year
            // * if year is given, don't default anything
            for (i = 0; i < 3 && config._a[i] == null; ++i) {
                config._a[i] = input[i] = currentDate[i];
            }

            // Zero out whatever was not defaulted, including time
            for (; i < 7; i++) {
                config._a[i] = input[i] = (config._a[i] == null) ? (i === 2 ? 1 : 0) : config._a[i];
            }

            // Check for 24:00:00.000
            if (config._a[HOUR] === 24 &&
                config._a[MINUTE] === 0 &&
                config._a[SECOND] === 0 &&
                config._a[MILLISECOND] === 0) {
                config._nextDay = true;
                config._a[HOUR] = 0;
            }

            config._d = (config._useUTC ? makeUTCDate : makeDate).apply(null, input);
            // Apply timezone offset from input. The actual utcOffset can be changed
            // with parseZone.
            if (config._tzm != null) {
                config._d.setUTCMinutes(config._d.getUTCMinutes() - config._tzm);
            }

            if (config._nextDay) {
                config._a[HOUR] = 24;
            }
        }

        function dateFromObject(config) {
            var normalizedInput;

            if (config._d) {
                return;
            }

            normalizedInput = normalizeObjectUnits(config._i);
            config._a = [
                normalizedInput.year,
                normalizedInput.month,
                    normalizedInput.day || normalizedInput.date,
                normalizedInput.hour,
                normalizedInput.minute,
                normalizedInput.second,
                normalizedInput.millisecond
            ];

            dateFromConfig(config);
        }

        function currentDateArray(config) {
            var now = new Date();
            if (config._useUTC) {
                return [
                    now.getUTCFullYear(),
                    now.getUTCMonth(),
                    now.getUTCDate()
                ];
            } else {
                return [now.getFullYear(), now.getMonth(), now.getDate()];
            }
        }

        // date from string and format string
        function makeDateFromStringAndFormat(config) {
            if (config._f === moment.ISO_8601) {
                parseISO(config);
                return;
            }

            config._a = [];
            config._pf.empty = true;

            // This array is used to make a Date, either with `new Date` or `Date.UTC`
            var string = '' + config._i,
                i, parsedInput, tokens, token, skipped,
                stringLength = string.length,
                totalParsedInputLength = 0;

            tokens = expandFormat(config._f, config._locale).match(formattingTokens) || [];

            for (i = 0; i < tokens.length; i++) {
                token = tokens[i];
                parsedInput = (string.match(getParseRegexForToken(token, config)) || [])[0];
                if (parsedInput) {
                    skipped = string.substr(0, string.indexOf(parsedInput));
                    if (skipped.length > 0) {
                        config._pf.unusedInput.push(skipped);
                    }
                    string = string.slice(string.indexOf(parsedInput) + parsedInput.length);
                    totalParsedInputLength += parsedInput.length;
                }
                // don't parse if it's not a known token
                if (formatTokenFunctions[token]) {
                    if (parsedInput) {
                        config._pf.empty = false;
                    }
                    else {
                        config._pf.unusedTokens.push(token);
                    }
                    addTimeToArrayFromToken(token, parsedInput, config);
                }
                else if (config._strict && !parsedInput) {
                    config._pf.unusedTokens.push(token);
                }
            }

            // add remaining unparsed input length to the string
            config._pf.charsLeftOver = stringLength - totalParsedInputLength;
            if (string.length > 0) {
                config._pf.unusedInput.push(string);
            }

            // clear _12h flag if hour is <= 12
            if (config._pf.bigHour === true && config._a[HOUR] <= 12) {
                config._pf.bigHour = undefined;
            }
            // handle meridiem
            config._a[HOUR] = meridiemFixWrap(config._locale, config._a[HOUR],
                config._meridiem);
            dateFromConfig(config);
            checkOverflow(config);
        }

        function unescapeFormat(s) {
            return s.replace(/\\(\[)|\\(\])|\[([^\]\[]*)\]|\\(.)/g, function (matched, p1, p2, p3, p4) {
                return p1 || p2 || p3 || p4;
            });
        }

        // Code from http://stackoverflow.com/questions/3561493/is-there-a-regexp-escape-function-in-javascript
        function regexpEscape(s) {
            return s.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
        }

        // date from string and array of format strings
        function makeDateFromStringAndArray(config) {
            var tempConfig,
                bestMoment,

                scoreToBeat,
                i,
                currentScore;

            if (config._f.length === 0) {
                config._pf.invalidFormat = true;
                config._d = new Date(NaN);
                return;
            }

            for (i = 0; i < config._f.length; i++) {
                currentScore = 0;
                tempConfig = copyConfig({}, config);
                if (config._useUTC != null) {
                    tempConfig._useUTC = config._useUTC;
                }
                tempConfig._pf = defaultParsingFlags();
                tempConfig._f = config._f[i];
                makeDateFromStringAndFormat(tempConfig);

                if (!isValid(tempConfig)) {
                    continue;
                }

                // if there is any input that was not parsed add a penalty for that format
                currentScore += tempConfig._pf.charsLeftOver;

                //or tokens
                currentScore += tempConfig._pf.unusedTokens.length * 10;

                tempConfig._pf.score = currentScore;

                if (scoreToBeat == null || currentScore < scoreToBeat) {
                    scoreToBeat = currentScore;
                    bestMoment = tempConfig;
                }
            }

            extend(config, bestMoment || tempConfig);
        }

        // date from iso format
        function parseISO(config) {
            var i, l,
                string = config._i,
                match = isoRegex.exec(string);

            if (match) {
                config._pf.iso = true;
                for (i = 0, l = isoDates.length; i < l; i++) {
                    if (isoDates[i][1].exec(string)) {
                        // match[5] should be 'T' or undefined
                        config._f = isoDates[i][0] + (match[6] || ' ');
                        break;
                    }
                }
                for (i = 0, l = isoTimes.length; i < l; i++) {
                    if (isoTimes[i][1].exec(string)) {
                        config._f += isoTimes[i][0];
                        break;
                    }
                }
                if (string.match(parseTokenTimezone)) {
                    config._f += 'Z';
                }
                makeDateFromStringAndFormat(config);
            } else {
                config._isValid = false;
            }
        }

        // date from iso format or fallback
        function makeDateFromString(config) {
            parseISO(config);
            if (config._isValid === false) {
                delete config._isValid;
                moment.createFromInputFallback(config);
            }
        }

        function map(arr, fn) {
            var res = [], i;
            for (i = 0; i < arr.length; ++i) {
                res.push(fn(arr[i], i));
            }
            return res;
        }

        function makeDateFromInput(config) {
            var input = config._i, matched;
            if (input === undefined) {
                config._d = new Date();
            } else if (isDate(input)) {
                config._d = new Date(+input);
            } else if ((matched = aspNetJsonRegex.exec(input)) !== null) {
                config._d = new Date(+matched[1]);
            } else if (typeof input === 'string') {
                makeDateFromString(config);
            } else if (isArray(input)) {
                config._a = map(input.slice(0), function (obj) {
                    return parseInt(obj, 10);
                });
                dateFromConfig(config);
            } else if (typeof(input) === 'object') {
                dateFromObject(config);
            } else if (typeof(input) === 'number') {
                // from milliseconds
                config._d = new Date(input);
            } else {
                moment.createFromInputFallback(config);
            }
        }

        function makeDate(y, m, d, h, M, s, ms) {
            //can't just apply() to create a date:
            //http://stackoverflow.com/questions/181348/instantiating-a-javascript-object-by-calling-prototype-constructor-apply
            var date = new Date(y, m, d, h, M, s, ms);

            //the date constructor doesn't accept years < 1970
            if (y < 1970) {
                date.setFullYear(y);
            }
            return date;
        }

        function makeUTCDate(y) {
            var date = new Date(Date.UTC.apply(null, arguments));
            if (y < 1970) {
                date.setUTCFullYear(y);
            }
            return date;
        }

        function parseWeekday(input, locale) {
            if (typeof input === 'string') {
                if (!isNaN(input)) {
                    input = parseInt(input, 10);
                }
                else {
                    input = locale.weekdaysParse(input);
                    if (typeof input !== 'number') {
                        return null;
                    }
                }
            }
            return input;
        }

        /************************************
         Relative Time
         ************************************/


            // helper function for moment.fn.from, moment.fn.fromNow, and moment.duration.fn.humanize
        function substituteTimeAgo(string, number, withoutSuffix, isFuture, locale) {
            return locale.relativeTime(number || 1, !!withoutSuffix, string, isFuture);
        }

        function relativeTime(posNegDuration, withoutSuffix, locale) {
            var duration = moment.duration(posNegDuration).abs(),
                seconds = round(duration.as('s')),
                minutes = round(duration.as('m')),
                hours = round(duration.as('h')),
                days = round(duration.as('d')),
                months = round(duration.as('M')),
                years = round(duration.as('y')),

                args = seconds < relativeTimeThresholds.s && ['s', seconds] ||
                    minutes === 1 && ['m'] ||
                    minutes < relativeTimeThresholds.m && ['mm', minutes] ||
                    hours === 1 && ['h'] ||
                    hours < relativeTimeThresholds.h && ['hh', hours] ||
                    days === 1 && ['d'] ||
                    days < relativeTimeThresholds.d && ['dd', days] ||
                    months === 1 && ['M'] ||
                    months < relativeTimeThresholds.M && ['MM', months] ||
                    years === 1 && ['y'] || ['yy', years];

            args[2] = withoutSuffix;
            args[3] = +posNegDuration > 0;
            args[4] = locale;
            return substituteTimeAgo.apply({}, args);
        }


        /************************************
         Week of Year
         ************************************/


            // firstDayOfWeek       0 = sun, 6 = sat
            //                      the day of the week that starts the week
            //                      (usually sunday or monday)
            // firstDayOfWeekOfYear 0 = sun, 6 = sat
            //                      the first week is the week that contains the first
            //                      of this day of the week
            //                      (eg. ISO weeks use thursday (4))
        function weekOfYear(mom, firstDayOfWeek, firstDayOfWeekOfYear) {
            var end = firstDayOfWeekOfYear - firstDayOfWeek,
                daysToDayOfWeek = firstDayOfWeekOfYear - mom.day(),
                adjustedMoment;


            if (daysToDayOfWeek > end) {
                daysToDayOfWeek -= 7;
            }

            if (daysToDayOfWeek < end - 7) {
                daysToDayOfWeek += 7;
            }

            adjustedMoment = moment(mom).add(daysToDayOfWeek, 'd');
            return {
                week: Math.ceil(adjustedMoment.dayOfYear() / 7),
                year: adjustedMoment.year()
            };
        }

        //http://en.wikipedia.org/wiki/ISO_week_date#Calculating_a_date_given_the_year.2C_week_number_and_weekday
        function dayOfYearFromWeeks(year, week, weekday, firstDayOfWeekOfYear, firstDayOfWeek) {
            var d = makeUTCDate(year, 0, 1).getUTCDay(), daysToAdd, dayOfYear;

            d = d === 0 ? 7 : d;
            weekday = weekday != null ? weekday : firstDayOfWeek;
            daysToAdd = firstDayOfWeek - d + (d > firstDayOfWeekOfYear ? 7 : 0) - (d < firstDayOfWeek ? 7 : 0);
            dayOfYear = 7 * (week - 1) + (weekday - firstDayOfWeek) + daysToAdd + 1;

            return {
                year: dayOfYear > 0 ? year : year - 1,
                dayOfYear: dayOfYear > 0 ?  dayOfYear : daysInYear(year - 1) + dayOfYear
            };
        }

        /************************************
         Top Level Functions
         ************************************/

        function makeMoment(config) {
            var input = config._i,
                format = config._f,
                res;

            config._locale = config._locale || moment.localeData(config._l);

            if (input === null || (format === undefined && input === '')) {
                return moment.invalid({nullInput: true});
            }

            if (typeof input === 'string') {
                config._i = input = config._locale.preparse(input);
            }

            if (moment.isMoment(input)) {
                return new Moment(input, true);
            } else if (format) {
                if (isArray(format)) {
                    makeDateFromStringAndArray(config);
                } else {
                    makeDateFromStringAndFormat(config);
                }
            } else {
                makeDateFromInput(config);
            }

            res = new Moment(config);
            if (res._nextDay) {
                // Adding is smart enough around DST
                res.add(1, 'd');
                res._nextDay = undefined;
            }

            return res;
        }

        moment = function (input, format, locale, strict) {
            var c;

            if (typeof(locale) === 'boolean') {
                strict = locale;
                locale = undefined;
            }
            // object construction must be done this way.
            // https://github.com/moment/moment/issues/1423
            c = {};
            c._isAMomentObject = true;
            c._i = input;
            c._f = format;
            c._l = locale;
            c._strict = strict;
            c._isUTC = false;
            c._pf = defaultParsingFlags();

            return makeMoment(c);
        };

        moment.suppressDeprecationWarnings = false;

        moment.createFromInputFallback = deprecate(
                'moment construction falls back to js Date. This is ' +
                'discouraged and will be removed in upcoming major ' +
                'release. Please refer to ' +
                'https://github.com/moment/moment/issues/1407 for more info.',
            function (config) {
                config._d = new Date(config._i + (config._useUTC ? ' UTC' : ''));
            }
        );

        // Pick a moment m from moments so that m[fn](other) is true for all
        // other. This relies on the function fn to be transitive.
        //
        // moments should either be an array of moment objects or an array, whose
        // first element is an array of moment objects.
        function pickBy(fn, moments) {
            var res, i;
            if (moments.length === 1 && isArray(moments[0])) {
                moments = moments[0];
            }
            if (!moments.length) {
                return moment();
            }
            res = moments[0];
            for (i = 1; i < moments.length; ++i) {
                if (moments[i][fn](res)) {
                    res = moments[i];
                }
            }
            return res;
        }

        moment.min = function () {
            var args = [].slice.call(arguments, 0);

            return pickBy('isBefore', args);
        };

        moment.max = function () {
            var args = [].slice.call(arguments, 0);

            return pickBy('isAfter', args);
        };

        // creating with utc
        moment.utc = function (input, format, locale, strict) {
            var c;

            if (typeof(locale) === 'boolean') {
                strict = locale;
                locale = undefined;
            }
            // object construction must be done this way.
            // https://github.com/moment/moment/issues/1423
            c = {};
            c._isAMomentObject = true;
            c._useUTC = true;
            c._isUTC = true;
            c._l = locale;
            c._i = input;
            c._f = format;
            c._strict = strict;
            c._pf = defaultParsingFlags();

            return makeMoment(c).utc();
        };

        // creating with unix timestamp (in seconds)
        moment.unix = function (input) {
            return moment(input * 1000);
        };

        // duration
        moment.duration = function (input, key) {
            var duration = input,
            // matching against regexp is expensive, do it on demand
                match = null,
                sign,
                ret,
                parseIso,
                diffRes;

            if (moment.isDuration(input)) {
                duration = {
                    ms: input._milliseconds,
                    d: input._days,
                    M: input._months
                };
            } else if (typeof input === 'number') {
                duration = {};
                if (key) {
                    duration[key] = input;
                } else {
                    duration.milliseconds = input;
                }
            } else if (!!(match = aspNetTimeSpanJsonRegex.exec(input))) {
                sign = (match[1] === '-') ? -1 : 1;
                duration = {
                    y: 0,
                    d: toInt(match[DATE]) * sign,
                    h: toInt(match[HOUR]) * sign,
                    m: toInt(match[MINUTE]) * sign,
                    s: toInt(match[SECOND]) * sign,
                    ms: toInt(match[MILLISECOND]) * sign
                };
            } else if (!!(match = isoDurationRegex.exec(input))) {
                sign = (match[1] === '-') ? -1 : 1;
                parseIso = function (inp) {
                    // We'd normally use ~~inp for this, but unfortunately it also
                    // converts floats to ints.
                    // inp may be undefined, so careful calling replace on it.
                    var res = inp && parseFloat(inp.replace(',', '.'));
                    // apply sign while we're at it
                    return (isNaN(res) ? 0 : res) * sign;
                };
                duration = {
                    y: parseIso(match[2]),
                    M: parseIso(match[3]),
                    d: parseIso(match[4]),
                    h: parseIso(match[5]),
                    m: parseIso(match[6]),
                    s: parseIso(match[7]),
                    w: parseIso(match[8])
                };
            } else if (duration == null) {// checks for null or undefined
                duration = {};
            } else if (typeof duration === 'object' &&
                ('from' in duration || 'to' in duration)) {
                diffRes = momentsDifference(moment(duration.from), moment(duration.to));

                duration = {};
                duration.ms = diffRes.milliseconds;
                duration.M = diffRes.months;
            }

            ret = new Duration(duration);

            if (moment.isDuration(input) && hasOwnProp(input, '_locale')) {
                ret._locale = input._locale;
            }

            return ret;
        };

        // version number
        moment.version = VERSION;

        // default format
        moment.defaultFormat = isoFormat;

        // constant that refers to the ISO standard
        moment.ISO_8601 = function () {};

        // Plugins that add properties should also add the key here (null value),
        // so we can properly clone ourselves.
        moment.momentProperties = momentProperties;

        // This function will be called whenever a moment is mutated.
        // It is intended to keep the offset in sync with the timezone.
        moment.updateOffset = function () {};

        // This function allows you to set a threshold for relative time strings
        moment.relativeTimeThreshold = function (threshold, limit) {
            if (relativeTimeThresholds[threshold] === undefined) {
                return false;
            }
            if (limit === undefined) {
                return relativeTimeThresholds[threshold];
            }
            relativeTimeThresholds[threshold] = limit;
            return true;
        };

        moment.lang = deprecate(
            'moment.lang is deprecated. Use moment.locale instead.',
            function (key, value) {
                return moment.locale(key, value);
            }
        );

        // This function will load locale and then set the global locale.  If
        // no arguments are passed in, it will simply return the current global
        // locale key.
        moment.locale = function (key, values) {
            var data;
            if (key) {
                if (typeof(values) !== 'undefined') {
                    data = moment.defineLocale(key, values);
                }
                else {
                    data = moment.localeData(key);
                }

                if (data) {
                    moment.duration._locale = moment._locale = data;
                }
            }

            return moment._locale._abbr;
        };

        moment.defineLocale = function (name, values) {
            if (values !== null) {
                values.abbr = name;
                if (!locales[name]) {
                    locales[name] = new Locale();
                }
                locales[name].set(values);

                // backwards compat for now: also set the locale
                moment.locale(name);

                return locales[name];
            } else {
                // useful for testing
                delete locales[name];
                return null;
            }
        };

        moment.langData = deprecate(
            'moment.langData is deprecated. Use moment.localeData instead.',
            function (key) {
                return moment.localeData(key);
            }
        );

        // returns locale data
        moment.localeData = function (key) {
            var locale;

            if (key && key._locale && key._locale._abbr) {
                key = key._locale._abbr;
            }

            if (!key) {
                return moment._locale;
            }

            if (!isArray(key)) {
                //short-circuit everything else
                locale = loadLocale(key);
                if (locale) {
                    return locale;
                }
                key = [key];
            }

            return chooseLocale(key);
        };

        // compare moment object
        moment.isMoment = function (obj) {
            return obj instanceof Moment ||
                (obj != null && hasOwnProp(obj, '_isAMomentObject'));
        };

        // for typechecking Duration objects
        moment.isDuration = function (obj) {
            return obj instanceof Duration;
        };

        for (i = lists.length - 1; i >= 0; --i) {
            makeList(lists[i]);
        }

        moment.normalizeUnits = function (units) {
            return normalizeUnits(units);
        };

        moment.invalid = function (flags) {
            var m = moment.utc(NaN);
            if (flags != null) {
                extend(m._pf, flags);
            }
            else {
                m._pf.userInvalidated = true;
            }

            return m;
        };

        moment.parseZone = function () {
            return moment.apply(null, arguments).parseZone();
        };

        moment.parseTwoDigitYear = function (input) {
            return toInt(input) + (toInt(input) > 68 ? 1900 : 2000);
        };

        moment.isDate = isDate;

        /************************************
         Moment Prototype
         ************************************/


        extend(moment.fn = Moment.prototype, {

            clone : function () {
                return moment(this);
            },

            valueOf : function () {
                return +this._d - ((this._offset || 0) * 60000);
            },

            unix : function () {
                return Math.floor(+this / 1000);
            },

            toString : function () {
                return this.clone().locale('en').format('ddd MMM DD YYYY HH:mm:ss [GMT]ZZ');
            },

            toDate : function () {
                return this._offset ? new Date(+this) : this._d;
            },

            toISOString : function () {
                var m = moment(this).utc();
                if (0 < m.year() && m.year() <= 9999) {
                    if ('function' === typeof Date.prototype.toISOString) {
                        // native implementation is ~50x faster, use it when we can
                        return this.toDate().toISOString();
                    } else {
                        return formatMoment(m, 'YYYY-MM-DD[T]HH:mm:ss.SSS[Z]');
                    }
                } else {
                    return formatMoment(m, 'YYYYYY-MM-DD[T]HH:mm:ss.SSS[Z]');
                }
            },

            toArray : function () {
                var m = this;
                return [
                    m.year(),
                    m.month(),
                    m.date(),
                    m.hours(),
                    m.minutes(),
                    m.seconds(),
                    m.milliseconds()
                ];
            },

            isValid : function () {
                return isValid(this);
            },

            isDSTShifted : function () {
                if (this._a) {
                    return this.isValid() && compareArrays(this._a, (this._isUTC ? moment.utc(this._a) : moment(this._a)).toArray()) > 0;
                }

                return false;
            },

            parsingFlags : function () {
                return extend({}, this._pf);
            },

            invalidAt: function () {
                return this._pf.overflow;
            },

            utc : function (keepLocalTime) {
                return this.utcOffset(0, keepLocalTime);
            },

            local : function (keepLocalTime) {
                if (this._isUTC) {
                    this.utcOffset(0, keepLocalTime);
                    this._isUTC = false;

                    if (keepLocalTime) {
                        this.subtract(this._dateUtcOffset(), 'm');
                    }
                }
                return this;
            },

            format : function (inputString) {
                var output = formatMoment(this, inputString || moment.defaultFormat);
                return this.localeData().postformat(output);
            },

            add : createAdder(1, 'add'),

            subtract : createAdder(-1, 'subtract'),

            diff : function (input, units, asFloat) {
                var that = makeAs(input, this),
                    zoneDiff = (that.utcOffset() - this.utcOffset()) * 6e4,
                    anchor, diff, output, daysAdjust;

                units = normalizeUnits(units);

                if (units === 'year' || units === 'month' || units === 'quarter') {
                    output = monthDiff(this, that);
                    if (units === 'quarter') {
                        output = output / 3;
                    } else if (units === 'year') {
                        output = output / 12;
                    }
                } else {
                    diff = this - that;
                    output = units === 'second' ? diff / 1e3 : // 1000
                            units === 'minute' ? diff / 6e4 : // 1000 * 60
                            units === 'hour' ? diff / 36e5 : // 1000 * 60 * 60
                            units === 'day' ? (diff - zoneDiff) / 864e5 : // 1000 * 60 * 60 * 24, negate dst
                            units === 'week' ? (diff - zoneDiff) / 6048e5 : // 1000 * 60 * 60 * 24 * 7, negate dst
                        diff;
                }
                return asFloat ? output : absRound(output);
            },

            from : function (time, withoutSuffix) {
                return moment.duration({to: this, from: time}).locale(this.locale()).humanize(!withoutSuffix);
            },

            fromNow : function (withoutSuffix) {
                return this.from(moment(), withoutSuffix);
            },

            calendar : function (time) {
                // We want to compare the start of today, vs this.
                // Getting start-of-today depends on whether we're locat/utc/offset
                // or not.
                var now = time || moment(),
                    sod = makeAs(now, this).startOf('day'),
                    diff = this.diff(sod, 'days', true),
                    format = diff < -6 ? 'sameElse' :
                            diff < -1 ? 'lastWeek' :
                            diff < 0 ? 'lastDay' :
                            diff < 1 ? 'sameDay' :
                            diff < 2 ? 'nextDay' :
                            diff < 7 ? 'nextWeek' : 'sameElse';
                return this.format(this.localeData().calendar(format, this, moment(now)));
            },

            isLeapYear : function () {
                return isLeapYear(this.year());
            },

            isDST : function () {
                return (this.utcOffset() > this.clone().month(0).utcOffset() ||
                    this.utcOffset() > this.clone().month(5).utcOffset());
            },

            day : function (input) {
                var day = this._isUTC ? this._d.getUTCDay() : this._d.getDay();
                if (input != null) {
                    input = parseWeekday(input, this.localeData());
                    return this.add(input - day, 'd');
                } else {
                    return day;
                }
            },

            month : makeAccessor('Month', true),

            startOf : function (units) {
                units = normalizeUnits(units);
                // the following switch intentionally omits break keywords
                // to utilize falling through the cases.
                switch (units) {
                    case 'year':
                        this.month(0);
                    /* falls through */
                    case 'quarter':
                    case 'month':
                        this.date(1);
                    /* falls through */
                    case 'week':
                    case 'isoWeek':
                    case 'day':
                        this.hours(0);
                    /* falls through */
                    case 'hour':
                        this.minutes(0);
                    /* falls through */
                    case 'minute':
                        this.seconds(0);
                    /* falls through */
                    case 'second':
                        this.milliseconds(0);
                    /* falls through */
                }

                // weeks are a special case
                if (units === 'week') {
                    this.weekday(0);
                } else if (units === 'isoWeek') {
                    this.isoWeekday(1);
                }

                // quarters are also special
                if (units === 'quarter') {
                    this.month(Math.floor(this.month() / 3) * 3);
                }

                return this;
            },

            endOf: function (units) {
                units = normalizeUnits(units);
                if (units === undefined || units === 'millisecond') {
                    return this;
                }
                return this.startOf(units).add(1, (units === 'isoWeek' ? 'week' : units)).subtract(1, 'ms');
            },

            isAfter: function (input, units) {
                var inputMs;
                units = normalizeUnits(typeof units !== 'undefined' ? units : 'millisecond');
                if (units === 'millisecond') {
                    input = moment.isMoment(input) ? input : moment(input);
                    return +this > +input;
                } else {
                    inputMs = moment.isMoment(input) ? +input : +moment(input);
                    return inputMs < +this.clone().startOf(units);
                }
            },

            isBefore: function (input, units) {
                var inputMs;
                units = normalizeUnits(typeof units !== 'undefined' ? units : 'millisecond');
                if (units === 'millisecond') {
                    input = moment.isMoment(input) ? input : moment(input);
                    return +this < +input;
                } else {
                    inputMs = moment.isMoment(input) ? +input : +moment(input);
                    return +this.clone().endOf(units) < inputMs;
                }
            },

            isBetween: function (from, to, units) {
                return this.isAfter(from, units) && this.isBefore(to, units);
            },

            isSame: function (input, units) {
                var inputMs;
                units = normalizeUnits(units || 'millisecond');
                if (units === 'millisecond') {
                    input = moment.isMoment(input) ? input : moment(input);
                    return +this === +input;
                } else {
                    inputMs = +moment(input);
                    return +(this.clone().startOf(units)) <= inputMs && inputMs <= +(this.clone().endOf(units));
                }
            },

            min: deprecate(
                'moment().min is deprecated, use moment.min instead. https://github.com/moment/moment/issues/1548',
                function (other) {
                    other = moment.apply(null, arguments);
                    return other < this ? this : other;
                }
            ),

            max: deprecate(
                'moment().max is deprecated, use moment.max instead. https://github.com/moment/moment/issues/1548',
                function (other) {
                    other = moment.apply(null, arguments);
                    return other > this ? this : other;
                }
            ),

            zone : deprecate(
                    'moment().zone is deprecated, use moment().utcOffset instead. ' +
                    'https://github.com/moment/moment/issues/1779',
                function (input, keepLocalTime) {
                    if (input != null) {
                        if (typeof input !== 'string') {
                            input = -input;
                        }

                        this.utcOffset(input, keepLocalTime);

                        return this;
                    } else {
                        return -this.utcOffset();
                    }
                }
            ),

            // keepLocalTime = true means only change the timezone, without
            // affecting the local hour. So 5:31:26 +0300 --[utcOffset(2, true)]-->
            // 5:31:26 +0200 It is possible that 5:31:26 doesn't exist with offset
            // +0200, so we adjust the time as needed, to be valid.
            //
            // Keeping the time actually adds/subtracts (one hour)
            // from the actual represented time. That is why we call updateOffset
            // a second time. In case it wants us to change the offset again
            // _changeInProgress == true case, then we have to adjust, because
            // there is no such time in the given timezone.
            utcOffset : function (input, keepLocalTime) {
                var offset = this._offset || 0,
                    localAdjust;
                if (input != null) {
                    if (typeof input === 'string') {
                        input = utcOffsetFromString(input);
                    }
                    if (Math.abs(input) < 16) {
                        input = input * 60;
                    }
                    if (!this._isUTC && keepLocalTime) {
                        localAdjust = this._dateUtcOffset();
                    }
                    this._offset = input;
                    this._isUTC = true;
                    if (localAdjust != null) {
                        this.add(localAdjust, 'm');
                    }
                    if (offset !== input) {
                        if (!keepLocalTime || this._changeInProgress) {
                            addOrSubtractDurationFromMoment(this,
                                moment.duration(input - offset, 'm'), 1, false);
                        } else if (!this._changeInProgress) {
                            this._changeInProgress = true;
                            moment.updateOffset(this, true);
                            this._changeInProgress = null;
                        }
                    }

                    return this;
                } else {
                    return this._isUTC ? offset : this._dateUtcOffset();
                }
            },

            isLocal : function () {
                return !this._isUTC;
            },

            isUtcOffset : function () {
                return this._isUTC;
            },

            isUtc : function () {
                return this._isUTC && this._offset === 0;
            },

            zoneAbbr : function () {
                return this._isUTC ? 'UTC' : '';
            },

            zoneName : function () {
                return this._isUTC ? 'Coordinated Universal Time' : '';
            },

            parseZone : function () {
                if (this._tzm) {
                    this.utcOffset(this._tzm);
                } else if (typeof this._i === 'string') {
                    this.utcOffset(utcOffsetFromString(this._i));
                }
                return this;
            },

            hasAlignedHourOffset : function (input) {
                if (!input) {
                    input = 0;
                }
                else {
                    input = moment(input).utcOffset();
                }

                return (this.utcOffset() - input) % 60 === 0;
            },

            daysInMonth : function () {
                return daysInMonth(this.year(), this.month());
            },

            dayOfYear : function (input) {
                var dayOfYear = round((moment(this).startOf('day') - moment(this).startOf('year')) / 864e5) + 1;
                return input == null ? dayOfYear : this.add((input - dayOfYear), 'd');
            },

            quarter : function (input) {
                return input == null ? Math.ceil((this.month() + 1) / 3) : this.month((input - 1) * 3 + this.month() % 3);
            },

            weekYear : function (input) {
                var year = weekOfYear(this, this.localeData()._week.dow, this.localeData()._week.doy).year;
                return input == null ? year : this.add((input - year), 'y');
            },

            isoWeekYear : function (input) {
                var year = weekOfYear(this, 1, 4).year;
                return input == null ? year : this.add((input - year), 'y');
            },

            week : function (input) {
                var week = this.localeData().week(this);
                return input == null ? week : this.add((input - week) * 7, 'd');
            },

            isoWeek : function (input) {
                var week = weekOfYear(this, 1, 4).week;
                return input == null ? week : this.add((input - week) * 7, 'd');
            },

            weekday : function (input) {
                var weekday = (this.day() + 7 - this.localeData()._week.dow) % 7;
                return input == null ? weekday : this.add(input - weekday, 'd');
            },

            isoWeekday : function (input) {
                // behaves the same as moment#day except
                // as a getter, returns 7 instead of 0 (1-7 range instead of 0-6)
                // as a setter, sunday should belong to the previous week.
                return input == null ? this.day() || 7 : this.day(this.day() % 7 ? input : input - 7);
            },

            isoWeeksInYear : function () {
                return weeksInYear(this.year(), 1, 4);
            },

            weeksInYear : function () {
                var weekInfo = this.localeData()._week;
                return weeksInYear(this.year(), weekInfo.dow, weekInfo.doy);
            },

            get : function (units) {
                units = normalizeUnits(units);
                return this[units]();
            },

            set : function (units, value) {
                var unit;
                if (typeof units === 'object') {
                    for (unit in units) {
                        this.set(unit, units[unit]);
                    }
                }
                else {
                    units = normalizeUnits(units);
                    if (typeof this[units] === 'function') {
                        this[units](value);
                    }
                }
                return this;
            },

            // If passed a locale key, it will set the locale for this
            // instance.  Otherwise, it will return the locale configuration
            // variables for this instance.
            locale : function (key) {
                var newLocaleData;

                if (key === undefined) {
                    return this._locale._abbr;
                } else {
                    newLocaleData = moment.localeData(key);
                    if (newLocaleData != null) {
                        this._locale = newLocaleData;
                    }
                    return this;
                }
            },

            lang : deprecate(
                'moment().lang() is deprecated. Instead, use moment().localeData() to get the language configuration. Use moment().locale() to change languages.',
                function (key) {
                    if (key === undefined) {
                        return this.localeData();
                    } else {
                        return this.locale(key);
                    }
                }
            ),

            localeData : function () {
                return this._locale;
            },

            _dateUtcOffset : function () {
                // On Firefox.24 Date#getTimezoneOffset returns a floating point.
                // https://github.com/moment/moment/pull/1871
                return -Math.round(this._d.getTimezoneOffset() / 15) * 15;
            }

        });

        function rawMonthSetter(mom, value) {
            var dayOfMonth;

            // TODO: Move this out of here!
            if (typeof value === 'string') {
                value = mom.localeData().monthsParse(value);
                // TODO: Another silent failure?
                if (typeof value !== 'number') {
                    return mom;
                }
            }

            dayOfMonth = Math.min(mom.date(),
                daysInMonth(mom.year(), value));
            mom._d['set' + (mom._isUTC ? 'UTC' : '') + 'Month'](value, dayOfMonth);
            return mom;
        }

        function rawGetter(mom, unit) {
            return mom._d['get' + (mom._isUTC ? 'UTC' : '') + unit]();
        }

        function rawSetter(mom, unit, value) {
            if (unit === 'Month') {
                return rawMonthSetter(mom, value);
            } else {
                return mom._d['set' + (mom._isUTC ? 'UTC' : '') + unit](value);
            }
        }

        function makeAccessor(unit, keepTime) {
            return function (value) {
                if (value != null) {
                    rawSetter(this, unit, value);
                    moment.updateOffset(this, keepTime);
                    return this;
                } else {
                    return rawGetter(this, unit);
                }
            };
        }

        moment.fn.millisecond = moment.fn.milliseconds = makeAccessor('Milliseconds', false);
        moment.fn.second = moment.fn.seconds = makeAccessor('Seconds', false);
        moment.fn.minute = moment.fn.minutes = makeAccessor('Minutes', false);
        // Setting the hour should keep the time, because the user explicitly
        // specified which hour he wants. So trying to maintain the same hour (in
        // a new timezone) makes sense. Adding/subtracting hours does not follow
        // this rule.
        moment.fn.hour = moment.fn.hours = makeAccessor('Hours', true);
        // moment.fn.month is defined separately
        moment.fn.date = makeAccessor('Date', true);
        moment.fn.dates = deprecate('dates accessor is deprecated. Use date instead.', makeAccessor('Date', true));
        moment.fn.year = makeAccessor('FullYear', true);
        moment.fn.years = deprecate('years accessor is deprecated. Use year instead.', makeAccessor('FullYear', true));

        // add plural methods
        moment.fn.days = moment.fn.day;
        moment.fn.months = moment.fn.month;
        moment.fn.weeks = moment.fn.week;
        moment.fn.isoWeeks = moment.fn.isoWeek;
        moment.fn.quarters = moment.fn.quarter;

        // add aliased format methods
        moment.fn.toJSON = moment.fn.toISOString;

        // alias isUtc for dev-friendliness
        moment.fn.isUTC = moment.fn.isUtc;

        /************************************
         Duration Prototype
         ************************************/


        function daysToYears (days) {
            // 400 years have 146097 days (taking into account leap year rules)
            return days * 400 / 146097;
        }

        function yearsToDays (years) {
            // years * 365 + absRound(years / 4) -
            //     absRound(years / 100) + absRound(years / 400);
            return years * 146097 / 400;
        }

        extend(moment.duration.fn = Duration.prototype, {

            _bubble : function () {
                var milliseconds = this._milliseconds,
                    days = this._days,
                    months = this._months,
                    data = this._data,
                    seconds, minutes, hours, years = 0;

                // The following code bubbles up values, see the tests for
                // examples of what that means.
                data.milliseconds = milliseconds % 1000;

                seconds = absRound(milliseconds / 1000);
                data.seconds = seconds % 60;

                minutes = absRound(seconds / 60);
                data.minutes = minutes % 60;

                hours = absRound(minutes / 60);
                data.hours = hours % 24;

                days += absRound(hours / 24);

                // Accurately convert days to years, assume start from year 0.
                years = absRound(daysToYears(days));
                days -= absRound(yearsToDays(years));

                // 30 days to a month
                // TODO (iskren): Use anchor date (like 1st Jan) to compute this.
                months += absRound(days / 30);
                days %= 30;

                // 12 months -> 1 year
                years += absRound(months / 12);
                months %= 12;

                data.days = days;
                data.months = months;
                data.years = years;
            },

            abs : function () {
                this._milliseconds = Math.abs(this._milliseconds);
                this._days = Math.abs(this._days);
                this._months = Math.abs(this._months);

                this._data.milliseconds = Math.abs(this._data.milliseconds);
                this._data.seconds = Math.abs(this._data.seconds);
                this._data.minutes = Math.abs(this._data.minutes);
                this._data.hours = Math.abs(this._data.hours);
                this._data.months = Math.abs(this._data.months);
                this._data.years = Math.abs(this._data.years);

                return this;
            },

            weeks : function () {
                return absRound(this.days() / 7);
            },

            valueOf : function () {
                return this._milliseconds +
                    this._days * 864e5 +
                    (this._months % 12) * 2592e6 +
                    toInt(this._months / 12) * 31536e6;
            },

            humanize : function (withSuffix) {
                var output = relativeTime(this, !withSuffix, this.localeData());

                if (withSuffix) {
                    output = this.localeData().pastFuture(+this, output);
                }

                return this.localeData().postformat(output);
            },

            add : function (input, val) {
                // supports only 2.0-style add(1, 's') or add(moment)
                var dur = moment.duration(input, val);

                this._milliseconds += dur._milliseconds;
                this._days += dur._days;
                this._months += dur._months;

                this._bubble();

                return this;
            },

            subtract : function (input, val) {
                var dur = moment.duration(input, val);

                this._milliseconds -= dur._milliseconds;
                this._days -= dur._days;
                this._months -= dur._months;

                this._bubble();

                return this;
            },

            get : function (units) {
                units = normalizeUnits(units);
                return this[units.toLowerCase() + 's']();
            },

            as : function (units) {
                var days, months;
                units = normalizeUnits(units);

                if (units === 'month' || units === 'year') {
                    days = this._days + this._milliseconds / 864e5;
                    months = this._months + daysToYears(days) * 12;
                    return units === 'month' ? months : months / 12;
                } else {
                    // handle milliseconds separately because of floating point math errors (issue #1867)
                    days = this._days + Math.round(yearsToDays(this._months / 12));
                    switch (units) {
                        case 'week': return days / 7 + this._milliseconds / 6048e5;
                        case 'day': return days + this._milliseconds / 864e5;
                        case 'hour': return days * 24 + this._milliseconds / 36e5;
                        case 'minute': return days * 24 * 60 + this._milliseconds / 6e4;
                        case 'second': return days * 24 * 60 * 60 + this._milliseconds / 1000;
                        // Math.floor prevents floating point math errors here
                        case 'millisecond': return Math.floor(days * 24 * 60 * 60 * 1000) + this._milliseconds;
                        default: throw new Error('Unknown unit ' + units);
                    }
                }
            },

            lang : moment.fn.lang,
            locale : moment.fn.locale,

            toIsoString : deprecate(
                    'toIsoString() is deprecated. Please use toISOString() instead ' +
                    '(notice the capitals)',
                function () {
                    return this.toISOString();
                }
            ),

            toISOString : function () {
                // inspired by https://github.com/dordille/moment-isoduration/blob/master/moment.isoduration.js
                var years = Math.abs(this.years()),
                    months = Math.abs(this.months()),
                    days = Math.abs(this.days()),
                    hours = Math.abs(this.hours()),
                    minutes = Math.abs(this.minutes()),
                    seconds = Math.abs(this.seconds() + this.milliseconds() / 1000);

                if (!this.asSeconds()) {
                    // this is the same as C#'s (Noda) and python (isodate)...
                    // but not other JS (goog.date)
                    return 'P0D';
                }

                return (this.asSeconds() < 0 ? '-' : '') +
                    'P' +
                    (years ? years + 'Y' : '') +
                    (months ? months + 'M' : '') +
                    (days ? days + 'D' : '') +
                    ((hours || minutes || seconds) ? 'T' : '') +
                    (hours ? hours + 'H' : '') +
                    (minutes ? minutes + 'M' : '') +
                    (seconds ? seconds + 'S' : '');
            },

            localeData : function () {
                return this._locale;
            },

            toJSON : function () {
                return this.toISOString();
            }
        });

        moment.duration.fn.toString = moment.duration.fn.toISOString;

        function makeDurationGetter(name) {
            moment.duration.fn[name] = function () {
                return this._data[name];
            };
        }

        for (i in unitMillisecondFactors) {
            if (hasOwnProp(unitMillisecondFactors, i)) {
                makeDurationGetter(i.toLowerCase());
            }
        }

        moment.duration.fn.asMilliseconds = function () {
            return this.as('ms');
        };
        moment.duration.fn.asSeconds = function () {
            return this.as('s');
        };
        moment.duration.fn.asMinutes = function () {
            return this.as('m');
        };
        moment.duration.fn.asHours = function () {
            return this.as('h');
        };
        moment.duration.fn.asDays = function () {
            return this.as('d');
        };
        moment.duration.fn.asWeeks = function () {
            return this.as('weeks');
        };
        moment.duration.fn.asMonths = function () {
            return this.as('M');
        };
        moment.duration.fn.asYears = function () {
            return this.as('y');
        };

        /************************************
         Default Locale
         ************************************/


            // Set default locale, other locale will inherit from English.
        moment.locale('en', {
            ordinalParse: /\d{1,2}(th|st|nd|rd)/,
            ordinal : function (number) {
                var b = number % 10,
                    output = (toInt(number % 100 / 10) === 1) ? 'th' :
                        (b === 1) ? 'st' :
                            (b === 2) ? 'nd' :
                                (b === 3) ? 'rd' : 'th';
                return number + output;
            }
        });

        /* EMBED_LOCALES */

        /************************************
         Exposing Moment
         ************************************/

        function makeGlobal(shouldDeprecate) {
            /*global ender:false */
            if (typeof ender !== 'undefined') {
                return;
            }
            oldGlobalMoment = globalScope.moment;
            if (shouldDeprecate) {
                globalScope.moment = deprecate(
                        'Accessing Moment through the global scope is ' +
                        'deprecated, and will be removed in an upcoming ' +
                        'release.',
                    moment);
            } else {
                globalScope.moment = moment;
            }
        }

        // CommonJS module is defined
        if (hasModule) {
            module.exports = moment;
        } else if (typeof define === 'function' && define.amd) {
            define('moment/moment',['require','exports','module'],function (require, exports, module) {
                if (module.config && module.config() && module.config().noGlobal === true) {
                    // release the global variable
                    globalScope.moment = oldGlobalMoment;
                }

                return moment;
            });
            makeGlobal(true);
        } else {
            makeGlobal();
        }
    }).call(this);

    define('moment', ['moment/moment'], function (main) { return main; });

    /**
     * ESUI (Enterprise Simple UI library)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 日期相关基础库
     * @author otakustay
     */
    define(
        'esui/lib/date',['require','moment'],function (require) {
            var moment = require('moment');

            /**
             * @class lib.date
             * @singleton
             */
            var date = {};

            /**
             * 默认日期格式，可通过向该属性追回格式来调整{@link lib.date#parse}支持的格式
             *
             * 默认格式有以下几个：
             *
             * - `YYYYMMDDHHmmss`
             * - `YYYY-MM-DD HH:mm:ss`
             * - `YYYY/MM/DD HH:mm:ss`
             * - `YYYY-MM-DDTHH:mm:ss.SSSZ`
             *
             * @cfg
             * @type {string[]}
             * @deprecated 将在4.0版本中移除，应尽量确定格式并使用`moment#parse`代替
             */
            date.dateFormats = [
                'YYYYMMDDHHmmss',
                'YYYY-MM-DD HH:mm:ss',
                'YYYY/MM/DD HH:mm:ss',
                'YYYY-MM-DDTHH:mm:ss.SSSZ' // ISO字符串
            ];

            /**
             * 对目标日期对象进行格式化
             *
             * 具体支持的格式参考
             * [moment文档](http://momentjs.com/docs/#/displaying/format/)
             *
             * @param {Date} source 目标日期对象
             * @param {string} pattern 日期格式化规则
             * @return {string} 格式化后的字符串
             * @deprecated 将在4.0版本中移除，请使用`moment#format`代替
             */
            date.format = function (source, pattern) {
                return moment(source).format(pattern);
            };

            /**
             * 将目标字符串转换成日期对象
             *
             * 具体支持的格式参考
             * [moment文档](http://momentjs.com/docs/#/displaying/format/)
             *
             * 默认使用{@link lib.date#dateFormats}作为解析格式
             *
             * @param {string} source 目标字符串
             * @param {string} [format] 指定解析格式，
             * 不提供此参数则使用{@link lib.date#dateFormats}作为解析格式，
             * 由于默认包含多个格式，这将导致性能有所下降，因此尽量提供明确的格式参数
             * @return {Date} 转换后的日期对象
             * @deprecated 将在4.0版本中移除，请使用`moment#parse`代替
             */
            date.parse = function (source, format) {
                var dateTime = moment(source, format || date.dateFormats);
                return dateTime.toDate();
            };

            return { date: date };
        }
    );

    /**
     * ESUI (Enterprise Simple UI library)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 页面相关基础库
     * @author otakustay
     */
    define(
        'esui/lib/page',['require'],function (require) {
            var documentElement = document.documentElement;
            var body = document.body;
            var viewRoot = document.compatMode === 'BackCompat'
                ? body
                : documentElement;

            /**
             * @class lib.page
             * @singleton
             */
            var page = {};

            /**
             * 获取页面宽度
             *
             * @return {number} 页面宽度
             */
            page.getWidth = function () {
                return Math.max(
                    (documentElement ? documentElement.scrollWidth : 0),
                    (body ? body.scrollWidth : 0),
                    (viewRoot ? viewRoot.clientWidth : 0),
                    0
                );
            };

            /**
             * 获取页面高度
             *
             * @return {number} 页面高度
             */
            page.getHeight = function () {
                return Math.max(
                    (documentElement ? documentElement.scrollHeight : 0),
                    (body ? body.scrollHeight : 0),
                    (viewRoot ? viewRoot.clientHeight : 0),
                    0
                );
            };


            /**
             * 获取页面视觉区域宽度
             *
             * @return {number} 页面视觉区域宽度
             */
            page.getViewWidth = function () {
                return viewRoot ? viewRoot.clientWidth : 0;
            };

            /**
             * 获取页面视觉区域高度
             *
             * @return {number} 页面视觉区域高度
             */
            page.getViewHeight = function () {
                return viewRoot ? viewRoot.clientHeight : 0;
            };

            /**
             * 获取纵向滚动量
             *
             * @return {number} 纵向滚动量
             */
            page.getScrollTop = function () {
                return window.pageYOffset
                    || document.documentElement.scrollTop
                    || document.body.scrollTop
                    || 0;
            };

            /**
             * 获取横向滚动量
             *
             * @return {number} 横向滚动量
             */
            page.getScrollLeft = function () {
                return window.pageXOffset
                    || document.documentElement.scrollLeft
                    || document.body.scrollLeft
                    || 0;
            };

            /**
             * 获取页面纵向坐标
             *
             * @return {number}
             */
            page.getClientTop = function () {
                return document.documentElement.clientTop
                    || document.body.clientTop
                    || 0;
            };

            /**
             * 获取页面横向坐标
             *
             * @return {number}
             */
            page.getClientLeft = function () {
                return document.documentElement.clientLeft
                    || document.body.clientLeft
                    || 0;
            };

            return { page: page };
        }
    );

    /**
     * ESUI (Enterprise Simple UI library)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file DOM事件相关基础库
     * @author otakustay
     */
    define(
        'esui/lib/event',['require','./dom','./page'],function (require) {
            var dom = require('./dom');
            var page = require('./page').page;

            /**
             * @class lib.event
             * @singleton
             */
            var event = {};


            /**
             * 阻止事件默认行为
             *
             * @param {Event | undefined} event 事件对象
             */
            event.preventDefault = function (event) {
                event = event || window.event;

                if (event.preventDefault) {
                    event.preventDefault();
                }
                else {
                    event.returnValue = false;
                }
            };

            /**
             * 阻止事件冒泡
             *
             * @param {Event | undefined} event 事件对象
             */
            event.stopPropagation = function (event) {
                event = event || window.event;

                if (event.stopPropagation) {
                    event.stopPropagation();
                }
                else {
                    event.cancelBubble = true;
                }
            };

            /**
             * 获取鼠标位置
             *
             * @param {Event | undefined} event 事件对象
             * @return {Event} 经过修正的事件对象
             */
            event.getMousePosition = function (event) {
                event = event || window.event;

                if (typeof event.pageX !== 'number') {
                    event.pageX =
                        event.clientX + page.getScrollLeft() - page.getClientLeft();
                }

                if (typeof event.pageY !== 'number') {
                    event.pageY =
                        event.clientY + page.getScrollTop() - page.getClientTop();
                }

                return event;
            };

            /**
             * 获取事件目标对象
             *
             * @param {Event | undefined} event 事件对象
             * @return {HTMLElement} 事件目标对象
             */
            event.getTarget = function (event) {
                event = event || window.event;

                return event.target || event.srcElement;
            };


            /**
             * @override lib
             */
            return {
                /**
                 * 为DOM元素添加事件
                 *
                 * 本方法 *不处理* DOM事件的兼容性，包括执行顺序、`Event`对象属性的修正等
                 *
                 * @param {HTMLElement | string} element DOM元素或其id
                 * @param {string} type 事件类型， *不能* 带有`on`前缀
                 * @param {Function} listener 事件处理函数
                 */
                on: function (element, type, listener) {
                    element = dom.g(element);

                    if (element.addEventListener) {
                        element.addEventListener(type, listener, false);
                    }
                    else if (element.attachEvent) {
                        element.attachEvent('on' + type, listener);
                    }
                },

                /**
                 * 为DOM元素移除事件
                 *
                 * 本方法 *不处理* DOM事件的兼容性，包括执行顺序、`Event`对象属性的修正等
                 *
                 * @param {HTMLElement | string} element DOM元素或其id
                 * @param {string} type 事件类型， *不能* 带有`on`前缀
                 * @param {Function} listener 事件处理函数
                 */
                un: function (element, type, listener) {
                    element = dom.g(element);

                    if (element.addEventListener) {
                        element.removeEventListener(type, listener, false);
                    }
                    else if (element.attachEvent) {
                        element.detachEvent('on' + type, listener);
                    }
                },

                event: event
            };
        }
    );

    /**
     * ESUI (Enterprise Simple UI library)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 语言基础库
     * @author otakustay
     */
    define(
        'esui/lib/lang',['require','underscore'],function (require) {
            var u = require('underscore');

            /**
             * @override lib
             */
            var lib = {};

            var counter = 0x861005;
            /**
             * 获取唯一id
             *
             * @param {string} [prefix="esui"] 前缀
             * @return {string}
             */
            lib.getGUID = function (prefix) {
                prefix = prefix || 'esui';
                return prefix + counter++;
            };

            /**
             * 为类型构造器建立继承关系
             *
             * @param {Function} subClass 子类构造器
             * @param {Function} superClass 父类构造器
             * @return {Function} 返回`subClass`构造器
             */
            lib.inherits = function (subClass, superClass) {
                var Empty = function () {};
                Empty.prototype = superClass.prototype;
                var selfPrototype = subClass.prototype;
                var proto = subClass.prototype = new Empty();

                for (var key in selfPrototype) {
                    proto[key] = selfPrototype[key];
                }
                subClass.prototype.constructor = subClass;
                subClass.superClass = superClass.prototype;

                return subClass;
            };

            /**
             * 对一个对象进行深度复制
             *
             * @param {Object} source 需要进行复制的对象
             * @return {Object} 复制出来的新对象
             * @deprecated 将在4.0版本中移除，使用{@link lib#deepClone}方法代替
             */
            lib.clone = function (source) {
                if (!source || typeof source !== 'object') {
                    return source;
                }

                var result = source;
                if (u.isArray(source)) {
                    result = u.clone(source);
                }
                else if (({}).toString.call(source) === '[object Object]'
                    // IE下，DOM和BOM对象上一个语句为true，
                    // isPrototypeOf挂在`Object.prototype`上的，
                    // 因此所有的字面量都应该会有这个属性
                    // 对于在`window`上挂了`isPrototypeOf`属性的情况，直接忽略不考虑
                    && ('isPrototypeOf' in source)
                    ) {
                    result = {};
                    for (var key in source) {
                        if (source.hasOwnProperty(key)) {
                            result[key] = lib.deepClone(source[key]);
                        }
                    }
                }

                return result;
            };

            /**
             * 对一个对象进行深度复制
             *
             * @param {Object} source 需要进行复制的对象
             * @return {Object} 复制出来的新对象
             */
            lib.deepClone = lib.clone;

            /**
             * 将数组转换为字典
             *
             * @param {Array} array 数组
             * @return {Object} 以`array`中的每个对象为键，以`true`为值的字典对象
             */
            lib.toDictionary = function (array) {
                var dictionary = {};
                u.each(
                    array,
                    function (value) {
                        dictionary[value] = true;
                    }
                );

                return dictionary;
            };

            /**
             * 判断一个对象是否为数组
             *
             * @param {Mixed} source 需要判断的对象
             * @return {boolean}
             * @deprecated 将在4.0版本中移除，使用`underscore.isArray`代替
             */
            lib.isArray = u.isArray;

            /**
             * 将对象转为数组
             *
             * @param {Mixed} source 需要转换的对象
             * @return {Array}
             * @deprecated 将在4.0版本中移除，使用`underscore.toArray`代替
             */
            lib.toArray = u.toArray;

            /**
             * 扩展对象
             *
             * @param {Object} source 需要判断的对象
             * @param {Object...} extensions 用于扩展`source`的各个对象
             * @return {Object} 完成扩展的`source`对象
             * @deprecated 将在4.0版本中移除，使用`underscore.extend`代替
             */
            lib.extend = u.extend;

            /**
             * 固定函数的`this`对象及参数
             *
             * @param {Function} fn 需要处理的函数
             * @param {Object} thisObject 执行`fn`时的`this`对象
             * @param {Mixed...} args 执行`fn`时追回在前面的参数
             * @return {Function}
             * @deprecated 将在4.0版本中移除，使用`underscore.bind`代替
             */
            lib.bind = u.bind;

            /**
             * 为函数添加参数
             *
             * 该函数类似于{@link lib#bind}，但不固定`this`对象
             *
             * @param {Function} fn 需要处理的函数
             * @param {Mixed...} args 执行`fn`时追回在前面的参数
             * @return {Function}
             * @deprecated 将在4.0版本中移除，使用`underscore.partial`代替
             */
            lib.curry = u.partial;

            /**
             * 在数组或类数组对象中查找指定对象的索引
             *
             * @param {Array | Object} array 用于查找的数组或类数组对象
             * @param {Mixed} value 需要查找的对象
             * @param {number} [fromIndex] 开始查找的索引
             * @return {number}
             * @deprecated 将在4.0版本中移除，使用`underscore.indexOf`代替
             */
            lib.indexOf = u.indexOf;

            /**
             * 对字符串进行HTML解码
             *
             * @param {string} source 需要解码的字符串
             * @return {string}
             * @deprecated 将在4.0版本中移除，使用`underscore.unescape`代替
             */
            lib.decodeHTML = u.unescape;

            /**
             * 对字符串进行HTML编码
             *
             * @param {string} source 需要编码的字符串
             * @return {string}
             * @deprecated 将在4.0版本中移除，使用`underscore.escape`代替
             */
            lib.encodeHTML = u.escape;

            return lib;
        }
    );

    /**
     * ESUI (Enterprise Simple UI library)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file UI基础库适配层
     * @author otakustay, firede(firede@firede.us), erik
     */
    define(
        'esui/lib',['require','underscore','./lib/attribute','./lib/class','./lib/date','./lib/dom','./lib/event','./lib/lang','./lib/page','./lib/string'],function (require) {
            /**
             * 工具对象
             *
             * @class
             * @singleton
             */
            var lib = {};
            var u = require('underscore');

            if (/msie (\d+\.\d+)/i.test(navigator.userAgent)) {
                /**
                 * IE浏览器版本号
                 *
                 * @type {number}
                 * @deprecated 不要使用浏览器版本号检测特性
                 */
                lib.ie = document.documentMode || +RegExp.$1;
            }

            u.extend(
                lib,
                require('./lib/attribute'),
                require('./lib/class'),
                require('./lib/date'),
                require('./lib/dom'),
                require('./lib/event'),
                require('./lib/lang'),
                require('./lib/page'),
                require('./lib/string')
            );

            return lib;
        }
    );
    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 控件集合，类似`jQuery`对象的功能
     * @author otakustay
     */
    define(
        'esui/ControlCollection',['require','underscore'],function (require) {
            var u = require('underscore');

            /**
             * 控件集合，类似`jQuery`对象，提供便携的方法来访问和修改一个或多个控件
             *
             * `ControlCollection`提供{@link Control}的所有 **公有** 方法，
             * 但 *没有* 任何 **保护或私有** 方法
             *
             * 对于方法，`ControlCollection`采用 **Write all, Read first** 的策略，
             * 需要注意的是，类似{@link Control#setProperties}的方法虽然有返回值，
             * 但被归类于写操作，因此会对所有内部的控件生效，但只返回第一个控件执行的结果
             *
             * `ControlCollection`仅继承{@link Control}的方法，并不包含任何子类独有方法，
             * 因此无法认为集合是一个{@link InputControl}而执行如下代码：
             *
             *     collection.setValue('foo');
             *
             * 此时可以使用通用的{@link Control#set}方法来代替：
             *
             *     collection.set('value', 'foo');
             *
             * 根据{@link Control#set}方法的规则，如果控件存在`setValue`方法，则会进行调用
             *
             * @constructor
             */
            function ControlCollection() {
                /**
                 * @property {number} length
                 *
                 * 当前控件分组中控件的数量
                 *
                 * @readonly
                 */
                this.length = 0;
            }

            // 为了让Firebug认为这是个数组
            ControlCollection.prototype.splice = Array.prototype.splice;

            /**
             * 向集合中添加控件
             *
             * @param {Control} control 添加的控件
             */
            ControlCollection.prototype.add = function (control) {
                var index = u.indexOf(this, control);
                if (index < 0) {
                    [].push.call(this, control);
                }
            };

            /**
             * 从集合中移除控件
             *
             * @param {Control} control 需要移除的控件
             */
            ControlCollection.prototype.remove = function (control) {
                for (var i = 0; i < this.length; i++) {
                    if (this[i] === control) {
                        //  ie8 splice下有问题，只会改变length,并设置元素索引，但不会删除元素
                        //  var t = {0:'a', 1: 'b', 2:'c', 3:'d', length: 4};
                        //  [].splice.call(t, 3, 1);
                        //  alert(t.length)
                        //  for(var k in t) {
                        //     alert(k+ ':' + t[k])
                        //  }

                        [].splice.call(this, i, 1);
                        return;
                    }
                }
            };

            /**
             * 对分组内每个控件调用指定函数
             *
             * @param {Function} iterator 每次循环调用的函数，
             * 函数接受 **当前的控件** 、 **索引** 及 **当前控件集合实例** 为参数
             * @param {Mixed} thisObject 执行`iterator`时的`this`对象，
             * 如果不指定此参数，则`iterator`内的`this`对象为控件实例
             */
            ControlCollection.prototype.each = function (iterator, thisObject) {
                u.each(
                    this,
                    function (control, i) {
                        iterator.call(thisObject || control, control, i, this);
                    }
                );
            };

            /**
             * 对分组内的每个控件调用给定名称的方法
             *
             * 调用此方法必须保证此集合中的 **所有** 控件均有`methodName`指定的方法，
             * 否则将会出现`TypeError("has no method 'methodName'")`异常
             *
             * @param {string} methodName 需要调用的函数的名称
             * @param {Mixed...} args 调用方法时指定的参数
             * @return {Mixed[]} 返回一个数组，依次包含每个控件调用方法的结果
             */
            ControlCollection.prototype.invoke = function (methodName) {
                var args = [this];
                args.push.apply(args, arguments);
                return u.invoke.apply(u, args);
            };

            // 写方法
            u.each(
                [
                    'enable', 'disable', 'setDisabled',
                    'show', 'hide', 'toggle',
                    'addChild', 'removeChild',
                    'set', 'setProperties',
                    'addState', 'removeState', 'toggleState',
                    'on', 'off', 'fire',
                    'dispose', 'destroy',
                    'setViewContext',
                    'render', 'repaint', 'appendTo', 'insertBefore'
                ],
                function (method) {
                    ControlCollection.prototype[method] = function () {
                        var args = [method];
                        args.push.apply(args, arguments);
                        var result = this.invoke.apply(this, args);
                        return result && result[0];
                    };
                }
            );

            // 读方法
            u.each(
                [
                    'isDisabled', 'isHidden', 'hasState',
                    'get', 'getCategory', 'getChild', 'getChildSafely'
                ],
                function (method) {
                    ControlCollection.prototype[method] = function () {
                        var first = this[0];
                        return first
                            ? first[method].apply(first, arguments)
                            : undefined;
                    };
                }
            );

            return ControlCollection;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 控件包装类，用于模拟一个不存在的控件
     * @author otakustay
     */
    define(
        'esui/SafeWrapper',['require','underscore'],function (require) {
            var u = require('underscore');

            /**
             * 控件安全包装，模拟一个无任何功能的控件
             *
             * **由于技术限制，此类不继承{@link Control}，不能用`instanceof`判断类型**
             *
             * 在实际使用中，经常会有这样的代码：
             *
             *     var panel = ui.get('panel');
             *     if (panel) {
         *         panel.set('content', someHTML);
         *     }
             *
             * 为了消除这些分支，可以使用本类。本类提供控件所有的基础方法：
             *
             * - 禁用 / 启用：`enable` | `disable` | `setDisabled` | `isDisabled`
             * - 显示 / 隐藏：`show` | `hide` | `toggle` | `isHidden`
             * - 分类：`getCategory`
             * - 取值：`getValue` | getRawValue | `setValue` | `setRawValue`
             * - 子控件：`getChild` | `getChildSafely` | `addChild` | `removeChild`
             * - 设置值：`set` | `get` | `setProperties`
             * - 状态：`addState` | `removeState` | `toggleState` | `hasState`
             * - 事件：`on` | `off` | `fire`
             * - 销毁：`dispose` | `destroy`
             * - 生命周期：`initOptions` | `createMain` | `initStructure`
             * - 视图管理：`setViewContext`
             * - 渲染：`appendTo` | `insertBefore` | `render` | `repaint`
             * - 内部辅助：`isPropertyChanged`
             * - 已废弃：`initChildren` | `disposeChildren`
             *
             * 所有设置、改变值的方法均为空逻辑。获取值的方法根据分类有如下可能：
             *
             * - 获取字符串的方法，返回空字符串`""`
             * - 获取未知类型的方法，返回`null`
             * - 获取对象的方法，返回空对象`{}`
             * - 获取数组的方法，返回空数组`[]`
             * - 获取`boolean`值的方法，返回`false`
             * - {@link SafeWrapper#getCategory}返回`"control"`
             * - {@link SafeWrapper#getChildSafely}返回一个{@link SafeWrapper}对象
             *
             * 通常不应该直接实例化此类，通过以下方法获取此类的实例：
             *
             * - {@link ViewContext#getSafely}
             * - {@link Control#getChildSafely}
             * - {@link main#wrap}
             *
             * @extends Control
             * @constructor
             */
            function SafeWrapper() {
            }

            // 设置值的方法
            u.each(
                [
                    'enable', 'disable', 'setDisabled',
                    'show', 'hide', 'toggle',
                    'setValue', 'setRawValue',
                    'addChild', 'removeChild',
                    'set',
                    'addState', 'removeState', 'toggleState',
                    'on', 'off', 'fire',
                    'dispose', 'destroy',
                    'initOptions', 'createMain', 'initStructure',
                    'setViewContext',
                    'render', 'repaint', 'appendTo', 'insertBefore',
                    'initChildren', 'disposeChildren'
                ],
                function (method) {
                    SafeWrapper.prototype[method] = function () {};
                }
            );

            // 获取值方法
            u.each(
                ['isDisabled', 'isHidden', 'hasState', 'isPropertyChanged'],
                function (method) {
                    SafeWrapper.prototype[method] = function () {
                        return false;
                    };
                }
            );

            u.each(
                ['getRawValue', 'getChild', 'get'],
                function (method) {
                    SafeWrapper.prototype[method] = function () {
                        return null;
                    };
                }
            );

            u.each(
                ['getValue'],
                function (method) {
                    SafeWrapper.prototype[method] = function () {
                        return '';
                    };
                }
            );

            u.each(
                ['setProperties'],
                function (method) {
                    SafeWrapper.prototype[method] = function () {
                        return {};
                    };
                }
            );

            // 特殊的几个
            SafeWrapper.prototype.getCategory = function () {
                return 'control';
            };

            SafeWrapper.prototype.getChildSafely = function (childName) {
                var wrapper = new SafeWrapper();

                wrapper.childName = childName;
                wrapper.parent = this;
                if (this.viewContext) {
                    wrapper.viewContext = this.viewContext;
                }

                return wrapper;
            };

            return SafeWrapper;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 视图环境类 用于对控件视图的管理
     * @author DBear, errorrik, otakustay
     */
    define(
        'esui/ViewContext',['require','./ControlCollection','./lib','./lib','./SafeWrapper'],function (require) {
            var ControlCollection = require('./ControlCollection');

            /**
             * 控件分组
             *
             * 控件分组表达一组控件，类似`getElementsByClass(className)`的效果，
             * 分组同时提供一些方法以方便地操作这个集合
             *
             * 控件分组是内部类，仅可以通过{@link ViewContext#getGroup}方法获取
             *
             * 为了保持私有性，`ControlGroup`去除了{@link ControlCollection#add}和
             * {@link ControlCollection#remove}方法，使用者不能修改集合
             *
             * @param {string} name 分组名称
             * @extends ControlCollection
             * @constructor
             * @private
             */
            function ControlGroup(name) {
                ControlCollection.apply(this, arguments);

                /**
                 * @property {string} name
                 *
                 * 当前控件分组的名称
                 *
                 * @readonly
                 */
                this.name = name;
            }

            require('./lib').inherits(ControlGroup, ControlCollection);

            /**
             * @method
             *
             * `ControlGroup`不提供此方法
             */
            ControlGroup.prototype.add = undefined;

            /**
             * @method
             *
             * `ControlGroup`不提供此方法
             */
            ControlGroup.prototype.remove = undefined;

            /**
             * 销毁当前实例
             */
            ControlGroup.prototype.disposeGroup = function () {
                for (var i = 0; i < this.length; i++) {
                    delete this[i];
                }
                this.length = 0;
            };

            function addToGroup(control, group) {
                ControlCollection.prototype.add.call(group, control);
            }

            function removeFromGroup(control, group) {
                ControlCollection.prototype.remove.call(group, control);
            }

            function getGroupNames(control) {
                var group = control.get('group');
                return group ? group.split(/[\t\r\n ]/) : [];
            }

            var counter = 0x830903;

            /**
             * 获取唯一id
             *
             * @return {string}
             */
            function getGUID() {
                return 'vt' + counter++;
            }

            /**
             * 视图环境对象池
             *
             * @type {Object}
             * @private
             */
            var pool = {};

            /**
             * 视图环境类
             *
             * 一个视图环境是一组控件的集合，不同视图环境中相同id的控件的DOM id不会重复
             *
             * @constructor
             * @param {string} id 该`ViewContext`的id
             */
            function ViewContext(id) {
                /**
                 * 视图环境控件集合
                 *
                 * @type {Object}
                 * @private
                 */
                this.controls = {};

                /**
                 * 视图环境控件分组集合
                 *
                 * @type {Object}
                 * @private
                 */
                this.groups = {};

                id = id || getGUID();
                // 如果已经有同名的，就自增长一下
                if (pool.hasOwnProperty(id)) {
                    var i = 1;
                    var prefix = id + '-';
                    while (pool.hasOwnProperty(prefix + i)) {
                        i++;
                    }
                    id = prefix + i;
                }

                /**
                 * 视图环境id
                 *
                 * @type {string}
                 * @readonly
                 */
                this.id = id;

                // 入池
                pool[this.id] = this;
            }

            /**
             * 根据id获取视图环境
             *
             * @param {string} id 视图环境id
             * @static
             */
            ViewContext.get = function ( id ) {
                return pool[id] || null;
            };

            /**
             * 将控件实例添加到视图环境中
             *
             * @param {Control} control 待加控件
             */
            ViewContext.prototype.add = function (control) {
                var exists = this.controls[control.id];

                // id已存在
                if (exists) {
                    // 是同一控件，不做处理
                    if (exists === control) {
                        return;
                    }

                    // 不是同一控件，先覆盖原关联控件的viewContext
                    exists.setViewContext(null);
                }

                this.controls[control.id] = control;

                var groups = getGroupNames(control);
                for (var i = 0; i < groups.length; i++) {
                    var groupName = groups[i];

                    if (!groupName) {
                        continue;
                    }

                    var group = this.getGroup(groupName);
                    addToGroup(control, group);
                }

                control.setViewContext(this);

            };

            /**
             * 将控件实例从视图环境中移除。
             *
             * @param {Control} control 待移除控件
             */
            ViewContext.prototype.remove = function (control) {
                delete this.controls[control.id];

                var groups = getGroupNames(control);
                for (var i = 0; i < groups.length; i++) {
                    var groupName = groups[i];

                    if (!groupName) {
                        continue;
                    }

                    var group = this.getGroup(groupName);
                    removeFromGroup(control, group);
                }

                control.setViewContext(null);

            };

            /**
             * 通过id获取控件实例。
             *
             * @param {string} id 控件id
             * @return {Control} 根据id获取的控件
             */
            ViewContext.prototype.get = function (id) {
                return this.controls[id];
            };

            /**
             * 获取viewContext内所有控件
             *
             * @return {Control[]} viewContext内所有控件
             */
            ViewContext.prototype.getControls = function () {
                return require('./lib').extend({}, this.controls);
            };

            var SafeWrapper = require('./SafeWrapper');

            /**
             * 根据id获取控件实例，如无相关实例则返回{@link SafeWrapper}
             *
             * @param {string} id 控件id
             * @return {Control} 根据id获取的控件
             */
            ViewContext.prototype.getSafely = function (id) {
                var control = this.get(id);

                if (!control) {
                    control = new SafeWrapper();
                    control.id = id;
                    control.viewContext = this;
                }

                return control;
            };

            /**
             * 获取一个控件分组
             *
             * @param {string} name 分组名称
             * @return {ControlGroup}
             */
            ViewContext.prototype.getGroup = function (name) {
                if (!name) {
                    throw new Error('name is unspecified');
                }

                var group = this.groups[name];
                if (!group) {
                    group = this.groups[name] = new ControlGroup(name);
                }
                return group;
            };

            /**
             * 清除视图环境中所有控件
             */
            ViewContext.prototype.clean = function () {
                for (var id in this.controls) {
                    if (this.controls.hasOwnProperty(id)) {
                        var control = this.controls[id];
                        control.dispose();
                        // 如果控件销毁后“不幸”`viewContext`还在，就移除掉
                        if (control.viewContext && control.viewContext === this) {
                            this.remove(control);
                        }
                    }
                }

                for (var name in this.groups) {
                    if (this.groups.hasOwnProperty(name)) {
                        this.groups[name].disposeGroup();
                        this.groups[name] = undefined;
                    }
                }
            };

            /**
             * 销毁视图环境
             */
            ViewContext.prototype.dispose = function () {
                this.clean();
                delete pool[this.id];
            };

            return ViewContext;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 主模块
     * @author erik
     */
    define(
        'esui/main',['require','./lib','./ViewContext','./ControlCollection'],function (require) {
            var lib = require('./lib');

            /**
             * 主模块
             *
             * @class main
             * @alias ui
             * @singleton
             */
            var main = {};

            /**
             * 版本号常量
             *
             * @type {string}
             * @readonly
             */
            main.version = '3.1.0-beta.6';

            var ViewContext = require('./ViewContext');
            var defaultViewContext = new ViewContext('default');

            /**
             * 获取默认的控件视图环境
             *
             * @return {ViewContext}
             */
            main.getViewContext = function () {
                return defaultViewContext;
            };

            /**
             * 控件库配置数据
             *
             * @type {Object}
             * @ignore
             */
            var config = {
                uiPrefix: 'data-ui',
                extensionPrefix: 'data-ui-extension',
                customElementPrefix: 'esui',
                instanceAttr: 'data-ctrl-id',
                viewContextAttr: 'data-ctrl-view-context',
                uiClassPrefix: 'ui',
                skinClassPrefix: 'skin',
                stateClassPrefix: 'state'
            };

            /**
             * 配置控件库
             *
             * 可用的配置有：
             *
             * - `{string} uiPrefix="data-ui"`：HTML中用于表示控件属性的DOM属性前缀
             * - `{string} extensionPrefix="data-ui-extension"`：用于表示扩展属性的前缀
             * - `{string} instanceAttr="data-ctrl-id"`：
             * 标识控件id的DOM属性名，配合`viewContextAttr`可根据DOM元素获取对应的控件
             * - `{string} viewContextAttr="data-ctrl-view-context"`：
             * 标识视图上下文id的DOM属性名，配合`instanceAttr`可根据DOM元素获取对应的控件
             * - `{string} uiClassPrefix="ui"`：控件生成DOM元素的class的前缀
             * - `{string} skinClassPrefix="skin"`：控件生成皮肤相关DOM元素class的前缀
             * - `{string} stateClassPrefix="state"`：控件生成状态相关DOM元素class的前缀
             *
             * @param {Object} info 控件库配置信息对象
             */
            main.config = function (info) {
                lib.extend(config, info);
            };

            /**
             * 获取配置项
             *
             * 具体可用配置参考{@link main#config}方法的说明
             *
             * @param {string} name 配置项名称
             * @return {Mixed} 配置项的值
             */
            main.getConfig = function (name) {
                return config[name];
            };

            /**
             * 将`name:value[;name:value]`的属性值解析成对象
             *
             * @param {string} source 属性值源字符串
             * @param {Function} valueReplacer 替换值的处理函数，每个值都将经过此函数
             * @return {Object}
             */
            main.parseAttribute = function (source, valueReplacer) {
                if (!source) {
                    return {};
                }
                // 为了让key和value中有`:`或`;`这类分隔符时能正常工作，不采用正则
                //
                // 分析的原则是：
                //
                // 1. 找到第1个冒号，取前面部分为key
                // 2. 找下个早号前的最后一个分号，取前面部分为value
                // 3. 如果字符串没结束，回到第1步
                var result = {}; // 保存结果
                var lastStop = 0; // 上次找完时停下的位置，分隔字符串用
                var cursor = 0; // 当前检索到的字符
                // 为了保证只用一个`source`串就搞定，下面会涉及到很多的游标，
                // 简单的方法是每次截完一段后把`soruce`截过的部分去掉，
                // 不过这么做会频繁分配字符串对象，所以优化了一下保证`source`不变
                while (cursor < source.length) {
                    // 找key，找到第1个冒号
                    while (cursor < source.length && source.charAt(cursor) !== ':') {
                        cursor++;
                    }
                    // 如果找到尾也没找到冒号，那就是最后有一段非键值对的字符串，丢掉
                    if (cursor >= source.length) {
                        break;
                    }
                    // 把key截出来
                    var key = lib.trim(source.slice(lastStop, cursor));
                    // 移到冒号后面一个字符
                    cursor++;
                    // 下次切分就从这个字符开始了
                    lastStop = cursor;
                    // 找value，要找最后一个分号，这里就需要前溯了，先找到第1个分号
                    while (cursor < source.length
                        && source.charAt(cursor) !== ';'
                        ) {
                        cursor++;
                    }
                    // 然后做前溯一直到下一个冒号
                    var lookAheadIndex = cursor + 1;
                    while (lookAheadIndex < source.length) {
                        var ch = source.charAt(lookAheadIndex);
                        // 如果在中途还发现有分号，把游标移过来
                        if (ch === ';') {
                            cursor = lookAheadIndex;
                        }
                        // 如果发现了冒号，则上次的游标就是最后一个分号了
                        if (ch === ':') {
                            break;
                        }
                        lookAheadIndex++;
                    }
                    // 把value截出来，这里没有和key一样判断是否已经跑到尾，
                    // 是因为我们允许最后一个键值对没有分号结束，
                    // 但是会遇上`key:`这样的串，即只有键没有值，
                    // 这时我们就认为值是个空字符串了
                    var value = lib.trim(source.slice(lastStop, cursor));
                    // 加入到结果中
                    result[key] = valueReplacer ? valueReplacer(value) : value;
                    // 再往前进一格，开始下一次查找
                    cursor++;
                    lastStop = cursor;
                }

                return result;
            };

            /**
             * 寻找DOM元素所对应的控件
             *
             * @param {HTMLElement} dom DOM元素
             * @return {Control | null} `dom`对应的控件实例，
             * 如果`dom`不存在或不对应任何控件则返回`null`
             */
            main.getControlByDOM = function (dom) {
                if (!dom) {
                    return null;
                }

                var getConf = main.getConfig;

                var controlId = dom.getAttribute(getConf('instanceAttr'));
                var viewContextId = dom.getAttribute(getConf('viewContextAttr'));
                var viewContext;

                if (controlId
                    && viewContextId
                    && (viewContext = ViewContext.get(viewContextId))
                    ) {
                    return viewContext.get(controlId);
                }
                return null;
            };

            /**
             * 注册类。用于控件类、规则类或扩展类注册
             *
             * @param {Function} classFunc 类Function
             * @param {Object} container 类容器
             * @ignore
             */
            function registerClass(classFunc, container) {
                if (typeof classFunc === 'function') {
                    var type = classFunc.prototype.type;
                    if (type in container) {
                        throw new Error(type + ' is exists!');
                    }

                    container[type] = classFunc;
                }
            }

            /**
             * 创建类实例。用于控件类、规则类或扩展类的实例创建
             *
             * @param {string} type 类型
             * @param {Object} options 初始化参数
             * @param {Object} container 类容器
             * @ignore
             */
            function createInstance(type, options, container) {
                var Constructor = container[type];
                if (Constructor) {
                    delete options.type;
                    return new Constructor(options);
                }

                return null;
            }

            /**
             * 控件类容器
             *
             * @type {Object}
             * @ignore
             */
            var controlClasses = {};

            /**
             * 注册控件类
             *
             * 该方法通过类的`prototype.type`识别控件类型信息。
             *
             * @param {Function} controlClass 控件类
             * @throws
             * 已经有相同`prototype.type`的控件类存在，不能重复注册同类型控件
             */
            main.register = function (controlClass) {
                registerClass(controlClass, controlClasses);
            };

            /**
             * 创建控件
             *
             * @param {string} type 控件类型
             * @param {Object} options 初始化参数
             * @return {Control}
             */
            main.create = function (type, options) {
                return createInstance(type, options, controlClasses);
            };

            /**
             * 获取控件
             *
             * @param {string} id 控件的id
             * @return {Control | null}
             */
            main.get = function (id) {
                return defaultViewContext.get(id);
            };

            /**
             * 根据id获取控件实例，如无相关实例则返回{@link SafeWrapper}
             *
             * @param {string} id 控件id
             * @return {Control} 根据id获取的控件
             */
            main.getSafely = function (id) {
                return defaultViewContext.getSafely(id);
            };

            var ControlCollection = require('./ControlCollection');

            /**
             * 创建控件包裹，返回一个{@link ControlCollection}对象
             *
             * @param {Control...} controls 需要包裹的控件
             * @return {ControlCollection}
             */
            main.wrap = function () {
                var collection = new ControlCollection();

                for (var i = 0; i < arguments.length; i++) {
                    collection.add(arguments[i]);
                }

                return collection;
            };

            /**
             * 从容器DOM元素批量初始化内部的控件渲染
             *
             * @param {HTMLElement} [wrap=document.body] 容器DOM元素，默认
             * @param {Object} [options] init参数
             * @param {Object} [options.viewContext] 视图环境
             * @param {Object} [options.properties] 属性集合，通过id映射
             * @param {Object} [options.valueReplacer] 属性值替换函数
             * @return {Control[]} 初始化的控件对象集合
             */
            main.init = function (wrap, options) {
                wrap = wrap || document.body;
                options = options || {};

                var valueReplacer = options.valueReplacer || function (value) {
                    return value;
                };

                /**
                 * 将字符串数组join成驼峰形式
                 *
                 * @param {string[]} source 源字符串数组
                 * @return {string}
                 * @ignore
                 */
                function joinCamelCase(source) {
                    function replacer(c) {
                        return c.toUpperCase();
                    }

                    for (var i = 1, len = source.length; i < len; i++) {
                        source[i] = source[i].replace(/^[a-z]/, replacer);
                    }

                    return source.join('');
                }

                /**
                 * 不覆盖目标对象成员的extend
                 *
                 * @param {Object} target 目标对象
                 * @param {Object} source 源对象
                 * @ignore
                 */
                function noOverrideExtend(target, source) {
                    for (var key in source) {
                        if (!(key in target)) {
                            target[key] = source[key];
                        }
                    }
                }

                /**
                 * 将标签解析的值附加到option对象上
                 *
                 * @param {Object} optionObject option对象
                 * @param {string[]} terms 经过切分的标签名解析结果
                 * @param {string} value 属性值
                 * @ignore
                 */
                function extendToOption(optionObject, terms, value) {
                    if (terms.length === 0) {
                        noOverrideExtend(
                            optionObject,
                            main.parseAttribute(value, valueReplacer)
                        );
                    }
                    else {
                        optionObject[joinCamelCase(terms)] = valueReplacer(value);
                    }
                }

                // 把dom元素存储到临时数组中
                // 控件渲染的过程会导致Collection的改变
                var rawElements = wrap.getElementsByTagName('*');
                var elements = [];
                for (var i = 0, len = rawElements.length; i < len; i++) {
                    if (rawElements[i].nodeType === 1) {
                        elements.push(rawElements[i]);
                    }
                }

                var uiPrefix = main.getConfig('uiPrefix');
                var extPrefix = main.getConfig('extensionPrefix');
                var customElementPrefix = main.getConfig('customElementPrefix');
                var uiPrefixLen = uiPrefix.length;
                var extPrefixLen = extPrefix.length;
                var properties = options.properties || {};
                var controls = [];
                for (var i = 0, len = elements.length; i < len; i++) {
                    var element = elements[i];

                    // 有时候，一个控件会自己把`main.innerHTML`生成子控件，比如`Panel`，
                    // 但这边有缓存这些子元素，可能又会再生成一次，所以要去掉
                    if (element.getAttribute(config.instanceAttr)) {
                        continue;
                    }

                    var attributes = element.attributes;
                    var controlOptions = {};
                    var extensionOptions = {};

                    // 解析attribute中的参数
                    for (var j = 0, attrLen = attributes.length; j < attrLen; j++) {
                        var attribute = attributes[j];
                        var name = attribute.name;
                        var value = attribute.value;

                        if (name.indexOf(extPrefix) === 0) {
                            // 解析extension的key
                            var terms = name.slice(extPrefixLen + 1).split('-');
                            var extKey = terms[0];
                            terms.shift();

                            // 初始化该key的option对象
                            var extOption = extensionOptions[extKey];
                            if (!extOption) {
                                extOption = extensionOptions[extKey] = {};
                            }

                            extendToOption(extOption, terms, value);
                        }
                        else if (name.indexOf(uiPrefix) === 0) {
                            var terms = name.length === uiPrefixLen
                                ? []
                                : name.slice(uiPrefixLen + 1).split('-');
                            extendToOption(controlOptions, terms, value);
                        }
                    }

                    // 根据选项创建控件
                    var type = controlOptions.type;
                    if (!type) {
                        var nodeName = element.nodeName.toLowerCase();
                        var esuiPrefixIndex = nodeName.indexOf(customElementPrefix);
                        if (esuiPrefixIndex === 0) {
                            var typeFromCustomElement;
                            /* jshint ignore:start */
                            typeFromCustomElement = nodeName.replace(
                                /-(\S)/g,
                                function (match, ch) { return ch.toUpperCase(); }
                            );
                            /* jshint ignore:end */
                            typeFromCustomElement = typeFromCustomElement.slice(customElementPrefix.length);
                            controlOptions.type = typeFromCustomElement;
                            type = typeFromCustomElement;
                        }
                    }
                    if (type) {
                        // 从用户传入的properties中merge控件初始化属性选项
                        var controlId = controlOptions.id;
                        var customOptions = controlId
                            ? properties[controlId]
                            : {};
                        for (var key in customOptions) {
                            controlOptions[key] = valueReplacer(customOptions[key]);
                        }

                        // 创建控件的插件
                        var extensions = controlOptions.extensions || [];
                        controlOptions.extensions = extensions;
                        for (var key in extensionOptions) {
                            var extOption = extensionOptions[key];
                            var extension = main.createExtension(
                                extOption.type,
                                extOption
                            );
                            extension && extensions.push(extension);
                        }

                        // 绑定视图环境和控件主元素
                        controlOptions.viewContext = options.viewContext;
                        // 容器类控件会需要渲染自己的`innerHTML`，
                        // 这种渲染使用`initChildren`，再调用`main.init`，
                        // 因此需要把此处`main.init`的参数交给控件，方便再带回来，
                        // 以便`properties`、`valueReplacer`之类的能保留
                        controlOptions.renderOptions = options;
                        controlOptions.main = element;

                        // 创建控件
                        var control = main.create(type, controlOptions);
                        if (control) {
                            controls.push(control);
                            if (options.parent) {
                                options.parent.addChild(control);
                            }
                            try {
                                control.render();
                            }
                            catch (ex) {
                                var error = new Error(
                                        'Render control '
                                        + '"' + (control.id || 'anonymous') + '" '
                                        + 'of type ' + control.type + ' '
                                        + 'failed because: '
                                        + ex.message
                                );
                                error.actualError = ex;
                                throw error;
                            }
                        }
                    }
                }

                return controls;
            };

            /**
             * 扩展类容器
             *
             * @type {Object}
             * @ignore
             */
            var extensionClasses = {};

            /**
             * 注册扩展类。
             *
             * 该方法通过类的`prototype.type`识别扩展类型信息
             *
             * @param {Function} extensionClass 扩展类
             * @throws
             * 已经有相同`prototype.type`的扩展类存在，不能重复注册同类型扩展
             */
            main.registerExtension = function (extensionClass) {
                registerClass(extensionClass, extensionClasses);
            };

            /**
             * 创建扩展
             *
             * @param {string} type 扩展类型
             * @param {Object} options 初始化参数
             * @return {Extension}
             */
            main.createExtension = function (type, options) {
                return createInstance(type, options, extensionClasses);
            };

            /**
             * 全局扩展选项容器
             *
             * @type {Object}
             * @ignore
             */
            var globalExtensionOptions = {};

            /**
             * 绑定全局扩展
             *
             * 通过此方法绑定的扩展，会对所有的控件实例生效
             *
             * 每一次全局扩展生成实例时，均会复制`options`对象，而不会直接使用引用
             *
             * @param {string} type 扩展类型
             * @param {Object} options 扩展初始化参数
             */
            main.attachExtension = function (type, options) {
                globalExtensionOptions[type] = options;
            };

            /**
             * 创建全局扩展对象
             *
             * @return {Extension[]}
             */
            main.createGlobalExtensions = function () {
                var options = globalExtensionOptions;
                var extensions = [];
                for (var type in globalExtensionOptions) {
                    if (globalExtensionOptions.hasOwnProperty(type)) {
                        var extension = main.createExtension(type, globalExtensionOptions[type]);
                        extension && extensions.push(extension);
                    }
                }

                return extensions;
            };

            /**
             * 验证规则类容器
             *
             * @type {Object}
             * @ignore
             */
            var ruleClasses = [];

            /**
             * 注册控件验证规则类
             *
             * 该方法通过类的`prototype.type`识别验证规则类型信息
             *
             * @param {Function} ruleClass 验证规则类
             * @param {number} priority 优先级，越小的优先级越高
             * @throws
             * 已经有相同`prototype.type`的验证规则类存在，不能重复注册同类型验证规则
             */
            main.registerRule = function (ruleClass, priority) {
                // 多个Rule共享一个属性似乎也没问题
                ruleClasses.push({ type: ruleClass, priority: priority });
                // 能有几个规则，这里就不优化为插入排序了
                ruleClasses.sort(
                    function (x, y) { return x.priority - y.priority; });
            };

            /**
             * 创建控件实例需要的验证规则
             *
             * @param {Control} control 控件实例
             * @return {validator.Rule[]} 验证规则数组
             */
            main.createRulesByControl = function (control) {
                var rules = [];
                for (var i = 0; i < ruleClasses.length; i++) {
                    var RuleClass = ruleClasses[i].type;
                    if (control.get(RuleClass.prototype.type) != null) {
                        rules.push(new RuleClass());
                    }
                }

                return rules;
            };

            return main;
        }
    );

    define('esui', ['esui/main'], function (main) { return main; });

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 渲染器模块
     * @author otakustay
     */
    define(
        'esui/painters',['require','underscore','./lib'],function (require) {
            var u = require('underscore');
            var lib = require('./lib');

            /**
             * @class painters
             *
             * 渲染器模块，用于提供生成`painter`方法的工厂方法
             *
             * @singleton
             */
            var painters = {};

            /**
             * 生成一个将属性与控件状态关联的渲染器
             *
             * 当属性变为`true`的时候使用`addState`添加状态，反之使用`removeState`移除状态
             *
             * @param {string} name 指定负责的属性名，同时也是状态名称
             * @return {Object} 一个渲染器配置
             */
            painters.state = function (name) {
                return {
                    name: name,
                    paint: function (control, value) {
                        var method = value ? 'addState' : 'removeState';
                        control[method](this.name);
                    }
                };
            };

            /**
             * 生成一个将控件属性与控件主元素元素的属性关联的渲染器
             *
             * 当控件属性变化时，将根据参数同步到主元素元素的属性上
             *
             *     @example
             *     // 将target属性与<a>元素关联
             *     var painter = painters.attribute('target');
             *
             *     // 可以选择关联到不同的DOM属性
             *     var painter = painters.attribute('link', 'href');
             *
             *     // 可以指定DOM属性的值
             *     var painter = painters.attribute('active', 'checked', true);
             *
             * @param {string} name 指定负责的属性名
             * @param {string} [attribute] 对应DOM属性的名称，默认与`name`相同
             * @param {Mixed} [value] 固定DOM属性的值，默认与更新的值相同
             * @return {Object} 一个渲染器配置
             */
            painters.attribute = function (name, attribute, value) {
                return {
                    name: name,
                    attribute: attribute || name,
                    value: value,
                    paint: function (control, value) {
                        value = this.value == null ? value : this.value;
                        control.main.setAttribute(this.attribute, value);
                    }
                };
            };

            // 这些属性不用加`px`
            var unitProperties = {
                width: true,
                height: true,
                top: true,
                right: true,
                bottom: true,
                left: true,
                fontSize: true,
                padding: true,
                paddingTop: true,
                paddingRight: true,
                paddingBottom: true,
                paddingLeft: true,
                margin: true,
                marginTop: true,
                marginRight: true,
                marginBottom: true,
                marginLeft: true,
                borderWidth: true,
                borderTopWidth: true,
                borderRightWidth: true,
                borderBottomWidth: true,
                borderLeftWidth: true
            };

            /**
             * 生成一个将控件属性与控件主元素元素的样式关联的渲染器
             *
             * 当控件属性变化时，将根据参数同步到主元素元素的样式上
             *
             * @param {string} name 指定负责的属性名
             * @param {string} [property] 对应的样式属性名，默认与`name`相同
             * @return {Object} 一个渲染器配置
             */
            painters.style = function (name, property) {
                return {
                    name: name,
                    property: property || name,
                    paint: function (control, value) {
                        if (value == null) {
                            return;
                        }
                        if (unitProperties.hasOwnProperty(this.property)) {
                            value = value === 0 ? '0' : value + 'px';
                        }
                        control.main.style[this.property] = value;
                    }
                };
            };

            /**
             * 生成一个将控件属性与某个DOM元素的HTML内容关联的渲染器
             *
             * 当控件属性变化时，对应修改DOM元素的`innerHTML`
             *
             * @param {string} name 指定负责的属性名
             * @param {string | Function} [element] 指定DOM元素在当前控件下的部分名，
             * 可以提供函数作为参数，则函数返回需要更新的DOM元素
             * @param {Function} [generate] 指定生成HTML的函数，默认直接使用控件属性的值
             * @return {Object} 一个渲染器配置
             */
            painters.html = function (name, element, generate) {
                return {
                    name: name,
                    element: element || '',
                    generate: generate,
                    paint: function (control, value) {
                        var element = typeof this.element === 'function'
                            ? this.element(control)
                            : this.element
                            ? control.helper.getPart(this.element)
                            : control.main;
                        if (element) {
                            var html = typeof this.generate === 'function'
                                ? this.generate(control, value)
                                : value;
                            element.innerHTML = html || '';
                        }
                    }
                };
            };

            /**
             * 生成一个将控件属性与某个DOM元素的HTML内容关联的渲染器
             *
             * 当控件属性变化时，对应修改DOM元素的文本内容
             *
             * 本方法与{@link painters#html}相似，区别在于会将内容进行一次HTML转义
             *
             * @param {string} name 指定负责的属性名
             * @param {string | Function} [element] 指定DOM元素在当前控件下的部分名，
             * 可以提供函数作为参数，则函数返回需要更新的DOM元素
             * @param {Function} [generate] 指定生成HTML的函数，默认直接使用控件属性的值，
             * 该函数返回原始的HTML，不需要做额外的转义工作
             * @return {Object} 一个渲染器配置
             */
            painters.text = function (name, element, generate) {
                return {
                    name: name,
                    element: element || '',
                    generate: generate,
                    paint: function (control, value) {
                        var element = typeof this.element === 'function'
                            ? this.element(control)
                            : this.element
                            ? control.helper.getPart(this.element)
                            : control.main;
                        if (element) {
                            var html = typeof this.generate === 'function'
                                ? this.generate(control, value)
                                : value;
                            element.innerHTML = u.escape(html || '');
                        }
                    }
                };
            };


            /**
             * 生成一个将控件属性的变化代理到指定成员的指定方法上
             *
             * @param {string} name 指定负责的属性名
             * @param {string} member 指定成员名
             * @param {string} method 指定调用的方法名称
             * @return {Object} 一个渲染器配置
             */
            painters.delegate = function (name, member, method) {
                return {
                    name: name,
                    member: this.member,
                    method: this.method,
                    paint: function (control, value) {
                        control[this.member][this.method](value);
                    }
                };
            };

            /**
             * 通过提供一系列`painter`对象创建`repaint`方法
             *
             * 本方法接受以下2类作为“渲染器”：
             *
             * - 直接的函数对象
             * - 一个`painter`对象
             *
             * 当一个直接的函数对象作为“渲染器”时，会将`changes`和`changesIndex`两个参数
             * 传递给该函数，函数具有最大的灵活度来自由操作控件
             *
             * 一个`painter`对象必须包含以下属性：
             *
             * - `{string | string[]} name`：指定这个`painter`对应的属性或属性集合
             * - `{Function} paint`：指定渲染的函数
             *
             * 一个`painter`在执行时，其`paint`函数将接受以下参数：
             *
             * - `{Control} control`：当前的控件实例
             * - `{Mixed} args...`：根据`name`配置指定的属性，依次将属性的最新值作为参数
             *
             * @param {Object... | Function...} args `painter`对象
             * @return {Function} `repaint`方法的实现
             */
            painters.createRepaint = function () {
                var painters = [].concat.apply([], [].slice.call(arguments));

                return function (changes, changesIndex) {
                    // 临时索引，不能直接修改`changesIndex`，会导致子类的逻辑错误
                    var index = lib.extend({}, changesIndex);
                    for (var i = 0; i < painters.length; i++) {
                        var painter = painters[i];

                        // 如果是一个函数，就认为这个函数处理所有的变化，直接调用一下
                        if (typeof painter === 'function') {
                            painter.apply(this, arguments);
                            continue;
                        }

                        // 其它情况下，走的是`painter`的自动化属性->函数映射机制
                        var propertyNames = [].concat(painter.name);

                        // 以下2种情况下要调用：
                        // 
                        // - 第一次重会（没有`changes`）
                        // - `changesIndex`有任何一个负责的属性的变化
                        var shouldPaint = !changes;
                        if (!shouldPaint) {
                            for (var j = 0; j < propertyNames.length; j++) {
                                var name = propertyNames[j];
                                if (changesIndex.hasOwnProperty(name)) {
                                    shouldPaint = true;
                                    break;
                                }
                            }
                        }
                        if (!shouldPaint) {
                            continue;
                        }

                        // 收集所有属性的值
                        var properties = [this];
                        for (var j = 0; j < propertyNames.length; j++) {
                            var name = propertyNames[j];
                            properties.push(this[name]);
                            // 从索引中删除，为了后续构建`unpainted`数组
                            delete index[name];
                        }
                        // 绘制
                        try {
                            painter.paint.apply(painter, properties);
                        }
                        catch (ex) {
                            var paintingPropertyNames =
                                '"' + propertyNames.join('", "') + '"';
                            var error = new Error(
                                    'Failed to paint [' + paintingPropertyNames + '] '
                                    + 'for control "' + (this.id || 'anonymous')+ '" '
                                    + 'of type ' + this.type + ' '
                                    + 'because: ' + ex.message
                            );
                            error.actualError = error;
                            throw error;
                        }

                    }

                    // 构建出未渲染的属性集合
                    var unpainted = [];
                    for (var key in index) {
                        if (index.hasOwnProperty(key)) {
                            unpainted.push(index[key]);
                        }
                    }

                    return unpainted;
                };
            };

            return painters;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 子控件相关辅助方法
     * @author otakustay
     */
    define(
        'esui/helper/children',['require','underscore','../main'],function (require) {
            var u = require('underscore');
            var ui = require('../main');

            /**
             * @override Helper
             */
            var helper = {};

            /**
             * 批量初始化子控件
             *
             * @param {HTMLElement} [wrap] 容器DOM元素，默认为主元素
             * @param {Object} [options] init参数
             * @param {Object} [options.properties] 属性集合，通过id映射
             */
            helper.initChildren = function (wrap, options) {
                wrap = wrap || this.control.main;
                options = u.extend({}, this.control.renderOptions, options);
                options.viewContext = this.control.viewContext;
                options.parent = this.control;

                ui.init(wrap, options);
            };

            /**
             * 销毁所有子控件
             */
            helper.disposeChildren = function () {
                var children = this.control.children.slice();
                u.each(
                    children,
                    function (child) {
                        child.dispose();
                    }
                );
                this.children = [];
                this.childrenIndex = {};
            };

            /**
             * 禁用全部子控件
             */
            helper.disableChildren = function () {
                u.each(
                    this.control.children,
                    function (child) {
                        child.dispose();
                    }
                );
            };

            /**
             * 启用全部子控件
             */
            helper.enableChildren = function () {
                u.each(
                    this.control.children,
                    function (child) {
                        child.enable();
                    }
                );
            };

            return helper;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 控件部件相关辅助方法
     * @author otakustay
     */
    define(
        'esui/helper/dom',['require','underscore','../lib','../main'],function (require) {
            /**
             * 获取控件用于生成css class的类型
             *
             * @param {Control} control 控件实例
             * @return {string}
             * @ignore
             */
            function getControlClassType(control) {
                var type = control.styleType || control.type;
                return type.toLowerCase();
            }

            /**
             * 将参数用`-`连接成字符串
             *
             * @param {string...} args 需要连接的串
             * @return {string}
             * @ignore
             */
            function joinByStrike() {
                return [].slice.call(arguments, 0).join('-');
            }

            var u = require('underscore');
            var lib = require('../lib');
            var ui = require('../main');

            /**
             * @override Helper
             */
            var helper = {};

            /**
             * 获取控件部件相关的class数组
             *
             * 如果不传递`part`参数，则生成如下：
             *
             * - `ui-ctrl`
             * - `ui-{styleType}`
             * - `skin-{skin}`
             * - `skin-{skin}-{styleType}`
             *
             * 如果有`part`参数，则生成如下：
             *
             * - `ui-{styleType}-{part}`
             * - `skin-{skin}-{styleType}-{part}`
             *
             * @param {string} [part] 部件名称
             * @return {string[]}
             */
            helper.getPartClasses = function (part) {
                if (part
                    && this.partClassCache
                    && this.partClassCache.hasOwnProperty(part)
                    ) {
                    // 得复制一份，不然外面拿到后往里`push`些东西就麻烦了
                    return this.partClassCache[part].slice();
                }

                var type = getControlClassType(this.control);
                var skin = this.control.skin;
                var prefix = ui.getConfig('uiClassPrefix');
                var skinPrefix = ui.getConfig('skinClassPrefix');
                var classes = [];

                if (part) {
                    classes.push(joinByStrike(prefix, type, part));
                    if (skin) {
                        classes.push(joinByStrike(skinPrefix, skin, type, part));
                    }

                    // 缓存起来
                    if (!this.partClassCache) {
                        this.partClassCache = {};
                        // 还是得复制一份，不然这个返回回去就可能被修改了
                        this.partClassCache[part] = classes.slice();
                    }
                }
                else {
                    classes.push(joinByStrike(prefix, 'ctrl'));
                    classes.push(joinByStrike(prefix, type));
                    if (skin) {
                        classes.push(
                            joinByStrike(skinPrefix, skin),
                            joinByStrike(skinPrefix, skin, type)
                        );
                    }
                }

                return classes;
            };

            /**
             * 获取控件部件相关的class字符串，具体可参考{@link Helper#getPartClasses}方法
             *
             * @param {string} [part] 部件名称
             * @return {string}
             */
            helper.getPartClassName = function (part) {
                return this.getPartClasses(part).join(' ');
            };

            /**
             * 获取控件部件相关的主class字符串
             *
             * 如果不传递`part`参数，则生成如下：
             *
             * - `ui-{styleType}`
             *
             * 如果有`part`参数，则生成如下：
             *
             * - `ui-{styleType}-{part}`
             *
             * @param {string} [part] 部件名称
             * @return {string}
             */
            helper.getPrimaryClassName = function (part) {
                var type = getControlClassType(this.control);

                if (part) {
                    return joinByStrike(ui.getConfig('uiClassPrefix'), type, part);
                }
                else {
                    return joinByStrike(ui.getConfig('uiClassPrefix'), type);
                }
            };

            /**
             * 添加控件部件相关的class，具体可参考{@link Helper#getPartClasses}方法
             *
             * @param {string} [part] 部件名称
             * @param {HTMLElement | string} [element] 部件元素或部件名称，默认为主元素
             */
            helper.addPartClasses = function (part, element) {
                if (typeof element === 'string') {
                    element = this.getPart(element);
                }

                element = element || this.control.main;
                if (element) {
                    lib.addClasses(
                        element,
                        this.getPartClasses(part)
                    );
                }
            };

            /**
             * 移除控件部件相关的class，具体可参考{@link Helper#getPartClasses}方法
             *
             * @param {string} [part] 部件名称
             * @param {HTMLElement | string} [element] 部件元素或部件名称，默认为主元素
             */
            helper.removePartClasses = function (part, element) {
                if (typeof element === 'string') {
                    element = this.getPart(element);
                }

                element = element || this.control.main;
                if (element) {
                    lib.removeClasses(
                        element,
                        this.getPartClasses(part)
                    );
                }
            };

            /**
             * 获取控件状态相关的class数组
             *
             * 生成如下：
             *
             * - `ui-{styleType}-{state}`
             * - `state-{state}`
             * - `skin-{skin}-{state}`
             * - `skin-{skin}-{styleType}-{state}`
             *
             * @param {string} state 状态名称
             * @return {string[]}
             */
            helper.getStateClasses = function (state) {
                if (this.stateClassCache
                    && this.stateClassCache.hasOwnProperty(state)
                    ) {
                    // 得复制一份，不然外面拿到后往里`push`些东西就麻烦了
                    return this.stateClassCache[state].slice();
                }

                var type = getControlClassType(this.control);
                var getConf = ui.getConfig;
                var classes = [
                    joinByStrike(getConf('uiClassPrefix'), type, state),
                    joinByStrike(getConf('stateClassPrefix'), state)
                ];

                var skin = this.control.skin;
                if (skin) {
                    var skinPrefix = getConf('skinClassPrefix');
                    classes.push(
                        joinByStrike(skinPrefix, skin, state),
                        joinByStrike(skinPrefix, skin, type, state)
                    );
                }

                // 缓存起来
                if (!this.stateClassCache) {
                    this.stateClassCache = {};
                    // 还是得复制一份，不然这个返回回去就可能被修改了
                    this.stateClassCache[state] = classes.slice();
                }

                return classes;
            };

            /**
             * 添加控件状态相关的class，具体可参考{@link Helper#getStateClasses}方法
             *
             * @param {string} state 状态名称
             */
            helper.addStateClasses = function (state) {
                var element = this.control.main;
                if (element) {
                    lib.addClasses(
                        element,
                        this.getStateClasses(state)
                    );
                }
            };

            /**
             * 移除控件状态相关的class，具体可参考{@link Helper#getStateClasses}方法
             *
             * @param {string} state 状态名称
             */
            helper.removeStateClasses = function (state) {
                var element = this.control.main;
                if (element) {
                    lib.removeClasses(
                        element,
                        this.getStateClasses(state)
                    );
                }
            };

            /**
             * 获取用于控件DOM元素的id
             *
             * @param {string} [part] 部件名称，如不提供则生成控件主元素的id
             * @return {string}
             */
            helper.getId = function (part) {
                part = part ? '-' + part : '';
                if (!this.control.domIDPrefix) {
                    this.control.domIDPrefix =
                        this.control.viewContext && this.control.viewContext.id;
                }
                var prefix = this.control.domIDPrefix
                    ? this.control.domIDPrefix+ '-'
                    : '';
                return 'ctrl-' + prefix + this.control.id + part;
            };

            /**
             * 创建一个部件元素
             *
             * @param {string} part 部件名称
             * @param {string} [nodeName="div"] 使用的元素类型
             */
            helper.createPart = function (part, nodeName) {
                nodeName = nodeName || 'div';
                var element = document.createElement(nodeName);
                element.id = this.getId(part);
                this.addPartClasses(part, element);
                return element;
            };

            /**
             * 获取指定部件的DOM元素
             *
             * @param {string} part 部件名称
             * @return {HTMLElement}
             */
            helper.getPart = function (part) {
                return lib.g(this.getId(part));
            };

            /**
             * 判断DOM元素是否某一部件
             *
             * @param {HTMLElement} element DOM元素
             * @param {string} part 部件名称
             * @return {boolean}
             */
            helper.isPart = function (element, part) {
                var className = this.getPartClasses(part)[0];
                return lib.hasClass(element, className);
            };

            // 这些属性是不复制的，多数是某些元素特有
            var INPUT_SPECIFIED_ATTRIBUTES = {
                type: true, name: true, alt: true,
                autocomplete: true, autofocus: true,
                checked: true, dirname: true, disabled: true,
                form: true, formaction: true, formenctype: true,
                formmethod: true, formnovalidate: true, formtarget: true,
                width: true, height: true, inputmode: true, list: true,
                max: true, maxlength: true, min: true, minlength: true,
                multiple: true, pattern: true, placeholder: true,
                readonly: true, required: true, size: true, src: true,
                step: true, value: true
            };

            /**
             * 替换控件的主元素
             *
             * @param {HTMLElement} [main] 用于替换的主元素，
             * 如不提供则使用当前控件实例的{@link Control#createMain}方法生成
             * @return {HTMLElement} 原来的主元素
             */
            helper.replaceMain = function (main) {
                main = main || this.control.createMain();
                var initialMain = this.control.main;

                // 欺骗一下`main`模块，让它别再次对原主元素进行控件创建
                initialMain.setAttribute(
                    ui.getConfig('instanceAttr'),
                    lib.getGUID()
                );

                // 把能复制的属性全复制过去
                var attributes = initialMain.attributes;
                for (var i = 0; i < attributes.length; i++) {
                    var attribute = attributes[i];
                    var name = attribute.name;
                    if (lib.hasAttribute(initialMain, name)
                        && !INPUT_SPECIFIED_ATTRIBUTES.hasOwnProperty(name)
                        ) {
                        lib.setAttribute(main, name, attribute.value);
                    }
                }

                lib.insertBefore(main, initialMain);
                initialMain.parentNode.removeChild(initialMain);
                this.control.main = main;

                return initialMain;
            };

            var INPUT_PROPERTY_MAPPING = {
                name: { name: 'name' },
                maxlength: { name: 'maxLength', type: 'number' },
                required: { name: 'required', type: 'boolean' },
                pattern: { name: 'pattern' },
                min: { name: 'min', type: 'number' },
                max: { name: 'max', type: 'number' },
                autofocus: { name: 'autoFocus', type: 'boolean' },
                disabled: { name: 'disabled', type: 'boolean' },
                readonly: { name: 'readOnly', type: 'boolean' }
            };

            /**
             * 从输入元素上抽取属性
             *
             * 该方法按以下对应关系抽取属性，当元素上不存在对应的DOM属性时，不会添加该属性：
             *
             * - DOM元素的`value`对应控件的`value`属性
             * - DOM元素的`name`对应控件的`name`属性
             * - DOM元素的`maxlength`对应控件的`maxLength`属性，且转为`number`类型
             * - DOM元素的`required`对应控件的`required`属性，且转为`boolean`类型
             * - DOM元素的`pattern`对应控件的`pattern`属性
             * - DOM元素的`min`对应控件的`min`属性，且转为`number`类型
             * - DOM元素的`max`对应控件的`max`属性，且转为`number`类型
             * - DOM元素的`autofocus`对应控件的`autoFocus`属性，且转为`boolean`类型
             * - DOM元素的`disabled`对应控件的`disabled`属性，且转为`boolean`类型
             * - DOM元素的`readonly`对应控件的`readOnly`属性，且转为`boolean`类型
             *
             * @param {HTMLElement} input 输入元素
             * @param {Object} [options] 已有的配置对象，有此参数则将抽取的属性覆盖上去
             * @return {Object}
             */
            helper.extractOptionsFromInput = function (input, options) {
                var result = {};
                u.each(
                    INPUT_PROPERTY_MAPPING,
                    function (config, attributeName) {
                        var specified = lib.hasAttribute(input, attributeName);
                        if (specified) {
                            var value = lib.getAttribute(input, attributeName);

                            switch (config.type) {
                                case 'boolean':
                                    value = specified;
                                    break;
                                case 'number':
                                    value = parseInt(value, 10);
                                    break;
                            }
                            result[config.name] = value;
                        }
                    }
                );

                // value要特殊处理一下，可能是通过innerHTML设置的，但是`<select>`元素在没有`value`属性时会自动选中第1个，
                // 这会影响诸如`selectedIndex`属性的效果，因此对`<select>`要特别地排除
                if (lib.hasAttribute(input, 'value')
                    || (input.nodeName.toLowerCase() !== 'select' && input.value)
                    ) {
                    result.value = input.value;
                }

                return u.defaults(options || {}, result);
            };

            return helper;
        }
    );

    /**
     * mini-event
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 工具库，仅供内部使用
     * @author otakustay
     */
    define(
        'mini-event/lib',['require'],function (require) {
            /**
             * 工具库模块
             *
             * 此模块为内部使用，不保证API的稳定性， *不要* 直接引用此模块
             *
             * @class lib
             * @singleton
             */
            var lib = {};

            /**
             * 扩展对象
             *
             * @param {Object} source 源对象
             * @param {Object...} additions 扩展的对象
             * @return {Object} 返回扩展后的`source`对象
             */
            lib.extend = function (source) {
                for (var i = 1; i < arguments.length; i++) {
                    var addition = arguments[i];

                    if (!addition) {
                        continue;
                    }

                    for (var key in addition) {
                        if (addition.hasOwnProperty(key)) {
                            source[key] = addition[key];
                        }
                    }
                }

                return source;
            };

            return lib;
        }
    );

    /**
     * mini-event
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 事件队列
     * @author otakustay
     */
    define(
        'mini-event/EventQueue',['require','./lib'],function (require) {
            var lib = require('./lib');

            /**
             * 判断已有的一个事件上下文对象是否和提供的参数等同
             *
             * @param {Object} context 在队列中已有的事件上下文对象
             * @param {Function | boolean} handler 处理函数，可以是`false`
             * @param {Mixed} [thisObject] 处理函数的`this`对象
             * @return {boolean}
             * @ignore
             */
            function isContextIdentical(context, handler, thisObject) {
                // `thisObject`为`null`和`undefined`时认为等同，所以用`==`
                return context
                    && context.handler === handler
                    && context.thisObject == thisObject;
            }

            /**
             * 事件队列
             *
             * @constructor
             */
            function EventQueue() {
                this.queue = [];
            }

            /**
             * 添加一个事件处理函数
             *
             * @param {Function | boolean} handler 处理函数，
             * 可以传递`false`作为特殊的处理函数，参考{@link EventTarget#on}
             * @param {Object} [options] 相关配置
             * @param {Mixed} [options.thisObject] 执行处理函数时的`this`对象
             * @param {boolean} [options.once=false] 设定函数仅执行一次
             */
            EventQueue.prototype.add = function (handler, options) {
                if (handler !== false && typeof handler !== 'function') {
                    throw new Error(
                        'event handler must be a function or const false');
                }

                var wrapper = {
                    handler: handler
                };
                lib.extend(wrapper, options);

                for (var i = 0; i < this.queue.length; i++) {
                    var context = this.queue[i];
                    // 同样的处理函数，不同的`this`对象，相当于外面`bind`了一把再添加，
                    // 此时认为这是完全不同的2个处理函数，但`null`和`undefined`认为是一样的
                    if (isContextIdentical(context, handler, wrapper.thisObject)) {
                        return;
                    }
                }

                this.queue.push(wrapper);
            };

            /**
             * 移除一个或全部处理函数
             *
             * @param {Function | boolean} [handler] 指定移除的处理函数，
             * 如不提供则移除全部处理函数，可以传递`false`作为特殊的处理函数
             * @param {Mixed} [thisObject] 指定函数对应的`this`对象，
             * 不提供则仅移除没有挂载`this`对象的那些处理函数
             */
            EventQueue.prototype.remove = function (handler, thisObject) {
                // 如果没提供`handler`，则直接清空
                if (!handler) {
                    this.clear();
                    return;
                }

                for (var i = 0; i < this.queue.length; i++) {
                    var context = this.queue[i];

                    if (isContextIdentical(context, handler, thisObject)) {
                        // 为了让`execute`过程中调用的`remove`工作正常，
                        // 这里不能用`splice`直接删除，仅设为`null`留下这个空间
                        this.queue[i] = null;

                        // 完全符合条件的处理函数在`add`时会去重，因此这里肯定只有一个
                        return;
                    }
                }
            };

            /**
             * 移除全部处理函数，如果队列执行时调用这个函数，会导致剩余的处理函数不再执行
             */
            EventQueue.prototype.clear = function () {
                this.queue.length = 0;
            };

            /**
             * 执行所有处理函数
             *
             * @param {Event} event 事件对象
             * @param {Mixed} thisObject 函数执行时的`this`对象
             */
            EventQueue.prototype.execute = function (event, thisObject) {
                // 如果执行过程中销毁，`dispose`会把`this.queue`弄掉，所以这里留一个引用，
                // 在`dispose`中会额外把数组清空，因此不用担心后续的函数会执行
                var queue = this.queue;
                for (var i = 0; i < queue.length; i++) {
                    if (typeof event.isImmediatePropagationStopped === 'function'
                        && event.isImmediatePropagationStopped()
                        ) {
                        return;
                    }

                    var context = queue[i];

                    // 移除事件时设置为`null`，因此可能无值
                    if (!context) {
                        continue;
                    }

                    var handler = context.handler;

                    // `false`等同于两个方法的调用
                    if (handler === false) {
                        if (typeof event.preventDefault === 'function') {
                            event.preventDefault();
                        }
                        if (typeof event.stopPropagation === 'function') {
                            event.stopPropagation();
                        }
                    }
                    else {
                        // 这里不需要做去重处理了，在`on`的时候会去重，因此这里不可能重复
                        handler.call(context.thisObject || thisObject, event);
                    }

                    if (context.once) {
                        this.remove(context.handler, context.thisObject);
                    }
                }
            };

            /**
             * 获取队列的长度
             *
             * @reutrn {number}
             */
            EventQueue.prototype.getLength = function () {
                var count = 0;
                for (var i = 0; i < this.queue.length; i++) {
                    if (this.queue[i]) {
                        count++;
                    }
                }
                return count;
            };

            /**
             * 获取队列的长度，与{@link EventQueue#getLength}相同
             *
             * @method
             * @reutrn {number}
             */
            EventQueue.prototype.length = EventQueue.prototype.getLength;

            /**
             * 销毁
             *
             * 如果在队列执行的过程中销毁了对象，则在对象销毁后，剩余的处理函数不会再执行了
             */
            EventQueue.prototype.dispose = function () {
                // 在执行过程中被销毁的情况下，这里`length`置为0，循环就走不下去了
                this.clear();
                this.queue = null;
            };

            return EventQueue;
        }
    );

    /**
     * mini-event
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 事件对象类
     * @author otakustay
     */
    define(
        'mini-event/Event',['require','./lib'],function (require) {
            var lib = require('./lib');

            function returnTrue() {
                return true;
            }
            function returnFalse() {
                return false;
            }

            function isObject(target) {
                return Object.prototype.toString.call(target) === '[object Object]';
            }

            /**
             * 事件对象类
             *
             * 3个重载：
             *      - `new Event(type)`
             *      - `new Event(args)`
             *      - `new Event(type, args)`
             * 只提供一个对象作为参数，则是`new Event(args)`的形式，需要加上type
             *
             * @constructor
             * @param {string | Mixed} [type] 事件类型
             * @param {Mixed} [args] 事件中的数据，如果此参数为一个对象，
             * 则将参数扩展到`Event`实例上。如果参数是非对象类型，则作为实例的`data`属性使用
             */
            function Event(type, args) {
                // 如果第1个参数是对象，则就当是`new Event(args)`形式
                if (typeof type === 'object') {
                    args = type;
                    type = args.type;
                }

                if (isObject(args)) {
                    lib.extend(this, args);
                }
                else if (args) {
                    this.data = args;
                }

                if (type) {
                    this.type = type;
                }
            }

            /**
             * 判断默认行为是否已被阻止
             *
             * @return {boolean}
             */
            Event.prototype.isDefaultPrevented = returnFalse;

            /**
             * 阻止默认行为
             */
            Event.prototype.preventDefault = function () {
                this.isDefaultPrevented = returnTrue;
            };

            /**
             * 判断事件传播是否已被阻止
             *
             * @return {boolean}
             */
            Event.prototype.isPropagationStopped = returnFalse;

            /**
             * 阻止事件传播
             */
            Event.prototype.stopPropagation = function () {
                this.isPropagationStopped = returnTrue;
            };

            /**
             * 判断事件的立即传播是否已被阻止
             *
             * @return {boolean}
             */
            Event.prototype.isImmediatePropagationStopped = returnFalse;

            /**
             * 立即阻止事件传播
             */
            Event.prototype.stopImmediatePropagation = function () {
                this.isImmediatePropagationStopped = returnTrue;

                this.stopPropagation();
            };

            var globalWindow = (function () {
                return this;
            }());

            /**
             * 从DOM事件对象生成一个Event对象
             *
             * @param {Event} domEvent DOM事件对象
             * @param {string} [type] 事件类型
             * @param {Mixed} [args] 事件数据
             * @return {Event}
             * @static
             */
            Event.fromDOMEvent = function (domEvent, type, args) {
                domEvent = domEvent || globalWindow.event;

                var event = new Event(type, args);

                event.preventDefault = function () {
                    if (domEvent.preventDefault) {
                        domEvent.preventDefault();
                    }
                    else {
                        domEvent.returnValue = false;
                    }

                    Event.prototype.preventDefault.call(this);
                };

                event.stopPropagation = function () {
                    if (domEvent.stopPropagation) {
                        domEvent.stopPropagation();
                    }
                    else {
                        domEvent.cancelBubble = true;
                    }

                    Event.prototype.stopPropagation.call(this);
                };

                event.stopImmediatePropagation = function () {
                    if (domEvent.stopImmediatePropagation) {
                        domEvent.stopImmediatePropagation();
                    }

                    Event.prototype.stopImmediatePropagation.call(this);
                };

                return event;
            };

            // 复制事件属性的时候不复制这几个
            var EVENT_PROPERTY_BLACK_LIST = {
                type: true, target: true,
                preventDefault: true, isDefaultPrevented: true,
                stopPropagation: true, isPropagationStopped: true,
                stopImmediatePropagation: true, isImmediatePropagationStopped: true
            };

            /**
             * 从一个已有事件对象生成一个新的事件对象
             *
             * @param {Event} originalEvent 作为源的已有事件对象
             * @param {Object} [options] 配置项
             * @param {string} [options.type] 新事件对象的类型，不提供则保留原类型
             * @param {boolean} [options.preserveData=false] 是否保留事件的信息
             * @param {boolean} [options.syncState=false] 是否让2个事件状态同步，
             * 状态包括 **阻止传播** 、 **立即阻止传播** 和 **阻止默认行为**
             * @param {Object} [options.extend] 提供事件对象的更多属性
             * @static
             */
            Event.fromEvent = function (originalEvent, options) {
                var defaults = {
                    type: originalEvent.type,
                    preserveData: false,
                    syncState: false
                };
                options = lib.extend(defaults, options);

                var newEvent = new Event(options.type);
                // 如果保留数据，则把数据复制过去
                if (options.preserveData) {
                    // 要去掉一些可能出现的杂质，因此不用`lib.extend`
                    for (var key in originalEvent) {
                        if (originalEvent.hasOwnProperty(key)
                            && !EVENT_PROPERTY_BLACK_LIST.hasOwnProperty(key)
                            ) {
                            newEvent[key] = originalEvent[key];
                        }
                    }
                }

                // 如果有扩展属性，加上去
                if (options.extend) {
                    lib.extend(newEvent, options.extend);
                }

                // 如果要同步状态，把和状态相关的方法挂接上
                if (options.syncState) {
                    newEvent.preventDefault = function () {
                        originalEvent.preventDefault();

                        Event.prototype.preventDefault.call(this);
                    };

                    newEvent.stopPropagation = function () {
                        originalEvent.stopPropagation();

                        Event.prototype.stopPropagation.call(this);
                    };

                    newEvent.stopImmediatePropagation = function () {
                        originalEvent.stopImmediatePropagation();

                        Event.prototype.stopImmediatePropagation.call(this);
                    };
                }

                return newEvent;
            };

            /**
             * 将一个对象的事件代理到另一个对象
             *
             * @param {EventTarget} from 事件提供方
             * @param {EventTarget | string} fromType 为字符串表示提供方事件类型；
             * 为可监听对象则表示接收方，此时事件类型由第3个参数提供
             * @param {EventTarget | string} to 为字符串则表示提供方和接收方事件类型一致，
             * 由此参数作为事件类型；为可监听对象则表示接收方，此时第2个参数必须为字符串
             * @param {string} [toType] 接收方的事件类型
             * @param {Object} [options] 配置项
             * @param {boolean} [options.preserveData=false] 是否保留事件的信息
             * @param {boolean} [options.syncState=false] 是否让2个事件状态同步，
             * 状态包括**阻止传播**、**立即阻止传播**和**阻止默认行为**
             * @param {Object} [options.extend] 提供事件对象的更多属性
             *
             *     // 当`label`触发`click`事件时，自身也触发`click`事件
             *     Event.delegate(label, this, 'click');
             *
             *     // 当`label`触发`click`事件时，自身触发`labelclick`事件
             *     Event.delegate(label, 'click', this, 'labelclick');
             * @static
             */
            Event.delegate = function (from, fromType, to, toType, options) {
                // 重载：
                //
                // 1. `.delegate(from, fromType, to, toType)`
                // 2. `.delegate(from, fromType, to, toType, options)`
                // 3. `.delegate(from, to, type)`
                // 4. `.delegate(from, to, type, options)

                // 重点在于第2个参数的类型，如果为字符串则肯定是1或2，否则为3或4
                var useDifferentType = typeof fromType === 'string';
                var source = {
                    object: from,
                    type: useDifferentType ? fromType : to
                };
                var target = {
                    object: useDifferentType ? to : fromType,
                    type: useDifferentType ? toType : to
                };
                var config = useDifferentType ? options : toType;
                config = lib.extend({ preserveData: false }, config);

                // 如果提供方不能注册事件，或接收方不能触发事件，那就不用玩了
                if (typeof source.object.on !== 'function'
                    || typeof target.object.on !== 'function'
                    || typeof target.object.fire !== 'function'
                    ) {
                    return;
                }

                var delegator = function (originalEvent) {
                    var event = Event.fromEvent(originalEvent, config);
                    // 修正`type`和`target`属性
                    event.type = target.type;
                    event.target = target.object;

                    target.object.fire(target.type, event);
                };

                source.object.on(source.type, delegator);
            };

            return Event;
        }
    );

    /**
     * mini-event
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 入口
     * @author otakustay
     */
    define(
        'mini-event/main',['require','./Event'],function (require) {
            var Event = require('./Event');

            /**
             * @class main
             * @singleton
             */
            return {
                /**
                 * 版本号
                 *
                 * @type {string}
                 */
                version: '1.0.2',

                /**
                 * {@link Event}类
                 *
                 * @type {Function}
                 */
                Event: Event,

                /**
                 * 参考{@link Event#fromDOMEvent}
                 */
                fromDOMEvent: Event.fromDOMEvent,

                /**
                 * 参考{@link Event#fromEvent}
                 */
                fromEvent: Event.fromEvent,

                /**
                 * 参考{@link Event#delegate}
                 */
                delegate: Event.delegate
            };
        }
    );

    define('mini-event', ['mini-event/main'], function (main) { return main; });

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file DOM事件相关辅助方法
     * @author otakustay
     */
    define(
        'esui/helper/event',['require','underscore','mini-event/EventQueue','../lib','mini-event'],function (require) {
            var DOM_EVENTS_KEY = '_esuiDOMEvent';
            var globalEvents = {
                window: {},
                document: {},
                documentElement: {},
                body: {}
            };

            var u = require('underscore');
            var EventQueue = require('mini-event/EventQueue');
            var lib = require('../lib');

            /**
             * @override Helper
             */
            var helper = {};

            function getGlobalEventPool(element) {
                if (element === window) {
                    return globalEvents.window;
                }
                if (element === document) {
                    return globalEvents.document;
                }
                if (element === document.documentElement) {
                    return globalEvents.documentElement;
                }
                if (element === document.body) {
                    return globalEvents.body;
                }

                return null;
            }

            function triggerGlobalDOMEvent(element, e) {
                var pool = getGlobalEventPool(element);
                if (!pool) {
                    return;
                }

                var queue = pool[e.type];

                if (!queue) {
                    return;
                }

                u.each(
                    queue,
                    function (control) {
                        triggerDOMEvent(control, element, e);
                    }
                );
            }

            // 事件模块专用，无通用性
            function debounce(fn, interval) {
                interval = interval || 150;

                var timer = 0;

                return function (e) {
                    clearTimeout(timer);
                    var self = this;
                    e = e || window.event;
                    e = {
                        type: e.type,
                        srcElement: e.srcElement,
                        target: e.target,
                        currentTarget: e.currentTarget
                    };
                    timer = setTimeout(
                        function () { fn.call(self, e); },
                        interval
                    );
                };
            }

            function addGlobalDOMEvent(control, type, element) {
                var pool = getGlobalEventPool(element);

                if (!pool) {
                    return false;
                }

                var controls = pool[type];
                if (!controls) {
                    controls = pool[type] = [];
                    var handler = u.partial(triggerGlobalDOMEvent, element);
                    if (type === 'resize' || type === 'scroll') {
                        handler = debounce(handler);
                    }
                    controls.handler = handler;
                    lib.on(element, type, controls.handler);
                }

                if (u.indexOf(controls, control) >= 0) {
                    return;
                }

                controls.push(control);
                return true;
            }

            function removeGlobalDOMEvent(control, type, element) {
                var pool = getGlobalEventPool(element);

                if (!pool) {
                    return false;
                }

                if (!pool[type]) {
                    return true;
                }

                var controls = pool[type];
                for (var i = 0; i < controls.length; i++) {
                    if (controls[i] === control) {
                        controls.splice(i, 1);
                        break;
                    }
                }
                // 尽早移除事件
                if (!controls.length) {
                    var handler = controls.handler;
                    lib.un(element, type, handler);
                    pool[type] = null;
                }

                return true;
            }

            function triggerDOMEvent(control, element, e) {
                e = e || window.event;

                // 每个控件都能在某些状态下不处理DOM事件
                if (!control) {
                    return;
                }

                var isInIgnoringState = u.any(
                    control.ignoreStates,
                    function (state) {
                        return control.hasState(state);
                    }
                );
                
                if (isInIgnoringState) {
                    return;
                }

                if (!e.target) {
                    e.target = e.srcElement;
                }
                if (!e.currentTarget) {
                    e.currentTarget = element;
                }
                if (!e.preventDefault) {
                    e.preventDefault = function () {
                        e.returnValue = false;
                    };
                }
                if (!e.stopPropagation) {
                    e.stopPropagation = function () {
                        e.cancelBubble = true;
                    };
                }
                var queue =
                    control.domEvents[e.currentTarget[DOM_EVENTS_KEY]][e.type];

                if (!queue) {
                    return;
                }

                queue.execute(e, control);
            }

            /**
             * 为控件管理的DOM元素添加DOM事件
             *
             * 通过本方法添加的DOM事件处理函数，会进行以下额外的处理：
             *
             * - 修正`target`和`currentTarget`属性使其保持与标准兼容
             * - 修正`preventDefault`和`stopPropagation`方法使其保持与标准兼容
             * - 函数中的`this`对象永远指向当前控件实例
             * - 当控件处于由其{@link Control#ignoreStates}属性定义的状态时，不执行函数
             *
             * @param {HTMLElement | string} element 需要添加事件的DOM元素或部件名称
             * @param {string} type 事件的类型
             * @param {Function} handler 事件处理函数
             */
            helper.addDOMEvent = function (element, type, handler) {
                if (typeof element === 'string') {
                    element = this.getPart(element);
                }

                if (!this.control.domEvents) {
                    this.control.domEvents = {};
                }

                var guid = element[DOM_EVENTS_KEY];
                if (!guid) {
                    guid = element[DOM_EVENTS_KEY] = lib.getGUID();
                }

                var events = this.control.domEvents[guid];
                if (!events) {
                    // `events`中的键都是事件的名称，仅`element`除外，
                    // 因为DOM上没有`element`这个事件，所以这里占用一下没关系
                    events = this.control.domEvents[guid] = { element: element };
                }

                var isGlobal = addGlobalDOMEvent(this.control, type, element);
                var queue = events[type];
                if (!queue) {
                    queue = events[type] = new EventQueue();
                    // 非全局事件是需要自己管理一个处理函数的，以便到时候解除事件绑定
                    if (!isGlobal) {
                        // 无论注册多少个处理函数，其实在DOM元素上只有一个函数，
                        // 这个函数负责执行队列中的所有函数，
                        // 这样能保证执行的顺序，移除注册时也很方便
                        queue.handler =
                            u.partial(triggerDOMEvent, this.control, element);
                        lib.on(element, type, queue.handler);
                    }
                }

                queue.add(handler);
            };

            /**
             * 代理DOM元素的事件为自身的事件
             *
             * @param {HTMLElement | string} element 需要添加事件的DOM元素或部件名称
             * @param {string} type 需要代理的DOM事件类型
             * @param {string} [newType] 代理时触发的事件，默认与`type`一致
             */
            helper.delegateDOMEvent = function (element, type, newType) {
                var handler = function (e) {
                    var event = require('mini-event').fromDOMEvent(e);
                    this.fire(newType || e.type, event);

                    if (event.isDefaultPrevented()) {
                        lib.event.preventDefault(e);
                    }

                    if (event.isPropagationStopped()) {
                        lib.event.stopPropagation(e);
                    }
                };

                this.addDOMEvent(element, type, handler);
            };

            /**
             * 为控件管理的DOM元素添加DOM事件
             *
             * @param {HTMLElement | string} element 需要添加事件的DOM元素或部件名称
             * @param {string} type 事件的类型
             * @param {Function} [handler] 事件处理函数，不提供则清除所有处理函数
             */
            helper.removeDOMEvent = function (element, type, handler) {
                if (typeof element === 'string') {
                    element = this.getPart(element);
                }

                if (!this.control.domEvents) {
                    return;
                }

                var guid = element[DOM_EVENTS_KEY];
                var events = this.control.domEvents[guid];

                if (!events || !events[type]) {
                    return;
                }

                if (!handler) {
                    events[type].clear();
                }
                else {
                    var queue = events[type];
                    queue.remove(handler);

                    // 全局元素上的事件很容易冒泡到后执行，
                    // 在上面的又都是`mousemove`这种不停执行的，
                    // 因此对全局事件做一下处理，尽早移除
                    if (!queue.getLength()) {
                        removeGlobalDOMEvent(this.control, type, element);
                    }
                }
            };

            /**
             * 清除控件管理的DOM元素上的事件
             *
             * @param {HTMLElement | string} [element] 控件管理的DOM元素或部件名称，
             * 如不提供则去除所有该控件管理的元素的DOM事件
             */
            helper.clearDOMEvents = function (element) {
                if (typeof element === 'string') {
                    element = this.getPart(element);
                }

                if (!this.control.domEvents) {
                    return;
                }

                if (!element) {
                    // 在循环中直接删除一个属性不知道会发生什么（已知浏览器看上去没问题），
                    // 因此先拿到所有的元素然后再做遍历更安全，虽然是2次循环但能有多少个对象
                    u.each(
                        u.pluck(this.control.domEvents, 'element'),
                        this.clearDOMEvents,
                        this
                    );
                    this.control.domEvents = null;
                    return;
                }

                var guid = element[DOM_EVENTS_KEY];
                var events = this.control.domEvents[guid];

                // `events`中存放着各事件类型，只有`element`属性是一个DOM对象，
                // 因此要删除`element`这个键，
                // 以避免`for... in`的时候碰到一个不是数组类型的值
                delete events.element;
                u.each(
                    events,
                    function (queue, type) {
                        // 全局事件只要清掉在`globalEvents`那边的注册关系
                        var isGlobal =
                            removeGlobalDOMEvent(this.control, type, element);
                        if (!isGlobal) {
                            var handler = queue.handler;
                            queue.dispose();
                            queue.handler = null; // 防内存泄露
                            lib.un(element, type, handler);
                        }
                    },
                    this
                );

                delete this.control.domEvents[guid];
            };

            return helper;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 生成HTML相关的辅助方法
     * @author otakustay
     */
    define(
        'esui/helper/html',['require'],function (require) {
            /**
             * @override Helper
             */
            var helper = {};

            // 自闭合的标签列表
            var SELF_CLOSING_TAGS = {
                area: true, base: true, br: true, col: true,
                embed: true, hr: true, img: true, input: true,
                keygen: true, link: true, meta: true, param: true,
                source: true, track: true, wbr: true
            };

            /**
             * 获取部件的起始标签
             *
             * @param {string} part 部件名称
             * @param {string} nodeName 部件使用的元素类型
             * @return {string}
             */
            helper.getPartBeginTag = function (part, nodeName) {
                var html = '<' + nodeName + ' id="' + this.getId(part) + '" '
                    + 'class="' + this.getPartClassName(part) + '">';
                return html;
            };

            /**
             * 获取部件的结束标签
             *
             * @param {string} part 部件名称
             * @param {string} nodeName 部件使用的元素类型
             * @return {string}
             */
            helper.getPartEndTag = function (part, nodeName) {
                var html = SELF_CLOSING_TAGS.hasOwnProperty(nodeName)
                    ? ' />'
                    : '</' + nodeName + '>';
                return html;
            };

            /**
             * 获取部件的HTML模板
             *
             * @param {string} part 部件名称
             * @param {string} nodeName 部件使用的元素类型
             * @return {string}
             */
            helper.getPartHTML = function (part, nodeName) {
                return this.getPartBeginTag(part, nodeName)
                    + this.getPartEndTag(part, nodeName);
            };

            return helper;
        }
    );
    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 生命周期相关辅助方法
     * @author otakustay
     */
    define(
        'esui/helper/life',['require','underscore','../main'],function (require) {
            /**
             * LifeCycle枚举
             *
             * @type {Object}
             * @ignore
             */
            var LifeCycle = {
                NEW: 0,
                INITED: 1,
                RENDERED: 2,
                DISPOSED: 4
            };

            var u = require('underscore');
            var ui = require('../main');

            /**
             * @override Helper
             */
            var helper = {};

            /**
             * 初始化控件视图环境
             */
            helper.initViewContext = function () {
                var viewContext = this.control.viewContext || ui.getViewContext();

                // 因为`setViewContext`里有判断传入的`viewContext`和自身的是否相等，
                // 这里必须制造出**不相等**的情况，再调用`setViewContext`
                this.control.viewContext = null;
                this.control.setViewContext(viewContext);
            };

            /**
             * 初始化控件扩展
             */
            helper.initExtensions = function () {
                // 附加全局扩展
                var extensions = this.control.extensions;
                if (!u.isArray(extensions)) {
                    extensions = this.control.extensions = [];
                }
                Array.prototype.push.apply(
                    extensions,
                    ui.createGlobalExtensions()
                );

                // 同类型扩展去重
                var registeredExtensions = {};
                for (var i = 0; i < extensions.length; i++) {
                    var extension = extensions[i];
                    if (!registeredExtensions[extension.type]) {
                        extension.attachTo(this.control);
                        registeredExtensions[extension.type] = true;
                    }
                }
            };

            /**
             * 判断控件是否处于相应的生命周期阶段
             *
             * @param {string} stage 生命周期阶段
             * @return {boolean}
             */
            helper.isInStage = function (stage) {
                if (LifeCycle[stage] == null) {
                    throw new Error('Invalid life cycle stage: ' + stage);
                }

                return this.control.stage === LifeCycle[stage];
            };

            /**
             * 改变控件的生命周期阶段
             *
             * @param {string} stage 生命周期阶段
             */
            helper.changeStage = function (stage) {
                if (LifeCycle[stage] == null) {
                    throw new Error('Invalid life cycle stage: ' + stage);
                }

                this.control.stage = LifeCycle[stage];
            };

            /**
             * 销毁控件
             */
            helper.dispose = function () {
                // 清理子控件
                this.control.disposeChildren();
                this.control.children = null;
                this.control.childrenIndex = null;

                // 移除自身行为
                this.clearDOMEvents();

                // 移除所有扩展
                u.invoke(this.control.extensions, 'dispose');
                this.control.extensions = null;

                // 从控件树中移除
                if (this.control.parent) {
                    this.control.parent.removeChild(this.control);
                }

                // 从视图环境移除
                if (this.control.viewContext) {
                    this.control.viewContext.remove(this.control);
                }

                this.control.renderOptions = null;
            };

            /**
             * 执行控件销毁前动作
             */
            helper.beforeDispose = function () {
                /**
                 * @event beforedispose
                 *
                 * 在销毁前触发
                 *
                 * @member Control
                 */
                this.control.fire('beforedispose');
            };

            /**
             * 执行控件销毁后动作
             */
            helper.afterDispose = function () {
                this.changeStage('DISPOSED');
                /**
                 * @event afterdispose
                 *
                 * 在销毁后触发
                 *
                 * @member Control
                 */
                this.control.fire('afterdispose');

                // 销毁所有事件，这个必须在`afterdispose`事件之后，不然事件等于没用
                this.control.destroyEvents();
            };

            return helper;
        }
    );

    /**
     * ESUI (Enterprise Simple UI library)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 模板相关辅助方法
     * @author otakustay
     */
    define(
        'esui/helper/template',['require','underscore'],function (require) {
            var u = require('underscore');

            var FILTERS = {
                'id': function (part, instance) {
                    return instance.helper.getId(part);
                },

                'class': function (part, instance) {
                    return instance.helper.getPartClassName(part);
                },

                'part': function (part, nodeName, instance) {
                    return instance.helper.getPartHTML(part, nodeName);
                }
            };

            /**
             * @override Helper
             */
            var helper = {};

            /**
             * 设置模板引擎实例
             *
             * @param {etpl.Engine} engine 模板引擎实例
             */
            helper.setTemplateEngine = function (engine) {
                this.templateEngine = engine;

                if (!engine.esui) {
                    this.initializeTemplateEngineExtension();
                }
            };

            /**
             * 初始化模板引擎的扩展，添加对应的过滤器
             *
             * @protected
             */
            helper.initializeTemplateEngineExtension = function () {
                u.each(
                    FILTERS,
                    function (filter, name) {
                        this.addFilter(name, filter);
                    },
                    this.templateEngine
                );
            };

            /**
             * 通过模板引擎渲染得到字符串
             *
             * ESUI为[etpl](https://github.com/ecomfe/etpl')提供了额外的
             * [filter](https://github.com/ecomfe/etpl/#变量替换)：
             *
             * - `${xxx | id($instance)}`按规则生成以`xxx`为部件名的DOM元素id
             * - `${xxx | class($instance)}`按规则生成以`xxx`为部件名的DOM元素class
             * - `${xxx | part('div', $instance)}`生成以`xxx`为部件名的div元素HTML
             *
             * 在使用内置过滤器时，必须加上`($instance)`这一段，以将过滤器和当前控件实例关联
             *
             * 同时也可以用简化的方式使用以上的过滤器，如：
             *
             * - `${xxx.id}`等效于`${xxx | id($instance)}`
             * - `${xxx.class}`等效于`${xxx | class($instance)}`
             *
             * 需要注意`part`过滤器需要指定`nodeName`，因此不能使用以上方法简写，
             * 必须使用过滤器的语法实现
             *
             * 一般来说，如果一个控件需要使用模板，我们会为这个控件类生成一个模板引擎实例：
             *
             *     var engine = new require('etpl').Engine();
             *     // 可使用text插件来加载模板文本
             *     engine.parse(require('text!myControl.tpl.html'));
             *
             *     // 声明控件类
             *     function MyControl() {
         *         ...
         *
         * 注意模板引擎实例是一个 **控件类** 一个，而非每个实例一个。
         * 由于引擎实例的隔离，在模板中不需要对`target`命名使用前缀等方式进行防冲突处理
         * 但是如果在项目发布的过程中涉及到了模板合并的工作，如果`target`重名了，可能存在问题
         * 因此如果一个 **控件类** 使用了模板，那么在项目发布的过程中需要注意选择合适的策略来
         * 合并模板：
         *
         * - 将模板的代码内嵌到js中
         * - 所有的模板合并为一个，然后统一通过插件来加载
         *
         * 随后在控件的构造函数中，为{@link Helper}添加模板引擎实现：
             *
             *     function MyClass() {
         *         // 调用基类构造函数
         *         Control.apply(this, arguments);
         *
         *         // 设置模板引擎实例
         *         this.helper.setTemplateEngine(engine);
         *     }
             *
             * 在控件的实现中，即可使用本方法输出HTML：
             *
             *     MyClass.prototype.initStructure = function () {
         *         this.main.innerHTML = 
         *             this.helper.renderTemplate('content', data);
         *     }
             *
             * 需要注意，使用此方法时，仅满足以下所有条件时，才可以使用内置的过滤器：
             *
             * - `data`对象仅一层属性，即不能使用`${user.name}`这一形式访问深层的属性
             * - `data`对象不能包含名为`instance`的属性，此属性会强制失效
             *
             * 另外此方法存在微小的几乎可忽略不计的性能损失，
             * 但如果需要大量使用模板同时又不需要内置的过滤器，可以使用以下代码代替：
             *
             *     this.helper.templateEngine.render(target, data);
             *
             * @param {string} target 模板名
             * @param {Object} [data] 用于模板渲染的数据
             * @return {string}
             */
            helper.renderTemplate = function (target, data) {
                var helper = this;
                data = data || {};

                var templateData = {
                    get: function (name) {
                        if (name === 'instance') {
                            return helper.control;
                        }

                        if (typeof data.get === 'function') {
                            return data.get(name);
                        }

                        var propertyName = name;
                        var filter = null;

                        var indexOfDot = name.lastIndexOf('.');
                        if (indexOfDot > 0) {
                            propertyName = name.substring(0, indexOfDot);
                            var filterName = name.substring(indexOfDot + 1);
                            if (filterName && FILTERS.hasOwnProperty(filterName)) {
                                filter = FILTERS[filterName];
                            }
                        }

                        var value = data.hasOwnProperty(propertyName)
                            ? data[propertyName]
                            : propertyName;
                        if (filter) {
                            value = filter(value, helper.control);
                        }

                        return value;
                    }
                };

                if (!this.templateEngine) {
                    throw new Error('No template engine attached to this control');
                }

                return this.templateEngine.render(target, templateData);
            };

            return helper;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 控件类常用的方法辅助类
     * @author otakustay
     */
    define(
        'esui/Helper',['require','underscore','./helper/children','./helper/dom','./helper/event','./helper/html','./helper/life','./helper/template'],function (require) {
            var u = require('underscore');

            /**
             * 控件辅助类
             *
             * @constructor
             * @param {Control} control 关联的控件实例
             */
            function Helper(control) {
                this.control = control;
            }

            u.extend(
                Helper.prototype,
                require('./helper/children'),
                require('./helper/dom'),
                require('./helper/event'),
                require('./helper/html'),
                require('./helper/life'),
                require('./helper/template')
            );

            return Helper;
        }
    );

    /**
     * ER (Enterprise RIA)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 提供事件相关操作的基类
     * @author otakustay
     */
    define(
        'mini-event/EventTarget',['require','./lib','./Event','./EventQueue'],function (require) {
            var lib = require('./lib');
            var Event = require('./Event');
            var EventQueue = require('./EventQueue');

            /**
             * 提供事件相关操作的基类
             *
             * 可以让某个类继承此类，获得事件的相关功能：
             *
             *     function MyClass() {
         *         // 此处可以不调用EventTarget构造函数
         *     }
             *
             *     inherits(MyClass, EventTarget);
             *
             *     var instance = new MyClass();
             *     instance.on('foo', executeFoo);
             *     instance.fire('foo', { bar: 'Hello World' });
             *
             * 当然也可以使用`Object.create`方法：
             *
             *     var instance = Object.create(EventTarget.prototype);
             *     instance.on('foo', executeFoo);
             *     instance.fire('foo', { bar: 'Hello World' });
             *
             * 还可以使用`enable`方法让一个静态的对象拥有事件功能：
             *
             *     var instance = {};
             *     EventTarget.enable(instance);
             *
             *     // 同样可以使用事件
             *     instance.on('foo', executeFoo);
             *     instance.fire('foo', { bar: 'Hello World' });
             *
             * @constructor
             */
            function EventTarget() {
            }

            /**
             * 注册一个事件处理函数
             *
             * @param {string} type 事件的类型
             * @param {Function | boolean} fn 事件的处理函数，
             * 特殊地，如果此参数为`false`，将被视为特殊的事件处理函数，
             * 其效果等于`preventDefault()`及`stopPropagation()`
             * @param {Mixed} [thisObject] 事件执行时`this`对象
             * @param {Object} [options] 事件相关配置项
             * @param {boolean} [options.once=false] 控制事件仅执行一次
             */
            EventTarget.prototype.on = function (type, fn, thisObject, options) {
                if (!this.miniEventPool) {
                    this.miniEventPool = {};
                }

                if (!this.miniEventPool.hasOwnProperty(type)) {
                    this.miniEventPool[type] = new EventQueue();
                }

                var queue = this.miniEventPool[type];

                options = lib.extend({}, options);
                if (thisObject) {
                    options.thisObject = thisObject;
                }

                queue.add(fn, options);
            };

            /**
             * 注册一个仅执行一次的处理函数
             *
             * @param {string} type 事件的类型
             * @param {Function} fn 事件的处理函数
             * @param {Mixed} [thisObject] 事件执行时`this`对象
             * @param {Object} [options] 事件相关配置项
             */
            EventTarget.prototype.once = function (type, fn, thisObject, options) {
                options = lib.extend({}, options);
                options.once = true;
                this.on(type, fn, thisObject, options);
            };

            /**
             * 注销一个事件处理函数
             *
             * @param {string} type 事件的类型，
             * 如果值为`*`仅会注销通过`*`为类型注册的事件，并不会将所有事件注销
             * @param {Function} [handler] 事件的处理函数，
             * 无此参数则注销`type`指定类型的所有事件处理函数
             * @param {Mixed} [thisObject] 处理函数对应的`this`对象，
             * 无此参数则注销`type`与`handler`符合要求，且未挂载`this`对象的处理函数
             */
            EventTarget.prototype.un = function (type, handler, thisObject) {
                if (!this.miniEventPool
                    || !this.miniEventPool.hasOwnProperty(type)
                    ) {
                    return;
                }

                var queue = this.miniEventPool[type];
                queue.remove(handler, thisObject);
            };

            /**
             * 触发指定类型的事件
             *
             * 3个重载：
             *
             * - `.fire(type)`
             * - `.fire(args)`
             * - `.fire(type, args)`
             *
             * @param {string | Mixed} type 事件类型
             * @param {Mixed} [args] 事件对象
             * @return {Event} 事件传递过程中的`Event`对象
             */
            EventTarget.prototype.fire = function (type, args) {
                // 只提供一个对象作为参数，则是`.fire(args)`的形式，需要加上type
                if (arguments.length === 1 && typeof type === 'object') {
                    args = type;
                    type = args.type;
                }

                if (!type) {
                    throw new Error('No event type specified');
                }

                if (type === '*') {
                    throw new Error('Cannot fire global event');
                }

                var event = args instanceof Event
                    ? args
                    : new Event(type, args);
                event.target = this;

                // 无论`this.miniEventPool`有没有被初始化，
                // 如果有直接挂在对象上的方法是要触发的
                var inlineHandler = this['on' + type];
                if (typeof inlineHandler === 'function') {
                    inlineHandler.call(this, event);
                }

                // 在此处可能没有`miniEventPool`，这是指对象整个就没初始化，
                // 即一个事件也没注册过就`fire`了，这是正常现象
                if (this.miniEventPool && this.miniEventPool.hasOwnProperty(type)) {
                    var queue = this.miniEventPool[type];
                    queue.execute(event, this);
                }

                // 同时也有可能在上面执行标准事件队列的时候，把这个`EventTarget`给销毁了，
                // 此时`miniEventPool`就没了，这种情况是正常的不能抛异常，要特别处理
                if (this.miniEventPool && this.miniEventPool.hasOwnProperty('*')) {
                    var globalQueue = this.miniEventPool['*'];
                    globalQueue.execute(event, this);
                }

                return event;
            };

            /**
             * 销毁所有事件
             */
            EventTarget.prototype.destroyEvents = function () {
                if (!this.miniEventPool) {
                    return;
                }

                for (var name in this.miniEventPool) {
                    if (this.miniEventPool.hasOwnProperty(name)) {
                        this.miniEventPool[name].dispose();
                    }
                }

                this.miniEventPool = null;
            };

            /**
             * 在无继承关系的情况下，使一个对象拥有事件处理的功能
             *
             * @param {Mixed} target 需要支持事件处理功能的对象
             * @static
             */
            EventTarget.enable = function (target) {
                target.miniEventPool = {};
                lib.extend(target, EventTarget.prototype);
            };

            return EventTarget;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 控件基类模块
     * @author erik, otakustay
     */
    define(
        'esui/Control',['require','./lib','underscore','./main','./Helper','./SafeWrapper','mini-event/EventTarget'],function (require) {
            var lib = require('./lib');
            var u = require('underscore');
            var ui = require('./main');
            var Helper = require('./Helper');

            /**
             * 控件基类
             *
             * @constructor
             * @extends {mini-event.EventTarget}
             * @param {Object} [options] 初始化参数
             * @fires init
             */
            function Control(options) {
                options = options || {};

                /**
                 * 控件关联的{@link Helper}对象
                 *
                 * @type {Helper}
                 * @protected
                 */
                this.helper = new Helper(this);

                this.helper.changeStage('NEW');

                /**
                 * 子控件数组
                 *
                 * @type {Control[]}
                 * @protected
                 * @readonly
                 */
                this.children = [];
                this.childrenIndex = {};
                this.currentStates = {};
                this.domEvents = {};

                /**
                 * 控件的主元素
                 *
                 * @type {HTMLElement}
                 * @protected
                 * @readonly
                 */
                this.main = options.main ? options.main : this.createMain(options);

                // 如果没给id，自己创建一个，
                // 这个有可能在后续的`initOptions`中被重写，则会在`setProperties`中处理，
                // 这个不能放到`initOptions`的后面，
                // 不然会导致很多个没有id的控件放到一个`ViewContext`中，
                // 会把其它控件的`ViewContext`给冲掉导致各种错误

                /**
                 * 控件的id，在一个{@link ViewContext}中不能重复
                 *
                 * @property {string} id
                 * @readonly
                 */
                if (!this.id && !options.id) {
                    this.id = lib.getGUID();
                }

                this.initOptions(options);

                // 初始化视图环境
                this.helper.initViewContext();

                // 初始化扩展
                this.helper.initExtensions();

                // 切换控件所属生命周期阶段
                this.helper.changeStage('INITED');

                /**
                 * @event init
                 *
                 * 完成初始化
                 */
                this.fire('init');
            }

            /**
             * @property {string} type
             *
             * 控件的类型
             * @readonly
             */

            /**
             * @property {string} skin
             *
             * 控件皮肤，仅在初始化时设置有效，运行时不得变更
             *
             * @protected
             * @readonly
             */

            /**
             * @property {string} styleType
             *
             * 控件的样式类型，用于生成各class使用
             *
             * 如无此属性，则使用{@link Control#type}属性代替
             *
             * @readonly
             */

            Control.prototype = {
                constructor: Control,

                /**
                 * 指定在哪些状态下该元素不处理相关的DOM事件
                 *
                 * @type {string[]}
                 * @protected
                 */
                ignoreStates: ['disabled'],

                /**
                 * 获取控件的分类
                 *
                 * 控件分类的作用如下：
                 *
                 * - `control`表示普通控件，没有任何特征
                 * - `input`表示输入控件，在表单中使用`getRawValue()`获取其值
                 * - `check`表示复选控件，在表单中通过`isChecked()`判断其值是否加入结果中
                 *
                 * @return {string} 可以为`control`、`input`或`check`
                 */
                getCategory: function () {
                    return 'control';
                },

                /**
                 * 初始化控件需要使用的选项
                 *
                 * @param {Object} [options] 构造函数传入的选项
                 * @protected
                 */
                initOptions: function (options) {
                    options = options || {};
                    this.setProperties(options);
                },

                /**
                 * 创建控件主元素
                 *
                 * @return {HTMLElement}
                 * @protected
                 */
                createMain: function () {
                    if (!this.type) {
                        return document.createElement('div');
                    }

                    var name = this.type.replace(
                        /([A-Z])/g,
                        function (match, ch) { return '-' + ch.toLowerCase(); }
                    );
                    return document.createElement(ui.getConfig('customElementPrefix') + '-' + name.slice(1));
                },

                /**
                 * 初始化DOM结构，仅在第一次渲染时调用
                 *
                 * @protected
                 * @abstract
                 */
                initStructure: function () {
                },

                /**
                 * 初始化与DOM元素、子控件等的事件交互，仅在第一次渲染时调用
                 *
                 * @protected
                 * @abstract
                 */
                initEvents: function () {
                },

                /**
                 * 渲染控件
                 *
                 * @fires beforerender
                 * @fires afterrender
                 */
                render: function () {
                    if (this.helper.isInStage('INITED')) {
                        /**
                         * @event beforerender
                         *
                         * 开始初次渲染
                         */
                        this.fire('beforerender');

                        this.domIDPrefix = this.viewContext.id;

                        this.initStructure();
                        this.initEvents();

                        // 为控件主元素添加id
                        if (!this.main.id) {
                            this.main.id = this.helper.getId();
                        }

                        // 为控件主元素添加控件实例标识属性
                        this.main.setAttribute(
                            ui.getConfig('instanceAttr'),
                            this.id
                        );
                        this.main.setAttribute(
                            ui.getConfig('viewContextAttr'),
                            this.viewContext.id
                        );

                        this.helper.addPartClasses();

                        if (this.states) {
                            this.states = typeof this.states === 'string'
                                ? this.states.split(' ')
                                : this.states;

                            u.each(this.states, this.addState, this);
                        }
                    }

                    // 由子控件实现
                    this.repaint();

                    if (this.helper.isInStage('INITED')) {
                        // 切换控件所属生命周期阶段
                        this.helper.changeStage('RENDERED');

                        /**
                         * @event afterrender
                         *
                         * 结束初次渲染
                         */
                        this.fire('afterrender');
                    }
                },

                /**
                 * 重新渲染视图
                 *
                 * 仅当生命周期处于RENDER时，该方法才重新渲染
                 *
                 * 本方法的2个参数中的值均为 **属性变更对象** ，一个该对象包含以下属性：
                 *
                 * - `name`：属性名
                 * - `oldValue`：变更前的值
                 * - `newValue`：变更后的值
                 *
                 * @param {Object[]} [changes] 变更过的属性的集合
                 * @param {Object} [changesIndex] 变更过的属性的索引
                 * @protected
                 */
                repaint: function (changes, changesIndex) {
                    if (!changesIndex
                        || changesIndex.hasOwnProperty('disabled')
                        ) {
                        var method = this.disabled ? 'addState' : 'removeState';
                        this[method]('disabled');
                    }
                    if (!changesIndex || changesIndex.hasOwnProperty('hidden')) {
                        var method = this.hidden ? 'addState' : 'removeState';
                        this[method]('hidden');
                    }
                },

                /**
                 * 将控件添加到页面的某个元素中
                 *
                 * @param {HTMLElement | Control} wrap 控件要添加到的目标元素
                 */
                appendTo: function (wrap) {
                    if (wrap instanceof Control) {
                        wrap = wrap.main;
                    }

                    wrap.appendChild(this.main);
                    if (this.helper.isInStage('NEW')
                        || this.helper.isInStage('INITED')
                        ) {
                        this.render();
                    }
                },

                /**
                 * 将控件添加到页面的某个元素之前
                 *
                 * @param {HTMLElement | Control} reference 控件要添加到之前的目标元素
                 */
                insertBefore: function (reference) {
                    if (reference instanceof Control) {
                        reference = reference.main;
                    }

                    reference.parentNode.insertBefore(this.main, reference);
                    if (this.helper.isInStage('NEW')
                        || this.helper.isInStage('INITED')
                        ) {
                        this.render();
                    }
                },

                /**
                 * 销毁释放控件
                 *
                 * @fires beforedispose
                 * @fires afterdispose
                 */
                dispose: function () {
                    if (!this.helper.isInStage('DISPOSED')) {
                        this.helper.beforeDispose();
                        this.helper.dispose();
                        this.helper.afterDispose();
                    }
                },

                /**
                 * 销毁控件并移除所有DOM元素
                 *
                 * @fires beforedispose
                 * @fires afterdispose
                 */
                destroy: function () {
                    // 为了避免`dispose()`的时候把`main`置空了，这里先留存一个
                    var main = this.main;
                    this.dispose();
                    lib.removeNode(main);
                },

                /**
                 * 获取控件的属性值
                 *
                 * @param {string} name 属性名
                 * @return {Mixed}
                 */
                get: function (name) {
                    var method = this['get' + lib.pascalize(name)];

                    if (typeof method === 'function') {
                        return method.call(this);
                    }

                    return this[name];
                },

                /**
                 * 设置控件的属性值
                 *
                 * @param {string} name 属性名
                 * @param {Mixed} value 属性值
                 */
                set: function (name, value) {
                    var method = this['set' + lib.pascalize(name)];

                    if (typeof method === 'function') {
                        return method.call(this, value);
                    }

                    var property = {};
                    property[name] = value;
                    this.setProperties(property);
                },

                /**
                 * 判断属性新值是否有变化，内部用于`setProperties`方法
                 *
                 * @param {string} propertyName 属性名称
                 * @param {Mixed} newValue 新值
                 * @param {Mixed} oldValue 旧值
                 * @return {boolean}
                 * @protected
                 */
                isPropertyChanged: function (propertyName, newValue, oldValue) {
                    // 默认实现将值和当前新值进行简单比对
                    return oldValue !== newValue;
                },

                /**
                 * 批量设置控件的属性值
                 *
                 * @param {Object} properties 属性值集合
                 * @return {Object} `properties`参数中确实变更了的那些属性
                 */
                setProperties: function (properties) {
                    // 只有在渲染以前（就是`initOptions`调用的那次）才允许设置id
                    if (!this.stage) {
                        if (properties.hasOwnProperty('id')) {
                            this.id = properties.id;
                        }

                        if (properties.hasOwnProperty('group')) {
                            this.group = properties.group;
                        }

                        if (properties.hasOwnProperty('skin')) {
                            this.skin = properties.skin;
                        }
                    }

                    delete properties.id;
                    delete properties.group;
                    delete properties.skin;

                    // 吞掉`viewContext`的设置，逻辑都在`setViewContext`中
                    if (properties.hasOwnProperty('viewContext')) {
                        this.setViewContext(properties.viewContext);
                        delete properties.viewContext;
                    }

                    // 几个状态选项是要转为`boolean`的
                    if (this.hasOwnProperty('disabled')) {
                        this.disabled = !!this.disabled;
                    }
                    if (this.hasOwnProperty('hidden')) {
                        this.hidden = !!this.hidden;
                    }

                    var changes = [];
                    var changesIndex = {};
                    for (var key in properties) {
                        if (properties.hasOwnProperty(key)) {
                            var newValue = properties[key];
                            var getterMethodName =
                                'get' + lib.pascalize(key) + 'Property';
                            var oldValue = this[getterMethodName]
                                ? this[getterMethodName]()
                                : this[key];

                            var isChanged =
                                this.isPropertyChanged(key, newValue, oldValue);
                            if (isChanged) {
                                this[key] = newValue;
                                var record = {
                                    name: key,
                                    oldValue: oldValue,
                                    newValue: newValue
                                };
                                changes.push(record);
                                changesIndex[key] = record;
                            }
                        }
                    }

                    if (changes.length && this.helper.isInStage('RENDERED')) {
                        this.repaint(changes, changesIndex);
                    }

                    return changesIndex;

                },

                /**
                 * 设置控件的所属视图环境
                 *
                 * @param {ViewContext} viewContext 视图环境
                 */
                setViewContext: function (viewContext) {
                    // 为了避免程序流转，降低性能，以及死循环，做一次判断
                    var oldViewContext = this.viewContext;
                    if (oldViewContext === viewContext) {
                        return;
                    }

                    // 从老视图环境中清除
                    if (oldViewContext) {
                        this.viewContext = null;
                        oldViewContext.remove(this);
                    }

                    // 注册到新视图环境
                    this.viewContext = viewContext;
                    viewContext && viewContext.add(this);

                    // 切换子控件的视图环境
                    var children = this.children;
                    if (children) {
                        for (var i = 0, len = children.length; i < len; i++) {
                            children[i].setViewContext(viewContext);
                        }
                    }

                    // 在主元素上加个属性，以便找到`ViewContext`
                    if (this.viewContext && this.helper.isInStage('RENDERED')) {
                        this.main.setAttribute(
                            ui.getConfig('viewContextAttr'),
                            this.viewContext.id
                        );
                    }
                },

                /**
                 * 设置控件禁用状态
                 */
                setDisabled: function (disabled) {
                    this[disabled ? 'disable' : 'enable']();
                },

                /**
                 * 设置控件状态为禁用
                 */
                disable: function () {
                    this.addState('disabled');
                },

                /**
                 * 设置控件状态为启用
                 */
                enable: function () {
                    this.removeState('disabled');
                },

                /**
                 * 判断控件是否不可用
                 *
                 * @return {boolean}
                 */
                isDisabled: function () {
                    return this.hasState('disabled');
                },

                /**
                 * 设置控件状态为可见
                 */
                show: function () {
                    this.removeState('hidden');
                },

                /**
                 * 设置控件状态为不可见
                 */
                hide: function () {
                    this.addState('hidden');
                },

                /**
                 * 切换控件可见状态
                 */
                toggle: function () {
                    this[this.isHidden() ? 'show' : 'hide']();
                },

                /**
                 * 判断控件是否不可见
                 *
                 * @return {boolean}
                 */
                isHidden: function () {
                    return this.hasState('hidden');
                },

                /**
                 * 添加控件状态
                 *
                 * 调用该方法同时会让状态对应的属性变为`true`，
                 * 从而引起该属性对应的`painter`的执行
                 *
                 * 状态对应的属性名是指将状态名去除横线并以`camelCase`形式书写的名称，
                 * 如`align-left`对应的属性名为`alignLeft`
                 *
                 * @param {string} state 状态名
                 */
                addState: function (state) {
                    if (!this.hasState(state)) {
                        this.currentStates[state] = true;
                        this.helper.addStateClasses(state);
                        var properties = {};
                        var statePropertyName = state.replace(
                            /-(\w)/,
                            function (m, c) { return c.toUpperCase(); }
                        );
                        properties[statePropertyName] = true;
                        this.setProperties(properties);
                    }
                },

                /**
                 * 移除控件状态
                 *
                 * 调用该方法同时会让状态对应的属性变为`false`，
                 * 从而引起该属性对应的`painter`的执行
                 *
                 * 状态对应的属性名是指将状态名去除横线并以`camelCase`形式书写的名称，
                 * 如`align-left`对应的属性名为`alignLeft`
                 *
                 * @param {string} state 状态名
                 */
                removeState: function (state) {
                    if (this.hasState(state)) {
                        this.currentStates[state] = false;
                        this.helper.removeStateClasses(state);
                        var properties = {};
                        var statePropertyName = state.replace(
                            /-(\w)/,
                            function (m, c) { return c.toUpperCase(); }
                        );
                        properties[statePropertyName] = false;
                        this.setProperties(properties);
                    }
                },

                /**
                 * 切换控件指定状态
                 *
                 * 该方法根据当前状态调用{@link Control#addState}或
                 * {@link Control#removeState}方法，因此同样会对状态对应的属性进行修改
                 *
                 * @param {string} state 状态名
                 */
                toggleState: function (state) {
                    var methodName = this.hasState(state)
                        ? 'removeState'
                        : 'addState';

                    this[methodName](state);
                },

                /**
                 * 判断控件是否处于指定状态
                 *
                 * @param {string} state 状态名
                 * @return {boolean}
                 */
                hasState: function (state) {
                    return !!this.currentStates[state];
                },

                /**
                 * 添加子控件
                 *
                 * @param {Control} control 子控件实例
                 * @param {string} [childName] 子控件名
                 */
                addChild: function (control, childName) {
                    childName = childName || control.childName;

                    if (control.parent) {
                        control.parent.removeChild(control);
                    }

                    this.children.push(control);
                    control.parent = this;

                    if (childName) {
                        control.childName = childName;
                        this.childrenIndex[childName] = control;
                    }

                    // 将子视图环境设置与父控件一致
                    if (this.viewContext !== control.viewContext) {
                        control.setViewContext(this.viewContext);
                    }
                },

                /**
                 * 移除子控件
                 *
                 * @param {Control} control 子控件实例
                 */
                removeChild: function (control) {
                    // 从控件树容器中移除
                    var children = this.children;
                    var len = children.length;
                    while (len--) {
                        if (children[len] === control) {
                            children.splice(len, 1);
                        }
                    }

                    // 从具名子控件索引中移除
                    var childName = control.childName;
                    if (childName) {
                        this.childrenIndex[childName] = null;
                    }

                    control.parent = null;
                },

                /**
                 * 移除全部子控件
                 *
                 * @deprecated 将在4.0中移除，使用{@link Helper#disposeChildren}代替
                 */
                disposeChildren: function () {
                    var children = this.children.slice();
                    for (var i = 0; i < children.length; i++) {
                        children[i].dispose();
                    }
                    this.children = [];
                    this.childrenIndex = {};
                },

                /**
                 * 获取子控件
                 *
                 * @param {string} childName 子控件名
                 * @return {Control}
                 */
                getChild: function (childName) {
                    return this.childrenIndex[childName] || null;
                },

                /**
                 * 获取子控件，无相关子控件则返回{@link SafeWrapper}
                 *
                 * @param {string} childName 子控件名
                 * @return {Control}
                 */
                getChildSafely: function (childName) {
                    var child = this.getChild(childName);

                    if (!child) {
                        var SafeWrapper = require('./SafeWrapper');
                        child = new SafeWrapper();
                        child.childName = childName;
                        child.parent = this;
                        if (this.viewContext) {
                            child.viewContext = this.viewContext;
                        }
                    }

                    return child;
                },

                /**
                 * 批量初始化子控件
                 *
                 * @param {HTMLElement} [wrap] 容器DOM元素，默认为主元素
                 * @param {Object} [options] 初始化的配置参数
                 * @param {Object} [options.properties] 属性集合，通过id映射
                 * @deprecated 将在4.0中移除，使用{@link Helper#initChildren}代替
                 */
                initChildren: function (wrap, options) {
                    this.helper.initChildren(wrap, options);
                }
            };

            var EventTarget = require('mini-event/EventTarget');
            lib.inherits(Control, EventTarget);

            return Control;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 按钮
     * @author dbear, otakustay
     */
    define(
        'esui/Button',['require','underscore','./lib','./painters','./Control','./main'],function (require) {
            var u = require('underscore');
            var lib = require('./lib');
            var paint = require('./painters');
            var Control = require('./Control');

            /**
             * 按钮控件
             *
             * @extends Control
             * @constructor
             */
            function Button(options) {
                Control.apply(this, arguments);
            }

            /**
             * 获取元素border信息
             *
             * @param {HTMLElement} dom 目标元素
             * @return {Object}
             * @ignore
             */
            function getBorderInfo(dom) {
                var result = {};
                result.borderTop =
                    parseInt(lib.getComputedStyle(dom, 'borderTopWidth'), 10);
                result.borderBottom =
                    parseInt(lib.getComputedStyle(dom, 'borderBottomWidth'), 10);
                return result;
            }

            Button.prototype = {
                /**
                 * 控件类型，始终为`"Button"`
                 *
                 * @type {string}
                 * @readonly
                 * @override
                 */
                type: 'Button',

                /**
                 * 初始化参数
                 *
                 * @param {Object} [options] 构造函数传入的参数
                 * @protected
                 * @override
                 */
                initOptions: function (options) {
                    /**
                     * 默认选项配置
                     */
                    var properties = {
                        content: '', // 按钮的显示文字
                        disabled: false // 控件是否禁用
                    };
                    u.extend(properties, options);
                    properties.tagName = this.main.nodeName.toLowerCase();
                    if (properties.text == null) {
                        properties.text = lib.getText(this.main);
                    }
                    var innerDiv = this.main.firstChild;
                    if (!properties.content
                        && innerDiv
                        && innerDiv.nodeName.toLowerCase() !== 'div'
                        ) {
                        properties.content = this.main.innerHTML;
                    }

                    this.setProperties(properties);
                },

                /**
                 * 创建控件主元素，默认使用`<button type="button">`元素
                 *
                 * 如果需要使用其它类型作为主元素，
                 * 需要在始终化时提供{@link Control#main}属性
                 *
                 * @return {HTMLElement}
                 * @protected
                 * @override
                 */
                createMain: function () {
                    // IE创建带`type`属性的元素很麻烦，干脆这么来
                    var div = document.createElement('div');
                    div.innerHTML = '<button type="button"></button>';
                    return div.firstChild;
                },

                /**
                 * 初始化事件交互
                 *
                 * @protected
                 * @override
                 */
                initEvents: function () {
                    /**
                     * @event click
                     *
                     * 点击时触发
                     */
                    this.helper.delegateDOMEvent(this.main, 'click');
                },

                /**
                 * 重新渲染
                 *
                 * @method
                 * @protected
                 * @override
                 */
                repaint: paint.createRepaint(
                    Control.prototype.repaint,
                    /**
                     * @property {number} width
                     *
                     * 宽度
                     */
                    paint.style('width'),
                    {
                        /**
                         * @property {number} height
                         *
                         * 高度
                         */
                        name: 'height',
                        paint: function (button, value) {
                            if (!value) {
                                return;
                            }
                            var main = button.main;
                            main.style.height = value + 'px';
                            var lineHeight = value;
                            main.style.lineHeight = lineHeight + 'px';

                            var offsetHeight = main.offsetHeight;
                            // 说明是border-box模式
                            if (offsetHeight === value) {
                                var borderInfo = getBorderInfo(main);
                                var height = value
                                    + borderInfo.borderTop
                                    + borderInfo.borderBottom;
                                main.style.height = height + 'px';
                            }
                        }
                    },
                    /**
                     * @property {string} [content=""]
                     *
                     * 按钮的文本内容，不作HTML转义
                     */
                    paint.html('content'),
                    {
                        name: 'disabled',
                        paint: function (button, disabled) {
                            var nodeName = button.main.nodeName.toLowerCase();
                            if (nodeName === 'button' || nodeName === 'input') {
                                button.main.disabled = !!disabled;
                            }
                        }
                    }
                ),

                /**
                 * 设置内容
                 *
                 * @param {string} content 要设置的内容
                 */
                setContent: function (content) {
                    this.setProperties({ 'content': content });
                }
            };

            lib.inherits(Button, Control);
            require('./main').register(Button);

            return Button;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 控件浮层基类
     * @author otakustay
     */
    define(
        'esui/Layer',['require','underscore','./lib','./main','mini-event/EventTarget'],function(require) {
            var u = require('underscore');
            var lib = require('./lib');
            var ui = require('./main');

            /**
             * 浮层基类
             *
             * `Layer`类是一个与控件形成组合关系的类，但并不是一个控件
             *
             * 当一个控件需要一个浮层（如下拉框）时，可以使用此类，并重写相关方法来实现浮层管理
             *
             * 不把`Layer`作为一个控件来实现，是有以下考虑：
             *
             * - 即便`Layer`作为子控件使用，也必须重写所有相关方法才能起作用，并未节省代码
             * - 控件的生命周期管理、事件管理等一大堆事对性能多少有些负面影响
             * - 通常重写`Layer`的方法时，会依赖控件本身的一些开放接口。
             * 那么如果`Layer`是个子控件，就形成了 **子控件反调用父控件方法** 的现象，不合理
             *
             * 关于如何使用`Layer`控件，可以参考{@link CommandMenu}进行学习
             *
             * @constructor
             * @param {Control} control 关联的控件实例
             */
            function Layer(control) {
                this.control = control;
            }

            /**
             * 创建的元素标签类型
             *
             * @type {string}
             */
            Layer.prototype.nodeName = 'div';

            /**
             * 控制是否在页面滚动等交互发生时自动隐藏，默认为`true`
             *
             * 如果需要改变此属性，必须在初始化后立即设置，仅在第一次创建层时生效
             *
             * @type {boolean}
             */
            Layer.prototype.autoHide = true;

            /**
             * 通过点击关闭弹层的处理方法
             *
             * @param {Event} e DOM事件对象
             * @ignore
             */
            function close(e) {
                var target = e.target;
                var layer = this.getElement(this);
                var main = this.control.main;

                if (!layer) {
                    return;
                }

                while (target && (target !== layer && target !== main)) {
                    target = target.parentNode;
                }

                if (target !== layer && target !== main) {
                    this.hide();
                }
            }

            /**
             * 启用自动隐藏功能
             *
             * @param {HTMLElement} element 需要控制隐藏的层元素
             */
            Layer.prototype.enableAutoHide = function (element) {
                var eventName = 'onwheel' in document.body ? 'wheel' : 'mousewheel';
                this.control.helper.addDOMEvent(
                    document.documentElement,
                    eventName,
                    u.bind(this.hide, this)
                );
                // 自己的滚动不要关掉
                this.control.helper.addDOMEvent(
                    element,
                    eventName,
                    function (e) { e.stopPropagation(); }
                );
            };

            /**
             * 创建浮层
             *
             * @return {HTMLElement}
             */
            Layer.prototype.create = function () {
                var element =
                    this.control.helper.createPart('layer', this.nodeName);
                lib.addClass(element, ui.getConfig('uiClassPrefix') + '-layer');

                if (this.autoHide) {
                    this.enableAutoHide(element);
                }

                return element;
            };

            /**
             * 给Layer增加自定义class
             *
             * @return {array} layerClassNames样式集合
             */
            Layer.prototype.addCustomClasses = function (layerClassNames) {
                var element = this.getElement();
                lib.addClasses(element, layerClassNames);
            };

            /**
             * 渲染层内容
             *
             * @param {HTMLElement} element 层元素
             * @abstract
             */
            Layer.prototype.render = function (element) {
            };

            /**
             * 同步控件状态到层
             *
             * @param {HTMLElement} element 层元素
             * @abstract
             */
            Layer.prototype.syncState = function (element) {
            };

            /**
             * 重新渲染
             */
            Layer.prototype.repaint = function () {
                var element = this.getElement(false);
                if (element) {
                    this.render(element);
                }
            };

            /**
             * 初始化层的交互行为
             *
             * @param {HTMLElement} element 层元素
             * @abstract
             */
            Layer.prototype.initBehavior = function (element) {
            };

            function getHiddenClasses(layer) {
                var classes = layer.control.helper.getPartClasses('layer-hidden');
                classes.unshift(ui.getConfig('uiClassPrefix') + '-layer-hidden');

                return classes;
            }

            /**
             * 获取浮层DOM元素
             *
             * @param {boolean} [create=true] 不存在时是否创建
             * @return {HTMLElement}
             */
            Layer.prototype.getElement = function (create) {
                var element = this.control.helper.getPart('layer');

                if (!element && create !== false) {
                    element = this.create();
                    this.render(element);

                    lib.addClasses(element, getHiddenClasses(this));

                    this.initBehavior(element);
                    this.control.helper.addDOMEvent(
                        document, 'mousedown', u.bind(close, this));
                    // 不能点层自己也关掉，所以阻止冒泡到`document`
                    this.control.helper.addDOMEvent(
                        element,
                        'mousedown',
                        function (e) { e.stopPropagation(); }
                    );

                    this.syncState(element);

                    // IE下元素始终有`parentNode`，无法判断是否进入了DOM
                    if (!element.parentElement) {
                        document.body.appendChild(element);
                    }

                    this.fire('rendered');
                }

                return element;
            };

            /**
             * 隐藏层
             */
            Layer.prototype.hide = function () {
                var classes = getHiddenClasses(this);

                var element = this.getElement();
                lib.addClasses(element, classes);
                this.control.removeState('active');
                this.fire('hide');
            };

            /**
             * 显示层
             */
            Layer.prototype.show = function () {
                var element = this.getElement();
                element.style.zIndex = this.getZIndex();

                this.position();

                var classes = getHiddenClasses(this);
                lib.removeClasses(element, classes);
                this.control.addState('active');
                this.fire('show');
            };

            /**
             * 切换显示状态
             */
            Layer.prototype.toggle = function () {
                var element = this.getElement();
                if (!element
                    || this.control.helper.isPart(element, 'layer-hidden')
                    ) {
                    this.show();
                }
                else {
                    this.hide();
                }
            };

            /**
             * 放置层
             */
            Layer.prototype.position = function () {
                var element = this.getElement();
                Layer.attachTo(element, this.control.main, this.dock);
            };

            /**
             * 获取层应该有的`z-index`样式值
             *
             * @return {number}
             */
            Layer.prototype.getZIndex = function () {
                return Layer.getZIndex(this.control.main);
            };

            /**
             * 销毁
             */
            Layer.prototype.dispose = function () {
                var element = this.getElement(false);
                if (element) {
                    element.innerHTML = '';
                    lib.removeNode(element);
                }
                this.control = null;
            };

            // 控制浮层的静态方法，用与本身就漂浮的那些控件（如`Dialog`），
            // 它们无法组合`Layer`实例，因此需要静态方法为其服务

            // 初始最高的`z-index`值，将浮层移到最上就是参考此值
            var zIndexStack = 1000;

            /**
             * 创建层元素
             *
             * @param {string} [tagName="div"] 元素的标签名
             * @return {HTMLElement}
             * @static
             */
            Layer.create = function (tagName) {
                var element = document.createElement(tagName || 'div');
                element.style.position = 'absolute';
                return element;
            };

            /**
             * 获取层应当使用的`z-index`的值
             *
             * @param {HTMLElement} [owner] 层的所有者元素
             * @return {number}
             * @static
             */
            Layer.getZIndex = function (owner) {
                var zIndex = 0;
                while (!zIndex && owner && owner !== document) {
                    zIndex =
                        parseInt(lib.getComputedStyle(owner, 'zIndex'), 10);
                    owner = owner.parentNode;
                }
                zIndex = zIndex || 0;
                return zIndex + 1;
            };

            /**
             * 将当前层移到最前
             *
             * @param {HTMLElement} element 目标层元素
             * @static
             */
            Layer.moveToTop = function (element) {
                element.style.zIndex = ++zIndexStack;
            };

            /**
             * 移动层的位置
             *
             * @param {HTMLElement} element 目标层元素
             * @param {number} top 上边界距离
             * @param {number} left 左边界距离
             * @static
             */
            Layer.moveTo = function (element, top, left) {
                positionLayerElement(element, { top: top, left: left });
            };

            /**
             * 缩放层的大小
             *
             * @param {HTMLElement} element 目标层元素
             * @param {number} width 宽度
             * @param {number} height 高度
             * @static
             */
            Layer.resize = function (element, width, height) {
                positionLayerElement(element, { width: width, height: height });
            };

            /**
             * 让当前层靠住一个指定的元素
             *
             * @param {HTMLElement} layer 目标层元素
             * @param {HTMLElement} target 目标元素
             * @param {Object} [options] 停靠相关的选项
             * @param {boolean} [options.strictWidth=false] 是否要求层的宽度不小于目标元素的宽度
             * @static
             */
            Layer.attachTo = function (layer, target, options) {
                options = options || { strictWidth: false };
                // 垂直算法：
                //
                // 1. 将层的上边缘贴住目标元素的下边缘
                // 2. 如果下方空间不够，则转为层的下边缘贴住目标元素的上边缘
                // 3. 如果上方空间依旧不够，则强制使用第1步的位置
                //
                // 水平算法：
                //
                // 1. 如果要求层和目标元素等宽，则设置宽度，层的左边缘贴住目标元素的左边缘，结束
                // 2. 将层的左边缘贴住目标元素的左边缘
                // 3. 如果右侧空间不够，则转为层的右边缘贴住目标元素的右边缘
                // 4. 如果左侧空间依旧不够，则强制使用第2步的位置

                // 虽然这2个变量下面不一定用得到，但是不能等层出来了再取，
                // 一但层出现，可能造成滚动条出现，导致页面尺寸变小
                var pageWidth = lib.page.getViewWidth();
                var pageHeight = lib.page.getViewHeight();
                var pageScrollTop = lib.page.getScrollTop();
                var pageScrollLeft = lib.page.getScrollLeft();

                // 获取目标元素的属性
                var targetOffset = lib.getOffset(target);

                // 浮层的存在会影响页面高度计算，必须先让它消失，
                // 但在消失前，又必须先计算到浮层的正确高度
                var previousDisplayValue = layer.style.display;
                layer.style.display = 'block';
                layer.style.top = '-5000px';
                layer.style.left = '-5000px';
                // 如果对层宽度有要求，则先设置好最小宽度
                if (options.strictWidth) {
                    layer.style.minWidth = targetOffset.width + 'px';
                }
                // IE7下，如果浮层隐藏着反而会影响offset的获取，
                // 但浮层显示出来又可能造成滚动条出现，
                // 因此显示浮层显示后移到屏幕外面，然后计算坐标
                var layerOffset = lib.getOffset(layer);
                // 用完改回来再计算后面的
                layer.style.top = '';
                layer.style.left = '';
                layer.style.display = previousDisplayValue;


                var properties = {};

                // 先算垂直的位置
                var bottomSpace = pageHeight - (targetOffset.bottom - pageScrollTop);
                var topSpace = targetOffset.top - pageScrollTop;
                if (bottomSpace <= layerOffset.height && topSpace > layerOffset.height) {
                    // 放上面
                    properties.top = targetOffset.top - layerOffset.height;
                }
                else {
                    // 放下面
                    properties.top = targetOffset.bottom;
                }

                // 再算水平的位置
                var rightSpace = pageWidth - (targetOffset.left - pageScrollLeft);
                var leftSpace = targetOffset.right - pageScrollLeft;
                if (rightSpace <= layerOffset.width && leftSpace > layerOffset.width) {
                    // 靠右侧
                    properties.left = targetOffset.right - layerOffset.width;
                }
                else {
                    // 靠左侧
                    properties.left = targetOffset.left;
                }

                positionLayerElement(layer, properties);
            };

            /**
             * 将层在视图中居中
             *
             * @param {HTMLElement} element 目标层元素
             * @param {Object} [options] 相关配置项
             * @param {number} [options.width] 指定层的宽度
             * @param {number} [options.height] 指定层的高度
             * @param {number} [options.minTop] 如果层高度超过视图高度，
             * 则留下该值的上边界保底
             * @param {number} [options.minLeft] 如果层宽度超过视图高度，
             * 则留下该值的左边界保底
             * @static
             */
            Layer.centerToView = function (element, options) {
                var properties = options ? lib.clone(options) : {};

                if (typeof properties.width !== 'number') {
                    properties.width = this.width;
                }
                if (typeof properties.height !== 'number') {
                    properties.height = this.height;
                }

                properties.left = (lib.page.getViewWidth() - properties.width) / 2;

                var viewHeight = lib.page.getViewHeight();
                if (properties.height >= viewHeight &&
                    options.hasOwnProperty('minTop')
                    ) {
                    properties.top = options.minTop;
                }
                else {
                    properties.top =
                        Math.floor((viewHeight - properties.height) / 2);
                }

                var viewWidth = lib.page.getViewWidth();
                if (properties.height >= viewWidth &&
                    options.hasOwnProperty('minLeft')
                    ) {
                    properties.left = options.minLeft;
                }
                else {
                    properties.left =
                        Math.floor((viewWidth - properties.width) / 2);
                }

                properties.top += lib.page.getScrollTop();
                this.setProperties(properties);
            };

            // 统一浮层放置方法方法
            function positionLayerElement(element, options) {
                var properties = lib.clone(options || {});

                // 如果同时有`top`和`bottom`，则计算出`height`来
                if (properties.hasOwnProperty('top')
                    && properties.hasOwnProperty('bottom')
                    ) {
                    properties.height = properties.bottom - properties.top;
                    delete properties.bottom;
                }
                // 同样处理`left`和`right`
                if (properties.hasOwnProperty('left')
                    && properties.hasOwnProperty('right')
                    ) {
                    properties.width = properties.right - properties.left;
                    delete properties.right;
                }

                // 避免原来的属性影响
                if (properties.hasOwnProperty('top')
                    || properties.hasOwnProperty('bottom')
                    ) {
                    element.style.top = '';
                    element.style.bottom = '';
                }

                if (properties.hasOwnProperty('left')
                    || properties.hasOwnProperty('right')
                    ) {
                    element.style.left = '';
                    element.style.right = '';
                }

                // 设置位置和大小
                for (var name in properties) {
                    if (properties.hasOwnProperty(name)) {
                        element.style[name] = properties[name] + 'px';
                    }
                }
            }

            var EventTarget = require('mini-event/EventTarget');
            lib.inherits(Layer, EventTarget);

            return Layer;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 控件类常用的helper方法模块
     * @author erik, otakustay
     */
    define(
        'esui/controlHelper',['require','./lib','./Helper','./painters','underscore','./Layer'],function (require) {
            var lib = require('./lib');
            var Helper = require('./Helper');

            /**
             * 提供控件类常用的辅助方法
             *
             * **此对象将在4.0版本中移除** ，请按以下规则迁移：
             *
             * - `getGUID`移至{@link lib#getGUID}
             * - `createRepaint`移至{@link painters#createRepaint}
             * - 多数方法移至{@link Helper}类下
             * - 浮层相关方法移至{@link Layer}类下
             *
             * @class controlHelper
             * @singleton
             */
            var helper = {};

            /**
             * @ignore
             */
            helper.getGUID = lib.getGUID;

            var methods = [
                // life
                'initViewContext', 'initExtensions',
                'isInStage', 'changeStage',
                'dispose', 'beforeDispose', 'afterDispose',
                // dom
                'getPartClasses', 'addPartClasses', 'removePartClasses',
                'getStateClasses', 'addStateClasses', 'removeStateClasses',
                'getId', 'replaceMain',
                // event
                'addDOMEvent', 'removeDOMEvent', 'clearDOMEvents'
            ];

            helper.createRepaint = require('./painters').createRepaint;

            // 补上原有的方法，全部代理到`Helper`上
            require('underscore').each(
                methods,
                function (name) {
                    helper[name] = function (control) {
                        var helper = control.helper || new Helper(control);
                        var args = [].slice.call(arguments, 1);
                        return helper[name].apply(helper, args);
                    };
                }
            );

            // 再往下的全部是等待废弃的

            /**
             * @ignore
             * @deprecated 使用{@link Helper#extractOptionsFromInput}代替
             */
            helper.extractValueFromInput = function (control, options) {
                var main = control.main;
                // 如果是输入元素
                if (lib.isInput(main)) {
                    if (main.value && !options.value) {
                        options.value = main.value;
                    }
                    if (main.name && !options.name) {
                        options.name = main.name;
                    }
                    if (main.disabled
                        && (options.disabled === null
                            || options.disabled === undefined)) {
                        options.disabled = main.disabled;
                    }
                    if (main.readOnly
                        && (options.readOnly === null
                            || options.readOnly === undefined)) {
                        options.readOnly = main.readonly || main.readOnly;
                    }
                }
            };

            var layer = helper.layer = {};
            var Layer = require('./Layer');

            /**
             * @ignore
             */
            layer.create = Layer.create;

            /**
             * @ignore
             */
            layer.getZIndex = Layer.getZIndex;

            /**
             * @ignore
             */
            layer.moveToTop = Layer.moveToTop;

            /**
             * @ignore
             */
            layer.moveTo = Layer.moveTo;

            /**
             * @ignore
             */
            layer.resize = Layer.resize;

            /**
             * @ignore
             */
            layer.attachTo = Layer.attachTo;

            /**
             * @ignore
             */
            layer.centerToView = Layer.centerToView;

            return helper;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 验证信息显示控件
     * @author otakustay
     */
    define(
        // 你说为啥要有这么个控件？因为有2货喜欢在验证提示里放别的控件！
        // 你说为啥这东西不继承`Label`？因为有2货要往里放控件！
        // 你说为啥名字不叫`ValidityLabel`？CSS样式里看到`validitylabel`多丑！
        'esui/Validity',['require','underscore','./lib','./Control','./Helper','./painters','./main'],function (require) {
            var u  = require('underscore');
            var lib = require('./lib');
            var Control = require('./Control');
            var Helper = require('./Helper');

            /**
             * 验证信息显示控件
             *
             * @extends Control
             * @constructor
             */
            function Validity() {
                Control.apply(this, arguments);
            }

            Validity.prototype.type = 'Validity';

            /**
             * 创建主元素，默认使用`<label>`元素
             *
             * @return {HTMLElement}
             * @protected
             * @override
             */
            Validity.prototype.createMain = function () {
                return document.createElement('label');
            };

            /**
             * 初始化参数
             *
             * @param {Object} [options] 输入的参数
             * @protected
             * @override
             */
            Validity.prototype.initOptions = function (options) {
                var properties =
                    u.extend({}, Validity.defaultProperties, options);
                Control.prototype.initOptions.call(this, properties);
            };

            /**
             * 获取元素的全部class
             *
             * @param {Validity} label 控件实例
             * @param {string} state 验证状态
             * @ignore
             */
            function getClasses(label, state) {
                var target = label.target;

                var targetHelper = null;
                if (target || label.targetType) {
                    var targetContext = {
                        type: label.targetType || target.type,
                        skin: target && target.skin
                    };
                    targetHelper = new Helper(targetContext);
                }

                var classes = label.helper.getPartClasses();
                if (targetHelper) {
                    classes.push.apply(
                        classes,
                        targetHelper.getPartClasses('validity-label')
                    );
                }
                if (state) {
                    classes.push.apply(
                        classes,
                        label.helper.getPartClasses(state)
                    );
                    if (targetHelper) {
                        classes.push.apply(
                            classes,
                            targetHelper.getPartClasses('validity-label-' + state)
                        );
                    }
                }
                if ((target && target.isHidden()) || label.isHidden()) {
                    classes.push.apply(
                        classes,
                        label.helper.getStateClasses('hidden')
                    );
                    if (target) {
                        classes.push.apply(
                            classes,
                            target.helper.getPartClasses('validity-label-hidden')
                        );
                    }
                }
                return classes;
            }

            /**
             * 显示验证信息，可重写
             *
             * @param {string} validState 验证结果
             * @param {string} message 验证信息
             * @param {validator.Validity} validity 最原始的验证结果对象
             * @protected
             */
            Validity.prototype.display = function (validState, message, validity) {
                this.main.innerHTML = message;
            };

            /**
             * 重绘
             *
             * @method
             * @protected
             * @override
             */
            Validity.prototype.repaint = require('./painters').createRepaint(
                Control.prototype.repaint,
                {
                    /**
                     * @property {Control} target
                     *
                     * 对应的控件
                     */

                    /**
                     * @property {string} targetType
                     *
                     * 对应的控件的类型，可覆盖{@link Validity#target}的`type`属性
                     */
                    name: ['target', 'targetType'],
                    paint: function (label) {
                        var validState = label.validity
                            ? label.validity.getValidState()
                            : '';
                        var classes = getClasses(label, validState);
                        label.main.className = classes.join(' ');
                    }
                },
                {
                    /**
                     * @property {HTMLElement} focusTarget
                     *
                     * 点击当前标签后获得焦点的元素
                     *
                     * 此元素如果没有`id`属性，则不会获得焦点
                     *
                     * 私有属性，仅通过{@link InputControl#getFocusTarget}方法获得
                     *
                     * @private
                     */
                    name: 'focusTarget',
                    paint: function (label, focusTarget) {
                        if (label.main.nodeName.toLowerCase() === 'label') {
                            if (focusTarget && focusTarget.id) {
                                lib.setAttribute(label.main, 'for', focusTarget.id);
                            }
                            else {
                                lib.removeAttribute(label.main, 'for');
                            }
                        }
                    }
                },
                {
                    /**
                     * @property {validator.Validity} validity
                     *
                     * 验证结果
                     */
                    name: 'validity',
                    paint: function (label, validity) {
                        var validState = validity && validity.getValidState();
                        var classes = getClasses(label, validState);
                        label.main.className = classes.join(' ');

                        label.disposeChildren();
                        if (validity) {
                            var message = validity.getCustomMessage();
                            if (!message) {
                                var invalidState = u.find(
                                    validity.getStates(),
                                    function (state) {
                                        return !state.getState();
                                    }
                                );
                                message = invalidState && invalidState.getMessage();
                            }
                            label.display(validState, message || '', validity);
                            label.helper.initChildren();
                            if (message) {
                                label.show();
                            }
                            else {
                                label.hide();
                            }
                        }
                        else {
                            label.main.innerHTML = '';
                            label.hide();
                        }
                    }
                }
            );

            /**
             * 销毁控件
             *
             * @override
             */
            Validity.prototype.dispose = function () {
                if (this.helper.isInStage('DISPOSED')) {
                    return;
                }

                if (this.target) {
                    this.target.validityLabel = null;
                    this.target = null;
                }
                this.focusTarget = null;

                if (this.main.parentNode) {
                    this.main.parentNode.removeChild(this.main);
                }

                Control.prototype.dispose.apply(this, arguments);
            };

            lib.inherits(Validity, Control);
            require('./main').register(Validity);
            return Validity;
        }
    );

    /**
     * ESUI (Enterprise UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 验证信息类
     * @author DBear
     */
    define(
        'esui/validator/Validity',['require','underscore'],function (require) {
            var u = require('underscore');

            /**
             * 验证结果类
             *
             * 一个`Validity`是对一个控件的验证结果的表达，
             * 是一系列{@link validator.ValidityState}的组合
             *
             * 当有至少一个{@link validator.ValidityState}处于错误状态时，
             * 该`Validity`对象将处于错误状态
             *
             * @class validator.Validity
             * @constructor
             */
            function Validity() {
                this.states = [];
                this.stateIndex = {};
                this.customMessage = '';
                this.customValidState = null;
            }

            /**
             * 添加验证状态
             *
             * @param {string} name 状态名
             * @param {validator.ValidityState} state 规则验证状态对象
             */
            Validity.prototype.addState = function (name, state) {
                //如果状态名已存在
                if (this.stateIndex[name]) {
                    // 同样的状态对象，不处理
                    if (this.stateIndex[name] === state) {
                        return;
                    }

                    // 不一样，删除原list中元素
                    for (var i = 0; i < this.states.length; i++) {
                        if (this.states[i] === this.stateIndex[name]) {
                            this.states.splice(i, 1);
                            break;
                        }
                    }
                }

                // 更新数据
                this.states.push(state);
                this.stateIndex[name] = state;
            };

            /**
             * 获取验证状态
             *
             * @param {string} name 状态名
             * @return {validator.ValidityState} 规则验证状态对象
             */
            Validity.prototype.getState = function (name) {
                return this.stateIndex[name] || null;
            };

            /**
             * 获取验证状态集合
             *
             * @return {validator.ValidityState[]}
             */
            Validity.prototype.getStates = function () {
                return this.states.slice();
            };

            /**
             * 获取自定义验证信息
             *
             * @return {string}
             */
            Validity.prototype.getCustomMessage = function () {
                return this.customMessage;
            };


            /**
             * 设置自定义验证信息
             *
             * @param {string} message 自定义验证信息
             */
            Validity.prototype.setCustomMessage = function (message) {
                this.customMessage = message;
            };

            /**
             * 设置自定义验证结果
             *
             * @param {string} validState 验证结果字符串
             */
            Validity.prototype.setCustomValidState = function (validState) {
                this.customValidState = validState;
            };


            /**
             * 获取整体是否验证通过
             *
             * @return {boolean}
             */
            Validity.prototype.isValid = function () {
                return u.all(
                    this.getStates(),
                    function (state) {
                        return state.getState();
                    }
                );
            };

            /**
             * 获取验证状态的字符串
             *
             * @return {string}
             */
            Validity.prototype.getValidState = function () {
                return this.customValidState
                    || (this.isValid() ? 'valid' : 'invalid');
            };

            return Validity;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 输入控件基类模块
     * @author erik, otakustay
     */
    define(
        'esui/InputControl',['require','./lib','./controlHelper','./Control','./Validity','./validator/Validity','./main'],function (require) {
            var lib = require('./lib');
            var helper = require('./controlHelper');
            var Control = require('./Control');
            var ValidityLabel = require('./Validity');
            var Validity = require('./validator/Validity');
            var main = require('./main');

            /**
             * 输入控件基类
             *
             * 输入控件用于表示需要在表单中包含的控件，
             * 其主要提供`getRawValue`和`getValue`方法供获取值
             *
             * 需要注意的是，控件其实并不通过严格的继承关系来判断一个控件是否为输入控件，
             * 只要`getCategory()`返回为`"input"`、`"check"或`"extend"`就认为是输入控件
             *
             * 相比普通控件的 **禁用 / 启用** ，输入控件共有3种状态：
             *
             * - 普通状态：可编辑，值随表单提交
             * - `disabled`：禁用状态，此状态下控件不能编辑，同时值不随表单提交
             * - `readOnly`：只读状态，此状态下控件不能编辑，但其值会随表单提交
             *
             * @extends Control
             * @constructor
             * @param {Object} [options] 初始化参数
             */
            function InputControl(options) {
                options = options ? lib.extend({}, options) : {};
                if (options.main && !options.name) {
                    /**
                     * @property {string} name
                     *
                     * 输入控件的名称，用于表单提交时作为键值
                     *
                     * @readonly
                     */
                    options.name = options.main.getAttribute('name');
                }
                Control.call(this, options);
            }

            InputControl.prototype = {
                constructor: InputControl,

                /**
                 * 指定在哪些状态下该元素不处理相关的DOM事件，
                 * 输入控件额外增加`read-only`状态
                 *
                 * @type {string[]}
                 * @protected
                 * @override
                 */
                ignoreStates: Control.prototype.ignoreStates.concat('read-only'),

                /**
                 * 获取控件的分类，默认返回`"input"`以表示为输入控件
                 *
                 * @return {string}
                 * @override
                 */
                getCategory: function () {
                    return 'input';
                },

                /**
                 * 获得应当获取焦点的元素，主要用于验证信息的`<label>`元素的`for`属性设置
                 *
                 * @return {HTMLElement}
                 * @protected
                 */
                getFocusTarget: function () {
                    return null;
                },

                /**
                 * 获取输入控件的值的字符串形式
                 *
                 * @return {string}
                 */
                getValue: function () {
                    /**
                     * @property {string} value
                     *
                     * 输入控件的字符串形式的值
                     */
                    return this.stringifyValue(this.getRawValue());
                },

                /**
                 * 设置输入控件的值
                 *
                 * @param {string} value 输入控件的值
                 */
                setValue: function (value) {
                    var rawValue = this.parseValue(value);
                    this.setRawValue(rawValue);
                },

                /**
                 * 获取输入控件的原始值，原始值的格式由控件自身决定
                 *
                 * @return {Mixed}
                 */
                getRawValue: function () {
                    /**
                     * @property {Mixed} rawValue
                     *
                     * 输入控件的原始值，其格式由控件自身决定
                     */
                    return this.rawValue;
                },

                /**
                 * 设置输入控件的原始值，原始值的格式由控件自身决定
                 *
                 * @param {Mixed} rawValue 输入控件的原始值
                 */
                setRawValue: function (rawValue) {
                    this.setProperties({ rawValue: rawValue });
                },

                /**
                 * 批量设置控件的属性值
                 *
                 * @param {Object} properties 属性值集合
                 * @override
                 */
                setProperties: function (properties) {
                    // 当value和rawValue同时存在时，以rawValue为准
                    // 否则，将value解析成rawValue
                    var value = properties.value;
                    delete properties.value;
                    if (value != null && properties.rawValue == null) {
                        properties.rawValue = this.parseValue(value);
                    }

                    if (this.hasOwnProperty('readOnly')) {
                        this.readOnly = !!this.readOnly;
                    }

                    return Control.prototype.setProperties.call(this, properties);
                },

                /**
                 * 重渲染
                 *
                 * @method repaint
                 * @protected
                 * @override
                 */
                repaint: helper.createRepaint(
                    Control.prototype.repaint,
                    {
                        name: 'disabled',
                        paint: function (control, value) {
                            var nodeName = control.main.nodeName.toLowerCase();
                            if (nodeName === 'input'
                                || nodeName === 'select'
                                || nodeName === 'textarea'
                                ) {
                                control.main.disabled = value;
                            }
                        }
                    },
                    {
                        /**
                         * @property {boolean} readOnly
                         *
                         * 是否只读
                         *
                         * 只读状态下，控件通过用户操作不能修改值，但值随表单提交
                         */
                        name: 'readOnly',
                        paint: function (control, value) {
                            var method = value ? 'addState' : 'removeState';
                            control[method]('read-only');
                            var nodeName = control.main.nodeName.toLowerCase();
                            if (nodeName === 'input'
                                || nodeName === 'select'
                                || nodeName === 'textarea'
                                ) {
                                control.main.readOnly = value;
                            }
                        }
                    },
                    {
                        name: 'hidden',
                        paint: function (control, hidden) {
                            // 需要同步验证信息的样式
                            var validityLabel = control.getValidityLabel(true);
                            if (validityLabel) {
                                var classPrefix = main.getConfig('uiClassPrefix');
                                var classes = [].concat(
                                        classPrefix + '-hidden',
                                        classPrefix + '-validity-hidden',
                                    helper.getPartClasses(
                                        control, 'validity-hidden')
                                );
                                var method = control.isHidden()
                                    ? 'addClasses'
                                    : 'removeClasses';
                                lib[method](validityLabel, classes);
                            }
                        }

                    }
                ),

                /**
                 * 将值从原始格式转换成字符串，复杂类型的输入控件需要重写此接口
                 *
                 * @param {Mixed} rawValue 原始值
                 * @return {string}
                 * @protected
                 */
                stringifyValue: function (rawValue) {
                    return rawValue != null ? (rawValue + '') : '';
                },

                /**
                 * 将字符串类型的值转换成原始格式，复杂类型的输入控件需要重写此接口
                 *
                 * @param {string} value 字符串值
                 * @return {Mixed}
                 * @protected
                 */
                parseValue: function (value) {
                    return value;
                },

                /**
                 * 设置控件的只读状态
                 *
                 * @param {boolean} readOnly 是否只读
                 */
                setReadOnly: function (readOnly) {
                    readOnly = !!readOnly;
                    this[readOnly ? 'addState' : 'removeState']('read-only');
                },

                /**
                 * 判读控件是否处于只读状态
                 *
                 * @return {boolean}
                 */
                isReadOnly: function () {
                    return this.hasState('read-only');
                },

                /**
                 * 获取验证结果的{@link validator.Validity}对象
                 *
                 * @return {validator.Validity}
                 * @fires beforevalidate
                 * @fires aftervalidate
                 * @fires invalid
                 */
                getValidationResult: function () {
                    var validity = new Validity();
                    var eventArg = {
                        validity: validity
                    };

                    /**
                     * @event beforevalidate
                     *
                     * 在验证前触发
                     *
                     * @param {validator.Validity} validity 验证结果
                     * @member InputControl
                     */
                    eventArg = this.fire('beforevalidate', eventArg);

                    // 验证合法性
                    var rules = main.createRulesByControl(this);
                    for (var i = 0, len = rules.length; i < len; i++) {
                        var rule = rules[i];
                        validity.addState(
                            rule.getName(),
                            rule.check(this.getValue(), this)
                        );
                    }

                    // 触发invalid和aftervalidate事件
                    // 这两个事件中用户可能会对validity进行修改操作
                    // 所以validity.isValid()结果不能缓存
                    if (!validity.isValid()) {
                        /**
                         * @event invalid
                         *
                         * 在验证结果为错误时触发
                         *
                         * @param {validator.Validity} validity 验证结果
                         * @member InputControl
                         */
                        eventArg = this.fire('invalid', eventArg);
                    }

                    /**
                     * @event aftervalidate
                     *
                     * 在验证后触发
                     *
                     * @param {validator.Validity} validity 验证结果
                     * @member InputControl
                     */
                    this.fire('aftervalidate', eventArg);

                    return validity;
                },

                /**
                 * 验证控件，仅返回`true`或`false`
                 *
                 * @return {boolean}
                 * @fires beforevalidate
                 * @fires aftervalidate
                 * @fires invalid
                 */
                checkValidity: function () {
                    var validity = this.getValidationResult();
                    return validity.isValid();
                },

                /**
                 * 验证控件，当值不合法时显示错误信息
                 *
                 * @return {boolean}
                 * @fires beforevalidate
                 * @fires aftervalidate
                 * @fires invalid
                 */
                validate: function () {
                    var validity = this.getValidationResult();
                    this.showValidity(validity);
                    return validity.isValid();
                },

                /**
                 * 获取显示验证信息用的元素
                 *
                 * @param {boolean} [dontCreate=false]
                 * 指定在没有找到已经存在的元素的情况下，不要额外创建
                 * @return {Validity}
                 * 返回一个已经放在DOM正确位置的{@link validator.Validity}控件
                 */
                getValidityLabel: function (dontCreate) {
                    if (!helper.isInStage(this, 'RENDERED')) {
                        return null;
                    }

                    var label = this.validityLabel
                        && this.viewContext.get(this.validityLabel);

                    if (!label && !dontCreate) {
                        var options = {
                            id: this.id + '-validity',
                            viewContext: this.viewContext
                        };
                        label = new ValidityLabel(options);
                        if (this.main.nextSibling) {
                            var nextSibling = this.main.nextSibling;
                            label.insertBefore(nextSibling);
                        }
                        else {
                            label.appendTo(this.main.parentNode);
                        }
                        this.validityLabel = label.id;
                    }

                    // Adjacent sibling selector not working with dynamically added class in IE7/8
                    // Put the class on a parent to force repainting 
                    if ((lib.ie === 8 || lib.ie === 7) && label) {
                        // otakustay赐名
                        lib.toggleClass(label.main.parentNode, 'fuck-the-ie');
                    }

                    return label;
                },

                /**
                 * 显示验证信息
                 *
                 * @param {validator.Validity} validity 验证结果
                 */
                showValidity: function (validity) {
                    if (this.validity) {
                        this.removeState(
                                'validity-' + this.validity.getValidState());
                    }
                    this.validity = validity;
                    this.addState('validity-' + validity.getValidState());

                    var label = this.getValidityLabel();

                    if (!label) {
                        return;
                    }

                    var properties = {
                        target: this,
                        focusTarget: this.getFocusTarget(),
                        validity: validity
                    };
                    label.setProperties(properties);
                },

                /**
                 * 直接显示验证消息
                 *
                 * @param {string} validState 验证状态，通常未通过验证为`"invalid"`
                 * @param {string} message 待显示的信息
                 */
                showValidationMessage: function (validState, message) {
                    message = message || '';
                    var validity = new Validity();
                    validity.setCustomValidState(validState);
                    validity.setCustomMessage(message);
                    this.showValidity(validity);
                },

                /**
                 * 销毁控件
                 *
                 * @override
                 */
                dispose: function () {
                    if (helper.isInStage(this, 'DISPOSED')) {
                        return;
                    }

                    var validityLabel = this.getValidityLabel(true);
                    if (validityLabel) {
                        validityLabel.dispose();
                    }
                    Control.prototype.dispose.apply(this, arguments);
                }
            };

            lib.inherits(InputControl, Control);
            return InputControl;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 下拉框控件
     * @author otakustay
     */
    define(
        'esui/Select',['require','underscore','./lib','./InputControl','./Layer','./painters','./main'],function (require) {
            var u = require('underscore');
            var lib = require('./lib');
            var InputControl = require('./InputControl');
            var Layer = require('./Layer');

            /**
             * 根据下拉弹层的`click`事件设置值
             *
             * @param {Event} e 触发事件的事件对象
             * @ignore
             */
            function selectValue(e) {
                var target = lib.event.getTarget(e);
                var layer = this.layer.getElement();
                while (target && target !== layer
                    && !lib.hasAttribute(target, 'data-index')
                    ) {
                    target = target.parentNode;
                }
                if (target && !this.helper.isPart(target, 'item-disabled')) {
                    var index = target.getAttribute('data-index');
                    this.set('selectedIndex', +index);
                    this.layer.hide();
                }
            }

            /**
             * `Select`控件使用的层类
             *
             * @extends Layer
             * @ignore
             * @constructor
             */
            function SelectLayer() {
                Layer.apply(this, arguments);
            }

            lib.inherits(SelectLayer, Layer);

            SelectLayer.prototype.nodeName = 'ol';

            SelectLayer.prototype.render = function (element) {
                var html = '';

                if (this.control.childName === 'yearSel') {
                    var dataIndex = this.control.datasource.length - 1;
                    for (var i = 0; i < this.control.datasource.length; i++) {
                        var item = this.control.datasource[dataIndex];
                        var classes = this.control.helper.getPartClasses('item');

                        html += '<li data-index="' + dataIndex + '" '
                            + 'class="' + classes.join(' ') + '">';
                        html += this.control.getItemHTML(item);
                        html += '</li>';
                        dataIndex--;
                    }
                } else {
                    for (var i = 0; i < this.control.datasource.length; i++) {
                        var item = this.control.datasource[i];
                        var classes = this.control.helper.getPartClasses('item');

                        html += '<li data-index="' + i + '" '
                            + 'class="' + classes.join(' ') + '">';
                        html += this.control.getItemHTML(item);
                        html += '</li>';
                    }
                }

                element.innerHTML = html;
            };

            SelectLayer.prototype.initBehavior = function (element) {
                this.control.helper.addDOMEvent(element, 'click', selectValue);
            };

            SelectLayer.prototype.syncState = function (element) {
                var classes = this.control.helper.getPartClasses('item-selected');

                var items = lib.getChildren(element);
                if (this.control.childName === 'yearSel') {
                    for (var i = items.length - 1; i >= 0; i--) {
                        var item = items[i];
                        var index = this.control.selectedIndex;
                        index = 32 - index;
                        if (i === index) {
                            lib.addClasses(item, classes);
                        }
                        else {
                            lib.removeClasses(item, classes);
                        }
                    }
                } else {
                    for (var i = items.length - 1; i >= 0; i--) {
                        var item = items[i];
                        if (i === this.control.selectedIndex) {
                            lib.addClasses(item, classes);
                        } else {
                            lib.removeClasses(item, classes);
                        }
                    }
                }
            };

            SelectLayer.prototype.dock = {
                strictWidth: true
            };

            /**
             * 下拉选择控件
             *
             * 类似HTML的`<select>`元素
             *
             * @extends InputControl
             * @constructor
             */
            function Select(options) {
                InputControl.apply(this, arguments);
                this.layer = new SelectLayer(this);
            }

            /**
             * 控件类型，始终为`"Select"`
             *
             * @type {string}
             * @readonly
             * @override
             */
            Select.prototype.type = 'Select';

            /**
             * 根据`selectedIndex` < `value` < `rawValue`的顺序调整三个参数的值
             *
             * @param {Object} context 有可能包含以上3个参数的参数对象
             * @ignore
             */
            function adjustValueProperties(context) {
                // 因为`value`可能多个项是相同的，所以必须一切以`selectedIndex`为准

                // 如果3个值都没有，就搞出默认值来
                if (context.selectedIndex == null
                    && context.rawValue == null
                    && context.value == null
                    ) {
                    context.selectedIndex = -1;
                }

                // 按`rawValue` > `value` > `selectedIndex`的顺序处理一下
                if (context.rawValue != null) {
                    context.value = null;
                    context.selectedIndex = null;
                }
                else if (context.value != null) {
                    context.selectedIndex = null;
                }

                // 如果没给`selectedIndex`，那么就用`value`或`rawValue`来找第一个对上的
                if (context.selectedIndex == null
                    && (context.value != null || context.rawValue != null)
                    ) {
                    // 以`rawValue`为优先
                    context.selectedIndex = -1;
                    var value = context.rawValue || context.value;
                    for (var i = 0; i < context.datasource.length; i++) {
                        if (context.datasource[i].value == value) {   // jshint ignore:line
                            context.selectedIndex = i;
                            break;
                        }
                    }
                    delete context.value;
                    delete context.rawValue;
                }

                // 有可能更换过`datasource`，或者给了一个不存在的`value`，
                // 则会导致`selectedIndex`无法同步，
                // 因此如果`selectedIndex`在数组范围外，要根据`emptyText`来决定修正
                if (context.selectedIndex < 0
                    || context.selectedIndex >= context.datasource.length
                    ) {
                    if (context.emptyText) {
                        context.selectedIndex = -1;
                    }
                    else {
                        // 找最近的一个未禁用的项
                        context.selectedIndex = -1;
                        for (var i = 0; i < context.datasource.length; i++) {
                            if (!context.datasource[i].disabled) {
                                context.selectedIndex = i;
                                break;
                            }
                        }
                    }
                }
            }

            /**
             * 初始化参数
             *
             * 如果初始化时未给定{@link Select#datasource}属性，
             * 且主元素是`<select>`元素，则按以下规则生成一个数据源：
             *
             * 1. 遍历主元素下所有`<option>`元素
             * 2. 以`<option>`元素的`name`属性作为数据项的`name`
             * 3. 如果`<option>`元素没有`name`属性，则使用`text`属性
             * 4. 以`<option>`元素的`value`属性作为数据项的`value`
             * 5. 如果`<option>`元素处于禁用状态，则此数据项同样禁用
             * 6. 如果`<option>`处于选中状态，
             * 且初始化未给定{@link Select#selectedIndex 值相关的属性}，
             * 则使用此项的下标作为{@link Select#selectedIndex}属性
             *
             * 如果主元素是`<select>`元素，控件会从主元素上抽取相关DOM属性作为控件自身的值，
             * 详细参考{@link Helper#extractOptionsFromInput}方法
             *
             * @param {Object} [options] 构造函数传入的参数
             * @protected
             * @override
             */
            Select.prototype.initOptions = function (options) {
                var defaults = {
                    datasource: []
                };

                var properties = {};
                u.extend(properties, defaults, options);

                // 如果主元素是个`<select>`元素，则需要从元素中抽取数据源，
                // 这种情况下构造函数中传入的`datasource`无效
                if (this.main.nodeName.toLowerCase() === 'select') {
                    properties.datasource = [];
                    var elements = this.main.getElementsByTagName('option');
                    for (var i = 0, length = elements.length; i < length; i++) {
                        var item = elements[i];
                        var dataItem = {
                            name: item.name || item.text,
                            value: item.value
                        };
                        if (item.disabled) {
                            dataItem.disabled = true;
                        }

                        properties.datasource.push(dataItem);

                        // 已经选择的那个会作为值，
                        // 但如果构造函数中有传入和值相关的选项，则跳过这段逻辑
                        if (item.selected
                            && properties.selectedIndex == null
                            && properties.value == null
                            && properties.rawValue == null
                            ) {
                            // 无值的话肯定是在最前面
                            properties.selectedIndex = item.value ? i : 0;
                        }
                    }

                    this.helper.extractOptionsFromInput(this.main, properties);
                }

                if (typeof properties.selectedIndex === 'string') {
                    properties.selectedIndex = +properties.selectedIndex;
                }

                this.setProperties(properties);
            };

            /**
             * 每个节点显示的内容的模板
             *
             * 在模板中可以使用以下占位符：
             *
             * - `{string} text`：文本内容，经过HTML转义
             *
             * @type {string}
             */
            Select.prototype.itemTemplate = '<span>${text}</span>';

            /**
             * 获取每个节点显示的内容
             *
             * @param {meta.SelectItem} item 当前节点的数据项
             * @return {string} 返回HTML片段
             */
            Select.prototype.getItemHTML = function (item) {
                var data = {
                    text: u.escape(item.name || item.text),
                    value: u.escape(item.value)
                };
                return lib.format(this.itemTemplate, data);
            };

            /**
             * 显示选中值的模板
             *
             * 在模板中可以使用以下占位符：
             *
             * - `{string} text`：文本内容，经过HTML转义
             *
             * @type {string}
             */
            Select.prototype.displayTemplate = '${text}';

            /**
             * 获取选中值的内容
             *
             * @param {meta.SelectItem | null} item 选中节点的数据项，
             * 如果{@link Select#emptyText}属性值不为空且未选中任何节点，则传递`null`
             * @return {string} 显示的HTML片段
             */
            Select.prototype.getDisplayHTML = function (item) {
                if (!item) {
                    return u.escape(this.emptyText || '');
                }

                var data = {
                    text: u.escape(item.name || item.text),
                    value: u.escape(item.value)
                };
                return lib.format(this.displayTemplate, data);
            };

            /**
             * 初始化DOM结构
             *
             * @protected
             * @override
             */
            Select.prototype.initStructure = function () {
                // 如果主元素是`<select>`，删之替换成`<div>`
                if (this.main.nodeName.toLowerCase() === 'select') {
                    this.helper.replaceMain();
                }

                this.main.tabIndex = 0;

                this.main.innerHTML = this.helper.getPartHTML('text', 'span');
            };

            /**
             * 初始化事件交互
             *
             * @protected
             * @override
             */
            Select.prototype.initEvents = function () {
                this.helper.addDOMEvent(this.main, 'click', u.bind(this.layer.toggle, this.layer));
                this.layer.on('rendered', u.bind(addLayerClass, this));
            };

            function addLayerClass() {
                this.fire('layerrendered', { layer: this.layer });
            }

            /**
             * 根据控件的值更新其视图
             *
             * @param {Select} select 控件实例
             * @ignore
             */
            function updateValue(select) {
                // 同步显示的文字
                var textHolder = select.helper.getPart('text');
                var selectedItem = select.selectedIndex === -1
                    ? null
                    : select.datasource[select.selectedIndex];
                textHolder.innerHTML = select.getDisplayHTML(selectedItem);

                var layerElement = select.layer.getElement(false);
                if (layerElement) {
                    select.layer.syncState(layerElement);
                }
            }

            /**
             * 获取原始值
             *
             * @return {Mixed}
             * @override
             */
            Select.prototype.getRawValue = function () {
                if (this.selectedIndex < 0) {
                    return null;
                }

                var item = this.datasource[this.selectedIndex];

                return item ? item.value : null;
            };

            var paint = require('./painters');
            /**
             * 重渲染
             *
             * @method
             * @protected
             * @override
             */
            Select.prototype.repaint = paint.createRepaint(
                InputControl.prototype.repaint,
                /**
                 * @property {number} width
                 *
                 * 宽度
                 */
                paint.style('width'),
                /**
                 * @property {number} height
                 *
                 * 高度，指浮层未展开时的可点击元素的高度， **与浮层高度无关**
                 */
                paint.style('height'),
                {
                    /**
                     * @property {meta.SelectItem[]} datasource
                     *
                     * 数据源，其中每一项生成浮层中的一条
                     */
                    name: 'datasource',
                    paint: function (select) {
                        select.layer.repaint();
                    }
                },
                {
                    /**
                     * @property {number} selectedIndex
                     *
                     * 选中项的索引
                     *
                     * 本控件有3个属性能影响选中值，分别为{@link Select#selectedIndex}、
                     * {@link Select#value}和{@link Select#rawValue}
                     *
                     * 当这三个属性同时存在两个或多个时，它们之间按以下优先级处理：
                     *
                     *     selectedIndex < value < rawValue
                     *
                     * 即当{@link Select#rawValue}存在时，即便有{@link Select#value}或
                     * {@link Select#selectedIndex}属性，也会被忽略
                     *
                     * 当{@link Select#emptyText}不为空时，此属性可以为`-1`，
                     * 其它情况下属性值必须大于或等于`0`
                     */

                    /**
                     * @property {string} emptyText
                     *
                     * 未选中任何项时显示的值
                     *
                     * 当此属性不为空，且{@link Select#selectedIndex}属性的值为`-1`时，
                     * 控件处于未选中任何项的状态，此时将显示此属性的内容
                     */
                    name: ['selectedIndex', 'emptyText', 'datasource'],
                    paint: updateValue
                },
                {
                    name: ['disabled', 'hidden', 'readOnly'],
                    paint: function (select, disabled, hidden, readOnly) {
                        if (disabled || hidden || readOnly) {
                            select.layer.hide();
                        }
                    }
                }
            );

            /**
             * 更新{@link Select#datasource}属性，无论传递的值是否变化都会进行更新
             *
             * @param {meta.SelectItem[]} datasource 新的数据源对象
             */
            Select.prototype.updateDatasource = function (datasource) {
                if (!datasource) {
                    datasource = this.datasource;
                }
                this.datasource = datasource;
                var record = { name: 'datasource' };
                this.repaint([record], { datasource: record });
            };

            /**
             * 批量更新属性并重绘
             *
             * @param {Object} properties 需更新的属性
             * @override
             * @fires change
             */
            Select.prototype.setProperties = function (properties) {
                // 为了`adjustValueProperties`正常工作，需要加上一点东西，
                // 由于在`setProperties`有相等判断，所以额外加相同的东西不影响逻辑
                if (properties.datasource == null) {
                    properties.datasource = this.datasource;
                }

                /**
                 * @property {string} value
                 *
                 * 字符串形式的值
                 *
                 * 该属性是将选中的{@link meta.SelectItem}中的`value`属性转为字符串后返回
                 *
                 * 对于属性的优先级，参考{@link Select#selectedIndex}属性的说明
                 *
                 * @override
                 */

                /**
                 * @property {Mixed} rawValue
                 *
                 * 控件的原始值
                 *
                 * 该属性是将选中的{@link meta.SelectItem}中的`value`属性直接返回
                 *
                 * 对于属性的优先级，参考{@link Select#selectedIndex}属性的说明
                 *
                 * @override
                 */
                if (properties.value == null
                    && properties.rawValue == null
                    && properties.selectedIndex == null
                    && properties.datasource === this.datasource
                    ) {
                    properties.selectedIndex = this.selectedIndex;
                }
                if (!properties.hasOwnProperty('emptyText')) {
                    properties.emptyText = this.emptyText;
                }

                adjustValueProperties(properties);
                var changes =
                    InputControl.prototype.setProperties.apply(this, arguments);

                if (changes.hasOwnProperty('selectedIndex')) {
                    /**
                     * @event change
                     *
                     * 值发生变化时触发
                     *
                     * `Select`控件的值变化是以{@link Select#selectedIndex}属性为基准
                     */
                    this.fire('change');
                }

                return changes;
            };

            /**
             * 销毁控件
             *
             * @override
             */
            Select.prototype.dispose = function () {
                if (this.helper.isInStage('DISPOSED')) {
                    return;
                }

                if (this.layer) {
                    this.layer.dispose();
                    this.layer = null;
                }

                InputControl.prototype.dispose.apply(this, arguments);
            };

            /**
             * 获取当前选中的{@link meta.SelectItem}对象
             *
             * @return {meta.SelectItem}
             */
            Select.prototype.getSelectedItem = function () {
                return this.get('datasource')[this.get('selectedIndex')];
            };

            lib.inherits(Select, InputControl);
            require('./main').register(Select);
            return Select;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file Panel控件
     * @author otakustay
     */
    define(
        'esui/Panel',['require','underscore','./lib','./Control','./painters','./main','./main'],function (require) {
            var u = require('underscore');
            var lib = require('./lib');
            var Control = require('./Control');

            /**
             * 通用面板
             *
             * 本身没有特别意义，仅作为一个容器存在，方便显示/隐藏等操作
             *
             * 需要特别注意的是，对面板进行`disable()`操作，并不会禁用其内部的控件，
             * 对控件进行批量禁用/启用操作，请使用{@link ViewContext#getGroup}
             * 及{@link ControlCollection}提供的相关方法
             *
             * @extends Control
             * @constructor
             */
            function Panel() {
                Control.apply(this, arguments);
            }

            /**
             * 控件类型，始终为`"Panel"`
             *
             * @type {string}
             * @readonly
             * @override
             */
            Panel.prototype.type = 'Panel';

            /**
             * 获取控件的分类
             *
             * @return {string} 始终返回`"container"`
             * @override
             */
            Panel.prototype.getCategory = function () {
                return 'container';
            };

            /**
             * 创建控件主元素
             *
             * 如果初始化时提供{@link Panel#tagName}属性，则以此创建元素，
             * 默认使用`<div>`元素
             *
             * @param {Object} options 构造函数传入的参数
             * @return {HTMLElement}
             * @protected
             * @override
             */
            Panel.prototype.createMain = function (options) {
                if (!options.tagName) {
                    return Control.prototype.createMain.call(this);
                }
                return document.createElement(options.tagName);
            };

            /**
             * 初始化参数
             *
             * 如果初始化时提供了主元素，则使用主元素的标签名作为{@link Panel#tagName}属性
             *
             * @param {Object} [options] 构造函数传入的参数
             * @protected
             * @override
             */
            Panel.prototype.initOptions = function (options) {
                var properties = {};
                u.extend(properties, options);
                /**
                 * @property {string} tagName
                 *
                 * 指定主元素标签名
                 *
                 * 此属性仅在初始化时生效，运行期不能修改
                 *
                 * @readonly
                 */
                properties.tagName = this.main.nodeName.toLowerCase();
                this.setProperties(properties);
            };

            /**
             * 重渲染
             *
             * @method
             * @protected
             * @override
             */
            Panel.prototype.repaint = require('./painters').createRepaint(
                Control.prototype.repaint,
                {
                    /**
                     * @property {string} content
                     *
                     * 面板的内容，为一个HTML片段
                     *
                     * 此属性中可包含ESUI相关的属性，在设置内容后，
                     * 会使用{@link Helper#initChildren}进行内部控件的初始化
                     */
                    name: 'content',
                    paint: function (panel, content) {
                        // 第一次刷新的时候是可能没有`content`的，
                        // 这时在`innerHTML`上就地创建控件，不要刷掉内容，
                        // 后续有要求`content`是字符串，所以不管非字符串的后果
                        if (content != null) {
                            panel.helper.disposeChildren();
                            panel.main.innerHTML = content;
                        }
                        panel.helper.initChildren();
                    }
                }
            );

            /**
             * 设置内容
             *
             * @param {string} html 内容HTML，具体参考{@link Panel#content}属性的说明
             */
            Panel.prototype.setContent = function (html) {
                this.setProperties({ content: html });
            };

            /**
             * 追加内容
             *
             * @param {string} html 追加内容的HTML代码
             * @param {boolean} isPrepend 是否加到面板最前面
             * @ignore
             */
            function addContent(html, isPrepend) {
                var main = this.main;
                var container = document.createElement('div');
                container.innerHTML = html;

                var options = u.extend({}, this.renderOptions, {
                    viewContext: this.viewContext,
                    parent: this
                });

                var childNodes = container.childNodes;
                var children = [];
                for (var i = 0; i < childNodes.length; i++) {
                    children.push(childNodes[i]);
                }

                var ui = require('./main');
                u.each(children, function (child) {
                    if (isPrepend) {
                        main.insertBefore(child, main.firstChild);
                    }
                    else {
                        main.appendChild(child);
                    }
                    ui.init(main, options);
                });
            }

            /**
             * 在面板最前面追加内容
             *
             * @param {string} html 追加内容的HTML代码
             */
            Panel.prototype.prependContent = function (html) {
                addContent.call(this, html, true);
            };

            /**
             * 在面板最后面追加内容
             *
             * @param {string} html 追加内容的HTML代码
             */
            Panel.prototype.appendContent = function (html) {
                addContent.call(this, html, false);
            };

            /**
             * 统一化样式名
             *
             * @param {string} name 样式名称
             * @return {string} 统一化后`camelCase`的样式名称
             * @ignore
             */
            function normalizeStyleName(name) {
                if (name.indexOf('-') >= 0) {
                    name = name.replace(
                        /-\w/g,
                        function (word) {
                            return word.charAt(1).toUpperCase();
                        }
                    );
                }

                return name;
            }

            /**
             * 获取样式，仅获取设置的样式，不包含外部CSS给定的
             *
             * @param {string} name 样式名称
             * @return {string}
             */
            Panel.prototype.getStyle = function (name) {
                name = normalizeStyleName(name);
                return this.main
                    ? this.main.style[name]
                    : '';
            };

            /**
             * 设置样式
             *
             * @param {string} name 样式名称，如果只有这一个参数，则表示为整串样式
             * @param {string} [value=""] 样式值
             */
            Panel.prototype.setStyle = function (name, value) {
                name = normalizeStyleName(name);
                if (this.main) {
                    this.main.style[name] = value || '';
                }
            };

            lib.inherits(Panel, Control);
            require('./main').register(Panel);
            return Panel;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @file 单月日历
     * @author dbear
     */

    define(
        'esui/MonthView',['require','./Button','./Select','./Panel','./lib','./controlHelper','./Control','./main','moment'],function (require) {
            require('./Button');
            require('./Select');
            require('./Panel');
            var lib = require('./lib');
            var helper = require('./controlHelper');
            var Control = require('./Control');
            var ui = require('./main');
            var m = require('moment');

            /**
             * 日历控件类
             *
             * @constructor
             * @param {Object} options 初始化参数
             */
            function MonthView(options) {
                Control.apply(this, arguments);
            }

            /**
             * 获取可选择的年列表
             *
             * @param {MonthView} monthView MonthView控件实例
             * @inner
             * @return {Array}
             */
            function getYearOptions(monthView) {
                var range = monthView.viewRange || monthView.range;
                var ds = [];
                var end = range.end.getFullYear();

                for (var i = range.begin.getFullYear(); i <= end; i++) {
                    ds.push({text: i, value: i});
                }

                return ds;
            }

            /**
             * 获取可选择的月列表
             *
             * @param {MonthView} monthView MonthView控件实例
             * @param {number} year 选中的年
             * @inner
             * @return {Array}
             */
            function getMonthOptions(monthView, year) {
                var range = monthView.viewRange || monthView.range;
                var ds = [];
                var len = 11;

                var i = 0;

                if (year === range.begin.getFullYear()) {
                    i = range.begin.getMonth();
                }

                if (year === range.end.getFullYear()) {
                    len = range.end.getMonth();
                }

                for (; i <= len; i++) {
                    ds.push({text: (i + 1), value: i});
                }

                return ds;
            }

            /**
             * 获取日历弹出层的HTML
             *
             * @param {MonthView} monthView MonthView控件实例
             * @inner
             */
            function getMainHTML(monthView) {
                var tpl = [
                    '<div class="${headClass}"><table><tr>',
                    '<td width="40" align="left">',
                    '<div class="${monthBackClass}"',
                    ' data-ui-type="Button"',
                    ' data-ui-child-name="monthBack"',
                    ' data-ui-id="${monthBackId}"',
                    '></div>',
                    '</td>',
                    '<td>',
                    '<div class="${yearSelectClass}"',
                    ' data-ui="type:Select;childName:yearSel;',
                    ' id:${yearSelId};"></div>',
                    '</td>',
                    '<td>',
                    '<div class="${monthSelectClass}"',
                    ' data-ui="type:Select;childName:monthSel;',
                    ' id:${monthSelId};"></div>',
                    '</td>',
                    '<td width="40" align="right">',
                    '<div class="${monthForClass}"',
                    ' data-ui-type="Button"',
                    ' data-ui-child-name="monthForward"',
                    ' data-ui-id="${monthForwardId}"',
                    '></div>',
                    '</td>',
                    '</tr></table></div>',
                    '<div id="${monthMainId}" class="${monthMainClass}"></div>'
                ];
                tpl = tpl.join('');

                return lib.format(
                    tpl,
                    {
                        headClass: monthView.helper.getPartClassName('head'),
                        monthBackId: monthView.helper.getId('monthBack'),
                        monthForwardId: monthView.helper.getId('monthForward'),
                        yearSelId: monthView.helper.getId('yearSel'),
                        monthSelId: monthView.helper.getId('monthSel'),
                        monthMainId: monthView.helper.getId('monthMain'),
                        monthMainClass: monthView.helper.getPartClassName('month'),
                        monthBackClass:
                            monthView.helper.getPartClassName('month-back'),
                        monthForClass:
                            monthView.helper.getPartClassName('month-forward'),
                        yearSelectClass:
                            monthView.helper.getPartClassName('year-select'),
                        monthSelectClass:
                            monthView.helper.getPartClassName('month-select')
                    }
                );
            }

            /**
             * 日历月份显示单元的HTML
             *
             * @param {MonthView} monthView MonthView控件实例
             * @inner
             */
            function getMonthMainHTML(monthView) {

                /** 绘制表头 */
                // 标题显示配置
                var titles = [];
                if (monthView.mode === 'multi') {
                    titles.push('');
                }
                titles = titles.concat(['一', '二', '三', '四', '五', '六', '日']);

                // 日期表格头的模板
                var tplHead = ''
                    + '<table border="0" cellpadding="0" cellspacing="0" '
                    + 'class="${className}"><thead><tr>';

                var html = [];
                html.push(
                    lib.format(
                        tplHead,
                        {
                            className:
                                monthView.helper.getPartClassName('month-main')
                        }
                    )
                );

                // 日期表格头单元的模板
                var tplHeadItem = ''
                    + '<td id="${id}" data-index="${index}" class="${className}">'
                    + '${text}</td>';
                var headItemClass =
                    monthView.helper.getPartClassName('month-title');
                var headItemId = monthView.helper.getId('month-title');
                var emptyHeadItemClass =
                    monthView.helper.getPartClassName('month-select-all');

                var tLen = titles.length;
                for (var tIndex = 0; tIndex < tLen; tIndex++) {
                    html.push(
                        lib.format(
                            tplHeadItem,
                            {
                                className: titles[tIndex] === ''
                                    ? emptyHeadItemClass : headItemClass,
                                text: titles[tIndex],
                                index: tIndex,
                                id: headItemId + '-' + tIndex
                            }
                        )
                    );
                }
                html.push('</tr></thead><tbody><tr>');

                /** 绘制表体 */
                // 日期单元的模板
                var tplItem = ''
                    + '<td data-year="${year}" data-month="${month}" '
                    + 'data-date="${date}" class="${className}" '
                    + 'id="${id}">${date}</td>';

                // 单行全选模板
                var rowSelectClass =
                    monthView.helper.getPartClassName('month-row-select');
                var tplRowSelectId = monthView.helper.getId('row-select');
                var rowTagIndex = 0;
                var tplRowSelectTpl = ''
                    + '<td id="${id}" class="' + rowSelectClass + '">&gt;</td>';

                var index = 0;
                var year = monthView.year;
                var month = monthView.month;
                var repeater = new Date(year, month, 1);
                var nextMonth = new Date(year, month + 1, 1);
                var begin = 1 - (repeater.getDay() + 6) % 7;
                repeater.setDate(begin);

                var itemClass = monthView.helper.getPartClassName('month-item');

                var todayClass =
                    monthView.helper.getPartClassName('month-item-today');

                var virClass =
                    monthView.helper.getPartClassName('month-item-virtual');
                var disabledClass =
                    monthView.helper.getPartClassName('month-item-disabled');
                var range = monthView.range;

                if (monthView.mode === 'multi') {
                    html.push(lib.format(
                        tplRowSelectTpl,
                        {'id': tplRowSelectId + '-' + rowTagIndex++}
                    ));
                }
                while (nextMonth - repeater > 0 || index % 7 !== 0) {
                    if (begin > 1 && index % 7 === 0) {
                        html.push('</tr><tr>');

                        if (monthView.mode === 'multi') {
                            html.push(lib.format(
                                tplRowSelectTpl,
                                {'id': tplRowSelectId + '-' + rowTagIndex++}
                            ));
                        }
                    }

                    // 不属于当月的日期
                    var virtual = (repeater.getMonth() !== month);

                    // 不可选的日期
                    var disabled = false;

                    //range定义的begin之前的日期不可选
                    if (repeater < range.begin) {
                        disabled = true;
                    }
                    else if (repeater > range.end) {
                        disabled = true;
                    }
                    // 构建date的css class
                    var currentClass = itemClass;
                    if (virtual) {
                        currentClass += ' ' + virClass;
                    }
                    else if (m().isSame(repeater, 'day')) {
                        currentClass += ' ' + todayClass;
                    }
                    if (disabled) {
                        currentClass += ' ' + disabledClass;
                    }

                    html.push(
                        lib.format(
                            tplItem,
                            {
                                year: repeater.getFullYear(),
                                month: repeater.getMonth(),
                                date: repeater.getDate(),
                                className: currentClass,
                                id: getItemId(monthView, repeater)
                            }
                        )
                    );

                    repeater = new Date(year, month, ++begin);
                    index++;
                }
                monthView.rowTagNum = rowTagIndex;

                html.push('</tr></tbody></table>');
                return html.join('');
            }

            /**
             * 获取日期对应的dom元素item的id
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {Date} date 日期.
             * @return {string}
             */
            function getItemId(monthView, date) {
                return monthView.helper.getId(
                        date.getFullYear()
                        + '-' + date.getMonth()
                        + '-' + date.getDate()
                );
            }


            /**
             * 日历元素点击事件
             *
             * @inner
             * @param {MonthView} this MonthView控件实例
             * @param {Event} 触发事件的事件对象
             */
            function monthViewClick(e) {
                var tar = e.target || e.srcElement;
                var allSelectClasses =
                    helper.getPartClasses(this, 'month-select-all');
                var headClasses = helper.getPartClasses(this, 'month-title');
                var itemClasses = helper.getPartClasses(this, 'month-item');
                var rowSelectClasses =
                    helper.getPartClasses(this, 'month-row-select');
                var virClasses =
                    helper.getPartClasses(this, 'month-item-virtual');
                var disabledClasses =
                    helper.getPartClasses(this, 'month-item-disabled');
                while (tar && tar !== document.body) {
                    if (lib.hasClass(tar, itemClasses[0])
                        && !lib.hasClass(tar, virClasses[0])
                        && !lib.hasClass(tar, disabledClasses[0])) {
                        selectByItem(this, tar);
                        return;
                    }
                    // 点击行批量选中
                    else if (this.mode === 'multi'){
                        if (lib.hasClass(tar, rowSelectClasses[0])) {
                            selectByTagClick(this, tar);
                            return;
                        }
                        if (lib.hasClass(tar, headClasses[0])) {
                            selectByColumn(this, tar);
                            return;
                        }
                        if (lib.hasClass(tar, allSelectClasses[0])) {
                            selectAll(this);
                            return;
                        }
                    }
                    tar = tar.parentNode;
                }
            }


            /**
             * 将元数据转换为简单格式
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             */
            function parseToCache(monthView) {
                var rawValue = monthView.rawValue;
                monthView.viewValue = {};
                for (var i = 0; i < rawValue.length; i++) {
                    var singleDay = rawValue[i];
                    var year = singleDay.getFullYear();
                    var month = singleDay.getMonth();
                    var date = singleDay.getDate();
                    var id = year + '-' + month + '-' + date;
                    monthView.viewValue[id] = {
                        isSelected: true,
                        value: new Date(year, month, date)
                    };
                }

            }

            /**
             * 该元素是否可以选择
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {HTMLElement} dateItem 日期节点
             * @return {number} 1: 可以选择 -1: 虚拟日期 0:
             */
            function isItemSelectable(monthView, dateItem) {
                var virtualClasses =
                    helper.getPartClasses(monthView, 'month-item-virtual');
                var disabledClasses =
                    helper.getPartClasses(monthView, 'month-item-disabled');
                // 既不是范围外的，又不是虚拟的
                if(!lib.hasClass(dateItem, virtualClasses[0])
                    && !lib.hasClass(dateItem, disabledClasses[0])) {
                    return 1;
                }
                // 虚拟的但不是范围外的
                else if (lib.hasClass(dateItem, virtualClasses[0])
                    && !lib.hasClass(dateItem, disabledClasses[0])) {
                    return -1;
                }
                return 0;
            }

            /**
             * 更新横向批量选择按钮状态
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {HTMLElement} rowTagItem 横向批量选择按钮
             * @param {boolean} isSelected 置为已选还是未选
             */
            function setRowTagSelected(monthView, rowTagItem, isSelected) {
                helper.removePartClasses(
                    monthView, 'month-row-select-selected', rowTagItem
                );
                if (isSelected) {
                    helper.addPartClasses(
                        monthView, 'month-row-select-selected', rowTagItem
                    );
                }
            }

            /**
             * 批量渲染横向批量选择按钮状态
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             */
            function batchRepaintRowTag(monthView) {
                var rowTagNum = monthView.rowTagNum;
                var rowTagId = helper.getId(monthView, 'row-select');

                for (var i = 0; i < rowTagNum; i++) {
                    var rowTag = lib.g(rowTagId + '-' + i);
                    // 遍历这一行，如果都选了，则置为选择状态
                    repaintRowTag(monthView, rowTag);
                }
            }

            /**
             * 渲染特定横向批量选择按钮状态
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {HTMLElement} rowTagItem 横向批量选择按钮
             */
            function repaintRowTag(monthView, rowTag) {
                var selectedClasses =
                    helper.getPartClasses(monthView, 'month-item-selected');
                var dateItem = rowTag.nextSibling;
                var isAllSelected = true;
                var selectableNum = 0;
                while (dateItem) {
                    if (isItemSelectable(monthView, dateItem) === 1) {
                        ++selectableNum;
                        if (!lib.hasClass(dateItem, selectedClasses[0])) {
                            isAllSelected = false;
                            break;
                        }
                    }
                    dateItem = dateItem.nextSibling;
                }
                if (selectableNum === 0) {
                    isAllSelected = false;
                }
                setRowTagSelected(monthView, rowTag, isAllSelected);
            }

            /**
             * 整列选中日期
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {Element} item 行箭头元素.
             */
            function selectByColumn(monthView, columnTag) {
                var index = columnTag.getAttribute('data-index');
                var columnSelectedClasses =
                    helper.getPartClasses(monthView, 'month-title-selected');

                var selectAll = true;
                if (lib.hasClass(columnTag, columnSelectedClasses[0])) {
                    selectAll = false;
                    helper.removePartClasses(
                        monthView, 'month-title-selected', columnTag
                    );
                }
                else {
                    helper.addPartClasses(
                        monthView, 'month-title-selected', columnTag
                    );
                }

                // 可以通过rowTag寻找节点
                var rowTagNum = monthView.rowTagNum;
                var rowTagId = helper.getId(monthView, 'row-select');

                var viewValue = monthView.viewValue;
                var changedDates = [];

                for (var i = 0; i < rowTagNum; i++) {
                    var rowTag = lib.g(rowTagId + '-' + i);
                    // 找到第index个节点，置为选择状态
                    var sibling = rowTag.parentNode.children[index];
                    if (isItemSelectable(monthView, sibling) === 1) {
                        var date = sibling.getAttribute('data-date');
                        var month = sibling.getAttribute('data-month');
                        var year = sibling.getAttribute('data-year');
                        var id = year + '-' + month + '-' + date;
                        viewValue[id] = {
                            isSelected: selectAll,
                            value: new Date(year, month, date)
                        };
                        changedDates.push(id);
                    }
                }

                if (changedDates && changedDates.length > 0) {
                    updateMultiRawValue(monthView);
                    updateMultiSelectState(monthView, changedDates, selectAll);
                    // 同步横行批量选择状态
                    batchRepaintRowTag(monthView);
                    repaintAllSelectTag(monthView);
                }
            }

            /**
             * 更新纵向批量选择按钮状态
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {HTMLElement} columnTagItem 纵向批量选择按钮
             * @param {boolean} isSelected 置为已选还是未选
             */
            function setColumnTagSelected(monthView, columnTagItem, isSelected) {
                helper.removePartClasses(
                    monthView, 'month-title-selected', columnTagItem
                );
                if (isSelected) {
                    helper.addPartClasses(
                        monthView, 'month-title-selected', columnTagItem
                    );
                }
            }

            /**
             * 批量渲染纵向批量选择按钮状态
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             */
            function batchRepaintColumnTag(monthView) {
                var headItemId = helper.getId(monthView, 'month-title');
                for (var i = 1; i <= 7; i++) {
                    var columnTag = lib.g(headItemId + '-' + i);
                    // 遍历这一行，如果都选了，则置为选择状态
                    repaintColumnTag(monthView, columnTag);
                }
            }

            /**
             * 渲染特定纵向批量选择按钮状态
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {HTMLElement} columnTagItem 纵向批量选择按钮
             */
            function repaintColumnTag(monthView, columnTagItem) {
                var selectedClasses =
                    helper.getPartClasses(monthView, 'month-item-selected');
                var index = columnTagItem.getAttribute('data-index');
                var isAllSelected = true;
                var selectableNum = 0;

                // 可以通过rowTag寻找节点
                var rowTagNum = monthView.rowTagNum;
                var rowTagId = helper.getId(monthView, 'row-select');

                for (var i = 0; i < rowTagNum; i++) {
                    var rowTag = lib.g(rowTagId + '-' + i);
                    // 找到第index个节点，置为选择状态
                    var sibling = rowTag.parentNode.children[index];
                    if (isItemSelectable(monthView, sibling) === 1) {
                        ++selectableNum;
                        if (!lib.hasClass(sibling, selectedClasses[0])) {
                            isAllSelected = false;
                            break;
                        }
                    }
                }

                if (selectableNum === 0) {
                    isAllSelected = false;
                }

                setColumnTagSelected(monthView, columnTagItem, isAllSelected);
            }

            /**
             * 整行选中日期
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {Element} rowTag 行箭头元素.
             */
            function selectByTagClick(monthView, rowTag) {
                var row = rowTag.parentNode;
                var rowSelectClasses =
                    helper.getPartClasses(monthView, 'month-row-select');
                var rowSelectedClasses =
                    helper.getPartClasses(monthView, 'month-row-select-selected');
                var virtualClasses =
                    helper.getPartClasses(monthView, 'month-item-virtual');
                var disabledClasses =
                    helper.getPartClasses(monthView, 'month-item-disabled');

                var selectAll = true;
                if (lib.hasClass(rowTag, rowSelectedClasses[0])) {
                    selectAll = false;
                    helper.removePartClasses(
                        monthView, 'month-row-select-selected', rowTag
                    );
                }
                else {
                    helper.addPartClasses(
                        monthView, 'month-row-select-selected', rowTag
                    );
                }

                var children = row.children;
                var viewValue = monthView.viewValue;
                var changedDates = [];

                for (var i = 0; i < children.length; i++) {
                    var child = children[i];
                    if (child.nodeType === 1
                        && !lib.hasClass(child, rowSelectClasses[0])
                        && !lib.hasClass(child, virtualClasses[0])
                        && !lib.hasClass(child, disabledClasses[0])) {
                        var date = child.getAttribute('data-date');
                        var month = child.getAttribute('data-month');
                        var year = child.getAttribute('data-year');
                        var id = year + '-' + month + '-' + date;
                        viewValue[id] = {
                            isSelected: selectAll,
                            value: new Date(year, month, date)
                        };
                        changedDates.push(id);
                    }
                }

                if (changedDates && changedDates.length > 0) {
                    updateMultiRawValue(monthView);
                    updateMultiSelectState(monthView, changedDates, selectAll);
                    batchRepaintColumnTag(monthView);
                    repaintAllSelectTag(monthView);
                }
            }

            /**
             * 更新全选状态
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             */
            function repaintAllSelectTag(monthView) {
                // 获取横向选择状态
                var rowTagNum = monthView.rowTagNum;
                var rowTagId = helper.getId(monthView, 'row-select');
                var selectAllTag = lib.g(helper.getId(monthView, 'month-title-0'));
                var rowSelectedClasses =
                    helper.getPartClasses(monthView, 'month-row-select-selected');
                var selectedRowNum = 0;
                for (var i = 0; i < rowTagNum; i++) {
                    var rowTag = lib.g(rowTagId + '-' + i);
                    if (lib.hasClass(rowTag, rowSelectedClasses[0])) {
                        selectedRowNum ++;
                    }
                }

                if (selectedRowNum === rowTagNum) {
                    helper.addPartClasses(
                        monthView, 'month-select-all-selected', selectAllTag);
                }
                else {
                    helper.removePartClasses(
                        monthView, 'month-select-all-selected', selectAllTag);
                }
            }


            /**
             * 选择全部
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             */
            function selectAll(monthView) {
                // 获取横向选择状态
                var rowTagNum = monthView.rowTagNum;
                var rowTagId = helper.getId(monthView, 'row-select');
                for (var i = 0; i < rowTagNum; i++) {
                    var rowTag = lib.g(rowTagId + '-' + i);
                    // 先移除所有的选择
                    helper.removePartClasses(
                        monthView, 'month-row-select-selected', rowTag
                    );
                    selectByTagClick(monthView, rowTag);
                }
            }

            /**
             * 选择当前日期
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {Array} dates 日期集合，每个元素的格式：YYYY-MM-DD
             * @param {boolean} selectAll 批量选中还是批量不选.
             */
            function updateMultiRawValue(monthView) {
                // 重排cache
                var selectedDates = [];
                for (var key in monthView.viewValue) {
                    if (monthView.viewValue[key].isSelected) {
                        selectedDates.push(monthView.viewValue[key].value);
                    }
                }
                selectedDates.sort(function(a, b) { return a - b; });
                monthView.rawValue = selectedDates;
                monthView.fire('change');
            }

            function updateMultiSelectState(monthView, dates, selectAll) {
                if (selectAll) {
                    paintMultiSelected(monthView, dates);
                }
                else {
                    resetMultiSelected(monthView, dates);
                }
            }

            /**
             * 批量清空多选日历未选中的日期
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {Array} dates 日期集合.
             */
            function resetMultiSelected(monthView, dates) {
                var me = monthView;
                for (var i = 0; i < dates.length; i++) {
                    var id = helper.getId(monthView, dates[i]);
                    var item = lib.g(id);
                    if (item) {
                        lib.removeClasses(
                            item,
                            helper.getPartClasses(me, 'month-item-selected')
                        );
                    }
                }
            }

            /**
             * 批量绘制多选日历选中的日期
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {Array} dates 日期集合.
             */
            function paintMultiSelected(monthView, dates) {
                var me = monthView;
                for (var i = 0; i < dates.length; i++) {
                    var id = helper.getId(monthView, dates[i]);
                    var item = lib.g(id);
                    if (item) {
                        lib.addClasses(
                            item,
                            helper.getPartClasses(me, 'month-item-selected')
                        );
                    }
                }
            }

            /**
             * 切换节点状态
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {HTMLElement} item 目标元素
             * @param {string} className 切换的class
             * @return {boolean} 切换后状态 true为选中，false为未选中
             */
            function switchState(monthView, item, className) {
                if (!item) {
                    return false;
                }
                var classes = helper.getPartClasses(monthView, className);
                if (lib.hasClass(item, classes[0])) {
                    helper.removePartClasses(monthView, className, item);
                    return false;
                }
                else {
                    helper.addPartClasses(monthView, className, item);
                    return true;
                }
            }

            /**
             * 通过点击日期单元dom元素选择日期
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {Element} item dom元素td.
             */
            function selectByItem(monthView, item) {
                var date = item.getAttribute('data-date');
                var month = item.getAttribute('data-month');
                var year = item.getAttribute('data-year');
                var id = year + '-' + month + '-' + date;
                if (monthView.mode === 'multi') {
                    // 切换状态
                    var state = switchState(monthView, item, 'month-item-selected');

                    // 更新cache数据
                    monthView.viewValue[id] = {
                        isSelected: state,
                        value: new Date(year, month, date)
                    };
                    updateMultiRawValue(monthView);
                    // 找到这行的rowTag
                    var rowTag = item.parentNode.firstChild;
                    repaintRowTag(monthView, rowTag);
                    batchRepaintColumnTag(monthView);
                    repaintAllSelectTag(monthView);
                }
                else {
                    var itemSelectClasses =
                        helper.getPartClasses(monthView, 'month-item-selected');
                    if (lib.hasClass(item, itemSelectClasses[0])) {
                        return;
                    }
                    var newDate = new Date(year, month, date);
                    updateSingleSelectState(monthView, monthView.rawValue, newDate);
                    monthView.rawValue = newDate;
                    monthView.fire('change');
                    monthView.fire('itemclick');

                }
            }


            /**
             * 根据range修正year month
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {number} year 年.
             * @param {number} month 月.
             * @return {Object}
             */
            function reviseYearMonth(monthView, year, month) {
                var me = monthView;
                // 允许设置的范围
                var range = me.viewRange || me.range;
                var rangeBegin = range.begin.getFullYear() * 12
                    + range.begin.getMonth();
                var rangeEnd = range.end.getFullYear() * 12 + range.end.getMonth();
                // 欲设置的年月
                var viewMonth = year * 12 + month;
                var view = new Date(year, month, 1);
                month = view.getMonth();

                // 设置早了，补足
                if (rangeBegin - viewMonth > 0) {
                    month += (rangeBegin - viewMonth);
                }
                // 设置晚了，减余
                else if (viewMonth - rangeEnd > 0) {
                    month -= (viewMonth - rangeEnd);
                }

                // 重新设置
                view.setMonth(month);
                month = view.getMonth();
                year = view.getFullYear();

                return {
                    year: year,
                    month: month
                };

            }

            /**
             * 绘制浮动层内的日历部件
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {number} year 年.
             * @param {number} month 月.
             */
            function repaintMonthView(monthView, year, month) {
                // 如果没有指定，则显示rawValue对应月份日期
                if (year == null) {
                    year = monthView.year;
                }

                if (month == null) {
                    month = monthView.month;
                }

                var me = monthView;
                var revisedYearMonth = reviseYearMonth(me, year, month);
                me.month = revisedYearMonth.month;
                me.year = revisedYearMonth.year;

                var yearSelect = me.getChild('yearSel');
                var lastYear = yearSelect.getValue();

                // 通过year选择框来触发其它部分的重渲染
                yearSelect.setProperties({
                    datasource: getYearOptions(me),
                    value: me.year
                });


                // 如果year选择的数据没改变，
                // 但可能还是需要重回日历，
                // 因此要手动触发year的change
                // 3.1.0 beta6 bug：lastYear为string，me.year为number
                if (+lastYear === me.year) {
                    yearSelect.fire('change');
                }

            }

            /**
             * 更新单选模式日历选择状态
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {Date} oldValue 旧日期.
             * @param {Date} newValue 新日期.
             * @param {Date} 真实有效的新日期
             */
            function updateSingleSelectState(monthView, oldDate, newDate) {
                if (oldDate !== newDate) {
                    if (oldDate) {
                        var lastSelectedItem = lib.g(getItemId(monthView, oldDate));
                        if (lastSelectedItem) {
                            switchState(
                                monthView, lastSelectedItem, 'month-item-selected'
                            );
                        }
                    }
                    var curSelectedItem = lib.g(getItemId(monthView, newDate));
                    if (curSelectedItem) {
                        if (isItemSelectable(monthView, curSelectedItem)) {
                            switchState(
                                monthView, curSelectedItem, 'month-item-selected'
                            );
                        }
                        else {
                            monthView.rawValue = null;
                            return null;
                        }
                    }
                }
                return newDate;
            }

            /**
             * “下一个月”按钮点击的handler
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             */
            function goToNextMonth(monthView) {
                var nowDate = new Date(monthView.year, monthView.month, 1);
                var newDate = m(nowDate).add('month', 1);
                repaintMonthView(monthView, newDate.year(), newDate.month());
            }

            /**
             * 获取“上一个月”按钮点击的handler
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             */
            function goToPrevMonth(monthView) {
                var nowDate = new Date(monthView.year, monthView.month, 1);
                var newDate = m(nowDate).subtract('month', 1);
                repaintMonthView(monthView, newDate.year(), newDate.month());
            }

            /**
             * 年份切换
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {Select} yearSel Select控件实例
             */
            function changeYear(monthView, yearSel) {
                var year = parseInt(yearSel.getValue(), 10);
                monthView.year = year;

                var month = monthView.month;

                var revisedYearMonth = reviseYearMonth(monthView, year, month);
                month = revisedYearMonth.month;
                monthView.month = month;

                // 年份改变导致月份重绘
                var monthSelect = monthView.getChild('monthSel');
                var changes = monthSelect.setProperties({
                    datasource: getMonthOptions(monthView, monthView.year),
                    value: monthView.month
                });

                // 如果month选择的数据没改变，则要手动触发变化
                if (!changes.hasOwnProperty('rawValue')) {
                    changeMonth(monthView, monthSelect);
                }
                monthView.fire('changeyear');
            }

            /**
             * 月份切换
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {Select} monthSel Select控件实例
             */
            function changeMonth(monthView, monthSel) {
                var month = parseInt(monthSel.getValue(), 10);
                monthView.month = month;
                updateMain(monthView);
                monthView.fire('changemonth');
            }

            /**
             * 更新日历主体
             *
             * @inner
             * @param {MonthView} monthView MonthView控件实例
             * @param {Select} monthSel Select控件实例
             */
            function updateMain(monthView) {
                //填充日历主体
                var monthMainId = helper.getId(monthView, 'monthMain');
                var monthMain = lib.g(monthMainId);
                monthMain.innerHTML = getMonthMainHTML(monthView);
                // 找到最后一行，增加一个class
                var rowElements = monthMain.getElementsByTagName('tr');
                var lastRow = rowElements[rowElements.length - 1];
                helper.addPartClasses(monthView, 'last-row', lastRow);
                // 更新选择状态
                updateSelectStateByValue(monthView);
            }

            /**
             * range适配器，将string型range适配为Object
             *
             * @inner
             * @param {Object | string} range
             */
            function rangeAdapter(range) {
                var begin;
                var end;
                // range类型如果是string
                if (typeof range === 'string') {
                    var beginAndEnd = range.split(',');
                    begin = parseToDate(beginAndEnd[0]);
                    end = parseToDate(beginAndEnd[1]);
                }
                else {
                    begin = range.begin;
                    end = range.end;
                }

                if (begin > end) {
                    return {
                        begin: end,
                        end: begin
                    };
                }

                return {
                    begin: begin,
                    end: end
                };
            }

            /**
             * 字符串日期转换为Date对象
             *
             * @inner
             * @param {string} dateStr 字符串日期
             */
            function parseToDate(dateStr) {
                /** 2011-11-04 */
                function parse(source) {
                    var dates = source.split('-');
                    if (dates) {
                        return new Date(
                            parseInt(dates[0], 10),
                                parseInt(dates[1], 10) - 1,
                            parseInt(dates[2], 10)
                        );
                    }
                    return null;
                }

                dateStr = dateStr + '';
                var dateAndHour =  dateStr.split(' ');
                var date = parse(dateAndHour[0]);
                if (dateAndHour[1]) {
                    var clock = dateAndHour[1].split(':');
                    date.setHours(clock[0]);
                    date.setMinutes(clock[1]);
                    date.setSeconds(clock[2]);
                }
                return date;
            }

            /**
             * 根据不同模式将字符串值解析为rawValue
             *
             * @inner
             * @param {string} value 字符串日期
             * @param {string} mode 日历模式 multi | single
             * @return {Date | Array}
             */
            function parseValueByMode(value, mode) {
                if (mode === 'single') {
                    return parseToDate(value);
                }
                else {
                    var dateStrs = value.split(',');
                    var dates = [];
                    for (var i = 0; i < dateStrs.length - 1; i += 2) {
                        var begin = parseToDate(dateStrs[i]);
                        var end = parseToDate(dateStrs[i + 1]);
                        var temp;
                        if (!begin || !end) {
                            continue;
                        }
                        if (begin - end === 0) {
                            dates.push(begin);
                        } else {
                            temp = begin;
                            while (temp <= end) {
                                dates.push(temp);
                                temp = new Date(
                                    temp.getFullYear(),
                                    temp.getMonth(),
                                        temp.getDate() + 1
                                );
                            }
                        }
                    }
                    return dates;
                }
            }

            function updateSelectStateByValue(monthView) {
                // 单选模式
                if (monthView.mode !== 'multi') {
                    updateSingleSelectState(monthView, null, monthView.rawValue);
                    return;
                }

                // 多选模式
                var viewValue = monthView.viewValue;
                for (var key in viewValue) {
                    var item = lib.g(helper.getId(monthView, key));
                    if (item) {
                        // 有可能这个item是不可选的
                        var isSelectable = isItemSelectable(monthView, item);
                        if (isSelectable === 1) {
                            if (viewValue[key].isSelected) {
                                helper.addPartClasses(
                                    monthView, 'month-item-selected', item
                                );
                            }
                            else {
                                helper.removePartClasses(
                                    monthView, 'month-item-selected', item
                                );
                            }
                        }
                        // 应该修正了rawValue和viewValue
                        else if (isSelectable === 0) {
                            // 有可能是virtual的，这种不应该更新数据
                            viewValue[key].isSelected = false;
                            updateMultiRawValue(monthView);
                        }
                    }
                }
                batchRepaintRowTag(monthView);
                batchRepaintColumnTag(monthView);
                repaintAllSelectTag(monthView);
            }


            /**
             * 给select的layer人肉增加class命名空间
             *
             * @inner
             * @param {Event} e layer渲染事件
             */
            function addCustomClassesForSelectLayer(monthView, selectClass, e) {
                var layerClasses = monthView.helper.getPartClasses(selectClass + '-layer');
                var layer = e.layer;
                layer.addCustomClasses(layerClasses);
                monthView.fire('selectlayerrendered', { layer: layer });
            }

            MonthView.prototype = {
                /**
                 * 控件类型
                 *
                 * @type {string}
                 */
                type: 'MonthView',

                /**
                 * 初始化参数
                 *
                 * @param {Object=} options 构造函数传入的参数
                 * @override
                 * @protected
                 */
                initOptions: function (options) {
                    /**
                     * 默认选项配置
                     */
                    var properties = {
                        range: {
                            begin: new Date(1982, 10, 4),
                            end: new Date(2046, 10, 4)
                        },
                        dateFormat: 'YYYY-MM-DD',
                        paramFormat: 'YYYY-MM-DD',
                        viewValue: {},
                        mode: 'single'
                    };
                    lib.extend(properties, options);
                    this.setProperties(properties);
                },

                setProperties: function (properties) {
                    if (properties.range) {
                        properties.range = rangeAdapter(properties.range);
                    }

                    // 如果么设置rawValue
                    var now = new Date();
                    var mode = properties.mode || this.mode;
                    if (properties.rawValue == null) {
                        // 从value转
                        if (properties.value) {
                            properties.rawValue =
                                parseValueByMode(properties.value, mode);
                        }
                        // 都没设
                        else {
                            // 来自初始设置
                            if (this.rawValue == null) {
                                // 单模式下rawValue默认当天
                                if (mode === 'single') {
                                    properties.rawValue = now;
                                }
                                // 多选模式下rawValue默认空数组
                                else {
                                    properties.rawValue = [];
                                }
                            }
                        }
                    }

                    // 初始化显示年月
                    var year = properties.year;
                    var month = properties.month;

                    // 都没设置
                    if ((!year && month == null)) {
                        // 单选模式下，year和month取rawValue的年月
                        if (mode === 'single') {
                            if (properties.rawValue) {
                                year = properties.rawValue.getFullYear();
                                month = properties.rawValue.getMonth() + 1;
                            }
                        }
                        // 多选模式下，year和month取当天的年月
                        else {
                            year = now.getFullYear();
                            month = now.getMonth() + 1;
                        }
                    }

                    if (year && month) {
                        properties.year = parseInt(year, 10);
                        // 开放给外部的month，为了符合正常思维，计数从1开始
                        // 但是保存时要按照Date的规则从0开始
                        properties.month = parseInt(month, 10) - 1;
                    }
                    else if (properties.hasOwnProperty('year')) {
                        // 如果此时month还没初始化，为了不混淆，year的设置也是无效的
                        if (this.month == null) {
                            delete properties.year;
                        }
                    }
                    else if (properties.hasOwnProperty('month')) {
                        // 如果此时year还没初始化，为了不混淆，month的设置也是无效的
                        if (this.year == null) {
                            delete properties.month;
                        }
                        else {
                            properties.month = parseInt(month, 10) - 1;
                        }
                    }
                    var changes =
                        Control.prototype.setProperties.apply(this, arguments);

                    if (changes.hasOwnProperty('rawValue')) {
                        this.fire('change');
                    }
                    return changes;
                },

                /**
                 * 初始化DOM结构
                 *
                 * @protected
                 */
                initStructure: function () {
                    this.main.innerHTML = getMainHTML(this);

                    // 创建控件树
                    this.initChildren(this.main);

                    if (this.mode === 'multi') {
                        this.addState('multi-select');
                    }
                },

                /**
                 * 初始化事件交互
                 *
                 * @protected
                 * @override
                 */
                initEvents: function () {
                    // 向后按钮
                    var monthBack = this.getChild('monthBack');
                    monthBack.on(
                        'click',
                        lib.curry(goToPrevMonth, this)
                    );

                    // 向前按钮
                    var monthForward = this.getChild('monthForward');
                    monthForward.on(
                        'click',
                        lib.curry(goToNextMonth, this)
                    );

                    // 月份选择
                    var monthSel = this.getChild('monthSel');
                    monthSel.on(
                        'change',
                        lib.curry(changeMonth, this, monthSel)
                    );

                    // 给layer人肉增加class命名空间
                    monthSel.on(
                        'layerrendered',
                        lib.curry(addCustomClassesForSelectLayer, this, 'month-select')
                    );

                    // 年份选择
                    var yearSel = this.getChild('yearSel');
                    yearSel.on(
                        'change',
                        lib.curry(changeYear, this, yearSel)
                    );

                    yearSel.on(
                        'layerrendered',
                        lib.curry(addCustomClassesForSelectLayer, this, 'year-select')
                    );

                    // 为日期绑定点击事件
                    var monthMain = this.helper.getPart('monthMain');
                    helper.addDOMEvent(this, monthMain, 'click', monthViewClick);
                },

                /**
                 * 重新渲染视图
                 * 仅当生命周期处于RENDER时，该方法才重新渲染
                 *
                 * @param {Array=} 变更过的属性的集合
                 * @override
                 */
                repaint: helper.createRepaint(
                    Control.prototype.repaint,
                    {
                        name: ['range', 'rawValue', 'year', 'month'],
                        paint: function (monthView, range, rawValue, year, month) {
                            // 如果只是改变了rawValue，year和month也会跟随更改
                            // 只对单选模式日历有效
                            if (rawValue) {
                                if (monthView.mode === 'multi') {
                                    parseToCache(monthView);
                                }
                            }
                            repaintMonthView(
                                monthView,
                                monthView.year,
                                monthView.month
                            );

                        }
                    },
                    {
                        name: 'disabled',
                        paint: function (monthView, disabled) {
                            // 向后按钮
                            var monthBack = monthView.getChild('monthBack');
                            monthBack.setProperties({disabled: disabled});
                            // 向前按钮
                            var monthForward = monthView.getChild('monthForward');
                            monthForward.setProperties({disabled: disabled});

                            // 月份选择
                            var monthSel = monthView.getChild('monthSel');
                            monthSel.setProperties({disabled: disabled});
                            // 月份选择
                            var yearSel = monthView.getChild('yearSel');
                            yearSel.setProperties({disabled: disabled});
                        }
                    }
                ),


                /**
                 * 设置控件状态为禁用
                 */
                disable: function () {
                    this.setProperties({
                        disabled: true
                    });
                    this.addState('disabled');
                },

                /**
                 * 设置控件状态为启用
                 */
                enable: function () {
                    this.setProperties({
                        disabled: false
                    });
                    this.removeState('disabled');
                },

                /**
                 * 设置可选中的日期区间
                 *
                 * @param {Object} range 可选中的日期区间
                 */
                setRange: function (range) {
                    this.setProperties({ 'range': range });
                },


                /**
                 * 设置日期
                 *
                 * @param {Date|Array} date 选取的日期.
                 */
                setRawValue: function (date) {
                    this.setProperties({ 'rawValue': date });
                },

                /**
                 * 获取选取日期值
                 *
                 * @return {Date|Array}
                 */
                getRawValue: function () {
                    return this.rawValue;
                },

                getValue: function () {
                    return this.stringifyValue(this.rawValue);
                },

                /**
                 * 将value从原始格式转换成string
                 *
                 * @param {*} rawValue 原始值
                 * @return {string}
                 */
                stringifyValue: function (rawValue) {
                    if (this.mode === 'single') {
                        return lib.date.format(rawValue, this.paramFormat) || '';
                    }
                    else {
                        var dateStrs = [];
                        var oneDay = 86400000;
                        for (var i = 0; i < rawValue.length; i ++) {
                            if (i === 0) {
                                dateStrs.push(
                                    lib.date.format(rawValue[i], this.paramFormat)
                                );
                            }
                            else {
                                if ((rawValue[i] - rawValue[i-1]) > oneDay) {
                                    dateStrs.push(
                                        lib.date.format(
                                            rawValue[i-1], this.paramFormat
                                        )
                                    );
                                    dateStrs.push(
                                        lib.date.format(
                                            rawValue[i], this.paramFormat
                                        )
                                    );
                                }
                                else if (i === (rawValue.length - 1)) {
                                    dateStrs.push(
                                        lib.date.format(
                                            rawValue[i], this.paramFormat
                                        )
                                    );
                                }
                                else {
                                    continue;
                                }
                            }
                        }
                        return dateStrs.join(',');
                    }
                },

                parseValue: function (value) {
                    return parseValueByMode(value, this.mode);
                },

                setRawValueWithoutFireChange: function (value) {
                    this.rawValue = value;
                    parseToCache(this);
                },

                getDateItemHTML: function (date) {
                    return lib.g(getItemId(this, date));
                }

            };

            lib.inherits(MonthView, Control);
            ui.register(MonthView);

            return MonthView;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 复选框
     * @author otakustay
     */
    define(
        'esui/CheckBox',['require','underscore','./lib','./InputControl','./painters','./main'],function (require) {
            var u = require('underscore');
            var lib = require('./lib');
            var InputControl = require('./InputControl');

            /**
             * 同步选中状态
             *
             * @param {Event} e DOM事件对象
             * @ignore
             */
            function syncChecked(e) {
                var checked = lib.g(this.boxId).checked;
                this.setProperties({ checked: checked });
            }

            /**
             * 复选框
             *
             * @extends InputControl
             * @constructor
             */
            function CheckBox() {
                InputControl.apply(this, arguments);
            }

            CheckBox.prototype = {
                /**
                 * 控件类型，始终为`"CheckBox"`
                 *
                 * @type {string}
                 * @readonly
                 * @override
                 */
                type: 'CheckBox',

                /**
                 * 创建控件主元素，默认使用`<label>`属性
                 *
                 * @return {HTMLElement}
                 * @protected
                 * @override
                 */
                createMain: function () {
                    return document.createElement('label');
                },

                /**
                 * 获取控件的分类，始终返回`"check"`
                 *
                 * @return {string}
                 * @override
                 */
                getCategory: function () {
                    return 'check';
                },

                /**
                 * 初始化配置
                 *
                 * 如果未给出{@link CheckBox#title}属性，
                 * 则按以下规则以优先级从高到低从主元素上提取：
                 *
                 * 1. 如果主元素有`title`属性，使用该属性的值
                 * 2. 如果提供了`value`属性，则以`value`属性作为值
                 * 3. 使用空字符串
                 *
                 * 以下属性如果未给出，则使用主元素上的对应属性：
                 *
                 * - `name`
                 * - `value`，如果主元素上也未给出，则默认值为`"on"`
                 * - `checked`
                 *
                 * @param {Object} [options] 初始化配置项
                 * @param {Mixed[] | Mixed} [options.datasource] 初始化数据源
                 *
                 * `CheckBox`控件在初始化时可以提供`datasource`属性，
                 * 该属性用于控件判断一开始是否选中，且这个属性只在初始化时有效，不会保存下来
                 *
                 * `datasource`可以是以下类型：
                 *
                 * - 数组：此时只要`rawValue`在`datasource`中（使用`==`比较）则选上
                 * - 其它：只要`rawValue`与此相等（使用`==`比较）则选上
                 * @protected
                 * @override
                 */
                initOptions: function (options) {
                    var properties = {
                        value: this.main.value || 'on',
                        checked: this.main.checked || false
                    };

                    u.extend(properties, options);

                    properties.name =
                        properties.name || this.main.getAttribute('name');

                    var datasource = properties.datasource;
                    delete properties.datasource;

                    // 这里涉及到`value`和`rawValue`的优先级问题，
                    // 而这个处理在`InputControl.prototype.setProperties`里，
                    // 因此要先用一下，然后再管`datasource`
                    this.setProperties(properties);
                    if (datasource != null) {
                        if (u.isArray(datasource)) {
                            this.checked = u.any(
                                datasource,
                                function (item) {
                                    return item.value == this.value;    // jshint ignore:line
                                },
                                this
                            );
                        }
                        else if (this.rawValue == datasource) {         // jshint ignore:line
                            this.checked = true;
                        }
                    }

                    if (!this.title) {
                        this.title = this.main.title
                            || (this.getValue() === 'on' ? '' : this.getValue());
                    }
                },

                /**
                 * 初始化DOM结构
                 *
                 * @protected
                 * @override
                 */
                initStructure: function () {
                    // 如果用的是一个`<input>`，替换成`<div>`
                    if (this.main.nodeName.toLowerCase() === 'input') {
                        this.boxId = this.main.id || this.helper.getId('box');
                        this.helper.replaceMain();
                        this.main.id = this.helper.getId();
                    }
                    else {
                        this.boxId = this.helper.getId('box');
                    }

                    var html = '<input type="checkbox" name="${name}" id="${id}" />'
                        + '<span id="${textId}"></span>';
                    this.main.innerHTML = lib.format(
                        html,
                        {
                            name: this.name,
                            id: this.boxId,
                            textId: this.helper.getId('text')
                        }
                    );
                },

                /**
                 * 初始化事件交互
                 *
                 * @protected
                 * @override
                 */
                initEvents: function () {
                    var box = lib.g(this.boxId);
                    this.helper.addDOMEvent(
                        box,
                        'click',
                        function (e) {
                            /**
                             * @event click
                             *
                             * 点击时触发
                             */
                            this.fire('click');
                            if (!box.addEventListener) {
                                syncChecked.call(this, e);
                            }
                        }
                    );

                    if (box.addEventListener) {
                        this.helper.addDOMEvent(box, 'change', syncChecked);
                    }
                },

                /**
                 * 批量更新属性并重绘
                 *
                 * @param {Object} properties 需更新的属性
                 * @override
                 * @fires change
                 */
                setProperties: function (properties) {
                    var changes =
                        InputControl.prototype.setProperties.apply(this, arguments);
                    if (changes.hasOwnProperty('checked')) {
                        /**
                         * @event change
                         *
                         * 当值发生变化时触发
                         */
                        this.fire('change');
                    }
                },

                /**
                 * 获得应当获取焦点的元素，主要用于验证信息的`<label>`元素的`for`属性设置
                 *
                 * @return {HTMLElement}
                 * @protected
                 * @override
                 */
                getFocusTarget: function () {
                    var box = lib.g(this.boxId);
                    return box;
                },

                /**
                 * 更新标签文本
                 *
                 * @param {string} title 新的标签文本内容，未经HTML转义
                 * @protected
                 */
                updateTitle: function (title) {
                    // 如果外部直接调用，则要更新下当前实体上的属性
                    this.title = title;
                    title = u.escape(title);
                    this.helper.getPart('text').innerHTML = title;
                    lib.setAttribute(this.boxId, 'title', title);
                },


                /**
                 * 重渲染
                 *
                 * @method
                 * @protected
                 * @override
                 */
                repaint: require('./painters').createRepaint(
                    InputControl.prototype.repaint,
                    {
                        /**
                         * @property {boolean} checked
                         *
                         * 标识是否为选中状态
                         */
                        name: ['rawValue', 'checked'],
                        paint: function (box, rawValue, checked) {
                            var value = box.stringifyValue(rawValue);
                            var box = lib.g(box.boxId);
                            box.value = value;
                            box.checked = checked;
                        }
                    },
                    {
                        name: ['disabled', 'readOnly'],
                        paint: function (box, disabled, readOnly) {
                            var box = lib.g(box.boxId);
                            box.disabled = disabled;
                            box.readOnly = readOnly;
                        }
                    },
                    {
                        /**
                         * @property {string} title
                         *
                         * 复选框的文本内容
                         */
                        name: 'title',
                        paint: function (box, title) {
                            box.updateTitle(title);
                        }
                    }
                ),

                /**
                 * 设置选中状态
                 *
                 * @param {boolean} checked 状态
                 */
                setChecked: function ( checked ) {
                    this.setProperties({ checked: checked });
                },

                /**
                 * 获取选中状态
                 *
                 * @return {boolean} 如已经选中则返回`true`
                 */
                isChecked: function () {
                    if (this.helper.isInStage('RENDERED')) {
                        var box = lib.g(this.boxId);
                        return box.checked;
                    }
                    else {
                        return this.checked;
                    }
                }
            };

            lib.inherits( CheckBox, InputControl );
            require('./main').register(CheckBox);
            return CheckBox;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @ignore
     * @file 文本标签控件
     * @author erik, otakustay
     */
    define(
        'esui/Label',['require','underscore','./lib','./Control','./painters','./main'],function (require) {
            var u = require('underscore');
            var lib = require('./lib');
            var Control = require('./Control');

            /**
             * 文本标签控件
             *
             * 与{@link Panel}类似，但不允许有内部控件，
             * 设置的内容会被HTML转义，但支持{@link Label#click}事件
             *
             * @extends Control
             * @constructor
             */
            function Label(options) {
                Control.apply(this, arguments);
            }

            /**
             * 控件类型，始终为`"Label"`
             *
             * @type {string}
             * @readonly
             * @override
             */
            Label.prototype.type = 'Label';

            /**
             * 创建控件主元素
             *
             * 如果初始化时提供{@link Label#tagName}属性，则以此创建元素，
             * 默认使用`<span>`元素
             *
             * @param {Object} options 构造函数传入的参数
             * @return {HTMLElement}
             * @protected
             * @override
             */
            Label.prototype.createMain = function (options) {
                if (!options.tagName) {
                    return Control.prototype.createMain.call(this);
                }
                return document.createElement(options.tagName);
            };

            /**
             * 初始化参数
             *
             * 如果初始化时提供了主元素，则使用主元素的标签名作为{@link Label#tagName}属性
             *
             * 如果未提供{@link Label#text}属性，则使用主元素的文本内容作为此属性的初始值
             *
             * @param {Object} [options] 构造函数传入的参数
             * @override
             * @protected
             */
            Label.prototype.initOptions = function (options) {
                var properties = {};
                u.extend(properties, options);
                /**
                 * @property {string} tagName
                 *
                 * 指定主元素标签名
                 *
                 * 此属性仅在初始化时生效，运行期不能修改
                 *
                 * @readonly
                 */
                properties.tagName = this.main.nodeName.toLowerCase();
                if (options.text == null) {
                    properties.text = lib.trim(lib.getText(this.main));
                }
                u.extend(this, properties);
            };

            /**
             * 初始化事件交互
             *
             * @protected
             * @override
             */
            Label.prototype.initEvents = function () {
                /**
                 * @event click
                 *
                 * 点击时触发
                 */
                this.helper.delegateDOMEvent(this.main, 'click');
            };

            var paint = require('./painters');

            /**
             * 重渲染
             *
             * @method
             * @protected
             * @override
             */
            Label.prototype.repaint = paint.createRepaint(
                Control.prototype.repaint,
                /**
                 * @property {string} title
                 *
                 * 鼠标放置在控件上时的提示信息
                 */
                paint.attribute('title'),
                /**
                 * @property {string} text
                 *
                 * 文本内容，会被自动HTML转义
                 */
                paint.text('text'),
                /**
                 * @property {string} forTarget
                 *
                 * 与当前标签关联的输入控件的id，仅当主元素为`<label>`元素时生效，相当于`for`属性的效果，但指定的是控件的id
                 */
                {
                    name: 'forTarget',
                    paint: function (label, forTarget) {
                        // 仅对`<label>`元素生效
                        if (label.main.nodeName.toLowerCase() !== 'label') {
                            return;
                        }

                        label.helper.addDOMEvent(
                            label.main,
                            'mousedown',
                            function fixForAttribute() {
                                var targetControl = this.viewContext.get(forTarget);
                                var targetElement = targetControl
                                    && (typeof targetControl.getFocusTarget === 'function')
                                    && targetControl.getFocusTarget();
                                if (targetElement && targetElement.id) {
                                    lib.setAttribute(this.main, 'for', targetElement.id);
                                }

                                this.helper.removeDOMEvent(this.main, 'mousedown', fixForAttribute);
                            }
                        );
                    }
                }
            );

            /**
             * 设置文本
             *
             * @param {string} text 文本内容，参考{@link Label#text}属性的说明
             */
            Label.prototype.setText = function (text) {
                this.setProperties({ text: text });
            };

            /**
             * 获取文本
             *
             * @return {string}
             */
            Label.prototype.getText = function () {
                return this.text;
            };

            /**
             * 设置标题
             *
             * @param {string} title 需要设置的值，参考{@link Label#title}属性的说明
             */
            Label.prototype.setTitle = function (title) {
                this.setProperties({ title: title });
            };

            /**
             * 获取标题
             *
             * @return {string}
             */
            Label.prototype.getTitle = function () {
                return this.title;
            };

            lib.inherits(Label, Control);
            require('./main').register(Label);
            return Label;
        }
    );

    /**
     * ESUI (Enterprise Simple UI)
     * Copyright 2013 Baidu Inc. All rights reserved.
     *
     * @file 无限区间日历
     * @author dbear
     */

    define(
        'esui/RangeCalendar',['require','./Button','./MonthView','./CheckBox','./Label','./lib','./InputControl','./controlHelper','./Layer','./main','moment','underscore'],function (require) {
            require('./Button');
            require('./MonthView');
            require('./CheckBox');
            require('./Label');

            var lib = require('./lib');
            var InputControl = require('./InputControl');
            var helper = require('./controlHelper');
            var Layer = require('./Layer');
            var ui = require('./main');
            var m = require('moment');
            var u = require('underscore');

            /**
             * 日历用浮层
             *
             * @extends Layer
             * @ignore
             * @constructor
             */
            function RangeCalendarLayer() {
                Layer.apply(this, arguments);
            }

            lib.inherits(RangeCalendarLayer, Layer);

            RangeCalendarLayer.prototype.render = function (element) {
                var calendar = this.control;
                document.body.appendChild(element);
                element.innerHTML = getLayerHtml(calendar);
                calendar.helper.initChildren(element);
                paintLayer(calendar, calendar.view, 'render');
            };

            RangeCalendarLayer.prototype.toggle = function () {
                var element = this.getElement();
                if (!element
                    || this.control.helper.isPart(element, 'layer-hidden')
                    ) {
                    // 展示之前先跟main同步
                    var calendar = this.control;
                    paintLayer(calendar, calendar.rawValue, 'repaint');
                    this.show();
                }
                else {
                    this.hide();
                }
            };

            /**
             * 重绘弹出层数据
             *
             * @inner
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @param {{begin:Date,end:Date}=} value 显示的日期
             * @param {string} state 渲染时控件状态
             */
            function paintLayer(calendar, value, state) {
                if (state === 'render') {
                    // 为mini日历绑定点击事件
                    var shortcutDom = calendar.helper.getPart('shortcut');
                    helper.addDOMEvent(
                        calendar, shortcutDom, 'click', shortcutClick);

                    // 绑定“无限结束”勾选事件
                    var endlessCheck = calendar.getChild('endlessCheck');
                    if (endlessCheck) {
                        endlessCheck.on(
                            'change',
                            lib.curry(makeCalendarEndless, calendar)
                        );
                        // 设置endless
                        if (calendar.isEndless) {
                            endlessCheck.setChecked(true);
                            calendar.helper.addPartClasses(
                                'shortcut-disabled',
                                calendar.helper.getPart(calendar)
                            );
                        }
                    }

                    // 绑定提交和取消按钮
                    var okBtn = calendar.getChild('okBtn');
                    okBtn.on('click', lib.curry(commitValue, calendar));
                    var cancelBtn = calendar.getChild('cancelBtn');
                    cancelBtn.on(
                        'click',
                        u.bind(calendar.layer.hide, calendar.layer)
                    );
                    // 关闭按钮
                    var closeBtn = calendar.getChild('closeBtn');
                    closeBtn.on(
                        'click',
                        u.bind(calendar.layer.hide, calendar.layer)
                    );
                }
                else {
                    calendar.view.begin = value.begin;
                    calendar.view.end = value.end;
                    calendar.value = calendar.convertToParam(value);

                    var isEndless;
                    if (!value.end) {
                        isEndless = true;
                    }
                    else {
                        isEndless = false;
                    }
                    calendar.setProperties({ isEndless: isEndless });
                }

                // 渲染开始结束日历
                paintCal(calendar, 'begin', calendar.view.begin, state === 'render');
                paintCal(calendar, 'end', calendar.view.end, state === 'render');


                // 渲染mini日历
                var selectedIndex = getSelectedIndex(calendar, calendar.view);
                paintMiniCal(calendar, selectedIndex);
            }

            /**
             * 控件类
             *
             * @constructor
             * @param {Object} options 初始化参数
             */
            function RangeCalendar(options) {
                this.now = new Date();
                InputControl.apply(this, arguments);
                this.layer = new RangeCalendarLayer(this);
            }

            /**
             * 搭建弹出层内容
             *
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @return {string}
             */
            function getLayerHtml(calendar) {
                var tpl = ''
                    + '<div class="${shortCutClass}" id="${shortcutId}">'
                    + '${shortCut}</div>'
                    + '<div class="${bodyClass}">'
                    +   '${beginCalendar}${endCalendar}'
                    + '</div>'
                    + '<div class="${footClass}">'
                    +   '<div class="${okBtnClass}"'
                    +   ' data-ui="type:Button;childName:okBtn;">确定</div>'
                    +   '<div class="${cancelBtnClass}"'
                    +   ' data-ui="type:Button;childName:cancelBtn;">取消</div>'
                    + '</div>'
                    + '<div data-ui="type:Button;childName:'
                    + 'closeBtn;skin:layerClose;height:12;"></div>';

                return lib.format(tpl, {
                    bodyClass: calendar.helper.getPartClassName('body'),
                    shortcutId: calendar.helper.getId('shortcut'),
                    shortCutClass: calendar.helper.getPartClassName('shortcut'),
                    shortCut: getMiniCalendarHtml(calendar),
                    beginCalendar: getCalendarHtml(calendar, 'begin'),
                    endCalendar: getCalendarHtml(calendar, 'end'),
                    footClass: calendar.helper.getPartClassName('foot'),
                    okBtnClass: calendar.helper.getPartClassName('okBtn'),
                    cancelBtnClass: calendar.helper.getPartClassName('cancelBtn')
                });
            }

            /**
             * 获取某日开始时刻
             *
             * @param {Date} day 某日
             * @return {Date}
             */
            function startOfDay(day) {
                return m(day).startOf('day').toDate();
            }

            /**
             * 获取某日结束时刻
             *
             * @param {Date} day 某日
             * @return {Date}
             */
            function endOfDay(day) {
                return m(day).endOf('day').toDate();
            }

            /**
             * 判断是否不在可选范围内
             *
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @param {object} shortItem 快捷对象
             * @return {boolean}
             */
            function isOutOfRange(calendar, shortItem) {
                var range = calendar.range;
                var itemValue = shortItem.getValue.call(calendar, calendar.now);

                // 得先格式化一下，去掉时间
                if (startOfDay(range.begin) > startOfDay(range.begin)
                    || endOfDay(itemValue.end) < endOfDay(itemValue.end)) {
                    return true;
                }

                return false;
            }

            /**
             * 搭建快捷迷你日历
             *
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @return {string}
             */
            function getMiniCalendarHtml(calendar) {
                var shownShortCut = calendar.shownShortCut.split(',');
                var shownShortCutHash = {};
                for (var k = 0; k < shownShortCut.length; k++) {
                    shownShortCutHash[shownShortCut[k]] = true;
                }

                var tplItem = ''
                    + '<span data-index="${shortIndex}" class="'
                    + calendar.helper.getPartClassName('shortcut-item')
                    + ' ${shortClass}"'
                    + ' id="${shortId}">${shortName}</span>';
                var shortItems = calendar.shortCutItems;
                var len = shortItems.length;
                var html = [];
                for (var i = 0; i < len; i++) {
                    var shortItem = shortItems[i];
                    if (shownShortCutHash[shortItem.name]) {
                        var shortName = shortItem.name;
                        var shortClasses = [];
                        if (i === 0) {
                            shortClasses = shortClasses.concat(
                                calendar.helper.getPartClasses(
                                    'shortcut-item-first'
                                )
                            );
                        }
                        // 超出范围或者日历是无限结束
                        var disabled = isOutOfRange(calendar, shortItem);
                        if (disabled) {
                            shortClasses = shortClasses.concat(
                                calendar.helper.getPartClasses(
                                    'shortcut-item-disabled'
                                )
                            );
                        }
                        var shortId = calendar.helper.getId('shortcut-item' + i);

                        html.push(
                            lib.format(
                                tplItem,
                                {
                                    shortIndex: i,
                                    shortClass: shortClasses.join(' '),
                                    shortId: shortId,
                                    shortName: shortName
                                }
                            )
                        );
                    }
                }
                return html.join('');
            }

            /**
             * 搭建单个日历
             *
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @param {string} type 日历类型 begin|end
             * @return {string}
             */
            function getCalendarHtml(calendar, type) {
                var endlessCheckDOM = '';
                // 可以无限
                if (calendar.endlessCheck && type === 'end') {
                    endlessCheckDOM = ''
                        + '<input type="checkbox" title="不限结束" '
                        + 'data-ui-type="CheckBox" '
                        + 'data-ui-child-name="endlessCheck" />';
                }
                var tpl = ''
                    + '<div class="${frameClass}">'
                    +   '<div class="${labelClass}">'
                    +     '<h3>${labelTitle}</h3>'
                    +     endlessCheckDOM
                    +   '</div>'
                    +   '<div class="${calClass}">'
                    +     '<div data-ui="type:MonthView;'
                    +     'childName:${calName}"></div>'
                    +   '</div>'
                    + '</div>';

                return lib.format(tpl, {
                    frameClass: calendar.helper.getPartClassName(type),
                    labelClass: calendar.helper.getPartClassName('label'),
                    labelTitle: type === 'begin' ? '开始日期' : '结束日期',
                    titleId: calendar.helper.getId(type + 'Label'),
                    calClass: calendar.helper.getPartClassName(type + '-cal'),
                    calName: type + 'Cal'
                });
            }

            /**
             * 将日历置为无结束时间
             *
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @param {CheckBox} checkbox CheckBox控件实例
             */
            function makeCalendarEndless(calendar) {
                var endCalendar = calendar.getChild('endCal');
                var shortCutItems = calendar.helper.getPart('shortcut');
                var selectedIndex;
                if (this.isChecked()) {
                    calendar.isEndless = true;
                    endCalendar.disable();
                    selectedIndex = -1;
                    calendar.view.end = null;
                    calendar.helper.addPartClasses(
                        'shortcut-disabled', shortCutItems
                    );
                }
                else {
                    calendar.isEndless = false;
                    endCalendar.enable();
                    // 恢复结束日历的选择值
                    updateView.apply(calendar, [endCalendar, 'end']);
                    calendar.helper.removePartClasses(
                        'shortcut-disabled', shortCutItems
                    );
                }
            }

            /**
             * 比较两个日期是否同一天(忽略时分秒)
             *
             * @inner
             * @param {Date} date1 日期.
             * @param {Date} date2 日期.
             * @return {boolean}
             */
            function isSameDate(date1, date2) {
                if ((!date1 && date2) || (date1 && !date2)) {
                    return false;
                }
                else if (!date1 && !date2) {
                    return true;
                }
                return m(date1).isSame(date2, 'day');
            }

            /**
             * 获取mini日历中应该选中的索引值
             *
             * @inner
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @param {{begin:Date,end:Date}} value 日期区间对象.
             * @return {number}
             */
            function getSelectedIndex(calendar, value) {
                var shortcutItems = calendar.shortCutItems;
                var len = shortcutItems.length;

                for (var i = 0; i < len; i++) {
                    var item = shortcutItems[i];
                    var itemValue = item.getValue.call(calendar, calendar.now);

                    if (isSameDate(value.begin, itemValue.begin)
                        && isSameDate(value.end, itemValue.end)) {
                        return i;
                    }
                }

                return -1;
            }

            /**
             * 根据索引选取日期，在点击快捷日期链接时调用
             *
             * @inner
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @param {number} index
             */
            function selectIndex(calendar, index) {
                var me = calendar;
                var shortcutItems = calendar.shortCutItems;

                if (index < 0 || index >= shortcutItems.length) {
                    return;
                }

                var value = shortcutItems[index].getValue.call(me, me.now);
                var begin = value.begin;
                var end = value.end;

                // 更新view
                calendar.view = { begin: begin, end: end };

                // 更新日历
                paintCal(calendar, 'begin', begin);
                paintCal(calendar, 'end', end);

                // 更新快捷链接样式
                paintMiniCal(me, index);
            }

            /**
             * 渲染mini日历
             *
             * @inner
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @param {number} index 选择的索引
             */
            function paintMiniCal(calendar, index) {
                var shortcutItems = calendar.shortCutItems;
                var miniMode = calendar.miniMode;
                // 重置选择状态
                if (miniMode !== null && miniMode !== index) {
                    calendar.helper.removePartClasses(
                        'shortcut-item-selected',
                        calendar.helper.getPart('shortcut-item' + miniMode)
                    );
                }
                calendar.miniMode = index;
                if (index >= 0) {
                    calendar.helper.addPartClasses(
                        'shortcut-item-selected',
                        calendar.helper.getPart('shortcut-item' + index)
                    );
                    calendar.curMiniName = shortcutItems[index].name;
                }
                else {
                    calendar.curMiniName = null;
                }
            }

            /**
             * 初始化开始和结束日历
             *
             * @inner
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @param {string} type 日历类型
             * @param {Date} value 日期
             * @param {boolean} bindEvent 是否需要绑定事件
             */
            function paintCal(calendar, type, value, bindEvent) {
                var monthView = calendar.getChild(type + 'Cal');
                if (!monthView) {
                    return;
                }
                // 只有在第一次生成MonthView视图时需要绑定事件，其余时候都无需事件绑定
                if (bindEvent === true) {
                    monthView.on(
                        'change',
                        u.bind(updateView, calendar, monthView, type)
                    );
                    monthView.on(
                        'changemonth',
                        u.bind(updateHighlightRange, null, calendar)
                    );
                }
                // 更新日历选值
                monthView.setProperties({
                    rawValue: value,
                    range: calendar.range
                });
            }

            /**
             * mini日历点击事件
             *
             * @inner
             * @param {RangeCalendar} this RangeCalendar控件实例
             * @param {Event} 触发事件的事件对象
             */
            function shortcutClick(e) {
                if (this.isEndless) {
                    return;
                }

                var tar = e.target || e.srcElement;
                var classes = this.helper.getPartClasses('shortcut-item');
                var disableClasses =
                    this.helper.getPartClasses('shortcut-item-disabled');

                while (tar && tar !== document.body) {
                    if (lib.hasClass(tar, classes[0])
                        && !lib.hasClass(tar, disableClasses[0])) {
                        var index = tar.getAttribute('data-index');
                        selectIndex(this, index);
                        return;
                    }
                    tar = tar.parentNode;
                }
            }

            /**
             * 更新显示数据
             *
             * @inner
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @param {MonthView} monthView MonthView控件实例
             * @param {String} type 日历类型 begin | end
             */
            function updateView(monthView, type) {
                var date = monthView.getRawValue();
                if (!date) {
                    return;
                }
                this.view[type] = date;
                // 更新shortcut
                var selectedIndex = getSelectedIndex(this, this.view);
                paintMiniCal(this, selectedIndex);
                updateHighlightRange(this);
            }

            /**
             * 确定按钮的点击处理
             *
             * @inner
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             */
            function commitValue(calendar) {
                var me = calendar;
                var view = calendar.view;

                var begin = view.begin;
                var end = view.end;
                if (calendar.isEndless) {
                    end = null;
                }
                var dvalue = end - begin;
                if (!end) {
                    dvalue = begin;
                }
                var value;
                if (dvalue > 0) {
                    value = {
                        'begin': begin,
                        'end': end
                    };
                }
                else if (end !== null) {
                    value = {
                        'begin': end,
                        'end': begin
                    };
                }

                var event = me.fire('beforechange', { value: value });

                // 阻止事件，则不继续运行
                if (event.isDefaultPrevented()) {
                    return false;
                }

                me.rawValue = value;
                me.value = me.convertToParam(value);
                updateMain(me, value);
                me.layer.hide();
                me.fire('change', value);
            }

            /**
             * 更新主显示
             *
             * @inner
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @param {{begin:Date,end:Date}=} range 外部设置的日期
             */
            function updateMain(calendar, range) {
                var text = calendar.helper.getPart('text');
                text.innerHTML = getValueText(calendar, range);
            }


            /**
             * 将对象型rawValue转换成字符串
             *
             * @inner
             * @param {{begin:Date,end:Date}=} rawValue 外部设置的日期
             * @return {string} 2011-03-01,2011-04-01
             */
            RangeCalendar.prototype.convertToParam = function (rawValue) {
                var beginTime = rawValue.begin;
                var endTime = rawValue.end;

                var beginTail = ' 00:00:00';
                var endTail = ' 23:59:59';

                var timeResult = [];
                timeResult.push(m(beginTime).format('YYYY-MM-DD') + beginTail);
                if (endTime) {
                    timeResult.push(m(endTime).format('YYYY-MM-DD') + endTail);
                }
                return timeResult.join(',');
            };

            /**
             * 将字符串转换成对象型rawValue
             * 可重写
             *
             * @inner
             * @param {string} value 目标日期字符串 ‘YYYY-MM-DD,YYYY-MM-DD’
             * @return {{begin:Date,end:Date}=}
             */
            RangeCalendar.prototype.convertToRaw = function(value) {
                var strDates = value.split(',');
                // 可能会只输入一个，默认当做begin，再塞一个默认的end
                if (strDates.length === 1) {
                    strDates.push('2046-11-04');
                }
                // 第一个是空的
                else if (strDates[0] === ''){
                    strDates[0] = '1983-09-03';
                }
                // 第二个是空的
                else if (strDates[1] === ''){
                    strDates[1] = '2046-11-04';
                }

                return {
                    begin: m(strDates[0], 'YYYY-MM-DD').toDate(),
                    end: m(strDates[1], 'YYYY-MM-DD').toDate()
                };
            };

            /**
             * 获取当前选中日期区间的最终显示字符（含快捷日历展示）
             *
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @param {{begin:Date,end:Date}=} rawValue 外部设置的日期
             * @return {string}
             */
            function getValueText(calendar, rawValue) {
                // 日期部分 2008-01-01至2009-09-30
                var dateText = getDateValueText(calendar, rawValue);
                // 无结束时间日历，直接返回，无需增加快捷日历描述
                if (calendar.isEndless && dateText) {
                    return dateText;
                }
                // 快捷日历
                var shortcut = '';
                if (!calendar.curMiniName
                    && calendar.miniMode !== null
                    && calendar.miniMode >= 0
                    && calendar.miniMode < calendar.shortCutItems.length) {
                    calendar.curMiniName =
                        calendar.shortCutItems[calendar.miniMode].name;
                }
                if (calendar.curMiniName) {
                    shortcut = calendar.curMiniName + '&nbsp;&nbsp;';
                }

                if (dateText) {
                    // return shortcut + dateText;
                    return dateText;
                }

                return '';
            }

            /**
             * 获取当前选中的日期区间的显示字符
             *
             * @param {RangeCalendar} calendar RangeCalendar控件实例
             * @param {{begin:Date,end:Date}=} rawValue 外部设置的日期
             * @return {string}
             */
            function getDateValueText(calendar, rawValue) {
                rawValue = rawValue || calendar.getRawValue();
                var begin = rawValue.begin;
                var end = rawValue.end;
                var pattern = calendar.dateFormat;

                if (begin && end) {
                    return m(begin).format(pattern)
                        + ' 至 '
                        + m(end).format(pattern);
                }
                else if (!end) {
                    return m(begin).format(pattern) + ' 起 ';
                }
                return '';
            }

            RangeCalendar.defaultProperties = {
                dateFormat: 'YYYY-MM-DD',
                endlessCheck: false,
                /**
                 * 日期区间快捷选项列表配置
                 */
                shortCutItems: [
                    {
                        name: '昨天',
                        value: 0,
                        getValue: function () {
                            var yesterday = new Date(this.now.getTime());
                            yesterday.setDate(yesterday.getDate() - 1);
                            return {
                                begin: yesterday,
                                end: yesterday
                            };
                        }
                    },
                    {
                        name: '最近7天',
                        value: 1,
                        getValue: function () {
                            var mDate = m(this.now);
                            return {
                                begin: mDate.clone().subtract('day', 7).toDate(),
                                end: mDate.clone().subtract('day', 1).toDate()
                            };
                        }
                    },
                    {
                        name: '上周',
                        value: 2,
                        getValue: function () {
                            var now = this.now;
                            var begin = new Date(now.getTime());
                            var end = new Date(now.getTime());
                            var startOfWeek = 1; // 周一为第一天;

                            if (begin.getDay() < startOfWeek % 7) {
                                begin.setDate(
                                        begin.getDate() - 14 + startOfWeek - begin.getDay()
                                );
                            } else {
                                begin.setDate(
                                        begin.getDate() - 7 - begin.getDay() + startOfWeek % 7
                                );
                            }
                            begin.setHours(0, 0, 0, 0);
                            end.setFullYear(
                                begin.getFullYear(),
                                begin.getMonth(),
                                    begin.getDate() + 6
                            );
                            end.setHours(0, 0, 0, 0);

                            return {
                                begin: begin,
                                end: end
                            };
                        }
                    },
                    {
                        name: '本月',
                        value: 3,
                        getValue: function () {
                            return {
                                begin: m(this.now).startOf('month').toDate(),
                                end: m(this.now).toDate()
                            };
                        }
                    },
                    {
                        name: '上个月',
                        value: 4,
                        getValue: function () {
                            var begin =
                                m(this.now).subtract('month', 1)
                                    .startOf('month').toDate();
                            var end =
                                m(this.now).startOf('month')
                                    .subtract('day', 1).toDate();
                            return {
                                begin: begin,
                                end: end
                            };
                        }
                    },
                    {
                        name: '上个季度',
                        value: 5,
                        getValue: function () {
                            var now = this.now;
                            var begin = m(now)
                                .subtract('month', now.getMonth() % 3 + 3)
                                .startOf('month').toDate();
                            var end = m(now)
                                .subtract('month', now.getMonth() % 3)
                                .startOf('month').subtract('day', 1).toDate();
                            return {
                                begin: begin,
                                end: end
                            };
                        }
                    }
                ]
            };

            /**
             * 控件类型
             *
             * @type {string}
             */
            RangeCalendar.prototype.type = 'RangeCalendar';

            /**
             * 初始化参数
             *
             * @param {Object=} options 构造函数传入的参数
             * @override
             * @protected
             */
            RangeCalendar.prototype.initOptions = function (options) {
                var now = this.now;
                var defaultRaw = { begin: now, end: now };
                /**
                 * 默认选项配置
                 */
                var properties = {
                    range: {
                        begin: new Date(1983, 8, 3),
                        end: new Date(2046, 10, 4)
                    },
                    // 默认今天
                    rawValue: defaultRaw,
                    // 默认今天
                    view: u.clone(defaultRaw),
                    value: this.convertToParam(defaultRaw),
                    shownShortCut: '昨天,最近7天,上周,本月,上个月,上个季度'
                };
                lib.extend(properties, RangeCalendar.defaultProperties);

                helper.extractValueFromInput(this, options);

                // 设置了value，以value为准
                if (options.value) {
                    options.rawValue = this.convertToRaw(options.value);
                    options.view = {
                        begin: options.rawValue.begin,
                        end: options.rawValue.end
                    };
                    options.miniMode = null;
                }
                // 设置了rawValue，以rawValue为准，外部设置的miniMode先清空
                else if (options.rawValue) {
                    options.miniMode = null;
                }
                // 没有设置rawValue，设置了‘miniMode’，rawValue按照miniMode计算
                else if (!options.rawValue && options.miniMode != null) {
                    var shortcutItem =
                        properties.shortCutItems[options.miniMode];
                    if (shortcutItem) {
                        options.rawValue =
                            shortcutItem.getValue.call(this, this.now);
                        options.miniMode = parseInt(options.miniMode, 10);
                    }
                    else {
                        options.miniMode = null;
                    }
                }

                lib.extend(properties, options);

                if (properties.range && typeof properties.range === 'string') {
                    properties.range = this.convertToRaw(properties.range);
                }

                if (properties.endlessCheck === 'false') {
                    properties.endlessCheck = false;
                }

                if (properties.endlessCheck) {
                    if (properties.isEndless === 'false') {
                        properties.isEndless = false;
                    }
                }
                else {
                    // 如果值中没有end，则默认日历是无限型的
                    if (!properties.rawValue.end) {
                        properties.endlessCheck = true;
                        properties.isEndless = true;
                    }
                }
                // 如果是无限的，结束时间无需默认值
                if (properties.isEndless) {
                    properties.endlessCheck = true;
                    properties.rawValue.end = null;
                    properties.view.end = null;
                    properties.view.value = this.convertToParam({
                        begin: now,
                        end: null
                    });
                }
                this.setProperties(properties);
            };


            /**
             * 初始化DOM结构
             *
             * @protected
             */
            RangeCalendar.prototype.initStructure = function () {
                // 如果主元素是输入元素，替换成`<div>`
                // 如果输入了非块级元素，则不负责
                if (lib.isInput(this.main)) {
                    helper.replaceMain(this);
                }

                var tpl = [
                    '<div class="${className}" id="${id}"></div>',
                    '<div class="${arrow}"></div>'
                ];

                this.main.innerHTML = lib.format(
                    tpl.join('\n'),
                    {
                        className: this.helper.getPartClassName('text'),
                        id: helper.getId(this, 'text'),
                        arrow: this.helper.getPartClassName('arrow')
                    }
                );

            };

            /**
             * 初始化事件交互
             *
             * @protected
             * @override
             */
            RangeCalendar.prototype.initEvents = function () {
                this.helper.addDOMEvent(this.main, 'mousedown', u.bind(this.layer.toggle, this.layer));
            };

            /**
             * 重新渲染视图
             * 仅当生命周期处于RENDER时，该方法才重新渲染
             *
             * @param {Array=} 变更过的属性的集合
             * @override
             */
            RangeCalendar.prototype.repaint = helper.createRepaint(
                InputControl.prototype.repaint,
                {
                    name: ['rawValue', 'range'],
                    paint: function (calendar, rawValue, range) {
                        if (range) {
                            if (typeof range === 'string') {
                                range = calendar.convertToRaw(range);
                            }
                            // 还要支持只设置begin或只设置end的情况
                            if (!range.begin) {
                                // 设置一个特别远古的年
                                range.begin = new Date(1983, 8, 3);
                            }
                            else if (!range.end) {
                                // 设置一个特别未来的年
                                range.end = new Date(2046, 10, 4);
                            }
                            calendar.range = range;
                        }
                        if (rawValue) {
                            updateMain(calendar, rawValue);
                        }
                        if (calendar.layer) {
                            paintLayer(calendar, rawValue);
                        }
                    }
                },
                {
                    name: ['disabled', 'hidden', 'readOnly'],
                    paint: function (calendar, disabled, hidden, readOnly) {
                        if (disabled || hidden || readOnly) {
                            calendar.layer.hide();
                        }
                    }
                },
                {
                    name: 'isEndless',
                    paint: function (calendar, isEndless) {
                        // 不是无限日历，不能置为无限
                        if (!calendar.endlessCheck) {
                            calendar.isEndless = false;
                        }
                        else {
                            var endlessCheck =
                                calendar.getChild('endlessCheck');
                            if (endlessCheck) {
                                endlessCheck.setChecked(isEndless);
                            }
                        }
                    }
                }
            );

            /**
             * 设置日期
             *
             * @param {Date} date 选取的日期.
             */
            RangeCalendar.prototype.setRawValue = function (date) {
                this.setProperties({ 'rawValue': date });
            };

            /**
             * 获取选取日期值
             *
             * @return {Date}
             */
            RangeCalendar.prototype.getRawValue = function () {
                return this.rawValue;
            };


            /**
             * 将value从原始格式转换成string
             *
             * @param {*} rawValue 原始值
             * @return {string}
             */
            RangeCalendar.prototype.stringifyValue = function (rawValue) {
                return this.convertToParam(rawValue) || '';
            };

            /**
             * 将string类型的value转换成原始格式
             *
             * @param {string} value 字符串值
             * @return {*}
             */
            RangeCalendar.prototype.parseValue = function (value) {
                return this.convertToRaw(value);
            };

            /**
             * 更新高亮区间
             *
             * @param {RangeCalendar} calendar RangeCalendar控件实例

             */
            function updateHighlightRange(calendar) {
                function updateSingleMonth(monthView, monthViewType) {
                    var begin = new Date(monthView.year, monthView.month, 1);
                    begin = m(begin);
                    var end = begin.clone().endOf('month');
                    var cursor = begin;
                    while (cursor <= end) {
                        var highlight = true;
                        if (monthViewType === 'begin'
                            && (cursor <= m(rangeBegin) || cursor > m(rangeEnd))) {
                            highlight = false;
                        }
                        else if (monthViewType === 'end'
                            && (cursor < m(rangeBegin) || cursor >= m(rangeEnd))) {
                            highlight = false;
                        }

                        changeHighlightState(monthView, cursor.toDate(), highlight);
                        cursor.add('day', 1);
                    }
                }

                function changeHighlightState (monthView, date, highlight) {
                    var dateItem = monthView.getDateItemHTML(date);
                    if (highlight) {
                        monthView.helper.addPartClasses('month-item-highlight', dateItem);
                    }
                    else {
                        monthView.helper.removePartClasses('month-item-highlight', dateItem);
                    }
                }

                var beginMonth = calendar.getChild('beginCal');
                var endMonth = calendar.getChild('endCal');
                var rangeBegin = calendar.view.begin;
                var rangeEnd = calendar.view.end;
                updateSingleMonth(beginMonth, 'begin');
                updateSingleMonth(endMonth, 'end');

            }

            RangeCalendar.prototype.dispose = function () {
                if (helper.isInStage(this, 'DISPOSED')) {
                    return;
                }

                if (this.layer) {
                    this.layer.dispose();
                    this.layer = null;
                }

                InputControl.prototype.dispose.apply(this, arguments);
            };

            lib.inherits(RangeCalendar, InputControl);
            ui.register(RangeCalendar);

            return RangeCalendar;
        }
    );

/**
 * ESUI (Enterprise Simple UI)
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @ignore
 * @file 渲染器模块
 * @author otakustay
 */
define('ESUI/painters',['require','./lib','underscore'],function (require) {
        var u = require('underscore');
        var lib = require('./lib');

        /**
         * @class painters
         *
         * 渲染器模块，用于提供生成`painter`方法的工厂方法
         *
         * @singleton
         */
        var painters = {};

        /**
         * 生成一个将属性与控件状态关联的渲染器
         *
         * 当属性变为`true`的时候使用`addState`添加状态，反之使用`removeState`移除状态
         *
         * @param {string} name 指定负责的属性名，同时也是状态名称
         * @return {Object} 一个渲染器配置
         */
        painters.state = function (name) {
            return {
                name: name,
                paint: function (control, value) {
                    var method = value ? 'addState' : 'removeState';
                    control[method](this.name);
                }
            };
        };

        /**
         * 生成一个将控件属性与控件主元素元素的属性关联的渲染器
         *
         * 当控件属性变化时，将根据参数同步到主元素元素的属性上
         *
         *     @example
         *     // 将target属性与<a>元素关联
         *     var painter = painters.attribute('target');
         *
         *     // 可以选择关联到不同的DOM属性
         *     var painter = painters.attribute('link', 'href');
         *
         *     // 可以指定DOM属性的值
         *     var painter = painters.attribute('active', 'checked', true);
         *
         * @param {string} name 指定负责的属性名
         * @param {string} [attribute] 对应DOM属性的名称，默认与`name`相同
         * @param {Mixed} [value] 固定DOM属性的值，默认与更新的值相同
         * @return {Object} 一个渲染器配置
         */
        painters.attribute = function (name, attribute, value) {
            return {
                name: name,
                attribute: attribute || name, 
                value: value,
                paint: function (control, value) {
                    value = this.value == null ? value : this.value;
                    control.main.setAttribute(this.attribute, value);
                }
            };
        };

        // 这些属性不用加`px`
        var unitProperties = {
            width: true,
            height: true,
            top: true,
            right: true,
            bottom: true,
            left: true,
            fontSize: true,
            padding: true,
            paddingTop: true, 
            paddingRight: true,
            paddingBottom: true,
            paddingLeft: true,
            margin: true,
            marginTop: true,
            marginRight: true,
            marginBottom: true,
            marginLeft: true,
            borderWidth: true,
            borderTopWidth: true,
            borderRightWidth: true,
            borderBottomWidth: true,
            borderLeftWidth: true
        };

        /**
         * 生成一个将控件属性与控件主元素元素的样式关联的渲染器
         *
         * 当控件属性变化时，将根据参数同步到主元素元素的样式上
         *
         * @param {string} name 指定负责的属性名
         * @param {string} [property] 对应的样式属性名，默认与`name`相同
         * @return {Object} 一个渲染器配置
         */
        painters.style = function (name, property) {
            return {
                name: name,
                property: property || name,
                paint: function (control, value) {
                    if (value == null) {
                        return;
                    }
                    if (unitProperties.hasOwnProperty(this.property)) {
                        value = value === 0 ? '0' : value + 'px';
                    }
                    control.main.style[this.property] = value;
                }
            };
        };

        /**
         * 生成一个将控件属性与某个DOM元素的HTML内容关联的渲染器
         *
         * 当控件属性变化时，对应修改DOM元素的`innerHTML`
         *
         * @param {string} name 指定负责的属性名
         * @param {string | Function} [element] 指定DOM元素在当前控件下的部分名，
         * 可以提供函数作为参数，则函数返回需要更新的DOM元素
         * @param {Function} [generate] 指定生成HTML的函数，默认直接使用控件属性的值
         * @return {Object} 一个渲染器配置
         */
        painters.html = function (name, element, generate) {
            return {
                name: name,
                element: element || '',
                generate: generate,
                paint: function (control, value) {
                    var element = typeof this.element === 'function'
                        ? this.element(control)
                        : this.element
                            ? control.helper.getPart(this.element)
                            : control.main;
                    if (element) {
                        var html = typeof this.generate === 'function'
                            ? this.generate(control, value)
                            : value;
                        element.innerHTML = html || '';
                    }
                }
            };
        };

        /**
         * 生成一个将控件属性与某个DOM元素的HTML内容关联的渲染器
         *
         * 当控件属性变化时，对应修改DOM元素的文本内容
         *
         * 本方法与{@link painters#html}相似，区别在于会将内容进行一次HTML转义
         *
         * @param {string} name 指定负责的属性名
         * @param {string | Function} [element] 指定DOM元素在当前控件下的部分名，
         * 可以提供函数作为参数，则函数返回需要更新的DOM元素
         * @param {Function} [generate] 指定生成HTML的函数，默认直接使用控件属性的值，
         * 该函数返回原始的HTML，不需要做额外的转义工作
         * @return {Object} 一个渲染器配置
         */
        painters.text = function (name, element, generate) {
            return {
                name: name,
                element: element || '',
                generate: generate,
                paint: function (control, value) {
                    var element = typeof this.element === 'function'
                        ? this.element(control)
                        : this.element
                            ? control.helper.getPart(this.element)
                            : control.main;
                    if (element) {
                        var html = typeof this.generate === 'function'
                            ? this.generate(control, value)
                            : value;
                        element.innerHTML = u.escape(html || '');
                    }
                }
            };
        };


        /**
         * 生成一个将控件属性的变化代理到指定成员的指定方法上
         *
         * @param {string} name 指定负责的属性名
         * @param {string} member 指定成员名
         * @param {string} method 指定调用的方法名称
         * @return {Object} 一个渲染器配置
         */
        painters.delegate = function (name, member, method) {
            return {
                name: name,
                member: this.member,
                method: this.method,
                paint: function (control, value) {
                    control[this.member][this.method](value);
                }
            };
        };

        /**
         * 通过提供一系列`painter`对象创建`repaint`方法
         *
         * 本方法接受以下2类作为“渲染器”：
         *
         * - 直接的函数对象
         * - 一个`painter`对象
         *
         * 当一个直接的函数对象作为“渲染器”时，会将`changes`和`changesIndex`两个参数
         * 传递给该函数，函数具有最大的灵活度来自由操作控件
         *
         * 一个`painter`对象必须包含以下属性：
         *
         * - `{string | string[]} name`：指定这个`painter`对应的属性或属性集合
         * - `{Function} paint`：指定渲染的函数
         *
         * 一个`painter`在执行时，其`paint`函数将接受以下参数：
         *
         * - `{Control} control`：当前的控件实例
         * - `{Mixed} args...`：根据`name`配置指定的属性，依次将属性的最新值作为参数
         *
         * @param {Object... | Function...} args `painter`对象
         * @return {Function} `repaint`方法的实现
         */
        painters.createRepaint = function () {
            var painters = [].concat.apply([], [].slice.call(arguments));

            return function (changes, changesIndex) {
                // 临时索引，不能直接修改`changesIndex`，会导致子类的逻辑错误
                var index = lib.extend({}, changesIndex);
                for (var i = 0; i < painters.length; i++) {
                    var painter = painters[i];

                    // 如果是一个函数，就认为这个函数处理所有的变化，直接调用一下
                    if (typeof painter === 'function') {
                        painter.apply(this, arguments);
                        continue;
                    }

                    // 其它情况下，走的是`painter`的自动化属性->函数映射机制
                    var propertyNames = [].concat(painter.name);

                    // 以下2种情况下要调用：
                    // 
                    // - 第一次重会（没有`changes`）
                    // - `changesIndex`有任何一个负责的属性的变化
                    var shouldPaint = !changes;
                    if (!shouldPaint) {
                        for (var j = 0; j < propertyNames.length; j++) {
                            var name = propertyNames[j];
                            if (changesIndex.hasOwnProperty(name)) {
                                shouldPaint = true;
                                break;
                            }
                        }
                    }
                    if (!shouldPaint) {
                        continue;
                    }

                    // 收集所有属性的值
                    var properties = [this];
                    for (var j = 0; j < propertyNames.length; j++) {
                        var name = propertyNames[j];
                        properties.push(this[name]);
                        // 从索引中删除，为了后续构建`unpainted`数组
                        delete index[name]; 
                    }
                    // 绘制
                    try {
                        painter.paint.apply(painter, properties);
                    }
                    catch (ex) {
                        var paintingPropertyNames = 
                            '"' + propertyNames.join('", "') + '"';
                        var error = new Error(
                            'Failed to paint [' + paintingPropertyNames + '] '
                            + 'for control "' + (this.id || 'anonymous')+ '" '
                            + 'of type ' + this.type + ' '
                            + 'because: ' + ex.message
                        );
                        error.actualError = error;
                        throw error;
                    }

                }

                // 构建出未渲染的属性集合
                var unpainted = [];
                for (var key in index) {
                    if (index.hasOwnProperty(key)) {
                        unpainted.push(index[key]);
                    }
                }

                return unpainted;
            };
        };

        return painters;
    }
);

/**
 * ESUI (Enterprise Simple UI)
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file 提示层
 * @author dbear
 */

define('esui/TipLayer',['require','./Button','./Label','./Panel','./lib','./controlHelper','./Control','./main','./painters','underscore'], function (require) {
        require('./Button');
        require('./Label');
        require('./Panel');

        var u = require('underscore');
        var lib = require('./lib');
        var helper = require('./controlHelper');
        var Control = require('./Control');
        var ui = require('./main');
        var paint = require('./painters');

        /**
         * 提示层控件类
         *
         * @constructor
         * @param {Object} options 初始化参数
         */
        function TipLayer(options) {
            Control.apply(this, arguments);
        }

        /**
         * 渲染控件前重绘控件
         *
         */
        function parseMain(options) {
            var main = options.main;
            // 如果main未定义，则不作解析
            if (!main) {
                return;
            }
            var els = lib.getChildren(main);
            var len = els.length;
            var roleName;
            var roles = {};

            while (len--) {
                roleName = els[len].getAttribute('data-role');
                if (roleName) {
                    // 不再校验，如果设置了相同的data-role，
                    // 直接覆盖
                    roles[roleName] = els[len];
                }
            }

            options.roles = roles;

        }

        /**
         * 构建提示层标题栏
         *
         * @param {ui.TipLayer} 控件对象
         * @param {HTMLElement} mainDOM head主元素
         * @inner
         */
        function createHead(control, mainDOM) {
            if (mainDOM) {
                control.title = mainDOM.innerHTML;
            }
            else {
                mainDOM = document.createElement('h3');
                if (control.main.firstChild) {
                    lib.insertBefore(mainDOM, control.main.firstChild);
                }
                else {
                    control.main.appendChild(mainDOM);
                }
            }
            var headClasses = [].concat(
                helper.getPartClasses(control, 'title')
            );
            lib.addClasses(mainDOM, headClasses);
            var properties = {
                main: mainDOM,
                childName: 'title'
            };
            var label = ui.create('Label', properties);
            label.render();
            control.addChild(label);
            return label;

        }

        /**
         * 构建提示层主内容和底部内容
         *
         * @param {ui.TipLayer} control 控件
         * @param {string} type foot | body
         * @param {HTMLElement} mainDOM body或foot主元素
         * @inner
         */
        function createBF(control, type, mainDOM) {
            if (mainDOM) {
                control.content = mainDOM.innerHTML;
            }
            else {
                mainDOM = document.createElement('div');
                if (type === 'body') {
                    // 找到head
                    var head = control.getChild('title');
                    if (head) {
                        lib.insertAfter(mainDOM, head.main);
                    }
                    // 放到第一个
                    else if (control.main.firstChild) {
                        lib.insertBefore(
                            mainDOM, head, control.main.firstChild
                        );
                    }
                    else {
                        control.main.appendChild(mainDOM);
                    }
                }
                else {
                    control.main.appendChild(mainDOM);
                }
            }

            lib.addClasses(
                mainDOM,
                helper.getPartClasses(control, type + '-panel')
            );
            var properties = {
                main: mainDOM,
                renderOptions: control.renderOptions
            };

            var panel = ui.create('Panel', properties);
            panel.render();
            control.addChild(panel, type);
            return panel;
        }

        /**
         * 页面resize时事件的处理函数
         *
         * @param {ui.TipLayer} tipLayer 控件
         * @param {HTMLElement} targetElement 提示层绑定元素
         * @param {object} 定位参数
         * @inner
         */
        function resizeHandler(tipLayer, targetElement, options) {
            // 隐藏状态不触发
            if (!tipLayer.isShow) {
                return;
            }
            tipLayer.autoPosition(
                targetElement,
                options
            );
        }

        /**
         * 默认延迟展现时间
         * @type {number}
         */
        var DEFAULT_DELAY_SHOW = 0;

        /**
         * 默认延迟隐藏时间
         * @type {number}
         */
        var DEFAULT_DELAY_HIDE = 150;

        /**
         * 延迟展现
         *
         * @param {ui.TipLayer} tipLayer 控件
         * @param {number} delayTime 延迟时间
         * @param {HTMLElement} targetElement 绑定元素
         * @param {Object=} options 构造函数传入的参数
         * @inner
         */
        function delayShow(tipLayer, delayTime, targetElement, options) {
            delayTime = delayTime || DEFAULT_DELAY_SHOW;
            if (delayTime) {
                clearTimeout(tipLayer.showTimeout);
                clearTimeout(tipLayer.hideTimeout);
                tipLayer.showTimeout = setTimeout(
                    lib.bind(tipLayer.show, tipLayer, targetElement, options),
                    delayTime
                );
            }
            else {
                tipLayer.show(targetElement, options);
            }
        }

        /**
         * 延迟隐藏
         *
         * @param {ui.TipLayer} tipLayer 控件
         * @param {number=} delayTime 延迟时间
         * @inner
         */
        function delayHide(tipLayer, delayTime) {
            delayTime = delayTime || DEFAULT_DELAY_HIDE;
            clearTimeout(tipLayer.showTimeout);
            clearTimeout(tipLayer.hideTimeout);
            tipLayer.hideTimeout =
                setTimeout(lib.bind(tipLayer.hide, tipLayer), delayTime);
        }

        function getElementByControl(tipLayer, control) {
            if (typeof control === 'string') {
                control = tipLayer.viewContext.get(control);
            }
            return control.main;
        }

        TipLayer.prototype = {
            /**
             * 控件类型
             *
             * @type {string}
             */
            type: 'TipLayer',

            /**
             * 初始化参数
             *
             * @param {Object=} options 构造函数传入的参数
             * @override
             * @protected
             */
            initOptions: function (options) {
                //由main解析
                parseMain(options);
                /**
                 * 默认TipLayer选项配置
                 */
                var properties = {
                    roles: {}
                };

                lib.extend(properties, options);
                this.setProperties(properties);
            },

            /**
             * 初始化DOM结构，仅在第一次渲染时调用
             */
            initStructure: function () {
                var main = this.main;
                // 判断main是否在body下，如果不在，要移到body下
                if (main.parentNode
                    && main.parentNode.nodeName.toLowerCase() !== 'body') {
                    document.body.appendChild(main);
                }

                // 设置样式
                this.main.style.left = '-10000px';

                // 不是所有的提示层都需要title
                if (this.title || this.roles.title) {
                    createHead(this, this.roles.title);
                }
                createBF(this, 'body', this.roles.content);

                // 不是所有的提示层都需要foot
                if (this.foot || this.roles.foot) {
                    createBF(this, 'foot', this.roles.foot);
                }


                if (this.arrow) {
                    var arrow = document.createElement('div');
                    // 初始化箭头
                    arrow.id = helper.getId(this, 'arrow');
                    arrow.className =
                        helper.getPartClasses(this, 'arrow').join(' ');
                    this.main.appendChild(arrow);
                }
            },



            /**
             * 重新渲染视图
             * 仅当生命周期处于RENDER时，该方法才重新渲染
             *
             * @param {Array=} 变更过的属性的集合
             * @override
             */
            repaint: helper.createRepaint(
                Control.prototype.repaint,
                paint.style('width'),
                {
                    name: 'title',
                    paint: function (tipLayer, value) {
                        // 取消了title
                        var head = tipLayer.getHead();
                        if (value == null) {
                            if (head) {
                                tipLayer.removeChild(head);
                            }
                        }
                        else {
                            if (!head) {
                                head = createHead(tipLayer);
                            }
                            head.setText(value);
                        }
                    }
                },
                {
                    name: 'content',
                    paint: function (tipLayer, value) {
                        var bfTpl = ''
                            + '<div class="${class}" id="${id}">'
                            + '${content}'
                            + '</div>';
                        // 获取body panel
                        var body = tipLayer.getBody();
                        var bodyId = helper.getId(tipLayer, 'body');
                        var bodyClass = helper.getPartClasses(tipLayer, 'body');
                        var data = {
                            'class': bodyClass.join(' '),
                            'id': bodyId,
                            'content': value
                        };
                        body.setContent(
                            lib.format(bfTpl, data)
                        );
                    }
                },
                {
                    name: 'foot',
                    paint: function (tipLayer, value) {
                        var bfTpl = ''
                            + '<div class="${class}" id="${id}">'
                            + '${content}'
                            + '</div>';
                        var footId = helper.getId(tipLayer, 'foot');
                        var footClass = helper.getPartClasses(tipLayer, 'foot');
                        // 取消了foot
                        var foot = tipLayer.getFoot();
                        if (value == null) {
                            if (foot) {
                                tipLayer.removeChild(foot);
                            }
                        }
                        else {
                            var data = {
                                'class': footClass.join(' '),
                                'id': footId,
                                'content': value
                            };
                            if (!foot) {
                                foot = createBF(tipLayer, 'foot');
                            }
                            foot.setContent(
                                lib.format(bfTpl, data)
                            );
                        }
                    }
                },
                {
                    name: [
                        'targetDOM', 'targetControl',
                        'showMode', 'positionOpt', 'delayTime', 'showDuration'
                    ],
                    paint:
                        function (tipLayer, targetDOM, targetControl,
                            showMode, positionOpt, delayTime, showDuration) {
                        var options = {
                            targetDOM: targetDOM,
                            targetControl: targetControl,
                            showMode: showMode,
                            delayTime: delayTime || DEFAULT_DELAY_SHOW,
                            showDuration: showDuration || DEFAULT_DELAY_HIDE
                        };
                        if (positionOpt) {
                            positionOpt = positionOpt.split('|');
                            options.positionOpt = {
                                top: positionOpt[0] || 'top',
                                right: positionOpt[1] || 'left'
                            };
                        }
                        tipLayer.attachTo(options);
                    }
                }
            ),

            /**
             * 让当前层靠住一个元素
             *
             * @param {HTMLElement} target 目标元素
             * @param {Object=} options 停靠相关的选项
             * @param {string=} options.top 指示目标的上边缘靠住当前层的哪个边，
             * 可选值为**top**或**bottom**
             * @param {string=} options.bottom 指示目标的下边缘靠住当前层的哪个边，
             * 可选值为**top**或**bottom**，* 当`top`值为**bottom**时，该值无效
             * @param {string=} options.left 指示目标的左边缘靠住当前层的哪个边，
             * 可选值为**left**或**right**
             * @param {string=} options.right 指示目标的右边缘靠住当前层的哪个边，
             * 可选值为**left**或**right**，* 当`left`值为**right**时，该值无效
             * @param {number=} options.width 指定层的宽度
             * @param {number=} options.height 指定层的高度
             * @public
             */
            autoPosition: function (target, options) {
                var tipLayer = this;
                var element = this.main;
                options = options || { left: 'right', top: 'top' };

                var rect = target.getBoundingClientRect();
                var offset = lib.getOffset(target);
                var targetPosition = {
                    top: rect.top,
                    right: rect.right,
                    bottom: rect.bottom,
                    left: rect.left,
                    width: rect.right - rect.left,
                    height: rect.bottom - rect.top
                };

                // 浮层的存在会影响页面高度计算，必须先让它消失，
                // 但在消失前，又必须先计算到浮层的正确高度
                var previousDisplayValue = element.style.display;
                element.style.display = 'block';
                var elementHeight = element.offsetHeight;
                var elementWidth = element.offsetWidth;
                element.style.display = 'none';

                var config = u.omit(options, 'targetControl');

                var viewWidth = lib.page.getViewWidth();
                var viewHeight = lib.page.getViewHeight();

                // 计算出所有的位置
                // 目标元素 —— 层元素
                // left —— right
                var gapLR = targetPosition.left - elementWidth;
                // right —— left
                var gapRL = viewWidth - targetPosition.right - elementWidth;

                // top —— top
                var gapTT = viewHeight - targetPosition.top - elementHeight;
                // bottom —— bottom
                var gapBB = targetPosition.bottom - elementHeight;


                if (gapLR >= 0) {
                    if (gapRL >= 0){
                        // 如果没有设置，哪边大放哪边
                        if (!config.right && !config.left) {
                            if (gapRL < gapLR) {
                                config.left = 'right';
                                config.right = null;
                            }
                            else {
                                config.right = 'left';
                                config.left = null;
                            }
                        }
                    }
                    else {
                        config.left = 'right';
                        config.right = null;
                    }
                }
                else {
                    config.right = 'left';
                    config.left = null;
                }

                if (gapTT >= 0) {
                    if (gapBB >= 0){
                        // 如果没有设置，哪边大放哪边
                        if (!config.bottom && !config.top) {
                            if (gapBB < gapTT) {
                                config.top = 'top';
                                config.bottom = null;
                            }
                            else {
                                config.bottom = 'bottom';
                                config.top = null;
                            }
                        }
                    }
                    else {
                        config.top = 'top';
                        config.bottom = null;
                    }
                }
                else {
                    config.bottom = 'bottom';
                    config.top = null;
                }

                var properties = {};
                var arrowClass;
                if (config.right) {
                    properties.left = offset.right;
                    if (config.top) {
                        arrowClass = 'lt';
                    }
                    else {
                        arrowClass = 'lb';
                    }
                }
                else if (config.left) {
                    properties.left = offset.left - elementWidth;
                    // properties.left = offset.left;
                    if (config.top) {
                        arrowClass = 'rt';
                    }
                    else {
                        arrowClass = 'rb';
                    }
                }

                if (config.top) {
                    // properties.top = offset.top;
                    properties.top = offset.bottom;
                }
                else if (config.bottom) {
                    properties.top = offset.bottom - elementHeight;
                }

                element.style.display = previousDisplayValue;

                element.className = ''
                    + helper.getPartClasses(tipLayer).join(' ')
                    + ' '
                    + helper.getPartClasses(tipLayer, arrowClass).join(' ');

                var arrow = lib.g(helper.getId(tipLayer, 'arrow'));
                if (arrow) {
                    arrow.className = ''
                        + helper.getPartClasses(tipLayer, 'arrow').join(' ')
                        + ' '
                        + helper.getPartClasses(
                            tipLayer, 'arrow' + '-' + arrowClass
                        ).join(' ');
                }
                tipLayer.renderLayer(element, properties);
            },

            /**
             * 渲染层样式
             *
             * @param {HTMLElement} element 提示层元素
             * @param {object} 定位参数
             * @inner
             */
            renderLayer: function (element, options) {
                var properties = lib.clone(options || {});

                // 如果同时有`top`和`bottom`，则计算出`height`来
                if (properties.hasOwnProperty('top')
                    && properties.hasOwnProperty('bottom')
                ) {
                    properties.height = properties.bottom - properties.top;
                    delete properties.bottom;
                }
                // 同样处理`left`和`right`
                if (properties.hasOwnProperty('left')
                    && properties.hasOwnProperty('right')
                ) {
                    properties.width = properties.right - properties.left;
                    delete properties.right;
                }

                // 避免原来的属性影响
                if (properties.hasOwnProperty('top')
                    || properties.hasOwnProperty('bottom')
                ) {
                    element.style.top = '';
                    element.style.bottom = '';
                }

                if (properties.hasOwnProperty('left')
                    || properties.hasOwnProperty('right')
                ) {
                    element.style.left = '';
                    element.style.right = '';
                }

                // 设置位置和大小
                for (var name in properties) {
                    if (properties.hasOwnProperty(name)) {
                        element.style[name] = properties[name] + 'px';
                    }
                }
            },
            /**
             * 将提示层捆绑到一个DOM元素或控件上
             *
             * @param {Object=} options 绑定参数
             *    {string} showMode 展示触发模式
             *    {string} targetDOM 绑定元素的id
             *    {ui.Control | string} targetControl 绑定控件的实例或id
             *    {number} delayTime 延迟展示时间
             *    {number} showDuration 展示后自动隐藏的延迟时间
             *    {Object=} positionOpt 层布局参数
             */
            attachTo: function (options) {
                var showMode = options.showMode || 'over';

                var targetElement;
                if (options.targetDOM) {
                    targetElement = lib.g(options.targetDOM);
                }
                else if (options.targetControl) {
                    targetElement =
                        getElementByControl(this, options.targetControl);
                }

                if (!targetElement) {
                    return;
                }

                switch (showMode) {
                    case 'auto':
                        this.initAutoMode(options);
                        break;
                    case 'over':
                        this.initOverMode(options);
                        break;
                    case 'click':
                        this.initClickMode(options);
                        break;
                }
            },

            /**
             * 获取初始化时的事件方法集
             *
             * @param {Object=} options 绑定参数
             *    {string} showMode 展示触发模式
             *    {string} targetDOM 绑定元素的id
             *    {ui.Control | string} targetControl 绑定控件的实例或id
             *    {number} delayTime 延迟展示时间
             *    {number} showDuration 展示后自动隐藏的延迟时间
             *    {Object=} positionOpt 层布局参数
             * @returns {Object}
             */
            getInitHandlers: function (options) {
                var me = this;

                var targetElement;
                if (options.targetDOM) {
                    targetElement = lib.g(options.targetDOM);
                }
                else if (options.targetControl) {
                    targetElement =
                        getElementByControl(this, options.targetControl);
                }

                if (!targetElement) {
                    return;
                }

                // 处理方法集
                var handler = {
                    targetElement: targetElement,
                    // 浮层相关方法
                    layer: {
                        /**
                         * 展现浮层
                         */
                        show: lib.curry(
                            delayShow, me, options.delayTime,
                            targetElement, options.positionOpt
                        ),

                        /**
                         * 隐藏浮层
                         */
                        hide: lib.curry(delayHide, me),

                        /**
                         * 绑定浮层展现的默认事件，针对于targetDOM
                         * @param {string=} showEvent 事件名称，例如click、mouseup
                         * @param {Function=} callback 回调方法
                         */
                        bind: function (showEvent, callback) {
                            showEvent = showEvent || 'mouseup';
                            // 配置展现的触发事件
                            helper.addDOMEvent(
                                me, targetElement, showEvent, function (e) {
                                    handler.layer.show();
                                    // 点击其他区域隐藏事件绑定
                                    handler.clickOutsideHide.bind();
                                    if (typeof callback == 'function') {
                                        callback();
                                    }
                                    e.stopPropagation();
                                }
                            );
                        }
                    },

                    /**
                     * 点击外部隐藏浮层的相应处理
                     */
                    clickOutsideHide: {
                        /**
                         * 绑定于浮层元素上的阻止冒泡的方法
                         */
                        preventPopMethod: function (e) {
                            e.stopPropagation();
                        },

                        /**
                         * 绑定于body主体上面的隐藏layer的方法
                         */
                        method: function () {
                            handler.layer.hide();
                            handler.clickOutsideHide.unbind();
                        },

                        /**
                         * 绑定
                         */
                        bind: function () {
                            helper.addDOMEvent(
                                me, document.documentElement,
                                'mouseup',
                                handler.clickOutsideHide.method
                            );

                            // 为主体layer元素配置阻止冒泡，防止点击关闭
                            helper.addDOMEvent(
                                me, me.main, 'mouseup',
                                handler.clickOutsideHide.preventPopMethod
                            );
                        },

                        /**
                         * 解除绑定
                         */
                        unbind: function () {
                            helper.removeDOMEvent(
                                me, document.documentElement,
                                'mouseup',
                                handler.clickOutsideHide.method
                            );
                            helper.removeDOMEvent(
                                me, me.main, 'mouseup',
                                handler.clickOutsideHide.preventPopMethod
                            );
                        }
                    }
                };

                return handler;
            },

            /**
             * 在绑定提示层至目标DOM时，初始化自动展现（showMode为auto）的相应行为
             *
             * @param {Object=} options 绑定参数
             *    {string} showMode 展示触发模式
             *    {string} targetDOM 绑定元素的id
             *    {ui.Control | string} targetControl 绑定控件的实例或id
             *    {number} delayTime 延迟展示时间
             *    {number} showDuration 展示后自动隐藏的延迟时间
             *    {Object=} positionOpt 层布局参数
             */
            initAutoMode: function (options) {
                var handler = this.getInitHandlers(options);

                // 直接展现浮层
                handler.layer.show();

                // 如果不是自动隐藏，则配置点击其他位置关闭
                if (!options.showDuration) {
                    // 点击其他区域隐藏事件绑定
                    handler.clickOutsideHide.bind();
                    // 之后行为变为click隐藏行为
                    handler.layer.bind('mouseup');
                }
                else {
                    // 自动隐藏
                    setTimeout(function () {
                        // 执行隐藏
                        handler.layer.hide(options.showDuration);
                        // 之后行为变为click隐藏行为
                        handler.layer.bind('mouseup');

                    }, options.delayTime);
                }
            },

            /**
             * 在绑定提示层至目标DOM时，初始化点击展现（showMode为click）的相应行为
             *
             * @param {Object=} options 绑定参数
             *    {string} showMode 展示触发模式
             *    {string} targetDOM 绑定元素的id
             *    {ui.Control | string} targetControl 绑定控件的实例或id
             *    {number} delayTime 延迟展示时间
             *    {number} showDuration 展示后自动隐藏的延迟时间
             *    {Object=} positionOpt 层布局参数
             */
            initClickMode: function (options) {
                var handler = this.getInitHandlers(options);

                // 鼠标点击在目标DOM上展现提示层
                handler.layer.bind('mouseup');
            },

            /**
             * 在绑定提示层至目标DOM时，初始化悬浮触发展现（showMode为over）的相应行为
             *
             * @param {HtmlElement} 目标DOM
             * @param {Object=} options 绑定参数
             *    {string} showMode 展示触发模式
             *    {string} targetDOM 绑定元素的id
             *    {ui.Control | string} targetControl 绑定控件的实例或id
             *    {number} delayTime 延迟展示时间
             *    {number} showDuration 展示后自动隐藏的延迟时间
             *    {Object=} positionOpt 层布局参数
             */
            initOverMode: function (options) {
                var handler = this.getInitHandlers(options);

                // 鼠标悬浮在目标DOM上展现提示层
                handler.layer.bind('mouseover');

                // 防止点击targetElement导致浮层关闭
                helper.addDOMEvent(
                    this, handler.targetElement, 'mouseup', function (e) {
                        e.stopPropagation();
                    }
                );

                // 如果是mouseover，还要配置main的mouseover事件
                // 否则浮层会自动隐藏
                helper.addDOMEvent(
                    this, this.main, 'mouseover',
                    lib.bind(
                        this.show, this, handler.targetElement,
                        options.positionOpt
                    )
                );

                // 鼠标划出目标元素，隐藏
                this.helper.addDOMEvent(
                    handler.targetElement,
                    'mouseout',
                    function () {
                        handler.layer.hide(options.showDuration);
                    }
                );

                this.helper.addDOMEvent(
                    this.main,
                    'mouseout',
                    function () {
                        handler.layer.hide(options.showDuration);
                    }
                );

                helper.addDOMEvent(
                    this, this.main, 'mouseup', function (e) {
                        e.stopPropagation();
                    }
                );
            },

            /**
             * 获取提示层腿部的控件对象
             *
             *
             * @return {ui.Panel}
             */
            getHead: function () {
                return this.getChild('title');
            },

            /**
             * 获取提示层主体的控件对象
             *
             *
             * @return {ui.Panel}
             */
            getBody: function () {
                return this.getChild('body');
            },


            /**
             * 获取提示层腿部的控件对象
             *
             *
             * @return {ui.Panel}
             */
            getFoot: function () {
                return this.getChild('foot');
            },

            /**
             * 显示提示层
             * @param {HTMLElement} targetElement 提示层的捆绑元素
             *
             */
            show: function (targetElement, options) {
                if (helper.isInStage(this, 'INITED')) {
                    this.render();
                }
                else if (helper.isInStage(this, 'DISPOSED')) {
                    return;
                }

                clearTimeout(this.hideTimeout);

                helper.addDOMEvent(
                    this, window, 'resize',
                    lib.curry(resizeHandler, this, targetElement, options)
                );

                // 动态计算layer的zIndex
                // this.main.style.zIndex = helper.layer.getZIndex(targetElement);

                this.removeState('hidden');

                // 定位，八种。。
                this.autoPosition(
                    targetElement,
                    options
                );

                if (this.isShow) {
                    return;
                }

                this.isShow = true;
                this.fire('show');
            },

            /**
             * 隐藏提示层
             *
             */
            hide: function () {
                if (!this.isShow) {
                    return;
                }

                this.isShow = false;
                this.addState('hidden');
                this.fire('hide');
            },


            /**
             * 设置标题文字
             *
             * @param {string} html 要设置的文字，支持html
             */
            setTitle: function (html) {
                this.setProperties({'title': html});
            },

            /**
             * 设置内容
             *
             * @param {string} content 要设置的内容，支持html.
             */
            setContent: function (content) {
                this.setProperties({'content': content});
            },

            /**
             * 设置腿部内容
             *
             * @param {string} foot 要设置的内容，支持html.
             */
            setFoot: function (foot) {
                this.setProperties({'foot': foot});
            },


            /**
             * 销毁控件
             */
            dispose: function () {
                if (helper.isInStage(this, 'DISPOSED')) {
                    return;
                }
                this.hide();
                //移除dom
                var domId = this.main.id;
                lib.removeNode(domId);
                Control.prototype.dispose.apply(this, arguments);
            }

        };


        /**
         * 一次提醒提示
         * @param {Object=} args 参数 支持如下字段
         * {string} content 提示内容
         * {HTMLElement} attachedNode 绑定提示的节点
         * {Function} onok 点击底部按钮触发事件
         * {string} okText 按钮显示文字
         */
        TipLayer.onceNotice = function (args) {
            var tipLayerPrefix = 'tipLayer-once-notice';
            var okPrefix = 'tipLayer-notice-ok';

            /**
             * 获取按钮点击的处理函数
             *
             * @private
             * @param {ui.TipLayer} tipLayer 控件对象
             * @param {string} 事件类型
             */
            function btnClickHandler(tipLayer) {
                // 有可能在参数里设置了处理函数
                var handler = tipLayer.onok;
                var isFunc = (typeof handler === 'function');
                if (isFunc) {
                    handler(tipLayer);
                }
                tipLayer.fire('ok');
                tipLayer.dispose();
            }

            var content = lib.encodeHTML(args.content) || '';

            var properties = {
                type: 'onceNotice',
                skin: 'onceNotice',
                arrow: true
            };

            lib.extend(properties, args);

            //创建main
            var main = document.createElement('div');
            document.body.appendChild(main);

            var tipLayerId = helper.getGUID(tipLayerPrefix);
            properties.id = tipLayerId;
            properties.main = main;

            properties.type = null;

            var tipLayer = ui.create('TipLayer', properties);

            tipLayer.setContent(content);

            var okText = args.okText || '知道了';
            tipLayer.setFoot(''
                + '<div data-ui="type:Button;childName:okBtn;id:'
                + tipLayerId + '-' + okPrefix + ';width:50;"'
                + 'class="'
                + helper.getPartClasses(tipLayer, 'once-notice')
                + '">'
                + okText
                + '</div>'
            );

            tipLayer.render();

            var okBtn = tipLayer.getFoot().getChild('okBtn');
            okBtn.on(
                'click',
                lib.curry(btnClickHandler, tipLayer, 'ok')
            );
            tipLayer.attachTo({
                targetDOM: args.targetDOM,
                targetControl: args.targetControl,
                showMode: 'auto',
                positionOpt: { top: 'top', right: 'left' }
            });
            return tipLayer;

        };


        lib.inherits(TipLayer, Control);
        ui.register(TipLayer);

        return TipLayer;
    }
);

/**
 * ESUI (Enterprise Simple UI)
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @ignore
 * @file 提示信息控件
 * @author lisijin, dbear, otakustay
 */
define('esui/Tip',['require','./lib','./Control','./TipLayer','./main','./Layer','underscore'], function (require) {
        var u = require('underscore');
        var Control = require('./Control');
        var ui = require('./main');
        var Layer = require('./Layer');
        var lib = require('./lib');

        require('./TipLayer');

        /**
         * 提示信息控件
         *
         * `Tip`控件是一个小图标，当鼠标移到图标上时，会出现一个层显示提示信息
         *
         * @extends Control
         * @requires TipLayer
         * @constructor
         */
        function Tip(options) {
            Control.apply(this, arguments);
        }

        // lib.inherits(Tip, Layer);

        /**
         * 控件类型，始终为`"Tip"`
         *
         * @type {string}
         * @readonly
         * @override
         */
        Tip.prototype.type = 'Tip';

        /**
         * 初始化参数
         *
         * @param {Object} [options] 构造函数传入的参数
         * @protected
         * @override
         */
        Tip.prototype.initOptions = function (options) {
            // 默认选项配置
            var properties = {
                title: '',
                content: '',
                /**
                 * @property {boolean} arrow
                 *
                 * 是否需要箭头
                 *
                 * 为了方便从DOM生成，此属性在初始化时如果为字符串`"false"`，
                 * 将被认为是布尔值`false`处理
                 *
                 * 具体参考{@link TipLayer#arrow}属性
                 */
                arrow: true,

                /**
                 * @property {string} showMode
                 *
                 * 指定信息浮层的显示方案，
                 * 具体参考{@link TipLayer#attachTo}方法的`showMode`参数的说明
                 */
                showMode: 'over',

                /**
                 * @property {number} delayTime
                 *
                 * 指定信息浮层的显示的延迟时间，以毫秒为单位，
                 * 具体参考{@link TipLayer#attachTo}方法的`delayTime`参数的说明
                 */
                delayTime: 500,

                /**
                 * @property {number} showDuration
                 *
                 * 指定信息浮层在展现后的自动隐藏的延迟时间，以毫秒为单位，为0是不自动隐藏
                 * 具体参考{@link TipLayer#attachTo}方法的`showDuration`参数的说明
                 */
                showDuration: 0
            };
            if (options.arrow === 'false') {
                options.arrow = false;
            }

            extractDOMProperties(this.main, properties);

            u.extend(properties, options);

            this.setProperties(properties);
        };

        /**
         * 从DOM中抽取`title`和`content`属性，如果有的话优先级低于外部给定的
         *
         * @param {HTMLElement} 主元素
         * @param  {Object} options 构造函数传入的参数
         * @ignore
         */
        function extractDOMProperties(main, options) {
            options.title = options.title || main.getAttribute('title');
            main.removeAttribute('title');
            options.content = options.content || main.innerHTML;
            main.innerHTML = '';
        }

        /**
         * 初始化DOM结构
         *
         * @protected
         * @override
         */
        Tip.prototype.initStructure = function () {
            var main = document.createElement('div');
            document.body.appendChild(main);
            var tipLayer = ui.create(
                'TipLayer',
                {
                    main: main,
                    childName: 'layer',
                    content: this.content,
                    title: this.title,
                    arrow: this.arrow,
                    /**
                     * @property {number} [layerWidth=200]
                     *
                     * 指定信息浮层的宽度，具体参考{@link TipLayer#width}属性
                     */
                    width: this.layerWidth || 100,
                    viewContext: this.viewContext
                }
            );
            this.addChild(tipLayer);
            tipLayer.render();

            var attachOptions = {
                showMode: this.showMode,
                delayTime: +this.delayTime,
                showDuration: +this.showDuration,
                targetControl: this,
                positionOpt: {top: 'top', right: 'left'}
            };
            tipLayer.attachTo(attachOptions);
        };

        /**
         * 重渲染
         *
         * @method
         * @protected
         * @override
         */
        Tip.prototype.repaint = require('./painters').createRepaint(
            Control.prototype.repaint,
            {
                name: 'title',
                paint: function (tip, value) {
                    var layer = tip.getChild('layer');
                    if (layer) {
                        layer.setTitle(value);
                    }
                }
            },
            {
                name: 'content',
                paint: function (tip, value) {
                    var layer = tip.getChild('layer');
                    if (layer) {
                        layer.setContent(value);
                    }
                }
            }
        );

        lib.inherits(Tip, Control);
        ui.register(Tip);
        return Tip;
    }
);

    require('esui/RangeCalendar');

    require('esui/Tip');
    require('esui/TipLayer');
    _global['esui'] = require('esui/main');

})(window);
