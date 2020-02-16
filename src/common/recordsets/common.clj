(ns recordsets.common
  (:import (java.time LocalDate)
           (java.time.format DateTimeFormatter)))

(def input-date-format
  DateTimeFormatter/ISO_DATE)

(def output-date-format
  (DateTimeFormatter/ofPattern "M/D/YYYY"))

(defn parse-date [date-string]
  (LocalDate/parse date-string input-date-format))

(defn serialize-date [date]
  (.format output-date-format date))

(def WORD "([^\\s\\,\\|]+)")
(def DATE "(\\d{4}\\-\\d{2}-\\d{2})")
(def DELIM "\\s*(?:\\||,|\\s+)\\s*")
(def PADDING "\\s*")

(def attributes
  ; order intentional
  (array-map
    :last-name
    {:pattern WORD}
    :first-name
    {:pattern WORD}
    :gender
    {:pattern WORD}
    :favorite-color
    {:pattern WORD}
    :date-of-birth
    {:pattern DATE :input parse-date :output serialize-date}))

(def input-line-regex
  (let [contents (interpose DELIM (map :pattern (vals attributes)))]
    (re-pattern (apply str (flatten ["^" PADDING contents PADDING "$"])))))

(defn ascending-by [key-fn]
  #(compare (key-fn %1) (key-fn %2)))

(defn descending-by [key-fn]
  #(compare (key-fn %2) (key-fn %1)))

(def desc-by-last-name (descending-by (juxt :last-name :id)))
(def asc-by-date-of-birth (ascending-by (juxt :date-of-birth :id)))
(def asc-by-gender-and-last-name (ascending-by (juxt :gender :last-name :id)))

(defn validate-and-parse-input-row [line]
  (if-some [result (re-find input-line-regex line)]
    (into {} (for [[prop value]
                   (map vector (keys attributes) (rest result))]
               [prop ((-> prop attributes (:input identity)) value)]))
    (let [message (format "Invalidly row '%s'" line)]
      (throw (ex-info message {:status 400})))))