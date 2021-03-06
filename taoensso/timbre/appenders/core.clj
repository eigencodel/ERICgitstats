(ns taoensso.timbre.appenders.core
  "Core Timbre appenders without any special dependency requirements.
  These can be aliased into the main Timbre ns for convenience."
  {:author "Peter Taoussanis (@ptaoussanis)"}
       
  (:require
   [clojure.string  :as str]
   [taoensso.encore :as enc :refer (have have? qb)])

        
           
                            
                                                        )

;; TODO Add a simple official rolling spit appender?

;;;; Println appender (clj & cljs)

      (enc/declare-remote taoensso.timbre/default-out
                          taoensso.timbre/default-err)
      (alias 'timbre 'taoensso.timbre)

     
(def ^:private ^:const system-newline
  (System/getProperty "line.separator"))

      (defn- atomic-println [x] (print (str x system-newline)) (flush))

(defn println-appender
  "Returns a simple `println` appender for Clojure/Script.
  Use with ClojureScript requires that `cljs.core/*print-fn*` be set.

  :stream (clj only) - e/o #{:auto :*out* :*err* :std-err :std-out <io-stream>}."

  ;; Unfortunately no easy way to check if *print-fn* is set. Metadata on the
  ;; default throwing fn would be nice...

  [&       [{:keys [stream] :or {stream :auto}}]               ]
  (let [      stream
              (case stream
                :std-err timbre/default-err
                :std-out timbre/default-out
                stream)]

    {:enabled?   true
     :async?     false
     :min-level  nil
     :rate-limit nil
     :output-fn  :inherit
     :fn
     (fn [data]
       (let [{:keys [output_]} data]
                                         
              
         (let [stream
               (case stream
                 :auto  (if (:error? data) *err* *out*)
                 :*out* *out*
                 :*err* *err*
                 stream)]

           (binding [*out* stream]
                    (atomic-println (force output_))
                                                    ))))}))

(comment (println-appender))

;;;; Spit appender (clj only)

     
(defn spit-appender
  "Returns a simple `spit` file appender for Clojure."
  [& [{:keys [fname] :or {fname "./timbre-spit.log"}}]]
  {:enabled?   true
   :async?     false
   :min-level  nil
   :rate-limit nil
   :output-fn  :inherit
   :fn
   (fn self [data]
     (let [{:keys [output_]} data]
       (try
         (spit fname (str (force output_) "\n") :append true)
         (catch java.io.IOException e
           (if (:__spit-appender/retry? data)
             (throw e) ; Unexpected error
             (let [_    (have? enc/nblank-str? fname)
                   file (java.io.File. ^String fname)
                   dir  (.getParentFile (.getCanonicalFile file))]

               (when-not (.exists dir) (.mkdirs dir))
               (self (assoc data :__spit-appender/retry? true))))))))})

(comment
  (spit-appender)
  (let [f (:fn (spit-appender))]
    (enc/qb 1000 (f {:output_ "boo"}))))

;;;; js/console appender (cljs only)

      
                      
                                                          

                                                                      
                                           
                            
                      

                                 

                                                                   
                                                                     
                                           

            
                   
                    
                  
                  
                       
      
                           
                                                                        
                        
                      
                
                          
                                         
                                         
                                        
                                        
                                         
                                         
                                         
                                

                 
                                                         

                                       
                                                                       
                         
                                     
                               
                               
                                 
                                                                       
                                                                              

                                                            
                                                                     

                       

(comment (console-appender))

;;;; Deprecated

                                                            

;;;;;;;;;;;; This file autogenerated from src/taoensso/timbre/appenders/core.cljx
