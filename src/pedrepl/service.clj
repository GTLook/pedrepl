(ns pedrepl.service
  (:require [io.pedestal.http :as http]
            [com.stuartsierra.component :as component]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (println "something")
  (ring-resp/response "Hello User!"))

(defn increment
  [request]
  (ring-resp/response (str #_(swap! counter inc))))


;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
(defn make-routes
  [ctx]
  (route/expand-routes
      #{["/" :get home-page :route-name :home]
        ["/about" :get `about-page]}))

;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

; Terse/Vector-based routes
; (defn make-routes
;   [ctx]
;  (route/expand-routes
;     [["/" {:get home-page}
;       ;^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]
;      ["/increment" {:get increment}]]]))


;; Consumed by pedrepl.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service-base {:env :prod
              ;; ::http/interceptors []
              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"
              ::http/type :jetty
            ;;::http/host "localhost"
              ::http/port 8080
              ::http/join? false
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})



  (defrecord Pedestal [data-source service]
    component/Lifecycle

    (start
      [this]
      (if service
        this
          (let [config (merge service-base this),
          db-interceptor
          (interceptor/interceptor {:name :database-interceptor
          :enter
          (fn [context]
            (println "something else")
            (update context :request assoc :data-source data-source))})
              routes (make-routes config)
              service (-> config (assoc ::http/routes routes ::http/interceptors [db-interceptor])
               http/default-interceptors
               http/create-server
               http/start)]
            (assoc this :service service))))

    (stop
      [this]
      (when (and service (not (= :test (:env this))))
        (http/stop service))
        (assoc this :service nil)))

  ;constructor function
  (defn new-pedestal
    []
    (map->Pedestal {}))
