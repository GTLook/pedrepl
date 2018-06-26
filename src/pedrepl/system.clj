(ns pedrepl.system
  (:require [com.stuartsierra.component :as component]
            [pedrepl.service :as service]
            [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]))

(defn initialize
  []
  (component/system-map
   :data-source (atom 0)
   :server
   (component/using
    (service/new-pedestal)
    [:data-source])))

  ; (defn run-dev
  ;   "The entry-point for 'lein run-dev'"
  ;   [& args]
  ;   (println "\nCreating your [DEV] server...")
  ;   (-> service/service ;; start with production configuration
  ;       (merge {:env :dev
  ;               ;; do not block thread that starts web server
  ;               ::server/join? false
  ;               ;; Routes can be a function that resolve routes,
  ;               ;;  we can use this to set the routes to be reloadable
  ;               ::server/routes #(route/expand-routes (deref #'service/routes))
  ;               ;; all origins are allowed in dev mode
  ;               ::server/allowed-origins {:creds true :allowed-origins (constantly true)}
  ;               ;; Content Security Policy (CSP) is mostly turned off in dev mode
  ;               ::server/secure-headers {:content-security-policy-settings {:object-src "'none'"}}})
  ;       ;; Wire up interceptor chains
  ;       server/default-interceptors
  ;       server/dev-interceptors
  ;       server/create-server
  ;       server/start))
