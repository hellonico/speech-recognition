# speech-recognition

Library to listen to audio input and interpret it to text.
This is a fork from: 

    https://github.com/klutometis/speech-recognition

# Usage

Add this to your project.clj:
   
    [hellonico/speech-recognition "1.0.1"]

Add to your ns:

    (use '[speech-recognition.hear :as hear])

Turn speech to text in a REPL:

    (binding [
        speech-recognition.hear/*language* "ja" 
        speech-recognition.hear/*sample-time* 2000] 
        (hear/hear))

This will wait for the sample time amount before analysis.
This will also use a different language as expected.
