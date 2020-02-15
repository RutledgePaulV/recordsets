(defproject recordsets "0.1.0-SNAPSHOT"

  :dependencies
  [[org.clojure/clojure "1.10.1"]]

  :source-paths ["src/common"]

  :profiles
  {:cli
   {:source-paths ["src/cli"]
    :dependencies [[org.clojure/tools.cli "0.4.2"]]
    :main         recordsets.cli}

   :server
   {:source-paths ["src/server"]
    :dependencies [[ring "1.8.0"] [cheshire "5.10.0"]]
    :main         recordsets.server}

   :dev
   [:cli
    :server
    {:source-paths ["repl"]
     :repl-options {:init-ns user}}]

   :uberjar
   {:aot :all}}

  :aliases
  {"build-cli"    ["with-profile" "+cli" "uberjar"]
   "build-server" ["with-profile" "+server" "uberjar"]})
