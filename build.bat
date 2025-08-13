@echo off
echo 正在构建Handheld3D mod...
echo.

echo 检查Java版本...
java -version
echo.

echo 开始构建...
gradlew.bat build

if %ERRORLEVEL% EQU 0 (
    echo.
    echo 构建成功！
    echo 生成的jar文件位于: build/libs/
) else (
    echo.
    echo 构建失败！
    pause
)
