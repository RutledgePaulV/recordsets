(ns recordsets.cli-test
  (:require [recordsets.cli :refer :all]
            [clojure.test :refer :all]
            [clojure.java.io :as io])
  (:import (clojure.lang ExceptionInfo)
           (java.util.regex Pattern)))

(defmacro capture-exit [& body]
  `(with-redefs
     [exit
      (fn [msg# code#]
        (throw (ex-info "exit!" {::capture [msg# code#]})))]
     (try ~@body
      (catch ExceptionInfo e#
        (or (::capture (ex-data e#)) (throw e#))))))

(defn substring-count [s sub]
  (count (re-seq (re-pattern (Pattern/quote sub)) s)))

(deftest process-files-test
  (let [raw    (slurp (io/file "samples/data.csv"))
        output (with-out-str (process-files #{"samples/data.csv"} "name"))]
    (is (= (substring-count output "\n") (substring-count raw "\n")))))

(deftest process-bad-file
  (let [[_ code] (capture-exit (process-files #{"samples/data.nope"} "name"))]
    (is (= code 1))))