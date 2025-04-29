#!/bin/bash
set -e

rm scenarios/vehApp/application/*.jar
cp ../NPR-2425/Network/OBU/target/*.jar scenarios/vehApp/application/
cp ../NPR-2425/Network/RSU/target/*.jar scenarios/vehApp/application/
cp ../NPR-2425/Network/FOG/target/*.jar scenarios/vehApp/application/