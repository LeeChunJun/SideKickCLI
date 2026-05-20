#!/bin/bash

echo "查找所有 feature 模块..."
echo ""

for dir in feature/*/; do
    if [ -f "$dir/build.gradle.kts" ]; then
        MODULE_NAME=$(basename "$dir")
        echo "模块: $MODULE_NAME"

        # 检查是否有 main 函数
        if grep -r "fun main()" "$dir/src/jvmMain" >/dev/null 2>&1; then
            echo "  ✓ 有主函数"
            echo "  运行: ./gradlew :feature:$MODULE_NAME:run"
        else
            echo "  ✗ 没有主函数"
        fi
        echo ""
    fi
done