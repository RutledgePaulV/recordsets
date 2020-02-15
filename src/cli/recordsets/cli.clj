(ns recordsets.cli
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as strings]
            [clojure.java.io :as io]
            [recordsets.common :as common])
  (:gen-class))

(def sort-options
  {:birthdate common/asc-by-date-of-birth
   :gender    common/asc-by-gender-and-last-name
   :name      common/desc-by-last-name})

(def csv-sort-options
  (strings/join ", " (map name (keys sort-options))))

(def cli-options
  [["-f" "--files <files>"
    :parse-fn #(set (strings/split % #","))
    :default #{}]
   ["-s" "--sort <sort>"
    :parse-fn keyword
    :default :name
    :validate [sort-options (str "Sort options: " csv-sort-options)]]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Recordsets"
        ""
        "Options:"
        options-summary]
       (strings/join \newline)))

(defn error-message [errors]
  (str "Errors encountered parsing your command:" \newline (strings/join \newline errors)))

(defn record->table-row [record]
  (let [cells      (for [[k v] common/headers]
                     ((:output v identity) (get record k)))
        fmt-string (strings/join "" (repeat (count cells) "%-15s"))]
    (apply format fmt-string cells)))

(defn exit
  ([] (exit ""))
  ([msg] (exit msg 0))
  ([msg status] (do (println msg) (flush) (System/exit status))))

(defn read-source [source]
  (with-open [reader (io/reader source)]
    (mapv common/validate-and-parse-input-row (line-seq reader))))

(defn -main [& args]
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      (exit (usage summary))
      (not-empty errors)
      (exit (error-message errors) 1)
      :otherwise
      (try
        (->> (mapcat read-source (:files options))
             (sort (sort-options (:sort options)))
             (run! (comp println record->table-row)))
        (catch Exception e
          (exit (ex-message e) 1))))))