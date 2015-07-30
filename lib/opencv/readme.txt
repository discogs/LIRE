Using openCV with LIRe
======================

On Windows
----------

In order to use openCV library with LIRe, the java.library.path system property is needed to be specified as following (for IntelliJ IDEA):
In Tools -> Run/Debug Configuration -> Application -> VM options, enter:
-Djava.library.path=path/to/dll
e.g.: -Djava.library.path="lib\opencv"

On Mac OSX
----------

Follow the instructions on
http://blogs.wcode.org/2014/10/howto-install-build-and-use-opencv-macosx-10-10/

On othery platforms
-------------------

Follow the instructions on
http://opencv.org/quickstart.html


Current version: openCV 2.4.11 (x64)