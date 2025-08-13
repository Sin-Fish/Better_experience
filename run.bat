@echo off
echo 正在启动Minecraft开发环境...
echo.

echo 检查Java版本...
java -version
echo.

echo 启动Minecraft客户端...
gradlew.bat runClient

echo.
echo Minecraft已关闭。
pause
