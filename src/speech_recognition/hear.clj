(ns speech-recognition.hear
  (:use [clojure.data.json :as json]
        [fs.core :as fs])
  (:require [clj-http.client :as client])
  (:import (javax.sound.sampled AudioFormat
                                AudioSystem
                                AudioInputStream
                                AudioFileFormat
                                AudioFileFormat$Type)
           (javaFlacEncoder FLAC_FileEncoder
                            StreamConfiguration)))


(def ^:dynamic *input-index* 
  "Default index of the recording device; NB: this is a hack."
  2)

(def ^:dynamic *sample-rate* 44000)

(def ^:dynamic *language* "ja-JP")

(def ^:dynamic *google-url*
  "https://www.google.com/speech-api/v1/recognize?xjerr=1&client=chromium&lang=")

(def ^:dynamic *sample-size* 16)

(def ^:dynamic *sample-time* 5000) ; 5seconds

(def ^:dynamic *channels* 1)

(def ^:dynamic *signed* true)

(def ^:dynamic *big-endian* false)

(def ^:dynamic *format*
  (new AudioFormat
       *sample-rate*
       *sample-size*
       *channels*
       *signed*
       *big-endian*))

(defn post-to-google [flac]
  (:body
      (client/post
       (str *google-url* *language*)
       {
        :headers {"Content-type" (format "audio/x-flac; rate=%s" *sample-rate*)}
        :body (clojure.java.io/input-stream flac)})))

(defn sort-hypotheses [hypotheses]
  (sort-by (fn [hypothesis]
                 (let [{utterance :utterance confidence :confidence}
                       hypothesis]
                   confidence))
              >
              hypotheses))

(defn parse-response [response]
  ; (println response)
  (let [{status :status
         id :id
         hypotheses :hypotheses}
        (json/read-json response)
        {utterance :utterance
         confidence :confidence}
        (first (sort-hypotheses hypotheses))]
    utterance))

(defn hear []
  (let [mixer-info (clojure.core/get (javax.sound.sampled.AudioSystem/getMixerInfo) *input-index*)
        target (AudioSystem/getTargetDataLine *format* mixer-info)]
    ;; `with-open'?
    (.open target *format*)
    (.start target)
    (println "I'm listening in " *language* " for " *sample-time* " ms." )
    (.start (Thread.
             (fn []
                (Thread/sleep *sample-time*)
                (.flush target)
                (.stop target)
                (.close target)
                (println "|"))))
    (let [input-stream (new AudioInputStream target)]
      (let [wave (fs/temp-file "hear" ".wav")
            flac (fs/temp-file "hear" ".flac")]
        (AudioSystem/write input-stream
                           AudioFileFormat$Type/WAVE
                           wave)
        (let [encoder (FLAC_FileEncoder.)]
          ; (.setStreamConfig encoder
          ;                   (new StreamConfiguration
          ;                        *channels*
          ;                        StreamConfiguration/DEFAULT_MIN_BLOCK_SIZE
          ;                        StreamConfiguration/DEFAULT_MAX_BLOCK_SIZE
          ;                        *sample-rate*
          ;                        *sample-size*))
          (.encode encoder wave flac)
          (parse-response (post-to-google flac))
            )))))

(defn -main [& args]
  (if args
   (do 
    (println "Binding to language " (first args))
    (binding [*language* (first args)]
    (hear)))
   (hear)))