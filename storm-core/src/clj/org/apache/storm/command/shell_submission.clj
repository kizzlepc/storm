;; Licensed to the Apache Software Foundation (ASF) under one
;; or more contributor license agreements.  See the NOTICE file
;; distributed with this work for additional information
;; regarding copyright ownership.  The ASF licenses this file
;; to you under the Apache License, Version 2.0 (the
;; "License"); you may not use this file except in compliance
;; with the License.  You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
(ns org.apache.storm.command.shell-submission
  (:import [org.apache.storm Config StormSubmitter]
           [org.apache.storm.utils ServerUtils]
           [org.apache.storm.zookeeper Zookeeper])
  (:use [org.apache.storm util config log])
  (:require [clojure.string :as str])
  (:import [org.apache.storm.callback DefaultWatcherCallBack]
           [org.apache.storm.utils ConfigUtils]
           [org.apache.storm.zookeeper Zookeeper ClientZookeeper])
  (:gen-class))


(defn -main [^String tmpjarpath & args]
  (let [conf (clojurify-structure (ConfigUtils/readStormConfig))
        servers (.get conf Config/STORM_ZOOKEEPER_SERVERS)
        port (.get conf Config/STORM_ZOOKEEPER_PORT)
        root (.get conf Config/STORM_ZOOKEEPER_ROOT)
        zk (ClientZookeeper/mkClient conf servers port root (DefaultWatcherCallBack.) conf)
        ; since this is not a purpose to add to leader lock queue, passing nil as blob-store and topo cache is ok
        zk-leader-elector (Zookeeper/zkLeaderElector conf zk nil nil nil nil)
        leader-nimbus (.getLeader zk-leader-elector)
        host (.getHost leader-nimbus)
        port (.getPort leader-nimbus)
        no-op (.close zk-leader-elector)
        jarpath (StormSubmitter/submitJar conf tmpjarpath)
        args (concat args [host port jarpath])]
    (ServerUtils/execCommand args)))
