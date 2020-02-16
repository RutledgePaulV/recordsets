(ns recordsets.server-tests
  (:require [recordsets.server :refer :all]
            [clojure.test :refer :all]
            [recordsets.common :as common]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as strings])
  (:import (java.io ByteArrayInputStream)))

(use-fixtures :each
  (fn [tests] (reset! db (initial-db)) (tests)))

(defn byte-stream [s]
  (ByteArrayInputStream. (.getBytes s)))

(defn valid-record? [x]
  (= (set (conj (keys common/headers) :id)) (set (keys x))))

(defn create-record [s]
  (application
    {:request-method :post
     :body           (byte-stream s)
     :uri            "/records"}))

(defn load-data [filename]
  (with-open [reader (io/reader (io/file (str "./samples/" filename)))]
    (->> (line-seq reader) (run! create-record))))

(defn check-read-endpoint [endpoint]
  (let [request  {:request-method :get :uri endpoint}
        response (application request)
        results  (json/parse-string (:body response) true)]
    (is (= 200 (:status response)))
    (is (not-empty results))
    (is (every? valid-record? results))))

(deftest creating-a-record
  (let [response (create-record "Paul, Rutledge, Male, Blue, 1992-05-14")]
    (is (= 201 (:status response)))
    (is (valid-record? (json/parse-string (:body response) true)))))

(deftest reading-records-by-name
  (load-data "data.csv")
  (check-read-endpoint "/records/name"))

(deftest reading-records-by-birthdate
  (load-data "data.csv")
  (check-read-endpoint "/records/birthdate"))

(deftest reading-records-by-gender
  (load-data "data.csv")
  (check-read-endpoint "/records/gender"))

(deftest nice-error-if-invalid-record
  (let [response (create-record "banana12312313123123123131231231231")
        results  (json/parse-string (:body response) true)]
    (is (= 400 (:status response)))
    (is (not (strings/blank? (:error results))))))

(deftest accessing-invalid-route
  (let [response (application {:request-method :get :uri "/does-not-exist"})
        results  (json/parse-string (:body response) true)]
    (is (= 404 (:status response)))
    (is (not (strings/blank? (:error results))))))