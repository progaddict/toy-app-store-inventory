#!/bin/bash

curl -v -F 'file=@./products.csv' http://127.0.0.1:8080/api/v1/product/csv
