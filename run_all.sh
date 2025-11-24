#!/bin/bash

echo ""
echo "==============================================================="
echo "          STATIC RACE DETECTION — FULL PIPELINE RUN"
echo "==============================================================="
echo ""

# -------------------------------------------------------------
# 1. CLEAN BUILD FOLDER
# -------------------------------------------------------------
echo "[1] Cleaning build directory..."
rm -rf build
mkdir -p build
echo "✔ Build directory ready."
echo ""

# -------------------------------------------------------------
# 2. COMPILE ALL BANK BACKEND FILES
# -------------------------------------------------------------
echo "[2] Compiling backend (bank/) ..."

javac -cp "libs/*" \
    annotations/*.java \
    bank/account/*.java \
    bank/loan/*.java \
    bank/investment/*.java \
    bank/payment/*.java \
    bank/fraud/*.java \
    bank/audit/*.java \
    bank/Server.java \
    -d build

if [ $? -ne 0 ]; then
    echo "❌ Backend compilation FAILED."
    exit 1
fi

echo "✔ Backend compiled."
echo ""

# -------------------------------------------------------------
# 3. COMPILE PARSER
# -------------------------------------------------------------
echo "[3] Compiling parser/parser.java ..."

javac -cp "libs/*:build" parser/parser.java -d build

if [ $? -ne 0 ]; then
    echo "❌ Parser compilation FAILED."
    exit 1
fi

echo "✔ Parser compiled."
echo ""

# -------------------------------------------------------------
# 4. RUN PARSER → generate race_report.json
# -------------------------------------------------------------
echo "[4] Running parser to generate race_report.json ..."

cd build
java -cp ".:../libs/*" parser.parser
cd ..

if [ $? -ne 0 ]; then
    echo "❌ Parser execution FAILED."
    exit 1
fi

echo "✔ race_report.json generated inside /parser folder."
echo ""

# -------------------------------------------------------------
# 5. RUN PYTHON MODEL EVALUATOR
# -------------------------------------------------------------
echo "[5] Running Python evaluator ..."

python3 evaluator/evaluate_models.py

if [ $? -ne 0 ]; then
    echo "❌ Python evaluator FAILED."
    exit 1
fi

echo ""
echo "==============================================================="
echo " 🎉 FULL PIPELINE COMPLETED SUCCESSFULLY 🎉"
echo "==============================================================="
echo "Generated files:"
echo "  ➤ parser/race_report.json"
echo "  ➤ evaluator/model_comparison_recall.png"
echo "==============================================================="
echo ""
