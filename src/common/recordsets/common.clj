(ns recordsets.common
  (:require [clojure.string :as strings])
  (:import (java.time LocalDate)
           (java.time.format DateTimeFormatter)))

(def parsing-regex
  (let [word-term      "([^\\s\\,\\|]+)(?:\\s*\\|\\s*|\\s*,\\s*|\\s+)"
        date-term      "(\\d{4}\\-\\d{2}-\\d{2})"
        pattern-string (strings/join "" (flatten ["^" (repeat 4 word-term) date-term "$"]))]
    (re-pattern pattern-string)))

(def input-date-format
  DateTimeFormatter/ISO_DATE)

(def output-date-format
  (DateTimeFormatter/ofPattern "M/D/YYYY"))

(defn parse-date [date-string]
  (LocalDate/parse date-string input-date-format))

(defn serialize-date [date-string]
  (.format output-date-format date-string))

(def headers
  ; order intentional
  (array-map
    :last-name {:input strings/trim :output identity}
    :first-name {:input strings/trim :output identity}
    :gender {:input strings/trim :output identity}
    :favorite-color {:input strings/trim :output identity}
    :date-of-birth {:input parse-date :output serialize-date}))

(defn ascending [key-fn]
  #(compare (key-fn %1) (key-fn %2)))

(defn descending [key-fn]
  #(compare (key-fn %2) (key-fn %1)))

(def desc-by-last-name (descending :last-name))

(def asc-by-date-of-birth (ascending :date-of-birth))

(def asc-by-gender-and-last-name (ascending (juxt :gender :last-name)))

(defn validate-and-parse-input-row [line]
  (if-some [result (re-find parsing-regex line)]
    (into {} (for [[prop value]
                   (map vector (keys headers) (rest result))]
               [prop ((-> prop headers :input) value)]))
    (let [message (format "Invalidly row '%s'" line)]
      (throw (ex-info message {:status 400})))))