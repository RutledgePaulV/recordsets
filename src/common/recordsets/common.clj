(ns recordsets.common
  (:require [clojure.string :as strings])
  (:import (java.time LocalDate)
           (java.time.format DateTimeFormatter)))

(def input-line-regex
  (let [pad    "\\s*"
        delim  "(?:\\||,|\\s+)"
        pdelim (str pad delim pad)
        word   "([^\\s\\,\\|]+)"
        date   "(\\d{4}\\-\\d{2}-\\d{2})"
        start  "^"
        end    "$"]
    (->> [start pad word pdelim word pdelim word pdelim word pdelim date pad end]
         (apply str)
         (re-pattern))))

(def input-date-format
  DateTimeFormatter/ISO_DATE)

(def output-date-format
  (DateTimeFormatter/ofPattern "M/D/YYYY"))

(defn parse-date [date-string]
  (LocalDate/parse date-string input-date-format))

(defn serialize-date [date]
  (.format output-date-format date))

(defn ascending-by [key-fn]
  #(compare (key-fn %1) (key-fn %2)))

(defn descending-by [key-fn]
  #(compare (key-fn %2) (key-fn %1)))

(def headers
  ; order intentional
  (array-map
    :last-name {:input strings/trim :output identity}
    :first-name {:input strings/trim :output identity}
    :gender {:input strings/trim :output identity}
    :favorite-color {:input strings/trim :output identity}
    :date-of-birth {:input parse-date :output serialize-date}))

(def desc-by-last-name (descending-by (juxt :last-name :id)))
(def asc-by-date-of-birth (ascending-by (juxt :date-of-birth :id)))
(def asc-by-gender-and-last-name (ascending-by (juxt :gender :last-name :id)))

(defn validate-and-parse-input-row [line]
  (if-some [result (re-find input-line-regex line)]
    (into {} (for [[prop value]
                   (map vector (keys headers) (rest result))]
               [prop ((-> prop headers :input) value)]))
    (let [message (format "Invalidly row '%s'" line)]
      (throw (ex-info message {:status 400})))))