(defproject recordsets "0.1.0-SNAPSHOT"

  :dependencies
  [[org.clojure/clojure "1.10.1"]]

  :source-paths
  ["src/common"]

  :test-paths
  ["test/common"]

  :profiles
  {:cli
   {:source-paths ["src/cli"]
    :test-paths   ["test/cli"]
    :dependencies [[org.clojure/tools.cli "0.4.2"]]
    :main         recordsets.cli
    :uberjar-name "recordsets-cli.jar"}

   :server
   {:source-paths ["src/server"]
    :test-paths   ["test/server"]
    :dependencies [[ring "1.8.0"] [cheshire "5.10.0"]]
    :main         recordsets.server
    :uberjar-name "recordsets-server.jar"}

   :dev
   [:cli
    :server
    {:source-paths ["repl"]
     :repl-options {:init-ns user}}]

   :test
   [:dev :server]

   :uberjar
   {:aot :all}}

  :plugins
  [[lein-cloverage "1.1.2"]]

  :aliases
  {"build-cli"    ["with-profile" "+cli" "uberjar"]
   "build-server" ["with-profile" "+server" "uberjar"]})
