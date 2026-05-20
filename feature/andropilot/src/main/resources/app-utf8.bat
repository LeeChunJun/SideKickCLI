@echo off
chcp 65001 > nul
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
%~dp0app.bat %*



@REM
@REM ## CMD 用户
@REM ## 设置控制台编码为 UTF-8
@REM chcp 65001
@REM

@REM
@REM ## powershell 用户
@REM ## 设置控制台编码为 UTF-8
@REM [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
@REM chcp 65001
@REM




