(defproject visualloy "0.6.0-SNAPSHOT"
  :description "Computes heat transfer across a metal alloy and displays it
               graphically"
  :url "https://github.com/Rosnec/visualloy"
  :license {:name "GNU General Public License version 3",
            :url  "https://www.gnu.org/licenses/gpl-3.0.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [seesaw "1.4.4"]]
  :main visualloy.core
  :java-source-paths ["src/util/java/process"])
