(ns leiningen.datomic
  (:require [conch.core :as sh])
  (:use [leinjacker.eval :only (eval-in-project)])
  (:import java.io.File))


(defn start
  "Start a Datomic instance as specified in project.clj."
  [root {:keys [install-location config db-uri test-data]}]
  (if config 
    (let [p (sh/proc "bin/transactor" (str root File/separator config)
                     :dir install-location)]
      (while true (try
                    (sh/stream-to-out p :out)
                    (catch Exception e (println (str e))))))
    (println "No Datomic config specified.")))

(defn schema-paths
  "Returns a vector of schema files as specified by [:datomic :schemas]"
  [project]
  (if-let [schemas (get-in project [:datomic :schemas])]
    (cond
     (string? schemas) (vector schemas)
     (vector? schemas) (apply concat
                              (for [dir-pair (partition 2 schemas)]
                                (let [[base-dir file-names] dir-pair 
                                      schema-dir (File. (str (:root project)
                                                             File/separator base-dir))]
                                  (for [file-name file-names]
                                    (.getAbsolutePath (File. schema-dir file-name)))))))
    []))

(defn generate-schema-load-code
  [project]
  (let [{:keys [db-uri init-data]} (:datomic project)
        conn (gensym "conn")
        code
        `(try
           (datomic.api/delete-database ~db-uri)
           (datomic.api/create-database ~db-uri)
           (let [~conn (datomic.api/connect ~db-uri)]
             ~@(for [schema-file (schema-paths project)]
                 `(do
                    (println ~(str "Loading " schema-file "..."))
                    (datomic.api/transact
                     ~conn
                     (read-string (slurp ~schema-file))))))

           (catch Exception e#
             (.printStackTrace e#))
           (finally
             ;;This exit is here because the datomic
             ;;calls spin up threads that are preventing
             ;;the process from ending on its own.

             (do (shutdown-agents)
                 (System/exit 0))))]
    code))

(defn initialize
  "Deletes, creates and repopulates the schema of the database."
  [project]
  (do
    (eval-in-project
     project
     (generate-schema-load-code project)
     `(require '[datomic.api]))
    (println "Database initialized.")))

(defn datomic
  "Various tasks related to using Datomic."
  {:subtasks [#'start #'initialize]}
  [project sub-task & args]
  (case sub-task
    "start" (start (:root project) (:datomic project))
    "initialize" (initialize project)))