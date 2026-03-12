@echo off
setlocal enabledelayedexpansion

echo.
echo Compiling app
echo.

if not exist "out" mkdir out

echo [*] Compiling Java files...
echo.

javac --module-path lib\javafx-sdk-21.0.2\lib ^
    --add-modules javafx.controls ^
    -d out -sourcepath src ^
    src\com\grafapp\Main.java ^
    src\com\grafapp\model\NodeState.java ^
    src\com\grafapp\model\EdgeState.java ^
    src\com\grafapp\model\GraphNode.java ^
    src\com\grafapp\model\GraphEdge.java ^
    src\com\grafapp\model\Graph.java ^
    src\com\grafapp\algorithm\ParameterInfo.java ^
    src\com\grafapp\algorithm\AlgorithmStep.java ^
    src\com\grafapp\algorithm\AlgorithmResult.java ^
    src\com\grafapp\algorithm\GraphAlgorithm.java ^
    src\com\grafapp\algorithm\AlgorithmRegistry.java ^
    src\com\grafapp\algorithm\impl\DFSAlgorithm.java ^
    src\com\grafapp\algorithm\impl\BFSAlgorithm.java ^
    src\com\grafapp\algorithm\impl\ConnectedComponentsAlgorithm.java ^
    src\com\grafapp\algorithm\impl\ConnectivityCheckAlgorithm.java ^
    src\com\grafapp\algorithm\impl\PathFinderAlgorithm.java ^
    src\com\grafapp\algorithm\impl\LargestComponentAlgorithm.java ^
    src\com\grafapp\algorithm\impl\BipartiteCheckAlgorithm.java ^
    src\com\grafapp\algorithm\impl\CycleDetectionAlgorithm.java ^
    src\com\grafapp\algorithm\impl\DiameterAlgorithm.java ^
    src\com\grafapp\algorithm\impl\GirthAlgorithm.java ^
    src\com\grafapp\layout\ForceDirectedLayout.java ^
    src\com\grafapp\visualization\GraphCanvas.java ^
    src\com\grafapp\visualization\SimulationController.java ^
    src\com\grafapp\ui\MainView.java ^
    src\com\grafapp\ui\AlgorithmSidebar.java ^
    src\com\grafapp\ui\ControlPanel.java ^
    src\com\grafapp\ui\ResultPanel.java ^
    src\com\grafapp\util\GraphParser.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Kompilasi GAGAL!
    pause
    exit /b 1
)

echo.
echo [OK] Kompilasi berhasil!
echo.
echo Jalankan dengan: launch.bat
pause
