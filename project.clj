(defproject greffe "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3123" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.4"]
                 [compojure "1.3.2"]
                 [enlive "1.1.5"]
                 [org.omcljs/om "0.8.8"]
                 [figwheel "0.2.5"]
                 [environ "1.0.0"]
                 [com.cemerick/piggieback "0.1.5"]
                 [weasel "0.6.0"]
                 [leiningen "2.5.1"]
                 [cljs-xml "0.1.0-SNAPSHOT"]
                 [gmark "0.1.0-SNAPSHOT"]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-environ "1.0.0"]
            [cider/cider-nrepl "0.9.0-SNAPSHOT"]]

  :min-lein-version "2.5.0"

  :uberjar-name "greffe.jar"
  :clean-targets  ^{:protect false}  ["resources/public/js/out"
                                      "resources/public/js/app.js"
                                      "target/cljsbuild-compiler-1"
                                      "target/testable.js"] 
  :cljsbuild {:builds
              {:app {:source-paths ["src/cljs"]
                     :compiler {:output-to     "resources/public/js/app.js"
                                :output-dir    "resources/public/js/out"
                                :source-map    "resources/public/js/out.js.map"
                                :preamble      ["react/react.min.js"]
                                :externs       ["react/externs/react.js"]
                                :optimizations :none
                                :pretty-print  true}}
               :test {:source-paths ["src/cljs" "test/cljs"]
                      :notify-command ["phantomjs"
                                       "phantom/unit-test.js"
                                       "phantom/unit-test.html"]
                      :compiler {:optimizations :whitespace
                                 :pretty-print true
                                 :output-to "target/testable.js"}}}}

  :profiles {:dev {:repl-options {:init-ns greffe.server
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :plugins [[lein-figwheel "0.1.4-SNAPSHOT"]]

                   :figwheel {:http-server-root "public"
                              :port 3449
                              :css-dirs ["resources/public/css"]}

                   :env {:is-dev true}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]}}}}

             :uberjar {:hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
