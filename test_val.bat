@echo off
cd gensort
valsort ../s0
valsort ../s1
valsort ../s2
valsort ../s3
cd ..
type s0 > ss
type s1 >> ss
type s2 >> ss
type s3 >> ss
cd gensort
valsort ../ss
cd ..
