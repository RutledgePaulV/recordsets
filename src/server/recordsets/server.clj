(ns recordsets.server
  (:require [ring.adapter.jetty :as jetty]
            [recordsets.common :as common]
            [cheshire.core :as json]
            [ring.util.response :as response])
  (:gen-class))


(defonce db
  (atom {:birthdate (sorted-set-by common/asc-by-date-of-birth)
         :gender    (sorted-set-by common/asc-by-gender-and-last-name)
         :name      (sorted-set-by common/desc-by-last-name)}))


(defn persist! [record]
  (swap! db #(reduce-kv (fn [m k v] (assoc m k (conj v record))) {} %)))


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
          {:status (or status 500)
           :body   {:error (or message (ex-message e))}})))))


(defn route-handler [{:keys [request-method uri body]}]
  (case [request-method uri]

    [:get "/records/gender"]
    {:body (:gender @db)}

    [:get "/records/birthdate"]
    {:body (:birthdate @db)}

    [:get "/records/name"]
    {:body (:name @db)}

    [:post "/records"]
    (let [new-record (common/validate-and-parse-input-row (slurp body))]
      (persist! new-record)
      {:body new-record :status 201})

    {:status 404 :body {:error "Route not found."}}))


(def application
  (-> route-handler
      error-middleware
      json-middleware))


(defn -main [& _]
  (let [ring-opts {:port 3000 :join? true}]
    (jetty/run-jetty (fn [req] (#'application req)) ring-opts)))

