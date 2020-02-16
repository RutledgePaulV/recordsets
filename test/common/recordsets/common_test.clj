(ns recordsets.common-test
  (:require [recordsets.common :refer :all]
            [clojure.test :refer :all]
            [clojure.string :as strings]
            [clojure.java.io :as io]))

(defn read-data-file [filename]
  (let [rows
        (->> (slurp (io/file (str "./samples/" filename)))
             (strings/split-lines)
             (mapv validate-and-parse-input-row))]
    (is (not-empty rows))
    rows))

(defn contains-all-fields? [record]
  (and (= (set (keys headers)) (set (keys record)))
       (every? some? (vals record))))

(defn ascending? [coll]
  (->> (partition 2 1 coll)
       (map (fn [[a b]] (compare a b)))
       (every? (some-fn zero? neg?))))

(defn descending? [coll]
  (ascending? (reverse coll)))

(deftest parsing-input-data
  (testing "comma separated"
    (doseq [record (read-data-file "data.csv")]
      (is (contains-all-fields? record))))

  (testing "pipe separated"
    (doseq [record (read-data-file "data.psv")]
      (is (contains-all-fields? record))))

  (testing "space separated"
    (doseq [record (read-data-file "data.ssv")]
      (is (contains-all-fields? record))))

  (testing "invalid input"
    (try
      (validate-and-parse-input-row "Paul : Rutledge : Male : Blue : 1992-05-14")
      (is false "Should not have gotten this far.")
      (catch Exception e
        (let [{:keys [status] :as data} (ex-data e)]
          (is (not-empty data))
          (is (= 400 status)))))))

(deftest sorting-records
  (testing "descending by last name"
    (let [data (read-data-file "data.csv")]
      (is (->> (sort desc-by-last-name data)
               (map :last-name)
               (descending?)))))

  (testing "ascending by date of birth"
    (let [data (read-data-file "data.csv")]
      (is (->> (sort asc-by-date-of-birth data)
               (map :date-of-birth)
               (ascending?)))))

  (testing "ascending by gender then last name"
    (let [data (read-data-file "data.csv")]
      (is (->> (sort asc-by-gender-and-last-name data)
               (map (juxt :gender :last-name))
               (ascending?))))))


