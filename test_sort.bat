@echo off
javac Main.java

start cmd /c "java Main 127.0.0.1 8000 master 4"

start cmd /c "java Main 127.0.0.1 9000 0 127.0.0.1 8000 uns0 s0"
start cmd /c "java Main 127.0.0.1 9001 1 127.0.0.1 8000 uns1 s1"
start cmd /c "java Main 127.0.0.1 9002 2 127.0.0.1 8000 uns2 s2
start cmd /c "java Main 127.0.0.1 9003 3 127.0.0.1 8000 uns3 s3
