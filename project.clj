(defproject hellonico/speech-recognition "1.0.2"
  :description "Library to listen to audio input and interpret it to text."
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [net.sourceforge.javaflacencoder/java-flac-encoder "0.2.3"]
                 [clj-http "0.7.2"]
                 [fs "1.0.0"]
                 [org.clojure/data.json "0.1.1"]]
  :main speech-recognition.hear
  :repositories {"conjars" "http://conjars.org/repo/"}
)