(ns recordsets.server
  (:require [ring.adapter.jetty :as jetty]
            [recordsets.common :as common]
            [ring.util.response :as response]
            [cheshire.core :as json]
            [cheshire.generate :as cg])
  (:import (java.util UUID)
           (java.time LocalDate)
           (com.fasterxml.jackson.core JsonGenerator))
  (:gen-class))

(defonce STATIC_INIT
  (cg/add-encoder LocalDate
    (fn [date ^JsonGenerator json-generator]
      (.writeString json-generator ^String (common/serialize-date date)))))


(defn initial-db []
  {:birthdate (sorted-set-by common/asc-by-date-of-birth)
   :gender    (sorted-set-by common/asc-by-gender-and-last-name)
   :name      (sorted-set-by common/desc-by-last-name)})


(defonce db (atom (initial-db)))


(defn persist! [record]
  (let [identifier     (str (UUID/randomUUID))
        record-with-id (assoc record :id identifier)]
    (swap! db #(reduce-kv (fn [m k v] (assoc m k (conj v record-with-id))) {} %))
    record-with-id))


(defn json-middleware [handler]
  (fn [request]
    (-> (handler request)
        (update :body json/generate-string)
        (response/content-type "application/json"))))


(defn error-middleware [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (let [{:keys [message status]} (ex-data e)]
          (-> {:error (or message (ex-message e))}
              (json/generate-string)
              (response/response)
              (response/status (or status 500))))))))


(defn route-handler [{:keys [request-method uri body]}]
  (case [request-method uri]

    [:get "/records/gender"]
    {:body (:gender @db) :status 200}

    [:get "/records/birthdate"]
    {:body (:birthdate @db) :status 200}

    [:get "/records/name"]
    {:body (:name @db) :status 200}

    [:post "/records"]
    {:body (persist! (common/validate-and-parse-input-row (slurp body))) :status 201}

    {:status 404 :body {:error "Route not found."}}))

(def application (-> route-handler json-middleware error-middleware))

(defn -main [& _]
  (let [ring-opts {:port 3000 :join? true}]
    (jetty/run-jetty (fn [req] (#'application req)) ring-opts)))

