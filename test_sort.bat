@echo off
javac Main.java

start java Main 127.0.0.1 8000 master 4

start java Main 127.0.0.1 9000 127.0.0.1 8000 uns0 s0
start java Main 127.0.0.1 9001 127.0.0.1 8000 uns1 s1
start java Main 127.0.0.1 9002 127.0.0.1 8000 uns2 s2
start java Main 127.0.0.1 9003 127.0.0.1 8000 uns3 s3

