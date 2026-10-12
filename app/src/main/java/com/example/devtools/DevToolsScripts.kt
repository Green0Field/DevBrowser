package com.example.devtools

import com.example.model.BoxModelMetrics
import com.example.model.BrowserTab
import com.example.model.DomNodeItem
import com.example.model.HomeShortcut
import com.example.model.JsSnippet
import com.example.model.QuickBookmark
import com.example.model.ShortcutSource
import com.example.model.StorageEntry
import com.example.model.StorageType

object DevToolsScripts {

    val DEFAULT_DOM_NODES = listOf(
        DomNodeItem(
            path = "n1",
            cssSelector = "html",
            tag = "html",
            attributes = mapOf("lang" to "en"),
            childCount = 2,
            depth = 0
        ),
        DomNodeItem(
            path = "n2",
            cssSelector = "html > body",
            tag = "body",
            childCount = 1,
            depth = 1
        ),
        DomNodeItem(
            path = "n3",
            cssSelector = "body > main#app-main",
            tag = "main",
            idAttr = "app-main",
            childCount = 1,
            depth = 2
        ),
        DomNodeItem(
            path = "n4",
            cssSelector = "main#app-main > section#customizable-box.card",
            tag = "section",
            idAttr = "customizable-box",
            classAttr = "card",
            inlineStyle = "padding: 18px; border-radius: 16px;",
            childCount = 4,
            depth = 3,
            computedStyles = mapOf(
                "display" to "block",
                "padding" to "18px",
                "border-radius" to "16px",
                "width" to "360px",
                "height" to "180px"
            ),
            boxModel = BoxModelMetrics(
                width = 360,
                height = 180,
                marginBottom = 14,
                borderTop = 1,
                borderRight = 1,
                borderBottom = 1,
                borderLeft = 1,
                paddingTop = 18,
                paddingRight = 18,
                paddingBottom = 18,
                paddingLeft = 18
            )
        ),
        DomNodeItem(
            path = "n5",
            cssSelector = "section#customizable-box > h1#hero-heading",
            tag = "h1",
            idAttr = "hero-heading",
            textPreview = "DevBrowser Inspection Target",
            depth = 4
        ),
        DomNodeItem(
            path = "n6",
            cssSelector = "section#customizable-box > p#hero-description",
            tag = "p",
            idAttr = "hero-description",
            textPreview = "Use the Inspect tool or Elements panel below to inspect and modify this page.",
            depth = 4
        )
    )

    val DEFAULT_STORAGE_ENTRIES = listOf(
        StorageEntry("devbrowser_theme", "dark-blue", StorageType.LOCAL_STORAGE),
        StorageEntry("test_page_url", "devbrowser:test", StorageType.LOCAL_STORAGE),
        StorageEntry("session_id", "s_devbrowser01", StorageType.SESSION_STORAGE),
        StorageEntry("devbrowser_mode", "inspector", StorageType.COOKIES)
    )

    val DEFAULT_PINNED_SHORTCUTS = listOf(
        HomeShortcut(
            id = "sc_test",
            title = "DevTools Test",
            url = BrowserTab.URL_TEST,
            isPinned = true,
            source = ShortcutSource.PINNED,
            count = 5
        ),
        HomeShortcut(
            id = "sc_mdn",
            title = "MDN Web Docs",
            url = "https://developer.mozilla.org",
            isPinned = true,
            source = ShortcutSource.PINNED,
            count = 4
        )
    )

    val DEFAULT_BOOKMARKS = listOf(
        QuickBookmark("DevBrowser Test Page", BrowserTab.URL_TEST, "Internal"),
        QuickBookmark("Example.com", "https://example.com", "Web"),
        QuickBookmark("Hacker News", "https://news.ycombinator.com", "Tech"),
        QuickBookmark("MDN Web Docs", "https://developer.mozilla.org", "Docs"),
        QuickBookmark("Wikipedia", "https://en.m.wikipedia.org", "Reference"),
        QuickBookmark("HTTPBin HTML", "https://httpbin.org/html", "Test")
    )

    val DEFAULT_SNIPPETS = listOf(
        JsSnippet(
            id = "css_xray",
            title = "Toggle CSS Outlines",
            description = "Draw 1px cyan outlines around all elements",
            code = """
                (function() {
                  var id = '__devscope_xray';
                  var el = document.getElementById(id);
                  if (el) { el.remove(); return 'Removed CSS outlines'; }
                  var s = document.createElement('style');
                  s.id = id;
                  s.textContent = '* { outline: 1px solid rgba(56, 189, 248, 0.5) !important; }';
                  document.head.appendChild(s);
                  return 'Outlined ' + document.querySelectorAll('*').length + ' elements';
                })();
            """.trimIndent()
        ),
        JsSnippet(
            id = "extract_links",
            title = "List All Links",
            description = "Log all <a href> URLs on the page",
            code = """
                Array.from(document.querySelectorAll('a[href]')).map(function(a) {
                  return { text: a.innerText.trim() || '[link]', href: a.href };
                });
            """.trimIndent()
        ),
        JsSnippet(
            id = "dom_stats",
            title = "DOM Tree Stats",
            description = "Count total elements and nesting depth",
            code = """
                (function() {
                  var all = document.querySelectorAll('*');
                  var maxD = 0;
                  all.forEach(function(e) {
                    var d = 0, p = e;
                    while (p && p.parentElement) { d++; p = p.parentElement; }
                    if (d > maxD) maxD = d;
                  });
                  return { elements: all.length, maxDepth: maxD, title: document.title };
                })();
            """.trimIndent()
        ),
        JsSnippet(
            id = "dark_invert",
            title = "Invert Page Colors",
            description = "Toggle CSS invert(1) hue-rotate(180deg)",
            code = """
                (function() {
                  var h = document.documentElement;
                  h.style.filter = h.style.filter ? '' : 'invert(1) hue-rotate(180deg)';
                  return h.style.filter ? 'Inverted colors' : 'Restored colors';
                })();
            """.trimIndent()
        ),
        JsSnippet(
            id = "editable_page",
            title = "Toggle designMode (Edit Page Text)",
            description = "Make the entire webpage directly editable on tap",
            code = """
                (function() {
                  document.designMode = (document.designMode === 'on') ? 'off' : 'on';
                  return 'document.designMode = ' + document.designMode;
                })();
            """.trimIndent()
        )
    )

    val PLAYGROUND_HTML = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <meta name="color-scheme" content="dark light" />
          <meta name="description" content="DevBrowser Test Page (devbrowser:test) for Chrome DevTools inspection" />
          <title>DevBrowser Test Page</title>
          <style>
            :root {
              --bg: #090f1e;
              --surface: #101b33;
              --accent: #3b82f6;
              --text: #f8fafc;
              --muted: #94a3b8;
              --status-bg: #090d16;
              --status-text: #bae6fd;
            }
            @media (prefers-color-scheme: light) {
              :root {
                --bg: #eef5ff;
                --surface: #f7faff;
                --accent: #2563eb;
                --text: #0f172a;
                --muted: #475569;
                --status-bg: #dceafe;
                --status-text: #1e3a8a;
              }
            }
            * { box-sizing: border-box; }
            body {
              margin: 0;
              padding: 20px;
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
              background: var(--bg);
              color: var(--text);
              line-height: 1.5;
              transition: background-color 0.2s ease, color 0.2s ease;
            }
            section.card {
              background: var(--surface);
              border: 1px solid rgba(148, 163, 184, 0.24);
              border-radius: 16px;
              padding: 18px;
              margin-bottom: 14px;
              transition: background-color 0.2s ease, border-color 0.2s ease;
            }
            h1 {
              margin: 0 0 6px 0;
              font-size: 19px;
            }
            p {
              margin: 0 0 12px 0;
              font-size: 13px;
              color: var(--muted);
            }
            .actions {
              display: flex;
              flex-wrap: wrap;
              gap: 8px;
            }
            button {
              background: var(--accent);
              color: #fff;
              border: none;
              border-radius: 12px;
              padding: 9px 14px;
              font-size: 12px;
              font-weight: 600;
              cursor: pointer;
            }
            button.secondary { background: #334155; color: #fff; }
            button.emerald { background: #059669; color: #fff; }
            button.rose { background: #e11d48; color: #fff; }
            #status-bar {
              margin-top: 12px;
              font-family: monospace;
              font-size: 11px;
              color: var(--status-text);
              background: var(--status-bg);
              padding: 8px 10px;
              border-radius: 8px;
            }
          </style>
        </head>
        <body>
          <main id="app-main">
            <section id="customizable-box" class="card" style="padding: 18px; border-radius: 16px;">
              <h1 id="hero-heading">DevBrowser Inspection Target</h1>
              <p id="hero-description">
                Use the Inspect tool or Elements panel below to inspect and modify this page, or test network and console events.
              </p>
              <div class="actions">
                <button id="btn-style" onclick="cycleTheme()">Toggle Card Style</button>
                <button id="btn-api-ok" class="emerald" onclick="triggerMockApi('GET', '/api/v1/status', 200)">Fetch 200 JSON</button>
                <button id="btn-api-post" class="secondary" onclick="triggerMockApi('POST', '/api/v1/events', 201)">POST Payload</button>
                <button id="btn-log-err" class="rose" onclick="emitSampleError()">Log Error</button>
              </div>
              <div id="status-bar">Ready — Located at devbrowser:test</div>
            </section>
          </main>

          <script>
            try {
              if (!localStorage.getItem('devbrowser_theme')) {
                localStorage.setItem('devbrowser_theme', 'dark-blue');
                localStorage.setItem('test_page_url', 'devbrowser:test');
              }
              if (!sessionStorage.getItem('session_id')) {
                sessionStorage.setItem('session_id', 's_' + Math.random().toString(36).substring(2, 9));
              }
              document.cookie = "devbrowser_mode=inspector; path=/";
            } catch (e) {}

            var toggled = false;
            function cycleTheme() {
              var card = document.getElementById('customizable-box');
              toggled = !toggled;
              if (toggled) {
                card.style.borderColor = 'var(--accent)';
                card.style.borderWidth = '2px';
              } else {
                card.style.borderColor = 'rgba(148, 163, 184, 0.24)';
                card.style.borderWidth = '1px';
              }
              document.getElementById('status-bar').innerText = 'Updated #customizable-box inline style';
              console.log('Toggled #customizable-box style:', { highlighted: toggled });
              if (window.__devScopeRefreshDom) window.__devScopeRefreshDom('');
            }

            function emitSampleError() {
              console.error('TypeError: Failed to read property "id" of undefined at onClick (test.js:48:12)');
              document.getElementById('status-bar').innerText = 'Logged sample error to Console';
            }

            window.triggerMockApi = function(method, path, status) {
              document.getElementById('status-bar').innerText = method + ' ' + path + ' -> HTTP ' + status;
              if (window.__devScopeRecordSyntheticFetch) {
                window.__devScopeRecordSyntheticFetch(method, 'devbrowser:test' + path, status);
              }
            };

            if (window.matchMedia) {
              var mq = window.matchMedia('(prefers-color-scheme: dark)');
              if (mq && mq.addEventListener) {
                mq.addEventListener('change', function(e) {
                  var sb = document.getElementById('status-bar');
                  if (sb) sb.innerText = 'prefers-color-scheme changed to ' + (e.matches ? 'dark' : 'light');
                });
              }
            }
          </script>
        </body>
        </html>
    """.trimIndent()

    fun buildColorSchemeSyncScript(
        isDark: Boolean,
        accentHex: String,
        bgHex: String,
        surfaceHex: String
    ): String {
        val scheme = if (isDark) "dark" else "light"
        return """
            (function() {
              var isDark = $isDark;
              var scheme = '$scheme';
              window.__devScopePrefersDark = isDark;
              if (!window.__devScopeMediaListeners) {
                window.__devScopeMediaListeners = [];
              }

              // 1. Patch window.matchMedia so websites querying prefers-color-scheme receive DevBrowser's theme preference
              if (window.matchMedia && !window.__devScopeMatchMediaPatched) {
                window.__devScopeMatchMediaPatched = true;
                var origMatchMedia = window.matchMedia.bind(window);
                window.matchMedia = function(query) {
                  var q = String(query || '');
                  var m = q.match(/prefers-color-scheme\s*:\s*(dark|light)/i);
                  if (m) {
                    var targetScheme = m[1].toLowerCase();
                    var mql = {
                      media: q,
                      get matches() {
                        return targetScheme === 'dark' ? !!window.__devScopePrefersDark : !window.__devScopePrefersDark;
                      },
                      onchange: null,
                      addListener: function(cb) {
                        if (typeof cb === 'function') window.__devScopeMediaListeners.push({ mql: mql, cb: cb });
                      },
                      removeListener: function(cb) {
                        window.__devScopeMediaListeners = window.__devScopeMediaListeners.filter(function(x) { return x.cb !== cb; });
                      },
                      addEventListener: function(type, cb) {
                        if (type === 'change' && typeof cb === 'function') {
                          window.__devScopeMediaListeners.push({ mql: mql, cb: cb });
                        }
                      },
                      removeEventListener: function(type, cb) {
                        if (type === 'change') {
                          window.__devScopeMediaListeners = window.__devScopeMediaListeners.filter(function(x) { return x.cb !== cb; });
                        }
                      },
                      dispatchEvent: function() { return true; }
                    };
                    return mql;
                  }
                  return origMatchMedia(query);
                };
              }

              // 2. Apply root color-scheme CSS property, meta tag, and common website theme attributes
              var root = document.documentElement;
              if (root) {
                root.style.colorScheme = scheme;
                root.setAttribute('data-theme', scheme);
                root.setAttribute('data-color-mode', scheme);
                root.setAttribute('data-bs-theme', scheme);
                if (root.classList) {
                  root.classList.toggle('dark', isDark);
                  root.classList.toggle('light', !isDark);
                }
              }
              if (document.head) {
                var meta = document.querySelector('meta[name="color-scheme"]');
                if (!meta) {
                  meta = document.createElement('meta');
                  meta.setAttribute('name', 'color-scheme');
                  document.head.appendChild(meta);
                }
                meta.setAttribute('content', scheme);
              }

              // 3. Synchronize @media (prefers-color-scheme: dark|light) CSS rules in accessible stylesheets
              try {
                var sheets = document.styleSheets || [];
                for (var s = 0; s < sheets.length; s++) {
                  var rules;
                  try { rules = sheets[s].cssRules; } catch (e) { continue; }
                  if (!rules) continue;
                  for (var r = 0; r < rules.length; r++) {
                    var rule = rules[r];
                    if (rule && rule.media) {
                      var orig = rule.__devScopeOrigMedia || rule.media.mediaText || '';
                      if (!rule.__devScopeOrigMedia && /prefers-color-scheme/i.test(orig)) {
                        rule.__devScopeOrigMedia = orig;
                      }
                      if (rule.__devScopeOrigMedia) {
                        var wantsDark = /prefers-color-scheme\s*:\s*dark/i.test(rule.__devScopeOrigMedia);
                        var wantsLight = /prefers-color-scheme\s*:\s*light/i.test(rule.__devScopeOrigMedia);
                        if (wantsDark) {
                          rule.media.mediaText = isDark ? 'all' : 'not all';
                        } else if (wantsLight) {
                          rule.media.mediaText = !isDark ? 'all' : 'not all';
                        }
                      }
                    }
                  }
                }
              } catch (e) {}

              // 4. Update devbrowser:test page theme variables if on the internal test page
              if (root && (window.location.href.indexOf('devbrowser.internal') !== -1 || document.getElementById('customizable-box'))) {
                root.style.setProperty('--bg', '$bgHex');
                root.style.setProperty('--surface', '$surfaceHex');
                root.style.setProperty('--accent', '$accentHex');
                root.style.setProperty('--text', isDark ? '#f8fafc' : '#0f172a');
                root.style.setProperty('--muted', isDark ? '#94a3b8' : '#475569');
                root.style.setProperty('--status-bg', isDark ? '#090d16' : '$surfaceHex');
                root.style.setProperty('--status-text', '$accentHex');
              }

              // 5. Notify all registered matchMedia('prefers-color-scheme') listeners
              if (window.__devScopeMediaListeners && window.__devScopeMediaListeners.length > 0) {
                window.__devScopeMediaListeners.forEach(function(item) {
                  try {
                    var evt = { matches: item.mql.matches, media: item.mql.media };
                    item.cb.call(item.mql, evt);
                    if (typeof item.mql.onchange === 'function') item.mql.onchange(evt);
                  } catch (e) {}
                });
              }
            })();
        """.trimIndent()
    }

    val DEVTOOLS_AGENT_JS = """
        (function() {
          if (window.__devScopeInstalled) {
            window.__devScopeRefreshAll();
            return;
          }
          window.__devScopeInstalled = true;
          window.__devScopeInspectMode = false;
          window.__devScopeSelectedId = '';
          window.__devScopeNodeSeq = 1;
          window['${'$'}_'] = undefined;

          function safeBridgeCall(fnName, payload) {
            try {
              if (window.DevScopeBridge && typeof window.DevScopeBridge[fnName] === 'function') {
                window.DevScopeBridge[fnName](typeof payload === 'string' ? payload : JSON.stringify(payload));
              }
            } catch (e) {}
          }

          function formatArg(a) {
            if (a === null) return 'null';
            if (a === undefined) return 'undefined';
            if (typeof a === 'string') return a;
            if (a instanceof Error) return a.stack || (a.name + ': ' + a.message);
            if (typeof a === 'object') {
              try { return JSON.stringify(a, null, 2); } catch (e) { return String(a); }
            }
            return String(a);
          }

          // 1. Console Interception
          var origLog = console.log;
          var origInfo = console.info;
          var origWarn = console.warn;
          var origError = console.error;

          function interceptConsole(level, origFn) {
            return function() {
              var args = Array.prototype.slice.call(arguments);
              var msg = args.map(formatArg).join(' ');
              safeBridgeCall('onConsoleLog', {
                level: level,
                message: msg,
                source: (window.location.hostname || 'devbrowser')
              });
              if (origFn) origFn.apply(console, arguments);
            };
          }

          console.log = interceptConsole('LOG', origLog);
          console.info = interceptConsole('INFO', origInfo);
          console.warn = interceptConsole('WARN', origWarn);
          console.error = interceptConsole('ERROR', origError);

          window.addEventListener('error', function(evt) {
            safeBridgeCall('onConsoleLog', {
              level: 'ERROR',
              message: (evt.message || 'Uncaught Error') + ' (' + (evt.filename || 'inline') + ':' + (evt.lineno || 0) + ')',
              source: 'window.onerror'
            });
          });

          window.addEventListener('unhandledrejection', function(evt) {
            safeBridgeCall('onConsoleLog', {
              level: 'ERROR',
              message: 'Unhandled Promise Rejection: ' + formatArg(evt.reason),
              source: 'Promise'
            });
          });

          // 2. Network Fetch & XHR Interception
          window.__devScopeRecordSyntheticFetch = function(method, url, status) {
            var startOffset = Math.max(10, Math.round((performance.now ? performance.now() : 80)));
            var duration = Math.floor(Math.random() * 110) + 22;
            var reqBody = method === 'POST' ? JSON.stringify({ event: "user_interaction", timestamp: Date.now() }, null, 2) : '';
            var respBody = status >= 400
              ? JSON.stringify({ error: "Not Found", status: status, path: url }, null, 2)
              : JSON.stringify({
                  ok: true,
                  status: status,
                  url: url,
                  latencyMs: duration,
                  data: { version: "1.5.0", activeNodes: document.querySelectorAll('*').length }
                }, null, 2);

            setTimeout(function() {
              safeBridgeCall('onNetworkRequest', {
                id: 'net_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6),
                method: method,
                url: url,
                status: status,
                statusText: status === 200 ? 'OK' : (status === 201 ? 'Created' : 'Not Found'),
                resourceType: 'FETCH_XHR',
                mimeType: 'application/json',
                startTimeOffsetMs: startOffset,
                durationMs: duration,
                sizeBytes: respBody.length + 180,
                requestHeaders: {
                  'Accept': 'application/json',
                  'User-Agent': navigator.userAgent,
                  'Content-Type': method === 'POST' ? 'application/json' : 'text/plain'
                },
                responseHeaders: {
                  'Content-Type': 'application/json; charset=utf-8',
                  'Cache-Control': 'no-cache'
                },
                requestBody: reqBody,
                responsePreview: respBody,
                isError: status >= 400
              });
            }, duration);
          };

          if (window.fetch) {
            var origFetch = window.fetch;
            window.fetch = function(input, init) {
              var method = (init && init.method) ? init.method.toUpperCase() : 'GET';
              var url = typeof input === 'string' ? input : (input && input.url ? input.url : String(input));
              var startMs = performance.now ? performance.now() : Date.now();
              var reqBody = (init && init.body) ? String(init.body) : '';
              return origFetch.apply(this, arguments).then(function(response) {
                var dur = Math.max(1, Math.round((performance.now ? performance.now() : Date.now()) - startMs));
                var clone = response.clone();
                clone.text().then(function(txt) {
                  var respHeaders = {};
                  try {
                    response.headers.forEach(function(v, k) { respHeaders[k] = v; });
                  } catch (e) {}
                  safeBridgeCall('onNetworkRequest', {
                    id: 'fetch_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6),
                    method: method,
                    url: url,
                    status: response.status || 200,
                    statusText: response.statusText || 'OK',
                    resourceType: 'FETCH_XHR',
                    mimeType: respHeaders['content-type'] || 'application/json',
                    startTimeOffsetMs: Math.round(startMs),
                    durationMs: dur,
                    sizeBytes: txt ? txt.length : 512,
                    requestHeaders: { 'Accept': '*/*', 'User-Agent': navigator.userAgent },
                    responseHeaders: respHeaders,
                    requestBody: reqBody,
                    responsePreview: (txt || '').substring(0, 2000),
                    isError: response.status >= 400
                  });
                }).catch(function() {});
                return response;
              });
            };
          }

          // 3. Visual Box-Model Overlay & Touch Element Picker
          function ensureOverlayElements() {
            var box = document.getElementById('__devscope_overlay_box');
            if (!box && document.body) {
              box = document.createElement('div');
              box.id = '__devscope_overlay_box';
              box.style.cssText = 'position:fixed;pointer-events:none;z-index:2147483646;display:none;box-sizing:border-box;border:2px solid #3b82f6;background:rgba(59,130,246,0.22);transition:all 0.08s ease;';
              var label = document.createElement('div');
              label.id = '__devscope_overlay_label';
              label.style.cssText = 'position:fixed;pointer-events:none;z-index:2147483647;display:none;background:#090d16;color:#f8fafc;border:1px solid #3b82f6;padding:4px 8px;border-radius:6px;font-family:monospace;font-size:11px;font-weight:bold;box-shadow:0 4px 12px rgba(0,0,0,0.5);white-space:nowrap;';
              document.body.appendChild(box);
              document.body.appendChild(label);
            }
          }

          function parsePx(val) {
            if (!val) return 0;
            var n = parseFloat(val);
            return isNaN(n) ? 0 : Math.round(n);
          }

          function assignNodeId(el) {
            if (!el || el.nodeType !== 1) return '';
            var existing = el.getAttribute('data-devscope-id');
            if (existing) return existing;
            var assigned = 'ds_' + (window.__devScopeNodeSeq++);
            el.setAttribute('data-devscope-id', assigned);
            return assigned;
          }

          function buildCssSelector(el) {
            if (!el || el.nodeType !== 1) return '';
            if (el.id) return '#' + el.id;
            var parts = [];
            var cur = el;
            while (cur && cur.nodeType === 1 && parts.length < 5) {
              var name = cur.tagName.toLowerCase();
              if (cur.id) {
                parts.unshift(name + '#' + cur.id);
                break;
              } else if (cur.className && typeof cur.className === 'string') {
                var cls = cur.className.trim().split(/\s+/)[0];
                if (cls) name += '.' + cls;
              }
              parts.unshift(name);
              cur = cur.parentElement;
            }
            return parts.join(' > ');
          }

          function serializeDomElement(el, depth) {
            var nodeId = assignNodeId(el);
            var cs = window.getComputedStyle ? window.getComputedStyle(el) : {};
            var rect = el.getBoundingClientRect ? el.getBoundingClientRect() : { width: 0, height: 0, top: 0, left: 0 };
            var attrs = {};
            if (el.attributes) {
              for (var i = 0; i < el.attributes.length; i++) {
                var a = el.attributes[i];
                if (a.name !== 'data-devscope-id') {
                  attrs[a.name] = a.value;
                }
              }
            }
            var directText = '';
            for (var c = 0; c < el.childNodes.length; c++) {
              if (el.childNodes[c].nodeType === 3) {
                var t = el.childNodes[c].nodeValue.trim();
                if (t) directText += (directText ? ' ' : '') + t;
              }
            }
            if (directText.length > 90) directText = directText.substring(0, 90) + '…';

            return {
              path: nodeId,
              cssSelector: buildCssSelector(el),
              tag: el.tagName.toLowerCase(),
              idAttr: el.id || '',
              classAttr: (typeof el.className === 'string') ? el.className.trim() : '',
              attributes: attrs,
              textPreview: directText,
              childCount: el.children ? el.children.length : 0,
              depth: depth || 0,
              inlineStyle: el.getAttribute('style') || '',
              outerHtmlPreview: (el.outerHTML || '').substring(0, 600),
              computedStyles: {
                'display': cs.display || '',
                'position': cs.position || '',
                'color': cs.color || '',
                'background-color': cs.backgroundColor || '',
                'font-family': cs.fontFamily || '',
                'font-size': cs.fontSize || '',
                'font-weight': cs.fontWeight || '',
                'line-height': cs.lineHeight || '',
                'padding': cs.padding || '',
                'margin': cs.margin || '',
                'border': cs.border || '',
                'border-radius': cs.borderRadius || '',
                'width': Math.round(rect.width) + 'px',
                'height': Math.round(rect.height) + 'px',
                'z-index': cs.zIndex || 'auto'
              },
              boxModel: {
                width: Math.round(rect.width),
                height: Math.round(rect.height),
                marginTop: parsePx(cs.marginTop),
                marginRight: parsePx(cs.marginRight),
                marginBottom: parsePx(cs.marginBottom),
                marginLeft: parsePx(cs.marginLeft),
                borderTop: parsePx(cs.borderTopWidth),
                borderRight: parsePx(cs.borderRightWidth),
                borderBottom: parsePx(cs.borderBottomWidth),
                borderLeft: parsePx(cs.borderLeftWidth),
                paddingTop: parsePx(cs.paddingTop),
                paddingRight: parsePx(cs.paddingRight),
                paddingBottom: parsePx(cs.paddingBottom),
                paddingLeft: parsePx(cs.paddingLeft)
              }
            };
          }

          window.__devScopeHighlightNode = function(nodeId, shouldScroll) {
            ensureOverlayElements();
            var el = document.querySelector('[data-devscope-id="' + nodeId + '"]');
            var box = document.getElementById('__devscope_overlay_box');
            var label = document.getElementById('__devscope_overlay_label');
            if (!el) {
              if (box) box.style.display = 'none';
              if (label) label.style.display = 'none';
              return;
            }
            window.__devScopeSelectedId = nodeId;
            window['${'$'}0'] = el;
            if (shouldScroll && el.scrollIntoView) {
              el.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
            var rect = el.getBoundingClientRect();
            if (box && label) {
              box.style.display = 'block';
              box.style.top = rect.top + 'px';
              box.style.left = rect.left + 'px';
              box.style.width = Math.max(4, rect.width) + 'px';
              box.style.height = Math.max(4, rect.height) + 'px';

              var tagTitle = el.tagName.toLowerCase();
              if (el.id) tagTitle += '#' + el.id;
              else if (el.className && typeof el.className === 'string') {
                var firstCls = el.className.trim().split(/\s+/)[0];
                if (firstCls) tagTitle += '.' + firstCls;
              }
              label.innerText = tagTitle + '  |  ' + Math.round(rect.width) + ' × ' + Math.round(rect.height) + ' px';
              label.style.display = 'block';
              var labelTop = rect.top - 28;
              if (labelTop < 6) labelTop = rect.bottom + 6;
              label.style.top = Math.max(6, Math.min(window.innerHeight - 30, labelTop)) + 'px';
              label.style.left = Math.max(6, Math.min(window.innerWidth - 200, rect.left)) + 'px';

              clearTimeout(window.__devScopeOverlayTimer);
              if (!window.__devScopeInspectMode) {
                window.__devScopeOverlayTimer = setTimeout(function() {
                  if (box) box.style.display = 'none';
                  if (label) label.style.display = 'none';
                }, 2200);
              }
            }
            safeBridgeCall('onNodeSelected', serializeDomElement(el, 0));
          };

          window.__devScopeHideOverlay = function() {
            var box = document.getElementById('__devscope_overlay_box');
            var label = document.getElementById('__devscope_overlay_label');
            if (box) box.style.display = 'none';
            if (label) label.style.display = 'none';
          };

          window.__devScopeSetInspectMode = function(enabled) {
            window.__devScopeInspectMode = !!enabled;
            if (!enabled) {
              window.__devScopeHideOverlay();
            }
          };

          document.addEventListener('click', function(e) {
            if (!window.__devScopeInspectMode) return;
            var target = e.target;
            if (!target || target.id === '__devscope_overlay_box' || target.id === '__devscope_overlay_label') return;
            e.preventDefault();
            e.stopPropagation();
            var nodeId = assignNodeId(target);
            window.__devScopeHighlightNode(nodeId, false);
            safeBridgeCall('onElementPickedFromPage', serializeDomElement(target, 0));
          }, true);

          window.__devScopeRefreshDom = function(query) {
            var list = [];
            var q = (query || '').trim();
            if (q) {
              try {
                var matched = document.querySelectorAll(q);
                for (var i = 0; i < matched.length && list.length < 160; i++) {
                  var m = matched[i];
                  if (m.id === '__devscope_overlay_box' || m.id === '__devscope_overlay_label') continue;
                  list.push(serializeDomElement(m, 0));
                }
              } catch (e) {}
            } else {
              function walk(node, depth) {
                if (!node || node.nodeType !== 1 || list.length >= 180) return;
                if (node.id === '__devscope_overlay_box' || node.id === '__devscope_overlay_label') return;
                list.push(serializeDomElement(node, depth));
                var children = node.children;
                if (children) {
                  for (var i = 0; i < children.length; i++) {
                    walk(children[i], depth + 1);
                  }
                }
              }
              walk(document.documentElement, 0);
            }
            safeBridgeCall('onDomTreeUpdated', list);
          };

          window.__devScopeApplyInlineStyle = function(nodeId, styleText) {
            var el = document.querySelector('[data-devscope-id="' + nodeId + '"]');
            if (!el) return;
            el.setAttribute('style', styleText);
            window.__devScopeHighlightNode(nodeId, false);
            window.__devScopeRefreshDom('');
          };

          window.__devScopeHideElement = function(nodeId) {
            var el = document.querySelector('[data-devscope-id="' + nodeId + '"]');
            if (!el) return;
            el.style.setProperty('display', 'none', 'important');
            window.__devScopeHighlightNode(nodeId, false);
            window.__devScopeRefreshDom('');
          };

          window.__devScopeEditNodeText = function(nodeId, newText) {
            var el = document.querySelector('[data-devscope-id="' + nodeId + '"]');
            if (!el) return;
            el.innerText = newText;
            window.__devScopeHighlightNode(nodeId, false);
            window.__devScopeRefreshDom('');
          };

          window.__devScopeSetAttribute = function(nodeId, attrName, attrVal) {
            var el = document.querySelector('[data-devscope-id="' + nodeId + '"]');
            if (!el || !attrName) return;
            if (attrVal === '') el.removeAttribute(attrName);
            else el.setAttribute(attrName, attrVal);
            window.__devScopeHighlightNode(nodeId, false);
            window.__devScopeRefreshDom('');
          };

          window.__devScopeRemoveNode = function(nodeId) {
            var el = document.querySelector('[data-devscope-id="' + nodeId + '"]');
            if (!el || el.tagName === 'HTML' || el.tagName === 'BODY') return;
            el.remove();
            window.__devScopeHideOverlay();
            window.__devScopeRefreshDom('');
          };

          // 4. Storage Inspection & Live Mutation
          window.__devScopeRefreshStorage = function() {
            var items = [];
            try {
              for (var i = 0; i < localStorage.length; i++) {
                var k = localStorage.key(i);
                items.push({ key: k, value: localStorage.getItem(k) || '', type: 'LOCAL_STORAGE' });
              }
            } catch (e) {}
            try {
              for (var j = 0; j < sessionStorage.length; j++) {
                var sk = sessionStorage.key(j);
                items.push({ key: sk, value: sessionStorage.getItem(sk) || '', type: 'SESSION_STORAGE' });
              }
            } catch (e) {}
            try {
              var rawCookie = document.cookie || '';
              if (rawCookie) {
                rawCookie.split(';').forEach(function(pair) {
                  var eq = pair.indexOf('=');
                  if (eq > 0) {
                    var ck = pair.substring(0, eq).trim();
                    var cv = pair.substring(eq + 1).trim();
                    if (ck) items.push({ key: ck, value: cv, type: 'COOKIES' });
                  }
                });
              }
            } catch (e) {}
            safeBridgeCall('onStorageUpdated', items);
          };

          window.__devScopeSetStorageItem = function(type, key, value) {
            try {
              if (type === 'LOCAL_STORAGE') localStorage.setItem(key, value);
              else if (type === 'SESSION_STORAGE') sessionStorage.setItem(key, value);
              else if (type === 'COOKIES') document.cookie = key + '=' + value + '; path=/';
            } catch (e) {}
            window.__devScopeRefreshStorage();
          };

          window.__devScopeDeleteStorageItem = function(type, key) {
            try {
              if (type === 'LOCAL_STORAGE') localStorage.removeItem(key);
              else if (type === 'SESSION_STORAGE') sessionStorage.removeItem(key);
              else if (type === 'COOKIES') document.cookie = key + '=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
            } catch (e) {}
            window.__devScopeRefreshStorage();
          };

          // 5. Performance & Lighthouse Audit Collector
          window.__devScopeCollectPerformance = function() {
            var allEls = document.querySelectorAll('*');
            var maxDepth = 0;
            allEls.forEach(function(el) {
              var d = 0, p = el;
              while (p && p.parentElement) { d++; p = p.parentElement; }
              if (d > maxDepth) maxDepth = d;
            });
            var imgs = document.querySelectorAll('img');
            var missingAlt = 0;
            imgs.forEach(function(img) {
              if (!img.hasAttribute('alt') || img.getAttribute('alt').trim() === '') missingAlt++;
            });

            var dclMs = 38;
            var loadMs = 74;
            var fpMs = 24;
            var fcpMs = 32;
            try {
              var navEntries = performance.getEntriesByType ? performance.getEntriesByType('navigation') : [];
              if (navEntries && navEntries.length > 0) {
                var nav = navEntries[0];
                dclMs = Math.max(10, Math.round(nav.domContentLoadedEventEnd || 38));
                loadMs = Math.max(dclMs, Math.round(nav.loadEventEnd || 74));
              }
              var paintEntries = performance.getEntriesByType ? performance.getEntriesByType('paint') : [];
              if (paintEntries) {
                paintEntries.forEach(function(p) {
                  if (p.name === 'first-paint') fpMs = Math.max(8, Math.round(p.startTime));
                  if (p.name === 'first-contentful-paint') fcpMs = Math.max(10, Math.round(p.startTime));
                });
              }
            } catch (e) {}

            var heapUsed = 7.8;
            var heapTotal = 24.0;
            try {
              if (performance && performance.memory) {
                heapUsed = Math.round((performance.memory.usedJSHeapSize / 1048576) * 10) / 10;
                heapTotal = Math.round((performance.memory.totalJSHeapSize / 1048576) * 10) / 10;
              }
            } catch (e) {}

            var hasViewport = !!document.querySelector('meta[name="viewport"]');
            var hasTitle = !!(document.title && document.title.trim().length > 0);
            var hasDescription = !!document.querySelector('meta[name="description"]');
            var hasLang = !!(document.documentElement.getAttribute('lang'));

            var perfScore = Math.max(60, Math.min(100, 100 - Math.floor(allEls.length / 150) - Math.floor(loadMs / 400)));
            var a11yScore = Math.max(65, Math.min(100, 100 - (missingAlt * 8) - (hasLang ? 0 : 8)));
            var bpScore = window.location.protocol === 'https:' || window.location.href.indexOf('devbrowser') !== -1 ? 96 : 82;
            var seoScore = (hasViewport ? 35 : 15) + (hasTitle ? 35 : 15) + (hasDescription ? 30 : 18);

            var audits = [
              {
                category: 'Performance',
                title: 'First Contentful Paint (FCP)',
                description: 'Time at which the first text or image is painted.',
                passed: fcpMs < 1800,
                metricValue: fcpMs + ' ms'
              },
              {
                category: 'Performance',
                title: 'DOM Size & Nesting Depth',
                description: 'Keep DOM trees under 1,500 nodes and depth under 32.',
                passed: allEls.length < 1500 && maxDepth < 32,
                metricValue: allEls.length + ' nodes (d:' + maxDepth + ')'
              },
              {
                category: 'Accessibility',
                title: 'Image elements have [alt] attributes',
                description: 'All images should provide descriptive alt text.',
                passed: missingAlt === 0,
                metricValue: missingAlt === 0 ? 'Passed' : missingAlt + ' missing'
              },
              {
                category: 'SEO',
                title: 'Document has <meta name="viewport">',
                description: 'Optimizes layout for mobile screens.',
                passed: hasViewport,
                metricValue: hasViewport ? 'Present' : 'Missing'
              }
            ];

            safeBridgeCall('onPerformanceUpdated', {
              domNodesCount: allEls.length,
              maxDomDepth: maxDepth,
              scriptsCount: document.querySelectorAll('script').length,
              stylesheetsCount: document.querySelectorAll('style, link[rel="stylesheet"]').length,
              imagesCount: imgs.length,
              missingAltImagesCount: missingAlt,
              domContentLoadedMs: dclMs,
              loadEventMs: loadMs,
              firstPaintMs: fpMs,
              firstContentfulPaintMs: fcpMs,
              jsHeapUsedMb: heapUsed,
              jsHeapTotalMb: heapTotal,
              performanceScore: perfScore,
              accessibilityScore: a11yScore,
              bestPracticesScore: bpScore,
              seoScore: seoScore,
              audits: audits
            });
          };

          window.__devScopeCaptureSources = function() {
            var html = document.documentElement ? document.documentElement.outerHTML : '';
            safeBridgeCall('onSourcesUpdated', html.substring(0, 45000));
          };

          window.__devScopeRefreshAll = function() {
            window.__devScopeRefreshDom('');
            window.__devScopeRefreshStorage();
            window.__devScopeCollectPerformance();
            window.__devScopeCaptureSources();
          };

          if (window.location.href.indexOf('devbrowser.internal') !== -1 && !window.__devScopeSeededNet) {
            window.__devScopeSeededNet = true;
            window.__devScopeRecordSyntheticFetch('GET', 'devbrowser:test/api/v1/config', 200);
          }

          window.__devScopeRefreshAll();
        })();
    """.trimIndent()
}
