(ns recordsets.cli
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as strings]
            [clojure.java.io :as io]
            [recordsets.common :as common])
  (:gen-class))

(def sort-options
  {"birthdate" common/asc-by-date-of-birth
   "gender"    common/asc-by-gender-and-last-name
   "name"      common/desc-by-last-name})

(def sort-options-validation-msg
  (str "Choose: " (strings/join ", " (map name (keys sort-options)))))

(def cli-options
  [["-f" "--files <files>" "Example: file1.csv,file2.psv,file3.ssv"
    :parse-fn #(set (strings/split % #","))
    :default #{}]
   ["-s" "--sort  <sort>" sort-options-validation-msg
    :default "name"
    :validate [sort-options sort-options-validation-msg]]
   ["-h" "--help"]])

(defn usage [options-summary]
  (strings/join \newline [(slurp (io/resource "preamble.txt")) "Options:" options-summary]))

(defn error-message [errors]
  (str "Errors encountered parsing your command:" \newline (strings/join \newline errors)))

(defn record->table-row [record]
  (let [cells      (for [[k v] common/attributes]
                     ((:output v identity) (get record k)))
        fmt-string (strings/join "" (repeat (count cells) "%-15s"))]
    (apply format fmt-string cells)))

(defn exit [msg status]
  (println msg)
  (System/exit status))

(defn read-source [source]
  (with-open [reader (io/reader source)]
    (mapv common/validate-and-parse-input-row (line-seq reader))))

(defn process-files [files sort-key]
  (try
    (->> (mapcat read-source files)
         (sort (sort-options sort-key))
         (run! (comp println record->table-row)))
    (catch Exception e
      (exit (ex-message e) 1))))

(defn -main [& args]
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) (exit (usage summary) 0)
      (not-empty errors) (exit (error-message errors) 1)
      (empty? (:files options)) (exit (usage summary) 1)
      :otherwise (process-files (:files options) (:sort options)))))