#!/bin/bash

if [ -z "$1" ]; then
    echo "用法: ./check-module.sh <module-name>"
    exit 1
fi

MODULE_PATH="feature/$1"

echo "检查模块: $1"
echo "========================"

# 检查必要文件
FILES=(
    "$MODULE_PATH/build.gradle.kts"
    "$MODULE_PATH/src/commonMain"
    "$MODULE_PATH/src/jvmMain"
)

for file in "${FILES[@]}"; do
    if [ -e "$file" ]; then
        echo "✓ $file 存在"
    else
        echo "✗ $file 缺失"
    fi
done

echo ""
echo "目录结构:"
tree -L 3 "$MODULE_PATH/src" 2>/dev/null || find "$MODULE_PATH/src" -type d | head -20